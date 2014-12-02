package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;

import de.greenrobot.event.EventBus;

/**
 * 1. Tìm kiếm từ database với keyword được cung cấp (không hỗ trợ full text search)
 * 2. Post kết quả tìm được (nếu có) thông qua EventBus
 * 3. Nếu không tìm thấy ở trên client thì tiến hành search ở server.
 */
public class ChangeImageUploadStatusCommand extends Command {

	Context context;
	String containerId;
	String imageName;
	ImageType imageType;
	UploadStatus uploadStatus;
	UploadObject object;

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.changeImageUploadStatus(context, containerId, imageName, imageType, uploadStatus);
		if (uploadStatus == UploadStatus.COMPLETE) {
			EventBus.getDefault().post(new UploadSucceededEvent(containerId, UploadType.IMAGE));
		}
	}

	public ChangeImageUploadStatusCommand(Context context, String containerId, String imageName, ImageType imageType, UploadStatus status, UploadObject object) {
		this.context = context;
		this.containerId = containerId;
		this.imageName = imageName;
		this.imageType = imageType;
		this.uploadStatus = status;
		this.object = object;
	}
}
