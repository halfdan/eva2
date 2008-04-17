package eva2.server.go.operators.selection.probability;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** A non-linear ranking, which is difficult to tune to
 * the given optimization problem i guess.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.06.2004
 * Time: 11:57:55
 * To change this template use File | Settings | File Templates.
 */
public class SelProbNonLinearRanking extends AbstractSelProb implements java.io.Serializable {

    private double          m_C   = 0.04;

    public SelProbNonLinearRanking() {
    }

    public SelProbNonLinearRanking(SelProbNonLinearRanking a) {
        this.m_C    = a.m_C;
    }

    public Object clone() {
        return (Object) new SelProbNonLinearRanking(this);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete populaiton the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data          The input as double[][]
     */
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double                  temp, sum;
        double[]                result = new double[data.length];
        int                     tempI, size = population.size();

        if (obeyConst) {
            // first check if anyone holds the constraints
            boolean isFeasible = false;
            for (int i = 0; i < population.size(); i++) {
                if (!((AbstractEAIndividual)population.get(i)).violatesConstraint()) isFeasible = true;
            }
            if (isFeasible) {
                // at least one is feasible
                for (int x = 0; x < data[0].length; x++) {
                    // first find the worst, to be able to default
                    double worst = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i][x] > worst) worst = data[i][x];
                    }
                    int[]       rank_index = new int[data.length];
                    double[]    fitness    = new double[data.length];
                    for (int i = 0; i < result.length; i++) result[i] = 0;
                    for (int i = 0; i < data.length; i++) {
                        if (!(((AbstractEAIndividual)population.get(i)).violatesConstraint())) {
                            fitness[i]      = data[i][x];
                        } else {
                            fitness[i]      = worst;
                        }
                        rank_index[i]   = i;
                    }

                    //rank sorting
                    for (int index = 0; index < fitness.length; index++) {
                        int min = index;
                        for(int i = index; i < fitness.length; i++) {
                            if(fitness[min] > fitness[i])  min = i;
                        }
                        if(fitness[index] != fitness[min]) {
                            temp                = fitness[index];
                            fitness[index]      = fitness[min];
                            fitness[min]        = temp;
                            tempI               = rank_index[index];
                            rank_index[index]   = rank_index[min];
                            rank_index[min]     = tempI;
                        }
                    }
                    // set the selection propability
                    sum = 0;
                    for (int i = 0; i < data.length; i++) {
                        result[i] = this.m_C * Math.pow(1-this.m_C, i);
                        sum += result[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        ((AbstractEAIndividual)population.get(rank_index[i])).SetSelectionProbability(x, result[i]/sum);
                    }
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                int[]       rank_index = new int[data.length];
                double[]    fitness    = new double[data.length];
                for (int i = 0; i < result.length; i++) result[i] = 0;
                for (int i = 0; i < data.length; i++) {
                    fitness[i]      = -((AbstractEAIndividual)population.get(i)).getConstraintViolation();
                    rank_index[i]   = i;
                }

                //rank sorting
                for (int index = 0; index < fitness.length; index++) {
                    int min = index;
                    for(int i = index; i < fitness.length; i++) {
                        if(fitness[min] > fitness[i])  min = i;
                    }
                    if(fitness[index] != fitness[min]) {
                        temp                = fitness[index];
                        fitness[index]      = fitness[min];
                        fitness[min]        = temp;
                        tempI               = rank_index[index];
                        rank_index[index]   = rank_index[min];
                        rank_index[min]     = tempI;
                    }
                }
                // set the selection propability
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = this.m_C * Math.pow(1-this.m_C, i);
                    sum += result[i];
                }
                for (int i = 0; i < data.length; i++) {
                    double[] tmpD   = new double[1];
                    tmpD[0]         = result[i]/sum;
                    ((AbstractEAIndividual)population.get(rank_index[i])).SetSelectionProbability(tmpD);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                int[]       rank_index = new int[data.length];
                double[]    fitness    = new double[data.length];
                for (int i = 0; i < result.length; i++) result[i] = 0;
                for (int i = 0; i < data.length; i++) {
                    fitness[i]      = data[i][x];
                    rank_index[i]   = i;
                }

                //rank sorting
                for (int index = 0; index < fitness.length; index++) {
                    int min = index;
                    for(int i = index; i < fitness.length; i++) {
                        if(fitness[min] > fitness[i])  min = i;
                    }
                    if(fitness[index] != fitness[min]) {
                        temp                = fitness[index];
                        fitness[index]      = fitness[min];
                        fitness[min]        = temp;
                        tempI               = rank_index[index];
                        rank_index[index]   = rank_index[min];
                        rank_index[min]     = tempI;
                    }
                }
                // set the selection propability
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = this.m_C * Math.pow(1-this.m_C, i);
                    sum += result[i];
                }
                for (int i = 0; i < data.length; i++) {
                    ((AbstractEAIndividual)population.get(rank_index[i])).SetSelectionProbability(x, result[i]/sum);
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
    public String globalInfo() {
        return "This is non-linear ranking normation.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Non-Linear Ranking";
    }

    /** This methods allow you to set and get the nappa.
     * @param x     Long seed.
     */
    public void setC(double x) {
        if (x < 0) x = 0;
        if (x > 1) x = 1;
        this.m_C = x;
    }
    public double getC() {
        return m_C;
    }
    public String cTipText() {
        return "The c should be << 1.";
    }
}