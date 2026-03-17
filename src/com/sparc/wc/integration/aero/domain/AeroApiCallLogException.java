package com.sparc.wc.integration.aero.domain;

/**
 * Exception to handle Aero Integrations catch-able execution conditions and support the Aero Log Entry model.
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development" 
 */
public class AeroApiCallLogException extends Exception {
	
	private static final long serialVersionUID = 2924886341328238366L;
	
	private String colorwayNumber;
	private String seasonType;
	private String seasonYear;
	private AeroApiCallLogEntry.ErrorTypes errorType = AeroApiCallLogEntry.ErrorTypes.ERROR;
	
	public AeroApiCallLogException(String message, AeroApiCallLogEntry.ErrorTypes errorType) {
		this(message, null, null, null, errorType);
	}
	
	public AeroApiCallLogException(String message, String colorwayNumber, AeroApiCallLogEntry.ErrorTypes errorType) {
		this(message, colorwayNumber, null, null, errorType);
	}
	
	public AeroApiCallLogException(String message, String colorwayNumber, String seasonType, String seasonYear, AeroApiCallLogEntry.ErrorTypes errorType) {
		super(message);
		this.colorwayNumber = colorwayNumber;
		this.seasonType = seasonType;
		this.seasonYear = seasonYear;
		this.errorType = errorType;
	}

	public AeroApiCallLogException(Throwable cause, String colorwayNumber, String seasonType, String seasonYear, AeroApiCallLogEntry.ErrorTypes errorType) {
		super(cause);
		this.colorwayNumber = colorwayNumber;
		this.seasonType = seasonType;
		this.seasonYear = seasonYear;
		this.errorType = errorType;
	}

	public AeroApiCallLogException(String message, Throwable cause, String colorwayNumber, String seasonType, String seasonYear, AeroApiCallLogEntry.ErrorTypes errorType) {
		super(message, cause);
		this.colorwayNumber = colorwayNumber;
		this.seasonType = seasonType;
		this.seasonYear = seasonYear;
		this.errorType = errorType;
	}
	
	public AeroApiCallLogException(String message) {
		super(message);
	}

	public AeroApiCallLogException(Throwable cause) {
		super(cause);
	}

	public AeroApiCallLogException(String message, Throwable cause) {
		super(message, cause);
	}

	public AeroApiCallLogException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public String getColorwayNumber() {
		return colorwayNumber;
	}

	public void setColorwayNumber(String colorwayNumber) {
		this.colorwayNumber = colorwayNumber;
	}

	public String getSeasonType() {
		return seasonType;
	}

	public void setSeasonType(String seasonType) {
		this.seasonType = seasonType;
	}

	public String getSeasonYear() {
		return seasonYear;
	}

	public void setSeasonYear(String seasonYear) {
		this.seasonYear = seasonYear;
	}

	public AeroApiCallLogEntry.ErrorTypes getErrorType() {
		return errorType;
	}

	public void setErrorType(AeroApiCallLogEntry.ErrorTypes errorType) {
		this.errorType = errorType;
	}

	@Override
	public String toString() {
		return "AeroApiCallLogException [colorwayNumber=" + colorwayNumber + ", seasonType=" + seasonType
				+ ", seasonYear=" + seasonYear + ", errorType=" + errorType + "]";
	}
	
}
