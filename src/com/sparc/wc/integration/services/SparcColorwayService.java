package com.sparc.wc.integration.services;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductClientModel;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUClientModel;
import com.lcs.wc.season.*;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import com.sparc.wc.integration.constants.SparcIntegrationConstants;
import com.sparc.wc.integration.domain.SparcApiCallLogEntry;
import com.sparc.wc.integration.domain.SparcColorwayUpdateRequest;
import com.sparc.wc.integration.domain.SparcColorwayDetails;
import com.sparc.wc.integration.domain.SparcColorwayIndexRequest;
import com.sparc.wc.integration.domain.SparcColorwayIndices;
import com.sparc.wc.integration.domain.SparcColorwayProcesses;
import com.sparc.wc.integration.domain.SparcColorwayUpdateResponse;
import com.sparc.wc.integration.domain.SparcPropertyDefinitions;
import com.sparc.wc.integration.domain.SparcTypeAttributeCollection;
import com.sparc.wc.integration.exceptions.SparcEntityNotFoundException;
import com.sparc.wc.integration.repository.ColorwayRepository;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.vc.Versioned;
import com.lcs.wc.product.LCSSKUClientModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_COLORWAY_CAP_SHARED_ATTR;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.AERO_SENT_YES_VALUE;
import static com.sparc.wc.integration.constants.SparcIntegrationConstants.FC_SHARED_ATTR;

/**
 * SparcColorwayService class<br>
 *
 * FIXES/AMENDMENTS:<br>
 * - Task #10314 (Hypercare): Updates to colorway index logic components and methods to optimize execution time: Updated methods "getColorwayIndices" and "getColorwayDetails".
 * Removed Added logger to print relevant activity.
 * - Task #10390 (Hypercare): Fixed HTS logic in both FC and CAP lucky colorway integrations.
 * - Task #????: Sending lucky colorway to FC is incorrectly triggering the Sent To CAP flag to reset.<br>
 * - Updated methods "markUpdatedOnColorway" and "markColorway": Replaced use of "LCSSeasonSKULinkClientModel" by "LCSLogic" approach to skip execution of other SSP.<br>
 * - Updated method "markUpdatedOnProduct": Replaced use of "LCSProductClientModel" by "LCSLogic" approach to skip execution of other SSP.<br>
 *
 * @author Acnovate
 */
public class SparcColorwayService {

    private static final Logger LOGGER = LogR.getLogger(SparcColorwayService.class.getName());

    public SparcColorwayIndices getColorwayIndices(final SparcColorwayIndexRequest request) throws WTException {

        final Set<String> colorwayIds = ColorwayRepository.findColorwayIds(request.getFrom(), request.getTo(), request.getProcess());
        LOGGER.info("[getColorwayIndices for process " + request.getProcess() + "] Found unique " + colorwayIds.size() + " colorway ids.");

        final SparcColorwayIndices indices = new SparcColorwayIndices();

        indices.addColorways(colorwayIds);

        return indices;
    }

    public List<SparcColorwayDetails> getColorwayDetails(final String paddedColorwayId, final SparcColorwayProcesses process, final boolean isVerify) throws WTException, SparcEntityNotFoundException {
        final String colorwayId = trimZeroPrefix(paddedColorwayId);
        if (!FormatHelper.hasContent(colorwayId)) {
            return null;
        }

        final Set<String> colorwaySeasonIds = ColorwayRepository.findColorwaySeasonIds(colorwayId, process);
		
		System.out.println("colorwaySeasonIds----------------------"+colorwaySeasonIds);
        LOGGER.info("[getColorwayDetails for process " + process + "] Found unique " + colorwaySeasonIds.size() + " colorway season ids for colorway #" + colorwayId + ".");

        if (colorwaySeasonIds == null || colorwaySeasonIds.isEmpty()) {
            throw new SparcEntityNotFoundException("No colorway was found for id: " + colorwayId);
        }

        final List<LCSSKUSeasonLink> skuSeasonLinks = ColorwayRepository.getSkuSeasonLinks(colorwayId, colorwaySeasonIds);
		
		//System.out.println("skuSeasonLinks----------------------"+skuSeasonLinks);
        
		

        if (skuSeasonLinks == null || skuSeasonLinks.isEmpty()) {
            throw new SparcEntityNotFoundException("No colorways has been found with the given colorwayId:" + colorwayId);
        }

        LOGGER.debug("[getColorwayDetails for process " + process + "] Found " + skuSeasonLinks.size() + " colorway-season links for colorwayId: " + colorwayId + ".");

        return skuSeasonLinks.stream()
                .map(link -> getDetailsOfSkuSeasonLink(link, process, isVerify))
                .filter(detail -> detail != null)
                .peek(detail -> detail.setScColorwayNo(colorwayId))
                .collect(Collectors.toList());
    }

