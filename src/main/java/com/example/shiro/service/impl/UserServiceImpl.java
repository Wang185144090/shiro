package com.example.shiro.service.impl;

import com.example.shiro.dao.*;
import com.example.shiro.model.po.*;
import com.example.shiro.service.IUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户操作实现
 *
 * @author wangguoqiang
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public User getByUserName(String userName) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andUserNameEqualTo(userName);
        List<User> userList = userMapper.selectByExample(userExample);
        if (userList == null || userList.size() < 1) {
            return null;
        }
        return userList.get(0);
    }

    @Override
    public List<Role> getRolesByUserId(Long id) {
        UserRoleExample userRoleExample = new UserRoleExample();
        UserRoleExample.Criteria criteria = userRoleExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);
        RoleExample roleExample = new RoleExample();
        roleExample.setOrderByClause("id asc");
        RoleExample.Criteria roleCriteria = roleExample.createCriteria();
        roleCriteria.andIdIn(userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        return roleMapper.selectByExample(roleExample);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long id) {
        RolePermissionExample rolePermissionExample = new RolePermissionExample();
        RolePermissionExample.Criteria criteria = rolePermissionExample.createCriteria();
        criteria.andRoleIdEqualTo(id);
        List<RolePermission> rolePermissionList = rolePermissionMapper.selectByExample(rolePermissionExample);
        PermissionExample permissionExample = new PermissionExample();
        permissionExample.setOrderByClause("id asc");
        PermissionExample.Criteria permissionCriteria = permissionExample.createCriteria();
        permissionCriteria.andIdIn(rolePermissionList.stream().map(RolePermission::getPermissionId).collect(Collectors.toList()));
        return permissionMapper.selectByExample(permissionExample);
    }

    @Override
    public Long save(User user) {
        if (userMapper.insert(user) > 0) {
            return user.getId();
        }
        return null;
    }

}
