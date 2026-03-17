package com.lcs.wc.load;

import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentCollection;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.document.LCSImage;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.doc.DepartmentList;
import wt.doc.DocumentType;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class CustomDocumentLoad {
	private static final Logger LOGGER = LogManager.getLogger(LoadDocument.class.getName());
	protected static LCSDocumentLogic DOCUMENT_LOGIC = new LCSDocumentLogic();
	protected static LCSDocumentQuery DOCUMENT_QUERY = new LCSDocumentQuery();
	public static final String IMAGE_URL = LCSProperties.get("com.lcs.wc.content.imageURL", "/Windchill/images");
	public static String CONTENT_LOAD_LOCATION = "";

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
		System.out.println("inside the createDocument method");
		boolean ans = false;

		System.out.println("dataValues ::"+dataValues);
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
			String imageFile = LoadCommon.getValue(dataValues, "Image", false);
			System.out.println("imageFile ::"+imageFile);
			if (imageFile != null) {
				imageFile = CustomDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "Images", imageFile);
				System.out.println("imageFile 2nd ::"+imageFile);
				String imageFullPath = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + imageFile;
			    System.out.println("imageFullPath ::"+imageFullPath);
				String uploadedName = LoadCommon.uploadFile(imageFullPath);
				System.out.println("uploadedName ::"+uploadedName);
				if (uploadedName != null) {
					document.setThumbnailLocation(IMAGE_URL + "/" + uploadedName);
				}
				DeleteFileHelper.deleteFile(imageFullPath);
			}

			String primaryFile = LoadCommon.getValue(dataValues, "PrimaryContent", false);
			System.out.println("primaryFile ::" + primaryFile);

			if (primaryFile != null) {
				primaryFile = CustomDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content",
						primaryFile);
				System.out.println("primaryFile 2nd :: " + primaryFile);
				String primaryContentFullPath = LoadCommon.LOAD_DIRECTORY + "content" + File.separator + primaryFile;
				System.out.println("primaryContentFullPath ::" + primaryContentFullPath);
				if (LoadCommon.doesFileExist(primaryContentFullPath)) {
					LoadCommon.display("Setting Content File '" + primaryContentFullPath + "' For LCSDocument ...");
					dataValues.put("PRIMARYCONTENT", primaryContentFullPath);
					String description = LoadCommon.getValue(dataValues, "PrimaryContentDescription", false);
					if (description == null) {
						description = "";
					}
					dataValues.put("PRIMARYCONTENT_DESCRIPTION", description);
				} else {
					LoadCommon.display("#WARNING : Content File '" + primaryContentFullPath + "' Does Not Exist !!!");
				}

			}

			ans = LoadHelper.save(document, dataValues, fileName);
		} catch (WTException | UnsupportedEncodingException | WTPropertyVetoException var6) {
			if (LoadCommon.DEBUG) {
				var6.printStackTrace();
			}

			LoadCommon.display("\n#Exception : " + var6.getLocalizedMessage());
		}

		return ans;
	}


	protected static boolean addSecondaryContent(Hashtable dataValues, String fileName) {
		
		System.out.println("addSecondaryContent inside  the method");
		LCSDocument document = null;
		if (LoadCommon.getValue(dataValues, "DocumentType", false) == null) {
			if ((document = (LCSDocument) LoadCommon.getCache(fileName, "CURRENT_DOCUMENT")) == null) {
				LoadCommon.display("No LCSDocument Exists In Cache !!!");
			}
		} else {
			try {
				document = (LCSDocument) LoadCommon.searchForObject(fileName, "Document", dataValues);
			} catch (WTException var9) {
				var9.printStackTrace();
				LoadCommon.display("\n#WTException : " + var9.getLocalizedMessage());
			}
		}

		if (document == null) {
			return false;
		} else {

			String secondaryFileName = LoadCommon.getValue(dataValues, "SecondaryContent", true);
			if (secondaryFileName == null) {
				return false;
			}

			secondaryFileName = CustomDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "content",
					secondaryFileName);

			String secondaryFullPath = secondaryFileName.contains(File.separator) ? secondaryFileName
					: LoadCommon.LOAD_DIRECTORY + "content" + File.separator + secondaryFileName;
			String secondaryContentDescription = LoadCommon.getValue(dataValues, "SecondaryContentDescription", false);
			if (secondaryContentDescription == null) {
				secondaryContentDescription = "";
			}
			dataValues.put("SecondaryContent", new File(secondaryFullPath).getName());

			String[] secondaryContents = new String[] { secondaryFullPath };
			String[] secondaryContentDescriptions = new String[] { secondaryContentDescription };
			LoadCommon.display("Adding Secondary Content To LCSDocument With Values '" + dataValues + "' ...");

			try {
				document = DOCUMENT_LOGIC.associateSecondaryContentsLoad(document, secondaryContents,
						secondaryContentDescriptions);
			} catch (WTPropertyVetoException | WTException var8) {
				var8.printStackTrace();
				LoadCommon.display("\n#Exception : " + var8.getLocalizedMessage());
			}

			LoadCommon.putCache(fileName, document);
			return true;
		}
	}

	protected static boolean associateDocument(Hashtable dataValues, String fileName) {
		System.out.println("---------------- inside the associateDocument ----------------");
		System.out.println("----------fileName--------------- " + fileName);
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

			LCSDocument document = (LCSDocument) LoadCommon.searchForObject(fileName, "Document", dataValues);
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
		System.out.println("inside the associateDocument method");
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
				LCSDocument document = (LCSDocument) LoadCommon.searchForObject(fileName, "Document", dataValues);
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
		System.out.println("Inside the create Images method");
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
		System.out.println("inside the createImage method 2");
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
				String renamedImageFile = CustomDocumentLoad.renameFileIfPrefixed(LoadCommon.LOAD_DIRECTORY + "Images",
						imageFileName);
				String fullImagePath = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + renamedImageFile;

				// Upload the image
				if ((value = LoadCommon.uploadFile(fullImagePath)) != null) {
					image.setThumbnailLocation(IMAGE_URL + "/" + value);
				}
				DeleteFileHelper.deleteFile(fullImagePath);
			}

			String primaryContent = LoadCommon.getValue(dataValues, "PrimaryContent", false);
			if (primaryContent != null) {
				String renamedPrimaryFile = CustomDocumentLoad
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
		System.out.println("addImageToCollection  method ");
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
		System.out.println("createDocument3DModel inside this method");
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

				DeleteFileHelper.deleteFile(primaryContent);
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
				System.out.println("Renamed file: " + originalFile.getName() + " --> " + renamedFile.getName());
				return filteredFileName;
			} else {
				System.out.println("Failed to rename file: " + originalFile.getAbsolutePath());
			}
		} else {
			System.out.println("Original file not found or target already exists: " + originalFile.getAbsolutePath());
		}

		return fileName; // Return original if rename failed
	}

}