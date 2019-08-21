package util;

import java.util.concurrent.Exchanger;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ldh on 2019/4/1.
 */
public class ExchangeTest {

    public static void main(String[] args) {
        testExchanger();
    }

    public static void testExchanger() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Exchanger<Integer> exchanger = new Exchanger();
        executor.execute(()->{
            for (int i=0; i<100; i+=2) {
                try {
                    int r = exchanger.exchange(i);
                    System.out.println("thread1: " + i + ", r:" + r);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.execute(()->{
            for (int i=1; i<100; i+=2) {
                try {
                    int r = exchanger.exchange(10);
//                    Thread.sleep(1);
                    System.out.println("thread2: " + i + ", r:" + r);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    public void commonTest() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ExchangeTest exchangeTest = new ExchangeTest();
        executor.execute(()->{
            for (int i=0; i<100; i+=2) {
                try {
                    exchangeTest.println("thread1", i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.execute(()->{
            for (int i=1; i<100; i+=2) {
                try {
                    exchangeTest.println2("thread2", i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    private boolean printlnThread = true;
    public synchronized void println(String threadName, int n) throws InterruptedException {
        if (!printlnThread) {
            wait();
        }
        System.out.println(threadName + ":" + n);
        printlnThread = false;
        notifyAll();
    }

    public synchronized void println2(String threadName, int n) throws InterruptedException {
        if (printlnThread) {
            wait();
        }
        System.out.println(threadName + ":" + n);
        printlnThread = true;
        notifyAll();
    }
}
