
package com.cloudjay.cjay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_sample)
public class SampleFragment
    extends Fragment
{

    @ViewById
    TextView labelText;

    @AfterViews
    void afterViews() {
        Bundle bundle = getArguments();
        String label = bundle.getString("label");
        labelText.setText(label);
    }

}
