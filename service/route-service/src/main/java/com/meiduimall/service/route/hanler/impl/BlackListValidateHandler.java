/*
 *  @项目名称: ${project_name}
 *
 *  @文件名称: ${file_name}
 *  @Date: ${date}
 *  @Copyright: ${year} www.meiduimall.com Inc. All rights reserved.
 *
 *  注意：本内容仅限于美兑壹购物公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.meiduimall.service.route.hanler.impl;

import com.meiduimall.core.BaseApiCode;
import com.meiduimall.core.util.ExceptionUtils;
import com.meiduimall.core.util.JsonUtils;
import com.meiduimall.redis.util.JedisUtil;
import com.meiduimall.service.route.Constants;
import com.meiduimall.service.route.ResponsePackUtil;
import com.meiduimall.service.route.hanler.Handler;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class BlackListValidateHandler implements Handler {

  private static Logger log = LoggerFactory.getLogger(BlackListValidateHandler.class);


  /**
   * 功能描述:  验证请求url是不是在黑名单中
   * Author: 陈建宇
   * Date:   2017年2月22日 上午10:02:55
   */
  @Override
  public Boolean process(RequestContext ctx) {
    HttpServletRequest request = ctx.getRequest();

    try {

      String blackListJson = JedisUtil.getJedisInstance().execGetFromCache(Constants.BLACK_LIST_JSON);
      List<String> blackList = JsonUtils.jsonToList(blackListJson, String.class);
      if (!CollectionUtils.isEmpty(blackList) && isBlackList(request.getRequestURL().toString(), blackList)) {
        log.info("黑名单验证处理层,url:{},黑名单:{}", request.getRequestURL().toString(), blackListJson);
        ResponsePackUtil.responseWrapper(ctx, BaseApiCode.FAIL_BLACKLIST_VALIDATE);
        return false;
      }

    } catch (Exception e) {
      log.error("黑名单验证处理层异常,url:{},异常:{}", request.getRequestURL().toString(), ExceptionUtils.getFullStackTrace(e));
      ResponsePackUtil.responseWrapper(ctx, BaseApiCode.EXCEPTION_BLACKLIST);
      return false;
    }
    return true;
  }


  private Boolean isBlackList(String url, List<String> blackList) {
    for (String key : blackList) {
      if (url.contains(key)) {
        return true;
      }
    }
    return false;
  }

}
