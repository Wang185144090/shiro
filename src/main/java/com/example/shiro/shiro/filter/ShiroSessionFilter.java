package com.example.shiro.shiro.filter;

import com.example.shiro.common.ShiroConstant;
import com.example.shiro.model.po.User;
import com.example.shiro.shiro.ShiroConfig;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 限制账号登录人数
 * 实现访问控制过滤器
 *
 * @author wangguoqiang
 */
@Service
public class ShiroSessionFilter {
    /**
     * session管理中心
     */
    @Resource
    private SessionManager sessionManager;
    /**
     * 用户session缓存队列
     */
    @Resource
    private CacheManager cacheManager;

    private Cache<String, Deque<Serializable>> cache;


    public Boolean check(Subject subject) {
        if (cache == null) {
            cache = cacheManager.getCache(ShiroConstant.CACHE_PREFIX);
        }
        //获取当前用户的session信息
        Session session = subject.getSession();
        //获取当前登录用户信息
        User user = ShiroConfig.getCurrentLoginUser();
        //获取用户的sessionId
        Serializable sessionId = session.getId();
        //获取用户名作为用户的缓存名称
        String userName = user.getUserName();
        //读取缓存中的session信息，没有则存入
        Deque<Serializable> deque = cache.get(userName);
        //如果此用户没有session队列，也就是还没有登录过，缓存中没有
        //就new一个空队列，不然deque对象为空，会报空指针
        if (deque == null) {
            deque = new LinkedList<>();
        }
        //当前用户是否限制登录
        Boolean userKickoutFlag = (Boolean) session.getAttribute(ShiroConstant.KICKOUT_FLAG);
        //如果队列里没有此sessionId，且用户没有被踢出；放入队列
        if (!deque.contains(sessionId) && !userKickoutFlag) {
            //将sessionId存入队列
            deque.push(sessionId);
            //将用户的sessionId队列缓存
            cache.put(userName, deque);
        }
        //如果队列里的sessionId超出最大会话数，则账号限制登录（踢出）
        while (deque.size() > ShiroConstant.MAX_SESSION) {
            //限制登录用户的sessionId
            Serializable kickoutSessionId;
            //限制后登录的
            if (ShiroConstant.KICKOUT_AFTER) {
                //LinkedList的push方法将新放入的放到最前边
                kickoutSessionId = deque.removeFirst();
            } else {
                kickoutSessionId = deque.removeLast();
            }
            //更新缓存队列
            cache.put(userName, deque);
            //获取限制登录用户的session对象
            Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
            if (kickoutSession != null) {
                //设置用户session为限制登录
                kickoutSession.setAttribute(ShiroConstant.KICKOUT_FLAG, true);
            }
        }
        return userKickoutFlag;
    }
}
