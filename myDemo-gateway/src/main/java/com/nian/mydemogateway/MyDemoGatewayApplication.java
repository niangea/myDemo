package com.nian.mydemogateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MyDemoGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyDemoGatewayApplication.class, args);
    }

}
