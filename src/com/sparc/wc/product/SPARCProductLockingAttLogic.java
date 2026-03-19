package com.sparc.wc.product;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonHelper;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.whereused.FAWhereUsedQuery;
import com.sparc.wc.util.SPARCLockingAttUtil;

import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author dsagar56
 *
 */
public class SPARCProductLockingAttLogic {
	
	/**Initializing log */
	private static Logger log = LogManager.getLogger(SPARCProductLockingAttLogic.class);

	/**Initializing COLORWAY_LIFECYCLE */
	private static String COLORWAY_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.Status");
	
	/**Initializing PRODUCT_LIFECYCLE */
	private static String PRODUCT_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSProduct.ProductLifecycle.Status");
	
	/**Initializing APP_RBK_FLEXTYPE */
	private static String APP_RBK_FLEXTYPE = LCSProperties.get("com.lcs.wc.product.LCSProduct.FlexType.Reebok");
	
	/**Initializing FTW_RBK_FLEXTYPE */
	private static String FTW_RBK_FLEXTYPE = LCSProperties.get("com.lcs.wc.product.LCSProduct.FlexType.Footwear");

	/**Initializing GENERIC_MSG */
	private static String GENERIC_MSG = LCSProperties.get("com.lcs.wc.genrericMessage");
	
	/**Initializing COLORWAY_OBJ_REF */
	private static String COLORWAY_OBJ_REF = LCSProperties.get("com.lcs.wc.product.LCSSKU.ObjectRef.color");
	
	/**Initializing COLORWAY_NAME */
	private static String COLORWAY_NAME = LCSProperties.get("com.lcs.wc.product.LCSSKU.COlorway.Name");

	/**Initializing SKU_SEASON_EXCEPTION_GENERIC_MSG */
	private static String SKU_SEASON_EXCEPTION_GENERIC_MSG =  LCSProperties.get("com.lcs.wc.product.ColorwaySeason.GenericErrorMessage");

	/**Initializing COLORWAY_COLORS_FLEXTYPE */
	private static String COLORWAY_COLORS_FLEXTYPE = LCSProperties.get("com.lcs.wc.color.LCSColor.FlexType.Colorway.Colors");
	
	/**Initializing BUY_READY */
	private static String BUY_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.BUYREADY.Status");
	
	/**Initializing SAMPLE_READY */
	private static String SAMPLE_READY = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.SAMPLEREADY.Status");
	
	/**Initializing DROPPED */
	private static String DROPPED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.DROPPED.Status");
	
	/**Initializing PLANNED */
	private static String PLANNED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.PLANNED.Status");
	
	/**Initializing RELEASED */
	private static String RELEASED = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.ColorwayLifecycle.RELEASED.Status");

	/**Initializing PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE */
	private static String PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.Planned.Status.Keys");
	
	/**Initializing PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE */
	private static String PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.Released.Status.Keys");
	
	/**Initializing PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE */
	private static String PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.SampleReady.Status.Keys");
	
	/**Initializing PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE */
	private static String PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProduct.LockedAttribute.BuyReady.Status.Keys");

	/**Initializing COLORWAY_RELEASED_LOCKED_ATTRIBUTE */
	private static String COLORWAY_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.Released.Status.Keys");
	
	/**Initializing COLORWAY_SAMPLE_READYLOCKED_ATTRIBUTE */
	private static String COLORWAY_SAMPLE_READYLOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.SampleReady.Status.Keys");
	
	/**Initializing COLORWAY_BUY_READY_LOCKED_ATTRIBUTE */
	private static String COLORWAY_BUY_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSSKU.LockedAttribute.BuyReady.Status.Keys");

	/**Initializing PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE */
	private static String PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.Released.StatusKeys");
	
	/**Initializing PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE */
	private static String PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.Released.StatusKeys");
	
	/**Initializing PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE */
	private static String PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.LockedAttribute.BuyReady.Status.Keys");

	/**Initializing COLOR_RELEASED_LOCKED_ATTRIBUTE */
	private static String COLOR_RELEASED_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.Released.Status.Keys");
	
	/**Initializing COLOR_BUYREADY_LOCKED_ATTRIBUTE */
	private static String COLOR_BUYREADY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.BuyReady.Status.Keys");
	
	/**Initializing COLOR_SAMPLEREADY_LOCKED_ATTRIBUTE */
	private static String COLOR_SAMPLEREADY_LOCKED_ATTRIBUTE = LCSProperties.get("com.lcs.wc.color.LCSColor.LockedAttribute.SampleReady.Status.Keys");

	/**Initializing SENT_TO_S4 */
	private static String SENT_TO_S4 = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Flag");
	
	/**Initializing COLORWAY_SENT_TO_S4_SC_YES */
	private static String COLORWAY_SENT_TO_S4_SC_YES = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Keys.Yes");
	
	/**Initializing COST_SENT_TO_S4_SC_NO */
	private static String COST_SENT_TO_S4_SC_NO = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.SentToS4.Keys.No");

	/**Initializing APPROVAL_ATT_ERROR_MSG */
	public static final String APPROVAL_ATT_ERROR_MSG = LCSProperties.get("com.lcs.wc.product.ColorwaySeason.ApprovalAtt.GenericErrorMessage");
	
	/**Initializing adminGroup */
	public static final String adminGroup = LCSProperties.get("com.lcs.wc.client.adminGroups.name");
	
	/**Initializing checkuserGroups */
	public static boolean checkuserGroups = true;


