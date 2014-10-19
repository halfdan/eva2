package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingPESAII;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/**
 * The multi-objective PESA selection method.
 */
public class SelectMOPESA implements InterfaceSelection, java.io.Serializable {

    ArchivingPESAII PESAII = new ArchivingPESAII();
    int[] squeeze;
    int tournamentSize = 2;
    boolean obeyDebsConstViolationPrinciple = true;

    public SelectMOPESA() {
    }

    public SelectMOPESA(SelectMOPESA a) {
        this.PESAII = new ArchivingPESAII();
        this.tournamentSize = a.tournamentSize;
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectMOPESA(this);
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
        this.squeeze = this.PESAII.calculateSqueezeFactor(population);
    }

    /**
     * This method will select one Individual from the given
     * Population in respect to the selection propability of the
     * individual.
     *
     * @param population The source population where to select from
     * @param size       The number of Individuals to select
     * @return The selected population.
     */
    @Override
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        result.setTargetSize(size);
        for (int i = 0; i < size; i++) {
            result.add(this.select(population));
        }
        return result;
    }

    /**
     * This method selects a single individual from the current population
     *
     * @param population The population to select from
     */
    private AbstractEAIndividual select(Population population) {
        AbstractEAIndividual resultIndy;
        int winner, tmp;

        try {
            winner = RNG.randomInt(0, population.size() - 1);
            for (int i = 1; i < this.tournamentSize; i++) {
                tmp = RNG.randomInt(0, population.size() - 1);
                if (this.squeeze[tmp] < this.squeeze[winner]) {
                    winner = tmp;
                }
            }
            resultIndy = (AbstractEAIndividual) population.get(winner);
        } catch (java.lang.IndexOutOfBoundsException e) {
            System.out.println("Tournament Selection produced IndexOutOfBoundsException!");
            resultIndy = population.getBestEAIndividual();
        }
        return resultIndy;
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
        return "Performs a binary tournament selection, preferring the individual with the smaller squeezing factor.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESA Selection";
    }

    /**
     * You can choose the tournament size.
     */
    public String tournamentSizeTipText() {
        return "Choose the tournament size.";
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(int g) {
        tournamentSize = g;
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
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
}
