package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Operator;

import java.util.List;


public class OperatorsGotEvent {

	public List<Operator> getOperators() {
		return operators;
	}

	public List<Operator> getTargets() {
		return operators;
	}

	public Operator getTarget() {
		if (isSingleChange()) {
			return operators.get(0);
		} else {
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
		}
	}

	private List<Operator> operators;

	public OperatorsGotEvent(List<Operator> operators) {
		this.operators = operators;
	}

	public boolean isSingleChange() {
		return operators.size() == 1;
	}
}
