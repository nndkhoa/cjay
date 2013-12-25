package com.cloudjay.cjay;

import java.util.ArrayList;
import com.cloudjay.cjay.model.ContainerSession;
import android.content.Context;

public class PhotoUploadController {

	private final Context mContext;
	private final ArrayList<ContainerSession> mSelectedPhotoList;
	private final ArrayList<ContainerSession> mUploadingList;

	PhotoUploadController(Context context) {
		mContext = context;

		mSelectedPhotoList = new ArrayList<ContainerSession>();
		mUploadingList = new ArrayList<ContainerSession>();

		// populateFromDatabase();
	}
}
