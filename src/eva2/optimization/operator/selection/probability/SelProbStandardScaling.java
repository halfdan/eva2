package eva2.optimization.operator.selection.probability;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/** A simple sum with a scaling factor.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 16:57:51
 * To change this template use File | Settings | File Templates.
 */
public class SelProbStandardScaling extends AbstractSelProb implements java.io.Serializable {

    private double      m_Q = 0;

    public SelProbStandardScaling() {
    }

    public SelProbStandardScaling(double q) {
    	m_Q = q;
    }
    
    public SelProbStandardScaling(SelProbStandardScaling a) {
        this.m_Q    = a.m_Q;
    }

    @Override
    public Object clone() {
        return (Object) new SelProbStandardScaling(this);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data         The input data as double[][].
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double      sum = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, delta;
        double[]    result = new double[data.length];

        if (obeyConst) {
            // first check if anyone holds the constraints
            boolean isFeasible = false;
            int k=0;
            while ((k < population.size()) && !isFeasible) {
                if (!((AbstractEAIndividual)population.get(k)).violatesConstraint()) {
                    isFeasible = true;
                }
                k++;
            }
            if (isFeasible) {
                // at least one is feasible
                // iterating over the fitness cases
                for (int x = 0; x < data[0].length; x++) {
                    sum = 0;
                    min = Double.POSITIVE_INFINITY;
                    // first find the worst, to be able to default
                    double worst = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i][x] > worst) {
                            worst = data[i][x];
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (!((AbstractEAIndividual)population.get(i)).violatesConstraint()) {
                            result[i] = -data[i][x];
                        }
                        else {
                            result[i] = -worst;
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (result[i] < min) {
                            min = result[i];
                        }
                        if (result[i] > max) {
                            max = result[i];
                        }
                    }
                    if (max != min) {
                        delta = max -min;
                    }
                    else {
                        delta = 1;
                    }

                    for (int i = 0; i < data.length; i++) {
                        result[i] = ((result[i] - min)/delta) + this.m_Q;
                        sum += result[i];
                    }

                    for (int i = 0; i < population.size(); i++) {
                        ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i]/sum);
                    }
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                sum = 0;
                min = Double.POSITIVE_INFINITY;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -((AbstractEAIndividual)population.get(i)).getConstraintViolation();
                }
                for (int i = 0; i < data.length; i++) {
                    if (result[i] < min) {
                        min = result[i];
                    }
                    if (result[i] > max) {
                        max = result[i];
                    }
                }
                if (max != min) {
                    delta = max - min;
                }
                else {
                    delta = 1;
                }

                for (int i = 0; i < data.length; i++) {
                    result[i] = ((result[i] - min)/delta) + this.m_Q;
                    sum += result[i];
                }
                for (int i = 0; i < population.size(); i++) {
                    double[] tmpD = new double[1];
                    tmpD[0] = result[i]/sum;
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(tmpD);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                min = Double.POSITIVE_INFINITY;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -data[i][x];
                }
                for (int i = 0; i < data.length; i++) {
                    if (result[i] < min) {
                        min = result[i];
                    }
                    if (result[i] > max) {
                        max = result[i];
                    }
                }
                if (max != min) {
                    delta = max -min;
                }
                else {
                    delta = 1;
                }

                for (int i = 0; i < data.length; i++) {
                    result[i] = ((result[i] - min)/delta) + this.m_Q;
                    sum += result[i];
                }

                for (int i = 0; i < population.size(); i++) {
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i]/sum);
                }
            }
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a standard normation method with scaling.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Scaled Normation";
    }

    /** This method will allow you to set and get the Q Parameter
     * @return The new selection pressure q.
     */
    public double getQ() {
        return this.m_Q;
    }
    public void setQ(double b){
        this.m_Q = Math.abs(b);
    }
    public String qTipText() {
        return "The selection pressure. The bigger q, the higher the selection pressure.";
    }
}