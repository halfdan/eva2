package eva2.optimization.operator.selection.probability;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * A non-linear ranking, which is difficult to tune to
 * the given optimization problem i guess.
 */
@Description("This is non-linear ranking normation.")
public class SelProbNonLinearRanking extends AbstractSelProb implements java.io.Serializable {

    private double c = 0.04;

    public SelProbNonLinearRanking() {
    }

    public SelProbNonLinearRanking(double theC) {
        this.c = theC;
    }

    public SelProbNonLinearRanking(SelProbNonLinearRanking a) {
        this.c = a.c;
    }

    @Override
    public Object clone() {
        return new SelProbNonLinearRanking(this);
    }

    /**
     * This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     * probability sums up to one.
     *
     * @param population The population to compute.
     * @param data       The input as double[][]
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double temp, sum;
        double[] result = new double[data.length];
        int tempI, size = population.size();

        if (obeyConst) {
            // first check if anyone holds the constraints
            boolean isFeasible = false;
            for (int i = 0; i < population.size(); i++) {
                if (!population.get(i).violatesConstraint()) {
                    isFeasible = true;
                }
            }
            if (isFeasible) {
                // at least one is feasible
                for (int x = 0; x < data[0].length; x++) {
                    // first find the worst, to be able to default
                    double worst = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i][x] > worst) {
                            worst = data[i][x];
                        }
                    }
                    int[] rank_index = new int[data.length];
                    double[] fitness = new double[data.length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = 0;
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (!(population.get(i).violatesConstraint())) {
                            fitness[i] = data[i][x];
                        } else {
                            fitness[i] = worst;
                        }
                        rank_index[i] = i;
                    }

                    //rank sorting
                    for (int index = 0; index < fitness.length; index++) {
                        int min = index;
                        for (int i = index; i < fitness.length; i++) {
                            if (fitness[min] > fitness[i]) {
                                min = i;
                            }
                        }
                        if (fitness[index] != fitness[min]) {
                            temp = fitness[index];
                            fitness[index] = fitness[min];
                            fitness[min] = temp;
                            tempI = rank_index[index];
                            rank_index[index] = rank_index[min];
                            rank_index[min] = tempI;
                        }
                    }
                    // set the selection propability
                    sum = 0;
                    for (int i = 0; i < data.length; i++) {
                        result[i] = this.c * Math.pow(1 - this.c, i);
                        sum += result[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        population.get(rank_index[i]).setSelectionProbability(x, result[i] / sum);
                    }
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                int[] rank_index = new int[data.length];
                double[] fitness = new double[data.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = 0;
                }
                for (int i = 0; i < data.length; i++) {
                    fitness[i] = -population.get(i).getConstraintViolation();
                    rank_index[i] = i;
                }

                //rank sorting
                for (int index = 0; index < fitness.length; index++) {
                    int min = index;
                    for (int i = index; i < fitness.length; i++) {
                        if (fitness[min] > fitness[i]) {
                            min = i;
                        }
                    }
                    if (fitness[index] != fitness[min]) {
                        temp = fitness[index];
                        fitness[index] = fitness[min];
                        fitness[min] = temp;
                        tempI = rank_index[index];
                        rank_index[index] = rank_index[min];
                        rank_index[min] = tempI;
                    }
                }
                // set the selection propability
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = this.c * Math.pow(1 - this.c, i);
                    sum += result[i];
                }
                for (int i = 0; i < data.length; i++) {
                    double[] tmpD = new double[1];
                    tmpD[0] = result[i] / sum;
                    population.get(rank_index[i]).setSelectionProbability(tmpD);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                int[] rank_index = new int[data.length];
                double[] fitness = new double[data.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = 0;
                }
                for (int i = 0; i < data.length; i++) {
                    fitness[i] = data[i][x];
                    rank_index[i] = i;
                }

                //rank sorting
                for (int index = 0; index < fitness.length; index++) {
                    int min = index;
                    for (int i = index; i < fitness.length; i++) {
                        if (fitness[min] > fitness[i]) {
                            min = i;
                        }
                    }
                    if (fitness[index] != fitness[min]) {
                        temp = fitness[index];
                        fitness[index] = fitness[min];
                        fitness[min] = temp;
                        tempI = rank_index[index];
                        rank_index[index] = rank_index[min];
                        rank_index[min] = tempI;
                    }
                }
                // set the selection propability
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = this.c * Math.pow(1 - this.c, i);
                    sum += result[i];
                }
                for (int i = 0; i < data.length; i++) {
                    population.get(rank_index[i]).setSelectionProbability(x, result[i] / sum);
                }
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Non-Linear Ranking";
    }

    /**
     * This methods allow you to set and get the scaling.
     *
     * @param x Long seed.
     */
    public void setC(double x) {
        if (x < 0) {
            x = 0;
        }
        if (x > 1) {
            x = 1;
        }
        this.c = x;
    }

    public double getC() {
        return c;
    }

    public String cTipText() {
        return "The exponential base c is taken to the power of the individual's rank and should be << 1.";
    }
}