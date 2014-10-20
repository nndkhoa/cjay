package com.cloudjay.cjay.task.jobqueue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.api.ApiEndpoint;
import com.cloudjay.cjay.api.NetworkClient;
import com.cloudjay.cjay.api.NetworkClient_;
import com.cloudjay.cjay.api.NetworkService;
import com.cloudjay.cjay.event.UpLoadingEvent;
import com.cloudjay.cjay.event.StartUpLoadEvent;
import com.cloudjay.cjay.event.StopUpLoadEvent;
import com.cloudjay.cjay.event.UploadedEvent;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.snappydb.SnappydbException;

import java.io.File;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by thai on 04/10/2014.
 */

public class UpLoadImageJob extends Job {
    Context context;
    String containerId;
    String uri;
    String imageName;


    public UpLoadImageJob(Context context, String uri, String imageName, String containerId) {
        super(new Params(2).requireNetwork().persist().groupBy(containerId));
        Logger.e("Create Job");
        this.context = context;
        this.containerId = containerId;
        this.uri = uri;
        this.imageName = imageName;
    }

    @Override
    public void onAdded() {
        Logger.e("Added Job");
//        try {
//            DataCenter_.getInstance_(context).addUploadingSession(containerId);
//            EventBus.getDefault().post(new StartUpLoadEvent(containerId));
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onRun() throws Throwable {
        Logger.e("Running Job");
        EventBus.getDefault().post(new UpLoadingEvent());
        //Call network client to upload image
//        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ApiEndpoint.CJAY_TMP_STORAGE).build();
//        File image = new File(uri);
//        TypedFile typedFile = new TypedFile("image/jpeg", image);
//        Response response = restAdapter.create(NetworkService.class).postImageFile("image/jpeg", "media", imageName, typedFile);
//        Logger.e("Uploaded Image");

        //Change status image in db
//        Session uploadingSession = App.getSnappyDB(context).getObject(CJayConstant.UPLOADING_DB + containerId, Session.class);
//        for (GateImage gateImage : uploadingSession.getGateImages()) {
//            if (gateImage.getName().equals(imageName)) {
//                gateImage.setUploaded(true);
//            }
//        }
//        App.getSnappyDB(context).put(CJayConstant.UPLOADING_DB + containerId, uploadingSession);
//        EventBus.getDefault().post(new UploadedEvent(containerId));


    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new StopUpLoadEvent());
        return true;
    }
}
