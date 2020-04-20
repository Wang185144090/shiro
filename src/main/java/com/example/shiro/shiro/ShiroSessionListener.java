package com.example.shiro.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * session会话监听
 *
 * @author wangguoqiang
 */
public class ShiroSessionListener implements SessionListener {

    /**
     * 切换为redis计数
     * 统计有效session个数
     */
    AtomicInteger sessionCount = new AtomicInteger(0);

    /**
     * 启动 计数+1
     *
     * @param session
     */
    @Override
    public void onStart(Session session) {
        sessionCount.incrementAndGet();
    }

    /**
     * 停止 计数-1
     *
     * @param session
     */
    @Override
    public void onStop(Session session) {
        sessionCount.decrementAndGet();
    }

    /**
     * 过期  计数-1
     *
     * @param session
     */
    @Override
    public void onExpiration(Session session) {
        sessionCount.decrementAndGet();
    }
}
