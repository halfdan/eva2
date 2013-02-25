package eva2.examples;

import eva2.OptimizerFactory;
import eva2.optimization.problems.F1Problem;

public class TestingF1PSO {

    public static void main(String[] args) {
        F1Problem f1 = new F1Problem();
        // start a PSO with a runtime of 50000 evaluations
        OptimizerFactory.setEvaluationTerminator(50000);
        double[] sol = OptimizerFactory.optimizeToDouble(OptimizerFactory.PSO, f1, null);
        System.out.println(OptimizerFactory.terminatedBecause() + "\nFound solution: ");
        for (int i = 0; i < f1.getProblemDimension(); i++) {
            System.out.print(sol[i] + " ");
        }
        System.out.println();
    }
}