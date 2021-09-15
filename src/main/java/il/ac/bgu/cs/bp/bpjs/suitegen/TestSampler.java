package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.context.PrintCOBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestSampler {
    static @NotNull Set<List<BEvent>> generateRandomRunsOf(String bprogramname, int SAMPLE_SIZE, double WISH_PROBABILITY, double OBJECT_PROBABILITY) {

        Set<List<BEvent>> samples = new HashSet<>();

        int not_growing_counter = 0;
        int previous_size = 0;

        while (samples.size() < SAMPLE_SIZE && not_growing_counter < 10) {
//            ResourceBProgram program = new ResourceBProgram(bprogramname);
            BProgram program = new ContextBProgram(bprogramname);

            program.setEventSelectionStrategy(new GoalDrivenEventSelectionStrategy(WISH_PROBABILITY, OBJECT_PROBABILITY));

            var runner = new BProgramRunner(program);

            var eventLogger = runner.addListener(new InMemoryEventLoggingListener());
            runner.run();
            samples.add(eventLogger.getEvents());

            if (samples.size() == previous_size)
                not_growing_counter++;
            else
                not_growing_counter = 0;

            previous_size = samples.size();
        }

        return samples;
    }
}