    public SparcColorwayUpdateResponse updateColorway(final List<SparcColorwayUpdateRequest> request) {
        final SparcColorwayUpdateResponse response = new SparcColorwayUpdateResponse();
        request.stream().forEach(updateRequest -> {
            try {
                final String colorwayId = trimZeroPrefix(updateRequest.getCriteria().getScColorwayNo());
				
                final List<LCSSKUSeasonLink> skuSeasonLinks = ColorwayRepository.getSkuSeasonLinks(colorwayId, updateRequest);
				
                LOGGER.info("SKULinks found:" + skuSeasonLinks.size());
				
				LOGGER.info("SKULinks found:" + skuSeasonLinks.size());
				
                if (skuSeasonLinks.isEmpty()) {
                    response.getErrors().add(SparcColorwayUpdateResponse.Error.builder().id(updateRequest.getCriteria().getScColorwayNo()).error("No colorway is found").build());
                    return;
                }
                
				final List<SparcColorwayUpdateResponse.Error> errors = skuSeasonLinks.stream().flatMap(link -> updateColorway(colorwayId, link, updateRequest).stream()).collect(Collectors.toList());
				
                if (!containsUpdateErrors(errors)) {
                    response.getUpdated().add(updateRequest.getCriteria().getScColorwayNo());
                }
                response.getErrors().addAll(errors);
            } catch (Exception e) {
                response.getErrors().add(SparcColorwayUpdateResponse.Error.builder().error(e.getMessage()).id(updateRequest.getCriteria().getScColorwayNo()).build());
            }
        });
        return response;
    }

    public SparcApiCallLogEntry.Status getTransactionStatus(final List<SparcColorwayUpdateResponse.Error> errors) {
        if (errors == null || errors.isEmpty()) {
            return SparcApiCallLogEntry.Status.SUCCESS;
        }
        final boolean errorsExist = errors.stream()
                .map(err -> err.getErrorType())
                .filter(type -> type != null)
                .anyMatch(type -> SparcColorwayUpdateResponse.ErrorType.ERROR == type);
        if (errorsExist) {
            return SparcApiCallLogEntry.Status.FAIL;
        }
        final boolean warningExists = errors.stream()
                .map(err -> err.getErrorType())
                .filter(type -> type != null)
                .anyMatch(type -> SparcColorwayUpdateResponse.ErrorType.WARNING == type);
        if (warningExists) {
            return SparcApiCallLogEntry.Status.SUCCESS_WARNINGS;
        }
        return SparcApiCallLogEntry.Status.SUCCESS;
    }

    public boolean containsUpdateErrors(final List<SparcColorwayUpdateResponse.Error> errors) {
        if (errors == null || errors.isEmpty()) {
            return false;
        }
        return errors.stream()
                .map(err -> err.getErrorType())
                .filter(type -> type != null)
                .anyMatch(type -> SparcColorwayUpdateResponse.ErrorType.ERROR == type);
    }

