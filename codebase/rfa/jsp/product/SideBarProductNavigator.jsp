<%-- Copyright (c) 2020 PTC Inc.   All Rights Reserved --%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ///////////////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
    errorPage="../../exception/ErrorReport.jsp"
    import=" com.lcs.wc.util.*,
            com.lcs.wc.util.OSHelper,
            com.lcs.wc.client.Activities,
            com.lcs.wc.client.web.*,
            com.lcs.wc.client.web.html.*,
            com.lcs.wc.flextype.*,
            com.lcs.wc.db.*,
            com.lcs.wc.resource.ContextBarsRB,
            com.lcs.wc.season.*,
            com.lcs.wc.sourcing.*,
            com.lcs.wc.specification.*,
            com.lcs.wc.part.*,
            com.lcs.wc.foundation.*,
            com.lcs.wc.product.*,
            com.lcs.wc.changeAudit.*,
            java.io.*,
            java.util.*,
            wt.util.*,
            org.apache.logging.log4j.Logger,
            org.apache.logging.log4j.LogManager,
            wt.fc.*,
            wt.part.*,
            wt.access.NotAuthorizedException,
            com.lcs.wc.client.web.image.*"
%><%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS //////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE /////////////////////////////--%>
<%-- ////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.v2.product.SideBarProductNavigator");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");

    private static final String JSPNAME = "SideBarProductNavigator";

    public static String getUrl = "";
    public static String WindchillContext = "/Windchill";

    static
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            getUrl = wtproperties.getProperty("wt.server.codebase","");
            WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String maxWidth = "266";//LCSProperties.get("jsp.image.ObjectThumbnailPlugin.imageWidth", "100");
    public static final int colorwayCountPerSlide = 12;
    public static final String THUMBNAIL_LIGHT_BOX = PageManager.getPageURL("THUMBNAIL_LIGHT_BOX", null);

    //Properties for new quick info panel
    private static final String currencyAtt = LCSProperties.get("jsp.product.sidebar.currencyAtt"); //Empty if none defined
    private static final String headerAtt = LCSProperties.get("jsp.product.sidebar.identifierAtt","productName");  //Default will be the product name att if none defined
    private static final String subheaderAtt = LCSProperties.get("jsp.product.sidebar.statusAtt");  //Default will be the lifecycle state if none defined
    private static final Map<String,String> panelAtts = LCSProperties.getPropertyEntriesStartWith("jsp.product.sidebar.classificationAtts"); //hash map of atts or empty if none defined
    private static final String emphasisAtt = LCSProperties.get("jsp.product.sidebar.fabricGroupAtt"); //Default will be empty if none defined

