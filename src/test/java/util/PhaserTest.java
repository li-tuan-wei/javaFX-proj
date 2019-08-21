package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * Created by ldh on 2019/4/3.
 */
public class PhaserTest {

    public static void main(String[] args) {
        MyPhaser phaser = new MyPhaser();

        int num = 5;
        StudentTask[] studentTask = new StudentTask[num];
        for (int i = 0; i < studentTask.length; i++) {
            studentTask[i] = new StudentTask(phaser);
            phaser.register();	//注册一次表示phaser维护的线程个数
        }

        ExecutorService executorService = Executors.newFixedThreadPool(num);
        for (int i = 0; i < studentTask.length; i++) {
            Thread thread = new Thread(studentTask[i], "Student "+i);
            executorService.submit(thread);
        }

        executorService.shutdown();
        System.out.println("Phaser has finished:"+phaser.isTerminated());
    }

    public static class StudentTask implements Runnable {
        private Phaser phaser;
        public StudentTask(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+"到达考试");
            phaser.arriveAndAwaitAdvance();

            System.out.println(Thread.currentThread().getName()+"做第1题时间...");
            doExercise1();
            System.out.println(Thread.currentThread().getName()+"做第1题完成...");
            phaser.arriveAndAwaitAdvance();

            System.out.println(Thread.currentThread().getName()+"做第2题时间...");
            doExercise2();
            System.out.println(Thread.currentThread().getName()+"做第2题完成...");
            phaser.arriveAndAwaitAdvance();

            System.out.println(Thread.currentThread().getName()+"做第3题时间...");
            doExercise3();
            System.out.println(Thread.currentThread().getName()+"做第3题完成...");
            phaser.arriveAndAwaitAdvance();
        }

        private void doExercise1() {
            long duration = (long)(Math.random()*10);
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void doExercise2() {
            long duration = (long)(Math.random()*10);
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void doExercise3() {
            long duration = (long)(Math.random()*10);
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static class MyPhaser extends Phaser {

        @Override
        protected boolean onAdvance(int phase, int registeredParties) {    //在每个阶段执行完成后回调的方法

            switch (phase) {
                case 0:
                    return studentArrived();
                case 1:
                    return finishFirstExercise();
                case 2:
                    return finishSecondExercise();
                case 3:
                    return finishExam();
                default:
                    return true;
            }

        }

        private boolean studentArrived() {
            System.out.println(Thread.currentThread().getName()+ "学生准备好了,学生人数：" + getRegisteredParties());
            return false;
        }

        private boolean finishFirstExercise() {
            System.out.println(Thread.currentThread().getName()+ "第一题所有学生做完");
            return false;
        }

        private boolean finishSecondExercise() {
            System.out.println(Thread.currentThread().getName()+"第二题所有学生做完");
            return false;
        }

        private boolean finishExam() {
            System.out.println(Thread.currentThread().getName()+"第三题所有学生做完，结束考试");
            return true;
        }

    }
}
