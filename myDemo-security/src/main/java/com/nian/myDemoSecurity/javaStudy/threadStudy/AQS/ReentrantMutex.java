package com.nian.myDemoSecurity.javaStudy.threadStudy.AQS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义可重入锁
 * 功能：支持重入的独占锁
 */
public class ReentrantMutex implements Lock {

    private static class Sync extends AbstractQueuedSynchronizer {

        // 获取当前持有锁的线程
        private Thread getOwner() {
            return getExclusiveOwnerThread();
        }

        // 获取重入次数
        private int getHoldCount() {
            return getState();
        }

        @Override
        protected boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        // 尝试获取锁
        @Override
        protected boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();

            if (c == 0) {
                // 锁未被持有，尝试获取
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 当前线程已经持有锁，重入
                int nextc = c + acquires;
                if (nextc < 0) { // 溢出检查
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }

            return false;
        }

        // 尝试释放锁
        @Override
        protected boolean tryRelease(int releases) {
            int c = getState() - releases;

            if (Thread.currentThread() != getExclusiveOwnerThread()) {
                throw new IllegalMonitorStateException();
            }

            boolean free = false;
            if (c == 0) {
                // 完全释放锁
                free = true;
                setExclusiveOwnerThread(null);
            }

            setState(c);
            return free;
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

    // 扩展方法
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    public int getHoldCount() {
        return sync.getHoldCount();
    }

    public Thread getOwner() {
        return sync.getOwner();
    }

    // 测试可重入
    public static void main(String[] args) {
        ReentrantMutex lock = new ReentrantMutex();

        new Thread(() -> {
            lock.lock();
            System.out.println("第一次获取锁，重入次数: " + lock.getHoldCount());

            lock.lock();
            System.out.println("第二次获取锁（重入），重入次数: " + lock.getHoldCount());

            lock.unlock();
            System.out.println("第一次释放锁，重入次数: " + lock.getHoldCount());

            lock.unlock();
            System.out.println("第二次释放锁，重入次数: " + lock.getHoldCount());
        }).start();
    }
}