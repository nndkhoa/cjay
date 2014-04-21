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
import org.droidparts.contract.DB;

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

			DepotDaoImpl depotDaoImpl = databaseManager.getHelper(ctx).getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx).getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx).getContainerDaoImpl();
			CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(ctx).getCJayImageDaoImpl();
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager.getHelper(ctx).getDamageCodeDaoImpl();
			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager.getHelper(ctx).getRepairCodeDaoImpl();
			ComponentCodeDaoImpl componentCodeDaoImpl = databaseManager.getHelper(ctx).getComponentCodeDaoImpl();
			IssueDaoImpl issueDaoImpl = databaseManager.getHelper(ctx).getIssueDaoImpl();

			String operatorCode = tmpSession.getOperatorCode();
			String containerId = tmpSession.getContainerId();

			Operator operator = null;
			if (TextUtils.isEmpty(operatorCode)) {
				Logger.e("Container " + containerId + " does not have Operator");
			} else {
				List<Operator> listOperators = operatorDaoImpl.queryForEq(Operator.FIELD_CODE, operatorCode);

				if (listOperators.isEmpty()) {
					operator = new Operator();
					operator.setCode(tmpSession.getOperatorCode());
					operator.setName(tmpSession.getOperatorCode());
					operatorDaoImpl.addOperator(operator);
				} else {
					operator = listOperators.get(0);
				}
			}

			// Create `depot` object if needed
			Depot depot = null;
			List<Depot> listDepots = depotDaoImpl.queryForEq(Depot.DEPOT_CODE, tmpSession.getDepotCode());
			if (listDepots.isEmpty()) {
				depot = new Depot();
				depot.setDepotCode(tmpSession.getDepotCode());
				depot.setDepotName(tmpSession.getDepotCode());
				depotDaoImpl.addDepot(depot);
			} else {
				depot = listDepots.get(0);
			}

			// Create `container` object if needed
			Container container = null;
			List<Container> listContainers = containerDaoImpl.queryForEq(	Container.CONTAINER_ID,
																			tmpSession.getContainerId());
			if (listContainers.isEmpty()) {
				container = new Container();
				container.setContainerId(tmpSession.getContainerId());
				if (null != operator) {
					container.setOperator(operator);
				}

				if (null != depot) {
					container.setDepot(depot);
				}

				containerDaoImpl.addContainer(container);
			} else {
				container = listContainers.get(0);
			}

			// Create `container session` object
			String uuid = UUID.randomUUID().toString();

			// UUID is primary key
			ContainerSession containerSession = new ContainerSession();
			containerSession.setId(tmpSession.getId());
			containerSession.setCheckInTime(tmpSession.getCheckInTime());
			containerSession.setCheckOutTime(tmpSession.getCheckOutTime());
			containerSession.setImageIdPath(tmpSession.getImageIdPath());
			containerSession.setUuid(uuid);

			if (null != container) {
				containerSession.setContainer(container);
			}

			// Get server state
			containerSession.setServerState(tmpSession.getStatus());
			// Logger.Log(containerId + " | " + ContainerState.values()[tmpSession.getStatus()].name());

			// TODO: NOTE: may cause bugs
			// process audit report item
			List<AuditReportItem> auditReportItems = tmpSession.getAuditReportItems();
			Collection<Issue> issues = new ArrayList<Issue>();

			if (null != auditReportItems) {
				for (AuditReportItem auditReportItem : auditReportItems) {

					DamageCode damageCode = damageCodeDaoImpl.queryForId(auditReportItem.getDamageId());
					RepairCode repairCode = repairCodeDaoImpl.queryForId(auditReportItem.getRepairId());
					ComponentCode componentCode = componentCodeDaoImpl.queryForId(auditReportItem.getComponentId());

					Issue issue = new Issue(auditReportItem.getId(), damageCode, repairCode, componentCode,
											auditReportItem.getLocationCode(), auditReportItem.getLength(),
											auditReportItem.getHeight(), auditReportItem.getQuantity());

					if (issue != null) {
						issue.setContainerSession(containerSession);

						List<AuditReportImage> auditReportImages = auditReportItem.getAuditReportImages();
						Collection<CJayImage> cJayImages = new ArrayList<CJayImage>();
						for (AuditReportImage item : auditReportImages) {
							CJayImage tmpCJayImage = new CJayImage(item.getId(), item.getType(), item.getImageName(),
																	item.getImageUrl());
							tmpCJayImage.setIssue(issue);
							cJayImages.add(tmpCJayImage);
							cJayImageDaoImpl.addCJayImage(tmpCJayImage);
						}

						if (null != cJayImages) {
							issue.setCJayImages(cJayImages);
						}

						issues.add(issue);
						issueDaoImpl.addIssue(issue);
					}
				}
			}

			// process gate report images
			List<GateReportImage> gateReportImages = tmpSession.getGateReportImages();
			List<CJayImage> listImages = new ArrayList<CJayImage>();
			if (null != gateReportImages) {
				for (GateReportImage gateReportImage : gateReportImages) {

					CJayImage image = new CJayImage(gateReportImage.getId(), gateReportImage.getType(),
													gateReportImage.getCreatedAt(), gateReportImage.getImageName(),
													gateReportImage.getImageUrl());

					// set default value
					// image.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);

					if (null != image) {
						image.setContainerSession(containerSession);
					}

					Logger.Log("Add cjay image " + image.getImageName());
					cJayImageDaoImpl.addCJayImage(image);
					listImages.add(image);
				}
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
		tmpContainerSession.setOperatorCode(containerSession.getOperatorName());
		tmpContainerSession.setOperatorId(containerSession.getOperatorId());
		tmpContainerSession.setDepotCode(containerSession.getContainer().getDepot().getDepotCode());
		tmpContainerSession.setContainerId(containerId);
		tmpContainerSession.setCheckInTime(containerSession.getRawCheckInTime());

		if (TextUtils.isEmpty(checkoutTime)) {
			Logger.e(containerId + " | Checkout Time is NULL");
		}
		tmpContainerSession.setCheckOutTime(checkoutTime);

		if (officialUpload) {

			if (TextUtils.isEmpty(imageIdPath)) {
				Logger.e(containerId + " | Image Id Path is NULL");
			}
			tmpContainerSession.setImageIdPath(imageIdPath);

			List<GateReportImage> gateReportImages = new ArrayList<GateReportImage>();
			Collection<CJayImage> cJayImages = containerSession.getCJayImages();

			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getType() == CJayImage.TYPE_IMPORT || cJayImage.getType() == CJayImage.TYPE_EXPORT) {

					if (cJayImage.getId() != 0) {
						gateReportImages.add(new GateReportImage(cJayImage.getId(), cJayImage.getType(),
																	cJayImage.getTimePosted(), cJayImage.getImageName()));
					} else {
						gateReportImages.add(new GateReportImage(cJayImage.getType(), cJayImage.getTimePosted(),
																	cJayImage.getImageName()));
					}
				}
			}
			tmpContainerSession.setGateReportImages(gateReportImages);

			if (TextUtils.isEmpty(tmpContainerSession.getImageIdPath()) && gateReportImages.isEmpty() == false) {
				tmpContainerSession.setImageIdPath(gateReportImages.get(0).getImageName());
			}

			//

			List<AuditReportItem> auditReportItems = new ArrayList<AuditReportItem>();
			Collection<Issue> issues = containerSession.getIssues();

			if (null != issues) {
				for (Issue issue : issues) {
					auditReportItems.add(new AuditReportItem(issue));
				}
			}

			// TODO: only handle for app Auditor
			for (CJayImage cJayImage : cJayImages) {
				if (cJayImage.getType() == CJayImage.TYPE_REPORT && cJayImage.getIssue() == null) {

					Logger.Log("Container Id Image: " + cJayImage.getImageName());
					tmpContainerSession.setContainerIdImage(cJayImage.getImageName());
					break;
				}
			}

			tmpContainerSession.setAuditReportItems(auditReportItems);
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

				// // Update imageIdPath
				// main.setId(tmp.getId());
				// if (updateImageIdPath) {
				// if (!TextUtils.isEmpty(tmp.getImageIdPath())
				// && !tmp.getImageIdPath()
				// .equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {
				// main.setImageIdPath(tmp.getImageIdPath());
				// }
				//
				// }
				//
				// // Update check in time
				// main.setCheckInTime(tmp.getCheckInTime());
				//
				// // Update check out time
				// PreferencesUtil.storePrefsValue(ctx, PreferencesUtil.PREF_CONTAINER_SESSION_LAST_UPDATE,
				// tmp.getCheckInTime());

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
								+ " and locationCode LIKE ? and containerSession_id = ? and coalesce(length, 0) = "
								+ auditReportItem.getLength() + " and coalesce(height, 0) = "
								+ auditReportItem.getHeight();

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
									sql = "SELECT * FROM cjay_image WHERE image_name LIKE ?";
									Cursor auditCursor = db.rawQuery(sql, new String[] { "%" + auditReportImageName });

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

				String sqlString = "UPDATE container_session SET id = " + tmp.getId() + ", check_in_time = '"
						+ tmp.getCheckInTime() + "', image_id_path = '" + tmp.getImageIdPath() + "', server_state = "
						+ tmp.getStatus() + " WHERE _id = '" + main.getUuid() + "'";
				db.execSQL(sqlString);

				// Post ContainerSessionUpdatedEvent
				EventBus.getDefault().post(new ContainerSessionChangedEvent());
			}
		} catch (Exception e) {
			throw e;
		}

	}

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

								// Logger.Log("Gate Report Image Id: " + Integer.toString(gateReportImage.getId())
								// + "\nGate Report Image Name: " + gateReportImageName
								// + "\nGate Report Image Type: " + Integer.toString(gateReportImage.getType())
								// + "\nGate Report Image Time: " + gateReportImage.getCreatedAt());

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

											Logger.Log(

											"Audit Report Image Id: " + Integer.toString(cJayImage.getId())
													+ "\nAudit Report Image Name: " + cJayImage);

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

	public synchronized void update(Context ctx, String jsonString, ContainerSession main) throws Exception {
		update(ctx, jsonString, main, false);
	}
}
