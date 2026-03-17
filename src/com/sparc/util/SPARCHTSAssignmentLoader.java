package com.sparc.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.lcs.wc.db.*;
import com.lcs.wc.foundation.*;
import org.apache.logging.log4j.Logger;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
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

public class SPARCHTSAssignmentLoader implements RemoteAccess{

    private static final String BO_OBJECT_IDENTIFIER_LITERAL ="thePersistInfo.theObjectIdentifier.id";


    final static Logger LOGGER = LogR.getLogger(SPARCHTSAssignmentLoader.class.getName());
    public static void main(String[] args) {

        try {
            RemoteMethodServer remoteMethodServer = RemoteMethodServer.getDefault();
            String username ="wcadmin";
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser(username);
            remoteMethodServer.setAuthenticator(auth);
            String arg1 = args[0];
            String arg2 = args[1];
            Object[] objArgs = {arg1,arg2};
            Class[] classArr = {String.class, String.class};
            if(remoteMethodServer != null) {
                remoteMethodServer.invoke("loadBusinessObject", "com.sparc.util.SPARCHTSAssignmentLoader", null, classArr, objArgs);
            }

        }catch (Exception e) {
            e.printStackTrace();
            LOGGER.debug(e.getLocalizedMessage());
        }
    }
    public static void loadBusinessObject(String fileName, String count) throws IOException {

        LOGGER.debug("**loadBusinessObject start");

        Integer g = Integer.valueOf(count);
        LOGGER.debug("**Queues will be created multiples of:"+g);
        LOGGER.debug("**loadBusinessObject start");
        BufferedReader br = null;
        try {

            String sCurrentLine = "";

            String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
            String dirSep = WTProperties.getLocalProperties().getProperty("dir.sep");
            String inputFileLocation = wtHome + dirSep +fileName;
            LOGGER.debug("**inputFileLocation:"+inputFileLocation);
           // br = new BufferedReader(new FileReader(inputFileLocation));
            br = new BufferedReader(new FileReader(inputFileLocation, StandardCharsets.UTF_8));
            ProcessingQueue queue = QueueHelper.manager.getQueue("SPARCLoaderPoolQueue");
            QueueHelper.manager.enableThreadPoolExecution(queue);
            int i = 0;
            int j = 0;
            List<String> arrayList = new ArrayList<String>();
            while((sCurrentLine = br.readLine()) != null) {
                i++;
                arrayList.add(sCurrentLine);
                if((j = i % g) == 0){
                    QueueEntry entry = queue.addEntry(SessionHelper.getPrincipal(), "loadHTSAssignment", SPARCHTSAssignmentLoader.class.getName(), new Class[] {List.class}, new Object[] {arrayList});
                    LOGGER.debug("**Queue Entry created:"+entry.getEntryNumber());
                    arrayList = new ArrayList<String>();
                }
            }

        } catch(Exception e ) {
            e.printStackTrace();
            LOGGER.debug("**Error:"+e.getLocalizedMessage());
        }

        finally {
            if(br!=null) {
                br.close();
            }
        }
    }


    public static void loadHTSAssignment(List<String> arrayList) throws WTException {

        FlexType flextype = FlexTypeCache.getFlexTypeFromPath("Business Object\\scHTSAssignmetTable");
        for(String temp:arrayList){

            try {
                String[] array = temp.split("~",2);
                LCSLifecycleManaged test = getBusinessObjectByName(flextype,array[0]);
				System.out.println("test----------------"+test);
                if(test == null){
                LCSLifecycleManagedClientModel businessObj = new LCSLifecycleManagedClientModel();
                businessObj.setFlexType(flextype);
                businessObj.setValue("scHTSCodeAssignment",array[0]);
                businessObj.setValue("scHTSDescription",array[1]);
                businessObj.save();
                }
                else{
                    test.setValue("scHTSCodeAssignment",array[0]);
                    test.setValue("scHTSDescription",array[1]);
                    LCSLogic.deriveFlexTypeValues(test);
                    LCSLogic.persist(test,true);
                }
                LOGGER.debug("businessObj saved");

            }catch(Exception e) {
                e.printStackTrace();
                LOGGER.debug("**FATAL:Error when trying to create data for:"+temp+e.getLocalizedMessage());
                continue;
            }

        }
    }

    public static LCSLifecycleManaged getBusinessObjectByName(FlexType BOType, String data){
        LCSLifecycleManaged businessObject = null;
        try {
            PreparedQueryStatement statement = new PreparedQueryStatement();
            statement.appendFromTable(LCSLifecycleManaged.class);
			statement.appendSelectColumn(new QueryColumn(LCSLifecycleManaged.class, BO_OBJECT_IDENTIFIER_LITERAL));	            
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, BOType.getAttribute("scHTSCodeAssignment").getColumnDescriptorName()), data, "="));
            statement.appendAndIfNeeded();
            statement.appendCriteria(new Criteria(new QueryColumn(LCSLifecycleManaged.class, LCSQuery.TYPED_BRANCH_ID), BOType.getIdNumber(), "="));
			System.out.println("statement toString-------------"+statement.toString());
            SearchResults results = LCSQuery.runDirectQuery(statement);
            if(results.getResultsFound() > 0){
                FlexObject flexBO= (FlexObject) results.getResults().elementAt(0);
                businessObject =(LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + flexBO.getString("LCSLIFECYCLEMANAGED.IDA2A2"));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        LOGGER.debug("**Returning businessObject:"+businessObject);
        return businessObject;
    }


}
