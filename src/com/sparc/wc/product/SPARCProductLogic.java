package com.sparc.wc.product;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lcs.wc.access.FlexAccessEvaluator;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.construction.LCSConstructionInfo;
import com.lcs.wc.construction.LCSConstructionInfoMaster;
import com.lcs.wc.construction.LCSConstructionLogic;
import com.lcs.wc.construction.LCSConstructionQuery;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.FileRenamer;
import com.lcs.wc.document.IteratedDocumentReferenceLink;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.document.LCSDocumentToObjectLink;
import com.lcs.wc.document.LCSDocumentToVersionLink;
import com.lcs.wc.document.ZipGenerator;
import com.lcs.wc.epmstruct.FlexEPMDocToSpecLinkQuery;
import com.lcs.wc.epmstruct.LCSEPMDocumentDescribeLink;
import com.lcs.wc.epmstruct.LCSEPMDocumentLogic;
import com.lcs.wc.epmstruct.LCSEPMDocumentQuery;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.FlexBOMPartMaster;
import com.lcs.wc.flexbom.LCSFlexBOMLogic;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.flextype.PropertyBasedAttributeValueLogic;
import com.lcs.wc.flextype.RetypeLogic;
import com.lcs.wc.foundation.FavoriteObjectLogic;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSObjectToObjectLink;
import com.lcs.wc.foundation.LCSPluginManager;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSRevisionControlled;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsLogic;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.part.LCSPart;
import com.lcs.wc.part.LCSPartLogic;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.partstruct.FlexPartToSpecLinkQuery;
import com.lcs.wc.partstruct.LCSPartToProductLink;
import com.lcs.wc.partstruct.LCSPartToProductLinkLogic;
import com.lcs.wc.partstruct.LCSPartToProductLinkQuery;
import com.lcs.wc.placeholder.Placeholder;
import com.lcs.wc.planning.PlanLineItem;
import com.lcs.wc.planning.PlanLogic;
import com.lcs.wc.planning.PlanQuery;
import com.lcs.wc.sample.LCSLatestSampleHelper;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleLogic;
import com.lcs.wc.sample.LCSSampleQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonLogic;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.ProductSizingLogic;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSizeUtility;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigLogic;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.sourcing.SourceComponentNumberPlugin;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToComponentLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.specification.SpecOwner;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.ChangedAttributeValue;
import com.lcs.wc.util.DeleteFileHelper;
import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.FlexContainerHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.JarHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.QueuePool;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.util.SynchronizedAttributeHelper;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.util.ZipHelper;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.doc.WTDocumentMaster;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.folder.FolderEntry;
import wt.inf.container.ExchangeContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.method.MethodContext;
import wt.method.RemoteMethodServer;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.part.PartType;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartReferenceLink;
import wt.queue.ProcessingQueue;
import wt.series.Series;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.IterationIdentifier;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.VersionInfo;
import wt.vc.Versioned;
import wt.vc.wip.Workable;
import com.lcs.wc.product.*;

public class SPARCProductLogic extends LCSProductLogic {
  private static final Logger logger = LogManager.getLogger(SPARCProductLogic.class.getName());
  
  private static final String CLASSNAME = SPARCProductLogic.class.getName();
  
  private static final boolean DEBUG;
  
  private static final int VALIDATE_LINK_HEIRARCHY_RECURSIVE_LIMIT;
  
  public static final String PRODUCT_FOLDERLOCATION;
  
  public static final String SKU_FOLDERLOCATION;
  
  public static final Class PRODUCT_SEQUENCE_CLASS;
  
  private static final Class SKU_SEQUENCE_CLASS;
  
  private static final String PRODUCT_LIFECYCLE;
  
  private static final String COLOR_REFERENCE;
  
  private static final String MULTI_SPEC_GEN_CLASS;
  
  private static final boolean ALLOW_MULTPLE_COST_SHEETS;
  
  private static final boolean COPY_DOC_DEFAULT;
  
  public static final String COPY_IN_PROGRESS = "COPY_IN_PROGRESS";
  
  public static final String COPY_PARAMS_MAP = "COPY_PARAMS_MAP";
  
  public static final String COPY_MODE = "copyMode";
  
  public static final String COPY_PARAM_MAP = "copyParamMap";
  
  static final boolean SERVER = RemoteMethodServer.ServerFlag;
  
  public static final String SEASON_ID = "seasonID";
  
  public static final String COLORWAY_DATA = "colorwayData";
  
  public static final String SOURCING_DATA = "sourcingData";
  
  public static final String COSTSHEET_DATA = "costsheetData";
  
  public static final String SIZE_CATEGORY_DATA = "sizecategoryData";
  
  public static final String DESTINATION_DATA = "destinationData";
  
  public static final String SPEC_DATA = "specData";
  
  public static final String SPEC_COMPONENT_DATA = "specComponentData";
  
  public static final String DOCUMENTS_DATA = "documentsData";
  
  public static final String SAMPLE_DATA = "sampleData";
  
  public static final String SAMPLE_DOCS = "sampledocs";
  
  public static final String SAMPLE_IMAGE_PAGES = "sampleImagePages";
  
  public static final String CONSTRUCTION_DATA = "constructionData";
  
  public static final String PRODUCT_IMAGE_PAGES = "productImagePages";
  
  public static final String PRODUCT_DOC3DMODELS = "productDoc3DModels";
  
  public static final String MEASUREMENT_DATA = "measurementData";
  
  public static final String BOM_DATA = "bomData";
  
