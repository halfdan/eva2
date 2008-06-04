package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.moso.MOSOMaxiMin;
import eva2.server.go.populations.Population;

/** Experimental selection mechanism for MOMA II where
 * a single individual is a whole set of Pareto optimal
 * solution. Currently defunct.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.02.2005
 * Time: 16:51:52
 * To change this template use File | Settings | File Templates.
 */
public class SelectMOMAIIDominanceCounter implements InterfaceSelection, java.io.Serializable {

    private InterfaceSelection      m_Selection = new SelectBestIndividuals();
    private boolean                 m_ObeyDebsConstViolationPrinciple = true;

    public SelectMOMAIIDominanceCounter() {
    }

    public SelectMOMAIIDominanceCounter(SelectMOMAIIDominanceCounter a) {
        this.m_Selection            = (InterfaceSelection)a.m_Selection.clone();
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectMOMAIIDominanceCounter(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        // here i need to calculate the number of donimating solutions for all
        // individuals that have a MatlabMultipleSolution element
        AbstractEAIndividual    tmpIndy1, tmpIndy2;
        Object                  tmpObj;
        MOMultipleSolutions malta, tmpMalta;

        // reset the stuff
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1 = (AbstractEAIndividual)population.get(i);
            tmpObj = tmpIndy1.getData("MOMAII");
            if ((tmpObj != null) && (tmpObj instanceof MOMultipleSolutions)) {
                ((MOMultipleSolutions)tmpObj).reset();
            }
        }

        // now check the stuff
        for (int i = 0; i < population.size(); i++) {
            tmpIndy1 = (AbstractEAIndividual)population.get(i);
            tmpObj = tmpIndy1.getData("MOMAII");
            if ((tmpObj != null) && (tmpObj instanceof MOMultipleSolutions)) {
                malta = (MOMultipleSolutions)tmpObj;
                //for (int j = i+1; j < population.size(); i++) {
                // MK: Im rather sure the i++ was intended to become a j++
                for (int j = i+1; j < population.size(); j++) {
                    tmpIndy2 = (AbstractEAIndividual)population.get(j);
                    tmpObj = tmpIndy2.getData("MOMAII");
                    if ((tmpObj != null) && (tmpObj instanceof MOMultipleSolutions)) {
                        tmpMalta = (MOMultipleSolutions)tmpObj;
                        malta.testDominance(tmpMalta);
                    }
                }
                // now i have compared malta to all remaining individuals
                // lets count the number of dominant solutions
                int     domCount = 0;
                for (int j = 0; j < malta.size(); j++) {
                    if (malta.get(i).isDominant) domCount++;
                }
                malta.m_SizeDominantSolutions = domCount;
                double[] fitness = new double[1];
                fitness[0] = 1/((double)(domCount+1));
                tmpIndy1.SetFitness(fitness);
            } else {
                double[] fitness = new double[1];
                fitness[0] = 2;
                tmpIndy1.SetFitness(fitness);
            }
        }
    }

    /** This method will select one Individual from the given
     * Population in respect to the selection propability of the
     * individual.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        return this.m_Selection.selectFrom(population, size);
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
        return "This selection method only works for MOMA-II, it selects an individual depending on the number of non-dominated solutions.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MOMAII-DonCount";
    }

    /** Since SelectMOMaxiMin relies on a MOSO conversion
     * a single criterion selection method can be used.
     * @param pop     The selection method used.
     */
    public void setSelectionMethod(InterfaceSelection pop) {
        this.m_Selection = pop;
    }
    public InterfaceSelection getSelectionMethod() {
        return this.m_Selection;
    }
    public String selectionMethodTipText() {
        return "Choose the selection method (single-criteria ones please).";
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