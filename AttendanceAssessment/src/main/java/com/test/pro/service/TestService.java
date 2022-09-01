package com.test.pro.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.pro.entity.AttendanceRecords;
import com.test.pro.entity.RecordStatus;
import com.test.pro.mapper.AttendanceRecordsMapper;
import com.test.pro.mapper.RecordStatusMapper;
import com.test.pro.util.DateUtils;
import com.test.pro.util.ExcelUtil;
import com.test.pro.util.StatusConst;
import com.test.pro.vo.EvaluateResultVo;
import com.test.pro.vo.UserVo;

/**
 * 考勤服务
 * 
 * @author hai
 *
 */
@Service
public class TestService {
	@Autowired
	AttendanceRecordsMapper attendanceRecordsMapper;
	@Autowired
	RecordStatusMapper recordStatusMapper;

	/**
	 * 获取所有用户ID
	 * 
	 * @return
	 */
	public List<UserVo> getAllUserID() {
		List<String> lstUserIds = attendanceRecordsMapper.queryAllUserIds();
		List<UserVo> lstUserVo = new ArrayList<>();
		lstUserIds.forEach(x -> {
			UserVo userVo = new UserVo();
			userVo.setUserId(x);
			lstUserVo.add(userVo);
		});
		return lstUserVo;
	}

	/**
	 * 评估考勤结果
	 * 
	 * @param userVo
	 * @return
	 */
	public EvaluateResultVo evaluateAttendance(UserVo userVo) {
		List<AttendanceRecords> lstAttendanceRecords = attendanceRecordsMapper.queryRecordsByUserId(userVo.getUserId());

		EvaluateResultVo resultVo = new EvaluateResultVo();
		// 先检查该用户数据是否存在
		if (lstAttendanceRecords != null && lstAttendanceRecords.size() > 0) {
			// 检查考勤记录日期存在不连续的日期的时候
			if (checkConsecutiveDates(lstAttendanceRecords)) {
				List<Integer> lstStatus = lstAttendanceRecords.stream().map(x -> x.getStatusId())
						.collect(Collectors.toList());
				if (getEvaluateResult(lstStatus) == StatusConst.ATTENDANCE_NORMAL) {
					resultVo.setResult("正常");
				} else {
					resultVo.setResult("失败");
				}
			} else {
				resultVo.setResult("该用户的考勤记录日期不连续");
			}
		} else {
			resultVo.setResult("该用户的考勤记录不存在");
		}

		return resultVo;
	}

	/**
	 * 
	 * 1:正常 2:迟到 3:缺勤 4:休假
	 * 
	 * @param lstStatus
	 * @return 1：考勤失败 0：考勤正常
	 */
	private int getEvaluateResult(List<Integer> lstStatus) {
		// 员工连续7天内存在2次缺勤视为考勤失败
		Function<Map<Integer, Long>, Integer> fcuntion1 = k -> convertNull2Zero(k.get(StatusConst.ABSENT)) >= 2
				? StatusConst.ATTENDANCE_FAILED
				: StatusConst.ATTENDANCE_NORMAL;
		// 员工连续7天内存在1次缺勤和两次迟到视为考勤失败
		Function<Map<Integer, Long>, Integer> fcuntion2 = k -> convertNull2Zero(k.get(StatusConst.ABSENT)) >= 1
				&& convertNull2Zero(k.get(StatusConst.LATE)) >= 2 ? StatusConst.ATTENDANCE_FAILED
						: StatusConst.ATTENDANCE_NORMAL;
		// 员工存在连续3次迟到视为考勤失败
		Function<Map<Integer, Long>, Integer> fcuntion3 = k -> convertNull2Zero(k.get(StatusConst.LATE)) >= 3
				? StatusConst.ATTENDANCE_FAILED
				: StatusConst.ATTENDANCE_NORMAL;
		// 员工连续30天考勤正常次数小于17天视为考勤失败
		Function<Map<Integer, Long>, Integer> fcuntion4 = k -> convertNull2Zero(k.get(StatusConst.NORMAL)) < 17
				? StatusConst.ATTENDANCE_FAILED
				: StatusConst.ATTENDANCE_NORMAL;
		// 员工存在连续3次迟到视为考勤失败
		int k = checkRecordsByWindowsSize(lstStatus, 3, new ArrayList<Function<Map<Integer, Long>, Integer>>() {
			{
				add(fcuntion3);
			}
		});
		// 一旦考勤失败的情况，返回
		if (k != StatusConst.ATTENDANCE_NORMAL) {
			return k;
		}
		// 员工连续7天内的数据检查存在两次缺勤、或者存在一次缺勤和两次迟到的情况
		k = checkRecordsByWindowsSize(lstStatus, 7, new ArrayList<Function<Map<Integer, Long>, Integer>>() {
			{
				add(fcuntion1);
				add(fcuntion2);
			}
		});
		// 一旦考勤失败的情况，返回
		if (k != StatusConst.ATTENDANCE_NORMAL) {
			return k;
		}
		// 数据超过30天的情况
		if (lstStatus.size() >= 30) {
			// 计算考勤正常次数是否小于17天
			k = checkRecordsByWindowsSize(lstStatus, 30, new ArrayList<Function<Map<Integer, Long>, Integer>>() {
				{
					add(fcuntion4);
				}
			});
			// 一旦考勤失败的情况，返回
			if (k != StatusConst.ATTENDANCE_NORMAL) {
				return k;
			}
		}
		return StatusConst.ATTENDANCE_NORMAL;
	}

