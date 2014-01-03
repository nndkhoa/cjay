package com.cloudjay.cjay;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
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
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DatabaseHelper;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.ImageLoader;

//slide 15

@EActivity(R.layout.activity_auditor_container)
@OptionsMenu(R.menu.menu_auditor_container)
public class AuditorContainerActivity extends CJayActivity {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	// private static final String TAG = "AuditorContainerActivity";

	private ArrayList<CJayImage> mFeeds;
	private FunDapter<CJayImage> mFeedsAdapter;
	private ContainerSession mContainerSession;
	private CJayImage mSelectedCJayImage;
	private CJayImage mLongClickedCJayImage;
	private ImageLoader imageLoader;

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
		try {
			imageLoader = ImageLoader.getInstance();

			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				containerIdTextView.setText(mContainerSession.getContainerId());
				populateCjayImages();
				initImageFeedAdapter(mFeeds);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		Intent intent = new Intent(this, CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPORT);
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
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(mLongClickedCJayImage == null);
		menu.findItem(R.id.menu_trash).setVisible(isDisplayed);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	public void refresh() {
		populateCjayImages();
		mFeedsAdapter.updateData(mFeeds);
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

	private void showReportDialog() {
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

	private void initImageFeedAdapter(ArrayList<CJayImage> containers) {
		BindDictionary<CJayImage> feedsDict = new BindDictionary<CJayImage>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueLocationCode());
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueDamageCode());
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueRepairCode());
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueComponentCode());
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_length,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueLength());
					}
				});
		feedsDict.addStringField(R.id.issue_height,
				new StringExtractor<CJayImage>() {
					@Override
					public String getStringValue(CJayImage item, int position) {
						return Utils.stripNull(item.getIssueHeight());
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
						}
					}
				});
		mFeedsAdapter = new FunDapter<CJayImage>(this, containers,
				R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
