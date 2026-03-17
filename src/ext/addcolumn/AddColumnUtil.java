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
import java.util.ArrayList;
import java.util.Arrays;
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

import wt.pom.DBProperties;
import wt.util.WTProperties;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.ptc.windchill.keystore.WTKeyStoreUtil;

public class AddColumnUtil {

    private final String FILE_PREFIX = "/AddColumns_";

    private ArrayList<String> flexTypeList = new ArrayList<String>();
    private String user = "";
    private String password = "";
    private static String sourceServer = "";
       private PrintWriter writerSh = null;
    private PrintWriter writerBat = null;
    private String flexObject = "";
    private String shFile;
    private static String exportDirPath;
    private static String wtHome;
    private static String protcol = "";
	private static String hostName = "";
	private static String action = "";
	private static String dbUser = "";
	private static String dbPassword = "";
	private static String platExt;
	
    private HashMap<String, JSONArray> masterTypeCountList = null;
    private boolean filesCreated=false;
    static{
    	try {
    		WTProperties dbProp = DBProperties.getDBProperties();
    		dbUser = dbProp.getProperty("wt.pom.dbUser");
    		WTProperties wtProp = WTProperties.getLocalProperties();
            wtHome = wtProp.getProperty("wt.home");
           
    		hostName = wtProp.getProperty("wt.rmi.server.hostname");
			protcol = wtProp.getProperty("wt.webserver.protocol");
			exportDirPath = wtHome + File.separator + "addColumnFiles";
			String os = System.getProperty("os.name");
			if("linux".equalsIgnoreCase(os)){
				platExt = ".sh";
			}else{
				platExt = ".bat";
			}	
    		if(FormatHelper.hasContent(hostName) && FormatHelper.hasContent(protcol)) {
				sourceServer = protcol + "://" + hostName;
			}
    		System.out.println("Env is --->"+sourceServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
   
    public static void main(String[] args) {
    	AddColumnUtil util = new AddColumnUtil();
    	util.processInputArguments(args);
    	/*try {
			FileUtils.deleteDirectory(new File(wtHome + "/addColumnFiles/"));
			System.out.println("Clean Directory");
			File deleteLoc = new File(wtHome + "/addColumnFiles/");
			if(!deleteLoc.exists()){
				deleteLoc.mkdir();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/
    	if("Export".equalsIgnoreCase(action)) {
    		util.executeExportUtility();
    	}else {
    		 dbPassword = WTKeyStoreUtil.decryptProperty("wt.pom.dbPassword", "encrypted.wt.pom.dbPassword", wtHome);
             System.out.println("dbUser " + dbUser + " dbPassword " + dbPassword);
    		util.executeImportUtility();
    	}
    }

    private void executeImportUtility() {
    	System.out.println("import action called"+flexObject);
    	boolean updateDBColumns = generateShAndBatFileFromDotTxt(flexObject);
		if(updateDBColumns){
			//addColumnsScriptForAll(flexObject);
			String[] split = flexObject.split(",");
			for(int i=0;i<split.length;i++){
				if(!"Enums".equals(split[i])){
					String executeCommand = new StringBuffer().append(wtHome).append("/addColumnFiles/AddColumns_").append(split[i]).append(platExt).toString();
					executeCommandUsingProcess(executeCommand, "addColumns");
				}
			}
			String fileName = new StringBuffer().append(wtHome).append("/bin/AddColumns").append(platExt).toString();
			File extFile = new File(fileName);
			if(extFile.exists()){
				String executeCommand = fileName+" -u ";
				executeCommandUsingProcess(executeCommand, "addColumns U");
			}		
		}
	}

	private void executeCommandUsingProcess(String commandToExecute, String str) {
		if("addColumns U".equals(str) || "syncFromDB".equals(str)){
			commandToExecute+=" -s -DauthDBPassword=" + dbPassword + " -DexitInstall=\"\"";
		}else{			
			//commandToExecute+=" -u " + user + " -p " + password;
		}
		try{
			System.out.println("execute command is "+commandToExecute);
			Process process = Runtime.getRuntime().exec(commandToExecute);
			StringBuffer logStr = new StringBuffer();
			if(process != null){
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			    // read the output from the command
				//print("Here is the standard output of the command:\n");
				String tempStr;
			    while ((tempStr = stdInput.readLine()) != null) {		    	
			    	System.out.println(tempStr+"\n");
			    	logStr.append(tempStr+"\n");
			    }
			    		    
			    while ((tempStr = stdError.readLine()) != null) {
			    	System.out.println(tempStr+"\n");
				    logStr.append(tempStr+"\n");
			    }
			    stdInput.close();
			    stdError.close();
			    System.out.println("logStr is "+logStr);
			}
			//appendToLogFileAndValidate(process, str);
			while(true){
				if (!process.isAlive()){
					break;
				}		    			
			}
			System.out.println("Command Executed");
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("***Aborting utility for action "+action+"***");
		}
	}

	private boolean generateShAndBatFileFromDotTxt(String flexObject) {
		System.out.println("Generate_AddColumns_Script_And_Bat_File" + flexObject);
		boolean updateColumns = false;
		String[] split = flexObject.split(",");
		for(int i=0;i<split.length;i++){
			if(!"Enums".equals(split[i])){
				String args[] = new String[]{"-targetUser", user, "-targetPassword", password, "-t", sourceServer, "-f", split[i], "-path", action};
				CustomColumnDiff cusColDif= new CustomColumnDiff();
				boolean checkStatus = cusColDif.executeUtility(args);
				if(checkStatus){
					updateColumns = true;
				}			
			}
		}
		System.out.println("Generate_AddColumns_Script_And_Bat_File return from method is "+updateColumns);
		return updateColumns;
		//return false;
	}

	public boolean executeExportUtility(){
    	boolean exceptionThrwn = false;
    	boolean restartRequired = false;
		try {
    		
   			//System.out.println("executeUtility flexTypeList=" + flexTypeList + " flexTypeGroup=" + flexTypeGroup + "> sourceexport=" + sourceexport+" path is "+exportDirPath);
   			System.out.println("Generating master file for source environment.");
   			intializeAddColumnsOutputFiles("Export",exportDirPath);
   			for (int i = 0; i < flexTypeList.size(); i++) {
   				String flexType = flexTypeList.get(i);
   				System.out.println("Exporting column counts for -> " + flexType);
   				exportColumnCount(writerSh, flexType);
   			}
   			System.out.println("Auto-generated 'Export' file is located at > " + shFile);
    	} catch (Exception e) {    		
    		e.printStackTrace();
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
    	File files = new File(dir_part);
    	if (!files.exists()) {
    		if (files.mkdirs()) {
    			System.out.println("Multiple directories are created! = " + dir_part);
    		} else {
    			System.out.println("Failed to create multiple directories! = " + dir_part);
    		}
    	}
    	shFile = dir_part + FILE_PREFIX + flexObject + ".txt";
    	File shfile = new File(shFile);
    	
    	writerSh = new PrintWriter(shFile, "UTF-8");        	
    	writerSh.println("## This file is auto generated, DO NOT modify manually ");
    	writerSh.println("## AddColumns Full Export");
    	//writerSh.println("## Generated on " + df.format(dateobj));
    	writerSh.println("## Source Environment:" + sourceServer);
    	writerSh.println();
    	writerSh.flush();
       	if(shfile.exists()) {
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
            if (args[curArgCount].equals("-user")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    user = new String(args[curArgCount]);
                }
            } else if (args[curArgCount].equals("-password")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    password = new String(args[curArgCount]);
                }
            }else if (args[curArgCount].equals("-action")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    action = new String(args[curArgCount]);
                }
            }else if (args[curArgCount].equals("-f")) {
                ++curArgCount;
                if (curArgCount < argsCount) {
                    flexObject = new String(args[curArgCount]);
                    try {
                    	WTProperties wtProp = WTProperties.getServerProperties();
                    	String flexTypes = (String)LCSProperties.get(flexObject);
                    	System.out.println("new Flex Type is "+flexTypes);
                    	if(FormatHelper.hasContent(flexTypes)) {
                    		flexTypeList = new ArrayList<String>(Arrays.asList(flexTypes.split(",")));
                    	}
                    }catch(IOException io) {
                    	
                    }
                }

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
        JSONArray sourceColumnsArray = getColumnCounts(sourceServer, flexType, user, password,"Export");
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

            String envURL = env + "/Windchill/servlet/rest/add/util/flextype/columncount?flexTypeStr=" + flexType+"&impExp="+impExp;

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
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                /* End of the fix */
                System.out.println("URL ------------->>> "+envURL);
                URL srcURL = new URL(envURL);
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                System.exit(0);
            }
            return colArray;
        }
    }

    private JSONArray getColumnCountsFromExport(String flexType) throws JSONException, Exception {
        if (masterTypeCountList == null) {
            initializeMasterTypeCountList();
        }
        JSONArray typeArray = masterTypeCountList.get(flexType);
        return typeArray == null ? new JSONArray() : typeArray;
    }

    /**
     * Initializes a HashMap based on the Export file for the flextype being passed to utility.
     */
    private void initializeMasterTypeCountList() throws Exception{
		masterTypeCountList = new HashMap<>();
		//logger.debug("export file before .... "+shFile);
        String exportFile = exportDirPath + FILE_PREFIX + flexObject + ".txt";
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
        System.out.println("Usage: windchill ext.addColumn.AddColumnUtil");
        System.out.println("       [-sourceUser Source's Target user ]");
        System.out.println("       [-sourcePassword Source's  password ]");
        System.out.println("       [-targetUser Target's user name ]");
        System.out.println("       [-targetPassword Target's  password ]");
        System.out.println("       [-f flex-Type, example:  BOM, Season, All..... ]");
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
            info("Feeding username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(username, password.toCharArray()));
        }
    }

    private void info(String message) {
        System.out.println(message);
    }
}
