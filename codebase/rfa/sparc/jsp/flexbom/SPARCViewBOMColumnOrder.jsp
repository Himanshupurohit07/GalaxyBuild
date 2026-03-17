<%-- Copyright (c) 2002 PTC Inc.     Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import=" com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.material.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.material.*,
                com.lcs.wc.report.*,
                com.lcs.wc.season.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.util.*,
                wt.util.*,
				org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,

                java.text.*,
                com.infoengine.object.factory.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="columnList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<% wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.flexbom.ViewBOMColumnOrder");
	public static final String JSPNAME = "ViewBOMColumnOrder";
    public static final boolean DEBUG = false;
    public static final boolean USE_AUTOMATIC_BOM_HIGHLIGHT = LCSProperties.getBoolean("jsp.flexbom.automaticBOMHighlight"); 
    public static final boolean USE_AUTOMATIC_BOL_HIGHLIGHT = LCSProperties.getBoolean("jsp.flexbom.automaticBOLHighlight");
    public static final boolean WCPART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    //--------------------------------------------
    public void buildColumnList(Collection columnList, Collection viewAtts, String[] arIgnoreAtts,boolean billOfLabor,Collection skuList,String skuMode, boolean advView) {
        //anything that applied for material bom only or for labor bom only must be checked against "billOfLabor"
		if(viewAtts == null) viewAtts = new ArrayList();

        //iterate thru the columns of view
        for (Iterator itViews = viewAtts.iterator();itViews.hasNext();) {

            String attKey = (String)itViews.next();
            //check for ignored columns
            boolean bIgnore = false;
            for (int iIgnore = 0;iIgnore < arIgnoreAtts.length;iIgnore ++){
                if (arIgnoreAtts[iIgnore].equals(attKey)) {
                    bIgnore = true;
                    break;
                }
            }
            if (bIgnore)    continue;

            if("materialDescription".equals(attKey) || "BOM.materialDescription".equals(attKey)){
                if(!columnList.contains("MATERIAL.name")) columnList.add("MATERIAL.name");
            }
            else if("supplierName".equals(attKey)){
                columnList.add("SUPPLIER.name");
            }
            else if("ColorDescription".equals(attKey) && !billOfLabor){ // Labor BOM doesn't need this
                if(LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)){
                    FlexObject skuObj;

                    for (Iterator skuIter = skuList.iterator();skuIter.hasNext();) {
                        skuObj = (FlexObject) skuIter.next();
                        if(FormatHelper.hasContent(skuObj.getString("LCSSKU.IDA3MASTERREFERENCE"))){
                            columnList.add("BOM.color$sku$"+ skuObj.getString("LCSSKU.IDA3MASTERREFERENCE"));
                        }
                        else if(FormatHelper.hasContent(skuObj.getString("LCSMATERIALCOLOR.IDA2A2"))){
                            columnList.add("BOM.color$sku$"+ skuObj.getString("LCSMATERIALCOLOR.IDA2A2"));
                        }
                        
                    }

                } else if (!advView) {
                    columnList.add("BOM.colorDescription");
                }
            }
            else if("Colorways".equals(attKey)){
                columnList.add("BOM.colorDescription");
            }
            else if(!columnList.contains(attKey)){
                if(attKey.indexOf(".") > -1){
                    attKey = attKey.substring(0, attKey.indexOf(".")).toUpperCase() + attKey.substring(attKey.indexOf("."));
                }
                columnList.add(attKey);
            }
        } //iterate thru the columns of view

        logger.debug("ColumnList: " + columnList);
    }
    //--------------------------------------------
