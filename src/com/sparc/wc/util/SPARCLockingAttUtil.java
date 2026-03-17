package com.sparc.wc.util;



import java.util.Date;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.part.LCSPart;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author dsagar56
 *
 */
public class SPARCLockingAttUtil {
	/**Initializing log */
	private static Logger log = LogManager.getLogger(SPARCLockingAttUtil.class);

	/**Initializing COLORWAY_SEASON_INTEGRATION_KEYS */
	private static String COLORWAY_SEASON_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.product.LCSSKUSeasonLink.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing PRODUCT_SEASON_INTEGRATION_KEYS */
	private static String PRODUCT_SEASON_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.product.LCSProductSeasonLink.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing COLORWAY_INTEGRATION_KEYS */
	private static String COLORWAY_INTEGRATION_KEYS = LCSProperties.get("com.lcs.wc.product.LCSSKU.IntegrationAttributesKeys.INTEGRATION.ATT.KEYS");

	/**Initializing APP_PRODUCT_LOCKING_ATT */
	private static String APP_PRODUCT_LOCKING_ATT = LCSProperties.get("com.lcs.wc.product.LCSProduct.IntegrationAttributesKeys.APPAREL.REEBOK.INTEGRATION.ATT.KEYS");

	/**Initializing FTW_PRODUCT_LOCKING_ATT */
	private static String FTW_PRODUCT_LOCKING_ATT = LCSProperties.get("com.lcs.wc.product.LCSProduct.IntegrationAttributesKeys.FOOTWEAR.REEBOK.INTEGRATION.ATT.KEYS");

	/**Initializing APP_RBK_FLEXTYPE */
	private static String APP_RBK_FLEXTYPE = LCSProperties.get("com.lcs.wc.product.LCSProduct.FlexType.Reebok");

	/**Initializing FTW_RBK_FLEXTYPE */
	private static String FTW_RBK_FLEXTYPE = LCSProperties.get("com.lcs.wc.product.LCSProduct.FlexType.Footwear");


