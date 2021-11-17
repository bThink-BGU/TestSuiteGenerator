package il.ac.bgu.cs.bp.bpjs.suitegen;

import io.jenetics.internal.collection.Array;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class OptimalOptimizer implements OptimizerInterface {
    private final Statistics statistics;
    private final int NUM_OF_ITERATIONS;


    static Stack<Integer> stack = new Stack();
    static List<Object[]> allSuites = new ArrayList<Object[]>();

    public OptimalOptimizer(int NUM_OF_ITERATIONS, Statistics statistics) {
        this.NUM_OF_ITERATIONS = NUM_OF_ITERATIONS;
        this.statistics = statistics;
    }

    public OptimalOptimizer(int NUM_OF_ITERATIONS) {
        this.NUM_OF_ITERATIONS = NUM_OF_ITERATIONS;
        this.statistics = null;
    }


    public @NotNull
    Set<List<String>> optimize(Set<List<String>> sample, int SUITE_SIZE, @NotNull Function<Set<List<String>>, Integer> rankingFunction) {
        var list = new ArrayList<>(sample);

//        System.out.println("list-"+list.size());
        var bestSuite = new HashSet<List<String>>();
        var bestRank = 0;

        loop(0, 0, SUITE_SIZE, sample.size());
        for (Object[] suiteNum: allSuites ) {
//            System.out.println("TestSuit - " + (int) suiteNum[0] + " " + (int) suiteNum[1] +" "+(int) suiteNum[2] + " " + (int) suiteNum[3] + " " + (int) suiteNum[4]);
//            System.out.print(".");

            var candidateSuite = new HashSet<List<String>>();
            // Optimal operator is searching the highest value of each sublist of 3 test cases suite
            for (Object i: suiteNum) {
//                            System.out.println("[" + i + "] "+list.get((int) i));
                candidateSuite.add(list.get((int) i));
            }

            var candidateRank = rankingFunction.apply(candidateSuite);

            if (candidateRank > bestRank) {
                bestSuite = candidateSuite;
                bestRank = candidateRank;
            }


            if (statistics != null)
                statistics.average += candidateRank;
        }

        if (statistics != null)
            statistics.average /= NUM_OF_ITERATIONS;

        return bestSuite;
    }

    private static void loop(int start, int level, int max_level, int size) {

        if (level >= max_level) {
//            System.out.println("in return-"+ stack.toString());
            allSuites.add((stack.toArray()));
            return;
        }

        for (int i = start; i < size; i++) {
            stack.push(i);
            loop(i+1, level + 1, max_level, size);
            stack.pop();

        }
    }

    static class Statistics {
        public double average = 0.0;
    }

}
