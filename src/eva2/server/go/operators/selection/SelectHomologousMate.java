package eva2.server.go.operators.selection;

import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.ObjectiveSpaceMetric;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;

/** An experimential implementation for mating restriction.
 * Possibly defunct.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 06.08.2003
 * Time: 19:05:24
 * To change this template use Options | File Templates.
 */
public class SelectHomologousMate extends SelectTournament implements java.io.Serializable {

    private double                      m_MatingRadius = 0.1;
    private InterfaceDistanceMetric     m_Metric = new ObjectiveSpaceMetric();

    public SelectHomologousMate() {
    }

    public SelectHomologousMate(SelectHomologousMate a) {
        this.m_MatingRadius         = a.m_MatingRadius;
        this.m_Metric               = (InterfaceDistanceMetric)a.m_Metric.clone();
    }

    public Object clone() {
        return (Object) new SelectHomologousMate(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /** This method allows you to select partners for a given Individual
     * @param dad               The already seleceted parent
     * @param availablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        Population      possibleMates = new Population();

        // first select all possible partners for daddy
        // to be honest daddy himself is not omitted....
        for (int i = 0; i < availablePartners.size(); i++) {
            if (this.m_Metric.distance(dad, (AbstractEAIndividual)availablePartners.get(i)) < this.m_MatingRadius) {
                possibleMates.add(availablePartners.get(i));
            }
        }
        //System.out.println("Partners Size: " + possibleMates.size());
        if (possibleMates.size() <=1) return this.selectFrom(availablePartners, size);
        else return this.selectFrom(possibleMates, size);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Homologous Mating Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This selection will select n mates from all individuals within the mating distance (extends Tournament Selection)." +
                "This is a single objective selecting method, it will select in respect to a random criterion.";
    }

    /** This method allows you to set/get the mating radius.
     * @return The current optimizing method
     */
    public double getMatingRadius() {
        return this.m_MatingRadius;
    }
    public void setMatingRadius(double b){
        this.m_MatingRadius = b;
    }
    public String matingRadiusTipText() {
        return "Choose the mating radius.";
    }

    /** These methods allows you to set/get the type of Distance Metric.
     * @param Metric
     */
    public void setMetric(InterfaceDistanceMetric Metric) {
        this.m_Metric = Metric;
    }
    public InterfaceDistanceMetric getMetric() {
        return this.m_Metric;
    }
    public String metricTipText() {
        return "The distance metric used. Note: This depends on the type of EAIndividual used!";
    }
}