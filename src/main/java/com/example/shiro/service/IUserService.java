package com.example.shiro.service;

import com.example.shiro.model.po.Permission;
import com.example.shiro.model.po.Role;
import com.example.shiro.model.po.User;

import java.util.List;

/**
 * 用户操作接口
 *
 * @author wangguoqiang
 */
public interface IUserService {
    /**
     * 根据用户名查询用户信息
     *
     * @param userName 用户名
     */
    User getByUserName(String userName);

    /**
     * 根据用户ID获取用户角色信息
     *
     * @param id 用户ID
     */
    List<Role> getRolesByUserId(Long id);

    /**
     * 根据角色ID获取角色权限信息
     *
     * @param id 角色ID
     */
    List<Permission> getPermissionsByRoleId(Long id);

    /**
     * 保存用户信息
     *
     * @param user 用户信息
     */
    Long save(User user);
}
