import org.jcsp.lang.*;

public class MultiplexerConsumer implements CSProcess {
    private One2OneChannelInt[] buffers;
    private One2OneChannelInt[] bufferRequests;
    private One2OneChannelInt[] consumerRequests;
    private One2OneChannelInt[] consumers;
    private int currentBuffer = 0;
    private int consumersNumber;

    public MultiplexerConsumer(One2OneChannelInt[] buffers, One2OneChannelInt[] bufferRequests, One2OneChannelInt[] consumerRequests, One2OneChannelInt[] consumers, int consumersNumber) {
        this.buffers = buffers;
        this.bufferRequests = bufferRequests;
        this.consumerRequests = consumerRequests;
        this.consumers = consumers;
        this.consumersNumber = consumersNumber;
    }


    @Override
    public void run() {
        int processCount = consumersNumber;
        boolean[] isAlive = new boolean[consumersNumber];
        for (int i = 0; i < consumersNumber; i++) {
            isAlive[i] = true;
        }

        while (processCount > 0) {
//            System.out.println("loop test 1 ");
            for (int i = 0; i < consumers.length; i++) {
                if (!isAlive[i]) {continue;}
//                System.out.println("loop test 1 ");
                int msg = consumerRequests[i].in().read();
                if (msg < 0) {
                    processCount--;
                    isAlive[i] = false;
//                    System.out.println("loop test 4 " + processCount);
                    break;
                }

//                System.out.println("MC received and sent request");
                bufferRequests[currentBuffer].out().write(0);

                int item = buffers[currentBuffer].in().read();
//                System.out.println("MC received and sent " + item);
                consumers[i].out().write(item);

                currentBuffer = (currentBuffer + 1) % buffers.length;
            }
        }
        for (One2OneChannelInt buffer : bufferRequests) {
            buffer.out().write(-1);
        }
//        System.out.println("MC ended");
    }
}
