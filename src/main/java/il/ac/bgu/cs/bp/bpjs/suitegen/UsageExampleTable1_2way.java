package il.ac.bgu.cs.bp.bpjs.suitegen;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.out;

// TODO: Use Moodle-cloud and/or alternating-bit-protocol as a use-case
public class UsageExampleTable1_2way {
    private final String programName;
    private final int SAMPLE_SIZE;
    Instant now = Instant.now();

//    private final int SUITE_SIZE = 5;
    private final int SUITE_SIZE = 10;
//    private final int SUITE_SIZE = 20;

    static Function<Set<List<String>>, Integer> rankingFunction  = BenchmarRanking::rankTestSuiteNext;
//    Function<Set<List<String>>, Integer> rankingFunction  = BenchmarRanking::rankTestSuiteKuhn;


    UsageExampleTable1_2way(String programName, int SAMPLE_SIZE, Function<Set<List<String>>, Integer> rankingFunction) {
        this.programName = programName;
        this.SAMPLE_SIZE = SAMPLE_SIZE;
    }

    public static void main(String[] args) {

        String program = "\"abp/dal.js\",\"abp/bl.js\",\"abp/Tester.js\",\"abp/kohn.js\"";
        new UsageExampleTable1_2way("abp.js", 50000, rankingFunction).run();
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

        var optimizers = List.of(
                // new OptimalOptimizer(1, statistics1),
                new BruteForceOptimizer(1, statistics),
                new GeneticOptimizer(0.7, 0.05, 300, 5));

        StatisticData sdOften = new StatisticData();
        StatisticData sdRare = new StatisticData();
        for (int i=0; i<1000; i++) {

            for (var optimizer : optimizers) {

                var testSuite = optimizer.optimize(samples, SUITE_SIZE, rankingFunction);


                for (var test : testSuite) {
                    List<String> eventList = test.stream().collect(Collectors.toList());

//                    2-way words
                    for (int x = 0; x < eventList.size()-1; x++) {
                        if ((eventList.get(x) + "," + eventList.get(x+1)).equals("ackOk,ackOk"))
                        {
                            if (optimizer.getClass().getSimpleName().equals("GeneticOptimizer"))
                                sdRare.towWayOur[i] = 1;
                            else
                                sdRare.towWayRandom[i] = 1;
                        }
                        else if ((eventList.get(x) + "," + eventList.get(x+1)).equals("recNak,recAck"))
                        {
                            if (optimizer.getClass().getSimpleName().equals("GeneticOptimizer"))
                                sdOften.towWayOur[i] = 1;
                            else
                                sdOften.towWayRandom[i] = 1;
                        }
                    }

                }

                try {
                    String fileName = "BestTestSuite_"+optimizer.getClass().getSimpleName()+"_t1.txt";
                    FileWriter writer = new FileWriter(fileName, (i==0 ? false: true));

                    writer.write("// "+reportDuration()+"\r\n");

                    writer.write("// "+optimizer.getClass().getSimpleName()+" generated a suite with rank "+rankingFunction.apply(testSuite)+", No-"+i+": \r\n");
                    for (var test : testSuite) {
                        writer.write(test.stream().map(e -> e)
                                .collect(Collectors.joining(",")));
                        writer.write("\r\n"); // write new line
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            var khunCriterion = new KhunCriterion(1000);
            var khunTestSuite = khunCriterion.genSuite(samples, SUITE_SIZE, BenchmarRanking::rankTestSuiteKuhn, 75);
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
                String fileName = "KhunTestSuite_t1.txt";

                FileWriter writer = new FileWriter(fileName, (i==0 ? false: true));

                writer.write("// "+reportDuration()+"\r\n");
                writer.write ("// Kuhn's operator generated a suite with rank -"+ BenchmarRanking.rankTestSuiteKuhn(khunTestSuite)+", No-"+i+"\r\n");
                for (var test : khunTestSuite) {
                    writer.write(test.stream().map(e -> e).collect(Collectors.joining(","))+"\r\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        //2-ways
        out.println("sdRare.towWayOur-"+Arrays.stream(sdRare.towWayOur).sum());
        out.println("sdRare.towWayRand-"+Arrays.stream(sdRare.towWayRandom).sum());
        out.println("sdOften.towWayOur-"+Arrays.stream(sdOften.towWayOur).sum());
        out.println("sdOften.towWayRandom-"+Arrays.stream(sdOften.towWayRandom).sum());
        out.println("sdRare.towWayKuhn-"+Arrays.stream(sdRare.towWayKuhn).sum());
        out.println("sdOften.towWayKuhn-"+Arrays.stream(sdOften.towWayKuhn).sum());
        out.println("sdRare.towWayOptimal-"+Arrays.stream(sdRare.towWayOptimal).sum());
        out.println("sdOften.towWayOptimal-"+Arrays.stream(sdOften.towWayOptimal).sum());


        try {
            //2-ways
            String fileName = "BestTestSuite_statisticData_t1.txt";

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
            // writer.write("// Number of \"ackNok,ackNok,recAck\" towWayOptimal-"+Arrays.stream(sdRare.towWayOptimal).sum()+"\r\n");
            // writer.write("// Number of \"send,send,ackOk\" towWayOptimal-"+Arrays.stream(sdOften.towWayOptimal).sum()+"\r\n");


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

        //rankTestSuiteOld - original ranking test suite - find the suite that at least each GOAL events appears 9 times
        static public int rankTestSuiteNext(@NotNull Set<List<String>> testSuite) {

            List<String> newTestSuite = new ArrayList<>();

            for (var test : testSuite) {
                List<String> eventList = test.stream().collect(Collectors.toList());
                //2-ways
                for (int x = 0; x < eventList.size()-1; x++) {
                    newTestSuite.add("(" + eventList.get(x) + "," + eventList.get(x+1)+")");
                }

            }
            Map<String, Long> hm = newTestSuite.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
            return hm.size();
        }

        //rankTestSuiteOld - original ranking test suite - find the suite that at least each GOAL events appears 9 times
        static public int rankTestSuiteKuhn(@NotNull Set<List<String>> testSuite) {

            List<String> newTestSuite = new ArrayList<>();

            for (var test : testSuite) {
                List<String> eventList = test.stream().collect(Collectors.toList());

                for (int x=0; x<eventList.size(); x++) {
                    for (int y=0; y<eventList.size(); y++) {
                            //2-ways
                            newTestSuite.add("(" + eventList.get(x) + "," + eventList.get(y)+ ")");
                    }
                }
            }
            Map<String, Long> hm = newTestSuite.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            return hm.size();
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


    }
}

