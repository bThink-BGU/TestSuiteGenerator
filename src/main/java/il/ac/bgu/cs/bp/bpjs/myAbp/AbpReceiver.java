package il.ac.bgu.cs.bp.bpjs.myAbp;

import java.util.Queue;

public class AbpReceiver {


    //    private AbpInfra.states receiverState = AbpInfra.states.NONE;
    private int rSeq = 0;
    private AbpInfra infra = new AbpInfra();

    public AbpReceiver() {
        this.rSeq = 0;
    }
    public int getrSeq() { return rSeq; }
    public void setrSeq(int rSeq) { this.rSeq = rSeq; }

    public Queue<String> getReceived() {
        return infra.received;
    }
    public void setReceived(Queue<String> received) {
        infra.received = received;
    }


    public boolean sendAck(){
        System.out.println("A1-"+(!infra.t2r.isEmpty()));
        if (!infra.t2r.isEmpty()) System.out.println("A2-"+(infra.t2r.peek().getSecNo() == rSeq)+" t2r.seq-"+(infra.t2r.peek().getSecNo())+" rSeq-"+rSeq);
        System.out.println("A3-"+(infra.r2t.size() < infra.getCHN_SIZE()));

        return (!infra.t2r.isEmpty() && infra.t2r.peek().getSecNo() == rSeq && infra.r2t.size() < infra.getCHN_SIZE());
    }
    public boolean sendNak(){
        System.out.println("N1-"+(!infra.t2r.isEmpty()));
        if (!infra.t2r.isEmpty()) System.out.println("N2-"+(infra.t2r.peek().getSecNo() != rSeq)+" t2r.seq-"+(infra.t2r.peek().getSecNo())+" rSeq-"+rSeq);
        System.out.println("N3-"+(infra.r2t.size() < infra.getCHN_SIZE()));
        return (!infra.t2r.isEmpty() && infra.t2r.peek().getSecNo() != rSeq && infra.r2t.size() < infra.getCHN_SIZE());
    }
    public boolean ifLostR2t(){
        return (!infra.r2t.isEmpty() && infra.getCHN_LOSS());
    }
    public boolean ifReorderR2t(){
        return (!infra.r2t.isEmpty() && infra.getCHN_REORDER());
    }

    public void runReceiver(AbpInfra getInfra, AbpInfra.states receiverState) {

        infra = getInfra;

        switch (receiverState){
            case RECACK:
                if (sendAck()) {
                    sentAckMsg();
                    System.out.println("//sentAckMsg "+infra.toString()+" rSeq-"+rSeq+" ");
                }
                else {
                    System.out.println("//No sentAckMsg "+infra.toString()+" rSeq-"+rSeq+" ");
//                    throw new RuntimeException("fail to //sentAckMsg");
                }
                break;
            case RECNAK:
                if (sendNak()) {
                    sendNakMsg();
                    System.out.println("//sendNakMsg "+infra.toString()+" rSeq-"+rSeq+" ");
                }
                else {
                    System.out.println("//No sendNakMsg "+infra.toString()+" rSeq-"+rSeq+" ");
//                    throw new RuntimeException("fail to //sendNakMsg");
                }

                break;
            case R2TLOSS:
                if (ifLostR2t()) {
                    lostR2t();
                    System.out.println("//R2TLOSS "+infra.toString()+" rSeq-"+rSeq+" ");
                } else {
                    System.out.println("//No R2TLOSS "+infra.toString()+" rSeq-"+rSeq+" ");
//                    throw new RuntimeException("fail to //R2TLOSS");
                }
                break;
            case R2TREORDER:
                if (ifReorderR2t()) {
                    reorderR2t();
                    System.out.println("//R2TREORDER "+infra.toString()+" rSeq-"+rSeq+" ");
                }
                else {
                    System.out.println("//No R2TREORDER "+infra.toString()+" rSeq-"+rSeq+" ");
//                    throw new RuntimeException("//fail to R2TREORDER");
                }

                break;
        }

    }

    public void sentAckMsg(){
//        System.out.println("//sentAckMsg");
        L3Msg x = infra.t2r.remove();
        String payload = x.getData();

//        if (infra.prevInput == AbpInfra.externalInput.ACKNOK)
//            rSeq = rSeq;
//        else
            rSeq = (rSeq + 1) % infra.getSEQ_MAX();

        L3Msg t = new L3Msg(rSeq);
        infra.r2t.add(t);
        infra.received.add(payload);
    }
    public void sendNakMsg(){
//        System.out.println("//sendNakMsg");
        infra.t2r.remove();
        L3Msg t = new L3Msg(rSeq);
        infra.r2t.add(t);
    }

    public void lostR2t(){
//        System.out.println("//lostR2t");
        infra.r2t.remove();
    }

    public void reorderR2t() {
//        System.out.println("//reorderR2t");
        infra.reversequeue(infra.r2t);
    }


}
