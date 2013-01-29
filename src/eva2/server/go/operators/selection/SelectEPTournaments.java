package eva2.server.go.operators.selection;


import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;
import java.util.ArrayList;

/** This method implements the multiple tournament scheme
 * for EP.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.04.2003
 * Time: 16:17:26
 * To change this template use Options | File Templates.
 */
public class SelectEPTournaments implements InterfaceSelection, java.io.Serializable {

    private int         m_TournamentSize    = 4;
    private int         m_Tournaments       = 10;
    private boolean     m_ObeyDebsConstViolationPrinciple = true;
    private int[][]     m_Victories;

    public SelectEPTournaments() {
    }

    public SelectEPTournaments(SelectEPTournaments a) {
        this.m_TournamentSize       = a.m_TournamentSize;
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return (Object) new SelectEPTournaments(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    @Override
    public void prepareSelection(Population population) {
        int[]   best = new int[population.getBestEAIndividual().getFitness().length];
        int     rand;
        AbstractEAIndividual    tmpIndy;

        this.m_Victories = new int[population.size()][population.getBestEAIndividual().getFitness().length];
        for (int i = 0; i < this.m_Victories.length; i++) {
            for (int j = 0; j < this.m_Victories[i].length; j++) {
                this.m_Victories[i][j] = 0;
            }
        }
        for (int i = 0; i < population.size(); i++) {
            for (int j = 0; j < this.m_Tournaments; j++) {
                for (int k = 0; k < best.length; k++) {
                    best[k] = i;
                }
                // perform tournament
                for (int k = 0; k < this.m_TournamentSize; k++) {
                    rand = RNG.randomInt(0, population.size()-1);
                    tmpIndy = ((AbstractEAIndividual)population.get(rand));
                    for (int l = 0; l < best.length; l++) {
                        if (this.m_ObeyDebsConstViolationPrinciple) {
                             if ((!tmpIndy.violatesConstraint()) && (tmpIndy.getFitness(l) < ((AbstractEAIndividual)population.get(best[l])).getFitness(l))) {
                                best[l] = rand;
                            }
                        } else {
                            if (tmpIndy.getFitness(l) < ((AbstractEAIndividual)population.get(best[l])).getFitness(l)) {
                                best[l] = rand;
                            }
                        }
                    }
                }
                // assign victories
                for (int k = 0; k < best.length; k++) {
                    this.m_Victories[best[k]][k]++;
                }
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
    @Override
    public Population selectFrom(Population population, int size) {
        Population              result          = new Population();
        int                     currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual)population.get(0)).getFitness().length;

        ArrayList[] bestIndividuals = new ArrayList[critSize];
        for (int i = 0; i < critSize; i++) {
            bestIndividuals[i] = new ArrayList();
            // select the best individuals regarding crit i
            for (int j = 0; j < size; j++) {
                bestIndividuals[i].add(this.getBestIndividualExcept(population, bestIndividuals[i], i));
            }
        }

        // now get the actual result from the tmp list
        for (int i = 0; i < size; i++) {
            currentCriteria = RNG.randomInt(0, critSize-1);
            result.add(bestIndividuals[currentCriteria].get(0));
            bestIndividuals[currentCriteria].remove(0);
        }

        return result;
    }

    /** This method will return the best individual that is not in tabu regarding
     * crit
     * @param pop   The population to select from
     * @param tabu  The individuals that are to be ignored
     * @param crit  The criterion
     * @return Object the individual
     */
    private Object getBestIndividualExcept(Population pop, ArrayList tabu, int crit) {
        int     index           = -1;
        int     mostVictories   = -1;
        boolean member;
        AbstractEAIndividual indy;

        for (int i = 0; i < pop.size(); i++) {
            indy = (AbstractEAIndividual)pop.get(i);
            // check if indy is tabu
            member = false;
            for (int j = 0; j < tabu.size(); j++) {
                if (indy == tabu.get(j)) {
                    member = true;
                    break;
                }
            }
            if ((!member) && (this.m_Victories[i][crit] > mostVictories)) {
                index       = i;
                mostVictories    = this.m_Victories[i][crit];
            }
        }
        if (index >= 0) {
            return pop.get(index);
        }
        else {
            return pop.get(RNG.randomInt(0, pop.size()-1));
        }
    }

    /** This method allows you to select partners for a given Individual
     * @param dad               The already seleceted parent
     * @param avaiablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    @Override
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
        return "EP Tournament Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The EP tournament selection performs a number of tournaments per individual, the winner is assigned a point." +
                " The individuals with the most points are selected." +
                " This is a single objective selecting method, it will select in respect to a random criterion.";
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

    /** You can choose the number of tournaments
    */
    public String tournamentsTipText() {
        return "Choose the number of tournaments.";
    }
    public int getTournaments() {
        return m_Tournaments;
    }
    public void setTournaments(int g) {
        m_Tournaments = g;
    }

    /** Toggle the use of obeying the constraint violation principle
     * of Deb
     * @param b     The new state
     */
    @Override
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
