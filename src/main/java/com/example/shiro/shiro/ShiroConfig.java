package com.example.shiro.shiro;

import com.example.shiro.common.PublicConstant;
import com.example.shiro.common.ShiroConstant;
import com.example.shiro.model.po.User;
import com.example.shiro.redis.JedisConfig;
import com.example.shiro.shiro.filter.LoginFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro核心配置类
 *
 * @author wangguoqiang
 */
@Configuration
public class ShiroConfig {

    /**
     * shiro总配置入口
     *
     * @param securityManager 安全管理中心
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //设置shiro的核心安全接口
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //设置登录接口地址(也可以设置为页面地址)，如果不配置，则会自动寻找根目录下的/login.jsp页面
        shiroFilterFactoryBean.setLoginUrl(ShiroConstant.LOGIN_INTERFACE_URL);
        //设置登录成功跳转接口地址（也可以设置为页面地址），可以不配置
//        shiroFilterFactoryBean.setSuccessUrl("");
        //配置自定义拦截器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        //登录授权拦截,从上向下顺序执行，一般将 /**放在最为下边
        filterMap.put("loginAuthFilter", loginFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        //除了登录授权拦截处理器之外的接口，都需要登录
        Map<String, String> filterChanDefinitionMap = new LinkedHashMap<>();
        //配置登录拦截器
        filterChanDefinitionMap.put("/**", "loginAuthFilter");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChanDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 登录拦截器
     */
    @Bean("loginFilter")
    public LoginFilter loginFilter() {
        return new LoginFilter();
    }

