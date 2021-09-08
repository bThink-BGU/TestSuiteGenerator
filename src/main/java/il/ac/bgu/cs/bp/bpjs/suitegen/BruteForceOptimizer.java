package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class BruteForceOptimizer implements OptimizerInterface {
    private final Statistics statistics;
    private final int NUM_OF_ITERATIONS;

    public BruteForceOptimizer(int NUM_OF_ITERATIONS, Statistics statistics) {
        this.NUM_OF_ITERATIONS = NUM_OF_ITERATIONS;
        this.statistics = statistics;
    }

    public BruteForceOptimizer(int NUM_OF_ITERATIONS) {
        this.NUM_OF_ITERATIONS = NUM_OF_ITERATIONS;
        this.statistics = null;
    }


    public @NotNull
    Set<List<BEvent>> optimize(Set<List<BEvent>> sample, int SUITE_SIZE, @NotNull Function<Set<List<BEvent>>, Integer> rankingFunction) {
        var list = new ArrayList<>(sample);

        var bestSuite = new HashSet<>(list.subList(0, SUITE_SIZE));
        var bestRank = rankingFunction.apply(bestSuite);

        for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
            Collections.shuffle(list);
            var candidateSuite = new HashSet<>(list.subList(0, SUITE_SIZE));
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

    static class Statistics {
        public double average = 0.0;
    }

}
