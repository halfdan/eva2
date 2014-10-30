package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * The infamous NSGA-II selection scheme for multi-objective
 * optimization based on Pareto ranks and hybergrids.
 */
@Description("The Crowded Tournament Selection first chooses the better Pareto Front and then the smaller Crowding Distance.")
public class SelectMONSGAIICrowedTournament implements InterfaceSelection, java.io.Serializable {

    private int tournamentSize = 4;
    private ArchivingNSGAII NSGAII = new ArchivingNSGAII();
    private Population[] fronts;
    private boolean obeyDebsConstViolationPrinciple = true;


    public SelectMONSGAIICrowedTournament() {
    }

    public SelectMONSGAIICrowedTournament(SelectMONSGAIICrowedTournament a) {
        this.tournamentSize = a.tournamentSize;
        this.NSGAII = new ArchivingNSGAII();
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectMONSGAIICrowedTournament(this);
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
        this.fronts = this.NSGAII.getNonDominatedSortedFronts(population);
        this.NSGAII.calculateCrowdingDistance(this.fronts);
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
        AbstractEAIndividual result = null, tmpIndy;
        Population feasiblePop = new Population();
        Population infeasiblePop = new Population();
        int smallestLevel = Integer.MAX_VALUE, tmpL;
        double curCrowdingDistance, tmpCrowdingDistance;

        try {
            for (int i = 0; i < this.tournamentSize; i++) {
                tmpIndy = (AbstractEAIndividual) population.get(RNG.randomInt(0, population.size() - 1));
                tmpL = ((Integer) tmpIndy.getData("ParetoLevel")).intValue();
                if (tmpL < smallestLevel) {
                    smallestLevel = tmpL;
                }
                if (tmpIndy.getConstraintViolation() > 0) {
                    infeasiblePop.add(tmpIndy);
                } else {
                    feasiblePop.add(tmpIndy);
                }
            }
            if (feasiblePop.size() == 0) {
                // choose the least infeasible one
                int best = 0;
                for (int i = 1; i < infeasiblePop.size(); i++) {
                    if (((AbstractEAIndividual) infeasiblePop.get(i)).getConstraintViolation() < ((AbstractEAIndividual) infeasiblePop.get(best)).getConstraintViolation()) {
                        best = i;
                    }
                }
                return (AbstractEAIndividual) infeasiblePop.get(best);
            }
            // recalculate the smallest level for the feasible solutions
            smallestLevel = Integer.MAX_VALUE;
            for (int i = 0; i < feasiblePop.size(); i++) {
                tmpIndy = (AbstractEAIndividual) feasiblePop.get(i);
                tmpL = ((Integer) tmpIndy.getData("ParetoLevel")).intValue();
                if (tmpL < smallestLevel) {
                    smallestLevel = tmpL;
                }
            }
            // first remove all individual from tmpPop which are not of smallestLevel
            for (int i = 0; i < feasiblePop.size(); i++) {
                if (((Integer) ((AbstractEAIndividual) feasiblePop.get(i)).getData("ParetoLevel")).intValue() > smallestLevel) {
                    feasiblePop.remove(i);
                    i--;
                }
            }
            if (feasiblePop.size() == 1) {
                return (AbstractEAIndividual) feasiblePop.get(0);
            } else {
                // now find the one with the biggest crowding distance
                result = (AbstractEAIndividual) feasiblePop.get(0);
                curCrowdingDistance = ((Double) (result.getData("HyperCube"))).doubleValue();
                for (int i = 1; i < feasiblePop.size(); i++) {
                    tmpCrowdingDistance = ((Double) ((AbstractEAIndividual) feasiblePop.get(i)).getData("HyperCube")).doubleValue();
                    if (tmpCrowdingDistance > curCrowdingDistance) {
                        curCrowdingDistance = tmpCrowdingDistance;
                        result = (AbstractEAIndividual) feasiblePop.get(i);
                    }
                }
            }
        } catch (java.lang.IndexOutOfBoundsException e) {
            System.out.println("Tournament Selection produced IndexOutOfBoundsException!");
            System.out.println("Feasible population size   : " + feasiblePop.size());
            System.out.println("Infeasible population size : " + infeasiblePop.size());
            //System.out.println(""+e.getMessage());
            result = population.getBestEAIndividual();
        }
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
        return "MO Crowded Tournament Selection";
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