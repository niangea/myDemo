package com.nian.myDemoSecurity.javaStudy.threadStudy;

import java.util.concurrent.atomic.LongAdder;

public class study01 {

    // 使用静态内部类，这样可以在静态main方法中直接实例化
    static class LongAddExp implements Runnable {
        // 使用共享的LongAdder实例 - 线程安全
        private static LongAdder longAdder = new LongAdder();
        // 使用普通long变量作为对比 - 非线程安全
        private static long simpleLong = 0L;

        @Override
        public void run() {
            longAdder.increment();  // 原子操作，线程安全
            simpleLong += 1;        // 非原子操作，存在线程安全问题
        }

        // 添加获取结果的方法
        public static long getLongAdderValue() {
            return longAdder.sum();
        }

        public static long getSimpleLongValue() {
            return simpleLong;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[100000];

        // 创建并启动10个线程
        for(int i = 0; i < 100000; i++){
            threads[i] = new Thread(new LongAddExp());
            threads[i].start();
        }

        // 等待所有线程执行完毕
        for(Thread thread : threads) {
            thread.join();
        }

        // 输出结果
        System.out.println("LongAdder结果: " + LongAddExp.getLongAdderValue()); // 应该是10
        System.out.println("普通long结果: " + LongAddExp.getSimpleLongValue());  // 可能小于10，因为线程不安全
    }
}