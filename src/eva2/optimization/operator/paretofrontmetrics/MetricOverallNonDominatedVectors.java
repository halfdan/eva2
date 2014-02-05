package eva2.optimization.operator.paretofrontmetrics;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;

/**
 * Overall Non-Dom. Vector Generation calculates simply the number of
 * non-dominated solutions in the current soltuion set.
 */
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
        return (Object) new MetricOverallNonDominatedVectors(this);
    }

    /**
     * This method allows you to init the metric loading data etc
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
            if (this.dominating.isDominant((AbstractEAIndividual) tmpPPO.get(i), tmpPop)) {
                this.dominating.addIndividualToArchive((AbstractEAIndividual) tmpPPO.get(i), tmpPop);
            }
        }
        return tmpPop.size();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Overall Non-Dominated Vectors";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Calculating the number of non dominated individuals.";
    }
}