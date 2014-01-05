package com.cloudjay.cjay.util;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.content.Context;
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

public class Mapper {

	private final String LOG_TAG = "Mapper";

	private static IDatabaseManager databaseManager = null;
	private static Mapper instance = null;

	public Mapper() {
		if (null == databaseManager) {
			databaseManager = CJayClient.getInstance().getDatabaseManager();
		}
	}

	public static Mapper getInstance() {
		if (instance == null) {
			instance = new Mapper();
		}

		return instance;
	}

	public synchronized void update(Context ctx, String jsonString,
			ContainerSession main) {

		try {

			TmpContainerSession tmp = null;
			Gson gson = new GsonBuilder().setDateFormat(
					CJayConstant.CJAY_SERVER_DATETIME_FORMAT).create();

			Type listType = new TypeToken<TmpContainerSession>() {
			}.getType();

			try {
				tmp = gson.fromJson(jsonString, listType);
			} catch (Exception e) {
				Logger.Log(LOG_TAG, "jsonString is on wrong format");
				e.printStackTrace();
				return;
			}

			if (null != tmp) {
				CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(
						ctx).getCJayImageDaoImpl();

				IssueDaoImpl issueDaoImpl = databaseManager.getHelper(ctx)
						.getIssueDaoImpl();

				main.setId(tmp.getId());
				main.setImageIdPath(tmp.getImageIdPath());

				List<GateReportImage> gateReportImages = tmp
						.getGateReportImages();
				Collection<CJayImage> cJayImages = main.getCJayImages();

				if (gateReportImages != null) {
					for (GateReportImage gateReportImage : gateReportImages) {
						for (CJayImage cJayImage : cJayImages) {
							String gateReportImageName = gateReportImage
									.getImageName();
							String cJayImageName = cJayImage.getImageName();
							if (gateReportImageName.contains(cJayImageName)) {

								// Logger.Log(
								// LOG_TAG,
								// "Gate Report Image Id: "
								// + Integer
								// .toString(gateReportImage
								// .getId())
								// + "\nGate Report Image Name: "
								// + gateReportImageName
								// + "\nGate Report Image Type: "
								// + Integer
								// .toString(gateReportImage
								// .getType())
								// + "\nGate Report Image Time: "
								// + gateReportImage
								// .getTimePosted());

								cJayImage.setId(gateReportImage.getId());
								cJayImage.setImageName(gateReportImageName);
								cJayImageDaoImpl.update(cJayImage);

								break;
							}
						}
					}
				}

				List<AuditReportItem> auditReportItems = tmp
						.getAuditReportItems();

				Collection<Issue> issues = main.getIssues();

				if (auditReportItems != null) {
					for (AuditReportItem auditReportItem : auditReportItems) {
						for (Issue issue : issues) {
							if (issue.equals(auditReportItem)) {
								issue.setId(auditReportItem.getId());

								List<AuditReportImage> auditReportImages = auditReportItem
										.getAuditReportImages();

								Collection<CJayImage> issueImages = issue
										.getCJayImages();

								for (AuditReportImage auditReportImage : auditReportImages) {
									for (CJayImage cJayImage : issueImages) {
										String auditReportImageName = auditReportImage
												.getImageName();
										String cJayImageName = cJayImage
												.getImageName();
										if (auditReportImageName
												.contains(cJayImageName)) {

											cJayImage.setId(auditReportImage
													.getId());
											cJayImage
													.setImageName(auditReportImageName);

											Logger.Log(
													LOG_TAG,
													"Gate Report Image Id: "
															+ Integer
																	.toString(cJayImage
																			.getId())
															+ "\nGate Report Image Name: "
															+ cJayImage);

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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TmpContainerSession toTmpContainerSession(
			ContainerSession containerSession, Context ctx) {

		TmpContainerSession tmpContainerSession = new TmpContainerSession();

		tmpContainerSession.setOperatorCode(containerSession.getOperatorName());
		tmpContainerSession.setDepotCode(containerSession.getContainer()
				.getDepot().getDepotCode());
		tmpContainerSession.setContainerId(containerSession.getContainerId());

		tmpContainerSession
				.setCheckInTime(containerSession.getRawCheckInTime());

		tmpContainerSession.setCheckOutTime(containerSession
				.getRawCheckOutTime());

		tmpContainerSession.setImageIdPath(containerSession.getImageIdPath());

		Collection<CJayImage> cJayImages = containerSession.getCJayImages();

		List<GateReportImage> gateReportImages = new ArrayList<GateReportImage>();

		for (CJayImage cJayImage : cJayImages) {
			if (cJayImage.getType() == CJayImage.TYPE_IMPORT
					|| cJayImage.getType() == CJayImage.TYPE_EXPORT) {
				gateReportImages.add(new GateReportImage(cJayImage.getType(),
						cJayImage.getTimePosted(), cJayImage.getImageName()));
			}
		}

		tmpContainerSession.setGateReportImages(gateReportImages);

		if (TextUtils.isEmpty(tmpContainerSession.getImageIdPath())
				&& gateReportImages.isEmpty() == false)
			tmpContainerSession.setImageIdPath(gateReportImages.get(0)
					.getImageName());

		List<AuditReportItem> auditReportItems = new ArrayList<AuditReportItem>();
		Collection<Issue> issues = containerSession.getIssues();

		if (null != issues) {
			for (Issue issue : issues) {
				auditReportItems.add(new AuditReportItem(issue));
			}
		}

		tmpContainerSession.setAuditReportItems(auditReportItems);
		return tmpContainerSession;
	}

	/**
	 * 
	 * Sử dụng để convert data lấy từ server thành local data
	 * 
	 * @param tmpSession
	 * @param ctx
	 * @return
	 */
	public ContainerSession toContainerSession(TmpContainerSession tmpSession,
			Context ctx) {

		try {

			DepotDaoImpl depotDaoImpl = databaseManager.getHelper(ctx)
					.getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx)
					.getContainerDaoImpl();
			CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(ctx)
					.getCJayImageDaoImpl();
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();
			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();
			ComponentCodeDaoImpl componentCodeDaoImpl = databaseManager
					.getHelper(ctx).getComponentCodeDaoImpl();
			IssueDaoImpl issueDaoImpl = databaseManager.getHelper(ctx)
					.getIssueDaoImpl();

			Operator operator = null;
			List<Operator> listOperators = operatorDaoImpl.queryForEq(
					Operator.CODE, tmpSession.getOperatorCode());

			if (listOperators.isEmpty()) {
				operator = new Operator();
				operator.setCode(tmpSession.getOperatorCode());
				operator.setName(tmpSession.getOperatorCode());
				operatorDaoImpl.addOperator(operator);
			} else {
				operator = listOperators.get(0);
			}

			// Create `depot` object if needed
			Depot depot = null;
			List<Depot> listDepots = depotDaoImpl.queryForEq(Depot.DEPOT_CODE,
					tmpSession.getDepotCode());
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
			List<Container> listContainers = containerDaoImpl.queryForEq(
					Container.CONTAINER_ID, tmpSession.getContainerId());
			if (listContainers.isEmpty()) {
				container = new Container();
				container.setContainerId(tmpSession.getContainerId());
				if (null != operator)
					container.setOperator(operator);

				if (null != depot)
					container.setDepot(depot);

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

			if (null != container)
				containerSession.setContainer(container);

			// TODO: may cause bugs
			// process audit report item
			List<AuditReportItem> auditReportItems = tmpSession
					.getAuditReportItems();
			Collection<Issue> issues = new ArrayList<Issue>();
			if (null != auditReportItems) {
				for (AuditReportItem auditReportItem : auditReportItems) {

					List<AuditReportImage> auditReportImages = auditReportItem
							.getAuditReportImages();
					Collection<CJayImage> cJayImages = new ArrayList<CJayImage>();

					for (AuditReportImage item : auditReportImages) {
						CJayImage tmp = new CJayImage(item.getId(),
								item.getType(), item.getImageName());
						cJayImages.add(tmp);
					}

					cJayImageDaoImpl
							.addListCJayImages((List<CJayImage>) cJayImages);

					DamageCode damageCode = damageCodeDaoImpl
							.queryForId(auditReportItem.getDamageId());
					RepairCode repairCode = repairCodeDaoImpl
							.queryForId(auditReportItem.getRepairId());
					ComponentCode componentCode = componentCodeDaoImpl
							.queryForId(auditReportItem.getComponentId());

					Issue issue = new Issue(auditReportItem.getId(),
							damageCode, repairCode, componentCode,
							auditReportItem.getLocationCode(),
							auditReportItem.getLength(),
							auditReportItem.getHeight(),
							auditReportItem.getQuantity(), cJayImages);

					if (issue != null)
						issue.setContainerSession(containerSession);

					issueDaoImpl.addIssue(issue);
					issues.add(issue);
				}
			}

			// process gate report images
			List<GateReportImage> gateReportImages = tmpSession
					.getGateReportImages();
			List<CJayImage> listImages = new ArrayList<CJayImage>();
			if (null != gateReportImages) {
				for (GateReportImage gateReportImage : gateReportImages) {

					CJayImage image = new CJayImage(gateReportImage.getId(),
							gateReportImage.getType(),
							gateReportImage.getTimePosted(),
							gateReportImage.getImageName());

					// set default value
					image.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);

					if (null != image)
						image.setContainerSession(containerSession);

					cJayImageDaoImpl.addCJayImage(image);
					listImages.add(image);
				}
			}

			// TODO: Không cần add chiều xuôi??
			if (null != listImages)
				containerSession.setCJayImages(listImages);

			if (null != issues)
				containerSession.setIssues(issues);

			return containerSession;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
