package com.sparc.wc.integration.controller;

import com.ptc.core.rest.AbstractResource;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.domain.SparcCostingResponse;
import com.sparc.wc.integration.exceptions.SparcGenericException;
import com.sparc.wc.integration.services.SparcCostingService;
import com.sparc.wc.integration.services.SparcLogEntryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Controller for F21 Costing API.<br>
 * 
 * FIEXES/AMENDMENTS:<br>
 * - Task #10751 (UAT): Change target log entry subtype to "SCM Integration Log Entry".<br> 
 * 
 * @author Acnovate
 */
@Path("/sparc/boomi/f21")
@Logged
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(
        value = "This resource provides endpoints to CostSheets data",
        tags = {"F21 Costing Controller"}
)
public class F21Controller extends AbstractResource {

    private static final Logger LOGGER = LogR.getLogger(F21Controller.class.getName());

    private static final SparcCostingService  COSTING_SERVICE   = new SparcCostingService();
    private static final SparcLogEntryService LOG_ENTRY_SERVICE = new SparcLogEntryService();

    @GET
    @Produces({"application/json"})
    @Path("/costing/{sourcingNumber}")
    @ApiOperation("Retrieves the cost-sheets associated to the given sourcing config number")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "On Success",
            response = SparcCostingResponse.class
    ), @ApiResponse(
            code = 400,
            message = "If malformed URL or query parameters supplied."
    ), @ApiResponse(
            code = 404,
            message = "If no cost sheets were found."
    ), @ApiResponse(
            code = 500,
            message = "If an unexpected error occurs."
    )
    })
    public Response getCostsheets(@ApiParam @PathParam("sourcingNumber") final String sourcingNumber) {
        final long requestTs = System.currentTimeMillis();
        try {
            final SparcCostingResponse costingResponse = COSTING_SERVICE.getCostingResponse(sourcingNumber);
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_COSTING_API_CALL)
                    .request("sourcingNumber:" + sourcingNumber)
                    .response(costingResponse)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.SUCCESS)
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            return Response.status(200).entity(costingResponse.genericResponse()).build();
        } catch (Exception e) {
            final SparcApiCallLogEntry logEntry = SparcApiCallLogEntry.builder()
                    .apiCallType(SparcIntegrationConstants.F21_COSTING_API_CALL)
                    .request("sourcingNumber:" + sourcingNumber)
                    .requestTime(requestTs)
                    .responseTime(System.currentTimeMillis())
                    .flexTypePath(SparcIntegrationConstants.F21_SCM_API_CALL_LOG_ENTRY_FLEX_TYPE_PATH)
                    .status(SparcApiCallLogEntry.Status.FAIL)
                    .message(e.getMessage())
                    .build();
            LOG_ENTRY_SERVICE.log(logEntry);
            final SparcGenericException sparcGenericException = new SparcGenericException(e);
            return Response.status(sparcGenericException.getCode()).entity(sparcGenericException.getMessage()).build();
        }
    }

}