	/**
	 * 
	 * 算出窗口内是否存在考勤失败或者正常的数据
	 * 
	 * @param lstStatus
	 * @return 1：数据包含考勤失败的数据 0：考勤数据正常
	 */
	private static int checkRecordsByWindowsSize(List<Integer> lstStatus, int windowsize,
			List<Function<Map<Integer, Long>, Integer>> frule) {
		LinkedList<Integer> windowDatas = new LinkedList<>();
		for (int index = 0; index < lstStatus.size(); index++) {
			windowDatas.add(lstStatus.get(index));
			if (windowDatas.size() == windowsize) {
				System.out.println(windowDatas);
				// 统计窗口中的各状态个数
				Map<Integer, Long> countMap = windowDatas.stream()
						.collect(Collectors.groupingBy(x -> (Integer) x, Collectors.counting()));
				// 对数据进行评估
				// 一旦发现评估失败的数据，循环结束
				if (!countStatus(countMap, frule)) {
					return StatusConst.ATTENDANCE_FAILED;
				}
				windowDatas.removeFirst();
			}
		}
		// 数据量小于窗口大小的时候
		if (lstStatus.size() < windowsize) {
			System.out.println(windowDatas);
			// 统计窗口中的各状态个数
			Map<Integer, Long> countMap = windowDatas.stream()
					.collect(Collectors.groupingBy(x -> (Integer) x, Collectors.counting()));
			if (!countStatus(countMap, frule)) {
				return StatusConst.ATTENDANCE_FAILED;
			}
		}
		return StatusConst.ATTENDANCE_NORMAL;
	}

	/**
	 * 是否有考勤失败的数据
	 * 
	 * @param countMap
	 * @param frule
	 * @return true：考勤正常 、false：考勤失败
	 */
	private static boolean countStatus(Map<Integer, Long> countMap, List<Function<Map<Integer, Long>, Integer>> frule) {
		// 对数据进行评估
		int result = 0;
		for (Function<Map<Integer, Long>, Integer> f : frule) {
			result = result + f.apply(countMap);
		}
		return result == StatusConst.ATTENDANCE_NORMAL;
	}

	/**
	 * 检查考勤记录日期是否是连续的日期
	 * 
	 * @param lstAttendanceRecords
	 * @return true：日期连续、false：日期不连续
	 */
	private boolean checkConsecutiveDates(List<AttendanceRecords> lstAttendanceRecords) {
		AttendanceRecords attendanceRecords = lstAttendanceRecords.get(0);
		LocalDate locadateStart = DateUtils.convertDate2LocalDate(attendanceRecords.getRecordDate());
		attendanceRecords = lstAttendanceRecords.get(lstAttendanceRecords.size() - 1);
		LocalDate locadateEnd = DateUtils.convertDate2LocalDate(attendanceRecords.getRecordDate());
		long days = ChronoUnit.DAYS.between(locadateStart, locadateEnd) + 1;
		// 件数=（最大日期和最小日期）之差的天数的话
		return new Long(days).intValue() == lstAttendanceRecords.size();
	}

