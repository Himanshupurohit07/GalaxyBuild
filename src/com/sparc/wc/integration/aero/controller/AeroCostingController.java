package com.sparc.wc.integration.aero.controller;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_CAP_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_S4_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COSTING_INDEX_API_CALL;

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
import com.sparc.wc.integration.aero.domain.AeroApiCallLogEntry;
import com.sparc.wc.integration.aero.domain.AeroCostingDetailsPayload;
import com.sparc.wc.integration.aero.domain.AeroCostingIdsPayload;
import com.sparc.wc.integration.aero.domain.AeroGenericResponse;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.services.AeroLogEntryService;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.log4j.LogR;

/**
 * Costing API Controller for Aeropostale Integration to S4 & CAP systems.
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 */
@Path("/sparc/boomi/aero/costing")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to send Costing details applicable to S4 and CAP",
        tags = {"Aeropostale Costing Outbound Controller"}
)
public class AeroCostingController extends AbstractRFARestService {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_COSTING_LOGGER_NAME);
	
	@GET
    @Produces({"application/json"})
    @Path("/ids/{process}")
    @ApiOperation("To fetch costing sourcing config ids eligible for S4 or CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroCostingIdsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getCostingSourcingConfigIds(@ApiParam @PathParam("process") final AeroProcessesParam process) {
		
		final long requestTs = System.currentTimeMillis();
		
		Response costingIdsResp = null;
		
		try {
			
			if (process == null) {
				throw new Exception("Expected process parameter is missing.");
			}
			
			String responsePayloadJSON = new AeroGenericResponse<Object, AeroCostingIdsPayload>(
					AeroCostingIdsPayload.newBuilder()
					.setProcess(process)
					.build())
					.toJSON();
			
			LOGGER.debug("[getCostingSourcingConfigIds] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_INDEX_API_CALL)
                    .request("process: " + process)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.SUCCESS)
                    .build();
			
			AeroLogEntryService.log(logEntry);
	        LOGGER.debug("[getCostingSourcingConfigIds] Log Entry created for" + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
	        costingIdsResp = Response.status(Response.Status.OK)
	        		.entity(responsePayloadJSON)
	        		.build();
	        
	        LOGGER.info("[getCostingSourcingConfigIds] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch (Exception e) {
			LOGGER.error("[getCostingSourcingConfigIds] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_INDEX_API_CALL)
                    .request("process: " + process)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getCostingSourcingConfigIds] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			costingIdsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		}
		
		return costingIdsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/cap/{sourcingConfigNumber}")
    @ApiOperation("To fetch costing details eligible for CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroCostingDetailsPayload.class
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
			
			LOGGER.info("[getCostingForCAP] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("[getCostingForCAP] Parameters: sourcingConfigNumber --> " + sourcingConfigNumber);
			
			if (sourcingConfigNumber == null) {
				throw new Exception("Unable to process request. Required sourcingConfigNumber parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = new AeroGenericResponse<Object, AeroCostingDetailsPayload>(
					AeroCostingDetailsPayload.newBuilder(AeroProcessesParam.CAP)
					.setSourcingConfigNumber(Long.parseLong(sourcingConfigNumber))
					.build())
					.toJSON();
			
			LOGGER.debug("[getCostingForCAP] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_CAP_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.SUCCESS)
                    .build();
			
			AeroLogEntryService.log(logEntry);
            LOGGER.debug("[getCostingForCAP] Log Entry created for" + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            capCostingDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("[getCostingForCAP] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			
			LOGGER.error("[getCostingForCAP] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_CAP_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getCostingForCAP] Error Log Entry created for" + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			capCostingDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
		}
		
		return capCostingDetailsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/s4/{sourcingConfigNumber}")
    @ApiOperation("To fetch costing details eligible for S4")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroCostingDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getCostingForS4(@ApiParam @PathParam("sourcingConfigNumber") final String sourcingConfigNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response s4CostingDetailsResp = null;
		
		try {
			
			LOGGER.info("[getCostingForS4] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("[getCostingForS4] Parameters: sourcingConfigNumber --> " + sourcingConfigNumber);
			
			if (sourcingConfigNumber == null) {
				throw new Exception("Unable to process request. Required sourcingConfigNumber parameter is missing in the URL.");
			}
			
			String responsePayloadJSON = new AeroGenericResponse<Object, AeroCostingDetailsPayload>(
					AeroCostingDetailsPayload.newBuilder(AeroProcessesParam.S4)
					.setSourcingConfigNumber(Long.parseLong(sourcingConfigNumber))
					.build())
					.toJSON();
			
			LOGGER.debug("[getCostingForS4] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_S4_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.SUCCESS)
                    .build();
			
			AeroLogEntryService.log(logEntry);
            LOGGER.debug("[getCostingForS4] Log Entry created for" + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            s4CostingDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("[getCostingForS4] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch(Exception e) {
			
			LOGGER.error("[getCostingForS4] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_COSTING_S4_DETAIL_API_CALL)
                    .request("sourcingConfigNumber: " + sourcingConfigNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getCostingForS4] Error Log Entry created for" + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			s4CostingDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		}
		
		return s4CostingDetailsResp;
	}

}
