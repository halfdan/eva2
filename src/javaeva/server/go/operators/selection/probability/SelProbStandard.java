package javaeva.server.go.operators.selection.probability;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.populations.Population;

/** A simple sum to calculate the selection probability.
 * 
 * p(i is selected) = exp(-fitness(i))/sum_j(exp(-fitness(j)))
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 16:54:48
 * To change this template use File | Settings | File Templates.
 */
public class SelProbStandard extends AbstractSelProb implements java.io.Serializable {

    public SelProbStandard() {
    }

    public SelProbStandard(SelProbStandard a) {
    }

    public Object clone() {
        return (Object) new SelProbStandard(this);
    }

    /** This method computes the selection probability for each individual
     *  in the population. Note: Summed over the complete population the selection
     *  probability sums up to one. Keep in mind that fitness is always to be
     *  minimizied! Small values for data => big values for selectionprob.
     * @param population    The population to compute.
     * @param data          The input data as double[][].
     */
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double      sum = 0;
        double[]    result = new double[data.length];

        if (obeyConst) {
            // first check if anyone holds the constraints
            boolean isFeasible = false;
            for (int i = 0; i < population.size(); i++) {
                if (!((AbstractEAIndividual)population.get(i)).violatesConstraint()) isFeasible = true;
            }
            if (isFeasible) {
                // at least one is feasible
                // iterating over the fitness cases
                for (int x = 0; x < data[0].length; x++) {
                    sum = 0;
                    // iterating over the individuals
                    for (int i = 0; i < data.length; i++) {
                        if (!((AbstractEAIndividual)population.get(i)).violatesConstraint())
                            result[i] = Math.exp(-data[i][x]);
                        else
                            result[i] = 0;
                    }
                    for (int i = 0; i < data.length; i++)
                        sum += result[i];
                    for (int i = 0; i < population.size(); i++)
                        ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i]/sum);
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                sum = 0;
                // iterating over the individuals
                for (int i = 0; i < data.length; i++)
                    result[i] = Math.exp(-((AbstractEAIndividual)population.get(i)).getConstraintViolation());
                for (int i = 0; i < data.length; i++)
                    sum += result[i];
                for (int i = 0; i < population.size(); i++) {
                    double[] tmpD = new double[1];
                    tmpD[0] = result[i]/sum;
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(tmpD);
                }
            }
        } else {
            // iterating over the fitness cases
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                // iterating over the individuals
                for (int i = 0; i < data.length; i++)
                    result[i] = Math.exp(-data[i][x]);
                for (int i = 0; i < data.length; i++)
                    sum += result[i];
                for (int i = 0; i < population.size(); i++)
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i]/sum);
            }
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a standard normation method using the exp function.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Standard Normation";
    }
}