package general_tests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

class Producer extends Thread {
    private final Buffer buffer;
    private final int id;
    private final int maxProduction;
    private int operations = 0;
    private Random random = new Random();

    public Producer(Buffer buffer, int maxProduction, int id) {
        this.buffer = buffer;
        this.maxProduction = maxProduction;
        this.id = id;

    }

    @Override
    public void run() {
        try {
            while (buffer.limitNotReached()) {
                int toProduce = random.nextInt(maxProduction) + 1;
                buffer.produce(id, toProduce);
                operations++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public int getOperations() {
        return operations;
    }
}

class Consumer extends Thread {
    private final Buffer buffer;
    private final int id;;
    private final int maxConsumption;
    private int operations = 0;
    private final Random random = new Random();

    public Consumer(Buffer buffer, int maxConsumption , int id) {
        this.buffer = buffer;
        this.maxConsumption = maxConsumption;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (buffer.limitNotReached()) {
                int toConsume = random.nextInt(maxConsumption) + 1;
                buffer.consume(id, toConsume);
                operations++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public int getOperations() {
        return operations;
    }
}

public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";


    public static void main(String[] args) {

//        int[] Ns = {2, 5, 20, 50, 100, 250, 500, 1000, 2000, 5000};  // Liczba producentów
//        int[] Ms = {2, 5, 20, 50, 100, 250, 500, 1000, 2000, 5000};  // Liczba konsumentów
//        int[] bufferCapacities = {5, 8, 10, 100, 250, 500, 1000, 2000, 5000, 10000};  // Pojemność bufora
//        float[] bufferPart = {0.01F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.5F};

        int[] Ns = {250, 500};
        int[] Ms = {2, 2, 2, 2};
        int[] bufferCapacities = {100, 100, 100, 100};
        float[] bufferPart = {0.01F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.5F};
        int[] maxProductions = {5, 5, 5, 5};
        int[] maxConsumptions = {50, 50, 50, 50};


        int[] operationsList = new int[100];
        for (int i = 0; i < operationsList.length; i++) {
            operationsList[i] = (i + 1) * 500;
        }
        Random random = new Random();
        int tests = 2;

        StringBuilder operationsData = new StringBuilder();
        operationsData.append("TestNumber,ThreadOperations,Type,Operations\n");

//        operationsList = new int[]{100 * 500};
//        StringBuilder header = new StringBuilder();
//        header.append("TestNumber,")
//                .append("ThreadOperations,")
//                .append("Type,")
//                .append("ID,")
//                .append("Operations,")
//                .append("Producers,")
//                .append("Consumers,")
//                .append("BufferCapacity,")
//                .append("MaxProductions,")
//                .append("MaxConsumptions,")
//                .append("Algorithm\n");


        StringBuilder header = new StringBuilder();
        header.append("TestNumber,")
                .append("Producers,")
                .append("Consumers,")
                .append("BufferCapacity,")
                .append("MaxProductions,")
                .append("MaxConsumptions,")
                .append("Operations,")
                .append("TimeMillis,")
                .append("Algorithm\n");

        try (FileWriter writer = new FileWriter("Eksperyment3.1.csv")) {
            writer.write(header.toString());

            for (int n = 0; n < tests; n++) {

//                int N = Ns[random.nextInt(Ns.length)];
//                int M = Ms[random.nextInt(Ms.length)];
//                int bufferCapacity = bufferCapacities[random.nextInt(bufferCapacities.length)];
//                int maxProduction = (int) floor((float) bufferCapacity * bufferPart[random.nextInt(bufferPart.length)]);
//                int maxConsumption = (int) floor((float) bufferCapacity * bufferPart[random.nextInt(bufferPart.length)]);
//
//                if (maxProduction == 0) maxProduction = 1;
//                if (maxConsumption == 0) maxConsumption = 1;

                int N = Ns[n];
                int M = Ms[n];
                int bufferCapacity = bufferCapacities[n];
                int maxProduction = maxProductions[n];
                int maxConsumption = maxConsumptions[n];

                for (int k = 0; k < 3; k++) {
                    for (int j = 0; j < operationsList.length; j++) {

                        System.out.print("Test " + ((n * 3 + k) * operationsList.length + j + 1) + ", Conf: " + n + ", Step: " + j + ", Algo: " + k + ", Ops: " + operationsList[j] + "\n");

                        Buffer buffer;
                        if (k == 0) {
                            buffer = new Buffer_2Cond(bufferCapacity, operationsList[j]);
                        } else if (k == 1) {
                            buffer = new Buffer_4Cond(bufferCapacity, operationsList[j]);
                        } else {
                            buffer = new Buffer_3Locks(bufferCapacity, operationsList[j]);
                        }

                        Producer[] producers = new Producer[N];
                        Consumer[] consumers = new Consumer[M];

                        long startTime = System.currentTimeMillis();

                        for (int i = 0; i < N; i++) {
                            producers[i] = new Producer(buffer, maxProduction, i + 1);
                            producers[i].start();
                        }

                        for (int i = 0; i < M; i++) {
                            consumers[i] = new Consumer(buffer, maxConsumption, -N + 1);
                            consumers[i].start();
                        }

                        for (int i = 0; i < N; i++) {
                            try {
                                producers[i].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for (int i = 0; i < M; i++) {
                            try {
                                consumers[i].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;

                        //                    int producerOps = buffer.getProducerOperations();
                        //                    int consumerOps = buffer.getConsumerOperations();
//
                        StringBuilder rowData = new StringBuilder();
                        rowData.append(n).append(",")
                                .append(N).append(",")
                                .append(M).append(",")
                                .append(bufferCapacity).append(",")
                                .append(maxProduction).append(",")
                                .append(maxConsumption).append(",")
                                .append(operationsList[j]).append(",")
                                .append(totalTime).append(",");

                        if (k == 0) rowData.append("2Cond\n");
                        else if (k == 1) rowData.append("4Cond\n");
                        else rowData.append("3Locks\n");

                        writer.write(rowData.toString());
                        writer.flush();

//                        StringBuilder rowData = null;
//                        for (int i = 0; i < N; i++) {
//                            rowData = new StringBuilder();
//                            rowData.append(n*3 + k).append(",")
//                                    .append(producers[i].getOperations()).append(",")
//                                    .append('P').append(",")
//                                    .append(i).append(",")
//                                    .append(operationsList[j]).append(",")
//                                    .append(N).append(",")
//                                    .append(M).append(",")
//                                    .append(bufferCapacity).append(",")
//                                    .append(maxProduction).append(",")
//                                    .append(maxConsumption).append(",");
//                            if (k == 0) rowData.append("2Cond\n");
//                            else if (k == 1) rowData.append("4Cond\n");
//                            else rowData.append("3Locks\n");
//                            writer.write(rowData.toString());
//                            writer.flush();
//                        }
//
//                        for (int i = 0; i < M; i++) {
//                            rowData = new StringBuilder();
//                            rowData.append(n*3 + k).append(",")
//                                    .append(consumers[i].getOperations()).append(",")
//                                    .append('C').append(",")
//                                    .append(N + i).append(",")
//                                    .append(operationsList[j]).append(",")
//                                    .append(N).append(",")
//                                    .append(M).append(",")
//                                    .append(bufferCapacity).append(",")
//                                    .append(maxProduction).append(",")
//                                    .append(maxConsumption).append(",");
//                            if (k == 0) rowData.append("2Cond\n");
//                            else if (k == 1) rowData.append("4Cond\n");
//                            else rowData.append("3Locks\n");
//                            writer.write(rowData.toString());
//                            writer.flush();
//                        }



//                        System.out.println("Test " + (j + n * operationsList.length + 1) + ": Time = " + totalTime + "ms, Producer Ops = " + producerOps + ", Consumer Ops = " + consumerOps);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Koniec\n");
    }
}
