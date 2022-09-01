package com.test.pro.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import com.test.pro.entity.AttendanceRecords;

/**
 * 考勤记录表
 * 
 * @author hai
 *
 */
@Mapper
public interface AttendanceRecordsMapper {
	
	
	/**
	 * 批量插入考勤记录
	 * @return
	 */
	public int addBatchAttendanceRecords(List<AttendanceRecords> lstAttendanceRecords);
	
	/**
	 * 删除考勤记录
	 * @return
	 */
	@Delete("delete from attendancerecords")
	public int deleteAttendanceRecords();
	
	
	/**
	 * 获取所有用户ID
	 * @return
	 */
	public List<String> queryAllUserIds();
	/**
	 * 获取所有考勤记录
	 * @return
	 */
	public List<AttendanceRecords> queryAllAttendanceRecords();

	
	/**
	 * 根据用户ID获取用户考勤记录
	 * @param userId
	 * @return
	 */
	public List<AttendanceRecords> queryRecordsByUserId(String userId);

}
