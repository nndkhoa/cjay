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
import com.cloudjay.cjay.util.MismatchDataException;
import com.cloudjay.cjay.util.NoConnectionException;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.ServerInternalErrorException;

public interface ICJayClient {

	void addGCMDevice(String regid, Context ctx) throws NoConnectionException, NullSessionException, JSONException;

	List<ComponentCode> getComponentCodes(Context ctx, String date) throws NoConnectionException, NullSessionException;

	ContainerSessionResult
			getContainerSessionsByPage(Context ctx, String date, int page, int type) throws NoConnectionException,
																					NullSessionException;

	User getCurrentUser(String token, Context ctx) throws NoConnectionException;

	List<DamageCode> getDamageCodes(Context ctx, String date) throws NoConnectionException, NullSessionException;

	List<Operator> getOperators(Context ctx, String modifiedAfter) throws NoConnectionException, NullSessionException;

	List<RepairCode> getRepairCodes(Context ctx, String date) throws NoConnectionException, NullSessionException;

	String getUserToken(String username, String password, Context ctx) throws SocketTimeoutException,
																		NoConnectionException;

	String postContainerSession(Context ctx, TmpContainerSession item) throws NoConnectionException,
																		NullSessionException, MismatchDataException,
																		ServerInternalErrorException;
}
