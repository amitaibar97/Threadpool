package thread_pool;




import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



public class ThreadPool implements Executor {
    private final AtomicInteger numOfThreads;
    private final SemWaitableQueue<Task<?>> tasks;
    private final int LOWEST_PRIORITY = Priority.LOW.value - 1;

    private final int HIGHEST_PRIORITY = Priority.HIGH.value + 1;

    Semaphore pauseSem;

    AtomicBoolean isShutDown = new AtomicBoolean(false);
    /*----------------------------------------------------------------*/

    public ThreadPool(int numOfThreads) {
        this.tasks = new SemWaitableQueue<>(numOfThreads);
        this.numOfThreads = new AtomicInteger(0);
        createWorkingThreads(numOfThreads);
        this.pauseSem = new Semaphore(0);
    }

    public <V> Future<V> submit(Callable<V> command, @NotNull Priority priority) {
        if(!isShutDown.get()) {
            return internalSubmit(command, priority.getValue());
        }
        return null;
    }

    public <V> Future<V> submit(Callable<V> command) {
        return this.submit(command, Priority.DEFAULT);
    }

    public <V> Future<V> submit(Runnable command, Priority priority) {
        return this.submit(command, priority, null);
    }


    public <V> Future<V> submit(Runnable command, Priority priority, V returnValue) {
        Callable<V> callableWrapper = () -> {
            command.run();
            return returnValue;
        };

        return this.submit(callableWrapper, priority); // code reuse
    }


    private <V> Future<V> internalSubmit(Callable<V> command, int priority) {

        Task<V> newTask = new Task<>(command, priority);
        if (!this.tasks.enqueue(newTask)) {
            throw new RuntimeException();
        }
        return newTask.getFuture();

    }

    @Override
    public void execute(Runnable command) {
        this.submit(command, Priority.DEFAULT);
    }

    public void setNumOfThreads(int numOfThreads) {
        int diff = numOfThreads - this.numOfThreads.get();
        if (diff > 0) {
            createWorkingThreads(diff);
        } else {
            killWorkingThreads(-diff, HIGHEST_PRIORITY);
        }

    }

    public void pause() {
        for (int i = 0; i < this.numOfThreads.get(); ++i) {
            this.internalSubmit(() -> {
                try {
                    this.pauseSem.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }, HIGHEST_PRIORITY);

        }
    }

    public void resume() {
        this.pauseSem.release(this.numOfThreads.get());
    }

    public void shutDown() {
        this.isShutDown.getAndSet(true);
        killWorkingThreads(this.numOfThreads.get(), LOWEST_PRIORITY);
    }

    public void awaitTermination(long timeout, TimeUnit unit) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (this.numOfThreads.get() != 0 && System.currentTimeMillis() - startTime < unit.toMillis(timeout)) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (this.numOfThreads.get() != 0) {
            throw new TimeoutException();
        }
    }

    public void awaitTermination() {
        while (this.numOfThreads.get() != 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void killWorkingThreads(int numOfThreadsToKill, int priority) {
        for (int i = 0; i < numOfThreadsToKill; ++i) {
            this.internalSubmit(() -> {
                ((WorkingThread) (Thread.currentThread())).isStopped = true;
                return null;
            }, priority);
        }
    }

    private void createWorkingThreads(int numOfThreadsToCreate) {
        for (int i = 0; i < numOfThreadsToCreate; ++i) {
            new WorkingThread().start();
        }
    }

    /*----------------------------------------------------------------*/
    public enum Priority {
        LOW(1),
        DEFAULT(5),
        HIGH(10);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /*----------------------------------------------------------------*/
    private class WorkingThread extends Thread {

        private volatile boolean isStopped;

        @Override
        public void run() {
            numOfThreads.incrementAndGet();
            while (!isStopped) {
                try {
                    tasks.dequeue().execute((WorkingThread) (Thread.currentThread()));
                } catch (InterruptedException e) {
                    Thread.interrupted();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            numOfThreads.decrementAndGet();
        }
    }

    /*----------------------------------------------------------------*/
    private class Task<V> implements Comparable<Task<V>> {

        private final int priority;

        private final TaskFuture<V> future;

        public Task(Callable<V> callable, int priority) {

            this.future = new TaskFuture<>(callable, this);
            this.priority = priority;
        }

        public int getPriority() {
            return this.priority;
        }

        public void execute(WorkingThread thread) throws Exception {
            future.run(thread);
        }

        public TaskFuture<V> getFuture() {
            return future;
        }

        @Override
        public int compareTo(@NotNull Task<V> task) {
            return task.priority - this.priority;
        }
    }

    /*----------------------------------------------------------------*/
    private class TaskFuture<V> implements Future<V> {

        private final Task<V> task;
        private V value;
        private final Callable<V> callable;
        private boolean isDone;
        private boolean isCancelled;
        private WorkingThread currentThread = null;

        private ExecutionException executionException = null;

        public TaskFuture(Callable<V> callable, Task<V> task) {
            this.callable = callable;
            this.value = null;
            this.isCancelled = false;
            this.isDone = false;
            this.task = task;
        }

        public void run(WorkingThread thread) throws Exception {
            try {
                value = callable.call();
            } catch (ExecutionException e) {
                this.executionException = e;
            }

            this.isDone = true;
            this.currentThread = thread;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (this.isDone || this.isCancelled) {
                return false;
            } else if (!tasks.remove(task)) {
                isCancelled = false;
                if (mayInterruptIfRunning) {
                    this.currentThread.interrupt();
                }
            } else {
                isCancelled = true;
            }
            return isCancelled;
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            try {
                return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new ExecutionException(e);
            }

        }

        @Override
        public V get(long timeout, @NotNull TimeUnit unit) throws
                InterruptedException, ExecutionException, TimeoutException {

            if (isCancelled) {
                throw new CancellationException();
            }
            if (executionException != null) {
                throw executionException;
            }
            long start = System.currentTimeMillis();

            while (!this.isDone && System.currentTimeMillis() - start < unit.toMillis(timeout)) {

                TimeUnit.SECONDS.sleep(1);

            }
            return value;
        }
    }
}


