package com.example.shiro.shiro.filter;

import com.alibaba.fastjson.JSONObject;
import com.example.base.ResultEntity;
import com.example.shiro.common.ShiroCodeMessage;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登录拦截器
 * 实现授权过滤器
 *
 * @author wangguoqiang
 */
public class LoginFilter extends AuthenticationFilter {

    /**
     * 登录处理
     *
     * @param servletRequest  http请求
     * @param servletResponse http响应
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);
        System.out.println("当前请求地址为-->" + ((HttpServletRequest) servletRequest).getRequestURI());
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            //登录失效返回
            returnMsg(response, ShiroCodeMessage.LOGIN_EXPIRE);
            return false;
        }
        //此用户限制登录
        if (new ShiroSessionFilter().check(subject)) {
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
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) {
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
     * 用户登录已失效请求返回数据
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
