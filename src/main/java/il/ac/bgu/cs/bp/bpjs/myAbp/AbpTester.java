package il.ac.bgu.cs.bp.bpjs.myAbp;

import java.util.EnumSet;

public class AbpTester {
    EnumSet<AbpInfra.states> senderStates = EnumSet.range(AbpInfra.states.SEND, AbpInfra.states.FINISH);
    EnumSet<AbpInfra.states> receiveStates = EnumSet.range(AbpInfra.states.RECACK, AbpInfra.states.R2TREORDER);
    private AbpInfra.states senderState = AbpInfra.states.NONE;
    private AbpInfra.states receiverState = AbpInfra.states.NONE;

    public AbpSender senderSimulator = new AbpSender();
    private AbpReceiver receiverSimulator = new AbpReceiver();
    private final AbpInfra infra = new AbpInfra();



    public void abpSimulator(AbpInfra.externalInput nextInput, String... data) {

        boolean isReceiver = false;
        String newData = (data.length >= 1) ? data[0] : "";

        if (nextInput != AbpInfra.externalInput.NONE) {
//            System.out.println("Now play - "+nextInput);
            switch (nextInput) {
                case TOSEND:
                    senderSimulator.TO_BE_SENT.add(newData);
                    break;
                case SEND:
                    senderState = AbpInfra.states.SEND;
                    receiverState = AbpInfra.states.NONE;
                    isReceiver = false;
                    break;
                case ACKOK:
                    senderState = AbpInfra.states.ACKOK;
                    receiverState = AbpInfra.states.NONE;
                    isReceiver = false;
                    break;
                case ACKNOK:
                    senderState = AbpInfra.states.ACKNOK;
                    receiverState = AbpInfra.states.NONE;
                    isReceiver = false;
                    break;
                case RECACK:
                    receiverState = AbpInfra.states.RECACK;
                    senderState = AbpInfra.states.NONE;
                    isReceiver = true;
                    break;
                case RECNAK:
                    receiverState = AbpInfra.states.RECNAK;
                    senderState = AbpInfra.states.NONE;
                    isReceiver = true;
                    break;
                case R2TLOSS:
                    receiverState = AbpInfra.states.R2TLOSS;
                    senderState = AbpInfra.states.NONE;
                    isReceiver = true;
                    break;
                case T2RLOSS:
                    senderState = AbpInfra.states.T2RLOSS;
                    receiverState = AbpInfra.states.NONE;
                    isReceiver = false;
                    break;
                case T2RREORDER:
                    senderState = AbpInfra.states.T2RREORDER;
                    receiverState = AbpInfra.states.NONE;
                    isReceiver = false;
                    break;
                case R2TREORDER:
                    receiverState = AbpInfra.states.R2TREORDER;
                    senderState = AbpInfra.states.NONE;
                    isReceiver = true;
                    break;
                case FINISH:
                case FAIL:
                case SUCCESS:
                    receiverState = AbpInfra.states.NONE;
                    senderState = AbpInfra.states.FINISH;
                    isReceiver = false;
                    break;
            }
        }
        else {
            receiverState = AbpInfra.states.NONE;
            senderState = AbpInfra.states.NONE;
        }

        if (isReceiver){
            receiverSimulator.runReceiver(infra, receiverState);
        }
        else {
            senderSimulator.runSender(infra, senderState);
        }
        infra.prevInput = nextInput;
        receiverState = AbpInfra.states.NONE;
        senderState = AbpInfra.states.NONE;
//        System.out.println("nextInput-"+nextInput+" senderState-"+senderState+" receiverState-"+receiverState);
//        System.out.println("TBS-"+senderSimulator.TO_BE_SENT+" t2r-"+infra.t2r+" r2t-"+infra.r2t+" rcv-"+infra.received);

    }
    public void resetInfra(){
        infra.resetInfra();
//        senderSimulator.clearTO_BE_SENT();
        senderSimulator.setSendNext(0);
        senderSimulator.settSeq(0);
        receiverSimulator.setrSeq(0);
    }
}
