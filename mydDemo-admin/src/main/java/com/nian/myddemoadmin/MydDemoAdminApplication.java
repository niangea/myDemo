package com.nian.myddemoadmin;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
@EnableTransactionManagement
@EnableFeignClients
@MapperScan({"com.nian.myddemoadmin.mapper","com.nian.myddemoadmin.dao"})
public class MydDemoAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MydDemoAdminApplication.class, args);
    }

}
