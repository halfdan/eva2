package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.gp.AbstractGPNode;
import eva2.optimization.individuals.codings.gp.GPArea;
import eva2.optimization.individuals.codings.gp.InterfaceProgram;
import eva2.optimization.operator.crossover.CrossoverGADefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateDefault;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * This individual uses a binary genotype to code for a tree-based representation
 * using a BNF grammar, see also Grammatical Evolution.
 */
@Description(value = "This is a GE individual suited to optimize programs.")
public class GEIndividualProgramData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeProgram, java.io.Serializable {

    protected GPArea[] gpAreas;
    protected double initFullGrowRatio = 0.5;
    protected int initDepth = 5;
    protected int targetDepth = 10;
    protected boolean checkTargetDepth = true;

    protected BitSet genotype;
    protected AbstractGPNode[] phenotype;
    protected int genotypeLengthPerProgram = 240; // this is the overall length
    protected int maxNumberOfNodes = 80;
    protected int numberOfBitPerInt = 6;
    protected int currentIndex = 0;
    protected int currentNumberOfNodes = 0;
    protected Object[][] rules;

    public GEIndividualProgramData() {
        this.gpAreas = new GPArea[1];
        this.genotypeLengthPerProgram = 240;
        this.genotype = new BitSet();
        this.mutationOperator = new MutateDefault();
        this.crossoverOperator = new CrossoverGADefault();
        this.mutationProbability = 0.5;
        this.crossoverProbability = 0.5;
    }

    public GEIndividualProgramData(GEIndividualProgramData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new AbstractGPNode[individual.phenotype.length];
            for (int i = 0; i < individual.phenotype.length; i++) {
                this.phenotype[i] = (AbstractGPNode) individual.phenotype[i].clone();
            }
        }
        this.genotypeLengthPerProgram = individual.genotypeLengthPerProgram;
        this.maxNumberOfNodes = individual.maxNumberOfNodes;
        this.numberOfBitPerInt = individual.numberOfBitPerInt;
        this.currentIndex = individual.currentIndex;
        if (individual.genotype != null) {
            this.genotype = (BitSet) individual.genotype.clone();
        }
        if (individual.gpAreas != null) {
            this.gpAreas = new GPArea[individual.gpAreas.length];
            for (int i = 0; i < this.gpAreas.length; i++) {
                this.gpAreas[i] = (GPArea) individual.gpAreas[i].clone();
            }
        }
        // User                     : "Copy the rules set!"
        // GEIndividualProgramData  : "Naay! I wanna go playing with my friends... !"
        if (individual.rules != null) {
            this.rules = new Object[individual.rules.length][];
            for (int t = 0; t < this.rules.length; t++) {
                this.rules[t] = new Object[individual.rules[t].length];
                int[][] copyRulz, orgRulz = (int[][]) individual.rules[t][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.rules[t][0] = copyRulz;
                AbstractGPNode[] copyNode, orgNode;
                for (int i = 1; i < this.rules[t].length; i++) {
                    orgNode = (AbstractGPNode[]) individual.rules[t][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.rules[t][i] = copyNode;
                }
            }
        }
        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new GEIndividualProgramData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GEIndividualProgramData) {
            GEIndividualProgramData indy = (GEIndividualProgramData) individual;
            if (this.genotypeLengthPerProgram != indy.genotypeLengthPerProgram) {
                return false;
            }
            if (this.maxNumberOfNodes != indy.maxNumberOfNodes) {
                return false;
            }
            if (this.numberOfBitPerInt != indy.numberOfBitPerInt) {
                return false;
            }
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            return this.genotype.equals(indy.genotype);
        } else {
            return false;
        }
    }

