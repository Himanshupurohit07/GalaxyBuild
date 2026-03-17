package com.galaxy.sizing;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.*;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.sizing.ProductSizingLogic;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSRevisionControlled;
import com.lcs.wc.load.ExtractMeasurement;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.load.LoadSku;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsLogic;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.measurements.LCSPointsOfMeasureLogic;
import com.lcs.wc.measurements.MeasurementValues;
import com.lcs.wc.part.LCSPart;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.FullSizeRange;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.ProductSizeCategoryMaster;
import com.lcs.wc.sizing.SizeCategory;
import com.lcs.wc.sizing.SizingHelper;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import com.lcs.wc.sizing.ProductSizeCategoryClientModel;
import com.lcs.wc.sizing.ProdSizeCategoryToSeasonClientModel;

public class SPARCLoadProductSizing21 implements wt.method.RemoteAccess {
	private static final Logger logger = LogManager.getLogger(SPARCLoadProductSizing21.class.getName());
	public static boolean DEBUG = true;
	public static Hashtable allSeasons = new Hashtable();
	public static Hashtable allSizCat = new Hashtable();
	public static Hashtable allFullSizRange = new Hashtable();
	public static Hashtable allTemplates = new Hashtable();

	public static String saveDetails(ProdSizeCategoryToSeasonClientModel pscscm, ProductSizeCategoryClientModel psccm, String status, String seasonName ) {
		String newStatus =status;
		try {
			if(pscscm !=null) {
			pscscm.save(psccm);
			}else {
				psccm.save();
			}
		}  catch (WTException | WTPropertyVetoException e) {
			if(seasonName!=null) {
				newStatus = status + " FAILED in season :" +seasonName+".\n" ;
			}
			else {
				newStatus = status + " FAILED.\n" ;
			}
			e.printStackTrace();
			return newStatus;
			
		}
		
		return newStatus;
	}
	
	
	public static ProductSizeCategoryMaster persistPSDMaster(ProductSizeCategoryMaster pscm1, String status) {
		
		ProductSizeCategoryMaster pscm = null;
		String newStatus = status;
		try {
		pscm =  (ProductSizeCategoryMaster)LCSLogic.persist(pscm1);
		}catch(WTException e){
			logger.debug(status + " FAILED Due to Persistence issues.\n") ;
			e.printStackTrace();
			return pscm;
		}
			return pscm;
	}
	 public static ProductSizeCategory createProductSizeCategory(ArrayList ar )
	      throws WTException, WTPropertyVetoException
	   {
		 logger.debug("START Loading ProductSizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");  
		 logger.debug("Data Received:"+ar); 
		 StringBuffer sb = new StringBuffer();
		 String status ="";
		 getAllTemplates();
			getAllSeason();
			getAllSizeCategory();
			getAllFullSizeRange();
			FlexType psType = FlexTypeCache.getFlexTypeFromPath("Size Definition");
			ProductSizeCategoryMaster pscm= null;
		 for(int t=0;t<ar.size();t++){
			 status ="";
			 	pscm= null;
			  Hashtable nv = (Hashtable)ar.get(t);
			  LCSProduct product = getProduct( nv.get("productNumber").toString());
			  status = "Line Number :" +t +" : Product #: " + nv.get("productNumber").toString();
			  if(product== null ) {
				  status = status + " does not exist.\n" ;
			  }
			  LCSSeason season = (LCSSeason) allSeasons.get(nv.get("seasonName").toString());  //getSeason( nv.get("seasonName").toString());
			  SizeCategory sizeCat = (SizeCategory)allSizCat.get(nv.get("scName").toString()); 
			  getSizeCategory( nv.get("scName").toString());
			  FullSizeRange  fsr = (FullSizeRange)allFullSizRange.get(nv.get("fsrName").toString()); //getFullSizeRange( nv.get("fsrName").toString());
			  String oldPSDMasterID = nv.get("PSDID").toString();
			  
			  logger.debug("Fetching Master:"+oldPSDMasterID);
			  if(product !=null && sizeCat!= null && fsr !=null){
				  pscm =  getProductSizeCategoryByAtt(oldPSDMasterID);
				  logger.debug("pscm1:"+pscm);
				  ProductSizeCategoryMaster template = (ProductSizeCategoryMaster)allTemplates.get(nv.get("TemplateID").toString());
				  
				  if(pscm ==null) {
					  pscm = new ProductSizeCategoryMaster();
				 // ProductSizeCategoryMaster template = (ProductSizeCategoryMaster)allTemplates.get(nv.get("TemplateID").toString());
				  logger.debug("template:"+template);
				  pscm.setProductMaster(product.getMaster());
				  pscm.setContainer(product.getContainer());
				  pscm.setName( nv.get("scmName").toString());
				  pscm.setSizeCategoryType("INSTANCE");
				  pscm.setFullSizeRange(fsr);
				  pscm.setSizeCategory(sizeCat);
				  pscm.setSeries("wt.series.HarvardSeries");
				  pscm.setSizeRange(template);
				  //pscm = (ProductSizeCategoryMaster)LCSLogic.persist(pscm);
				  pscm =  persistPSDMaster(pscm, status);
				  logger.debug("pscm2:"+pscm);
				  }
				  if(pscm!=null) {
					  ProductSizeCategory psdObject = (ProductSizeCategory)VersionHelper.latestIterationOf(pscm);
					  logger.debug("latest psdObject:"+psdObject);
					  
					   psdObject = (ProductSizeCategory)VersionHelper.getVersion(pscm,"A");
					  logger.debug("A version psdObject:"+psdObject);
					  if(psdObject!=null) {
						  logger.debug("psdObject:"+psdObject+"::"+String.valueOf(psdObject.getValue("oldPSDMasterID")));
						  if(!(String.valueOf(psdObject.getValue("oldPSDMasterID")).equalsIgnoreCase(oldPSDMasterID))) {
							  psdObject.setValue("oldPSDMasterID", oldPSDMasterID);
							  LCSLogic.persist(psdObject, true);
							  logger.debug("psdObject persist complete");
						  }
						  Collection<ProductSizeCategory> psdObjColl=new Vector<ProductSizeCategory>();
						  psdObject.setSizeRange(template);
						  psdObjColl.add(psdObject);
						  if(season != null){
							  SizingHelper.service.addPSDsToSeason(psdObjColl,season);
							  logger.debug("psdObject added to Season");
						  }
					  }else { 
				  ProductSizeCategoryClientModel psccm = new ProductSizeCategoryClientModel();;
				  psccm.setBaseSize(nv.get("BaseSize").toString());
				  psccm.setBaseSize2(nv.get("BaseSize2").toString());
				  psccm.setSizeValues(nv.get("SizeValues").toString());
				  psccm.setSize2Values(nv.get("Size2Values").toString());
				  psccm.setSizeCategoryType(nv.get("SizeCategoryType").toString());
				  psccm.setFlexType(psType);
				  ProdSizeCategoryToSeasonClientModel pscscm = new ProdSizeCategoryToSeasonClientModel();
				  psccm.setMaster(pscm);
				  pscscm.setSizeValues(nv.get("SizeValues").toString());
				  pscscm.setSize2Values(nv.get("Size2Values").toString());
				  pscscm.setSizeCategoryMaster(pscm);
				  pscscm.setFlexType(psType);
				  psccm.setValue("oldPSDMasterID", oldPSDMasterID);
				  logger.debug("initial oldPSDMasterID set complete");
				  pscscm.copyState(pscscm);
				  if(season != null){
					pscscm.setSeasonMaster(season.getMaster());
					saveDetails(pscscm,psccm,status,nv.get("seasonName").toString());
					status = status + " created in season :" +nv.get("seasonName").toString()+".\n" ;
				  }else{
					  saveDetails(null,psccm,status,null);
					 // psccm.save();
					  status = status + " created.\n" ;
				  }
				  } 
			  }
		 
		 }
			  sb.append(status);
		  }
		 //System.out.println(sb.toString());
		 logger.debug(sb.toString());
	      return null;
	   }
	
	 
	 
