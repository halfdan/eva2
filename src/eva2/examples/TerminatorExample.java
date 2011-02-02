package eva2.examples;
import eva2.OptimizerFactory;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.operators.terminators.*;
import eva2.server.go.operators.terminators.PopulationMeasureTerminator.*;

public class TerminatorExample {
	public static void main(String[] args) {
		F1Problem f1 = new F1Problem(); 
		double[] sol;
		// A combined terminator for fitness and phenotype convergence
		CombinedTerminator convT = new CombinedTerminator(
				// fitness-based stagnation period, absolute threshold, consider stagnation 
				// in both direction (per dim.) or w.r.t. minimization only
				new FitnessConvergenceTerminator(0.0001, 1000, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease),
				new PhenotypeConvergenceTerminator(0.0001, 1000, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.bidirectional),
				CombinedTerminator.AND);
		// Adding an evaluation terminator with OR to the convergence criterion
		OptimizerFactory.setTerminator(new CombinedTerminator(
				new EvaluationTerminator(20000),
				convT,
				CombinedTerminator.OR));
		sol = OptimizerFactory.optimizeToDouble(OptimizerFactory.PSO, f1, null);
		System.out.println(OptimizerFactory.lastEvalsPerformed() 
				+ " evals performed. "
				+ OptimizerFactory.terminatedBecause()
				+ " Found solution: ");
		for (int i=0; i<f1.getProblemDimension(); i++) System.out.print(sol[i] + " ");
		System.out.println();
	};
}