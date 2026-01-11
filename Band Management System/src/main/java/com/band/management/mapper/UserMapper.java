package com.band.management.mapper;

import com.band.management.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 * 
 * @author Band Management Team
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("userId") Long userId);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("userId") Long userId);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户名和角色查询用户
     */
    User selectByUsernameAndRole(@Param("username") String username, @Param("role") String role);

    /**
     * 根据角色和关联ID查询用户
     */
    User selectByRoleAndRelatedId(@Param("role") String role, @Param("relatedId") Long relatedId);

    /**
     * 根据角色和关联ID删除用户
     */
    int deleteByRoleAndRelatedId(@Param("role") String role, @Param("relatedId") Long relatedId);
}
