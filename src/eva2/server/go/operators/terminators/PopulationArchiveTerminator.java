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
	IndividualWeightedFitnessComparator wfComp = new IndividualWeightedFitnessComparator(null);
	
//	private boolean isStillConverged(PopulationInterface pop) {
//		Population archive = ((Population)pop).getArchive();
//		if (archive==null || (archive.size()<1)) {
//			System.err.println("Error, population had no archive in " + this.getClass());
//			return false;
//		} else {
//			double bestScore = Population.getScore(archive.getEAIndividual(0), fitWeights);
//			for (int i=1; i<archive.size(); i++) {
//				double tmpScore =  Population.getScore(archive.getEAIndividual(i), fitWeights);
//				if (tmpScore<bestScore) bestScore=tmpScore;
//			}
//			if (bestScore>=oldScore) return true;
//			else {
//				oldScore=bestScore;
//				return false;
//			}
//		}
//	}
	
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