		public static ProductSizeCategoryMaster getProductSizeCategoryByAtt(String oldPSDMasterID)
			      throws WTException
			   {
					FlexType siseDefinitionFlexType = FlexTypeCache.getFlexTypeFromPath("Size Definition");
					String oldMasterIDCol = siseDefinitionFlexType.getAttribute("oldPSDMasterID").getColumnDescriptorName();
					logger.debug("**oldMasterIDCol:"+oldMasterIDCol);
					ProductSizeCategory psdObject =null;
					ProductSizeCategoryMaster psdMasterObject = null;
					PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
		      		preparedquerystatement.appendFromTable(ProductSizeCategory.class);
					preparedquerystatement.appendSelectColumn("ProductSizeCategory", "IDA2A2");
		    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	ProductSizeCategory.class, oldMasterIDCol), oldPSDMasterID, "="));
		    		preparedquerystatement.addLatestIterationClause(ProductSizeCategory.class);
		            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
				   
				   logger.debug("**PSD Query:"+preparedquerystatement.toString()+"::"+searchresults.getResultsFound());
			      if (searchresults.getResultsFound()>0) {
			    	   FlexObject object =(FlexObject)  searchresults.getResults().get(0);
			    	   logger.debug("**object:"+object);
			    	   psdObject = (ProductSizeCategory)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.ProductSizeCategory:" +  object.getData("ProductSizeCategory.IDA2A2"));
			    	   psdMasterObject = psdObject.getMaster();
			    	   logger.debug("**Returning PSD Master:"+psdMasterObject.getPersistInfo().getObjectIdentifier().getId());
			    	   return psdMasterObject;
			      }
			      logger.debug("**Returning PSD Master as null");
			      return null;
			   }
	 
	public static LCSProduct getProduct(String number)
		      throws WTException
		   {
		      wt.method.MethodContext.getContext();

			   FlexType prodType = FlexTypeCache.getFlexTypeFromPath("Product");

	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable("LCSPRODUCT");

				preparedquerystatement.appendSelectColumn("LCSPRODUCT", "BRANCHIDITERATIONINFO");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, prodType.getAttribute("scPLMProductNo").getColumnDescriptorName()), number, "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, LCSProduct.LATEST_ITERATION), "1", "="));
	    		//System.out.println("Query ::" +preparedquerystatement.toString());
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
			   
			   
		      if (searchresults.getResultsFound()>0)
			  {
		    	 FlexObject object =(FlexObject)  searchresults.getResults().get(0);
				 LCSProduct product = (LCSProduct)LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" +  object.getData("LCSPRODUCT.BRANCHIDITERATIONINFO"));
				 //System.out.println("product ::" +product.toString());
				 return product;

		      }

		      return null;
		   }
	public static LCSSeason getSeason(String seasonName)
		      throws WTException
		   {
		      wt.method.MethodContext.getContext();
			   FlexType seasontype = FlexTypeCache.getFlexTypeFromPath("Season");
			   String seasonNameColDesc = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName").getColumnDescriptorName();
	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(LCSSeason.class);
				preparedquerystatement.appendSelectColumn("LCSSeason", "BRANCHIDITERATIONINFO");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, seasonNameColDesc), seasonName, "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, LCSSeason.LATEST_ITERATION), "1", "="));
	    		//System.out.println("Query ::" +preparedquerystatement.toString());
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
		      if (searchresults.getResultsFound()>0)
			  {
		    	  FlexObject object =(FlexObject)  searchresults.getResults().get(0);
				 LCSSeason season = (LCSSeason)LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:" +  object.getData("LCSSeason.BRANCHIDITERATIONINFO"));
				 //System.out.println("season ::" +season.toString());
				  return season;

		      }

		      return null;
		   }

	public static FullSizeRange getFullSizeRange(String FSRName)
		      throws WTException
		   {
	    		 wt.method.MethodContext.getContext();
				PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(FullSizeRange.class);
				preparedquerystatement.appendSelectColumn("FullSizeRange", "IDa2A2");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	FullSizeRange.class, "name"), FSRName, "="));

	    		//System.out.println("Query ::" +preparedquerystatement.toString());
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
		      if (searchresults.getResultsFound()>0)
			  {
		    	  FlexObject object =(FlexObject)  searchresults.getResults().get(0);
				 FullSizeRange fsr = (FullSizeRange)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.FullSizeRange:" +  object.getData("FullSizeRange.IDA2A2"));
				  return fsr;

		      }

		      return null;
		   }	
	

	public static SizeCategory getSizeCategory(String sizeCategoryName)
		      throws WTException
		   {
	    		 wt.method.MethodContext.getContext();
				PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(SizeCategory.class);
				preparedquerystatement.appendSelectColumn("SizeCategory", "IDA2A2");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	SizeCategory.class, "name"), sizeCategoryName, "="));

	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
			   
			   
		      if (searchresults.getResultsFound()>0)
			  {
		    	   FlexObject object =(FlexObject)  searchresults.getResults().get(0);
				 SizeCategory sr = (SizeCategory)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.SizeCategory:" +  object.getData("SizeCategory.IDA2A2"));
				  return sr;
		      }

		      return null;
		   }	

	public static void getAllFullSizeRange()
		      throws WTException
		   {
	    		 wt.method.MethodContext.getContext();
				PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(FullSizeRange.class);
				preparedquerystatement.appendSelectColumn("FullSizeRange", "IDa2A2");
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
			   
			   int c= searchresults.getResultsFound();
		        for (int i=0; i<c ; i++)
			     {
		    	  FlexObject object =(FlexObject)  searchresults.getResults().get(i);
				 FullSizeRange fsr = (FullSizeRange)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.FullSizeRange:" +  object.getData("FullSizeRange.IDA2A2"));
				allFullSizRange.put(fsr.getName(),fsr);

		      }
		        
		        logger.debug("allFullSizRange:"+allFullSizRange);
		   }

	public static void getAllSizeCategory()
		      throws WTException
		   {
	    		 wt.method.MethodContext.getContext();
				PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(SizeCategory.class);
				preparedquerystatement.appendSelectColumn("SizeCategory", "IDA2A2");
	    		preparedquerystatement.setDistinct(true);
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
				int c= searchresults.getResultsFound();
		        for (int i=0; i<c ; i++)
			     {
		    	   FlexObject object =(FlexObject)  searchresults.getResults().get(i);
				 SizeCategory sr = (SizeCategory)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.SizeCategory:" +  object.getData("SizeCategory.IDA2A2"));
				 allSizCat.put(sr.getName(), sr);
		      }
		        
		        logger.debug("allSizCat:"+allSizCat);
		   }
	public static void getAllTemplates()
		      throws WTException
		   {
		      wt.method.MethodContext.getContext();
	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(ProductSizeCategoryMaster.class);
				preparedquerystatement.appendSelectColumn("ProductSizeCategoryMaster", "IDA2A2");
				preparedquerystatement.appendSelectColumn("ProductSizeCategoryMaster", "NAME");
	    		preparedquerystatement.setDistinct(true);
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
				int c= searchresults.getResultsFound();
		        for (int i=0; i<c ; i++)
			     {
		    	  FlexObject object =(FlexObject)  searchresults.getResults().get(i);
				 ProductSizeCategoryMaster pscm = (ProductSizeCategoryMaster)LCSQuery.findObjectById("OR:com.lcs.wc.sizing.ProductSizeCategoryMaster:" +  object.getData("ProductSizeCategoryMaster.IDA2A2"));
				 allTemplates.put(object.getData("PRODUCTSIZECATEGORYMASTER.NAME"), pscm);
		      }
		        
		        logger.debug("allTemplates:"+allTemplates);
		   }
	public static void getAllSeason()
		      throws WTException
		   {
		      wt.method.MethodContext.getContext();
			   //String seasonNameColDesc = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName").getColumnDescriptorName();
	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	      		preparedquerystatement.appendFromTable(LCSSeason.class);
				preparedquerystatement.appendSelectColumn("LCSSeason", "BRANCHIDITERATIONINFO");
	    		preparedquerystatement.setDistinct(true);
	            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
				int c= searchresults.getResultsFound();
		        for (int i=0; i<c ; i++)
			     {
		    	  FlexObject object =(FlexObject)  searchresults.getResults().get(i);
				 LCSSeason season = (LCSSeason)LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:" +  object.getData("LCSSEASON.BRANCHIDITERATIONINFO"));
				 allSeasons.put(season.getValue("seasonName"), season);
		      }
		        
		        logger.debug("allSeasons:"+allSeasons);
		   }
	
	public static void main(String[] argv) throws WTPropertyVetoException, WTException {
		//LoadCommon.SERVER_MODE = false;
		ArrayList al =new ArrayList();
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		String username = "wcadmin";
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser(username);
		wt.method.MethodContext mc = new wt.method.MethodContext("utility", rms);

		String value = null;
		String[][] tableValues = LoadCommon.getValuesFromFile(argv[0], "\t");
		wt.method.MethodContext.getContext();
		for (int i = 0; i < tableValues.length; ) {
			Hashtable<String, String> dataValues = new Hashtable();
			String newVal = tableValues[i][0];
			//String[] st = newVal.split("^");
			StringTokenizer st = new StringTokenizer(newVal,"^");
			value = st.nextToken();
			dataValues.put("seasonName",value);
			value = st.nextToken();
			dataValues.put("productNumber",value);
			value = st.nextToken();
		    dataValues.put("scName",value);
			value = st.nextToken();
		    dataValues.put("fsrName",value);
			value = st.nextToken();
			dataValues.put("scmName",value);
			value = st.nextToken();
		    dataValues.put("SizeValues",value);
			value = st.nextToken();
		    dataValues.put("Size2Values",value);
			value = st.nextToken();
			dataValues.put("BaseSize",value);
			value = st.nextToken();
		    dataValues.put("BaseSize2",value);
			value = st.nextToken();
			dataValues.put("SizeCategoryType",value);
			value = st.nextToken();
			dataValues.put("TemplateID",value);
			value = st.nextToken();
			dataValues.put("PSDID",value);
			value = st.nextToken();
			dataValues.put("PSDtoSeasonID",value);
		    al.add(dataValues);
			i++;
		}
		
		try {
		@SuppressWarnings("rawtypes")
			Class[] argsTypes = { ArrayList.class };
			Object[] argsVal = { al };
			rms.invoke("createProductSizeCategory", SPARCLoadProductSizing21.class.getName(), null, argsTypes, argsVal);
		} catch (final Exception ex) {
			ex.getLocalizedMessage();
		}
		System.exit(0);
	}
}
