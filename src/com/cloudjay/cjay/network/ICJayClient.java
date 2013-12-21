package com.cloudjay.cjay.network;

import java.util.List;

import org.json.JSONException;

import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.User;

import android.content.Context;

public interface ICJayClient {

	String getUserToken(String username, String password, Context ctx)
			throws JSONException;

	String getGoogleCloudToken(String token);

	void addGCMDevice(String regid, Context ctx) throws JSONException;

	// List<ItemModel> getNewItems(Context ctx);
	//
	// ItemTeamResultModel getNewItemsByTeam(Context ctx, UserModel currentUser,
	// int page);
	//

	List<Operator> getOperators(String token, Context ctx);

	User getCurrentUser(String token, Context ctx);

	//
	// List<UserModel> getTeamMembers(UserModel currentUser, Context ctx)
	// throws SQLException;
	//
	// ItemModel getItemModel(Context ctx, UserModel currentUser, int itemId);

	// void uploadItem(Context ctx, ItemModel item);
}
