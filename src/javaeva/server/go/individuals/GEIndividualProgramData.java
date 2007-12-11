package javaeva.server.go.individuals;

import javaeva.server.go.individuals.codings.gp.AbstractGPNode;
import javaeva.server.go.individuals.codings.gp.GPArea;
import javaeva.server.go.individuals.codings.gp.InterfaceProgram;
import javaeva.server.go.operators.crossover.CrossoverGADefault;
import javaeva.server.go.operators.mutation.InterfaceMutation;
import javaeva.server.go.operators.mutation.MutateGADefault;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.BitSet;
import java.util.ArrayList;

/** This individual uses a binary genotype to code for a tree-based representation
 * using a BNF grammar, see also Grammatical Evolution.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.07.2004
 * Time: 10:43:39
 * To change this template use File | Settings | File Templates.
 */
public class GEIndividualProgramData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeProgram, java.io.Serializable {

    protected GPArea[]                      m_Area;
    protected double                        m_InitFullGrowRatio = 0.5;
    protected int                           m_InitDepth         = 5;
    protected int                           m_TargetDepth       = 10;
    protected boolean                       m_CheckTargetDepth  = true;

    protected BitSet                        m_Genotype;
    protected AbstractGPNode[]              m_Phenotype;
    protected int                           m_GenotypeLengthPerProgram = 240; // this is the overall length
    protected int                           m_MaxNumberOfNodes      = 80;
    protected int                           m_NumberOfBitPerInt     = 6;
    protected int                           m_CurrentIndex          = 0;
    protected int                           m_CurrentNumberOfNodes  = 0;
    protected Object[][]                    m_Rules;

    public GEIndividualProgramData() {
        this.m_Area                 = new GPArea[1];
        this.m_GenotypeLengthPerProgram       = 240;
        this.m_Genotype             = new BitSet();
        this.m_MutationOperator     = new MutateGADefault();
        this.m_CrossoverOperator    = new CrossoverGADefault();
        this.m_MutationProbability  = 0.5;
        this.m_CrossoverProbability = 0.5;
    }

