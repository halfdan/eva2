package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.gp.AbstractGPNode;
import eva2.optimization.individuals.codings.gp.GPArea;
import eva2.optimization.individuals.codings.gp.InterfaceProgram;
import eva2.optimization.operator.crossover.CrossoverGPDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateDefault;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual uses a tree-based genotype to code for program trees.
 */
@Description(value = "This is a GP individual suited to optimize Koza style program trees.")
public class GPIndividualProgramData extends AbstractEAIndividual implements InterfaceGPIndividual, InterfaceDataTypeProgram, java.io.Serializable {

    protected AbstractGPNode[] genotype;
    protected AbstractGPNode[] phenotype;
    protected GPArea[] gpArea;
    protected double initFullGrowRatio = 0.5;
    protected int initDepth = 4;
    protected int maxAllowedDepth = 8;
    protected boolean checkMaxDepth = true;

    public GPIndividualProgramData() {
        this.gpArea = new GPArea[1];
        gpArea[0] = new GPArea();
        this.genotype = new AbstractGPNode[1];
        this.mutationOperator = new MutateDefault();
        this.crossoverOperator = new CrossoverGPDefault();
    }

    public GPIndividualProgramData(GPIndividualProgramData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new AbstractGPNode[individual.phenotype.length];
            for (int i = 0; i < individual.phenotype.length; i++) {
                if (individual.phenotype[i] != null) {
                    this.phenotype[i] = (AbstractGPNode) individual.phenotype[i].clone();
                }
            }
        }
        if (individual.genotype != null) {
            this.genotype = new AbstractGPNode[individual.genotype.length];
            this.gpArea = new GPArea[individual.gpArea.length];
            for (int i = 0; i < individual.genotype.length; i++) {
                if (individual.genotype[i] != null) {
                    this.genotype[i] = (AbstractGPNode) individual.genotype[i].clone();
                    this.genotype[i].connect(null);
                }
                if (individual.gpArea[i] != null) {
                    this.gpArea[i] = (GPArea) individual.gpArea[i].clone();
                }
            }
        }
        this.initFullGrowRatio = individual.initFullGrowRatio;
        this.initDepth = individual.initDepth;
        this.maxAllowedDepth = individual.maxAllowedDepth;
        this.checkMaxDepth = individual.checkMaxDepth;

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
        return new GPIndividualProgramData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GPIndividualProgramData) {
            GPIndividualProgramData indy = (GPIndividualProgramData) individual;
            //@todo Eigendlich k�nnte ich noch die Areas vergleichen
            if (this.maxAllowedDepth != indy.maxAllowedDepth) {
                return false;
            }
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            } else {
                for (int i = 0; i < this.genotype.length; i++) {
                    if ((this.genotype[i] == null) || (indy.genotype[i] == null) || (!this.genotype[i].equals(indy.genotype[i]))) {
                        return false;
                    }
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
    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setProgramDataLength(int length) {
        GPArea[] oldArea = this.gpArea;
        AbstractGPNode[] oldProg = this.genotype;
        this.gpArea = new GPArea[length];
        this.genotype = new AbstractGPNode[length];
        for (int i = 0; ((i < this.gpArea.length) && (i < oldArea.length)); i++) {
            this.gpArea[i] = oldArea[i];
            this.genotype[i] = oldProg[i];
        }
        for (int i = oldArea.length; i < this.gpArea.length; i++) {
            this.gpArea[i] = oldArea[oldArea.length - 1];
            this.genotype[i] = oldProg[oldProg.length - 1];
        }
    }

    /**
     * This method allows you to read the program stored as Koza style node tree
     *
     * @return AbstractGPNode representing the binary data.
     */
    @Override
    public InterfaceProgram[] getProgramData() {
        this.phenotype = new AbstractGPNode[this.genotype.length];
        for (int i = 0; i < this.genotype.length; i++) {
            this.phenotype[i] = (AbstractGPNode) this.genotype[i].clone();
            // if (!phenotype[0].checkDepth(0)) {
            // 	System.err.println("error... " + genotype[0].checkDepth(0));
            // }

            if ((this.checkMaxDepth) && (this.phenotype[i].isMaxDepthViolated(this.maxAllowedDepth))) {
                System.err.println("Trying to meet the Target Depth! " + this.phenotype[i].isMaxDepthViolated(this.maxAllowedDepth) + " " + phenotype[i].getMaxDepth());
                this.phenotype[i].repairMaxDepth(this.gpArea[i], this.maxAllowedDepth);
                //System.out.println("TragetDepth: " + this.targetDepth + " : " + this.m_Program.getMaxDepth());
            }
        }
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
        if (this.phenotype == null) {
            return getProgramData();
        } else {
            return this.phenotype;
        }
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
            for (int i = 0; i < this.phenotype.length; i++) {
                this.phenotype[i] = (AbstractGPNode) ((AbstractGPNode) program[i]).clone();
            }
        }
    }

    /**
     * This method allows you to set the program genotype.
     *
     * @param program The new program.
     */
    @Override
    public void SetProgramGenotype(InterfaceProgram[] program) {
        this.SetProgramPhenotype(program);
        if (program instanceof AbstractGPNode[]) {
            this.genotype = new AbstractGPNode[program.length];
            for (int i = 0; i < this.genotype.length; i++) {
                this.genotype[i] = (AbstractGPNode) ((AbstractGPNode) program[i]).clone();
            }
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
            this.gpArea = (GPArea[]) area;
        }
    }

    /**
     * This method allows you to set the function area
     *
     * @return The function area
     */
    @Override
    public Object[] getFunctionArea() {
        return this.gpArea;
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
        if (obj instanceof InterfaceProgram[]) {
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
        result += "GPIndividual coding program: (";
        result += "Fitness {";
        for (int i = 0; i < this.fitness.length; i++) {
            result += this.fitness[i] + ";";
        }
        result += "}/SelProb{";
        for (int i = 0; i < this.selectionProbability.length; i++) {
            result += this.selectionProbability[i] + ";";
        }
        result += "})\n Value: ";
        for (int i = 0; i < this.genotype.length; i++) {
            if (this.genotype[i] != null) {
                result += this.genotype[i].getStringRepresentation();
            }
            result += "\nUsing " + this.genotype[i].getNumberOfNodes() + " nodes.";
        }
        return result;
    }

/************************************************************************************
 * InterfaceGPIndividual methods
 */

    /**
     * This method will allow the user to read the program genotype
     *
     * @return AbstractGPNode
     */
    @Override
    public AbstractGPNode[] getPGenotype() {
        return this.genotype;
    }

    /**
     * This method will allow the user to set the current program 'genotype'.
     *
     * @param b The new programgenotype of the Individual
     */
    @Override
    public void setPGenotype(AbstractGPNode[] b) {
        this.genotype = b;
        this.phenotype = null;
    }

    /**
     * This method will allow the user to set the current program 'genotype'.
     *
     * @param b The new program genotype of the Individual
     * @param i The index where to insert the new program
     */
    @Override
    public void setPGenotype(AbstractGPNode b, int i) {
        this.genotype[i] = b;
        genotype[i].updateDepth(0);
//        System.out.println("Setting pheno of depth " + b.getMaxDepth() + " " + b.getStringRepresentation());
        this.phenotype = null;
    }

    /**
     * This method performs a eva2.problems.simple one element mutation on the program
     */
    @Override
    public void defaultMutate() {
        for (int i = 0; i < this.genotype.length; i++) {
            AbstractGPNode nodeToMutate = this.genotype[i].getRandomNode();
            if (nodeToMutate.getParent() == null) { // mutate at root
                this.defaultInit(null);
            } else {
                AbstractGPNode parent = nodeToMutate.getParent();
                if (checkMaxDepth && (nodeToMutate.getDepth() == maxAllowedDepth)) { // mutate with a constant
                    AbstractGPNode newNode = ((AbstractGPNode) this.gpArea[i].getRandomNodeWithArity(0).clone());
                    newNode.setDepth(nodeToMutate.getDepth());
                    parent.setNode(newNode, nodeToMutate);
                } else {
                    AbstractGPNode newNode = ((AbstractGPNode) this.gpArea[i].getRandomNode().clone());
                    newNode.setDepth(nodeToMutate.getDepth());
                    newNode.initGrow(this.gpArea[i], this.maxAllowedDepth);
                    parent.setNode(newNode, nodeToMutate);
                }
                //if (!genotype[i].checkDepth(0) || (genotype[i].isMaxDepthViolated(maxAllowedDepth))) {
                //	System.err.println("Error in GPIndividualProgramData.defaultMutate!");
                //}
            }
        }
        phenotype = null; // reset pheno
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        phenotype = null; // reset pheno
        for (int i = 0; i < this.gpArea.length; i++) {
            if (this.gpArea[i] == null) {
                EVAERROR.errorMsgOnce("Error in GPIndividualProgramData.defaultInit(): Area[" + i + "] == null !!");
            } else {
                this.genotype[i] = (AbstractGPNode) (this.gpArea[i].getRandomNonTerminal()).clone();
                this.genotype[i].setDepth(0);
                int targetDepth = RNG.randomInt(1, this.initDepth);
                if (RNG.flipCoin(this.initFullGrowRatio)) {
                    this.genotype[i].initFull(this.gpArea[i], targetDepth);
                } else {
                    this.genotype[i].initGrow(this.gpArea[i], targetDepth);
                }
            }
        }
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "GP individual";
    }

    /**
     * This method will toggle between checking for max depth or not.
     *
     * @param b the Switch.
     */
    public void setCheckMaxDepth(boolean b) {
        this.checkMaxDepth = b;
    }

    public boolean getCheckMaxDepth() {
        return this.checkMaxDepth;
    }

    public String checkMaxDepthTipText() {
        return "If activated the maximum depth of the program tree will be enforced.";
    }

    /**
     * This method set/get the initialize Full Grow Ratio.
     *
     * @param b The new initialize Full Grow Ratio of the GP Tree.
     */
    public void setInitFullGrowRatio(double b) {
        if (b < 0) {
            b = 0;
        }
        if (b > 1) {
            b = 1;
        }
        this.initFullGrowRatio = b;
    }

    public double getInitFullGrowRatio() {
        return this.initFullGrowRatio;
    }

    public String initFullGrowRatioTipText() {
        return "The ratio between the full and the grow initialize methods (1 uses only full initializing).";
    }

    /**
     * This method set/get the initialize depth.
     *
     * @param b The new initialize Depth of the GP Tree.
     */
    public void setInitDepth(int b) {
        if (b > this.maxAllowedDepth) {
            System.out.println("Waring Init Depth will be set to Target Depth!");
            b = this.maxAllowedDepth;
        }
        this.initDepth = b;
    }

    public int getInitDepth() {
        return this.initDepth;
    }

    public String initDepthTipText() {
        return "The initial depth of the GP Tree.";
    }

    /**
     * This method set/get the target depth.
     *
     * @param b The new target Depth of the GP Tree.
     */
    public void setMaxAllowedDepth(int b) {
        this.maxAllowedDepth = b;
    }

    @Override
    public int getMaxAllowedDepth() {
        return this.maxAllowedDepth;
    }

    public String maxAllowedDepthTipText() {
        return "The maximum depth allowed for the GP tree.";
    }

    public String[] customPropertyOrder() {
        return new String[]{"initDepth", "checkMaxDepth", "maxAllowedDepth"};
    }

    public void updateDepth() {
        for (int i = 0; i < genotype.length; i++) {
            genotype[i].updateDepth(0);
        }
    }

    public void checkDepth() {
        for (int i = 0; i < genotype.length; i++) {
            genotype[i].checkDepth(0);
        }
    }
}
