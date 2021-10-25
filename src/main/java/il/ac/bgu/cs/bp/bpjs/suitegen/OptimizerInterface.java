package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface OptimizerInterface {
    Set<List<String>> optimize(Set<List<String>> sample, int SUITE_SIZE, Function<Set<List<String>>, Integer> rankingFunction);
}
