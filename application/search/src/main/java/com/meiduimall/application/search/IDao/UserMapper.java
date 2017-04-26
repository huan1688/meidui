package com.meiduimall.application.search.IDao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.meiduimall.application.search.system.domain.User;

@Mapper
public interface UserMapper {
	/**
	 * 查询所有用户
	 * @param user
	 * @return 
	 */
	public  List<User> selectUserList(User user);

	/**
	 * 修改用户信息
	 * @param user
	 */
	public void updateUser(User user);
	
	/**
	 * 新增用户 
	 * @param user 
	 */
	public void insertUser(User user);
    
	/**
	 * 根据用户id查询用户
	 * @param userId
	 */
	public User selectByUserId(Integer userId);
	
    /**
     * 查询总记录数
     * @return
     */
	public int selectPageCount(User user);

	public List<User> validateOldPwd(Map<String,String> param);

}
