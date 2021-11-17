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
    Set<List<String>> genSuite(Set<List<String>> sample, int SUITE_SIZE, @NotNull Function<Set<List<String>>, Integer> rankingFunction, double rankMax) {

        var list = new ArrayList<>(sample);
        Collections.shuffle(list);
        var candidateSuite = new HashSet<>(list.subList(0, SUITE_SIZE));

        for (int i = 0; i < NUM_OF_ITERATIONS; i++) {
            Collections.shuffle(list);
            candidateSuite = new HashSet<>(list.subList(0, SUITE_SIZE));
            var candidateRank = rankingFunction.apply(candidateSuite);

//            System.out.println("cand-"+candidateRank);
            if (candidateRank > rankMax) {
//                System.out.println("indx-" + i + " candidateRank-" + candidateRank);
                return candidateSuite;
            }
        }
        return  candidateSuite;
    }


}
