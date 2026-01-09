package com.nian.myDemoSecurity.javaStudy.threadStudy.AQS;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义互斥锁 - 基于AQS实现
 * 功能：不可重入的独占锁
 */
public class Mutex implements Lock, Serializable {

    // 自定义同步器
    private static class Sync extends AbstractQueuedSynchronizer {

        // 是否被当前线程独占
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        // 尝试获取锁
        @Override
        protected boolean tryAcquire(int acquires) {
            assert acquires == 1; // 这里只能是1

            // CAS操作：将state从0改为1
            if (compareAndSetState(0, 1)) {
                // 设置当前线程为独占线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 尝试释放锁
        @Override
        protected boolean tryRelease(int releases) {
            assert releases == 1;

            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }

            // 清空独占线程
            setExclusiveOwnerThread(null);
            // 注意：这里必须先清空线程，再设置state
            setState(0);
            return true;
        }

        // 创建条件变量
        Condition newCondition() {
            return new ConditionObject();
        }
    }

    private final Sync sync = new Sync();

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    // 工具方法
    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    // 测试
    public static void main(String[] args) throws InterruptedException {
        Mutex mutex = new Mutex();
        int[] counter = {0};

        // 创建10个线程竞争锁
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    mutex.lock();
                    try {
                        counter[0]++;
                    } finally {
                        mutex.unlock();
                    }
                }
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("最终计数: " + counter[0]); // 应该是 10000
        System.out.println("是否有等待线程: " + mutex.hasQueuedThreads());
    }
}