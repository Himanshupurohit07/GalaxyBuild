package com.sparc.wc.util;

import wt.httpgw.GatewayAuthenticator;

import java.io.File;
import java.util.logging.Logger;

import wt.fc.PersistenceHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.template.LoadReportTemplate;
import wt.query.template.ReportTemplate;
import wt.util.WTException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Hashtable;



public class SparcCustomReportsLoader implements RemoteAccess{

    /**
     * Logger Class.
     */
    //public static final Logger logger = LogManager.getLogger(SparcCustomReportsLoader.class);

    private static final Logger logger = Logger.getLogger(SparcCustomReportsLoader.class.getName());


    /**
     * @param args
     */
    public static void main(String args[]) {
        logger.info("SparcCustomReportsLoader Method Start");
        SparcCustomReportsLoader repLoader = new SparcCustomReportsLoader();
        repLoader.loadReports(args);
        logger.info("SparcCustomReportsLoader Method End");
    }

    /**
     * @param args
     */
    public void loadReports(String[] args) {
        RemoteMethodServer rms = RemoteMethodServer.getDefault();
        if(args.length > 1){
            GatewayAuthenticator auth = new GatewayAuthenticator();
            auth.setRemoteUser("wcadmin");
            rms.setAuthenticator(auth);
            Class<?>[] argTypes = {String.class, String.class};
            Object[] argValues = {args[0], args[1]};
            try {
                rms.invoke("loadXMLReports", "com.sparc.wc.util.SparcCustomReportsLoader", null, argTypes, argValues);
            } catch (RemoteException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param reportsFolder
     * @param orgName
     */
    public static void loadXMLReports(String reportsFolder, String orgName) {
		
		System.out.println("reportsFolder-------------"+reportsFolder);
		System.out.println("orgName-------------"+orgName);		
        try {
            File[] files = new File(reportsFolder).listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName().replace("_", " ").replace(".qml", "");
                    logger.info("File Name is : " + file.getName());
                    String[] reports = { fileName, "/System", file.getAbsolutePath(), orgName, "-replace" };
                    createSingleReport(reports);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param paramArrayOfString
     */
    public static void createSingleReport(String[] paramArrayOfString) {
        int i = paramArrayOfString.length;
        Hashtable<String, String> localHashtable = new Hashtable<>(10);
        if (i < 3) {
            logger.info("Usage: <report name> <Folder> <XML File Name> [<container path>] [-replace]");
        } else {
            for (int j = 3; j < i; j++) {
                localHashtable.put(paramArrayOfString[j], paramArrayOfString[j]);
            }
            try {
                String str = null;
                if (((i == 4) && (!paramArrayOfString[3].equals("-replace"))) || (i == 5)) {
                    str = paramArrayOfString[3];
                }
				System.out.println("paramArrayOfString[0]-----------"+paramArrayOfString[0]);
				
				System.out.println("paramArrayOfString[1]-----------"+paramArrayOfString[1]);				
				System.out.println("paramArrayOfString[2]-----------"+paramArrayOfString[2]);			
				
                System.out.println("localHashtable-----------"+localHashtable);
                System.out.println("str-----------"+str);

                ReportTemplate localReportTemplate = LoadReportTemplate.createReportTemplate(paramArrayOfString[0],
                        paramArrayOfString[1], paramArrayOfString[2], str, null, localHashtable);

                localReportTemplate = (ReportTemplate) PersistenceHelper.manager.refresh(localReportTemplate);
                logger.info("Succssfully created: " + localReportTemplate.getName());
            } catch (WTException localWTException) {
                localWTException.printStackTrace();
            }
        }
    }
}