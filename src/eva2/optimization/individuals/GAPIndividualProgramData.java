package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.gp.InterfaceProgram;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 *
 */
public class GAPIndividualProgramData extends AbstractEAIndividual implements InterfaceDataTypeProgram, InterfaceDataTypeDouble, java.io.Serializable {

    private InterfaceDataTypeDouble numberData = new ESIndividualDoubleData();
    private InterfaceDataTypeProgram programData = new GPIndividualProgramData();

    public GAPIndividualProgramData() {
        this.mutationProbability = 1.0;
        this.crossoverProbability = 1.0;
        this.numberData = new GAIndividualDoubleData();
        this.programData = new GPIndividualProgramData();
    }

    public GAPIndividualProgramData(GAPIndividualProgramData individual) {
        this.numberData = (InterfaceDataTypeDouble) ((AbstractEAIndividual) individual.getNumbers()).clone();
        this.programData = (InterfaceDataTypeProgram) ((AbstractEAIndividual) individual.getProgramRepresentation()).clone();

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        for (int i = 0; i < this.selectionProbability.length; i++) {
            this.selectionProbability[i] = individual.selectionProbability[i];
        }
        this.fitness = new double[individual.fitness.length];
        for (int i = 0; i < this.fitness.length; i++) {
            this.fitness[i] = individual.fitness[i];
        }
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new GAPIndividualProgramData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAPIndividualProgramData) {
            GAPIndividualProgramData indy = (GAPIndividualProgramData) individual;
            if (!((AbstractEAIndividual) this.numberData).equalGenotypes((AbstractEAIndividual) indy.numberData)) {
                return false;
            }
            if (!((AbstractEAIndividual) this.programData).equalGenotypes((AbstractEAIndividual) indy.programData)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method will allow a default initialisation of the individual
     *
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void init(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual) this.numberData).init(opt);
        ((AbstractEAIndividual) this.programData).init(opt);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        ((AbstractEAIndividual) this.numberData).defaultInit(prob);
        ((AbstractEAIndividual) this.programData).defaultInit(prob);
    }

