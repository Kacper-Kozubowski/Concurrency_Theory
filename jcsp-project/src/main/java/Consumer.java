import org.jcsp.lang.*;

public class Consumer implements CSProcess {
    private One2OneChannelInt req;
    private One2OneChannelInt in;
    private int id;
    private int numConsumptions;

    public Consumer(One2OneChannelInt req, One2OneChannelInt in,  int id, int numConsumptions) {
        this.req = req;
        this.in = in;
        this.id = id;
        this.numConsumptions = numConsumptions;
    }

    public void run() {
        for (int i = 0; i < numConsumptions; i++) {
            req.out().write(0);
            int item = in.in().read();
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // DEBUG
            if (item > 0) {
//                System.out.println("Consumer " + id + " consumed; " + i);
            }
            else {
//                System.out.println("Consumer " + id + " failed to consume");
            }
            // END_DEBUG
        }
        req.out().write(-1);
//        System.out.println("Consumer " + id + " ended");
    }
}