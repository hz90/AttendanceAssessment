package com.test.pro.util;

import static org.apache.logging.log4j.util.Strings.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.test.pro.entity.AttendanceRecords;
/**
 * excel常用类
 * @author hai
 *
 */
public class ExcelUtil {

	/**
	 * 读取excel文件
	 *
	 * @param path 文件地址
	 */
	public List<AttendanceRecords> upload(String fileName, InputStream is, Map<String, Integer> mapStatus)
			throws Exception {
		Workbook workbook = getWorkbook(fileName, is);
		List<AttendanceRecords> lstAttendanceRecords = new ArrayList<>();
		/*
		 * wb.getSheetAt(0) 简单的取第一个sheet的表格读取
		 */
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		Sheet sheet = workbook.getSheetAt(0); // 获取表格页码
		for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 一般来说第一行是标题,所以第二行开始读取
			Row row = sheet.getRow(i);
			AttendanceRecords attendanceRecords = new AttendanceRecords();
			attendanceRecords.setId(Integer.valueOf(row.getCell(0).getStringCellValue()));
			attendanceRecords.setUserId((row.getCell(1).getStringCellValue()));
			attendanceRecords.setStatusId(mapStatus.get(row.getCell(2).getStringCellValue()));
			attendanceRecords.setRecordDate(ft.parse(row.getCell(3).getStringCellValue()));
			lstAttendanceRecords.add(attendanceRecords);
		}
		return lstAttendanceRecords;
	}

	/**
	 * 单元格不同格式获取值
	 */
	public static String getCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
				return timeFormatter.format(cell.getLocalDateTimeCellValue());
			} else {
				return cell.getNumericCellValue() + "";
			}
		case FORMULA:
			return cell.getCellFormula() + EMPTY;
		default:
			return EMPTY;
		}
	}

	// 判断传入的文件是哪种类型的excel文件
	public Workbook getWorkbook(String fileName, InputStream is) throws Exception {
		Workbook workbook = null;
		String name = fileName.substring(fileName.lastIndexOf("."));
		System.out.println(name);
		if (".xls".equals(name)) {
			workbook = new HSSFWorkbook(is);
		} else if (".xlsx".equals(name)) {
			workbook = new XSSFWorkbook(is);
		} else {
			throw new Exception(" 请上传.xls/.xlsx格式文件！");
		}
		return workbook;
	}
}
