package com.cloudjay.cjay.util;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.androidannotations.annotations.EBean;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.AuditReportImage;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.greenrobot.event.EventBus;

@EBean
public class Mapper {

	private static IDatabaseManager databaseManager = null;
	private static Mapper instance = null;

	public static Mapper getInstance() {
		if (instance == null) {
			instance = new Mapper();
		}

		return instance;
	}

	public Mapper() {
		if (null == databaseManager) {
			databaseManager = CJayClient.getInstance().getDatabaseManager();
		}
	}

	/**
	 * 
	 * Sử dụng để convert data lấy từ server thành local data
	 * 
	 * @param tmpSession
	 * @param ctx
	 * @return
	 */
	public synchronized ContainerSession toContainerSession(TmpContainerSession tmpSession, Context ctx) {

		try {

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager.getHelper(ctx)
																				.getContainerSessionDaoImpl();
			SQLiteDatabase db = databaseManager.getHelper(ctx).getWritableDatabase();

			String operatorCode = tmpSession.getOperatorCode();
			String depotCode = tmpSession.getDepotCode();

			long operatorId = -1;
			Cursor cursor = db.rawQuery("select * from operator where operator_code = ?", new String[] { operatorCode });
			if (cursor.moveToFirst()) {
				operatorId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
			} else {
				ContentValues values = new ContentValues();
				values.put(Operator.FIELD_CODE, operatorCode);
				values.put(Operator.FIELD_NAME, operatorCode);
				operatorId = db.insertWithOnConflict("operator", null, values, SQLiteDatabase.CONFLICT_REPLACE);
				// Logger.Log("Create new Operator: " + operatorCode);
			}

			long depotId = -1;
			cursor = db.rawQuery(	"select id as _id, depot_code, depot_name from depot where depot_code = ?",
									new String[] { depotCode });
			if (cursor.moveToFirst()) {
				depotId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
			} else {
				ContentValues values = new ContentValues();
				values.put(Depot.DEPOT_CODE, depotCode);
				values.put(Depot.DEPOT_NAME, depotCode);
				depotId = db.insertWithOnConflict("depot", null, values, SQLiteDatabase.CONFLICT_REPLACE);
				// Logger.Log("Create new depot: " + depotCode);
			}

			long containerId = -1;
			cursor = db.rawQuery(	"select * from container where container_id = ?",
									new String[] { tmpSession.getContainerId() });
			if (cursor.moveToFirst()) {
				containerId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
			} else {

				ContentValues values = new ContentValues();
				values.put(Container.CONTAINER_ID, tmpSession.getContainerId());
				values.put("operator_id", operatorId);
				values.put("depot_id", depotId);
				containerId = db.insertWithOnConflict("container", null, values, SQLiteDatabase.CONFLICT_REPLACE);
				// Logger.Log("Create new container: " + tmpSession.getContainerId());
			}

			// Create `container session` object
			String uuid = UUID.randomUUID().toString();

			ContentValues csValues = new ContentValues();
			csValues.put("check_in_time", tmpSession.getCheckInTime());
			csValues.put("check_out_time", tmpSession.getCheckOutTime());
			csValues.put("_id", uuid);
			csValues.put("container_id", containerId);
			csValues.put("image_id_path", tmpSession.getImageIdPath());
			csValues.put("id", tmpSession.getId());
			csValues.put("server_state", tmpSession.getStatus());

			// TODO: Server do not return is_available
			// csValues.put("is_available", tmpSession.getIsAvailable());

			db.insertWithOnConflict("container_session", null, csValues, SQLiteDatabase.CONFLICT_REPLACE);

			// process AuditReportItems --> create issues
			List<AuditReportItem> auditReportItems = tmpSession.getAuditReportItems();
			if (null != auditReportItems) {
				for (AuditReportItem auditReportItem : auditReportItems) {

					String issueUuid = UUID.randomUUID().toString();
					ContentValues values = new ContentValues();
					values.put("id", auditReportItem.getId());
					values.put("_id", issueUuid);
					values.put("componentCode_id", auditReportItem.getComponentId());
					values.put("damageCode_id", auditReportItem.getDamageId());
					values.put("repairCode_id", auditReportItem.getRepairId());
					values.put("locationCode", auditReportItem.getLocationCode());
					values.put("containerSession_id", uuid);
					values.put("quantity", auditReportItem.getQuantity());
					values.put("length", auditReportItem.getLength());
					values.put("height", auditReportItem.getHeight());
					db.insertWithOnConflict("issue", null, values, SQLiteDatabase.CONFLICT_REPLACE);

					List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
					for (AuditReportImage image : auditReportImages) {
						ContentValues imageValues = new ContentValues();
						imageValues.put("id", image.getId());
						imageValues.put("_id", image.getImageUrl());
						imageValues.put("uuid", UUID.randomUUID().toString());
						imageValues.put("issue_id", issueUuid);
						imageValues.put("type", image.getType());
						imageValues.put("containerSession_id", uuid);
						imageValues.put("image_name", image.getImageName());
						imageValues.put("time_posted", image.getCreatedAt());
						db.insertWithOnConflict("cjay_image", null, imageValues, SQLiteDatabase.CONFLICT_REPLACE);
					}
				}
			}

			List<GateReportImage> gateReportImages = tmpSession.getGateReportImages();
			for (GateReportImage image : gateReportImages) {
				ContentValues values = new ContentValues();
				values.put("id", image.getId());
				values.put("_id", image.getImageUrl());
				values.put("uuid", UUID.randomUUID().toString());
				values.put("type", image.getType());
				values.put("containerSession_id", uuid);
				values.put("image_name", image.getImageName());
				values.put("time_posted", image.getCreatedAt());
				db.insertWithOnConflict("cjay_image", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}

			ContainerSession containerSession = containerSessionDaoImpl.queryForId(uuid);
			if (containerSession == null) {
				Logger.e("Cannot find container " + tmpSession.getContainerId() + " after conversion");
			}

			return containerSession;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public TmpContainerSession toTmpContainerSession(Context ctx, ContainerSession containerSession,
														boolean officialUpload) {

		String containerId = containerSession.getContainerId();
		String checkoutTime = containerSession.getRawCheckOutTime();
		String imageIdPath = containerSession.getImageIdPath();

		TmpContainerSession tmpContainerSession = new TmpContainerSession();

		tmpContainerSession.setId(containerSession.getId());
		tmpContainerSession.setOperatorCode(containerSession.getOperatorCode());
		tmpContainerSession.setOperatorId(containerSession.getOperatorId());
		tmpContainerSession.setDepotCode(containerSession.getContainer().getDepot().getDepotCode());
		tmpContainerSession.setContainerId(containerId);
		tmpContainerSession.setCheckInTime(containerSession.getRawCheckInTime());
		tmpContainerSession.setIsAvailable(containerSession.isAvailable());

		if (TextUtils.isEmpty(checkoutTime)) {
			Logger.e(containerId + " | Checkout Time is NULL");
		}
		tmpContainerSession.setCheckOutTime(checkoutTime);

		if (officialUpload) {

			if (TextUtils.isEmpty(imageIdPath)) {
				Logger.e(containerId + " | Image Id Path is NULL");
			}

			tmpContainerSession.setImageIdPath(imageIdPath);

			// Create `gate_report_images`
			List<GateReportImage> gateReportImages = new ArrayList<GateReportImage>();
			Collection<CJayImage> cJayImages = containerSession.getCJayImages();
			for (CJayImage cJayImage : cJayImages) {

				if (cJayImage.getType() == CJayImage.TYPE_IMPORT || cJayImage.getType() == CJayImage.TYPE_EXPORT) {

					GateReportImage gateImage;
					if (cJayImage.getId() != 0) {
						gateImage = new GateReportImage(cJayImage.getId(), cJayImage.getType(),
														cJayImage.getTimePosted(), cJayImage.getImageName(),
														cJayImage.getUri());

					} else {
						gateImage = new GateReportImage(cJayImage.getType(), cJayImage.getTimePosted(),
														cJayImage.getImageName(), cJayImage.getUri());
					}
					gateReportImages.add(gateImage);
				}

			}
			tmpContainerSession.setGateReportImages(gateReportImages);

			// Re-set image_id_path
			if (TextUtils.isEmpty(tmpContainerSession.getImageIdPath()) && gateReportImages.isEmpty() == false) {
				tmpContainerSession.setImageIdPath(gateReportImages.get(0).getImageName());
			}

			// Create `audit_report_items`
			List<AuditReportItem> auditReportItems = new ArrayList<AuditReportItem>();
			Collection<Issue> issues = containerSession.getIssues();
			if (null != issues) {
				for (Issue issue : issues) {
					auditReportItems.add(new AuditReportItem(issue));
				}
			}
			tmpContainerSession.setAuditReportItems(auditReportItems);

			// Set container Id Image
			// TODO: only handle for app Auditor
			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT && cJayImage.getIssue() == null) {
					Logger.Log("Container Id Image: " + cJayImage.getImageName());
					tmpContainerSession.setContainerIdImage(cJayImage.getImageName());
					break;
				}

			}
		}

		return tmpContainerSession;

	}

	public TmpContainerSession toTmpContainerSession(Context ctx, ContainerSession containerSession) {
		return toTmpContainerSession(ctx, containerSession, true);
	}

	public synchronized void update(Context ctx, TmpContainerSession tmp, String uuid) {

		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager.getHelper(ctx)
																				.getContainerSessionDaoImpl();

			ContainerSession main = containerSessionDaoImpl.queryForId(uuid);
			update(ctx, tmp, main, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void update(Context ctx, TmpContainerSession tmp, ContainerSession main,
									boolean updateImageIdPath) throws Exception {

		try {
			if (null != tmp) {

				SQLiteDatabase db = databaseManager.getHelper(ctx).getWritableDatabase();

				// Update GateReportImages
				List<GateReportImage> gateReportImages = tmp.getGateReportImages();

				if (gateReportImages != null) {
					for (GateReportImage gateReportImage : gateReportImages) {

						String gateImageName = gateReportImage.getImageName();
						String sql = "SELECT * FROM cjay_image WHERE image_name LIKE ?";
						Cursor cursor = db.rawQuery(sql, new String[] { "%" + gateImageName });

						// existed
						if (cursor.moveToFirst()) { // update

							String uuid = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
							String imageName = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_IMAGE_NAME));
							sql = "UPDATE cjay_image SET id = " + gateReportImage.getId() + " WHERE uuid = '" + uuid
									+ "'";

							db.execSQL(sql);
							Logger.Log("Update CJayImage UUID: " + uuid + " | Image name: " + imageName);

						} else { // create

							String uuid = UUID.randomUUID().toString();
							sql = "INSERT INTO cjay_image VALUES ('" + main.getUuid() + "', '" + uuid + "', '"
									+ gateReportImage.getImageName() + "', NULL, '" + gateReportImage.getCreatedAt()
									+ "', '" + gateReportImage.getImageUrl() + "', 4," + gateReportImage.getType()
									+ ", " + gateReportImage.getId() + ")";

							db.execSQL(sql);
							Logger.Log("Create new CJayImage: " + gateReportImage.getImageName());

						}
					}
				}

				// Update AuditReportItems
				List<AuditReportItem> auditReportItems = tmp.getAuditReportItems();
				if (auditReportItems != null) {
					for (AuditReportItem auditReportItem : auditReportItems) {

						int componentId = auditReportItem.getComponentId();
						int damageId = auditReportItem.getDamageId();
						int repairId = auditReportItem.getRepairId();

						String sql = "select * from issue where componentCode_id = " + componentId
								+ " and damageCode_id = " + damageId + " and repairCode_id = " + repairId
								+ " and locationCode LIKE ? and containerSession_id = ?";

						Cursor cursor = db.rawQuery(sql,
													new String[] { auditReportItem.getLocationCode(), main.getUuid() });

						if (cursor.moveToFirst()) { // existed

							// update issue_id
							String uuid = cursor.getString(cursor.getColumnIndexOrThrow(Issue.FIELD_UUID));
							sql = "UPDATE issue SET id = " + auditReportItem.getId() + " WHERE _id = '" + uuid + "'";
							db.execSQL(sql);
							Logger.Log("Update Issue with id: " + auditReportItem.getId());

							// update audit report images
							List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
							if (auditReportImages != null) {
								for (AuditReportImage auditReportImage : auditReportImages) {

									String auditReportImageName = auditReportImage.getImageName();
									sql = "SELECT * FROM cjay_image WHERE image_name LIKE ? ";
									Cursor auditCursor = db.rawQuery(sql, new String[] { "%" + auditReportImageName });

									// existed
									if (auditCursor.moveToFirst()) { // update

										String auditImageUuid = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
										String imageName = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_IMAGE_NAME));

										sql = "UPDATE cjay_image SET id = " + auditReportImage.getId()
												+ " WHERE uuid = '" + auditImageUuid + "'";
										db.execSQL(sql);
										Logger.Log("Update CJayImage UUID: " + auditImageUuid + " | Image name: "
												+ imageName);

									} else { // create

										SimpleDateFormat dateFormat = new SimpleDateFormat(
																							CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
										String nowString = dateFormat.format(new Date());

										String auditImageUuid = UUID.randomUUID().toString();
										sql = "INSERT INTO cjay_image VALUES ('" + main.getUuid() + "', '"
												+ auditImageUuid + "', '" + auditReportImage.getImageName() + "', '"
												+ uuid + "', '" + nowString + "', '" + auditReportImage.getImageUrl()
												+ "', 4," + auditReportImage.getType() + ", "
												+ auditReportImage.getId() + ")";

										db.execSQL(sql);
										Logger.Log("Create new CJayImage based on AuditReportImage");

									}
								}
							}

						} else { // create
							Logger.Log("Create new Issue object");
							List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
							if (auditReportImages != null) {

								String height = "NULL";
								String length = "NULL";

								if (!TextUtils.isEmpty(auditReportItem.getHeight())) {
									height = auditReportItem.getHeight();
								}

								if (!TextUtils.isEmpty(auditReportItem.getLength())) {
									length = auditReportItem.getLength();
								}

								String issueUuid = UUID.randomUUID().toString();
								sql = "INSERT INTO issue VALUES (" + componentId + ", '" + main.getUuid() + "', "
										+ damageId + ", '" + issueUuid + "', " + height + ", '" + repairId + "', "
										+ length + ", '" + auditReportItem.getLocationCode() + "', "
										+ auditReportItem.getQuantity() + ", " + auditReportItem.getId() + ", 0)";

								db.execSQL(sql);

								// update audit report images
								List<AuditReportImage> auditReportImages1 = auditReportItem.getAuditReportImages();
								if (auditReportImages1 != null) {
									for (AuditReportImage auditReportImage : auditReportImages1) {

										String auditReportImageName = auditReportImage.getImageName();
										sql = "SELECT * FROM cjay_image WHERE image_name LIKE ?";
										Cursor auditCursor = db.rawQuery(	sql,
																			new String[] { "%" + auditReportImageName });

										// existed
										if (cursor.moveToFirst()) { // update

											String auditImageUuid = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
											String imageName = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_IMAGE_NAME));

											sql = "UPDATE cjay_image SET id = " + auditReportImage.getId()
													+ " WHERE uuid = '" + auditImageUuid + "'";
											db.execSQL(sql);
											Logger.Log("Update CJayImage UUID: " + auditImageUuid + " | Image name: "
													+ imageName);

										} else { // create

											SimpleDateFormat dateFormat = new SimpleDateFormat(
																								CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE);
											String nowString = dateFormat.format(new Date());

											String auditImageUuid = UUID.randomUUID().toString();
											sql = "INSERT INTO cjay_image VALUES ('" + main.getUuid() + "', '"
													+ auditImageUuid + "', '" + auditReportImage.getImageName()
													+ "', '" + issueUuid + "', '" + nowString + "', '"
													+ auditReportImage.getImageUrl() + "', 4,"
													+ auditReportImage.getType() + ", " + auditReportImage.getId()
													+ ")";

											db.execSQL(sql);
											Logger.Log("Create new CJayImage based on AuditReportImage");

										}
									}
								}
							}
						}
					}
				} else {
					Logger.e("AuditReportItems is NULL");
				}

				String sqlString = "";
				if (updateImageIdPath) {
					if (!TextUtils.isEmpty(tmp.getImageIdPath())
							&& !tmp.getImageIdPath()
									.matches("^https://storage\\.googleapis\\.com/storage-cjay\\.cloudjay\\.com/\\s+$")) {

						sqlString = "UPDATE container_session SET id = " + tmp.getId() + ", check_in_time = '"
								+ tmp.getCheckInTime() + "', image_id_path = '" + tmp.getImageIdPath()
								+ "', server_state = " + tmp.getStatus() + " WHERE _id = '" + main.getUuid() + "'";
					}
				} else {
					sqlString = "UPDATE container_session SET id = " + tmp.getId() + ", check_in_time = '"
							+ tmp.getCheckInTime() + "', server_state = " + tmp.getStatus() + " WHERE _id = '"
							+ main.getUuid() + "'";
				}

				db.execSQL(sqlString);

				// PreferencesUtil.storePrefsValue(ctx, PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
				// tmp.getCheckInTime());

				// Post ContainerSessionUpdatedEvent
				EventBus.getDefault().post(new ContainerSessionChangedEvent());
			}
		} catch (Exception e) {
			throw e;
		}

	}

	public synchronized void update(Context ctx, String jsonString, ContainerSession main) throws Exception {

		TmpContainerSession tmp = null;
		Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();
		Type listType = new TypeToken<TmpContainerSession>() {
		}.getType();

		try {
			tmp = gson.fromJson(jsonString, listType);
		} catch (Exception e) {
			Logger.Log("jsonString is on wrong format");
			e.printStackTrace();
			return;
		}

		Logger.Log("ContainerSession is already existed. Prepare to update.");
		update(ctx, tmp, main, false);
		// update(ctx, jsonString, main, false);
	}

	@Deprecated
	public synchronized void
			update(Context ctx, String jsonString, ContainerSession main, boolean updateImageIdPath) throws Exception {

		try {

			TmpContainerSession tmp = null;
			Gson gson = new GsonBuilder().setDateFormat(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE).create();

			Type listType = new TypeToken<TmpContainerSession>() {
			}.getType();

			try {
				tmp = gson.fromJson(jsonString, listType);
			} catch (Exception e) {
				Logger.Log("jsonString is on wrong format");
				e.printStackTrace();
				return;
			}

			if (null != tmp) {
				CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(ctx).getCJayImageDaoImpl();
				IssueDaoImpl issueDaoImpl = databaseManager.getHelper(ctx).getIssueDaoImpl();

				// Uodate imageIdPath
				main.setId(tmp.getId());
				if (updateImageIdPath) {
					if (!TextUtils.isEmpty(tmp.getImageIdPath())
							&& !tmp.getImageIdPath()
									.equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {
						main.setImageIdPath(tmp.getImageIdPath());
					}

				}
				main.setCheckInTime(tmp.getCheckInTime());

				PreferencesUtil.storePrefsValue(ctx, PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
												tmp.getCheckInTime());

				// Update GateReportImages
				List<GateReportImage> gateReportImages = tmp.getGateReportImages();
				Collection<CJayImage> cJayImages = main.getCJayImages();

				if (gateReportImages != null) {
					for (GateReportImage gateReportImage : gateReportImages) {
						for (CJayImage cJayImage : cJayImages) {

							String gateReportImageName = gateReportImage.getImageName();
							String cJayImageName = cJayImage.getImageName();

							if (gateReportImageName.contains(cJayImageName)) {
								cJayImage.setId(gateReportImage.getId());
								cJayImage.setImageName(gateReportImageName);
								cJayImageDaoImpl.update(cJayImage);
								break;
							}
						}
					}
				}

				// Update AuditReportItems
				List<AuditReportItem> auditReportItems = tmp.getAuditReportItems();
				Collection<Issue> issues = main.getIssues();

				if (auditReportItems != null) {
					for (AuditReportItem auditReportItem : auditReportItems) {
						for (Issue issue : issues) {

							if (issue.equals(auditReportItem)) {
								issue.setId(auditReportItem.getId());

								List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
								Collection<CJayImage> issueImages = issue.getCJayImages();

								for (AuditReportImage auditReportImage : auditReportImages) {
									for (CJayImage cJayImage : issueImages) {

										String auditReportImageName = auditReportImage.getImageName();
										String cJayImageName = cJayImage.getImageName();
										if (auditReportImageName.contains(cJayImageName)) {

											cJayImage.setId(auditReportImage.getId());
											cJayImage.setImageName(auditReportImageName);
											cJayImageDaoImpl.update(cJayImage);
											break;
										}
									}
								}

								issueDaoImpl.update(issue);
								break;
							}
						}
					}
				}

				// Post ContainerSessionUpdatedEvent

			}
		} catch (SQLException e) {
			throw e;

		} catch (Exception e) {
			throw e;
		}

	}

}
