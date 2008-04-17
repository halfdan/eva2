package eva2.server.go.operators.distancemetric;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;

/** Objective space metric suited for multi-objective optimization.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.08.2003
 * Time: 14:39:47
 * To change this template use Options | File Templates.
 */
public class ObjectiveSpaceMetric implements InterfaceDistanceMetric, java.io.Serializable {

    public ObjectiveSpaceMetric() {
    }

    public ObjectiveSpaceMetric(ObjectiveSpaceMetric a) {
    }

    public Object clone() {
        return (Object) new ObjectiveSpaceMetric(this);
    }

    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double[]    dIndy1, dIndy2;
        double      result = 0;

        dIndy1 = indy1.getFitness();
        dIndy2 = indy2.getFitness();

        for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
            result += Math.pow((dIndy1[i] - dIndy2[i]), 2);
        }
        return Math.sqrt(result);
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a objective space based metric.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Objective Space Metric";
    }
}
