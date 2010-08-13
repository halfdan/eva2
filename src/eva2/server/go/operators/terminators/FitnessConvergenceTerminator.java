package eva2.server.go.operators.terminators;

import java.io.Serializable;

import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.tools.math.Mathematics;


/**
 * Fitness convergence is measured as the norm of the best fitness of the population.
 * The PopulationMeasureTerminator is then used with this measure.
 * 
 * @see PopulationMeasureTerminator
 */
public class FitnessConvergenceTerminator extends PopulationMeasureTerminator 
implements InterfaceTerminator, Serializable {
	
	public FitnessConvergenceTerminator() {
		super();
	}

	public FitnessConvergenceTerminator(double thresh, int stagnPeriod, boolean bFitCallBased, boolean bAbsolute, boolean bImprovement) {
		super(thresh, stagnPeriod, bFitCallBased, ChangeTypeEnum.absoluteChange, bImprovement);
	}

	public FitnessConvergenceTerminator(FitnessConvergenceTerminator other) {
		super(other);
	}
	
	public static String globalInfo() {
		return "Stop if a fitness convergence criterion has been met.";
	}

	@Override
	protected double calcInitialMeasure(PopulationInterface pop) {
		return Mathematics.norm(pop.getBestFitness());
	}

	@Override
	protected double calcPopulationMeasure(PopulationInterface pop) {
//		if (oldFit==null) return Double.MAX_VALUE;
//		return EuclideanMetric.euclideanDistance(oldFit, pop.getBestFitness());
		return Mathematics.norm(pop.getBestFitness());
	}

	@Override
	protected String getMeasureName() {
		return "Fitness";
	}
}