	/**
	 * Ã¢â‚¬Â¢ This method called when update of colorway season
	 * 
	 * @param wtObj
	 * @throws WTException
	 * @throws WTPropertyVetoException 
	 */
	public static void doNotAllowPreviousStatus(WTObject wtObj) throws WTException, WTPropertyVetoException {
		log.debug("### START SPARCProductLockingAttLogic.doNotAllowPreviousStatus ### ");

		if (!(wtObj instanceof LCSSKUSeasonLink)) {
			return;
		}
		LCSSKUSeasonLink colorwaySeasonLink = (LCSSKUSeasonLink) wtObj; 


		if(!(colorwaySeasonLink.isEffectLatest())){

			return;
		}
		if( !(colorwaySeasonLink.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE)) && 
				!(colorwaySeasonLink.getFlexType().getFullNameDisplay().toString().equals(FTW_RBK_FLEXTYPE) && 
						(colorwaySeasonLink.getFlexType().getFullNameDisplay().toString().contains("Reebok")		))	
				) {
			return;
		}


		//If user exist in groups then don't lock attribute
		try {
			if (FormatHelper.hasContent(adminGroup)) {
				StringTokenizer groupsTokenizer = new StringTokenizer(adminGroup, ",");
				while (groupsTokenizer.hasMoreTokens()) {
					String groupsName = (String) groupsTokenizer.nextToken();
					if(groupsName == null){return;}
					Collection<String> gropus = ClientContext.getContext().getGroups();
					if(gropus == null){return;}
					WTUser user = ClientContext.getContext().getUser();
					Iterator<String> groupsItr = gropus.iterator();
					while (groupsItr.hasNext()) {
						String grpName = groupsItr.next();
						if (grpName != null && grpName.equals(groupsName)) {
							checkuserGroups = false;
							break;
						}
					}

				}

			}
		}catch(Exception ex) {
			log.debug("\t Groups Not Found Error");
		}

		String colorwayLifeCycle = (String) colorwaySeasonLink.getValue(COLORWAY_LIFECYCLE);

		LCSSKUSeasonLink prevSeasonLink = null;
		String oldColorwayLifeCycle = null;
		if (colorwaySeasonLink != null  ) {
			prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(colorwaySeasonLink);

		}
		if(prevSeasonLink == null) {
			return;
		}
		String sentToS4 = (String) colorwaySeasonLink.getValue(SENT_TO_S4); 
		if(log.isDebugEnabled()) {
			log.debug("sentToS4 : " +sentToS4);
		}
		boolean isSentToS4 = false;
		if(null != sentToS4  && sentToS4.equals(COLORWAY_SENT_TO_S4_SC_YES)) {
			isSentToS4 = SPARCLockingAttUtil.resetSKUSeasonFlag(colorwaySeasonLink);
		}
		
		if (prevSeasonLink != null) {
			// get old version of colorway lifecycle value
			oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
		}
		if(log.isDebugEnabled()) {
			log.debug("oldColorwayLifeCycle : " +oldColorwayLifeCycle);
		}
		
		Boolean isLifeCycleCheck = true;
		if((colorwaySeasonLink.getCopiedFrom() != null || colorwaySeasonLink.getCarriedOverFrom() != null) || colorwaySeasonLink.getEffectSequence() < 2){
			isLifeCycleCheck = false;
		}


