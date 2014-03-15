package com.cloudjay.cjay;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;

@EActivity(R.layout.activity_gate_photo_viewer)
public class PhotoViewerActivity extends CJayActivity {

	public static final String CJAY_CONTAINER_SESSION_EXTRA = "cjay_container_session";
	private ContainerSession mContainerSession;
	
	@ViewById(R.id.gridview) GridView gridView;	
	
	@Extra(CJAY_CONTAINER_SESSION_EXTRA) String containerSessionUUID;
	
	@AfterViews
	void afterViews() {
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(this)
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl.queryForId(containerSessionUUID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		gridView.setAdapter(new ImageAdapter(this, (List<CJayImage>)mContainerSession.getCJayImages()));
	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    private List<CJayImage> mCJayImages;

	    public ImageAdapter(Context c, List<CJayImage> cJayImages) {
	        mContext = c;
	        mCJayImages = cJayImages;
	    }

	    public int getCount() {
	        return mCJayImages.size();
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        try {
		        Uri uri = Uri.parse(mCJayImages.get(position).getUri());
		        int size = 250;
		        imageView.setImageBitmap(Utils.decodeImage(mContext.getContentResolver(), uri, size));
	        } catch (FileNotFoundException e) {
	        	e.printStackTrace();
	        }
	        
	        return imageView;
	    }
	}
}