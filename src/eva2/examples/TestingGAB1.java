package eva2.examples;

import eva2.OptimizerFactory;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.selection.SelectXProbRouletteWheel;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.strategies.GeneticAlgorithm;

import java.util.BitSet;

public class TestingGAB1 {
    public static void main(String[] args) {
        B1Problem b1 = new B1Problem();
        BitSet sol;
        // default go-parameter instance with a GA
        OptimizationParameters gaParams = OptimizerFactory.standardGA(b1);
        // add an evaluation terminator
        gaParams.setTerminator(new EvaluationTerminator(1000));
        // set a specific random seed
        gaParams.setSeed(2342);

        // access the GA
        GeneticAlgorithm ga = (GeneticAlgorithm) gaParams.getOptimizer();
        ga.setElitism(false);
        ga.setParentSelection(new SelectXProbRouletteWheel()); // roulette wheel selection
        ga.setPopulation(new Population(150));    // population size 150

        // run optimization and print intermediate results to a file with given prefix
        sol = OptimizerFactory.optimizeToBinary(gaParams, "ga-opt-results");
        System.out.println(OptimizerFactory.terminatedBecause() + "\nFound solution: ");
        for (int i = 0; i < b1.getProblemDimension(); i++) {
            System.out.print(sol.get(i) + " ");
        }
        System.out.println();
    }

    ;
}

