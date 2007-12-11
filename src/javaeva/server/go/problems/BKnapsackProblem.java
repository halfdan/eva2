package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.GAIndividualBinaryData;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 21.03.2003
 * Time: 15:12:46
 * To change this template use Options | File Templates.
 */
public class BKnapsackProblem extends AbstractOptimizationProblem implements java.io.Serializable {

    private int                         m_Limit         = 5000;
    private double                      m_Punish        = 2.0;
    private double                      m_LocalSearch   = 0.0;
    private boolean                     m_Lamarkism     = false;
    private double                      m_ProblemSpecificInit   = 0.0;
    static final int[][] items = {
        {334,-328},
        {303,-291},
        {228,-232},
        {229,-226},
        {346,-347},
        {256,-257},
        {243,-243},
        {334,-328},
        {382,-377},
        {236,-234},
        {301,-295},
        {215,-209},
        {298,-305},
        {313,-313},
        {219,-212},
        {215,-214},
        {250,-244},
        {380,-373},
        {319,-317},
        {392,-396},
        {304,-304},
        {249,-252},
        {337,-331},
        {368,-365},
        {356,-354},
        {267,-260},
        {258,-251},
        {248,-252},
        {391,-391},
        {277,-273},
        {293,-297},
        {213,-214},
        {223,-218},
        {353,-359},
        {337,-338},
        {389,-396},
        {251,-256},
        {344,-340},
        {243,-236},
        {368,-367},
        {380,-378},
        {274,-269},
        {289,-291},
        {346,-353},
        {271,-268},
        {286,-281},
        {294,-292},
        {279,-282},
        {226,-221},
        {344,-337},
        {353,-352},
        {266,-265},
        {391,-393},
        {202,-199},
        {224,-223},
        {333,-330},
        {369,-368},
        {301,-308},
        {241,-237},
        {240,-233},
        {236,-238},
        {217,-212},
        {308,-308},
        {324,-320},
        {218,-220},
        {356,-358},
        {236,-234},
        {310,-299},
        {382,-382},
        {350,-357},
        {377,-382},
        {359,-351},
        {322,-321},
        {351,-347},
        {315,-309},
        {389,-382},
        {212,-213},
        {212,-209},
        {352,-352},
        {217,-215},
        {369,-366},
        {398,-399},
        {320,-323},
        {343,-353},
        {329,-322},
        {338,-332},
        {289,-294},
        {341,-339},
        {201,-194},
        {234,-235},
        {392,-399},
        {344,-347},
        {371,-380},
        {286,-288},
        {210,-219},
        {220,-218},
        {253,-247},
        {392,-390},
        {319,-324},
        {392,-386}};

