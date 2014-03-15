/**
 * @author tieubao
 */

package com.cloudjay.cjay.util;

import java.sql.SQLException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import android.content.Context;

import com.cloudjay.cjay.dao.IUserDao;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.j256.ormlite.table.TableUtils;

@EBean(scope = Scope.Singleton)
public class CJaySession {

	private static IDatabaseManager databaseManager;
	private static IUserDao userDao;
	private User currentUser;

	public CJaySession() {

	}

	public User getCurrentUser() {
		if (currentUser == null) {
			Logger.Log("Current user is null ??");
		}

		return currentUser;
	}

	public void setCurrentUser(User user) {
		currentUser = user;
	}

	public Depot getDepot() {

		if (null != currentUser) {
			return currentUser.getDepot();
		}

		return null;
	}

	public int getUserRole() {
		return currentUser.getRole();
	}

	public int getFilterStatus() {
		return currentUser.getFilterStatus();
	}

	public static void save(Context context) {

	}

	public static CJaySession restore(Context context) {
		Logger.Log("restoring session ... ");
		databaseManager = CJayClient.getInstance().getDatabaseManager();
		try {
			DatabaseHelper helper = databaseManager.getHelper(context);
			userDao = helper.getUserDaoImpl();

			User user = userDao.getMainUser();

			if (null != user) {
				CJaySession session = new CJaySession();
				session.setCurrentUser(user);
				return session;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean deleteSession(Context context) {

		Logger.Log("deleting session ...");
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
			}

			for (Class<?> dataClass : DatabaseHelper.DROP_CLASSES) {
				TableUtils.dropTable(helper.getConnectionSource(), dataClass,
						true);
			}

			for (Class<?> dataClass : DatabaseHelper.DROP_CLASSES) {
				TableUtils.createTable(helper.getConnectionSource(), dataClass);
			}

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
			context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
			return false;
		}
	}

	public void extendAccessTokenIfNeeded(Context applicationContext) {
		Logger.Log("extending user access token ...");
	}
}