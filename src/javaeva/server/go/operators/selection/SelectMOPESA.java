package javaeva.server.go.operators.selection;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.archiving.ArchivingPESAII;
import javaeva.server.go.operators.moso.MOSOMaxiMin;
import javaeva.server.go.populations.Population;
import javaeva.server.go.tools.RandomNumberGenerator;

/** The multi-objective PESA selection method. 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.08.2004
 * Time: 10:56:47
 * To change this template use File | Settings | File Templates.
 */
public class SelectMOPESA implements InterfaceSelection, java.io.Serializable {

    ArchivingPESAII     m_PESAII            = new ArchivingPESAII();
    int[]               m_Squeeze;
    int                 m_TournamentSize    = 2;
    boolean             m_ObeyDebsConstViolationPrinciple = true;

    public SelectMOPESA() {
    }

    public SelectMOPESA(SelectMOPESA a) {
        this.m_PESAII           = new ArchivingPESAII();
        this.m_TournamentSize   = a.m_TournamentSize;
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectMOPESA(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        this.m_Squeeze = this.m_PESAII.calculateSqueezeFactor(population);
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
       int                      winner, tmp;

        try {
            winner = RandomNumberGenerator.randomInt(0, population.size()-1);
            for (int i = 1; i < this.m_TournamentSize; i++) {
                tmp = RandomNumberGenerator.randomInt(0, population.size()-1);
                if (this.m_Squeeze[tmp] < this.m_Squeeze[winner]) winner = tmp;
            }
            resultIndy = (AbstractEAIndividual) population.get(winner);
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
        return "Performs a binary tournament selection, preferring the individual with the smaller squeezing factor.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESA Selection";
    }

    /** You can choose the tournament size.
    */
    public String tournamentSizeTipText() {
        return "Choose the tournament size.";
    }
    public int getTournamentSize() {
        return m_TournamentSize;
    }
    public void setTournamentSize(int g) {
        m_TournamentSize = g;
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
