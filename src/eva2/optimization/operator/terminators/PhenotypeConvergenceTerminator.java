package eva2.optimization.operator.terminators;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceOptimizationProblem;

public class PhenotypeConvergenceTerminator extends PopulationMeasureTerminator implements InterfaceTerminator {
    AbstractEAIndividual oldIndy = null;
    private PhenotypeMetric pMetric = null;
//	double oldPhenNorm = 0;

    public PhenotypeConvergenceTerminator() {
        super();
        pMetric = new PhenotypeMetric();
    }

    public PhenotypeConvergenceTerminator(double thresh, int stagnTime, StagnationTypeEnum stagType, ChangeTypeEnum changeType, DirectionTypeEnum dirType) {
        super(thresh, stagnTime, stagType, changeType, dirType);
        pMetric = new PhenotypeMetric();
    }

    public PhenotypeConvergenceTerminator(PhenotypeConvergenceTerminator o) {
        super(o);
        oldIndy = (AbstractEAIndividual) o.oldIndy.clone();
        pMetric = (PhenotypeMetric) o.pMetric.clone();
//		oldPhenNorm = o.oldPhenNorm;
    }

    @Override
    public void init(InterfaceOptimizationProblem prob) {
        super.init(prob);
//		oldPhenNorm  = 0;
        oldIndy = null;
    }

    @Override
    protected double calcInitialMeasure(PopulationInterface pop) {
        oldIndy = (AbstractEAIndividual) ((AbstractEAIndividual) pop.getBestIndividual()).clone();
//		oldPhenNorm = PhenotypeMetric.norm(oldIndy);
        return Double.MAX_VALUE;
    }

    @Override
    protected double calcPopulationMeasure(PopulationInterface pop) {
        return pMetric.distance(oldIndy, (AbstractEAIndividual) pop.getBestIndividual());
    }

    @Override
    protected void saveState(PopulationInterface Pop) {
        super.saveState(Pop);
        oldIndy = (AbstractEAIndividual) ((AbstractEAIndividual) Pop.getBestIndividual()).clone();
//		oldPhenNorm = PhenotypeMetric.norm(oldIndy);
    }

    @Override
    protected String getMeasureName() {
        return "Phenotype";
    }

    public static String globalInfo() {
        return "Terminate if the best individual of the current population moved less than a threshold within phenotypic space.";
    }
}
