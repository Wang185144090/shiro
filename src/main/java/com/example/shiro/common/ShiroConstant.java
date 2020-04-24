package com.example.shiro.common;

/**
 * shiro常量
 *
 * @author wangguoqiang
 */
public class ShiroConstant {
    /**
     * 身份认证缓存名称
     */
    public static final String AUTHENTICATION_CACHE_NAME = "authenticationCache";

    /**
     * 授权信息缓存名称
     */
    public static final String AUTHORIZATION_CACHE_NAME = "authorizationCache";

    /**
     * rememberMe cookie加密密钥
     */
    public static final String REMEMBER_ME_COOKIE_KEY = "2d5a0d9bef454b06bf4835eff32ae2a4";

    /**
     * rememberMe cookie页面checkbox的name
     */
    public static final String REMEMBER_ME_COOKIE_VIEW_NAME = "rememberMe";

    /**
     * 设置cookie根路径
     */
    public static final String REMEMBER_ME_COOKIE_PATH = "/";

    /**
     * cookie有效时间，单位秒
     */
    public static final Integer REMEMBER_ME_COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

    /**
     * shiro缓存前缀
     */
    public static final String CACHE_PREFIX = "redis_shiro_cache";

    /**
     * 缓存有效时间，单位秒
     */
    public static final Integer CACHE_EXPIRE = 60 * 60;

    /**
     * 全局会话超时时间，单位：毫秒
     */
    public static final Integer GLOBAL_SESSION_TIMEOUT = 30 * 60 * 1000;

    /**
     * session失效的扫描时间，单位：毫秒
     */
    public static final Integer SESSION_VALIDATION_INTERVAL = 60 * 60 * 1000;

    /**
     * sessionId cookie页面checkbox的name
     */
    public static final String SESSION_ID_COOKIE_VIEW_NAME = "sessionId";

    /**
     * 设置cookie根路径
     */
    public static final String SESSION_ID_COOKIE_PATH = "/";

    /**
     * cookie有效时间，单位秒
     */
    public static final Integer SESSION_ID_COOKIE_MAX_AGE = -1;

    /**
     * redis session超时时间，单位：毫秒
     */
    public static final Integer REDIS_SESSION_TIMEOUT = 35 * 60 * 1000;

    /**
     * 登录接口地址
     */
    public static final String LOGIN_INTERFACE_URL = "/login";

    /**
     * 用户限制登录标识
     */
    public static final String KICKOUT_FLAG = "kickout";

    /**
     * false:限制之前登录的用户，true:限制之后登录的用户
     */
    public static final Boolean KICKOUT_AFTER = false;

    /**
     * 同一账号同时登录会话数
     */
    public static final Integer MAX_SESSION = 1;
}