    private List<SparcColorwayUpdateResponse.Error> updateColorway(final String colorwayId, final LCSSKUSeasonLink link, final SparcColorwayUpdateRequest request) {
		LOGGER.debug("updateColorway method start");
        final List<SparcColorwayUpdateResponse.Error> errors = new ArrayList<>();
        final long seasonRevId = (long) link.getSeasonRevId();
        final LCSSeason season = SparcQueryUtil.<LCSSeason>findObjectById("VR:com.lcs.wc.season.LCSSeason:" + seasonRevId);

        final boolean validatedSeason = validateSeasonCriteria(colorwayId, season, request.getCriteria(), errors);
        if (!validatedSeason) {
            return errors;
        }
        final long skuARevId = (long) link.getSkuARevId();
        final LCSSKU sku = SparcQueryUtil.<LCSSKU>findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
        final long productARevId = (long) link.getProductARevId();
        final LCSProduct productARev = SparcQueryUtil.<LCSProduct>findObjectById("VR:com.lcs.wc.product.LCSProduct:" + productARevId);
        final boolean validLuckyUpdate = validateUpdateForLucky(colorwayId, season, productARev, errors);
        if (!validLuckyUpdate) {
            return errors;
        }
        updateProduct(colorwayId, productARev, request.getAttrs(), errors);
        updateSku(colorwayId, sku, request.getAttrs(), errors);
        updateSkuLink(colorwayId, link, request.getAttrs(), errors);
        return errors;
    }