%>
<%
    // LOCALIZED LABELS
    String colorways_LBL = WTMessage.getLocalizedMessage ( RB.PRODUCT, "colorways_LBL", RB.objA ) ;
    String imagesLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "prodThumbnail_LBL", RB.objA ) ;
    String createSkusLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "createSkus_LBL", RB.objA ) ;
    String trackedChangesLabel = WTMessage.getLocalizedMessage ( RB.CONTEXTBARS, "changes_LBL", RB.objA ) ;
    String newLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "new_Btn", RB.objA ) ;
    String carryoverLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "carryover_LBL", RB.objA ) ;

    String oid = java.net.URLDecoder.decode(request.getParameter("oid"), defaultCharsetEncoding);
    if(logger.isDebugEnabled())logger.debug(JSPNAME +": oid= " + oid);
    String quickNavTab = FormatHelper.format(request.getParameter("quickNavTab"));
    if(logger.isDebugEnabled())logger.debug(JSPNAME +": quickNavTab= " + quickNavTab);

    // LOCATE THE SEASON OBJECT. NEEDED FOR
    // GETTING ACCURATE SKU, SOURCE, AND SPEC LISTS.
    // WILL BE NULL OF PRODUCT IS A REV.
    LCSSKU sku = appContext.getSKUARev();
    // LOCATE SKU LIST BASED ON SEASON
    Collection<FlexObject> skus = appContext.getSKUsData();
    // SET SKU LEVEL
    boolean skuLevel = false;
    if(sku != null){
        skuLevel = true;
    }
    if(!appContext.getSKUsMap().keySet().contains(FormatHelper.format(request.getParameter("contextSKUId"))) && FormatHelper.hasContent(request.getParameter("contextSKUId"))){
        skuLevel=false;
    }
    FlexSpecToSeasonLink fstsl = appContext.getFstsl();

    // REPLACE WITH FLEXTYPEDINFO
    appContext.setProductContext(oid);
    if(fstsl != null){
        appContext.setProductContext(fstsl);
    }
    LCSProduct productRevA = appContext.getProductARev();
    String productId = appContext.getProductARevId();
    LCSSeason season = appContext.getSeason();
    LCSProduct productSeasonRev = appContext.getProductSeasonRev();
    LCSSeasonProductLink productSeasonLink = appContext.getProductLink();
    LCSProduct product = productSeasonRev!=null ? productSeasonRev : productRevA;
    String splId = productSeasonLink!=null ? FormatHelper.getObjectId(productSeasonLink) : "";
    String thumbLocation = null;
    String skuThumbnail = null;
    if(null != sku) {
        appContext.setSKUContext(sku);
        String skuBranchId = FormatHelper.getNumericVersionIdFromObject(sku);
        FlexObject skuObject = skus.stream()
        .filter(skuObj -> skuBranchId.equals(skuObj.getString("LCSSKU.BRANCHIDITERATIONINFO"))).findFirst().orElse(null);
        if(skuObject != null) {
            skuThumbnail = skuObject.getData("LCSSKU.PARTPRIMARYIMAGEURL");
        }
    }
    if (FormatHelper.hasContent(skuThumbnail)){
        thumbLocation = skuThumbnail;
        imagesLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "skuThumbnail_LBL", RB.objA ) ;
    } else {
        thumbLocation = productRevA.getPartPrimaryImageURL();
    }

%>
<script>
document.SEASON_PRODUCT_PAGE = new Object();
document.SEASON_PRODUCT_PAGE.prodActiveId = '<%= appContext.getActiveProductId() %>';
document.SEASON_PRODUCT_PAGE.productMasterId = '<%= appContext.getProductMasterId() %>';
document.SEASON_PRODUCT_PAGE.hasSeason = <%= (season != null)%>;
document.SEASON_PRODUCT_PAGE.productSeasonId = '<%= appContext.getActiveProductId() %>';
document.SEASON_PRODUCT_PAGE.seasonVersionId = '<%= FormatHelper.format(appContext.getSeasonId()) %>';
document.SEASON_PRODUCT_PAGE.returnOid = '<%= appContext.getActiveProductId() %>';
<% if(appContext.getSourcingConfig() != null){%>
document.SEASON_PRODUCT_PAGE.sourcingConfigVersionId = '<%= FormatHelper.getVersionId(appContext.getSourcingConfig()) %>';
document.SEASON_PRODUCT_PAGE.sourcingConfigMasterObjectId = '<%= FormatHelper.getObjectId((LCSSourcingConfigMaster)appContext.getSourcingConfig().getMaster()) %>';
<%}else{%>
document.SEASON_PRODUCT_PAGE.sourcingConfigVersionId = '';
document.SEASON_PRODUCT_PAGE.sourcingConfigMasterObjectId = '';
<%}%>

document.APP_CONTEXT = new Object();
document.APP_CONTEXT.activeProductId ='<%= appContext.getActiveProductId() %>';
document.APP_CONTEXT.productARevId='<%= appContext.getProductARevId() %>';
document.APP_CONTEXT.skuARevId='<%= appContext.getSKUARevId() %>';
document.APP_CONTEXT.seasonId='<%= appContext.getSeasonId() %>';
document.APP_CONTEXT.productLinkId='<%=appContext.getProductLinkId() %>';
document.APP_CONTEXT.placeholderId='<%=appContext.getPlaceholderId()%>';
document.SEASON_HEADER = new Object();
document.SEASON_HEADER.seasonVId =  '<%= FormatHelper.getVersionId(season) %>';
<%if(season == null){%>
document.SEASON_HEADER.seasonMasterId = '';
<%}else{%>
document.SEASON_HEADER.seasonMasterId = '<%= FormatHelper.getObjectId((WTObject)season.getMaster())%>';
<%}%>

