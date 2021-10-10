package il.ac.bgu.cs.bp.bpjs.myAbp;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.NativeObject;


public class AbpActuator extends BProgramRunnerListenerAdapter {
  AbpTester simulator = new AbpTester();
  AbpInfra.externalInput nextInput = AbpInfra.externalInput.NONE;
  String nextData;

  @Override
  public void eventSelected(BProgram bp, BEvent e) {
    AbpInfra.externalInput nextEvent = AbpInfra.externalInput.NONE;

    if (e.name.equals("doNothing")) {
    } else if (e.name.equals("dataToBeSend")) {
      //DataToBeSend dataToBeSend = (DataToBeSend)e;
      NativeObject t = (NativeObject) e.getData();
      simulator.abpSimulator(AbpInfra.externalInput.TOSEND, (String) t.get("info"));
    } else if (e.name.equals("send")) {
      nextEvent = AbpInfra.externalInput.SEND;
    } else if (e.name.equals("ackOk")) {
      nextEvent = AbpInfra.externalInput.ACKOK;
    } else if (e.name.equals("ackNok")) {
      nextEvent = AbpInfra.externalInput.ACKNOK;
    } else if (e.name.equals("recAck")) {
      nextEvent = AbpInfra.externalInput.RECACK;
    } else if (e.name.equals("recNak")) {
      nextEvent = AbpInfra.externalInput.RECNAK;
    } else if (e.name.equals("t2rLoss")) {
      nextEvent = AbpInfra.externalInput.T2RLOSS;
    } else if (e.name.equals("r2tLoss")) {
      nextEvent = AbpInfra.externalInput.R2TLOSS;
    } else if (e.name.equals("t2rReorder")) {
      nextEvent = AbpInfra.externalInput.T2RREORDER;
    } else if (e.name.equals("r2tReorder")) {
      nextEvent = AbpInfra.externalInput.R2TREORDER;
    } else if (e.name.equals("success")) {
      nextEvent = AbpInfra.externalInput.FINISH;
    } else
        nextEvent = AbpInfra.externalInput.NONE;

    simulator.abpSimulator(nextEvent);
  }
}

