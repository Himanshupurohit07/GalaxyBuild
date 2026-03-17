package com.sparc.wc.integration;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.lcs.wc.util.FormatHelper;
import com.ptc.rfa.rest.AbstractRFARestService;
import com.sparc.wc.util.SparcConstants;
import com.sparc.wc.util.SparcLogger;
/** fetchArticleObjects method is used for the Article Integration Starting Point
 *  fetchCostsheetObjects method is used for the PIR Integration Starting Point
 * @author Infosys
 *
 */
@Path("/sparc/boomi")
public class SparcRestIntegration extends AbstractRFARestService {
	SparcLogger logger = SparcLogger.getInstance();
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/fetchArticles")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response fetchArticleObjects(@QueryParam("pageNo") String pageNo, @QueryParam("fetchNoOfRows") String fetchNoOfRows, @QueryParam("totalNoOfRows") String totalNoOfRows, @QueryParam("objectID") String objectID) {
		if(logger != null) {
			logger.getDebugValue(SparcConstants.INTEGRATION_DEBUG_CALL);
			logger.logInfo(SparcRestIntegration.class.getName(), "fetchArticleObjects","logger Object is "+logger);
		}
		logger.logInfo(SparcRestIntegration.class.getName(), "fetchArticleObjects", "Start");
		logger.logInfo(SparcRestIntegration.class.getName(), "fetchArticleObjects", "pageNo --> "+ pageNo +" fetchNoOfRows -->" + fetchNoOfRows + " totalNoOfRows --> "+totalNoOfRows);
		SparcRestIntegrationHelper articleHelper = new SparcRestIntegrationHelper();
		JSONObject masterPOJO = null;
		if(!FormatHelper.hasContent(totalNoOfRows)) {
			totalNoOfRows = "0";
		}
		if(FormatHelper.hasContent(pageNo) && Integer.parseInt(pageNo) > 1 && !FormatHelper.hasContent(totalNoOfRows)) {
			masterPOJO = new JSONObject();
			masterPOJO.put("Error", "TotalNoOfRows is missing");
		}else {
			masterPOJO = articleHelper.sendFinalJSON("Article", pageNo, fetchNoOfRows, totalNoOfRows, SparcConstants.CONSTANT_PRODUCT_NAME, true, objectID);
		}
		logger.logInfo(SparcRestIntegration.class.getName(), "fetchArticleObjects", "End and Master Pojo is "+masterPOJO);
		return Response.status(Response.Status.OK).entity(masterPOJO).build();
	}


	@SuppressWarnings("unchecked")
	@POST
	@Path("/fetchCostsheets")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response fetchCostsheetObjects(@QueryParam("pageNo") String pageNo, @QueryParam("fetchNoOfRows") String fetchNoOfRows, @QueryParam("totalNoOfRows") String totalNoOfRows, @QueryParam("objectID") String objectID) {
		if(logger != null) {
			logger.getDebugValue(SparcConstants.INTEGRATION_DEBUG_CALL);
		}
		logger.logInfo(SparcRestIntegration.class.getName(), "fetchCostsheetObjects", "Start");
				
		SparcRestIntegrationHelper pirHelper = new SparcRestIntegrationHelper();
		JSONObject masterPOJO;
		if(Integer.parseInt(pageNo) > 1 && !FormatHelper.hasContent(totalNoOfRows)) {
			masterPOJO = new JSONObject();
			masterPOJO.put("Error", "TotalNoOfRows is missing");
		}else {
			masterPOJO = pirHelper.sendFinalJSON("PIR", pageNo, fetchNoOfRows, totalNoOfRows, SparcConstants.CONSTANT_SOURCING_NAME, false, objectID);
		}
		logger.logInfo(SparcRestIntegration.class.getName(), "fetchCostsheetObjects", "End and Master Pojo is "+masterPOJO);
		return Response.status(Response.Status.OK).entity(masterPOJO).build();
	}
}
