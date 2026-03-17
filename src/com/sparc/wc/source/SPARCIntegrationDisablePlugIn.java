package com.sparc.wc.source;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SPARCLockingAttUtil;
import com.sparc.wc.util.SparcConstants;
import wt.fc.PersistenceHelper;

import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author dsagar56
 *
 */
public class SPARCIntegrationDisablePlugIn {

	/**Initializing log */
	private static Logger log = LogManager.getLogger(SPARCIntegrationDisablePlugIn.class);

	/**Initializing COST_SENT_TO_S4 */
	private static String COST_SENT_TO_S4 = LCSProperties.get("com.lcs.wc.product.CostSheet.CostSentToS4.Flag");

	/**Initializing SOURCING_SENT_TO_S4 */
	private static String SOURCING_SENT_TO_S4 = LCSProperties.get("com.lcs.wc.source.SourcingConfig.SourcingSentToS4.Flag");

	/**Initializing COSTSHEET_INTEGRATION_KEYS */
	private static String COSTSHEET_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.source.CostSheet.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing SOURCING_INTEGRATION_KEYS */
	private static String SOURCING_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.source.SourcingConfig.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing SOURCETOSEASON_INTEGRATION_KEYS */
	private static String SOURCETOSEASON_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.source.SourceToSeasonLink.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing COST_SENT_TO_S4_SC_YES */
	private static String COST_SENT_TO_S4_SC_YES =LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Keys.Yes");

	/**Initializing COST_SENT_TO_S4_SC_NO */
	private static String COST_SENT_TO_S4_SC_NO =  LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Keys.No");

	/**Initializing SENT_TO_S4 */
	private static String SENT_TO_S4 = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Flag");

