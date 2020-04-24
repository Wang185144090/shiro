package com.example.shiro.controller;

import com.example.base.CommonUtil;
import com.example.base.ResultEntity;
import com.example.shiro.common.ShiroCodeMessage;
import com.example.shiro.model.po.User;
import com.example.shiro.service.IUserService;
import com.example.shiro.shiro.ShiroRealm;
import com.example.shiro.shiro.ShiroSessionListener;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 登录管理
 *
 * @author wangguoqiang
 */
@Api(value = "登录管理", tags = {"登录管理"})
@RestController
public class LoginController {

    @Resource
    private IUserService userService;

    @ApiOperation(value = "添加用户", notes = "JSON")
    @GetMapping("addUser")
    public ResultEntity<Long> addUser(@ApiParam(value = "用户名", required = true) @RequestParam(name = "userName") String userName,
                                      @ApiParam(value = "密码", required = true) @RequestParam(name = "password") String password) {
        String salt = CommonUtil.getUUID(CommonUtil.SHORT_UUID);
        User user = new User();
        user.setUserName(userName);
        user.setSalt(salt);
        user.setUserPassword(new SimpleHash(Sha256Hash.ALGORITHM_NAME, password, salt, 1).toHex());
        return ResultEntity.success(userService.save(user));
    }

    @ApiOperation(value = "登录", notes = "JSON")
    @GetMapping("login")
    public ResultEntity<User> login(@ApiParam(value = "用户名", required = true) @RequestParam(name = "userName") String userName,
                                    @ApiParam(value = "密码", required = true) @RequestParam(name = "password") String password) {
        //获取用户认证主体
        Subject subject = SecurityUtils.getSubject();
        //添加用户认证信息
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(userName, password);
        //当前登录用户信息
        User user;
        //验证登录，捕获相关异常，返回对应信息
        try {
            //登录
            subject.login(usernamePasswordToken);
            user = (User) subject.getPrincipal();
        } catch (UnknownAccountException e) {
            System.out.println("账号不存在，账号为:" + userName);
            return ResultEntity.error(ShiroCodeMessage.UNKNOWN_ACCOUNT);
        } catch (IncorrectCredentialsException e) {
            System.out.println("密码不正确，账号为:" + userName + ",密码为:" + password);
            return ResultEntity.error(ShiroCodeMessage.PASSWORD_ERROR);
        }
        return ResultEntity.success(user);
    }

    @ApiOperation(value = "登出", notes = "JSON")
    @GetMapping("logout")
    public ResultEntity<?> logout() {
        //获取用户认证主体
        Subject subject = SecurityUtils.getSubject();
        //主体为空说明未登录
        if (subject != null) {
            //登出
            subject.logout();
        }
        return ResultEntity.success();
    }

    @ApiOperation(value = "清空缓存", notes = "JSON")
    @GetMapping("clearCache")
    public ResultEntity<?> clearCache(@ApiParam(value = "类型（0：所有缓存，1：认证缓存，2：授权缓存）", required = true) @RequestParam(name = "type") Integer type) {
        //获取shiro认证信息
        ShiroRealm shiroRealm = new ShiroRealm();
        if (type == 0) {
            shiroRealm.clearAllCache();
        } else if (type == 1) {
            shiroRealm.clearAllCachedAuthenticationInfo();
        } else if (type == 2) {
            shiroRealm.clearAllCachedAuthorizationInfo();
        } else {
            return ResultEntity.error(ShiroCodeMessage.PARAM_ERROR);
        }
        return ResultEntity.success();
    }

    @ApiOperation(value = "获取当前系统登录人数", notes = "JSON")
    @GetMapping("getLoginNum")
    public ResultEntity<?> getLoginNum() {
        return ResultEntity.success(ShiroSessionListener.getLoginNum());
    }
}
