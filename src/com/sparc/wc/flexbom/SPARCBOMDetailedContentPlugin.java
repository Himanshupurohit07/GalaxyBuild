package com.sparc.wc.flexbom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;

public final class SPARCBOMDetailedContentPlugin {

	private final static Logger LOGGER = LogManager.getLogger(SPARCBOMDetailedContentPlugin.class);
	private static final String MATERIAL_SUPPLIER_COMP_ATTR = LCSProperties
			.get("com.sparc.wc.flexbom.material.supplier.comp.attr", "scSSupContent");
	private static final String BOM_LINK_COMP_ATTR = LCSProperties.get("com.sparc.wc.flexbom.supplier.comp.attr",
			"scBOMDetailedContent");
	private static final boolean ROLLBACK_ON_ERROR = LCSProperties
			.getBoolean("com.sparc.wc.flexbom.detailed.content.plugin.rollback.on.error", false);
	private static final String DETAILED_ERROR = "Detailed Error:";

	public static void handleFlexBOMSaveEvent(final WTObject wtObject) throws WTException {

		if (wtObject == null || !(wtObject instanceof FlexBOMLink)) {
			LOGGER.error("Expecting a WTObject of type FlexBOMLink for Detailed Content Auto-Fill");
			return;
		}

		final FlexBOMLink flexBOMLink = (FlexBOMLink) wtObject;
		LOGGER.info("Handling BOM-Link Detailed content auto-fill");
		try {

			final LCSMaterialSupplier materialSupplier = getMaterialSupplier(flexBOMLink);
			if (containsMaterialSupplierAndEmptyComposition(flexBOMLink, materialSupplier)) {
				final String materialSupplierComp = (String) getValue(materialSupplier, MATERIAL_SUPPLIER_COMP_ATTR);
				setValue(flexBOMLink, BOM_LINK_COMP_ATTR, materialSupplierComp);
				LOGGER.info("Auto-filled detailed composition from material supplier to BOM-Link");
			}

		} catch (final Exception e) {
			LOGGER.error("Encountered an error while handling the Detailed content auto-fill on FlexBOMLink, error:"
					+ e.getMessage());
			if (LOGGER.isTraceEnabled()) {
				LOGGER.error(DETAILED_ERROR + e);
			}
			if (ROLLBACK_ON_ERROR) {
				throw e;
			}
		}

	}

	private static boolean containsMaterialSupplierAndEmptyComposition(final FlexBOMLink flexBOMLink,
			final LCSMaterialSupplier materialSupplier) throws WTException {
		if (materialSupplier == null) {
			LOGGER.debug("No Material Supplier found, cannot proceed with Detailed Composition auto-fill");
			return false;
		}
		final String supplierComp = (String) getValue(materialSupplier, MATERIAL_SUPPLIER_COMP_ATTR);
		LOGGER.debug("Material Supplier composition:" + supplierComp);
		if (!FormatHelper.hasContent(supplierComp)) {
			LOGGER.debug(
					"No Detailed Compostion is set in the given material-supplier, cannot proceed with Detailed Composition auto-fill");
			return false;
		}
		final String bomLinkComp = (String) getValue(flexBOMLink, BOM_LINK_COMP_ATTR);
		if (FormatHelper.hasContent(bomLinkComp)) {
			LOGGER.debug("BOM-Link already contains the detailed composition, ignoring auto-fill from supplier");
			return false;
		}

		return true;
	}

	private static LCSMaterialSupplier getMaterialSupplier(final FlexBOMLink flexBOMLink) {
		final LCSSupplierMaster supplierMaster = flexBOMLink.getSupplier();
		final LCSMaterialMaster materialMaster = flexBOMLink.getChild();
		if (supplierMaster == null || materialMaster == null) {
			return null;
		}

		try {
			return LCSMaterialSupplierQuery.findMaterialSupplier(materialMaster, supplierMaster);
		} catch (final Exception e) {
			LOGGER.error("Encountered an error while fetching material supplier from the given BOM Link, error:"
					+ e.getMessage());
			if (LOGGER.isTraceEnabled()) {
				LOGGER.error(DETAILED_ERROR + e);
			}
		}
		return null;
	}

	private static Object getValue(final FlexTyped flexTyped, final String attr) throws WTException {
		if (flexTyped == null || !FormatHelper.hasContent(attr)) {
			LOGGER.error("Cannot get value reference from an empty FlexTyped Object/empty key");
			return null;
		}
		try {
			return flexTyped.getValue(attr);
		} catch (final Exception e) {
			LOGGER.error("Encountered an error while fetching value of attribute:" + attr + " on FlexTyped:"
					+ flexTyped.getFlexType().getFullNameDisplay(true) + ", error:" + e.getMessage());
			if (LOGGER.isTraceEnabled()) {
				LOGGER.error(DETAILED_ERROR + e);
			}
		}
		return null;
	}

	private static void setValue(final FlexTyped flexTyped, final String attr, final Object value) throws WTException {

		if (flexTyped == null || !FormatHelper.hasContent(attr)) {
			LOGGER.error("Cannot set value on an empty FlexTyped Object/empty attr");
			return;
		}

		try {

			LOGGER.debug("Setting attribute:" + attr + " with value:" + value);
			flexTyped.setValue(attr, value);

		} catch (final Exception e) {
			LOGGER.error("Encountered an error while setting value of attribute:" + attr + " on FlexTyped:"
					+ flexTyped.getFlexType().getFullNameDisplay(true) + ", error:" + e.getMessage());
			if (LOGGER.isTraceEnabled()) {
				LOGGER.error(DETAILED_ERROR + e);
			}
		}
	}

}
