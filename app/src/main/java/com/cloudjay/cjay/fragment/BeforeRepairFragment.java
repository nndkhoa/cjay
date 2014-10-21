package com.cloudjay.cjay.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.GateImageAdapter;
import com.cloudjay.cjay.adapter.RepairedImageAdapter;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.enums.ImageType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by thai on 20/10/2014.
 */

@EFragment(R.layout.fragment_before_after_repaierd)
public class BeforeRepairFragment extends Fragment {
    @Bean
    DataCenter dataCenter;

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

    public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItem";

    @FragmentArg(CONTAINER_ID_EXTRA)
    public String containerID;

    @FragmentArg(AUDIT_ITEM_EXTRA)
    public AuditItem auditItem;

    @ViewById(R.id.tv_code_comp_repaired)
    TextView tvCompCode;

    @ViewById(R.id.tv_code_location_repaired)
    TextView tvLocaitonCode;

    @ViewById(R.id.tv_code_damaged_repaired)
    TextView tvDamageCode;

    @ViewById(R.id.tv_code_repair_repaired)
    TextView tvRepairCode;

    @ViewById(R.id.tv_size_repaired)
    TextView tvSize;

    @ViewById(R.id.tv_number_repaired)
    TextView tvNumber;

    @ViewById(R.id.lv_image_repaired)
    ListView lvImage;

    @ViewById(R.id.btn_camera_repaired)
    Button btnCamera;

    RepairedImageAdapter imageAdapter;

    @AfterViews
    void setup() {
        tvCompCode.setText(auditItem.getComponentCode());
        tvLocaitonCode.setText(auditItem.getLocationCode());
        tvDamageCode.setText(auditItem.getDamageCode());
        tvRepairCode.setText(auditItem.getRepairCode());
        tvSize.setText("Dài " + auditItem.getHeight() + "," + " Rộng " + auditItem.getLength());
        imageAdapter = new RepairedImageAdapter(getActivity(), R.layout.item_gridview_photo_multi_select, ImageType.AUDIT);
        List<AuditImage> auditImages = auditItem.getAuditImages();
        imageAdapter.setData(auditImages);


    }
}
