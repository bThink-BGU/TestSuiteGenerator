package il.ac.bgu.cs.bp.bpjs.suitegen;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class OptimalOptimizer implements OptimizerInterface {
    private final Statistics statistics;
    private final int NUM_OF_ITERATIONS;

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

        // Optimal operator is searching the highest value of each sublist of 3 test cases suite
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                for (int k = j + 1; k < list.size(); k++) {

                    System.out.println("["+i+"]"+"["+j+"]"+"["+k+"]");
                    var candidateSuite = new HashSet<>(List.of(list.get(i), list.get(j), list.get(k)));
                    var candidateRank = rankingFunction.apply(candidateSuite);

                    if (candidateRank > bestRank) {
                        bestSuite = candidateSuite;
                        bestRank = candidateRank;
                    }

                    if (statistics != null)
                        statistics.average += candidateRank;

                }
            }
        }

        if (statistics != null)
            statistics.average /= NUM_OF_ITERATIONS;

        return bestSuite;
    }

    static class Statistics {
        public double average = 0.0;
    }

}
