package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.navigation.Navigator;

public class LoginActivity extends Activity {

	@InjectView(R.id.btn_login)
	Button mLoginButton;

	@OnClick(R.id.btn_login)
	void submit() {
		this.navigator.navigateToHome(this);
	}

	private Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
	    ButterKnife.inject(this);
	    this.navigator = new Navigator();
    }
}
