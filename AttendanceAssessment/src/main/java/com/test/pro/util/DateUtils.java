package com.test.pro.util;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期常用类
 * @author hai
 *
 */
public class DateUtils {
	public static LocalDate convertDate2LocalDate(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		int year = ca.get(Calendar.YEAR);
		int month = ca.get(Calendar.MONTH)+1;
		int day = ca.get(Calendar.DATE);
//		LocalDate locadateStart = LocalDate.of(ca.get(Calendar.DAY_OF_YEAR), ca.get(Calendar.DAY_OF_MONTH),
//				ca.get(Calendar.DATE));
		return LocalDate.of(year, month, day);
	}
}
