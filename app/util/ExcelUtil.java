package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ExcelUtil {

	public static File generateXlsFile(String sheetName, String[] columnHeader, Object[][] dataList) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(sheetName);

		int rownum = 0;
		int cellnum = 0; 
		Row row = sheet.createRow(rownum++);
		for (String column : columnHeader) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(column);
		}

		for (Object[] data : dataList) {
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (Object obj : data) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date)
					cell.setCellValue((Date) obj);
				else if (obj instanceof Boolean)
					cell.setCellValue((Boolean) obj);
				else if (obj instanceof Double)
					cell.setCellValue((Double) obj);
				else
					cell.setCellValue(String.valueOf(obj));
			}
		}

		for (int i = 0; i < columnHeader.length; i++) {
			sheet.autoSizeColumn(i);
		}

		File file = new File("file.xls");
		try {
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();

			return file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