	/**
	 * @param obj
	 * @param integrationListAttKeys
	 * @return
	 * @throws WTException
	 */
	public static boolean isAttsUpdated(WTObject obj,String integrationListAttKeys) throws WTException {
		// TODO Auto-generated method stub
		log.debug("## START : SPARCLockingAttUtil.isAttsUpdated ##");
		boolean isAttModify = false;
		if(obj instanceof LCSProductCostSheet ) {
			LCSProductCostSheet currentProductCostSheet = (LCSProductCostSheet) obj;
			LCSProductCostSheet prevProductCostSheet = null;
			
			
			if(currentProductCostSheet != null) {
				prevProductCostSheet = (LCSProductCostSheet) VersionHelper.predecessorOf(currentProductCostSheet);
			}
			if(prevProductCostSheet == null) {
				log.debug("\t There are no previous version of cost sheet \t" + prevProductCostSheet + 
						"\t OR having data issue\t");
				isAttModify = true;
			}
			
			if(prevProductCostSheet != null && currentProductCostSheet != null) {
				StringTokenizer keysIterator = new StringTokenizer(integrationListAttKeys,",");
				while(keysIterator.hasMoreTokens()) {
					String integrationKeys = keysIterator.nextToken();
					if(log.isDebugEnabled() && currentProductCostSheet.getValue(integrationKeys) == null && prevProductCostSheet.getValue(integrationKeys) == null) {
						log.debug("\t Keys frm property entries \t" + integrationKeys + 
								"\t getting null values for current product cost sheet \t" + currentProductCostSheet.getValue(integrationKeys) + 
								"\t getting null values for previous version of product cost sheet\t" + currentProductCostSheet.getValue(integrationKeys));
					}

					if((prevProductCostSheet.getValue(integrationKeys) == null && currentProductCostSheet.getValue(integrationKeys) == null )){
						isAttModify = false;

					}
					if((prevProductCostSheet.getValue(integrationKeys)!= null && currentProductCostSheet.getValue(integrationKeys)!= null )){

						//When prev and currnet not  null
						if(prevProductCostSheet.getValue(integrationKeys).equals("") && currentProductCostSheet.getValue(integrationKeys).equals("")) {
							isAttModify = false;
						}
						if(!(currentProductCostSheet.getValue(integrationKeys).equals(prevProductCostSheet.getValue(integrationKeys)))) {
							isAttModify = true;
							break;
						}

					}else  if((currentProductCostSheet.getValue(integrationKeys)!= null )
							&&!currentProductCostSheet.getValue(integrationKeys).equals("")
							&&!currentProductCostSheet.getValue(integrationKeys).equals(" ")) {
						isAttModify = true;
						break;
					}
					if( prevProductCostSheet.getValue(integrationKeys) != null) {
						if((currentProductCostSheet.getValue(integrationKeys)!= null)) {
							if(currentProductCostSheet.getValue(integrationKeys).equals("")
									||currentProductCostSheet.getValue(integrationKeys).equals(" ")){
								if(!(currentProductCostSheet.getValue(integrationKeys).equals(prevProductCostSheet.getValue(integrationKeys)))) {
									isAttModify = true;
									break;
								}
							}
						}else if(null == currentProductCostSheet.getValue(integrationKeys)) {
							isAttModify = true;
							break;
						}

					}

				}

				return isAttModify;

			}

		}else if(obj instanceof LCSSourcingConfig ) {
			LCSSourcingConfig currentsourcingConfig = (LCSSourcingConfig) obj;
			LCSSourcingConfig prevsourcingConfig = null;
			if(currentsourcingConfig != null) {
				prevsourcingConfig = (LCSSourcingConfig) VersionHelper.predecessorOf(currentsourcingConfig);
			}
			if(log.isDebugEnabled() && prevsourcingConfig == null) {
				log.debug("\t There are no previous version of Sourcing Config \t" + prevsourcingConfig + 
						"\t OR having data issue\t");
			}
			if(prevsourcingConfig != null) {
				StringTokenizer keysIterator = new StringTokenizer(integrationListAttKeys,",");
				while(keysIterator.hasMoreTokens()) {
					String integrationKeys = keysIterator.nextToken();
					if(log.isDebugEnabled() && currentsourcingConfig.getValue(integrationKeys) == null && prevsourcingConfig.getValue(integrationKeys) == null) {
						log.debug("\t Keys frm property entries \t" + integrationKeys + 
								"\t getting null values for current Sourcing Config \t" + currentsourcingConfig.getValue(integrationKeys) + 
								"\t getting null values for previous version of Sourcing Config\t" + prevsourcingConfig.getValue(integrationKeys));
					}
					if((prevsourcingConfig.getValue(integrationKeys) == null && currentsourcingConfig.getValue(integrationKeys) == null )){
						isAttModify = false;

					}
					if((prevsourcingConfig.getValue(integrationKeys)!= null && currentsourcingConfig.getValue(integrationKeys)!= null )){

						//When prev and currnet not  null
						if(prevsourcingConfig.getValue(integrationKeys).equals("") && currentsourcingConfig.getValue(integrationKeys).equals("")) {
							isAttModify = false;
						}
						if(!(currentsourcingConfig.getValue(integrationKeys).equals(prevsourcingConfig.getValue(integrationKeys)))) {
							isAttModify = true;
							break;
						}

					}else  if((currentsourcingConfig.getValue(integrationKeys)!= null )
							&&!currentsourcingConfig.getValue(integrationKeys).equals("")
							&&!currentsourcingConfig.getValue(integrationKeys).equals(" ")) {
						isAttModify = true;
						break;
					}
					if( prevsourcingConfig.getValue(integrationKeys) != null) {
						if((currentsourcingConfig.getValue(integrationKeys)!= null)) {
							if(currentsourcingConfig.getValue(integrationKeys).equals("")
									||currentsourcingConfig.getValue(integrationKeys).equals(" ")){
								if(!(currentsourcingConfig.getValue(integrationKeys).equals(prevsourcingConfig.getValue(integrationKeys)))) {
									isAttModify = true;
									break;
								}
							}
						}else if(null==currentsourcingConfig.getValue(integrationKeys)) {
							isAttModify = true;
							break;
						}

					}

				}
				return isAttModify;
			}
		}

		log.debug("## END : SPARCLockingAttUtil.isAttsUpdated ##");
		return isAttModify;

	}