    private boolean validateUpdateForLucky(final String colorwayId, final LCSSeason season, final LCSProduct product, final List<SparcColorwayUpdateResponse.Error> errors) {
        if (season != null) {
            if (!season.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.LUCKY_SEASON_FLEX_PATH)) {
                errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("Only Lucky Seasons are allowed").build());
                return false;
            }
        }
        if (product != null) {
            if (!product.getFlexType().getFullName(true).startsWith(SparcIntegrationConstants.LUCKY_PRODUCT_FLEX_PATH)) {
                errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("Only Lucky Products are allowed").build());
                return false;
            }
        }
        return true;
    }

    private void updateProduct(final String colorwayId, final LCSProduct product, final Map<String, Object> data, final List<SparcColorwayUpdateResponse.Error> errors) {
        if (product == null) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("No associated product found for given colorwayId").build());
            return;
        }
        try {
            final List<SparcTypeAttributeCollection.Attribute> attrs = SparcIntegrationConstants.COLORWAY_UPDATE_ATTRIBUTES.getAttributesByScopeByLevel("PRODUCT", "PRODUCT");
            if (attrs.isEmpty()) {
                LOGGER.error("No product attributes were defined to update");
                return;
            }
            final LCSProductClientModel model = new LCSProductClientModel();
            model.load(product);
            attrs.forEach(attr -> {
                try {
                    final FlexTypeAttribute flexTypeAttribute = model.getFlexType().getAttribute(attr.getInternalName());
                    final Object rawValue = data.get(attr.getAlias());
                    final Object invertedValue = SparcIntegrationUtil.invert(flexTypeAttribute, rawValue, attr.getParams());
                    if (invertedValue != null) {
                        model.setValue(attr.getInternalName(), invertedValue);
                    }
                } catch (Exception e) {
                    errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.WARNING).error("Encountered an error while setting the attribute on Product:" + attr.getAlias() + ", message:" + e.getMessage()).build());
                    LOGGER.error("Encountered an error while setting the attribute on Product:" + attr.getInternalName());
                }
            });
            model.save();
        } catch (Exception e) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.ERROR).error("Encountered an error while saving product, error:" + e.getMessage()).build());
        }
    }

    private void updateSku(final String colorwayId, final LCSSKU sku, final Map<String, Object> data, final List<SparcColorwayUpdateResponse.Error> errors) {
        if (sku == null) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("No associated Colorway found for given colorwayId").build());
            return;
        }
        try {
            final List<SparcTypeAttributeCollection.Attribute> attrs = SparcIntegrationConstants.COLORWAY_UPDATE_ATTRIBUTES.getAttributesByScopeByLevel("PRODUCT", "SKU");
            if (attrs.isEmpty()) {
                LOGGER.error("No Colorway attributes were defined to update");
                return;
            }
            final LCSSKUClientModel model = new LCSSKUClientModel();
            model.load(sku);
            attrs.forEach(attr -> {
                try {
                    final FlexTypeAttribute flexTypeAttribute = model.getFlexType().getAttribute(attr.getInternalName());
                    final Object rawValue = data.get(attr.getAlias());
                    final Object invertedValue = SparcIntegrationUtil.invert(flexTypeAttribute, rawValue, attr.getParams());
                    if (invertedValue != null) {
                        model.setValue(attr.getInternalName(), invertedValue);
                    }
                } catch (Exception e) {
                    errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.WARNING).error("Encountered an error while setting the attribute on Colorway:" + attr.getAlias() + ", message:" + e.getMessage()).build());
                    LOGGER.error("Encountered an error while setting the attribute on Colorway:" + attr.getInternalName());
                }
            });
            model.save();
        } catch (Exception e) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.ERROR).error("Encountered an error while saving Colorway, error:" + e.getMessage()).build());
        }
    }

    private void updateSkuLink(final String colorwayId, final LCSSKUSeasonLink link, final Map<String, Object> data, final List<SparcColorwayUpdateResponse.Error> errors) {
        try {
            final List<SparcTypeAttributeCollection.Attribute> attrs = SparcIntegrationConstants.COLORWAY_UPDATE_ATTRIBUTES.getAttributesByScopeByLevel("PRODUCT-SEASON", "SKU");
            if (attrs.isEmpty()) {
                LOGGER.error("No Colorway attributes were defined to update");
                return;
            }
            final LCSSeasonSKULinkClientModel model = new LCSSeasonSKULinkClientModel();
            final LCSSeasonProductLink latestSpl = LCSSeasonQuery.findSeasonProductLink(link.getSkuMaster(), link.getSeasonMaster());
            model.load(latestSpl);
            attrs.forEach(attr -> {
                try {
                    final FlexTypeAttribute flexTypeAttribute = model.getFlexType().getAttribute(attr.getInternalName());
                    final Object rawValue = data.get(attr.getAlias());
                    final Object invertedValue = SparcIntegrationUtil.invert(flexTypeAttribute, rawValue, attr.getParams());
                    if (invertedValue != null) {
                        model.setValue(attr.getInternalName(), invertedValue);
                    }
                } catch (Exception e) {
                    errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.WARNING).error("Encountered an error while setting the attribute on Colorway-Season Link:" + attr.getAlias() + ", message:" + e.getMessage()).build());
                    LOGGER.error("Encountered an error while setting the attribute on Colorway-Season Link:" + attr.getInternalName());
                }
            });
            model.save();
        } catch (Exception e) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).errorType(SparcColorwayUpdateResponse.ErrorType.ERROR).error("Encountered an error while saving Colorway, error:" + e.getMessage()).build());
        }
    }

    private boolean validateSeasonCriteria(final String colorwayId, final LCSSeason season, final SparcColorwayUpdateRequest.Criteria criteria, final List<SparcColorwayUpdateResponse.Error> errors) {
        if (season == null) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("No associated season found for given colorwayId").build());
            return false;
        }
        final String seasonType = (String) SparcIntegrationUtil.getValueFrom(season, SparcIntegrationConstants.DEVELOPMENT_SEASON_INTERNAL_NAME, true);
        if (!seasonType.equals(criteria.getSeasonType())) {
            return false;
        }
        final Object year = SparcIntegrationUtil.getValueFrom(season, SparcIntegrationConstants.SEASON_YEAR_INTERNAL_NAME, true);
		
		LOGGER.debug("CRITERIA >>>>>>"+criteria.getSeasonType()+"<<<<<<CRITERIA>>>>>>><CRITERIA YEAR>>>>>>"+criteria.getYear());
		
		if (!year.toString().equals(criteria.getYear() + "")) {
            errors.add(SparcColorwayUpdateResponse.Error.builder().id(colorwayId).error("Season criteria (Year) is not matched, expected:" + criteria.getYear() + ", actual:" + year).build());
            return false;
        }
        return true;
    }

    private SparcColorwayDetails getDetailsOfSkuSeasonLink(final LCSSKUSeasonLink link, final SparcColorwayProcesses process, final boolean isVerify) {
        final List<FlexTyped> sources = getSources(link);
        final List<FlexTyped> colorwaySource = getColorwaySource(link);
        //Lucky Enhancement - Start
        final LCSProductSeasonLink prodSeasonLink = getProdSeasonLink(link);
        final List<FlexTyped> prodBSources = new ArrayList<>();
        prodBSources.add(prodSeasonLink);
        final List<FlexTyped> colorwaySeasonSources = new ArrayList<>();
        colorwaySeasonSources.add(link);
        //Lucky Enhancement - End
        final SparcColorwayDetails details = new SparcColorwayDetails();
        SparcIntegrationConstants.COLORWAY_PAYLOAD_PROPERTY_DEFINITIONS.getProperties().forEach((internalName, propDef) -> {
            final Object value = SparcIntegrationUtil.getValueFrom(sources, internalName, true);
            putProperty(details, value, propDef, process);
        });
        SparcIntegrationConstants.COLORWAY_PAYLOAD_PRODUCT_SEASON_DEFINITIONS.getProperties().forEach((internalName, propDef) -> {
            final Object value = SparcIntegrationUtil.getValueFrom(prodBSources, internalName, true);
            putProperty(details, value, propDef, process);
        });
        SparcIntegrationConstants.COLORWAY_PAYLOAD_COLORWAY_PROPERTY_DEFINITIONS.getProperties().forEach((internalName, propDef) -> {
            final Object value = SparcIntegrationUtil.getValueFrom(colorwaySource, internalName, true);
            putProperty(details, value, propDef, process);
        });
        SparcIntegrationConstants.COLORWAY_PAYLOAD_COLORWAY_SEASON_DEFINITIONS.getProperties().forEach((internalName, propDef) -> {
            final Object value = SparcIntegrationUtil.getValueFrom(colorwaySeasonSources, internalName, true);
            putProperty(details, value, propDef, process);
        });
        htsCorrection(details, sources);
        if (SparcColorwayProcesses.FC == process && !isVerify) {
            markUpdatedOnProduct(link);
            markUpdatedOnColorway(link);
        }
        if (!isVerify) {
            markColorway(link, process);
        }
        return details;
    }

    private void htsCorrection(final SparcColorwayDetails details, final List<FlexTyped> sources) {
        LCSSourceToSeasonLink objSourceToSeasonLink = null;
        for(FlexTyped source : sources){
            if(source instanceof  LCSSourceToSeasonLink){
                objSourceToSeasonLink =(LCSSourceToSeasonLink) source;
                break;
            }
        }
        final SparcPropertyDefinitions.Property property = SparcIntegrationConstants.SOURCE_TO_SEASON_LINK_HTS_CODE_DEFINITION.getProperties().get(
                SparcIntegrationConstants.BUSINESS_OBJECT_HTS_ASSIGNMENT_HTS_CODE_INTERNAL_NAME);
        if (property != null) {
            try {
                putProperty(details, resolveHTSCode1(objSourceToSeasonLink), property, SparcColorwayProcesses.FC);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private static Object resolveHTSCode1(FlexTyped flexTypedObj) throws WTException {
        String htsCode1 = null;
        if (flexTypedObj == null) {
            return htsCode1;
        }
        try {
            String htsCode1Name = null;
            Double htsCode1WeightPercentThreshold = Double.parseDouble(SparcIntegrationConstants.AERO_HTS_CODE_1_WEIGHT_THRESHOLD_VALUE);
            Double htsCode1WeightPercent = (Double)flexTypedObj.getValue(SparcIntegrationConstants.AERO_SOURCING_HTS_CODE_1_WEIGHT_ATTR);

            if (htsCode1WeightPercentThreshold.equals(htsCode1WeightPercent)) {
                LCSLifecycleManaged htsAssignmentTableObj = (LCSLifecycleManaged)flexTypedObj.getValue(SparcIntegrationConstants.AERO_SOURCING_HTS_CODE_1_ATTR);

                if (htsAssignmentTableObj != null) {
                    htsCode1Name = (String)htsAssignmentTableObj.getValue(SparcIntegrationConstants.AERO_HTS_ASSIGNMENT_TABLE_CODE_ATTR);
                    htsCode1 = htsCode1Name;
                }

            } else if (htsCode1WeightPercent != null && htsCode1WeightPercent.doubleValue() != 0d) {
                htsCode1 = SparcIntegrationConstants.AERO_HTS_CODE_1_DEFAULT;
            }

            LOGGER.debug("[resolveHTSCode1] HTS Code 1 Weight %: " + htsCode1WeightPercent
                    + ", HTS Code 1 Weight % threshold: " + htsCode1WeightPercentThreshold
                    + ", HTS Code 1 (From HTS Assignment Table Name): " + htsCode1Name
                    + ", HTS Code 1 (Default value): " + SparcIntegrationConstants.AERO_HTS_CODE_1_DEFAULT
                    + ", HTS Code 1 is: " + htsCode1);

        } catch(WTException wtx) {
            throw wtx;
        } catch(Exception e) {
            LOGGER.error("[resolveHTSCode1] Unable to resolve value for HTS Code 1 attribute: " + e.getMessage(), e);
        }

        return htsCode1;
    }

    private void putProperty(final SparcColorwayDetails details, final Object value, final SparcPropertyDefinitions.Property property, final SparcColorwayProcesses process) {
        final boolean isBlank = value == null || value.toString().trim().isEmpty();
        if (isBlank && !property.isAllowBlank() && (process == SparcColorwayProcesses.FC)) {
            throw new RuntimeException("The value of property:" + property.getFlexInternalName() + " can't be blank.");
        }

        switch (property.getHierarchy()) {
            case FC:
                details.addFcParam(property.getAliasName(), value);
                break;
            case CAP:
                details.addCapParam(property.getAliasName(), value);
                break;
            case STYLE:
                details.addStyleParam(property.getAliasName(), value);
                break;
            default:
                break;
        }

    }

    private List<FlexTyped> getColorwaySource(final LCSSKUSeasonLink link) {
        final long skuARevId = (long) link.getSkuARevId();
        final LCSSKU sku = (LCSSKU) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
        final List<FlexTyped> sources = new ArrayList<>();
        if (sku != null) {
            sources.add(sku);
        }
        return sources;
    }

    private List<FlexTyped> getSources(final LCSSKUSeasonLink link) {
        final long skuARevId = (long) link.getSkuARevId();
        final LCSSKU sku = (LCSSKU) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);
        final long seasonRevId = (long) link.getSeasonRevId();
        final LCSSeason season = (LCSSeason) SparcQueryUtil.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + seasonRevId);
        final long productARevId = (long) link.getProductARevId();
        final LCSProduct productARev = (LCSProduct) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + productARevId);
        final List<FlexTyped> sources = new ArrayList<>();
        sources.add(link);
        sources.add(sku);
        sources.add(season);
        sources.add(productARev);
        addPrimarySourceToSeasonLink(sources, productARev, season);
        SparcIntegrationConstants.PRODUCT_OBJECT_REF_INTERNAL_NAMES.stream().forEach(objRef -> {
            try {
                final Object refValue = SparcIntegrationUtil.getValueFrom(sources, objRef, false);
                if (refValue != null) {
                    if (refValue instanceof LCSColor) {
                        verifyColorFlexType(refValue);
                        sources.add((FlexTyped) refValue);
                    } else {
                        sources.add((FlexTyped) refValue);
                    }
                }
            } catch (Exception e) {
                //do nothing
            }
        });
        return sources;
    }

    private void addPrimarySourceToSeasonLink(final List<FlexTyped> sources, final LCSProduct prd, final LCSSeason season) {
        if (prd != null && season != null) {
            try {
                final LCSSourceToSeasonLink primarySourceToSeasonLink = LCSSourcingConfigQuery.getPrimarySourceToSeasonLink(prd.getMaster(), season.getMaster());
                if (primarySourceToSeasonLink != null) {
                    sources.add(primarySourceToSeasonLink);
                    final FlexTyped htsBusinessObj = (FlexTyped) SparcIntegrationUtil.getValueFrom(primarySourceToSeasonLink, SparcIntegrationConstants.LUCKY_SOURCING_TO_SEASON_CONFIG_HTS_CODE_INTERNAL_NAME, false);
                    if (htsBusinessObj == null) {
                        LOGGER.warn("No HTS business object is set on Primary Sourcing to Season link");
                        return;
                    }
                    sources.add(htsBusinessObj);
                }
            } catch (Exception e) {
                LOGGER.error("Encountered an error while fetching Source To Season link, error:" + e.getMessage());
            }
        }
    }

    private void verifyColorFlexType(final Object refValue) {

        final LCSColor color = (LCSColor) refValue;
        final FlexType flexType = color.getFlexType();
        if (!flexType.getFullName(true).startsWith(SparcIntegrationConstants.LUCKY_COLOR_FLEX_TYPE_PATH)) {
            throw new RuntimeException("Expecting a color of type:" + SparcIntegrationConstants.LUCKY_COLOR_FLEX_TYPE_PATH + ", but found:" + flexType.getFullName(true));
        }
    }

    private LCSProduct getProdBRev(final LCSSKUSeasonLink link) {
        final long productARevId = (long) link.getProductARevId();
        final LCSProduct productARev = (LCSProduct) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + productARevId);
        try {
            final Versioned productBRev = VersionHelper.getVersion(productARev, "B");
            return (LCSProduct) VersionHelper.latestIterationOf(productBRev);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This gets the Product season link from the colorway season link object
     * @param link
     * @return
     */
    private LCSProductSeasonLink getProdSeasonLink(final LCSSKUSeasonLink link){
        //Lucky Enhancement - Start
        LCSProductSeasonLink objProdSeasLink = null;
        try{
            objProdSeasLink = link.getProductLink();
        }catch(Exception ex){
            LOGGER.debug("Error while getting the ProductSeason link object--"+ex.getMessage());
        }
        //Lucky Enhancement - End
        return objProdSeasLink;
    }
    public void markColorway(final LCSSKUSeasonLink link, final SparcColorwayProcesses process) {
        if (link == null || process == null) {
            return;
        }

		LCSSKUSeasonLink objSKUSeasonLink = null;      
        try {
            final LCSSeasonProductLink latestSpl = LCSSeasonQuery.findSeasonProductLink(link.getSkuMaster(), link.getSeasonMaster());
			if(latestSpl != null ){
                objSKUSeasonLink = (LCSSKUSeasonLink) latestSpl;
            }
			LCSSeasonSKULinkClientModel skuClientModel = new LCSSeasonSKULinkClientModel();
			skuClientModel.load(objSKUSeasonLink);

			

            if (SparcColorwayProcesses.FC == process) {
                //latestSpl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_YES);
				skuClientModel.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_FC_INTERNAL_NAME, SparcIntegrationConstants.SENT_YES);

                //Lucky Enhancement - Start
                if (!SparcIntegrationConstants.SENT_YES.equalsIgnoreCase((String)latestSpl.getValue(FC_SHARED_ATTR))) {
                    //latestSpl.setValue(FC_SHARED_ATTR, SparcIntegrationConstants.SENT_YES);
					  skuClientModel.setValue(FC_SHARED_ATTR, SparcIntegrationConstants.SENT_YES);
                }
                //Lucky Enhancement - End
            } else {
                //latestSpl.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_YES);
				  skuClientModel.setValue(SparcIntegrationConstants.COLORWAY_TO_SEASON_SENT_TO_CAP_INTERNAL_NAME, SparcIntegrationConstants.SENT_YES);

                //Lucky Enhancement - Start
                if (!SparcIntegrationConstants.SENT_YES.equalsIgnoreCase((String)latestSpl.getValue(AERO_COLORWAY_CAP_SHARED_ATTR))) {
                    //latestSpl.setValue(AERO_COLORWAY_CAP_SHARED_ATTR, SparcIntegrationConstants.SENT_YES);
					skuClientModel.setValue(AERO_COLORWAY_CAP_SHARED_ATTR, SparcIntegrationConstants.SENT_YES);
                }
                //Lucky Enhancement - End
            }
            //LCSLogic.persist(latestSpl, true);
			skuClientModel.save();
        } catch (Exception e) {
            throw new RuntimeException("Encountered an error while marking the colorway, error:" + e.getMessage());
        }
    }

    public void markUpdatedOnProduct(final LCSSKUSeasonLink link) {

        LCSProduct productARev = null;

        try {
            LOGGER.debug("[markUpdatedOnProduct] Marking Product New Vs Update flag..");
            final long productARevId = (long) link.getProductARevId();
            productARev = (LCSProduct) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + productARevId);

            if(!VersionHelper.isCheckedOut(productARev)) {
                productARev = VersionHelper.checkout(productARev);
                LOGGER.debug("[markUpdatedOnProduct] Product " + productARev + " is checked-out.");
            }

            final String newVsUpdateValue = (String) SparcIntegrationUtil.getValueFrom(productARev, SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME);
            if (newVsUpdateValue == null || newVsUpdateValue.equals(SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE)) {
                productARev.setValue(SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_INTERNAL_NAME, SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_UPDATE_VALUE);
            }

            LCSProductLogic.persist(productARev, true);

            LOGGER.debug("[markUpdatedOnProduct] Marked Product New Vs Update flag..");
        } catch (Exception e) {
            LOGGER.error("[markUpdatedOnProduct] Encountered an error while setting the New Vs Update flag on the product, error:" + e);
        } finally {
            try {
                if(productARev != null && VersionHelper.isCheckedOut(productARev)){
                    VersionHelper.checkin(productARev);
                    LOGGER.debug("[markUpdatedOnProduct] Product " + productARev + " is checked-in.");
                }
            } catch (WTException e) {
                LOGGER.error("[markUpdatedOnProduct] failed to check in product " + productARev + ". " + e.getMessage(), e);
            }
        }
    }

    public void markUpdatedOnColorway(final LCSSKUSeasonLink link) {

        LCSSKU sku = null;

        try {
            LOGGER.debug("[markUpdatedOnColorway] Marking colorway New Vs Update flag..");
            final long skuARevId = (long) link.getSkuARevId();
            sku = (LCSSKU) SparcQueryUtil.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuARevId);

            if(!VersionHelper.isCheckedOut(sku)) {
                sku = VersionHelper.checkout(sku);
                LOGGER.debug("[markUpdatedOnColorway] Colorway " + sku + " is checked-out.");
            }

            final String newVsUpdateValue = (String) SparcIntegrationUtil.getValueFrom(sku, SparcIntegrationConstants.COLORWAY_NEW_VS_UPDATE_FLAG_INTERNAL_NAME);
            if (newVsUpdateValue == null || newVsUpdateValue.equals(SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_NEW_VALUE)) {
                sku.setValue(SparcIntegrationConstants.COLORWAY_NEW_VS_UPDATE_FLAG_INTERNAL_NAME, SparcIntegrationConstants.PRODUCT_NEW_VS_UPDATE_FLAG_UPDATE_VALUE);
            }

            LCSLogic.persist(sku, true);

            LOGGER.debug("[markUpdatedOnColorway] Marked colorway New Vs Update flag..");
        } catch (Exception e) {
            LOGGER.error("[markUpdatedOnColorway] Encountered an error while setting the New Vs Update flag on the Colorway, error:" + e, e);
        } finally {
            try {
                if(sku != null && VersionHelper.isCheckedOut(sku)){
                    VersionHelper.checkin(sku);
                    LOGGER.debug("[markUpdatedOnColorway] Product " + sku + " is checked-in.");
                }
            } catch (WTException e) {
                LOGGER.error("[markUpdatedOnColorway] failed to check in colorway " + sku + ". " + e.getMessage(), e);
            }
        }
    }

    private String trimZeroPrefix(final String colorwayId) {
        if (colorwayId == null || !colorwayId.startsWith("0")) {
            return colorwayId;
        }
        final byte[] colorwayIdChars = colorwayId.getBytes();
        for (int iterator = 0; iterator < colorwayIdChars.length; iterator++) {
            if (colorwayIdChars[iterator] != '0') {
                return colorwayId.substring(iterator);
            }
        }
        return null;
    }

}
