/**
 * @author tieubao
 */

package com.cloudjay.cjay.util;

import java.sql.SQLException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import android.content.Context;
import android.os.AsyncTask;

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
	private static User currentUser;
	private static CJaySession instance;

	public static CJaySession restore(Context context) {

		if (instance != null) return instance;

		// Logger.Log("restoring session ... ");
		databaseManager = CJayClient.getInstance().getDatabaseManager();
		try {
			DatabaseHelper helper = databaseManager.getHelper(context);
			userDao = helper.getUserDaoImpl();

			User user = userDao.getMainUser();
			if (null != user) {
				instance = new CJaySession();
				instance.setCurrentUser(user);
				return instance;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void save(Context context) {

	}

	public CJaySession() {

	}

	public void deleteSession(final Context context) {

		new AsyncTask<Void, Boolean, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				// PreferencesUtil.storePrefsValue(context, PreferencesUtil.PREF_INITIALIZED, false);
				DataCenter.getDatabaseHelper(context).addUsageLog("#backup database");

				Utils.backupDatabase(getCurrentUser().getUserName());
				PreferencesUtil.clearPrefs(context);
				context.deleteDatabase(DatabaseHelper.DATABASE_NAME);

				// Logger.Log("deleting session ...");
				// databaseManager = CJayClient.getInstance().getDatabaseManager();
				// try {
				//
				// DatabaseHelper helper = databaseManager.getHelper(context);
				//
				// // userDao = helper.getUserDaoImpl();
				// // User user = userDao.getMainUser();
				// //
				// // if (null != user) {
				// // user.setMainAccount(false);
				// // user.setAccessToken("");
				// // userDao.update(user);
				// // currentUser = null;
				// // }
				//
				// for (Class<?> dataClass : DatabaseHelper.DROP_CLASSES) {
				// TableUtils.dropTable(helper.getConnectionSource(), dataClass, true);
				// }
				//
				// for (Class<?> dataClass : DatabaseHelper.DROP_CLASSES) {
				// TableUtils.createTable(helper.getConnectionSource(), dataClass);
				// }
				//
				// DataCenter.getDatabaseHelper(context).addUsageLog("User #logout");
				// } catch (SQLException e) {
				//
				// e.printStackTrace();
				// context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
				//
				// }

				return null;
			}
		}.execute();

	}

	public void extendAccessTokenIfNeeded(Context applicationContext) {
		Logger.Log("extending user access token ...");
	}

	public String getAccessToken() {
		if (currentUser == null) { return ""; }
		return currentUser.getAccessToken();
	}

	public User getCurrentUser() {

		if (currentUser == null) {
			Logger.Log("Current user is null ??");
			return null;
		}

		return currentUser;
	}

	public Depot getDepot() {

		if (null != currentUser) return currentUser.getDepot();

		return null;
	}

	public int getUserRole() {
		return currentUser.getRole();
	}

	public void setCurrentUser(User user) {
		currentUser = user;
	}
}
