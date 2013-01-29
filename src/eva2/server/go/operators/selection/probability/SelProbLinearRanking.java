package eva2.server.go.operators.selection.probability;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** A linear ranking method with offsets.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.06.2004
 * Time: 10:30:47
 * To change this template use File | Settings | File Templates.
 */
public class SelProbLinearRanking extends AbstractSelProb implements java.io.Serializable {

    private double          nappaPlus   = 1.1;
    private double          nappaMinus  = 0.9;

    public SelProbLinearRanking() {
    }

    public SelProbLinearRanking(SelProbLinearRanking a) {
        this.nappaPlus  = a.nappaPlus;
        this.nappaMinus = a.nappaMinus;
    }

    @Override
    public Object clone() {
        return (Object) new SelProbLinearRanking(this);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data          The input as double[][]
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double                  temp;
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
                    for (int i = 0; i < data.length; i++) {
                        temp = (1/(double)size) * (this.nappaPlus - ((this.nappaPlus - this.nappaMinus)*((double)(i)/(double)(size-1))));
                        ((AbstractEAIndividual)population.get(rank_index[i])).SetSelectionProbability(x, temp);
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
                for (int i = 0; i < data.length; i++) {
                    double[] tmpD   = new double[1];
                    tmpD[0] = (1/(double)size) * (this.nappaPlus - ((this.nappaPlus - this.nappaMinus)*((double)(i)/(double)(size-1))));
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
                for (int i = 0; i < data.length; i++) {
                    temp = (1/(double)size) * (this.nappaPlus - ((this.nappaPlus - this.nappaMinus)*((double)(i)/(double)(size-1))));
                    ((AbstractEAIndividual)population.get(rank_index[i])).SetSelectionProbability(x, temp);
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
        return "This is linear ranking normation.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Linear Ranking";
    }

    /** This methods allow you to set and get the nappa.
     * @param x     Long seed.
     */
    public void setNappa(double x) {
        if (x < 1) x = 1;
        if (x > 2) x = 2;
        nappaPlus = x;
        this.nappaMinus = 2 - this.nappaPlus;
    }
    public double getNappa() {
        return nappaPlus;
    }
    public String nappaTipText() {
        return "The etha variable in lin. ranking (1<= etha <= 2).";
    }
}