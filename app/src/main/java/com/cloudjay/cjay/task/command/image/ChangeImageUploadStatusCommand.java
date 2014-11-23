package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.operator.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 *
 * 1. Tìm kiếm từ database với keyword được cung cấp (không hỗ trợ full text search)
 * 2. Post kết quả tìm được (nếu có) thông qua EventBus
 * 3. Nếu không tìm thấy ở trên client thì tiến hành search ở server.
 *
 */
public class ChangeImageUploadStatusCommand extends Command {

	Context context;
	String containerId;
	String imageName;
	ImageType imageType;
	UploadStatus uploadStatus;

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.changeImageUploadStatus(context, containerId, imageName, imageType, uploadStatus);
	}

	public ChangeImageUploadStatusCommand(Context context, String containerId, String imageName, ImageType imageType, UploadStatus status) {
		this.context = context;
		this.containerId = containerId;
		this.imageName = imageName;
		this.imageType = imageType;
		this.uploadStatus = status;
	}
}
