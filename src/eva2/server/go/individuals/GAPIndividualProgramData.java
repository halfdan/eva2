package eva2.server.go.individuals;


import java.util.ArrayList;

import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.individuals.codings.gp.GPArea;
import eva2.server.go.individuals.codings.gp.InterfaceProgram;
import eva2.server.go.operators.crossover.CrossoverESDefault;
import eva2.server.go.operators.crossover.CrossoverGPDefault;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESDefault;
import eva2.server.go.operators.mutation.MutateGPDefault;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/** This individual combines a real-valued phenotype with a tree-based phenotype.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.04.2003
 * Time: 10:04:44
 * To change this template use Options | File Templates.
 */
public class GAPIndividualProgramData extends AbstractEAIndividual implements InterfaceDataTypeProgram, InterfaceDataTypeDouble, java.io.Serializable {
    
    private InterfaceDataTypeDouble     m_Numbers   = new ESIndividualDoubleData();
    private InterfaceDataTypeProgram    m_Program   = new GPIndividualProgramData();

    public GAPIndividualProgramData() {
        this.m_MutationProbability  = 1.0;
        this.m_CrossoverProbability = 1.0;
        this.m_Numbers      = new GAIndividualDoubleData();
        this.m_Program      = new GPIndividualProgramData();
    }

    public GAPIndividualProgramData(GAPIndividualProgramData individual) {
        this.m_Numbers      = (InterfaceDataTypeDouble)((AbstractEAIndividual)individual.getNumbers()).clone();
        this.m_Program      = (InterfaceDataTypeProgram)((AbstractEAIndividual)individual.getProgramRepresentation()).clone();

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
        return (Object) new GAPIndividualProgramData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAPIndividualProgramData) {
            GAPIndividualProgramData indy = (GAPIndividualProgramData)individual;
            if (!((AbstractEAIndividual)this.m_Numbers).equalGenotypes((AbstractEAIndividual)indy.m_Numbers)) return false;
            if (!((AbstractEAIndividual)this.m_Program).equalGenotypes((AbstractEAIndividual)indy.m_Program)) return false;
            return true;
        } else {
            return false;
        }
    }

    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    public void init(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual)this.m_Numbers).init(opt);
        ((AbstractEAIndividual)this.m_Program).init(opt);
    }

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof Object[]) {
            if (((Object[])obj)[0] instanceof double[]) {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[0], opt);
                ((AbstractEAIndividual)this.m_Program).initByValue(((Object[])obj)[1], opt);
            } else {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[1], opt);
                ((AbstractEAIndividual)this.m_Program).initByValue(((Object[])obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual)this.m_Numbers).init(opt);
            ((AbstractEAIndividual)this.m_Program).init(opt);
            System.out.println("Initial value for GAPIndividualDoubleData is not suitable!");
        }
    }

    /** This method will mutate the individual randomly
     */
    public void mutate() {
        if (RNG.flipCoin(this.m_MutationProbability))((AbstractEAIndividual)this.m_Numbers).mutate();
        if (RNG.flipCoin(this.m_MutationProbability))((AbstractEAIndividual)this.m_Program).mutate();
    }

    /** This method will mate the Individual with given other individuals
     * of the same type.
     * @param partners  The possible partners
     * @return offsprings
     */
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RNG.flipCoin(this.m_CrossoverProbability)) {
            AbstractEAIndividual[]  resNum, resBin;
            AbstractEAIndividual    numTmp, binTmp;
            Population              numPop, binPop;

            numTmp = (AbstractEAIndividual)this.getNumbers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) numPop.add(((GAPIndividualProgramData)partners.get(i)).getNumbers());
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual)this.getProgramRepresentation();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) binPop.add(((GAPIndividualProgramData)partners.get(i)).getProgramRepresentation());
            resBin = binTmp.mateWith(binPop);

            result = new GAPIndividualProgramData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GAPIndividualProgramData(this);
                ((GAPIndividualProgramData)result[i]).setNumbers((InterfaceDataTypeDouble)resNum[i]);
                ((GAPIndividualProgramData)result[i]).setProgramRepresentation((InterfaceDataTypeProgram)resBin[i]);
            }
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
        String result = "This is a hybrid Individual:\n";
        result += "The Numbers Part:\n"+((AbstractEAIndividual)this.m_Numbers).getStringRepresentation();
        result += "\nThe Binarys Part:\n"+((AbstractEAIndividual)this.m_Program).getStringRepresentation();
        return result;
    }

