package com.cloudjay.cjay.task.command.session.get;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 *
 * 1. Tìm kiếm từ database với keyword được cung cấp (không hỗ trợ full text search)
 * 2. Post kết quả tìm được (nếu có) thông qua EventBus
 * 3. Nếu không tìm thấy ở trên client thì tiến hành search ở server.
 *
 */
public class SearchCommand extends Command {

	Context context;
	String keyword;
	boolean searchInImportFragment;

	@Override
	protected void run() {

		DataCenter dataCenter = DataCenter_.getInstance_(context);
		List<Session> sessions = dataCenter.getListSessions(context, keyword, "");

		// Check if local search has results
		if (sessions.size() != 0) {
			EventBus.getDefault().post(new ContainerSearchedEvent(sessions, searchInImportFragment));
		} else {

			// If there was not result in local, send search request to server
			//  --> alert to user about that no results was found in local
			EventBus.getDefault().post(new SearchAsyncStartedEvent(context.getResources().getString(R.string.search_on_server)));
			dataCenter.searchAsync(context, keyword, searchInImportFragment);
		}
	}

	public SearchCommand(Context context, String keyword, boolean searchInImportFragment) {
		this.context = context;
		this.keyword = keyword;
		this.searchInImportFragment = searchInImportFragment;
	}
}
