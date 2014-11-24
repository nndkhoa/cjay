package com.cloudjay.cjay.event.isocode;

import com.cloudjay.cjay.model.IsoCode;

public class IsoCodeGotEvent {

	private IsoCode isoCode;
	private String prefix;

	public IsoCodeGotEvent(IsoCode isoCode, String prefix) {
		this.isoCode = isoCode;
		this.prefix = prefix;
	}

	public IsoCode getIsoCode() {
		return this.isoCode;
	}

	public String getPrefix() {
		return this.prefix;
	}
}
