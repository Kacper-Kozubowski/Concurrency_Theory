import org.jcsp.lang.*;

public class Buffer implements CSProcess {
    private final One2OneChannelInt in;
    private final One2OneChannelInt out;
    private final One2OneChannelInt req;
    private final int[] memory;
    private int first = 0;
    private int last = 0;
    private int buffer = 0;

    public Buffer(One2OneChannelInt in, One2OneChannelInt out, One2OneChannelInt req, int buffersize) {
        this.in = in;
        this.out = out;
        this.req = req;
        this.memory = new int[buffersize];
    }

    @Override
    public void run() {
        final Guard[] guards = {req.in(), in.in()};
        final Alternative alt = new Alternative(guards);

        boolean aliveMP = true;
        boolean aliveMC = true;

        while (aliveMC || aliveMP) {
//            int selected = alt.select();
            int selected = (buffer > 0 || !aliveMP) ? alt.priSelect() : alt.select();

            switch (selected) {
                case 0: // Consumption

                    if (buffer > 0 || !aliveMP) {
                        int msg = req.in().read();
                        if (msg < 0) {
                            aliveMC = false;
                            continue;
                        }

                        out.out().write(memory[first]);
                        memory[first] = 0;
                        first = (first + 1) % memory.length;
                        buffer--;

                        // DEBUG

//                        System.out.print("[");
//                        for (int val : memory) {
//                            System.out.print(val + ", ");
//                        }
//                        System.out.print("] ");
//                        System.out.print(first + " " + last + "\n");
//                        System.out.println("Buffer (Cons): " + buffer);
//                        END_DEBUG
                    }
                    break;

                case 1: // Production

                    if (buffer < memory.length) {
                        int item = in.in().read();
                        if (item < 0) {
                            aliveMP = false;
                            continue;
                        }
                        memory[last] = item;
                        last = (last + 1) % memory.length;
                        buffer++;

                        // DEBUG
//                        System.out.print("[");
//                        for (int val : memory) {
//                            System.out.print(val + ", ");
//                        }
//                        System.out.print("] ");
//                        System.out.print(first + " " + last + "\n");
                        // END_DEBUG
                    }
//                    System.out.println("Buffer (Prod): " + buffer);
                    break;
            }
        }
//        System.out.println("Buffer ended");
//        System.out.println("Buffer final state: " + buffer);
    }
}
