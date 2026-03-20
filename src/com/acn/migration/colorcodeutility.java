package com.acn.migration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import org.apache.jena.sparql.function.library.leviathan.log;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSLogicHelper;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.util.SparcConstants;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;

public class colorcodeutility implements RemoteAccess, Serializable  {
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
		//System.out.println("filePath==============="+FilePath);
		Class[] argTypes = {String.class};
		Object[] args1 = {FilePath};
		//System.out.println("Start Export of "+FilePath+" : "
			//+ new Date(System.currentTimeMillis()));
			server.invoke("initializeExport", "com.acn.migration.colorcodeutility", null, argTypes, args1);
			//System.out.println("Finish Export of "+FilePath+" : "
			//	+ new Date(System.currentTimeMillis()));
	}
public static void initializeExport(String FilePath) throws IOException, Exception {


		System.out.println("exporttype ====== " + FilePath);
		try (FileInputStream fis = new FileInputStream(FilePath);
	             Workbook workbook = new XSSFWorkbook(fis)) {
			String PRODUCT_BRANCHID = "LCSPRODUCT.BRANCHIDITERATIONINFO";
			String SEASON_BRANCHID = "LCSSEASON.BRANCHIDITERATIONINFO";
	            Sheet sheet = workbook.getSheetAt(0);
	        	LCSProduct productObj = null;
	        	LCSSeason seasonObj = null;
	            // Iterate through all rows (skip header row at index 0)
	        	DataFormatter formatter = new DataFormatter();
	            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	                Row row = sheet.getRow(i);
	                if (row != null) {
	                	  String colorway = formatter.formatCellValue(row.getCell(0));;
	                	//String seasonName = getCellValue(row.getCell(2));
	                   // String productName = getCellValue(row.getCell(1));
	                  
	                    String galaxyColorcode = formatter.formatCellValue(row.getCell(1));
	                    String licenseeRef = formatter.formatCellValue(row.getCell(2));
	                   // FlexObject productFlexObj = null;
	                    // Print values for this row
	                    //System.out.println("Row " + i + ": "
	                    		
	                          //  + "Colorway = " + colorway + ", "
	                           // + "Galaxy Colorcode = " + galaxyColorcode + ", "
	                          //  + "Licensee Ref Number = " + licenseeRef);
	                  //  LCSProduct prd =new LCSProductQuery().findProductByNameType(productName, null);
	                	//System.out.println("productObject"+prd.getName());
	                	LCSSKU sku = findColorWayByNumber(colorway);
	                	System.out.println("colorwayName---------------"+sku.getName());
	                	sku.setValue("gxGalaxyColorCode",galaxyColorcode.toString());
	                	sku.setValue("sclicenserefnumber",licenseeRef.toString());
	                	sku = (LCSSKU) LCSLogicHelper.service.save(sku);
	                	System.out.println("**setting value::"+"gxGalaxyColorCode"+":::"+galaxyColorcode);
	                    
	                }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public static LCSSKU findColorWayByNumber(String colorway) throws WTException {
	    	PreparedQueryStatement stmt = new PreparedQueryStatement();
			stmt.appendFromTable("LCSSKU");
			stmt.appendSelectColumn(new QueryColumn("LCSSKU", "IDA2A2"));
		// 	stmt.appendCriteria(new Criteria("LCSSKU", "ptc_lng_2typeInfoLCSSKU", colorwayNo, Criteria.EQUALS));
			String attColumn = FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("scColorwayNo").getColumnName();
			//System.out.println(" expected : ptc_lng_2typeInfoLCSSKU : " + attColumn);
			stmt.appendCriteria(new Criteria("LCSSKU", attColumn , colorway, Criteria.EQUALS));

			LCSQuery.printStatement(stmt);
			Collection<?> results = LCSQuery.runDirectQuery(stmt).getResults();
			if (!results.isEmpty()) {
				FlexObject fo = (FlexObject) results.iterator().next();
				String objId = fo.getString("LCSSKU.IDA2A2");
				LCSSKU colorwayObj = (LCSSKU) LCSQuery.findObjectById("com.lcs.wc.product.LCSSKU:" + objId);
				return (LCSSKU) VersionHelper.latestIterationOf(colorwayObj);

			}
			return null;
}
		// Helper method to safely get cell values as String
	    private static String getCellValue(Cell cell) {
	        if (cell == null) return "";
	        switch (cell.getCellType()) {
	            case STRING: return cell.getStringCellValue();
	            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
	            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
	            default: return "";
	        }
	    }
}

