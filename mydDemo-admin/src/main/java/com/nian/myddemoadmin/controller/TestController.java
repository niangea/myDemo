package com.nian.myddemoadmin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test")
    public String testLog() {
        logger.info("这是一条测试日志信息 - 用户访问了/test端点");
        // 可以记录更复杂的对象信息
        // logger.debug("用户信息: {}", userObject);
        return "日志已记录，请查看Kibana！";
    }
}