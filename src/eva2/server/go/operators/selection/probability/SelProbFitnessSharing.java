package eva2.server.go.operators.selection.probability;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.populations.Population;

/** Here we have the infamous fitness sharing method.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.06.2005
 * Time: 15:23:05
 * To change this template use File | Settings | File Templates.
 */
public class SelProbFitnessSharing extends AbstractSelProb implements java.io.Serializable {

    private InterfaceSelectionProbability   m_BasicNormationMethod  = new SelProbStandard();
    private InterfaceDistanceMetric         m_DistanceMetric        = new PhenotypeMetric();
    private double                          m_SharingDistance       = 0.1;

    public SelProbFitnessSharing() {
    }

    public SelProbFitnessSharing(SelProbFitnessSharing a) {
        if (a.m_BasicNormationMethod != null) {
            this.m_BasicNormationMethod = (InterfaceSelectionProbability)a.m_BasicNormationMethod.clone();
        }
        if (a.m_DistanceMetric != null) {
            this.m_DistanceMetric = (InterfaceDistanceMetric)a.m_DistanceMetric;
        }
        this.m_SharingDistance = a.m_SharingDistance;
    }

    public Object clone() {
        return (Object) new SelProbFitnessSharing(this);
    }

    /** This method computes the selection probability for each individual
     *  in the population. Note: Summed over the complete populaiton the selection
     *  probability sums up to one. Keep in mind that fitness is always to be
     *  minimizied! Small values for data => big values for selectionprob.
     * @param population    The population to compute.
     * @param data          The input data as double[][].
     */
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        this.m_BasicNormationMethod.computeSelectionProbability(population, data, obeyConst);
        // now perform fitness sharing
        double[] selProb = new double[population.size()];
        double   distance, sharing, sum = 0;
        AbstractEAIndividual tmpIndy1, tmpIndy2;
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1    = ((AbstractEAIndividual)population.get(i));
            selProb[i]  = tmpIndy1.getSelectionProbability()[0];
            sharing     = 0;
            for (int j = 0; j < population.size(); j++) {
                if (i != j) {
                    distance = this.m_DistanceMetric.distance(tmpIndy1, ((AbstractEAIndividual)population.get(i)));
                    if (distance < this.m_SharingDistance) {
                        sharing += (1 - distance/this.m_SharingDistance);
                    }
                }
            }
            selProb[i] = selProb[i]/(1+sharing);
            sum += selProb[i];
        }
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1    = ((AbstractEAIndividual)population.get(i));
            tmpIndy1.SetSelectionProbability(0, (selProb[i]/sum));
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a fitness sharing based normation method.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Fitness Sharing";
    }

    /** This method will allow you to set and get the Q Parameter
     * @return The new selection pressure q.
     */
    public InterfaceSelectionProbability getBasicNormationMethod() {
        return this.m_BasicNormationMethod;
    }
    public void setBasicNormationMethod(InterfaceSelectionProbability b){
        this.m_BasicNormationMethod = b;
    }
    public String basicNormationMethodTipText() {
        return "Choose the basic normation method.";
    }

    /** This method will allow you to set and get the Q Parameter
     * @return The new selection pressure q.
     */
    public InterfaceDistanceMetric getDistanceMetric() {
        return this.m_DistanceMetric;
    }
    public void setDistanceMetric(InterfaceDistanceMetric b){
        this.m_DistanceMetric = b;
    }
    public String distanceMetricTipText() {
        return "Select the distance metric to use.";
    }

    /** This method will allow you to set and get the Q Parameter
     * @return The new selection pressure q.
     */
    public double getSharingDistance() {
        return this.m_SharingDistance;
    }
    public void setSharingDistance(double b){
        if (b < 0.000001) b = 0.000001;
        this.m_SharingDistance = b;
    }
    public String sharingDistanceTipText() {
        return "Choose the sharing distance to use.";
    }
}