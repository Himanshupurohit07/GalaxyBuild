package com.sparc.wc.integration.f21.builders;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.sparc.wc.integration.f21.domain.SCMColorwayIdsPayload;
import com.sparc.wc.integration.f21.repository.SCMColorwayRepository;

/**
 * Builds a Colorway Ids payload.<br>
 * FIXES/AMENDMENTS:<br>
 * - Removed explicit conversion of dates to GMT when printing to output (will use server TZ).<br>
 * - Bug #8654: Fixed epich time conversion to properly use millisecond conversion.
 */
public class ColorwayIdsPayloadBuilder {
	
	private Instant fromDate;
	private Instant toDate;
	private SCMColorwayIdsPayload colorwayIds;
	
	public ColorwayIdsPayloadBuilder(SCMColorwayIdsPayload colorwayIds) {
		this.colorwayIds = colorwayIds;
	}
	
	public ColorwayIdsPayloadBuilder setFromDate(Instant fromDate) {
		this.fromDate = fromDate;
		return this;
	}
	
	public ColorwayIdsPayloadBuilder setToDate(Instant toDate) {
		this.toDate = toDate;
		return this;
	}
	
	/**
	 * Builds the fromDate & toDate date range used as criteria for pulling the colorways ids.
	 */
	private void buildDates() {
		
		if (fromDate != null) {
			colorwayIds.getData().setFromDate(fromDate.truncatedTo(ChronoUnit.SECONDS).toString());
		}
		
		if (toDate != null) {
			colorwayIds.getData().setToDate(toDate.truncatedTo(ChronoUnit.SECONDS).toString());
		}
		
	}
	
	/**
	 * Builds the list of colorway ids (sourcing configuration numbers) from PLM.
	 */
	private void buildSourcingConfigNumberList() throws Exception {
		
		Timestamp fromDateTs = null;
		Timestamp toDateTs = null;
		
		if (fromDate != null) {
			fromDateTs = new Timestamp(fromDate.toEpochMilli());
		}
		
		if (toDate != null) {
			toDateTs = new Timestamp(toDate.toEpochMilli());
		}
		
		colorwayIds.getData().getSourcingConfigNumbers().addAll(
				SCMColorwayRepository.findSourcingConfigIds(fromDateTs, toDateTs));
		
	}
	
	/**
	 * Builds the total record count of colorway ids retrieved from PLM.
	 */
	private void buildTotalRecords() {
		colorwayIds.getData().setTotalRecords(colorwayIds.getData().getSourcingConfigNumbers().size());
	}
	
	public SCMColorwayIdsPayload build() throws Exception {
		
		buildDates();
		buildSourcingConfigNumberList();
		buildTotalRecords();
		
		return colorwayIds;
	}

}