    /**
     * This method will initialize the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof Object[]) {
            if (((Object[]) obj)[0] instanceof double[]) {
                ((AbstractEAIndividual) this.numberData).initByValue(((Object[]) obj)[0], opt);
                ((AbstractEAIndividual) this.programData).initByValue(((Object[]) obj)[1], opt);
            } else {
                ((AbstractEAIndividual) this.numberData).initByValue(((Object[]) obj)[1], opt);
                ((AbstractEAIndividual) this.programData).initByValue(((Object[]) obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual) this.numberData).init(opt);
            ((AbstractEAIndividual) this.programData).init(opt);
            System.out.println("Initial value for GAPIndividualDoubleData is not suitable!");
        }
    }

    /**
     * This method will mutate the individual randomly
     */
    @Override
    public void mutate() {
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.numberData).mutate();
        }
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.programData).mutate();
        }
    }

    @Override
    public void defaultMutate() {
        ((AbstractEAIndividual) this.numberData).defaultMutate();
        ((AbstractEAIndividual) this.programData).defaultMutate();
    }

    /**
     * This method will mate the Individual with given other individuals
     * of the same type.
     *
     * @param partners The possible partners
     * @return offsprings
     */
    @Override
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RNG.flipCoin(this.crossoverProbability)) {
            AbstractEAIndividual[] resNum, resBin;
            AbstractEAIndividual numTmp, binTmp;
            Population numPop, binPop;

            numTmp = (AbstractEAIndividual) this.getNumbers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                numPop.add(((GAPIndividualProgramData) partners.get(i)).getNumbers());
            }
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual) this.getProgramRepresentation();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                binPop.add(((GAPIndividualProgramData) partners.get(i)).getProgramRepresentation());
            }
            resBin = binTmp.mateWith(binPop);

            result = new GAPIndividualProgramData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GAPIndividualProgramData(this);
                ((GAPIndividualProgramData) result[i]).setNumbers((InterfaceDataTypeDouble) resNum[i]);
                ((GAPIndividualProgramData) result[i]).setProgramRepresentation((InterfaceDataTypeProgram) resBin[i]);
            }
        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() + 1];
            result[0] = (AbstractEAIndividual) this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i + 1] = (AbstractEAIndividual) ((AbstractEAIndividual) partners.get(i)).clone();
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i].giveNewName();
        }
        return result;
    }

    /**
     * This method will return a string description of the GAIndividal
     * noteably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "This is a hybrid Individual:\n";
        result += "The Numbers Part:\n" + ((AbstractEAIndividual) this.numberData).getStringRepresentation();
        result += "\nThe Binarys Part:\n" + ((AbstractEAIndividual) this.programData).getStringRepresentation();
        return result;
    }

    /**
     * *******************************************************************************************************************
     * These are for InterfaceDataTypeDouble
     * <p/>
     * /** This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setDoubleDataLength(int length) {
        this.numberData.setDoubleDataLength(length);
    }

    /**
     * This method returns the length of the double data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.numberData.size();
    }

    /**
     * This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     *
     * @param range The new range for the double data.
     */
    @Override
    public void setDoubleRange(double[][] range) {
        this.numberData.setDoubleRange(range);
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.numberData.getDoubleRange();
    }

    /**
     * This method allows you to read the double data
     *
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        return this.numberData.getDoubleData();
    }

    /**
     * This method allows you to read the double data without
     * an update from the genotype
     *
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        return this.numberData.getDoubleDataWithoutUpdate();
    }

    /**
     * This method allows you to set the phenotype data. To change the genotype, use
     * SetDoubleDataLamarckian().
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoublePhenotype(double[] doubleData) {
        this.numberData.setDoublePhenotype(doubleData);
    }

    /**
     * This method allows you to set the genotype data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoubleGenotype(double[] doubleData) {
        this.numberData.setDoubleGenotype(doubleData);
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
        this.programData.setProgramDataLength(length);
    }

    /**
     * This method allows you to read the program stored as Koza style node tree
     *
     * @return AbstractGPNode representing the binary data.
     */
    @Override
    public InterfaceProgram[] getProgramData() {
        return this.programData.getProgramData();
    }

    /**
     * This method allows you to read the Program data without
     * an update from the genotype
     *
     * @return InterfaceProgram[] representing the Program.
     */
    @Override
    public InterfaceProgram[] getProgramDataWithoutUpdate() {
        return this.programData.getProgramDataWithoutUpdate();
    }

    /**
     * This method allows you to set the program.
     *
     * @param program The new program.
     */
    @Override
    public void SetProgramPhenotype(InterfaceProgram[] program) {
        this.programData.SetProgramPhenotype(program);
    }

    /**
     * This method allows you to set the program.
     *
     * @param program The new program.
     */
    @Override
    public void SetProgramGenotype(InterfaceProgram[] program) {
        this.programData.SetProgramGenotype(program);
    }

    /**
     * This method allows you to set the function area
     *
     * @param area The area contains functions and terminals
     */
    @Override
    public void SetFunctionArea(Object[] area) {
        this.programData.SetFunctionArea(area);
    }

    /**
     * This method allows you to set the function area
     *
     * @return The function area
     */
    @Override
    public Object[] getFunctionArea() {
        return this.programData.getFunctionArea();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "GAP individual";
    }

    /**
     * This method will allow you to set the inner constants
     *
     * @param Numbers The new representation for the inner constants.
     */
    public void setNumbers(InterfaceDataTypeDouble Numbers) {
        this.numberData = Numbers;
    }

    public InterfaceDataTypeDouble getNumbers() {
        return this.numberData;
    }

    public String numbersTipText() {
        return "Choose the type of inner constants to use.";
    }

    /**
     * This method will allow you to set the inner constants
     *
     * @param program The new representation for the program.
     */
    public void setProgramRepresentation(InterfaceDataTypeProgram program) {
        this.programData = program;
    }

    public InterfaceDataTypeProgram getProgramRepresentation() {
        return this.programData;
    }

    public String programRepresentationTipText() {
        return "Choose the type of inner constants to use.";
    }
}

