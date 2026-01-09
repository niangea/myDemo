package com.nian.myDemoSecurity.javaStudy.threadStudy.ABA;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用 AtomicReference 演示更实际的 ABA 问题
 */
public class ComplexABAProblem {

    static class Node {
        final String value;
        Node next;

        public Node(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" + value + "}";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 创建一个栈
        AtomicReference<Node> stackTop = new AtomicReference<>();
        Node initialNode = new Node("初始节点");
        stackTop.set(initialNode);

        System.out.println("初始栈顶: " + stackTop.get());

        // 线程1：尝试出栈操作
        Thread thread1 = new Thread(() -> {
            // 记录当前栈顶
            Node currentTop = stackTop.get();
            System.out.println("线程1: 当前栈顶 = " + currentTop);

            // 模拟一些处理时间
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 尝试弹出栈顶（CAS操作）
            Node newTop = (currentTop != null) ? currentTop.next : null;
            boolean success = stackTop.compareAndSet(currentTop, newTop);

            System.out.println("线程1: 出栈操作 " + (success ? "成功" : "失败"));
            if (success) {
                System.out.println("线程1: 新栈顶 = " + (newTop != null ? newTop : "空"));
            }
        });

        // 线程2：制造ABA变化
        Thread thread2 = new Thread(() -> {
            // 第一次修改：添加新节点
            Node currentTop = stackTop.get();
            Node nodeA = new Node("节点A");
            nodeA.next = currentTop;
            stackTop.set(nodeA);
            System.out.println("线程2: 添加新节点A，栈顶 = " + nodeA);

            // 第二次修改：恢复原节点
            stackTop.set(currentTop);
            System.out.println("线程2: 恢复原节点，栈顶 = " + currentTop);

            // 第三次修改：再次修改
            Node nodeB = new Node("节点B");
            nodeB.next = currentTop;
            stackTop.set(nodeB);
            System.out.println("线程2: 添加新节点B，栈顶 = " + nodeB);

            // 第四次修改：再次恢复
            stackTop.set(currentTop);
            System.out.println("线程2: 再次恢复原节点，栈顶 = " + currentTop);
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("\n最终栈顶: " + stackTop.get());
        System.out.println("问题：栈经历了多次变化，但线程1的出栈操作仍然成功了！");
    }
}