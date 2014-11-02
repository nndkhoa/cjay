package com.cloudjay.cjay.event.operator;

import com.cloudjay.cjay.model.Operator;

import java.util.List;

/**
 * Event được trigger khi thực hiện thao tác lấy operators ở `AddContainerDialog` hoặc search operator ở `DataCenter`
 */
public class OperatorsGotEvent {

	public List<Operator> getOperators() {
		return operators;
	}

	private List<Operator> operators;

	public OperatorsGotEvent(List<Operator> operators) {
		this.operators = operators;
	}

}
