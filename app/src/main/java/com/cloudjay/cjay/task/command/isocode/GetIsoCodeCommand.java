package com.cloudjay.cjay.task.command.isocode;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.isocode.IsoCodeGotEvent;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/23.
 */
public class GetIsoCodeCommand extends Command {

    Context context;
    String prefix;
    String code;

    public GetIsoCodeCommand(Context context, String prefix, String code) {
        this.context = context;
        this.prefix = prefix;
        this.code = code;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        IsoCode isoCode = dataCenter.getIsoCode(context, prefix, code);
        EventBus.getDefault().post(new IsoCodeGotEvent(isoCode, prefix));
    }
}