    public GEIndividualProgramData(GEIndividualProgramData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype = new AbstractGPNode[individual.m_Phenotype.length];
            for (int i = 0; i < individual.m_Phenotype.length; i++) {
                this.m_Phenotype[i] = (AbstractGPNode)individual.m_Phenotype[i].clone();
            }
        }
        this.m_GenotypeLengthPerProgram   = individual.m_GenotypeLengthPerProgram;
        this.m_MaxNumberOfNodes = individual.m_MaxNumberOfNodes;
        this.m_NumberOfBitPerInt= individual.m_NumberOfBitPerInt;
        this.m_CurrentIndex     = individual.m_CurrentIndex;
        if (individual.m_Genotype != null)
            this.m_Genotype                 = (BitSet)individual.m_Genotype.clone();
        if (individual.m_Area != null) {
            this.m_Area     = new GPArea[individual.m_Area.length];
            for (int i = 0; i < this.m_Area.length; i++) {
                this.m_Area[i]       = (GPArea)individual.m_Area[i].clone();
            }
        }
        // User                     : "Copy the rules set!"
        // GEIndividualProgramData  : "Naay! I wanna go playing with my friends... !"
        if (individual.m_Rules != null) {
            this.m_Rules = new Object[individual.m_Rules.length][];
            for (int t = 0; t < this.m_Rules.length; t++) {
                this.m_Rules[t] = new Object[individual.m_Rules[t].length];
                int[][] copyRulz, orgRulz = (int[][]) individual.m_Rules[t][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.m_Rules[t][0] = copyRulz;
                AbstractGPNode[]  copyNode, orgNode;
                for (int i = 1; i < this.m_Rules[t].length; i++) {
                    orgNode = (AbstractGPNode[])individual.m_Rules[t][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.m_Rules[t][i] = copyNode;
                }
            }
        }
        // cloning the members of AbstractEAIndividual
        this.m_Age                      = individual.m_Age;
        this.m_CrossoverOperator        = individual.m_CrossoverOperator;
        this.m_CrossoverProbability     = individual.m_CrossoverProbability;
        this.m_MutationOperator         = (InterfaceMutation)individual.m_MutationOperator.clone();
        this.m_MutationProbability      = individual.m_MutationProbability;
        this.m_SelectionProbability = new double[individual.m_SelectionProbability.length];
        for (int i = 0; i < this.m_SelectionProbability.length; i++) {
            this.m_SelectionProbability[i] = individual.m_SelectionProbability[i];
        }
        this.m_Fitness = new double[individual.m_Fitness.length];
        for (int i = 0; i < this.m_Fitness.length; i++) {
            this.m_Fitness[i] = individual.m_Fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    public Object clone() {
        return (Object) new GEIndividualProgramData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GEIndividualProgramData) {
            GEIndividualProgramData indy = (GEIndividualProgramData) individual;
            if (this.m_GenotypeLengthPerProgram != indy.m_GenotypeLengthPerProgram) return false;
            if (this.m_MaxNumberOfNodes != indy.m_MaxNumberOfNodes) return false;
            if (this.m_NumberOfBitPerInt != indy.m_NumberOfBitPerInt) return false;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;
            if (!this.m_Genotype.equals(indy.m_Genotype)) return false;
            return true;
        } else {
            return false;
        }
    }

    /** This method compiles the area
     */
    private void compileArea() {
        if (this.m_Area == null) {
            this.m_Rules = null;
            return;
        }
        //this.m_Rules = new Object[this.m_Area.length][];
        for (int t = 0; t < this.m_Area.length; t++) {
            // first lets find out what kind of elements are available
            int         arity, maxArity = 0;

            // first find out the max arity in the GPArea
            this.m_Area[t].compileReducedList();
            ArrayList area = this.m_Area[t].getReducedList();
            for (int i = 0; i < area.size(); i++) {
                arity = ((AbstractGPNode) area.get(i)).getArity();
                if (arity > maxArity) maxArity = arity;
            }

            // Now i get a sorted list
            ArrayList[] directList = new ArrayList[maxArity + 1];
            for (int i = 0; i < directList.length; i++) directList[i] = new ArrayList();
            for (int i = 0; i < area.size(); i++) {
                directList[((AbstractGPNode) area.get(i)).getArity()].add(area.get(i));
            }
            // Now write the rules
            this.m_Rules[t] = new Object[maxArity+2];
            // the first rule describes how to decode an <expr>
            int     numberOfRules = 0, index = 0;
            int[]   tmpRule;
            int[][] tmpExpr = new int[directList.length+1][];
            for (int i = 0; i < directList.length; i++) {
                tmpRule = new int[i+1];
                if (i == 0) {
                    // this is a <var>
                    tmpRule[0] = 1;
                } else {
                    // this is a <opX> <expr> <expr>....
                    if (directList[i].size() > 0) {
                        tmpRule[0] = i+1;
                        for (int j = 1; j < i+1; j++) tmpRule[j] = 0;
                    } else {
                        tmpRule = null;
                    }
                }
                tmpExpr[i] = tmpRule;
                if (tmpRule != null) numberOfRules++;
            }
            // Now get rid of the null rules
            int[][] trueExpr = new int[numberOfRules][];

            for (int i = 0; i < tmpExpr.length; i++) {
                if (tmpExpr[i] != null) {
                    trueExpr[index] = tmpExpr[i];
                    index++;
                }
            }
            this.m_Rules[t][0] =trueExpr;

            // now the rules that define <var>, <op1>, <op2>, ....
            AbstractGPNode[]    tmpListOfGPNodes;
            for (int i = 0; i < directList.length; i++) {
                tmpListOfGPNodes = new AbstractGPNode[directList[i].size()];
                for (int j = 0; j < directList[i].size(); j++) {
                    tmpListOfGPNodes[j] = (AbstractGPNode)directList[i].get(j);
                }
                this.m_Rules[t][i+1] = tmpListOfGPNodes;
            }
            // this should be the complete rules set
            //this.printRuleSet();
        }
    }

    /** This method will print the currently used rule set
     */
    private void printRuleSet() {
        String      result = "";
        AbstractGPNode[]    tmpNodes;
        for (int t = 0; t < this.m_Area.length; t++) {
            // first the Non-Terminals
            result += "N \t := \t{";
            for (int i = 0; i < this.m_Rules[t].length; i++) {
                if (i == 0) result += "expr, ";
                else {
                    if (i == 1) result += "var, ";
                    else {
                        if (((AbstractGPNode[])this.m_Rules[t][i]).length > 0) result += "op"+(i-1)+", ";
                    }
                }
            }
            result += "}\n";
            // then the Ternimnals
            result += "T \t := \t{";
            this.m_Area[t].compileReducedList();
            ArrayList area = this.m_Area[t].getReducedList();
            for (int i = 0; i < area.size(); i++) result += ((AbstractGPNode)area.get(i)).getStringRepresentation()+", ";
            result += "}\n";
            // now the S
            result += "S \t := \t<expr>\n\n";

            // now the rules
            for (int i = 0; i < this.m_Rules[t].length; i++) {
                if (i == 0) {
                    // the first rules
                    result += "0. \t := \t<expr> \t::\t";
                    System.out.println("i: " + i);
                    int[][] rulz = (int[][])this.m_Rules[t][i];
                    for (int j = 0; j < rulz.length; j++) {
                        result += this.getRuleString(rulz[j]) + "\n";
                        if ((j+1) < rulz.length) result += "\t \t \t \t \t \t";
                    }
                } else {
                    // now the rules for the terminals
                    if (i == 1) {
                        // These are the GP-Terminals
                        tmpNodes = (AbstractGPNode[])this.m_Rules[t][i];
                        result += "1. \t := \t<var> \t::\t"+ tmpNodes[0].getStringRepresentation()+"\n";
                        for (int j = 1; j < tmpNodes.length; j++) result += "\t \t \t \t \t \t"+ tmpNodes[j].getStringRepresentation()+"\n";
                    } else {
                        // These are the GP-Functions
                        tmpNodes = (AbstractGPNode[])this.m_Rules[t][i];
                        if (tmpNodes.length > 0) {
                            result += i+". \t := \t<op"+(i-1)+"> \t::\t"+ tmpNodes[0].getStringRepresentation()+"\n";
                            for (int j = 1; j < tmpNodes.length; j++) result += "\t \t \t \t \t \t"+ tmpNodes[j].getStringRepresentation()+"\n";
                        }
                    }
                }
            }
            result += "\n";
        }

        // Now print the result:
        System.out.println(""+result);
    }


    /** This method returns a string for the BitSet
     * @return A String
     */
    private String getBitSetString() {
        String result = "";
        result += "{";
        for (int i = 0; i < this.m_GenotypeLengthPerProgram*this.m_Area.length; i++) {
            if (i % this.m_NumberOfBitPerInt == 0) result += " ";
            if (this.m_Genotype.get(i)) result += "1";
            else result += "0";
        }
        result += "}";
        return result;
    }

    /** This method returns a String for a given rule
     * @param rule     The rulz to transform into a string
     * @return String
     */
    private String getRuleString(int[] rule) {
        String result ="";
        for (int k = 0; k < rule.length; k++) {
            if (rule[k] == 0) result += "<expr> ";
            if (rule[k] == 1) result += "<var> ";
            if (rule[k] >  1) result += "<op"+(rule[k]-1)+"> ";
        }
        return result;
    }

/************************************************************************************
 * InterfaceDataTypeProgram methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setProgramDataLength (int length) {
        GPArea[] oldArea    = this.m_Area;
        Object[][] oldRulz  = this.m_Rules;

        this.m_Area         = new GPArea[length];
        for (int t = 0; ((t < this.m_Area.length) && (t < oldArea.length)); t++) {
            this.m_Area[t]      = oldArea[t];
        }
        for (int t = oldArea.length; t < this.m_Area.length; t++) {
            this.m_Area[t]      = oldArea[oldArea.length-1];
        }
        this.m_Rules        = new Object[length][];
        if (oldRulz == null) return;
        for (int t = 0; ((t < this.m_Area.length) && (t < oldArea.length)); t++) {
            if (oldRulz[t] != null) {
                this.m_Rules[t]     = new Object[oldRulz[t].length];
                int[][] copyRulz, orgRulz = (int[][]) oldRulz[t][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.m_Rules[t][0] = copyRulz;
                AbstractGPNode[]  copyNode, orgNode;
                for (int i = 1; i < this.m_Rules[t].length; i++) {
                    orgNode = (AbstractGPNode[])oldRulz[t][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.m_Rules[t][i] = copyNode;
                }
            }
        }
        for (int t = oldArea.length; t < this.m_Area.length; t++) {
            if (oldRulz[oldArea.length-1] != null) {
                this.m_Rules[t] = new Object[oldRulz[oldArea.length-1].length];
                int[][] copyRulz, orgRulz = (int[][]) oldRulz[oldArea.length-1][0];
                copyRulz = new int[orgRulz.length][];
                for (int i = 0; i < copyRulz.length; i++) {
                    copyRulz[i] = new int[orgRulz[i].length];
                    System.arraycopy(orgRulz[i], 0, copyRulz[i], 0, orgRulz[i].length);
                }
                this.m_Rules[t][0] = copyRulz;
                AbstractGPNode[]  copyNode, orgNode;
                for (int i = 1; i < this.m_Rules[t].length; i++) {
                    orgNode = (AbstractGPNode[])oldRulz[oldArea.length-1][i];
                    copyNode = new AbstractGPNode[orgNode.length];
                    for (int j = 0; j < orgNode.length; j++) {
                        copyNode[j] = (AbstractGPNode) orgNode[j].clone();
                    }
                    this.m_Rules[t][i] = copyNode;
                }
            }
        }
    }

    /** This method will fetch the next int value from the BitSet. If necessary the
     * method will continue at the beginning of the BitSet if m_Genotype length is
     * exceeded.
     * Note: You need to set the current ReadingIndx = 0 before starting to decode
     * the BitSet
     * @param t     The index of the program.
     * @return      The int value
     */
    private int decodeNextInt(int t) {
        int result = 0;
        for (int i = 0; i < this.m_NumberOfBitPerInt; i++) {
            if (this.m_Genotype.get(this.m_CurrentIndex+(t*this.m_GenotypeLengthPerProgram)))
                result += Math.pow(2, i);
            this.m_CurrentIndex++;
            if (this.m_CurrentIndex >= (t+1)*this.m_GenotypeLengthPerProgram) this.m_CurrentIndex = t*this.m_GenotypeLengthPerProgram;
        }
        return result;
    }

    /** This method will decode a GPNode from the BitSet
     * @param t     The index of the program
     * @param mode  The modex
     * @return GPNode
     */
    private AbstractGPNode decodeGPNode(int t, int mode) {
        AbstractGPNode  result  = null;
        int             value   = this.decodeNextInt(t);

//        System.out.println("Decoding mode: " + mode);

        if (mode == 0) {
            int[][] rulz = (int[][]) this.m_Rules[t][0];
            int[]   myRule = rulz[value%rulz.length];
//            System.out.print("Value % rulz : "+ value +" % " + rulz.length + " = " +(value%rulz.length));
//            System.out.println(" => my rule " + this.getRuleString(myRule));
            this.m_CurrentNumberOfNodes += myRule.length;
            if ((this.m_CurrentNumberOfNodes + myRule.length) > this.m_MaxNumberOfNodes) {
                // no i have to limit the number of nodes
                myRule = rulz[0];
//                System.out.println("Limiting to "+ this.getRuleString(myRule));
            }
            result = this.decodeGPNode(t, myRule[0]);
            result.initNodeArray();
            for (int i = 0; i < result.getArity(); i++) {
                result.setNode(this.decodeGPNode(t, myRule[i+1]), i);
            }
        } else {
            AbstractGPNode[] availableNodes = (AbstractGPNode[]) this.m_Rules[t][mode];
//            System.out.print("Choosing a terminal : "+ value +" % " + availableNodes.length + " = " +(value%availableNodes.length));
//            System.out.println(" => " +availableNodes[value % availableNodes.length].getStringRepresentation());
            result = (AbstractGPNode)availableNodes[value % availableNodes.length].clone();
        }
        return result;
    }

    /** This method allows you to read the program stored as Koza style node tree
     * @return GPNode representing the binary data.
     */
    public InterfaceProgram[] getProgramData() {
//        if (true) {
//            String test ="GE decoding:\n";
//            test += this.getBitSetString() +"\n{";
//            this.m_CurrentIndex = 0;
//            for (int i = 0; i < this.m_MaxNumberOfNodes; i++) {
//                test += this.decodeNextInt();
//                if ((i + 1) < this.m_MaxNumberOfNodes) test += "; ";
//            }
//            test += "}\n";
//            System.out.println(""+test);
//        }
        // lets decode the stuff!
        if (this.m_Rules == null) {
            this.compileArea();
            if (this.m_Rules == null) return null;
        }
        this.m_CurrentIndex         = 0;
        this.m_CurrentNumberOfNodes = 0;
        int     mode                = 0;
        this.m_Phenotype = new AbstractGPNode[this.m_Area.length];
        for (int t = 0; t < this.m_Area.length; t++) {
            mode                        = 0;
            this.m_CurrentIndex         = t*this.m_GenotypeLengthPerProgram;
            this.m_CurrentNumberOfNodes = 0;
            this.m_Phenotype[t] = this.decodeGPNode(t, mode);
        }
//        System.out.println("Decoded: ");
//        System.out.println(""+ result.getStringRepresentation());
        return this.m_Phenotype;
    }

    /** This method allows you to read the Program data without
     * an update from the genotype
     * @return InterfaceProgram[] representing the Program.
     */
    public InterfaceProgram[] getProgramDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the program phenotype.
     * @param program    The new program.
     */
    public void SetProgramData(InterfaceProgram[] program) {
        if (program instanceof AbstractGPNode[]) {
            this.m_Phenotype = new AbstractGPNode[program.length];
            for (int t = 0; t < program.length; t++) {
                this.m_Phenotype[t] = (AbstractGPNode)((AbstractGPNode)program[t]).clone();
            }
        }
    }

    /** 
     * Warning - this is not implemented, it only sets the phenotype using SetProgramData.
     * @param program    The new program.
     */
    public void SetProgramDataLamarkian(InterfaceProgram[] program) {
        this.SetProgramData(program);
        if (program instanceof AbstractGPNode[]) System.err.println("Warning setProgram() for GEIndividualProgramData not implemented!");
    }

    /** This method allows you to set the function area
     * @param area  The area contains functions and terminals
     */
    public void SetFunctionArea(Object[] area) {
        if (area instanceof GPArea[]) {
            this.m_Area     = new GPArea[area.length];
            for (int t = 0; t < this.m_Area.length; t++) {
                this.m_Area[t] = (GPArea) area[t];
            }
            this.compileArea();
        }
    }

    /** This method allows you to set the function area
     * @return The function area
     */
    public Object[] getFunctionArea() {
        return this.m_Area;
    }

/************************************************************************************
 * InterfaceEAIndividual methods
 */
    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    public void init(InterfaceOptimizationProblem opt) {
        this.defaultInit();
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof InterfaceProgram) {
            this.SetProgramDataLamarkian((InterfaceProgram[])obj);
        } else {
            this.defaultInit();
            System.out.println("Initial value for GPIndividualDoubleData is no InterfaceProgram[]!");
        }
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will mutate the individual randomly
     */
    public void mutate() {
        if (RandomNumberGenerator.flipCoin(this.m_MutationProbability)) this.m_MutationOperator.mutate(this);
    }

    /** This method will mate the Individual with given other individuals
     * of the same type.
     * @param partners  The possible partners
     * @return offsprings
     */
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RandomNumberGenerator.flipCoin(this.m_CrossoverProbability)) {
            result = this.m_CrossoverOperator.mate(this, partners);
        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() +1];
            result[0] = (AbstractEAIndividual)this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
            }
        }
        for (int i = 0; i < result.length; i++) result[i].giveNewName();
        return result;         
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "GEIndividual coding program:\n";
        result += "{";
        for (int i = 0; i < this.m_GenotypeLengthPerProgram*this.m_Area.length; i++) {
            if (this.m_Genotype.get(i)) result += "1";
            else result += "0";
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

        /** This method allows you to read the binary data
         * @return BitSet representing the binary data.
         */
        public BitSet getBGenotype() {
            return this.m_Genotype;
        }

        /** This method allows you to set the binary data, this can be used for
         * memetic algorithms.
         * @param binaryData    The new binary data.
         */
        public void SetBGenotype(BitSet binaryData) {
            this.m_Genotype = binaryData;
        }

        /** This method allows the user to read the length of the genotype.
         * This may be necessary since BitSet.lenght only returns the index
         * of the last significat bit.
         * @return The length of the genotype.
         */
        public int getGenotypeLength() {
            return this.m_GenotypeLengthPerProgram*this.m_Area.length;
        }

        /** This method inits the genotpye of the individual
         */
        public void defaultInit() {
            for (int i = 0; i < this.m_GenotypeLengthPerProgram*this.m_Area.length; i++) {
                if (RandomNumberGenerator.flipCoin(0.5)) this.m_Genotype.set(i);
                else this.m_Genotype.clear(i);
            }
        }

        /** This method performs a simple one point mutation in the genotype
         */
        public void defaultMutate() {
            int mutationIndex = RandomNumberGenerator.randomInt(0, this.m_GenotypeLengthPerProgram*this.m_Area.length);
            //if (mutationIndex > 28) System.out.println("Mutate: " + this.getSolutionRepresentationFor());
            if (this.m_Genotype.get(mutationIndex)) this.m_Genotype.clear(mutationIndex);
            else this.m_Genotype.set(mutationIndex);
            //if (mutationIndex > 28) System.out.println(this.getSolutionRepresentationFor());
        }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GE indiviudal";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a GE individual suited to optimize programs.";
    }

    /** This method allows you to set the length of the binary genotype
     * @param size      The length
     */
    public void setGenotypeLengthPerProgram(int size) {
        this.m_GenotypeLengthPerProgram = size;
    }
    public int getGenotypeLengthPerProgram() {
        return this.m_GenotypeLengthPerProgram;
    }
    public String genotypeLengthPerProgramTipText() {
        return "Choose the length of the genotype.";
    }

    /** This method allows you to set the maximum number of
     * nodes allowed for the program.
     * @param nodes   The maximum number of nodes
     */
    public void setMaxNumberOfNodes(int nodes) {
        this.m_MaxNumberOfNodes = nodes;
    }
    public int getMaxNumberOfNodes() {
        return this.m_MaxNumberOfNodes;
    }
    public String maxNumberOfNodesTipText() {
        return "Set the maximum number of nodes for the program.";
    }

    /** This method allows you to set the number of bits per int value
     * stored on the BitSet
     * @param length   The number of bits per int.
     */
    public void setNumberOfBitPerInt(int length) {
        this.m_NumberOfBitPerInt = length;
    }
    public int getNumberOfBitPerInt() {
        return this.m_NumberOfBitPerInt;
    }
    public String numberOfBitPerIntTipText() {
        return "Choose the number of bits ber int.";
    }
}