package eva2.server.go.individuals;


import java.util.ArrayList;

import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.individuals.codings.gp.GPArea;
import eva2.server.go.individuals.codings.gp.InterfaceProgram;
import eva2.server.go.operators.crossover.CrossoverGPDefault;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateGPDefault;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;
import eva2.tools.EVAERROR;
import eva2.tools.EVAHELP;

/** This individual uses a tree-based genotype to code for program trees.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:27:55
 * To change this template use Options | File Templates.
 */
public class GPIndividualProgramData extends AbstractEAIndividual implements InterfaceGPIndividual, InterfaceDataTypeProgram, java.io.Serializable {

    protected AbstractGPNode[]            m_Genotype;
    protected AbstractGPNode[]            m_Phenotype;
    protected GPArea[]                    m_Area;
    protected double                      m_InitFullGrowRatio   = 0.5;
    protected int                         m_InitDepth           = 4;
    protected int                         m_TargetDepth         = 8;
    protected boolean                     m_CheckTargetDepth    = true;

    public GPIndividualProgramData() {
        this.m_Area                 = new GPArea[1];
        m_Area[0] = new GPArea();
        this.m_Genotype             = new AbstractGPNode[1];
        this.m_MutationOperator     = new MutateGPDefault();
        this.m_CrossoverOperator    = new CrossoverGPDefault();
    }

