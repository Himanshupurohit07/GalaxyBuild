package com.lcs.wc.load;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.collection.FlexCollection;
import com.lcs.wc.collection.FlexCollectionMaster;
import com.lcs.wc.collection.MatColToCollectionLink;
import com.lcs.wc.collection.MatColToCollectionLinkMaster;
import com.lcs.wc.collection.MatSupToCollectionLink;
import com.lcs.wc.collection.MatSupToCollectionLinkMaster;
import com.lcs.wc.collection.MatToCollectionLink;
import com.lcs.wc.collection.MatToCollectionLinkMaster;
import com.lcs.wc.construction.LCSConstructionInfo;
import com.lcs.wc.construction.LCSConstructionInfoMaster;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.country.LCSCountryMaster;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.flextype.ForiegnKeyDefinition;
import com.lcs.wc.flextype.WTSAttributeValueList;
import com.lcs.wc.flextype.WTSFlexTypeAttribute;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.last.LCSLast;
import com.lcs.wc.last.LCSLastMaster;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialColorQuery;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.media.LCSMedia;
import com.lcs.wc.media.LCSMediaMaster;
import com.lcs.wc.part.LCSPartLogic;
import com.lcs.wc.planning.FlexPlan;
import com.lcs.wc.planning.PlanMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.SeasonGroup;
import com.lcs.wc.season.SeasonGroupMaster;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.ProductSizeCategoryMaster;
import com.lcs.wc.sourcing.CostSheetToColorLink;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLinkMaster;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.OrderConfirmation;
import com.lcs.wc.sourcing.OrderConfirmationMaster;
import com.lcs.wc.sourcing.RFQRequest;
import com.lcs.wc.sourcing.RFQRequestMaster;
import com.lcs.wc.sourcing.RFQResponse;
import com.lcs.wc.sourcing.RFQResponseMaster;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.testing.TestSpecification;
import com.lcs.wc.testing.TestSpecificationMaster;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.kie.api.runtime.conf.BeliefSystemTypeOption;

import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.*;
import wt.fc.WTObject;
import wt.introspection.WTIntrospectionException;
import wt.introspection.WTIntrospector;
import wt.method.MethodContext;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;

public class CustomLoadFlexTyped {
 public static final String DERIVE_STRINGS = "DERIVE_STRINGS";
 public static final String DERIVE_NUMERICS = "DERIVE_NUMERICS";
 protected static String MOA_DELIMITER = "|~*~|";
 public static final String IMAGE_URL = LCSProperties.get("com.lcs.wc.content.imageURL", "/Windchill/images");
 static final boolean DERIVE_PROD_DEF_NAME = LCSProperties.getBoolean("com.lcs.wc.sizing.SizingNamePlugin.useProductSizeDefinitionDerivedName");
 static final String TIME_ZONE = " " + LCSProperties.get("com.lcs.wc.load.timeZone", "GMT");
 static final String DATE_FORMAT = LCSProperties.get("com.lcs.wc.load.dateFormat");
 static boolean once= false;
 public CustomLoadFlexTyped() {
 }
 
 
 

