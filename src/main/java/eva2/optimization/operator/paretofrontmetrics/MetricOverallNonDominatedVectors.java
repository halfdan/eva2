package eva2.optimization.operator.paretofrontmetrics;

import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * Overall Non-Dom. Vector Generation calculates simply the number of
 * non-dominated solutions in the current solution set.
 */
@Description("Calculating the number of non dominated individuals.")
public class MetricOverallNonDominatedVectors implements InterfaceParetoFrontMetric, java.io.Serializable {

    private ArchivingAllDominating dominating = new ArchivingAllDominating();

    public MetricOverallNonDominatedVectors() {

    }

    public MetricOverallNonDominatedVectors(MetricOverallNonDominatedVectors b) {
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new MetricOverallNonDominatedVectors(this);
    }

    /**
     * This method allows you to initialize the metric loading data etc
     */
    public void init() {

    }

    /**
     * This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        Population tmpPop = new Population();
        Population tmpPPO = new Population();
        tmpPPO.addPopulation(pop);
        if (pop.getArchive() != null) {
            tmpPPO.addPopulation(pop.getArchive());
        }
        for (int i = 0; i < tmpPPO.size(); i++) {
            if (this.dominating.isDominant(tmpPPO.get(i), tmpPop)) {
                this.dominating.addIndividualToArchive(tmpPPO.get(i), tmpPop);
            }
        }
        return tmpPop.size();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Overall Non-Dominated Vectors";
    }
}