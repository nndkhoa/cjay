package com.cloudjay.cjay.util;

import android.content.Context;

import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.model.User;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class DataCenter {

	@Bean
	NetworkClient networkClient;

	Context context;

	public DataCenter(Context context) {
		this.context = context;
	}

	public String getToken(String email, String password) {
		return networkClient.getToken(email, password);
	}

	public User getCurrentUser(Context context) {
		User user = networkClient.getCurrentUser(context);
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_ROLE, user.getRole() + "");
		PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_USER_DEPOT, user.getDepotCode() + "");
		return user;
	}

	public void fetchOperators(Context context) {
		networkClient.getOperators(context, null);
	}

	public void fetchIsoCodes(Context context) {
		networkClient.getDamageCodes(context, null);
		networkClient.getRepairCodes(context, null);
		networkClient.getComponentCodes(context, null);
	}
}