package eva2.optimization.operator.selection;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingPESAII;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The multi-objective PESA II selection criteria based on an n-dimensional
 * grid using a squeezing factor.
 */
public class SelectMOPESAII implements InterfaceSelection, java.io.Serializable {

    ArchivingPESAII PESAII = new ArchivingPESAII();
    int[] squeeze;
    int tournamentSize = 2;
    ArrayList gridBoxes;
    boolean obeyDebsConstViolationPrinciple = true;

    public SelectMOPESAII() {
    }

    public SelectMOPESAII(SelectMOPESAII a) {
        this.PESAII = new ArchivingPESAII();
        this.tournamentSize = a.tournamentSize;
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectMOPESAII(this);
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
        Hashtable tmpGridBoxes = new Hashtable();
        ArrayList tmp;
        AbstractEAIndividual tmpIndy;
        int[] gridBox;
        String tmpString;

        // first build the hashtable
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            gridBox = (int[]) tmpIndy.getData("GridBox");
            tmpString = "";
            for (int j = 0; j < gridBox.length; j++) {
                tmpString += gridBox[j] + "/";
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
        this.gridBoxes = new ArrayList();
        Enumeration myEnum = tmpGridBoxes.elements();
        while (myEnum.hasMoreElements()) {
            this.gridBoxes.add(myEnum.nextElement());
        }
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
        ArrayList box1, box2;
        int winner, tmp;

        try {
            box1 = (ArrayList) this.gridBoxes.get(RNG.randomInt(0, this.gridBoxes.size() - 1));
            box2 = (ArrayList) this.gridBoxes.get(RNG.randomInt(0, this.gridBoxes.size() - 1));
            if (((Integer) ((AbstractEAIndividual) box1.get(0)).getData("SqueezeFactor")).intValue()
                    < ((Integer) ((AbstractEAIndividual) box2.get(0)).getData("SqueezeFactor")).intValue()) {
                resultIndy = (AbstractEAIndividual) (box1.get(RNG.randomInt(0, box1.size() - 1)));
            } else {
                resultIndy = (AbstractEAIndividual) (box2.get(RNG.randomInt(0, box2.size() - 1)));
            }
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
     * @param availablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        return this.selectFrom(availablePartners, size);
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
        return "Performs a binary tournament selection, preferring the gridbox of smaller squeezing factor and selecting a random individual from the winner box.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "PESAII Selection";
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