    /**
     * 安全管理中心
     */
    @Bean("securityManager")
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //配置记住我（只需登录一次）
        securityManager.setRememberMeManager(rememberMeManager());
        //配置缓存管理器
        securityManager.setCacheManager(cacheManager());
        //配置自定义session管理
        securityManager.setSessionManager(sessionManager());
        //设置自定义realm（身份认证/登录、授权），这个放到最后，防止shiro登录不获取授权信息
        securityManager.setRealm(shiroRealm());
        return securityManager;
    }

    /**
     * shiro自定义realm
     */
    @Bean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        //开启shiro验证缓存
        shiroRealm.setCachingEnabled(true);
        //启用身份验证缓存，默认不缓存(此处开启身份认证缓存，导致密码加密登录失败，具体原因不详)
        shiroRealm.setAuthenticationCachingEnabled(false);
        //设置身份验证缓存名称
        shiroRealm.setAuthenticationCacheName(ShiroConstant.AUTHENTICATION_CACHE_NAME);
        //启用授权信息缓存，默认不缓存
        shiroRealm.setAuthorizationCachingEnabled(true);
        //设置授权信息缓存名称
        shiroRealm.setAuthorizationCacheName(ShiroConstant.AUTHORIZATION_CACHE_NAME);
        //配置自定义密码比较器
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * 凭证匹配器
     *
     * @return 返回凭证匹配器
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hcm = new HashedCredentialsMatcher();
        hcm.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
        hcm.setStoredCredentialsHexEncoded(true);
        return hcm;
    }

    /**
     * 记住我管理器
     */
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        //设置自定义cookie
        cookieRememberMeManager.setCookie(customCookie(ShiroConstant.REMEMBER_ME_COOKIE_VIEW_NAME, ShiroConstant.REMEMBER_ME_COOKIE_PATH, ShiroConstant.REMEMBER_ME_COOKIE_MAX_AGE));
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法
        cookieRememberMeManager.setCipherKey(Base64.decode(ShiroConstant.REMEMBER_ME_COOKIE_KEY));
        return cookieRememberMeManager;
    }

    /**
     * 设置自定义cookie
     */
    private SimpleCookie customCookie(String cookieViewName, String cookiePath, int cookieMaxAge) {
        //设置页面checkbox的name值
        SimpleCookie simpleCookie = new SimpleCookie(cookieViewName);
        //增加对xss防护的安全系数（开启之后只能通过http访问，JavaScript无法访问）
        simpleCookie.setHttpOnly(true);
        //设置cookie根路径
        simpleCookie.setPath(cookiePath);
        //设置cookie有效时间（单位：秒）-1表示浏览器关闭时失效此cookie
        simpleCookie.setMaxAge(cookieMaxAge);
        return simpleCookie;
    }

    /**
     * 缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        //设置shiro缓存名称
        redisCacheManager.setKeyPrefix(ShiroConstant.CACHE_PREFIX);
        //设置redis作为缓存
        redisCacheManager.setRedisManager(redisManager());
        //设置用户权限信息缓存有效时间
        redisCacheManager.setExpire(ShiroConstant.CACHE_EXPIRE);
        return redisCacheManager;
    }

    /**
     * redis管理中心
     */
    @Bean
    public RedisManager redisManager() {
        //配置redis连接信息(详细解释查看项目配置文件)
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(redisConfig().getHost() + PublicConstant.KEY_VALUE_SEPARATOR + redisConfig().getPort());
        redisManager.setPassword(redisConfig().getPassword());
        redisManager.setDatabase(redisConfig().getDatabase());
        redisManager.setTimeout(redisConfig().getTimeout());
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisConfig().getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(redisConfig().getMaxWaitMillis());
        jedisPoolConfig.setMaxTotal(redisConfig().getMaxActive());
        jedisPoolConfig.setMinIdle(redisConfig().getMinIdle());
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisConfig().getHost(), redisConfig().getPort(), redisConfig().getTimeout(), redisConfig().getPassword());
        redisManager.setJedisPool(jedisPool);
        redisManager.setJedisPoolConfig(jedisPoolConfig);
        return redisManager;
    }

    @Bean
    public JedisConfig redisConfig() {
        return new JedisConfig();
    }

    /**
     * 会话管理中心
     */
    @Bean("sessionManager")
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        Collection<SessionListener> listeners = new ArrayList<>();
        //配置session监听器
        listeners.add(sessionListener());
        sessionManager.setSessionListeners(listeners);
        //配置会话ID cookie
        sessionManager.setSessionIdCookie(customCookie(ShiroConstant.SESSION_ID_COOKIE_VIEW_NAME, ShiroConstant.SESSION_ID_COOKIE_PATH, ShiroConstant.SESSION_ID_COOKIE_MAX_AGE));
        //配置sessionDao
        sessionManager.setSessionDAO(sessionDao());
        //配置缓存管理
        sessionManager.setCacheManager(cacheManager());
        //全局会话超时时间(默认30分钟)单位：毫秒
        sessionManager.setGlobalSessionTimeout(ShiroConstant.GLOBAL_SESSION_TIMEOUT);
        //开启删除无效session对象，默认为true
        sessionManager.setDeleteInvalidSessions(true);
        //开启定时调度器进行检测过期session，默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //设置session失效扫描时间（清理用户关闭浏览器造成的孤立会话，默认1小时）
        sessionManager.setSessionValidationInterval(ShiroConstant.SESSION_VALIDATION_INTERVAL);
        //取消url后面携带的sessionId(默认为true)
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    /**
     * session监听器
     */
    @Bean("sessionListener")
    public ShiroSessionListener sessionListener() {
        return new ShiroSessionListener();
    }

    /**
     * session持久化组件
     */
    @Bean
    public SessionDAO sessionDao() {
        RedisSessionDAO redisSessionDao = new RedisSessionDAO();
        //使用redis持久化
        redisSessionDao.setRedisManager(redisManager());
        //session在redis中的有效时间，最好大于session的会话超时时间
        redisSessionDao.setExpire(ShiroConstant.REDIS_SESSION_TIMEOUT);
        redisSessionDao.setSessionIdGenerator(sessionIdGenerator());
        return redisSessionDao;
    }

    /**
     * 配置会话ID生成器
     */
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    /**
     * 开启shiro aop注解支持.
     * 可以在controller中的方法前加上注解
     * 如 @RequiresPermissions("userInfo:add")
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 获取session中的当前登录对象
     */
    public static User getCurrentLoginUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }
}
