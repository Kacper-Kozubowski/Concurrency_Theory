import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.jcsp.lang.*;

public class PerformanceTest {
    public static int runTest(int numProducers, int numConsumers, int numBuffers, int numProductions, int bufferSize) {

        One2OneChannelInt[] producerChannels = new One2OneChannelInt[numProducers];
        for (int i = 0; i < numProducers; i++) {
            producerChannels[i] = Channel.one2oneInt();
        }

        One2OneChannelInt[] consumerChannels = new One2OneChannelInt[numConsumers];
        One2OneChannelInt[] consumerChannelsReq = new One2OneChannelInt[numConsumers];
        for (int i = 0; i < numConsumers; i++) {
            consumerChannels[i] = Channel.one2oneInt();
            consumerChannelsReq[i] = Channel.one2oneInt();
        }

        One2OneChannelInt[] bufferChannelsIn = new One2OneChannelInt[numBuffers];
        One2OneChannelInt[] bufferChannelsOut = new One2OneChannelInt[numBuffers];
        One2OneChannelInt[] bufferChannelsReq = new One2OneChannelInt[numBuffers];
        for (int i = 0; i < numBuffers; i++) {
            bufferChannelsIn[i] = Channel.one2oneInt();
            bufferChannelsOut[i] = Channel.one2oneInt();
            bufferChannelsReq[i] = Channel.one2oneInt();
        }

        CSProcess[] processes = new CSProcess[numProducers + numConsumers + numBuffers + 2];
        int numProductionsPerProducer = numProductions / numProducers;
        for (int i = 0; i < numProducers; i++) {
            processes[i] = new Producer(producerChannels[i], i, numProductionsPerProducer);
        }

        int numConsumptions = (numProductions * numProducers) / numConsumers;
        for (int i = 0; i < numConsumers; i++) {
            processes[numProducers + i] = new Consumer(consumerChannelsReq[i], consumerChannels[i], i, numConsumptions);
        }

        for (int i = 0; i < numBuffers; i++) {
            processes[numProducers + numConsumers + i] = new Buffer(bufferChannelsIn[i], bufferChannelsOut[i], bufferChannelsReq[i], bufferSize);
        }

        processes[numProducers + numConsumers + numBuffers] = new MultiplexerProducer(producerChannels, bufferChannelsIn, numProducers);
        processes[numProducers + numConsumers + numBuffers + 1] = new MultiplexerConsumer(bufferChannelsOut, bufferChannelsReq, consumerChannelsReq, consumerChannels, numConsumers);

        long startTime = System.nanoTime();
        Parallel par = new Parallel(processes);
        par.run();
        long endTime = System.nanoTime();

        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Test completed in " + durationMillis + " ms");

        return (int) durationMillis;
    }

    public static int runTest(int numProducers, int numConsumers, int numBuffers, int numProductions) {
        return runTest( numProducers, numConsumers, numBuffers, numProductions, 1);
        }

