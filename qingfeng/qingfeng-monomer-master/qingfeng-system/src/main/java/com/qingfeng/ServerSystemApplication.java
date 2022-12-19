package com.qingfeng;

import com.qingfeng.annotation.EnableApplication;
import com.qingfeng.annotation.EnableMyAuthExceptionHandler;
import com.qingfeng.annotation.EnableMyLettuceRedis;
import com.qingfeng.annotation.EnableMyServerProtect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**  
* @title: ServerSystemApplication
* @projectName: ServerSystemApplication
* @description: TODO
* @author: qingfeng
* @date: 2021/2/23 0023 21:58
*/
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@SpringBootApplication
////拦截器、过滤器等
//@ServletComponentScan
////定时器
//@EnableScheduling
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableApplication
@EnableMyLettuceRedis
@EnableTransactionManagement
@MapperScan("com.qingfeng.*.mapper")
public class ServerSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerSystemApplication.class, args);
    }
}