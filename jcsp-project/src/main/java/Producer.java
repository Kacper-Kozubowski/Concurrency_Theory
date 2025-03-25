import org.jcsp.lang.*;

public class Producer implements CSProcess {
    private One2OneChannelInt out;
    private int id;
    private int numProductions;

    public Producer(One2OneChannelInt out, int id, int numProductions) {
        this.out = out;
        this.id = id;
        this.numProductions = numProductions;
    }

    public void run() {
        for (int i = 0; i < numProductions; i++) {
//            System.out.println("Producer " + id + " produced; " + i);
            out.out().write(i+1);
        }
        out.out().write(-1);
//        System.out.println("Producer " + id + " ended");
    }
}