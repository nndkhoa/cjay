package com.cloudjay.cjay.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.Util;

import org.androidannotations.annotations.BeforeTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EFragment(R.layout.fragment_search)
public class SearchFragment extends Fragment {

	@ViewById(R.id.btn_search)
	Button btnSearch;

	@ViewById(R.id.et_search)
	EditText etSearch;

	Pattern pattern = Pattern.compile("^[a-zA-Z]{4}");

	public SearchFragment() {
	}

	@Click(R.id.btn_search)
	void buttonSearchClicked() {
		String containerID = etSearch.getText().toString();
		if (TextUtils.isEmpty(containerID)) {
			etSearch.setError(getString(R.string.dialog_container_id_required));

		} else if (!Util.simpleValid(containerID)) {
			etSearch.setError(getString(R.string.dialog_container_id_invalid));
			return;
		} else {
			List<Session> result = searchSession(containerID);
			if (result != null) {
				refreshListView();
			} else {
				showAddContainerDialog(containerID);
			}
			etSearch.setText("");
		}
	}

	@BeforeTextChange(R.id.et_search)
	void beforeTextChangedOnHelloTextView(EditText editText, CharSequence s) {
		if (s.length() == 0) {
			etSearch.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		}
	}

	@TextChange(R.id.et_search)
	void onTextChangedOnButtonSearch(EditText editText, CharSequence s) {
		Matcher matcher = pattern.matcher(s);
		if (s.length() < 4) {
			if (etSearch.getInputType() != InputType.TYPE_CLASS_TEXT) {
				etSearch.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}

		} else if (matcher.matches()) {

			if (etSearch.getInputType() != InputType.TYPE_CLASS_NUMBER) {
				etSearch.setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_VARIATION_NORMAL);
			}
		}
	}

	//TODO refresh list view after search
	private void refreshListView() {

	}

	//TODO add logic search
	private List<Session> searchSession(String containeriD) {
		return null;
	}

	private void showAddContainerDialog(String containerID) {
		FragmentManager fragmentManager = getChildFragmentManager();
		AddContainerDialog addContainerDialog = AddContainerDialog_.builder().containerID(containerID).build();
		addContainerDialog.show(fragmentManager, "fragment_addcontainer");
	}
}
