package com.cloudjay.cjay.util;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.DepotDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.dao.UploadItem;
import com.cloudjay.cjay.dao.UploadItemDaoImpl;
import com.cloudjay.cjay.dao.UserDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Depot;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.User;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final Class<?>[] DATA_CLASSES = { DamageCode.class,
			ComponentCode.class, RepairCode.class, Operator.class, User.class,
			Depot.class, Container.class, ContainerSession.class, Issue.class,
			CJayImage.class };

	public static final Class<?>[] DROP_CLASSES = { User.class, Depot.class,
			Container.class, ContainerSession.class, Issue.class,
			CJayImage.class };

	public static final String DATABASE_NAME = "cjay.db";
	public static final int DATABASE_VERSION = 1;

	UserDaoImpl userDaoImpl = null;
	OperatorDaoImpl operatorDaoImpl = null;
	IssueDaoImpl issueDaoImpl = null;
	CJayImageDaoImpl cJayImageDaoImpl = null;
	ContainerDaoImpl containerDaoImpl = null;
	ContainerSessionDaoImpl containerSessionDaoImpl = null;
	DamageCodeDaoImpl damageCodeDaoImpl = null;
	DepotDaoImpl depotDaoImpl = null;
	RepairCodeDaoImpl repairCodeDaoImpl = null;
	ComponentCodeDaoImpl componentCodeDaoImpl = null;
	UploadItemDaoImpl uploadItemDeoImpl = null;

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
			Logger.Log("onCreate DB");
			for (Class<?> dataClass : DATA_CLASSES) {
				TableUtils.createTable(connectionSource, dataClass);
			}

			// Create view
			String sql = "CREATE VIEW csview AS"
					+ " SELECT cs._id, cs.check_out_time, cs.check_in_time, cs.image_id_path, cs.on_local, cs.fixed, cs.upload_confirmation, cs.state, cs.cleared, c.container_id, o.operator_name"
					+ " FROM container_session AS cs, container AS c, operator as o"
					+ " WHERE cs.container_id = c._id AND c.operator_id = o._id";

			db.execSQL(sql);

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

		try {
			userDaoImpl = null;
			operatorDaoImpl = null;
			issueDaoImpl = null;
			cJayImageDaoImpl = null;
			containerDaoImpl = null;
			containerSessionDaoImpl = null;
			damageCodeDaoImpl = null;
			depotDaoImpl = null;
			repairCodeDaoImpl = null;
			componentCodeDaoImpl = null;

			this.connectionSource.close();
			super.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserDaoImpl getUserDaoImpl() throws SQLException {
		if (userDaoImpl == null) {
			userDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					User.class);
		}
		return userDaoImpl;
	}

	public OperatorDaoImpl getOperatorDaoImpl() throws SQLException {
		if (null == operatorDaoImpl) {
			operatorDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					Operator.class);
		}
		return operatorDaoImpl;
	}

	public CJayImageDaoImpl getCJayImageDaoImpl() throws SQLException {
		if (null == cJayImageDaoImpl) {
			cJayImageDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					CJayImage.class);
		}
		return cJayImageDaoImpl;
	}

	public ContainerDaoImpl getContainerDaoImpl() throws SQLException {
		if (null == containerDaoImpl) {
			containerDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					Container.class);
		}
		return containerDaoImpl;
	}

	public ContainerSessionDaoImpl getContainerSessionDaoImpl()
			throws SQLException {
		if (null == containerSessionDaoImpl) {
			containerSessionDaoImpl = DaoManager.createDao(
					this.getConnectionSource(), ContainerSession.class);
		}
		return containerSessionDaoImpl;
	}

	public DepotDaoImpl getDepotDaoImpl() throws SQLException {
		if (null == depotDaoImpl) {
			depotDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					Depot.class);
		}
		return depotDaoImpl;
	}

	public IssueDaoImpl getIssueDaoImpl() throws SQLException {
		if (null == issueDaoImpl) {
			issueDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					Issue.class);
		}
		return issueDaoImpl;
	}

	public RepairCodeDaoImpl getRepairCodeDaoImpl() throws SQLException {
		if (null == repairCodeDaoImpl) {
			repairCodeDaoImpl = DaoManager.createDao(
					this.getConnectionSource(), RepairCode.class);
		}
		return repairCodeDaoImpl;
	}

	public DamageCodeDaoImpl getDamageCodeDaoImpl() throws SQLException {
		if (null == damageCodeDaoImpl) {
			damageCodeDaoImpl = DaoManager.createDao(
					this.getConnectionSource(), DamageCode.class);
		}
		return damageCodeDaoImpl;
	}

	public ComponentCodeDaoImpl getComponentCodeDaoImpl() throws SQLException {
		if (null == componentCodeDaoImpl) {
			componentCodeDaoImpl = DaoManager.createDao(
					this.getConnectionSource(), ComponentCode.class);
		}
		return componentCodeDaoImpl;
	}

	public UploadItemDaoImpl getUploadItemImpl() throws SQLException {
		if (null == uploadItemDeoImpl) {
			uploadItemDeoImpl = DaoManager.createDao(
					this.getConnectionSource(), UploadItem.class);
		}

		return uploadItemDeoImpl;
	}

}