	/**
	 * null转成0
	 * 
	 * @param number
	 * @return
	 */
	private static long convertNull2Zero(Long number) {
		if (number == null) {
			return 0;
		} else {
			return number;
		}
	}

	/**
	 * 批量插入
	 * 
	 * @param fileName
	 * @param is
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean importUserInfo(String fileName, InputStream is) {
		List<RecordStatus> lstRecordStatus = recordStatusMapper.queryAllRecordStatus();
		Map<String, Integer> mapStatus = lstRecordStatus.stream()
				.collect(Collectors.toMap(RecordStatus::getRecordStatus, RecordStatus::getStatusId));
		List<AttendanceRecords> lstAttendanceRecords;
		try {
			lstAttendanceRecords = new ExcelUtil().upload(fileName, is, mapStatus);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (lstAttendanceRecords != null && lstAttendanceRecords.size() > 0) {
			// 插入数据之前删除整表
			attendanceRecordsMapper.deleteAttendanceRecords();
			return attendanceRecordsMapper.addBatchAttendanceRecords(lstAttendanceRecords) > 0; // 批量添加
		} else {
			return false;
		}
	}

	/**
	 * 导出数据到excel
	 * 
	 * @param response
	 */
	public void download(HttpServletResponse response) {
		List<RecordStatus> lstRecordStatus = recordStatusMapper.queryAllRecordStatus();
		Map<Integer, String> mapStatus = lstRecordStatus.stream()
				.collect(Collectors.toMap(RecordStatus::getStatusId, RecordStatus::getRecordStatus));
		List<AttendanceRecords> lstAttendanceRecords = attendanceRecordsMapper.queryAllAttendanceRecords();
		List<String> fieldMap = new ArrayList<>(); // 数据列信息
		fieldMap.add("id");
		fieldMap.add("userid");
		fieldMap.add("record");
		fieldMap.add("record_date");
		XSSFWorkbook workbook = new XSSFWorkbook(); // 新建工作簿对象
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFDataFormat xSSFDataFormat = workbook.createDataFormat();
		cellStyle.setDataFormat(xSSFDataFormat.getFormat("@"));
		XSSFSheet sheet = workbook.createSheet("attendancerecords");// 创建sheet
		int rowNum = 0;
		Row row = sheet.createRow(rowNum);// 创建第一行对象,设置表标题
		Cell cell;
		int cellNum = 0;
		for (String name : fieldMap) {
			cell = row.createCell(cellNum);
			cell.setCellValue(name);
			cellNum++;
		}
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		int rows = 1;
		for (AttendanceRecords attendanceRecords : lstAttendanceRecords) {// 遍历数据插入excel中
			row = sheet.createRow(rows);
			int col = 0;
			createCell(row, col, cellStyle).setCellValue(String.valueOf(attendanceRecords.getId())); // id
			createCell(row, col + 1, cellStyle).setCellValue(attendanceRecords.getUserId()); // UserId
			createCell(row, col + 2, cellStyle).setCellValue(mapStatus.get(attendanceRecords.getStatusId())); // record
			createCell(row, col + 3, cellStyle).setCellValue(ft.format(attendanceRecords.getRecordDate())); // record_date
			rows++;
			row.setRowStyle(cellStyle);
		}
		String fileName = "attendancerecords";
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			response.reset();
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			workbook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建单元格
	 * 
	 * @param row
	 * @param col
	 * @param cellStyle
	 * @return
	 */
	private Cell createCell(Row row, int col, XSSFCellStyle cellStyle) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(cellStyle);
		return cell;
	}

	public static String main(String strIn) {
		String str = "";
		Queue<Character> lstStrt = new LinkedList<>();
		HashSet<Character> set = new HashSet<Character>();
		char[] strArray = strIn.toCharArray();
		for (int index = 0; index < strArray.length; index++) {
			char c=strArray[index];
			if(set.contains(c)) {
				lstStrt.offer(strArray[index]);
				Queue<Character> lstStrtTmp = new LinkedList<>();
				for (int i = lstStrt.size(); i ==0; i--) {
					
				}
			}else {
				set.add(strArray[index]);
			}
			lstStrt.offer(strArray[index]);
			
		}
		return str;
	}

}