%>
<%
try{
    Set viewColumns = new TreeSet();

    boolean useBOMCosting = FormatHelper.parseBoolean(request.getParameter("useBOMCosting"));
    String viewBOMMode = FormatHelper.format(request.getParameter("viewBOMMode"));
    String skuMode = FormatHelper.format(request.getParameter("skuMode"));
    Collection skuList = (Collection) request.getAttribute("contextSKUList");
    FlexBOMPart bomPart = (FlexBOMPart) request.getAttribute("contextBOMPart");

    boolean productBOM = false;
    boolean materialBOM = false;
    BOMOwner ownerMaster = bomPart.getOwnerMaster();
    if(ownerMaster instanceof LCSPartMaster){
        productBOM = true;
    }
    else{
        materialBOM = true;
    }

    boolean billOfLabor = "LABOR".equals(bomPart.getBomType());

    String bomId = FormatHelper.getVersionId(bomPart);
    
    request.setAttribute("BOMoid",bomId);
    FlexBOMPart bom = (FlexBOMPart)LCSQuery.findObjectById(bomId);
    FlexType bomType = bom.getFlexType();
    boolean showThumbs = FormatHelper.parseBoolean(request.getParameter("showThumbs"));

    logger.debug("viewBOMMode: " + viewBOMMode);
    if(!"VIEW".equals(viewBOMMode) && !"ADV_VIEW".equals(viewBOMMode)){
        //String activity = request.getParameter("activity");
        String activity = "EDIT_BOM";
        //lcsContext.viewCache.clearCache(FormatHelper.getObjectId(bomType), activity);
        Collection reportColumns = LCSQuery.getObjectsFromResults(lcsContext.viewCache.getViews(FormatHelper.getObjectId(bomType), activity),
            "OR:com.lcs.wc.report.ColumnList:", "COLUMNLIST.IDA2A2");

        for(Iterator i = reportColumns.iterator();i.hasNext();){
            ColumnList list = (ColumnList)i.next();
            if(list.getAttributes() != null){
                for (Iterator subList = list.getAttributes().iterator();subList.hasNext();){
                    String id = (String)subList.next();
                    if(id.indexOf(".") > -1){
                        id = id.substring(0, id.indexOf(".")).toUpperCase() + id.substring(id.indexOf("."));
                    }

                    while(id.indexOf(" ") > -1){
                        id = id.substring(0, id.indexOf(" ")) + id.substring(id.indexOf(" ") + 1);
                    }

                    viewColumns.add(id);
                }
            }
        }

       for (Iterator cleanup = viewColumns.iterator();cleanup.hasNext();){
            String tv = (String)cleanup.next();
            if("Colorways".equals(tv) ||
                "sourceDim".equals(tv) ||
                "destinationDim".equals(tv) ||
                "colorDim".equals(tv) ||
                "size1Dim".equals(tv) ||
                "size2Dim".equals(tv) ||
                "SIZE1".equals(tv) ||
                "SIZE2".equals(tv) ||
                "supplierName".equals(tv) ||
                "materialDescription".equals(tv)){

                cleanup.remove();
            }
        }
        logger.debug("Total View Columns: " + viewColumns);
    }

    String stViewId = request.getParameter("viewId");
    if(request.getAttribute("viewIdOverride") != null){
        stViewId = (String) request.getAttribute("viewIdOverride");
    }

    if( billOfLabor){  // THIS IS THE COLUMN LAYOUT FOR A BILL OF LABOR
        columnList.add("BOM.materialReference");
        columnList.add("BOM.partName");
        //columnList.add("BOM.materialDescription");
        if (USE_AUTOMATIC_BOL_HIGHLIGHT) {
            columnList.add("BOM.markUp");
        }
        
        ColumnList view = lcsContext.getViewCache().getColumnList(stViewId, bomType, "EDIT_BOM");
        if("VIEW".equals(viewBOMMode)){// view mode

            //these columns must be added in both case: there is a selected view or no view
            if (showThumbs) {
                columnList.add("MATERIAL.thumbnail");
            }
            columnList.add("BOM.rowNumber");

            if(view != null){
                //CHUCK - Not sure if Size gets displayed at all or what the column would be in View mode
                String [] arIgnoreAtts = {"Colorways","sourceDim","destinationDim","colorDim","size1Dim","SIZE1","SIZE2"};
                buildColumnList(columnList,  view.getAttributes(), arIgnoreAtts,billOfLabor,null,null,false) ; //skuList and skuMode are of no use here
            }
            else { // there is no view selected, display the default
                
                if (WCPART_ENABLED){
	            //columnList.add("BOM.wcPartNumber");
	            columnList.add("BOM.wcPartName");
                }  
                
                //This array contains all the columns for default view of Labor Bom
                String [] arColumns = {"MATERIAL.name","BOM.process","SUPPLIER.name","BOM.quantity","BOM.lossAdjustment"
                                      ,"MATERIALSUPPLIER.materialPrice","BOM.priceOverride","BOM.rowTotal","BOM.sams"
                                      };
                //add all the default columns
                for (int i=0;i<arColumns.length;i++){
                    columnList.add(arColumns[i]);
                }		        

            }

        }
        else { //edit mode
            columnList.add("BOM.materialDescription");
            // columns that we want to allow users to edit must be added here
            if(view != null){
            	
                columnList.add("sourceDim");
                columnList.add("colorDim");
                columnList.add("destinationDim");
                columnList.add("size1Dim");
                columnList.add("size2Dim");
                columnList.add("SUPPLIER.name");
                
                //CHUCK - Not sure if Size gets displayed at all or what the column would be in View mode
                //String [] arIgnoreAtts = {"Colorways","sourceDim","destinationDim","colorDim","size1Dim","SIZE1","SIZE2"};
                String [] arIgnoreAtts = {"Colorways"};
                buildColumnList(columnList,  view.getAttributes(), arIgnoreAtts,billOfLabor,null,null,false) ; //skuList and skuMode are of no use here
            }
            else { // there is no view selected, display the default
            	            	
            	 if (WCPART_ENABLED){
                     //columnList.add("BOM.wcPartNumber");
                     columnList.add("BOM.wcPartName");
            	 }          
                 columnList.add("sourceDim");
                 columnList.add("colorDim");
                 columnList.add("destinationDim");
                 columnList.add("size1Dim");
                 columnList.add("size2Dim");                 
            	
                //This array contains all the columns for default view of Labor Bom
                //Remove Material.name from below array to fix SPR#1541243, Tyrone 2009-12-02
                String [] arColumns = {"BOM.process","SUPPLIER.name","BOM.quantity","BOM.lossAdjustment"
                                      ,"MATERIALSUPPLIER.materialPrice","BOM.priceOverride","BOM.rowTotal","BOM.sams"
                                      };
                //add all the default columns
                for (int i=0;i<arColumns.length;i++){
                    columnList.add(arColumns[i]);
                }
            }
	    for(Iterator it = viewColumns.iterator();it.hasNext();){
		String c = (String)it.next();
		if(!columnList.contains(c)){
		    columnList.add(c);
		}
	    }
            
        }
    }//if( billOfLabor)
    else {//not bill of labor
        logger.debug("Not bill of labor");
        if(!"VIEW".equals(viewBOMMode)  && !"ADV_VIEW".equals(viewBOMMode)){//edit mode

            ColumnList view = lcsContext.getViewCache().getColumnList(stViewId, bomType, "EDIT_BOM");
            if(view != null) {
                if (showThumbs) {
                    columnList.add("MATERIAL.thumbnail");
                }
                columnList.add("BOM.rowNumber");
		//Patch 13 Start
              //  columnList.add("BOM.partName");
	      	columnList.add("BOM.scComponent");
		//Patch 13 End
                columnList.add("BOM.materialDescription");
				columnList.add("SUPPLIER.name");
                //columnList.add("BOM.supplierDescription");

                if (WCPART_ENABLED){
                    //columnList.add("BOM.wcPartNumber");
                    columnList.add("BOM.wcPartName");
                }
                if (USE_AUTOMATIC_BOM_HIGHLIGHT) {
                    columnList.add("BOM.markUp");
                }

                //Not sure if Size gets displayed at all or what the column would be in Edit mode
                //if we need to ignore more columns, add them here
                String [] arIgnoreAtts = {};
                //buildColumnList(columnList,  view.getAttributes(), arIgnoreAtts,billOfLabor,skuList,skuMode) ;
				buildColumnList(columnList,  viewColumns, arIgnoreAtts,billOfLabor,skuList,skuMode,false) ;
            } else {//default view
                columnList.add("BOM.rowNumber");
		//Patch 13 Start
               // columnList.add("BOM.partName");
	       	columnList.add("BOM.scComponent");
		//Patch 13 End
                columnList.add("BOM.materialDescription");

                if (WCPART_ENABLED){
                    //columnList.add("BOM.wcPartNumber");
                    columnList.add("BOM.wcPartName");
                }

                if (USE_AUTOMATIC_BOM_HIGHLIGHT) {
                    columnList.add("BOM.markUp");
                }
                columnList.add("sourceDim");
                columnList.add("colorDim");
                if(!materialBOM){
                    columnList.add("destinationDim");
                    columnList.add("size1Dim");
                    columnList.add("size2Dim");
                }
                columnList.add("SUPPLIER.name");
                columnList.add("BOM.colorDescription");
                columnList.add("MATERIAL.unitOfMeasure");
                columnList.add("BOM.quantity");

                for(Iterator it = viewColumns.iterator();it.hasNext();){
                    String c = (String)it.next();
                    if(!columnList.contains(c)){
                        columnList.add(c);
                    }
                }
            }
        }
        else{// view mode
            ColumnList view = lcsContext.getViewCache().getColumnList(stViewId, bomType, "EDIT_BOM");

            // THIS IS THE COLUMN LAYOUT FOR A STANDARD BILL OF MATERIALS
            if(view != null){
                if (showThumbs) {
                    columnList.add("MATERIAL.thumbnail");
                }
                columnList.add("BOM.rowNumber");
		//Patch 13 Start
               // columnList.add("BOM.partName");
                	columnList.add("BOM.scComponent");
	        //Patch 13 End					
                if (USE_AUTOMATIC_BOM_HIGHLIGHT) {
                    columnList.add("BOM.markUp");
                }

                //CHUCK - Not sure if Size gets displayed at all or what the column would be in View mode
                //if we need to ignore more columns, add them here
                String [] arIgnoreAtts = {"Colorways","sourceDim","destinationDim","colorDim","size1Dim","SIZE1","SIZE2"};
                buildColumnList(columnList,  view.getAttributes(), arIgnoreAtts,billOfLabor,skuList,skuMode,"ADV_VIEW".equals(viewBOMMode)) ;
            }
            else{//default view
                if (showThumbs) {
                    columnList.add("MATERIAL.thumbnail");
                }
                columnList.add("BOM.rowNumber");
		//Patch 13 Start
                //columnList.add("BOM.partName");
				columnList.add("BOM.scComponent");
	        //Patch 13 End			
              
                if (WCPART_ENABLED){
	            //columnList.add("BOM.wcPartNumber");
	      	    columnList.add("BOM.wcPartName");
                }
                
                if (USE_AUTOMATIC_BOM_HIGHLIGHT) {
                    columnList.add("BOM.markUp");
                }
                if(LCSFlexBOMQuery.ALL_SKUS.equals(skuMode)){

                    columnList.add("MATERIAL.name");
                    columnList.add("SUPPLIER.name");

                    FlexObject skuObj;
                    for (Iterator skuIter = skuList.iterator();skuIter.hasNext();){
                        skuObj = (FlexObject) skuIter.next();
                        if(FormatHelper.hasContent(skuObj.getString("LCSSKU.IDA3MASTERREFERENCE"))){
                            columnList.add("BOM.color$sku$"+ skuObj.getString("LCSSKU.IDA3MASTERREFERENCE"));
                        }
                        else if(FormatHelper.hasContent(skuObj.getString("LCSMATERIALCOLOR.IDA2A2"))){
                            columnList.add("BOM.color$sku$"+ skuObj.getString("LCSMATERIALCOLOR.IDA2A2"));
                        }
                        
                    }

                } else{
                    columnList.add("MATERIAL.name");
                    columnList.add("SUPPLIER.name");
                    columnList.add("BOM.color");
                    columnList.add("MATERIAL.unitOfMeasure");
                    columnList.add("BOM.quantity");
                }

                if(useBOMCosting){

                    //columnList.add("BOM.lossAdjustment");
                    //columnList.add("MATERIAL.materialPrice");
                    //columnList.add("BOM.priceOverride");
                    //columnList.add("BOM.rowTotal");

                }				
            }
        }
    }
}catch(Throwable t){
    t.printStackTrace();
}
%>
