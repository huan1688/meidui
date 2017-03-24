/*
 *  @项目名称: ${project_name}
 *
 *  @文件名称: ${file_name}
 *  @Date: ${date}
 *  @Copyright: ${year} www.meiduimall.com Inc. All rights reserved.
 *
 *  注意：本内容仅限于美兑壹购物公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.meiduimall.exception;



public class ApiException extends RuntimeException {

	private static final long serialVersionUID = -1972925542520532318L;
	
	public ApiException(String e) {
		super(e);
	}

	public ApiException(Exception e) {
		super(e);
	}
	

	public ApiException(String message, Throwable cause) {
		super(message, cause);
	}

}