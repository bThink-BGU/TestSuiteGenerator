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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.System.out;

// TODO: Use Moodle-cloud and/or alternating-bit-protocol as a use-case
public class UsageExampleTable2 {
    private final String programName;
    private final int SAMPLE_SIZE;
    Instant now = Instant.now();

    UsageExampleTable2(String programName, int SAMPLE_SIZE, Function<Set<List<String>>, Integer> rankingFunction) {
        this.programName = programName;
        this.SAMPLE_SIZE = SAMPLE_SIZE;
    }

    public static void main(String[] args) {

        String program = "\"abp/dal.js\",\"abp/bl.js\",\"abp/Tester.js\",\"abp/kohn.js\"";
//        new UsageExampleTable2("abp.js", 100, BenchmarRanking::rankTestSuiteNext).run();   //Next
        new UsageExampleTable2("abp.js", 100, BenchmarRanking::rankTestSuiteKuhn).run(); //Kuhn
    }

    public void run() {
        out.printf("// Generating tests for %s%n", programName);

        Instant now = Instant.now();
        out.printf("// Sampling %d random paths%n", SAMPLE_SIZE);

        var samples = TestSampler.generateRandomRunsOf(programName, SAMPLE_SIZE, 0.5, 0.5);
//        samples = goalLogic.goalFindSuite(samples);

        out.println(reportDuration());

        out.printf("// Computing an optimal test suites%n");

        OptimalOptimizer.Statistics statistics1 = new OptimalOptimizer.Statistics();
        BruteForceOptimizer.Statistics statistics = new BruteForceOptimizer.Statistics();

        int saveNo = 0;
        var optimizers = List.of(
                new OptimalOptimizer(1, statistics1),
                new BruteForceOptimizer(1, statistics),
//                new BruteForceOptimizer(10000, statistics),
                new GeneticOptimizer(0.7, 0.3, 300, 10));

        StatisticData sdOften = new StatisticData();
        StatisticData sdRare = new StatisticData();
        for (int i=0; i<1; i++) {

            for (var optimizer : optimizers) {

//                var testSuite = optimizer.optimize(samples, 5, BenchmarRanking::rankTestSuiteNext);
//                 var testSuite = optimizer.optimize(samples, 10, BenchmarRanking::rankTestSuiteNext);   // without optimal
                 var testSuite = optimizer.optimize(samples, 5, BenchmarRanking::rankTestSuiteKuhn);
//                 var testSuite = optimizer.optimize(samples, 10, BenchmarRanking::rankTestSuiteKuhn);   // without optimal
                saveNo += 1;
                saveNo %= 3;

//                out.printf("// %s generated a suite with rank %d:%n", optimizer.getClass().getSimpleName(), BenchmarRanking.rankTestSuiteNext(testSuite));
                for (var test : testSuite) {
//                    out.println("\t" + test.stream().map(e -> e)
////                            .filter(e -> !e.startsWith("Context"))
//                            .collect(Collectors.joining(",")));

                    List<String> eventList = test.stream().collect(Collectors.toList());

                    //2-way words
                    for (int x = 0; x < eventList.size()-1; x++) {
                        if ((eventList.get(x) + "," + eventList.get(x+1)).equals("ackOk,ackOk"))
                        {
                            if (optimizer.getClass().getSimpleName().equals("GeneticOptimizer"))
                                sdRare.towWayOur[i] = 1;
                            else if (optimizer.getClass().getSimpleName().equals("OptimalOptimizer"))    // Optimal
                                sdRare.towWayOptimal[i] = 1;
                            else
                                sdRare.towWayRandom[i] = 1;
                        }
                        else if ((eventList.get(x) + "," + eventList.get(x+1)).equals("recNak,recAck"))
                        {
                            if (optimizer.getClass().getSimpleName().equals("GeneticOptimizer"))
                                sdOften.towWayOur[i] = 1;
                            else if (optimizer.getClass().getSimpleName().equals("OptimalOptimizer")) // Optimal
                                sdOften.towWayOptimal[i] = 1;
                            else
                                sdOften.towWayRandom[i] = 1;
                        }
                    }

                }

                try {
                    String fileName = "BestTestSuite_"+optimizer.getClass().getSimpleName()+"_"+saveNo+"_t2.txt";
                    FileWriter writer = new FileWriter(fileName, (i==0 ? false: true));

                    writer.write("// "+reportDuration()+"\r\n");

//                    writer.write("// "+optimizer.getClass().getSimpleName()+" generated a suite with rank "+BenchmarRanking.rankTestSuiteNext(testSuite)+", No-"+i+": \r\n");
                    writer.write("// "+optimizer.getClass().getSimpleName()+" generated a suite with rank "+BenchmarRanking.rankTestSuiteKuhn(testSuite)+", No-"+i+": \r\n");
                    for (var test : testSuite) {
                        writer.write(test.stream().map(e -> e)
//                                .filter(e -> !e.startsWith("Context"))
                                .collect(Collectors.joining(",")));
                        writer.write("\r\n"); // write new line
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            var khunCriterion = new KhunCriterion(1);
            var khunTestSuite = khunCriterion.genSuite(samples, 5, BenchmarRanking::rankTestSuiteKuhn, 55);
            // var khunTestSuite = khunCriterion.genSuite(samples, 5, BenchmarRanking::rankTestSuiteNext, 65);
            // var khunTestSuite = khunCriterion.`genSuite(samples, 10, BenchmarRanking::rankTestSuiteKuhn, 75);
            // var khunTestSuite = khunCriterion.genSuite(samples, 10, BenchmarRanking::rankTestSuiteNext, 65);
            for (var test : khunTestSuite) {
                List<String> eventList = test.stream().collect(Collectors.toList());

                //2-ways
                for (int x = 0; x < eventList.size()-1; x++) {
                        if ((eventList.get(x) + "," + eventList.get(x+1)).equals("ackOk,ackOk"))
                            sdRare.towWayKuhn[i] = 1;
                        else if ((eventList.get(x) + "," + eventList.get(x+1)).equals("recNak,recAck"))
                            sdOften.towWayKuhn[i] = 1;
                }

            }

            try {
                String fileName = "KhunTestSuite_t2.txt";

                FileWriter writer = new FileWriter(fileName, (i==0 ? false: true));

                writer.write("// "+reportDuration()+"\r\n");
//                writer.write ("// Kuhn's operator generated a suite with rank -"+BenchmarRanking.rankTestSuiteNext(khunTestSuite)+", No-"+i+"\r\n");
                writer.write ("// Kuhn's operator generated a suite with rank -"+BenchmarRanking.rankTestSuiteKuhn(khunTestSuite)+", No-"+i+"\r\n");
                for (var test : khunTestSuite) {
                    writer.write(test.stream().map(e -> e).collect(Collectors.joining(","))+"\r\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        out.println("sdRare.towWayOur-"+Arrays.stream(sdRare.towWayOur).sum());
        out.println("sdRare.towWayRand-"+Arrays.stream(sdRare.towWayRandom).sum());
        out.println("sdOften.towWayOur-"+Arrays.stream(sdOften.towWayOur).sum());
        out.println("sdOften.towWayRandom-"+Arrays.stream(sdOften.towWayRandom).sum());
        out.println("sdRare.towWayKuhn-"+Arrays.stream(sdRare.towWayKuhn).sum());
        out.println("sdOften.towWayKuhn-"+Arrays.stream(sdOften.towWayKuhn).sum());
        out.println("sdRare.towWayOptimal-"+Arrays.stream(sdRare.towWayOptimal).sum());
        out.println("sdOften.towWayOptimal-"+Arrays.stream(sdOften.towWayOptimal).sum());

        try {
            String fileName = "BestTestSuite_statisticData_t2.txt";

            FileWriter writer = new FileWriter(fileName, false);
            writer.write("// All data to \"ackOk,ackOk\"-"+sdRare.toString()+"\r\n");
            writer.write("\r\n"); // write new line
            writer.write("// All data to \"recNak,recAck\"-"+sdOften.toString()+"\r\n");
            writer.write("\r\n"); // write new line
            writer.write("// Number of \"ackOk,ackOk\" towWayOur-"+Arrays.stream(sdRare.towWayOur).sum()+"\r\n");
            writer.write("// Number of \"ackOk,ackOk\" towWayRand-"+Arrays.stream(sdRare.towWayRandom).sum()+"\r\n");
            writer.write("// Number of \"recNak,recAck\" towWayOur-"+Arrays.stream(sdOften.towWayOur).sum()+"\r\n");
            writer.write("// Number of \"recNak,recAck\" towWayRandom-"+Arrays.stream(sdOften.towWayRandom).sum()+"\r\n");
            writer.write("// Number of \"ackOk,ackOk\" towWayKuhn-"+Arrays.stream(sdRare.towWayKuhn).sum()+"\r\n");
            writer.write("// Number of \"recNak,recAck\" towWayKuhn-"+Arrays.stream(sdOften.towWayKuhn).sum()+"\r\n");
            writer.write("// Number of \"ackOk,ackOk\" towWayOptimal-"+Arrays.stream(sdRare.towWayOptimal).sum()+"\r\n");
            writer.write("// Number of \"recNak,recAck\" towWayOptimal-"+Arrays.stream(sdOften.towWayOptimal).sum()+"\r\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (statistics.average != 0)
            out.printf("// Average score of a random suite is %f%n", statistics.average);

        out.println("// done");
    }


    String reportDuration() {
        String duration = Duration.between(now, Instant.now()).toString();
        now = Instant.now();
        return ("// Duration: "+duration.substring(2));
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


    }
}

