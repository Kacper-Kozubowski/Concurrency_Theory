import org.jcsp.lang.*;

public class MultiplexerProducer implements CSProcess {
    private One2OneChannelInt[] producers;
    private One2OneChannelInt[] buffers;
    private int currentBuffer = 0;
    private int producersNumber;

    public MultiplexerProducer(One2OneChannelInt[] producers, One2OneChannelInt[] buffers, int producersNumber) {
        this.producers = producers;
        this.buffers = buffers;
        this.producersNumber = producersNumber;
    }

    @Override
    public void run() {
        int processCount = producersNumber;

        while (processCount > 0) {
            for (One2OneChannelInt producer : producers) {
                int item = producer.in().read();
                if (item < 0) {
                    processCount--;
                    continue;
                }

//                System.out.println("MP received and sent " + item);
                buffers[currentBuffer].out().write(item);
                currentBuffer = (currentBuffer + 1) % buffers.length;
            }
        }
        for (One2OneChannelInt buffer : buffers) {
            buffer.out().write(-1);
        }
//        System.out.println("MP ended");
    }
}
