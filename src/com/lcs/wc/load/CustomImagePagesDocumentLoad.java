package com.lcs.wc.load;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentCollection;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.document.LCSImage;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.DepartmentList;
import wt.doc.DocumentType;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.fv.FvVault;
import wt.fv.StandardFvService;
import wt.fv.Vault;
import wt.intersvrcom.Site;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class CustomImagePagesDocumentLoad {
	private static final Logger LOGGER = LogManager.getLogger(LoadDocument.class.getName());
	protected static LCSDocumentLogic DOCUMENT_LOGIC = new LCSDocumentLogic();
	protected static LCSDocumentQuery DOCUMENT_QUERY = new LCSDocumentQuery();
	public static final String IMAGE_URL = LCSProperties.get("com.lcs.wc.content.imageURL", "/Windchill/images");
	private static final String String = null;
	public static String CONTENT_LOAD_LOCATION = "";
	public static String glDocumentMasterId = null;
	public static String glDocColumnName = null;
	//public static Set<String> ImagePageCSVmap = new HashSet<>();
	//private static String csv_file_path = "C:\\PTC\\Extraction\\VisualAssets\\ImagePageCSVFile.csv";

	//static {
		// set ImagePageCSVmap as per csv
		//setImagePageCSVmap(csv_file_path);
	//}

	public static boolean createDocumentCollection(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return createDocumentCollection(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createDocument(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return createDocument(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createDocument3DModel(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return createDocument3DModel(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean addSecondaryContent(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return addSecondaryContent(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean associateDocument(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return associateDocument(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean addDocumentToCollection(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return addDocumentToCollection(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean createImage(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return createImage(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	public static boolean addImageToCollection(Hashtable dataValues, Hashtable commandLine, Vector returnObjects) {
		return addImageToCollection(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
	}

	protected static boolean createDocumentCollection(Hashtable dataValues, String fileName) {
		try {
			LCSDocumentCollection documentCollection = LCSDocumentCollection.newLCSDocumentCollection();
			return (documentCollection = (LCSDocumentCollection) LoadCommon.getObjectByCriteria(fileName,
					documentCollection, dataValues, "LCSDocumentCollection")) == null ? true
							: LoadHelper.save(documentCollection, dataValues, fileName);
		} catch (WTException var3) {
			var3.printStackTrace();
			LoadCommon.display("\n#WTException : " + var3.getLocalizedMessage());
			return false;
		}
	}

	protected static boolean createDocument(Hashtable dataValues, String fileName) {
		boolean ans = false;

		try {
			if (LoadCommon.DEBUG) {
				LoadCommon.display(dataValues.toString());
			}

			String type = (String) dataValues.get("Type");
			glDocumentMasterId = (String) dataValues.get("glDocumentMasterId");

			//if (!ImagePageCSVmap.contains(glDocumentMasterId)) {
				// skip the data
				//return true;
			//}

			//if (ImagePageCSVmap.contains(glDocumentMasterId)) {
				FlexType flexType = FlexTypeCache.getFlexTypeFromPath(type);

				glDocColumnName = flexType.getAttribute("glDocumentMasterId").getColumnName();
				LCSDocument document = findDocumentByMasterId(glDocumentMasterId, glDocColumnName);

				if (document == null) {
					LOGGER.info("Creating new Document");
					System.out.println("Creating new Document");
					document = createNewReportDocument(fileName, flexType, dataValues, glDocumentMasterId);
					String imageFile = LoadCommon.getValue(dataValues, "Image", false);
					if (imageFile != null) {
						imageFile = CustomImagePagesDocumentLoad
								.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "Images", imageFile);
						String imageFullPath = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + imageFile;
						System.out.println("if document created -> ImageFullPath = "+imageFullPath);
						String uploadedName = LoadCommon.uploadFile(imageFullPath);
						if (uploadedName != null) {
							document.setThumbnailLocation(IMAGE_URL + "/" + uploadedName);
						}
						// DeleteFileHelper.deleteFile(imageFullPath);
					}

					String primaryFile = LoadCommon.getValue(dataValues, "PrimaryContent", false);

					if (primaryFile != null) {
						LOGGER.info("Adding Primary File");
						System.out.println("If document creating -> Adding PrimaryFile :"+primaryFile);
						primaryFile = CustomImagePagesDocumentLoad
								.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content", primaryFile);
						String primaryContentFullPath = LoadCommon.LOAD_DIRECTORY + "content" + File.separator
								+ primaryFile;
						System.out.println("PrimaryFile content full path :"+primaryContentFullPath);
						if (LoadCommon.doesFileExist(primaryContentFullPath)) {
							LoadCommon.display(
									"Setting Content File '" + primaryContentFullPath + "' For LCSDocument ...");
							dataValues.put("PRIMARYCONTENT", primaryContentFullPath);
							String description = LoadCommon.getValue(dataValues, "PrimaryContentDescription", false);
							if (description == null) {
								description = "";
							}
							dataValues.put("PRIMARYCONTENT_DESCRIPTION", description);
						} else {
							LoadCommon.display(
									"#WARNING : Content File '" + primaryContentFullPath + "' Does Not Exist !!!");
						}

					}
					ans = LoadHelper.save(document, dataValues, fileName);
					// LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", document);
					return true;
				}
				LOGGER.info("Updating Existing Document");
				System.out.println("Updating Existing Document");

				String value = null;
				if ((value = LoadCommon.getValue(dataValues, "DocType", false)) == null) {
					value = "$$Document";
				}

				if (!PersistenceHelper.isPersistent(document)) {
					document.setDocType(DocumentType.toDocumentType(value));
				}

				if ((value = LoadCommon.getValue(dataValues, "DeptList", false)) == null) {
					value = "ENG";
				}

				document.setValue("glDocumentMasterId", dataValues.get("glDocumentMasterId"));

				document.setDepartment(DepartmentList.toDepartmentList(value));
				String imageFile = LoadCommon.getValue(dataValues, "Image", false);
				if (imageFile != null) {
					imageFile = CustomImagePagesDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "Images",
							imageFile);
					String imageFullPath = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + imageFile;
					System.out.println("if document updating -> ImageFullPath = "+imageFullPath);
					String uploadedName = LoadCommon.uploadFile(imageFullPath);
					if (uploadedName != null) {
						document.setThumbnailLocation(IMAGE_URL + "/" + uploadedName);
					}
					// DeleteFileHelper.deleteFile(imageFullPath);
				}

				String primaryFile = LoadCommon.getValue(dataValues, "PrimaryContent", false);

				if (primaryFile != null) {
					LOGGER.info("Adding Primary File");
					primaryFile = CustomImagePagesDocumentLoad
							.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content", primaryFile);
					String primaryContentFullPath = LoadCommon.LOAD_DIRECTORY + "content" + File.separator
							+ primaryFile;
					System.out.println("If document updating -> Adding PrimaryFile :"+primaryFile);
					if (LoadCommon.doesFileExist(primaryContentFullPath)) {
						LoadCommon.display("Setting Content File '" + primaryContentFullPath + "' For LCSDocument ...");
						dataValues.put("PRIMARYCONTENT", primaryContentFullPath);
						String description = LoadCommon.getValue(dataValues, "PrimaryContentDescription", false);
						if (description == null) {
							description = "";
						}
						dataValues.put("PRIMARYCONTENT_DESCRIPTION", description);
					} else {
						LoadCommon
								.display("#WARNING : Content File '" + primaryContentFullPath + "' Does Not Exist !!!");
					}

				}

				ans = LoadHelper.save(document, dataValues, fileName);
			//}

		} catch (WTException | UnsupportedEncodingException | WTPropertyVetoException var6) {
			if (LoadCommon.DEBUG) {
				var6.printStackTrace();
			}

			LoadCommon.display("\n#Exception : " + var6.getLocalizedMessage());
		}

		return ans;
	}

//	private static void setImagePageCSVmap(String csv_file_path2) {
//		try (Reader reader = new FileReader(csv_file_path, StandardCharsets.UTF_8);
//				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(',') // coma-delimited
//						.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
//
//			for (CSVRecord record : csvParser) {
//
//				String csvDocumentMasterId = record.get("Document Master Id");
//
//				ImagePageCSVmap.add(csvDocumentMasterId);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	public static LCSDocument createNewReportDocument(String fileName, FlexType type, Hashtable dataValues,
			String uniqueId) throws WTPropertyVetoException, WTException {
		LOGGER.info("createNewReportDocument start>>>");
		LCSDocument document = LCSDocument.newLCSDocument();
		document.setFlexType(type);
		document.setName((String) dataValues.get("flexAttname"));

		document.setValue("glDocumentMasterId", uniqueId);

		LOGGER.info("Document Created>>>" + document);

		return document;

	}

	public static LCSDocument findDocumentByMasterId(String masterId, String columnName) throws WTException {
		LCSDocument document = null;

		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendSelectColumn("LCSDocument", "IDA2A2");
		statement.appendFromTable("LCSDocument");
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria("LCSDocument", columnName, masterId, Criteria.EQUALS));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria("LCSDocument", "latestIterationInfo", "1", Criteria.EQUALS));

		SearchResults results = LCSQuery.runDirectQuery(statement);
		if (results.getResultsFound() > 0) {
			FlexObject obj = (FlexObject) results.getResults().elementAt(0);
			document = (LCSDocument) LCSQuery
					.findObjectById("OR:com.lcs.wc.document.LCSDocument:" + obj.getString("LCSDOCUMENT.IDA2A2"));
		}

		return document;
	}

	protected static boolean addSecondaryContent(Hashtable dataValues, String fileName) {
		LOGGER.info("Enter into addSecondaryContent method");
		LCSDocument document = null;
		if (LoadCommon.getValue(dataValues, "DocumentType", false) == null) {
			if ((document = (LCSDocument) LoadCommon.getCache(fileName, "CURRENT_DOCUMENT")) == null) {
				LoadCommon.display("No LCSDocument Exists In Cache !!!");
			}
		} else {
			try {
				//if (!ImagePageCSVmap.contains(glDocumentMasterId)) {
					//return true;
				//}
				document = findDocumentByMasterId(glDocumentMasterId, glDocColumnName);

			} catch (WTException var9) {
				var9.printStackTrace();
				LoadCommon.display("\n#WTException : " + var9.getLocalizedMessage());
			}
		}

		if (document == null) {
			LOGGER.info("Document Not Found!");
			return false;
		} else {
			LOGGER.info("Document Found");

			String secondaryFileName = LoadCommon.getValue(dataValues, "SecondaryContent", true);

			if (secondaryFileName == null) {
				return false;
			}

			secondaryFileName = CustomImagePagesDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content",
					secondaryFileName);

			String secondaryFullPath = secondaryFileName.contains(File.separator) ? secondaryFileName
					: LoadCommon.LOAD_DIRECTORY + "content" + File.separator + secondaryFileName;
			System.out.println("Adding Secondary Content -> secondaryFullPath = "+secondaryFullPath);
			String secondaryContentDescription = LoadCommon.getValue(dataValues, "SecondaryContentDescription", true);
			System.out.println("Print Secondary Content Description is (true): "+secondaryContentDescription);
			if (secondaryContentDescription == null) {
				secondaryContentDescription = "";
			}

			dataValues.put("SecondaryContent", new File(secondaryFullPath).getName());

			String[] secondaryContents = new String[] { secondaryFullPath };
			String[] secondaryContentDescriptions = new String[] { secondaryContentDescription };
			LoadCommon.display("Adding Secondary Content To LCSDocument With Values '" + dataValues + "' ...");

			try {
				Collection<String> urlsForSecondaryContent = document.getUrlsForSecondaryContent(false);
				
				if (urlsForSecondaryContent.size() != 0) {
					LOGGER.info("Secondary Content Found");

					Set<String> matchedFiles = new HashSet<>();
					for (String url : urlsForSecondaryContent) {
						// skipping if already added

						Pattern pattern = Pattern.compile("/([^/?]+)(?:\\?|$)");
						Matcher matcher = pattern.matcher(url);
						String fileNameFromUrl = "";
						if (matcher.find()) {
							fileNameFromUrl = matcher.group(1);
						}
						System.out.println("If urlsForSecondaryContent Found -> Adding Secondary FileName in Set<String> matchedFiles: "+fileNameFromUrl);

						matchedFiles.add(fileNameFromUrl);
					}

					for (int i = 0; i < secondaryContents.length; ++i) {

						File contentFile = new File(secondaryContents[i]);
						
						System.out.println("If urlsForSecondaryContent Found -> associateSecondaryContent is: "+secondaryContents[i]);
						System.out.println("If urlsForSecondaryContent Found -> associateSecondaryContent fileName is : "+contentFile.getName());
						if (!matchedFiles.contains(contentFile.getName())) {
							document = associateSecondaryContentsLoad(document, secondaryContents[i],
									secondaryContentDescriptions[i]);
						}

					}
				} else {
					LOGGER.info("Creating new Secondary Conent");
					
					// creating secondary document
					for (int i = 0; i < secondaryContents.length; ++i) {
						System.out.println("If urlsForSecondaryContent NOT FOUND (Creating new Secondary Content) -> secondaryContents[i] is: "+secondaryContents[i]);
						document = associateSecondaryContentsLoad(document, secondaryContents[i],
								secondaryContentDescriptions[i]);
					}
				}

			} catch (WTPropertyVetoException | WTException var8) {
				var8.printStackTrace();
				LoadCommon.display("\n#Exception : " + var8.getLocalizedMessage());
			}

			LoadCommon.putCache(fileName, document);
			return true;
		}
	}

	public static LCSDocument associateSecondaryContentsLoad(LCSDocument document, String content, String comment)
			throws WTException, WTPropertyVetoException {
		boolean isCheckedVault = false;

		if (FormatHelper.hasContent(content)) {
			File contentFile = new File(content);
			if (contentFile.canRead() && contentFile.length() != 0L) {
				if (!isCheckedVault) {
					checkVault(document);
					isCheckedVault = true;
				}

				try {
					LOGGER.info("associateSecondaryContents in document");
					document = (LCSDocument) ContentHelper.service.getContents(document);
					ApplicationData appData = ApplicationData.newApplicationData(document);
					appData.setRole(ContentRoleType.toContentRoleType("SECONDARY"));
					if (FormatHelper.hasContent(comment)) {
						appData.setDescription(comment);
					}

					ContentServerHelper.service.updateContent(document, appData, content);
					document = (LCSDocument) ContentServerHelper.service.updateHolderFormat(document);
				} catch (PropertyVetoException var12) {
					LOGGER.info("Getting Exception in associateSecondaryContentsLoad method");
					var12.printStackTrace();
					throw new WTException(var12);
				} catch (IOException var13) {
					LOGGER.info("Getting Exception in associateSecondaryContentsLoad method");
					var13.printStackTrace();
					throw new WTException(var13);
				}
			}
		}

		return document;
	}

	private static void checkVault(LCSDocument document) throws WTException {
		Vault vault = StandardFvService.getVault(document, (Site) null, FvVault.class.getName());
		if (vault == null) {
			LOGGER.debug("\t vault is null");
		} else if (LOGGER.isDebugEnabled() && LOGGER.isDebugEnabled()) {
			LOGGER.debug("\t vault " + vault + " enabled " + vault.isWriteEnabled() + " active folder "
					+ StandardFvService.getActiveFolder(vault));
		}

		if (vault != null && (!vault.isWriteEnabled() || StandardFvService.getActiveFolder(vault) == null)) {
			Object[] params = new Object[0];
			throw new LCSException("com.lcs.wc.resource.DocumentRB", "vaultReadOnly_ERR", params);
		}

	}

	protected static boolean associateDocument(Hashtable dataValues, String fileName) {
		boolean ans = false;

		try {
			FlexTyped flextyped = null;
			if (LoadCommon.getValue(dataValues, "FlexTypedType", false) == null) {
				if ((flextyped = (FlexTyped) LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED")) == null) {
					LoadCommon.display("No FlexTyped Exists In Cache !!!");
				}
			} else {
				flextyped = LoadCommon.searchForObject(fileName, "FlexTyped", dataValues,
						LoadCommon.getValue(dataValues, "FlexTypedTable", false));
			}

			if (flextyped == null) {
				return false;
			}

			LCSDocument document = findDocumentByMasterId(glDocumentMasterId, glDocColumnName);
			ans = document == null ? false : associateDocument(flextyped, document);
		} catch (WTException var5) {
			var5.printStackTrace();
			LoadCommon.display("\n#WTException : " + var5.getLocalizedMessage());
		}

		return ans;
	}

	protected static void associateDocument(FlexTyped flextyped, LCSDocument[] documents) {
		for (int i = 0; i < documents.length; ++i) {
			associateDocument(flextyped, documents[i]);
		}

	}

	protected static boolean associateDocument(FlexTyped flextyped, LCSDocument document) {
		if (flextyped == null) {
			LoadCommon.display("#The FlexTyped Object Is Null !!!");
			return false;
		} else if (document == null) {
			LoadCommon.display("#The LCSDocument Is Null !!!");
			return false;
		} else {
			boolean ans = false;

			try {
				WTDocumentMaster documentMaster = document.getMaster();
				LCSDocument[] docList = ExtractDocument.getDocuments((WTObject) flextyped);

				for (int i = 0; i < docList.length; ++i) {
					if (docList[i] == null) {
						throw new WTException("Null associated document");
					}

					if (documentMaster.getPersistInfo().getObjectIdentifier().getStringValue()
							.equals(docList[i].getMaster().getPersistInfo().getObjectIdentifier().getStringValue())) {
						String var10000 = document.getIdentity();
						LoadCommon.display("The Document " + var10000 + " Is Already Associated To The FlexTyped "
								+ flextyped.toString() + " !!!");
						return false;
					}
				}

				ArrayList<String> docIds = new ArrayList();
				docIds.add(document.getMaster().getPersistInfo().getObjectIdentifier().getStringValue());
				DOCUMENT_LOGIC.associateDocuments((WTObject) flextyped, docIds, true);
				ans = true;
			} catch (WTException var6) {
				var6.printStackTrace();
				LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
			}

			return ans;
		}
	}

	protected static boolean addDocumentToCollection(Hashtable dataValues, String fileName) {
		try {
			LCSDocumentCollection documentCollection = (LCSDocumentCollection) LoadCommon.searchForObject(fileName,
					"DocCollection", dataValues);
			if (documentCollection == null) {
				return false;
			} else {

				String type = (String) dataValues.get("Type");
				String glDocMasterId = (String) dataValues.get("glDocumentMasterId");
				FlexType flexType = FlexTypeCache.getFlexTypeFromPath(type);

				String columnName = flexType.getAttribute("glDocumentMasterId").getColumnName();
				LCSDocument document = findDocumentByMasterId(glDocMasterId, columnName);
				if (document == null) {
					return false;
				} else {
					LoadCommon
							.display("Adding LCSDocument To LCSDocumentCollection With Values '" + dataValues + " ...");
					ArrayList id = new ArrayList();
					id.add(document.getPersistInfo().getObjectIdentifier().getStringValue());
					DOCUMENT_LOGIC.addToCollection(documentCollection, id);
					return true;
				}
			}
		} catch (WTException var5) {
			LoadCommon.display("\n#WTException : " + var5.getLocalizedMessage());
			return false;
		}
	}

	public static boolean createImages(WTObject obj) {
		(new Throwable("Don't remove this methos")).printStackTrace();
		LCSDocumentCollection docCol = (LCSDocumentCollection) obj;
		String directory = LoadFlexTyped.getValue(docCol, "imageLocation");
		String[] fileNames = null;
		Hashtable dataValues = new Hashtable();
		String path = LoadCommon.getPathFromFlexType(docCol.getFlexType());
		String flextype = FormatHelper.replaceCharacter(path, "Document Collection\\Image", "Image");
		File file = new File(directory);
		if (file.exists() && file.isDirectory()) {
			fileNames = file.list();
			dataValues.put("Type", flextype);

			for (int i = 0; i < fileNames.length; ++i) {
				dataValues.put("flexAttname", fileNames[i]);
				String var10002 = file.getAbsolutePath();
				fileNames[i] = var10002 + File.separatorChar + fileNames[i];
				dataValues.put("Image", fileNames[i]);
				createImage(dataValues, "NO_FILE");
			}
		} else {
			LoadCommon.display(directory + " Is Not A Directory !!!");
		}

		return true;
	}

	protected static boolean createImage(Hashtable dataValues, String fileName) {
		try {
			if (LoadCommon.DEBUG) {
				LoadCommon.display(dataValues.toString());
			}

			LCSImage image = LCSImage.newLCSImage();
			if ((image = (LCSImage) LoadCommon.getObjectByCriteria(fileName, image, dataValues, "LCSImage")) == null) {
				return true;
			}

			String value = null;
			if ((value = LoadCommon.getValue(dataValues, "DocType", false)) == null) {
				value = "$$Document";
			}

			image.setDocType(DocumentType.toDocumentType(value));
			if ((value = LoadCommon.getValue(dataValues, "DeptList", false)) == null) {
				value = "DOCUMENTATION";
			}

			image.setDepartment(DepartmentList.toDepartmentList(value));
			// String primaryContent;

			String imageFileName = LoadCommon.getValue(dataValues, "Image", false);
			if (imageFileName != null) {
				String renamedImageFile = CustomImagePagesDocumentLoad
						.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "Images", imageFileName);
				String fullImagePath = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + renamedImageFile;

				// Upload the image
				if ((value = LoadCommon.uploadFile(fullImagePath)) != null) {
					image.setThumbnailLocation(IMAGE_URL + "/" + value);
				}
				// DeleteFileHelper.deleteFile(fullImagePath);
			}

			String primaryContent = LoadCommon.getValue(dataValues, "PrimaryContent", false);
			if (primaryContent != null) {
				String renamedPrimaryFile = CustomImagePagesDocumentLoad
						.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content", primaryContent);
				String fullPrimaryPath = LoadCommon.LOAD_DIRECTORY + "content" + File.separator + renamedPrimaryFile;

				if ((value = LoadCommon.getValue(dataValues, "PrimaryContentDescription", false)) == null) {
					value = "";
				}

				if (LoadCommon.doesFileExist(fullPrimaryPath)) {
					LoadCommon.display("Setting Content File '" + fullPrimaryPath + "' For LCSDocument ...");
					dataValues.put("PRIMARYCONTENT", fullPrimaryPath);
					dataValues.put("PRIMARYCONTENT_DESCRIPTION", value);
				} else {
					LoadCommon.display("#WARNING : Content File '" + fullPrimaryPath + "' Does Not Exist !!!");
				}
			}

			return LoadHelper.save(image, dataValues, fileName);
		} catch (WTPropertyVetoException var5) {
			LoadCommon.display("\n#WTPropertyVetoException : " + var5.getLocalizedMessage());
		} catch (WTException var6) {
			LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
		} catch (UnsupportedEncodingException var7) {
			LoadCommon.display("\n#WTException : " + var7.getLocalizedMessage());
		}

		return false;
	}

	protected static boolean addImageToCollection(Hashtable dataValues, String fileName) {
		try {
			LCSDocumentCollection documentCollection = (LCSDocumentCollection) LoadCommon.searchForObject(fileName,
					"DocCollection", dataValues);
			if (documentCollection == null) {
				return false;
			} else {
				LCSImage image = (LCSImage) LoadCommon.searchForObject(fileName, "Image", dataValues);
				if (image == null) {
					return false;
				} else {
					LoadCommon.display("Adding LCSImage To LCSDocumentCollection With Values '" + dataValues + " ...");
					ArrayList imageIds = new ArrayList();
					imageIds.add(image.getPersistInfo().getObjectIdentifier().getStringValue());
					DOCUMENT_LOGIC.addToCollection(documentCollection, imageIds);
					return true;
				}
			}
		} catch (WTException var5) {
			LoadCommon.display("\n#WTException : " + var5.getLocalizedMessage());
			return false;
		}
	}

	protected static boolean createDocument3DModel(Hashtable dataValues, String fileName) {
		boolean ans = false;

		try {
			if (LoadCommon.DEBUG) {
				LoadCommon.display(dataValues.toString());
			}

			LCSDocument document = LCSDocument.newLCSDocument();
			if ((document = (LCSDocument) LoadCommon.getObjectByCriteria(fileName, document, dataValues)) == null) {
				return true;
			}

			String value = null;
			if ((value = LoadCommon.getValue(dataValues, "DocType", false)) == null) {
				value = "$$Document";
			}

			if (!PersistenceHelper.isPersistent(document)) {
				document.setDocType(DocumentType.toDocumentType(value));
			}

			if ((value = LoadCommon.getValue(dataValues, "DeptList", false)) == null) {
				value = "ENG";
			}

			document.setDepartment(DepartmentList.toDepartmentList(value));
			String primaryContent;
			if ((value = LoadCommon.getValue(dataValues, "Image", false)) != null) {
				value = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + value;
				primaryContent = value;
				if ((value = LoadCommon.uploadFile(value)) != null) {
					document.setThumbnailLocation(IMAGE_URL + "/" + value);
				}

				// DeleteFileHelper.deleteFile(primaryContent);
			}

			primaryContent = LoadCommon.getValue(dataValues, "PrimaryContent", false);
			if (primaryContent != null) {
				if ((value = LoadCommon.getValue(dataValues, "PrimaryContentDescription", false)) == null) {
					value = "";
				}

				if (primaryContent.indexOf(File.separator) != 0) {
					primaryContent = LoadCommon.LOAD_DIRECTORY + "content" + File.separator + primaryContent;
				}

				if (LoadCommon.doesFileExist(primaryContent)) {
					LoadCommon.display("Setting Content File '" + primaryContent + "' For LCSDocument ...");
				} else {
					LoadCommon.display("#WARNING : Content File '" + primaryContent + "' Does Not Exist !!!");
					primaryContent = null;
				}
			}

			if (primaryContent != null) {
				dataValues.put("PRIMARYCONTENT", primaryContent);
				dataValues.put("PRIMARYCONTENT_DESCRIPTION", value);
			}

			String productName = LoadCommon.getValue(dataValues, "ProductName");
			String productType = LoadCommon.getValue(dataValues, "ProductType");
			if (!FormatHelper.hasContent(productType)) {
				productType = "Product";
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("productName = " + productName);
				LOGGER.debug("productType = " + productType);
			}

			if (null != productName) {
				FlexType productFlextype = FlexTypeCache.getFlexTypeFromPath(productType);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("productFlextype.getFullName(true)=" + productFlextype.getFullName(true));
				}

				LCSProduct product = null;
				if (productFlextype != null) {
					product = (new LCSProductQuery()).findProductByNameType(productName, productFlextype);
				}

				if (product != null) {
					LOGGER.debug("product Name = " + product.getName());
				}

				if (product != null) {
					document.setValue("ptc3DProductReference", Long.toString(product.getBranchIdentifier()));
				}
			}

			ans = LoadHelper.save(document, dataValues, fileName);
		} catch (WTException | UnsupportedEncodingException | WTPropertyVetoException var10) {
			if (LoadCommon.DEBUG) {
				var10.printStackTrace();
			}

			LoadCommon.display("\n#Exception : " + var10.getLocalizedMessage());
		}

		return ans;
	}

	public static String renameFileIfPrefixed(String directoryPath, String fileName) {
		if (fileName == null || !fileName.matches("^\\d+_.*")) {
			return fileName; // No numeric prefix, return as-is
		}

		String filteredFileName = fileName.substring(fileName.indexOf("_") + 1);
		File originalFile = new File(directoryPath + File.separator + fileName);
		File renamedFile = new File(directoryPath + File.separator + filteredFileName);

		if (originalFile.exists() && !renamedFile.exists()) {
			boolean renamed = originalFile.renameTo(renamedFile);
			if (renamed) {
				return filteredFileName;
			} else {
				System.out.println("Failed to rename file: " + originalFile.getAbsolutePath());
			}
		}
		if (!originalFile.exists() && renamedFile.exists()) {
			return filteredFileName;
		} else {
			return filteredFileName;
		}

	}

}