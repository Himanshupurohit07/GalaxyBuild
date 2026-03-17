package com.sparc.sizing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.infoengine.client.web.ElementTableData;
import com.lcs.wc.load.ExtractDocument;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sizing.FullSizeRange;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.ProductSizeCategoryMaster;
import com.lcs.wc.sizing.SizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;
//import com.sparc.wc.util.SparcConstants;

import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class SPARCProductSizingExtract14 implements wt.method.RemoteAccess {

	public static Logger logger = LogManager.getLogger(SPARCProductSizingExtract14.class.getName());
	static Hashtable templates = new Hashtable();
	static Set PSDMasterID = new HashSet();
	static Hashtable ProdNumbers = new Hashtable();
	static HashMap<String, ArrayList<FlexObject>> PSDSeason14 = new HashMap<String, ArrayList<FlexObject>>();

	public static void main(String[] args) {
		try {

			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			String username = "wcadmin";
			GatewayAuthenticator auth = new GatewayAuthenticator();
			auth.setRemoteUser(args[1]);
			rms.setAuthenticator(auth);
			String fileName = args[0];
			String mode = args[1].toLowerCase();
			String fromDate = null;
			@SuppressWarnings("rawtypes")
			Class[] argsTypes = { String.class, String.class };
			Object[] argsVal = { fileName, fromDate };
			rms.invoke("extractProductSizing", SPARCProductSizingExtract14.class.getName(), null, argsTypes, argsVal);

		} catch (final Exception ex) {
			ex.getLocalizedMessage();
		}

	}

	/**
	 * 
	 * @param args0 Date
	 * @throws WTException
	 * @implNote This Method is used to extract Measurement for Rebook and generate
	 *           the data file in text format
	 */
	@SuppressWarnings("unchecked")
	public static String extractProductSizing(String fileName, String date) throws WTException {
		logger.debug("START EXTRACT ProductSizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String seasonNameColDesc = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName")
				.getColumnDescriptorName();
		Date var10001 = new Date();
		int c = 0;
		int count = 0;
		List<List<String>> rows = new ArrayList<>();
		LCSSeason seasonObj = null;
		LCSProduct productObj = null;
		int counter = 0;
		getProductNumbers();
		getAllTemplates();
		findAllPSDSeason(null, null);

		try {
			// fileName = "ProductSizing.txt";
			int psdCount = 0;
			LoadCommon.printToFile(fileName, "#Start Of Extract " + new Date(), false);
			StringBuffer sb = new StringBuffer();
			SearchResults seasonResultsPSD = findAllPSD(null);
			counter = seasonResultsPSD.getResultsFound();
			//Set<String> PSDset = new HashSet<String>();
			List<String> PSDList = new ArrayList<String>();
			logger.debug("** Season Count :" + counter);
			for (c = 0; c < counter; c++) {
				FlexObject object = (FlexObject) seasonResultsPSD.getResults().get(c);
				String prodMasterID = object.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6");
				if (ProdNumbers.get(object.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6")) != null) {
					ArrayList<FlexObject> tempSeasonList = PSDSeason14.get(object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"));
					logger.debug("** Checking:" + prodMasterID +"::"+tempSeasonList);
					if (tempSeasonList!=null && tempSeasonList.size()>0) {
						logger.debug("** Checking:" + prodMasterID +"::"+tempSeasonList.size());
						//FlexObject seaObj = (FlexObject) PSDSeason14.get(object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"));
						for(FlexObject tempSeason:tempSeasonList) {
							PSDList.add(getExtractedObject(object, tempSeason));
						}
						
					} else {
						PSDList.add(getExtractedObjectNS(object));
					}
					psdCount++;
				}
				count++;
			}
			Iterator it = PSDList.iterator();
			String pLine= "";
			while (it.hasNext()) {
				pLine = (String)it.next();
				//pLine = pLine.replaceAll("^^","^ ^");
				sb.append(pLine + "\n");
			}
			LoadCommon.printToFile(fileName, sb.toString(), false);
			logger.debug("** Extarcted PSD Data ---------- " + count);
			String var10000 = fileName;
			var10001 = new Date();
			LoadCommon.printToFile(var10000, LoadCommon.LINEFEED + "#End Of Extract " + var10001);
			LoadCommon.SERVER_MODE = true;
			if (rows != null && rows.size() > 0) {
				// createDataFile(rows, FILE_PATH);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getExtractedObjectNS(FlexObject flexObject) throws WTException {
		FlexType prodType = FlexTypeCache.getFlexTypeFromPath("Product");
		String ProdNo = (prodType.getAttribute("scPLMProductNo")).getColumnName();
		//String ProdNo = (prodType.getAttribute("lfRootProductNum")).getColumnName();
		String line = "";
		line = "NONSEASON^";
		// line = line +
		// ProdNumbers.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6" ))
		// +"^";
		// line = line +flexObject.getData("LCSPRODUCT." +ProdNo) +"^";
		line = line + ProdNumbers.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6")) + "^";
		line = line + flexObject.getData("SIZECATEGORY.NAME") + "^";
		line = line + flexObject.getData("FULLSIZERANGE.NAME") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORYMASTER.NAME") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.SIZEVALUES") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.SIZE2VALUES") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.BASESIZE") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.BASESIZE2") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORYMASTER.SIZECATEGORYTYPE") + "^";
		line = line + templates.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3C6")) + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.IDA2A2") + "^^";
		logger.debug("Returning Line:"+line);
		return line;
	}

	public static String getExtractedObject(FlexObject flexObject, FlexObject seasonObj) throws WTException {
		//FlexType prodType = FlexTypeCache.getFlexTypeFromPath("Product");
		//String ProdNo = (prodType.getAttribute("scPLMProductNo")).getColumnName();
		logger.debug("Processing flexObject:"+flexObject);
		logger.debug("Processing seasonObj:"+seasonObj);
		String seasonNameColDesc = ((FlexTypeCache.getFlexTypeRoot("Season")).getAttribute("seasonName"))
				.getColumnName();
		String line = "";
		line = seasonObj.getData("LCSSEASON." +seasonNameColDesc) + "^";
		// line = ProdNumbers.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6"
		// )) +"^";
		line = line + ProdNumbers.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6")) + "^";
		
		// line = line +flexObject.getData("LCSPRODUCT." +ProdNo) +"^";
		line = line + flexObject.getData("SIZECATEGORY.NAME") + "^";
		line = line + flexObject.getData("FULLSIZERANGE.NAME") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORYMASTER.NAME") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.SIZEVALUES") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.SIZE2VALUES") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.BASESIZE") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.BASESIZE2") + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORYMASTER.SIZECATEGORYTYPE") + "^";
		line = line + templates.get(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA3C6")) + "^";
		line = line + flexObject.getData("PRODUCTSIZECATEGORY.IDA2A2") + "^";
		line = line + seasonObj.getData("ACTIVEPSDTOSEASONMASTER.IDA2A2") + "^";
		PSDMasterID.add(flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"));
		//System.out.println("#### SeasonObj ::" +flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2") +"::" + seasonObj.getData("LCSSEASON." +seasonNameColDesc));
		// flexObject.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2") +":: ::" );
		logger.debug("Returning Line:"+line);
		return line;
	}

	public static void getProductNumbers() throws WTException {
		FlexType prodType = FlexTypeCache.getFlexTypeFromPath("Product");
		String ProdNo = (prodType.getAttribute("scPLMProductNo")).getColumnName();
		//String ProdNo = (prodType.getAttribute("lfRootProductNum")).getColumnName();
		//FlexType prdseasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Product\\Apparel");
		//FlexType prdRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Product\\Accessories");
		FlexType prdseasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Product\\scApparel\\scReebok");
		FlexType prdRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Product\\scFootwear\\scFootwearReebok");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(ProductSizeCategoryMaster.class);
		statement.appendFromTable("PRODAREV");
		statement.appendSelectColumn(
				new QueryColumn(ProductSizeCategoryMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "productMasterReference.key.id"));
		statement.appendSelectColumn("PRODAREV", ProdNo);
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(ProductSizeCategoryMaster.class, "productMasterReference.key.id"),
				new QueryColumn("PRODAREV", "ida3MasterReference"));
		// statement.appendAndIfNeeded();
		// statement.addLatestIterationClause(LCSProduct.class);
		statement.appendAndIfNeeded();
		statement.appendOpenParen();
		statement.appendCriteria(
				new Criteria("PRODAREV", "flexTypeIdPath", prdseasonRbkApparelType.getTypeIdPath(), Criteria.EQUALS));
		statement.appendOr();
		statement.appendCriteria(
				new Criteria("PRODAREV", "flexTypeIdPath", prdRbkFootwearType.getTypeIdPath(), Criteria.EQUALS));
		statement.appendClosedParen();
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryType"), "?", "="),
				"INSTANCE");
		statement.setDistinct(true);
		SearchResults searchresults = LCSQuery.runDirectQuery(statement);
		int c = searchresults.getResultsFound();
		//.out.println("#### ::" + statement.toString() +":: ::" + c);
		
		logger.debug("#### product Nos ::" + statement.toString() +":: ::" + c);
		
		for (int i = 0; i < c; i++) {
			FlexObject object = (FlexObject) searchresults.getResults().get(i);
			logger.debug("prod object:"+i+":"+object);
			ProdNumbers.put(object.getData("PRODUCTSIZECATEGORYMASTER.IDA3B6"),
					(object.getData("PRODAREV." + ProdNo)));
		}
		logger.debug("#### ProdNumbers:"+ProdNumbers);
	}

	public static void getAllTemplates() throws WTException {

		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
		preparedquerystatement.appendFromTable(ProductSizeCategoryMaster.class);
		preparedquerystatement.appendSelectColumn("ProductSizeCategoryMaster", "IDA2A2");
		preparedquerystatement.appendSelectColumn("ProductSizeCategoryMaster", "NAME");
		preparedquerystatement.appendCriteria(
				new Criteria(new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryType"), "?", "="),
				"TEMPLATE");
		preparedquerystatement.setDistinct(true);
		SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
		int c = searchresults.getResultsFound();
		
		logger.debug("#### Size Definitions ::" + preparedquerystatement.toString() +":: ::" + c);
		
		for (int i = 0; i < c; i++) {
			FlexObject object = (FlexObject) searchresults.getResults().get(i);
			logger.debug("SD object:"+c+":"+object);
			templates.put(object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"),
					(object.getData("PRODUCTSIZECATEGORYMASTER.NAME")));
		}
		
		logger.debug("#### templates:"+templates);
	}

	public static SearchResults findAllPSD(Collection attCols) throws WTException {
		logger.debug("START EXTRACT findPSDBySeason >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(ProductSizeCategory.class);
		statement.appendFromTable(ProductSizeCategoryMaster.class);
		statement.appendFromTable(SizeCategory.class);
		statement.appendFromTable(FullSizeRange.class);
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "name"));
		statement.appendSelectColumn(
				new QueryColumn(ProductSizeCategoryMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "sizeRangeReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "productMasterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryType"));
		statement.appendSelectColumn(
				new QueryColumn(ProductSizeCategory.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategory.class, "sizeValues"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategory.class, "size2Values"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategory.class, "baseSize"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategory.class, "baseSize2"));
		statement.appendSelectColumn(new QueryColumn(SizeCategory.class, "name"));
		statement.appendSelectColumn(new QueryColumn(FullSizeRange.class, "name"));
		statement.appendJoin(new QueryColumn(SizeCategory.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryReference.key.id"));
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(FullSizeRange.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(ProductSizeCategoryMaster.class, "fullSizeRangeReference.key.id"));
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(ProductSizeCategoryMaster.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(ProductSizeCategory.class, "masterReference.key.id"));
		logger.debug(">>>>>>>>>>>>>>>");
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryType"), "?", "="),
				"INSTANCE");
		logger.debug(">>>>>>>>>>>>>>>");
		statement.addLatestIterationClause(ProductSizeCategory.class);
		statement.setDistinct(true);
		logger.debug(">>>>>>>>>>>>>>>" + statement.toString());
		// System.out.println(">>>>>>>>>>>>>>>" + statement.toString());
		
		return LCSQuery.runDirectQuery(statement);
	}

	public static void findAllPSDSeason(LCSSeason season, Collection attCols) throws WTException {
		FlexType seasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scRbkApparel");
		FlexType seasonRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Season\\scSeasonReebok\\scFootwear");
		//FlexType seasonRbkApparelType = FlexTypeCache.getFlexTypeFromPath("Season\\Apparel");
		//FlexType seasonRbkFootwearType = FlexTypeCache.getFlexTypeFromPath("Season\\Accessories");
		String seasonNameColDesc = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName")
				.getColumnDescriptorName();
		String seasonColDesc = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName")
				.getColumnName();
		logger.debug("START EXTRACT findAllPSDSeason >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(ProductSizeCategoryMaster.class);
		statement.appendFromTable(LCSSeason.class);
		statement.appendFromTable("ActivePSDToSeasonMaster");
		statement.appendFromTable(ProdSizeCategoryToSeason.class);
		statement.appendSelectColumn(
				new QueryColumn(ProductSizeCategoryMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(ProductSizeCategoryMaster.class, "productMasterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSeason.class, seasonNameColDesc));
		statement.appendSelectColumn(new QueryColumn("ActivePSDToSeasonMaster", "idA2A2"));
		// statement.appendSelectColumn(new QueryColumn(ProdSizeCategoryToSeason.class,
		// "sizeValues"));
		// statement.appendSelectColumn(new QueryColumn(ProdSizeCategoryToSeason.class,
		// "size2Values"));
		statement.appendCriteria(
				new Criteria(new QueryColumn(ProductSizeCategoryMaster.class, "sizeCategoryType"), "?", "="),
				"INSTANCE");
		logger.debug(">>>>>>>>>>>>>>>");
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn("ActivePSDToSeasonMaster", "idA3A6"),
				new QueryColumn(ProductSizeCategoryMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn("ActivePSDToSeasonMaster", "idA3B6"),
				new QueryColumn(LCSSeason.class, "masterReference.key.id"));
		statement.appendAndIfNeeded();
		statement.appendJoin(new QueryColumn(ProdSizeCategoryToSeason.class, "masterReference.key.id"),
				new QueryColumn("ActivePSDToSeasonMaster", "idA2A2"));
		statement.appendAndIfNeeded();
		statement.appendOpenParen();
		statement.appendCriteria(
				new Criteria("LCSSeason", "flexTypeIdPath", seasonRbkApparelType.getTypeIdPath(), Criteria.EQUALS));
		statement.appendOr();
		statement.appendCriteria(
				new Criteria("LCSSeason", "flexTypeIdPath", seasonRbkFootwearType.getTypeIdPath(), Criteria.EQUALS));
		statement.appendClosedParen();
		statement.addLatestIterationClause(ProdSizeCategoryToSeason.class);
		statement.addLatestIterationClause(LCSSeason.class);
		statement.setDistinct(true);
		//logger.debug(">>>>>>>>>>>>>>> PSD to Season" + statement.toString());
		
		//System.out.println(">>>>>>>>>>>>>>>" + statement.toString());
		SearchResults searchresults = LCSQuery.runDirectQuery(statement);
		int c = searchresults.getResultsFound();
		logger.debug("#### PSD to season ::" + statement.toString() +":: ::" + c);
		for (int i = 0; i < c; i++) {
			FlexObject object = (FlexObject) searchresults.getResults().get(i);
			System.out.println( "### PSD Season Obj: ->" + object.toString());
			object.put("LCSSEASON.NAME", object.getData("LCSSeason." + seasonNameColDesc));
			//System.out.println(seasonNameColDesc + "::@@@@ : ->" +  object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2")+":: ::" + object.getData("LCSSEASON."+ seasonColDesc) +"::" );
			logger.debug("**Putting PSDSeason14:"+object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"));
			ArrayList<FlexObject> tempArrayList = null;
			if (PSDSeason14.get((String)object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"))==null) {
				tempArrayList = new ArrayList<FlexObject>();
				tempArrayList.add(object);
				PSDSeason14.put(object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"), tempArrayList);
			}
			else {
				tempArrayList = PSDSeason14.get((String)object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"));
				tempArrayList.add(object);
				PSDSeason14.put(object.getData("PRODUCTSIZECATEGORYMASTER.IDA2A2"), tempArrayList);
			}
			
		}
		logger.debug("**PSDSeason14:"+PSDSeason14);
		return;
	}
}
