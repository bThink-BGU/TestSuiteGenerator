package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import org.mozilla.javascript.NativeObject;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * A single node in a program's execution tree. Contains the program's state,
 * and the last event to happen when getting to this state.
 *
 * @author Gera
 * @author Reut
 * @author michael
 */
public class DfsTraversalNode {

    public static double BOOST_PROBABILITY = 0.3;
    public static int MAX_TRAVERSAL_SPAN = 1000;

    static private Random rng = new Random();


    /**
     * Get the initial nod for a run of the passed {@code BPorgram}.
     *
     * @param bp   The {@link BProgram} being verified.
     * @param seed The {@link BProgramSyncSnapshot} received from the setup function of {@link BProgram}.
     * @return Initial node for the BProgram run
     */
    public static DfsTraversalNode getInitialNode(BProgram bp, BProgramSyncSnapshot seed) {
        return new DfsTraversalNode(bp, seed, null);
    }


    private final BProgramSyncSnapshot systemState;
    private final BProgram bp;
    private final Set<BEvent> selectableEvents;
    private final BEvent lastEvent;
    private final Iterator<BEvent> iterator;

    protected DfsTraversalNode(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
        this.bp = bp;
        this.systemState = systemState;
        this.lastEvent = e;

        if (bp != null) {
            selectableEvents = bp.getEventSelectionStrategy().selectableEvents(systemState);

            ArrayList<BEvent> eventOrdered = new ArrayList<>(selectableEvents);

            var boostedEvent = new ArrayList<BEvent>();
            for (var btss : systemState.getBThreadSnapshots()) {
                NativeObject data = (NativeObject) btss.getData();
                if (data != null) {
                    if (data.get("progress").equals("provider")) {
                        try {
                            BEvent waitFor = (BEvent) btss.getSyncStatement().getWaitFor();
                            if (eventOrdered.contains(waitFor))
                                boostedEvent.add(waitFor);
                        } catch (ClassCastException ex) {
                            // Ignore nonatomic waitFor statements
                        }
                    }
                }
            }

            Collections.shuffle(eventOrdered);

            if (!boostedEvent.isEmpty() && rng.nextFloat() < BOOST_PROBABILITY) {
                Collections.shuffle(boostedEvent);
                eventOrdered.addAll(0, boostedEvent);
            }

            iterator = eventOrdered.subList(0, Math.min(MAX_TRAVERSAL_SPAN, eventOrdered.size())).iterator();

            //iterator = eventOrdered.iterator();
        } else {
            selectableEvents = new HashSet<>();
            iterator = selectableEvents.iterator();
        }

    }

    private String stateString() {

        StringBuilder str = new StringBuilder();
        systemState.getBThreadSnapshots().forEach(
                s -> str.append("\t").append(s.toString()).append(" {").append(s.getSyncStatement()).append("} \n"));

        return str.toString();
    }

    @Override
    public String toString() {
        return ((lastEvent != null) ? "\n\tevent: " + lastEvent + "\n" : "") + stateString();
    }

    /**
     * Get a Node object for each possible state of the system after triggering
     * the given event.
     *
     * @param e the selected event
     * @param exSvc The executor service that will run the threads
     * @return State of the BProgram after event {@code e} was selected while
     * the program was at {@code this} node's state.
     * @throws BPjsRuntimeException  In case there's an error running the JavaScript code.
     *
     *
     * Note about {@code listeners} - while type-wise these are RUNNER listeners, they won't get
     * the start/stop etc. events, only the event-selected events.
     *
     * TODO: Make the above note obsolete via refactor of BPjs or the BP paradigm (e.g. using STM)
     *
     */
    public DfsTraversalNode getNextNode(BEvent e, ExecutorService exSvc) throws BPjsRuntimeException {
        try {
            return new DfsTraversalNode(bp, BProgramSyncSnapshotCloner.clone(systemState).triggerEvent(e, exSvc, Collections.emptyList(), bp.getStorageModificationStrategy()), e);
        } catch ( InterruptedException ie ) {
            throw new BPjsRuntimeException("Thread interrupted during event invocaiton", ie);
        }
    }

    /**
     * Get the events that can be triggered at the state.
     *
     * @return An iterator for the set of requested and not blocked events.
     */
    public Iterator<BEvent> getEventIterator() {
        return iterator;
    }

    public BEvent getLastEvent() {
        return lastEvent;
    }

    public BProgramSyncSnapshot getSystemState() {
        return systemState;
    }

    public Set<BEvent> getSelectableEvents() {
        return selectableEvents;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(systemState);
        //result = prime * result + Objects.hash(lastEvent);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DfsTraversalNode)) {
            return false;
        }

        DfsTraversalNode other = (DfsTraversalNode) obj;
        if (!Objects.equals(lastEvent, other.getLastEvent())) {
            return false;
        }

        return Objects.equals(systemState, other.getSystemState());
    }

}
