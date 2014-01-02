package com.cloudjay.cjay;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_auditor_issue_assignment)
@OptionsMenu(R.menu.menu_audit_issue_report)
public class AuditorIssueAssigmentActivity extends CJayActivity {

	public static final String CJAY_IMAGE_EXTRA = "cjay_image";
	
	private ArrayList<Issue> mFeeds;
	private FunDapter<Issue> mFeedsAdapter;
	private ContainerSession mContainerSession;
	private CJayImage mCJayImage;
	private Issue mSelectedIssue;

	@Extra(CJAY_IMAGE_EXTRA)		String mCJayImageUUID = "";

	@ViewById(R.id.item_picture)	ImageView imageView;
	@ViewById(R.id.feeds)			ListView mFeedListView;
	
	@AfterViews
	void afterViews() {
		try {
			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			mCJayImage = cJayImageDaoImpl.findByUuid(mCJayImageUUID);

			if (mCJayImage != null) {
				imageView.setImageBitmap(Utils.decodeImage(getContentResolver(), mCJayImage.getOriginalPhotoUri(), Utils.MINI_THUMBNAIL_SIZE));
				mContainerSession = mCJayImage.getContainerSession();
			}
			
			if (mContainerSession != null) {
				mFeeds = new ArrayList<Issue>(mContainerSession.getIssues());
				initImageFeedAdapter(mFeeds);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
		mSelectedIssue = null;
	}
	
	@OptionsItem(R.id.menu_check)
	void checkMenuItemClicked() {
		// assign issue to image
		mCJayImage.setIssue(mSelectedIssue);
		
		// save db records
		try {			
			CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(this).getCJayImageDaoImpl();
			cJayImageDaoImpl.update(mCJayImage);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// go back
		this.onBackPressed();
	}
	
	@ItemClick(R.id.feeds)
	void issueItemClicked(int position) {
		mSelectedIssue = mFeedsAdapter.getItem(position);

		// refresh menu
		supportInvalidateOptionsMenu();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isDisplayed = !(mSelectedIssue == null);
		menu.findItem(R.id.menu_check).setVisible(isDisplayed);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	private void initImageFeedAdapter(ArrayList<Issue> containers) {
		BindDictionary<Issue> feedsDict = new BindDictionary<Issue>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLocationCode());
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getDamageCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getRepairCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
//						return item.getIssue().getDamageCode().getCode();
						// TODO
						return Utils.stripNull(null);
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_length,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLength());
					}
				});
		feedsDict.addStringField(R.id.issue_height,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getHeight());
					}
				});
		feedsDict.addDynamicImageField(R.id.issue_picture,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						for (CJayImage cJayImage : item.getCJayImages()) {
							return cJayImage.getUri();
						}
						return null;
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
		mFeedsAdapter = new FunDapter<Issue>(this, containers, R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
