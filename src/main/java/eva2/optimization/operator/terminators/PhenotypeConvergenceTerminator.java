package eva2.optimization.operator.terminators;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

@Description("Terminate if the best individual of the current population moved less than a threshold within phenotypic space.")
public class PhenotypeConvergenceTerminator extends PopulationMeasureTerminator implements InterfaceTerminator {
    AbstractEAIndividual oldIndy = null;
    private PhenotypeMetric pMetric = null;

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
    }

    @Override
    public void initialize(InterfaceOptimizationProblem prob) {
        super.initialize(prob);
        oldIndy = null;
    }

    @Override
    protected double calculateInitialMeasure(PopulationInterface pop) {
        oldIndy = (AbstractEAIndividual) ((AbstractEAIndividual) pop.getBestIndividual()).clone();
        return Double.MAX_VALUE;
    }

    @Override
    protected double calculatePopulationMeasure(PopulationInterface pop) {
        return pMetric.distance(oldIndy, (AbstractEAIndividual) pop.getBestIndividual());
    }

    @Override
    protected void saveState(PopulationInterface Pop) {
        super.saveState(Pop);
        oldIndy = (AbstractEAIndividual) ((AbstractEAIndividual) Pop.getBestIndividual()).clone();
    }

    @Override
    protected String getMeasureName() {
        return "Phenotype";
    }
}
