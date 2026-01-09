import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义二元信号量
 * 功能：许可证数量为1的信号量
 */
public class BinarySemaphore implements Lock {

    private static class Sync extends AbstractQueuedSynchronizer {

        Sync(int permits) {
            if (permits <= 0) {
                throw new IllegalArgumentException("permits must be positive");
            }
            setState(permits);
        }

        // 获取当前许可证数量
        public int getPermits() {
            return getState();  // 在子类中可以访问 protected 的 getState()
        }

        // 尝试获取共享锁
        @Override
        protected int tryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;

                if (remaining < 0 || compareAndSetState(available, remaining)) {
                    return remaining;
                }
            }
        }

        // 尝试释放共享锁
        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;

                if (next < current) { // 溢出检查
                    throw new Error("Maximum permit count exceeded");
                }

                if (compareAndSetState(current, next)) {
                    return true;
                }
            }
        }

        // 是否被独占
        @Override
        protected boolean isHeldExclusively() {
            return false; // 共享模式
        }

        // 创建条件变量
        Condition newCondition() {
            return new ConditionObject();
        }
    }

    private final Sync sync;

    public BinarySemaphore(int permits) {
        this.sync = new Sync(permits);
    }

    @Override
    public void lock() {
        sync.acquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquireShared(1) >= 0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.releaseShared(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    // 获取可用许可证数量
    public int getPermits() {
        return sync.getPermits();  // 通过 Sync 的公共方法获取
    }

    // 测试
    public static void main(String[] args) throws InterruptedException {
        // 创建许可证为2的信号量
        BinarySemaphore semaphore = new BinarySemaphore(2);

        System.out.println("初始许可证数量: " + semaphore.getPermits());

        // 线程1获取许可证
        new Thread(() -> {
            semaphore.lock();
            System.out.println("线程1获取许可证，剩余: " + semaphore.getPermits());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            semaphore.unlock();
            System.out.println("线程1释放许可证，剩余: " + semaphore.getPermits());
        }).start();

        // 线程2获取许可证
        new Thread(() -> {
            semaphore.lock();
            System.out.println("线程2获取许可证，剩余: " + semaphore.getPermits());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            semaphore.unlock();
            System.out.println("线程2释放许可证，剩余: " + semaphore.getPermits());
        }).start();

        // 线程3尝试获取许可证
        new Thread(() -> {
            boolean acquired = semaphore.tryLock();
            if (acquired) {
                System.out.println("线程3获取许可证成功");
                semaphore.unlock();
            } else {
                System.out.println("线程3获取许可证失败");
            }
        }).start();

        Thread.sleep(3000);
        System.out.println("最终许可证数量: " + semaphore.getPermits());
    }
}