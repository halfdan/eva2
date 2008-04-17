package eva2.server.go.operators.paretofrontmetrics;

import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;

/** Interface for general Pareto front methods.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2005
 * Time: 14:27:08
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceParetoFrontMetric {

    /** This method allows you to get a perfect clone
     * @return the clone
     */
    public Object clone();

    /** This method gives a metirc how to evaluate
     * an achieved Pareto-Front
     */
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem);
}
