package com.nian.myDemoSecurity.javaStudy.threadStudy.ABA;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ABA 问题复现示例
 */
public class ABAProblemDemo {

    // 简单的引用对象
    static class SimpleValue {
        int value;

        public SimpleValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SimpleValue{" + "value=" + value + '}';
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 使用 AtomicInteger 模拟ABA问题
        AtomicInteger atomicInt = new AtomicInteger(100);

        System.out.println("========== ABA 问题复现 ==========");
        System.out.println("初始值: " + atomicInt.get());

        // 线程1：模拟ABA场景
        Thread thread1 = new Thread(() -> {
            // 线程1读取到值为100
            int value = atomicInt.get();
            System.out.println("线程1: 读取到值 = " + value);

            // 线程1休眠2秒，让线程2有时间操作
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 线程1尝试将100改为200
            boolean success = atomicInt.compareAndSet(value, 200);
            System.out.println("线程1: CAS(100→200) 结果 = " + success);
            System.out.println("线程1: 当前值 = " + atomicInt.get());
        });

        // 线程2：制造ABA变化
        Thread thread2 = new Thread(() -> {
            // 线程2立即将100改为150
            System.out.println("线程2: CAS(100→150) 结果 = " +
                    atomicInt.compareAndSet(100, 150));
            System.out.println("线程2: 当前值 = " + atomicInt.get());

            // 线程2再将150改回100
            System.out.println("线程2: CAS(150→100) 结果 = " +
                    atomicInt.compareAndSet(150, 100));
            System.out.println("线程2: 当前值 = " + atomicInt.get());

            System.out.println("线程2: 完成 A→B→A 的变化");
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("\n最终值: " + atomicInt.get());
        System.out.println("问题：虽然值从 100→150→100 变化了，但线程1的CAS仍然成功！");
    }
}