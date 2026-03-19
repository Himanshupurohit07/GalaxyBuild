<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<qml bypassAccessControl="false" caseInsensitive="true" addTimeToDateFields="false" mainType="Lucky (com.ptcmscloud.scLuckySeason)" joinModel="false" xsi:noNamespaceSchemaLocation="qml.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <query>
        <selectOrConstrain distinct="false" group="false">
            <reportAttribute heading="Season Name" reportAttributeId="Season_Name" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.season.LCSSeason|com.ptcmscloud.scLuckySeason~MBA|typeInfoLCSSeason.ptc_str_4">WCTYPE|com.lcs.wc.season.LCSSeason|com.ptcmscloud.scLuckySeason~MBA|typeInfoLCSSeason.ptc_str_4</column>
            </reportAttribute>
            <reportAttribute heading="Product Name" reportAttributeId="Name" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.String" propertyName="name">master&gt;name</column>
            </reportAttribute>
            <reportAttribute heading="PLM Product No" reportAttributeId="PLM_Product_" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.Long" propertyName="WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_lng_1">WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_lng_1</column>
            </reportAttribute>
            <reportAttribute heading="Brand" reportAttributeId="Brand" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_13">WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_13</column>
            </reportAttribute>
            <reportAttribute heading="Gender" reportAttributeId="Gender" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_14">WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_14</column>
            </reportAttribute>
            <reportAttribute heading="Category" reportAttributeId="Category" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_56">WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct~MBA|typeInfoLCSProduct.ptc_str_56</column>
            </reportAttribute>
            <reportAttribute heading="Sourcing Name" reportAttributeId="Name_1" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.sourcing.LCSSourcingConfig|com.ptcmscloud.scGeneral$com_lcs_wc_sourcing_LCSSourcingConfig~MBA|typeInfoLCSSourcingConfig.ptc_str_2">WCTYPE|com.lcs.wc.sourcing.LCSSourcingConfig|com.ptcmscloud.scGeneral$com_lcs_wc_sourcing_LCSSourcingConfig~MBA|typeInfoLCSSourcingConfig.ptc_str_2</column>
            </reportAttribute>
            <reportAttribute heading="Is Primary Source" reportAttributeId="Primary_STSL" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourceToSeasonLink)" isExternal="false" type="boolean" propertyName="primarySTSL">primarySTSL</column>
            </reportAttribute>
            <reportAttribute heading="Spec Name" reportAttributeId="Name_2" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="General (com.ptcmscloud.general)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.specification.FlexSpecification|com.ptcmscloud.general~MBA|typeInfoFlexSpecification.ptc_str_2">WCTYPE|com.lcs.wc.specification.FlexSpecification|com.ptcmscloud.general~MBA|typeInfoFlexSpecification.ptc_str_2</column>
            </reportAttribute>
            <reportAttribute heading="Is Primary Spec" reportAttributeId="Primary_Spec" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Flex Specification To Season Link" isExternal="false" type="boolean" propertyName="primarySpec">primarySpec</column>
            </reportAttribute>
            <reportAttribute heading="BOM NAME" reportAttributeId="Name_3" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.flexbom.FlexBOMPart|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMPart|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMPart~MBA|typeInfoFlexBOMPart.ptc_str_3">WCTYPE|com.lcs.wc.flexbom.FlexBOMPart|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMPart|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMPart~MBA|typeInfoFlexBOMPart.ptc_str_3</column>
            </reportAttribute>
            <reportAttribute heading="Is BOM Primary" reportAttributeId="Primary_Component" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Flex Specification To Component Link" isExternal="false" type="boolean" propertyName="primaryComponent">primaryComponent</column>
            </reportAttribute>
            <reportAttribute heading="Material ID" reportAttributeId="Material_ID" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="java.lang.Long" propertyName="WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_lng_2">WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_lng_2</column>
            </reportAttribute>
            <reportAttribute heading="Section" reportAttributeId="Section" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.flexbom.FlexBOMLink|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMLink|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMLink~MBA|typeInfoFlexBOMLink.ptc_str_2">WCTYPE|com.lcs.wc.flexbom.FlexBOMLink|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMLink|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMLink~MBA|typeInfoFlexBOMLink.ptc_str_2</column>
            </reportAttribute>
            <reportAttribute heading="Component Name" reportAttributeId="Component_Name" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Component List Apparel" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.foundation.LCSLifecycleManaged|com.ptcmscloud.scComponentListApparel~MBA|typeInfoLCSLifecycleManaged.ptc_str_10">WCTYPE|com.lcs.wc.foundation.LCSLifecycleManaged|com.ptcmscloud.scComponentListApparel~MBA|typeInfoLCSLifecycleManaged.ptc_str_10</column>
            </reportAttribute>
            <reportAttribute heading="Number" reportAttributeId="Number" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.flexbom.FlexBOMPart|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMPart|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMPart~MBA|typeInfoFlexBOMPart.ptc_str_1">WCTYPE|com.lcs.wc.flexbom.FlexBOMPart|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMPart|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMPart~MBA|typeInfoFlexBOMPart.ptc_str_1</column>
            </reportAttribute>
            <reportAttribute heading="Material Name" reportAttributeId="Material_Name" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_str_4">WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_str_4</column>
            </reportAttribute>
            <reportAttribute heading="Fabric Type" reportAttributeId="Fabric_Type" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="long" propertyName="iterationInfo.branchId">iterationInfo.branchId</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">scType</constant>
                    <object alias="Material (com.lcs.wc.material.LCSMaterial)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
            <reportAttribute heading="Name" reportAttributeId="Name_4" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <column alias="Vendor (Supplier)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.supplier.LCSSupplier~MBA|typeInfoLCSSupplier.ptc_str_1">WCTYPE|com.lcs.wc.supplier.LCSSupplier~MBA|typeInfoLCSSupplier.ptc_str_1</column>
            </reportAttribute>
            <reportAttribute heading="Designer 1" reportAttributeId="Designer_Name" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">scDesigner</constant>
                    <object alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
            <reportAttribute heading="Designer 2" reportAttributeId="Designer_2" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">scDesigner</constant>
                    <object alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
            <reportAttribute heading="Product Manager" reportAttributeId="Product_Manager" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">scdeveloper</constant>
                    <object alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
            <reportAttribute heading="PD Specialist" reportAttributeId="PD_Specialist" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">scdeveloper</constant>
                    <object alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
            <reportAttribute heading="US Technical Designer" reportAttributeId="US_Technical_Designer" userCanSelect="true" userCanConstrain="true" alwaysSelect="false" defaultValue="" constantValue="" isMacro="false">
                <script className="com.sparc.wc.report.SparcReportHelper" name="getDisplayValueForAtts" type="">
                    <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                    <constant type="java.lang.String" isMacro="false" xml:space="preserve">sctechdesigner</constant>
                    <object alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" propertyName="conceptualClassname"/>
                </script>
            </reportAttribute>
        </selectOrConstrain>
        <from>
            <table alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" xposition="0px" yposition="40px">WCTYPE|com.lcs.wc.season.LCSSeason|com.ptcmscloud.scLuckySeason</table>
            <table alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" xposition="0px" yposition="80px">WCTYPE|com.lcs.wc.product.LCSProduct|com.ptcmscloud.scApparel$com_lcs_wc_product_LCSProduct|com.ptcmscloud.scLucky$com_lcs_wc_product_LCSProduct</table>
            <table alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" xposition="0px" yposition="120px">WCTYPE|com.lcs.wc.season.LCSProductSeasonLink|com.ptcmscloud.scApparel$com_lcs_wc_season_LCSProductSeasonLink|com.ptcmscloud.scLucky$com_lcs_wc_season_LCSProductSeasonLink</table>
            <table alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" xposition="0px" yposition="160px">WCTYPE|com.lcs.wc.sourcing.LCSSourcingConfig|com.ptcmscloud.scGeneral$com_lcs_wc_sourcing_LCSSourcingConfig</table>
            <table alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourceToSeasonLink)" isExternal="false" xposition="0px" yposition="200px">WCTYPE|com.lcs.wc.sourcing.LCSSourceToSeasonLink|com.ptcmscloud.scGeneral$com_lcs_wc_sourcing_LCSSourceToSeasonLink</table>
            <table alias="Flex Specification To Season Link" isExternal="false" xposition="0px" yposition="240px">com.lcs.wc.specification.FlexSpecToSeasonLink</table>
            <table alias="Flex Specification To Component Link" isExternal="false" xposition="0px" yposition="280px">com.lcs.wc.specification.FlexSpecToComponentLink</table>
            <table alias="General (com.ptcmscloud.general)" isExternal="false" xposition="0px" yposition="320px">WCTYPE|com.lcs.wc.specification.FlexSpecification|com.ptcmscloud.general</table>
            <table alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" xposition="0px" yposition="360px">WCTYPE|com.lcs.wc.flexbom.FlexBOMLink|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMLink|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMLink</table>
            <table alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" xposition="0px" yposition="400px">WCTYPE|com.lcs.wc.flexbom.FlexBOMPart|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMPart|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMPart</table>
            <table alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" xposition="0px" yposition="440px">com.lcs.wc.material.LCSMaterial</table>
            <table alias="Vendor (Supplier)" isExternal="false" xposition="0px" yposition="480px">com.lcs.wc.supplier.LCSSupplier</table>
            <table alias="Component List Apparel" isExternal="false" xposition="0" yposition="520">WCTYPE|com.lcs.wc.foundation.LCSLifecycleManaged|com.ptcmscloud.scComponentListApparel</table>
        </from>
        <where>
            <compositeCondition type="and">
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="java.lang.String" propertyName="versionInfo.identifier.versionId">versionInfo.identifier.versionId</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="java.lang.String" isMacro="false" xml:space="preserve">A</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="boolean" propertyName="effectLatest">effectLatest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <nullCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="java.sql.Timestamp" propertyName="effectOutDate">effectOutDate</column>
                        </operand>
                        <nullOperator type="isNull"/>
                    </nullCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="boolean" propertyName="seasonRemoved">seasonRemoved</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">0</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.general)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourceToSeasonLink)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Flex Specification To Component Link" isExternal="false" type="java.lang.String" propertyName="componentType">componentType</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="java.lang.String" isMacro="false" xml:space="preserve">BOM</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="double" propertyName="seasonRevId">seasonRevId</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" type="long" propertyName="iterationInfo.branchId">iterationInfo.branchId</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_season_LCSProductSeasonLink)" isExternal="false" type="double" propertyName="productARevId">productARevId</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="long" propertyName="iterationInfo.branchId">iterationInfo.branchId</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLucky_com_lcs_wc_product_LCSProduct)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" type="long" propertyName="productMasterReference.objectId.id">productMasterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="General (com.ptcmscloud.general)" isExternal="false" type="long" propertyName="specSourceReference.objectId.id">specSourceReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Flex Specification To Season Link" isExternal="false" type="long" propertyName="roleAObjectRef.key.id">roleAObjectRef.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="General (com.ptcmscloud.general)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Flex Specification To Season Link" isExternal="false" type="long" propertyName="roleBObjectRef.key.id">roleBObjectRef.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Flex Specification To Component Link" isExternal="false" type="long" propertyName="specificationMasterReference.objectId.id">specificationMasterReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="General (com.ptcmscloud.general)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Flex Specification To Component Link" isExternal="false" type="long" propertyName="componentReference.objectId.id">componentReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourceToSeasonLink)" isExternal="false" type="double" propertyName="sourcingConfigMasterId">sourcingConfigMasterId</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourcingConfig)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="General (com.ptcmscloud.scGeneral_com_lcs_wc_sourcing_LCSSourceToSeasonLink)" isExternal="false" type="double" propertyName="seasonMasterId">seasonMasterId</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Lucky (com.ptcmscloud.scLuckySeason)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMPart)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="long" propertyName="parentReference.objectId.id">parentReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="boolean" propertyName="dropped">dropped</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">0</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <nullCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="java.sql.Timestamp" propertyName="outDate">outDate</column>
                        </operand>
                        <nullOperator type="isNull"/>
                    </nullCondition>
                </condition>
                <condition>
                    <nullCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="java.lang.String" propertyName="dimensionName">dimensionName</column>
                        </operand>
                        <nullOperator type="isNull"/>
                    </nullCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="long" propertyName="childReference.objectId.id">childReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="long" propertyName="supplierReference.objectId.id">supplierReference.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Vendor (Supplier)" isExternal="false" type="long" propertyName="masterReference.objectId.id">masterReference.key.id</column>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Vendor (Supplier)" isExternal="false" type="boolean" propertyName="latestIteration">iterationInfo.latest</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="boolean" isMacro="false" xml:space="preserve">1</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition>
                    <inCondition>
                        <operand>
                            <column alias="Material (com.lcs.wc.material.LCSMaterial)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_str_4">WCTYPE|com.lcs.wc.material.LCSMaterial~MBA|typeInfoLCSMaterial.ptc_str_4</column>
                        </operand>
                        <inOperator type="notIn"/>
                        <inOperand>
                            <delimitedList>
                                <constant type="java.lang.String" isMacro="false" xml:space="preserve">material_placeholder</constant>
                            </delimitedList>
                        </inOperand>
                    </inCondition>
                </condition>
                <condition>
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="java.lang.String" propertyName="WCTYPE|com.lcs.wc.flexbom.FlexBOMLink|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMLink|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMLink~MBA|typeInfoFlexBOMLink.ptc_str_2">WCTYPE|com.lcs.wc.flexbom.FlexBOMLink|com.ptcmscloud.Materials_BOM$com_lcs_wc_flexbom_FlexBOMLink|com.ptcmscloud.scApparel$com_lcs_wc_flexbom_FlexBOMLink~MBA|typeInfoFlexBOMLink.ptc_str_2</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <constant type="java.lang.String" isMacro="false" xml:space="preserve">mainmaterials</constant>
                        </operand>
                    </standardCondition>
                </condition>
                <condition outerJoinAlias="Component List Apparel">
                    <standardCondition>
                        <operand>
                            <column alias="Apparel (com.ptcmscloud.scApparel_com_lcs_wc_flexbom_FlexBOMLink)" isExternal="false" type="long" propertyName="typeInfoFlexBOMLink.ptc_ref_1.objectId.id">typeInfoFlexBOMLink.ptc_ref_1.key.id</column>
                        </operand>
                        <operator type="equal"/>
                        <operand>
                            <column alias="Component List Apparel" isExternal="false" type="long" propertyName="persistInfo.objectIdentifier.id">thePersistInfo.theObjectIdentifier.id</column>
                        </operand>
                    </standardCondition>
                </condition>
            </compositeCondition>
        </where>
        <orderBy>
            <orderByItem type="asc">
                <reportAttributeReference id="PLM_Product_"/>
            </orderByItem>
            <orderByItem type="asc">
                <reportAttributeReference id="Number"/>
            </orderByItem>
            <orderByItem type="asc">
                <reportAttributeReference id="Section"/>
            </orderByItem>
        </orderBy>
        <descriptionForExport>This report displays the materials and suppliers associated with BOM for the Lucky Brand</descriptionForExport>
    </query>
</qml>