package com.example.shiro.shiro;

import com.example.shiro.model.po.Permission;
import com.example.shiro.model.po.Role;
import com.example.shiro.model.po.User;
import com.example.shiro.service.IUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * shiro自定义realm
 *
 * @author wangguoqiang
 */
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    IUserService userService;

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //添加角色
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        //获取用户
        User user = (User) principalCollection.getPrimaryPrincipal();
        //获取当前用户的角色
        List<Role> roleList = userService.getRolesByUserId(user.getId());
        for (Role role : roleList) {
            simpleAuthorizationInfo.addRole(role.getRoleName());
            //获取当前角色的权限
            List<Permission> permissionList = userService.getPermissionsByRoleId(role.getId());
            //添加权限信息
            simpleAuthorizationInfo.addStringPermissions(permissionList.stream().map(Permission::getPermission).collect(Collectors.toList()));
        }
        return simpleAuthorizationInfo;
    }

    /**
     * 身份认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String userName = (String) authenticationToken.getPrincipal();
        User user = userService.getByUserName(userName);
        if (user == null) {
            throw new UnknownAccountException();
        }
        return new SimpleAuthenticationInfo(user, user.getUserPassword(), ByteSource.Util.bytes(user.getSalt()), getName());
    }
}
