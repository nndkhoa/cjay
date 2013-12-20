package com.cloudjay.cjay.model;

import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.Team;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.Flags;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final Class<?>[] DATA_CLASSES = { Container.class,
			Issue.class, Team.class, User.class };

	public static final String DATABASE_NAME = "cjay.db";
	private static final int DATABASE_VERSION = 1;

	UserDaoImpl userDaoImpl = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			if (Flags.DEBUG) {
				Log.i(DatabaseHelper.class.getName(), "onCreate");
			}
			for (Class<?> dataClass : DATA_CLASSES) {
				TableUtils.createTable(connectionSource, dataClass);
			}

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			if (Flags.DEBUG) {
				Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			}
			for (Class<?> dataClass : DATA_CLASSES) {
				TableUtils.dropTable(connectionSource, dataClass, true);
			}

			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		userDaoImpl = null;
		super.close();
	}

	public UserDaoImpl getUserImpl() throws java.sql.SQLException {
		if (userDaoImpl == null) {
			userDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					User.class);
		}
		return userDaoImpl;
	}

}