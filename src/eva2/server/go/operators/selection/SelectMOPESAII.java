package eva2.server.go.operators.selection;


import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.archiving.ArchivingPESAII;
import eva2.server.go.populations.Population;
import eva2.server.go.tools.RandomNumberGenerator;

/** The multi-objective PESA II selection criteria based on a n-dimensional
 * grid using a squezzing factor. 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.08.2004
 * Time: 10:55:24
 * To change this template use File | Settings | File Templates.
 */
public class SelectMOPESAII implements InterfaceSelection, java.io.Serializable {

    ArchivingPESAII     m_PESAII            = new ArchivingPESAII();
    int[]               m_Squeeze;
    int                 m_TournamentSize    = 2;
    ArrayList           m_GridBoxes;
    boolean             m_ObeyDebsConstViolationPrinciple = true;

    public SelectMOPESAII() {
    }

    public SelectMOPESAII(SelectMOPESAII a) {
        this.m_PESAII           = new ArchivingPESAII();
        this.m_TournamentSize   = a.m_TournamentSize;
        this.m_ObeyDebsConstViolationPrinciple  = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectMOPESAII(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        this.m_Squeeze              = this.m_PESAII.calculateSqueezeFactor(population);
        Hashtable tmpGridBoxes      = new Hashtable();
        ArrayList           tmp;
        AbstractEAIndividual tmpIndy;
        int[]               gridBox;
        String              tmpString;

        // first build the hashtable
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual)population.get(i);
            gridBox = (int[])tmpIndy.getData("GridBox");
            tmpString ="";
            for (int j = 0; j < gridBox.length; j++) {
                tmpString += gridBox[j]+"/";
            }
            if (tmpGridBoxes.get(tmpString) == null) {
                tmp = new ArrayList();
                tmpGridBoxes.put(tmpString, tmp);
            } else {
                tmp = (ArrayList) tmpGridBoxes.get(tmpString);
            }
            tmp.add(tmpIndy);
        }
        // now build the arraylist from the Hashtable
        this.m_GridBoxes = new ArrayList();
        Enumeration myEnum = tmpGridBoxes.elements();
        while (myEnum.hasMoreElements()) {
            this.m_GridBoxes.add(myEnum.nextElement());
        }
    }

    /** This method will select one Indiviudal from the given
     * Population in respect to the selection propability of the
     * individual.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        result.setPopulationSize(size);
        for (int i = 0; i < size; i++) {
            result.add(this.select(population));
        }
        return result;
    }

   /** This method selects a single individual from the current population
     * @param population The population to select from
     */
    private AbstractEAIndividual select(Population population) {
       AbstractEAIndividual     resultIndy;
       ArrayList                box1, box2;
       int                      winner, tmp;

        try {
            box1 = (ArrayList)this.m_GridBoxes.get(RandomNumberGenerator.randomInt(0, this.m_GridBoxes.size()-1));
            box2 = (ArrayList)this.m_GridBoxes.get(RandomNumberGenerator.randomInt(0, this.m_GridBoxes.size()-1));
            if (((Integer)((AbstractEAIndividual)box1.get(0)).getData("SqueezeFactor")).intValue()
                < ((Integer)((AbstractEAIndividual)box2.get(0)).getData("SqueezeFactor")).intValue()) {
                resultIndy = (AbstractEAIndividual) (box1.get(RandomNumberGenerator.randomInt(0, box1.size()-1)));
            } else {
                resultIndy = (AbstractEAIndividual) (box2.get(RandomNumberGenerator.randomInt(0, box2.size()-1)));
            }
        } catch (java.lang.IndexOutOfBoundsException e) {
            System.out.println("Tournament Selection produced IndexOutOfBoundsException!");
            resultIndy = population.getBestEAIndividual();
        }
        return resultIndy;
    }

    /** This method allows you to select partners for a given Individual
     * @param dad               The already seleceted parent
     * @param avaiablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population avaiablePartners, int size) {
        return this.selectFrom(avaiablePartners, size);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Performs a binary tournament selection, preferring the gridbox of smaller squeezing factor and selecting a random individual from the winner box.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESAII Selection";
    }

    /** Toggel the use of obeying the constraint violation principle
     * of Deb
     * @param b     The new state
     */
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.m_ObeyDebsConstViolationPrinciple = b;
    }
    public boolean getObeyDebsConstViolationPrinciple() {
        return this.m_ObeyDebsConstViolationPrinciple;
    }
    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
}