package eva2.optimization.operator.selection.probability;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * Boltzman selection, actually it is no a selection method
 * but a scaling method, but it is very good, because it is
 * invariant to any linear transition function.
 */
@Description("This is the Boltzman Normation.")
public class SelProbBoltzman extends AbstractSelProb implements java.io.Serializable {

    private double q = 1.0;

    public SelProbBoltzman() {
    }

    public SelProbBoltzman(double q) {
        this.q = q;
    }

    public SelProbBoltzman(SelProbBoltzman a) {
        this.q = a.q;
    }

    @Override
    public Object clone() {
        return new SelProbBoltzman(this);
    }

    /**
     * This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     * probability sums up to one.
     *
     * @param population The population to compute.
     * @param data       The input data as double[][].
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double sum = 0, mean = 0, dev = 0;
        double[] result = new double[data.length];

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
                // iterating over the fitness cases
                for (int x = 0; x < data[0].length; x++) {
                    sum = 0;
                    mean = 0;
                    dev = 0;
                    // first find the worst, to be able to default
                    double worst = Double.NEGATIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i][x] > worst) {
                            worst = data[i][x];
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (!population.get(i).violatesConstraint()) {
                            result[i] = -data[i][x];
                        } else {
                            result[i] = -worst;
                        }
                        sum += result[i];
                    }

                    mean = sum / ((double) data.length);
                    for (int i = 0; i < data.length; i++) {
                        dev += Math.pow((result[i] - mean), 2);
                    }
                    dev = Math.sqrt(dev / (data.length - 1));
                    if (dev < 0.0000001) {
                        dev = 0.0000001;
                    }
                    sum = 0;
                    for (int i = 0; i < data.length; i++) {
                        if (!population.get(i).violatesConstraint()) {
                            result[i] = Math.exp((this.q * -data[i][x]) / dev);
                        } else {
                            result[i] = Math.exp((this.q * -worst) / dev);
                        }
                        sum += result[i];
                    }

                    if (sum == 0) {
                        for (int i = 0; i < data.length; i++) {
                            result[i] = 1 / result.length;
                        }
                    } else {
                        for (int i = 0; i < data.length; i++) {
                            result[i] /= sum;
                        }
                    }

                    boolean check = true;
                    for (int i = 0; i < data.length; i++) {
                        if (Double.isNaN(result[i])) {
                            check = false;
                        }
                    }
                    if (!check) {
                        System.out.println("Boltzman Normation created major error (const+feasible)!");
                        System.out.println("Sum:  " + sum);
                        System.out.println("Mean: " + mean);
                        System.out.println("Dev:  " + dev);
                        System.out.println("Worst:" + worst);
                        for (int i = 0; i < data.length; i++) {
                            result[i] = 1 / result.length;
                        }
                    }

                    for (int i = 0; i < population.size(); i++) {
                        population.get(i).setSelectionProbability(x, result[i]);
                    }
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                sum = 0;
                mean = 0;
                dev = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -population.get(i).getConstraintViolation();
                    sum += result[i];
                }
                mean = sum / ((double) data.length);
                for (int i = 0; i < data.length; i++) {
                    dev += Math.pow((result[i] - mean), 2);
                }
                dev = Math.sqrt(dev / (data.length - 1));
                if (dev < 0.0000001) {
                    dev = 0.0000001;
                }
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = Math.exp((this.q * -population.get(i).getConstraintViolation()) / dev);
                    sum += result[i];
                }

                if (sum == 0) {
                    for (int i = 0; i < data.length; i++) {
                        result[i] = 1 / result.length;
                    }
                } else {
                    for (int i = 0; i < data.length; i++) {
                        result[i] /= sum;
                    }
                }

                boolean check = true;
                for (int i = 0; i < data.length; i++) {
                    if (Double.isNaN(result[i])) {
                        check = false;
                    }
                }
                if (!check) {
                    System.out.println("Boltzman Normation created major error (const, but no feasible)!");
                    for (int i = 0; i < data.length; i++) {
                        result[i] = 1 / result.length;
                    }
                }
                for (int i = 0; i < population.size(); i++) {
                    double[] tmpD = new double[1];
                    tmpD[0] = result[i] / sum;
                    population.get(i).setSelectionProbability(tmpD);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                mean = 0;
                dev = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -data[i][x];
                    sum += result[i];
                }
                mean = sum / ((double) data.length);
                for (int i = 0; i < data.length; i++) {
                    dev += Math.pow((result[i] - mean), 2);
                }
                dev = Math.sqrt(dev / (data.length - 1));
                if (dev < 0.0000001) {
                    dev = 0.0000001;
                }
                sum = 0;
                for (int i = 0; i < data.length; i++) {
                    result[i] = Math.exp((this.q * -data[i][x]) / dev);
                    sum += result[i];
                }

                if (sum == 0) {
                    for (int i = 0; i < data.length; i++) {
                        result[i] = 1 / result.length;
                    }
                } else {
                    for (int i = 0; i < data.length; i++) {
                        result[i] /= sum;
                    }
                }

                boolean check = true;
                for (int i = 0; i < data.length; i++) {
                    if (Double.isNaN(result[i])) {
                        check = false;
                    }
                }
                if (!check) {
                    System.out.println("Boltzman Normation created major error (no const)!");
                    for (int i = 0; i < data.length; i++) {
                        result[i] = 1 / result.length;
                    }
                }

                for (int i = 0; i < population.size(); i++) {
                    population.get(i).setSelectionProbability(x, result[i]);
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
        return "Boltzman Normation";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public double getQ() {
        return this.q;
    }

    public void setQ(double b) {
        this.q = Math.abs(b);
    }

    public String qTipText() {
        return "The selection pressure. The bigger q, the higher the selection pressure.";
    }
}

