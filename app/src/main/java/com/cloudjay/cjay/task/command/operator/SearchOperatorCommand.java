package com.cloudjay.cjay.task.command.operator;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.event.session.ContainerSearchedEvent;
import com.cloudjay.cjay.event.session.SearchAsyncStartedEvent;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 *
 * 1. Tìm kiếm từ database với keyword được cung cấp (không hỗ trợ full text search)
 * 2. Post kết quả tìm được (nếu có) thông qua EventBus
 * 3. Nếu không tìm thấy ở trên client thì tiến hành search ở server.
 *
 */
public class SearchOperatorCommand extends Command {

	Context context;
	String keyword;

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		List<Operator> operators = dataCenter.searchOperator(context, keyword);
		EventBus.getDefault().post(new OperatorsGotEvent(operators));
	}

	public SearchOperatorCommand(Context context, String keyword) {
		this.context = context;
		this.keyword = keyword;
	}
}
