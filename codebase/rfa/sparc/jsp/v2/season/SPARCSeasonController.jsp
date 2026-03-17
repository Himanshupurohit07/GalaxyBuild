<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: Controller
--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="java.util.regex.Pattern"%>
<%@ page language="java"
    errorPage="../../../../jsp/exception/ControlException.jsp"
        import="com.lcs.wc.db.SearchResults,
            com.lcs.wc.client.web.PageManager,
                com.lcs.wc.client.web.WebControllers,
                com.lcs.wc.client.Activities,
                com.lcs.wc.foundation.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.db.*,
                com.lcs.wc.season.*,
                com.lcs.wc.season.query.*,
                com.lcs.wc.planning.*,
                com.lcs.wc.placeholder.*,
                com.google.inject.Guice,
                com.google.inject.Inject,
                com.google.inject.Injector,
                java.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                wt.util.*,
                com.lcs.wc.report.*,
                com.infoengine.object.factory.*,
                com.lcs.wc.product.*,
                com.lcs.wc.util.*,
		wt.method.*"

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<jsp:useBean id="seasonModel" scope="request" class="com.lcs.wc.season.LCSSeasonClientModel" />
<jsp:useBean id="linePlanConfig" scope="session" class="com.lcs.wc.season.LinePlanConfig" />

<%-- DMF 02/07/07  --%>
<jsp:useBean id="productModel" scope="request" class="com.lcs.wc.product.LCSProductClientModel" />

<%@ taglib uri="http://www.ptc.com/infoengine/taglib/core" prefix="ie" %>
<ie:getService varName="ie"/>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%
   String viewSeasonPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "viewSeason_PG_TLE",RB.objA ) ;
   String createPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "create_PG_TLE",RB.objA ) ;
   String viewPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "view_PG_TLE",RB.objA ) ;
   String updatePgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "update_PG_TLE",RB.objA ) ;
   String detailsPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "details_PG_TLE",RB.objA ) ;
   String styleSheetPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "styleSheet_PG_TLE",RB.objA ) ;
   String searchCriteriaPgTle = WTMessage.getLocalizedMessage ( RB.MAIN, "searchCriteria_PG_TLE",RB.objA ) ;
   String searchResultsPgTle = WTMessage.getLocalizedMessage ( RB.MAIN, "searchResults_PG_TLE",RB.objA ) ;
   String linePlanPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlan_LBL",RB.objA ) ;
   //String selectPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "select_PG_TLE",RB.objA ) ;
   String selectProductsPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "selectProducts_PG_TLE",RB.objA ) ;
   String seasonGroupTypePgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "seasonGroupOptions_GRP_TLE",RB.objA ) ;
   String seasonDashboardsPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "seasonDashboards_PG_TLE",RB.objA ) ;
   String samplesConsolePgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "samplesConsole_PG_TLE",RB.objA ) ;
   String seasonCalendarPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "seasonCalendar_PG_TLE",RB.objA ) ;
   String changeReportPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "changeReport_OPT",RB.objA ) ;
   String materialCommitmentReportPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "materialCommitmentReport_PG_TLE",RB.objA ) ;
   String costsRecalculatedMsg = WTMessage.getLocalizedMessage ( RB.SEASON, "costsRecalculated_MSG",RB.objA ) ;
   String bulkUpdateError1Msg = WTMessage.getLocalizedMessage ( RB.EXCEPTION, "bulkUpdateError1_MSG",RB.objA ) ;
   String bulkUpdateError2Msg = WTMessage.getLocalizedMessage ( RB.EXCEPTION, "bulkUpdateError2_MSG",RB.objA ) ;
   String seasonLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "seasonColon_LBL", RB.objA ) ;
   String massCopyProducts = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_PG_TLE",RB.objA ) ;
   String planningLabel = WTMessage.getLocalizedMessage ( RB.PLAN, "planning_LBL", RB.objA ) ;
   String phGenerated = WTMessage.getLocalizedMessage ( RB.SEASON, "placeholdersGenerated_MSG",RB.objA ) ;
   String colorManagerPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "colorManager_PG_TLE",RB.objA ) ;

%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.season.SeasonController");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");

    public static final String JSPNAME = "SeasonController";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static String instance = "";
    public static final String AJAX_PDF_GENERATOR = PageManager.getPageURL("AJAX_PDF_GENERATOR", null);
    public static final String AJAX_EXCEL_GENERATOR = PageManager.getPageURL("AJAX_EXCEL_GENERATOR", null);
    public static final String AJAX_CSV_GENERATOR = PageManager.getPageURL("AJAX_CSV_GENERATOR", null);
    public static final String CONTEXT_URL_MASTER_CONTROLLER = PageManager.getContextOverrideUrlAndMasterContorller();

    static {
        try {
            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
        } catch(Exception e){
            e.printStackTrace();
        }
    }





