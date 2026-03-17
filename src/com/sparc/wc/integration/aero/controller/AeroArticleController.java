package com.sparc.wc.integration.aero.controller;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_LOGGER_NAME;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_INDEX_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_S4_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_CAP_DETAIL_API_CALL;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_ARTICLE_CAP_UPDATE_API_CALL;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;

import com.ptc.rfa.rest.AbstractRFARestService;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.sparc.wc.integration.aero.domain.AeroArticleIdsPayload;
import com.sparc.wc.integration.aero.domain.AeroGenericResponse;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogEntry;
import com.sparc.wc.integration.aero.domain.AeroApiCallLogException;
import com.sparc.wc.integration.aero.domain.AeroArticleDetailsPayload;
import com.sparc.wc.integration.aero.domain.AeroProcessesParam;
import com.sparc.wc.integration.aero.loaders.AeroArticlePayloadLoader;
import com.sparc.wc.integration.aero.services.AeroLogEntryService;
import com.sparc.wc.integration.domain.SparcColorwayUpdateRequest;
import com.sparc.wc.integration.domain.SparcColorwayUpdateResponse;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.util.SparcIntegrationUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.log4j.LogR;

/**
 * Article API Controller for Aeropostale Integration to S4 & CAP systems.<br>
 * 
 * FIXES/AMENDMENTS:<br>
 * - Task #9989 (UAT): Article Update: Log entry logging enhancements.<br>
 * 
 * @author Acnovate
 * @see "Task #9424 Aero - Development"
 * @see "Task #9645 Aero - Article Interface"
 * @see "Task #9647 Aero - Colorway Inbound"
 */
@Path("/sparc/boomi/aero/article")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to send or update Article details applicable to S4 and CAP",
        tags = {"Aeropostale Article Controller"}
)
public class AeroArticleController extends AbstractRFARestService {
	
	private static final Logger LOGGER = LogR.getLogger(AERO_ARTICLE_LOGGER_NAME);
	
