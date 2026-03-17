package com.sparc.wc.integration.pojo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SparcArticlePOJO {
	//Integration Colorway Attributes Starts
	private String scColorwayNo;
	private String sclicenserefnumber;
	private String scNRFcolorgroup;
	private String scNRFcolorcode;
	private String sclicensed;
	private String sclegacyarticle;
	private String gxGalaxyColorCode;
	//Integration Colorway Attributes End


	//Integration Color Attributes Starts
	private String scColorNo;
	private String scColorDesc; 
	private String scColorShortDesc;
	//Integration Color Attributes End
	
	//Integration Season Attributes Starts
	private String seasonType; //Secondary Key yes
	private String year;  //Secondary Key yes
	//Integration Season Attributes End
	
	//Integration Product Attributes Starts
	private String scPLMProductNo;
	private String scBrand; //Secondary Key yes
	private String scGender; //Secondary Key yes
	private String scAge; //Secondary Key yes
	private String scProductDescriptionShort;
	private String scProductDescription;
	private String scworkingnumber;
	private String scOutsole;
	private String sclining; //Secondary Key yes
	private String scinlaysole; //Secondary Key yes
	private String scupper; //Secondary Key yes
	private String scoutsoledescription; //Secondary Key yes
	private String scfitsilhoette; //Secondary Key yes
	private String scproductwidth;
	private String scbrandassetpartner; //Secondary Key yes
	private String sclegacymodelnumber;
	private String sctechnology; //Secondary Key yes
	private String scbenefits; //Secondary Key yes
	private String scfeatures; //Secondary Key yes
	private String scSizeRange; //Secondary Key yes
	//private String gxSilhouetteDesc; 
	//Integration Product Attributes End

	//Integration Product Season Attributes Starts
	private String scDivision; //Secondary Key yes
	private String scProductGroup; //Secondary Key yes
	private String scProductType; //Secondary Key yes
	private String scRetailintrodate;
	private String scRetailexitdate;
	private String scdistchannel; //Secondary Key yes
	private String scsportcategory; //Secondary Key yes
	private String sccorpmarketingline; //Secondary Key yes
	private String sccatmarketingline; //Secondary Key yes
	private String scsalesline; //Secondary Key yes
	private String sctechnologyconcept;
	private String scsustainabilityethics; //Secondary Key yes
	private String scsellingregion;
	private String scFranchise; //Secondary Key yes
	private String scdevelopmentype; //Secondary Key yes
	private String scprodmanager;
	private String gxGlobalMSRPProdSeason;
	
	private String scKeyCategory;
	private String scKeyCategoryCode;
	private String scKeyCategoryCluster;
	private String scKeyCategoryClusterCode;
	private String scBusinessSegment;
	private String scBusinessSegmentCode;


	private String scInitialEstimatedUnits2;
	//Integration Product Season Attributes End
	

	public String getScInitialEstimatedUnits2() {
		return scInitialEstimatedUnits2;
	}

	public void setScInitialEstimatedUnits2(String scInitialEstimatedUnits2) {
		this.scInitialEstimatedUnits2 = scInitialEstimatedUnits2;
	}

	//Integration SKU Season Attributes Start
	private String scspecialusage; //Secondary Key yes
	private String sccreationtrack; //Secondary Key yes
	private String scTargetRetail;
	private String scInitialEstimatedUnits;
	private String sccolorwaylifecycle;
	private String  scmatconstruction; //Secondary Key yes
	private String  scFactoryPriority;
	private String gxGlobalMSRPSKUSeason;
	private String scprodmanager2;
	//Integration SKU Season Attributes End
	
	public String getScprodmanager2() {
		return scprodmanager2;
	}

	public void setScprodmanager2(String scprodmanager2) {
		this.scprodmanager2 = scprodmanager2;
	}

	//Integration CostSheet Attributes Starts
	private String scHTSCode;
	private String scHTSCode2;
	private String scHTSCode3;
	private String scHTSCode4;
	//Integration CostSheet Attributes End
	
	public String getScHTSCode2() {
		return scHTSCode2;
	}

	public void setScHTSCode2(String scHTSCode2) {
		this.scHTSCode2 = scHTSCode2;
	}

	public String getScHTSCode3() {
		return scHTSCode3;
	}

	public void setScHTSCode3(String scHTSCode3) {
		this.scHTSCode3 = scHTSCode3;
	}

	public String getScHTSCode4() {
		return scHTSCode4;
	}

	public void setScHTSCode4(String scHTSCode4) {
		this.scHTSCode4 = scHTSCode4;
	}

	public SparcArticlePOJO() {
		
	}

	public String getScColorwayNo() {
		return scColorwayNo;
	}

	public void setScColorwayNo(String scColorwayNo) {
		this.scColorwayNo = scColorwayNo;
	}

	public String getSclicenserefnumber() {
		return sclicenserefnumber;
	}

	public void setSclicenserefnumber(String sclicenserefnumber) {
		this.sclicenserefnumber = sclicenserefnumber;
	}

	public String getScNRFcolorgroup() {
		return scNRFcolorgroup;
	}

	public void setScNRFcolorgroup(String scNRFcolorgroup) {
		this.scNRFcolorgroup = scNRFcolorgroup;
	}

	public String getScNRFcolorcode() {
		return scNRFcolorcode;
	}

	public void setScNRFcolorcode(String scNRFcolorcode) {
		this.scNRFcolorcode = scNRFcolorcode;
	}

	public String getSclicensed() {
		return sclicensed;
	}

	public void setSclicensed(String sclicensed) {
		this.sclicensed = sclicensed;
	}

	public String getSclegacyarticle() {
		return sclegacyarticle;
	}

	public void setSclegacyarticle(String sclegacyarticle) {
		this.sclegacyarticle = sclegacyarticle;
	}
	
	public String getGxGalaxyColorCode() {
		return gxGalaxyColorCode;
	}

	public void setGxGalaxyColorCode(String gxGalaxyColorCode) {
		this.gxGalaxyColorCode = gxGalaxyColorCode;
	}

	public String getScColorNo() {
		return scColorNo;
	}

	public void setScColorNo(String scColorNo) {
		this.scColorNo = scColorNo;
	}

	public String getScColorDesc() {
		return scColorDesc;
	}

	public void setScColorDesc(String scColorDesc) {
		this.scColorDesc = scColorDesc;
	}

	public String getScColorShortDesc() {
		return scColorShortDesc;
	}

	public void setScColorShortDesc(String scColorShortDesc) {
		this.scColorShortDesc = scColorShortDesc;
	}

	public String getSeasonType() {
		return seasonType;
	}

	public void setSeasonType(String seasonType) {
		this.seasonType = seasonType;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getScPLMProductNo() {
		return scPLMProductNo;
	}

	public void setScPLMProductNo(String scPLMProductNo) {
		this.scPLMProductNo = scPLMProductNo;
	}

	public String getScBrand() {
		return scBrand;
	}

	public void setScBrand(String scBrand) {
		this.scBrand = scBrand;
	}

	public String getScGender() {
		return scGender;
	}

	public void setScGender(String scGender) {
		this.scGender = scGender;
	}

	public String getScAge() {
		return scAge;
	}

	public void setScAge(String scAge) {
		this.scAge = scAge;
	}

	public String getScProductDescriptionShort() {
		return scProductDescriptionShort;
	}

	public void setScProductDescriptionShort(String scProductDescriptionShort) {
		this.scProductDescriptionShort = scProductDescriptionShort;
	}

	public String getScProductDescription() {
		return scProductDescription;
	}

	public void setScProductDescription(String scProductDescription) {
		this.scProductDescription = scProductDescription;
	}

	public String getScworkingnumber() {
		return scworkingnumber;
	}

	public void setScworkingnumber(String scworkingnumber) {
		this.scworkingnumber = scworkingnumber;
	}

	public String getScOutsole() {
		return scOutsole;
	}

	public void setScOutsole(String scOutsole) {
		this.scOutsole = scOutsole;
	}

	public String getSclining() {
		return sclining;
	}

	public void setSclining(String sclining) {
		this.sclining = sclining;
	}

	public String getScinlaysole() {
		return scinlaysole;
	}

	public void setScinlaysole(String scinlaysole) {
		this.scinlaysole = scinlaysole;
	}

	public String getScupper() {
		return scupper;
	}

	public void setScupper(String scupper) {
		this.scupper = scupper;
	}

	public String getScoutsoledescription() {
		return scoutsoledescription;
	}

	public void setScoutsoledescription(String scoutsoledescription) {
		this.scoutsoledescription = scoutsoledescription;
	}

	public String getScfitsilhoette() {
		return scfitsilhoette;
	}

	public void setScfitsilhoette(String scfitsilhoette) {
		this.scfitsilhoette = scfitsilhoette;
	}

	public String getScproductwidth() {
		return scproductwidth;
	}

	public void setScproductwidth(String scproductwidth) {
		this.scproductwidth = scproductwidth;
	}

	public String getScbrandassetpartner() {
		return scbrandassetpartner;
	}

	public void setScbrandassetpartner(String scbrandassetpartner) {
		this.scbrandassetpartner = scbrandassetpartner;
	}

	public String getSclegacymodelnumber() {
		return sclegacymodelnumber;
	}

	public void setSclegacymodelnumber(String sclegacymodelnumber) {
		this.sclegacymodelnumber = sclegacymodelnumber;
	}

	public String getSctechnology() {
		return sctechnology;
	}

	public void setSctechnology(String sctechnology) {
		this.sctechnology = sctechnology;
	}

	public String getScbenefits() {
		return scbenefits;
	}

	public void setScbenefits(String scbenefits) {
		this.scbenefits = scbenefits;
	}

	public String getScfeatures() {
		return scfeatures;
	}

	public void setScfeatures(String scfeatures) {
		this.scfeatures = scfeatures;
	}

	public String getScSizeRange() {
		return scSizeRange;
	}

	public void setScSizeRange(String scSizeRange) {
		this.scSizeRange = scSizeRange;
	}
	
//	public String getGxSilhouetteDesc() {
//		return gxSilhouetteDesc;
//	}
//
//	public void setGxSilhouetteDesc(String gxSilhouetteDesc) {
//		this.gxSilhouetteDesc = gxSilhouetteDesc;
//	}

	public String getScDivision() {
		return scDivision;
	}

	public void setScDivision(String scDivision) {
		this.scDivision = scDivision;
	}

	public String getScProductGroup() {
		return scProductGroup;
	}

	public void setScProductGroup(String scProductGroup) {
		this.scProductGroup = scProductGroup;
	}

	public String getScProductType() {
		return scProductType;
	}

	public void setScProductType(String scProductType) {
		this.scProductType = scProductType;
	}

	public String getScRetailintrodate() {
		return scRetailintrodate;
	}

	public void setScRetailintrodate(String scRetailintrodate) {
		this.scRetailintrodate = scRetailintrodate;
	}

	public String getScRetailexitdate() {
		return scRetailexitdate;
	}

	public void setScRetailexitdate(String scRetailexitdate) {
		this.scRetailexitdate = scRetailexitdate;
	}

	public String getScdistchannel() {
		return scdistchannel;
	}

	public void setScdistchannel(String scdistchannel) {
		this.scdistchannel = scdistchannel;
	}

	public String getScsportcategory() {
		return scsportcategory;
	}

	public void setScsportcategory(String scsportcategory) {
		this.scsportcategory = scsportcategory;
	}

	public String getSccorpmarketingline() {
		return sccorpmarketingline;
	}

	public void setSccorpmarketingline(String sccorpmarketingline) {
		this.sccorpmarketingline = sccorpmarketingline;
	}

	public String getSccatmarketingline() {
		return sccatmarketingline;
	}

	public void setSccatmarketingline(String sccatmarketingline) {
		this.sccatmarketingline = sccatmarketingline;
	}

	public String getScsalesline() {
		return scsalesline;
	}

	public void setScsalesline(String scsalesline) {
		this.scsalesline = scsalesline;
	}

	public String getSctechnologyconcept() {
		return sctechnologyconcept;
	}

	public void setSctechnologyconcept(String sctechnologyconcept) {
		this.sctechnologyconcept = sctechnologyconcept;
	}

	public String getScsustainabilityethics() {
		return scsustainabilityethics;
	}

	public void setScsustainabilityethics(String scsustainabilityethics) {
		this.scsustainabilityethics = scsustainabilityethics;
	}

	public String getScsellingregion() {
		return scsellingregion;
	}

	public void setScsellingregion(String scsellingregion) {
		this.scsellingregion = scsellingregion;
	}

	public String getScFranchise() {
		return scFranchise;
	}

	public void setScFranchise(String scFranchise) {
		this.scFranchise = scFranchise;
	}

	public String getScdevelopmentype() {
		return scdevelopmentype;
	}

	public void setScdevelopmentype(String scdevelopmentype) {
		this.scdevelopmentype = scdevelopmentype;
	}

	public String getScprodmanager() {
		return scprodmanager;
	}

	public void setScprodmanager(String scprodmanager) {
		this.scprodmanager = scprodmanager;
	}
	public String getGxGlobalMSRPProdSeason() {
		return gxGlobalMSRPProdSeason;
	}

	public void setGxGlobalMSRPProdSeason(String gxGlobalMSRPProdSeason) {
		this.gxGlobalMSRPProdSeason = gxGlobalMSRPProdSeason;
	}

	public String getScspecialusage() {
		return scspecialusage;
	}

	public void setScspecialusage(String scspecialusage) {
		this.scspecialusage = scspecialusage;
	}

	public String getSccreationtrack() {
		return sccreationtrack;
	}

	public void setSccreationtrack(String sccreationtrack) {
		this.sccreationtrack = sccreationtrack;
	}

	public String getScTargetRetail() {
		return scTargetRetail;
	}

	public void setScTargetRetail(String scTargetRetail) {
		this.scTargetRetail = scTargetRetail;
	}

	public String getScInitialEstimatedUnits() {
		return scInitialEstimatedUnits;
	}

	public void setScInitialEstimatedUnits(String scInitialEstimatedUnits) {
		this.scInitialEstimatedUnits = scInitialEstimatedUnits;
	}

	public String getSccolorwaylifecycle() {
		return sccolorwaylifecycle;
	}

	public void setSccolorwaylifecycle(String sccolorwaylifecycle) {
		this.sccolorwaylifecycle = sccolorwaylifecycle;
	}

	public String getScmatconstruction() {
		return scmatconstruction;
	}

	public void setScmatconstruction(String scmatconstruction) {
		this.scmatconstruction = scmatconstruction;
	}
	
	public String getScFactoryPriority() {
		return scFactoryPriority;
	}

	public void setScFactoryPriority(String scFactoryPriority) {
		this.scFactoryPriority = scFactoryPriority;
	}
	
	public String getGxGlobalMSRPSKUSeason() {
		return gxGlobalMSRPSKUSeason;
	}

	public void setGxGlobalMSRPSKUSeason(String gxGlobalMSRPSKUSeason) {
		this.gxGlobalMSRPSKUSeason = gxGlobalMSRPSKUSeason;
	}

	public String getScHTSCode() {
		return scHTSCode;
	}

	public void setScHTSCode(String scHTSCode) {
		this.scHTSCode = scHTSCode;
	}
	
	public String getScKeyCategory() {
		return scKeyCategory;
	}

	public void setScKeyCategory(String scKeyCategory) {
		this.scKeyCategory = scKeyCategory;
	}

	public String getScKeyCategoryCode() {
		return scKeyCategoryCode;
	}

	public void setScKeyCategoryCode(String scKeyCategoryCode) {
		this.scKeyCategoryCode = scKeyCategoryCode;
	}

	public String getScKeyCategoryCluster() {
		return scKeyCategoryCluster;
	}

	public void setScKeyCategoryCluster(String scKeyCategoryCluster) {
		this.scKeyCategoryCluster = scKeyCategoryCluster;
	}

	public String getScKeyCategoryClusterCode() {
		return scKeyCategoryClusterCode;
	}

	public void setScKeyCategoryClusterCode(String scKeyCategoryClusterCode) {
		this.scKeyCategoryClusterCode = scKeyCategoryClusterCode;
	}

	public String getScBusinessSegment() {
		return scBusinessSegment;
	}

	public void setScBusinessSegment(String scBusinessSegment) {
		this.scBusinessSegment = scBusinessSegment;
	}

	public String getScBusinessSegmentCode() {
		return scBusinessSegmentCode;
	}

	public void setScBusinessSegmentCode(String scBusinessSegmentCode) {
		this.scBusinessSegmentCode = scBusinessSegmentCode;
	}

	@Override
	public String toString() {
		return "SparcArticlePOJO [scColorwayNo=" + scColorwayNo + ", sclicenserefnumber=" + sclicenserefnumber
				+ ", scNRFcolorgroup=" + scNRFcolorgroup + ", scNRFcolorcode=" + scNRFcolorcode + ", sclicensed="
				+ sclicensed + ", sclegacyarticle=" + sclegacyarticle + ", scColorNo=" + scColorNo + ", scColorDesc="
				+ scColorDesc + ", scColorShortDesc=" + scColorShortDesc + ", seasonType=" + seasonType + ", year="
				+ year + ", scPLMProductNo=" + scPLMProductNo + ", scBrand=" + scBrand + ", scGender=" + scGender
				+ ", scAge=" + scAge + ", scProductDescriptionShort=" + scProductDescriptionShort
				+ ", scProductDescription=" + scProductDescription + ", scworkingnumber=" + scworkingnumber
				+ ", scOutsole=" + scOutsole + ", sclining=" + sclining + ", scinlaysole=" + scinlaysole + ", scupper="
				+ scupper + ", scoutsoledescription=" + scoutsoledescription + ", scfitsilhoette=" + scfitsilhoette
				+ ", scproductwidth=" + scproductwidth + ", scbrandassetpartner=" + scbrandassetpartner
				+ ", sclegacymodelnumber=" + sclegacymodelnumber + ", sctechnology=" + sctechnology + ", scbenefits="
				+ scbenefits + ", scfeatures=" + scfeatures + ", scSizeRange=" + scSizeRange + ", scDivision="
				+ scDivision + ", scProductGroup=" + scProductGroup + ", scProductType=" + scProductType
				+ ", scRetailintrodate=" + scRetailintrodate + ", scRetailexitdate=" + scRetailexitdate
				+ ", scdistchannel=" + scdistchannel + ", scsportcategory=" + scsportcategory + ", sccorpmarketingline="
				+ sccorpmarketingline + ", sccatmarketingline=" + sccatmarketingline + ", scsalesline=" + scsalesline
				+ ", sctechnologyconcept=" + sctechnologyconcept + ", scsustainabilityethics=" + scsustainabilityethics
				+ ", scsellingregion=" + scsellingregion + ", scFranchise=" + scFranchise + ", scdevelopmentype="
				+ scdevelopmentype + ", scprodmanager=" + scprodmanager + ", scspecialusage=" + scspecialusage
				+ ", sccreationtrack=" + sccreationtrack + ", scTargetRetail=" + scTargetRetail
				+ ", scInitialEstimatedUnits=" + scInitialEstimatedUnits + ", sccolorwaylifecycle="
				+ sccolorwaylifecycle + ", scmatconstruction=" + scmatconstruction + ", scHTSCode=" + scHTSCode + ",scFactoryPriority=" + scFactoryPriority +" ]";
	}

	public SparcArticlePOJO(String scColorwayNo, String sclicenserefnumber, String scNRFcolorgroup,
			String scNRFcolorcode, String sclicensed, String sclegacyarticle, String scColorNo, String scColorDesc,
			String scColorShortDesc, String seasonType, String year, String scPLMProductNo, String scBrand,
			String scGender, String scAge, String scProductDescriptionShort, String scProductDescription,
			String scworkingnumber, String scOutsole, String sclining, String scinlaysole, String scupper,
			String scoutsoledescription, String scfitsilhoette, String scproductwidth, String scbrandassetpartner,
			String sclegacymodelnumber, String sctechnology, String scbenefits, String scfeatures, String scSizeRange,
			String scDivision, String scProductGroup, String scProductType, String scRetailintrodate,
			String scRetailexitdate, String scdistchannel, String scsportcategory, String sccorpmarketingline,
			String sccatmarketingline, String scsalesline, String sctechnologyconcept, String scsustainabilityethics,
			String scsellingregion, String scFranchise, String scdevelopmentype, String scprodmanager,
			String scspecialusage, String sccreationtrack, String scTargetRetail, String scInitialEstimatedUnits,
			String sccolorwaylifecycle, String scmatconstruction, String scHTSCode,String scFactoryPriority) {
		super();
		this.scColorwayNo = scColorwayNo;
		this.sclicenserefnumber = sclicenserefnumber;
		this.scNRFcolorgroup = scNRFcolorgroup;
		this.scNRFcolorcode = scNRFcolorcode;
		this.sclicensed = sclicensed;
		this.sclegacyarticle = sclegacyarticle;
		this.scColorNo = scColorNo;
		this.scColorDesc = scColorDesc;
		this.scColorShortDesc = scColorShortDesc;
		this.seasonType = seasonType;
		this.year = year;
		this.scPLMProductNo = scPLMProductNo;
		this.scBrand = scBrand;
		this.scGender = scGender;
		this.scAge = scAge;
		this.scProductDescriptionShort = scProductDescriptionShort;
		this.scProductDescription = scProductDescription;
		this.scworkingnumber = scworkingnumber;
		this.scOutsole = scOutsole;
		this.sclining = sclining;
		this.scinlaysole = scinlaysole;
		this.scupper = scupper;
		this.scoutsoledescription = scoutsoledescription;
		this.scfitsilhoette = scfitsilhoette;
		this.scproductwidth = scproductwidth;
		this.scbrandassetpartner = scbrandassetpartner;
		this.sclegacymodelnumber = sclegacymodelnumber;
		this.sctechnology = sctechnology;
		this.scbenefits = scbenefits;
		this.scfeatures = scfeatures;
		this.scSizeRange = scSizeRange;
		this.scDivision = scDivision;
		this.scProductGroup = scProductGroup;
		this.scProductType = scProductType;
		this.scRetailintrodate = scRetailintrodate;
		this.scRetailexitdate = scRetailexitdate;
		this.scdistchannel = scdistchannel;
		this.scsportcategory = scsportcategory;
		this.sccorpmarketingline = sccorpmarketingline;
		this.sccatmarketingline = sccatmarketingline;
		this.scsalesline = scsalesline;
		this.sctechnologyconcept = sctechnologyconcept;
		this.scsustainabilityethics = scsustainabilityethics;
		this.scsellingregion = scsellingregion;
		this.scFranchise = scFranchise;
		this.scdevelopmentype = scdevelopmentype;
		this.scprodmanager = scprodmanager;
		this.scspecialusage = scspecialusage;
		this.sccreationtrack = sccreationtrack;
		this.scTargetRetail = scTargetRetail;
		this.scInitialEstimatedUnits = scInitialEstimatedUnits;
		this.sccolorwaylifecycle = sccolorwaylifecycle;
		this.scmatconstruction = scmatconstruction;
		this.scHTSCode = scHTSCode;
		this.scFactoryPriority=scFactoryPriority;
	}
	
}