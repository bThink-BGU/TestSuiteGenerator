package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.context.PrintCOBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestSampler {
    static @NotNull Set<List<String>> generateRandomRunsOf(String bprogramname, int SAMPLE_SIZE, double WISH_PROBABILITY, double OBJECT_PROBABILITY) {

        List<String> inputSamples = new ArrayList<>();
        Set<List<String>> samples = new HashSet<>();
        Set<List<BEvent>> origSamples = new HashSet<>();

        int not_growing_counter = 0;
        int previous_size = 0;

        while (origSamples.size() < SAMPLE_SIZE && not_growing_counter < 10) {
//            ResourceBProgram program = new ResourceBProgram(bprogramname);
            BProgram program = new ContextBProgram(bprogramname);

            program.setEventSelectionStrategy(new GoalDrivenEventSelectionStrategy(WISH_PROBABILITY, OBJECT_PROBABILITY));

            var runner = new BProgramRunner(program);
            var eventLogger = runner.addListener(new InMemoryEventLoggingListener());
            runner.run();
            origSamples.add(eventLogger.getEvents());

            if (origSamples.size() == previous_size)
                not_growing_counter++;
            else
                not_growing_counter = 0;

            previous_size = origSamples.size();

        }
        try {
            String fileName = "SampledCases.txt";

            FileWriter writer = new FileWriter(fileName, false);
            for (var test : origSamples) {
                writer.write(test.stream().map(e -> e.name).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
                writer.write("\r\n"); // write new line
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//                                                  |
//        ------------------  generate sampled data | OR read generated sampled data from file | ----------------
//                                                                                             |
        String strline;
        try (BufferedReader br = new BufferedReader(new FileReader("SampledCases.txt"))) {
            while ((strline = br.readLine()) != null) {
                String[] lineArray = strline.split(",");
                inputSamples = Arrays.stream(lineArray).collect(Collectors.toList());
                samples.add(inputSamples);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return samples;
    }
}
