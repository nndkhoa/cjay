package com.cloudjay.cjay.task.command.isocode;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.isocode.IsoCodesGotEvent;
import com.cloudjay.cjay.model.IsoCode;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import java.util.List;

import de.greenrobot.event.EventBus;

public class GetListIsoCodesCommand extends Command {

    Context context;
    String prefix;

    public GetListIsoCodesCommand(Context context, String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        List<IsoCode> isoCodes = dataCenter.getListIsoCodes(context, prefix);
        EventBus.getDefault().post(new IsoCodesGotEvent(isoCodes, prefix));
    }
}
