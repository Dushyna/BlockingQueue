package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final LinkedList<T> query = new LinkedList<>();
    private final int maxSize;
    private final Lock mutex = new ReentrantLock();
    private final Condition producerWaitCondition = mutex.newCondition();
    private final Condition consumerWaitCondition = mutex.newCondition();


    public BlkQueueImpl(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {
        mutex.lock();
        try {
            while (query.size() >=  maxSize) {
                try {
                    producerWaitCondition.await();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            query.push(message);
            consumerWaitCondition.signal();
        } finally {
            mutex.unlock();
        }

    }

    @Override
    public T pop() {
        mutex.lock();
        try {
            while (query.isEmpty()) {
                try {
                    consumerWaitCondition.await();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T res = query.pop();
            producerWaitCondition.signal();
            return res;
        } finally {
            mutex.unlock();
        }
    }
}
