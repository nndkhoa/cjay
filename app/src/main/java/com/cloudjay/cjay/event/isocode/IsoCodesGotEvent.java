package com.cloudjay.cjay.event.isocode;

import com.cloudjay.cjay.model.IsoCode;

import java.util.List;

/**
 * Created by nambv on 2014/11/13.
 */
public class IsoCodesGotEvent {

	private List<IsoCode> isoCodes;
	private String prefix;

	public IsoCodesGotEvent(List<IsoCode> isoCodes, String prefix) {
		this.isoCodes = isoCodes;
		this.prefix = prefix;
	}

	public List<IsoCode> getListIsoCodes() {
		return this.isoCodes;
	}

	public String getPrefix() {
		return this.prefix;
	}

}