		if (oldColorwayLifeCycle != null && colorwayLifeCycle != null ) {
			if(checkuserGroups) {
				if (oldColorwayLifeCycle.equals(BUY_READY) && ( (colorwayLifeCycle.equals(PLANNED))
						|| (colorwayLifeCycle.equals(RELEASED)) || (colorwayLifeCycle.equals(SAMPLE_READY)))) {
					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, BUY_READY);
					if(log.isDebugEnabled()) {
						log.debug( SKU_SEASON_EXCEPTION_GENERIC_MSG + "\t oldColorwayLifeCycle \t" + oldColorwayLifeCycle
								+ "\t colorwayLifeCycle \t "+ colorwayLifeCycle);
					}
					throw new LCSException(SKU_SEASON_EXCEPTION_GENERIC_MSG);
				} else if (oldColorwayLifeCycle.equals(SAMPLE_READY) && ( (colorwayLifeCycle.equals(PLANNED))
						|| (colorwayLifeCycle.equals(RELEASED)) ) ) {

					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, SAMPLE_READY);
					if(log.isDebugEnabled()) {
						log.debug( SKU_SEASON_EXCEPTION_GENERIC_MSG + "\t oldColorwayLifeCycle \t" + oldColorwayLifeCycle
								+ "\t colorwayLifeCycle \t "+ colorwayLifeCycle);
					}
					throw new LCSException(SKU_SEASON_EXCEPTION_GENERIC_MSG);
				} else if (oldColorwayLifeCycle.equals(RELEASED) && colorwayLifeCycle.equals(PLANNED)) {
					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, RELEASED);
					if(log.isDebugEnabled()) {
						log.debug( SKU_SEASON_EXCEPTION_GENERIC_MSG + "\t oldColorwayLifeCycle \t" + oldColorwayLifeCycle
								+ "\t colorwayLifeCycle \t "+ colorwayLifeCycle);
					}
					throw new LCSException(SKU_SEASON_EXCEPTION_GENERIC_MSG);
				}else if (oldColorwayLifeCycle.equals(DROPPED) && ( (colorwayLifeCycle.equals(PLANNED))
						|| (colorwayLifeCycle.equals(RELEASED)) || (colorwayLifeCycle.equals(BUY_READY)) || (colorwayLifeCycle.equals(SAMPLE_READY)))) {
					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, BUY_READY);
					if(log.isDebugEnabled()) {
						log.debug( SKU_SEASON_EXCEPTION_GENERIC_MSG + "\t oldColorwayLifeCycle \t" + oldColorwayLifeCycle
								+ "\t colorwayLifeCycle \t "+ colorwayLifeCycle);
					}
					throw new LCSException(SKU_SEASON_EXCEPTION_GENERIC_MSG);
				}
			}
			LCSProductSeasonLink seasonProductLink = colorwaySeasonLink.getProductLink();
			log.debug("seasonProductLink >>> "+seasonProductLink);
			if (colorwayLifeCycle.equals(BUY_READY) && null != seasonProductLink) {
						
				String testingSigningOff = (String) colorwaySeasonLink.getValue("sctestsignoff");
				String developmentSigningOff = (String) colorwaySeasonLink.getValue("scdevsignoff");
				String customSigningOff = (String) colorwaySeasonLink.getValue("sccustomssignoff");
			
            // Code improvement
					if(testingSigningOff == null || developmentSigningOff == null || customSigningOff == null ||
							!(("scYes".equalsIgnoreCase(developmentSigningOff) || "scna".equalsIgnoreCase(developmentSigningOff))
									&& ("scYes".equalsIgnoreCase(testingSigningOff) || "scna".equalsIgnoreCase(testingSigningOff))
									&& ("scYes".equalsIgnoreCase(customSigningOff) || "scna".equalsIgnoreCase(customSigningOff))) )
					{
						
						colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, oldColorwayLifeCycle);
						throw new LCSException(APPROVAL_ATT_ERROR_MSG);
					}


             //Commenting for better fix
			/*	if(testingSigningOff == null || developmentSigningOff == null || customSigningOff == null ||
						"".equals(testingSigningOff) || "".equals(developmentSigningOff)  || "".equals(customSigningOff)) {
					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, oldColorwayLifeCycle);
					if(log.isDebugEnabled()) {
						log.debug(APPROVAL_ATT_ERROR_MSG + 
								"\t testingSigningOff \t " + testingSigningOff +
								"\t developmentSigningOff \t " + developmentSigningOff +
								"\t developmentSigningOff \t " + developmentSigningOff);

					}
					throw new LCSException(APPROVAL_ATT_ERROR_MSG);

				}else if ("scNo".equals(testingSigningOff) || "scNo".equals(developmentSigningOff)  || "scNo".equals(customSigningOff)) {
					colorwaySeasonLink.setValue(COLORWAY_LIFECYCLE, oldColorwayLifeCycle);
					if(log.isDebugEnabled()) {
						log.debug(APPROVAL_ATT_ERROR_MSG + 
								"\t testingSigningOff \t " + testingSigningOff +
								"\t developmentSigningOff \t " + developmentSigningOff +
								"\t developmentSigningOff \t " + developmentSigningOff);
					}
					throw new LCSException(APPROVAL_ATT_ERROR_MSG);
				} */



			}
			String colorwayLifeCycleVal = (String) colorwaySeasonLink.getValue(COLORWAY_LIFECYCLE);
			if ( FormatHelper.hasContent(colorwayLifeCycleVal) && seasonProductLink != null && colorwayLifeCycleVal != null) {
				try {
					HashMap<Integer, String> lifecycleMap = new HashMap<Integer,String>();
					LCSProduct product = SeasonProductLocator.getProductARev(seasonProductLink);
					LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonProductLink.getSeasonMaster());
					Collection<?> collection = LCSSKUQuery.findSKUs(product, false);
					Iterator<?> itr = collection.iterator();
					while (itr.hasNext()) {
						LCSSKU sku = (LCSSKU) itr.next();
						LCSSKUSeasonLink skuSeasonProductLink = (LCSSKUSeasonLink) LCSSeasonQuery
								.findSeasonProductLink(sku, season);
						//START : FP-346 - When the product from one season is added to another season, Product life cycle is not taking the higher value of colorway life cycle (for new season)
						if(null == skuSeasonProductLink) {
							if(log.isDebugEnabled()) {
								log.debug("\t ColorwayLinkNotFoundError- \t"+skuSeasonProductLink);
							}
							continue;
						}
						//END : FP-346 - When the product from one season is added to another season, Product life cycle is not taking the higher value of colorway life cycle (for new season)

						String skuLifeCycleVal = (String) skuSeasonProductLink.getValue(COLORWAY_LIFECYCLE);

						if(skuLifeCycleVal.equals(DROPPED)) {
							lifecycleMap.put(1, DROPPED);
						}else if(skuLifeCycleVal.equals(BUY_READY)) {
							lifecycleMap.put(2, BUY_READY);
						}else if(skuLifeCycleVal.equals(SAMPLE_READY)) {
							lifecycleMap.put(3, SAMPLE_READY);
						}else if(skuLifeCycleVal.equals(RELEASED)) {
							lifecycleMap.put(4, RELEASED);
						}else if(skuLifeCycleVal.equals(PLANNED)) {
							lifecycleMap.put(5, PLANNED);
						}
					}
					String tempLifecycle = null;


					for(Map.Entry<Integer,String> entry : lifecycleMap.entrySet()) {

						if(entry.getValue().equals(BUY_READY)) {
							tempLifecycle = BUY_READY;
							break;
						}
						else if(entry.getValue().equals(SAMPLE_READY)) {
							tempLifecycle = SAMPLE_READY;
							break;
						}else if(entry.getValue().equals(RELEASED)) {
							tempLifecycle = RELEASED;
							break;
						}
						else if(entry.getValue().equals(PLANNED)) {
							tempLifecycle = PLANNED;
							break;
						}
					}
					if(tempLifecycle != null && !(tempLifecycle.equals(DROPPED))) {
						seasonProductLink.setValue(PRODUCT_LIFECYCLE, tempLifecycle);
						seasonProductLink = (LCSProductSeasonLink) LCSSeasonHelper.service.saveSeasonProductLink(seasonProductLink);
					}
				}catch(Exception ex) {
					if(log.isDebugEnabled()) {
						log.debug("\t Something Went Wrong \t"+ex.getStackTrace());
					}
				}
			}


		}
		if(isSentToS4) {
			colorwaySeasonLink.setValue(SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
		}
		log.debug("Copied from-------------"+colorwaySeasonLink.getCopiedFrom());
		log.debug("Effect sequence-------------"+colorwaySeasonLink.getEffectSequence());
		
		if(colorwaySeasonLink.getCopiedFrom() != null && colorwaySeasonLink.getEffectSequence() < 2){
		  log.debug("do not persis the object");	
		}
		else{
		LCSLogic.persist(colorwaySeasonLink,true);
		}

		log.debug("### END SPARCProductLockingAttLogic.doNotAllowPreviousStatus ### ");
	}

	/**
	 * Ã¢â‚¬Â¢ From the Colorway : Get the Previous version Colorway and get the the
	 * locking attribute(ex:color) value and compare the previous and current
	 * values. Ã¢â‚¬Â¢ If there in difference in values,get all the colorway-season
	 * objects, Iterate all the Colorway-Season objects,if any oneÃ¢â‚¬â„¢s lifecycle
	 * status is Released,set the color attribute to previous value.
	 * 
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setPreviousColorwayAttVal(WTObject obj) throws WTException, WTPropertyVetoException {
		log.debug("### START SPARCProductLockingAttLogic.setPreviousColorwayAttVal ### ");

		if (!(obj instanceof LCSSKU)) {
			return;
		}

		LCSSKU lcssku = (LCSSKU) obj;

		//If user exist in groups then don't lock attribute

		if (!(lcssku.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE))  && 
				!(lcssku.getFlexType().getFullNameDisplay().toString().equals(FTW_RBK_FLEXTYPE)) && 
				!(lcssku.getFlexType().getFullNameDisplay().toString().contains("Reebok"))
				) {
			return;
		}

		LCSSKU prevLcsSku = null;


		if (lcssku != null) {
			prevLcsSku = (LCSSKU) VersionHelper.predecessorOf(lcssku);
		}
		if(log.isDebugEnabled()) {
			log.debug("prevLcsSku : " +prevLcsSku);
		}
		if(prevLcsSku == null) {
			return;
		}
		LCSSKUSeasonLink lcsSeasonProductLink = null;
		boolean isSentToS4 = false;
		String sentToS4 = null;
		if (prevLcsSku != null && lcssku != null ) {
			LCSSeasonMaster lcsSeasonMaster = null;
			@SuppressWarnings({ "unchecked" })
			Collection<LCSSeasonMaster> masters = new LCSSeasonQuery().findSeasons(lcssku.getMaster());
			if(masters == null) {return;}
			if(log.isDebugEnabled()) {
				log.debug("masters : " +masters);
			}
			if (masters != null) {
				Iterator<LCSSeasonMaster> masterIter = masters.iterator();
				while (masterIter.hasNext()) {
					lcsSeasonMaster = (LCSSeasonMaster) masterIter.next();
					LCSSeason lcsSeason = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasonMaster);
					if (lcsSeason != null) {
						lcsSeasonProductLink = (LCSSKUSeasonLink) LCSSeasonQuery
								.findSeasonProductLink(lcssku, lcsSeason);

						if (lcsSeasonProductLink != null) {
							sentToS4 = (String) lcsSeasonProductLink.getValue(SENT_TO_S4);

							if(sentToS4.equals(COLORWAY_SENT_TO_S4_SC_YES)) {
								isSentToS4 = SPARCLockingAttUtil.resetSKUSeasonFlag(lcssku);
							}
							if(!isSentToS4 && log.isDebugEnabled()) {
								log.debug("sentToS4 : " +sentToS4 + 
										"\t isSentToS4 \t" +isSentToS4 + 
										"\t lcsSeasonProductLink \t"+lcsSeasonProductLink);
							}
							if(isSentToS4 && lcsSeasonProductLink != null) {
								lcsSeasonProductLink.setValue(SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
								LCSLogic.persist(lcsSeasonProductLink,true);
								if(log.isDebugEnabled()) {
									log.debug("sentToS4 : " +sentToS4 + 
											"\t isSentToS4 \t" +isSentToS4 + 
											"\t lcsSeasonProductLink \t"+lcsSeasonProductLink);
								}
							}


							String colorwayLifeCycle = (String) lcsSeasonProductLink.getValue(COLORWAY_LIFECYCLE);
							LCSSKUSeasonLink prevSeasonLink = null;
							String oldColorwayLifeCycle = null;
							if (lcsSeasonProductLink != null  ) {
								prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(lcsSeasonProductLink);

							}
							if (prevSeasonLink != null) {
								// get old version of colorway lifecycle value
								oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);

							}

							if(colorwayLifeCycle == null) {return;}
							String lockedAtts = null;
							try {
								if (RELEASED.equals(colorwayLifeCycle) || (RELEASED.equals(oldColorwayLifeCycle) && DROPPED.equals(colorwayLifeCycle) ))  {

									lockedAtts = COLORWAY_RELEASED_LOCKED_ATTRIBUTE;
								}else if (SAMPLE_READY.equals(colorwayLifeCycle) || (SAMPLE_READY.equals(oldColorwayLifeCycle) && DROPPED.equals(colorwayLifeCycle) )) {
									lockedAtts = COLORWAY_SAMPLE_READYLOCKED_ATTRIBUTE;

								}
								if(BUY_READY.equals(colorwayLifeCycle) || (BUY_READY.equals(oldColorwayLifeCycle) && DROPPED.equals(colorwayLifeCycle) )) {
									lockedAtts = COLORWAY_BUY_READY_LOCKED_ATTRIBUTE;
								}
								/** NRF Color Code Integration issue fix ---START **/
								if(DROPPED.equals(colorwayLifeCycle)) {
									lockedAtts = COLORWAY_BUY_READY_LOCKED_ATTRIBUTE;
								}
								/** NRF Color Code Integration issue fix ---END **/
								
							}catch(Exception ex) {
								if(log.isDebugEnabled()) {
									log.debug("\t colorwayLifeCycle- \t"+ colorwayLifeCycle 
											+"\t oldColorwayLifeCycle- \t"+oldColorwayLifeCycle);
								}
							}
							if(lockedAtts == null) {return;}
							StringTokenizer  tokens = new StringTokenizer(lockedAtts,",");
							while (tokens.hasMoreTokens()) {
								String keys = tokens.nextToken();
								if(null != prevLcsSku.getValue(keys) && null != lcssku.getValue(keys) ) {
									if(!(prevLcsSku.getValue(keys).equals(lcssku.getValue(keys)))) {
										lcssku.setValue(COLORWAY_OBJ_REF, prevLcsSku.getValue(COLORWAY_OBJ_REF)); 
										lcssku.setValue(COLORWAY_NAME, prevLcsSku.getName().toString());
										if(log.isDebugEnabled()) {
											log.debug(GENERIC_MSG + 
													"\t lockedAtts \t " + lockedAtts +
													"\t colorwayLifeCycle \t " + colorwayLifeCycle +
													"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);


										}
										throw new LCSException(GENERIC_MSG);
									}
								}
							}
						}
					}
				}
			}
		}
		log.debug("### END SPARCProductLockingAttLogic.setPreviousColorwayAttVal ### ");
	}



	/**
	 * Locked Product Attrbute if Colorway Lifecyle is released or planned or sample ready
	 * 
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setPreviousProductAttVal(WTObject obj) throws WTException, WTPropertyVetoException {
		log.debug("### START SPARCProductLockingAttLogic.setPreviousProductAttVal ### ");

		if (!(obj instanceof LCSProduct)) {
			return;
		}
		LCSProduct product = (LCSProduct) obj;


		if (!(product.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE))&& 
				!(product.getFlexType().getFullNameDisplay().toString().equals(FTW_RBK_FLEXTYPE)) && 
				!(product.getFlexType().getFullNameDisplay().toString().contains("Reebok"))) {
			return;
		}
		@SuppressWarnings("unchecked")
		Collection<LCSSeasonMaster> masters = new LCSSeasonQuery().findSeasons(product);
		if(masters == null) {return;}
		if (masters != null ) {
			Iterator<LCSSeasonMaster> iterator = masters.iterator();
			LCSSeasonMaster lcsSeasonMaster = null;
			while (iterator.hasNext()) {
				List colorwayLifecycleList = new ArrayList();
				lcsSeasonMaster = (LCSSeasonMaster) iterator.next();
				LCSSeason lcsSeason = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasonMaster);
				log.debug("lcsSeason  >>>> "+lcsSeason);
				if (lcsSeason != null) {
					log.debug("lcsSeason getname >>>> "+lcsSeason.getName());
					LCSSeasonProductLink lcsprdSeasonLink = (LCSSeasonProductLink) LCSSeasonQuery
							.findSeasonProductLink(product, lcsSeason);
					if(lcsprdSeasonLink == null) {return;}
					if(lcsprdSeasonLink.isSeasonRemoved()){
						continue;
					}
					log.debug("lcsprdSeasonLink >>>> "+lcsprdSeasonLink);
					
					PreparedQueryStatement statement = LCSSeasonQuery
							.findLCSSKUSeasonLinksForProductQuery((LCSProductSeasonLink) lcsprdSeasonLink);
					SearchResults results = LCSQuery.runDirectQuery(statement);
					Vector<?> skusVector = results.getResults();

					if(skusVector == null) {return;}


					Iterator<?> itr = skusVector.iterator();
					while(itr.hasNext()) {
						FlexObject flexObject = (FlexObject) itr.next();
						log.debug("flexObject @@@@@@ "+flexObject);
						String objectId = flexObject.getString("LCSSKUSEASONLINK.IDA2A2");
						LCSSKUSeasonLink colorwayLink = (LCSSKUSeasonLink) LCSQuery
								.findObjectById("OR:com.lcs.wc.season.LCSSKUSeasonLink:" + objectId);
						
						log.debug("getSkuMaster @@@@####"+colorwayLink.getSkuMaster());
						LCSPartMaster skuMaster = colorwayLink.getSkuMaster();
						log.debug("Skumaster getName @@@@####"+skuMaster.getName());
						
						if(colorwayLink == null) {return;}
						if(colorwayLink.isSeasonRemoved()){continue;}
						String sentToS4Val = (String) colorwayLink.getValue(SENT_TO_S4);
						String clrwayLifeCycle = (String) colorwayLink.getValue(COLORWAY_LIFECYCLE);
						
						log.debug("colorwayLifecycleList >>>"+colorwayLifecycleList);
						colorwayLifecycleList.add(clrwayLifeCycle);
						
						boolean isSentToS4 = false;
						try {
							if(sentToS4Val.trim().equals(COLORWAY_SENT_TO_S4_SC_YES.trim())) {

								isSentToS4 = SPARCLockingAttUtil.resetSKUSeasonFlag(product);
							}
							if(isSentToS4) {
								colorwayLink.setValue(SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
								LCSLogic.persist(colorwayLink,true);
							}
						}catch(Exception ex) {
							log.debug("\t isSentToS4 \t"+isSentToS4);
						}
					}
					
					log.debug("colorwayLifecycleList >>>> "+colorwayLifecycleList);
					
					Iterator<?> itrs = skusVector.iterator();
					while (itrs.hasNext()) {
						FlexObject flexObject = (FlexObject) itrs.next();
						String objectId = flexObject.getString("LCSSKUSEASONLINK.IDA2A2");
						LCSSKUSeasonLink colorwayLink = (LCSSKUSeasonLink) LCSQuery
								.findObjectById("OR:com.lcs.wc.season.LCSSKUSeasonLink:" + objectId);
						if(colorwayLink == null) {return;}

						String lifeCycle = (String) colorwayLink.getValue(COLORWAY_LIFECYCLE);
						
						if(lifeCycle == null) {return;}

						LCSSKUSeasonLink prevSeasonLink = null;
						String oldColorwayLifeCycle = null;
						if (colorwayLink != null  ) {
							prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(colorwayLink);

						}
						if (prevSeasonLink != null) {
							// get old version of colorway lifecycle value
							oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
						}

						if(lifeCycle == null) {return;}
						log.debug("lifeCycle >>>> "+lifeCycle);
						if(lifeCycle.equals(DROPPED)){
							log.debug("lifeCycle is dropped skipping this colorway>>>> ");
							continue;
						}
						if ((lifeCycle != null && oldColorwayLifeCycle !=null) && ( lifeCycle.equals(DROPPED)|| lifeCycle.equals(SAMPLE_READY) || lifeCycle.equals(RELEASED) || lifeCycle.equals(PLANNED) ||  lifeCycle.equals(BUY_READY) )) {

							LCSProduct prevProduct = null;
							if (product != null) {
								prevProduct = (LCSProduct) VersionHelper.predecessorOf(product);
							}
							if(prevProduct == null) {return;}

							if (prevProduct != null && product != null) {
								String lockedKeys = null;
								try {
									
									log.debug("colorwayLifecycleList >>>"+colorwayLifecycleList);
									//Adding below logic for locking the attributes properly across seasons
									log.debug("validate >>>> ");
									if(colorwayLifecycleList.contains(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is buy ready: ");
									}else if (colorwayLifecycleList.contains(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycle.equals(DROPPED) ) ) {
										lockedKeys = PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is SAMPLE_READY: ");
									}else if (colorwayLifecycleList.contains(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is RELEASED: ");
									}else if (colorwayLifecycleList.contains(PLANNED) || (oldColorwayLifeCycle.equals(PLANNED) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is PLANNED: ");
									}
										
									
									
									//scbuyready, scPlanned, ,scsampleready
									/*
									if (lifeCycle.equals(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycle.equals(DROPPED) ) ) {
										lockedKeys = PRODUCT_SAMPLE_READY_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is 1: "+lifeCycle);
									}else if (lifeCycle.equals(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_RELEASED_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is 2: "+lifeCycle);
									}else if (lifeCycle.equals(PLANNED) || (oldColorwayLifeCycle.equals(PLANNED) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_PLANNED_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is 3: "+lifeCycle);
									}else if(lifeCycle.equals(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycle.equals(DROPPED) )) {
										lockedKeys = PRODUCT_BUY_READY_STATUS_LOCKED_ATTRIBUTE;
										log.debug("lifeCycle is 4: "+lifeCycle);
									}*/
								}catch(Exception ex) {
									if(log.isDebugEnabled()) {
										log.debug(GENERIC_MSG + 
												"\t lockedKeys \t " + lockedKeys +
												"\t lifeCycle \t " + lifeCycle +
												"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);
									}
								}
								if(lockedKeys == null) {return;}
								StringTokenizer  itrsToken = new StringTokenizer(lockedKeys,",");
								while (itrsToken.hasMoreTokens()) {
									String getKeys = (String) itrsToken.nextToken();
									if(null != prevProduct.getValue(getKeys)  &&  null != product.getValue(getKeys)  ) {
										if (!(prevProduct.getValue(getKeys).equals(product.getValue(getKeys)))) {
											product.setValue(getKeys, prevProduct.getValue(getKeys));
											throw new LCSException(GENERIC_MSG);
										}
									}


								}



							}

						}
					}

				}

			}
		}
		log.debug("### END SPARCProductLockingAttLogic.setPreviousProductAttVal ### ");
	}

	/**
	 * Locked Product Season Attribute When Colorway Lifecycle is released or sample ready
	 * 
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setPreviousProductSeasonAttVal(WTObject obj) throws WTException, WTPropertyVetoException {

		if (!(obj instanceof LCSProductSeasonLink)) {
			return;
		}

		LCSProductSeasonLink seasonProductLink = (LCSProductSeasonLink) obj;


		if ( !(seasonProductLink.isEffectLatest())) {
			return;
		}
		if ( !(seasonProductLink.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE)) && 
				!(seasonProductLink.getFlexType().getFullNameDisplay().toString().equals(FTW_RBK_FLEXTYPE)) && 
				!(seasonProductLink.getFlexType().getFullNameDisplay().toString().contains("Reebok"))) {
			return;
		}

		PreparedQueryStatement statement = LCSSeasonQuery
				.findLCSSKUSeasonLinksForProductQuery((LCSProductSeasonLink) seasonProductLink);
		if(statement == null) {
			return;
		}
		SearchResults results = LCSQuery.runDirectQuery(statement);
		@SuppressWarnings("rawtypes")
		Vector vector = results.getResults();
		Iterator<?> integrationItr =  vector.iterator();
		List clrwayLifecycleListForPSLink = new ArrayList();
		
		while(integrationItr.hasNext()) {
			FlexObject flexObject = (FlexObject) integrationItr.next();
			String objectId = flexObject.getString("LCSSKUSEASONLINK.IDA2A2");
			LCSSKUSeasonLink colorwayLink = (LCSSKUSeasonLink) LCSQuery
					.findObjectById("OR:com.lcs.wc.season.LCSSKUSeasonLink:" + objectId);
			if(null != colorwayLink) {
				String lifeCycle = (String) colorwayLink.getValue(COLORWAY_LIFECYCLE);
				clrwayLifecycleListForPSLink.add(lifeCycle);
				String sentToS4Val = (String) colorwayLink.getValue(SENT_TO_S4);
				if(null == sentToS4Val) {
					return;
				}
				boolean isSentToS4 = false;
				if(null != sentToS4Val && null !=  seasonProductLink && null != colorwayLink) {
					if(sentToS4Val.equals(COLORWAY_SENT_TO_S4_SC_YES)) {
						isSentToS4 = SPARCLockingAttUtil.resetSKUSeasonFlag(seasonProductLink);
					}
					if(isSentToS4) {
						colorwayLink.setValue(SENT_TO_S4, COST_SENT_TO_S4_SC_NO);
						LCSLogic.persist(colorwayLink,true);
					}
				}
				
			}
		}
		log.debug("colorwayLifecycleList at Product Season Link >>>> "+clrwayLifecycleListForPSLink);

		@SuppressWarnings("rawtypes")
		Iterator itr = vector.iterator();
		while (itr.hasNext()) {
			FlexObject flexObject = (FlexObject) itr.next();
			String objectId = flexObject.getString("LCSSKUSEASONLINK.IDA2A2");
			LCSSKUSeasonLink colorwayLink = (LCSSKUSeasonLink) LCSQuery
					.findObjectById("OR:com.lcs.wc.season.LCSSKUSeasonLink:" + objectId);
			String lifeCycle = (String) colorwayLink.getValue(COLORWAY_LIFECYCLE);

			if(lifeCycle == null) {return;}
			LCSSKUSeasonLink prevSeasonLink = null;
			String oldColorwayLifeCycle = null;
			if (colorwayLink != null  ) {
				prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(colorwayLink);

			}
			if (prevSeasonLink != null) {
				// get old version of colorway lifecycle value
				oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
			}
			if (lifeCycle.equals(SAMPLE_READY) || lifeCycle.equals(RELEASED) || lifeCycle.equals(DROPPED) || lifeCycle.equals(PLANNED) ||lifeCycle.equals(BUY_READY)) {
				LCSProductSeasonLink prevSeasonProductLink = null;
				if (seasonProductLink != null &&  PersistenceHelper.isPersistent(seasonProductLink)) {
					prevSeasonProductLink = (LCSProductSeasonLink) LCSSeasonQuery.getPriorSeasonProductLink(seasonProductLink); 
				}
				if (prevSeasonProductLink == null) {
					return;
				}
				String lockedAtts = null;
				try {
					//Adding below logic for locking the attributes properly across seasons
					log.debug("validate >>>> ");
					if(clrwayLifecycleListForPSLink.contains(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycle.equals(DROPPED) )) {
						lockedAtts = PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE;
						log.debug("lifeCycle is buy ready: ");
					}else if (clrwayLifecycleListForPSLink.contains(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycle.equals(DROPPED) ) ) {
						lockedAtts = PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE;
						log.debug("lifeCycle is SAMPLE_READY: ");
					}else if (clrwayLifecycleListForPSLink.contains(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycle.equals(DROPPED) )) {
						lockedAtts = PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE;
						log.debug("lifeCycle is RELEASED: ");
					}
					
					/*if (lifeCycle.equals(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycle.equals(DROPPED) )) {
						lockedAtts = PROD_SEASON_SAMPLE_READY_LOCKED_ATTRIBUTE;
					}else if (lifeCycle.equals(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycle.equals(DROPPED) )) {
						lockedAtts = PROD_SEASON_RELEASED_LOCKED_ATTRIBUTE;
					}
					if(lifeCycle.equals(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycle.equals(DROPPED) )) {
						lockedAtts = PROD_SEASON_BUY_READY_LOCKED_ATTRIBUTE;
					}*/
				}catch(Exception ex) {
					if(log.isDebugEnabled()) {
						log.debug(GENERIC_MSG + 
								"\t lockedAtts \t " + lockedAtts +
								"\t lifeCycle \t " + lifeCycle +
								"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);
					}
				}
				if(lockedAtts == null) {return;}
				StringTokenizer  itrs = new StringTokenizer(lockedAtts,",");
				while (itrs.hasMoreTokens()) {
					String keys = (String) itrs.nextToken();
					if(null != prevSeasonProductLink.getValue(keys) && null != seasonProductLink.getValue(keys) ) {
						if (!(prevSeasonProductLink.getValue(keys).equals(seasonProductLink.getValue(keys)))) {
							seasonProductLink.setValue(keys, prevSeasonProductLink.getValue(keys));
							if(log.isDebugEnabled()) {
								log.debug(GENERIC_MSG + 
										"\t lockedAtts \t " + lockedAtts +
										"\t lifeCycle \t " + lifeCycle +
										"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);
							}
							throw new LCSException(GENERIC_MSG);
						}
					}

				}


			}

		}
	}

	/**
	 * Locked Colorway Short Description Attribute if Colorway Lifecyle is released
	 * 
	 * @param obj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void setPreviousColorAttVal(WTObject obj) throws WTException, WTPropertyVetoException {
		if(!(obj instanceof LCSColor)) {
			return;
		}
		LCSColor color = (LCSColor) obj;


		if (!(color.getFlexType().getFullNameDisplay().toString().equals(COLORWAY_COLORS_FLEXTYPE))) {
			return;
		}
		LCSColor prevColor = null;
		if(color != null) {
			prevColor = (LCSColor) VersionHelper.getPreSavePersistable(color);
		}
		if(prevColor == null) {
			return;
		}
		FAWhereUsedQuery whereUsedQuery = new FAWhereUsedQuery();
		Collection<FlexObject> collection =  whereUsedQuery.checkForObjectReferences(color);
		if(collection == null) {return;}
		Iterator<FlexObject> whereUsedIter = collection.iterator();
		Iterator<FlexObject> whereUsedIterator = collection.iterator();
		List clrwayLifecycleForSSLink = new ArrayList();
		while (whereUsedIterator.hasNext()) {
			FlexObject flexObject = (FlexObject) whereUsedIterator.next();
			String objectClass = flexObject.getString("CLASS");
			String objectOID = flexObject.getString("OID");
			String whereUsedClass = flexObject.getString("WHEREUSEDCLASS");
			if(objectClass.equals("com.lcs.wc.product.LCSSKU")) {
				LCSSKU lcssku = (LCSSKU) LCSQuery.findObjectById(whereUsedClass+":"+objectOID);
				LCSSeasonMaster lcsSeasMaster = null;
				@SuppressWarnings({ "unchecked" })
				Collection<LCSSeasonMaster> masters = new LCSSeasonQuery().findSeasons(lcssku.getMaster());
				if (masters != null) {
					Iterator<LCSSeasonMaster> masterIter = masters.iterator();
					
					while (masterIter.hasNext()) {
						log.debug("Test while loop");
						lcsSeasMaster = (LCSSeasonMaster) masterIter.next();
						LCSSeason lcsSea = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasMaster);
						if (lcsSea != null) {
							LCSSKUSeasonLink lcsSKUSeasLink = (LCSSKUSeasonLink) LCSSeasonQuery
									.findSeasonProductLink(lcssku, lcsSea);
							
							String lifeCycleStatus = (String) lcsSKUSeasLink.getValue(COLORWAY_LIFECYCLE);
						
							Iterator<LCSSeasonMaster> masterItratr = masters.iterator();
							clrwayLifecycleForSSLink.add(lifeCycleStatus);
						}	
					}	
				}
			}
		}
		
		log.debug("clrwayLifecycleForSSLink : "+clrwayLifecycleForSSLink);
		 Set<String> set = new LinkedHashSet<>(clrwayLifecycleForSSLink); 
		 List<String> clrwayLifecycleListForSSLink = new ArrayList<>(set); 
		 
		while (whereUsedIter.hasNext()) {
			FlexObject flexObject = (FlexObject) whereUsedIter.next();
			String objectClass = flexObject.getString("CLASS");
			String objectOID = flexObject.getString("OID");
			String whereUsedClass = flexObject.getString("WHEREUSEDCLASS");
			if(objectClass.equals("com.lcs.wc.product.LCSSKU")) {
				LCSSKU lcssku = (LCSSKU) LCSQuery.findObjectById(whereUsedClass+":"+objectOID);
				LCSSeasonMaster lcsSeasonMaster = null;
				@SuppressWarnings({ "unchecked" })
				Collection<LCSSeasonMaster> masters = new LCSSeasonQuery().findSeasons(lcssku.getMaster());
				if (masters != null) {
					Iterator<LCSSeasonMaster> masterIter = masters.iterator();
					
					while (masterIter.hasNext()) {
						log.debug("Test while loop");
						lcsSeasonMaster = (LCSSeasonMaster) masterIter.next();
						LCSSeason lcsSeason = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasonMaster);
						if (lcsSeason != null) {
							LCSSKUSeasonLink lcsSeasonProductLink = (LCSSKUSeasonLink) LCSSeasonQuery
									.findSeasonProductLink(lcssku, lcsSeason);
							
							String lifeCycleStatus = (String) lcsSeasonProductLink.getValue(COLORWAY_LIFECYCLE);
							String lockedAtts = null; 
							LCSSKUSeasonLink prevSeasonLink = null;
							String oldColorwayLifeCycle = null;
							if (lcsSeasonProductLink != null  ) {
								prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(lcsSeasonProductLink);

							}
							try {
								if (prevSeasonLink != null) {
									// get old version of colorway lifecycle value
									oldColorwayLifeCycle = (String) prevSeasonLink.getValue(COLORWAY_LIFECYCLE);
								}
								log.debug("clrwayLifecycleListForSSLink : "+clrwayLifecycleListForSSLink);
								
								if(clrwayLifecycleListForSSLink.contains(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycleStatus.equals(DROPPED) )) {
									lockedAtts = COLOR_BUYREADY_LOCKED_ATTRIBUTE;
									log.debug("lifeCycle is buy ready: ");
								}else if (clrwayLifecycleListForSSLink.contains(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycleStatus.equals(DROPPED) ) ) {
									lockedAtts = COLOR_SAMPLEREADY_LOCKED_ATTRIBUTE;
									log.debug("lifeCycle is SAMPLE_READY: ");
								}else if (clrwayLifecycleListForSSLink.contains(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycleStatus.equals(DROPPED) )) {
									lockedAtts = COLOR_RELEASED_LOCKED_ATTRIBUTE;
									log.debug("lifeCycle is RELEASED: ");
								}
					
								/*if(clrwayLifecycleListForPSLink.contains(RELEASED) || (oldColorwayLifeCycle.equals(RELEASED) && lifeCycleStatus.equals(DROPPED) )) {
									lockedAtts = COLOR_RELEASED_LOCKED_ATTRIBUTE;
								}else if(lifeCycleStatus.equals(BUY_READY) || (oldColorwayLifeCycle.equals(BUY_READY) && lifeCycleStatus.equals(DROPPED) )) {
									lockedAtts = COLOR_BUYREADY_LOCKED_ATTRIBUTE;
								}else if(lifeCycleStatus.equals(SAMPLE_READY) || (oldColorwayLifeCycle.equals(SAMPLE_READY) && lifeCycleStatus.equals(DROPPED) )) {
									lockedAtts = COLOR_SAMPLEREADY_LOCKED_ATTRIBUTE;
								}*/
								
							}catch(Exception ex) {
								if(log.isDebugEnabled()) {
									log.debug(GENERIC_MSG + 
											"\t lockedAtts \t " + lockedAtts +
											"\t lifeCycleStatus \t " + lifeCycleStatus +
											"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);
								}
							}

							if(lockedAtts == null ) {return;}
							StringTokenizer  itrs = new StringTokenizer(lockedAtts,",");
							while (itrs.hasMoreTokens()) {
								String getKeys = (String) itrs.nextToken();
								if(null != prevColor.getValue(getKeys)  &&  null != color.getValue(getKeys)  ) {
									if (!(prevColor.getValue(getKeys).equals(color.getValue(getKeys)))) {
										color.setValue(getKeys, prevColor.getValue(getKeys));
										if(log.isDebugEnabled()) {
											log.debug(GENERIC_MSG + 
													"\t lockedAtts \t " + lockedAtts +
													"\t ColorwaylifeCycleStatus \t " + lifeCycleStatus +
													"\t oldColorwayLifeCycle \t " + oldColorwayLifeCycle);
										}
										throw new LCSException(GENERIC_MSG);
									}
								}


							}
							
						}
					}
				}
			}
		}
	}


}
