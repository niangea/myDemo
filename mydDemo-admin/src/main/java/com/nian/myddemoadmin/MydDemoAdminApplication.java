package com.nian.myddemoadmin;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
@MapperScan({"com.nian.myddemoadmin.mapper","com.nian.myddemoadmin.dao"})
public class MydDemoAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MydDemoAdminApplication.class, args);
    }

}
