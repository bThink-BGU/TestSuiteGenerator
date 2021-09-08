package il.ac.bgu.cs.bp.bpjs.suitegen;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import io.jenetics.EnumGene;
import io.jenetics.Mutator;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class GeneticOptimizer implements OptimizerInterface {
    private final long MAXIMAL_PHENOTYPE_AGE;
    private final double CROSSOVER_PROBABILITY;
    private final double MUTATION_PROBABILITY;
    private final int NUMBER_OF_GENERATIONS;


    public GeneticOptimizer(double CROSSOVER_PROBABILITY, double MUTATION_PROBABILITY, int NUMBER_OF_GENERATIONS, long MAXIMAL_PHENOTYPE_AGE) {
        this.MAXIMAL_PHENOTYPE_AGE = MAXIMAL_PHENOTYPE_AGE;
        this.CROSSOVER_PROBABILITY = CROSSOVER_PROBABILITY;
        this.MUTATION_PROBABILITY = MUTATION_PROBABILITY;
        this.NUMBER_OF_GENERATIONS = NUMBER_OF_GENERATIONS;
    }

    public Set<List<BEvent>> optimize(Set<List<BEvent>> samples, int suiteSize, Function<Set<List<BEvent>>, Integer> rankingFunction) {
        final GeneticOptimizerHelper problem = new GeneticOptimizerHelper(ISeq.of(samples), suiteSize, rankingFunction);

        var engine = Engine.builder(problem)
                .maximizing()
                .maximalPhenotypeAge(MAXIMAL_PHENOTYPE_AGE)
                .alterers(
                        new PartiallyMatchedCrossover<>(CROSSOVER_PROBABILITY),
                        new Mutator<>(MUTATION_PROBABILITY))
                .build();

        var result = engine.stream()
                .limit(Limits.bySteadyFitness(NUMBER_OF_GENERATIONS))
                .collect(EvolutionResult.toBestPhenotype());

        return result.genotype().chromosome().stream()
                .map(EnumGene::allele)
                .collect(Collectors.toSet());
    }

    static class GeneticOptimizerHelper
            implements Problem<ISeq<List<BEvent>>, EnumGene<List<BEvent>>, Integer> {

        private final ISeq<List<BEvent>> universe;
        private final int subsetSize;
        private final Function<Set<List<BEvent>>, Integer> rankingFunction;

        public GeneticOptimizerHelper(final ISeq<List<BEvent>> universe, final int subsetSize, Function<Set<List<BEvent>>, Integer> rankingFunction) {
            this.universe = requireNonNull(universe);
            this.subsetSize = subsetSize;
            this.rankingFunction = rankingFunction;
        }

        @Override
        public Function<ISeq<List<BEvent>>, Integer> fitness() {
            return suite -> rankingFunction.apply(new HashSet<>(suite.asList()));
        }

        @Override
        public Codec<ISeq<List<BEvent>>, EnumGene<List<BEvent>>> codec() {
            return Codecs.ofSubSet(universe, subsetSize);
        }
    }
}
