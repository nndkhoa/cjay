package com.cloudjay.cjay.event;

import android.view.Menu;

/**
 * Created by thai on 06/11/2014.
 */
public class EventMenuCreated {
	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	Menu menu;
	public EventMenuCreated(Menu menu) {
	this.menu = menu;
	}
}
