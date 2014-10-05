package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Operator;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class OperatorsGotEvent {

	public RealmResults<Operator> getOperators() {
		return operators;
	}

	public RealmResults<Operator> getTargets() {
		return operators;
	}

	public RealmObject getTarget() {
		if (isSingleChange()) {
			return operators.get(0);
		} else {
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
		}
	}

	private RealmResults<Operator> operators;

	public OperatorsGotEvent(RealmResults<Operator> operators) {
		this.operators = operators;
	}

	public boolean isSingleChange() {
		return operators.size() == 1;
	}
}
