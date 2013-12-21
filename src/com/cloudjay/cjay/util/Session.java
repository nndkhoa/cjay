/**
 * @author tieubao
 */

package com.cloudjay.cjay.util;

import java.sql.SQLException;

import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.IUserDao;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;

import android.content.Context;

public class Session {

	private static IDatabaseManager databaseManager;
	private static IUserDao userDao;
	private User currentUser;

	public Session(User user) {
		currentUser = user;
	}

	public static void save(Context context) {

	}

	public static Session restore(Context context) {
		databaseManager = CJayClient.getInstance().getDatabaseManager();
		try {
			userDao = databaseManager.getHelper(context).getUserImpl();
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
		// TODO: extend access token expire

	}
}