    public static void main(String[] args) {

        int[] productionsList = new int[20];
        for (int i = 0; i < productionsList.length; i++) {
            productionsList[i] = (i+1) * 500;
        }

//        boolean[] runTest = {true, true, true, true, false, true};
        boolean[] runTest = {false, false, false, false, true, false};

        StringBuilder header = new StringBuilder();
        header.append("TestNumber,numProducers,numConsumers,numBuffers,numProductions,TimeMillis,bufferSize\n");

        try (FileWriter writer = new FileWriter("TestData2.csv")) {
            writer.write(header.toString());

            // Test 1: Single producer, single consumer
            if (runTest[0]) {
                System.out.println("Running Test 1...");
                for (int i = 0; i < productionsList.length; i++) {
                    if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                    // Time measurement
                    int time = runTest(1, 1, 1, productionsList[i], 10);

                    // Append test data
                    StringBuilder rowData = new StringBuilder();
                    rowData.append(1).append(",")                   // Test number
                            .append(1).append(",")                  // Number of Producers
                            .append(1).append(",")                  // Number of Consumers
                            .append(1).append(",")                  // Number of Buffers
                            .append(productionsList[i]).append(",") // Number of Productions
                            .append(time).append(",")               // Measured time
                            .append(10).append("\n");               // Size of each buffer

                    writer.write(rowData.toString());
                    writer.flush();
                }
            }


            // Test 2: Multiple producers, single consumer
            if (runTest[1]) {
                System.out.println("Running Test 2...");
                int[] numProducers = {10, 100};
                for (int k = 0; k < numProducers.length; k++) {
                    for (int i = 0; i < productionsList.length; i++) {
                        if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                        // Time measurement
                        int time = runTest(numProducers[k], 1, 1, productionsList[i], 10);

                        // Append test data
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(20 + k + 1).append(",")          // Test number
                                .append(numProducers[k]).append(",")    // Number of Producers
                                .append(1).append(",")                  // Number of Consumers
                                .append(1).append(",")                  // Number of Buffers
                                .append(productionsList[i]).append(",") // Number of Productions
                                .append(time).append(",")               // Measured time
                                .append(10).append("\n");               // Size of each buffer

                        writer.write(rowData.toString());
                        writer.flush();
                    }
                }
            }


            // Test 3: Single producer, multiple consumers
            if (runTest[2]) {
                System.out.println("Running Test 3...");
                int[] numConsumers = {10, 100};
                for (int k = 0; k < numConsumers.length; k++) {
                    for (int i = 0; i < productionsList.length; i++) {
                        if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                        // Time measurement
                        int time = runTest(1, numConsumers[k], 1, productionsList[i], 10);

                        // Append test data
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(30 + k + 1).append(",")          // Test number
                                .append(1).append(",")                  // Number of Producers
                                .append(numConsumers[k]).append(",")    // Number of Consumers
                                .append(1).append(",")                  // Number of Buffers
                                .append(productionsList[i]).append(",") // Number of Productions
                                .append(time).append(",")               // Measured time
                                .append(10).append("\n");               // Size of each buffer

                        writer.write(rowData.toString());
                        writer.flush();
                    }
                }
            }


            // Test 4: Scaling buffers
            if (runTest[3]) {
                System.out.println("Running Test 4...");
                int[] numBuffers = {1, 10, 100};
                for (int k = 0; k < numBuffers.length; k++) {
                    for (int i = 0; i < productionsList.length; i++) {
                        if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                        // Time measurement
                        int time = runTest(10, 10, numBuffers[k], productionsList[i], 10);

                        // Append test data
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(40 + k + 1).append(",")          // Test number
                                .append(10).append(",")                 // Number of Producers
                                .append(10).append(",")                 // Number of Consumers
                                .append(numBuffers[k]).append(",")      // Number of Buffers
                                .append(productionsList[i]).append(",") // Number of Productions
                                .append(time).append(",")               // Measured time
                                .append(10).append("\n");               // Size of each buffer

                        writer.write(rowData.toString());
                        writer.flush();
                    }
                }
            }


            // Test 5: Stress testing
            if (runTest[4]) {
                System.out.println("Running Test 5...");
                int[] numConsumers = {100, 10, 1};
                for (int k = 0; k < numConsumers.length; k++) {
                    for (int i = 0; i < productionsList.length; i++) {
                        if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                        // Time measurement
                        int time = runTest(100, numConsumers[k], 10, productionsList[i], 10);

                        // Append test data
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(70 + k + 1).append(",")           // Test number
                                .append(100).append(",")                 // Number of Producers
                                .append(numConsumers[k]).append(",")    // Number of Consumers
                                .append(10).append(",")                 // Number of Buffers
                                .append(productionsList[i]).append(",") // Number of Productions
                                .append(time).append(",")               // Measured time
                                .append(10).append("\n");               // Size of each buffer

                        writer.write(rowData.toString());
                        writer.flush();
                    }
                }
            }

            // Test 6: Buffer capacity
            if (runTest[5]) {
                System.out.println("Running Test 6...");
                int[] bufferSizes = {1, 10, 100};
                for (int k = 0; k < bufferSizes.length; k++) {
                    for (int i = 0; i < productionsList.length; i++) {
                        if (i % 10 == 0) System.out.println("Number of productions: " + productionsList[i]);

                        // Time measurement
                        int time = runTest(5, 5, 5, productionsList[i], bufferSizes[k]);

                        // Append test data
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(60 + k + 1).append(",")          // Test number
                                .append(10).append(",")                 // Number of Producers
                                .append(10).append(",")                 // Number of Consumers
                                .append(5).append(",")                  // Number of Buffers
                                .append(productionsList[i]).append(",") // Number of Productions
                                .append(time).append(",")               // Measured time
                                .append(bufferSizes[k]).append("\n");   // Size of each buffer

                        writer.write(rowData.toString());
                        writer.flush();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
