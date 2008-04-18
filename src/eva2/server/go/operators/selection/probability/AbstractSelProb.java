package eva2.server.go.operators.selection.probability;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** This abstract implementation gives some general
 * methods for retrieving and cleaning fitness values.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.03.2004
 * Time: 16:55:14
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSelProb implements InterfaceSelectionProbability, java.io.Serializable {

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public abstract Object clone();

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param input         The name of the input.
     */
    public void computeSelectionProbability(Population population, String[] input, boolean obeyConst) {
        this.computeSelectionProbability(population, this.preprocess(population, input), obeyConst);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param input         The name of the input.
     */
    public void computeSelectionProbability(Population population, String input, boolean obeyConst) {
        String[] tmp = new String[1];
        tmp[0] = input;
        this.computeSelectionProbability(population, tmp, obeyConst);
    }

    /** This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     *  probability sums up to one.
     * @param population    The population to compute.
     * @param data         The input data as double[][].
     */
    public abstract void computeSelectionProbability(Population population, double[][] data, boolean obeyConst);

    /** This method converts all inputs to a list of double values
     * @param population    A population that is to be computed.
     * @param inputs        A list of inputs.
     * @return double[][]   A array of values, first index gives the individual, second index the value.
     */
    protected double[][] preprocess(Population population, String[] inputs) {
        double[][]  result;
        double[]    tmpD;
        ArrayList   tmpList = new ArrayList();
        AbstractEAIndividual tmpIndy;
        Object      obj;

        result = new double[population.size()][];
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual)population.get(i);
            tmpList = new ArrayList();
            for (int j = 0; j < inputs.length; j++) {
                obj = tmpIndy.getData(inputs[j]);
                if (obj instanceof double[]) {
                    for (int m = 0; m < ((double[])obj).length; m++) {
                        tmpList.add(new Double(((double[])obj)[m]));
                    }
                }
                if (obj instanceof Double) {
                    tmpList.add((Double)obj);
                }
                if (obj instanceof float[]) {
                    for (int m = 0; m < ((float[])obj).length; m++) {
                        tmpList.add(new Double(((float[])obj)[m]));
                    }
                }
                if (obj instanceof Float) {
                    tmpList.add((Float)obj);
                }
                if (obj instanceof long[]) {
                    for (int m = 0; m < ((long[])obj).length; m++) {
                        tmpList.add(new Double(((long[])obj)[m]));
                    }
                }
                if (obj instanceof Long) {
                    tmpList.add((Long)obj);
                }
                if (obj instanceof int[]) {
                    for (int m = 0; m < ((int[])obj).length; m++) {
                        tmpList.add(new Double(((int[])obj)[m]));
                    }
                }
                if (obj instanceof Integer) {
                    tmpList.add((Integer)obj);
                }
            }
            // now we got a complete ArrayList
            tmpD = new double[tmpList.size()];
            for (int j = 0; j < tmpD.length; j++) tmpD[j] = ((Double)tmpList.get(j)).doubleValue();
            result[i] = tmpD;
        }

        // Here i could check for NaN and BufferOverflows
        double  tmpSum, min, max;
        for (int i = 0; i < result.length; i++) {
            tmpSum  = 0;
            min     = Double.MAX_VALUE;
            max     = Double.MIN_VALUE;
            for (int j = 0; j < result[i].length; j++) {
                if (Double.isInfinite(result[i][j])) result[i][j] = Double.NaN;
                if (!Double.isNaN(result[i][j])) {
                    if (result[i][j] < min) min = result[i][j];
                    if (result[i][j] > max) max = result[i][j];
                }
            }
            for (int j = 0; j < result[i].length; j++) {
                if (Double.isNaN(result[i][j])) {
                    //System.out.println("Warning: Fitness function produces NaN as fitness values! Please check fitness function!");
                    result[i][j] = max;
                }
                tmpSum += result[i][j];
            }
            while (Double.isNaN(tmpSum) || Double.isInfinite(tmpSum)) {
                //System.out.println("Problem: Sum over fitness is NaN! Please check fitness function!");
                tmpSum = 0;
                for (int j = 0; j < result[i].length; j++) {
                    result[i][j] = result[i][j]/1000;
                    tmpSum += result[i][j];
                }
            }
        }

        return result;
    }
}
