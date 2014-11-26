package com.cloudjay.cjay.task.command.session;

import android.content.Context;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.ContainersFetchedEvent;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class FetchSessionsCommand extends Command {

	Context context;
	String modifiedDate;
	boolean fetchWithFirstPageTime;

	public FetchSessionsCommand(Context context, String modifiedDate, boolean fetchWithFirstPageTime) {
		this.context = context;
		this.modifiedDate = modifiedDate;
		this.fetchWithFirstPageTime = fetchWithFirstPageTime;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter_.getInstance_(context).fetchSession(context, modifiedDate, fetchWithFirstPageTime);
		EventBus.getDefault().post(new ContainersFetchedEvent());
	}
}
