package com.test.pro.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.test.pro.entity.RecordStatus;

/**
 * 考勤状态表
 * 
 * @author hai
 *
 */
@Mapper
public interface RecordStatusMapper {
	/**
	 * 获取所有考勤状态
	 * @return
	 */
	public List<RecordStatus> queryAllRecordStatus();
}
