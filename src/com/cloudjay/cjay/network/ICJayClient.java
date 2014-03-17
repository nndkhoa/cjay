package com.cloudjay.cjay.network;

import java.net.SocketTimeoutException;
import java.util.List;

import org.json.JSONException;

import android.content.Context;

import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSessionResult;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;

public interface ICJayClient {

	String getUserToken(String username, String password, Context ctx)
			throws JSONException, SocketTimeoutException, NoConnectionException;

	void addGCMDevice(String regid, Context ctx) throws JSONException,
			NoConnectionException;

	User getCurrentUser(String token, Context ctx) throws NoConnectionException;

	List<Operator> getOperators(Context ctx) throws NoConnectionException,
			NullSessionException;

	List<Operator> getOperators(Context ctx, String date)
			throws NoConnectionException, NullSessionException;

	List<DamageCode> getDamageCodes(Context ctx) throws NoConnectionException,
			NullSessionException;

	List<DamageCode> getDamageCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException;

	List<RepairCode> getRepairCodes(Context ctx) throws NoConnectionException,
			NullSessionException;

	List<RepairCode> getRepairCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException;

	List<ComponentCode> getComponentCodes(Context ctx)
			throws NoConnectionException, NullSessionException;

	List<ComponentCode> getComponentCodes(Context ctx, String date)
			throws NoConnectionException, NullSessionException;

	String postContainerSession(Context ctx, TmpContainerSession item)
			throws NoConnectionException;

	ContainerSessionResult getContainerSessionsByPage(Context ctx, int page)
			throws NoConnectionException, NullSessionException;

	ContainerSessionResult getContainerSessionsByPage(Context ctx, String date,
			int page) throws NoConnectionException, NullSessionException;
}
