package com.cloudjay.cjay.network;

import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.content.Context;

import com.cloudjay.cjay.model.CJayResourceStatus;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.NoConnectionException;

public interface ICJayClient {

	boolean hasNewMetadata(Context ctx);

	String getUserToken(String username, String password, Context ctx)
			throws JSONException, SocketTimeoutException, NoConnectionException;

	void addGCMDevice(String regid, Context ctx) throws JSONException,
			NoConnectionException;

	User getCurrentUser(String token, Context ctx) throws NoConnectionException;

	List<Operator> getOperators(Context ctx) throws NoConnectionException;

	List<Operator> getOperators(Context ctx, Date date)
			throws NoConnectionException;

	List<Operator> getOperators(Context ctx, String date)
			throws NoConnectionException;

	List<DamageCode> getDamageCodes(Context ctx) throws NoConnectionException;

	List<RepairCode> getRepairCodes(Context ctx) throws NoConnectionException;

	List<ComponentCode> getComponentCodes(Context ctx)
			throws NoConnectionException;

	List<ContainerSession> getAllContainerSessions(Context ctx)
			throws NoConnectionException;

	List<ContainerSession> getContainerSessions(Context ctx, int userRole,
			int filterStatus) throws NoConnectionException;

	List<ContainerSession> getContainerSessions(Context ctx, int userRole,
			int filterStatus, Date date) throws NoConnectionException;

	List<ContainerSession> getContainerSessions(Context ctx, int userRole,
			int filterStatus, String date) throws NoConnectionException;

	List<ContainerSession> getContainerSessions(Context ctx, Date date)
			throws NoConnectionException;

	List<ContainerSession> getContainerSessions(Context ctx, String date)
			throws NoConnectionException;

	List<CJayResourceStatus> getCJayResourceStatus(Context ctx)
			throws NoConnectionException;

	String postContainerSession(Context ctx, TmpContainerSession item)
			throws NoConnectionException;

	String postContainerSessionReportList(Context ctx, TmpContainerSession item)
			throws NoConnectionException;

	void fetchData(Context ctx) throws NoConnectionException;

	void updateListContainerSessions(Context ctx) throws NoConnectionException,
			SQLException;

	void updateListISOCode(Context ctx) throws NoConnectionException,
			SQLException;

}
