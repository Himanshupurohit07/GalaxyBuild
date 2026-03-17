package com.sparc.wc.integration.aero.domain;

import com.google.gson.GsonBuilder;

/**
 * A wrapper for generating standard JSON output payloads.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroGenericResponse<M, D> {
	
	private M meta;
    private D data;

	public AeroGenericResponse(M meta, D data) {
		this.meta = meta;
		this.data = data;
	}
	
	public AeroGenericResponse(D data) {
		this.data = data;
	}
	
	public M getMeta() {
		return meta;
	}

	public void setMeta(M meta) {
		this.meta = meta;
	}

	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}

	public String toJSON() {
		return new GsonBuilder().serializeNulls().create().toJson(this);
	}
	
}
