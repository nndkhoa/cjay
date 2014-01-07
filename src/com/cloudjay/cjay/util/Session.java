/**
 * @author tieubao
 */

package com.cloudjay.cjay.util;

import java.sql.SQLException;

import android.content.Context;

import com.cloudjay.cjay.dao.IUserDao;
import com.cloudjay.cjay.model.DatabaseHelper;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;

public class Session {

	private static final String LOG_TAG = "Session";
	private static IDatabaseManager databaseManager;
	private static IUserDao userDao;
	private User currentUser;

	public User getCurrentUser() {
		return currentUser;
	}

	public int getUserRole() {
		return currentUser.getRole();
	}

	public int getFilterStatus() {
		return currentUser.getFilterStatus();
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
			DatabaseHelper helper = databaseManager.getHelper(context);
			userDao = helper.getUserDaoImpl();

			User user = userDao.getMainUser();

			if (null != user) {
				return new Session(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean deleteSession(Context context) {
		Logger.Log(LOG_TAG, "deleting session ...");

		databaseManager = CJayClient.getInstance().getDatabaseManager();
		try {
			DatabaseHelper helper = databaseManager.getHelper(context);
			userDao = helper.getUserDaoImpl();

			User user = userDao.getMainUser();

			if (null != user) {
				user.setMainAccount(false);
				user.setAccessToken("");
				userDao.update(user);

				this.currentUser = null;
				return true;
			}

			helper.close();
			context.deleteDatabase(helper.DATABASE_NAME);

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}

	public void extendAccessTokenIfNeeded(Context applicationContext) {
		Logger.Log(LOG_TAG, "extending user access token ...");
	}
}
