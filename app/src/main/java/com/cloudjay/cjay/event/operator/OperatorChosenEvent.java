package com.cloudjay.cjay.event.operator;

import com.cloudjay.cjay.model.Operator;

public class OperatorChosenEvent {

	private Operator operator;

	public OperatorChosenEvent(Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}


}
