package com.sparc.wc.costsheet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.web.TableData;
import com.lcs.wc.client.web.TableDataUtil;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;

import wt.fc.ObjectIdentifier;
import wt.fc.PersistInfo;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * 
 * This class has the logic to perform the BOM Roll-up into cost sheet
 */
public final class SPARCBOMRollupPlugin {

	private final static Logger LOGGER = LogManager.getLogger(SPARCBOMRollupPlugin.class);
	private static final String MATERIAL_PRICE_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.MaterialPriceKey", "materialPrice");
	private static final String MATERIAL_PRICE_OVERRIDE_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.PriceOverrideKey", "priceOverride");
	private static final String CIF_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.CIFKey", "scCIF");
	private static final String GROSS_QUANTITY_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.GrossQuantityKey", "scGrossQuantity");
	private static final String RETRIEVE_FROM_BOM_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.RetrievefromBOMKey", "scRetrievefromBOM");
	private static final String BOM_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.BOMKey", "scBOM");
	private static final String BOM_ITERATION_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.BOMIterationKey", "scBOMIteration");
	private static final String BOM_RETRIEVE_DATE_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.BOMRetrieveKey", "scBOMRetrieveDate");
	private static final String BOM_SECTION_KEY = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.BOMSectionKey", "section");
	private static final String SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP", "SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP");
	private static final String SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP", "SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP");
	private static final String SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP = LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP", "SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP");
	
	
	@SuppressWarnings("unchecked")
	private static  Collection<String> SECTION_KEY_TO_COSTSHEET_ATTR_MAPPING = FormatHelper.commaSeparatedListToCollection(LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.Apparel.sectionKeyToBOMAttrMapping", "mainmaterials~scBOMMainMaterials,sctrims~scBOMTrims,scartwork~scBOMArtwork,scprocess~scBOMProcess,labels~scBOMLabels,packaging~scBOMPackaging,sclining~scBOMLining"));
	private static  Collection<String> FW_SECTION_KEY_TO_COSTSHEET_ATTR_MAPPING = FormatHelper.commaSeparatedListToCollection(LCSProperties.get("com.sparc.wc.flexbom.SPARCBOMRollupPlugin.Footwear.sectionKeyToBOMAttrMapping", "scFwUpper~scBOMUpper,scSockliner~scBOMSockliner,scInsole~scBOMInsole,scBottom~scBOMBottom,scsundries~scBOMSundries,scPackaging~scBOMPackaging"));

	/**
	 * Main method configured in Plugin to carryout the BOM Rollup
	 * @param wtObject
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	@SuppressWarnings("unchecked")
	public static void performBOMRollup(WTObject wtObject) throws WTException, WTPropertyVetoException {
		
		if (wtObject == null || !(wtObject instanceof LCSProductCostSheet)) {
			LOGGER.error("***BOMRollup:performBOMRollup-Exit as different object received:"+wtObject);
			return;
		}
		
		LCSProductCostSheet costSheet = (LCSProductCostSheet)wtObject;
		LOGGER.debug("**BOMRollup:costSheet:"+costSheet);
		String flexTypeName = costSheet.getFlexType().getFullName();
		LOGGER.debug("**BOMRollup:flexTypeName:"+flexTypeName);
		if(!(flexTypeName.indexOf("Breakdown")>0)) {
			LOGGER.debug("**BOMRollup:Exiting from performBOMRollup as the costsheet type is not BreakDown:");
			return;
		}
		
		boolean retrieveFromBOM = false;
		if(costSheet.getValue(RETRIEVE_FROM_BOM_KEY)==null) {
			LOGGER.debug("**BOMRollup:Exiting from performBOMRollup as the Retrive from BOM is blank:");
			return;
		}
		retrieveFromBOM = ((Boolean)costSheet.getValue(RETRIEVE_FROM_BOM_KEY)).booleanValue();
		String bomSelected = costSheet.getValue(BOM_KEY)!=null? String.valueOf(costSheet.getValue(BOM_KEY)) : "";
		LOGGER.debug("**BOMRollup:retrieveFromBOM:"+retrieveFromBOM);
		LOGGER.debug("**BOMRollup:bomSelected:"+bomSelected);
		if(!(retrieveFromBOM && FormatHelper.hasContent(bomSelected))) {
			LOGGER.debug("**BOMRollup:Exiting from performBOMRollup as BOM is not selected or Checkbox is not ticked:");
			return;
		}
		
		FlexBOMPart bomObject= null;
		bomObject = getBOMByName(costSheet, bomSelected);
		
		if(bomObject==null) {
			throw new LCSException("Rollup is not complete, Please contact Administrator !!!");
		}
		
		LOGGER.debug("BOMRollup4:"+bomObject.getIterationDisplayIdentifier().toString());
		Collection<FlexObject> representativeColorCol = null;
		LCSSKU sku = null;
		representativeColorCol = getRepresentativeColor(costSheet);
		
		if(representativeColorCol != null) {
			sku = getSKUFromRepresentativeColor(representativeColorCol);
		}
		
		LOGGER.debug("**BOMRollup:MethodContext Variables:"+MethodContext.getContext(Thread.currentThread()));
		
		if((MethodContext.getContext().get(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP)==null 
				|| !FormatHelper.hasContent(String.valueOf(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP)))){
				 
			if(MethodContext.getContext().get(SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP)==null 
					|| !FormatHelper.hasContent(String.valueOf(SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP))){
				sku = overrideSKUFromMethodContext(costSheet, sku);
			}
			else {
				LOGGER.debug("**BOMRollup:Its a Refresh Scenario so continuing with already associated Colorways:");
				MethodContext.getContext().remove(SPARC_REFRESH_MULTI_COSTSHEET_FOR_BOM_ROLLUP);
			}
		}
		else {
			
			LOGGER.debug("**BOMRollup:Either No Colorway Presen ot Previously adde Colourway removed");
			MethodContext.getContext().remove(SPARC_CUSTOMREP_COLORWAY_REMOVED_NOTPRESENT_FOR_BOM_ROLLUP);
			sku = null;
		}
		
			LOGGER.debug("**BOMRollup: Rep. Colourway considered as:"+sku);
		FlexType bomType = bomObject.getFlexType();
		LOGGER.debug("**BOMRollup:bomType:"+bomType);
		if(!"LABOR".equals(bomObject.getBomType())) {
			
			if(VersionHelper.isCheckedOutByUser(bomObject)){
				LOGGER.debug("**BOMRollup:bomObject is checkedout by user:"+bomObject);
				FlexBOMPart bomObjectOld  = (FlexBOMPart) VersionHelper.latestIterationOf(bomObject);
				if(!VersionHelper.isWorkingCopy(bomObjectOld))
					bomObject = bomObjectOld  ;          
			}
			LOGGER.debug("**BOMRollup:version considered for rollup :"+bomObject);
			String scMasterId ="";
			String skuMasterId = "";
			String size1 = FormatHelper.format(costSheet.getRepresentativeSize());
			String size2 = FormatHelper.format(costSheet.getRepresentativeSize2());
			Date timestamp = null;
			FlexTypeAttribute sectionAtt = bomType.getAttribute(BOM_SECTION_KEY);
			FlexType materialType = FlexTypeCache.getFlexTypeFromPath("Material");
			if(costSheet.getSourcingConfigMaster()!=null) {
				scMasterId = FormatHelper.getObjectId(costSheet.getSourcingConfigMaster());
				}
			if(sku!=null) {
				skuMasterId = FormatHelper.getObjectId((WTPartMaster)sku.getMaster());
			}
			Collection<FlexObject> data = LCSFlexBOMQuery.findFlexBOMData(bomObject, scMasterId, skuMasterId, size1, size2, null, LCSFlexBOMQuery.EFFECTIVE_ONLY, timestamp, false, false, LCSFlexBOMQuery.ALL_APPLICABLE_TO_DIMENSION, "", "", "", materialType).getResults();
			LOGGER.debug("**BOMRollup:data1:"+data);
			Collection bomData = new Vector();
			bomData.addAll(LCSFlexBOMQuery.mergeDimensionBOM(data));
			bomData = groupDataToBranchId(bomData,bomType);
			LOGGER.debug("**BOMRollup:bomData2:"+bomData);
			String sectionIndex = sectionAtt.getSearchResultIndex();
			Map<String, List<TableData>> dataMap = TableDataUtil.groupIntoCollections(bomData, sectionIndex);
			LOGGER.debug("**BOMRollup:dataMap:"+dataMap);
			AttributeValueList sectionAttList = bomType.getAttribute(BOM_SECTION_KEY).getAttValueList();
			LOGGER.debug("**BOMRollup:sectionAttList:"+sectionAttList);
			Iterator<String> sectionIter = sectionAttList.getKeys().iterator();
			HashMap<String, FlexObject> sectionMap  =new HashMap<String, FlexObject>();
			String section;
			FlexObject sectionObj;
			
			BigDecimal sectionTotal = new BigDecimal("0.0");
			while(sectionIter.hasNext()) {
				section = (String) sectionIter.next();
				LOGGER.debug("**BOMRollup:processing :"+section);
				if(sectionMap.get(section) == null) {
					sectionObj = new FlexObject();
					LOGGER.debug("**BOMRollup:new Object is created :");
				} else {
					sectionObj = (FlexObject) sectionMap.get(section);
					LOGGER.debug("**BOMRollup:existing Object is retrived :"+sectionObj);
				}
			  
				if(sectionAtt.getAttValueList().isSelectable(section)) {
					sectionObj.put("display",sectionAtt.getDisplayValue(section));
					
					if(sectionObj.get("totalCost") != null) {
						sectionTotal = new BigDecimal((String)sectionObj.get("totalCost"));
					}

					List<TableData> sectionCol = (List<TableData>) dataMap.get(section);
					if(sectionCol != null) {
						sectionObj.put("totalCost",sectionTotal.add(calculateBOMRollUpTotal(bomObject, sectionCol)));
					}
					if(sectionObj.get("sortingNumber") == null) {
						sectionObj.put("sortingNumber",bomType.getAttribute(BOM_SECTION_KEY).getAttValueList().get(section,"ORDER"));
					}
					LOGGER.debug("**BOMRollup:putting :"+sectionObj+":::Into:::"+section);
					sectionMap.put(section,sectionObj);
				}

			}
			LOGGER.debug("**BOMRollup:sectionMap After rowTotal:"+sectionMap);
			
			Collection<String> sectionAttrCol = null;
			if(flexTypeName.indexOf("scApparelBreakdown") > -1) {
				sectionAttrCol = SECTION_KEY_TO_COSTSHEET_ATTR_MAPPING;
			}
			else if(flexTypeName.indexOf("scFootwearBreakdown") > -1) {
				sectionAttrCol = FW_SECTION_KEY_TO_COSTSHEET_ATTR_MAPPING;
			}
			String[] tempStr ;
			HashMap<String, String> sectionBOMAtrMap = new HashMap<String, String>();
			for(String str: sectionAttrCol) {
				tempStr = str.split("~");
				sectionBOMAtrMap.put(tempStr[0], tempStr[1]);
			}
			LOGGER.debug("**BOMRollup:sectionBOMAtrMap :"+sectionBOMAtrMap);
			BigDecimal bomTotal = new BigDecimal("0.0");
			String csSectionAttrKey = "";
			  if(sectionMap != null) {  				
					Iterator<String> sectionIter1 = sectionMap.keySet().iterator();
					while(sectionIter1.hasNext()) {
						section = (String) sectionIter1.next();
						csSectionAttrKey = sectionBOMAtrMap.get(section);
						sectionObj = (FlexObject) sectionMap.get(section);
						if(sectionObj != null) {
							try {
								if(sectionObj.get("totalCost")!=null) {
								bomTotal =new BigDecimal((String)sectionObj.get("totalCost"));
								costSheet.setValue(csSectionAttrKey,""+bomTotal);
								LOGGER.debug("**BOMRollup:Looking to set:"+bomTotal + ":::: for:::"+csSectionAttrKey);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					costSheet.setValue(BOM_ITERATION_KEY,bomObject.getIterationDisplayIdentifier().toString());
					costSheet.setValue(BOM_RETRIEVE_DATE_KEY,new Date());
					costSheet.setValue(RETRIEVE_FROM_BOM_KEY,false);
					LOGGER.debug("**BOMRollup:Completed setting all three attributes");
             } 
		}
		else {
			LOGGER.debug("**BOMRollup:Exited - Completed as BOL - setting checkbox");
			costSheet.setValue(RETRIEVE_FROM_BOM_KEY,false);
			}
	}
	/**
	 * This method groups the rows into its respective sections
	 * @param data
	 * @param type
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Collection groupDataToBranchId(Collection data, FlexType type) throws WTException{
		if(data == null || data.size() < 1){
			return new Vector();
		}
		Map table = TableDataUtil.groupIntoCollections(data, "FLEXBOMLINK.MASTERBRANCHID");
		Vector groupedVector = new Vector();
		List current = (List) table.get("0");
		if(current == null){
			return new Vector();
		}
		current = new Vector(sortTopLevel(current, type));
		table.remove("0");
		
		Set keys = table.keySet();

		for (int i=0;i<current.size();i++){
			TableData  td = (TableData)current.get(i);
			groupedVector.add(td);
			String value = td.getData("FLEXBOMLINK.BRANCHID");
			if(keys.contains(value)) {
				Collection colSubBranches = (Collection)table.get(value);
				colSubBranches = SortHelper.sortFlexObjectsByNumber(colSubBranches,"FLEXBOMLINK.SORTINGNUMBER");

				groupedVector.addAll( colSubBranches );
			}
		}
		return groupedVector;
	}
	/**
	 * This method will sort the groups 
	 * @param data
	 * @param type
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static final Collection sortTopLevel(Collection data, FlexType type) throws WTException{
		if(data == null || data.size() < 1){
			return new Vector();
		}
		Vector groupedVector = new Vector();
		Map table = TableDataUtil.groupIntoCollections(data, type.getAttribute(BOM_SECTION_KEY).getSearchResultIndex());
		Collection sections = type.getAttribute(BOM_SECTION_KEY).getAttValueList().getSelectableKeys(null, true);
		String key = "";
		Iterator i = sections.iterator();
		Collection subSet = null;
		while(i.hasNext()){
			key = (String)i.next();
			subSet = (Collection)table.get(key);
			if(subSet != null){
				subSet = SortHelper.sortFlexObjectsByNumber(subSet,"FLEXBOMLINK.SORTINGNUMBER");
				groupedVector.addAll(subSet);
			}
		}
		return groupedVector;
	}
	/**
	 * This will return the Colorway Object based on the Colurway String passed 
	 * @param representativeColorCol
	 * @return
	 * @throws WTException
	 */
	public static LCSSKU getSKUFromRepresentativeColor(Collection<FlexObject> representativeColorCol) throws WTException {
		LCSSKU sku = null;
		String repColorId ="";
		Iterator<FlexObject> repColorColIter = representativeColorCol.iterator();
	    FlexObject fo = null;
	    while(repColorColIter.hasNext()) {
	    	fo = (FlexObject)repColorColIter.next();
	    	repColorId = SparcConstants.SKU_VR + fo.getString("LCSSKU.BRANCHIDITERATIONINFO");
	        sku = (LCSSKU) LCSQuery.findObjectById(repColorId);
	    }
	    LOGGER.debug("BOMRollup:returning getSKUFromRepresentativeColor:"+sku);
		return sku;
		
	}
	
	/**
	 * This method is to get the Rep. Colourway which is part of the Form Parameter
	 * @param costSheet
	 * @param sku
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static LCSSKU overrideSKUFromMethodContext(LCSProductCostSheet costSheet, LCSSKU sku) throws WTException {
		
		LCSSKU representativeColorway = null;
		String representativeColorwayStr="";
		
		LOGGER.debug("**BOMRollup:MethodContext.getContext():"+MethodContext.getContext(Thread.currentThread()));
		if(MethodContext.getContext().get(SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP)!=null && FormatHelper.hasContent(String.valueOf(SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP))){
				representativeColorwayStr = (String) MethodContext.getContext().get("SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP");
				representativeColorway = (LCSSKU) LCSQuery.findObjectById(representativeColorwayStr);
				LOGGER.debug("**BOMRollup:representativeColorway:"+representativeColorway+":::"+representativeColorway.getName());
				MethodContext.getContext().remove(SPARC_CUSTOMREP_COLORWAY_FOR_BOM_ROLLUP);
				return representativeColorway;
		}
		
		LOGGER.debug("BOMRollup:Returning Rep Colorway as it is a special scenario:"+sku);
		return sku;
	}
	/**
	 * This method is to get the Representative Color String value from Cost sheet
	 * @param costSheet
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static Collection<FlexObject> getRepresentativeColor(LCSProductCostSheet costSheet) throws WTException {
		
		Collection<FlexObject> representativeColorCol = null;
		PersistInfo  costSheetMasterPersisitInfo= costSheet.getMaster().getPersistInfo();
		LOGGER.debug("BOMRollup:costSheetMasterPersisitInfo:"+costSheetMasterPersisitInfo);
		ObjectIdentifier costSheetMasterObjectIdentifier = null;
		long costSheetMasterID = 0;
		if(costSheetMasterPersisitInfo!=null) {
		costSheetMasterObjectIdentifier = costSheet.getMaster().getPersistInfo().getObjectIdentifier();
		LOGGER.debug("BOMRollup:costSheetMasterObjectIdentifier:"+costSheetMasterObjectIdentifier);
			}
		if(costSheetMasterObjectIdentifier!=null) {
			costSheetMasterID = costSheetMasterObjectIdentifier.getId();
			LOGGER.debug("BOMRollup:costSheetMasterID:"+costSheetMasterID);
			}
		if(costSheetMasterID>0) {
			representativeColorCol = LCSCostSheetQuery.getRepresentativeColor((LCSCostSheetMaster)costSheet.getMaster());
			}
		
		LOGGER.debug("BOMRollup:returning representativeColorCol:"+representativeColorCol);
		return representativeColorCol;
	}
	
	/**
	 * This method will return the BOM Object from the BOM Name selected in the Cost Sheet
	 * @param costSheet
	 * @param bomSelected
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static FlexBOMPart getBOMByName(LCSProductCostSheet costSheet, String bomSelected) throws WTException {
		
		FlexSpecMaster specMaster = null;
		specMaster = costSheet.getSpecificationMaster();
		FlexSpecification spec = null;
		LCSSeasonMaster seasonMaster = null;
		FlexBOMPart bomPart = null;
		Collection<FlexBOMPart> boms = null;
		String tempBOMName ="";
		if(specMaster!=null){
			spec = VersionHelper.latestIterationOf(specMaster);
		}
		LOGGER.debug("**BOMRollup:spec:"+spec.getName()+":::"+spec);
		String sourcingConfigRevID ="";
		if(costSheet.getSourcingConfigRevId()>0) {
			sourcingConfigRevID = String.valueOf(new BigDecimal(costSheet.getSourcingConfigRevId()).longValue());
		}
		LOGGER.debug("**BOMRollup:sourcingConfigRevID:"+sourcingConfigRevID);
		LOGGER.debug("****BOMRollup:selectedBOM:"+bomSelected);
		seasonMaster = costSheet.getSeasonMaster();
		if(sourcingConfigRevID!=null && FormatHelper.hasContent(sourcingConfigRevID) && seasonMaster!=null && spec!=null ) {
				boms = FlexSpecQuery.getSpecComponents(spec,"BOM");
				LOGGER.debug("**BOMRollup:boms:"+boms);
				for (Object bom: boms)		{
					bomPart = (FlexBOMPart)bom;
					if (!"LABOR".equals(bomPart.getBomType())) {
						tempBOMName = bomPart.getName();
						LOGGER.debug("**BOMRollup:tempBOMName:"+tempBOMName);
						if(tempBOMName.equalsIgnoreCase(bomSelected)){
							LOGGER.debug("**BOMRollup:returninggg:"+bomPart);
							return bomPart; 
						}
					}
				}
			}
		LOGGER.debug("**BOMRollup:returninggg null:");
		return null;
	}
	
	/**
	 * This method retrieves & sms up the row total from each BOMLink row for a given section and returns the section total
	 * @param bomPart
	 * @param bomData
	 * @return
	 * @throws WTException
	 */
	public static BigDecimal calculateBOMRollUpTotal(FlexBOMPart bomPart, List<TableData> bomData)
			throws WTException
			{
		Iterator<TableData> bomIter = bomData.iterator();
		TableData branch;
		BigDecimal materialPrice;
		BigDecimal priceOverride;
		BigDecimal grossQuantity;
		BigDecimal cif;
		BigDecimal bigDecimalzero = new BigDecimal("0.0");
		FlexType bomType = bomPart.getFlexType();
		FlexType matType = bomType.getReferencedFlexType(ReferencedTypeKeys.MATERIAL_TYPE);
		BigDecimal rowTotal = new BigDecimal("0.0");
		BigDecimal sectionTotal = new BigDecimal("0.0");
		bomIter = bomData.iterator();
		
		while(bomIter.hasNext()){
			branch = (TableData) bomIter.next();
			LOGGER.debug("**BOMRollup:branch:"+branch);
			if (FormatHelper.hasContent(branch.getData(matType.getAttribute(MATERIAL_PRICE_KEY).getSearchResultIndex()))) {
				materialPrice = new BigDecimal(branch.getData((matType.getAttribute(MATERIAL_PRICE_KEY).getSearchResultIndex())));
              }
			else {
				materialPrice = new BigDecimal("0.0");
			}
			LOGGER.debug("**BOMRollup:materialPrice:"+materialPrice);
			if (FormatHelper.hasContent(branch.getData(bomType.getAttribute(MATERIAL_PRICE_OVERRIDE_KEY).getSearchResultIndex()))) {
				priceOverride = new BigDecimal(branch.getData((bomType.getAttribute(MATERIAL_PRICE_OVERRIDE_KEY).getSearchResultIndex())));
              }
			else {
				priceOverride = new BigDecimal("0.0");
			}
			LOGGER.debug("**BOMRollup:priceOverride:"+priceOverride);
			if (FormatHelper.hasContent(branch.getData(bomType.getAttribute(GROSS_QUANTITY_KEY).getSearchResultIndex()))) {
				grossQuantity = new BigDecimal(branch.getData((bomType.getAttribute(GROSS_QUANTITY_KEY).getSearchResultIndex())));
              }
			else {
				grossQuantity = new BigDecimal("0.0");
			}
			LOGGER.debug("**BOMRollup:grossQuantity:"+grossQuantity);
			if (FormatHelper.hasContent(branch.getData(bomType.getAttribute(CIF_KEY).getSearchResultIndex()))) {
				cif = new BigDecimal(branch.getData((bomType.getAttribute(CIF_KEY).getSearchResultIndex())));
              }
			else {
				cif = new BigDecimal("0.0");
			}
			LOGGER.debug("**BOMRollup:cif:"+cif);
			if(priceOverride.compareTo(bigDecimalzero) == 1){
				materialPrice = priceOverride;
	        }
			LOGGER.debug("**BOMRollup:NEWW materialPrice:"+materialPrice);
			rowTotal = materialPrice.add(cif).multiply(grossQuantity);
			LOGGER.debug("**BOMRollup:rowTotal:"+rowTotal);
			sectionTotal = sectionTotal.add(rowTotal);
			LOGGER.debug("**BOMRollup:sectionTotal so far:"+sectionTotal);
			}
			return sectionTotal;
		}

}