    public BKnapsackProblem() {
        this.m_Template         = new GAIndividualBinaryData();
    }
    public BKnapsackProblem(BKnapsackProblem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        // BKnapsackProblem
        this.m_Limit            = b.m_Limit;
        this.m_Punish           = b.m_Punish;
        this.m_LocalSearch      = b.m_LocalSearch;
        this.m_Lamarkism        = b.m_Lamarkism;
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new BKnapsackProblem(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        // nothing to init here
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        population.clear();

        ((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(items.length);
        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            if (RandomNumberGenerator.flipCoin(this.m_ProblemSpecificInit)) {
                BitSet tmpSet = new BitSet();
                tmpSet.clear();

                while (evaluate(tmpSet, items.length)[1] > 0) {
                    tmpSet.set(RandomNumberGenerator.randomInt(0,items.length-1));
                }
                ((InterfaceDataTypeBinary)tmpIndy).SetBinaryDataLamarkian(tmpSet);
            }
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }

    /** This method evaluates a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        BitSet          tmpBitSet;
        double[]        result;
        InterfaceDataTypeBinary    tmpIndy;

        tmpIndy     = (InterfaceDataTypeBinary) individual;
        tmpBitSet   = tmpIndy.getBinaryData();
        result = this.evaluate(tmpBitSet, tmpIndy.size());
        if (RandomNumberGenerator.flipCoin(this.m_LocalSearch)) {
            // first remove surplus assets
            while (result[1] > 0) {
                // search for an element to replace
                int weakest = tmpBitSet.nextSetBit(0);
                for (int i = 0; i < items.length; i++) {
                    if (tmpBitSet.get(i)) {
                        if ( ((-items[i][1])/((double)items[i][0])) < ((-items[weakest][1])/((double)items[weakest][0])) ) {
                            weakest = i;
                        }
                        if ( ((-items[i][1])/((double)items[i][0])) == ((-items[weakest][1])/((double)items[weakest][0])) ) {
                            if (RandomNumberGenerator.flipCoin(0.5)) weakest = i;
                        }
                    }
                }
                // remove the weakest
                tmpBitSet.clear(weakest);
                result = this.evaluate(tmpBitSet, tmpIndy.size());
            }
            // now lets see if we can replace some guy with a more efficient one
            int weakest = tmpBitSet.nextSetBit(0);
            if (weakest >= 0) {
                int stronger = -1;
                for (int i = 0; i < items.length; i++) {
                    if (tmpBitSet.get(i)) {
                        if ( ((-items[i][1])/((double)items[i][0])) < ((-items[weakest][1])/((double)items[weakest][0])) ) {
                            weakest = i;
                        }
                        if ( ((-items[i][1])/((double)items[i][0])) == ((-items[weakest][1])/((double)items[weakest][0])) ) {
                            if (RandomNumberGenerator.flipCoin(0.5)) weakest = i;
                        }
                    }
                }

                tmpBitSet.clear(weakest);
                result = this.evaluate(tmpBitSet, tmpIndy.size());
                int weight = 0;
                for (int i = 0; i < items.length; i++) {
                    if (tmpBitSet.get(i)) weight  += items[i][0];
                }

                for (int i = 0; i < items.length; i++) {
                    if (items[i][0] < this.m_Limit - weight) {
                        // possible candidate
                        if (stronger < 0) stronger = i;
                        else {
                            if (items[i][1] < items[stronger][1]) stronger = i;
                        }
                    }
                }
                if (stronger >= 0) tmpBitSet.set(stronger);
                result = this.evaluate(tmpBitSet, tmpIndy.size());
            }

            if (this.m_Lamarkism) {
                tmpIndy.SetBinaryDataLamarkian(tmpBitSet);
            }
        }
        result[0] += 5100;
        individual.SetFitness(0, result[0]);
    }

    /** This is a simple method that evaluates a given Individual. The fitness
     * values of the individual will be set inside this method.
     * @param b         The BitSet that is to be evaluated.
     * @param l         The length of the BitSet.
     * @return          Double[]
     */
    public double[] evaluate(BitSet b, int l) {
        double[]                result = new double[3];

        result[0] = 0;
        result[1] = 0; // the weight exceed
        result[2] = 0; // net worth
        for (int i = 0; i < l; i++) {
            if (b.get(i)) {
                result[1]  += items[i][0];
                result[2]  += items[i][1];
            } else b.clear(i);
        }
        // write the solution
        result[1] = Math.max(0, result[1]- this.m_Limit);
        result[0] = (this.m_Punish * result[1]) + result[2];
        return result;
    }

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suiteable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
        BitSet          tmpBitSet;
        double[]        report;
        InterfaceDataTypeBinary    tmpIndy;
        String result = "Single Knapsack problem:\n";

        tmpIndy     = (InterfaceDataTypeBinary) individual;
        tmpBitSet   = tmpIndy.getBinaryData();
        report = this.evaluate(tmpBitSet, tmpIndy.size());
        result += individual.getStringRepresentation() + "\n";
        result += "Is worth: " + Math.abs(report[2]) + " and ";
        if (report[1] == 0) result += "does not exceed the weight limit!";
        else result += "exceeds the weight limit by " + report[1];
        result += "\n";
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer result = new StringBuffer(100);

        result.append("Knapsack Problem:\n");
        result.append("The task is to find a packing for a knapsack with limited size(");
        result.append(this.m_Limit);
        result.append(") and maximal value.\n");
        result.append("The default setting with limit=5000 allows a knapsack with value 5100. Note: value reads negative.");
        result.append("Available items {(weight/value),...}: {");
        for (int i = 0; i < BKnapsackProblem.items.length; i++) {
            result.append("(");
            result.append(BKnapsackProblem.items[i][0]);
            result.append("; ");
            result.append(BKnapsackProblem.items[i][1]);
            result.append("),");
        }
        result.append("}\n");
        result.append("Parameters:\n");
        result.append("Punish rate: ");
        result.append(this.m_Punish);
        result.append("\n");
        result.append("Solution representation:\n");
        //result += this.m_Template.getSolutionRepresentationFor();
        return result.toString();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Single Knapsack Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Maximize the value of the Knapsack without exceeding the weight limit of the knapsack.";
    }

    /** This method allows you to set the number of mulitruns that are to be performed,
     * necessary for stochastic optimizers to ensure reliable results.
     * @param punish The number of multiruns that are to be performed
     */
    public void setPunishment(double punish) {
        this.m_Punish = punish;
    }
    public double getPunishment() {
        return this.m_Punish;
    }
    public String punishmentTipText() {
        return "Rate of punishment if the Knapsack exceeds the weight limit.";
    }
    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeBinary indy) {
        this.m_Template = (AbstractEAIndividual) indy;
    }
    public InterfaceDataTypeBinary getEAIndividual() {
        return (InterfaceDataTypeBinary) this.m_Template;
    }
    public String eAIndividualTipText() {
        return "Choose the EAIndividual to use.";
    }
    /** This method allows you to toggle the problemspecific
     * initialization method, gives the chance of problem
     * specific initialization
     * @param b gives the chance of problemspecific initialization.
     */
    public void setProblemSpecificInit(double b) {
        this.m_ProblemSpecificInit = b;
    }
    public double getProblemSpecificInit() {
        return this.m_ProblemSpecificInit;
    }
    public String problemSpecificInitTipText() {
        return "Gives the chance of problemspecific initialization.";
    }
    /** This method allows you to toggle the problemspecific
     * local search method, gives the chance of problem
     * specific local search
     * @param b gives the chance of problemspecific local search.
     */
    public void setLocalSearch(double b) {
        this.m_LocalSearch = b;
    }
    public double getLocalSearch() {
        return this.m_LocalSearch;
    }
    public String localSearchTipText() {
        return "Gives the chance of local search.";
    }
    /** This method allows you to toggle the use of Lamarkism.
     * @param b toggles lamarkism.
     */
    public void setLamarkism(boolean b) {
        this.m_Lamarkism = b;
    }
    public boolean getLamarkism() {
        return this.m_Lamarkism;
    }
    public String lamarkismTipText() {
        return "Lamarkism alters the genotype after the local search.";
    }
}