	/**
	 * Getting true if given attribute value is changed on Cost Sheet
	 * 
	 * @param wtObject
	 * @throws WTException
	 */
	public static void disableCostSheetFlag(WTObject wtObject) throws WTException {
		log.debug("### START : SPARCIntegrationDisablePlugIn.disableCostSheetFlag ###");
		if(!(wtObject instanceof LCSProductCostSheet)) {
			return;
		}
		LCSProductCostSheet costSheet =(LCSProductCostSheet) wtObject; 

		System.out.println("costSheet-----------------------------"+PersistenceHelper.isPersistent(costSheet));
		//To handle the red line error on create of Product
		if(!PersistenceHelper.isPersistent(costSheet)){
			System.out.println("costsheet is not persistent");
			return;			
		}
		String sentToS4 = costSheet.getValue(COST_SENT_TO_S4) != null ? (String)costSheet.getValue(COST_SENT_TO_S4) : "";
		
		if(log.isDebugEnabled() && "scNo".equals(sentToS4)) {
			log.debug("\t Cost Sent To S4 \t"+sentToS4);
		}
		boolean isSentToS4 = false;
		//START-FP-435 , commenting because 'Cost Sent to S4' is NOT setting to Yes after publishing data to PIR, atleast one of colorway is not published
		//if(sentToS4.trim().equals(COST_SENT_TO_S4_SC_YES.trim())) {

		isSentToS4 =  SPARCLockingAttUtil.isAttsUpdated(costSheet,COSTSHEET_INTEGRATION_KEYS);

		//}
		//END-FP-435 , commenting because 'Cost Sent to S4' is NOT setting to Yes after publishing data to PIR, atleast one of colorway is not published
		if(log.isDebugEnabled() && "scNo".equals(sentToS4)) {
			log.debug("\t Is Cost Sent To S4 \t"+isSentToS4);
		}
		if(isSentToS4) {
			persistAllSKUSourceLinks(costSheet);
			costSheet.setValue(COST_SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
		}



		log.debug("### END : SPARCIntegrationDisablePlugIn.disableCostSheetFlag ###");
	}



	public static void populateIncoterms(WTObject wtObject) throws WTException, WTPropertyVetoException {
		if(!(wtObject instanceof LCSCostSheet)) {

			return;
		}
		LCSCostSheet costSheet =(LCSCostSheet) wtObject;

		LCSPartMaster partMaster = (LCSPartMaster) costSheet.getProductMaster();

		if(partMaster == null) {

			partMaster = (LCSPartMaster) ((LCSSKU) VersionHelper.getVersion((LCSPartMaster)costSheet.getSkuMaster(), "A")).getProduct().getMaster();
		}


		if(costSheet instanceof LCSProductCostSheet) {
			try {
				LCSProductCostSheet productCostSheet = (LCSProductCostSheet) costSheet;
				boolean isIncotrmsLocExist = false;
				LCSLifecycleManaged costSheetIncotrms =  (LCSLifecycleManaged) productCostSheet.getValue("scIncotermlocation");
				if(costSheetIncotrms == null) {
					isIncotrmsLocExist = true;
				}
				LCSSourcingConfig config = (LCSSourcingConfig) VersionHelper.latestIterationOf(costSheet.getSourcingConfigMaster());
				if(config == null) {

					return;
				}
				LCSSupplier supplier = (LCSSupplier) config.getValue("scFGFactory");
				if(supplier == null) {
					if(log.isDebugEnabled()) {
						log.debug("\t SupplierNotFound"+supplier);
					}
					return;
				}
				String getSupplierFlexType = supplier.getFlexType().getFullNameDisplay().toString();
				if(getSupplierFlexType.contains("Finished Goods Factory")) {
					String incoterms = (String) supplier.getValue("scIncoterms");
					LCSLifecycleManaged incotermsLocation = (LCSLifecycleManaged) supplier.getValue("scIncotermlocation");

					if(log.isDebugEnabled() && null == incotermsLocation ) {
						log.debug("\t IncotermLocationNotFound"+incotermsLocation);
					}
					if( null != incoterms && null != incotermsLocation && isIncotrmsLocExist) {


						costSheet.setValue("scIncoterms",incoterms);
						costSheet.setValue("scIncotermlocation",incotermsLocation);

					}
				}


			}catch(Exception ex) {
				if(log.isDebugEnabled()) {
					log.debug("\t cost sheet" +costSheet );
				}
			}


		}

	}

	/**
	 * Resetting All Cost Sheet "Cost Sent To S4" to No from Sourcing Config
	 * 
	 * @param wtObject
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void resetSourcingConfigFlag(WTObject wtObject) throws WTException, WTPropertyVetoException {

		if(!(wtObject instanceof LCSSourcingConfig)) {
			return;
		}
		LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) wtObject;

		@SuppressWarnings("unchecked")
		Collection<LCSSourceToSeasonLink> stsl = new LCSSourcingConfigQuery().getSourceToSeasonLinks(sourcingConfig);
		if(stsl == null) {
			if(log.isDebugEnabled()) {
				log.debug("\t STSLNotFound \t" + stsl);
			}
			return;
		}
		boolean isSentToS4 = false;
		LCSSeason season = null ;
		Iterator<LCSSourceToSeasonLink> iterator = stsl.iterator();
		while(iterator.hasNext()) {
			LCSSourceToSeasonLink sourceToSeasonLink = iterator.next();
			season = (LCSSeason) VersionHelper.latestIterationOf(sourceToSeasonLink.getSeasonMaster());
			if(season!= null) {
				break;
			}

		}
		try {
			//START : Fixed - When no season is selected
			if(season != null) {
				//END : Fixed - When no season is selected
				LCSSourceToSeasonLink sourceToSeasonLink =  new LCSSourcingConfigQuery().getSourceToSeasonLink(sourcingConfig.getMaster(), season.getMaster());

				Collection<?> costSheetColl = LCSCostSheetQuery.getAllCostSheetsForSourcingConfig(sourcingConfig, false);
				if(costSheetColl == null) {
					if(log.isDebugEnabled()) {
						log.debug("\t CostShetNotFound \t" + costSheetColl);
					}
					return;
				}
				Iterator<?> itr = costSheetColl.iterator();
				while(itr.hasNext()) {

					LCSCostSheet costSheet = (LCSCostSheet) itr.next();
					String sourcingConfigSentToS4 = (String) sourceToSeasonLink.getValue(SOURCING_SENT_TO_S4);
					if(FormatHelper.hasContent(sourcingConfigSentToS4) && sourcingConfigSentToS4.equals(COST_SENT_TO_S4_SC_YES)) {
						isSentToS4 = SPARCLockingAttUtil.isAttsUpdated(sourcingConfig,SOURCING_INTEGRATION_KEYS);
					}
					if(costSheet instanceof LCSProductCostSheet) {
						LCSProductCostSheet prodCostSheet = (LCSProductCostSheet) costSheet;
						if(isSentToS4) {
							persistAllSKUSourceLinks(prodCostSheet);
							prodCostSheet.setValue(COST_SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
							LCSCostSheetLogic logic = new LCSCostSheetLogic();
							logic.saveCostSheet(costSheet,false,true);
						}
					}
				}
			}
		}catch(Exception ex) {
			if(log.isDebugEnabled()) {
				log.debug("\t seasonNotFound : " + season);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void persistAllSKUSourceLinks(LCSProductCostSheet prodCostSheet) {
		try {

			//START- HIT 324 Fix
			Collection skuSourcingLinksCol = new LCSSourcingConfigQuery().getSkuSourcingLinks(prodCostSheet.getSourcingConfigMaster(), null, prodCostSheet.getSeasonMaster(), true);
			//END- HIT 324 Fix
			
			String applicableColorNames = prodCostSheet.getApplicableColorNames();
			
			log.debug("applicableColorNames : "+applicableColorNames);
			StringTokenizer st;
			
			Iterator skuLinksCollITR = skuSourcingLinksCol.iterator();
			log.debug("skuLinksCollITR > "+skuLinksCollITR);
			while(skuLinksCollITR.hasNext()) {
				LCSSKUSourcingLink eachSKUSourcingLink = (LCSSKUSourcingLink)skuLinksCollITR.next();
				if(eachSKUSourcingLink.isActive()){
					if(FormatHelper.hasContent(applicableColorNames)) {
						 st = new StringTokenizer(applicableColorNames,"|~*~|");  
						 while (st.hasMoreTokens()) {  
							 //System.out.println("Token> "+st.nextToken());  
							 if(st.nextToken().equals(eachSKUSourcingLink.getSkuMaster().getName())) {
								log.debug(" inside condition ");
								eachSKUSourcingLink.setValue(SparcConstants.INTEGRATION_FLAG , SparcConstants.BOOLEAN_NO);
								LCSLogic.persist(eachSKUSourcingLink);
							}
						 }  							
					}else {
						eachSKUSourcingLink.setValue(SparcConstants.INTEGRATION_FLAG , SparcConstants.BOOLEAN_NO);
						LCSLogic.persist(eachSKUSourcingLink);
					}
				}
			}
		}catch(WTException e) {
			System.out.println("Exception in persistAllSKUSourceLinks Method"+e);
		}

	}



	/**
	 * @param wtObject
	 * @throws WTException
	 */
	public static void resetSourcingToSeasonFlag(WTObject wtObject) throws WTException {
		log.debug("*** resetSourcingToSeasonFlag ***");
		if(!(wtObject instanceof LCSSourceToSeasonLink)) {
			return;
		}

		LCSSourceToSeasonLink sourceToSeasonLink = (LCSSourceToSeasonLink) wtObject;

		if(!sourceToSeasonLink.isLatestIteration()) {return;}
		sourceToSeasonLink = (LCSSourceToSeasonLink) VersionHelper.latestIterationOf(sourceToSeasonLink);
		LCSSourceToSeasonLink prevsourceToSeasonLink = null;

		if(sourceToSeasonLink != null) {
			prevsourceToSeasonLink = (LCSSourceToSeasonLink) VersionHelper.predecessorOf(sourceToSeasonLink);
		}

		if(prevsourceToSeasonLink == null) {
			return;
		}
		String sourcingConfigSentToS4 = (String) sourceToSeasonLink.getValue(SOURCING_SENT_TO_S4);
		LCSSourcingConfig sourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourceToSeasonLink.getSourcingConfigMaster());
		boolean isSourcingSentToS4 = false;
		log.debug("sourcingConfigSentToS4: "+sourcingConfigSentToS4);
		if(FormatHelper.hasContent(sourcingConfigSentToS4) && sourcingConfigSentToS4.equals(COST_SENT_TO_S4_SC_YES)) {
			String disableAttKeys =  SOURCETOSEASON_INTEGRATION_KEYS;
			log.debug("disableAttKeys: "+disableAttKeys);
			StringTokenizer tokenizer = new StringTokenizer(disableAttKeys,",");
			while(tokenizer.hasMoreTokens()) {
				String integrationKeys = tokenizer.nextToken();
				log.debug("integrationKeys: "+integrationKeys);
				if((prevsourceToSeasonLink.getValue(integrationKeys) == null && sourceToSeasonLink.getValue(integrationKeys) == null )){
					isSourcingSentToS4 = false;
					log.debug("isSourcingSentToS4 1: "+isSourcingSentToS4);
				}
				if((prevsourceToSeasonLink.getValue(integrationKeys)!= null && sourceToSeasonLink.getValue(integrationKeys)!= null )){

					//When prev and currnet not  null
					if(prevsourceToSeasonLink.getValue(integrationKeys).equals("") && sourceToSeasonLink.getValue(integrationKeys).equals("")) {
						isSourcingSentToS4 = false;
						log.debug("isSourcingSentToS4 2: "+isSourcingSentToS4);
					}
					if(!(sourceToSeasonLink.getValue(integrationKeys).equals(prevsourceToSeasonLink.getValue(integrationKeys)))) {
						isSourcingSentToS4 = true;
						log.debug("isSourcingSentToS4 3: "+isSourcingSentToS4);
						break;
					}

				}else  if((sourceToSeasonLink.getValue(integrationKeys)!= null )
						&&!sourceToSeasonLink.getValue(integrationKeys).equals("")
						&&!sourceToSeasonLink.getValue(integrationKeys).equals(" ")) {
					isSourcingSentToS4 = true;
					log.debug("isSourcingSentToS4 4: "+isSourcingSentToS4);
					break;

				}
				if( prevsourceToSeasonLink.getValue(integrationKeys) != null) {
					if((sourceToSeasonLink.getValue(integrationKeys)!= null)) {
						if(sourceToSeasonLink.getValue(integrationKeys).equals("")
								||sourceToSeasonLink.getValue(integrationKeys).equals(" ")){
							if(!(sourceToSeasonLink.getValue(integrationKeys).equals(prevsourceToSeasonLink.getValue(integrationKeys)))) {
								isSourcingSentToS4 = true;
								log.debug("isSourcingSentToS4 5: "+isSourcingSentToS4);
								break;
							}
						}
					}else if(null == sourceToSeasonLink.getValue(integrationKeys)) {
						isSourcingSentToS4 = true;
						log.debug("isSourcingSentToS4 6: "+isSourcingSentToS4);
						break;
					}

				}
			}
			log.debug("isSourcingSentToS4::: "+isSourcingSentToS4);
			if(isSourcingSentToS4) {
				//START- HIT 324 Fix
				Collection emptyColl = new ArrayList<>();
				LCSProduct product = SeasonProductLocator.getProductARev(sourceToSeasonLink.getMaster());
				LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(sourceToSeasonLink.getSeasonMaster());
				Collection<?> costSheetColl = LCSCostSheetQuery.getCostSheetsForProduct(new HashMap<>(),product,sourcingConfig,season,emptyColl,false,false);
				//END- HIT 324 Fix
				if(costSheetColl == null) {
					if(log.isDebugEnabled()) {
						log.debug("\t CostShetNotFound \t" + costSheetColl);
					}
					return;
				}
				Iterator<?> itr = costSheetColl.iterator();
				while(itr.hasNext()) {
					//START- HIT 324 Fix
					FlexObject fobj = (FlexObject) itr.next();
					LCSProductCostSheet prodCostSheet = (LCSProductCostSheet) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSProductCostSheet:"+fobj.getString("LCSPRODUCTCOSTSHEET.BRANCHIDITERATIONINFO"));
					
					log.debug("prodCostSheet : "+prodCostSheet);
					//END- HIT 324 Fix
					if(prodCostSheet != null) {
						
						prodCostSheet.setValue(COST_SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
						LCSCostSheetLogic logic = new LCSCostSheetLogic();
						logic.saveCostSheet(prodCostSheet,false,false);
						persistAllSKUSourceLinks(prodCostSheet);
					}
				}
			}
		}
	}

	/**
	 * Reset Sent to S4 based on colorways associated with Cost Sheet.If it is blank then flip all Colorways
	 * 
	 * @param wtObj
	 * @throws WTException
	 */
	@SuppressWarnings("unchecked")
	public static void resetSentToS4FromCostSheet(WTObject wtObj) throws WTException {

		if(!(wtObj instanceof LCSMOAObject)) {
			return;
		}
		LCSMOAObject moaObj = (LCSMOAObject) wtObj;
		if(!moaObj.getFlexType().getFullNameDisplay().toString().trim().equals("HTS Classification")) {
			return;
		}
		LCSMOAObject prevMoaObj = null ;
		LCSSKUSeasonLink skuSeasonLink = null;
		LCSProductCostSheet productCostSheet = null;
		LCSMOAObject currentMoaObj = null ;
		LCSLifecycleManaged prevLifecycleManaged = null ;
		LCSLifecycleManaged currentLifecycleManaged = null ;
		LCSSeason season = null;

		if(!moaObj.isEffectLatest()) {
			if(moaObj != null) {
				prevMoaObj = (LCSMOAObject) PersistenceHelper.manager.refresh(moaObj);
				prevLifecycleManaged = (LCSLifecycleManaged) prevMoaObj.getValue("scHTScode");
			}
		}
		if(moaObj.isEffectLatest()) {
			currentMoaObj = moaObj;
			currentLifecycleManaged = (LCSLifecycleManaged) currentMoaObj.getValue("scHTScode");
		}

		if(!(FormatHelper.areWTObjectsEqual(currentLifecycleManaged, prevLifecycleManaged)) ){
			if(moaObj.getOwner() instanceof LCSCostSheetMaster) {
				productCostSheet = (LCSProductCostSheet) VersionHelper.latestIterationOf(moaObj.getOwner());
				if(null == productCostSheet.getSourcingConfigMaster()){
					return;
				}
				LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf(productCostSheet.getSourcingConfigMaster());
				//START : FP-317
				if(productCostSheet.isPrimaryCostSheet() && sourcingConfig.isPrimarySource()) {
					//END : FP-317

					LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(productCostSheet.getProductMaster());
					if(null != productCostSheet.getSeasonMaster()){
						season = (LCSSeason) VersionHelper.latestIterationOf(productCostSheet.getSeasonMaster());
						Collection<LCSSKU> skusCollection = LCSSKUQuery.findSKUs(product);

						if(skusCollection.size() <= 0 ) {
							return;
						}
						for(LCSSKU sku : skusCollection) {

							skuSeasonLink = (LCSSKUSeasonLink) LCSSeasonQuery
									.findSeasonProductLink(sku, season);
							if(skuSeasonLink != null) {
								skuSeasonLink.setValue(SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
								LCSLogic.persist(skuSeasonLink,true);
							}
						}
						//START : FP-317	
					}
					//END : FP-317
				}
			}
		}
	}
}