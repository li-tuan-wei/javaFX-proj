package ldh.common.testui.util;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadUtilFactory {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static ThreadUtilFactory instance = null;

    public static ThreadUtilFactory getInstance() {
        if (instance == null) {
            synchronized (ThreadUtilFactory.class) {
                if (instance == null) {
                    instance = new ThreadUtilFactory();
                }
            }
        }
        return instance;
    }

    public <T>Future<T> submit(Task<T> task) {
        return (Future<T>) executorService.submit(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public <T> Future<T> submit(Supplier<T> supplier, Consumer<Task> consumer) {
        Task<T> task = new Task<T>() {

            @Override
            protected T call() throws Exception {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };
        if (consumer != null) {
            consumer.accept(task);
        }
        Future<T> future = (Future<T>) executorService.submit(task);
        return future;
    }
}
