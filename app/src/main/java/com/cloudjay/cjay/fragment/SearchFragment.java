package com.cloudjay.cjay.fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.cloudjay.cjay.R;
import butterknife.InjectView;

import com.cloudjay.cjay.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SearchFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    View rootView;
    Button btnSearch;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(int sectionNumber) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        btnSearch = (Button) rootView.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContainerDialog();
            }
        });

        return rootView;
    }

    private void showAddContainerDialog() {
        FragmentManager fragmentManager = getChildFragmentManager();
        AddContainerDialog addContainerDialog = new AddContainerDialog(getActivity());
        addContainerDialog.show(fragmentManager, "fragment_addcontainer");
    }
}
