package com.meiduimall.api;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import com.meiduimall.constant.HttpReqConst;
import com.meiduimall.constant.ApiRespConst;
import com.meiduimall.interceptor.ValInterceptor;
import com.meiduimall.oauth.OauthValidate;
import com.meiduimall.util.HttpClientUtil;
import com.meiduimall.util.Logger;
import com.meiduimall.util.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户基本操作，包括登录、注册，校验token、登出等
 * @author chencong
 *
 */
@SuppressWarnings({ "rawtypes", "static-access", "unchecked" })
@RestController
@RequestMapping("/member/front_user_center")
public class MembersBasicOpController {
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private HttpServletResponse request;
	
	
	/**旧会员系统获取token或创建token(临时接口)
	 * 因为APP可能没升级，还会请求旧会员系统的登录和注册，所以这两个接口的put token要走这个接口
	 * 其他的接口get token也是走这个接口，两种操作用type区分，1是get，2是put
	 * */
	@RequestMapping(value = "v1/baseop/getput",method=RequestMethod.GET)
	String getput() { 
		response.setCharacterEncoding("utf-8");
		String result=null;
		try {
			
			/**从本地变量获取已解析过的json**/
			JSONObject j=OauthValidate.postjson.get();
			Logger.info("接收登录请求："+j.toString());
			
			/**转发请求到服务层**/
			Map  map=j;
			String url=SystemConfig.getInstance().configMap.get("GetPut_Url_Dev");
			Logger.info("转发登录请求的URL："+url);
			result=HttpClientUtil.doGet(url,map,null);
			Logger.info("用户"+j.getString("user_id")+"请求结果"+result);
		} catch (Exception e) {
			result=ApiRespConst.ERROR_MSG;
		}
		return result;
	    }
	
	/**
	 * 旧会员系统登出接口调用新会员系统的接口
	 * */
	
	@RequestMapping(value = "v1/baseop/handlesignout",method=RequestMethod.GET)
	String handlesignout() { 
		String result=null;
		try {
			response.setCharacterEncoding("utf-8");
			/**从本地变量获取已解析过的json**/
			JSONObject j=OauthValidate.postjson.get();
			Logger.info("接收登录请求："+j.toString());
			
			/**转发请求到服务层**/
			Map  map=j;
			String url=SystemConfig.getInstance().configMap.get("HandleSignOut_Url_Dev");
			Logger.info("转发登录请求的URL："+url);
			result=HttpClientUtil.doGet(url,map,null);
			Logger.info("用户"+j.getString("user_id")+"请求结果"+result);
		} catch (Exception e) {
			result=ApiRespConst.ERROR_MSG;
		}
		return result;
	    }
	
	/**登录*/
	@RequestMapping(value = "v1/baseop/login",produces="text/html;charset=utf-8")
	String login(){ 
		String result=null;
		try {
			/**从本地变量获取已解析过的json**/
			JSONObject requestj=OauthValidate.postjson.get();
			JSONObject check_result=ValInterceptor.check_result.get();

			//**如果拦截器校验失败，直接返回**//
			if(!"0".equals(check_result.getString(ApiRespConst.STATUS_CODE)))
				return check_result.toString();
			Logger.info("接收登录请求："+requestj.toString());
			

			/**转发请求到服务层**/
			String url=SystemConfig.getInstance().configMap.get("Login_Url_Dev");
			Logger.info("转发登录请求的URL："+url);
			//头部

			Map<String,String> heads=new HashMap<>();
			heads.put(HttpReqConst.CONTENT_TYPE,HttpReqConst.MEDIATYPE_JSON_FOR_APP);
			result=HttpClientUtil.doPost(url, requestj.toString(),heads);
			Logger.info("用户"+requestj.getString("user_id")+"请求结果"+result);
		} catch (Exception e) {
			e.printStackTrace();
			result=ApiRespConst.ERROR_MSG;
		}
		return result;
	    }
	
/*	
	*//**用户注册*//*
	@RequestMapping(value = "/register",method=RequestMethod.POST)
	void register() { 
		//得到IP地址
		String ip=HttpClientUtil.getIpAddr(request);
		PrintWriter pw=null;
		try {
			pw=response.getWriter();
			//从本地变量获取已解析过的json
			JSONObject j=OauthValidate.postjson.get();
			Logger.info("接收IP为："+ip+"的注册请求："+j.toString());
			校验参数
			JSONObject check=AuthorizationCheckParameter.vilidetePar(j);
			if((check.keySet().size()>0)&&check.getString(AuthorizationCheckParameter.RESULTKEYNAME).equals("false"))
			{
				check.remove(AuthorizationCheckParameter.RESULTKEYNAME);
				pw.print(check.toString());
				return;
			}
			//处理注册请求
			j.put("ip",ip);
			Map<String, Object> map=mbo.register(j);
			String result=JSON.toJSONString(map);
			pw.print(result);
		} catch (Exception e) {
			pw.println(getServerError(e));
		}
	    }
	
	
	*//**token校验*//*
	@HasToken
	@RequestMapping(value = "/checktoken",method=RequestMethod.POST)
	void checktoken() {	
		PrintWriter pw=null;
		try {
			pw=response.getWriter();
			//到拦截器那里从本地变量获取解析过的json
			JSONObject j=OauthValidate.postjson.get();
			//处理打开APP时检查token的逻辑
			Map<String, Object> map=mbo.checktoken(j);
			String result=JSON.toJSONString(map);
			Logger.info("用户"+j.getString("user_id")+"请求结果"+result);
			pw.print(result);
		} catch (Exception e) {
			pw.println(getServerError(e));
		}
	
}
	
	*//**生成短信验证码*//*
	@RequestMapping(value = "/createvalidatecode",method=RequestMethod.POST)
	void createValidateCode() { 
		PrintWriter pw=null;
		try {
			pw=response.getWriter();
			//从本地变量获取已解析过的json
			JSONObject j=OauthValidate.postjson.get();
			//得到IP地址
			String ip=HttpClientUtil.getIpAddr(request);
			j.put("ip",ip);
			Logger.info("接收IP为："+ip+"的获取短信验证码请求："+j.toString());
			校验参数
			JSONObject check=AuthorizationCheckParameter.vilidetePar(j);
			if((check.keySet().size()>0)&&check.getString(AuthorizationCheckParameter.RESULTKEYNAME).equals("false"))
			{
				check.remove(AuthorizationCheckParameter.RESULTKEYNAME);
				pw.print(check.toString());
				return;
			}
			//处理注册请求
			Map<String, Object> map=mbo.createValidateCode(j);
			String result=JSON.toJSONString(map);
			pw.print(result);
		} catch (Exception e) {
			pw.println(getServerError(e));
		}
	    }
	
	*/

}
