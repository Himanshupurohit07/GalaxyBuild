package com.acn.migration;

import java.util.Hashtable;
import java.util.Vector;

import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.load.LoadFlexTyped;
import com.lcs.wc.load.LoadHelper;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialColorQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;

public class CustomLoadFlexTyped extends LoadFlexTyped {
	public static boolean createFlexTypeObjectReferenceToLink(Hashtable<?, ?> dataValues, Hashtable<?, ?> commandLine,
			Vector<?> returnObjects) {
		return createFlexTypeObjectReferenceToLink(dataValues, LoadCommon.getValue(commandLine, "FileName", false));
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
									String ObjectSupplierValue = LoadCommon.getValue(dataValues, "ObjectRoleBValue",
											true);

									if (ObjectSupplierValue == null) {

										return false;
									} else {
										String ObjectSupplierAttKey = LoadCommon.getValue(dataValues,
												"ObjectRoleBAttKey", true);

										if (ObjectSupplierAttKey == null) {

											return false;
										} else {
											String ObjectColorType = LoadCommon.getValue(dataValues, "ObjectRoleCtype",
													true);

											String ObjectColorValue = LoadCommon.getValue(dataValues,
													"ObjectRoleCValue", true);

											String ObjectColorAttKey = LoadCommon.getValue(dataValues,
													"ObjectRoleCAttKey", true);

											String ObjectToObjectLinkAttKey = LoadCommon.getValue(dataValues,
													"ObjectToObjectLinkAttKey", true);

											if (ObjectToObjectLinkAttKey == null) {

												return false;
											} else {
												String attType = LoadCommon.getValue(dataValues, "AttributeType", true);

												if (attType == null) {

													return false;
												} else {
													String attValue = LoadCommon.getValue(dataValues, "AttributeValue",
															true);

													if (attValue == null) {
														attValue = "";
													}
													String attKey = LoadCommon.getValue(dataValues, "AttributeKey",
															true);
													LoadCommon.display("attKey: " + attKey);
													if (attKey == null) {

														return false;
													} else {
														Hashtable table;
														if ("MaterialSupplier".equals(linkIdentifier)) {

															LCSMaterialSupplier materialSupplier = LCSMaterialSupplier
																	.newLCSMaterialSupplier();
															if (FormatHelper.hasContent(fileName)
																	&& fileName.indexOf("LCSObjectReferences") > -1) {
																table = new Hashtable();
																table.put(ObjectMaterialAttKey, ObjectMaterialValue);
																FlexTyped material = (FlexTyped) LoadCommon
																		.getObjectByAttributes(ObjectMaterialType,
																				table, (String) null, false);
																LoadCommon.display("Material is " + material);
																table = new Hashtable();
																table.put(ObjectSupplierAttKey, ObjectSupplierValue);
																FlexTyped supplier = (FlexTyped) LoadCommon
																		.getObjectByAttributes(ObjectSupplierType,
																				table, (String) null, false);
																LoadCommon.display("Supplier is " + supplier);
																materialSupplier = LCSMaterialSupplierQuery
																		.findMaterialSupplier(
																				((LCSMaterial) material).getMaster(),
																				((LCSSupplier) supplier).getMaster());
																LoadCommon.display(
																		"Found materialSupplier: " + materialSupplier);
															}
															if (materialSupplier == null) {

																return false;
															}
															LoadCommon.display("Setting value on materialSupplier...");
															if (!setValue(materialSupplier,
																	ObjectToObjectLinkAttKey + "=" + attKey, attValue,
																	fileName).equals("")) {
																LoadCommon.display("Set value returned error");
																return false;
															}
															LoadHelper.deriveFlexTypeValues(materialSupplier);
															PersistenceServerHelper.manager.update(materialSupplier);
															LoadCommon.display("Updated materialSupplier");
														} else if ("MaterialColor".equals(linkIdentifier)
																&& ObjectColorType != null && ObjectColorValue != null
																&& ObjectColorAttKey != null) {
															LoadCommon.display("Processing MaterialColor link");
															LCSMaterialColor materialColor = LCSMaterialColor
																	.newLCSMaterialColor();
															table = new Hashtable();
															table.put(ObjectMaterialAttKey, ObjectMaterialValue);
															WTObject material = LoadCommon.getObjectByAttributes(
																	ObjectMaterialType, table, (String) null, false);
															table.put(ObjectSupplierAttKey, ObjectSupplierValue);
															WTObject supplier = LoadCommon.getObjectByAttributes(
																	ObjectSupplierType, table, (String) null, false);
															table.put(ObjectColorAttKey, ObjectColorValue);
															WTObject color = LoadCommon.getObjectByAttributes(
																	ObjectColorType, table, (String) null, false);
															String materialId = FormatHelper
																	.getNumericObjectIdFromObject(
																			((LCSMaterial) material).getMaster());
															String supplierId = FormatHelper
																	.getNumericObjectIdFromObject(
																			((LCSSupplier) supplier).getMaster());
															String colorId = FormatHelper
																	.getNumericObjectIdFromObject(color);
															LoadCommon.display(
																	"materialId: " + materialId + ", supplierId: "
																			+ supplierId + ", colorId: " + colorId);
															materialColor = LCSMaterialColorQuery
																	.findMaterialColorsForMaterialSupplierAndColor(
																			materialId, supplierId, colorId);
															if (materialColor == null) {

																return false;
															}
															LoadCommon.display("Setting value on materialColor...");
															if (!setValue(materialColor,
																	ObjectToObjectLinkAttKey + "=" + attKey, attValue,
																	fileName).equals("")) {
																LoadCommon.display("Set value returned error");
																return false;
															}
															LoadHelper.deriveFlexTypeValues(materialColor);
															PersistenceServerHelper.manager.update(materialColor);
															LoadCommon.display("Updated materialColor");
														}
														LoadCommon.display(
																"createFlexTypeObjectReferenceToLink successful");
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
			System.out.println("Exception occurred in createFlexTypeObjectReferenceToLink");
			return false;
		}
	}

//	protected static boolean createFlexTypeObjectReferenceToLink(Hashtable<?, ?> dataValues, String fileName) {
//		try {
//			String linkIdentifier = LoadCommon.getValue(dataValues, "linkIdentifier", true);
//			if (linkIdentifier == null) {
//				return false;
//			} else {
//				String ObjectLinkValue = LoadCommon.getValue(dataValues, "ObjectLinkValue", true);
//				if (ObjectLinkValue == null) {
//					return false;
//				} else {
//					String ObjectMaterialType = LoadCommon.getValue(dataValues, "ObjectRoleAtype", true);
//					if (ObjectMaterialType == null) {
//						return false;
//					} else {
//						String ObjectMaterialValue = LoadCommon.getValue(dataValues, "ObjectRoleAValue", true);
//						if (ObjectMaterialValue == null) {
//							return false;
//						} else {
//							String ObjectMaterialAttKey = LoadCommon.getValue(dataValues, "ObjectRoleAAttKey", true);
//							if (ObjectMaterialAttKey == null) {
//								return false;
//							} else {
//								String ObjectSupplierType = LoadCommon.getValue(dataValues, "ObjectRoleBtype", true);
//								if (ObjectSupplierType == null) {
//									return false;
//								} else {
//									String ObjectSupplierValue = LoadCommon.getValue(dataValues, "ObjectRoleBValue",
//											true);
//									if (ObjectSupplierValue == null) {
//										return false;
//									} else {
//										String ObjectSupplierAttKey = LoadCommon.getValue(dataValues,
//												"ObjectRoleBAttKey", true);
//										if (ObjectSupplierAttKey == null) {
//											return false;
//										} else {
//											String ObjectColorType = LoadCommon.getValue(dataValues, "ObjectRoleCtype",
//													true);
//											String ObjectColorValue = LoadCommon.getValue(dataValues,
//													"ObjectRoleCValue", true);
//											String ObjectColorAttKey = LoadCommon.getValue(dataValues,
//													"ObjectRoleCAttKey", true);
//											String ObjectToObjectLinkAttKey = LoadCommon.getValue(dataValues,
//													"ObjectToObjectLinkAttKey", true);
//											if (ObjectToObjectLinkAttKey == null) {
//												return false;
//											} else {
//												String attType = LoadCommon.getValue(dataValues, "AttributeType", true);
//												if (attType == null) {
//													return false;
//												} else {
//													String attValue = LoadCommon.getValue(dataValues, "AttributeValue",
//															true);
//													if (attValue == null) {
//														attValue = "";
//													}
//
//													String attKey = LoadCommon.getValue(dataValues, "AttributeKey",
//															true);
//													if (attKey == null) {
//														return false;
//													} else {
//														Hashtable table;
//														if ("MaterialSupplier".equals(linkIdentifier)) {
//															LCSMaterialSupplier materialSupplier = LCSMaterialSupplier
//																	.newLCSMaterialSupplier();
//															if (FormatHelper.hasContent(fileName)
//																	&& fileName.indexOf("LCSObjectReferences") > -1) {
//																table = new Hashtable();
//																table.put(ObjectMaterialAttKey, ObjectMaterialValue);
//																FlexTyped material = (FlexTyped) LoadCommon
//																		.getObjectByAttributes(ObjectMaterialType,
//																				table, (String) null, false);
//																table.put(ObjectSupplierAttKey, ObjectSupplierValue);
//																FlexTyped supplier = (FlexTyped) LoadCommon
//																		.getObjectByAttributes(ObjectSupplierType,
//																				table, (String) null, false);
//																materialSupplier = LCSMaterialSupplierQuery
//																		.findMaterialSupplier(
//																				((LCSMaterial) material).getMaster(),
//																				((LCSSupplier) supplier).getMaster());
//															}
//
//															if (materialSupplier == null) {
//																return false;
//															}
//
//															if (!setValue(materialSupplier,
//																	ObjectToObjectLinkAttKey + "=" + attKey, attValue,
//																	fileName).equals("")) {
//																return false;
//															}
//
//															LoadHelper.deriveFlexTypeValues(materialSupplier);
//															PersistenceServerHelper.manager.update(materialSupplier);
//														} else if ("MaterialColor".equals(linkIdentifier)
//																&& ObjectColorType != null && ObjectColorValue != null
//																&& ObjectColorAttKey != null) {
//															LCSMaterialColor materialColor = LCSMaterialColor
//																	.newLCSMaterialColor();
//															table = new Hashtable();
//															table.put(ObjectMaterialAttKey, ObjectMaterialValue);
//															WTObject material = LoadCommon.getObjectByAttributes(
//																	ObjectMaterialType, table, (String) null, false);
//															table.put(ObjectSupplierAttKey, ObjectSupplierValue);
//															WTObject supplier = LoadCommon.getObjectByAttributes(
//																	ObjectSupplierType, table, (String) null, false);
//															table.put(ObjectColorAttKey, ObjectColorValue);
//															WTObject color = LoadCommon.getObjectByAttributes(
//																	ObjectColorType, table, (String) null, false);
//															String materialId = FormatHelper
//																	.getNumericObjectIdFromObject(
//																			((LCSMaterial) material).getMaster());
//															String supplierId = FormatHelper
//																	.getNumericObjectIdFromObject(
//																			((LCSSupplier) supplier).getMaster());
//															String colorId = FormatHelper
//																	.getNumericObjectIdFromObject(color);
//															materialColor = LCSMaterialColorQuery
//																	.findMaterialColorsForMaterialSupplierAndColor(
//																			materialId, supplierId, colorId);
//															if (materialColor == null) {
//																return false;
//															}
//
//															if (!setValue(materialColor,
//																	ObjectToObjectLinkAttKey + "=" + attKey, attValue,
//																	fileName).equals("")) {
//																return false;
//															}
//
//															LoadHelper.deriveFlexTypeValues(materialColor);
//															PersistenceServerHelper.manager.update(materialColor);
//														}
//
//														return true;
//													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception var25) {
//			var25.printStackTrace();
//			LoadCommon.display("\n#WTException : " + var25.getLocalizedMessage());
//			return false;
//		}
//	}
//

}
