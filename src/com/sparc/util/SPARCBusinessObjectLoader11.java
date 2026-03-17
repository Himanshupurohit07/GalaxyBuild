package com.sparc.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.Logger;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLifecycleManagedClientModel;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadLifecycleManaged;
import com.lcs.wc.load.LoadLinePlan;
import com.lcs.wc.util.FormatHelper;

import wt.fc.WTObject;
import wt.httpgw.GatewayAuthenticator;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.queue.ProcessingQueue;
import wt.queue.QueueEntry;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class SPARCBusinessObjectLoader11 implements RemoteAccess {

    final static Logger LOGGER = LogR.getLogger(SPARCBusinessObjectLoader11.class.getName());

    public static void main(String[] args) {

        try {
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            String username = "wcadmin";
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser(username);
            remoteMethodServer.setAuthenticator(auth);
            String arg1 = args[0];
            String arg2 = args[1];
            String arg3 = args[2];
            Object[] objArgs = { arg1, arg2,arg3 };
            Class[] classArr = { String.class, String.class,String.class };
            if (remoteMethodServer != null) {
                remoteMethodServer.invoke("loadBusinessObject", "com.sparc.util.SPARCBusinessObjectLoader11", null,
                        classArr, objArgs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug(e.getLocalizedMessage());
        }
    }

    public static void loadBusinessObject(String fileName, String count,String isCheck) throws IOException {

        LOGGER.debug("**loadBusinessObject start");

        Integer g = Integer.valueOf(count);
        LOGGER.debug("**Queues will be created multiples of:" + g);
        LOGGER.debug("**loadBusinessObject start");
        BufferedReader br = null;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = inputFormat.parse("1-Jan-2023");
            String dateValue = String.valueOf(new Timestamp(date.getTime()));
            LOGGER.debug("**dateValue:" + dateValue);

            Boolean firstVar = Boolean.parseBoolean(isCheck);

            String sCurrentLine = "";

            String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
            String dirSep = WTProperties.getLocalProperties().getProperty("dir.sep");
            String inputFileLocation = wtHome + dirSep + fileName;
            LOGGER.debug("**inputFileLocation:" + inputFileLocation);
            br = new BufferedReader(new FileReader(inputFileLocation));
            ProcessingQueue queue = QueueHelper.manager.getQueue("SPARCLoaderPoolQueue");
            QueueHelper.manager.enableThreadPoolExecution(queue);
            int i = 0;
            int j = 0;
            ArrayList<String> arrayList = new ArrayList<String>();
            while ((sCurrentLine = br.readLine()) != null) {

                i++;
                arrayList.add(sCurrentLine);
                if ((j = i % g) == 0) {
                    QueueEntry entry = queue.addEntry(SessionHelper.getPrincipal(), "loadHTSCode",
                            SPARCBusinessObjectLoader11.class.getName(), new Class[] { ArrayList.class, String.class,Boolean.class },
                            new Object[] { arrayList, dateValue ,firstVar});
                    LOGGER.debug("**Queue created:" + entry.getEntryNumber());
                    arrayList = new ArrayList<String>();
                }
            }

            // ✅ Handle remaining lines (if any)
            if (!arrayList.isEmpty()) {
                QueueEntry entry = queue.addEntry(SessionHelper.getPrincipal(), "loadHTSCode",
                        SPARCBusinessObjectLoader11.class.getName(), new Class[] { ArrayList.class, String.class,Boolean.class },
                        new Object[] { arrayList, dateValue,firstVar });
                LOGGER.debug("**Queue created (last batch):" + entry.getEntryNumber());
            }
            /*
             * scStartDate scHTSCode scCountryOfOrigin scDestinationCountry scDutyPercent
             * scTariffPercent scFlatDutyDollar
             */
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug("**Error:" + e.getLocalizedMessage());
        }

        finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static void loadHTSCode(ArrayList<String> arrayList, String date,Boolean isVar) throws WTException {
        HashMap<String, LCSCountry> countryNameObjectMap = prepareCountryMap();

        LCSCountry destCountry = (LCSCountry) countryNameObjectMap.get("USA");

        LOGGER.debug("loadHTSCode:" + arrayList + "^^^" + date + "^^^");
        Iterator<String> stringIterator = arrayList.iterator();
        String temp = "";

        FlexType flextype = FlexTypeCache.getFlexTypeFromPath("Business Object\\scHTSLookUpTable");
        LOGGER.debug("**flextype:" + flextype);
        while (stringIterator.hasNext()) {

            try {
                temp = stringIterator.next();
                String[] array = temp.split("~", 9);
                String endDate = array[6];
                String startDate = array[7];
                LCSLifecycleManagedClientModel businessObj = new LCSLifecycleManagedClientModel();

                load(array, businessObj, flextype,isVar);

                // LCSLifecycleManaged businessObj =
                // LCSLifecycleManaged.newLCSLifecycleManaged();
                businessObj.setFlexType(flextype);
                if (!startDate.equals("NA")) {
                    businessObj.setValue("scStartDate", startDate);
                }

                if (!endDate.equals("NA")) {
                    businessObj.setValue("scEndDate", endDate);
                }

                businessObj.setValue("scHTSCode", array[0]);
                businessObj.setValue("scCountryOfOrigin", countryNameObjectMap.get(array[1].toUpperCase()));
                if(isVar) {
                    if(FormatHelper.hasContent(array[8])) {
                        businessObj.setValue("scDestinationCountry", destCountry);
                    }
                }
                if (array[3] != null && FormatHelper.hasContentAllowZero(array[3]))
                    businessObj.setValue("scTariffPercent", array[3]);
                if (array[2] != null && FormatHelper.hasContentAllowZero(array[2]))
                    businessObj.setValue("scDutyPercent", array[2]);
                if (array[4] != null && FormatHelper.hasContentAllowZero(array[4]))
                    businessObj.setValue("scFlatDutyDollar", array[4]);

                // businessObj.setLifecycleManagedName(array[5]);
                // new LCSLifecycleManagedLogic().persist(businessObj,true);
                //businessObj.set
                businessObj.save();
                LOGGER.debug("businessObj saved");

            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.debug("**FATAL:Error when trying to create data for:" + temp + e.getLocalizedMessage());
                continue;
            }

        }
    }

    private static void load(String[] array, LCSLifecycleManagedClientModel businessObj, FlexType flextype,Boolean isVar)
            throws Exception {
        if (array == null) {
            throw new IllegalArgumentException("Input array must have at least 8 elements.");
        }

        //System.out.println("Starting to process input array: " + Arrays.toString(array));

        String htsCode = array[0];
        String countryOfOrigin = array[1] != null ? array[1] : "";
        String destinationCountry = "USA";

        //System.out.println("HTS Code: " + htsCode);
        //System.out.println("Country of Origin: " + countryOfOrigin);
        //System.out.println("Destination Country: " + destinationCountry);

        String endDate = "NA".equalsIgnoreCase(array[6]) ? "" : formatDate(array[6]);
        String startDate = "NA".equalsIgnoreCase(array[7]) ? "" : formatDate(array[7]);

        //System.out.println("Start Date: " + startDate);
        //System.out.println("End Date: " + endDate);

        String dateRange= startDate + " to " + endDate;
        //String dateRange = (!startDate.isEmpty() || !endDate.isEmpty()) ? startDate + " to " + endDate : "";

        //System.out.println("Date Range: " + dateRange);

        String fullName = "";

        if(isVar){
            fullName = String.join(" ", htsCode, countryOfOrigin, destinationCountry, dateRange).trim();
        }else{
            fullName = htsCode;
        }
        //System.out.println("Generated Full Name: " + fullName);

        String oid = getByName(flextype, fullName);
        //System.out.println("OID retrieved from getByName: " + oid);

        if (oid != null) {
            businessObj.load(oid);
           // System.out.println("Business object loaded successfully with OID: " + oid);
        } else {
            LOGGER.info("No matching OID found for: " + fullName);
        }

        //System.out.println("Completed processing for array.");
    }

    public static String formatDate(String date) {
        // Parse the input string to LocalDateTime
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);

        // Format it to the desired output format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(outputFormatter);

    }

    public static String getByName(FlexType flexType, String name) throws WTException {
        PreparedQueryStatement statement = new PreparedQueryStatement();
        String tableName = "LCSLifecycleManaged";
        statement.appendFromTable(tableName);

        statement.appendSelectColumn(tableName, "idA2A2");
        statement.appendSelectColumn(tableName, "classnameA2A2");
        String flextypeId = flexType.getIdPath();
        flextypeId = flextypeId + "%";

        statement.appendCriteria(new Criteria(tableName, "flexTypeIdPath", "?", "LIKE"), flextypeId);

        FlexTypeAttribute ptcmaterialNameType = flexType.getAttribute("name");

        statement.appendAnd();
        statement.appendCriteria(new Criteria(tableName, ptcmaterialNameType.getColumnName(), name, Criteria.EQUALS));
        LoadCommon.display(statement.toString());

        Vector<?> results = LCSQuery.runDirectQuery(statement).getResults();
        if (results != null && !results.isEmpty()) {
            if (results.size() > 1) {
                throw new WTException("name comes with more than 1 result");
            }
            FlexObject obj = (FlexObject) results.elementAt(0);
            return "OR:" + obj.getString(tableName + ".CLASSNAMEA2A2") + ":" + obj.getString(tableName + ".IDA2A2");
        }

        return null;
    }

    public static HashMap<String, LCSCountry> prepareCountryMap() {

        HashMap<String, LCSCountry> localMap = null;

        try {

            LCSCountry country = null;
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSCountry.class);
            statement.appendSelectColumn(new QueryColumn(LCSCountry.class, "thePersistInfo.theObjectIdentifier.id"));
            statement
                    .appendCriteria(new Criteria(new QueryColumn(LCSCountry.class, "checkoutInfo.state"), "wrk", "<>"));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSCountry.class, "iterationInfo.latest"), "1", "="));
            LOGGER.debug("*statement:" + statement);
            SearchResults results = LCSQuery.runDirectQuery(statement);
            LOGGER.debug("*Results Found:" + results.getResultsFound());
            if (results.getResultsFound() > 0) {
                localMap = new HashMap<String, LCSCountry>();
                Collection<FlexObject> resultSet = (Collection) results.getResults();
                for (FlexObject fo : resultSet) {
                    country = (LCSCountry) LCSQuery
                            .findObjectById("OR:com.lcs.wc.country.LCSCountry:" + (String) fo.get("LCSCOUNTRY.IDA2A2"));
                    localMap.put(country.getName().toUpperCase(), country);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug("**Exception when tried to get Country:" + e.getLocalizedMessage());

        }
        LOGGER.debug("**localMap:" + localMap);

        return localMap;

    }

    public static void testmethodPreUpdatePersist(WTObject wtObject) {
        LOGGER.debug("**testmethodPreUpdatePersist:" + wtObject);
    }

    public static void testmethodPrePersist(WTObject wtObject) {
        LOGGER.debug("**testmethodPrePersist:" + wtObject);
    }

    public static void testmethodPreCreatePersist(WTObject wtObject) {
        LOGGER.debug("**testmethodPreCreatePersist:" + wtObject);
    }
}
