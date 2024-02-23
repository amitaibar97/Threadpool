package il.co.ILRD.thread_pool;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import static il.co.ILRD.thread_pool.ThreadPool.*;

class PoolTest {
    private ThreadPool threadPool;

    @BeforeEach
    void setUp() {
        threadPool = new ThreadPool(4);
    }

    @AfterEach
    void tearDown() {
        threadPool.shutDown();
    }

    @Test
    void testSubmitCallablePriority() throws Exception {
        Callable<String> task = () -> "Test";
        Future<String> future = threadPool.submit(task, Priority.HIGH);
        assertEquals("Test", future.get());
    }

    @Test
    void testSubmitRunnablePriorityReturnValue() throws Exception {
        Runnable task = () -> System.out.println("Running task");
        Future<String> future = threadPool.submit(task, Priority.HIGH, "Done");
        assertEquals("Done", future.get());
    }

    @Test
    public void pauseAndResume() {
        Runnable task = () -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Running task");
        };
        for (int i =0 ; i < 10;++i){
            threadPool.submit(task, Priority.DEFAULT);
        }
        Future<Void> future = threadPool.submit(task, Priority.LOW);

        threadPool.pause();
        System.out.println("I Stopped the Threads");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(future.isDone());
        System.out.println("I resume");
        threadPool.resume();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testShutDown() throws ExecutionException, InterruptedException {
        Callable<String> longRunningTask = () -> {
            Thread.sleep(3000);
            return "Finished";
        };
        Callable<String> task = () -> "Test";

        Future<String > future = threadPool.submit(longRunningTask);
        threadPool.shutDown();
        threadPool.awaitTermination();
        assertNull(threadPool.submit(task));
        assertEquals("Finished", future.get());
        assertTrue(future.isDone());
    }

    @Test
    void testSetNumOfThreads() throws Exception {
        Callable<String> task1 = () -> {
            threadPool.setNumOfThreads(2);
            return "Changed";
        };

        Callable<String> task2 = () -> "Task";

        threadPool.submit(task1);
        Future<String> future = threadPool.submit(task2);
        assertEquals("Task", future.get());
    }

    @Test
    void testAwaitTerminationWithTimeout() {
        Callable<Void> longRunningTask = () -> {
            Thread.sleep(5000);
            return null;
        };
        assertThrows(TimeoutException.class, () -> {
            threadPool.submit(longRunningTask);
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        });
    }

    @Test
    void TestWithTimeout() throws TimeoutException {
        Callable<Void> longRunningTask = () -> {
            Thread.sleep(3000);
            return null;
        };
        Future<Void> future = threadPool.submit(longRunningTask);
        threadPool.shutDown();
        threadPool.awaitTermination(5,TimeUnit.SECONDS);

        assertTrue(future.isDone());
    }

    @Test
    void testTaskCancellation() {
        Callable<String> longRunningTask = () -> {
            Thread.sleep(10000);
            return "Finished";
        };
        for (int i = 0; i < 4 ; ++i) {
            threadPool.submit(longRunningTask, Priority.HIGH);
        }
        Future<String> future = threadPool.submit(longRunningTask);
        assertTrue(future.cancel(true));
        assertTrue(future.isCancelled());
        assertThrows(CancellationException.class, () -> future.get(1, TimeUnit.SECONDS));
    }
}