  public static final String BOL_DATA = "bolData";
  
  public static final String SEASON = "season";
  
  private static final String MULTITPGENERATOR_ASYNCSERVICE_CLASS;
  
  private static final String MULTITPGENERATOR_ASYNCSERVICE_METHOD;
  
  final boolean productDocumentDeepCopy = LCSProperties.getBoolean("com.lcs.wc.document.productDocs.deepCopy");
  
  final boolean skuDocumentDeepCopy = LCSProperties.getBoolean("com.lcs.wc.document.SKUDocs.deepCopy");
  
  final boolean specDocumentDeepCopy = LCSProperties.getBoolean("com.lcs.wc.document.specDocs.deepCopy");
  
  private static FlexType FIT_SAMPLE_ROOT;
  
  static {
    try {
      DEBUG = LCSProperties.getBoolean("com.lcs.wc.product.LCSProductLogic.verbose");
      COPY_DOC_DEFAULT = LCSProperties.getBoolean("com.lcs.wc.product.LCSProductLogic.copyDocRefsOnCopyProduct");
      PRODUCT_FOLDERLOCATION = LCSProperties.get("com.lcs.wc.product.LCSProduct.rootFolder", "/Products");
      SKU_FOLDERLOCATION = LCSProperties.get("com.lcs.wc.product.LCSSKU.rootFolder", "/SKUs");
      PRODUCT_SEQUENCE_CLASS = Class.forName(LCSProperties.get("com.lcs.wc.product.LCSProduct.sequenceName", "com.lcs.wc.product.LCSProductNumberSeq"));
      SKU_SEQUENCE_CLASS = Class.forName(
          LCSProperties.get("com.lcs.wc.product.LCSSKU.sequenceName", "com.lcs.wc.product.LCSSKUNumberSeq"));
      PRODUCT_LIFECYCLE = LCSProperties.get("com.lcs.wc.product.LCSProduct.lifecycle", "Product Lifecycle");
      COLOR_REFERENCE = LCSProperties.get("com.lcs.wc.product.LCSSKU.CreateMultiSKUs.colorReference");
      MULTI_SPEC_GEN_CLASS = LCSProperties.get("com.lcs.wc.product.LCSProductLogic.multiSpecPDFGenClass", "com.sparc.wc.product.SPARCPDFMultiSpecGenerator");
      ALLOW_MULTPLE_COST_SHEETS = LCSProperties.getBoolean("com.lcs.wc.sourcing.useLCSMultiCosting");
      VALIDATE_LINK_HEIRARCHY_RECURSIVE_LIMIT = LCSProperties.get("com.lcs.wc.product.LCSProductLogic.ValidateLinkHeirarchyRecursiveLimit", 50);
      MULTITPGENERATOR_ASYNCSERVICE_CLASS = LCSProperties.get("com.lcs.wc.product.MultiTPGenAsyncService.ClassName", "com.lcs.wc.specification.AsyncTPGenService");
      MULTITPGENERATOR_ASYNCSERVICE_METHOD = LCSProperties.get("com.lcs.wc.product.MultiTPGenAsyncService.AsyncTPGenService", "asyncCreatePDFSpecifications");
      FIT_SAMPLE_ROOT = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.lcs.wc.sample.LCSSample.Product.Fit.Root"));
    } catch (Throwable throwable) {
      System.err.println("LCSProductLogic: Error reading com.lcs.wc.product.* properties");
      throwable.printStackTrace(System.err);
      throw new ExceptionInInitializerError(throwable);
    } 
  }
  

   public String createPDFSpecifications(Collection specIds, Map params, String outputFolder) throws WTException {
    String url = null;
    try {
      Class<?> genClass = Class.forName(MULTI_SPEC_GEN_CLASS);
	 
      if (logger.isDebugEnabled())
        logger.debug("genClass: " + genClass); 
      SPARCPDFMultiSpecGenerator generator = (SPARCPDFMultiSpecGenerator)genClass.newInstance();
      if (logger.isDebugEnabled())
        logger.debug("generator: " + generator); 
	 
	 
      url = generator.createZipForSpecs(specIds, params, outputFolder);
	  
    } catch (Exception e) {
      url = "";
      throw new WTException(e);
    } 
    return url;
  }
  
  
  
  
  
   public String createPDFSpecifications(Collection specIds, Map params, boolean synchronous) throws WTException {
    ClientContext cc = ClientContext.getContext();
    synchronized (cc) {
      Locale clientLocale = (Locale)params.get("requestLocale");
      Locale currentCCLocale = cc.getLocale();
      try {
        if (synchronous) {
			
          String folder = FileLocation.PDFDownloadLocationFiles + "temp";
		  
          File outFile = new File(folder);
          outFile = FileRenamer.rename(outFile);
          outFile.mkdir();
          if (clientLocale != null)
            ClientContext.getContext().setLocale(clientLocale); 
          folder = outFile.getAbsolutePath();
          logger.debug("createPDFSpecifications - Here");
		    
          String fileURL = createPDFSpecifications(specIds, params, folder);
		   
          logger.debug("createPDFSpecifications - Here 2");
          fileURL = (new ZipGenerator()).getURL(fileURL);
		 
          return fileURL;
        } 
        return null;
      } catch (Exception e) {
        e.printStackTrace();
        if (e instanceof WTException)
          throw (WTException)e; 
        throw new WTException(e);
      } finally {
        ClientContext.getContext().setLocale(currentCCLocale);
      } 
    } 
  }
}
