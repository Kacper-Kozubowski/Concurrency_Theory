package general_tests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Buffer_4Cond implements Buffer {
    private int buffer;
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();  // Lock for synchronization
    private final Condition firstCons = lock.newCondition();
    private final Condition otherCons = lock.newCondition();
    private final Condition firstProd = lock.newCondition();
    private final Condition otherProd = lock.newCondition();
    private boolean firstProdWaiting = false;
    private boolean firstConsWaiting = false;
    private int consumerOperations = 0;
    private int producerOperations = 0;
    private final AtomicInteger globalOperations = new AtomicInteger(0);
    private final int operationsLimit;


    public Buffer_4Cond(int capacity, int operationsLimit) {
        this.capacity = capacity;
        this.buffer = 0;
        this.operationsLimit = operationsLimit;
    }

    public void produce(int id, int toProduce) throws InterruptedException {
        lock.lock();
        try {
            while (firstProdWaiting && limitNotReached()) {
                otherProd.await();
            }
            while (buffer + toProduce > capacity && limitNotReached()) {
                firstProdWaiting = true;
                firstProd.await();
            }
            buffer += toProduce;
            producerOperations++;
            globalOperations.incrementAndGet();
            otherProd.signal();
            firstCons.signal();
            firstProdWaiting = false;
        } finally {
            lock.unlock();
        }
    }

    public void consume(int id, int toConsume) throws InterruptedException {
        lock.lock();
        try {
            while (firstConsWaiting && limitNotReached()) {
                otherCons.await();
            }
            while (buffer < toConsume && limitNotReached()) {
                firstConsWaiting = true;
                firstCons.await();
            }
            buffer -= toConsume;
            consumerOperations++;
            globalOperations.incrementAndGet();
            otherCons.signal();
            firstProd.signal();
            firstConsWaiting = false;
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


