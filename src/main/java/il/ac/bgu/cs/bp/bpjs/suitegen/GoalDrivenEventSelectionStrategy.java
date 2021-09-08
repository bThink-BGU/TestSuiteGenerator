package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.NativeObject;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GoalDrivenEventSelectionStrategy extends SimpleEventSelectionStrategy {
    private final double OBJECT_PROBABILITY;
    private final double WISH_PROBABILITY;

    public GoalDrivenEventSelectionStrategy(long seed, double WISH_PROBABILITY, double OBJECT_PROBABILITY) {
        super(seed);
        this.OBJECT_PROBABILITY = OBJECT_PROBABILITY;
        this.WISH_PROBABILITY = WISH_PROBABILITY;
    }

    public GoalDrivenEventSelectionStrategy(double WISH_PROBABILITY, double OBJECT_PROBABILITY) {
        this.OBJECT_PROBABILITY = OBJECT_PROBABILITY;
        this.WISH_PROBABILITY = WISH_PROBABILITY;
    }

    @Override
    protected void warnOnHints(BProgramSyncSnapshot bpss) {
        // This code is left empty intentionally
    }

    @Override
    public Optional<EventSelectionResult> select(@NotNull BProgramSyncSnapshot bpss, Set<BEvent> selectableEvents) {

        var wishedFor = bpss.getStatements().stream()
                .map(SyncStatement::getData)
                .filter(Objects::nonNull)
                .map(d -> (BEvent) ((NativeObject) d).get("wish"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var objected = bpss.getStatements().stream()
                .map(SyncStatement::getData)
                .filter(Objects::nonNull)
                .map(d -> (BEvent) ((NativeObject) d).get("object"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        wishedFor.retainAll(selectableEvents);

        if (!objected.isEmpty() && rnd.nextDouble() < OBJECT_PROBABILITY) {
            wishedFor.removeAll(objected);
            selectableEvents.removeAll(objected);
        }

        if (!wishedFor.isEmpty() && rnd.nextDouble() < WISH_PROBABILITY)
            selectableEvents = wishedFor;

        return super.select(bpss, selectableEvents);
    }
}