    /**
     * This method compiles the area
     */
    private void compileArea() {
        if (this.gpAreas == null) {
            this.rules = null;
            return;
        }
        //this.rules = new Object[this.gpArea.length][];
        for (int t = 0; t < this.gpAreas.length; t++) {
            // first lets find out what kind of elements are available
            int arity, maxArity = 0;

            // first find out the max arity in the GPArea
            this.gpAreas[t].compileReducedList();
            ArrayList area = this.gpAreas[t].getReducedList();
            for (int i = 0; i < area.size(); i++) {
                arity = ((AbstractGPNode) area.get(i)).getArity();
                if (arity > maxArity) {
                    maxArity = arity;
                }
            }

            // Now i get a sorted list
            ArrayList[] directList = new ArrayList[maxArity + 1];
            for (int i = 0; i < directList.length; i++) {
                directList[i] = new ArrayList();
            }
            for (int i = 0; i < area.size(); i++) {
                directList[((AbstractGPNode) area.get(i)).getArity()].add(area.get(i));
            }
            // Now write the rules
            this.rules[t] = new Object[maxArity + 2];
            // the first rule describes how to decode an <expr>
            int numberOfRules = 0, index = 0;
            int[] tmpRule;
            int[][] tmpExpr = new int[directList.length + 1][];
            for (int i = 0; i < directList.length; i++) {
                tmpRule = new int[i + 1];
                if (i == 0) {
                    // this is a <var>
                    tmpRule[0] = 1;
                } else {
                    // this is a <opX> <expr> <expr>....
                    if (directList[i].size() > 0) {
                        tmpRule[0] = i + 1;
                        for (int j = 1; j < i + 1; j++) {
                            tmpRule[j] = 0;
                        }
                    } else {
                        tmpRule = null;
                    }
                }
                tmpExpr[i] = tmpRule;
                if (tmpRule != null) {
                    numberOfRules++;
                }
            }
            // Now get rid of the null rules
            int[][] trueExpr = new int[numberOfRules][];

            for (int i = 0; i < tmpExpr.length; i++) {
                if (tmpExpr[i] != null) {
                    trueExpr[index] = tmpExpr[i];
                    index++;
                }
            }
            this.rules[t][0] = trueExpr;

            // now the rules that define <var>, <op1>, <op2>, ....
            AbstractGPNode[] tmpListOfGPNodes;
            for (int i = 0; i < directList.length; i++) {
                tmpListOfGPNodes = new AbstractGPNode[directList[i].size()];
                for (int j = 0; j < directList[i].size(); j++) {
                    tmpListOfGPNodes[j] = (AbstractGPNode) directList[i].get(j);
                }
                this.rules[t][i + 1] = tmpListOfGPNodes;
            }
            // this should be the complete rules set
            //this.printRuleSet();
        }
    }

    /**
     * This method will print the currently used rule set
     */
    private void printRuleSet() {
        String result = "";
        AbstractGPNode[] tmpNodes;
        for (int t = 0; t < this.gpAreas.length; t++) {
            // first the Non-Terminals
            result += "N \t := \t{";
            for (int i = 0; i < this.rules[t].length; i++) {
                if (i == 0) {
                    result += "expr, ";
                } else {
                    if (i == 1) {
                        result += "var, ";
                    } else {
                        if (((AbstractGPNode[]) this.rules[t][i]).length > 0) {
                            result += "op" + (i - 1) + ", ";
                        }
                    }
                }
            }
            result += "}\n";
            // then the Ternimnals
            result += "T \t := \t{";
            this.gpAreas[t].compileReducedList();
            ArrayList area = this.gpAreas[t].getReducedList();
            for (int i = 0; i < area.size(); i++) {
                result += ((AbstractGPNode) area.get(i)).getStringRepresentation() + ", ";
            }
            result += "}\n";
            // now the S
            result += "S \t := \t<expr>\n\n";

            // now the rules
            for (int i = 0; i < this.rules[t].length; i++) {
                if (i == 0) {
                    // the first rules
                    result += "0. \t := \t<expr> \t::\t";
                    System.out.println("i: " + i);
                    int[][] rulz = (int[][]) this.rules[t][i];
                    for (int j = 0; j < rulz.length; j++) {
                        result += this.getRuleString(rulz[j]) + "\n";
                        if ((j + 1) < rulz.length) {
                            result += "\t \t \t \t \t \t";
                        }
                    }
                } else {
                    // now the rules for the terminals
                    if (i == 1) {
                        // These are the GP-Terminals
                        tmpNodes = (AbstractGPNode[]) this.rules[t][i];
                        result += "1. \t := \t<var> \t::\t" + tmpNodes[0].getStringRepresentation() + "\n";
                        for (int j = 1; j < tmpNodes.length; j++) {
                            result += "\t \t \t \t \t \t" + tmpNodes[j].getStringRepresentation() + "\n";
                        }
                    } else {
                        // These are the GP-Functions
                        tmpNodes = (AbstractGPNode[]) this.rules[t][i];
                        if (tmpNodes.length > 0) {
                            result += i + ". \t := \t<op" + (i - 1) + "> \t::\t" + tmpNodes[0].getStringRepresentation() + "\n";
                            for (int j = 1; j < tmpNodes.length; j++) {
                                result += "\t \t \t \t \t \t" + tmpNodes[j].getStringRepresentation() + "\n";
                            }
                        }
                    }
                }
            }
            result += "\n";
        }

        // Now print the result:
        System.out.println("" + result);
    }


