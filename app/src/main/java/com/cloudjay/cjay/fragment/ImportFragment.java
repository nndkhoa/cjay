package com.cloudjay.cjay.fragment;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.cloudjay.cjay.R;
<<<<<<< HEAD
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.network.NetworkClient;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.account.AccountGeneral;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Fragment in MainProcessActivity
=======

/**
 * Màn hình nhập
>>>>>>> dcb8b6f96957d05ece6bd1cba07d2947c343c687
 */
public class ImportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Declear Controls and Views */
    Button btnCamera;
    Button btnContinue;
    Button btnComplete;
    Spinner spOperator, spStatus;
    EditText etContainerCode;
    ListView lvImages;
    View v;


    public static final int LOADER_OPERATOR = 1;  //Loader identifier for operators;
    public static final int LOADER_STATUS = 2;    //Loader identifier for statuses;

<<<<<<< HEAD
    protected final AccountManager mAccountManager;
    protected Account mConnectedAccount;

    /* Declare Adapters */
    SimpleCursorAdapter mOperatorAdapter, mStatusAdapter;

    public ImportFragment(Context context) {
        // Required empty public constructor
        mAccountManager = AccountManager.get(context);
=======
    public ImportFragment() {
>>>>>>> dcb8b6f96957d05ece6bd1cba07d2947c343c687
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
        /*mStatusAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null,
                new String[] {}, new int[] {android.R.id.text1}, 0);*/

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

        Logger.i("Get operators from server");
        /* Get operators from server */
        mConnectedAccount = new Account("giamdinhcong@test.com", AccountGeneral.ACCOUNT_TYPE);
        String authToken = "Token " + mAccountManager.peekAuthToken(mConnectedAccount, AccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS);
        List<Operator> operators = NetworkClient.getInstance().getOperators(getActivity(), authToken, null);
        ContentValues addValues[] = new ContentValues[operators.size()];
        int i = 0;
        for (Operator operator : operators) {
            addValues[i++] = operator.getContentValues();
        }
        getActivity().getContentResolver().bulkInsert(Operator.URI, addValues);

        getLoaderManager().initLoader(LOADER_OPERATOR, null, this);

        return v;
    }

    private void getControl() {
        btnCamera = (Button) v.findViewById(R.id.btn_camera);
        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        btnComplete = (Button) v.findViewById(R.id.btn_complete);
        etContainerCode = (EditText) v.findViewById(R.id.et_container_id);
        spOperator = (Spinner) v.findViewById(R.id.sp_operator);
        spStatus = (Spinner) v.findViewById(R.id.sp_status);
        lvImages = (ListView) v.findViewById(R.id.lv_image);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_OPERATOR:
                Logger.i ("onCreateLoader - Operator");
                return new CursorLoader(getActivity(), Operator.URI,
                        null, null, null, null);
            case LOADER_STATUS:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_OPERATOR:
                Logger.i("onLoadFinished - Operator");
                onLoadFinishedOperator(data);
                break;
            case LOADER_STATUS:
                Logger.i("onLoadFinished - Status");
                onLoadFinishedStatus(data);
                break;
        }
    }

    private void onLoadFinishedOperator(Cursor data) {
        mOperatorAdapter.swapCursor(data);
    }

    private void onLoadFinishedStatus(Cursor data) {
        mStatusAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_OPERATOR:
                Logger.i("onLoaderReset - Operator");
                mOperatorAdapter.swapCursor(null);
                break;
            case LOADER_STATUS:
                mStatusAdapter.swapCursor(null);
                break;
        }
    }
}
