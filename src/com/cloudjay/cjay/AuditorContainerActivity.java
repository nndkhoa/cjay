package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.ViewById;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ComponentCodeDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ComponentCode;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.DatabaseHelper;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

//slide 15

@EActivity(R.layout.activity_auditor_container)
@OptionsMenu(R.menu.menu_auditor_container)
public class AuditorContainerActivity extends CJayActivity {

	private static final String LOG_TAG = "AuditorContainerActivity";
	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";

	private ArrayList<CJayImage> mFeeds;
	private FunDapter<CJayImage> mFeedsAdapter;
	
	private ContainerSession mContainerSession;
	private CJayImage mSelectedCJayImage;
	private CJayImage mLongClickedCJayImage;
	private ImageLoader imageLoader;
	private int mNewImageCount;

	@ViewById(R.id.btn_add_new)
	ImageButton mAddButton;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@ViewById(R.id.container_id_textview)
	TextView containerIdTextView;

	@Extra(CJAY_CONTAINER_SESSION_EXTRA)
	String mContainerSessionUUID = "";

	@AfterViews
	void afterViews() {
		imageLoader = ImageLoader.getInstance();

		initImageFeedAdapter(null);

		mLongClickedCJayImage = null;
		mSelectedCJayImage = null;
		mNewImageCount = 0;
	}

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mLongClickedCJayImage = null;
		supportInvalidateOptionsMenu();

