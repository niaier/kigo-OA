package com.qingfeng.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.activiti.Assignment;
import com.qingfeng.entity.system.Group;
import com.qingfeng.entity.system.Organize;
import com.qingfeng.entity.system.SystemUser;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.system.service.IGroupService;
import com.qingfeng.system.service.IOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * @Title: ActivitiAssignmentController
 * @ProjectName com.qingfeng
 * @Description: Controllerå±‚
 * @author anxingtao
 * @date 2020-9-22 22:45
 */
@Controller
@RequestMapping(value = "/activiti/assignment")
public class AssignmentController extends BaseController {

	@Autowired
	private IAssignmentService assignmentService;
	@Autowired
	private IGroupService groupService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrganizeService organizeService;


	/**
	 * @title findAssignmentList
	 * @description findAssignmentList
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 17:00
	 */
	@GetMapping("/findActivitiAssignment")
	public void findActivitiAssignment(Assignment assignment, HttpServletResponse response) throws IOException  {
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("model_id",assignment.getModel_id());
		queryWrapper.eq("node_key",assignment.getNode_key());
		queryWrapper.orderByDesc("create_time");
		Assignment data = assignmentService.getOne(queryWrapper);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setData(data);
		json.setSuccess(true);
		this.writeJson(response,json);
	}


	/** 
	 * @Description: save
	 * @Param: [request, response, session] 
	 * @return: void 
	 * @Author: anxingtao
	 * @Date: 2020-11-8 21:29 
	 */ 
	@PostMapping("/save")
	public void save(Assignment assignment ,HttpServletResponse response) throws IOException  {
		String time = DateTimeUtil.getDateTimeStr();
		//èŽ·å?–å½“å‰?ç™»å½•ç”¨æˆ·ä¿¡æ?¯
		String user_id = "1";
		String organize_id = "1";

		if(Verify.verifyIsNotNull(assignment.getId())){
			assignment.setUpdate_time(time);
			assignment.setUpdate_user(user_id);
			assignmentService.updateById(assignment);
		}else{
			//ä¸»é”®id
			String id = GuidUtil.getUuid();
			assignment.setId(id);
			assignment.setCreate_time(time);
			assignment.setCreate_user(user_id);
			assignment.setCreate_organize(organize_id);
			assignmentService.save(assignment);
		}
		Json json = new Json();
		json.setSuccess(true);
		json.setMsg("æ“?ä½œæˆ?åŠŸã€‚");
		this.writeJson(response,json);
	}



	/**
	 * @title findGroupList
	 * @description æŸ¥è¯¢ç”¨æˆ·ç»„åˆ—è¡¨
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 17:27
	 */
	@GetMapping("/findGroupList")
	public void findList(QueryRequest queryRequest, Group group,HttpServletResponse response) throws Exception {
		//å¤„ç?†æ•°æ?®æ?ƒé™?
		List<Group> list = groupService.findList(group);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setData(list);
		json.setSuccess(true);
		this.writeJson(response,json);
	}

	/**
	 * @title findOrganizeList
	 * @description æŸ¥è¯¢ç»„ç»‡åˆ—è¡¨
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 18:25
	 */
	@GetMapping("/findOrganizeListPage")
	public void findOrganizeListPage(QueryRequest queryRequest, Organize organize,HttpServletResponse response) throws Exception {
		IPage<Organize> page = organizeService.findListPage(organize, queryRequest);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setCount((int)page.getTotal());
		json.setData(page.getRecords());
		json.setSuccess(true);
		this.writeJson(response,json);
	}

	/**
	 * @title findOrganizeInfo
	 * @description æŸ¥è¯¢ç»„ç»‡ä¿¡æ?¯è¯¦æƒ…
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 18:32
	 */
	@RequestMapping(value = "/findOrganizeInfo", method = RequestMethod.GET)
	public void findOrganizeInfo(Organize organize,HttpServletResponse response) throws Exception {
		Organize data = organizeService.getById(organize.getId());
		Json json = new Json();
		json.setData(data);
		json.setMsg("æ•°æ?®èŽ·å?–æˆ?åŠŸ");
		json.setSuccess(true);
		this.writeJson(response,json);
	}



	/**
	 * @title findUserList
	 * @description æŸ¥è¯¢ç”¨æˆ·åˆ—è¡¨
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 18:28
	 */
	@GetMapping("/findUserListPage")
	public void findUserListPage(QueryRequest queryRequest, SystemUser user, HttpServletResponse response) throws Exception {
		IPage<SystemUser> page = userService.findListPage(user, queryRequest);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setCount((int)page.getTotal());
		json.setData(page.getRecords());
		json.setSuccess(true);
		this.writeJson(response,json);
	}

	/**
	 * @title findUserInfo
	 * @description æŸ¥è¯¢ç”¨æˆ·ä¿¡æ?¯è¯¦æƒ…
	 * @author Administrator
	 * @updateTime 2021/8/29 0029 18:32
	 */
	@RequestMapping(value = "/findUserInfo", method = RequestMethod.GET)
	public void findUserInfo(SystemUser user,HttpServletResponse response) throws Exception {
		SystemUser data = userService.getById(user.getId());
		Json json = new Json();
		json.setData(data);
		json.setMsg("æ•°æ?®èŽ·å?–æˆ?åŠŸ");
		json.setSuccess(true);
		this.writeJson(response,json);
	}



	/**
	 * @title findOrganizeList
	 * @description æŸ¥è¯¢ç»„ç»‡åˆ—è¡¨
	 * @author Administrator
	 * @updateTime 2021/9/1 0001 22:49
	 */
	@GetMapping("/findOrganizeList")
	public void findOrganizeList(QueryRequest queryRequest, Organize organize,HttpServletResponse response) throws Exception {
		List<Organize> list = organizeService.findList(organize);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setData(list);
		json.setSuccess(true);
		this.writeJson(response,json);
	}

	/**
	 * @title findUserList
	 * @description findUserList
	 * @author Administrator
	 * @updateTime 2021/9/4 0004 17:26
	 */
	@GetMapping("/findUserList")
	public void findUserList(QueryRequest queryRequest, SystemUser systemUser,HttpServletResponse response) throws Exception {
		List<SystemUser> list = userService.findList(systemUser);
		Json json = new Json();
		json.setMsg("èŽ·å?–æ•°æ?®æˆ?åŠŸã€‚");
		json.setData(list);
		json.setSuccess(true);
		this.writeJson(response,json);
	}



}
