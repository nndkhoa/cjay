package com.cloudjay.cjay.events;

public class ListItemChangedEvent {
	private int position;
	private int count;

	public ListItemChangedEvent() {

	}

	public ListItemChangedEvent(int position, int count) {
		this.setCount(count);
		this.setPosition(position);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	
}
