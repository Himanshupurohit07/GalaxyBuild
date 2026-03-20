package com.acn.migration;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.jena.sparql.function.library.leviathan.log;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSLogicHelper;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonProductLinkPlugins;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.ChangedAttributeValue;
import com.lcs.wc.util.SynchronizedAttributeHelper;
import com.lcs.wc.util.VersionHelper;
import com.ptc.windchill.contextBuilder.util.PersistableHelper;
import com.sparc.wc.util.SparcConstants;

import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class GlobalMsrpLoadingUtility implements RemoteAccess, Serializable  {
	public static void main(String[] args) throws RemoteException, InvocationTargetException {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		String remoteUser = args[0]; 
		String remotePass = args[1];
		server.setUserName(remoteUser);
		server.setPassword(remotePass);
		//new MethodContext("", server);
		System.out.println("remoteUser----"+remoteUser+"--");
		System.out.println("remotePass----"+remotePass+"--");
		String FilePath  = args[2];
		System.out.println("filePath==============="+FilePath);
		Class[] argTypes = {String.class};
		Object[] args1 = {FilePath};
		System.out.println("Start Export of "+FilePath+" : "
				+ new Date(System.currentTimeMillis()));
		server.invoke("initializeExport", "com.acn.migration.GlobalMsrpLoadingUtility", null, argTypes, args1);
		System.out.println("Finish Export of "+FilePath+" : "
				+ new Date(System.currentTimeMillis()));

	}
	public static void initializeExport(String FilePath) throws IOException, Exception {
		try (FileInputStream fis = new FileInputStream(FilePath);
				Workbook workbook = new XSSFWorkbook(fis)) {

			// Always read first sheet
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {

				// Skip header row
				if (row.getRowNum() == 0) {
					continue;
				}

				String col0 = getCellValue(row.getCell(0));
				//String col1 = getCellValue(row.getCell(1));
				String col2 = getCellValue(row.getCell(1));
				String col3 = getCellValue(row.getCell(2));
				///String col4 = getCellValue(row.getCell(4));

				ProductSeasonLevelAttribute(col0, col2, col3);
				//colorwaySeasonLevelAttribute(col1, col2, col4);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String getCellValue(Cell cell) {if (cell == null) return "";

	if (cell == null) return "";

    switch (cell.getCellType()) {

        case STRING:
            return cell.getStringCellValue();

        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toString(); // optional
            }

            double value = cell.getNumericCellValue();

            // If no decimal part, return as integer string
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }

            return String.valueOf(value);

        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());

        default:
            return "";
    }
	}

	private static void ProductSeasonLevelAttribute(String productNo,String seasonName, String c3) throws WTException, WTPropertyVetoException {
		System.out.println("Method1 -> " + productNo + ", " + seasonName + ", " + c3);
		
			 LCSSeasonProductLink productSeasonLink =
					  findProductSeasonLinkByNumber(productNo, seasonName);
			LCSProduct product = findProductByNumber(productNo);
			 Iterator<LCSSKU> skusItr = LCSSKUQuery.findSKUs(product).iterator();
		        while(skusItr.hasNext()){
		            LCSSKU currentSku = (LCSSKU)skusItr.next();
		           System.out.println("skunumber===="+currentSku.getValue("scColorwayNo").toString()); 
		           String Colorway=currentSku.getValue("scColorwayNo").toString();
		        	   LCSSKUSeasonLink SKUSEASON=  (LCSSKUSeasonLink) findSkuSeasonLinkByNumber(Colorway,seasonName);	
		          if(SKUSEASON !=null) {
				SKUSEASON.setValue("gxGlobalMSRP", c3); 
	         		productSeasonLink.setValue("gxGlobalMSRP",c3); 
					  productSeasonLink = (LCSSeasonProductLink) PersistenceHelper.manager.save(productSeasonLink);
					 
					  updateLCSSKUSeasonLinksSyncdAttributes(productSeasonLink) ;
					  SKUSEASON = (LCSSKUSeasonLink) PersistenceHelper.manager.save(SKUSEASON);
				SKUSEASON = (LCSSKUSeasonLink) PersistenceHelper.manager.refresh(SKUSEASON);
		          }
		           
		        }
					  //productSeasonLink=(LCSProductSeasonLink) PersistenceHelper.manager.refresh(productSeasonLink);
					  //LCSSeasonProductLinkPlugins.updateLCSSKUSeasonLinksSyncdAttributes(productSeasonLink);
					 // productSeasonLink = (LCSSeasonProductLink) LCSLogic.persist(productSeasonLink,true); 
					  //LCSSKUSeasonLink skuSeason=
					 // findSkuSeasonLinkByNumber(colorway, seasonName);
					//  skuSeason.setValue("gxGlobalMSRP",c3);
					  //System.out.println("gm1-----before-----------------"+gm1); skuSeason =
					  //(LCSSKUSeasonLink) LCSLogicHelper.service.save(skuSeason);
					 // productSeasonLink=(LCSSeasonProductLink) PersistenceHelper.manager.save(productSeasonLink);
					  //skuSeason=(LCSSKUSeasonLink) PersistenceHelper.manager.refresh(skuSeason);
					  //gm1= (Double) skuSeason.getValue("gxGlobalMSRP");
					 // System.out.println("colorway msrp======================="+skuSeason.getValue(
					 // "gxGlobalMSRP"));
					  System.out.println("**setting value::"+"globalMsrp"+":::"+c3);	 
		
		
		
		 
	}

	private static void updateLCSSKUSeasonLinksSyncdAttributes(LCSSeasonProductLink object) throws WTPropertyVetoException, WTException {
		// TODO Auto-generated method stub

/* 37 */       System.out.println("updateLCSSKUsSyncdAttributes():  start");
/*    */     
/* 39 */     if (!(object instanceof LCSProductSeasonLink)) {
/* 40 */       throw new WTException("LCSProductPlugins.updateLCSSKUsSyncdAttributes: object must be an LCSProductSeasonLink");
/*    */     }
/* 42 */     LCSProductSeasonLink newLink = (LCSProductSeasonLink)object;
/* 43 */     int effectSequence = newLink.getEffectSequence();
/* 44 */     boolean cpcomv = false;
/* 45 */     if (newLink.getCopiedFrom() != null || newLink.getCarriedOverFrom() != null || newLink.getMovedFrom() != null) {
/* 46 */       cpcomv = true;
/*    */     }
/* 48 */     if (effectSequence == 0 || (effectSequence == 0 && cpcomv)) {
/*    */       return;
/*    */     }
/*    */     
/* 52 */     Collection<FlexTypeAttribute> syncAtts = newLink.getFlexType().getAllAttributes("PRODUCT-SEASON", "PRODUCT-SKU", true);
/*    */ 
/*    */     
/* 55 */     if (!syncAtts.isEmpty()) {
/* 56 */       FlexTyped oldIt = LCSSeasonQuery.findSeasonProductLink(newLink, newLink
/* 57 */           .getEffectSequence() - 1);
/*    */       try {
/* 62 */         Collection<ChangedAttributeValue> ACTs = SynchronizedAttributeHelper.getChangedValues(newLink, oldIt, syncAtts);
/*    */         
/* 64 */           
/* 68 */         if (!ACTs.isEmpty()) {
/* 69 */           Collection<FlexTyped> skus = LCSSeasonProductLinkPlugins.findSKULinks(newLink);
/* 70 */          
/* 71 */             System.out.println("updateLCSSKUsSyncdAttributes():skus.size():  " + skus.size());
/*    */           
/*    */           
/* 74 */           WTArrayList updatedSkus = SynchronizedAttributeHelper.setSKULevelValues(ACTs, skus);
/*    */           
/* 76 */           if (!updatedSkus.isEmpty()) {
/* 77 */           
/* 78 */               System.out.println("Updating " + updatedSkus.size() + " skus.");
/*    */             
/* 80 */             PersistenceServerHelper.manager.update(updatedSkus);
/*    */           } 
/*    */         } 
/*    */       } finally {
/* 84 */         SessionServerHelper.manager.setAccessEnforced(SessionServerHelper.manager.setAccessEnforced(false));
/*    */       } 
/*    */     } 
/* 87 */    
/* 88 */       System.out.println("updateLCSSKUsSyncdAttributes():  end");
/*    */     
/*    */   
		
	}
	public static LCSSeasonProductLink findProductSeasonLinkByNumber(String productNo, String seasonName)
			throws WTException {
		System.out.println("productNo :: " + productNo);
		System.out.println("seasonName :: " + seasonName);
		LCSProduct product = findProductByNumber(productNo);
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSEASON");
		stmt.appendSelectColumn(new QueryColumn("LCSSEASON", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSEASON", "ptc_str_4typeInfoLCSSeason", seasonName, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Season").getAttribute("seasonName").getColumnName();
		stmt.appendCriteria(new Criteria("LCSSEASON", attColumn , seasonName, Criteria.EQUALS));
		System.out.println(" expected : ptc_str_4typeInfoLCSSeason : " + attColumn);
		stmt.appendAnd();
		stmt.appendCriteria(new Criteria("LCSSEASON", "LATESTITERATIONINFO", "1", "="));
		LCSQuery.printStatement(stmt);
		System.out.println("stmt :: " + stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSEASON.IDA2A2");
			LCSSeason sea = (LCSSeason) LCSQuery.findObjectById("com.lcs.wc.season.LCSSeason:" + objId);
			LCSSeasonProductLink seasonProdLink = LCSSeasonQuery.findSeasonProductLink(product, sea);
			return seasonProdLink;
		}

		return null;
	}
	public static LCSSKUSeasonLink findSkuSeasonLinkByNumber(String colorwayNo, String seasonName) throws WTException {
		System.out.println("colorwayNo :: " + colorwayNo);
		System.out.println("seasonName :: " + seasonName);

		LCSSKU colorway = findColorWayByNumber(colorwayNo);
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSEASON");
		stmt.appendSelectColumn(new QueryColumn("LCSSEASON", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSSEASON", "ptc_str_4typeInfoLCSSeason", seasonName, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Season").getAttribute("seasonName").getColumnName();
		System.out.println(" expected : ptc_str_4typeInfoLCSSeason : "+attColumn);
		stmt.appendCriteria(new Criteria("LCSSEASON",attColumn, seasonName, Criteria.EQUALS));
		stmt.appendAnd();
		stmt.appendCriteria(new Criteria("LCSSEASON", "LATESTITERATIONINFO", "1", "="));
		LCSQuery.printStatement(stmt);
		System.out.println("stmt :: " + stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSEASON.IDA2A2");
			LCSSeason sea = (LCSSeason) LCSQuery.findObjectById("com.lcs.wc.season.LCSSeason:" + objId);
			LCSSKUSeasonLink seasonSKULink = (LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(colorway, sea);
			return seasonSKULink;
		}

		return null;

	}
	private static LCSSKU findColorWayByNumber(String colorwayNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSSKU");
		stmt.appendSelectColumn(new QueryColumn("LCSSKU", "IDA2A2"));
		// 	stmt.appendCriteria(new Criteria("LCSSKU", "ptc_lng_2typeInfoLCSSKU", colorwayNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("scColorwayNo").getColumnName();
		System.out.println(" expected : ptc_lng_2typeInfoLCSSKU : " + attColumn);
		stmt.appendCriteria(new Criteria("LCSSKU", attColumn , colorwayNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);
		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSSKU.IDA2A2");
			LCSSKU colorway = (LCSSKU) LCSQuery.findObjectById("com.lcs.wc.product.LCSSKU:" + objId);
			return (LCSSKU) VersionHelper.latestIterationOf(colorway);

		}
		return null;
	}
	public static LCSProduct findProductByNumber(String productNo) throws WTException {
		PreparedQueryStatement stmt = new PreparedQueryStatement();
		stmt.appendFromTable("LCSProduct");
		stmt.appendSelectColumn(new QueryColumn("LCSProduct", "IDA2A2"));
		//stmt.appendCriteria(new Criteria("LCSProduct", "ptc_lng_2typeInfoLCSProduct", productNo, Criteria.EQUALS));
		String attColumn = FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("scPLMProductNo").getColumnName();
		System.out.println( "expected : ptc_lng_2typeInfoLCSProduct : "+ attColumn);
		stmt.appendCriteria(new Criteria("LCSProduct", attColumn , productNo, Criteria.EQUALS));

		LCSQuery.printStatement(stmt);

		Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
		if (!results.isEmpty()) {
			FlexObject fo = (FlexObject) results.iterator().next();
			String objId = fo.getString("LCSPRODUCT.IDA2A2");
			LCSProduct prod = (LCSProduct) LCSQuery.findObjectById("com.lcs.wc.product.LCSProduct:" + objId);
			return (LCSProduct) VersionHelper.latestIterationOf(prod);
		}
		return null;
	}

}