		mSelectedCJayImage = mFeedsAdapter.getItem(position);
		if (mSelectedCJayImage.getIssue() != null) {
			// Already has issue, display that issue
			showIssueReport();
		} else {
			// Don't have issue, ask to whether create new issue, or assign an
			// issue
			showReportDialog();
		}
	}

	@ItemLongClick(R.id.feeds)
	void imageItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		mLongClickedCJayImage = mFeedsAdapter.getItem(position);
		supportInvalidateOptionsMenu();
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		mNewImageCount = 0;
		Intent intent = new Intent(this, CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPORT);
		intent.putExtra("tag", LOG_TAG);
		startActivity(intent);
	}

	@OptionsItem(R.id.menu_trash)
	void trashMenuItemClicked() {
		if (mLongClickedCJayImage != null) {
			boolean issueDeleted = false;

			// delete image from container session
			if (mContainerSession.getCJayImages().contains(
					mLongClickedCJayImage)) {
				mContainerSession.getCJayImages().remove(mLongClickedCJayImage);
			}

			// delete image from issue
			Issue issue = mLongClickedCJayImage.getIssue();
			if (issue != null
					&& issue.getCJayImages().contains(mLongClickedCJayImage)) {
				issue.getCJayImages().remove(mLongClickedCJayImage);
				// if issue has no image then delete the issue
				if (issue.getCJayImages().size() == 0
						&& mContainerSession.getIssues().contains(issue)) {
					mContainerSession.getIssues().remove(issue);
					issueDeleted = true;
				}
			}

			// update records in db
			try {
				DatabaseHelper databaseHelper = CJayClient.getInstance()
						.getDatabaseManager().getHelper(this);
				ContainerSessionDaoImpl containerSessionDaoImpl = databaseHelper
						.getContainerSessionDaoImpl();
				CJayImageDaoImpl cJayImageDaoImpl = databaseHelper
						.getCJayImageDaoImpl();
				IssueDaoImpl issueDaoImpl = databaseHelper.getIssueDaoImpl();

				containerSessionDaoImpl.update(mContainerSession);
				if (issueDeleted) {
					issueDaoImpl.delete(issue);
				} else {
					issueDaoImpl.update(issue);
				}
				cJayImageDaoImpl.delete(mLongClickedCJayImage);

			} catch (SQLException e) {
				e.printStackTrace();
			}

			// refresh image list
			refresh();

			// hide menu items
			mLongClickedCJayImage = null;
			mFeedListView.setItemChecked(-1, true);
			supportInvalidateOptionsMenu();
		}
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemClicked() {
		Logger.Log("Menu upload item clicked");

		if (mContainerSession.isValidForUpload(CJayImage.TYPE_REPORT)) {

			mContainerSession.setUploadType(ContainerSession.TYPE_AUDIT);
			CJayApplication.uploadContainerSesison(getApplicationContext(),
					mContainerSession);
		} else {
			Crouton.cancelAllCroutons();
			Crouton.makeText(this, R.string.alert_invalid_container,
					Style.ALERT).show();
		}

		// go back
		this.onBackPressed();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(mLongClickedCJayImage == null);

		menu.findItem(R.id.menu_trash).setVisible(isDisplayed);
		menu.findItem(R.id.menu_upload).setVisible(
				mContainerSession.isValidForUpload(CJayImage.TYPE_REPORT));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mNewImageCount > 1) {
			// when more than one images were taken continuously,
			// then go back to container list
			mNewImageCount = 0;
			this.onBackPressed();

		} else {
			// otherwise refresh the image list
			refresh();
		}
	}

	public void refresh() {
		populateCjayImages();
		mFeedsAdapter.updateData(mFeeds);
		supportInvalidateOptionsMenu();
	}

	private void populateCjayImages() {
		mFeeds = new ArrayList<CJayImage>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				containerIdTextView.setText(mContainerSession.getContainerId());

				for (CJayImage cJayImage : mContainerSession.getCJayImages()) {
					if (cJayImage.getType() == CJayImage.TYPE_REPORT) {
						mFeeds.add(cJayImage);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Trace(level = Log.INFO)
	void showReportDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_report_message)
				.setTitle(R.string.dialog_report_title)

				.setPositiveButton(R.string.dialog_report_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Issue not reported, report issue
								showIssueReport();
							}
						})

				.setNegativeButton(R.string.dialog_report_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// The issue already reported, assign this image
								// to that issue
								showIssueAssigment();
							}
						})
				.setNeutralButton(R.string.dialog_report_neutral,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								try {

									long startTime = System.currentTimeMillis();
									Logger.Log("*** Create container ve sinh ***");

									// Set issue vệ sinh
									Issue issue = new Issue();

									String val = "BXXX";
									// BXXX
									issue.setLocationCode(val);

									// DB
									val = "DB";
									DamageCode damageCode = null;
									if (val != null && !TextUtils.isEmpty(val)) {
										DamageCodeDaoImpl damageCodeDaoImpl = DataCenter
												.getDatabaseHelper(context)
												.getDamageCodeDaoImpl();
										damageCode = damageCodeDaoImpl
												.findDamageCode(val);
									}
									issue.setDamageCode(damageCode);

									// WW
									val = "WW";
									RepairCode repairCode = null;
									if (val != null && !TextUtils.isEmpty(val)) {
										RepairCodeDaoImpl repairCodeDaoImpl = DataCenter
												.getDatabaseHelper(context)
												.getRepairCodeDaoImpl();
										repairCode = repairCodeDaoImpl
												.findRepairCode(val);
									}
									issue.setRepairCode(repairCode);

									// FWA
									val = "FWA";
									ComponentCode componentCode = null;
									if (val != null && !TextUtils.isEmpty(val)) {
										ComponentCodeDaoImpl componentCodeDaoImpl = DataCenter
												.getDatabaseHelper(context)
												.getComponentCodeDaoImpl();
										componentCode = componentCodeDaoImpl
												.findComponentCode(val);
									}
									issue.setComponentCode(componentCode);

									issue.setQuantity("1");

									issue.setContainerSession(mSelectedCJayImage
											.getContainerSession());
									mSelectedCJayImage.setIssue(issue);

									IssueDaoImpl issueDaoImpl = DataCenter
											.getDatabaseHelper(context)
											.getIssueDaoImpl();
									issueDaoImpl.createOrUpdate(issue);

									CJayImageDaoImpl cJayImageDaoImpl = DataCenter
											.getDatabaseHelper(context)
											.getCJayImageDaoImpl();

									cJayImageDaoImpl
											.createOrUpdate(mSelectedCJayImage);

									long difference = System
											.currentTimeMillis() - startTime;
									Logger.w("---> Total time: "
											+ Long.toString(difference));

								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						});

		builder.show();
	}

	private void showIssueReport() {

		Intent intent = new Intent(this, AuditorIssueReportActivity_.class);
		intent.putExtra(AuditorIssueReportActivity_.CJAY_IMAGE_EXTRA,
				mSelectedCJayImage.getUuid());
		startActivity(intent);

	}

	private void showIssueAssigment() {

		Intent intent = new Intent(this, AuditorIssueAssigmentActivity_.class);
		intent.putExtra(AuditorIssueAssigmentActivity_.CJAY_IMAGE_EXTRA,
				mSelectedCJayImage.getUuid());
		startActivity(intent);

	}

	@Trace(level = Log.INFO)
	void initImageFeedAdapter(ArrayList<CJayImage> containers) {

		BindDictionary<CJayImage> feedsDict = new BindDictionary<CJayImage>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item
								.getIssueLocationCode());
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item
								.getIssueDamageCode());
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item
								.getIssueRepairCode());
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item
								.getIssueComponentCode());
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item.getIssueQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_length,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item.getIssueLength());
					}
				});
		feedsDict.addStringField(R.id.issue_height,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.replaceNullBySpace(item.getIssueHeight());
					}
				});
		feedsDict.addDynamicImageField(R.id.issue_picture,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return item.getUri();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String url, ImageView view) {
						if (url != null && !TextUtils.isEmpty(url)) {
							imageLoader.displayImage(url, view);
						} else {
							view.setImageResource(R.drawable.ic_app);
						}
					}
				});
		mFeedsAdapter = new FunDapter<CJayImage>(this, containers,
				R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}

	public void onEvent(CJayImageAddedEvent event) {
		if (event.getTag().equals(LOG_TAG)) {
			mNewImageCount++;
		}
	}
}
