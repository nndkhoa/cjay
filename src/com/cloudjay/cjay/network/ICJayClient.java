package com.cloudjay.cjay.network;

import java.util.Date;
import java.util.List;

import org.json.JSONException;

import com.cloudjay.cjay.model.CJayResourceStatus;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.User;

import android.content.Context;

public interface ICJayClient {

	boolean hasNewMetadata(Context ctx);

	String getUserToken(String username, String password, Context ctx)
			throws JSONException;

	String getGoogleCloudToken(String token);

	void addGCMDevice(String regid, Context ctx) throws JSONException;

	User getCurrentUser(String token, Context ctx);

	List<Operator> getOperators(Context ctx);

	List<Operator> getOperators(Context ctx, Date date);

	List<Operator> getOperators(Context ctx, String date);

	List<DamageCode> getDamageCodes(Context ctx);

	List<RepairCode> getRepairCodes(Context ctx);

	List<ContainerSession> getContainerSessions(Context ctx);

	List<ContainerSession> getContainerSessions(Context ctx, Date date);

	List<ContainerSession> getContainerSessions(Context ctx, String date);

	List<CJayResourceStatus> getCJayResourceStatus(Context ctx);

	// List<ItemModel> getNewItems(Context ctx);
	//
	// ItemTeamResultModel getNewItemsByTeam(Context ctx, UserModel currentUser,
	// int page);
	//

	// List<UserModel> getTeamMembers(UserModel currentUser, Context ctx)
	// throws SQLException;
	//
	// ItemModel getItemModel(Context ctx, UserModel currentUser, int itemId);

	// void uploadItem(Context ctx, ItemModel item);
}
