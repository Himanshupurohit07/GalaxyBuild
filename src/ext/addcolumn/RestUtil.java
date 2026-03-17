package ext.addcolumn;

import com.ptc.rfa.rest.AbstractRFARestService;
import com.ptc.rfa.rest.search.FlexObjectsResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**

 */

@Path("add")
@Produces({"application/json"})
@Consumes({"application/json"})
@Api(value="/column", tags={"Custom Rest Services", "Utility"})

public class RestUtil extends AbstractRFARestService {
	
    @GET
    @Path("/util/flextype/columncount")
    @ApiOperation(response = FlexObjectsResult.class, value = "Get get the column counts for a given FlexType", notes = "Returns column counts for a given FlexType.")
    @ApiResponses({@io.swagger.annotations.ApiResponse(code = 200, message = "Valid", response = FlexObjectsResult.class), @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid uri"), @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error")})
    public Response searchInstances(
            @QueryParam("flexTypeStr")
            @ApiParam(name = "flexTypeStr", value = "com.lcs.wc.flexbom.FlexBOMPart") String flexTypeStr,
            @QueryParam("impExp") String impExp  
    ) {
        try {
            System.out.println("Returning column counts for a given FlexType [" + flexTypeStr + "] impExp ["+impExp+ "]");
            return buildResponse(new AddColRestUtilHelper().getColumnDiff(flexTypeStr,impExp)).build();
        } catch (Exception e) {
            String sError = "{ \"error\" : \"Problem in searchInstances - " + e.getMessage() + "\" }";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sError).build();
        }
    }

}
