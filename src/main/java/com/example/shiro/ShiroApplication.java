package com.example.shiro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 项目启动类
 *
 * @author wangguoqiang
 */
@SpringBootApplication
@MapperScan("com.example.shiro.dao")
public class ShiroApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiroApplication.class, args);
    }

}
