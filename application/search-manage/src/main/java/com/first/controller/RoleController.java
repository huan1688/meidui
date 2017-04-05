package com.first.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.first.page.PageView;
import com.first.page.QueryResult;
import com.first.system.domain.Role;
import com.first.system.services.IRoleService;

@Controller
@RequestMapping("/role")
public class RoleController {
	
	@Resource
	private IRoleService roleService ;
	/**
	 * 查询角色列表
	 * @return ModelAndView
	 */
	@RequestMapping("/showRoleList")
	public  ModelAndView  showRoleList(Role role){
        ModelAndView mav = new ModelAndView();
		PageView pageView  = new PageView(role.getCurrentPage());
		role.setOffset(pageView.getFirstIndex());
		QueryResult result = roleService.selectRolePage(role);
		pageView.setQueryResult(result);
		mav.addObject("pageView", pageView);
		mav.setViewName("/role/rolelist");
        return  mav;
	}
	
	/**
	 * 新增角色页面
	 * @return ModelAndView
	 */
	@RequestMapping("/toAddRolePage")
	public  ModelAndView  toAddRolePage(){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/role/addRole");
		return  mav;
	}
	
	/**
	 * 新增角色
	 */
	@ResponseBody
	@RequestMapping("/addRole")
	public  Object  addRole(Role role){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean bool =  roleService.addRole(role);
		resultMap.put("result", bool);
		return  resultMap;
	}
	
	/**
	 * 编辑角色页面
	 * @return
	 */
	@RequestMapping("/toEditRolePage")
	public ModelAndView  toEditPage(int rid){
		ModelAndView  mav = new ModelAndView();
		Role  role = roleService.selectById(rid);
		mav.addObject("role", role);
		mav.setViewName("role/editRole");
		return mav;
	}
	
	/**
	 * 角色修改
	 * @param role
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/editRole")
	public Object  editRole(Role role){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean bool = roleService.editRole(role);
		resultMap.put("result", bool);
		return resultMap;
	}

	@RequestMapping("/deleteRole")
	public  ModelAndView deleteRole(Integer rid,String status){
		ModelAndView mav = new ModelAndView();
		roleService.deleteRole(rid,status);
		mav.setViewName("redirect:/role/showRoleList.do");
		return  mav ;
	}
	
	/**
	 * 检查角色名称是否存在
	 * @return
	 */
	@RequestMapping("/checkRoleName")
	public  void checkRoleName(Role role,HttpServletResponse response){
		boolean bl =  roleService.checkRoleName(role);
		response.setCharacterEncoding("utf-8");
		PrintWriter write = null;
		try {
			write = response.getWriter();
			if(bl){
				write.write("{\"error\":\"角色名已经被占用!\"}");
			}else{
				write.write("{\"ok\":\"角色名可以使用!\"}");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(write !=null){
			 write.close();
			}
		}
		
	}
	
	/**
	 * 检查角色是否有用户
	 * @param rid
	 * @return
	 */
	@RequestMapping("/checkRoleUser")
	@ResponseBody
	public  Object  checkRoleUser(Integer rid){
		boolean bl = roleService.checkRoleUser(rid);
		return bl;
	}
}