 public static boolean createFlexTypeAttribute(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createFlexTypeAttribute(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createFlexTypeObjectReference(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createFlexTypeObjectReference(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }
 public static boolean createFlexTypeObjectReference2(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createFlexTypeObjectReference2(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }
 public static boolean createFlexTypeObjectReferenceToLink(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createFlexTypeObjectReferenceToLink(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 public static boolean createFlexTypeObjectReferences(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine, Vector<?> returnObjects) {
     return createFlexTypeObjectReferences(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
 }

 @SuppressWarnings("unchecked")
protected static boolean createFlexTypeAttribute(Hashtable<?,?> dataValues, String fileName) {
     FlexTyped CURRENT_FLEXTYPED = (FlexTyped)LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED");
     if (CURRENT_FLEXTYPED == null) {
         LoadCommon.display("#No FlexTyped Object Available In Cache !!!");
         return false;
     } else {
         String attKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
         if (attKey == null) {
             return false;
         } else {
             boolean ans = false;

             try {
                 String attValue;
                 if (attKey.equalsIgnoreCase("End")) {
                     LoadCommon.display("Saving Object " + CURRENT_FLEXTYPED.toString() + " ...");
                     LoadCommon.parseConstantValues((Hashtable<String, Object>) dataValues);
                      LoadCommon.deleteCache(fileName, "CURRENT_FLEXTYPED");
                     attValue = null;
                     if (dataValues.containsKey("DERIVE_STRINGS")) {
                         attValue = (String)dataValues.get("DERIVE_STRINGS");
                     } else {
                         attValue = "true";
                     }

                     MethodContext.getContext().put("DERIVE_STRINGS", attValue);
                     String deriveNumericValue = null;
                     if (dataValues.containsKey("DERIVE_NUMERICS")) {
                         deriveNumericValue = (String)dataValues.get("DERIVE_NUMERICS");
                     } else {
                         deriveNumericValue = "true";
                     }
                 
                     MethodContext.getContext().put("DERIVE_NUMERICS", deriveNumericValue);
                     LCSLogic.deriveFlexTypeValues(CURRENT_FLEXTYPED);
                     MethodContext.getContext().remove("DERIVE_STRINGS");
                     MethodContext.getContext().remove("DERIVE_NUMERICS");
                     ans = LoadHelper.saveNoIterate(CURRENT_FLEXTYPED, fileName);
                     } else {
                	
                     attValue = getMultiLine(LoadCommon.getValue(dataValues, "AttributeValue", false));
                     if (setValue(CURRENT_FLEXTYPED, attKey, attValue, fileName).equals("")) {
                         if (CURRENT_FLEXTYPED instanceof RevisionControlled) {
                        	   setMaster(CURRENT_FLEXTYPED, attKey, attValue);
                         }

                         ans = true;
                     }
                 }
             } catch (WTPropertyVetoException | WTException var7) {
                 var7.printStackTrace();
                 LoadCommon.display("\n#WTException : " + var7.getLocalizedMessage());
             }

             return ans;
         }
     }
 }

 private static int getNameAttUpperLimit(Class<?> masterClass, String attKey) throws WTIntrospectionException {
     return (Integer)WTIntrospector.getClassInfo(masterClass.getName()).getPropertyDescriptor(attKey).getValue("UpperLimit");
 }

 private static void setMaster(FlexTyped obj, String attKey, String attValue) throws WTPropertyVetoException, WTException {
     if (obj instanceof LCSProduct && "productName".equalsIgnoreCase(attKey)) {
         (new LCSPartLogic()).rename((LCSProduct)obj, attValue);
     } else if (obj instanceof LCSSKU && "skuName".equalsIgnoreCase(attKey)) {
         (new LCSPartLogic()).rename((LCSSKU)obj, attValue);
     } else if (obj instanceof LCSSupplier && "name".equalsIgnoreCase(attKey)) {
         ((LCSSupplier)obj).getMaster().setSupplierName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSSupplierMaster.class, "supplierName")));
     } else if (obj instanceof LCSMaterial && "ptcmaterialName".equalsIgnoreCase(attKey)) {
         ((LCSMaterial)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSMaterialMaster.class, "name")));
     } else if (obj instanceof LCSConstructionInfo && "name".equalsIgnoreCase(attKey)) {
         ((LCSConstructionInfo)obj).getMaster().setConstructionInfoName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSConstructionInfoMaster.class, "productRevision")));
     } else if (obj instanceof LCSCountry && "name".equalsIgnoreCase(attKey)) {
         ((LCSCountry)obj).getMaster().setCountryName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSCountryMaster.class, "countryName")));
     } else if (obj instanceof LCSLast && "name".equalsIgnoreCase(attKey)) {
         ((LCSLast)obj).setLastName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSLastMaster.class, "lastName")));
     } else if (obj instanceof LCSMeasurements && "name".equalsIgnoreCase(attKey)) {
         ((LCSMeasurements)obj).getMaster().setMeasurementsName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSMeasurementsMaster.class, "measurementsType")));
     } else if (obj instanceof LCSMedia && "ptcmediaName".equalsIgnoreCase(attKey)) {
         ((LCSMedia)obj).setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSMediaMaster.class, "name")));
     } else if (obj instanceof LCSSourcingConfig && "name".equalsIgnoreCase(attKey)) {
         ((LCSSourcingConfig)obj).getMaster().setSourcingConfigName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSSourcingConfigMaster.class, "sourcingConfigName")));
     } else if (obj instanceof FlexCollection && "name".equalsIgnoreCase(attKey)) {
         ((FlexCollection)obj).setFlexCollectionName(FormatHelper.validateName(attValue, getNameAttUpperLimit(FlexCollectionMaster.class, "flexCollectionName")));
     } else if (obj instanceof FlexPlan && "planName".equalsIgnoreCase(attKey)) {
         ((FlexPlan)obj).setPlanName(FormatHelper.validateName(attValue, getNameAttUpperLimit(PlanMaster.class, "planName")));
     } else if (obj instanceof LCSDocument && "name".equalsIgnoreCase(attKey)) {
         ((LCSDocument)obj).setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(LCSDocument.class, "planName")));
     } else if (obj instanceof MatColToCollectionLink && "name".equalsIgnoreCase(attKey)) {
         ((MatColToCollectionLink)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(MatColToCollectionLinkMaster.class, "name")));
     } else if (obj instanceof MatSupToCollectionLink && "name".equalsIgnoreCase(attKey)) {
         ((MatSupToCollectionLink)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(MatSupToCollectionLinkMaster.class, "name")));
     } else if (obj instanceof MatToCollectionLink && "name".equalsIgnoreCase(attKey)) {
         ((MatToCollectionLink)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(MatToCollectionLinkMaster.class, "name")));
     } else if (obj instanceof OrderConfirmation && "ptcorderConfirmationName".equalsIgnoreCase(attKey)) {
         ((OrderConfirmation)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(OrderConfirmationMaster.class, "name")));
     } else if (obj instanceof ProductSizeCategory && "ptcsizeDefinitionName".equalsIgnoreCase(attKey)) {
         ((ProductSizeCategory)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(ProductSizeCategoryMaster.class, "name")));
     } else if (obj instanceof RFQRequest && "ptcrfqName".equalsIgnoreCase(attKey)) {
         ((RFQRequest)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(RFQRequestMaster.class, "name")));
     } else if (obj instanceof RFQResponse && "responseName".equalsIgnoreCase(attKey)) {
         ((RFQResponse)obj).getMaster().setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(RFQResponseMaster.class, "name")));
     } else if (obj instanceof SeasonGroup && "ptcseasonGroupName".equalsIgnoreCase(attKey)) {
         ((SeasonGroup)obj).setName(FormatHelper.validateName(attValue, getNameAttUpperLimit(SeasonGroupMaster.class, "name")));
     } else if (obj instanceof SeasonGroup && "testSpecName".equalsIgnoreCase(attKey)) {
         ((TestSpecification)obj).setTestSpecName(FormatHelper.validateName(attValue, getNameAttUpperLimit(TestSpecificationMaster.class, "testSpecName")));
     }

 }

 protected static boolean createFlexTypeObjectReference(Hashtable<?, ?> dataValues, String fileName) {
     try {
         if (LoadCommon.DEBUG) {
             LoadCommon.display(dataValues.toString());
         }
         
    	 System.out.println("dataValues->{ "+dataValues+" }");


         String objectType = LoadCommon.getValue(dataValues, "ObjectType", true);
         if (objectType == null) {
             return false;
         } else {
             String objectValue = LoadCommon.getValue(dataValues, "ObjectValue", true);
             String attType1 = LoadCommon.getValue(dataValues, "AttributeType", true);//AttributeType=Business Object\scHTSAssignmetTable
             if (attType1.equals("Business Object\\scHTSAssignmetTable") && objectValue == null) {   
            	 System.out.println("objectValue null set");
                 objectValue="";
              }
             
             
             if (objectValue == null) {
                 return false;
             } else {
                 String objectKey = LoadCommon.getValue(dataValues, "ObjectKey", true);
                 if (objectKey == null) {
                     return false;
                 } else {
                     String objectAttKey = LoadCommon.getValue(dataValues, "ObjectAttKey", true);
                     if (objectAttKey == null) {
                         return false;
                     } else {
                         String attType = LoadCommon.getValue(dataValues, "AttributeType", true);
                         if (attType == null) {
                             return false;
                         } else {
                             String attValue = LoadCommon.getValue(dataValues, "AttributeValue", true);
                             if (attValue == null) {
                                 attValue = "";
                             }

                             String attKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
                             if (attKey == null) {
                                 return false;
                             } else {
                                 WTObject flextyped = null;
                                 if (FormatHelper.hasContent(fileName) && fileName.indexOf("LCSObjectReferences") > -1) {
                                     Hashtable<String, String> table = new Hashtable();
                                     table.put(objectKey, objectValue);
                                     flextyped = LoadCommon.getObjectByAttributes(objectType, table, (String)null, false);
                                 } else {
                                     flextyped = (WTObject)LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED");
                                 }
                            
                                 if (flextyped == null) {
                                     return false;
                                 } else if (!setValue((FlexTyped)flextyped, objectAttKey + "=" + attKey, attValue, fileName).equals("")) {
                                     return false;
                                 } else {
                                     LoadHelper.deriveFlexTypeValues((FlexTyped)flextyped);
                                     PersistenceServerHelper.manager.update(flextyped);
                                     LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", PersistenceHelper.manager.refresh(flextyped));
                                     return true;
                                 }
                             }
                         }
                     }
                 }
             }
         }
     } catch (WTException var11) {
         var11.printStackTrace();
         LoadCommon.display("\n#WTException : " + var11.getLocalizedMessage());
         return false;
     }
 }
 protected static boolean createFlexTypeObjectReference2(Hashtable<?, ?> dataValues, String fileName) {
     try {
         if (LoadCommon.DEBUG) {
             LoadCommon.display(dataValues.toString());
         }
         
    	 System.out.println("dataValues- 2 -->{ "+dataValues+" }");


         String objectType = LoadCommon.getValue(dataValues, "ObjectType", true);
         if (objectType == null) {
             return false;
         } else {
             String objectValue = LoadCommon.getValue(dataValues, "ObjectValue", true);
             String attType1 = LoadCommon.getValue(dataValues, "AttributeType", true);//AttributeType=Business Object\scHTSAssignmetTable
             if (attType1.equals("Business Object\\scHTSAssignmetTable") && objectValue == null) {   
            	 System.out.println("objectValue null set");
                 objectValue="";
              }
               if (objectValue == null) {
                 return false;
             } else {
                 String objectKey = LoadCommon.getValue(dataValues, "ObjectKey", true);
                 if (objectKey == null) {
                     return false;
                 } else {
                     String objectAttKey = LoadCommon.getValue(dataValues, "ObjectAttKey", true);
                     if (objectAttKey == null) {
                         return false;
                     } else {
                         String attType = LoadCommon.getValue(dataValues, "AttributeType", true);
                         if (attType == null) {
                             return false;
                         } else {
                             String attValue = LoadCommon.getValue(dataValues, "AttributeValue", true);
                             if (attValue == null) {
                                 attValue = "";
                             }

                             String attKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
                             if (attKey == null) {
                                 return false;
                             } else {
                                 WTObject flextyped = null;
                                 if (FormatHelper.hasContent(fileName) && fileName.indexOf("LCSObjectReferences") > -1) {
                                     Hashtable<String, String> table = new Hashtable();
                                     table.put(objectKey, objectValue);
                                     flextyped = LoadCommon.getObjectByAttributes(objectType, table, (String)null, false);
                                 } else {
                                   //  flextyped = (WTObject)LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED");
                                	 System.out.println("------------line 371---------");
                                	 Hashtable<String, String> table = new Hashtable();
                                     table.put(objectKey, objectValue);
                                     flextyped = LoadCommon.getObjectByAttributes(objectType, table, (String)null, false);
                                 }
                            
                                 if (flextyped == null) {
                                     return false;
                                 } else if (!setValue((FlexTyped)flextyped, objectAttKey + "=" + attKey, attValue, fileName).equals("")) {
                                     return false;
                                 } else {
                                     LoadHelper.deriveFlexTypeValues((FlexTyped)flextyped);
                                     PersistenceServerHelper.manager.update(flextyped);
                                     LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", PersistenceHelper.manager.refresh(flextyped));
                                     return true;
                                 }
                             }
                         }
                     }
                 }
             }
         }
     } catch (WTException var11) {
         var11.printStackTrace();
         LoadCommon.display("\n#WTException : " + var11.getLocalizedMessage());
         return false;
     }
 }
 
 protected static boolean createFlexTypeObjectReferenceToLink(Hashtable<?, ?> dataValues, String fileName) {
     try {
         String linkIdentifier = LoadCommon.getValue(dataValues, "linkIdentifier", true);
         if (linkIdentifier == null) {
             return false;
         } else {
             String ObjectLinkValue = LoadCommon.getValue(dataValues, "ObjectLinkValue", true);
             if (ObjectLinkValue == null) {
                 return false;
             } else {
                 String ObjectMaterialType = LoadCommon.getValue(dataValues, "ObjectRoleAtype", true);
                 if (ObjectMaterialType == null) {
                     return false;
                 } else {
                     String ObjectMaterialValue = LoadCommon.getValue(dataValues, "ObjectRoleAValue", true);
                     if (ObjectMaterialValue == null) {
                         return false;
                     } else {
                         String ObjectMaterialAttKey = LoadCommon.getValue(dataValues, "ObjectRoleAAttKey", true);
                         if (ObjectMaterialAttKey == null) {
                             return false;
                         } else {
                             String ObjectSupplierType = LoadCommon.getValue(dataValues, "ObjectRoleBtype", true);
                             if (ObjectSupplierType == null) {
                                 return false;
                             } else {
                                 String ObjectSupplierValue = LoadCommon.getValue(dataValues, "ObjectRoleBValue", true);
                                 if (ObjectSupplierValue == null) {
                                     return false;
                                 } else {
                                     String ObjectSupplierAttKey = LoadCommon.getValue(dataValues, "ObjectRoleBAttKey", true);
                                     if (ObjectSupplierAttKey == null) {
                                         return false;
                                     } else {
                                         String ObjectColorType = LoadCommon.getValue(dataValues, "ObjectRoleCtype", true);
                                         String ObjectColorValue = LoadCommon.getValue(dataValues, "ObjectRoleCValue", true);
                                         String ObjectColorAttKey = LoadCommon.getValue(dataValues, "ObjectRoleCAttKey", true);
                                         String ObjectToObjectLinkAttKey = LoadCommon.getValue(dataValues, "ObjectToObjectLinkAttKey", true);
                                         if (ObjectToObjectLinkAttKey == null) {
                                             return false;
                                         } else {
                                             String attType = LoadCommon.getValue(dataValues, "AttributeType", true);
                                             if (attType == null) {
                                                 return false;
                                             } else {
                                                 String attValue = LoadCommon.getValue(dataValues, "AttributeValue", true);
                                                 if (attValue == null) {
                                                     attValue = "";
                                                 }

                                                 String attKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
                                                 if (attKey == null) {
                                                     return false;
                                                 } else {
                                                     Hashtable table;
                                                     if ("MaterialSupplier".equals(linkIdentifier)) {
                                                         LCSMaterialSupplier materialSupplier = LCSMaterialSupplier.newLCSMaterialSupplier();
                                                         if (FormatHelper.hasContent(fileName) && fileName.indexOf("LCSObjectReferences") > -1) {
                                                             table = new Hashtable();
                                                             table.put(ObjectMaterialAttKey, ObjectMaterialValue);
                                                             FlexTyped material = (FlexTyped)LoadCommon.getObjectByAttributes(ObjectMaterialType, table, (String)null, false);
                                                         	 LoadCommon.display("Material is " + material);
															 table = new Hashtable();
                                                             table.put(ObjectSupplierAttKey, ObjectSupplierValue);
                                                             FlexTyped supplier = (FlexTyped)LoadCommon.getObjectByAttributes(ObjectSupplierType, table, (String)null, false);
                                                             LoadCommon.display("Supplier is " + supplier);
                                                             materialSupplier = LCSMaterialSupplierQuery.findMaterialSupplier(((LCSMaterial)material).getMaster(), ((LCSSupplier)supplier).getMaster());
                                                             LoadCommon.display(
																		"Found materialSupplier: " + materialSupplier);
                                                         }

                                                         if (materialSupplier == null) {
                                                             return false;
                                                         }

                                                         if (!setValue(materialSupplier, ObjectToObjectLinkAttKey + "=" + attKey, attValue, fileName).equals("")) {
                                                             return false;
                                                         }

                                                         LoadHelper.deriveFlexTypeValues(materialSupplier);
                                                         PersistenceServerHelper.manager.update(materialSupplier);
                                                     } else if ("MaterialColor".equals(linkIdentifier) && ObjectColorType != null && ObjectColorValue != null && ObjectColorAttKey != null) {
                                                         LCSMaterialColor materialColor = LCSMaterialColor.newLCSMaterialColor();
                                                         table = new Hashtable();
                                                         table.put(ObjectMaterialAttKey, ObjectMaterialValue);
                                                         WTObject material = LoadCommon.getObjectByAttributes(ObjectMaterialType, table, (String)null, false);
                                                         table.put(ObjectSupplierAttKey, ObjectSupplierValue);
                                                         WTObject supplier = LoadCommon.getObjectByAttributes(ObjectSupplierType, table, (String)null, false);
                                                         table.put(ObjectColorAttKey, ObjectColorValue);
                                                         WTObject color = LoadCommon.getObjectByAttributes(ObjectColorType, table, (String)null, false);
                                                         String materialId = FormatHelper.getNumericObjectIdFromObject(((LCSMaterial)material).getMaster());
                                                         String supplierId = FormatHelper.getNumericObjectIdFromObject(((LCSSupplier)supplier).getMaster());
                                                         String colorId = FormatHelper.getNumericObjectIdFromObject(color);
                                                         materialColor = LCSMaterialColorQuery.findMaterialColorsForMaterialSupplierAndColor(materialId, supplierId, colorId);
                                                         if (materialColor == null) {
                                                             return false;
                                                         }

                                                         if (!setValue(materialColor, ObjectToObjectLinkAttKey + "=" + attKey, attValue, fileName).equals("")) {
                                                             return false;
                                                         }

                                                         LoadHelper.deriveFlexTypeValues(materialColor);
                                                         PersistenceServerHelper.manager.update(materialColor);
                                                     }

                                                     return true;
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     } catch (Exception var25) {
         var25.printStackTrace();
         LoadCommon.display("\n#WTException : " + var25.getLocalizedMessage());
         return false;
     }
 }

 protected static boolean createFlexTypeObjectReferences(Hashtable<?, ?> dataValues, String fileName) {
     try {
         String attKey = LoadCommon.getValue(dataValues, "AttributeKey", true);
         if (attKey == null) {
             return false;
         } else {
             String attRefValue = LoadCommon.getValue(dataValues, "AttributeRefValue", true);
             if (attRefValue == null) {
                 return false;
             } else {
                 String attRefKey = LoadCommon.getValue(dataValues, "AttributeRefKey", false);
                 if (attRefKey != null) {
                     attKey = attKey + "=" + attRefKey;
                 }

                 FlexTyped CURRENT_FLEXTYPED = (FlexTyped)LoadCommon.getCache(fileName, "CURRENT_FLEXTYPED");
                 if (CURRENT_FLEXTYPED == null) {
                     LoadCommon.display("#No FlexTyped Object Available In Cache !!!");
                     return false;
                 } else if (!setValue(CURRENT_FLEXTYPED, attKey, attRefValue, fileName).equals("")) {
                     return false;
                 } else {
                     PersistenceServerHelper.manager.update((WTObject)CURRENT_FLEXTYPED);
                     LoadCommon.putCache(fileName, "CURRENT_FLEXTYPED", PersistenceHelper.manager.refresh((WTObject)CURRENT_FLEXTYPED));
                     return true;
                 }
             }
         }
     } catch (WTException var6) {
         LoadCommon.display("\n#WTException : " + var6.getLocalizedMessage());
         return false;
     }
 }

 public static Vector<?> setAttributes(FlexTyped lcsObject, Hashtable<?, ?> dataValues) {
     return setAttributes(lcsObject, dataValues, (String)null);
 }

 public static Vector<String> setAttributes(FlexTyped lcsObject, Hashtable<?, ?> dataValues, String fileName) {
     Vector<String> messages = new Vector();

     try {
         String value = null;
         FlexType flexType = lcsObject.getFlexType();
         if (flexType == null) {
             messages.add("ERROR - #Cannot Set The Attributes For This Object Because The FlexType Has Not Been Set !!!");
             return messages;
         }

         Collection<String> attrKeys = flexType.getAttributeKeyList();
         LoadCommon.display("Setting Attributes For Object " + lcsObject.toString() + " ...");
         Iterator<?> keys = dataValues.keySet().iterator();

         while(keys.hasNext()) {
             value = (String)keys.next();
             if (value.startsWith("flexAtt")) {
                 String key = value.substring(7);
                 value = setValue(lcsObject, key, getMultiLine(LoadCommon.getValue(dataValues, value, false)), fileName);
                 if (!value.equals("")) {
                     messages.add(value);
                 }

                 int indx = key.indexOf(61);
                 if (indx > 0) {
                     key = key.substring(0, indx);
                 }

                 if (attrKeys.contains(key.toUpperCase())) {
                     attrKeys.remove(key.toUpperCase());
                     removeDuplicateKeys(attrKeys, flexType, key);
                 }
             }
         }
     } catch (WTException var10) {
         var10.printStackTrace();
         LoadCommon.display("\n#Exception : " + var10.getLocalizedMessage());
         messages.add("ERROR - #Exception : " + var10.getLocalizedMessage());
         var10.printStackTrace();
     }

     return messages;
 }

 private static void removeDuplicateKeys(Collection<String> attrKeys, FlexType flexType, String key) {
     if (flexType.getFullName(true).indexOf("Material") == 0) {
         if (key.equals("name")) {
             attrKeys.remove("PTCMATERIALNAME");
         } else if (key.equals("ptcmaterialName")) {
             attrKeys.remove("NAME");
         }
     } else if (flexType.getFullName(true).indexOf("Document") == 0) {
         if (key.equals("name")) {
             attrKeys.remove("PTCDOCUMENTNAME");
         } else if (key.equals("ptcdocumentName")) {
             attrKeys.remove("NAME");
         }
     }

 }
 
	
 public static String setValue(FlexTyped obj, String attKey, String attValue) throws WTException {
     return setValue(obj, attKey, attValue, (String)null);
 }

 public static String setValue(FlexTyped obj, String attKey, String attValue, String fileName) throws WTException {
     String value = "";
     boolean attRequired = false;
     Locale currentLocale = null;
     if (obj instanceof ProductSizeCategory && DERIVE_PROD_DEF_NAME && LoadSizeDefinition.NAME.equals(attKey)) {
         return "";
     } else {
    	  

         try {
             String[] values = LoadCommon.getTokens(attKey, "=");
             if (values.length > 1) {
                 attKey = values[0];
                 if (FormatHelper.hasContent(attValue)) {
                     attValue = values[1] + "=" + attValue;
                 }
             }

             FlexType flextype = obj.getFlexType();
             FlexTypeAttribute flextypeAttribute = flextype.getAttribute(attKey);
             String attType = flextypeAttribute.getAttVariableType();
             attRequired = flextypeAttribute.isAttRequired();
             String className;
             if ((flextypeAttribute = flextype.getAttribute(attKey)) == null) {
                 className = "ERROR - #No Attribute Exists With Name '" + attKey + "' For Type '" + LoadCommon.getPathFromFlexType(flextype) + "'!!!";
                 return className;
             }

             if (LoadCommon.needSeasonalObject(obj, flextypeAttribute)) {
                 obj = (FlexTyped)LoadCommon.getCache(fileName, "CURRENT_SEASONPRODUCTLINK");
                 if (obj == null) {
                     className = "#ERROR - No Seasonal FlexTyped Object Available In Cache !!!";
                     return className;
                 }
             }

             className = obj.getClass().getName();
             className = className.substring(className.lastIndexOf(".") + 1);
             String thumbnail;
             if (attValue == null || attValue.equals("")) {
                 if (attRequired) {
                     thumbnail = "#ERROR - The Required Attribute '" + attKey + "' Has A Null Value For The " + className + " !!!";
                     return thumbnail;
                 }

                 LoadCommon.display("Resetting Attribute '" + attKey + "' For Object = '" + obj.toString() + "' ...");
                 if (isAttTypeText(attType)) {
                     obj.setValue(attKey, "");
                 } else if (isAttTypeNum(attType)) {
                     obj.setValue(attKey, "0");
                 } else if (attType.equals("date")) {
                     obj.setValue(attKey, "0");
                 }

                 thumbnail = "";
                 return thumbnail;
             }

             LoadCommon.display("Object = '" + obj.toString() + "' - Attribute Key = '" + attKey + "' Type = '" + attType + "' value = '" + attValue + "' ...");
             if ((attType.equals("userList") || attType.equals("object_ref") || attType.equals("object_ref_list")) && LoadCommon.validateInt(attKey, attValue, false) == 0) {
                 boolean isUser = attType.equals("userList");
                 if (!isUser && (flextype = flextypeAttribute.getRefType()) == null) {
                     ForiegnKeyDefinition def = flextypeAttribute.getRefDefinition();
                     String var14;
                     if (def == null) {
                         var14 = "#ERROR - No ForiegnKeyDefinition Specified For Attribute !!!";
                         return var14;
                     }

                     if (!(isUser = def.getChooserModule().equals("USER")) && (flextype = FlexTypeCache.getFlexTypeRootByClass(def.getFlexTypeClass())) == null) {
                         var14 = "#ERROR - Could Not Retrieve FlexType For Foriegn Key Definition !!!";
                         return var14;
                     }
                 }

                 LoadCommon.display("" + isUser + " - " + attType);
                 values = LoadCommon.getTokens(attValue, "=");
                 attValue = "";
                 if (isUser) {
                     WTUser user = LoadCommon.getUserById(values[0]);
                     if (user != null) {
                         attValue = LoadCommon.getId(user);
                     }
                 } else {
                     attValue = LoadCommon.getObjectIdByAttributes(flextype, values[0], values[1]);
                 }

                 if (attValue.equals("")) {
                     value = "Error Retrieving Object For Attribute " + attKey + "{" + values[0] + "=" + values[1] + "}";
                     value = attRequired ? "#ERROR - " + value : "#WARNING - " + value;
                     LoadCommon.display(value);
                     String var38 = value;
                     if(attKey.equals("scFGFactory")) {
                    	
                    	 System.out.println("-----setting empty------");
                    	 var38="";
                     }
                     return var38;
                 }
             } else if (attType.equals("moaList")) {
                 thumbnail = MOA_DELIMITER;
                 values = LoadCommon.getTokens(attValue, "=");
                 if (values.length > 1) {
                     thumbnail = values[0];
                     attValue = values[1];
                 }

                 values = LoadCommon.getTokens(attValue, thumbnail);
                
                 
                 attValue = "";

                 for(int i = 0; i < values.length; ++i) {
                	 String str = values[i];
                	 if (str.contains("|")) {
                		 str = str.replace("|", "").trim();
                     } else {
                    	 str = str.trim();
                     }
                	
                     if (FormatHelper.hasContent(str) && !(value = getKeyForList(flextypeAttribute, str)).equals("")) {
                         if (!value.startsWith("#ERROR") && !value.startsWith("#WARNING")) {
                             attValue = attValue + value + MOA_DELIMITER;
                         } else {
                             LoadCommon.display(value);
                         }
                     }
                 }
             } else if (attType.equals("boolean")) {
                 attValue = String.valueOf(LoadCommon.validateBoolean(attKey, attValue));
             } else if (!attType.equals("choice") && !attType.equals("driven")) {
                 if (attType.equals("date")) {
                     thumbnail = setDate(obj, flextypeAttribute, attValue);
                     return thumbnail;
                 }

                 if (attType.equals("image")) {
                     attValue = LoadCommon.LOAD_DIRECTORY + "Images" + File.separator + attValue;
                     thumbnail = attValue;
                     attValue = LoadCommon.uploadFile(attValue);
                     attValue = attValue == null ? "" : IMAGE_URL + "/" + attValue;
                     DeleteFileHelper.deleteFile(thumbnail);
                 }
             } else {
                 attValue = getKeyForList(flextypeAttribute, attValue);
                 if (attValue.startsWith("#ERROR") || attValue.startsWith("#WARNING")) {
                     thumbnail = attValue;
                     return thumbnail;
                 }
             }

             LoadCommon.display("Setting Attribute '" + attKey + "' Of Type '" + attType + "' To '" + attValue + "' For Object " + obj.toString() + " ...");
             if (currentLocale != Locale.US && (attType.equals("float") || attType.equals("currency") || attType.equals("integer") || attType.equals("sequence") || attType.equals("uom"))) {
                 currentLocale = ClientContext.getContextLocale();
                 ClientContext.getContext().setLocale(Locale.US);
             }

             obj.setValue(attKey, attValue);
             thumbnail = "";
             return thumbnail;
         } catch (WTException | UnsupportedEncodingException | WTPropertyVetoException var33) {
             if (LoadCommon.DEBUG) {
                 var33.printStackTrace();
             }

             value = "#Exception : " + var33.getLocalizedMessage();
             LoadCommon.display(value);
             var33.printStackTrace();
         } finally {
             if (currentLocale != null) {
                 try {
                     ClientContext.getContext().setLocale(currentLocale);
                 } catch (WTException var32) {
                     var32.printStackTrace();
                 }
             }

         }

         value = attRequired ? "#ERROR - " + value : "#WARNING - " + value;
         LoadCommon.display(value);
         return value;
     }
 }

 static boolean isAttTypeText(String attType) {
     if (!FormatHelper.hasContent(attType)) {
         return false;
     } else {
         return attType.equals("boolean") || attType.equals("careWashImages") || attType.equals("careWashImages") || attType.equals("colorSelect") || attType.equals("image") || attType.equals("moaEntry") || attType.equals("moaList") || attType.equals("multiobject") || attType.equals("choice") || attType.equals("text") || attType.equals("textArea") || attType.equals("url");
     }
 }

 static boolean isAttTypeNum(String attType) {
     if (!FormatHelper.hasContent(attType)) {
         return false;
     } else {
         return attType.equals("currency") || attType.equals("float") || attType.equals("integer") || attType.equals("uom") || attType.equals("userList") || attType.equals("object_ref") || attType.equals("object_ref_list");
     }
 }

 public static String setDate(FlexTyped obj, FlexTypeAttribute flextypeAttribute, String attValue) {
     String value = "";

     try {
         SimpleDateFormat dateFormat = null;
         String[] values = LoadCommon.getTokens(attValue, "=");
         if (values.length > 1) {
             if (FormatHelper.hasContent(values[0]) && values[0].indexOf("mm/") > -1) {
                 values[0] = values[0].replace("mm/", "MM/");
             }

             dateFormat = new SimpleDateFormat(values[0]);
             attValue = values[1];
         } else if (attValue.indexOf(":") > -1) {
             dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S z");
         } else {
             dateFormat = new SimpleDateFormat("dd-MMM-yyyy z");
         }

         if (FormatHelper.hasContent(DATE_FORMAT)) {
             dateFormat = new SimpleDateFormat(DATE_FORMAT + " z");
         }

         Timestamp stamp = new Timestamp(dateFormat.parse(attValue + TIME_ZONE).getTime());
         obj.setValue(flextypeAttribute.getAttKey(), stamp);
         return value;
     } catch (Exception var7) {
         var7.printStackTrace();
         value = "Exception : " + var7.getLocalizedMessage();
         LoadCommon.display(value);
         if (flextypeAttribute.isAttRequired()) {
             value = "#ERROR - " + value;
         } else {
             value = "#WARNING - " + value;
         }

         return value;
     }
 }

 public static String getValue(FlexTyped object, String attKey) {
     String value = "";

     try {
         Object obj = object.getValue(attKey);
         if (obj == null) {
             return value;
         }

         value = obj.toString();
         value = value.replaceAll(LoadCommon.LINEFEED, "<-NextLine->");
         value = value.replaceAll("\n", "<-NextLine->");
     } catch (WTException var4) {
         var4.printStackTrace();
         LoadCommon.display("\n#WTException : " + var4.getLocalizedMessage());
         value = "";
     }

     return value;
 }

 public static String getKeyForList(FlexTypeAttribute flextypeAttribute, String value) {
     WTSFlexTypeAttribute attr = (WTSFlexTypeAttribute)flextypeAttribute;
     WTSAttributeValueList wtsList = (WTSAttributeValueList)attr.getAttValueList();
     
         if (wtsList.getKeys().contains(value)) {
         return value;
     } else {
         if (flextypeAttribute.isAttRequired()) {
             value = "#ERROR - The Value '" + value + "' For Mandatory Attribute '" + flextypeAttribute.getAttKey() + "' Is Not A Valid Value In The List !!!";
         } else {
             value = "#WARNING - The Value '" + value + "' For Non-Mandatory Attribute '" + flextypeAttribute.getAttKey() + "' Is Not A Valid Value In The List !!!";
         }

         LoadCommon.display(value);
         return value;
     }
 }

 protected static String getMultiLine(String value) {
     if (value == null) {
         return value;
     } else {
         boolean index = false;

         while(true) {
             int index1 = value.indexOf("<-NextLine->");
             if (index1 == -1) {
                 return value;
             }

             String var10000 = value.substring(0, index1);
             value = var10000 + LoadCommon.LINEFEED + value.substring(index1 + 12);
         }
     }
 }

 public static void main(String[] args) {
     LoadCommon.SERVER_MODE = false;
     Hashtable<String, String> table = new Hashtable();
     table.put("ObjectType", "Business Object\\Lookup Tables\\Footwear Lookup Tables");
     table.put("ObjectValue", "Footwear Phase 2 Lookup Tables");
     table.put("ObjectKey", "name");
     table.put("ObjectAttKey", "Season");
     table.put("AttributeType", "\\Footwear Season");
     table.put("AttributeValue", "Footwear Q1 2003");
     table.put("AttributeKey", "seasonName");
     createFlexTypeObjectReference(table, "D:\\File.txt");
     createFlexTypeObjectReferenceToLink(table, "D:\\File.txt");
     LoadCommon.SERVER_MODE = true;
 }
}