    public GPIndividualProgramData(GPIndividualProgramData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype = new AbstractGPNode[individual.m_Phenotype.length];
            for (int i = 0; i < individual.m_Phenotype.length; i++) {
                if (individual.m_Phenotype[i] != null) this.m_Phenotype[i] = (AbstractGPNode)individual.m_Phenotype[i].clone();
            }
        }        
        if (individual.m_Genotype != null) {
            this.m_Genotype = new AbstractGPNode[individual.m_Genotype.length];
            this.m_Area     = new GPArea[individual.m_Area.length];
            for (int i = 0; i < individual.m_Genotype.length; i++) {
                if (individual.m_Genotype[i] != null) {
                    this.m_Genotype[i]   = (AbstractGPNode)individual.m_Genotype[i].clone();
                    this.m_Genotype[i].connect(null);
                }
                if (individual.m_Area[i] != null) this.m_Area[i]       = (GPArea)individual.m_Area[i].clone();
            }
        }
        this.m_InitFullGrowRatio        = individual.m_InitFullGrowRatio;
        this.m_InitDepth                = individual.m_InitDepth;
        this.m_TargetDepth              = individual.m_TargetDepth;
        this.m_CheckTargetDepth         = individual.m_CheckTargetDepth;

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
        return (Object) new GPIndividualProgramData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GPIndividualProgramData) {
            GPIndividualProgramData indy = (GPIndividualProgramData) individual;
            //@todo Eigendlich k�nnte ich noch die Areas vergleichen
            if (this.m_TargetDepth != indy.m_TargetDepth)
                return false;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null))
                return false;
            else {
                for (int i = 0; i < this.m_Genotype.length; i++) {
                    if ((this.m_Genotype[i] == null) || (indy.m_Genotype[i] == null) || (!this.m_Genotype[i].equals(indy.m_Genotype[i])))
                        return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

/************************************************************************************
 * InterfaceDataTypeProgram methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setProgramDataLength (int length) {
        GPArea[] oldArea            = this.m_Area;
        AbstractGPNode[] oldProg    = this.m_Genotype;
        this.m_Area         = new GPArea[length];
        this.m_Genotype     = new AbstractGPNode[length];
        for (int i = 0; ((i < this.m_Area.length) && (i < oldArea.length)); i++) {
            this.m_Area[i]      = oldArea[i];
            this.m_Genotype[i]  = oldProg[i];
        }
        for (int i = oldArea.length; i < this.m_Area.length; i++) {
            this.m_Area[i]      = oldArea[oldArea.length-1];
            this.m_Genotype[i]  = oldProg[oldProg.length-1];
        }
    }

    /** This method allows you to read the program stored as Koza style node tree
     * @return AbstractGPNode representing the binary data.
     */
    public InterfaceProgram[] getProgramData() {
        this.m_Phenotype = new AbstractGPNode[this.m_Genotype.length];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Phenotype[i] = (AbstractGPNode)this.m_Genotype[i].clone();
            if ((this.m_CheckTargetDepth) && (this.m_Phenotype[i].isMaxDepthViolated(this.m_TargetDepth))) {
                //System.out.println("Trying to meet the Target Depth!");
                this.m_Phenotype[i].repairMaxDepth(this.m_Area[i], this.m_TargetDepth);
                //System.out.println("TragetDepth: " + this.m_TargetDepth + " : " + this.m_Program.getMaxDepth());
            }
        }
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
            for (int i = 0; i < this.m_Phenotype.length; i++)
                this.m_Phenotype[i] = (AbstractGPNode)((AbstractGPNode)program[i]).clone();
        }
    }

    /** This method allows you to set the program genotype.
     * @param program    The new program.
     */
    public void SetProgramDataLamarkian(InterfaceProgram[] program) {
        this.SetProgramData(program);
        if (program instanceof AbstractGPNode[]) {
            this.m_Genotype = new AbstractGPNode[program.length];
            for (int i = 0; i < this.m_Genotype.length; i++)
                this.m_Genotype[i] = (AbstractGPNode)((AbstractGPNode)program[i]).clone();
        }
    }

    /** This method allows you to set the function area
     * @param area  The area contains functions and terminals
     */
    public void SetFunctionArea(Object[] area) {
        if (area instanceof GPArea[]) {
            this.m_Area = (GPArea[]) area;
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
        if (obj instanceof InterfaceProgram[]) {
            this.SetProgramDataLamarkian((InterfaceProgram[])obj);
        } else {
            this.defaultInit();
            System.out.println("Initial value for GPIndividualDoubleData is no InterfaceProgram[]!");
        }
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "GPIndividual coding program: (";
        result += "Fitness {";
        for (int i = 0; i < this.m_Fitness.length; i++) result += this.m_Fitness[i] + ";";
        result += "}/SelProb{";
        for (int i = 0; i < this.m_SelectionProbability.length; i++) result += this.m_SelectionProbability[i] + ";";
        result += "})\n Value: ";
        for (int i = 0; i < this.m_Genotype.length; i++) {
            if (this.m_Genotype[i] != null) result += this.m_Genotype[i].getStringRepresentation();
            result += "\nUsing " + this.m_Genotype[i].getNumberOfNodes() + " nodes.";
        }
        return result;
    }

/************************************************************************************
 * InterfaceGPIndividual methods
 */

    /** This method will allow the user to read the program genotype
     * @return AbstractGPNode
     */
    public AbstractGPNode[] getPGenotype() {
        return this.m_Genotype;
    }

    /** This method will allow the user to set the current program 'genotype'.
     * @param b    The new programgenotype of the Individual
     */
    public void SetPGenotype(AbstractGPNode[] b) {
        this.m_Genotype = b;
    }

    /** This method will allow the user to set the current program 'genotype'.
     * @param b     The new program genotype of the Individual
     * @param i     The index where to insert the new program
     */
    public void SetPGenotype(AbstractGPNode b, int i) {
        this.m_Genotype[i] = b;
    }

    /** This method performs a simple one element mutation on the program
     */
    public void defaultMutate() {
        ArrayList allNodes = new ArrayList();
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i].addNodesTo(allNodes);
            AbstractGPNode nodeToMutate = (AbstractGPNode) allNodes.get(RNG.randomInt(0, allNodes.size()-1));
            if (nodeToMutate.getParent() == null) {
                this.defaultInit();
            } else {
                AbstractGPNode parent = nodeToMutate.getParent();
                AbstractGPNode newNode = (AbstractGPNode)(((AbstractGPNode)this.m_Area[i].getRandomNode().clone()));
                newNode.setDepth(nodeToMutate.getDepth());
                newNode.initGrow(this.m_Area[i], this.m_TargetDepth);
                parent.setNode(newNode, nodeToMutate);
            }
        }
    }

    /** This method initializes the program
     */
    public void defaultInit() {
        for (int i = 0; i < this.m_Area.length; i++) {
            if (this.m_Area[i] == null) {
            	EVAERROR.errorMsgOnce("Error in GPIndividualProgramData.defaultInit(): Area["+i+"] == null !!");
            } else {
                this.m_Genotype[i] = (AbstractGPNode)(this.m_Area[i].getRandomNonTerminal()).clone();
                this.m_Genotype[i].setDepth(0);
                int targetDepth = RNG.randomInt(1, this.m_InitDepth);
                if (RNG.flipCoin(this.m_InitFullGrowRatio))
                    this.m_Genotype[i].initFull(this.m_Area[i], targetDepth);
                else
                    this.m_Genotype[i].initGrow(this.m_Area[i], targetDepth);
            }
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GP individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a GP individual suited to optimize Koza style program trees.";
    }

    /** This method will toggle between checking for max depth or not.
     * @param b     the Switch.
     */
    public void setCheckTargetDepth(boolean b) {
        this.m_CheckTargetDepth = b;
    }
    public boolean getCheckTargetDepth() {
        return this.m_CheckTargetDepth;
    }
    public String checkTargetDepthTipText() {
        return "If activated the program will be checked for MaxDepth.";
    }

    /** This method set/get the init Full Grow Ratio.
     * @param b     The new init Full Grow Ratio of the GP Tree.
     */
    public void setInitFullGrowRatio(double b) {
        if (b < 0) b = 0;
        if (b > 1) b = 1;
        this.m_InitFullGrowRatio = b;
    }
    public double getInitFullGrowRatio() {
        return this.m_InitFullGrowRatio;
    }
    public String initFullGrowRatioTipText() {
        return "The ratio between the full and the grow init methods (1 uses only full initializing).";
    }

    /** This method set/get the init depth.
     * @param b     The new init Depth of the GP Tree.
     */
    public void setInitDepth(int b) {
        if (b > this.m_TargetDepth) {
            System.out.println("Waring Init Depth will be set to Target Depth!");
            b = this.m_TargetDepth;
        }
        this.m_InitDepth = b;
    }
    public int getInitDepth() {
        return this.m_InitDepth;
    }
    public String initDepthTipText() {
        return "The initial depth of the GP Tree.";
    }

    /** This method set/get the target depth.
     * @param b     The new target Depth of the GP Tree.
     */
    public void setTargetDepth(int b) {
        this.m_TargetDepth = b;
    }
    public int getTargetDepth() {
        return this.m_TargetDepth;
    }
    public String targetDepthTipText() {
        return "The target depth of the GP Tree.";
    }
}