</script>
<div class="flex-content">
  <!-- header BEGIN-->
  <div id='navHeader' class='flex-content-header'>
    <a href="javascript:loadProductNavigatorSCContext('<%= FormatHelper.getVersionId(product) %>')">
      <%= FormatHelper.encodeAndFormatForHTMLContent(productRevA.getValue("productName").toString()) %>
      </a>
  </div>
  <!-- header END-->
  <%
  String dateStr = request.getParameter("globalChangeTrackingSinceDate");
  Date sinceDate = FormatHelper.parseDate(dateStr.trim());
  if (sinceDate == null) {
     logger.error("Change tracking since date not specified (parameter value=" + dateStr + "); using default date.");
     sinceDate = ChangeTrackingPageHelper.getDefaultShowChangesSinceDate();
  }
  Collection<ChangeAuditEvent> auditEvents = ChangeAuditQuery.getLatestChangeAuditEvents((LCSPartMaster)productRevA.getMaster(),null,null);

  %>

  <input type="hidden" id="cancelBackInfo" name="cancelBackInfo" value="">
  <input type="hidden" name="splId" value="<%=splId%>">
  <div id='navContent' class='flex-content-main'>
      <!-- Images BEGIN-->
      <div id='imagesPanel' class='card'>
          <input type="checkbox" id="imagePanelCard" class="cardCheckbox" checked disabled>
          <div id='imagesPanelHeader' class='card-header'>
              <label class="card-label" for="imagePanelCard">
                  <%= imagesLabel %>
              </label>
              <div class="card-header-menu">
                  <div id='imagePanelNewThumbnail' class='imagePanelNewThumbnail'
                          onclick="javascript:openLightbox(event, '<%= THUMBNAIL_LIGHT_BOX %>', 'ThumbnailInfo', 'oid=<%=productId %>&contextObjectType=product');">
                  </div>
              </div>
          </div>
          <div id='imagesPanelContent' class='card-content'>
            <div id="fileDropZone" class="sideBarFileDropZone">
              <%
              ObjectThumbnailPlugin plugin = ObjectThumbnailPlugin.getObjectThumbnailPlugin();
              ObjectThumbnailPluginOptions options = new ObjectThumbnailPluginOptions();
              // Update Image for Colorway or Product based on Context
              if(skuLevel && sku != null) {
                  String imageURL =  skuThumbnail;
                  if(!FormatHelper.hasContent(imageURL)){
                      imageURL = sku.getProduct().getPartPrimaryImageURL();
                  }
                  options.setThumbLocation(imageURL);
                  options.setVersionId(FormatHelper.getVersionId(sku));
                  options.setActivity(Activities.UPDATE_SKU);
                  options.setReturnOid(FormatHelper.getObjectId(sku));
                  options.setHasModifyAccess(ACLHelper.hasModifyAccess(sku));

              }else{
                  options.setThumbLocation(thumbLocation);
                  options.setVersionId(FormatHelper.getVersionId(productRevA));
                  options.setActivity(Activities.UPDATE_PRODUCT);
                  options.setReturnOid(FormatHelper.getObjectId(productRevA));
                  options.setHasModifyAccess(ACLHelper.hasModifyAccess(productRevA));

              }
              // options common to both cases
              options.setImage("partPrimaryImageURL");
              options.setLabel("-");
              options.setReturnActivity("VIEW_PRODUCT");
              options.setReturnAction("INIT");
              options.setMaxWidth("252");
              options.setHighlightArea("fileDropZone");
              options.setHideChangeImageURL(true);
              options.setAutoLoad(true);
              options.setBorderColor("transparent");
              options.setBorderWidth("0");
              options.setObjectType("product");
              %>
              <%= plugin.getThumbnailPlugin(options,true) %>
            </div>
          </div>
      </div>
      <!-- Images END-->

      <!-- Colorways BEGIN-->
      <div id='colorwayPanel' class='card'>
          <input type="checkbox" id="colorwayPanelCard" class="cardCheckbox" checked disabled>
          <div id='colorwayPanelHeader' class='card-header'>
              <label class="card-label" for="colorwayPanelCard">
                  <%= colorways_LBL %>
              </label>
              <% if(ACLHelper.hasCreateAccess(product.getFlexType())){ %>
                  <div class="card-header-menu">
                      <div id='colorwayPanelNewColorways' class='addIconBtn'
                          onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(createSkusLabel) %>');"
                          onmouseout="return nd();"
                          onclick="javascript:launchModuleChooser('COLOR','','','',true,'addColors',false,'version', '<%= FormatHelper.getObjectId(product.getFlexType().getAttribute("color").getRefType()) %>');">
                      </div>
                  </div>
              <% } %>
          </div>
          <div class="card-content">
              <div class='colorwayContent'>
                  <% if (skus.size() > 12) {%>
                  <div class='colorwayContent-leftNav' onclick="plusSlides(-1)"></div>
                  <div class='colorwayContent-viewport maxSize'>
                  <div class='colorwayContent-rail'>
                  <table class='colorwayContent-slide maxSize'><tr>
                  <% } else { %>
                  <table class='colorwayContent-slide'><tr>
                  <% } %>
                          <%
                              Iterator colorwayIter = skus.iterator();
                              FlexObject colorway;
                              int count = 0;
                              String colorwayId;
                              String imagePath;
                              while(colorwayIter.hasNext()){
                                  colorway = (FlexObject) colorwayIter.next();
                                  count++;
                                  colorwayId = "VR:com.lcs.wc.product.LCSSKU:" + colorway.getData("LCSSKU.BRANCHIDITERATIONINFO");
                                  imagePath = FormatHelper.hasContent(colorway.getData("LCSSKU.PARTPRIMARYIMAGEURL"))?colorway.getData("LCSSKU.PARTPRIMARYIMAGEURL"):colorway.getData("LCSCOLOR.THUMBNAIL");
                                  String onMouseOver = "\"javascript:openColorwayQuickInfo(event, '" + colorwayId + "');\"";
                                  if(appContext.getSeason() != null) {
                                      onMouseOver = "\"javascript:openColorwayQuickInfo(event, '" + colorwayId + "', '" + appContext.getProductSeasonRevId() + "', 'PRODUCT');\"";
                                  }
                                  %>
                                  <td>
                                    <% if(colorwayId.equals(appContext.getContextSKUId())) {
                                        %>
                                        <div id='sku_<%= colorwayId %>' class='colorwayContent-chip colorwayContent-chip--highlight'
                                        onMouseOver=<%= onMouseOver %>
                                        style='<%= FormatHelper.hasContent(imagePath)?
                                            "background-image: url("+FormatHelper.formatImageUrl(imagePath)+");":
                                            "background: #"+colorway.getData("LCSCOLOR.COLORHEXIDECIMALVALUE")+";" %>'>
                                    </div>
                                    <%
                                    } else{ %>
                                    <div id='sku_<%= colorwayId %>' class='colorwayContent-chip'
                                        onMouseOver=<%= onMouseOver %>
                                        style='<%= FormatHelper.hasContent(imagePath)?
                                            "background-image: url("+FormatHelper.formatImageUrl(imagePath)+");":
                                            "background: #"+colorway.getData("LCSCOLOR.COLORHEXIDECIMALVALUE")+";" %>'>
                                    </div>
                                    <%} %>


                                  </td>
                                  <%
                                  if (count%12 == 0 && skus.size()-count > 0) {
                                      %>
                                      </tr></table>
                                      <table class='colorwayContent-slide maxSize'><tr>
                                      <%
                                  }
                                  else if(count%4 == 0){
                                      %>
                                      </tr><tr>
                                      <%
                                  }
                              }
                          %>
                      </tr>
                  </table>
                  <% if (skus.size() > 12) {%>
                  </div>
                  </div>
                  <div class='colorwayContent-rightNav' onclick="plusSlides(1)"></div>
                  <% } %>
              </div>
          </div>
      </div>
      <!-- Colorways END-->

      <!-- Key Information BEGIN-->
      <div id='keyInfoPanel' class='card'>
          <div class='keyInfoContent'>
              <div class='keyInfoRow'>
                  <div class='keyInfoValue'><%= FormatHelper.encodeAndFormatForHTMLContent(productRevA.getFlexType().getFullNameDisplay()) %></div>
                  <% if (FormatHelper.hasContent(currencyAtt) && productSeasonLink != null) {
                      FlexType ft = productSeasonLink.getFlexType();
                      FlexTypeAttribute fta = ft.attributeExist(currencyAtt)?ft.getAttribute(currencyAtt):null;
                      if (fta != null && ACLHelper.hasViewAccess(fta)) {
                  %>
                  <div class='keyInfoSpacer'></div>
                  <div class='keyInfoInline'>
                      <div class='keyInfoLabel'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getAttDisplay(true)) %></div>
                      <div class='keyInfoValue'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getDisplayValue(productSeasonLink)) %></div>
                  </div>
                  <%  }
                  } %>
              </div>
              <% if (FormatHelper.hasContent(headerAtt) && productRevA != null) {
                  FlexType ft = productRevA.getFlexType();
                  FlexTypeAttribute fta = ft.attributeExist(headerAtt)?ft.getAttribute(headerAtt):null;
                  if (fta != null && ACLHelper.hasViewAccess(fta)) {
              %>
              <div class='keyInfoRow'>
                  <div class='keyInfoHeading'>
                  <% if(FormatHelper.hasContentAllowZero(fta.getDisplayValue(productRevA))){ %>
                      <%= FormatHelper.encodeAndFormatForHTMLContent(fta.getDisplayValue(productRevA)) %>
                  <%} %>
                  </div>
              </div>
              <%  }
              } %>
              <% if(FormatHelper.hasContentAllowZero(appContext.getSeasonId())){ %>
                  <div class='keyInfoRow'>
                      <div class='keyInfoValue'>
                      <a href="javascript:viewLinePlanBack('<%= appContext.getSeasonId() %>');">
                          <%= FormatHelper.encodeAndFormatForHTMLContent(appContext.getSeasonName()) %>
                      </a>
                      </div>
                  </div>
              <%} %>
              <div class='keyInfoRow'>
                  <% if (FormatHelper.hasContent(subheaderAtt)) {

                      if (productSeasonLink != null) {
                          FlexType ft = productSeasonLink.getFlexType();
                          FlexTypeAttribute fta = ft.attributeExist(subheaderAtt)?ft.getAttribute(subheaderAtt):null;
                          if (fta != null && ACLHelper.hasViewAccess(fta)) {
                  %>
                  <div class='keyInfoSubheading'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getDisplayValue(productSeasonLink)) %></div>
                      <%  }
                      } else {%>
                  <div class='keyInfoSubheading'>&nbsp;</div>
                      <% } %>
                  <% } else { %>
                  <div class='keyInfoSubheading'><%=product.getLifeCycleState().getDisplay(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")))%></div>
                  <% } %>
              </div>
              <div class='keyInfoDivider'></div>
              <% if (panelAtts != null && panelAtts.size() > 0) { %>
              <div class='keyInfoRow'>
                  <%
                  FlexType ft = productRevA.getFlexType();
                  HTMLAttributeRendererProcessor ar = flexg2.getHTMLAttributeRendererProcessor(ft, "sidebar");
                  int sidePanelNo = 1, actualSize = 0;
                  while (actualSize < panelAtts.size()) {
                      FlexTyped ftObj = null;
                      String att = panelAtts.get("jsp.product.sidebar.classificationAtts." + sidePanelNo);
                      sidePanelNo++;
                      if (att == null)
                          continue;
                      FlexTypeAttribute fta = (FormatHelper.hasContent(att)&&ft.attributeExist(att))?ft.getAttribute(att):null;
                      if (fta != null) {
                          if (productSeasonLink != null && "PRODUCT-SEASON".equals(fta.getAttScope())) {
                              ftObj = productSeasonLink;
                          } else if (productRevA != null && "PRODUCT".equals(fta.getAttScope())) {
                              ftObj = productRevA;
                          }
                      }
                      if (ftObj != null && fta != null && ACLHelper.hasViewAccess(fta)) {
                  %>
                  <div class='keyInfoAttribute'>
                      <span class='keyInfoLabel'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getAttDisplay(true)) %></span>
                      <span class='keyInfoValue'><%= ar.drawDisplay(fta, ftObj, false, null, null) %></span>
                  </div>
                   <% }
                      actualSize++;
                  } %>
              </div>
              <% } %>
              <div class='keyInfoRowShaded'>
              <% if (null != productSeasonLink) { %>
                  <div class='keyInfoBox'>
                      <%= (null != productSeasonLink.getCarriedOverFrom()) ? carryoverLabel : newLabel %>
                  </div>
               <% } %>
                  <% if (FormatHelper.hasContent(emphasisAtt) && productRevA != null) {
                      FlexType ft = productRevA.getFlexType();
                      FlexTypeAttribute fta = ft.attributeExist(emphasisAtt)?ft.getAttribute(emphasisAtt):null;
                      if (fta != null && ACLHelper.hasViewAccess(fta)) {
                  %>
                  <div class='keyInfoInline'>
                      <div class='keyInfoSplitrow'>
                          <div class='keyInfoLabel'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getAttDisplay(true)) %></div>
                      </div>
                      <div class='keyInfoSplitrow'>
                          <div class='keyInfoValue'><%= FormatHelper.encodeAndFormatForHTMLContent(fta.getDisplayValue(productRevA)) %></div>
                      </div>
                  </div>
                  <%  }
                  } %>
              </div>
          </div>
      </div>
      <!-- Key Information END-->

      <!-- Tracked Changes BEGIN-->
      <div id='trackedChangesPanel' class='card'>
          <input type="checkbox" id="trackedChangesPanelCard" class="cardCheckbox" checked disabled>
          <div id='trackedChangesHeader' class='card-header'>
              <label class="card-label" for="trackedChangesPanelCard">
                  <%= FormatHelper.encodeAndFormatForHTMLContent(trackedChangesLabel) %>
              </label>
          </div>
          <div class="card-content">
              <div id='trackedChangeContentDetail'>
                  <%
                  count = 3;
                  for(ChangeAuditEvent evt:auditEvents) {
                      boolean isAccess = true;
                      if (evt.getEventType().contains("SKU")) {
                          try {
                              LCSPartMaster skuMaster = null;

                              if (evt.getEventType().contains("SEASON")) {
                                  skuMaster = (LCSPartMaster) evt.getTertiaryContext();
                              } else {
                                  skuMaster = (LCSPartMaster) evt.getChangedObject();
                              }
                              if (skuMaster != null) {
                                  LCSSKU skuObj = SeasonProductLocator.getSKUARev((LCSSKU) VersionHelper.latestIterationOf(skuMaster));
                              }
                          } catch (WTRuntimeException wte){
                              if (wte.getCause() instanceof NotAuthorizedException) {
                                  isAccess = false;
                              }
                          }
                      }

                      if(isAccess && ChangeAuditLogic.hasViewPermission(evt)){
                  %>
				  
				  <%                    
try {                 
%>
    <div class="display-only-label">
        <%= FormatHelper.encodeAndFormatForHTMLContent(
                ChangeEventMessageHelper.getDisplayMessage(evt, false)) %>
    </div>
<%
    if (--count == 0) {   
        break;
    }
} catch (Exception e) {   
    e.printStackTrace();
}
%>   
                  <div style='display: block; height: 8px;'></div>
                  <%
    } 
} 
%>
              </div>
          </div>
      </div>
      <!-- Tracked Changes END-->
  </div>
</div>
<!-- -- PAGE_LOAD_SUCESS -- -->
