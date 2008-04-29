package eva2.server.go.operators.selection;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;

/** Select the best individuals.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.05.2003
 * Time: 19:34:16
 * To change this template use Options | File Templates.
 */
public class SelectBestIndividuals implements InterfaceSelection, java.io.Serializable {

    private boolean     m_ObeyDebsConstViolationPrinciple = true;

    public SelectBestIndividuals() {
    }

    public SelectBestIndividuals(SelectBestIndividuals a) {
        this.m_ObeyDebsConstViolationPrinciple  = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectBestIndividuals(this);
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

    /** This method will select one Individual from the given
     * Population.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population              result = new Population();
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
     * @param crit  The criterium
     * @return Object the individual
     */
    private Object getBestIndividualExcept(Population pop, ArrayList tabu, int crit) {
        int     index = -1;
        double  bestProb = Double.POSITIVE_INFINITY;
        boolean member;
        AbstractEAIndividual indy;

        if (this.m_ObeyDebsConstViolationPrinciple) {
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
                if ((!indy.violatesConstraint()) && (!member) && (indy.getFitness(crit) < bestProb)) {
                    index       = i;
                    bestProb    = indy.getFitness(crit);
                }
            }
            if (index >= 0) return pop.get(index);
            else {
                // darn all individuals seem to violate the constraints
                // so lets select the guy with the least worst constraint violation
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
                    if ((!member) && (indy.getConstraintViolation() < bestProb)) {
                        index       = i;
                        bestProb    = indy.getConstraintViolation();
                    }
                }
                if (index >= 0) return pop.get(index);
                else return pop.get(RNG.randomInt(0, pop.size()-1));
            }
        } else {
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
                if ((!member) && (indy.getFitness(crit) < bestProb)) {
                    index       = i;
                    bestProb    = indy.getFitness(crit);
                }
            }
            if (index >= 0) return pop.get(index);
            else return pop.get(RNG.randomInt(0, pop.size()-1));
        }
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
        return "This selection method will select the n-Best individuals." +
                "This is a single objective selecting method, it will select in respect to a random criteria.";
    }
    
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Elitist Selection";
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
