package thread_pool;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;


public class SemWaitableQueue<E> {
    private final Semaphore semaphore;
    private final PriorityQueue<E> queue;

    public SemWaitableQueue(int capacity) {
        this(null, capacity);
    }

    public SemWaitableQueue(Comparator<E> comparator, int capacity) {
        queue = new PriorityQueue<>(capacity, comparator);
        semaphore = new Semaphore(0);
    }

    public boolean enqueue(E element) {
        boolean ans;
        synchronized (this.queue) {
            ans = this.queue.add(element);
        }
        if (ans) {
            this.semaphore.release();
        }
        return ans;
    }

    public E dequeue() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (this.queue) {
            return this.queue.poll();
        }
    }

    public boolean remove(E element) {
        boolean removeFlag;

        synchronized (queue) {
            removeFlag = queue.remove(element);
        }
        if (removeFlag) {
            try {
                semaphore.acquire();

            } catch (InterruptedException e) {
                throw new RuntimeException();

            }
        }
        return removeFlag;
    }

    public int size() {
        synchronized (queue) {
            return queue.size();
        }
    }

    public E peek() {
        synchronized (queue) {
            return queue.peek();
        }
    }

    public boolean isEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }
}

