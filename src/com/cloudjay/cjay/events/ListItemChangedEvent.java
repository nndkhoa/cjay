package com.cloudjay.cjay.events;

public class ListItemChangedEvent {
	private int position;
	private int count;

	public ListItemChangedEvent() {

	}

	public ListItemChangedEvent(int position, int count) {
		setCount(count);
		setPosition(position);
	}

	public int getCount() {
		return count;
	}

	public int getPosition() {
		return position;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
