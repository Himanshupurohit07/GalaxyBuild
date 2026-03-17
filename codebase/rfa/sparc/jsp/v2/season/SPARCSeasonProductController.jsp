<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@page import="org.apache.logging.log4j.Logger"%>
<%@ page language="java"
     errorPage="../../../../jsp/exception/ControlException.jsp"
        import="com.lcs.wc.db.SearchResults,
            com.lcs.wc.client.web.PageManager,
                com.lcs.wc.client.web.WebControllers,
                com.lcs.wc.client.Activities,
                com.lcs.wc.flextype.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.db.*,
                com.lcs.wc.product.*,
                com.lcs.wc.season.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.specification.*,
                java.util.*,
                com.lcs.wc.util.*,
                wt.util.*,
                wt.part.*,
                com.lcs.wc.client.ApplicationContext,
                com.lcs.wc.client.web.ProductPageNames,
				com.sparc.wc.integration.services.SparcColorwayToSeasonWatchDogService,
				org.apache.catalina.util.ParameterMap"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="seasonProductModel" scope="request" class="com.lcs.wc.season.LCSSeasonProductLinkClientModel" />
<jsp:useBean id="seasonSKUModel" scope="request" class="com.lcs.wc.season.LCSSeasonSKULinkClientModel" />
<jsp:useBean id="sourceToSeasonModel" scope="request" class="com.lcs.wc.sourcing.LCSSourceToSeasonLinkClientModel" />
<jsp:useBean id="productModel" scope="request" class="com.lcs.wc.product.LCSProductClientModel" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:useBean id="flexPlmWebRequestContext" scope="request" class="com.lcs.wc.product.html.FlexPlmWebRequestContext"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.v2.season.SeasonProductController");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String JSPNAME = "SeasonProductController";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static final boolean SHOW_SPEC_IMAGES_IN_POPUP_WINDOW = LCSProperties.getBoolean("jsp.sample.showSpecImages", false);
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

    String viewSeasonProductLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "viewSeasonProduct_LBL", RB.objA ) ;

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String ownerId = request.getParameter("ownerId");
    String seasonId = request.getParameter("seasonId");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    String costSheetId = request.getParameter("costSheetId");
    FlexSpecToSeasonLink fstsl = appContext.getFstsl();
    if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason:") == -1){
        appContext.setProductContext(oid);
    }else{
    	appContext.setProductContext(ownerId);
    }
    String contextSKUId = appContext.getContextSKUId();
    boolean checkReturn = false;
    String title = "";
    String errorMessage = request.getParameter("errorMessage");
    if (FormatHelper.hasContent(errorMessage)) {
        errorMessage = java.net.URLDecoder.decode(errorMessage, defaultCharsetEncoding);
    } else {
      errorMessage = "";
    }
    String infoMessage = request.getParameter("infoMessage");
    String view = null;
    String type = "";

    String additionalParameters = "";
    String contextHeaderPage = "";
    if(!(SHOW_SPEC_IMAGES_IN_POPUP_WINDOW && FormatHelper.hasContent(request.getParameter("windowType")) && "READ_ONLY_POPUP".equals(request.getParameter("windowType")))){
        contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        request.setAttribute("layoutType", "DIV_LAYOUT");
    }
    String templateType = "";
    
    if("VIEW_SEASON_PRODUCT_LINK".equals(activity)){
        costSheetId = "";
        lcsContext.setCacheSafe(true);
        view = "VIEW_SP_PAGE";
        title = viewSeasonProductLabel;
        request.setAttribute("layoutType", "DIV_LAYOUT");
        request.setAttribute("productPageTitle", title);
        if(FormatHelper.parseBoolean(request.getParameter("autoSelectProductMenus"))){
            if(oid.indexOf("RFQResponse:")>-1
              ||oid.indexOf("RFQToQuotableLink:")>-1)
            {
                request.setAttribute("rfqId", oid);
            }
            oid = appContext.setAutoSelectedProductId(oid);
        }else if(FormatHelper.hasContent(oid)) {
            appContext.setProductContext(oid);
        }
        LCSProduct prod = appContext.getProductSeasonRev();

        //IF THE OID IS LCSSEASONPRODUCTLINK, RETRIEVE IT AND GET THE OWNER
        //WHICH IS A LCSPRODUCT, THEN GET THE FLEXTYPE
        if(oid != null && oid.indexOf("LCSProduct:") > -1){
            productModel.load(oid);
        }

        if (oid != null && (oid.indexOf("LCSProductSeasonLink") > -1 || oid.indexOf("LCSSKUSeasonLink") > -1)) {
            if(oid.indexOf("LCSProductSeasonLink") > -1){
                seasonProductModel.load(oid);
            }else{
                seasonSKUModel.load(oid);
            }
        }
        if (oid != null && oid.indexOf("LCSSourceToSeasonLink") > -1) {
            sourceToSeasonModel.load(oid);
        }

        if(prod!=null){
            productModel.load(FormatHelper.getObjectId(prod));
        }else{
            String prodId = appContext.getProductARevId();
            if(prodId != null && prodId.indexOf("LCSProduct:") > -1){
                productModel.load(prodId);
            }
        }
        if (!FormatHelper.hasContent(action)) {
            action = "INIT";
        }
        if (prod != null) {
            FlexType flextype = prod.getFlexType();
            if (flextype != null) {
                type = FormatHelper.getObjectId(flextype);
            }
        } else {
            FlexType flextype = appContext.getProductARev().getFlexType();
            if (flextype != null) {
                type = FormatHelper.getObjectId(flextype);
            }
        }

        if("FRAMES".equals(request.getParameter("templateType"))){
           templateType = "FRAMES";
        }


        additionalParameters = "singlePSDErrorMessage=" + request.getParameter("singlePSDErrorMessage");
        //SPR 2186162  consider non-select condition
        boolean nonselectedForSizeSKU = FormatHelper.hasContent(request.getParameter("contextSKUId"))&&"SIZING".equals(request.getParameter("tabPage"))
                        &&(!request.getParameter("contextSKUId").contains("LCSSKU"));

        request.setAttribute("nonselectedForSizeSKU",nonselectedForSizeSKU);

        String specId = request.getParameter("specId");
        String extendedCADSpecId = (String)session.getAttribute("extendedCADSpecId");
        String cadViewId = request.getParameter("cadViewId");
        String extendedPartSpecId = (String)session.getAttribute("extendedPartSpecId");
        String partViewId = request.getParameter("partViewId");
       // CAD and Part logic should be seperated the logic is processing the logic to jump back from another page, saving the critical parameters into the request
        if(FormatHelper.hasContent(extendedCADSpecId)){

           request.setAttribute("extendedSpecId",extendedCADSpecId);
           request.setAttribute("viewType","CAD");
           session.removeAttribute("extendedCADSpecId");
           if(request.getParameter("viewId")!=null){
              cadViewId = request.getParameter("viewId");
              session.removeAttribute("cadViewId");
           }else if(FormatHelper.hasContent((String)session.getAttribute("cadViewId"))){
              cadViewId = (String)session.getAttribute("cadViewId");
              session.removeAttribute("cadViewId");
           }else{
              cadViewId = "";
           }
           if(FormatHelper.hasContent((String)session.getAttribute("partViewId"))){
              partViewId = (String)session.getAttribute("partViewId");
              session.removeAttribute("partViewId");
           }
         }
        if(FormatHelper.hasContent(extendedPartSpecId)){

           request.setAttribute("extendedSpecId",extendedPartSpecId);
           request.setAttribute("viewType","Part");
           session.removeAttribute("extendedPartSpecId");
           if(request.getParameter("viewId")!=null){
              partViewId = request.getParameter("viewId");
              session.removeAttribute("partViewId");
             }else if(FormatHelper.hasContent((String)session.getAttribute("partViewId"))){
              partViewId = (String)session.getAttribute("partViewId");
              session.removeAttribute("partViewId");
             }else{
              partViewId = "";
             }
           if(FormatHelper.hasContent((String)session.getAttribute("cadViewId"))){
              cadViewId = (String)session.getAttribute("cadViewId");
              session.removeAttribute("cadViewId");
           }
        }

        // consider none view situation
        if (FormatHelper.hasContent(cadViewId)||" ".equals(cadViewId)) {
            request.setAttribute("cadViewId", cadViewId);
        }

        if (FormatHelper.hasContent(partViewId)||" ".equals(partViewId)) {
            request.setAttribute("partViewId", partViewId);
        }

        //Pass the contextSKUId parameter to display the Colorway info on Product Details tab
        //get the appContext contextSKUId if returning to Product Details tab from Edit/View of Colorway Color Grid
        if(FormatHelper.hasContent(request.getParameter("contextSKUId"))){
            contextSKUId = request.getParameter("contextSKUId");
        }else if(FormatHelper.hasContent(appContext.getContextSKUId())){
            contextSKUId = appContext.getContextSKUId();
        }
    } else if("UPDATE_SEASON_PRODUCT_LINK".equals(activity)) {
        String placeholderId = request.getParameter("placeholderId");
        if(FormatHelper.hasContent(placeholderId)){
            additionalParameters = additionalParameters + "&placeholderId=" + placeholderId;
        }

        if(!FormatHelper.hasContent(action) || "INIT".equals(action) || "PSEUDO_CREATE".equals(action)){
            request.setAttribute("layoutType", "DIV_LAYOUT");
            lcsContext.setCacheSafe(true);

            if(oid.indexOf("LCSProductSeasonLink") > 1 || oid.indexOf("LCSSKUSeasonLink") > 1){
                if(oid.indexOf("LCSProductSeasonLink") > 1){
                    seasonProductModel.load(oid);
                    seasonProductModel.loadLogicalState();
                    if("PSEUDO_CREATE".equals(action)) {
                        view = PageManager.CREATE_SP_PAGE;
                    } else {
                        view = PageManager.UPDATE_SP_PAGE;
                    }
                }else{
                    seasonSKUModel.load(oid);
                    seasonSKUModel.loadLogicalState();
                    if("PSEUDO_CREATE".equals(action)) {
                        view = PageManager.CREATE_SEASON_SKU_PAGE;
                        if(fstsl != null){
                            appContext.setProductContext(fstsl);
                        }
                    } else {
                        view = PageManager.UPDATE_SEASON_SKU_PAGE;
                    }
                }
            } else {
                if(ownerId.indexOf("LCSSKU")>-1){
                    seasonSKUModel.load(ownerId, seasonId);
                    seasonSKUModel.loadLogicalState();
                    view = PageManager.UPDATE_SEASON_SKU_PAGE;
                }else{
                    seasonProductModel.load(ownerId, seasonId);
                    seasonProductModel.loadLogicalState();
                    view = PageManager.UPDATE_SP_PAGE;
                }
            }
            title = viewSeasonProductLabel;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        } else if(action.startsWith("SAVE")) {
            try{
                if (ApplicationContext.AUTO_SELECT_PRODUCT_ID)
                {
                 additionalParameters = additionalParameters+"&autoSelectProductMenus=true"; 
                }
            if(oid.indexOf("LCSProductSeasonLink") > 1 || oid.indexOf("LCSSKUSeasonLink") > 1){
                if(oid.indexOf("LCSProductSeasonLink") > 1){
                    seasonProductModel.load(oid);
                    AttributeValueSetter.setAllAttributes(seasonProductModel, RequestHelper.hashRequest(request));
                    seasonProductModel.save();
                    oid = FormatHelper.getObjectId(seasonProductModel.getBusinessObject());
                    appContext.setProductContext(seasonProductModel.getBusinessObject());
                }else{
                    seasonSKUModel.load(oid);
                    AttributeValueSetter.setAllAttributes(seasonSKUModel, RequestHelper.hashRequest(request));
					System.out.println("seasonSKUModel.getEffectSequence()----------"+seasonSKUModel.getEffectSequence());
					LCSSeasonProductLink objSeasProdLink = seasonSKUModel.getBusinessObject();
					FlexType objFlexType = seasonSKUModel.getFlexType();
					String strFlexTypePath = objFlexType.getFullName();
					
			/*		System.out.println("strFlexTypePath----------------------------------"+strFlexTypePath);
				   
				   String objStatus = "";
				   if(strFlexTypePath.contains("scLucky")){
			
				    if(0 == seasonSKUModel.getEffectSequence()){				 
					      objStatus = (String) seasonSKUModel.getValue("scPLMPolorwayStatus");
					 }			
			  	    if("scProduction".equalsIgnoreCase(objStatus) && 0 == seasonSKUModel.getEffectSequence()){
						 SparcColorwayToSeasonWatchDogService colorwaySeasonService = new SparcColorwayToSeasonWatchDogService();			
						 colorwaySeasonService.checkNonBlank(objStatus,(LCSSKUSeasonLink)objSeasProdLink);
					  
				   }
				   }
			*/		   
                    seasonSKUModel.save();
                    oid = FormatHelper.getObjectId(seasonSKUModel.getBusinessObject());
                    appContext.setProductContext(seasonSKUModel.getBusinessObject());
                }
            } else {
                 if(ownerId.indexOf("LCSSKU")>-1){
                    seasonSKUModel.load(ownerId, seasonId);
                    AttributeValueSetter.setAllAttributes(seasonSKUModel, RequestHelper.hashRequest(request));
                    seasonSKUModel.save();
                    oid = FormatHelper.getObjectId(seasonSKUModel.getBusinessObject());
                    appContext.setProductContext(seasonSKUModel.getBusinessObject());
                }else{
                    seasonProductModel.load(ownerId, seasonId);
                    AttributeValueSetter.setAllAttributes(seasonProductModel, RequestHelper.hashRequest(request));
                    seasonProductModel.save();
                    oid = FormatHelper.getObjectId(seasonProductModel.getBusinessObject());
                    appContext.setProductContext(seasonProductModel.getBusinessObject());
                }

            }


            view = PageManager.SAFE_PAGE;
            title = viewSeasonProductLabel;
            //action = "INIT";
            //activity = "VIEW_SEASON_PRODUCT_LINK";
            //checkReturn = true;
            checkReturn = false;

            if("SAVE_AND_CREATE_PRODUCT".equals(action)){
                additionalParameters = additionalParameters + "&rootTypeId=" + request.getParameter("rootTypeId");
                activity = "CREATE_PRODUCT";
                if(FormatHelper.hasContent(request.getParameter("placeholderId"))){
                    additionalParameters = additionalParameters + "&directClassifyId=" + request.getParameter("rootTypeId");
                    action = "CLASSIFY";
                }else{
                    action = "INIT";
                }
            } else if("SAVE_AND_CREATE_SKU".equals(action)){
                additionalParameters = additionalParameters + "&rootTypeId=" + request.getParameter("rootTypeId");
                activity = "CREATE_SKU";
                action = "INIT";
                title = "create product";

            } else if("SAVE_AND_VIEW_SPL".equals(action)){
                additionalParameters = additionalParameters + "&forceReloadSideMenu=true";
                title = viewSeasonProductLabel;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";

                if(FormatHelper.parseBoolean(request.getParameter("autoSelectProductMenus"))){
                    additionalParameters = additionalParameters + "&autoSelectProductMenus=true";
                }
                if(oid.indexOf("LCSSKUSeasonLink") > -1 && fstsl != null){
                    appContext.setProductContext(fstsl);
                }
                if(FormatHelper.hasContent(appContext.getSourceToSeasonLinkId())){

                    oid = appContext.getSourceToSeasonLinkId();
                }
                if(FormatHelper.hasContent(appContext.getContextSpecId())){

                    oid = appContext.getContextSpecId();
                }

            } else if("SAVE_AND_VIEW_LINESHEET".equals(action)){
                activity = "VIEW_LINE_PLAN";
                action = "INIT";
                oid = request.getParameter("returnOid");
                String linePlanPgTle = WTMessage.getLocalizedMessage ( RB.SEASON, "linePlan_LBL",RB.objA ) ;
                String seasonLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "seasonColon_LBL", RB.objA ) ;
                title = seasonLabel + " " + linePlanPgTle;
            }else{
                additionalParameters = additionalParameters + "&forceReloadSideMenu=true";
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
            }

            } catch(LCSException e){
                if(ownerId.indexOf("LCSSKU")>-1){
					System.out.println("Excption first--------");
					try{
                    seasonSKUModel.loadLogicalState();
                    AttributeValueSetter.setAllAttributes(seasonSKUModel, RequestHelper.hashRequest(request));
                    view = PageManager.UPDATE_SEASON_SKU_PAGE;
					}catch(Exception exe){
						System.out.println("inside first exception---------------");
						 exe.printStackTrace();
						 view = PageManager.UPDATE_SEASON_SKU_PAGE;
						
					}
                }else{
					System.out.println("Excption second--------");					
					try{
                    seasonProductModel.loadLogicalState();
                    AttributeValueSetter.setAllAttributes(seasonProductModel, RequestHelper.hashRequest(request));
                    view = PageManager.UPDATE_SP_PAGE;
					}catch(Exception ex){
						System.out.println("inside second exception---------------");						
						ex.printStackTrace();
						view = PageManager.UPDATE_SP_PAGE;
						
					}
                }
                request.setAttribute("layoutType", "DIV_LAYOUT");
                errorMessage = e.getLocalizedMessage();
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            }
        }

        //Get the product's flextype
        if (seasonProductModel.getBusinessObject() != null) {
            FlexType flextype = seasonProductModel.getOwner().getFlexType();
            if (flextype != null) {
                type = FormatHelper.getObjectId(flextype);
            }
        }else{
            FlexType flextype = seasonSKUModel.getOwner().getFlexType();
            if (flextype != null) {
                type = FormatHelper.getObjectId(flextype);
            }
        }
        //We need to do something to pass the ownerId over because the CSP needs it to
        //get the proper object during an update of season-product attributes
        if(seasonSKUModel.getBusinessObject()!=null){
            additionalParameters = additionalParameters +"&ownerId="+ownerId+ "&contextSKUId=" + FormatHelper.getVersionId(SeasonProductLocator.getSKUARev(seasonSKUModel.getBusinessObject()));
        }else{

            if(!FormatHelper.hasContent(contextSKUId)){
                contextSKUId = request.getParameter("contextSKUId");
            }
            additionalParameters = additionalParameters +"&ownerId="+ownerId+"&contextSKUId="+contextSKUId;
        }
        //oid = ownerId;
   }

    String workItemName = request.getParameter("workItemName");
    if(FormatHelper.hasContent(workItemName)){
        additionalParameters = additionalParameters + "&level1=" + request.getParameter("level1");
        additionalParameters = additionalParameters + "&level2=" + request.getParameter("level2");
        additionalParameters = additionalParameters + "&level3=" + request.getParameter("level3");
        additionalParameters = additionalParameters + "&level4=" + request.getParameter("level4");
    }
    String imagePageId = request.getParameter("imagePageId");
    if(FormatHelper.hasContent(imagePageId)) {
        additionalParameters = additionalParameters + "&imagePageId=" + imagePageId;
    }
    String contentPage = null;
    if(view != null){
        flexPlmWebRequestContext.pageLookupKey = view;
        contentPage = PageManager.getPageURL(view, null);
    } else {
        contentPage = "";
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
%>
<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
   <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
   <jsp:param name="infoMessage" value="<%= infoMessage %>" />
   <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
   <jsp:param name="requestedPage" value="<%= contentPage %>" />
   <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="activity" value="<%= activity %>" />
   <jsp:param name="objectType" value="Season" />
    <jsp:param name="type" value="<%= type %>" />
   <jsp:param name="typeClass" value="com.lcs.wc.season.LCSSeason" />
   <jsp:param name="workItemName" value="<%= workItemName %>" />
    <jsp:param name="contextHeaderPage" value="<%= contextHeaderPage %>" />
    <jsp:param name="additionalParameters" value="<%= additionalParameters %>" />
    <jsp:param name="templateType" value="<%= templateType %>" />
    <jsp:param name="contextSKUId" value="<%= contextSKUId %>" />
    <jsp:param name="costSheetId" value="<%= costSheetId %>"/>
</jsp:forward>