    /**
     * This method returns a string for the BitSet
     *
     * @return A String
     */
    private String getBitSetString() {
        String result = "";
        result += "{";
        for (int i = 0; i < this.genotypeLengthPerProgram * this.gpAreas.length; i++) {
            if (i % this.numberOfBitPerInt == 0) {
                result += " ";
            }
            if (this.genotype.get(i)) {
                result += "1";
            } else {
                result += "0";
            }
        }
        result += "}";
        return result;
    }

    /**
     * This method returns a String for a given rule
     *
     * @param rule The rulz to transform into a string
     * @return String
     */
    private String getRuleString(int[] rule) {
        String result = "";
        for (int k = 0; k < rule.length; k++) {
            if (rule[k] == 0) {
                result += "<expr> ";
            }
            if (rule[k] == 1) {
                result += "<var> ";
            }
            if (rule[k] > 1) {
                result += "<op" + (rule[k] - 1) + "> ";
            }
        }
        return result;
    }

/************************************************************************************
 * InterfaceDataTypeProgram methods
 */
    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setProgramDataLength(int length) {
        GPArea[] oldArea = this.gpAreas;
        Object[][] oldRulz = this.rules;

        this.gpAreas = new GPArea[length];
        for (int t = 0; ((t < this.gpAreas.length) && (t < oldArea.length)); t++) {
            this.gpAreas[t] = oldArea[t];
        }
        for (int t = oldArea.length; t < this.gpAreas.length; t++) {
            this.gpAreas[t] = oldArea[oldArea.length - 1];
        }
        this.rules = new Object[length][];
        if (oldRulz == null) {
            return;
        }
        for (int t = 0; ((t < this.gpAreas.length) && (t < oldArea.length)); t++) {
            if (oldRulz[t] != null) {
                this.rules[t] = new Object[oldRulz[t].length];
                int[][] copyRulz, orgRulz = (int[][]) oldRulz[t][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.rules[t][0] = copyRulz;
                AbstractGPNode[] copyNode, orgNode;
                for (int i = 1; i < this.rules[t].length; i++) {
                    orgNode = (AbstractGPNode[]) oldRulz[t][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.rules[t][i] = copyNode;
                }
            }
        }
        for (int t = oldArea.length; t < this.gpAreas.length; t++) {
            if (oldRulz[oldArea.length - 1] != null) {
                this.rules[t] = new Object[oldRulz[oldArea.length - 1].length];
                int[][] copyRulz, orgRulz = (int[][]) oldRulz[oldArea.length - 1][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.rules[t][0] = copyRulz;
                AbstractGPNode[] copyNode, orgNode;
                for (int i = 1; i < this.rules[t].length; i++) {
                    orgNode = (AbstractGPNode[]) oldRulz[oldArea.length - 1][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.rules[t][i] = copyNode;
                }
            }
        }
    }

    /**
     * This method will fetch the next int value from the BitSet. If necessary the
     * method will continue at the beginning of the BitSet if genotype length is
     * exceeded.
     * Note: You need to set the current ReadingIndx = 0 before starting to decode
     * the BitSet
     *
     * @param t The index of the program.
     * @return The int value
     */
    private int decodeNextInt(int t) {
        int result = 0;
        for (int i = 0; i < this.numberOfBitPerInt; i++) {
            if (this.genotype.get(this.currentIndex + (t * this.genotypeLengthPerProgram))) {
                result += Math.pow(2, i);
            }
            this.currentIndex++;
            if (this.currentIndex >= (t + 1) * this.genotypeLengthPerProgram) {
                this.currentIndex = t * this.genotypeLengthPerProgram;
            }
        }
        return result;
    }

    /**
     * This method will decode a GPNode from the BitSet
     *
     * @param t    The index of the program
     * @param mode The modex
     * @return GPNode
     */
    private AbstractGPNode decodeGPNode(int t, int mode) {
        AbstractGPNode result = null;
        int value = this.decodeNextInt(t);

//        System.out.println("Decoding mode: " + mode);

        if (mode == 0) {
            int[][] rulz = (int[][]) this.rules[t][0];
            int[] myRule = rulz[value % rulz.length];
//            System.out.print("Value % rulz : "+ value +" % " + rulz.length + " = " +(value%rulz.length));
//            System.out.println(" => my rule " + this.getRuleString(myRule));
            this.currentNumberOfNodes += myRule.length;
            if ((this.currentNumberOfNodes + myRule.length) > this.maxNumberOfNodes) {
                // no i have to limit the number of nodes
                myRule = rulz[0];
//                System.out.println("Limiting to "+ this.getRuleString(myRule));
            }
            result = this.decodeGPNode(t, myRule[0]);
            result.initNodeArray();
            for (int i = 0; i < result.getArity(); i++) {
                result.setNode(this.decodeGPNode(t, myRule[i + 1]), i);
            }
        } else {
            AbstractGPNode[] availableNodes = (AbstractGPNode[]) this.rules[t][mode];
//            System.out.print("Choosing a terminal : "+ value +" % " + availableNodes.length + " = " +(value%availableNodes.length));
//            System.out.println(" => " +availableNodes[value % availableNodes.length].getStringRepresentation());
            result = (AbstractGPNode) availableNodes[value % availableNodes.length].clone();
        }
        return result;
    }

    /**
     * This method allows you to read the program stored as Koza style node tree
     *
     * @return GPNode representing the binary data.
     */
    @Override
    public InterfaceProgram[] getProgramData() {
//        if (true) {
//            String test ="GE decoding:\n";
//            test += this.getBitSetString() +"\n{";
//            this.currentIndex = 0;
//            for (int i = 0; i < this.maxNumberOfNodes; i++) {
//                test += this.decodeNextInt();
//                if ((i + 1) < this.maxNumberOfNodes) test += "; ";
//            }
//            test += "}\n";
//            System.out.println(""+test);
//        }
        // lets decode the stuff!
        if (this.rules == null) {
            this.compileArea();
            if (this.rules == null) {
                return null;
            }
        }
        this.currentIndex = 0;
        this.currentNumberOfNodes = 0;
        int mode = 0;
        this.phenotype = new AbstractGPNode[this.gpAreas.length];
        for (int t = 0; t < this.gpAreas.length; t++) {
            mode = 0;
            this.currentIndex = t * this.genotypeLengthPerProgram;
            this.currentNumberOfNodes = 0;
            this.phenotype[t] = this.decodeGPNode(t, mode);
        }
//        System.out.println("Decoded: ");
//        System.out.println(""+ result.getStringRepresentation());
        return this.phenotype;
    }

    /**
     * This method allows you to read the Program data without
     * an update from the genotype
     *
     * @return InterfaceProgram[] representing the Program.
     */
    @Override
    public InterfaceProgram[] getProgramDataWithoutUpdate() {
        return this.phenotype;
    }

    /**
     * This method allows you to set the program phenotype.
     *
     * @param program The new program.
     */
    @Override
    public void SetProgramPhenotype(InterfaceProgram[] program) {
        if (program instanceof AbstractGPNode[]) {
            this.phenotype = new AbstractGPNode[program.length];
            for (int t = 0; t < program.length; t++) {
                this.phenotype[t] = (AbstractGPNode) ((AbstractGPNode) program[t]).clone();
            }
        }
    }

    /**
     * Warning - this is not implemented, it only sets the phenotype using SetProgramData.
     *
     * @param program The new program.
     */
    @Override
    public void SetProgramGenotype(InterfaceProgram[] program) {
        this.SetProgramPhenotype(program);
        if (program instanceof AbstractGPNode[]) {
            System.err.println("Warning setProgram() for GEIndividualProgramData not implemented!");
        }
    }

    /**
     * This method allows you to set the function area
     *
     * @param area The area contains functions and terminals
     */
    @Override
    public void SetFunctionArea(Object[] area) {
        if (area instanceof GPArea[]) {
            this.gpAreas = new GPArea[area.length];
            for (int t = 0; t < this.gpAreas.length; t++) {
                this.gpAreas[t] = (GPArea) area[t];
            }
            this.compileArea();
        }
    }

    /**
     * This method allows you to set the function area
     *
     * @return The function area
     */
    @Override
    public Object[] getFunctionArea() {
        return this.gpAreas;
    }

/************************************************************************************
 * InterfaceEAIndividual methods
 */
    /**
     * This method will initialize the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof InterfaceProgram) {
            this.SetProgramGenotype((InterfaceProgram[]) obj);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for GPIndividualDoubleData is no InterfaceProgram[]!");
        }
        this.mutationOperator.initialize(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /**
     * This method will return a string description of the GAIndividal
     * noteably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "GEIndividual coding program:\n";
        result += "{";
        for (int i = 0; i < this.genotypeLengthPerProgram * this.gpAreas.length; i++) {
            if (this.genotype.get(i)) {
                result += "1";
            } else {
                result += "0";
            }
        }
        result += "}\n";
        InterfaceProgram[] data = this.getProgramData();
        for (int i = 0; i < data.length; i++) {
            result += data[i].getStringRepresentation();
        }
        return result;
    }

    /************************************************************************************
     * InterfaceGAIndividual methods
     */

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBGenotype() {
        return this.genotype;
    }

    /**
     * This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     *
     * @param binaryData The new binary data.
     */
    @Override
    public void setBGenotype(BitSet binaryData) {
        this.genotype = binaryData;
    }

    /**
     * This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     *
     * @return The length of the genotype.
     */
    @Override
    public int getGenotypeLength() {
        return this.genotypeLengthPerProgram * this.gpAreas.length;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        for (int i = 0; i < this.genotypeLengthPerProgram * this.gpAreas.length; i++) {
            if (RNG.flipCoin(0.5)) {
                this.genotype.set(i);
            } else {
                this.genotype.clear(i);
            }
        }
    }

    /**
     * This method performs a simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.genotypeLengthPerProgram * this.gpAreas.length);
        //if (mutationIndex > 28) System.out.println("Mutate: " + this.getSolutionRepresentationFor());
        if (this.genotype.get(mutationIndex)) {
            this.genotype.clear(mutationIndex);
        } else {
            this.genotype.set(mutationIndex);
        }
        //if (mutationIndex > 28) System.out.println(this.getSolutionRepresentationFor());
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "GE individual";
    }

    /**
     * This method allows you to set the length of the binary genotype
     *
     * @param size The length
     */
    public void setGenotypeLengthPerProgram(int size) {
        this.genotypeLengthPerProgram = size;
    }

    public int getGenotypeLengthPerProgram() {
        return this.genotypeLengthPerProgram;
    }

    public String genotypeLengthPerProgramTipText() {
        return "Choose the length of the genotype.";
    }

    /**
     * This method allows you to set the maximum number of
     * nodes allowed for the program.
     *
     * @param nodes The maximum number of nodes
     */
    public void setMaxNumberOfNodes(int nodes) {
        this.maxNumberOfNodes = nodes;
    }

    public int getMaxNumberOfNodes() {
        return this.maxNumberOfNodes;
    }

    public String maxNumberOfNodesTipText() {
        return "Set the maximum number of nodes for the program.";
    }

    /**
     * This method allows you to set the number of bits per int value
     * stored on the BitSet
     *
     * @param length The number of bits per int.
     */
    public void setNumberOfBitPerInt(int length) {
        this.numberOfBitPerInt = length;
    }

    public int getNumberOfBitPerInt() {
        return this.numberOfBitPerInt;
    }

    public String numberOfBitPerIntTipText() {
        return "Choose the number of bits ber int.";
    }
}