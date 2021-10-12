package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
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
    Instant now = Instant.now();

    UsageExample(String programName, int SAMPLE_SIZE, Function<Set<List<BEvent>>, Integer> rankingFunction) {
        this.programName = programName;
        this.SAMPLE_SIZE = SAMPLE_SIZE;
    }

    public static void main(String[] args) {

        String program = "\"abp/dal.js\",\"abp/bl.js\",\"abp/Tester.js\",\"abp/kohn.js\"";
        new UsageExample("abp.js", 100, BenchmarRanking::rankTestSuiteExtendedKhun).run();
    }

    public void run() {
        out.printf("// Generating tests for %s%n", programName);

        Instant now = Instant.now();
        out.printf("// Sampling %d random paths%n", SAMPLE_SIZE);

        var samples = TestSampler.generateRandomRunsOf(
                programName, SAMPLE_SIZE, 0.5, 0.5);
//        samples = goalLogic.goalFindSuite(samples);

        reportDuration();

        out.printf("// Computing an optimal test suites%n");

        BruteForceOptimizer.Statistics statistics = new BruteForceOptimizer.Statistics();

        var optimizers = List.of(
//                new BruteForceOptimizer(10000, statistics),
                new GeneticOptimizer(0.7, 0.3, 200, 10));

        for (var optimizer : optimizers) {

            var testSuite = optimizer.optimize(samples, 10, BenchmarRanking::rankTestSuiteZero);

            reportDuration();

            out.printf("// %s generated a suite with rank %d:%n", optimizer.getClass().getSimpleName(), BenchmarRanking.rankTestSuiteZero(testSuite));
            for (var test : testSuite) {
                out.println("\t" + test.stream().map(e -> e.name).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
            }

            try {
                String fileName = "BestTestSuite.txt";

                FileWriter writer = new FileWriter(fileName, false);
                for (var test : testSuite) {
//                    writer.write(test.stream().map(e -> e.name.startsWith("Goal") ? "" : e.name ).collect(Collectors.joining(",")));
                    writer.write(test.stream().map(e -> e.name).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
                    writer.write("\r\n"); // write new line
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var khunCriterion = new KhunCriterion(10000);
        var khunTestSuite = khunCriterion.candidateSuite(samples, 10, BenchmarRanking::rankTestSuiteKhun, statistics.average);

        try {
            String fileName = "KhunTestSuite.txt";

            FileWriter writer = new FileWriter(fileName, false);
            for (var test : khunTestSuite) {
                writer.write(test.stream().map(e -> e.name).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
                writer.write("\r\n"); // write new line
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (statistics.average != 0)
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
        //rankTestSuiteOld - original ranking test suite - find the suite that at least each GOAL events appears 9 times
        static public int rankTestSuiteOld(@NotNull Set<List<BEvent>> testSuite) {
            GoalFind goalLogic = new GoalFind();

            testSuite = goalLogic.goalFindSuite(testSuite);

            ArrayList<String> list = new ArrayList<String>();
            Map<String, Integer> hm = new HashMap<String, Integer>();

            testSuite.stream().forEach(elem -> elem.forEach(subelem -> list.add(subelem.getName()))); //cat-2, fat-3
            hm.clear();
            for (String i : list) {
                if (i.startsWith("Goal")) {
                    Integer j = hm.get(i);
                    hm.put(i, (j == null) ? 1 : j + 1);
                }
            }
//            int j = 0;
//            for (Map.Entry<String, Integer> val : hm.entrySet()) {
//                j += 1;
//                System.out.println("Element " + j + " -" + val.getKey() + " "
//                        + "occurs"
//                        + ": " + val.getValue() + " times");
//            }
//            out.println("hm-" + hm.size() + " new rank-" + hm.values().stream().filter(x -> x > 8).count());
            return ((int) hm.values().stream().filter(x -> x > 8).count() * 10 / hm.size());

        }
        //rankTestSuiteExtendedKhun - find all pairs of events
        static public int rankTestSuiteExtendedKhun(@NotNull Set<List<BEvent>> testSuite) {
            GoalFind goalLogic = new GoalFind();
            testSuite = goalLogic.goalFindSuite(testSuite);

            var s1 = testSuite.stream().flatMap(test -> {
                Stream<BEvent> goal = test.stream().filter(e -> e.name.startsWith("Goal"));
                Stream<String> features = goal.map(g -> g.name + "::" + test.stream().filter(e -> e.name.equals(g.name)).count());
//                out.println("//"+features.collect(Collectors.toList()));
                return features;
            });
            var set = s1.collect(Collectors.toSet());
//            out.println(set);
            return set.size();
        }

        //rankTestSuiteKhun - find all pairs of events
        static public int rankTestSuiteKhun(@NotNull Set<List<BEvent>> testSuite) {
            GoalFind goalLogic = new GoalFind();
            testSuite = goalLogic.goalFindSuite(testSuite);

            var s1 = testSuite.stream().flatMap(test -> test.stream().filter(e -> e.name.startsWith("Goal")));
            var set = s1.collect(Collectors.toSet());
//            out.println(set);
            return set.size();
        }

        //Minimum Criteria -
        static public int rankTestSuite(@NotNull Set<List<BEvent>> testSuite) {
            var s1 = testSuite.stream().map(
                    test -> test.stream().filter(e -> e.name.startsWith("Goal")).collect(Collectors.toSet()).size()
            );
            return s1.reduce(Integer::min).get();
        }
        static public int rankTestSuiteZero(@NotNull Set<List<BEvent>> testSuite) {
            return 0;
        }


        private @NotNull
        static UsageExample.BenchmarRanking.RankingCriteria rankAnIndividualTest(@NotNull List<BEvent> test) {
            return new RankingCriteria(test.contains(GOAL));
        }

    }
}





