package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

// TODO: Use Moodle-cloud and/or alternating-bit-protocol as a use-case
public class UsageExample {
    private final String programName;
    private final int SAMPLE_SIZE;
    private final Function<Set<List<BEvent>>, Integer> rankingFunction;
    Instant now = Instant.now();

    UsageExample(String programName, int SAMPLE_SIZE, Function<Set<List<BEvent>>, Integer> rankingFunction) {
        this.programName = programName;
        this.SAMPLE_SIZE = SAMPLE_SIZE;
        this.rankingFunction = rankingFunction;
    }

    public static void main(String[] args) {
        String program = "\"abp/dal.js\",\"abp/bl.js\",\"abp/Tester.js\",\"abp/kohn.js\"";
        new UsageExample("abp.js", 1500, BenchmarRanking::rankTestSuite).run();
    }

    public void run() {
        out.printf("// Generating tests for %s%n", programName);

        Instant now = Instant.now();
        out.printf("// Sampling %d random paths%n", SAMPLE_SIZE);

        var samples = TestSampler.generateRandomRunsOf(
                programName, SAMPLE_SIZE, 0.5, 0.5);

        reportDuration();

        out.printf("// Computing an optimal test suites%n");

        BruteForceOptimizer.Statistics statistics = new BruteForceOptimizer.Statistics();

        var optimizers = List.of(
                new BruteForceOptimizer(50000, statistics),
                new GeneticOptimizer(0.7, 0.3, 100, 10));

        for (var optimizer : optimizers) {

            var testSuite = optimizer.optimize(samples, 10, rankingFunction);

            reportDuration();

            out.printf("// %s generated a suite with rank %d:%n", optimizer.getClass().getSimpleName(), BenchmarRanking.rankTestSuite(testSuite));
            for (var test : testSuite) {
                out.println("\t" + test.stream().map(e -> e.name).collect(Collectors.joining(",")));
            }
        }

        if(statistics.average != 0)
            out.printf("// Average score of a random suite is %f%n", statistics.average);

        out.println("// done");
    }


    void reportDuration() {
        String duration = Duration.between(now, Instant.now()).toString();
        out.printf("// Duration: %s%n", duration.substring(2));
        now = Instant.now();
    }

    static class BenchmarRanking {
        @NotNull
        static final BEvent GOAL = new BEvent("success");

        static class RankingCriteria {
            public RankingCriteria(boolean reachedGoal) {
                this.reachedGoal = reachedGoal;
            }

            final boolean reachedGoal;
        }

        static public int rankTestSuite(@NotNull Set<List<BEvent>> testSuite) {

            ArrayList<String> list = new ArrayList<String>();
            Map<String, Integer> hm = new HashMap<String, Integer>();

            testSuite.stream().forEach(elem -> elem.forEach(subelem -> list.add(subelem.getName())) ); //cat-2, fat-3
            hm.clear();
            for (String i : list) {
                if (i.startsWith("Goal")) {
                    Integer j = hm.get(i);
                    hm.put(i, (j == null) ? 1 : j + 1);
                }
            }
//            int j = 0;
//            for (Map.Entry<String, Integer> val : hm.entrySet()) {
//                j+=1;
//                System.out.println("Element " +j+ " -"+ val.getKey() + " "
//                        + "occurs"
//                        + ": " + val.getValue() + " times");
//            }
//            out.println("hm-"+hm.size()+" j-"+j+" "+hm);

            return (hm.size()/81)*10;

        }


        private @NotNull
        static UsageExample.BenchmarRanking.RankingCriteria rankAnIndividualTest(@NotNull List<BEvent> test) {
            return new RankingCriteria(test.contains(GOAL));
        }

    }
}





