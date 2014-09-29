package com.cloudjay.cjay.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.util.account.AccountGeneral;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.util.Logger;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Account mConnectedAccount;
    CursorLoader cursorLoader;
    LoaderManager loaderManager;
    SimpleCursorAdapter cursorAdapter;
    String[] columns = new String[] {
            IsoCode.CODE,
            IsoCode.DISPLAY_NAME
    };
    int[] to = new int[] {
        R.id.tv_iso_code,
        R.id.tv_display_name
    };

    //@InjectView(R.id.btn_get_data)
    Button btnGetData;

    //@InjectView(R.id.lv_data)
    ListView lvIsoCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//				.detectDiskReads()
//				.detectDiskWrites()
//				.detectNetwork()   // or .detectAll() for all detectable problems
//				.penaltyLog()
//				.build());
//
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//				.detectLeakedSqlLiteObjects()
//				.detectLeakedClosableObjects()
//				.penaltyLog()
//				.penaltyDeath()
//				.build());
//		String result = NetworkClient.getInstance().getToken("giamdinhcong@test.com","123456");

        loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(0, null, this);

        Intent i = getIntent();

        String token = i.getStringExtra("token");
        Logger.i("token: " + token);
        /*List<IsoCode> listRepairsCode = NetworkClient.getInstance().getRepairCodes(getApplicationContext(), token, null);
        Logger.i("data counts: " + listRepairsCode.size());*/

        mConnectedAccount = new Account("giamdinhcong@test.com", AccountGeneral.ACCOUNT_TYPE);

        Logger.i("AutoSync");
        ContentResolver.setSyncAutomatically(mConnectedAccount, AccountGeneral.ACCOUNT_TYPE, true);

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.iso_code_item, null, columns, to);
        lvIsoCode = (ListView) findViewById(R.id.lv_data);
        lvIsoCode.setAdapter(cursorAdapter);
	}



    //@OnClick(R.id.btn_get_data)
    void getIsoCodeData() {

        Logger.i("Try to sync data");

        if (null == mConnectedAccount) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mConnectedAccount, AccountGeneral.ACCOUNT_TYPE, bundle);
    }

	public static Intent getCallingIntent(Context context) {
		return new Intent(context, MainActivity.class);
	}


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        cursorLoader = new CursorLoader(
                this, IsoCode.URI, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursorAdapter!=null && cursor!=null) {
            cursorAdapter.swapCursor(cursor); //swap the new cursor in.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(cursorAdapter!=null) {
            cursorAdapter.swapCursor(null);
        }
    }
}
