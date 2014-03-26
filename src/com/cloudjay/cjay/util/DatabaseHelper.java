package com.cloudjay.cjay.util;

import java.sql.SQLException;

import org.androidannotations.annotations.rest.Get;

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
import com.cloudjay.cjay.dao.UserLogDaoImpl;
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
import com.cloudjay.cjay.model.UserLog;
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
	public static final int DATABASE_VERSION = 2;

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
	UserLogDaoImpl userLogDaoImpl = null;

	private static class Patch {

		public void apply(SQLiteDatabase db, ConnectionSource connectionSource) {
		}

		public void revert(SQLiteDatabase db, ConnectionSource connectionSource) {
		}

	}

	private static final Patch[] PATCHES = new Patch[] { new Patch() {

		public void apply(SQLiteDatabase db, ConnectionSource connectionSource) {

			for (Class<?> dataClass : DATA_CLASSES) {
				try {
					TableUtils.createTable(connectionSource, dataClass);
				} catch (SQLException e) {
					Log.e(DatabaseHelper.class.getName(),
							"Can't create database", e);
					throw new RuntimeException(e);
				}
			}

			// Create view
			// csview --> join table operator + container + container_session
			String sql = "CREATE VIEW csview AS"
					+ " SELECT cs._id, cs.check_out_time, cs.check_in_time, cs.image_id_path, cs.on_local, cs.fixed, cs.export, cs.upload_confirmation, cs.state, cs.cleared, c.container_id, o.operator_name"
					+ " FROM container_session AS cs, container AS c"
					+ " LEFT JOIN operator AS o ON c.operator_id = o._id"
					+ " WHERE cs.container_id = c._id";
			db.execSQL(sql);

			// view for validate container sessions before upload in Gate Import
			sql = "CREATE VIEW cs_import_validation_view as"
					+ " SELECT cs.*, count(cjay_image._id) as import_image_count "
					+ " FROM csview cs"
					+ " LEFT JOIN cjay_image ON cjay_image.containerSession_id = cs._id AND cjay_image.type = 0"
					+ " WHERE cs.upload_confirmation = 0 AND cs.on_local = 1 AND cs.export = 0 AND cs.state <> 4"
					+ " GROUP BY cs._id";
			db.execSQL(sql);

			// view for validate container sessions before upload in Gate Export
			sql = "CREATE VIEW cs_export_validation_view as"
					+ " SELECT cs.*, count(cjay_image._id) as export_image_count "
					+ " FROM csview cs"
					+ " LEFT JOIN cjay_image ON cjay_image.containerSession_id = cs._id AND cjay_image.type = 1"
					+ " WHERE cs.check_out_time = '' AND (cs.export = 1) OR (cs.on_local = 0)"
					+ " GROUP BY cs._id";
			db.execSQL(sql);

			// csiview --> csview + issue_count
			sql = "CREATE VIEW csiview AS"
					+ " SELECT csview.*, count(issue.containerSession_id) as issue_count"
					+ " FROM issue JOIN csview ON issue.containerSession_id = csview._id"
					+ " GROUP BY containerSession_id"
					+ " UNION ALL"
					+ " SELECT csview.*, 0 as issue_count"
					+ " FROM csview"
					+ " WHERE csview.container_id NOT IN"
					+ " (SELECT csview.container_id"
					+ " FROM issue JOIN csview ON issue.containerSession_id = csview._id"
					+ " GROUP BY containerSession_id"
					+ " HAVING count(containerSession_id) > 0)";

			db.execSQL(sql);

			// view for validate container sessions before upload in Auditor
			sql = "create view csi_auditor_validation_view as"
					+ " select csi.*, count(image._id) as auditor_image_no_issue_count"
					+ " from"
					+ "	(select csiview.*, count(issue.containerSession_id) as invalid_issue_count"
					+ "	from csiview"
					+ "	left join issue on csiview._id = issue.containerSession_id and issue._id in"
					+ "		(select issue._id"
					+ "		from issue"
					+ "		where coalesce(componentCode_id, '') = '' or coalesce(damageCode_id, '') = ''"
					+ "		or coalesce(locationCode, '') = '' or coalesce(repairCode_id, '') = '')"
					+ "	group by csiview._id) as csi"
					+ " left join cjay_image as image on csi._id = image.containerSession_id and image.type = 2 and image.issue_id is NULL"
					+ " group by csi._id";

			db.execSQL(sql);

			// view for validate container sessions before upload in Repair Mode
			sql = "create view csi_repair_validation_view as"
					+ " select csiview.*, count(issue._id) as fixed_issue_count"
					+ " from csiview"
					+ " left join issue on csiview._id = issue.containerSession_id and coalesce(issue.fixed, 0) = 1"
					+ " group by csiview._id";

			db.execSQL(sql);
		}

		public void revert(SQLiteDatabase db, ConnectionSource connectionSource) {

		}
	}, new Patch() {
			// version = 2
		public void apply(SQLiteDatabase db, ConnectionSource connectionSource) {

			// Add table UserLog
			try {
				TableUtils.createTable(connectionSource, UserLog.class);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Add column `upload_type` in table `container_session`
			try {
				db.execSQL("ALTER TABLE container_session ADD COLUMN upload_type INTEGER DEFAULT 0");
			} catch (Exception e) {
				Logger.w("Column upload_type is already existed.");
			}

		}

		public void revert(SQLiteDatabase db, ConnectionSource connectionSource) {
		}
	} };

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

		Logger.Log("onCreate DB");
		for (int i = 0; i < PATCHES.length; i++) {
			PATCHES[i].apply(db, connectionSource);
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

		for (int i = oldVersion; i < newVersion; i++) {
			PATCHES[i].apply(db, connectionSource);
		}

		// try {
		//
		// Logger.Log("onUpgrade");
		//
		// for (Class<?> dataClass : DATA_CLASSES) {
		// TableUtils.dropTable(connectionSource, dataClass, true);
		// }
		//
		// onCreate(db, connectionSource);
		// } catch (SQLException e) {
		// Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
		// throw new RuntimeException(e);
		// }
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// for (int i = oldVersion; i > newVersion; i++) {
		// PATCHES[i - 1].revert(db);
		// }

		super.onDowngrade(db, oldVersion, newVersion);
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

	public UserLogDaoImpl getUserLogDaoImpl() throws SQLException {

		if (null == userLogDaoImpl) {
			userLogDaoImpl = DaoManager.createDao(this.getConnectionSource(),
					UserLog.class);
		}

		return userLogDaoImpl;
	}
}