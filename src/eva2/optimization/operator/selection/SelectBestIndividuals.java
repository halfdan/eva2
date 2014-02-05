package eva2.optimization.operator.selection;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

import java.util.ArrayList;

/**
 * Select the best individuals.
 * In case of multiple fitness values the selection
 * criteria is selected randomly for each selection event.
 */
public class SelectBestIndividuals implements InterfaceSelection, java.io.Serializable {

    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectBestIndividuals() {
    }

    public SelectBestIndividuals(SelectBestIndividuals a) {
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return (Object) new SelectBestIndividuals(this);
    }

    /**
     * This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     *
     * @param population The population that is to be processed.
     */
    @Override
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /**
     * This method will select n best individuals from the given
     * Population.
     *
     * @param population The source population where to select from
     * @param size       The number of Individuals to select
     * @return The selected population.
     */
    @Override
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        int currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual) population.get(0)).getFitness().length;

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
            currentCriteria = RNG.randomInt(0, critSize - 1);
            result.add(bestIndividuals[currentCriteria].get(0));
            bestIndividuals[currentCriteria].remove(0);
        }

        return result;
    }

    /**
     * This method will return the best individual that is not in tabu regarding
     * crit
     *
     * @param pop  The population to select from
     * @param tabu The individuals that are to be ignored
     * @param crit The criterion
     * @return Object the individual
     */
    private Object getBestIndividualExcept(Population pop, ArrayList tabu, int crit) {
        int index = -1;
        double bestProb = Double.POSITIVE_INFINITY;
        boolean member;
        AbstractEAIndividual indy;

        if (this.obeyDebsConstViolationPrinciple) {
            for (int i = 0; i < pop.size(); i++) {
                indy = (AbstractEAIndividual) pop.get(i);
                // check if indy is tabu
                member = false;
                for (int j = 0; j < tabu.size(); j++) {
                    if (indy == tabu.get(j)) {
                        member = true;
                        break;
                    }
                }
                if ((!indy.violatesConstraint()) && (!member) && (indy.getFitness(crit) < bestProb)) {
                    index = i;
                    bestProb = indy.getFitness(crit);
                }
            }
            if (index >= 0) {
                return pop.get(index);
            } else {
                // darn all individuals seem to violate the constraints
                // so lets select the guy with the least worst constraint violation
                for (int i = 0; i < pop.size(); i++) {
                    indy = (AbstractEAIndividual) pop.get(i);
                    // check if indy is tabu
                    member = false;
                    for (int j = 0; j < tabu.size(); j++) {
                        if (indy == tabu.get(j)) {
                            member = true;
                            break;
                        }
                    }
                    if ((!member) && (indy.getConstraintViolation() < bestProb)) {
                        index = i;
                        bestProb = indy.getConstraintViolation();
                    }
                }
                if (index >= 0) {
                    return pop.get(index);
                } else {
                    return pop.get(RNG.randomInt(0, pop.size() - 1));
                }
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                indy = (AbstractEAIndividual) pop.get(i);
                // check if indy is tabu
                member = false;
                for (int j = 0; j < tabu.size(); j++) {
                    if (indy == tabu.get(j)) {
                        member = true;
                        break;
                    }
                }
                if ((!member) && (indy.getFitness(crit) < bestProb)) {
                    index = i;
                    bestProb = indy.getFitness(crit);
                }
            }
            if (index >= 0) {
                return pop.get(index);
            } else {
                return pop.get(RNG.randomInt(0, pop.size() - 1));
            }
        }
    }

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad              The already seleceted parent
     * @param avaiablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population avaiablePartners, int size) {
        return this.selectFrom(avaiablePartners, size);
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This selection method will select the n-Best individuals." +
                "This is a single objective selecting method, it will select in respect to a random criterion.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Elitist Selection";
    }

    /**
     * Toggle the use of obeying the constraint violation principle
     * of Deb
     *
     * @param b The new state
     */
    @Override
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.obeyDebsConstViolationPrinciple = b;
    }

    public boolean getObeyDebsConstViolationPrinciple() {
        return this.obeyDebsConstViolationPrinciple;
    }

    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's constraint violation principle.";
    }
}
