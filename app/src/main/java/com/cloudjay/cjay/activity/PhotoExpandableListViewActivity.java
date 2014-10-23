package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.PhotoExpandableListAdapter;
import com.cloudjay.cjay.event.GateImagesGotEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_photo_expandablelistview)
public class PhotoExpandableListViewActivity extends Activity {

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    @Extra(CONTAINER_ID_EXTRA)
    String containerID;

    @ViewById(R.id.lv_images_expandable)
    ExpandableListView lvImagesExpandable;

    PhotoExpandableListAdapter mListAdapter;
    // HashMap<Integer, List<GateImage>> listDataChild;

    List<GateImage> gateImages = null;
    List<GateImage> importImages = new ArrayList<GateImage>();
    List<GateImage> auditImages = new ArrayList<GateImage>();
    List<GateImage> repairedImages = new ArrayList<GateImage>();
    int mNewImageCount = 0;
    int[] mImageTypes;

    @Bean
    DataCenter dataCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void setUp() {
    }

    @UiThread
    void onEvent(GateImagesGotEvent event) {
        // Get gate image objects from event post back
        gateImages = event.getGateImages();
        Logger.Log("count gate images: " + gateImages.size());

        // Init image types
        mImageTypes = new int[3];
        mImageTypes[0] = CJayConstant.TYPE_IMPORT;
        mImageTypes[1] = CJayConstant.TYPE_AUDIT;
        mImageTypes[2] = CJayConstant.TYPE_REPAIRED;

        //Init ListImages for each type
        for (GateImage g : gateImages) {
           if (g.getType() == CJayConstant.TYPE_IMPORT) {
                importImages.add(g);
           } else if (g.getType() == CJayConstant.TYPE_AUDIT) {
               auditImages.add(g);
           } else if (g.getType() == CJayConstant.TYPE_REPAIRED) {
               repairedImages.add(g);
           }
       }

        /*mListAdapter = new PhotoExpandableListAdapter(this, mImageTypes, importImages, auditImages, repairedImages);
        lvImagesExpandable.setAdapter(mListAdapter);

        for (int i = 0; i < mImageTypes.length; i++) {
            lvImagesExpandable.expandGroup(i);
        }*/
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
