package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.tools.RandomNumberGenerator;

/** Tournament selection within a given tournament group size,
 * also scaling invariant.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2003
 * Time: 16:17:26
 * To change this template use Options | File Templates.
 */
public class SelectTournament implements InterfaceSelection, java.io.Serializable {

    private int         m_TournamentSize = 4;
    private boolean     m_ObeyDebsConstViolationPrinciple = true;

    public SelectTournament() {
    }

    public SelectTournament(SelectTournament a) {
        this.m_TournamentSize       = a.m_TournamentSize;
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectTournament(this);
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
        AbstractEAIndividual    result, tmpIndy;
        int                     currentCriteria = 0, critSize = 0;

        try {
            critSize = ((AbstractEAIndividual)population.get(0)).getFitness().length;
            currentCriteria = RandomNumberGenerator.randomInt(0, critSize-1);
            if (this.m_ObeyDebsConstViolationPrinciple) {
                Population tournamentGroup = new Population();
                for (int i = 0; i < this.m_TournamentSize; i++) {
                    tournamentGroup.add(population.get(RandomNumberGenerator.randomInt(0, population.size()-1)));
                }
                SelectBestIndividuals best = new SelectBestIndividuals();
                best.setObeyDebsConstViolationPrinciple(true);
                result = (AbstractEAIndividual)best.selectFrom(tournamentGroup, 1).get(0);
            } else {
                result = (AbstractEAIndividual) population.get(RandomNumberGenerator.randomInt(0, population.size()-1));
                for (int i = 1; i < this.m_TournamentSize; i++) {
                    tmpIndy = (AbstractEAIndividual) population.get(RandomNumberGenerator.randomInt(0, population.size()-1));
                    if (tmpIndy.getFitness(currentCriteria) < result.getFitness(currentCriteria)) result = tmpIndy;
                }
            }
        } catch (java.lang.IndexOutOfBoundsException e) {
            System.out.println("Tournament Selection produced IndexOutOfBoundsException!");
            //System.out.println(""+e.getMessage());
            result = population.getBestEAIndividual();
        }
        //System.out.println("CritSize: " + critSize +" "+ result.getFitness(0));
        return result;
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
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Tournament Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The tournament selection compares the raw fitness of n individuals and takes the best." +
                "This is a single-objective method, it will select with respect to the first criterium in the multi-objective case.";
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
