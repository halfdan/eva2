package eva2.server.go.operators.terminators;

import eva2.gui.BeanInspector;
import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;

public class PhenotypeConvergenceTerminator extends FitnessConvergenceTerminator implements InterfaceTerminator {
	AbstractEAIndividual oldIndy = null;
	double oldPhenNorm = 0;
	
	public PhenotypeConvergenceTerminator() {
		super();
	}
	
	public PhenotypeConvergenceTerminator(double thresh, int stagnTime, boolean bFitCallBased, boolean bAbsolute) {
		super(thresh, stagnTime, bFitCallBased, bAbsolute);
	}
	/**
	 * Return true if |oldPhen - curPhen| < |oldPhen| * thresh (relative case)
	 * and if |oldFit - curFit| < thresh (absolute case).
	 * 
	 * @param curFit
	 * @return
	 */
	protected boolean isStillConverged(IndividualInterface indy) {
		double dist = pMetric.distance(oldIndy, (AbstractEAIndividual)indy);
		boolean ret;
		if (getConvergenceCondition().isSelectedString("Relative")) {
			ret = (dist < (oldPhenNorm * convThresh));
		} else {
			ret = (dist < convThresh);
		}
		if (TRACE) System.out.println("isStillConverged returns " + ret + ", dist " + dist + ", old indy " + BeanInspector.toString(oldIndy) + ", cur indy" + BeanInspector.toString(indy));
		return ret;
	}
	
	public String terminatedBecause(PopulationInterface pop) {
		if (isTerminated(pop)) {
			return getTerminationMessage("Phenotype converged");		
		} else return "Not yet terminated.";
	}

	
	protected void saveState(PopulationInterface Pop) {
		super.saveState(Pop);
		oldIndy = (AbstractEAIndividual)((AbstractEAIndividual)Pop.getBestIndividual()).clone();
		oldPhenNorm = PhenotypeMetric.norm(oldIndy);
	}
}
