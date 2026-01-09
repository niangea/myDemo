package com.nian.myDemoSecurity.javaStudy.threadStudy.ABA;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * 使用 AtomicMarkableReference 解决 ABA 问题
 */
public class AtomicMarkableReferenceSolution {

    public static void main(String[] args) throws InterruptedException {
        // 初始值：100，初始标记：false
        AtomicMarkableReference<Integer> atomicMarkableRef =
                new AtomicMarkableReference<>(100, false);

        System.out.println("========== 使用 AtomicMarkableReference 解决 ABA ==========");

        boolean[] markHolder = new boolean[1];
        int initialValue = atomicMarkableRef.get(markHolder);
        System.out.println("初始值: " + initialValue + ", 标记: " + markHolder[0]);

        // 线程1
        Thread thread1 = new Thread(() -> {
            boolean[] mark = new boolean[1];
            int value = atomicMarkableRef.get(mark);

            System.out.println("线程1: 读取到值 = " + value + ", 标记 = " + mark[0]);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean success = atomicMarkableRef.compareAndSet(
                    value, 200,
                    mark[0], !mark[0]
            );

            System.out.println("线程1: CAS 结果 = " + success);
            System.out.println("线程1: 当前值 = " + atomicMarkableRef.getReference() +
                    ", 标记 = " + atomicMarkableRef.isMarked());
        });

        // 线程2
        Thread thread2 = new Thread(() -> {
            // 修改值并改变标记
            boolean success1 = atomicMarkableRef.compareAndSet(
                    100, 150,
                    atomicMarkableRef.isMarked(), !atomicMarkableRef.isMarked()
            );
            System.out.println("线程2: 第一次修改结果 = " + success1);

            // 再次修改并改变标记
            boolean success2 = atomicMarkableRef.compareAndSet(
                    150, 100,
                    atomicMarkableRef.isMarked(), !atomicMarkableRef.isMarked()
            );
            System.out.println("线程2: 第二次修改结果 = " + success2);

            System.out.println("线程2: 最终标记 = " + atomicMarkableRef.isMarked());
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}