package com.cloudjay.cjay;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.activity_user_log)
public class UserLogActivity extends Activity implements
		LoaderCallbacks<Cursor> {

	@ViewById(R.id.btn_search)
	Button searchButton;

	@ViewById(R.id.editText_search_content)
	EditText searchEditText;

	@ViewById(R.id.lv_user_log)
	ListView listView;

	public static ArrayList<String> userLogs = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@AfterViews
	void initialize() {
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, userLogs) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				TextView hashView;
				if (convertView == null) {
					hashView = new TextView(getApplicationContext());
				} else
					hashView = (TextView) convertView;

				// Get the message from list and set as text
				String message = userLogs.get(position);
				hashView.setText(message);

				// Pattern to find if there's a hash tag in the message
				// i.e. any word starting with a # and containing letter or
				// numbers or _
				Pattern tagMatcher = Pattern.compile("[#]+[A-Za-z0-9-_]+\\b");

				// Scheme for Linkify, when a word matched tagMatcher pattern,
				// that word is appended to this URL and used as content URI
				String newActivityURL = "content://com.sourabhsoni.hashtags.tagdetailsactivity/";

				// Attach Linkify to TextView
				Linkify.addLinks(hashView, tagMatcher, newActivityURL);

				return hashView;
			}
		};

		listView.setAdapter(adapter);
	}

	@Click(R.id.btn_search)
	void searchButtonClicked() {

		String searchText = searchEditText.getText().toString();

		if (TextUtils.isEmpty(searchText)) {
			return;
		}

		Toast.makeText(getApplicationContext(), "Keyword: " + searchText,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}
}
