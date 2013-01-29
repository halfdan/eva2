package eva2.server.go.operators.selection.probability;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** Ranking for calculating the selection probability.
 * This truly scaling invariant.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 16:58:44
 * To change this template use File | Settings | File Templates.
 */
public class SelProbRanking extends AbstractSelProb implements java.io.Serializable {

    public SelProbRanking() {
    }

    public SelProbRanking(SelProbRanking a) {
    }

    @Override
    public Object clone() {
        return (Object) new SelProbRanking(this);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data          The input as double[][]
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double                  sum = 0;
        double[]                result = new double[data.length];

        if (obeyConst) {
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                for (int i = 0; i < result.length; i++) result[i] = 0;
                for (int i = 0; i < data.length; i++) data[i][x] = -data[i][x];
                for (int i = 0; i < data.length; i++) {
                    for (int j = i + 1; j < data.length; j++) {
                        if (!(((AbstractEAIndividual)population.get(i)).violatesConstraint()) && (!((AbstractEAIndividual)population.get(j)).violatesConstraint())) {
                            // no one violates, therefore it is up to the data to decied
                            if (data[j][x] < data[i][x]) result[i]++;
                            else result[j]++;
                        } else {
                            // at least one violates, so the constraint violation is to decide
                            if (((AbstractEAIndividual)population.get(j)).getConstraintViolation() < ((AbstractEAIndividual)population.get(i)).getConstraintViolation()) {
                                result[j]++;
                            } else {
                                result[i]++;
                            }
                        }
                    }
                }

                for (int i = 0; i < result.length; i++) {
                    sum += result[i];
                }

                for (int i = 0; i < population.size(); i++) {
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i] /sum);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                for (int i = 0; i < result.length; i++) result[i] = 0;
                for (int i = 0; i < data.length; i++) data[i][x] = -data[i][x];
                for (int i = 0; i < data.length; i++) {
                    for (int j = i + 1; j < data.length; j++) {
                        if (data[j][x] < data[i][x]) result[i]++;
                        else result[j]++;
                    }
                }

                for (int i = 0; i < result.length; i++) {
                    sum += result[i];
                }

                for (int i = 0; i < population.size(); i++) {
                    ((AbstractEAIndividual)population.get(i)).SetSelectionProbability(x, result[i] /sum);
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
        return "This is ranking normation.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Ranking";
    }
}