package com.sparc.wc.integration.lucky.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;

import com.ptc.rfa.rest.AbstractRFARestService;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.lucky.domain.FCColorDetailsPayload;
import com.sparc.wc.integration.lucky.domain.FCColorIdsPayload;
import com.sparc.wc.integration.services.SparcLogEntryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.log4j.LogR;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_INDEX_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_FC_COLOR_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH;

@Path("/sparc/boomi/lucky/fc")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to get Color details applicable to FC",
        tags = {"Lucky Color Outbound Controller"}
)
public class FCColorController extends AbstractRFARestService {
	
	private static final Logger LOGGER = LogR.getLogger(FCColorController.class.getName());
	
	private static final SparcLogEntryService LOG_ENTRY_SERVICE = new SparcLogEntryService();
	
	@GET
    @Produces({"application/json"})
    @Path("/colors")
    @ApiOperation("To fetch colors unique identifiers which are eligible for FC")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = FCColorIdsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getColorIds() {
		
		final long requestTs = System.currentTimeMillis();
		
		Response colorIdsResp = null;
		
		try {
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
			String responsePayloadJSON = FCColorIdsPayload.newBuilder()
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_FC_COLOR_INDEX_API_CALL)
                    .request("")
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
            LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            colorIdsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
			
			LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_FC_COLOR_INDEX_API_CALL)
                    .request("")
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            
			LOG_ENTRY_SERVICE.log(logEntry);
            
            LOGGER.debug("Error Log Entry created for" + LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            colorIdsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
			
		}
		
		return colorIdsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/color/{colorId}")
    @ApiOperation("Retrieves the specified FC color details")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = FCColorDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getColorDetails(@ApiParam @PathParam("colorId") final String colorId) {
		
		final long requestTs = System.currentTimeMillis();
		Response colorDetailsResp = null;
		
		try {
			
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("Parameters: colorId --> " + colorId);
			
			if (colorId == null) {
				throw new Exception("Unable to process request. Required colorId parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = FCColorDetailsPayload.newBuilder()
					.setColorId(colorId)
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_FC_COLOR_DETAIL_API_CALL)
                    .request("colorId: " + colorId)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
            LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			colorDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
			
			LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
		
		} catch(Exception e) {
			
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_FC_COLOR_DETAIL_API_CALL)
                    .request("colorId: " + colorId)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            
			LOG_ENTRY_SERVICE.log(logEntry);
			
			LOGGER.debug("Error Log Entry created for" + LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            colorDetailsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
			
		}
		
		return colorDetailsResp;
	}
	
}
