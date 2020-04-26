package com.example.shiro.shiro.filter;

import com.alibaba.fastjson.JSONObject;
import com.example.base.ResultEntity;
import com.example.shiro.common.ShiroCodeMessage;
import com.example.shiro.common.ShiroConstant;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Deque;
import java.util.Set;

/**
 * 登录拦截器
 * 实现授权过滤器
 *
 * @author wangguoqiang
 */
@AutoConfigureAfter
public class LoginFilter extends AuthenticationFilter {
    /**
     * session管理中心
     */
    @Autowired
    private SessionManager sessionManager;
    /**
     * 用户session缓存
     */
    @Autowired
    private CacheManager cacheManager;
    /**
     * 用户session缓存队列
     */
    private Cache<String, Deque<Serializable>> cache;

    /**
     * 登录处理
     *
     * @param servletRequest  http请求
     * @param servletResponse http响应
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        System.out.println("当前请求地址为-->" + path);
        if (cache == null) {
            cache = cacheManager.getCache(ShiroConstant.CACHE_PREFIX);
        }
        Subject subject = getSubject(servletRequest, servletResponse);
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            //登录失效返回
            returnMsg(response, ShiroCodeMessage.LOGIN_EXPIRE);
            return false;
        }
        //此用户限制登录
        if (new ShiroSessionFilter().check(subject, sessionManager, cache)) {
            //登出当前帐号
            subject.logout();
            //保存请求信息到shiro
            saveRequest(servletRequest);
            returnMsg(response, ShiroCodeMessage.LOGIN_KICKOUT);
            return false;
        }
        return true;
    }

    /**
     * 不需要认证
     * 以下注释代码为认证权限的配置，未测试
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) {
//        Subject subject = getSubject(servletRequest, servletResponse);
//        String[] rolesArray = (String[]) o;
//​
//        if (rolesArray == null || rolesArray.length == 0) {
//            return true;
//        }
//​
//        Set<String> roles = CollectionUtils.asSet(rolesArray);
//        for (String role : roles) {
//            if (subject.hasRole(role)) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * 检查是否拦截当前路径
     *
     * @param pattern shiro（ShiroConfig）中配置的路径过滤规则
     * @param path    当前路径
     */
    @Override
    protected boolean pathsMatch(String pattern, String path) {
        return !PathFilter.check(path);
    }

    /**
     * 拼装拦截用户返回信息
     *
     * @param response http响应
     */
    private void returnMsg(HttpServletResponse response, ShiroCodeMessage shiroCodeMessage) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.append(JSONObject.toJSONString(ResultEntity.error(shiroCodeMessage)));
        printWriter.flush();
        printWriter.close();
    }
}
