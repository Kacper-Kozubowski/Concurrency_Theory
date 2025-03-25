package general_tests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer_3Locks implements Buffer {
    private int buffer;
    private final int capacity;
    private final Lock producerLock = new ReentrantLock();
    private final Lock consumerLock = new ReentrantLock();
    private final Lock mainLock = new ReentrantLock();  // Lock for synchronization
    private final Condition notFull = mainLock.newCondition();   // Condition for producers (buffer not full)
    private final Condition notEmpty = mainLock.newCondition();
    private int consumerOperations = 0;
    private int producerOperations = 0;
    private final AtomicInteger globalOperations = new AtomicInteger(0);
    private final int operationsLimit;

    public Buffer_3Locks(int capacity, int operationsLimit) {
        this.capacity = capacity;
        this.buffer = 0;
        this.operationsLimit = operationsLimit;
    }

    public void produce(int id, int toProduce) throws InterruptedException {
        producerLock.lock();
        try {
            mainLock.lock();
            try {
                while (buffer + toProduce >= capacity && limitNotReached()) {
                    notFull.await();
                }
                buffer += toProduce;
                producerOperations++;
                globalOperations.incrementAndGet();
                notEmpty.signal();
            } finally {
                mainLock.unlock();
            }
        } finally {
            producerLock.unlock();
        }
    }

    public void consume(int id, int toConsume) throws InterruptedException {
        consumerLock.lock();
        try {
            mainLock.lock();
            try {
                while (buffer - toConsume < 0 && limitNotReached()) {
                    notEmpty.await();
                }
                buffer -= toConsume;
                consumerOperations++;
                globalOperations.incrementAndGet();
                notFull.signal();
            } finally {
                mainLock.unlock();
            }
        } finally {
            consumerLock.unlock();
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
