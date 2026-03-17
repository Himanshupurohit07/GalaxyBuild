package ext.addcolumn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wt.util.WTProperties;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

public class CustomColumnDiff {

	private String ADD_COLUMNS_SH = "AddColumns.sh -a -f ";
    private String ADD_COLUMNS_BAT = "Call AddColumns.bat -a -f ";
    private final String EXPORT_TYPE_FILE = "Export File";
    private final String IMPORT_DIR_PART = "/addColumnFiles";
    private final String FILE_PREFIX = "/AddColumns_";

    private ArrayList<String> flexTypeList = new ArrayList<String>();
    private String sourceUser = "";
    private String sourcePassword = "";
    private String sourceServer = "";
    private boolean sourceexport = false;
    private String targetUser = ""; 
    private String targetPassword = "";
    private String targetServer = "";
    private String printall = "";
    private PrintWriter writerSh = null;
    private PrintWriter writerBat = null;
    private String flexTypeGroup = "";
    private String shFile;
    private String batFile;
    private String exportDirPath;
    
    private HashMap<String, JSONArray> masterTypeCountList = null;
    private boolean filesCreated=false;
    private static String wtHome;
    static{
    	try {
    		WTProperties wtProp = WTProperties.getLocalProperties();
    		wtHome = wtProp.getProperty("wt.home");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
   
 
    public static void main(String[] args) {
    	try {
			new CustomColumnDiff().executeUtility(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Executes the utility. If sourceexport flag is set, full export is done to an .txt file. Else if sourceServer is
     * not passed, target will be compared to corresponding .txt file to generate the .sh & .bat file. Else source and
     * target servers are compared to generate the .sh & .bat file.
     * @throws Exception 
     */
    public boolean executeUtility(String[] args){
    	boolean exceptionThrwn = false;
    	boolean restartRequired = false;
		try {
    		processInputArguments(args);
    		
    		if (sourceexport) {
    			System.out.println("executeUtility flexTypeList=" + flexTypeList + " flexTypeGroup=" + flexTypeGroup + "> sourceexport=" + sourceexport+" path is "+exportDirPath);
    			System.out.println("Generating master file for source environment.");
    			intializeAddColumnsOutputFiles("Export",exportDirPath);
    			for (int i = 0; i < flexTypeList.size(); i++) {
    				String flexType = flexTypeList.get(i);
    				System.out.println("Exporting column counts for -> " + flexType);
    				exportColumnCount(writerSh, flexType);
    			}
    			System.out.println("Auto-generated 'Export' file is located at > " + shFile);
    		} else {
    			filesCreated=false;
    			String exportFile = exportDirPath + FILE_PREFIX + flexTypeGroup + ".txt";
    			System.out.println("Export File si  " + exportFile);
    			if(new File(exportFile).exists()){
    				if (sourceServer == null || sourceServer.isEmpty()) {
    					System.out.println("Source environment not mentioned. Export will be done with 'AddColumns_" + flexTypeGroup
    			                    + ".txt' as the source.");
    					sourceServer = EXPORT_TYPE_FILE;
    			    }
    				System.out.println("executeUtility flexTypeList=" + flexTypeList + " flexTypeGroup=" + flexTypeGroup + "> sourceexport=" + sourceexport+" path is "+exportDirPath);
	    			for (int i = 0; i < flexTypeList.size(); i++) {                	
	    				String flexType = flexTypeList.get(i);
	    				JSONArray sourceColumnsArray = getColumnCounts(sourceServer, flexType, sourceUser, sourcePassword,"Export");
	    				JSONArray targetColumnsArray = getColumnCounts(targetServer, flexType, targetUser, targetPassword,"Import");
	    				System.out.println("Getting column difference for -> " + flexType);
	    				System.out.println("sourceColumnsArray=" + sourceColumnsArray + " targetColumnsArray=" + targetColumnsArray);
	    				getColumnDiff(flexType, sourceColumnsArray, targetColumnsArray);
	    			}
	    			if(filesCreated){
	    				restartRequired = true;
	    				System.out.println("Auto-generated 'AddColumns' Script file is located at > " + shFile);
	    				System.out.println("Auto-generated 'AddColumns' Batch  file is located at > " + batFile);
	    				System.out.println("#########################################################");
	    				System.out.println("############# Restart of Server is Required #############");
	    				System.out.println("#########################################################");
	    			}else{
	    				System.out.println("#############################################################");
	    				System.out.println("############# Not Required to Restart of Server #############");
	    				System.out.println("#############################################################");
	    			}
	    			if (EXPORT_TYPE_FILE.equals(sourceServer)) {
	    				System.out.println("Export completed with 'AddColumns_" + flexTypeGroup + ".txt' as the source.");
	    			}
    			}
    		}
    	} catch (Exception e) {    		
    		e.printStackTrace();
    		/*StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		String exceptionAsString = sw.toString();
    		System.out.println("transaction_id =" +transID + exceptionMessage);
    		System.out.println(exceptionAsString); */ 
    		System.out.println("exception is =" +e);    		
    		exceptionThrwn=true;
    	}finally{
    		if(filesCreated){
    			writerSh.flush();
    			writerSh.close();
    			writerBat.flush();
    			writerBat.close();
    		}
    		if(exceptionThrwn){
    		   System.exit(0);;
    		}
    	}
		return restartRequired;
    }

    /**
     * Creates directories and initializes the .txt file.
     * 
     * @throws Exception
     */
    private void intializeAddColumnsOutputFiles(String impExp,String dir_part) throws Exception {
    	DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        Date dateobj = new Date();
        WTProperties wtProp = WTProperties.getLocalProperties();
        String wtHome = wtProp.getProperty("wt.home");
        String javaHome = wtProp.getProperty("wt.jdk");
        System.out.println("wtHome=" + wtHome);
        System.out.println("wt.jdk=" + javaHome);

        String fileLocation =wtHome+ dir_part ;
        
        File files = new File(fileLocation);
    	if (!files.exists()) {
    		if (files.mkdirs()) {
    			System.out.println("Multiple directories are created! = " + fileLocation);
    		} else {
    			System.out.println("Failed to create multiple directories! = " + fileLocation);
    		}
    	}
        
        if("Export".equals(impExp)){
        	
        	shFile = fileLocation + FILE_PREFIX + flexTypeGroup + ".txt";
        	File shfile = new File(shFile);
        	
        	System.out.println("Deleting existing file if it exists ----- >>>>> >"+shfile.delete()); 
        	writerSh = new PrintWriter(shFile, "UTF-8");        	
        	writerSh.println("## This file is auto generated, DO NOT modify manually ");
        	writerSh.println("## AddColumns Full Export");
        	//writerSh.println("## Generated on " + df.format(dateobj));
        	writerSh.println("## Source Environment:" + sourceServer);
        	writerSh.println();
        	writerSh.flush();
			shfile.setExecutable(true);
        	shfile.setReadable(true);
        	shfile.setWritable(true);
        	//File shfile1 = new File(fileLocation + FILE_PREFIX + flexTypeGroup + "1.txt");
        	//PrintWriter writerSh1 = new PrintWriter(shfile1, "UTF-8");
        	//writerSh1.print("**************************************************************************");
        	//writerSh1.flush();
        	//writerSh1.close();
        }
        if("Import".equals(impExp)){
        	shFile= fileLocation+FILE_PREFIX+flexTypeGroup+".sh";
        	File shfileFile = new File(shFile);
        	
        	writerSh = new PrintWriter(shFile, "UTF-8");        	
        	writerSh.println("#!/bin/sh");
			writerSh.println("## This file is auto generated, DO NOT modify manually ");
			writerSh.println("## AddColumns Script");			
			writerSh.println("## Generated for Linux on " + df.format(dateobj));
			writerSh.println("## Source Environment:" + sourceServer);
			writerSh.println("## Target Environment:" + targetServer);			
			writerSh.println();  
			writerSh.flush();
        	batFile=wtHome+"/addColumnFiles"+FILE_PREFIX+flexTypeGroup+".bat";
        	File batfileFile = new File(batFile);
        	writerBat = new PrintWriter(batFile, "UTF-8");  
        	writerBat.println(":: This file is auto generated, DO NOT modify manually ");
			writerBat.println(":: AddColumns Script");
			writerBat.println(":: Generated for Windows  on " + df.format(dateobj));
			writerBat.println(":: Source Environment:" + sourceServer);
			writerBat.println(":: Target Environment:" + targetServer);
			writerBat.println();
			writerBat.flush();
			
			shfileFile.setExecutable(true,true);
        	shfileFile.setReadable(true,true);
        	shfileFile.setWritable(true,true);
        	batfileFile.setExecutable(true,true);
        	batfileFile.setReadable(true,true);
        	batfileFile.setWritable(true,true);         	
        	
        	System.out.println("Is Execute allow : " + shfileFile.canExecute());
        	System.out.println("Is Write allow : " + shfileFile.canWrite());
        	System.out.println("Is Read allow : " + shfileFile.canRead());           
        	
        }
    }
    /**
     * Process and validate input arguments >With flag -sourceexport, target server details are not required. >To
     * compare with pre-exported source file , source server details are not required.
     * 
     * @param args
     */
    private void processInputArguments(String[] args) {
        int argsCount = args.length;
        System.out.println("--->>"+Arrays.toString(args));        
        
        for (int curArgCount = 0; curArgCount < argsCount; ++curArgCount) {
            if (args[curArgCount].equals("-sourceUser")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    sourceUser = new String(args[curArgCount]);
                }
            } else if (args[curArgCount].equals("-sourcePassword")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    sourcePassword = new String(args[curArgCount]);
                }
            } else if (args[curArgCount].equals("-targetUser")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    targetUser = new String(args[curArgCount]);
                }
            } else if (args[curArgCount].equals("-targetPassword")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    targetPassword = new String(args[curArgCount]);
                }
            } else if (args[curArgCount].equals("-s")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    sourceServer = new String(args[curArgCount]);
                    if (!sourceServer.startsWith("http")) {
                        throw new IllegalArgumentException("Invalid value for sourceServer = " + sourceServer);
                    }
                }
            } else if (args[curArgCount].equals("-t")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    targetServer = new String(args[curArgCount]);
                    if (!targetServer.startsWith("http")) {
                        throw new IllegalArgumentException("Invalid value for targetServer = " + targetServer);
                    }
                }
            } else if (args[curArgCount].equals("-f")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    flexTypeGroup = new String(args[curArgCount]);
                    String flexTypes = LCSProperties.get(flexTypeGroup);
                    flexTypeList = new ArrayList<String>(Arrays.asList(flexTypes.split(",")));
                }

            } else if (args[curArgCount].equals("-path")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                	String action = new String(args[curArgCount]);
                	if(FormatHelper.hasContent(action) && !"executeDeltaFiles".equalsIgnoreCase(action)){
                		exportDirPath = wtHome + "/addColumnFiles";
                	}else{
                		exportDirPath = "/loadFiles/lcsLoadFiles/com/deltaData/addColumnFiles/";
                	}
                }

            } else if (args[curArgCount].equals("-sourceexport")) {
                sourceexport = true;
            } else {
                printUsage();
                throw new IllegalArgumentException("Inavalid paramter found : " + args[curArgCount]);
            }
        }
    }

    /**
     * Exports the full list of additional columns added for 'flexType' to a flat file.
     * 
     * @param writerSh
     * @param flexType
     * @throws JSONException
     * @throws Exception
     */
    private void exportColumnCount(PrintWriter writerSh, String flexType) throws JSONException, Exception {

        String addColScript = "";
        JSONArray sourceColumnsArray = getColumnCounts(sourceServer, flexType, sourceUser, sourcePassword,"Export");
        for (int i = 0; i < sourceColumnsArray.length(); i++) {
            JSONObject objects = sourceColumnsArray.getJSONObject(i);
            Iterator<?> keys = objects.keys();
            int srcDeltaCount = 0;

            while (keys.hasNext()) {
                Object key = keys.next();
                Object value = objects.get(key.toString());
                srcDeltaCount = Integer.parseInt(value.toString());

                System.out.println("Source Count for " + addColScript + " = " + srcDeltaCount);

                if (srcDeltaCount > 0) {
                    addColScript = key + "=" + srcDeltaCount + " " + addColScript;
                }
            }
        }
        if (addColScript.length() > 0) {
            writerSh.println(flexType + " " + addColScript);
            writerSh.flush();
        }
    }

    /**
     * Compares sourceColumnsArray & targetColumnsArray and generates the difference as AddColumns script. If the target
     * is ahead, warning is printed. This is ignored and not an error condition.
     * 
     * @param writerSh
     * @param writerBat
     * @param flexType
     * @param sourceColumnsArray
     * @param targetColumnsArray
     * @throws JSONException
     * @throws Exception
     */
    private void getColumnDiff(String flexType, JSONArray sourceColumnsArray,
            JSONArray targetColumnsArray) throws JSONException, Exception {
        String addColScript = "";// String=49 Long=12 Double=4 Boolean=5 ObjectReference=1 Timestamp=1        
        for (int i = 0; i < sourceColumnsArray.length(); i++) {
            JSONObject objects = sourceColumnsArray.getJSONObject(i);
            Iterator<?> keys = objects.keys();
            int srcDeltaCount = 0;
            int targetDeltaCount = 0;

            while (keys.hasNext()) {
                Object key = keys.next();
                Object value = objects.get(key.toString());
                srcDeltaCount = Integer.parseInt(value.toString());
                targetDeltaCount = getDeltaCount(targetColumnsArray, key);
                int colTobeAdded = srcDeltaCount - targetDeltaCount;


                if (colTobeAdded > 0 || printall.equals("true")) {
                    addColScript = key + "=" + colTobeAdded + " " + addColScript;
                } else if (colTobeAdded < 0) {
                    System.out.println(">> Target Server has more columns >>   " + key + "=" + colTobeAdded);
                }
                System.out.println("************getColumnDiff addColScript==" + addColScript + " srcDeltaCount=" 
                + srcDeltaCount + " targetDeltaCount=" + targetDeltaCount + " key=" + key + " srcDeltaCount =" + srcDeltaCount + ">");
            }
        }
        
        System.out.println("************getColumnDiff  flexType=" + flexType 
        		+ " addColScript==" + addColScript + "> addColScript.length()=" + addColScript.length() + " filesCreated=" + filesCreated + ">");
        
        if (addColScript.length() > 0) {
        	if(!filesCreated){
        		System.out.println("************getColumnDiff  Creating Import files for " + flexType);
        	   intializeAddColumnsOutputFiles("Import",IMPORT_DIR_PART);
        	   filesCreated=true;
        	}
			writerSh.println(ADD_COLUMNS_SH + flexType + " " + addColScript);
			writerSh.flush();
			writerBat.println(ADD_COLUMNS_BAT + flexType + " " + addColScript);
			writerBat.flush();
        }
    }

    /**
     * Calculates the difference in count for the columnType in targetColumnsArray.
     * 
     * @param targetColumnsArray
     * @param columnType
     *            - eg. STRING
     * @return
     * @throws JSONException
     */
    private int getDeltaCount(JSONArray targetColumnsArray, Object columnType) throws JSONException {

        int deltaCount = 0;
        boolean found = false;
        for (int i = 0; i < targetColumnsArray.length(); i++) {
            JSONObject objects = targetColumnsArray.getJSONObject(i);
            Iterator<?> keys = objects.keys();
            found = false;

            while (keys.hasNext()) {
                Object key = keys.next();
                if (key.equals(columnType)) {
                    Object value = objects.get(columnType.toString());
                    deltaCount = Integer.parseInt(value.toString());
                    found = true;
                    break;
                }
            }
            if (found == true)
                break;
        }
        System.out.println("getDeltaCount found==" + found + " deltaCount=" + deltaCount );
        return deltaCount;
    }

    /**
     * Calls the rest api to get column counts of the flextype.
     * 
     * @param env
     * @param flexType
     * @param user
     * @param password
     * @return
     * @throws Exception
     */
    private JSONArray getColumnCounts(String env, String flexType, String user, String password, String impExp) throws Exception {

        if (!env.startsWith("http")) {
            return getColumnCountsFromExport(flexType);
        } else {
			String environment = env + "/Windchill/servlet/rest/add/util/flextype/columncount?flexTypeStr=" + flexType+"&impExp="+impExp;
            String httpPort = WTProperties.getServerProperties().getProperty("wt.webserver.port");
           // System.out.println("wt.webserver.port is ----> "+httpPort);
            if(FormatHelper.hasContent(httpPort) && (!httpPort.equalsIgnoreCase("80") && !httpPort.equalsIgnoreCase("443"))){
            	if(env.startsWith("http://")){
            		environment = environment.replace(env.replace("http://", ""), env.replace("http://", "") + ":"+httpPort);		
            	}else{
            		environment = environment.replace(env.replace("https://", ""), env.replace("https://", "") + ":"+httpPort);	
            	}
            	
            }

            WCAuthenticator wcAuth = new WCAuthenticator();
            wcAuth.setUserPass(user, password);
            Authenticator.setDefault(wcAuth);

            JSONArray colArray = null;
            try {

                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                } };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                
                /* End of the fix */
                System.out.println("URL ------------->>> "+environment);
                URL srcURL = new URL(environment);
                if(environment.startsWith("https:")) {
                	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                	HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                	java.net.HttpURLConnection conn = (java.net.HttpURLConnection) srcURL.openConnection();
                    // HttpsURLConnection conn = (HttpsURLConnection) srcURL.openConnection();
                     conn.setRequestMethod("GET");
                     conn.setRequestProperty("Accept", "application/json");

                     if (conn.getResponseCode() != 200) {
                         throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                     }
                     BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                     String output;
                     String columnsJson = "";
                     System.out.println("Output from Server .... \n");
                     while ((output = br.readLine()) != null) {
                         columnsJson = columnsJson + output;
                         System.out.println(output);
                     }
                     conn.disconnect();
                     colArray = new JSONArray(columnsJson);
                }else {
                	java.net.HttpURLConnection conn = (java.net.HttpURLConnection) srcURL.openConnection();
                	//HttpsURLConnection conn = (HttpsURLConnection) srcURL.openConnection();
                	conn.setRequestMethod("GET");
                	conn.setRequestProperty("Accept", "application/json");

                	if (conn.getResponseCode() != 200) {
                		throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                	}
                	BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                	String output;
                	String columnsJson = "";
                	System.out.println("Output from Server .... \n");
                	while ((output = br.readLine()) != null) {
                		columnsJson = columnsJson + output;
                		System.out.println(output);
                	}
                	conn.disconnect();
                	colArray = new JSONArray(columnsJson);
                }
                
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                /*StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                System.out.println(exceptionMessage);
                System.out.println(exceptionAsString);*/
                System.out.println(e);
                System.exit(0);
            }
            return colArray;
        }
    }

    private JSONArray getColumnCountsFromExport(String flexType) throws JSONException, Exception {
        if (masterTypeCountList == null) {
            initializeMasterTypeCountList();
        }
        System.out.println("------------- master");
        JSONArray typeArray = masterTypeCountList.get(flexType);
        return typeArray == null ? new JSONArray() : typeArray;
    }

    /**
     * Initializes a HashMap based on the Export file for the flextype being passed to utility.
     */
    private void initializeMasterTypeCountList() throws Exception{
		masterTypeCountList = new HashMap<>();
		//logger.debug("export file before .... "+shFile);
        String exportFile = exportDirPath + FILE_PREFIX + flexTypeGroup + ".txt";
        if(new File(exportFile).exists()){
			System.out.println("export file after .... "+exportFile);
	        List<String> list = new ArrayList<>();
	        try (Stream<String> stream = Files.lines(Paths.get(exportFile))) {
	            list = stream.filter(line -> !line.startsWith("#")).filter(line -> !line.isEmpty()).collect(Collectors.toList());
	
	        } catch (IOException e) {
	            e.printStackTrace();
	            /*StringWriter sw = new StringWriter();
	            e.printStackTrace(new PrintWriter(sw));
	            String exceptionAsString = sw.toString();
	            System.out.println(exceptionMessage);
	            System.out.println(exceptionAsString);*/
	            System.exit(0);
	        }
	        list.forEach((eachFlexType) -> {
	            updateJsonArray(masterTypeCountList, eachFlexType);
	        });
        }
    }

    private void updateJsonArray(HashMap<String, JSONArray> colArray, String input) {
        String[] typeDetails = input.split(" ");
        JSONArray myArray = new JSONArray();
        colArray.put(typeDetails[0], myArray);

        for (int i = 1; i < typeDetails.length; i++) {
            JSONObject eachType = new JSONObject();
            String[] typeAndCounts = typeDetails[i].split("=");
            try {
                eachType.put(typeAndCounts[0], typeAndCounts[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            myArray.put(eachType);
        }
    }

    private void printUsage() {
        System.out.println("Usage: windchill ext.wc.rest.util.CustomColumnDiff -sourceUser wcadmin -sourcePassword xxxxxx -targetUser wcadmin -targetPassword xxxxhttps://ptc-pcx-localdev02-flex11.pes-preprod.com -t https://ptc-pcx-localdev02-flex11.pes-preprod.com -f com.lcs.wc.flexbom.FlexBOMPart");
        System.out.println("       [-sourceUser Source's Target user ]");
        System.out.println("       [-sourcePassword Source's  password ]");
        System.out.println("       [-targetUser Target's user name ]");
        System.out.println("       [-targetPassword Target's  password ]");
        System.out.println("       [-s source server, example:  https://ptc-local-preprod.com ]");
        System.out.println("       [-t target server, example:  https://ptc-local-preprod.com ]");
        System.out.println("       [-f flex-Type, example:  BOM, Season, All..... ]");
        System.out.println("       [-printall true -- Print all the type, even if the difference is ZERO. ]");
        System.out.println(".");
    }

    class WCAuthenticator extends Authenticator {

        String username = "";
        String password = "";

        public void setUserPass(String user, String pass) {
            username = user;
            password = pass;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            System.out.println("username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(username, password.toCharArray()));
        }
    }

}
