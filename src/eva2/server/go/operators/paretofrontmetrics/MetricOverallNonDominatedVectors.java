package eva2.server.go.operators.paretofrontmetrics;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingAllDominating;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;

/**  Overall Non-Dom. Vector Generation calculates simply the number of
 * non-dominated solutions in the current soltuion set.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2005
 * Time: 14:27:44
 * To change this template use File | Settings | File Templates.
 */
public class MetricOverallNonDominatedVectors implements InterfaceParetoFrontMetric, java.io.Serializable {

    private ArchivingAllDominating  m_Dom = new ArchivingAllDominating();

    public MetricOverallNonDominatedVectors() {

    }

    public MetricOverallNonDominatedVectors(MetricOverallNonDominatedVectors b) {
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new MetricOverallNonDominatedVectors(this);
    }

    /** This method allows you to init the metric loading data etc
     *
     */
    public void init() {

    }

    /** This method gives a metric how to evaluate
     * an achieved Pareto-Front
     */
    @Override
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem) {
        Population tmpPop = new Population();
        Population tmpPPO = new Population();
        tmpPPO.addPopulation(pop);
        if (pop.getArchive() != null) tmpPPO.addPopulation(pop.getArchive());
        for (int i = 0; i < tmpPPO.size(); i++) {
            if (this.m_Dom.isDominant((AbstractEAIndividual)tmpPPO.get(i), tmpPop)) {
                this.m_Dom.addIndividualToArchive((AbstractEAIndividual)tmpPPO.get(i), tmpPop);
            }
        }
        return tmpPop.size();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Overall Non-Dominated Vectors";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Calculating the number of non dominated individuals.";
    }
}