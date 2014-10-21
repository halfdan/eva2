package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.selection.probability.InterfaceSelectionProbability;
import eva2.optimization.operator.selection.probability.SelProbStandard;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * The RouletteWheel selection requires a selection probability calculator.
 * In case of multiple fitness values the selection
 * criteria is selected randomly for each selection event.
 */
@Description("This method chooses individuals similar to the roulette wheel. The chance for each individual to be selected depends on the selection probability." +
        "This is a single objective selecting method, it select with respect to a random criterion.")
public class SelectXProbRouletteWheel implements InterfaceSelection, java.io.Serializable {

    private transient TreeElement[] treeRoot = null;
    private InterfaceSelectionProbability selectionProbability = new SelProbStandard();
    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectXProbRouletteWheel() {
    }

    public SelectXProbRouletteWheel(SelectXProbRouletteWheel a) {
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
        this.selectionProbability = (InterfaceSelectionProbability) a.selectionProbability.clone();
    }

    @Override
    public Object clone() {
        return new SelectXProbRouletteWheel(this);
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
        this.selectionProbability.computeSelectionProbability(population, "Fitness", this.obeyDebsConstViolationPrinciple);
        this.treeRoot = this.buildSelectionTree(population);
    }

    /**
     * This method will select a pool of individuals from the given
     * Population in respect to the selection probability of the
     * individuals.
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
            result.add(this.selectTree(population));
        }

        return result;
    }

    /**
     * This method will build a selection tree.
     *
     * @param p The population
     */
    private TreeElement[] buildSelectionTree(Population p) {
        TreeElement result[];
        double[][] tmpList = new double[p.size()][];

        for (int i = 0; i < p.size(); i++) {
            tmpList[i] = new double[((AbstractEAIndividual) (p.get(i))).getSelectionProbability().length];
            System.arraycopy(((AbstractEAIndividual) (p.get(i))).getSelectionProbability(), 0, tmpList[i], 0, tmpList[i].length);
            if (i > 0) {
                for (int j = 0; j < tmpList[i].length; j++) {
                    tmpList[i][j] += tmpList[i - 1][j];
                }
            }
        }

        result = new TreeElement[tmpList[0].length];
        for (int i = 0; i < tmpList[0].length; i++) {
            //String s = "Input: {";
            //for (int j = 0; j < tmpList.length; j++) s += tmpList[j][i] +"; ";
            //System.out.println(s+"}");
            result[i] = new TreeElement(tmpList, i, 0, tmpList.length);
            //System.out.println("Resulting Tree: " + result[i].toString());
        }

        return result;
    }

    /**
     * This method selects a single individual from the current population
     *
     * @param population The population to select from
     */
    private AbstractEAIndividual selectTree(Population population) {
        int currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual) population.get(0)).getSelectionProbability().length;
        currentCriteria = RNG.randomInt(0, critSize - 1);
        double d = RNG.randomDouble();
        int index = this.treeRoot[currentCriteria].getIndexFor(d);
        //System.out.println("Looking for: " + d + " found " +index);
        return ((AbstractEAIndividual) (population.get(index)));
    }

    private AbstractEAIndividual selectStandard(Population population) {
        // old version
        double sum = 1, random, tmpD;
        int currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual) population.get(0)).getSelectionProbability().length;
        currentCriteria = RNG.randomInt(0, critSize - 1);
        String logger = "";
        while (sum > 0) {
            sum = 0;
            random = RNG.randomDouble();
            for (int i = 0; i < population.size(); i++) {
                tmpD = ((AbstractEAIndividual) (population.get(i))).getSelectionProbability(currentCriteria);
                logger += tmpD + "; ";
                if (random < (sum + tmpD)) {
                    return ((AbstractEAIndividual) (population.get(i)));
                } else {
                    sum += tmpD;
                }
            }
        }
        System.out.println("Selection returns null, while computing: " + logger);

        System.out.println(population.getStringRepresentation());
        return null;
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
        return "Roulette Wheel Selection";
    }

    /**
     * This method will set the normation method that is to be used.
     *
     * @param normation
     */
    public void setSelProbCalculator(InterfaceSelectionProbability normation) {
        this.selectionProbability = normation;
    }

    public InterfaceSelectionProbability getSelProbCalculator() {
        return this.selectionProbability;
    }

    public String selProbCalculatorTipText() {
        return "Select the normation method.";
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


class TreeElement implements java.io.Serializable {
    public double separator = 0;
    public int index = -1;
    public TreeElement leftElement = null, rightElement = null;

    public TreeElement(double[][] d, int list, int low, int high) {
        //System.out.println("Calling Low/high: "+low+"/"+high);
        if (low == high) {
            // end reached
            //System.out.println("This: "+low);
            this.index = low;
        } else {
            if (low == high - 1) {
                //System.out.println("This: "+high);
                this.index = high;
            } else {
                int midPoint = (high + low) / 2;
                this.separator = d[midPoint - 1][list];
                //System.out.println("Branching: "+midPoint + " : " + this.separator);
                this.leftElement = new TreeElement(d, list, low, midPoint);
                this.rightElement = new TreeElement(d, list, midPoint, high);
            }
        }
    }

    public int getIndexFor(double d) {
        if (this.index >= 0) {
            return this.index - 1;
        } else {
            if (d < this.separator) {
                return this.leftElement.getIndexFor(d);
            } else {
                return this.rightElement.getIndexFor(d);
            }
        }
    }

    @Override
    public String toString() {
        if (this.index >= 0) {
            return "Ind:" + this.index;
        } else {
            return "{" + this.leftElement.toString() + "} X<" + this.separator + " {" + this.rightElement.toString() + "}";
        }
    }
}