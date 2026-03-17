package com.sparc.wc.integration.f21.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;

import com.ptc.rfa.rest.AbstractRFARestService;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.f21.domain.SCMColorwayIdsParams;
import com.sparc.wc.integration.f21.domain.SCMColorwayIdsPayload;
import com.sparc.wc.integration.f21.domain.SCMColorwayPayload;
import com.sparc.wc.integration.services.SparcLogEntryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.log4j.LogR;

/**
 * This controller implements the following PLM API end points that are part of the integration between PLM to SCM for Forever 21 brand.
 * <li><b>getColorwayIds</b> is the End Point method that returns a list of PLM's source configuration IDs.</li>
 * <li><b>getColorwayDetails</b> is the End Point method that returns data specific to a given Source Configuration ID.</li>
 * <br>
 * FIEXES/AMENDMENTS:<br>
 * - Replaced custom logging with PLM's LogR.<br>
 * - Minor update on method comments related to parameters.<br>
 * - Minor update related to Log Entry for Colorway Details<br>
 * 
 * @author Acnovate
 */

@Path("/sparc/boomi/f21")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to get Colorways for F21/SCM integration",
        tags = {"Sparc F21/SCM Outbound Controller"}
)
public class SCMIntegrationController extends AbstractRFARestService {
	
	private static final Logger LOGGER = LogR.getLogger(SCMIntegrationController.class.getName());
	
	private static final SparcLogEntryService LOG_ENTRY_SERVICE = new SparcLogEntryService();

	/**
	 * End Point method of the PLM to SCM integration that delivers a list of PLM's source configuration IDs that meet the criteria for F21/SCM.
	 * @param requestParams FROM & TO modified date range parameters.
	 * @return The payload containing the list of eligible PLM's source configuration IDs according the specs for F21/SCM integration.
	 */
	@POST
    @Produces({"application/json"})
    @Path("/colorways")
    @ApiOperation("To fetch colorway (sourcing configuration) unique identifiers which are updated and are eligible for SCM from F21 Seasons")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SCMColorwayIdsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
    @ApiImplicitParams({@ApiImplicitParam(
            name = "CSRF_NONCE",
            value = "The CSRF nonce as returned from the /security/csrf endpoint.  See the Swagger documentation titled CSRF Protection for more information.",
            required = true,
            dataType = "string",
            paramType = "header"
    )})
	public Response getColorwayIds(@RequestBody final SCMColorwayIdsParams requestParams) {
		
		final long requestTs = System.currentTimeMillis();
		
		Instant fromDate = null;
		Instant toDate = null;
		Response colorwayIdsResp = null;
		
		try {
			
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("Parameters (raw): " + requestParams);
			
			if (requestParams != null) {
				
				if (requestParams.getFrom() >= 0) {
					fromDate = Instant.ofEpochMilli(requestParams.getFrom());
				}
				
				if (requestParams.getTo() >= 0) {
					toDate = Instant.ofEpochMilli(requestParams.getTo());
				}
				
				LOGGER.info("Paramaters (translated): from --> " + fromDate + " to --> " + toDate);
				
				if (fromDate != null && 
						toDate != null && 
						!toDate.equals(fromDate) &&
						!toDate.isAfter(fromDate)) {
					throw new Exception("Invalid parameters for date range found: 'from' date is after 'to' date.");
				}
				
				
			} else {
				LOGGER.info("No parameters found for date range. "
						+ "Modified Date range criteria will be skipped during colorway (sourcing configuration) ids lookup.");
			}
			
			String responsePayloadJSON = SCMColorwayIdsPayload.newBuilder()
					.setFromDate(fromDate)
					.setToDate(toDate)
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_SCM_COLORWAY_INDEX_API_CALL)
                    .request(requestParams)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
            LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			colorwayIdsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
			
			LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_SCM_COLORWAY_INDEX_API_CALL)
                    .request(requestParams)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            
			LOG_ENTRY_SERVICE.log(logEntry);
            
            LOGGER.debug("Error Log Entry created for" + SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            colorwayIdsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
		}
		
		return colorwayIdsResp;
	}
	
	/**
	 * End Point method of the PLM to SCM integration that returns a list of PLM's source configuration IDs.
	 * @pathparam sourceConfigNum
	 * @return The payload containing the colorway details according the specs for F21/SCM integration.
	 */
	@GET
    @Produces({"application/json"})
	@Path("/colorway/{sourcingConfigNumber}")
    @ApiOperation("Retrieves the specified colorway details")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SCMColorwayPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getColorwayDetails(@PathParam("sourcingConfigNumber") String sourcingConfigNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response colorwayDetailsResp = null;
		
		try {
			
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("Parameters: sourcingConfigNumber --> " + sourcingConfigNumber);
			
			if (sourcingConfigNumber == null) {
				throw new Exception("Unable to process request. Required sourcingConfigNumber parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = SCMColorwayPayload.newBuilder()
					.setSourcingConfigNumber(Long.parseLong(sourcingConfigNumber))
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_SCM_COLORWAY_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
            LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			colorwayDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
			
			LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_SCM_COLORWAY_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            
			LOG_ENTRY_SERVICE.log(logEntry);
            
            LOGGER.debug("Error Log Entry created for" + SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            colorwayDetailsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
		}
		
		return colorwayDetailsResp;
	}
	
}
