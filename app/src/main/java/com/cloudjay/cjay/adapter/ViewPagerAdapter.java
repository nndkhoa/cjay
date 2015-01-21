package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cloudjay.cjay.fragment.AfterRepairFragment_;
import com.cloudjay.cjay.fragment.BeforeRepairFragment_;
import com.cloudjay.cjay.fragment.IssuePendingFragment_;
import com.cloudjay.cjay.fragment.IssueRepairedFragment_;
import com.cloudjay.cjay.model.Session;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    Context mContext;
    String mContainerID;
    String auditItemUUID;
    int mType;

    /**
     * Create view pager fragment (type == 1) for repair fragment, (type == 2) for repaired fragment
     *
     * @param context
     * @param containerID
     * @param auditItemUUID
     * @param type
     * @paramTfm
     */
    public ViewPagerAdapter(Context context, FragmentManager fm, String containerID, String auditItemUUID, int type) {
        super(fm);
        mContext = context;
        mContainerID = containerID;
        this.auditItemUUID = auditItemUUID;
        mType = type;
    }
    /**
     * Create view pager fragment (type == 1) for repair fragment, (type == 2) for repaired fragment
     *
     * @param context
     * @param containerID
     * @param type
     * @paramTfm
     */
    public ViewPagerAdapter(Context context, FragmentManager fm, String containerID,  int type) {
        super(fm);
        mContext = context;
        mContainerID = containerID;
        mType = type;
    }

    public ViewPagerAdapter(Context context, FragmentManager fm, int type) {
        super(fm);
        mContext = context;
        mType = type;
    }


    @Override
    public android.support.v4.app.Fragment getItem(int position) {
//		return fragments.get(position);
        if (mType == 1) {
            switch (position) {
                case 0:
                    return new IssuePendingFragment_().builder().build();
                case 1:
                    return new IssueRepairedFragment_().builder().build();
                default:
                    return null;
            }
        } else if (mType == 2) {
            switch (position) {
                case 0:
                    return new BeforeRepairFragment_().builder().containerID(mContainerID).auditItemUUID(auditItemUUID).build();
                case 1:
                    return new AfterRepairFragment_().builder().containerID(mContainerID).auditItemUUID(auditItemUUID).build();
                default:
                    return null;
            }

        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mType == 1) {
            switch (position) {
                case 0:
                    return "Danh sách lỗi";
                case 1:
                    return "Đã sữa chữa";
            }
            return null;
        } else if (mType == 2) {
            switch (position) {
                case 0:
                    return "Trước sữa chữa";
                case 1:
                    return "Sau sữa chữa";
            }
            return null;
        } else {
            return null;
        }
    }
}

