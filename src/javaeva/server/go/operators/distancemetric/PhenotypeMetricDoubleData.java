package javaeva.server.go.operators.distancemetric;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;

/** A Phenotype distance for double data, i guess
 * this is guess for the clustering based niching.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.04.2003
 * Time: 13:27:39
 * To change this template use Options | File Templates.
 */
public class PhenotypeMetricDoubleData implements InterfaceDistanceMetric, java.io.Serializable {

    public PhenotypeMetricDoubleData() {
    }

    public PhenotypeMetricDoubleData(PhenotypeMetricDoubleData a) {
    }

    public Object clone() {
        return (Object) new PhenotypeMetricDoubleData(this);
    }

    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double[]    dIndy1, dIndy2;
        double[][]  range1, range2;
        double      result = 0;

        if ((indy1 instanceof InterfaceDataTypeDouble) && (indy2 instanceof InterfaceDataTypeDouble)) {
            dIndy1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
            range1 = ((InterfaceDataTypeDouble) indy1).getDoubleRange();
            dIndy2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
            range2 = ((InterfaceDataTypeDouble) indy2).getDoubleRange();
        } else return 1.0;

        for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
            result += Math.pow(((dIndy1[i] - range1[i][0])/(range1[i][1] - range1[i][0])) - ((dIndy2[i] - range2[i][0])/(range2[i][1] - range2[i][0])), 2);
        }
        return Math.sqrt(result);
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a phenotype based method suited for double data. Metric is computed on a normalized search space.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Phenotype Metric";
    }
}

