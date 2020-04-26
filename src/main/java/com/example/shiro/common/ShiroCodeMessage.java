package com.example.shiro.common;

import com.example.base.CodeMessage;

/**
 * 项目异常码
 *
 * @author wangguoqiang
 */
public class ShiroCodeMessage extends CodeMessage {

    public static final ShiroCodeMessage LOGIN_EXPIRE = new ShiroCodeMessage("shiro_10001", "登录已失效");

    public static final ShiroCodeMessage LOGIN_KICKOUT = new ShiroCodeMessage("shiro_10002", "您的账号已在别处登录，请修改密码或重新登录");

    public static final ShiroCodeMessage UNKNOWN_ACCOUNT = new ShiroCodeMessage("shiro_10003", "用户不存在");

    public static final ShiroCodeMessage PASSWORD_ERROR = new ShiroCodeMessage("shiro_10004", "用户密码错误");

    public static final ShiroCodeMessage PARAM_ERROR = new ShiroCodeMessage("shiro_10005", "参数错误");

    public ShiroCodeMessage(String serviceType, String serviceName) {
        super(serviceType, serviceName);
    }
}
