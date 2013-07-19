package eva2.optimization.operator.distancemetric;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.mutation.MutateESGlobal;

/** This method includes the sigma as distance element.
 *  This can be used to make the CBN-EA self-adaptive
 * at least on real-valued search spaces together
 * with the correct mutation operator. I guess this
 * could be a paper, but I'm so lazy right now.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.04.2004
 * Time: 12:48:26
 * To change this template use File | Settings | File Templates.
 */
public class SigmaSingleMetricGlobalMutation implements InterfaceDistanceMetric, java.io.Serializable {

    public SigmaSingleMetricGlobalMutation() {
    }

    public SigmaSingleMetricGlobalMutation(SigmaSingleMetricGlobalMutation a) {
    }

    @Override
    public Object clone() {
        return (Object) new SigmaSingleMetricGlobalMutation(this);
    }

    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    @Override
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double[]    dIndy1, dIndy2;
        double[][]  range1, range2;
        double      result = 0;

        if ((indy1.getMutationOperator() instanceof MutateESGlobal) && (indy2.getMutationOperator() instanceof MutateESGlobal)) {
            MutateESGlobal mutator1, mutator2;
            mutator1 = (MutateESGlobal) indy1.getMutationOperator();
            mutator2 = (MutateESGlobal) indy2.getMutationOperator();
            // Mutation is acting on a normalized search space...
            dIndy1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
            range1 = ((InterfaceDataTypeDouble) indy1).getDoubleRange();
            dIndy2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
            range2 = ((InterfaceDataTypeDouble) indy2).getDoubleRange();
            for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
                result += Math.pow(((dIndy1[i] - range1[i][0])/(range1[i][1] - range1[i][0])) - ((dIndy2[i] - range2[i][0])/(range2[i][1] - range2[i][0])), 2);
            }
            // this is the normalized distance
            result = Math.sqrt(result);
            if (result < Math.max(mutator1.getMutationStepSize(),mutator2.getMutationStepSize())) {
                return 0.0;
            }
            return 1.0;
        } else {
            return 1.0;
        }
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is an experimental method for individuals using global ES mutation.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Phenotype Metric Including MutateESGlobal";
    }
}
