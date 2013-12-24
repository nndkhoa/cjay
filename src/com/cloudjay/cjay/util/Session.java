/**
 * @author tieubao
 */

package com.cloudjay.cjay.util;

import java.sql.SQLException;

import com.cloudjay.cjay.dao.IUserDao;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;

import android.content.Context;

public class Session {

	private static final String LOG_TAG = "Session";
	private static IDatabaseManager databaseManager;
	private static IUserDao userDao;
	private User currentUser;

	public User getCurrentUser() {
		return currentUser;
	}

	public Session(User user) {
		currentUser = user;
	}

	public static void save(Context context) {

	}

	public static Session restore(Context context) {
		Logger.Log(LOG_TAG, "restoring session ... ");
		databaseManager = CJayClient.getInstance().getDatabaseManager();
		try {
			userDao = databaseManager.getHelper(context).getUserDaoImpl();
			User user = userDao.getMainUser();

			if (null != user) {
				return new Session(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void extendAccessTokenIfNeeded(Context applicationContext) {
		Logger.Log(LOG_TAG, "extending user access token ...");
	}
}