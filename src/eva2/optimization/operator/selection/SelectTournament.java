package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/**
 * Tournament selection within a given tournament group size,
 * also scaling invariant.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 */
public class SelectTournament implements InterfaceSelection, java.io.Serializable {

    private int tournamentSize = 4;
    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectTournament() {
    }

    public SelectTournament(int tSize) {
        this();
        setTournamentSize(tSize);
    }

    public SelectTournament(SelectTournament a) {
        this.tournamentSize = a.tournamentSize;
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectTournament(this);
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
        AbstractEAIndividual result, tmpIndy;
        int currentCriteria = 0, critSize = 0;

        try {
            critSize = ((AbstractEAIndividual) population.get(0)).getFitness().length;
            currentCriteria = RNG.randomInt(0, critSize - 1);
            if (this.obeyDebsConstViolationPrinciple) {
                Population tournamentGroup = new Population();
                for (int i = 0; i < this.tournamentSize; i++) {
                    tournamentGroup.add(population.get(RNG.randomInt(0, population.size() - 1)));
                }
                SelectBestIndividuals best = new SelectBestIndividuals();
                best.setObeyDebsConstViolationPrinciple(true);
                result = (AbstractEAIndividual) best.selectFrom(tournamentGroup, 1).get(0);
            } else {
                result = (AbstractEAIndividual) population.get(RNG.randomInt(0, population.size() - 1));
                for (int i = 1; i < this.tournamentSize; i++) {
                    tmpIndy = (AbstractEAIndividual) population.get(RNG.randomInt(0, population.size() - 1));
                    if (tmpIndy.getFitness(currentCriteria) < result.getFitness(currentCriteria)) {
                        result = tmpIndy;
                    }
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

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad              The already seleceted parent
     * @param availablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        return this.selectFrom(availablePartners, size);
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Tournament Selection";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The tournament selection compares the raw fitness of n individuals and takes the best." +
                "This is a single-objective method, it selects with respect to the first criterion in the multi-objective case.";
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
