package javaeva.server.go.operators.selection;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.selection.probability.InterfaceSelectionProbability;
import javaeva.server.go.operators.selection.probability.SelProbStandard;
import javaeva.server.go.populations.Population;
import javaeva.server.go.tools.RandomNumberGenerator;


class treeElement implements java.io.Serializable {
    public double           separator = 0;
    public int              m_Index = -1;
    public treeElement      m_Left = null, m_Right = null;

    public treeElement(double[][] d, int list, int low, int high) {
        //System.out.println("Calling Low/high: "+low+"/"+high);
        if (low == high) {
            // end reached
            //System.out.println("This: "+low);
            this.m_Index = low;
        } else {
            if (low == high-1) {
                //System.out.println("This: "+high);
                this.m_Index = high;
            } else {
                int midPoint = (int)((high+low)/2);
                this.separator = d[midPoint-1][list];
                //System.out.println("Branching: "+midPoint + " : " + this.separator);
                this.m_Left = new treeElement(d, list, low, midPoint);
                this.m_Right = new treeElement(d, list, midPoint, high);
            }
        }
    }

    public int getIndexFor(double d) {
        if (this.m_Index >= 0) return this.m_Index-1;
        else {
            if (d < this.separator) return this.m_Left.getIndexFor(d);
            else return this.m_Right.getIndexFor(d);
        }
    }

    public String toString() {
        if (this.m_Index >= 0) return "Ind:"+this.m_Index;
        else {
            return "{"+this.m_Left.toString()+"} X<"+this.separator+" {"+this.m_Right.toString()+"}";
        }
    }
}

/** The RoulettWheel selection requires a selection probability calculator.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 16:36:11
 * To change this template use Options | File Templates.
 */
public class SelectXProbRouletteWheel implements InterfaceSelection, java.io.Serializable {

    private treeElement[]                   m_TreeRoot;
    private InterfaceSelectionProbability   m_SelProbCalculator = new SelProbStandard();
    private boolean                         m_ObeyDebsConstViolationPrinciple = true;

    public SelectXProbRouletteWheel() {
    }

    public SelectXProbRouletteWheel(SelectXProbRouletteWheel a) {
        this.m_ObeyDebsConstViolationPrinciple  = a.m_ObeyDebsConstViolationPrinciple;
        this.m_SelProbCalculator    = (InterfaceSelectionProbability)a.m_SelProbCalculator.clone();
    }

    public Object clone() {
        return (Object) new SelectXProbRouletteWheel(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        this.m_SelProbCalculator.computeSelectionProbability(population, "Fitness", this.m_ObeyDebsConstViolationPrinciple);
        this.m_TreeRoot = this.buildSelectionTree(population);
    }

    /** This method will select a pool of indiviudals from the given
     * Population in respect to the selection propability of the
     * individuals.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        result.setPopulationSize(size);

        if (true) {
            //this.m_TreeRoot = this.buildSelectionTree(population);
            for (int i = 0; i < size; i++) {
                result.add(this.selectTree(population));
            }
        } else {
            for (int i = 0; i < size; i++) {
                result.add(this.selectStandard(population));
            }
        }

        return result;
    }

    /** This method will build a selection tree
     * @param p     The population
     */
    private treeElement[] buildSelectionTree(Population p) {
        treeElement result[];
        double[][]  tmpList = new double[p.size()][];

        for (int i = 0; i < p.size(); i++) {
            tmpList[i] = new double[((AbstractEAIndividual)(p.get(i))).getSelectionProbability().length];
            System.arraycopy(((AbstractEAIndividual)(p.get(i))).getSelectionProbability(), 0, tmpList[i], 0, tmpList[i].length);
            if (i > 0) {
                for (int j = 0; j < tmpList[i].length; j++) {
                    tmpList[i][j] += tmpList[i-1][j];
                }
            }
        }

        result = new treeElement[tmpList[0].length];
        for (int i = 0; i < tmpList[0].length; i++) {
            //String s = "Input: {";
            //for (int j = 0; j < tmpList.length; j++) s += tmpList[j][i] +"; ";
            //System.out.println(s+"}");
            result[i] = new treeElement(tmpList, i, 0, tmpList.length);
            //System.out.println("Resulting Tree: " + result[i].toString());
        }

        return result;
    }

    /** This method selects a single individual from the current population
     * @param population The population to select from
     */
    private AbstractEAIndividual selectTree(Population population) {
        int                     currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual)population.get(0)).getSelectionProbability().length;
        currentCriteria = RandomNumberGenerator.randomInt(0, critSize-1);
        double d = RandomNumberGenerator.randomDouble();
        int index = this.m_TreeRoot[currentCriteria].getIndexFor(d);
        //System.out.println("Looking for: " + d + " found " +index);
        return ((AbstractEAIndividual)(population.get(index)));
    }

    private AbstractEAIndividual selectStandard(Population population) {
        double                  sum = 1, random, tmpD;
        int                     currentCriteria = 0, critSize;

        critSize = ((AbstractEAIndividual)population.get(0)).getSelectionProbability().length;
        currentCriteria = RandomNumberGenerator.randomInt(0, critSize-1);
        String logger = "";
        while (sum > 0) {
            sum = 0;
            random = RandomNumberGenerator.randomDouble();
            for (int i = 0; i < population.size(); i++) {
                tmpD = ((AbstractEAIndividual)(population.get(i))).getSelectionProbability(currentCriteria);
                logger += tmpD + "; ";
                if (random < (sum + tmpD)) return ((AbstractEAIndividual)(population.get(i)));
                else sum += tmpD;
            }
        }
        System.out.println("Selection returns null, while computing: " + logger);

        System.out.println(population.getStringRepresentation());
        return null;
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
        return "Roulette Wheel Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method chooses individuals similar to the roulette wheel. The chance for each individual to be selected depends on the selection probability." +
                "This is a single objective selecting method, it will select in respect to a random criteria.";
    }

    /** This method will set the normation method that is to be used.
     * @param normation
     */
    public void setSelProbCalculator (InterfaceSelectionProbability normation) {
        this.m_SelProbCalculator = normation;
    }
    public InterfaceSelectionProbability getSelProbCalculator () {
        return this.m_SelProbCalculator;
    }
    public String selProbCalculatorTipText() {
        return "Select the normation method.";
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
