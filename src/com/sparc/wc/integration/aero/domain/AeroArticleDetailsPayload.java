package com.sparc.wc.integration.aero.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sparc.wc.integration.aero.builders.AeroArticleDetailsPayloadBuilder;
import com.sparc.wc.integration.aero.builders.AeroCAPArticleDetailsPayloadBuilder;
import com.sparc.wc.integration.aero.builders.AeroS4ArticleDetailsPayloadBuilder;

/**
 * Payload for an Article details related to Aeropostale integration to CAP or S4.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
public class AeroArticleDetailsPayload extends AeroGenericResponse<Object, List<Map<String, Object>>> {
	
	/**
	 * Creates a builder instance to build a Aero Article payload for CAP or S4.
	 */
	public static AeroArticleDetailsPayloadBuilder newBuilder(AeroProcessesParam process) {
		
		AeroArticleDetailsPayloadBuilder instance = null;
		
		if (AeroProcessesParam.S4 == process) {
			instance = new AeroS4ArticleDetailsPayloadBuilder(new AeroArticleDetailsPayload());
		} else if (AeroProcessesParam.CAP == process) {
			instance = new AeroCAPArticleDetailsPayloadBuilder(new AeroArticleDetailsPayload());
		}
		
		return instance;
	}
	
	private AeroArticleDetailsPayload() {
		super(new ArrayList<Map<String, Object>>());
	}

	public void setData(Map<String, Object> data) {
		if (data != null) {
			getData().add(data);
		}
	}

	@Override
	public String toString() {
		return "AeroArticleDetailsPayload [toString()=" + super.toString() + "]";
	}
	
}
