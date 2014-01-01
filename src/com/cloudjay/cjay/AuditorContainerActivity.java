package com.cloudjay.cjay;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;

//slide 15

@EActivity(R.layout.activity_auditor_container)
public class AuditorContainerActivity extends CJayActivity {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
//	private static final String TAG = "AuditorContainerActivity";
	
	private ArrayList<CJayImage> mFeeds;
	private FunDapter<CJayImage> mFeedsAdapter;
	private ContainerSession mContainerSession;
	private CJayImage mSelectedCJayImage;

	@ViewById(R.id.btn_add_new)				ImageButton mAddButton;
	@ViewById(R.id.feeds)					ListView mFeedListView;
	@ViewById(R.id.container_id_textview)	TextView containerIdTextView;
	
	@Extra(CJAY_CONTAINER_SESSION_EXTRA)	String mContainerSessionUUID = "";

	@AfterViews
	void afterViews() {
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

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
	void containerItemClicked(int position) {
		mSelectedCJayImage = mFeedsAdapter.getItem(position);
		if (mSelectedCJayImage.getIssue() != null) {
			// Already has issue, display that issue
			showIssueReport();
		} else {
			// Don't have issue, ask to whether create new issue, or assign an issue
			showReportDialog();	
		}
	}
	
	@Click(R.id.btn_add_new)
	void cameraClicked() {
		Intent intent = new Intent(this, CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA, mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPORT);
		startActivity(intent);
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
			mContainerSession = containerSessionDaoImpl.queryForId(mContainerSessionUUID);

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
				.setPositiveButton(R.string.dialog_report_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Issue not reported, report issue
						showIssueReport();
					}
				})
				.setNegativeButton(R.string.dialog_report_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// The issue already reported, assign this image to that issue
						showIssueAssigment();
					}
				});

		builder.show();
	}
	
	private void showIssueReport() {
		Intent intent = new Intent(this, AuditorIssueReportActivity_.class);
		intent.putExtra(AuditorIssueReportActivity_.CJAY_IMAGE_EXTRA, mSelectedCJayImage.getUuid());
		startActivity(intent);
	}
	
	private void showIssueAssigment() {
		Intent intent = new Intent(this, AuditorIssueAssigmentActivity_.class);
		intent.putExtra(AuditorIssueAssigmentActivity_.CJAY_IMAGE_EXTRA, mSelectedCJayImage.getUuid());
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
//						return Utils.stripNull(item.getIssue().getDamageCode().getCode();
						// TODO
						return Utils.stripNull(null);
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
						try {
							view.setImageBitmap(Utils.decodeImage(getContentResolver(), Uri.parse(url), Utils.MINI_THUMBNAIL_SIZE));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				});
		mFeedsAdapter = new FunDapter<CJayImage>(
				this, containers, R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
