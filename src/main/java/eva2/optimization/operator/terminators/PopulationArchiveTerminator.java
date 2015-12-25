package eva2.optimization.operator.terminators;

import eva2.optimization.individuals.IndividualWeightedFitnessComparator;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

/**
 * Terminate if a score based on the archive of the population converges.
 * Note that this only works if the archive is filled with sensible data.
 */
@Description("Stop if a linear recombination of the best fitness stagnates for a certain period.")
public class PopulationArchiveTerminator extends PopulationMeasureTerminator {
    IndividualWeightedFitnessComparator wfComp = new IndividualWeightedFitnessComparator(new double[]{1.});

    @Override
    protected double calculateInitialMeasure(PopulationInterface pop) {
        Population archive = ((Population) pop).getArchive();
        if (archive == null || (archive.size() < 1)) {
            return Double.MAX_VALUE;
        } else {
            return wfComp.calculateScore(archive.getBestEAIndividual(wfComp));
        }
    }

    @Override
    protected double calculatePopulationMeasure(PopulationInterface pop) {
        Population archive = ((Population) pop).getArchive();
        if (archive == null || (archive.size() < 1)) {
            return Double.MAX_VALUE;
        } else {
            return wfComp.calculateScore(archive.getBestEAIndividual(wfComp));
        }
    }

    @Override
    protected String getMeasureName() {
        return "Archive Weighted Score";
    }

    public double[] getFitWeights() {
        return wfComp.getFitWeights();
    }

    @Parameter(description = "Weights of the fitness values in the linear combination")
    public void setFitWeights(double[] fWeights) {
        wfComp.setFitWeights(fWeights);
    }
}
