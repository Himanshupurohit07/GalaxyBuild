package com.sparc.wc.integration.controller;

import com.ptc.core.rest.AbstractResource;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.domain.SparcColorwayDetails;
import com.sparc.wc.integration.domain.SparcColorwayIndexRequest;
import com.sparc.wc.integration.domain.SparcColorwayIndices;
import com.sparc.wc.integration.domain.SparcColorwayProcesses;
import com.sparc.wc.integration.domain.SparcColorwayUpdateRequest;
import com.sparc.wc.integration.domain.SparcColorwayUpdateResponse;
import com.sparc.wc.integration.domain.SparcGenericResponse;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.services.SparcColorwayService;
import com.sparc.wc.integration.services.SparcLogEntryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import wt.log4j.LogR;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sparc/boomi/lucky")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to get Style and colorway details applicable to FC and CAP",
        tags = {"Lucky Controller"}
)
public class LuckyController extends AbstractResource {

    private static final Logger               LOGGER            = LogR.getLogger(LuckyController.class.getName());
    private static final SparcColorwayService COLORWAY_SERVICE  = new SparcColorwayService();
    private static final SparcLogEntryService LOG_ENTRY_SERVICE = new SparcLogEntryService();

    @POST
    @Produces({"application/json"})
    @Path("/colorways")
    @ApiOperation("To fetch colorway unique identifiers which are updated and are eligible for FC and CAP from Lucky Seasons")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SparcColorwayIndices.class
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
    public Response getColorwayIndices(@RequestBody final SparcColorwayIndexRequest request) {
        final long requestTs = System.currentTimeMillis();
        try {
            final SparcColorwayIndices colorwayIndices = COLORWAY_SERVICE.getColorwayIndices(request);
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.LUCKY_COLORWAY_INDEX_API_CALL)
                    .request(request)
                    .response(colorwayIndices)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            return Response.status(200).entity(colorwayIndices.genericResponse()).build();
        } catch (Exception e) {
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.LUCKY_COLORWAY_INDEX_API_CALL)
                    .request(request)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            return Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
        }
    }

    @GET
    @Produces({"application/json"})
    @Path("/colorway/{colorwayId}/{process}")
    @ApiOperation("Retrieves the specified colorway details")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SparcColorwayDetails.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
    public Response getColorwayDetails(@ApiParam @PathParam("colorwayId") final String colorwayId, @ApiParam @PathParam("process") final SparcColorwayProcesses process) {
        final long requestTs = System.currentTimeMillis();
        try {
            final List<SparcColorwayDetails> colorwayDetails = COLORWAY_SERVICE.getColorwayDetails(colorwayId, process, false);
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.LUCKY_COLORWAY_DETAIL_API_CALL)
                    .request("ColorwayId:" + colorwayId + ", Type:" + process)
                    .response(colorwayDetails)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            return Response.status(200).entity(SparcGenericResponse.builder().data(colorwayDetails).build()).build();
        } catch (Exception e) {
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.LUCKY_COLORWAY_DETAIL_API_CALL)
                    .request("ColorwayId:" + colorwayId + ", Type:" + process)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            return Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
        }
    }

    @PUT
    @Produces({"application/json"})
    @Path("/product")
    @ApiOperation("To update a product using its colorwayId")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SparcColorwayUpdateResponse.class
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
    public Response updateColorway(final @RequestBody List<SparcColorwayUpdateRequest> request) {
        final long requestTs = System.currentTimeMillis();
        final SparcColorwayUpdateResponse response = COLORWAY_SERVICE.updateColorway(request);
        final SparcApiCallLogEntry.Status status = COLORWAY_SERVICE.getTransactionStatus(response.getErrors());
        final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                .apiCallType(SparcIntegrationConstants.LUCKY_COLORWAY_UPDATE_API_CALL)
                .request(request)
                .response(response)
                .requestTime(requestTs)
                .responseTime(System.currentTimeMillis())
                .flexTypePath(SparcIntegrationConstants.LUCKY_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                .status(status)
                .build();
        LOG_ENTRY_SERVICE.log(logEntry);
        return Response.status(status == SparcApiCallLogEntry.Status.FAIL ? 500 : 200).entity(SparcGenericResponse.builder().data(response).build()).build();
    }

}
