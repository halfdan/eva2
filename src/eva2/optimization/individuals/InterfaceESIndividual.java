package eva2.optimization.individuals;

/**
 * This interface gives access to a real-valued genotype and should
 * only be used by mutation and crossover operators. Onyl exception are
 * data type specific optimization strategies like PSO or DE.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 15:59:45
 * To change this template use Options | File Templates.
 */
public interface InterfaceESIndividual {

    /**
     * This method will allow the user to read the ES 'genotype'
     *
     * @return BitSet
     */
    public double[] getDGenotype();

    /**
     * This method will allow the user to set the current ES 'genotype'.
     *
     * @param b The new genotype of the Individual
     */
    public void setDGenotype(double[] b);
//
//    /** This method will set the range of the double attributes.
//     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
//     * for dimension d.
//     * @param range     The new range for the double data.
//     */
//    public void setDoubleRange(double[][] range);

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    public double[][] getDoubleRange();

}