%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    String templateType = request.getParameter("templateType");
    boolean checkReturn = false;
    String globalChangeTrackingSinceDate=request.getParameter("globalChangeTrackingSinceDate") ;
    String reportGroupByFilterAttribute=request.getParameter("reportGroupByFilterAttribute");
    String additionalParameters = request.getParameter("additionalParameters");
    if(!FormatHelper.hasContent(additionalParameters)){
        additionalParameters = "";
    }
    String workItemName = request.getParameter("workItemName");
    boolean multiple = true;
    if (FormatHelper.hasContent(request.getParameter("multiple"))) {
        multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    }
    boolean linePlanChooser = false;
    if(FormatHelper.hasContent(request.getParameter("linePlanChooser"))){
        linePlanChooser = FormatHelper.parseBoolean(request.getParameter("linePlanChooser"));
    }

    String title = "";
    String errorMessage = request.getParameter("errorMessage");
    String bulkErrorMessage = request.getParameter("bulkErrorMessage");
    if (FormatHelper.hasContent(errorMessage)) {
        errorMessage = java.net.URLDecoder.decode(errorMessage, defaultCharsetEncoding);
    } else {
      errorMessage = "";
    }
    String infoMessage = request.getParameter("infoMessage");
    infoMessage= (FormatHelper.hasContent(infoMessage))?java.net.URLDecoder.decode(infoMessage, defaultCharsetEncoding):"";

    String view = null;
    String type = "";

    String popupURL = "";

    FlexType flexType = null;
    String contextHeaderPage = "SEASON_CONTEXT_BAR";
    String formType = "standard";
    if(FormatHelper.hasContent(request.getParameter("formType"))){
        formType = request.getParameter("formType");
    }
    String linePlanChooserTitle = request.getParameter("linePlanChooserTitle");
    String seasonGroupTypeIdForChooserTitle = request.getParameter("seasonGroupTypeId");
    if(seasonGroupTypeIdForChooserTitle == null) seasonGroupTypeIdForChooserTitle = "";
    

   ///////////////////////////////////////////////////////////////////
    if(Activities.VIEW_SEASON.equals(activity)){
        lcsContext.setCacheSafe(true);
        seasonModel.load(oid);
        view = PageManager.VIEW_SEASON_PAGE;
        title = viewSeasonPgTle;
        flexType = seasonModel.getFlexType();
        appContext.setSeasonContext(oid);
    ///////////////////////////////////////////////////////////////////
    } else if("CREATE_SEASON".equals(activity)){
        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            title = createPgTle + " "  + seasonLabel;
            view = PageManager.CLASSIFY_PAGE;

        } else if("CLASSIFY".equals(action)){
            lcsContext.setCacheSafe(true);
            AttributeValueSetter.setAllAttributes(seasonModel, RequestHelper.hashRequest(request));
            if(FormatHelper.hasContent(request.getParameter("directClassifyId"))){
                seasonModel.setTypeId(request.getParameter("directClassifyId"));
            }
            title = createPgTle + " "  + seasonLabel;;
            view = PageManager.CREATE_SEASON_PAGE;

        } else if("SAVE".equals(action)){

             try {
                AttributeValueSetter.setAllAttributes(seasonModel, RequestHelper.hashRequest(request));
                seasonModel.setActive(true);
                seasonModel.save();

                appContext.loadSeasonList(true);
                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = viewPgTle + " "  + seasonLabel;
                action = "INIT";
                activity = Activities.VIEW_SEASON;
                checkReturn = true;

                additionalParameters = additionalParameters + "&forceReloadSeasonDropdown=true";
               } catch(LCSException e){
                    view = PageManager.CREATE_SEASON_PAGE;
                    title = createPgTle + " "  + seasonLabel;
                    errorMessage=e.getLocalizedMessage();
                    checkReturn = true;
               }
        }

        flexType = seasonModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
    } else if(Activities.UPDATE_SEASON.equals(activity)){
        appContext.setSeasonContext(oid);
        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            seasonModel.load(oid);
            view = PageManager.UPDATE_SEASON_PAGE;
            title = updatePgTle + " "  + seasonLabel;

        } else if("CHECKIN".equals(action)){

         seasonModel.load(oid);
         seasonModel.checkin();
          oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
         view = PageManager.SAFE_PAGE;
         title = seasonLabel + " " + detailsPgTle;
         action = "INIT";
         activity = Activities.VIEW_SEASON;
         checkReturn = true;

        } else if("SAVE".equals(action)){
            try{
                seasonModel.load( request.getParameter("oid"));
                AttributeValueSetter.setAllAttributes(seasonModel, RequestHelper.hashRequest(request));
                seasonModel.save();
                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());

                view = PageManager.SAFE_PAGE;
                title = seasonLabel + " " + detailsPgTle ;
                action = "INIT";
                activity = Activities.VIEW_SEASON;
                checkReturn = true;

                additionalParameters = additionalParameters + "&forceReloadSeasonDropdown=true";

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.UPDATE_SEASON_PAGE;
                title = updatePgTle + " "  + seasonLabel;
                checkReturn = true;
            }

        } else if("ADD_PRODUCTS".equals(action) || "ADD_PRODUCTS_SEASON_AND_PLACEHOLDER".equals(action)){
            try{

            String newIds = request.getParameter("newIds");
            Collection ids = MOAHelper.getMOACollection(newIds);
            seasonModel.load(oid);
            seasonModel.addProducts(ids);

            if("ADD_PRODUCTS".equals(action) ){
                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                activity = "VIEW_LINE_PLAN";
                checkReturn = true;
            } else{ //ADD_PRODUCTS_SEASON_AND_PLACEHOLDER

                action = "";
                activity = Activities.ADD_PRODUCTS_TO_PLACEHOLDER;
                additionalParameters = additionalParameters + "&placeholderId=" + request.getParameter("placeholderId") + "&productIds=" + newIds;
                oid = request.getParameter("placeholderId");
            }
            view = PageManager.SAFE_PAGE;
            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
            }

        } else if("ADD_SKUS".equals(action)){
            try{

            String newIds = request.getParameter("newIds");
            Collection ids = MOAHelper.getMOACollection(newIds);
            seasonModel.load(oid);
            seasonModel.addSKUs(ids);

                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                //activity = Activities.VIEW_SEASON_REPORT;
                activity = "VIEW_LINE_PLAN";
                checkReturn = true;

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
            }


        } else if("REMOVE_PRODUCT".equals(action)){
            try{
                String id = request.getParameter("productId");
                seasonModel.load(oid);
                seasonModel.removeProduct(id);

                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                activity = Activities.VIEW_SEASON_REPORT;
                checkReturn = true;
                appContext.clearAppContext();
                additionalParameters = additionalParameters + "&reloadFavorites=true";


            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
            }

        } else if("REMOVE_SKU".equals(action)){
            try{

            String id = request.getParameter("skuId");
            seasonModel.load(oid);
            seasonModel.removeSKU(id);

                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                activity = Activities.VIEW_SEASON_REPORT;
                checkReturn = true;
                appContext.clearAppContext();
            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
            }
        } else if("REMOVE_PRODUCTS".equals(action)){

            try{
                String oids = request.getParameter("oids");
                Collection pids = MOAHelper.getMOACollection(oids);
                seasonModel.load(oid);
                seasonModel.removeProducts(pids);

                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                activity = Activities.VIEW_SEASON_REPORT;
                checkReturn = true;
                appContext.clearAppContext();
                additionalParameters = additionalParameters + "&reloadFavorites=true";

            } catch (LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
            }
        } else if("REMOVE_SKUS".equals(action)){
            try{
                String oids = request.getParameter("oids");
                Collection sids = MOAHelper.getMOACollection(oids);
                seasonModel.load(oid);
                Iterator it= sids.iterator();
                boolean productsFoundFlag= false;
                Collection<String> collection = new Vector<String>();
                while(it.hasNext()){
                    String skuString=(String) it.next();
                    LCSSKU obj= (LCSSKU)seasonModel.findByOid(skuString);

                if(!obj.isPlaceholder()){
                    collection.add(skuString);
                }else{
                    productsFoundFlag=true;
                }
            }
                seasonModel.removeSKUs(collection);

                oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                action = "LINEPLAN";
                activity = Activities.VIEW_SEASON_REPORT;
                checkReturn = true;
                appContext.clearAppContext();
                if(productsFoundFlag){
                 additionalParameters = additionalParameters + "&productRemovalErrormessage=true";
                }
                else{
                 additionalParameters = additionalParameters + "&productRemovalErrormessage=false";
                }
           } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = styleSheetPgTle;
                checkReturn = true;
           }
        }
        flexType = seasonModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
    } else if(Activities.FIND_SEASON.equals(activity)){
      contextHeaderPage = "STANDARD_LIBRARY_CONTEXT_BAR";
      lcsContext.setCacheSafe(true);
      if(action == null || "".equals(action) || "INIT".equals(action)){
            view = PageManager.FIND_SEASON_CRITERIA;
            title = seasonLabel + " " + searchCriteriaPgTle;
            type = "";

        } else if("CHANGE_TYPE".equals(action)){
            view = PageManager.FIND_SEASON_CRITERIA;
            title = seasonLabel + " " + searchCriteriaPgTle;
            type = request.getParameter("type");
            flexType = FlexTypeCache.getFlexType(type);

        } else if("SEARCH".equals(action)){
            if("true".equals(request.getParameter("updateMode"))){
                formType = "image";
            }
            type = request.getParameter("type");
            view = PageManager.FIND_SEASON_RESULTS;
            title = seasonLabel + " " + searchResultsPgTle;

        } else if("RETURN".equals(action)){
         checkReturn = true;
      }

    ///////////////////////////////////////////////////////////////////
    } else if(Activities.VIEW_SEASON_REPORT.equals(activity)){
        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        if(!FormatHelper.hasContent(action) || "LINEPLAN".equals(action)){
            view = PageManager.VIEW_SEASON_LINEPLAN;
            title = seasonLabel + " " + linePlanPgTle;
        } else {
            view = PageManager.VIEW_SEASON_GRAPHICALMATRIX;
            title = seasonLabel + " " + styleSheetPgTle;
        }

        flexType = seasonModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
    } else if("VIEW_LINE_PLAN".equals(activity)){
        lcsContext.setCacheSafe(true);
        if(oid.indexOf("LCSProduct")>-1 || oid.indexOf("LCSSourceToSeasonLink")>-1 || oid.indexOf("FlexSpecToSeasonLink")>-1){
            String[] oid_Arrays= request.getParameterValues("oid");
            for(int i=0;i<oid_Arrays.length;i++){
                String tempOID =oid_Arrays[i];
                if(tempOID.indexOf("LCSSeason")>-1){
                    oid = tempOID;
                    break;
                }
            }
        }
        seasonModel.load(oid);
        appContext.setSeasonContext(oid);

        if("CHOOSE_SEASON".equals(action)){
            view = "LINEPLAN_SEASON_SELECT";
            title = WTMessage.getLocalizedMessage ( RB.SEASON, "selectSeason_PG_TLE", RB.objA ) ;
            additionalParameters = additionalParameters + "&multiple=" + multiple;

        } else if("CHOOSE_SPEC_PAGES".equals(action)){
            view = "SPEC_PAGE_SELECTION";
            title = selectProductsPgTle;

        } else if ("AJAX_LINE_PLAN_PDF".equals(action)){
            %>
            <jsp:forward page="<%=subURLFolder + AJAX_PDF_GENERATOR %>">
                <jsp:param name="title" value="noTitle" />
            </jsp:forward>
            <%

        } else if ("AJAX_LINE_PLAN_EXCEL".equals(action)){
            %>
            <jsp:forward page="<%=subURLFolder + AJAX_EXCEL_GENERATOR %>">
                <jsp:param name="title" value="noTitle" />
            </jsp:forward>
            <%
        } else if ("AJAX_LINE_PLAN_CSV".equals(action)){
            %>
            <jsp:forward page="<%=subURLFolder + AJAX_CSV_GENERATOR %>">
                <jsp:param name="title" value="noTitle" />
            </jsp:forward>
            <%
        } else if ("TWO_SEASONS".equals(action)){
            Object params[] = { request.getParameter("name1"), request.getParameter("name2") };
            throw new LCSException(RB.SEASON, "twoSeasons_ERR", params);
       } else {

            boolean setLinePlan = FormatHelper.parseBoolean(request.getParameter("setLinePlan"));
            boolean setWorkMode = FormatHelper.parseBoolean(request.getParameter("setWorkMode"));
            String filterId = request.getParameter("filterId");

            String productType = LinePlanConfig.getLinePlanConfigKey(request, seasonModel.getBusinessObject().getProductType());

            boolean findExistingSeasonalProdMatchingPlaceholder = "completeAddProductsToPlaceholder".equals(request.getParameter("linePlanChooserReturnMethod"));
            boolean updateViewReturn = "true".equals(linePlanConfig.get(productType, "isSuperViewUpdate"));

            // We don't want the context header in freezepane edit mode.
            String layout = (String) linePlanConfig.get(productType,"layout");
            boolean editLinePlan = "true".equals(request.getParameter("editLinePlan"));
            if ("freezepane".equals(layout) && editLinePlan) {
                contextHeaderPage = "";
            }

            boolean isDashboardFilter=false;
            if(FormatHelper.hasContent((String)linePlanConfig.get(productType,"reportGroupByFilterAttribute"))
                    && !FormatHelper.hasContent(filterId)){
                isDashboardFilter = true;
            }

            boolean isReturnFromLineBoard = "true".equals(linePlanConfig.get(productType,"createFromLineBoardSetup"));
            boolean revertToView = "true".equals(request.getParameter("revertToView"));

            if((setLinePlan  && !"RELOAD".equals(action) || findExistingSeasonalProdMatchingPlaceholder) && !revertToView){
                //linePlanConfig.clear();
                //linePlanConfig.putAll(RequestHelper.hashRequest(request));
                String prevViewId=(String)linePlanConfig.get(productType,"viewId");
                String reportGroupByFilterAttributeKey =(String)linePlanConfig.get(productType,"reportGroupByFilterAttributeKey");

                String currViewId=(String)request.getParameter("viewId");
                if(currViewId==null){
                    currViewId="";
                }
                if(prevViewId==null){
                    prevViewId="";
                }
                linePlanConfig.update(productType, request);
                // The above linePlanConfig.update will clear out the viewId if it doesn't exists in the request.
                // Restore the previous viewId if that is the case.
                if (request.getParameter("viewId") == null) {
                    linePlanConfig.put(productType, "viewId", prevViewId);
                    currViewId = prevViewId;
                }

                if((FormatHelper.hasContent(request.getParameter("filterId")) ||
                     FormatHelper.hasContent((String)linePlanConfig.get(productType,"filterId")) || !currViewId.equals(prevViewId)) && !isDashboardFilter){
                    if(FormatHelper.hasContent(reportGroupByFilterAttributeKey)){
                        linePlanConfig.put(productType,reportGroupByFilterAttributeKey,"");
                        reportGroupByFilterAttribute="";
                    }
                }

                if(FormatHelper.hasContent(prevViewId) && (!FormatHelper.hasContent(currViewId) || " ".equals(currViewId))){
                    linePlanConfig.put(productType, "groupByAttribute"," ");
                }
                if(!FormatHelper.hasContent(currViewId) && "INIT".equals(action)){
                    //Lookup default view
                    currViewId = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(seasonModel.getBusinessObject().getProductType()), request.getParameter("activity"));
                    if(currViewId!=null){
                        linePlanConfig.put(productType, "viewId",currViewId);
                        linePlanConfig.updateLinPlanConfigForSelectedView(productType, currViewId);
                    }
                }

             }

            else if(updateViewReturn || revertToView || (!setLinePlan && (linePlanConfig.get(productType)==null || isDashboardFilter))
                || isReturnFromLineBoard){
                String viewId=(String)linePlanConfig.get(productType,"viewId");

                //Get the view id from request parameter if there is no view available.
                if(!FormatHelper.hasContent(viewId)){
                    viewId=request.getParameter("viewId");
                }

                if(linePlanConfig.get(productType)==null){
                    if(!FormatHelper.hasContent(viewId)){
                        //Lookup default view
                        viewId = lcsContext.viewCache.getDefaultViewId(FormatHelper.getObjectId(seasonModel.getBusinessObject().getProductType()), request.getParameter("activity"));
                    }
                }
                if(FormatHelper.hasContent(viewId)){
                    linePlanConfig.update(productType, request);
                    linePlanConfig.put(productType, "viewId",viewId);
                    linePlanConfig.updateLinPlanConfigForSelectedView(productType, viewId);
                }else{
                    if("true".equals(request.getParameter("superViewFilterTab"))){
                        linePlanConfig.update(productType,request);
                    }else{
                        linePlanConfig.update(productType, new Hashtable());
                        reportGroupByFilterAttribute="";
                    }
                 }
            }else if(filterId!=null){
                linePlanConfig.put(productType, "filterId",filterId);
            }

            if("true".equals(request.getParameter("superViewFilterTab")) && filterId!=null){
                linePlanConfig.put(productType, "filterId",filterId);
                String reportGroupByFilterAttributeKey =(String)linePlanConfig.get(productType,"reportGroupByFilterAttributeKey");
                if(FormatHelper.hasContent(reportGroupByFilterAttributeKey)){
                    linePlanConfig.put(productType,reportGroupByFilterAttributeKey," ");
                    reportGroupByFilterAttribute="";
                }
            }

            if(setWorkMode){
                String linePlaneLevel = "";
                if("true".equals(request.getParameter("csv"))){
                    linePlaneLevel = request.getParameter("linePlanLevelHold");
                }
                else{
                    linePlaneLevel = request.getParameter("linePlanLevel");
                }
                linePlanConfig.put(productType, "linePlanLevel", linePlaneLevel);
                linePlanConfig.put(productType, "linePlanSourcing", "false");
                //linePlanConfig.put(productType, "viewId", viewId);
                //linePlanConfig.put(productType, "filterId", filterId);

            }

            view = PageManager.VIEW_SEASON_LINEPLAN;
            title = seasonLabel + " " + linePlanPgTle;
            if(FormatHelper.hasContent(bulkErrorMessage)){
                String[] items = bulkErrorMessage.substring(1, bulkErrorMessage.length() - 1).split(Pattern.quote(MOAHelper.DELIM+","));
                errorMessage = String.join("\n", items).replace(MOAHelper.DELIM, "");
            }
        }

        if("CHOOSE_SEASON2".equals(action)){
            view = "LINEPLAN_SEASON_SELECT2";
            title = WTMessage.getLocalizedMessage ( RB.SEASON, "selectSeason_PG_TLE", RB.objA ) ;
            additionalParameters = additionalParameters + "&multiple=" + multiple;
        }

        if("VIEW2".equals(action)){
            view = "VIEW_SEASON_LINEPLAN2";
            title = seasonLabel + " " + linePlanPgTle;
        }

        //flexType = seasonModel.getFlexType();

    } else if("REFRESH_SEASON_COSTS".equals(activity)){
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        String productType = LinePlanConfig.getLinePlanConfigKey(request, seasonModel.getBusinessObject().getProductType());
        boolean setLinePlan = FormatHelper.parseBoolean(request.getParameter("setLinePlan"));
        String linePlanLevel =  (String) linePlanConfig.get(productType, "linePlanLevel");
        boolean skus = (FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL.equals(linePlanLevel));
        boolean allSources = "true".equals((String)linePlanConfig.get(productType, "linePlanSourcing"));
        boolean activeCostSheets = FormatHelper.parseBoolean(request.getParameter("activeCostSheets"));
        boolean whatIfCostSheets = FormatHelper.parseBoolean(request.getParameter("whatIfCostSheets"));

        if(setLinePlan){
            //linePlanConfig.clear();
            //linePlanConfig.putAll(RequestHelper.hashRequest(request));
            linePlanConfig.update(productType, request);
            if(!linePlanConfig.get(productType).containsKey("linePlanLevel")){
                linePlanConfig.put(productType, "linePlanLevel", linePlanLevel);
            }
        }
        // RUN LINE PLAN QUERY
        Set idSet = new HashSet();
        com.lcs.wc.season.query.LineSheetQuery lsqn = null;
        LineSheetQueryOptions options = new LineSheetQueryOptions();
        options.setSeason(seasonModel.getBusinessObject());
        options.skus = skus;
        options.criteria = linePlanConfig.get(productType);
        options.costing = true;
        options.skuCosting = skus && true;
        options.setIncludeSourcing(true);
        options.secondarySourcing = allSources;
        options.primaryCostOnly = !activeCostSheets;
        options.whatifCosts = whatIfCostSheets;
        options.usedAttKeys = new Vector();
        options.includeRemoved = false;
        options.includePlaceHolders = true;
        options.includeCostSpec = true;
        options.setCostSpec(null);
        options.statement = null;
        options.excludeInActiveSKUSource = false;


        Injector injector = Guice.createInjector(new LSQModule());
        lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);

        Collection data = lsqn.getLineSheetResults(options);

        Iterator resultsIt = data.iterator();
        // COLLECT IDS
        FlexObject obj;
        long recalcTime = (long)LCSProperties.get("com.lcs.wc.season.RefreshCosts.costRecalcTime", 0);
        while(resultsIt.hasNext()){
            obj = (FlexObject) resultsIt.next();
            if(recalcTime <=0 || LCSSeasonQuery.linePlanChange(obj, recalcTime, false)){
                if(obj.getString("LCSPRODUCTCOSTSHEET.CLASSNAMEA2A2") != null) {
                    idSet.add("VR:" + obj.getString("LCSPRODUCTCOSTSHEET.CLASSNAMEA2A2") + ":" + obj.getString("LCSPRODUCTCOSTSHEET.BRANCHIDITERATIONINFO"));
                }
                if(obj.getString("LCSSKUCOSTSHEET.CLASSNAMEA2A2")!=null){
                    idSet.add("VR:" + obj.getString("LCSSKUCOSTSHEET.CLASSNAMEA2A2") + ":" + obj.getString("LCSSKUCOSTSHEET.BRANCHIDITERATIONINFO"));
                }
            }
        }
		
		//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
		logger.debug("**Inside REFRESH_SEASON_COSTS :"+RequestHelper.hashRequest(request));
			logger.debug("*** Representaive colorway"+request.getParameter("representativeColorway"));
			logger.debug("**MethodContext.getContext():"+MethodContext.getContext());
            if(request.getParameter("representativeColorway")==null || !FormatHelper.hasContent(request.getParameter("representativeColorway"))){   
                    MethodContext.getContext().put("SPARC_REFRESH_MULTI_COSTSHEET", "SPARC_REFRESH_MULTI_COSTSHEET");
					MethodContext.getContext().put("SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP", "SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP");
                }
			logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext());
			logger.debug("**idSet :"+idSet);
		//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
        // CALL METHOD
        seasonModel.refreshCosts(idSet);
		logger.debug("**After Refresh Completed:");

        oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());
        view = PageManager.SAFE_PAGE;
        title = linePlanPgTle;
        action = "INIT";
        activity = "VIEW_LINE_PLAN";
        checkReturn = true;
        infoMessage = costsRecalculatedMsg;

        flexType = seasonModel.getFlexType();

    } else if("MOVE_SKUS".equals(activity)){
		
		appContext.setSeasonContext(oid);
		view = PageManager.SAFE_PAGE;
		title = linePlanPgTle;
		action = "INIT";
		activity = "VIEW_LINE_PLAN";
		checkReturn = true;
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);
        seasonModel.load(oid);
		
		Map<String,String> failures = new HashMap();
		try {
			failures = seasonModel.moveSKUs(ids);
		}catch(LCSException lcsE){
            lcsE.printStackTrace();
            errorMessage = lcsE.getLocalizedMessage();
        }
		
		if (failures.size() > 0) {
			for (String errorMsg : failures.values()) {
				errorMessage = FormatHelper.hasContent(errorMessage)?errorMessage+",\n"+errorMsg:errorMsg;
			}
		}
		
		additionalParameters = additionalParameters + "&reloadFavorites=true";
        flexType = seasonModel.getFlexType();


    } else if("MOVE_PRODUCTS".equals(activity) || "MOVE_PRODUCTS_ADD_TO_PLACEHOLDER".equals(activity)){
        appContext.setSeasonContext(oid);
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);
        seasonModel.load(oid);
		
		Map<String,String> failures = new HashMap();
        try{
            view = PageManager.SAFE_PAGE;
            failures = seasonModel.moveProducts(ids);
            if("MOVE_PRODUCTS".equals(activity)){
                title = linePlanPgTle;
                action = "INIT";
                activity = "VIEW_LINE_PLAN";
                checkReturn = true;
            }else{
                String phId = request.getParameter("placeholderId");
                action = "";
                    activity = Activities.ADD_PRODUCTS_TO_PLACEHOLDER;
                    additionalParameters = additionalParameters + "&placeholderId=" + phId + "&productIds=" + oids + "&placeholderMode=" + request.getParameter("placeholderMode");
                    oid = phId;
            }
        } catch(LCSException lcsE){
            lcsE.printStackTrace();
            title = linePlanPgTle;
            action = "INIT";
            activity = "VIEW_LINE_PLAN";
            checkReturn = true;
            errorMessage = lcsE.getLocalizedMessage();
        }
		
		if (failures.size() > 0) {
			for (String errorMsg : failures.values()) {
				errorMessage = FormatHelper.hasContent(errorMessage)?errorMessage+",\n"+errorMsg:errorMsg;
			}
		}
		
        additionalParameters = additionalParameters + "&reloadFavorites=true";
        flexType = seasonModel.getFlexType();

    } else if("CARRYOVER_SKUS".equals(activity)){
        appContext.setSeasonContext(oid);
		view = PageManager.SAFE_PAGE;
		title = linePlanPgTle;
		action = "INIT";
		activity = "VIEW_LINE_PLAN";
		checkReturn = true;
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);
        seasonModel.load(oid);
		Map<String,String> failures = new HashMap();
		try {
			failures = seasonModel.carryOverSKUs(ids);
		}catch(LCSException lcsE){
            lcsE.printStackTrace();
            errorMessage = lcsE.getLocalizedMessage();
        }
		
		if (failures.size() > 0) {
			for (String errorMsg : failures.values()) {
				errorMessage = FormatHelper.hasContent(errorMessage)?errorMessage+",\n"+errorMsg:errorMsg;
			}
		}
		
		additionalParameters = additionalParameters + "&reloadFavorites=true";
        flexType = seasonModel.getFlexType();

    } else if("CARRYOVER_PRODUCTS".equals(activity) || "CARRYOVER_PRODUCTS_ADD_TO_PLACEHOLDER".equals(activity)){
        appContext.setSeasonContext(oid);
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);
        seasonModel.load(oid);
		
		Map<String,String> failures = new HashMap();
		try {
			view = PageManager.SAFE_PAGE;
			failures = seasonModel.carryOverProducts(ids);
			if("CARRYOVER_PRODUCTS".equals(activity)){
				title = linePlanPgTle;
				action = "INIT";
				activity = "VIEW_LINE_PLAN";
				checkReturn = true;
			} else{
				String phId = request.getParameter("placeholderId");
				action = "";
					activity = Activities.ADD_PRODUCTS_TO_PLACEHOLDER;
					additionalParameters = additionalParameters + "&placeholderId=" + phId + "&productIds=" + oids + "&placeholderMode=" + request.getParameter("placeholderMode");
					oid = phId;
			}
			
			
		} catch (LCSException lcsE) {
			lcsE.printStackTrace();
            title = linePlanPgTle;
            action = "INIT";
            activity = "VIEW_LINE_PLAN";
            checkReturn = true;
            errorMessage = lcsE.getLocalizedMessage();
		}
		
		if (failures.size() > 0) {
			for (String errorMsg : failures.values()) {
				errorMessage = FormatHelper.hasContent(errorMessage)?errorMessage+",\n"+errorMsg:errorMsg;
			}
		}
		
		additionalParameters = additionalParameters + "&reloadFavorites=true";
		flexType = seasonModel.getFlexType();
		
    } else if("MASS_COPY_PRODUCTS".equals(activity)){

        appContext.setSeasonContext(oid);
        String copyFromSeasonId = request.getParameter("copyFromSeasonId");

        additionalParameters = additionalParameters + "&copyFromSeasonId=" + copyFromSeasonId;

        String seasonId = request.getParameter("seasonId");
        if(FormatHelper.hasContent(seasonId)){
            productModel.setSeasonId(seasonId);
        }


        if("INIT".equals(action)){
            view = "MASS_COPY_PRODUCTS";
            title = massCopyProducts;

        } else if("COMPONENTS_OPTION".equals(action)){
            String productNameDataVal = request.getParameter("productNameDataVal");
            String productTypeDataVal = request.getParameter("productTypeDataVal");
            view = "MASS_COPY_COMPONENTS";
            title = massCopyProducts;

        } else if("SAVE".equals(action)){

            String productNameDataVal = request.getParameter("productNameDataVal");
            String productTypeDataVal = request.getParameter("productTypeDataVal");
            String componentSelections = request.getParameter("componentSelections");
            String productOids = request.getParameter("productOidsVal");
            String copyMode = request.getParameter("copyMode");


            productModel.setMassCopyData(productNameDataVal, productTypeDataVal, componentSelections, productOids, copyMode, seasonId);

            productModel.massCopy();

            view = PageManager.SAFE_PAGE;
            title = linePlanPgTle;
            action = "INIT";
            activity = "VIEW_LINE_PLAN";
            checkReturn = true;

            flexType = seasonModel.getFlexType();

        }


    } else if("VIEW_SEASON_CALENDAR".equals(activity)){

        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = "VIEW_SEASON_CALENDAR_PAGE";
        title = seasonCalendarPgTle;

        flexType = seasonModel.getFlexType();



    } else if("BULK_LINPLAN_UPDATE".equals(activity)){
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = PageManager.SAFE_PAGE;
        title = seasonLabel + " " + linePlanPgTle;
        action = "COMPLETE_EDIT";
        activity = "VIEW_LINE_PLAN";
        if(request.getParameter("linePlanLevelHold") != null){
            additionalParameters = additionalParameters + "&linePlanLevelHold=" + request.getParameter("linePlanLevelHold");
        }
        if("true".equals(request.getParameter("maintainHiddenColumns"))&&!FormatHelper.parseBoolean(request.getParameter("deActivateHidden"))){
            additionalParameters = additionalParameters + "&maintainHiddenColumns=true";
            String tempInt = request.getParameter("HC_Col_Count");
            additionalParameters = additionalParameters + "&HC_Col_Count=" + tempInt;

            int columnCount = 0;
            if (FormatHelper.hasContent(tempInt)) {
                columnCount = Integer.parseInt(tempInt);
                int start = 1;
                int idx = 0;
                for (int i = start; i < columnCount; i++) {
                    if (FormatHelper.hasContent(request.getParameter("HC"+i))) {
                        additionalParameters = additionalParameters + "&HC"+i + "=" + request.getParameter("HC"+i);
                    }
                }
            }

        }
            if(additionalParameters.startsWith("&")){
                additionalParameters = additionalParameters.substring(1, additionalParameters.length());
            }
        workItemName = request.getParameter("workItemName");

        //String dataString = MultiObjectHelper.createPackagedStringFromMultiForm(request);
        String dataString = request.getParameter("dataString");

        java.util.HashMap results = new BulkObjectUpdate().bulkUpdate(dataString, wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")).toString());


        String failedRows = (String) results.get("failedRows");
        failedRows = FormatHelper.removeCharacter(failedRows, "[");
        failedRows = FormatHelper.removeCharacter(failedRows, "]");
        if(FormatHelper.hasContent(failedRows)){
            String errMsg = (String) results.get("errorMessage");
            errorMessage = bulkUpdateError1Msg + " " + errMsg + " - " + bulkUpdateError2Msg + " " + failedRows;
        }
        flexType = seasonModel.getFlexType();


    } else if("VIEW_SEASON_DASHBOARDS".equals(activity)){

        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = "VIEW_SEASON_DASHBOARDS_PAGE";
        title = seasonDashboardsPgTle;

        flexType = seasonModel.getFlexType();


    } else if("SAMPLE_CONSOLE_VIEW".equals(activity)){
        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = "SAMPLE_CONSOLE_VIEW_PAGE";
        title = samplesConsolePgTle;
        templateType = "NONE";
        flexType = seasonModel.getFlexType();
        request.setAttribute("exclude.main.css","true");

    } else if("VIEW_SEASON_CHANGE_REPORT".equals(activity)){

        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = "SEASON_CHANGE_REPORT";
        title = seasonLabel + " " + changeReportPgTle;


    } else if("SELECT_SEASON_GROUPS".equals(activity)){

        if(!FormatHelper.hasContent(action) || "INIT".equals(action)){
            seasonModel.load(oid);
            view = "SEASON_SEASON_GROUP_SELECTION";
            title = seasonLabel + "  - " + seasonGroupTypePgTle;


        } else {
            seasonModel.load(oid);
            String seasonGroupTypeIds = request.getParameter("seasonGroupTypeIds");
            seasonModel.load(oid);
            view = "SEASON_SEASON_GROUP_SELECTION";
            title = seasonLabel + "  - " + seasonGroupTypePgTle;
            seasonModel.setSeasonGroupIds(seasonGroupTypeIds);
            seasonModel.save();
            view = PageManager.SAFE_PAGE;
            title = seasonLabel + " " + detailsPgTle;
            action = "INIT";
            activity = Activities.VIEW_SEASON;
            checkReturn = true;

        }

    } else if("VIEW_MATERIAL_COMMITMENT_REPORT".equals(activity)){
        lcsContext.setCacheSafe(true);
        appContext.setSeasonContext(oid);
        seasonModel.load(oid);
        view = "MATERIAL_COMMITMENT_REPORT";
        title = seasonLabel + " " + materialCommitmentReportPgTle;
    }
    else if("GENERATE_PRODUCT_SPECIFICATIONS".equals(activity)){

        //Do clean up of spec folders
        DeleteFileHelper dfh = new DeleteFileHelper();
        String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");
        dfh.deleteOldFiles(FileLocation.PDFDownloadLocationImages,timeToLive);
        dfh.deleteOldFiles(FileLocation.PDFDownloadLocationFiles,timeToLive);
        //end of clean up

        appContext.setSeasonContext(oid);
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);

        String pages = request.getParameter("selectedPages");
        Collection sPages = MOAHelper.getMOACollection(pages);

        seasonModel.load(oid);
        //seasonModel.carryOverProducts(ids);

        if(ids.size() > 0){
            String downloadFileKey = new java.util.Date().getTime() + "";
            String generatedPDF = com.lcs.wc.product.LCSProductHelper.service.createPDFSpecification(ids, true, sPages);
            lcsContext.requestedDownloadFile.put(downloadFileKey, generatedPDF);

            popupURL = CONTEXT_URL_MASTER_CONTROLLER + "?forwardedFileForDownload=true&forwardedFileForDownloadKey=" + downloadFileKey;
        }

        view = PageManager.SAFE_PAGE;
        title = linePlanPgTle;
        action = "INIT";
        activity = "VIEW_LINE_PLAN";
        checkReturn = true;
        if(FormatHelper.hasContent(popupURL)){
            popupURL = java.net.URLEncoder.encode(popupURL);
            additionalParameters = additionalParameters + "&popupURL=" + popupURL;
        }
        flexType = seasonModel.getFlexType();

    }
    else if("GENERATE_PRODUCT_SPECIFICATIONS_NEW".equals(activity)){
        //Do clean up of spec folders

        DeleteFileHelper dfh = new DeleteFileHelper();
        String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");
        dfh.deleteOldFiles(FileLocation.PDFDownloadLocationImages,timeToLive);
        dfh.deleteOldFiles(FileLocation.PDFDownloadLocationFiles,timeToLive);
        //end of clean up
        appContext.setSeasonContext(oid);
        String oids = request.getParameter("oids");
        Collection ids = MOAHelper.getMOACollection(oids);
        if(logger.isDebugEnabled()){
            logger.debug(RequestHelper.hashRequest(request).toString());
        }
		
		
System.out.println("SeasonContrller--...--pages--"+request.getParameter("selectedPages"));

System.out.println("SeasonContrller--...--pages--"+request.getParameter("specPages"));
System.out.println("SeasonContrller--...--request--"+request);
System.out.println("SeasonContrller--...-hash request--"+RequestHelper.hashRequest(request));
        boolean isAsyncGen = FormatHelper.parseBoolean(request.getParameter("asynchronousGeneration"));
        if(logger.isDebugEnabled()){
        logger.debug("request.getParameter(asynchronousGeneration)="+request.getParameter("asynchronousGeneration"));
        }
        seasonModel.load(oid);
        //seasonModel.carryOverProducts(ids);

        if(isAsyncGen && ids.size() > 0){
			//Tech pack line sheet asynch gen fix
            //LCSProductHelper.service.asyncCreatePDFSpecifications(ids,RequestHelper.hashRequest(request));
			new com.sparc.wc.product.SPARCProductLogic().asyncCreatePDFSpecifications(ids,RequestHelper.hashRequest(request));
        }else{
            if(ids.size() > 0){

                String downloadFileKey = new java.util.Date().getTime() + "";
				//Tech pack line sheet download gen fix
              //  String generatedPDF = com.lcs.wc.product.LCSProductHelper.service.createPDFSpecifications(ids, true, RequestHelper.hashRequest(request));
			    String generatedPDF = new com.sparc.wc.product.SPARCProductLogic().createPDFSpecifications(ids, RequestHelper.hashRequest(request),true);
				System.out.println("-234-generatedPDF---"+generatedPDF);
			   lcsContext.requestedDownloadFile.put(downloadFileKey, generatedPDF);
                popupURL = CONTEXT_URL_MASTER_CONTROLLER + "?forwardedFileForDownload=true&forwardedFileForDownloadKey=" + downloadFileKey;

                if(FormatHelper.hasContent(popupURL)){
                    popupURL = java.net.URLEncoder.encode(popupURL);
                    additionalParameters = additionalParameters + "&popupURL=" + popupURL;
                }
            }
         }


        view = PageManager.SAFE_PAGE;
        title = linePlanPgTle;
        action = "INIT";
        activity = "VIEW_LINE_PLAN";
        checkReturn = true;
        flexType = seasonModel.getFlexType();

    } else if("VIEW_SEASON_PLANNING".equals(activity)){

        contextHeaderPage = "SEASON_CONTEXT_BAR";
        lcsContext.setCacheSafe(true);

        if(oid.contains("com.lcs.wc.planning.FlexPlan"))
        {
            view = PageManager.SAFE_PAGE;
            returnActivity = "VIEW_PLAN";
            returnAction="INIT";
            returnOid=oid;
            checkReturn = true;
        }else{
            appContext.setSeasonContext(oid);
            seasonModel.load(oid);
            view = "VIEW_SEASON_PLANNING_PAGE";
            title = seasonLabel + planningLabel;
        }

    } else if("ADD_SEASON_PLANS".equals(activity)){
        try{
            String newIds = request.getParameter("planIds");
            Collection ids = MOAHelper.getMOACollection(newIds);
            seasonModel.load(oid);
            seasonModel.createPlans(ids);

            oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());

            view = "VIEW_SEASON_PLANNING_PAGE";
            title = seasonLabel + planningLabel;
            //action = "INIT";
            //activity = "VIEW_PLAN";
            checkReturn = true;

        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = PageManager.SAFE_PAGE;
            title = seasonLabel + planningLabel;
            checkReturn = true;
        }

    }else if("GENERATE_PLACEHOLDERS".equals(activity)){
        try{
            LCSSeason season = (LCSSeason)LCSQuery.findObjectById(oid);
            FlexPlan plan = (FlexPlan)LCSQuery.findObjectById(request.getParameter("currentPlanId"));
            Boolean forceUpdate = new Boolean("true".equals(request.getParameter("forceUpdatePH")));
            String filterBranch = request.getParameter("filterBranch");

            Map params = new HashMap();
            params.put(PlanToPlaceholderGenerator.PLAN, plan);
            params.put(PlanToPlaceholderGenerator.SEASON, season);
            params.put(PlanToPlaceholderGenerator.BRANCH, filterBranch);
            params.put(PlanToPlaceholderGenerator.FORCE_UPDATE, forceUpdate);
            params.put(PlanLogic.PLAN_GEN_KEY, "PlanToPlaceholder");

            PlanHelper.service.generateFromPlan(params);

            oid = FormatHelper.getVersionId(seasonModel.getBusinessObject());

            view = PageManager.SAFE_PAGE;
            title = seasonLabel + planningLabel;
            //action = "INIT";
            //activity = "VIEW_PLAN";
            checkReturn = true;

            errorMessage = phGenerated;

        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = PageManager.SAFE_PAGE;
            title = seasonLabel + planningLabel;
            checkReturn = true;
        }

    } else if("VIEW_COLORWAY_MANAGER".equals(activity)){
        //contextHeaderPage = "STANDARD_LIBRARY_CONTEXT_BAR";
        lcsContext.setCacheSafe(true);
        if(action == null || "".equals(action) || "INIT".equals(action)){
            view = "VIEW_COLORWAY_MANAGER";
            title = colorManagerPgTle;
            type = "";

        }
    }



    String contentPage = null;
    if(view != null){
        contentPage = PageManager.getPageURL(view, null);
    } else {
        contentPage = "";
    }

    ////////////////////////////////////////////////////////////////////////////
    // GET THE CLIENT MODELS FLEX TYPE FULL NAME
    ////////////////////////////////////////////////////////////////////////////
    String flexTypeName = null;
    if (flexType != null) {
        flexTypeName = flexType.getFullNameDisplay(true);
        type = FormatHelper.getObjectId(flexType);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CHECK RETURN ACTIVITY..
    ////////////////////////////////////////////////////////////////////////////
    if(FormatHelper.hasContent(returnActivity) && checkReturn){

        view = PageManager.SAFE_PAGE;
        title = WTMessage.getLocalizedMessage (RB.MAIN, "productName", RB.objA ) ;
        action = returnAction;
        activity = returnActivity;
        oid = returnOid;
        returnActivity = "";
        returnAction = "";
        returnOid = "";
    }

    if(FormatHelper.hasContent(workItemName)){
        additionalParameters = additionalParameters + "&level1=" + request.getParameter("level1");
        additionalParameters = additionalParameters + "&level2=" + request.getParameter("level2");
        additionalParameters = additionalParameters + "&level3=" + request.getParameter("level3");
        additionalParameters = additionalParameters + "&level4=" + request.getParameter("level4");
    }

 %>


<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
   <jsp:param name="infoMessage" value="<%= java.net.URLEncoder.encode(infoMessage, defaultCharsetEncoding) %>" />
   <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
   <jsp:param name="requestedPage" value="<%= contentPage %>" />
   <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="activity" value="<%= activity %>" />
   <jsp:param name="objectType" value="Season" />
   <jsp:param name="typeClass" value="com.lcs.wc.season.LCSSeason" />
    <jsp:param name="type" value="<%= type %>" />
    <jsp:param name="flexTypeName" value="<%= flexTypeName %>" />
    <jsp:param name="additionalParameters" value="<%= additionalParameters %>" />
    <jsp:param name="workItemName" value="<%= workItemName %>" />
    <jsp:param name="contextHeaderPage" value="<%= contextHeaderPage %>" />
    <jsp:param name="formType" value="<%= formType %>" />
    <jsp:param name="linePlanChooserTitle" value='<%= linePlanChooserTitle %>' />
    <jsp:param name="seasonGroupTypeIdForChooserTitle" value="<%=seasonGroupTypeIdForChooserTitle%>" />
    <jsp:param name="globalChangeTrackingSinceDate" value='<%= globalChangeTrackingSinceDate%>' />
    <jsp:param name="reportGroupByFilterAttribute" value='<%= reportGroupByFilterAttribute%>' />
    <jsp:param name="templateType" value="<%= templateType %>" />
</jsp:forward>