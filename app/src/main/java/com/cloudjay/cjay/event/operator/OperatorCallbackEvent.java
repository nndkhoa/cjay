package com.cloudjay.cjay.event.operator;

import com.cloudjay.cjay.model.Operator;

/**
 * Created by nambv on 07/10/2014.
 */
public class OperatorCallbackEvent {
    private Operator operator;

    public OperatorCallbackEvent(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }


}
