package com.nian.myDemoSecurity.javaStudy.threadStudy.ABA;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 使用 AtomicStampedReference 解决 ABA 问题
 */
public class AtomicStampedReferenceSolution {

    public static void main(String[] args) throws InterruptedException {
        // 初始值：100，初始版本：1
        AtomicStampedReference<Integer> atomicStampedRef =
                new AtomicStampedReference<>(100, 1);

        System.out.println("========== 使用 AtomicStampedReference 解决 ABA ==========");
        System.out.println("初始值: " + atomicStampedRef.getReference());
        System.out.println("初始版本: " + atomicStampedRef.getStamp());

        // 线程1
        Thread thread1 = new Thread(() -> {
            // 获取当前值和版本
            int[] stampHolder = new int[1];
            int value = atomicStampedRef.get(stampHolder);
            int oldStamp = stampHolder[0];

            System.out.println("线程1: 读取到值 = " + value + ", 版本 = " + oldStamp);

            // 休眠，让线程2有机会操作
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 尝试CAS操作
            boolean success = atomicStampedRef.compareAndSet(
                    value,           // 期望值
                    200,             // 新值
                    oldStamp,        // 期望版本
                    oldStamp + 1     // 新版本
            );

            System.out.println("线程1: CAS(100→200) 结果 = " + success);
            System.out.println("线程1: 当前值 = " + atomicStampedRef.getReference() +
                    ", 版本 = " + atomicStampedRef.getStamp());
        });

        // 线程2
        Thread thread2 = new Thread(() -> {
            // 第一次修改
            boolean success1 = atomicStampedRef.compareAndSet(
                    100, 150,
                    atomicStampedRef.getStamp(),
                    atomicStampedRef.getStamp() + 1
            );
            System.out.println("线程2: CAS(100→150) 结果 = " + success1);
            System.out.println("线程2: 当前值 = " + atomicStampedRef.getReference() +
                    ", 版本 = " + atomicStampedRef.getStamp());

            // 第二次修改
            boolean success2 = atomicStampedRef.compareAndSet(
                    150, 100,
                    atomicStampedRef.getStamp(),
                    atomicStampedRef.getStamp() + 1
            );
            System.out.println("线程2: CAS(150→100) 结果 = " + success2);
            System.out.println("线程2: 当前值 = " + atomicStampedRef.getReference() +
                    ", 版本 = " + atomicStampedRef.getStamp());
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("\n最终值: " + atomicStampedRef.getReference());
        System.out.println("最终版本: " + atomicStampedRef.getStamp());
        System.out.println("✓ 线程1的CAS操作失败了，因为版本号已经变化！");
    }
}