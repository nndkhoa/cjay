package com.cloudjay.cjay.util;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.androidannotations.annotations.EBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.Toast;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionChangedEvent;
import com.cloudjay.cjay.model.AuditReportImage;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.Operator;
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
			db.insertWithOnConflict("container_session", null, csValues, SQLiteDatabase.CONFLICT_REPLACE);

			// process AuditReportItems --> create issues
			List<AuditReportItem> auditReportItems = tmpSession.getAuditReportItems();
			if (null != auditReportItems) {
				for (AuditReportItem auditReportItem : auditReportItems) {

					String issueUuid = UUID.randomUUID().toString();

					DataCenter.getInstance().addIssue(ctx, auditReportItem, auditReportItem.getId(), issueUuid, uuid);

					List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
					for (AuditReportImage image : auditReportImages) {
						DataCenter.getInstance().addImage(ctx, image, image.getId(), UUID.randomUUID().toString(),
															issueUuid, uuid);
					}
				}
			}

			List<GateReportImage> gateReportImages = tmpSession.getGateReportImages();
			for (GateReportImage image : gateReportImages) {
				DataCenter.getInstance().addImage(ctx, image, image.getId(), UUID.randomUUID().toString(), uuid);
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
														boolean officialUpload) throws NullSessionException {

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

			CJaySession session = CJaySession.restore(ctx);
			if (session == null) throw new NullSessionException();

			// only convert if user role != gate
			// NOTE: xảy ra lỗi khi deploy vào đợt đầu tháng 5/2014, không new được audit_report_items
			// < do thiếu hình giám định >
			if (session.getUserRole() != UserRole.GATE_KEEPER.getValue()) {

				// Create `audit_report_items`
				List<AuditReportItem> auditReportItems = new ArrayList<AuditReportItem>();
				Collection<Issue> issues = containerSession.getIssues();
				if (null != issues) {
					for (Issue issue : issues) {
						auditReportItems.add(new AuditReportItem(issue));
					}
				}
				tmpContainerSession.setAuditReportItems(auditReportItems);
			}

			// Set container Id Image
			// TODO: only handle for app Auditor
			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getType() == CJayImage.TYPE_AUDIT && cJayImage.getIssue() == null) {
					Logger.Log("Container Id Image: " + cJayImage.getImageName());
					tmpContainerSession.setContainerIdImage(cJayImage.getImageName());
					break;
				}

			}
		}

		return tmpContainerSession;

	}

	public TmpContainerSession
			toTmpContainerSession(Context ctx, ContainerSession containerSession) throws NullSessionException {
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
						String sql = "SELECT * FROM cjay_image WHERE image_name LIKE ? and type = ?";
						Cursor cursor = db.rawQuery(sql,
													new String[] { "%" + gateImageName,
															Integer.toString(gateReportImage.getType()) });

						// existed
						if (cursor.moveToFirst()) { // update

							String uuid = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
							String imageName = cursor.getString(cursor.getColumnIndexOrThrow(CJayImage.FIELD_IMAGE_NAME));
							QueryHelper.update(	ctx, "cjay_image", "id", Integer.toString(gateReportImage.getId()),
												"uuid = " + Utils.sqlString(uuid));
							Logger.Log("Update CJayImage UUID: " + uuid + " | Image name: " + imageName);

						} else { // create

							DataCenter.getInstance().addImage(ctx, gateReportImage, gateReportImage.getId(),
																UUID.randomUUID().toString(), main.getUuid());
							Logger.Log("Create new CJayImage: " + gateReportImage.getImageName());

						}
					}
				}

				// Update AuditReportItems
				List<AuditReportItem> auditReportItems = tmp.getAuditReportItems();
				if (auditReportItems != null) {
					for (AuditReportItem auditReportItem : auditReportItems) {

						//
						int itemId = auditReportItem.getId();
						int componentId = auditReportItem.getComponentId();
						int damageId = auditReportItem.getDamageId();
						int repairId = auditReportItem.getRepairId();
						String issueId = "";

						String sql = "select * from issue where id = ?";
						Cursor cursor = db.rawQuery(sql, new String[] { Integer.toString(itemId) });

						// issue existed inside db with id # 0; --> REPAIR received
						if (cursor.moveToFirst()) {

							issueId = cursor.getString(cursor.getColumnIndexOrThrow(Issue.FIELD_UUID));
							DataCenter.getInstance().addIssue(ctx, auditReportItem, auditReportItem.getId(), issueId,
																main.getUuid());
							Logger.Log("Update Issue with id: " + auditReportItem.getId());

						} else {

							CJaySession session = CJaySession.restore(ctx);
							if (session == null) throw new NullSessionException();
							UserRole userRole = UserRole.values()[session.getUserRole()];

							switch (userRole) {
								case AUDITOR:

									// AUDIT received
									// issue existed with id = 0 --> update id
									// find that issue based on codes then update issue info

									String issueSql = "select * from issue where componentCode_id = " + componentId
											+ " and damageCode_id = " + damageId + " and repairCode_id = " + repairId
											+ " and locationCode LIKE ? and containerSession_id = ?";

									Cursor issueCursor = db.rawQuery(	issueSql,
																		new String[] {
																				auditReportItem.getLocationCode(),
																				main.getUuid() });
									if (issueCursor.moveToFirst()) {
										issueId = issueCursor.getString(issueCursor.getColumnIndexOrThrow(Issue.FIELD_UUID));
										QueryHelper.update(	ctx, "issue", "id",
															Integer.toString(auditReportItem.getId()),
															"_id = " + Utils.sqlString(issueId));
										Logger.Log("Update Issue with id: " + auditReportItem.getId() + " | " + issueId);
									} else {
										Toast.makeText(ctx, "Unexpected Exception", Toast.LENGTH_LONG).show();
									}
									break;

								case REPAIR_STAFF:
								case GATE_KEEPER:

									// REPAIR received --> create new issue
									// issue didnt exist in db
									issueId = UUID.randomUUID().toString();
									DataCenter.getInstance().addIssue(ctx, auditReportItem, auditReportItem.getId(),
																		issueId, main.getUuid());
									Logger.Log("Add issue: " + auditReportItem.getId() + " | " + issueId);
									break;

								default:
									Logger.e("Other role!!");
									break;
							}

						}

						if (TextUtils.isEmpty(issueId)) {
							Logger.e("Error #parse audit_report_item with id: " + auditReportItem.getId());
							DataCenter.getDatabaseHelper(ctx).addUsageLog(	"Error #parse audit_report_item with id: "
																					+ auditReportItem.getId());
							break;
						}

						// ----
						// update AuditReportImages
						List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
						if (auditReportImages != null) {
							for (AuditReportImage auditReportImage : auditReportImages) {

								String auditReportImageName = auditReportImage.getImageName();
								sql = "SELECT * FROM cjay_image WHERE image_name LIKE ? and type = ?";
								Cursor auditCursor = db.rawQuery(sql, new String[] { "%" + auditReportImageName,
										Integer.toString(auditReportImage.getType()) });

								// existed
								if (auditCursor.moveToFirst()) { // update

									String auditImageUuid = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_UUID));
									String imageName = auditCursor.getString(auditCursor.getColumnIndexOrThrow(CJayImage.FIELD_IMAGE_NAME));
									QueryHelper.update(	ctx, "cjay_image", "id",
														Integer.toString(auditReportImage.getId()),
														"uuid = " + Utils.sqlString(auditImageUuid));

									Logger.Log("Update CJayImage UUID: " + auditImageUuid + " | Image name: "
											+ imageName);

								} else { // create
									DataCenter.getInstance().addImage(ctx, auditReportImage, auditReportImage.getId(),
																		UUID.randomUUID().toString(), issueId,
																		main.getUuid());

									Logger.Log("create new image");
								}
							}
						} // end audit report images

					}
				} else {
					Logger.e("AuditReportItems is NULL");
				}

				// Update other fields
				String sqlString = "";

				// update operator
				if (!tmp.getOperatorCode().equals(main.getOperatorCode())) {

					sqlString = "UPDATE container SET operator_id = " + tmp.getOperatorId() + " WHERE container_id = "
							+ Utils.sqlString(main.getContainerId());

					db.execSQL(sqlString);
					Logger.Log("Update operator from " + main.getOperatorCode() + " to " + tmp.getOperatorCode());

				}

				// update container_id
				if (!tmp.getContainerId().equals(main.getContainerId())) {
					sqlString = "UPDATE container SET container_id = " + Utils.sqlString(tmp.getContainerId())
							+ " WHERE container_id = " + Utils.sqlString(main.getContainerId());
					db.execSQL(sqlString);

					Logger.Log("Update container_id from " + main.getContainerId() + " to " + tmp.getContainerId());
				}

				// ContentValues csValues = new ContentValues();
				// csValues.put("check_in_time", tmp.getCheckInTime());
				// csValues.put("check_out_time", tmp.getCheckOutTime());
				// csValues.put("_id", main.getUuid());
				// csValues.put("id", tmp.getId());
				// csValues.put("server_state", tmp.getStatus());

				if (updateImageIdPath) {
					if (!TextUtils.isEmpty(tmp.getImageIdPath())
							&& !tmp.getImageIdPath()
									.matches("^https://storage\\.googleapis\\.com/storage-cjay\\.cloudjay\\.com/\\s+$")) {

						sqlString = "UPDATE container_session SET id = " + tmp.getId() + ", check_in_time = '"
								+ tmp.getCheckInTime() + "', image_id_path = '" + tmp.getImageIdPath()
								+ "', server_state = " + tmp.getStatus() + " WHERE _id = '" + main.getUuid() + "'";

						// csValues.put("image_id_path", tmp.getImageIdPath());
					}
				} else {
					sqlString = "UPDATE container_session SET id = " + tmp.getId() + ", check_in_time = '"
							+ tmp.getCheckInTime() + "', server_state = " + tmp.getStatus() + " WHERE _id = '"
							+ main.getUuid() + "'";
				}

				db.execSQL(sqlString);
				// db.insertWithOnConflict("container_session", null, csValues, SQLiteDatabase.CONFLICT_REPLACE);

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
