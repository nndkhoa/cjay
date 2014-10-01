package com.cloudjay.cjay.fragment;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.account.AccountGeneral;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Fragment in MainProcessActivity
=======

/**
 * Màn hình nhập
 */
public class ImportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Declear Controls and Views */
    Button btnCamera, btnContinue, btnComplete;
    Spinner spOperator;
    RadioGroup rdnGroupStatus;
    RadioButton rdnStatusA, rdnStatusB, rdnStatusC;
    EditText etContainerCode;
    ListView lvImages;
    View v;


    public static final int LOADER_OPERATOR = 1;  //Loader identifier for operators;

    //public AccountManager mAccountManager;

    /* Declare Adapters */
    SimpleCursorAdapter mOperatorAdapter;

    public ImportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_import, container, false);

        getControl();

        /* Init simple cursor adapter */
        mOperatorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null,
                new String[]{Operator.OPERATOR_NAME}, new int[]{android.R.id.text1}, 0);
        /* Set apdater for spinner */
        spOperator.setAdapter(mOperatorAdapter);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Go to next fragment */
                AuditFragment fragment = new AuditFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.ll_main_process, fragment);
                transaction.commit();
            }
        });

        //getActivity().getSupportLoaderManager().initLoader(LOADER_OPERATOR, null, )

        return v;
    }

    private void getControl() {
        btnCamera = (Button) v.findViewById(R.id.btn_camera);
        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        btnComplete = (Button) v.findViewById(R.id.btn_complete);
        etContainerCode = (EditText) v.findViewById(R.id.et_container_id);
        spOperator = (Spinner) v.findViewById(R.id.sp_operator);
        lvImages = (ListView) v.findViewById(R.id.lv_image);
        rdnGroupStatus = (RadioGroup) v.findViewById(R.id.rdn_group_status);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Logger.i ("onCreateLoader - Operator");
        return new CursorLoader(getActivity(), Operator.URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.i("onLoadFinished - Operator");
        mOperatorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Logger.i("onLoaderReset - Operator");
        mOperatorAdapter.swapCursor(null);
    }
}
