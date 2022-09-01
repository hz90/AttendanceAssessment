package com.test.pro.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.test.pro.service.TestService;
import com.test.pro.vo.EvaluateResultVo;
import com.test.pro.vo.UserVo;

/**
 * 考勤服务
 * 
 * @author hai
 *
 */
@RestController
public class TestController {

	@Autowired
	TestService testService;

	/**
	 * 获取所有用户ID
	 * 
	 * @return
	 */
	@GetMapping("/getAllUserID")
	public List<UserVo> getAllUserID() {
		return testService.getAllUserID();
	}

	/**
	 * 获取评估结果
	 * 
	 * @param userVo
	 * @return
	 */
	@PostMapping("/evaluateAttendance")
	public EvaluateResultVo evaluateAttendance(@RequestBody UserVo userVo) {
		return testService.evaluateAttendance(userVo);
	}

	/**
	 * 上传数据到DB
	 * 
	 * @param excelFile
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/upload")
	public Map<String, String> upload(MultipartFile excelFile) throws Exception {
		Map<String, String> map = new HashMap<>();
		if (excelFile.isEmpty()) {
			map.put("mag", "文件为空，重新上传");
			return map;
		} else {
			String fileName = excelFile.getOriginalFilename();
			InputStream is = excelFile.getInputStream();
			if (testService.importUserInfo(fileName, is)) {
				map.put("msg", "数据添加成功");
				return map;
			} else {
				map.put("msg", "数据添加失败，请重新添加");
				return map;
			}
		}
	}

	/**
	 * 导出数据到excel
	 * 
	 * @param response
	 */
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void download(HttpServletResponse response) {
		testService.download(response);
	}
}