/**********************************************************************************************************************
 * These are for InterfaceDataTypeDouble

    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setDoubleDataLength (int length) {
        this.m_Numbers.setDoubleDataLength(length);
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_Numbers.size();
    }

    /** This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     * @param range     The new range for the double data.
     */
    public void SetDoubleRange(double[][] range) {
        this.m_Numbers.SetDoubleRange(range);
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange() {
        return this.m_Numbers.getDoubleRange();
    }

    /** This method allows you to read the double data
     * @return BitSet representing the double data.
     */
    public double[] getDoubleData() {
        return this.m_Numbers.getDoubleData();
    }

    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    public double[] getDoubleDataWithoutUpdate() {
        return this.m_Numbers.getDoubleDataWithoutUpdate();
    }

    /** This method allows you to set the phenotype data. To change the genotype, use
     * SetDoubleDataLamarckian().
     * @param doubleData    The new double data.
     */
    public void SetDoublePhenotype(double[] doubleData) {
        this.m_Numbers.SetDoublePhenotype(doubleData);
    }

    /** This method allows you to set the genotype data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    public void SetDoubleGenotype(double[] doubleData) {
        this.m_Numbers.SetDoubleGenotype(doubleData);
    }

/************************************************************************************
 * InterfaceDataTypeProgram methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setProgramDataLength (int length) {
        this.m_Program.setProgramDataLength(length);
    }

    /** This method allows you to read the program stored as Koza style node tree
     * @return AbstractGPNode representing the binary data.
     */
    public InterfaceProgram[] getProgramData() {
        return this.m_Program.getProgramData();
    }

    /** This method allows you to read the Program data without
     * an update from the genotype
     * @return InterfaceProgram[] representing the Program.
     */
    public InterfaceProgram[] getProgramDataWithoutUpdate() {
        return this.m_Program.getProgramDataWithoutUpdate();
    }

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    public void SetProgramPhenotype(InterfaceProgram[] program) {
        this.m_Program.SetProgramPhenotype(program);
    }

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    public void SetProgramGenotype(InterfaceProgram[] program) {
        this.m_Program.SetProgramGenotype(program);
    }

    /** This method allows you to set the function area
     * @param area  The area contains functions and terminals
     */
    public void SetFunctionArea(Object[] area) {
        this.m_Program.SetFunctionArea(area);
    }

    /** This method allows you to set the function area
     * @return The function area
     */
    public Object[] getFunctionArea() {
        return this.m_Program.getFunctionArea();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GAP individual";
    }

    /** This method will allow you to set the inner constants
     * @param Numbers     The new representation for the inner constants.
      */
    public void setNumbers(InterfaceDataTypeDouble Numbers) {
        this.m_Numbers = Numbers;
    }
    public InterfaceDataTypeDouble getNumbers() {
        return this.m_Numbers;
    }
    public String numbersTipText() {
        return "Choose the type of inner constants to use.";
    }
    /** This method will allow you to set the inner constants
     * @param program     The new representation for the program.
      */
    public void setProgramRepresentation(InterfaceDataTypeProgram program) {
        this.m_Program = program;
    }
    public InterfaceDataTypeProgram getProgramRepresentation() {
        return this.m_Program;
    }
    public String programRepresentationTipText() {
        return "Choose the type of inner constants to use.";
    }
}

