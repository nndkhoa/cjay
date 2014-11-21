package com.cloudjay.cjay.event.isocode;

import com.cloudjay.cjay.model.IsoCode;

/**
 * Created by nambv on 2014/11/21.
 */
public class IsoCodesGotToUpdateEvent {

    private IsoCode componentCode;
    private IsoCode damageCode;
    private IsoCode repairCode;

    public IsoCode getComponentCode() {
        return this.componentCode;
    }

    public IsoCode getDamageCode() {
        return this.damageCode;
    }

    public IsoCode getRepairCode() {
        return this.repairCode;
    }

    public IsoCodesGotToUpdateEvent(IsoCode componentCode, IsoCode damageCode, IsoCode repairCode) {
        this.componentCode = componentCode;
        this.damageCode = damageCode;
        this.repairCode = repairCode;
    }
}
