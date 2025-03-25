package general_tests;

public interface Buffer {
    void produce(int id, int toProduce) throws InterruptedException;
    void consume(int id, int toConsume) throws InterruptedException;
    int getConsumerOperations();
    int getProducerOperations();
    boolean limitNotReached();
}
