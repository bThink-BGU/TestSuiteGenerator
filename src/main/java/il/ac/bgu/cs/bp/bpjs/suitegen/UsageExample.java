package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

// TODO: Use Moodle-cloud and/or alternating-bit-protocol as a use-case
public class UsageExample {
    private final String programName;
    private final int SAMPLE_SIZE;
    Instant now = Instant.now();

    UsageExample(String programName, int SAMPLE_SIZE, Function<Set<List<String>>, Integer> rankingFunction) {
        this.programName = programName;
        this.SAMPLE_SIZE = SAMPLE_SIZE;
    }

    public static void main(String[] args) {

        String program = "\"abp/dal.js\",\"abp/bl.js\",\"abp/Tester.js\",\"abp/kohn.js\"";
        new UsageExample("abp.js", 30000, BenchmarRanking::rankTestSuiteNext).run();
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
                new BruteForceOptimizer(1000, statistics),
                new GeneticOptimizer(0.7, 0.3, 200, 10));

        for (var optimizer : optimizers) {

            var testSuite = optimizer.optimize(samples, 10, BenchmarRanking::rankTestSuiteNext);

            reportDuration();

            out.printf("// %s generated a suite with rank %d:%n", optimizer.getClass().getSimpleName(), BenchmarRanking.rankTestSuiteNext(testSuite));
            for (var test : testSuite) {
                out.println("\t" + test.stream().map(e -> e).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
            }

            try {
                String fileName = "BestTestSuite.txt";

                FileWriter writer = new FileWriter(fileName, false);
                for (var test : testSuite) {
                    writer.write(test.stream().map(e -> e).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
                    writer.write("\r\n"); // write new line
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var khunCriterion = new KhunCriterion(100);
        var khunTestSuite = khunCriterion.candidateSuite(samples, 10, BenchmarRanking::rankTestSuiteKuhn, 75);
        out.printf("// Kuhn's operator generated a suite with rank %d:%n",  BenchmarRanking.rankTestSuiteKuhn(khunTestSuite));
        for (var test : khunTestSuite) {
            out.println("\t" + test.stream().map(e -> e).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
        }

        try {
            String fileName = "KhunTestSuite.txt";

            FileWriter writer = new FileWriter(fileName, false);
            for (var test : khunTestSuite) {
                writer.write(test.stream().map(e -> e).filter(e -> !e.startsWith("Context")).collect(Collectors.joining(",")));
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
        static final double[] eithnDMP = {1, 0.9, 0.81, 0.73, 0.66, 0.59, 0.53, 0.48, 0.43, 0.39, 0.35}; // 0.1
//        static final double[] eithnDMP = {1, 0.1, 0.01, 0.001, 0.0001, 0.0001, 0.0001,0.0001, 0.0001,0.0001, 0.0001}; // 0.1

        static class RankingCriteria {
            public RankingCriteria(boolean reachedGoal) {
                this.reachedGoal = reachedGoal;
            }

            final boolean reachedGoal;
        }
        //rankTestSuiteOld - original ranking test suite - find the suite that at least each GOAL events appears 9 times
        static public int rankTestSuiteNext(@NotNull Set<List<String>> testSuite) {

            List<String> newTestSuite = new ArrayList<>();

            for (var test : testSuite) {
                List<String> eventList = test.stream().collect(Collectors.toList());

                for (int x = 0; x < eventList.size()-1; x++) {
                    newTestSuite.add(("(" + eventList.get(x) + "," + eventList.get(x+1))+")");
                }

            }
            Map<String, Long> hm = newTestSuite.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//            out.println("set after-"+hm.size()+" "+hm.toString());
            return hm.size();
        }

        //rankTestSuiteOld - original ranking test suite - find the suite that at least each GOAL events appears 9 times
        static public int rankTestSuiteKuhn(@NotNull Set<List<String>> testSuite) {

            List<String> newTestSuite = new ArrayList<>();

            for (var test : testSuite) {
                List<String> eventList = test.stream().collect(Collectors.toList());

                for (int x=0; x<eventList.size(); x++) {
                    for (int y=0; y<eventList.size(); y++) {
                        newTestSuite.add( ("(" + eventList.get(x) + "," + eventList.get(y))+")");
                    }
                }
            }
            Map<String, Long> hm = newTestSuite.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//            out.println("set kuhn-"+hm.toString());

            return hm.size();
        }

        //rankTestSuiteKhun - find all pairs of events
        static public int rankTestSuiteKhun(@NotNull Set<List<String>> testSuite) {
            GoalFind goalLogic = new GoalFind();
            testSuite = goalLogic.goalFindSuiteKuhn(testSuite);

            var s1 = testSuite.stream().flatMap(test -> test.stream().filter(e -> e.startsWith("Goal")));
            var set = s1.collect(Collectors.toSet());
            out.println("set k- "+set);
            return set.size();
        }



        //rankTestSuiteExtendedKhun - find all pairs of events
        static public int rankTestSuiteExtendedKhun(@NotNull Set<List<String>> testSuite) {
            GoalFind goalLogic = new GoalFind();
            testSuite = goalLogic.goalFindSuiteAfter(testSuite);

            var s1 = testSuite.stream().flatMap(test -> {
                Stream<String> goal = test.stream().filter(e -> e.startsWith("Goal"));
                Stream<String> features = goal.map(g -> g + "::" + test.stream().filter(e -> e.equals(g)).count());
//                out.println("//"+features.collect(Collectors.toList()));
                return features;
            });
            var set = s1.collect(Collectors.toSet());
            out.println("set-"+set.toString());

            return set.size();
        }

        //Minimum Criteria -
        static public int rankTestSuite(@NotNull Set<List<String>> testSuite) {
            var s1 = testSuite.stream().map(
                    test -> test.stream().filter(e -> e.startsWith("Goal")).collect(Collectors.toSet()).size()
            );
            return s1.reduce(Integer::min).get();
        }

        static public int rankTestSuiteZero(@NotNull Set<List<String>> testSuite) {
            return 0;
        }


        private @NotNull
        static UsageExample.BenchmarRanking.RankingCriteria rankAnIndividualTest(@NotNull List<String> test) {
            return new RankingCriteria(test.contains(GOAL));
        }

    }
}

