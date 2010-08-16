package eva2.server.go.operators.terminators;

import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.IndividualWeightedFitnessComparator;
import eva2.server.go.populations.Population;

/**
 * Terminate if a score based on the archive of the population converges.
 * Note that this only works if the archive is filled with sensible data.
 * 
 * @author mkron
 *
 */
public class PopulationArchiveTerminator extends PopulationMeasureTerminator {
	IndividualWeightedFitnessComparator wfComp = new IndividualWeightedFitnessComparator(new double[]{1.});

	public static String globalInfo() {
		return "Stop if a linear recombination of the best fitness stagnates for a certain period.";
	}
	
	@Override
	protected double calcInitialMeasure(PopulationInterface pop) {
		Population archive = ((Population)pop).getArchive();
		if (archive==null || (archive.size()<1)) return Double.MAX_VALUE;
		else return wfComp.calcScore(archive.getBestEAIndividual(wfComp));
	}

	@Override
	protected double calcPopulationMeasure(PopulationInterface pop) {
		Population archive = ((Population)pop).getArchive();
		if (archive==null || (archive.size()<1)) return Double.MAX_VALUE;
		else return wfComp.calcScore(archive.getBestEAIndividual(wfComp));
	}

	@Override
	protected String getMeasureName() {
		return "Archive Weighted Score";
	}

	public double[] getFitWeights() {
		return wfComp.getFitWeights();
	}
	public void setFitWeights(double[] fWeights) {
		wfComp.setFitWeights(fWeights);
	}
	public String fitWeightsTipText() {
		return wfComp.fitWeightsTipText();
	}
}