	/**
	 * @param object
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static boolean resetSKUSeasonFlag(WTObject object) throws WTException, WTPropertyVetoException {
		boolean isAttModify = false;
		if(object instanceof LCSSKUSeasonLink) {
			LCSSKUSeasonLink seasonLink = (LCSSKUSeasonLink) object;
			LCSSKUSeasonLink prevSeasonLink = null;
			if (seasonLink != null  ) {
				prevSeasonLink=(LCSSKUSeasonLink)LCSSeasonQuery.getPriorSeasonProductLink(seasonLink);

			}

			StringTokenizer tokenizer = new StringTokenizer(COLORWAY_SEASON_INTEGRATION_KEYS, ",");
			while(tokenizer.hasMoreTokens()) {
				String integrationKeys = tokenizer.nextToken();
				if(log.isDebugEnabled() && seasonLink.getValue(integrationKeys) == null && prevSeasonLink.getValue(integrationKeys) == null) {
					log.debug("\t Keys frm property entries \t" + integrationKeys + 
							"\t getting null values for current Colorway Season\t" + seasonLink.getValue(integrationKeys) + 
							"\t getting null values for previous version of Colorway Season\\\t" + prevSeasonLink.getValue(integrationKeys));
				}
				if((prevSeasonLink.getValue(integrationKeys) == null && seasonLink.getValue(integrationKeys) == null )){
					isAttModify = false;

				}
				if((prevSeasonLink.getValue(integrationKeys)!= null && seasonLink.getValue(integrationKeys)!= null )){

					//When prev and currnet not  null
					if(prevSeasonLink.getValue(integrationKeys).equals("") && seasonLink.getValue(integrationKeys).equals("")) {
						isAttModify = false;
					}
					if(!(seasonLink.getValue(integrationKeys).equals(prevSeasonLink.getValue(integrationKeys)))) {
						isAttModify = true;
						break;
					}

				}else  if((seasonLink.getValue(integrationKeys)!= null )
						&&!seasonLink.getValue(integrationKeys).equals("")
						&&!seasonLink.getValue(integrationKeys).equals(" ")) {
					isAttModify = true;
					break;
				}
				if( prevSeasonLink.getValue(integrationKeys) != null) {
					if((seasonLink.getValue(integrationKeys)!= null)) {
						if(seasonLink.getValue(integrationKeys).equals("")
								||seasonLink.getValue(integrationKeys).equals(" ")){
							if(!(seasonLink.getValue(integrationKeys).equals(prevSeasonLink.getValue(integrationKeys)))) {
								isAttModify = true;
								break;
							}
						}
					}else if(null==seasonLink.getValue(integrationKeys)) {
						isAttModify = true;
						break;
					}

				}

			}
			return isAttModify;
		}
		else if(object instanceof LCSSKU) {
			LCSSKU colorway = (LCSSKU) object;
			LCSSKU prevColorway = null;
			if (colorway != null  ) {
				prevColorway=(LCSSKU) VersionHelper.predecessorOf(colorway);

			}
			StringTokenizer tokenizer = new StringTokenizer(COLORWAY_INTEGRATION_KEYS, ",");
			while(tokenizer.hasMoreTokens()) {
				String integrationKeys = tokenizer.nextToken();
				if(log.isDebugEnabled() && colorway.getValue(integrationKeys) == null && prevColorway.getValue(integrationKeys) == null) {
					log.debug("\t Keys frm property entries \t" + integrationKeys + 
							"\t getting null values for current Colorway\t" + colorway.getValue(integrationKeys) + 
							"\t getting null values for previous version of Colorway \t" + prevColorway.getValue(integrationKeys));
				}
				if((prevColorway.getValue(integrationKeys) == null && colorway.getValue(integrationKeys) == null )){
					isAttModify = false;

				}
				if((prevColorway.getValue(integrationKeys)!= null && colorway.getValue(integrationKeys)!= null )){

					//When prev and currnet not  null
					if(prevColorway.getValue(integrationKeys).equals("") && colorway.getValue(integrationKeys).equals("")) {
						isAttModify = false;
					}
					if(!(colorway.getValue(integrationKeys).equals(prevColorway.getValue(integrationKeys)))) {
						isAttModify = true;
						break;
					}

				}else  if((colorway.getValue(integrationKeys)!= null )
						&&!colorway.getValue(integrationKeys).equals("")
						&&!colorway.getValue(integrationKeys).equals(" ")) {
					isAttModify = true;
					break;
				}
				if( prevColorway.getValue(integrationKeys) != null) {
					if((colorway.getValue(integrationKeys)!= null)) {
						if(colorway.getValue(integrationKeys).equals("")
								||colorway.getValue(integrationKeys).equals(" ")){
							if(!(colorway.getValue(integrationKeys).equals(prevColorway.getValue(integrationKeys)))) {
								isAttModify = true;
								break;
							}
						}
					}else if(null==colorway.getValue(integrationKeys)) {
						isAttModify = true;
						break;
					}

				}

			}
			return isAttModify;
		} else if(object instanceof LCSProduct) {
			LCSProduct product = (LCSProduct) object;
			LCSProduct prevProduct = null;
			if (product != null  ) {
				prevProduct=(LCSProduct) VersionHelper.predecessorOf(product);

			}
			String productTypeLockingKeys = null;
			if(product.getFlexType().getFullNameDisplay().toString().equals(APP_RBK_FLEXTYPE)) {
				productTypeLockingKeys = APP_PRODUCT_LOCKING_ATT;
			}else if(product.getFlexType().getFullNameDisplay().toString().equals(FTW_RBK_FLEXTYPE)) {
				productTypeLockingKeys = FTW_PRODUCT_LOCKING_ATT;
			}
			if(productTypeLockingKeys == null || productTypeLockingKeys.equals("")) {
				if(log.isDebugEnabled()) {
					log.debug("\t KeyNotFound \t" + productTypeLockingKeys);
				}
				return false;
			}
			StringTokenizer tokenizer = new StringTokenizer(productTypeLockingKeys, ",");
			while(tokenizer.hasMoreTokens()) {
				String integrationKeys = tokenizer.nextToken();
				if(log.isDebugEnabled() && product.getValue(integrationKeys) == null && prevProduct.getValue(integrationKeys) == null) {
					log.debug("\t Keys frm property entries \t" + integrationKeys + 
							"\t getting null values for current Product\t" + product.getValue(integrationKeys) + 
							"\t getting null values for previous version of Product \t" + prevProduct.getValue(integrationKeys));
				}
				LCSPart productSeasonLink;
				if((prevProduct.getValue(integrationKeys) == null && product.getValue(integrationKeys) == null )){
					isAttModify = false;

				}
				if((prevProduct.getValue(integrationKeys)!= null && product.getValue(integrationKeys)!= null )){

					//When prev and currnet not  null
					if(prevProduct.getValue(integrationKeys).equals("") && product.getValue(integrationKeys).equals("")) {
						isAttModify = false;
					}
					if(!(product.getValue(integrationKeys).equals(prevProduct.getValue(integrationKeys)))) {
						isAttModify = true;
						break;
					}

				}else  if((product.getValue(integrationKeys)!= null )
						&&!product.getValue(integrationKeys).equals("")
						&&!product.getValue(integrationKeys).equals(" ")) {
					isAttModify = true;
					break;
				}
				if( prevProduct.getValue(integrationKeys) != null) {
					if((product.getValue(integrationKeys)!= null)) {
						if(product.getValue(integrationKeys).equals("")
								||product.getValue(integrationKeys).equals(" ")){
							if(!(product.getValue(integrationKeys).equals(prevProduct.getValue(integrationKeys)))) {
								isAttModify = true;
								break;
							}
						}
					}else if(null==product.getValue(integrationKeys)) {
						isAttModify = true;
						break;
					}

				}

			}
			return isAttModify;
		}
		else if(object instanceof LCSProductSeasonLink) {

			LCSProductSeasonLink productSeasonLink = (LCSProductSeasonLink) object;
			LCSProductSeasonLink prevProductSeasonLink = null;
			if (productSeasonLink != null  ) {
				prevProductSeasonLink=(LCSProductSeasonLink) LCSSeasonQuery.getPriorSeasonProductLink(productSeasonLink);

			}
			StringTokenizer tokenizer = new StringTokenizer(PRODUCT_SEASON_INTEGRATION_KEYS, ",");
			while(tokenizer.hasMoreTokens()) {
				String integrationKeys = tokenizer.nextToken();
				if(log.isDebugEnabled() && productSeasonLink.getValue(integrationKeys) == null && prevProductSeasonLink.getValue(integrationKeys) == null) {
					log.debug("\t Keys frm property entries \t" + integrationKeys + 
							"\t getting null values for current Product Season\t" + productSeasonLink.getValue(integrationKeys) + 
							"\t getting null values for previous version of Product Season \t" + prevProductSeasonLink.getValue(integrationKeys));
				}
				if((prevProductSeasonLink.getValue(integrationKeys) == null && productSeasonLink.getValue(integrationKeys) == null )){
					isAttModify = false;

				}
				if((prevProductSeasonLink.getValue(integrationKeys)!= null && productSeasonLink.getValue(integrationKeys)!= null )){

					//When prev and currnet not  null
					if(prevProductSeasonLink.getValue(integrationKeys).equals("") && productSeasonLink.getValue(integrationKeys).equals("")) {
						isAttModify = false;
					}
					if(!(productSeasonLink.getValue(integrationKeys).equals(prevProductSeasonLink.getValue(integrationKeys)))) {
						
						isAttModify = true;
						break;
					}

				}else  if((productSeasonLink.getValue(integrationKeys)!= null )
						&&!productSeasonLink.getValue(integrationKeys).equals("")
						&&!productSeasonLink.getValue(integrationKeys).equals(" ")) {
					isAttModify = true;
					break;
				}
				if( prevProductSeasonLink.getValue(integrationKeys) != null) {
					if((productSeasonLink.getValue(integrationKeys)!= null)) {
						if(productSeasonLink.getValue(integrationKeys).equals("")
								||productSeasonLink.getValue(integrationKeys).equals(" ")){


							if(!(productSeasonLink.getValue(integrationKeys).equals(prevProductSeasonLink.getValue(integrationKeys)))) {
								isAttModify = true;
								break;
							}
						}
					}else if(null==productSeasonLink.getValue(integrationKeys)) {
						isAttModify = true;
						break;
					}

				}

			}			
			return isAttModify;
		}
		return isAttModify;
	}



}
