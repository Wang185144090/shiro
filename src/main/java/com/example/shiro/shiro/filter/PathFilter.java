package com.example.shiro.shiro.filter;

import com.example.shiro.common.ShiroConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求路径过滤器
 *
 * @author wangguoqiang
 */
public class PathFilter {

    /**
     * 访问地址白名单
     */
    private static List<String> WHITE_LIST = new ArrayList<>();

    /**
     * 静态资源
     */
    private static final String LIB = "/lib";
    private static final String FAVICON = "/favicon.ico";
    private static final String SWAGGER = "swagger";

    /**
     * 路径匹配规则
     *
     * @param path 当前请求路径(没有域名和端口号)
     * @return true表示路径匹配，false表示路径不匹配
     */
    public static boolean check(String path) {
        if (path.startsWith(LIB) || path.startsWith(FAVICON) || path.contains(SWAGGER)) {
            return true;
        }
        if (WHITE_LIST.size() < 1) {
            initWhiteList();
        }
        return WHITE_LIST.contains(path);
    }

    /**
     * 初始化白名单
     */
    private static void initWhiteList() {
        addApi(WHITE_LIST);
    }

    /**
     * 配置不需要登录访问的接口地址
     */
    private static void addApi(List<String> list) {
        list.add("/v2/api-docs");
        list.add("/addUser");
        list.add(ShiroConstant.LOGIN_INTERFACE_URL);
        list.add("/logout");
    }
}
