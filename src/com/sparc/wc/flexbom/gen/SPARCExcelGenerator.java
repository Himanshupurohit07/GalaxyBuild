package com.sparc.wc.flexbom.gen;

import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.ExcelGenerator;
import com.lcs.wc.client.web.ExcelGeneratorHelper;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.util.FormatHelper;

public class SPARCExcelGenerator extends ExcelGenerator {
	public String drawTable(Collection<FlexObject> data, Collection<TableColumn> columns, ClientContext context,
			String reportName, String headerContent) {

		setData(data);
		setColumns(columns);

		super.context = context;
		super.reportName = reportName;
		intializeWorksheet();
		setTableId();
		String tid = getTableId();
		Iterator i = columns.iterator();

		TableColumn column;
		while (i.hasNext()) {
			column = (TableColumn) i.next();
			column.setFormatHTML(false);
		}

		if (groupByColumns != null) {
			i = groupByColumns.iterator();

			while (i.hasNext()) {
				((TableColumn) i.next()).setFormatHTML(false);
			}
		}

		if (FormatHelper.hasContent(rowIdIndex) && columns != null && columns.size() > 0) {
			i = super.columns.iterator();

			while (i.hasNext()) {
				((TableColumn) i.next()).setRowIdIndex(rowIdIndex);
			}
		}
		drawHeader(headerContent);
		drawContentColumnHeaders("T" + tid);
		ws.createFreezePane(0, getRowcount());
		// LOGGER.debug("---before drawContentData()");
		drawContentData();
		// LOGGER.debug("---after drawContentData()");
		i = columns.iterator();

		while (i.hasNext()) {
			column = (TableColumn) i.next();
			column.setFormatHTML(true);
		}
		return this.printFile();
	}

	public void drawHeader(String headerContent) {
		if (ethg != null) {
			super.setRowcount(ethg.createHeader(wb, ws, columns.size()));
		}
		HSSFRow row = ws.createRow(getRowcount());
		setRowcount(getRowcount() + 1);
		HSSFCell cell = ExcelGeneratorHelper.createCell(row, 0);
		cell.setCellValue(headerContent);
		HSSFCellStyle cellstyle = egh.getCellStyle(tableBackgroundClass, "left", fontSize,
				HSSFDataFormat.getBuiltinFormat("text"), false);
		cell.setCellStyle(cellstyle);

	}
}