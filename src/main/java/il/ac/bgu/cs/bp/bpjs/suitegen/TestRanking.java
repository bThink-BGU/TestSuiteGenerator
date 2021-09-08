package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import java.util.List;
import java.util.Set;

public interface TestRanking {
    int rankTestSuite(Set<List<BEvent>> testSuite);
}
