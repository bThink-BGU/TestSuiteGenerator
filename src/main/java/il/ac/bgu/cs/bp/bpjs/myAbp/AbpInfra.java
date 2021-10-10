package il.ac.bgu.cs.bp.bpjs.myAbp;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class AbpInfra {

    @Override
    public String toString() {
        return "AbpInfra{" +
                "t2r=" + t2r.toString() +
                ", r2t=" + r2t.toString() +
                ", received=" + received.toString() +
                '}';
    }

    public AbpInfra() {
    }
    static enum states {
        NONE, SEND, ACKOK, ACKNOK, T2RLOSS, T2RREORDER, FINISH, RECACK, RECNAK, R2TLOSS, R2TREORDER ;
        static
        public final states[] values = values();
        public states prev() { return values[(ordinal() - 1  + values.length) % values.length]; }
        public states next() { return values[(ordinal() + 1) % values.length]; }
    }
    EnumSet<states> senderStates = EnumSet.range(states.SEND, states.FINISH);
    EnumSet<states> receiveStates = EnumSet.range(states.RECACK, states.R2TREORDER);

    enum externalInput {
        NONE, TOSEND, SEND, ACKOK, ACKNOK, T2RLOSS, T2RREORDER, FINISH, SUCCESS, FAIL,  RECACK, RECNAK, R2TLOSS, R2TREORDER ;
    }
    private int SEQ_MAX = 2; // should be 2
    private int CHN_SIZE = 2; // should be 2
    private boolean CHN_LOSS = true;
    private boolean CHN_REORDER = true;

    Queue<L3Msg> t2r = new LinkedList<L3Msg>();
    Queue<L3Msg> r2t = new LinkedList<L3Msg>();
    Queue<String> received = new LinkedList<String>();

    private externalInput nextInput = externalInput.NONE;
    public externalInput prevInput = externalInput.NONE;

    public int getSEQ_MAX() {
        return SEQ_MAX;
    }

    public void setSEQ_MAX(int SEQ_MAX) {
        this.SEQ_MAX = SEQ_MAX;
    }

    public int getCHN_SIZE() {
        return CHN_SIZE;
    }

    public void setCHN_SIZE(int CHN_SIZE) {
        this.CHN_SIZE = CHN_SIZE;
    }

    public boolean getCHN_LOSS() {
        return CHN_LOSS;
    }

    public void setCHN_LOSS(boolean xCHN_LOSS) {
        CHN_LOSS = xCHN_LOSS;
    }

    public boolean getCHN_REORDER() {
        return CHN_REORDER;
    }

    static void reversequeue(Queue<L3Msg> queue)
    {
        Stack<L3Msg> stack = new Stack<>();
        while (!queue.isEmpty()) {
            stack.add(queue.peek());
            queue.remove();
        }
        while (!stack.isEmpty()) {
            queue.add(stack.peek());
            stack.pop();
        }
    }
    public void resetInfra(){
        r2t.clear();
        t2r.clear();
        received.clear();
        nextInput = externalInput.NONE;
        prevInput = externalInput.NONE;

    }


}
