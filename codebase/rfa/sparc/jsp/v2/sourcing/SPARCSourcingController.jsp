<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="com.lcs.wc.client.web.ProductPageNames"%>
<%@page import="wt.method.MethodContext"%>
<%@ page language="java"
    errorPage="../../exception/ControlException.jsp"
        import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.client.web.PageManager,
                com.lcs.wc.client.web.WebControllers,
                com.lcs.wc.client.Activities,
                com.lcs.wc.flextype.*,
                com.lcs.wc.delete.*,
                com.lcs.wc.db.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.season.*,
                com.lcs.wc.product.*,
                com.lcs.wc.part.*,
                com.lcs.wc.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                java.util.*,
                wt.util.*,
		com.sparc.*"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="sconfigModel" scope="request" class="com.lcs.wc.sourcing.LCSSourcingConfigClientModel" />
<jsp:useBean id="stslModel" scope="request" class="com.lcs.wc.sourcing.LCSSourceToSeasonLinkClientModel" />
<jsp:useBean id="csheetModel" scope="request" class="com.lcs.wc.sourcing.LCSCostSheetClientModel" />
<jsp:useBean id="skucsheetModel" scope="request" class="com.lcs.wc.sourcing.LCSSKUCostSheetClientModel" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:useBean id="flexPlmWebRequestContext" scope="request" class="com.lcs.wc.product.html.FlexPlmWebRequestContext"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.sourcing.SARCSourcingController");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String JSPNAME = "SourcingController";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String AJAX_TEMPLATE = PageManager.getPageURL("AJAX_TEMPLATE", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
    public static final boolean USE_MULTI_COSTING = LCSProperties.getBoolean("com.lcs.wc.sourcing.useLCSMultiCosting");


%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

String templateName = MAINTEMPLATE;
String viewSourcingConfigurationPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "viewSourcingConfiguration_PG_TLE", RB.objA ) ;
String createSourcingConfigurationPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "createSourcingConfiguration_PG_TLE", RB.objA ) ;
String updateSourcingConfigurationPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "updateSourcingConfiguration_PG_TLE", RB.objA ) ;
String editSourcingConfiguration = WTMessage.getLocalizedMessage ( RB.SOURCING, "editSourcingConfiguration_PG_TLE", RB.objA ) ;
String sourcingConfigurationDetailsPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "sourcingConfigurationDetails_PG_TLE", RB.objA ) ;
String editSKUSourcingPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "editColorwaySourcing_PgHeader", RB.objA ) ;
String whatIfCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "whatIfCostSheet_PG_TLE", RB.objA ) ;
String viewCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "viewCostSheet_PG_TLE", RB.objA ) ;
String updateCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "updateCostSheet_PG_TLE", RB.objA ) ;
//String sourcingConfigurationLabel = WTMessage.getLocalizedMessage ( RB.SOURCING, "sourcingConfiguration_LBL", RB.objA ) ;

String createCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "createCostSheet_PG_TLE", RB.objA ) ;
String updateSourceToSeasonPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "updateSTSL_PG_TLE", RB.objA ) ;
String copyCostSheetPgTle = WTMessage.getLocalizedMessage ( RB.SOURCING, "copyCostSheet_PG_TLE", RB.objA ) ;

    String typeClass = "com.lcs.wc.sourcing.LCSSourcingConfig";
    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    String returnAddlParams = request.getParameter("returnAddlParams");
    if(FormatHelper.hasContent(returnAddlParams)){
        returnAddlParams = java.net.URLDecoder.decode(returnAddlParams,defaultCharsetEncoding);
    }

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
    String formType = "standard";
    String type = "";

    String productId = request.getParameter("productId");
    String seasonId = request.getParameter("seasonId");
    String rfqRequestId = request.getParameter("rfqRequestId");

    String typeId = request.getParameter("typeId");
    String scTypeId = request.getParameter("scTypeId");
    if(!FormatHelper.hasContent(scTypeId))scTypeId = typeId;

    String productVersionId = "";
    String additionalParameters = "";
    String tabPage =  request.getParameter("tabPage");
    FlexType flexType = null;

    String contextHeaderPage = "";

    String ajaxWindow = "false";
    if(FormatHelper.hasContent(request.getParameter("ajaxWindow")) && "true".equals(request.getParameter("ajaxWindow"))){
        ajaxWindow = "true";
    }


   ///////////////////////////////////////////////////////////////////
    if("VIEW_SOURCINGCONFIG".equals(activity)){

        sconfigModel.load(oid);

        contextHeaderPage = "SOURCING_CONTEXT_BAR";

        lcsContext.setCacheSafe(true);
        view = "VIEW_SOURCINGCONFIG_PAGE";
        title = viewSourcingConfigurationPgTle;

   ///////////////////////////////////////////////////////////////////
   ///////////////////////////////////////////////////////////////////
    } else if("CREATE_SOURCINGCONFIG".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            if(FormatHelper.hasContent(returnOid) && !returnOid.equals(appContext.getActiveProductId())){
                appContext.setProductContext(returnOid);
            }
            lcsContext.setCacheSafe(true);
            title = createSourcingConfigurationPgTle;
            view = "CREATE_SOURCINGCONFIG_PAGE";
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;

            String flexTypeId = request.getParameter("scTypeId");

            if (FormatHelper.hasContent(flexTypeId)) {
                flexType = FlexTypeCache.getFlexType("com.lcs.wc.flextype.FlexType:"+flexTypeId);
            }
            additionalParameters = java.net.URLEncoder.encode("seasonId="+seasonId);

        } else if("SAVE".equals(action)){


         try {
            AttributeValueSetter.setAllAttributes(sconfigModel, RequestHelper.hashRequest(request));
            sconfigModel.save();
            //oid = FormatHelper.getObjectId(sconfigModel.getBusinessObject());
            LCSSourcingConfig sconfig = sconfigModel.getBusinessObject();
            oid = FormatHelper.getObjectId(sconfig);

            //if seasonId != null then need to add the source to the season
            if(FormatHelper.hasContent(seasonId)){
                LCSSeason season = (LCSSeason)LCSSourcingConfigQuery.findObjectById(seasonId);
                LCSSourceToSeasonLink stsl = SourcingConfigHelper.service.createSourceToSeasonLink(sconfig, season);

                oid = FormatHelper.getObjectId(stsl);
            }

            appContext.setProductContext(oid);

            view = PageManager.SAFE_PAGE;
            //view = "VIEW_SOURCINGCONFIG_PAGE";
            title = viewSourcingConfigurationPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;

            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }

           } catch(LCSException e){
                view = "CREATE_SOURCINGCONFIG_PAGE";
                title = createSourcingConfigurationPgTle;
                errorMessage=e.getLocalizedMessage();
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
           }
            flexType = sconfigModel.getFlexType();
      }

   //////////////////////Bulk Sourcing Config Creation -- .//////////////////////////////////////

    } else if("BULK_CREATE_SOURCINGCONFIGS".equals(activity)){

        if("SAVE".equals(action)){
            HashMap results = new HashMap();
             String dataString = request.getParameter("dataString");
             String vendorType = request.getParameter("vendorType");

             if(FormatHelper.hasContent(dataString)){
                HashMap map = new HashMap();
                StringTokenizer st = new MultiCharDelimStringTokenizer(dataString, MultiObjectHelper.ATTRIBUTE_DELIMITER);
                StringTokenizer st2;
                StringTokenizer st3;
                String keyValuePair = "";
                String key = "";
                String value = "";
                map.put("vendorType", vendorType);
                if(request.getParameter("seasonId") != null){
                    map.put("seasonId", request.getParameter("seasonId"));
                }
                while(st.hasMoreTokens()){
                    keyValuePair = st.nextToken();
                    //logger.debug("***" + keyValuePair);
                    st2 = new MultiCharDelimStringTokenizer(keyValuePair, MultiObjectHelper.NAME_VALUE_DELIMITER);
                    if(st2.hasMoreTokens()){
                        key = st2.nextToken();
                        if(key.equals("selectedChooserIds")){
                            if(st2.hasMoreTokens()){
                                value = st2.nextToken();
                                st3 = new MultiCharDelimStringTokenizer(value, MOAHelper.DELIM);
                                while(st3.hasMoreTokens()){
                                    key = st3.nextToken();
                                    if(FormatHelper.hasContent(key)){
                                        key = key + "_LCSSOURCINGCONFIG$" + key.substring(key.indexOf("M_F_")+ 4) + "_createValid";
                                        map.put(key, "true");
                                    }
                                }
                            }
                        }else{
                            if(st2.hasMoreTokens()){
                                map.put(key, st2.nextToken());
                            }else{
                                map.put(key, "");
                            }
                        }
                    }
                }
                dataString = MultiObjectHelper.createPackagedStringFromMultiFormMap2(map);
                results = new BulkSourcingCreationUtility().bulkCreate(dataString, wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")).toString());
                String totalObjectCreated = "0";
                if(results.get("totalCreatedObjects")!=null){
                    totalObjectCreated = (String)results.get("totalCreatedObjects");
                }
                infoMessage = "totalCreatedObjects:" + totalObjectCreated;

                if(results.get("failedRows") != null){
                    String failedRows = (String)results.get("failedRows");

                }
                if(results.get("errorMessages") != null){
                    errorMessage = (String)results.get("errorMessages");
                }
             }
                view = "BULK_CREATE_SOURCINGCONFIG_PAGE";
                title = "";
                ajaxWindow = "true";
                templateName = AJAX_TEMPLATE;

        }else{

            view = "BULK_CREATE_SOURCINGCONFIG_PAGE";
            title = "";
        }

    } else if("UPDATE_SOURCINGCONFIG".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            sconfigModel.load(oid);
            view = "UPDATE_SOURCINGCONFIG_PAGE";
            title = updateSourcingConfigurationPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            flexPlmWebRequestContext.pageLookupKey = view;
            request.setAttribute("layoutType", "DIV_LAYOUT");
            if(FormatHelper.hasContent(returnOid) && !returnOid.equals(appContext.getActiveProductId())){
                appContext.setProductContext(returnOid);
            }
         //formType = "document";

        } else if("SAVE".equals(action)){
            try{

                sconfigModel.load(oid);
                AttributeValueSetter.setAllAttributes(sconfigModel, RequestHelper.hashRequest(request));
                sconfigModel.save();
                //oid = FormatHelper.getVersionId(sconfigModel.getBusinessObject());
                oid = FormatHelper.getVersionId(sconfigModel.getBusinessObject());
                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                //appContext.setProductContext(oid);

                view = PageManager.SAFE_PAGE;
                //view = "VIEW_SOURCINGCONFIG_PAGE";
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";

                    checkReturn = true;

                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_SOURCINGCONFIG_PAGE";
                title = editSourcingConfiguration;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            }
        }

        flexType = sconfigModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    }else if("UPDATE_SOURCE_TO_SEASON".equals(activity)){
        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            stslModel.load(oid);
            view = "UPDATE_SOURCE_TO_SEASON_PAGE";
            title = updateSourceToSeasonPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            flexPlmWebRequestContext.pageLookupKey = view;
            request.setAttribute("layoutType", "DIV_LAYOUT");
        } else if("SAVE".equals(action)){
            try{

                stslModel.load(oid);
                AttributeValueSetter.setAllAttributes(stslModel, RequestHelper.hashRequest(request));
                stslModel.save();
                //oid = FormatHelper.getVersionId(sconfigModel.getBusinessObject());
                oid = FormatHelper.getVersionId(stslModel.getBusinessObject());
                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                //appContext.setProductContext(oid);

                view = PageManager.SAFE_PAGE;
                //view = "VIEW_SOURCINGCONFIG_PAGE";
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_SOURCE_TO_SEASON_PAGE";
                title = updateSourceToSeasonPgTle;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                
            }
        }

        flexType = stslModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    }else if("REMOVE_SOURCE_TO_SEASON".equals(activity)){

        try{
            stslModel.load(oid);
            stslModel.remove();

            if(FormatHelper.hasContent(returnOid)){
                oid = returnOid;
            }

            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;
            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }

        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = "VIEW_SOURCINGCONFIG_PAGE";
            title = viewSourcingConfigurationPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        }

        flexType = stslModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    } else if("SET_SOURCINGCONFIG_PRIMARY".equals(activity)){
        try{

            sconfigModel.load(oid);
            sconfigModel.setAsPrimary();
            oid = FormatHelper.getObjectId(sconfigModel.getBusinessObject());
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;
            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }


        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = "VIEW_SOURCINGCONFIG_PAGE";
            title = viewSourcingConfigurationPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        }


    ///////////////////////////////////////////////////////////////////
    }else if("SET_SOURCE_TO_SEASON_PRIMARY".equals(activity)){
        try{
            logger.debug("Her I am trying to set source as primary");
            stslModel.load(oid);
            stslModel.setAsPrimary();
            oid = FormatHelper.getObjectId(stslModel.getBusinessObject());
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;
            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }


        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = "VIEW_SOURCINGCONFIG_PAGE";
            title = viewSourcingConfigurationPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        }


    ///////////////////////////////////////////////////////////////////
    } else if("UPDATE_SKU_SOURCING".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            sconfigModel.load(oid);
            view = "UPDATE_SKU_SOURCING_PAGE";
            title = editSKUSourcingPgTle;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            flexPlmWebRequestContext.pageLookupKey = view;
            request.setAttribute("layoutType", "DIV_LAYOUT");
        } else if("SET_ACTIVE".equals(action)){
            try{

                sconfigModel.load(oid);
                String ids = request.getParameter("skuSourcingIds");
                String nonActiveIdsStr = request.getParameter("nonActiveSkuSourcingIds");
                Collection<String> activeIds = MOAHelper.getMOACollection(ids);
                Collection<String> nonActiveIds = MOAHelper.getMOACollection(nonActiveIdsStr);

                String data = MultiObjectHelper.createPackagedStringFromMultiForm(request);
                sconfigModel.updateSKUSourcingLinks(data, activeIds, nonActiveIds, sconfigModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                oid = FormatHelper.getObjectId(sconfigModel.getBusinessObject());
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }

            } catch(LCSException e){
                e.printStackTrace();
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_SKU_SOURCING_PAGE";
                title = editSKUSourcingPgTle;
                oid = productVersionId;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            }
        }

        flexType = sconfigModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
    }  else if("DELETE_SOURCINGCONFIG".equals(activity)){

         if("DELETE".equals(action)){
            try{

                sconfigModel.load(oid);
                LCSSourcingConfig sconfig = sconfigModel.getBusinessObject();
                if(!ACLHelper.hasDeleteAccess(sconfig)) throw new LCSAccessException(RB.EXCEPTION, "noAccessToDeleteObject_MSG", RB.objA );

                LCSProduct product = new LCSSourcingConfigQuery().findProductForSourcingConfig(sconfig);
                SourcingConfigHelper.service.deleteSourcingConfig((LCSSourcingConfig)sconfigModel.getBusinessObject());

                oid = FormatHelper.getVersionId(product);
                appContext.setProductContext(oid);
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                activity = "VIEW_SEASON_PRODUCT_LINK";
                action = "INIT";
                title = updateSourcingConfigurationPgTle;
            }
        }

        flexType = sconfigModel.getFlexType();

    }else if("WHATIF_COSTSHEET".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            if(request.getParameter("copyFromOid").indexOf("LCSProductCostSheet")>-1){
                csheetModel.load(request.getParameter("copyFromOid"));
                type = FormatHelper.getObjectId(csheetModel.getFlexType());
            }else{
                skucsheetModel.load(request.getParameter("copyFromOid"));
                type = FormatHelper.getObjectId(skucsheetModel.getFlexType());
            }

            title = whatIfCostSheetPgTle;
            view = "WHATIF_COSTSHEET_PAGE";

        } else if("SAVE".equals(action)){
            try {
                LCSCostSheet from = (LCSCostSheet)LCSCostSheetQuery.findObjectById(request.getParameter("copyFromOid"));

                if(from instanceof LCSProductCostSheet){
                    csheetModel = (LCSCostSheetClientModel)from.copyState(csheetModel);
                    AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
                    csheetModel.setWhatIf(true);
                    csheetModel.setPrimaryCostSheet(false);
                    csheetModel.save();
                    oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                }else{
                    skucsheetModel = (LCSSKUCostSheetClientModel)from.copyState(skucsheetModel);
                    AttributeValueSetter.setAllAttributes(skucsheetModel, RequestHelper.hashRequest(request));
                    skucsheetModel.setWhatIf(true);
                    skucsheetModel.setPrimaryCostSheet(false);
                    skucsheetModel.save();
                    oid = FormatHelper.getObjectId(skucsheetModel.getBusinessObject());
                }

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_COSTSHEET";
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

           } catch(LCSException e){
                title = whatIfCostSheetPgTle;
                view = "WHATIF_COSTSHEET_PAGE";
                errorMessage=e.getLocalizedMessage();
           }
        }

        //flexType = productModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    }else if("UPDATE_WHATIF_COSTSHEET".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            if(request.getParameter("oid").indexOf("LCSProductCostSheet")>-1){
                csheetModel.load(request.getParameter("oid"));
            }else{
                skucsheetModel.load(request.getParameter("oid"));
            }
            title = whatIfCostSheetPgTle;
            view = "WHATIF_COSTSHEET_PAGE";
            type = FormatHelper.getObjectId(csheetModel.getFlexType());
        } else if("SAVE".equals(action)){

            try{
                if(oid.indexOf("LCSProductCostSheet")>-1){
                    csheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
                    csheetModel.save();
                    oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                }else{
                    skucsheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(skucsheetModel, RequestHelper.hashRequest(request));
                    skucsheetModel.save();
                    oid = FormatHelper.getObjectId(skucsheetModel.getBusinessObject());
                }

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_COSTSHEET";
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_WHATIF_COSTSHEET";
                title = whatIfCostSheetPgTle;
            }

        } else if("SET_AS_ACTIVE".equals(action)){

            try{

                if(oid.indexOf("LCSProductCostSheet")>-1){
                    csheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
                    csheetModel.setAsActive ();
                    oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                    if(FormatHelper.hasContent(returnOid)){
                        oid = returnOid;
                    }
                }else{
                    skucsheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(skucsheetModel, RequestHelper.hashRequest(request));
                    skucsheetModel.setAsActive ();
                    oid = FormatHelper.getObjectId(skucsheetModel.getBusinessObject());
                    if(FormatHelper.hasContent(returnOid)){
                        oid = returnOid;
                    }
                }

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                if( !FormatHelper.hasContent(tabPage) ){
                    tabPage="SOURCING";
                }
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_WHATIF_COSTSHEET";
                title = whatIfCostSheetPgTle;
            }

        }
        //flexType = productModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    }else if("VIEW_COSTSHEET".equals(activity)){
            lcsContext.setCacheSafe(true);
            if(request.getParameter("oid").indexOf("LCSProductCostSheet")>-1){
                csheetModel.load(request.getParameter("oid"));
            }else{
                skucsheetModel.load(request.getParameter("oid"));
            }
            title = viewCostSheetPgTle;
            view = "VIEW_COSTSHEET_PAGE";
    }else if("CREATE_COSTSHEET".equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            title = createCostSheetPgTle;
            type = FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRoot("Cost Sheet"));
            typeClass = "com.lcs.wc.sourcing.LCSProductCostSheet";
            additionalParameters = "sourcingId=" + request.getParameter("sourcingId");
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }
            view = PageManager.CLASSIFY_PAGE;


        } else if("CLASSIFY".equals(action)){
            if (FormatHelper.hasContent(oid)){
                appContext.setProductContext(oid);
            }

            lcsContext.setCacheSafe(true);
            view = "CREATE_COSTSHEET_PAGE";
            title = createCostSheetPgTle;
            request.setAttribute("layoutType", "DIV_LAYOUT");
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            String flexTypeId = request.getParameter("typeId");
            if (FormatHelper.hasContent(flexTypeId)) {
                flexType = FlexTypeCache.getFlexType(flexTypeId);
                type = flexTypeId;
            }

            additionalParameters = request.getParameter("additionalParameters");
            if(additionalParameters.contains("undefined")) {
                additionalParameters = additionalParameters.replaceAll("undefined", "");
            }
            if(FormatHelper.hasContent(request.getParameter("whatif"))){
                additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
            }
            if(FormatHelper.hasContent(request.getParameter("active"))){
                additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
            }
            additionalParameters = "seasonId="+seasonId +"&"+ additionalParameters;

        } else if("SAVE".equals(action)){
            try{

                String cs_Verid = "";
                csheetModel.setTypeId(request.getParameter("csTypeId"));
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
				logger.debug("**Cost Sheet Type:"+csheetModel.getFlexType().getFullName(true));
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				if(!csheetModel.getFlexType().getFullName(true).equalsIgnoreCase("Cost Sheet\\scSparc")){
				logger.debug("**Inside CREATE_COSTSHEET SAVE:"+RequestHelper.hashRequest(request));
				logger.debug("**MethodContext.getContext():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
				}
				logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				}
				if((csheetModel.getFlexType().getFullName(true).contains("Lucky")) && !(request.getParameter("representativeColorway")!=null) && !FormatHelper.hasContent(request.getParameter("representativeColorway"))){
						MethodContext.getContext().putIfAbsent("SPARC_NO_COLORWAY_PRESENT", "TRUE");				
				}
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
                csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                if(additionalParameters.contains("viewCostSheets=")) {
                    String firstHalf = additionalParameters.substring(0, additionalParameters.indexOf("viewCostSheets="));
                    additionalParameters = firstHalf + "viewCostSheets=" + cs_Verid + additionalParameters
                            .substring(additionalParameters.indexOf("viewCostSheets=") + "viewCostSheets=".length());
                } else {
                    additionalParameters = additionalParameters + "&viewCostSheets=" + cs_Verid;
                }
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }
                additionalParameters = additionalParameters +"&contextSKUId="+request.getParameter("contextSKUId");

            }catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "CREATE_COSTSHEET_PAGE";
                title = createCostSheetPgTle;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                oid = request.getParameter("oid");
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                if(csheetModel != null){
                    typeId = FormatHelper.getObjectId(csheetModel.getFlexType());
                }else{
                    typeId = FormatHelper.getObjectId(skucsheetModel.getFlexType());
                }
            }
        }
        if(csheetModel.getBusinessObject()!=null && csheetModel.getFlexType() != null){
            flexType=csheetModel.getFlexType();
        }else if(skucsheetModel.getBusinessObject()!=null && skucsheetModel.getFlexType() != null){
            flexType=skucsheetModel.getFlexType();
        }

    }else if("CREATE_MULTIPLE_COSTSHEET".equals(activity)){


        if(action == null || "INIT".equals(action) || "".equals(action)){

            if (FormatHelper.hasContent(oid)){
                appContext.setProductContext(oid);
            }

            lcsContext.setCacheSafe(true);
            if(appContext.getActiveProductId() == null && !FormatHelper.hasContent(request.getParameter("fromLineSheet"))){
                appContext.setProductContext(oid);
            }
            view = "CREATE_MULTIPLE_COSTSHEET_PAGE";
            title = createCostSheetPgTle;

            String flexTypeId = request.getParameter("csTypeId");
            if (FormatHelper.hasContent(flexTypeId)) {
                flexType = FlexTypeCache.getFlexType(flexTypeId);
                //type = flexTypeId;
            }

            additionalParameters = request.getParameter("additionalParameters");
            additionalParameters = "seasonId="+seasonId +"&"+ additionalParameters;

        } else if("SAVE".equals(action)){
            HashMap results = new HashMap();
             String dataString = request.getParameter("dataString");
             String sizeCategoryGroup = request.getParameter("sizeCategoryGroup");

             if(FormatHelper.hasContent(dataString)){
                HashMap map = new HashMap();
                StringTokenizer st = new MultiCharDelimStringTokenizer(dataString, MultiObjectHelper.ATTRIBUTE_DELIMITER);
                StringTokenizer st2;
                StringTokenizer st3;
                String keyValuePair = "";
                String key = "";
                String copiedKey="";
                String value = "";

                while(st.hasMoreTokens()){
                    keyValuePair = st.nextToken();
                    //logger.debug("***" + keyValuePair);
                    st2 = new MultiCharDelimStringTokenizer(keyValuePair, MultiObjectHelper.NAME_VALUE_DELIMITER);
                    if(st2.hasMoreTokens()){
                        key = st2.nextToken();
                        if((FormatHelper.hasContent(sizeCategoryGroup) || FormatHelper.hasContent(rfqRequestId)) && key.indexOf("_name") > -1){
                            if(st2.hasMoreTokens()){
                                value = st2.nextToken();
                                map.put(key, value);
                                copiedKey = key;
                                if(FormatHelper.hasContent(sizeCategoryGroup)){
                                    key = key.substring(0, key.indexOf("_name")) + "_sizeCategoryGroup";
                                    map.put(key, sizeCategoryGroup);
                                }
                                if(FormatHelper.hasContent(rfqRequestId)){
                                    copiedKey = copiedKey.substring(0, copiedKey.indexOf("_name")) + "_rfqRequestId";
                                    map.put(copiedKey, rfqRequestId);
                                }
                            }
                        }else if(key.equals("whatIf")){
                            if(st2.hasMoreTokens()){
                                value = st2.nextToken();
                                st3 = new MultiCharDelimStringTokenizer(value, MOAHelper.DELIM);
                                while(st3.hasMoreTokens()){
                                    key = st3.nextToken();
                                    if(FormatHelper.hasContent(key)){
                                        key = key + "_LCSPRODUCTCOSTSHEET$" + key.substring(key.indexOf("M_F_")+ 4) + "_whatIf";
                                        map.put(key, "true");
                                    }
                                }
                            }
                        }else{
                            if(st2.hasMoreTokens()){
                                map.put(key, st2.nextToken());
                            }else{
                                map.put(key, "");
                            }
                        }
                    }
                }

                dataString = MultiObjectHelper.createPackagedStringFromMultiFormMap2(map);
                results = new BulkSourcingCreationUtility().bulkCreate(dataString, wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")).toString());
                if(rfqRequestId != null){
                    additionalParameters = "fromRFQ=true";

                }
                String totalObjectCreated = "0";
                if(results.get("totalCreatedObjects")!=null){
                    totalObjectCreated = (String)results.get("totalCreatedObjects");
                }
                infoMessage = "totalCreatedObjects:" + totalObjectCreated;

                if(results.get("failedRows") != null){
                    String failedRows = (String)results.get("failedRows");

                }
                if(results.get("errorMessages") != null){
                    errorMessage = (String)results.get("errorMessages");
                }
             }
            view = "CREATE_MULTIPLE_COSTSHEET_PAGE";
            title = "";
            ajaxWindow = "true";
            templateName = AJAX_TEMPLATE;
        }

    }else if("UPDATE_COSTSHEET".equals(activity)){
        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            if(oid.indexOf("LCSProductCostSheet")>-1){
                csheetModel.load(oid);
            }else{
                skucsheetModel.load(oid);
            }
            if(USE_MULTI_COSTING){
                view = "UPDATE_MULTI_COSTSHEET_PAGE";
                request.setAttribute("layoutType", "DIV_LAYOUT");
                contextHeaderPage = "PRODUCT_PAGE_HEADER";
            }else{
                view = "UPDATE_COSTSHEET_PAGE";
            }
            title = updateCostSheetPgTle;

        } else if("SAVE".equals(action)){
            try{
                String imageAttribute = request.getParameter("imageAttribute");

                if(oid.indexOf("LCSProductCostSheet")>-1){
                    csheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
					
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				logger.debug("**Cost Sheet Type:"+csheetModel.getBusinessObject().getFlexType().getFullName(true));
				
				if(!csheetModel.getBusinessObject().getFlexType().getFullName(true).equalsIgnoreCase("Cost Sheet\\scSparc")){
				logger.debug("**Inside UPDATE_COSTSHEET SAVE:"+RequestHelper.hashRequest(request));
				logger.debug("**MethodContext.getContext().keySet():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
				}
				logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				}
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
				
				if((csheetModel.getBusinessObject().getFlexType().getFullName(true).contains("Lucky")) && !(request.getParameter("representativeColorway")!=null) && !FormatHelper.hasContent(request.getParameter("representativeColorway"))){
						MethodContext.getContext().putIfAbsent("SPARC_NO_COLORWAY_PRESENT", "TRUE");				
				}
				
                    if(FormatHelper.hasContent(imageAttribute)){ // update done by setImage activity
                        csheetModel.updateCostSheetForImage();
                    }else{
                        csheetModel.save();
                    }
                    oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                }else{
                    skucsheetModel.load(oid);
                    AttributeValueSetter.setAllAttributes(skucsheetModel, RequestHelper.hashRequest(request));
                    if(FormatHelper.hasContent(imageAttribute)){ // update done by setImage activity
                        skucsheetModel.updateCostSheetForImage();
                    }else{
                        skucsheetModel.save();
                    }
                    oid = FormatHelper.getObjectId(skucsheetModel.getBusinessObject());
                }

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_COSTSHEET_PAGE";
                title = updateCostSheetPgTle;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            }
        }
        if(csheetModel.getBusinessObject()!=null){
            flexType = csheetModel.getFlexType();
        }else{
            flexType = skucsheetModel.getFlexType();
        }
    }
    else if("UPDATE_MULTI_COSTSHEET".equals(activity)){
        if(action == null || "INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            csheetModel.load(oid);
            view = "UPDATE_MULTI_COSTSHEET_PAGE";
            title = updateCostSheetPgTle;
            request.setAttribute("layoutType", "DIV_LAYOUT");
            contextHeaderPage = "PRODUCT_PAGE_HEADER";

            additionalParameters = request.getParameter("additionalParameters");

        } else if("SAVE".equals(action)){
            try{
                csheetModel.load(oid);

                Hashtable requestHash = RequestHelper.hashRequest(request);
                if(!FormatHelper.hasContent(request.getParameter("representativeSize")))requestHash.put("representativeSize"," ");
                if(!FormatHelper.hasContent(request.getParameter("representativeSize2")))requestHash.put("representativeSize2"," ");

                AttributeValueSetter.setAllAttributes(csheetModel, requestHash);
                // D-23159
                if(requestHash.containsKey("specReference") && !FormatHelper.hasContent((String)requestHash.get("specReference"))){
                    csheetModel.setSpecReference(null);
                }
				
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				logger.debug("**Cost Sheet Type:"+csheetModel.getBusinessObject().getFlexType().getFullName(true));
				
				if(!(csheetModel.getBusinessObject().getFlexType().getFullName(true).equalsIgnoreCase("Cost Sheet\\scSparc"))){
				logger.debug("**Inside UPDATE_MULTI_COSTSHEET SAVE:"+RequestHelper.hashRequest(request));
				logger.debug("**MethodContext.getContext().keySet():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
					
					}
					logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				}
				
				if((csheetModel.getBusinessObject().getFlexType().getFullName(true).contains("Lucky")) && !(request.getParameter("representativeColorway")!=null) && !FormatHelper.hasContent(request.getParameter("representativeColorway"))){
						MethodContext.getContext().putIfAbsent("SPARC_NO_COLORWAY_PRESENT", "TRUE");				
				}
				
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
				
                csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                String cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                additionalParameters = request.getParameter("additionalParameters") + MOAHelper.DELIM + cs_Verid;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "UPDATE_MULTI_COSTSHEET_PAGE";
                request.setAttribute("layoutType", "DIV_LAYOUT");
                contextHeaderPage = "PRODUCT_PAGE_HEADER";
                title = updateCostSheetPgTle;
            }
        }

        flexType = csheetModel.getFlexType();
        } else if("REFRESH_COSTSHEET".equals(activity)){
        try{
            if(oid.indexOf("LCSProductCostSheet")>-1){
                csheetModel.load(oid);
                csheetModel.refresh();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                flexType = csheetModel.getFlexType();
            }else{
                skucsheetModel.load(oid);
                skucsheetModel.refresh();
                oid = FormatHelper.getObjectId(skucsheetModel.getBusinessObject());
                flexType = skucsheetModel.getFlexType();
            }

            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;
            if(FormatHelper.hasContent(returnOid)){
                oid = returnOid;
            }
            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
        }
    }
     else if("REFRESH_MULTI_COSTSHEET".equals(activity)){
        try{

            csheetModel.load(oid);
            //csheetModel.save();
			
			//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
			logger.debug("**Inside REFRESH_MULTI_COSTSHEET SAVE:"+RequestHelper.hashRequest(request));
			
				logger.debug("**Cost Sheet Type:"+csheetModel.getBusinessObject().getFlexType().getFullName(true));
				
				if(!(csheetModel.getBusinessObject().getFlexType().getFullName(true).equalsIgnoreCase("Cost Sheet\\scSparc"))){
			logger.debug("*** Representaive colorway"+request.getParameter("representativeColorway"));
			logger.debug("**MethodContext.getContext():"+MethodContext.getContext().keySet());
            if(request.getParameter("representativeColorway")==null || !FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_REFRESH_MULTI_COSTSHEET", "SPARC_REFRESH_MULTI_COSTSHEET");
					MethodContext.getContext().putIfAbsent("SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP", "SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP");
                }
			logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
			}

			//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
			
            csheetModel.refresh();
            oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;
            if(FormatHelper.hasContent(returnOid)){
                oid = returnOid;
            }
            String activeBox = "active=" + request.getParameter("active");
            String whatIfBox = "whatif=" + request.getParameter("whatif");
            additionalParameters = activeBox + "&" + whatIfBox;
            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
        }
    }
    else if("ADD_EXISTING_SOURCES".equals(activity)){
        if("ADD_SOURCES".equals(action)){
            try{
               LCSSeason season = (LCSSeason)LCSSourcingConfigQuery.findObjectById(oid);
               String oids = request.getParameter("oids");
               if(FormatHelper.hasContent(oids)){
                    Collection objects = LCSQuery.getObjectsFromCollection(MOAHelper.getMOACollection(oids));

                    SourcingConfigHelper.service.createSourceToSeasonLinks(objects, season);
               }

                oid = returnOid;
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;

            } catch(LCSException e){
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                oid = returnOid;
                errorMessage=e.getLocalizedMessage();
            }
        }
    }  else if("DELETE_COSTSHEET".equals(activity)){
         if("DELETE".equals(action)){
            try{
                if(oid.indexOf("LCSProductCostSheet")>-1){
                    csheetModel.load(oid);
                    LCSDeleteHelper.service.delete((LCSCostSheet)csheetModel.getBusinessObject());
                    flexType = csheetModel.getFlexType();
                }else{
                    skucsheetModel.load(oid);
                    LCSDeleteHelper.service.delete((LCSCostSheet)skucsheetModel.getBusinessObject());
                    flexType = skucsheetModel.getFlexType();
                }
                oid = returnOid;
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;

                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                oid = returnOid;
            }
        }
    ///////////////////////////////////////////////////////////////////
        }else if("SET_COSTSHEET_PRIMARY".equals(activity)){
        try{
            csheetModel.load(oid);
            csheetModel.setAsPrimary();
            oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
            view = PageManager.SAFE_PAGE;
            title = sourcingConfigurationDetailsPgTle;
            action = "INIT";
            activity = "VIEW_SEASON_PRODUCT_LINK";
            checkReturn = true;

            String activeBox = "active=" + request.getParameter("active");
            String whatIfBox = "whatif=" + request.getParameter("whatif");
            additionalParameters = activeBox + "&" + whatIfBox;

            if(request.getParameter("costSheetName")!=null){
                String cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());
                if(request.getParameter("additionalParameters")!=null){
                      additionalParameters = additionalParameters + "&" + request.getParameter("additionalParameters") + MOAHelper.DELIM + cs_Verid;
                }else{
                      additionalParameters = additionalParameters + "&viewCostSheets=" + cs_Verid;
                }
            }

            if(FormatHelper.hasContent(returnAddlParams)){
                additionalParameters = additionalParameters + returnAddlParams;
            }
        } catch(LCSException e){
            errorMessage = e.getLocalizedMessage();
            view = "VIEW_SOURCINGCONFIG_PAGE";
            title = viewSourcingConfigurationPgTle;
        }

    ///////////////////////////////////////////////////////////////////
    } else if("COPY_COSTSHEET".equals(activity)||"COPY_COSTSHEET_TO_ACTIVE".equals(activity)){
        if(action == null || "INIT".equals(action) || "".equals(action)){
            csheetModel.load(request.getParameter("copyFromOid"));
            //TODO-Costsheet
            LCSCostSheet cs = new LCSProductCostSheet();
            cs.setFlexType(csheetModel.getFlexType());
            cs.initDefaultValues();
            Map preserveMap = PropertyBasedAttributeValueLogic.backupPreserveAttributes(cs);
            csheetModel.copyState(csheetModel.getBusinessObject());
            PropertyBasedAttributeValueLogic.restorePreserveAttributes(csheetModel.getBusinessObject(), preserveMap);

            PropertyBasedAttributeValueLogic.setAttributes(csheetModel.getBusinessObject(), csheetModel.getBusinessObject().getClass().getName(), csheetModel.getBusinessObject().getCostSheetType(), "COPY");

            csheetModel.getBusinessObject().copyState(csheetModel);
            view = "COPY_COSTSHEET_PAGE";
            title = copyCostSheetPgTle;
            lcsContext.setCacheSafe(true);

            request.setAttribute("layoutType", "DIV_LAYOUT");
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            
            String flexTypeId = request.getParameter("csTypeId");
            if (FormatHelper.hasContent(flexTypeId)) {
                flexType = FlexTypeCache.getFlexType(flexTypeId);
            }
            additionalParameters = request.getParameter("additionalParameters");
            if(!FormatHelper.hasContent(seasonId)){
                seasonId = "VR:com.lcs.wc.season.LCSSeason:" + new Double("" + csheetModel.getSeasonRevId()).longValue();
            }
            additionalParameters = "seasonId="+seasonId +"&"+ additionalParameters;
        } else if("SAVE".equals(action)){
            try{
                LCSCostSheet from = (LCSCostSheet)LCSCostSheetQuery.findObjectById(request.getParameter("copyFromOid"));
                //csheetModel = (LCSCostSheetClientModel)from.copyState(csheetModel);
                String flexTypeId = request.getParameter("csTypeId");
                if(FormatHelper.hasContent(flexTypeId)){
                    csheetModel.setTypeId(flexTypeId);
                }
                csheetModel.setCopiedFrom(from);
                //SPR 2094089 force to copy image attributes firstly
                AttributeValueSetter.copyImageAttributes(from,csheetModel) ;
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));

                csheetModel.setPrimaryCostSheet(false);
                csheetModel.setCostSheetType("PRODUCT");
                if("COPY_COSTSHEET_TO_ACTIVE".equals(activity)){
                    csheetModel.setWhatIf(false);
                }
                csheetModel.setSkipAttributeTranferPlugin(true);
				
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				logger.debug("**Inside COPY_COSTSHEET OR COPY_COSTSHEET_TO_ACTIVE - SAVE:"+RequestHelper.hashRequest(request));
				logger.debug("*** Representaive colorway"+request.getParameter("representativeColorway"));
				logger.debug("**MethodContext.getContext():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
				}
				logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
				
				if((csheetModel.getFlexType().getFullName(true).contains("Lucky")) && !(request.getParameter("representativeColorway")!=null) && !FormatHelper.hasContent(request.getParameter("representativeColorway")))
				{
						MethodContext.getContext().putIfAbsent("SPARC_NO_COLORWAY_PRESENT", "TRUE");				
				}
                csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                String cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                if("COPY_COSTSHEET_TO_ACTIVE".equals(returnActivity)){
                    action = "INIT";
                    activity = "VIEW_SEASON_PRODUCT_LINK";
                    oid = appContext.getActiveProductId();
                }else{
                    checkReturn = true;
                }
                additionalParameters = request.getParameter("additionalParameters") +MOAHelper.DELIM + cs_Verid;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                csheetModel.load(request.getParameter("copyFromOid"));
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                view = "COPY_COSTSHEET_PAGE";
                title = copyCostSheetPgTle;
            }
        }
        flexType = csheetModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
      } else if("WHATIF_MULTI_COSTSHEET".equals(activity)){
        boolean initAction =action == null || "INIT".equals(action) || "".equals(action);
        boolean hasCopiedFromOid = FormatHelper.hasContent(request.getParameter("copyFromOid"));
        if((initAction)&&!hasCopiedFromOid  ){
            title = createCostSheetPgTle;
            type = FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRoot("Cost Sheet"));
            typeClass = "com.lcs.wc.sourcing.LCSProductCostSheet";
            additionalParameters = "sourcingId=" + request.getParameter("sourcingId");
            if(FormatHelper.hasContent(request.getParameter("whatif"))){
                additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
            }
            if(FormatHelper.hasContent(request.getParameter("active"))){
                additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
            }

            view = PageManager.CLASSIFY_PAGE;

        } else if("CLASSIFY".equals(action) || (initAction && hasCopiedFromOid)){

            if(hasCopiedFromOid) {
                csheetModel.load(request.getParameter("copyFromOid"));
                view = "WHATIF_MULTI_COSTSHEET_PAGE";
            }else{
                view = "WHATIF_MULTI_COSTSHEET_NEW_PAGE";
            }

            lcsContext.setCacheSafe(true);
            title = whatIfCostSheetPgTle;
            if(csheetModel!=null && csheetModel.getFlexType()!=null){
                type= FormatHelper.getObjectId(csheetModel.getFlexType());
            }else{
                String flexTypeId = request.getParameter("typeId");
                if (FormatHelper.hasContent(flexTypeId)) {
                    flexType = FlexTypeCache.getFlexType(flexTypeId);
                    type = flexTypeId;
                }
            }

            if(initAction && hasCopiedFromOid){
                //TODO-Costsheet
                LCSCostSheet cs = new LCSProductCostSheet();
                cs.setFlexType(csheetModel.getFlexType());
                cs.initDefaultValues();
                Map preserveMap = PropertyBasedAttributeValueLogic.backupPreserveAttributes(cs);
                csheetModel.copyState(csheetModel.getBusinessObject());
                PropertyBasedAttributeValueLogic.restorePreserveAttributes(csheetModel.getBusinessObject(), preserveMap);

                PropertyBasedAttributeValueLogic.setAttributes(csheetModel.getBusinessObject(), csheetModel.getBusinessObject().getClass().getName(), csheetModel.getBusinessObject().getCostSheetType(), "COPY");

                csheetModel.getBusinessObject().copyState(csheetModel);
            }
            
            request.setAttribute("layoutType", "DIV_LAYOUT");
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            
            additionalParameters = request.getParameter("additionalParameters");
            if(FormatHelper.hasContent(request.getParameter("whatif"))){
                additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
            }
            if(FormatHelper.hasContent(request.getParameter("active"))){
                additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
            }
            additionalParameters = "seasonId="+seasonId +"&"+ additionalParameters;

        } else if("SAVE".equals(action)){
            try {
                LCSCostSheet from = (LCSCostSheet)LCSCostSheetQuery.findObjectById(request.getParameter("copyFromOid"));
                csheetModel = (LCSCostSheetClientModel)from.copyState(csheetModel);
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));

                //session.setAttribute("lastCreatedOID", FormatHelper.getVersionId(productModel.getBusinessObject()));
                csheetModel.setWhatIf(true);
                if(csheetModel.isPrimaryCostSheet()) {
                    csheetModel.setPrimaryCostSheet(false);
                }

                csheetModel.setCopiedFrom(from);
                csheetModel.setSkipAttributeTranferPlugin(true);
				
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				logger.debug("**Inside WHATIF_MULTI_COSTSHEET SAVE :"+RequestHelper.hashRequest(request));
				logger.debug("*** Representaive colorway"+request.getParameter("representativeColorway"));
				logger.debug("**MethodContext.getContext():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
				}
				
				
				logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - end
				
                csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                String cs_WhatIfVerid = FormatHelper.getVersionId(csheetModel.getBusinessObject());

                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                if("WHATIF_MULTI_COSTSHEET".equals(returnActivity)){
                    action = "INIT";
                    activity = "VIEW_SEASON_PRODUCT_LINK";
                    oid = appContext.getActiveProductId();
                }else{
                    checkReturn = true;
                }
                additionalParameters = request.getParameter("additionalParameters") + "&viewCostSheets=" + cs_WhatIfVerid;
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }
                additionalParameters = additionalParameters +"&contextSKUId="+request.getParameter("contextSKUId");

           } catch(LCSException e){
                title = whatIfCostSheetPgTle;
                errorMessage=e.getLocalizedMessage();
                typeId = FormatHelper.getObjectId(csheetModel.getFlexType());
                if(hasCopiedFromOid) {
                    csheetModel.load(request.getParameter("copyFromOid"));
                    view = "WHATIF_MULTI_COSTSHEET_PAGE";
                }else{
                    view = "WHATIF_MULTI_COSTSHEET_NEW_PAGE";
                }
                additionalParameters = request.getParameter("additionalParameters");
                additionalParameters = "seasonId="+seasonId +"&"+ additionalParameters;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
           }
        } else if("SAVE_NEW".equals(action)){
            try {
                csheetModel.setTypeId(request.getParameter("csTypeId"));
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));

                //session.setAttribute("lastCreatedOID", FormatHelper.getVersionId(productModel.getBusinessObject()));
                csheetModel.setWhatIf(true);
                csheetModel.setCostSheetType("PRODUCT");
				
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				logger.debug("**Inside WHATIF_MULTI_COSTSHEET SAVE_NEW:"+RequestHelper.hashRequest(request));
				logger.debug("*** Representaive colorway"+request.getParameter("representativeColorway"));
				logger.debug("**MethodContext.getContext():"+MethodContext.getContext().keySet());
                if(request.getParameter("representativeColorway")!=null && FormatHelper.hasContent(request.getParameter("representativeColorway"))){    
                    MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY", request.getParameter("representativeColorway"));
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", request.getParameter("representativeColorway"));
                }
				else{
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT", "TRUE");
					MethodContext.getContext().putIfAbsent("SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "TRUE");
				}
				logger.debug("**MethodContext.getContext -  AFTER :"+MethodContext.getContext().keySet());
				//Changes to set the Representative Colorway into Method Context for non calculated SSP - start
				
                csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                String cs_WhatIfNewVerid = FormatHelper.getVersionId(csheetModel.getBusinessObject());
                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;
                if(additionalParameters.contains("viewCostSheets=")) {
                    additionalParameters = additionalParameters.substring(0, additionalParameters.indexOf("viewCostSheets="))+"viewCostSheets="+cs_WhatIfNewVerid;
                } else {
                    additionalParameters = additionalParameters + "viewCostSheets="+cs_WhatIfNewVerid;
                }
                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }
                if(FormatHelper.hasContent(request.getParameter("whatif"))){
                    additionalParameters = additionalParameters + "&whatif=" + request.getParameter("whatif");
                }
                if(FormatHelper.hasContent(request.getParameter("active"))){
                    additionalParameters = additionalParameters + "&active=" + request.getParameter("active");
                }
                additionalParameters = additionalParameters +"&contextSKUId="+request.getParameter("contextSKUId");

           } catch(LCSException e){
                title = whatIfCostSheetPgTle;
                view = "WHATIF_MULTI_COSTSHEET_NEW_PAGE";
                errorMessage=e.getLocalizedMessage();
                typeId = FormatHelper.getObjectId(csheetModel.getFlexType());
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
           }
        } else if("SET_AS_ACTIVE_NEW".equals(action)){
            try{
                csheetModel.load(oid);
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
                //csheetModel.setWhatIf(false);
                csheetModel.setAsActive();

                //csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());

                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;

                String activeBox = "active=" + request.getParameter("active");
                String whatIfBox = "whatif=" + request.getParameter("whatif");
                additionalParameters = activeBox + "&" + whatIfBox;

                if(request.getParameter("costSheetName")!=null){
                    String cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());
                    if(request.getParameter("additionalParameters")!=null){
                          additionalParameters = additionalParameters + "&" + request.getParameter("additionalParameters") + MOAHelper.DELIM + cs_Verid;
                    }else{
                          additionalParameters = additionalParameters + "&viewCostSheets=" + cs_Verid;
                    }
                }


                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
            }
        } else if("SET_AS_WHATIF_NEW".equals(action)){
            try{
                csheetModel.load(oid);
                AttributeValueSetter.setAllAttributes(csheetModel, RequestHelper.hashRequest(request));
                csheetModel.setAsWhatIf();

                //csheetModel.save();
                oid = FormatHelper.getObjectId(csheetModel.getBusinessObject());
                if(FormatHelper.hasContent(returnOid)){
                    oid = returnOid;
                }
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                checkReturn = true;

                String activeBox = "active=" + request.getParameter("active");
                String whatIfBox = "whatif=" + request.getParameter("whatif");
                additionalParameters = activeBox + "&" + whatIfBox;

                if(request.getParameter("costSheetName")!=null){
                    String cs_Verid = FormatHelper.getVersionId(csheetModel.getBusinessObject());
                    if(request.getParameter("additionalParameters")!=null){
                          additionalParameters = additionalParameters + "&" + request.getParameter("additionalParameters") + MOAHelper.DELIM + cs_Verid;
                    }else{
                          additionalParameters = additionalParameters + "&viewCostSheets=" + cs_Verid;
                    }
                }

                if(FormatHelper.hasContent(returnAddlParams)){
                    additionalParameters = additionalParameters + returnAddlParams;
                }

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.SAFE_PAGE;
                title = sourcingConfigurationDetailsPgTle;
                action = "INIT";
                activity = "VIEW_SEASON_PRODUCT_LINK";
            }
        }
        //flexType = productModel.getFlexType();

    ///////////////////////////////////////////////////////////////////
    }
    if (FormatHelper.hasContent(request.getParameter("viewId")) && FormatHelper.hasContent(additionalParameters) && !additionalParameters.contains("viewId=")) {
        additionalParameters = "viewId=" + request.getParameter("viewId") + "&" + additionalParameters;
    }

    String contentPage = null;
    if(view != null){
        contentPage = PageManager.getPageURL(view, null);
        flexPlmWebRequestContext.pageLookupKey = view;
    } else {
        contentPage = "";
    }

    ////////////////////////////////////////////////////////////////////////////
    // GET THE CLIENT MODEL'S FLEX TYPE FULL NAME
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
        if(FormatHelper.hasContent(returnAddlParams)){
            if(FormatHelper.hasContent(additionalParameters)){
                additionalParameters = additionalParameters + "&" + returnAddlParams;
            }
            else{
                additionalParameters = returnAddlParams;
            }
        }
        oid = returnOid;
        returnActivity = "";
        returnAction = "";
        returnOid = "";
    }

%>
<jsp:forward page="<%=subURLFolder+ templateName %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
    <jsp:param name="infoMessage" value="<%= infoMessage %>" />
   <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
    <jsp:param name="requestedPage" value="<%= contentPage %>" />
    <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="formType" value="<%= formType %>" />
    <jsp:param name="activity" value="<%= activity %>" />
    <jsp:param name="objectType" value="Sourcing Configuration" />
    <jsp:param name="typeClass" value="<%= typeClass %>" />
    <jsp:param name="type" value="<%= type %>" />
    <jsp:param name="productId" value="<%= productId %>" />
    <jsp:param name="flexTypeName" value="<%= flexTypeName %>" />
    <jsp:param name="additionalParameters" value="<%= additionalParameters %>" />
    <jsp:param name="contextHeaderPage" value="<%= contextHeaderPage %>" />
    <jsp:param name="ajaxWindow" value="<%=ajaxWindow%>" />
    <jsp:param name="rfqRequestId" value="<%=rfqRequestId%>" />
    <jsp:param name="typeId" value="<%=typeId%>" />
    <jsp:param name="scTypeId" value="<%=scTypeId%>" />
    <jsp:param name="tabPage" value="<%= tabPage %>" />
</jsp:forward>
