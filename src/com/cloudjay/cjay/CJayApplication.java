package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.androidannotations.annotations.EApplication;

import uk.co.senab.bitmapcache.BitmapLruCache;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.network.HttpRequestWrapper;
import com.cloudjay.cjay.network.IHttpRequestWrapper;
import com.cloudjay.cjay.receivers.InstantUploadReceiver;
import com.cloudjay.cjay.tasks.PhotupThreadFactory;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.DatabaseManager;
import com.cloudjay.cjay.util.IssueReportHelper;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.NullSessionException;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.util.UploadState;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import de.greenrobot.event.EventBus;

@ReportsCrashes(formKey = "",
				formUri = CJayConstant.ACRA,
				mode = ReportingInteractionMode.TOAST,
				resToastText = R.string.crash_toast_text)
@EApplication
public class CJayApplication extends Application {

	@Override
	public void onCreate() {
		Logger.Log("Start Application");

		// Configure Logger
		boolean debuggable = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
												.getBoolean(getString(R.string.pref_key_enable_logger_checkbox), true);

		Logger.getInstance().setDebuggable(debuggable);

		// Setup API ROOT
		CJayConstant.initBetaApi(false);

		Ion.getDefault(getBaseContext()).configure().setLogging("Ion", Log.INFO);

		super.onCreate();
		databaseManager = new DatabaseManager();
		httpRequestWrapper = new HttpRequestWrapper();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
																				.cacheOnDisc(true)
																				.bitmapConfig(Bitmap.Config.RGB_565)
																				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
																				.showImageForEmptyUri(R.drawable.ic_app)
																				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(	defaultOptions)
																										.discCacheSize(	500 * 1024 * 1024)
																										.memoryCache(	new WeakMemoryCache())
																										.threadPoolSize(3)
																										.threadPriority(Thread.MAX_PRIORITY)
																										.build();

		ACRA.init(CJayApplication.this);
		ACRA.getErrorReporter().checkReportsOnApplicationStart();

		ImageLoader.getInstance().init(config);
		CJayClient.getInstance().init(httpRequestWrapper, databaseManager);
		DataCenter.getInstance().initialize(databaseManager);
		databaseManager.getHelper(getApplicationContext());

		if (!CJayConstant.APP_DIRECTORY_FILE.exists()) {
			CJayConstant.APP_DIRECTORY_FILE.mkdir();
		}

		if (!CJayConstant.HIDDEN_APP_DIRECTORY_FILE.exists()) {
			CJayConstant.HIDDEN_APP_DIRECTORY_FILE.mkdir();
		}

		if (!CJayConstant.BACK_UP_DIRECTORY_FILE.exists()) {
			CJayConstant.BACK_UP_DIRECTORY_FILE.mkdir();
		}

		// Configure Alarm Manager
		// TODO: refactor if needed
		mContext = getApplicationContext();
		if (!Utils.isAlarmUp(mContext)) {

			Logger.w("Alarm Manager is not running.");
			Utils.startAlarm(mContext);

		} else {
			Logger.Log("Alarm is already running "
					+ StringHelper.getCurrentTimestamp(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE));
		}

		// Utils.cancelThenStartAlarm(mContext);

		if (NetworkHelper.isConnected(this)) {
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_NO_CONNECTION, false);
		} else {
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_NO_CONNECTION, true);
		}

	}

	public static CJayApplication getApplication(Context context) {
		return (CJayApplication) context.getApplicationContext();
	}

	public ExecutorService getSingleThreadExecutorService() {
		if (null == mSingleThreadExecutor || mSingleThreadExecutor.isShutdown()) {
			mSingleThreadExecutor = Executors.newSingleThreadExecutor(new PhotupThreadFactory(THREAD_FILTERS));
		}
		return mSingleThreadExecutor;
	}

	public static final String THREAD_FILTERS = "filters_thread";
	private ExecutorService mSingleThreadExecutor;
	private BitmapLruCache mImageCache;

	public BitmapLruCache getImageCache() {

		if (null == mImageCache) {
			mImageCache = new BitmapLruCache(this, CJayConstant.IMAGE_CACHE_HEAP_PERCENTAGE);
		}
		return mImageCache;

	}

	public static void logOutInstantly(Context ctx) {

		Logger.w("Access Token is expired");
		CJaySession session = CJaySession.restore(ctx);

		// TODO: Bugs
		try {
			session.deleteSession(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(ctx, LoginActivity_.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);

		// http://stackoverflow.com/questions/3007998/on-logout-clear-activity-history-stack-preventing-back-button-from-opening-l
		// Intent intent = new Intent();
		// broadcastIntent.setAction("com.package.ACTION_LOGOUT");
		// sendBroadcast(broadcastIntent);
	}

	public static void openCamera(Context ctx, String containerSessionUUID, int imageType, String activityTag) {

		Intent intent = new Intent(ctx, CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, containerSessionUUID);
		intent.putExtra(CameraActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);

		if (!TextUtils.isEmpty(activityTag)) {
			intent.putExtra(CameraActivity.SOURCE_TAG_EXTRA, activityTag);
		}

		ctx.startActivity(intent);
	}
	
	public static void openCamera(Context ctx, String containerSessionUUID, String issueUUID, int imageType, String activityTag) {

		Intent intent = new Intent(ctx, CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, containerSessionUUID);
		intent.putExtra(CameraActivity.CJAY_ISSUE_EXTRA, issueUUID);
		intent.putExtra(CameraActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);

		if (!TextUtils.isEmpty(activityTag)) {
			intent.putExtra(CameraActivity.SOURCE_TAG_EXTRA, activityTag);
		}

		ctx.startActivity(intent);
	}

	public static void openPhotoGridView(Context ctx, String uuid, String containerId, int imageType1, int imageType2,
											String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_SESSION_UUID_EXTRA, uuid);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_1_EXTRA, imageType1);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_2_EXTRA, imageType2);
		intent.putExtra(PhotoExpandableListViewActivity_.SOURCE_TAG_EXTRA, sourceTag);
		ctx.startActivity(intent);
	}

	public static void openPhotoGridView(Context ctx, String uuid, String containerId, int imageType, String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_SESSION_UUID_EXTRA, uuid);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_1_EXTRA, imageType);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra(PhotoExpandableListViewActivity_.SOURCE_TAG_EXTRA, sourceTag);
		ctx.startActivity(intent);
	}

	public static void openPhotoGridViewForImport(Context ctx, String uuid, String containerId, int fromType,
													int toType, String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_SESSION_UUID_EXTRA, uuid);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_1_EXTRA, fromType);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_COPY_TO_EXTRA, toType);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra(PhotoExpandableListViewActivity_.SOURCE_TAG_EXTRA, sourceTag);
		intent.putExtra(PhotoExpandableListViewActivity_.VIEW_MODE_EXTRA, PhotoExpandableListViewActivity_.MODE_IMPORT);
		ctx.startActivity(intent);
	}
	
	public static void openPhotoGridViewForIssue(Context ctx, String containerSessionUUID, String issueUUID,
			String containerId, String issueId, int imageType1, int imageType2, String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_SESSION_UUID_EXTRA, containerSessionUUID);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_ISSUE_UUID_EXTRA, issueUUID);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_ISSUE_ID_EXTRA, issueId);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_1_EXTRA, imageType1);
		intent.putExtra(PhotoExpandableListViewActivity_.CJAY_IMAGE_TYPE_2_EXTRA, imageType2);
		intent.putExtra(PhotoExpandableListViewActivity_.SOURCE_TAG_EXTRA, sourceTag);
		intent.putExtra(PhotoExpandableListViewActivity_.VIEW_MODE_EXTRA, PhotoExpandableListViewActivity_.MODE_ISSUE);
		ctx.startActivity(intent);
	}
	
	public static void openReportDialog(Context ctx, String cJayImageUuid, String containerSessionUUID) {
		IssueReportHelper.showReportDialog(ctx, cJayImageUuid, containerSessionUUID);
	}

	public static void openIssueAssigment(Context ctx, String imageUuid) {
		IssueReportHelper.showIssueAssigment(ctx, imageUuid);
	}

	public static void openIssueReport(Context ctx, String imageUuid) {
		IssueReportHelper.showIssueReport(ctx, imageUuid);
	}

	public static void openContainerDetailDialog(Fragment parent, String containerId, String operatorName, int mode) {
		openContainerDetailDialog(parent, containerId, operatorName, true, mode);
	}

	public static void openContainerDetailDialog(Fragment parent, String containerId, String operatorName,
													boolean operatorRequired, int mode) {
		FragmentManager fm = parent.getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog();
		addContainerDialog.setContainerId(containerId);
		addContainerDialog.setOperatorName(operatorName);
		addContainerDialog.setMode(mode);
		addContainerDialog.setParent(parent);
		addContainerDialog.isOperatorRequired = operatorRequired;
		addContainerDialog.show(fm, "add_container_dialog");
	}

	public static void startCJayHomeActivity(Context context) {

		/**
		 * Gate: 6 | Audit: 1 | Repair: 4
		 */

		Logger.Log("start CJayHome Activity");
		Intent intent = null;
		try {
			intent = new Intent(context, Utils.getHomeActivity(context));
		} catch (NullSessionException e) {
			e.printStackTrace();
		}

		if (intent != null) context.startActivity(intent);
	}

	public static void uploadContainerSesison(Context ctx, ContainerSession containerSession) {

		Logger.w("Checkout Time: " + containerSession.getRawCheckOutTime());
		ContainerSessionDaoImpl containerSessionDaoImpl = null;
		if (null == containerSessionDaoImpl) {
			try {
				containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(ctx)
													.getContainerSessionDaoImpl();
				containerSessionDaoImpl.refresh(containerSession);

				// User confirm upload
				containerSession.setUploadConfirmation(true);
				containerSession.setUploadState(UploadState.WAITING);

				containerSessionDaoImpl.update(containerSession);
			} catch (SQLException e) {

				DataCenter.getDatabaseHelper(ctx).addUsageLog(	containerSession.getContainerId()
																		+ " | Error set state and user_confirmation");
				e.printStackTrace();

				return;
			}
		}

		if (!Utils.isAlarmUp(ctx)) {
			Utils.startAlarm(ctx);
		}

		// It will trigger `UploadsFragment` Adapter
		EventBus.getDefault().post(new ContainerSessionEnqueueEvent(containerSession));
		DataCenter.getDatabaseHelper(ctx).addUsageLog(	containerSession.getContainerId()
																+ " | Added container to upload queue");
	}

	IDatabaseManager databaseManager = null;
	IHttpRequestWrapper httpRequestWrapper = null;
	static Context mContext = null;

	public static Context getContext() {
		return mContext;
	}

	public void checkInstantUploadReceiverState() {
		// SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// final boolean enabled = prefs.getBoolean(PreferenceConstants.PREF_INSTANT_UPLOAD_ENABLED, false);

		final ComponentName component = new ComponentName(this, InstantUploadReceiver.class);
		final PackageManager pkgMgr = getPackageManager();

		switch (pkgMgr.getComponentEnabledSetting(component)) {
			case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
				pkgMgr.setComponentEnabledSetting(	component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
													PackageManager.DONT_KILL_APP);
				Logger.Log("Enabled Instant Upload Receiver");
				break;

			case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
			case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
				// if (!enabled) {
				// pkgMgr.setComponentEnabledSetting( component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				// PackageManager.DONT_KILL_APP);
				// Logger.Log("Disabled Instant Upload Receiver");
				// }
				break;
		}
	}
}