	@GET
    @Produces({"application/json"})
    @Path("/ids/{process}")
    @ApiOperation("To fetch article colorway numbers eligible for S4 or CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroArticleIdsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getArticleColorwayNumberIds(@ApiParam @PathParam("process") final AeroProcessesParam process) {
		
		final long requestTs = System.currentTimeMillis();
		
		Response articleIdsResp = null;
		
		try {
			
			LOGGER.info("[getArticleColorwayNumberIds] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
			if (process == null) {
				throw new SparcGenericException("Expected process parameter is missing.", 
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			
			String responsePayloadJSON = new AeroGenericResponse<Object, AeroArticleIdsPayload>(
					AeroArticleIdsPayload.newBuilder()
					.setProcess(process)
					.build())
					.toJSON();
			
			LOGGER.debug("[getArticleColorwayNumberIds] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
	                .apiCallType(AERO_ARTICLE_INDEX_API_CALL)
	                .request("process: " + process)
	                .response(responsePayloadJSON)
	                .requestTime(requestTs)
	                .responseTime(System.currentTimeMillis())
	                .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
	                .status(AeroApiCallLogEntry.Status.SUCCESS)
	                .build();
			
			AeroLogEntryService.log(logEntry);
	        LOGGER.debug("[getArticleColorwayNumberIds] Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
	        articleIdsResp = Response.status(Response.Status.OK)
	        		.entity(responsePayloadJSON)
	        		.build();
	        
	        LOGGER.info("[getArticleColorwayNumberIds] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			
		} catch (Exception e) {
			LOGGER.error("[getArticleColorwayNumberIds] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_INDEX_API_CALL)
                    .request("process: " + process)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleColorwayNumberIds] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			articleIdsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		}
		
		return articleIdsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/s4/{colorwayNumber}")
	@ApiOperation("To fetch article details eligible for S4")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroArticleDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 404,
            message = "If colorway is not found or does not meet the expected criteria."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getArticleForS4(@ApiParam @PathParam("colorwayNumber") final String colorwayNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response s4ArticleDetailsResp = null;
		
		try {
			
			LOGGER.info("[getArticleForS4] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("[getArticleForS4] Parameters: colorwayNumber --> " + colorwayNumber);
			
			if (colorwayNumber == null) {
				throw new SparcGenericException("Unable to process request. Required colorwayNumber parameter is missing in the URL.", 
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			
			String responsePayloadJSON = AeroArticleDetailsPayload.newBuilder(AeroProcessesParam.S4)
					.setColorwayNumber(Long.parseLong(colorwayNumber))
					.build()
					.toJSON();
			
			LOGGER.debug("[getArticleForS4] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_S4_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.SUCCESS)
                    .colorwayNumber(colorwayNumber)
                    .build();
			
			AeroLogEntryService.log(logEntry);
            LOGGER.debug("[getArticleForS4] Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            s4ArticleDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("[getArticleForS4] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
            
		} catch (AeroApiCallLogException aeroEx) {
			
			LOGGER.error("[getArticleForS4] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + aeroEx.getMessage(), aeroEx);
			aeroEx.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_S4_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(aeroEx.getMessage())
                    .colorwayNumber(colorwayNumber)
                    .colorwaySeason(aeroEx.getSeasonType(), aeroEx.getSeasonYear())
                    .errorType(aeroEx.getErrorType())
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleForS4] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(Response.Status.NOT_FOUND.getStatusCode(), aeroEx.getMessage());
			s4ArticleDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		} catch(Exception e) {
			
			LOGGER.error("[getArticleForS4] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_S4_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .colorwayNumber(colorwayNumber)
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleForS4] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			s4ArticleDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		}
		
		return s4ArticleDetailsResp;
	}
	
	@GET
    @Produces({"application/json"})
    @Path("/cap/{colorwayNumber}")
	@ApiOperation("To fetch article details eligible for CAP")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = AeroArticleDetailsPayload.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 404,
            message = "If colorway is not found or does not meet the expected criteria."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
	public Response getArticleForCAP(@ApiParam @PathParam("colorwayNumber") final String colorwayNumber) {
		
		final long requestTs = System.currentTimeMillis();
		Response capArticleDetailsResp = null;
		
		try {
			
			LOGGER.info("[getArticleForCAP] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
			LOGGER.info("[getArticleForCAP] Parameters: colorwayNumber --> " + colorwayNumber);
			
			if (colorwayNumber == null) {
				throw new SparcGenericException("Unable to process request. Required colorwayNumber parameter is missing in the URL.", 
						Response.Status.BAD_REQUEST.getStatusCode());
			}
			
			String responsePayloadJSON = AeroArticleDetailsPayload.newBuilder(AeroProcessesParam.CAP)
					.setColorwayNumber(Long.parseLong(colorwayNumber))
					.build()
					.toJSON();
			
			LOGGER.debug("[getArticleForCAP] Outbound response (JSON): " + responsePayloadJSON);
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .response(responsePayloadJSON)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.SUCCESS)
                    .colorwayNumber(colorwayNumber)
                    .build();
			
			AeroLogEntryService.log(logEntry);
            LOGGER.debug("[getArticleForCAP] Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
            capArticleDetailsResp = Response.status(Response.Status.OK)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("[getArticleForCAP] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
            
		} catch (AeroApiCallLogException aeroEx) {
			
			LOGGER.error("[getArticleForCAP] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + aeroEx.getMessage(), aeroEx);
			aeroEx.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(aeroEx.getMessage())
                    .colorwayNumber(colorwayNumber)
                    .colorwaySeason(aeroEx.getSeasonType(), aeroEx.getSeasonYear())
                    .errorType(aeroEx.getErrorType())
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleForCAP] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(Response.Status.NOT_FOUND.getStatusCode(), aeroEx.getMessage());
			capArticleDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		} catch(Exception e) {
			
			LOGGER.error("[getArticleForCAP] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_DETAIL_API_CALL)
                    .request("colorwayNumber: " + colorwayNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .colorwayNumber(colorwayNumber)
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleForCAP] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			capArticleDetailsResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		}	
		
		return capArticleDetailsResp;
	}
	
	@PUT
    @Produces({"application/json"})
    @Path("/cap")
    @ApiOperation("To update an Aero article details at PLM from CAP using its colorway number")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SparcColorwayUpdateResponse.class
    ), @ApiResponse(
            code = 403,
            message = "If one ore more colorway attributes to be updated cannot be set to blank or are locked for updates."
    ), @ApiResponse(
            code = 404,
            message = "If colorway is not found due to missing or incorrect input criteria."
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
	public Response updateArticleFromCAP(final @RequestBody List<SparcColorwayUpdateRequest> request) {
		
        final long requestTs = System.currentTimeMillis();
        Response capArticleUpdateResp = null;
        
        try {
        	
        	String requestBodyParam = SparcIntegrationUtil.deserialize(request);
        	
        	LOGGER.info("[updateArticleFromCAP] Started @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
        	LOGGER.info("[updateArticleFromCAP] Parameter body --> " + requestBodyParam);

        	if (request == null || request.isEmpty()) {
        		throw new Exception("Unable to process request. Required body parameter is missing.");
        	}
        	
        	Map<SparcColorwayUpdateRequest, List<AeroApiCallLogException>> responseMap = new AeroArticlePayloadLoader().setPayload(request).load();
        	AeroApiCallLogEntry.Status updateStatus = AeroArticlePayloadLoader.resolveOverallLoadStatus(responseMap);
        	List<AeroApiCallLogEntry> errorLogEntries = AeroArticlePayloadLoader.createErrorLogEntries(responseMap);
        	
        	errorLogEntries.forEach(errLogEntry -> {
        		AeroLogEntryService.log(errLogEntry);
        		LOGGER.debug("[updateArticleFromCAP] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
        	});
        	
        	SparcColorwayUpdateResponse response = AeroArticlePayloadLoader.createUpdateResponse(responseMap);
        	String responsePayloadJSON = new AeroGenericResponse<Object, SparcColorwayUpdateResponse>(response).toJSON();
        	
        	LOGGER.debug("[updateArticleFromCAP] Outbound response (JSON): " + responsePayloadJSON);
        	LOGGER.debug("[updateArticleFromCAP] Overall Article Update Status is: " + updateStatus.getStatus());
        	
        	final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_UPDATE_API_CALL)
                    .request(request)
                    .response(response)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(updateStatus)
                    .build();
			
        	AeroLogEntryService.log(logEntry);
            LOGGER.debug("[updateArticleFromCAP] Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
        	
            capArticleUpdateResp = Response.status((updateStatus == AeroApiCallLogEntry.Status.FAIL) ? 500 : 200)
					.entity(responsePayloadJSON)
					.build();
            
            LOGGER.info("[updateArticleFromCAP] Completed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS));
            
        } catch (AeroApiCallLogException aeroEx) {
			
			LOGGER.error("[getArticleForCAP] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + aeroEx.getMessage(), aeroEx);
			aeroEx.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_UPDATE_API_CALL)
                    .request(request)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(aeroEx.getMessage())
                    .colorwayNumber(aeroEx.getColorwayNumber())
                    .colorwaySeason(aeroEx.getSeasonType(), aeroEx.getSeasonYear())
                    .errorType(aeroEx.getErrorType())
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[getArticleForCAP] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(Response.Status.NOT_FOUND.getStatusCode(), aeroEx.getMessage());
			capArticleUpdateResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
			
		} catch(Exception e) {
        	
        	LOGGER.error("[updateArticleFromCAP] Failed @ " + Instant.now().truncatedTo(ChronoUnit.SECONDS) + " -> " + e.getMessage(), e);
			e.printStackTrace();
			
			final AeroApiCallLogEntry logEntry = AeroApiCallLogEntry.builder()
                    .apiCallType(AERO_ARTICLE_CAP_UPDATE_API_CALL)
                    .request(request)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(AeroApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .errorType(AeroApiCallLogEntry.ErrorTypes.ERROR)
                    .build();
			
			AeroLogEntryService.log(logEntry);
			LOGGER.debug("[updateArticleFromCAP] Error Log Entry created for " + AERO_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH);
			
			final SparcGenericException sparcGenericException = new SparcGenericException(e);
			capArticleUpdateResp = Response.status(sparcGenericException.getCode()).entity(SparcIntegrationUtil.deserialize(sparcGenericException.getMessage())).build();
        	
        }
        
        return capArticleUpdateResp;
    }

}
