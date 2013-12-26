package com.cloudjay.cjay.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.model.AuditReportItem;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.j256.ormlite.dao.ForeignCollection;

public class Mapper {

	private static IDatabaseManager databaseManager = null;

	public static ContainerSession toContainerSession(
			TmpContainerSession tmpSession, Context ctx) {

		if (null == databaseManager) {
			databaseManager = CJayClient.getInstance().getDatabaseManager();
		}

		try {

			DepotDaoImpl depotDaoImpl = databaseManager.getHelper(ctx)
					.getDepotDaoImpl();
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			ContainerDaoImpl containerDaoImpl = databaseManager.getHelper(ctx)
					.getContainerDaoImpl();
			CJayImageDaoImpl cJayImageDaoImpl = databaseManager.getHelper(ctx)
					.getCJayImageDaoImpl();

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
			ContainerSession containerSession = new ContainerSession();
			containerSession.setId(tmpSession.getId());
			containerSession.setCheckInTime(tmpSession.getCheckInTime());
			containerSession.setCheckOutTime(tmpSession.getCheckOutTime());
			containerSession.setImageIdPath(tmpSession.getImageIdPath());
			if (null != container)
				containerSession.setContainer(container);

			// process audit report item
			// for (AuditReportItem auditReportItem : tmpSession
			// .getAuditReportItems()) {
			//
			// }

			// process gate report images
			List<CJayImage> listImages = new ArrayList<CJayImage>();
			for (GateReportImage gateReportImage : tmpSession
					.getGateReportImages()) {

				Logger.Log(Integer.toString(gateReportImage.getId()));

				CJayImage image = new CJayImage(gateReportImage.getId(),
						gateReportImage.getType(),
						gateReportImage.getTimePosted(),
						gateReportImage.getImageName());

				if (null != image)
					image.setContainerSession(containerSession);

				cJayImageDaoImpl.addCJayImage(image);

				// data for returning
				listImages.add(image);
			}

			// TODO: ??
			// containerSession.setCJayImages(listImages);

			return containerSession;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
