import org.jcsp.lang.*;

public class Main {

    public static void main(String[] args) {
        final int NUM_PRODUCERS = 10;
        final int NUM_CONSUMERS = 10;
        final int NUM_BUFFERS = 1;
        final int BUFFER_SIZE = 10;

        One2OneChannelInt[] producerChannels = new One2OneChannelInt[NUM_PRODUCERS];
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            producerChannels[i] = Channel.one2oneInt();
        }

        One2OneChannelInt[] consumerChannels = new One2OneChannelInt[NUM_CONSUMERS];
        One2OneChannelInt[] consumerChannelsReq = new One2OneChannelInt[NUM_CONSUMERS];
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            consumerChannels[i] = Channel.one2oneInt();
            consumerChannelsReq[i] = Channel.one2oneInt();
        }

        One2OneChannelInt[] bufferChannelsIn = new One2OneChannelInt[NUM_BUFFERS];
        One2OneChannelInt[] bufferChannelsOut = new One2OneChannelInt[NUM_BUFFERS];
        One2OneChannelInt[] bufferChannelsReq = new One2OneChannelInt[NUM_BUFFERS];
        for (int i = 0; i < NUM_BUFFERS; i++) {
            bufferChannelsIn[i] = Channel.one2oneInt();
            bufferChannelsOut[i] = Channel.one2oneInt();
            bufferChannelsReq[i] = Channel.one2oneInt();
        }

        CSProcess[] processes = new CSProcess[NUM_PRODUCERS + NUM_CONSUMERS + NUM_BUFFERS + 2];
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            processes[i] = new Producer(producerChannels[i], i, 10);
        }

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            processes[NUM_PRODUCERS + i] = new Consumer(consumerChannelsReq[i], consumerChannels[i], i, 10);
        }

        for (int i = 0; i < NUM_BUFFERS; i++) {
            processes[NUM_PRODUCERS + NUM_CONSUMERS + i] = new Buffer(bufferChannelsIn[i], bufferChannelsOut[i], bufferChannelsReq[i], BUFFER_SIZE);
        }

        processes[NUM_PRODUCERS + NUM_CONSUMERS + NUM_BUFFERS] = new MultiplexerProducer(producerChannels, bufferChannelsIn, NUM_PRODUCERS);
        processes[NUM_PRODUCERS + NUM_CONSUMERS + NUM_BUFFERS + 1] = new MultiplexerConsumer(bufferChannelsOut, bufferChannelsReq, consumerChannelsReq, consumerChannels, NUM_CONSUMERS);

        Parallel par = new Parallel(processes);
        par.run();

    }
}
