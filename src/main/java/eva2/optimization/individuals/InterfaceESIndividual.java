package eva2.optimization.individuals;

/**
 * This interface gives access to a real-valued genotype and should
 * only be used by mutation and crossover operators. Only exception are
 * data type specific optimization strategies like PSO or DE.
 */
public interface InterfaceESIndividual {

    /**
     * This method will allow the user to read the ES 'genotype'
     *
     * @return BitSet
     */
    double[] getDGenotype();

    /**
     * This method will allow the user to set the current ES 'genotype'.
     *
     * @param b The new genotype of the Individual
     */
    void setDGenotype(double[] b);

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    double[][] getDoubleRange();

}
