package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class KhunCriterion {

    private final int NUM_OF_ITERATIONS;

    public KhunCriterion(int NUM_OF_ITERATIONS) {
        this.NUM_OF_ITERATIONS = NUM_OF_ITERATIONS;
    }


    public @NotNull
    Set<List<BEvent>> candidateSuite(Set<List<BEvent>> sample, int SUITE_SIZE, @NotNull Function<Set<List<BEvent>>, Integer> rankingFunction, double rankMax) {

        var list = new ArrayList<>(sample);
        System.out.println("list-"+list.size());

        for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
            Collections.shuffle(list);
            var candidateSuite = new HashSet<>(list.subList(0, SUITE_SIZE));
            var candidateRank = rankingFunction.apply(candidateSuite);

            System.out.println("cand-"+candidateRank);
            if (candidateRank > rankMax) {
                System.out.println("indx-" + i + " candidateRank-" + candidateRank);
                return candidateSuite;
            }
        }
        return null;
    }


}
