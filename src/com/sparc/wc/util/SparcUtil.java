package com.sparc.wc.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lcs.wc.change.LCSChangeActivity;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FlexContainerHelper;
import com.lcs.wc.util.FormatHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Predicate;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.inf.container.ExchangeContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.query.template.ReportTemplate;
import wt.query.template.ReportTemplateHelper;
import wt.query.template.ReportTemplateQueryHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import static com.sparc.wc.util.SparcConstants.CHOICE;
import static com.sparc.wc.util.SparcConstants.DRIVEN;
import static com.sparc.wc.util.SparcConstants.OBJECT_REF;
import static com.sparc.wc.util.SparcConstants.OBJECT_REF_LIST;
import static com.sparc.wc.util.SparcConstants.USER_LIST;
import static com.sparc.wc.util.SparcConstants.EMPTY_STRING;
import static com.sparc.wc.util.SparcConstants.FULL_NAME;
import static com.sparc.wc.util.SparcConstants.COLON_DELIMITER;


public class SparcUtil {

	private static final Logger LOGGER = LogManager.getLogger(SparcUtil.class.getName());


	public static String getAttListValue(String attributeKey, FlexTyped typed)  {
		String key = "";
		String value = "";
		FlexTypeAttribute typeAttribute;
			try {					
			typeAttribute = typed.getFlexType().getAttribute(attributeKey);
			value = (String) typed.getValue(attributeKey);
			AttributeValueList valueList = typeAttribute.getAttValueList();
			if (valueList != null) {
				value = valueList.getValue(value, null);
			}
			}catch(WTException ex) {
				ex.printStackTrace();
			}
			return value;
	}
	
	
	/**
	 * 
	 * @param headerNames
	 * @return
	 */
	public static List<Map.Entry<String, String>> getListFromBrandContext(String headerNames){		
		List<Map.Entry<String, String>> list = new ArrayList<>();
		for (String entry : headerNames.split(SparcConstants.COMMA_DELIMITER)) {
			String[] keyValue = entry.split(SparcConstants.TILDE_DELIMITER);
			if (keyValue.length == 2) {
				list.add(new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]));
			}
		}
		return list;
	}



	/*public static String getObjectFrom(Map<String,FlexTyped> mapFlexType, String strKeyObjectIdentifier) {
		String value = "";
		value = getObjectFrom(mapFlexType.get(strKeyObjectIdentifier.split(COLON_DELIMITER)[1]), strKeyObjectIdentifier.split(COLON_DELIMITER)[0]);
		if (value != null && !value.equals(""))
			return value;

		return EMPTY_STRING;
	}

	public static String getObjectFrom(FlexTyped flexTyped, String key) {
		LCSSupplier objSupplier = null;
		FlexObject objUser = null;
		ProductSizeCategory objSizeCategory = null;
		String size1Values = "";
		String attVariableType = "";
		try {
			if(!"scSizeRange".equalsIgnoreCase(key)){
				attVariableType = flexTyped.getFlexType().getAttribute(key).getAttVariableType();
			}
			if(FormatHelper.equalsWithNull(flexTyped,null)){
				return EMPTY_STRING;
			}
			else if("scSizeRange".equalsIgnoreCase(key)){
				objSizeCategory = (ProductSizeCategory) flexTyped;
				size1Values = objSizeCategory.getSizeValues();
				String[] size1Array = size1Values.split("\\|~\\*~\\|");
				return String.join(",",size1Array);
			}
			else if (attVariableType.equalsIgnoreCase(CHOICE) || attVariableType.equalsIgnoreCase(DRIVEN))
			{
				getDisplayValueOfEnum(flexTyped.getFlexType(), (String) flexTyped.getValue(key), key);
				return getAttListValue(key, flexTyped);
			}
			else if(OBJECT_REF_LIST.equalsIgnoreCase(attVariableType)|| OBJECT_REF.equalsIgnoreCase(attVariableType)) {
				if(flexTyped.getValue(key)!=null) {
					if(flexTyped.getValue(key) instanceof LCSSupplier) {
						objSupplier = (LCSSupplier)flexTyped.getValue(key);
						return objSupplier.getName();
					}
				}
			}
			else if(USER_LIST.equalsIgnoreCase(attVariableType)) {
				objUser = flexTyped.getValue(key) != null? (FlexObject)flexTyped.getValue(key): null;
				if(FormatHelper.equalsWithNull(objUser, null)) {
					return EMPTY_STRING;
				}
				else {
					return (String)objUser.getData(FULL_NAME);
				}
			}
			else if("date".equalsIgnoreCase(attVariableType)){
				System.out.println("inside date attribute");
				if(flexTyped.getValue(key) != null){
					SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					String dateString = dateFormat.format((Date)flexTyped.getValue(key));
					System.out.println(dateString);
					return dateString;
				}
				else{
					return EMPTY_STRING;
				}

			}

			return flexTyped.getValue(key)!=null? (String)flexTyped.getValue(key) : EMPTY_STRING;
		} catch (Exception e) {
			e.printStackTrace();
			return EMPTY_STRING;
		}
	}*/

	public static String getObjectFrom(Map<String, FlexTyped> mapFlexType, String strKeyObjectIdentifier) {
		String[] keyParts = strKeyObjectIdentifier.split(COLON_DELIMITER);
		String attributeName = keyParts[0];
		String objectIdentifier = keyParts[1];
		try {
			LOGGER.debug("**strKeyObjectIdentifier:"+strKeyObjectIdentifier);
			FlexTyped flexTypedObject = mapFlexType.get(objectIdentifier);
			Predicate<FlexTyped> isFlexTypedNullOrEmpty = flexTyped -> FormatHelper.equalsWithNull(flexTyped, null);
			if (isFlexTypedNullOrEmpty.test(flexTypedObject)) {
				return EMPTY_STRING;
			}
			if ("scSizeRange".equalsIgnoreCase(attributeName) && flexTypedObject instanceof ProductSizeCategory) {
				return getSizeRangeValue(flexTypedObject);
			}else if (("scLsizerange".equalsIgnoreCase(attributeName) ||"scAeroSizeRange".equalsIgnoreCase(attributeName)) && flexTypedObject instanceof LCSProduct) {
				return getSizeRangeValuefromProduct(flexTypedObject,attributeName);
			}else if (isChoiceOrDrivenAttribute(flexTypedObject, attributeName,objectIdentifier)) {
				return getChoiceOrDrivenValue(flexTypedObject, attributeName, objectIdentifier);
			} else if (isSupplierAttribute(flexTypedObject,attributeName)) {
				return getSupplierName(flexTypedObject, attributeName);
			} else if (isUserAttribute(flexTypedObject,attributeName)) {
				return getUserFullName(flexTypedObject,attributeName);
			} else if (isDateAttribute(flexTypedObject,attributeName)) {
				return getDateValue(flexTypedObject,attributeName);
			}

			return flexTypedObject.getValue(attributeName)!=null? (String)flexTypedObject.getValue(attributeName) : EMPTY_STRING;
		}catch(Exception ex){
			ex.printStackTrace();
			LOGGER.debug("Error while fetching the data----"+ex.getLocalizedMessage());
		}

		return EMPTY_STRING;
	}

	private static String getSizeRangeValue(FlexTyped flexTyped) {
		if (flexTyped == null) {
			return EMPTY_STRING;
		}
		ProductSizeCategory objSizeCategory = (ProductSizeCategory) flexTyped;
		String sizeValues = objSizeCategory.getSizeValues();
		String[] sizeArray = sizeValues.split("\\|~\\*~\\|");
		return String.join(",", sizeArray);
	}
	//changes for RM Task 10480 - start
	private static String getSizeRangeValuefromProduct(FlexTyped flexTyped, String attributeName) throws WTException {
		if (flexTyped == null) {
			return EMPTY_STRING;
		}
		LOGGER.debug("**flexTyped"+flexTyped+"*attributeName:"+attributeName);
		LCSProduct product = (LCSProduct) flexTyped;
		String sizeRangeValue = ((LCSLifecycleManaged)product.getValue(attributeName)).getName();
		LOGGER.debug("**getSizeRangeValuefromProduct-sizeRangeValue"+sizeRangeValue);
		return sizeRangeValue ;
	}
	//changes for RM Task 10480 - end
	private static boolean isChoiceOrDrivenAttribute(FlexTyped flexTyped, String attributeName,String objectIdentifier) throws WTException {
		FlexTypeAttribute objAttribute = flexTyped.getFlexType().getAttribute(attributeName);
		String attVariableType = objAttribute.getAttVariableType();
		return attVariableType.equalsIgnoreCase(CHOICE) || attVariableType.equalsIgnoreCase(DRIVEN);
	}

	private static String getChoiceOrDrivenValue(FlexTyped flexTyped, String attributeName, String objectIdentifier) throws WTException {
		String displayValueOfEnum = getDisplayValueOfEnum(flexTyped.getFlexType(), (String) flexTyped.getValue(attributeName), attributeName);
		return displayValueOfEnum;
	}

	private static boolean isSupplierAttribute(FlexTyped flextyped,String attributeName) throws WTException {
		String attributeType = flextyped.getFlexType().getAttribute(attributeName).getAttVariableType();
		return OBJECT_REF_LIST.equalsIgnoreCase(attributeType) || OBJECT_REF.equalsIgnoreCase(attributeType);
	}



	private static String getSupplierName(FlexTyped flexTyped,String key) throws WTException {
		if (flexTyped != null && flexTyped.getValue(key) instanceof LCSSupplier) {
			LCSSupplier objSupplier = (LCSSupplier) flexTyped.getValue(key);
			return objSupplier.getName();
		}
		return EMPTY_STRING;
	}

	private static boolean isUserAttribute(FlexTyped flexTyped,String attributeName) throws WTException{
		var attributeType = flexTyped.getFlexType().getAttribute(attributeName).getAttVariableType();
		return USER_LIST.equalsIgnoreCase(attributeType);
	}

	private static boolean isDateAttribute(FlexTyped flexTyped,String attributeName) throws WTException {
		String attributeType = flexTyped.getFlexType().getAttribute(attributeName).getAttVariableType();
		return "date".equalsIgnoreCase(attributeType);
	}

	private static String getUserFullName(FlexTyped flexTyped,String strKey) throws WTException {
		FlexObject objUser = flexTyped != null ? (FlexObject) flexTyped.getValue(strKey) : null;
		if (objUser != null) {
			return (String) objUser.getData(FULL_NAME);
		}
		return EMPTY_STRING;
	}

	private static String getDateValue(FlexTyped flexTyped,String strKey) throws WTException {
		if (flexTyped != null && flexTyped.getValue(strKey) instanceof Date) {
			Date dateValue = (Date) flexTyped.getValue(strKey);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			return dateFormat.format(dateValue);
		}
		return EMPTY_STRING;
	}

	private static String getDisplayValueOfEnum(FlexType type, String keyValue, String attKey)
	            throws WTException {
	        String attValue = "";
	        if (FormatHelper.hasContentAllowZero(keyValue)) {
	            FlexTypeAttribute flexAtt = type.getAttribute(attKey);
	            AttributeValueList attList = flexAtt.getAttValueList();
	            attValue = attList.getValue(keyValue, Locale.getDefault());
	        }	       
	        return attValue;
	    }

	/**
	 *
 	 * @param reportName
	 * @return
	 */
	public static String fetchQMLURLForReport(String reportName){
		try {
			SessionHelper.getPrincipal();
			WTContainerRef exchangeConRef = WTContainerHelper.service.getExchangeRef();
			ExchangeContainer container = (ExchangeContainer)exchangeConRef.getObject();
			DirectoryContextProvider dcp = container.getContextProvider();
			WTOrganization org = OrganizationServicesHelper.manager.getOrganization (FlexContainerHelper.getOrganizationContainerName(), dcp);
			WTContainerRef containerRef = WTContainerHelper.service.getOrgContainerRef(org);
			ReportTemplate reportTem = ReportTemplateQueryHelper.find(reportName, containerRef, true);
			String url = ReportTemplateHelper.getGenerateFormURL(null, reportTem).toString().replace("adhocReportCriteria.jsp", "runReportTemplate.jsp");
			System.out.println("URL is ----> "+url);
			return url;
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}

}
