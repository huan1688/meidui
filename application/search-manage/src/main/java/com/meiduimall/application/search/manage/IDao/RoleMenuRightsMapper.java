package com.meiduimall.application.search.manage.IDao;
import com.meiduimall.application.search.manage.domain.RoleMenuRightsKey;


public interface RoleMenuRightsMapper {
    int deleteByPrimaryKey(RoleMenuRightsKey key);

    int insert(RoleMenuRightsKey record);

    int insertSelective(RoleMenuRightsKey record);
}