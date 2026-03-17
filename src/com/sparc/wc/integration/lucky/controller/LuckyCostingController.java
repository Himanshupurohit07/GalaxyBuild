package com.sparc.wc.integration.lucky.controller;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_INDEX_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_FC_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.LUCKY_COSTING_CAP_DETAIL_API_CALL;

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
import com.sparc.wc.integration.lucky.domain.CAPCostingDetailsPayload;
import com.sparc.wc.integration.lucky.domain.FCCostingDetailsPayload;
import com.sparc.wc.integration.lucky.domain.LuckyCostingIdsPayload;
import com.sparc.wc.integration.lucky.domain.LuckyCostingProcessesParam;
import com.sparc.wc.integration.services.SparcLogEntryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.log4j.LogR;

/**
 * Lucky Costing API Controller
 * @author Acnovate
 * @see Task #8259 3.1 Costing API - FC & CAP (Outbound)
 */
@Path("/sparc/boomi/lucky/costing")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to get Costing details applicable to FC and CAP",
        tags = {"Lucky Costing Outbound Controller"}
)
public class LuckyCostingController extends AbstractRFARestService {
	
	private static final Logger LOGGER = LogR.getLogger(LuckyCostingController.class.getName());
	
	private static final SparcLogEntryService LOG_ENTRY_SERVICE = new SparcLogEntryService();
	
	@GET
    @Produces({"application/json"})
    @Path("/ids/{process}")
    @ApiOperation("To fetch costing sourcing config ids eligible for FC or CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = LuckyCostingIdsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getCostingSourcingConfigIds(@ApiParam @PathParam("process") final LuckyCostingProcessesParam process) {
		
		final long requestTs = System.currentTimeMillis();
		
		Response costingIdsResp = null;
		
		try {
			
			String responsePayloadJSON = LuckyCostingIdsPayload.newBuilder()
					.setProcess(process)
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
	                .apiCallType(LUCKY_COSTING_INDEX_API_CALL)
	                .request("process: " + process)
	                .response(responsePayloadJSON)
	                .requestTime(requestTs)
	                .responseTime(System.currentTimeMillis())
	                .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
	                .status(SparcApiCallLogEntry.Status.SUCCESS)
	                .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
	        LOGGER.debug("Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
	        costingIdsResp = Response.status(Response.Status.OK)
	        		.entity(responsePayloadJSON)
	        		.build();
	        
	        LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch (Exception e) {
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_COSTING_INDEX_API_CALL)
                    .request("")
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
			LOGGER.debug("Error Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			costingIdsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
			
		}
		
		return costingIdsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/fc/{sourcingConfigNumber}")
    @ApiOperation("To fetch costing details eligible for FC")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = FCCostingDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getCostingForFC(@ApiParam @PathParam("sourcingConfigNumber") final String sourcingConfigNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response fcCostingDetailsResp = null;
		
		try {
			
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("Parameters: sourcingConfigNumber --> " + sourcingConfigNumber);
			
			if (sourcingConfigNumber == null) {
				throw new Exception("Unable to process request. Required sourcingConfigNumber parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = FCCostingDetailsPayload.newBuilder()
					.setSourcingConfigNumber(Long.parseLong(sourcingConfigNumber))
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_COSTING_FC_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            fcCostingDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
            
		} catch(Exception e) {
			
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_COSTING_FC_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
			LOGGER.debug("Error Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			fcCostingDetailsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
			
		}
		
		return fcCostingDetailsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/cap/{sourcingConfigNumber}")
    @ApiOperation("To fetch costing details eligible for CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = CAPCostingDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getCostingForCAP(@ApiParam @PathParam("sourcingConfigNumber") final String sourcingConfigNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response capCostingDetailsResp = null;
		
		try {
			
			LOGGER.info("Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("Parameters: sourcingConfigNumber --> " + sourcingConfigNumber);
			
			if (sourcingConfigNumber == null) {
				throw new Exception("Unable to process request. Required sourcingConfigNumber parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = CAPCostingDetailsPayload.newBuilder()
					.setSourcingConfigNumber(Long.parseLong(sourcingConfigNumber))
					.build()
					.toJSON();
			
			LOGGER.debug("Outbound response (JSON): " + responsePayloadJSON);
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_COSTING_CAP_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
            LOGGER.debug("Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            capCostingDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			
			LOGGER.error("Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(LUCKY_COSTING_CAP_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
			
			LOG_ENTRY_SERVICE.log(logEntry);
			LOGGER.debug("Error Log Entry created for" + LUCKY_COSTING_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			capCostingDetailsResp = Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
			
		}
		
		return capCostingDetailsResp;
	}

}
