package general_tests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer_2Cond implements Buffer {
    private int buffer;
    private final int capacity;
    private final Lock lock = new ReentrantLock();  // Lock for synchronization
    private final Condition notFull = lock.newCondition();   // Condition for producers (buffer not full)
    private final Condition notEmpty = lock.newCondition();
    private int consumerOperations = 0;
    private int producerOperations = 0;
    private final AtomicInteger globalOperations = new AtomicInteger(0);
    private final int operationsLimit;

    public Buffer_2Cond(int capacity, int operationsLimit) {
        this.capacity = capacity;
        this.buffer = 0;
        this.operationsLimit = operationsLimit;
    }

    public void produce(int id, int toProduce) throws InterruptedException {
        lock.lock();
        try {

            while (buffer + toProduce >= capacity && limitNotReached()) {
                notFull.await();
            }
            buffer += toProduce;
            globalOperations.incrementAndGet();
            producerOperations++;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void consume(int id, int toConsume) throws InterruptedException {
        lock.lock();
        try {

            while (buffer - toConsume < 0 && limitNotReached()) {
                notEmpty.await();
            }
            consumerOperations++;
            globalOperations.incrementAndGet();
            buffer -= toConsume;
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean limitNotReached() {
        return globalOperations.get() <= operationsLimit;
    }

    public int getConsumerOperations() {
        return consumerOperations;
    }
    public int getProducerOperations() {
        return producerOperations;
    }

}
