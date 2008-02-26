package javaeva.server.go.individuals;

import javaeva.server.go.IndividualInterface;
import javaeva.server.go.operators.crossover.CrossoverESDefault;
import javaeva.server.go.operators.mutation.InterfaceMutation;
import javaeva.server.go.operators.mutation.MutateESGlobal;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.Arrays;

/** This individual uses a real-valued genotype to code for double values.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 16:00:03
 * To change this template use Options | File Templates.
 */
public class ESIndividualDoubleData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeDouble, java.io.Serializable {

    private double[]                    m_Genotype;
    private double[]                    m_Phenotype;
    private double[][]                  m_Range;

    public ESIndividualDoubleData() {
        this.m_MutationProbability  = 1.0;
        this.m_MutationOperator     = new MutateESGlobal();
        this.m_CrossoverProbability = 0.5;
        this.m_CrossoverOperator    = new CrossoverESDefault();
        this.m_Genotype             = new double[1];
        this.m_Range                = new double[1][2];
        this.m_Range[0][0]          = -10;
        this.m_Range[0][1]          = 10;
    }

    public ESIndividualDoubleData(ESIndividualDoubleData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype            = new double[individual.m_Phenotype.length];
            System.arraycopy(individual.m_Phenotype, 0, this.m_Phenotype, 0, this.m_Phenotype.length);
        }
        this.m_Genotype                 = new double[individual.m_Genotype.length];
        this.m_Range                    = new double[individual.m_Range.length][2];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i]              = individual.m_Genotype[i];
            this.m_Range[i][0]              = individual.m_Range[i][0];
            this.m_Range[i][1]              = individual.m_Range[i][1];
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
        return (Object) new ESIndividualDoubleData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualDoubleData) {
            ESIndividualDoubleData indy = (ESIndividualDoubleData) individual;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;
            if ((this.m_Range == null) || (indy.m_Range == null)) return false;
            if (this.m_Genotype.length != indy.m_Genotype.length) return false;
            if (this.m_Range.length != indy.m_Range.length) return false;
            for (int i = 0; i < this.m_Genotype.length; i++) {
                if (this.m_Genotype[i] != indy.m_Genotype[i]) return false;
                if (this.m_Range[i][0] != indy.m_Range[i][0]) return false;
                if (this.m_Range[i][1] != indy.m_Range[i][1]) return false;
            }
            return true;
        } else {
            return false;
        }
    }

/************************************************************************************
 * InterfaceDataTypeDouble methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setDoubleDataLength (int length) {
        double[]        newDesPa = new double[length];
        double[][]      newRange = new double[length][2];

        // copy the old values for the decision parameters and the range
        for (int i = 0; ((i < newDesPa.length) && (i < this.m_Genotype.length)); i++) {
            newDesPa[i]     = this.m_Genotype[i];
            newRange[i][0]  = this.m_Range[i][0];
            newRange[i][1]  = this.m_Range[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.m_Genotype.length; (i < newDesPa.length); i++) {
            newDesPa[i]     = this.m_Genotype[this.m_Genotype.length-1];
            newRange[i][0]  = this.m_Range[this.m_Genotype.length-1][0];
            newRange[i][1]  = this.m_Range[this.m_Genotype.length-1][1];
        }
        this.m_Genotype   = newDesPa;
        this.m_Range      = newRange;

//        changed 28.08.03 by request of Spieth
//        this.m_DecisionParameters   = new double[length];
//        this.m_Range                = new double[length][2];
//        for (int i = 0; i < this.m_Range.length; i++) {
//            this.m_Range[i][0] = -10;
//            this.m_Range[i][1] = 10;
//        }
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_Genotype.length;
    }

    /** This method will set the range of the double attributes. If range.length
     * does not equal doubledata.length only range[i] will be used to set all
     * ranges.
     * @param range     The new range for the double data.
     */
    public void SetDoubleRange(double[][] range) {
        if (range.length != this.m_Range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.m_Range.length + "!\n Use method setDoubleDataLength first! (ESIndividualDoubleData:SetDoubleRange)");
        }
        for (int i = 0; ((i < this.m_Range.length) && (i < range.length)); i++) {
            this.m_Range[i][0] = range[i][0];
            this.m_Range[i][1] = range[i][1];
        }
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange() {
        return this.m_Range;
    }

    /** This method allows you to read the double data. A new phenotype array is allocated
     * and the genotype copied.
     * @return BitSet representing the double data.
     */
    public double[] getDoubleData() {
        this.m_Phenotype = new double[this.m_Genotype.length];
        System.arraycopy(this.m_Genotype, 0, this.m_Phenotype, 0, this.m_Genotype.length);
        return this.m_Phenotype;
    }
    
    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    public double[] getDoubleDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the phenotype double data. To change the genotype,
     * use SetDoubleDataLamarkian().
     * @param doubleData    The new double data.
     */
    public void SetDoubleData(double[] doubleData) {
        this.m_Phenotype = doubleData;
    }

    /** This method allows you to set the genotype data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    public void SetDoubleDataLamarkian(double[] doubleData) {
        this.SetDoubleData(doubleData);
        this.m_Genotype = new double[doubleData.length];
        System.arraycopy(doubleData, 0, this.m_Genotype, 0, doubleData.length);
    }

/************************************************************************************
 * AbstractEAIndividual methods
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
        if (obj instanceof double[]) {
            double[]  bs = (double[]) obj;
            if (bs.length != this.m_Genotype.length) System.out.println("Init value and requested length doesn't match!");
            this.SetDoubleDataLamarkian(bs);
        } else {
            this.defaultInit();
            System.out.println("Initial value for ESIndividualDoubleData is not double[]!");
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
    	StringBuilder strB = new StringBuilder(200);
    	strB.append("ESIndividual coding double: (Fitness {");
        for (int i = 0; i < this.m_Fitness.length; i++) {
        	strB.append(this.m_Fitness[i]);
        	strB.append(";");
        }
    	strB.append("}/SelProb{");
    	
        for (int i = 0; i < this.m_SelectionProbability.length; i++) {
        	strB.append(this.m_SelectionProbability[i]);
        	strB.append(";");
        }
        strB.append("}) Value: [");
        for (int i = 0; i < this.m_Genotype.length; i++) {
        	strB.append(this.m_Genotype[i]);
        	strB.append("; ");
        }
        strB.append("]");
        return strB.toString();
    }

/************************************************************************************
 * InterfaceESIndividual methods
 */
    /** This method will allow the user to read the ES 'genotype'
     * @return BitSet
     */
    public double[] getDGenotype() {
        return this.m_Genotype;
    }

    /** This method will allow the user to set the current ES 'genotype'.
     * @param b    The new genotype of the Individual
     */
    public void SetDGenotype(double[] b) {
        this.m_Genotype = b;
        for (int i = 0; i < this.m_Genotype.length; i++) {
            if (this.m_Genotype[i] < this.m_Range[i][0]) this.m_Genotype[i] = this.m_Range[i][0];
            if (this.m_Genotype[i] > this.m_Range[i][1]) this.m_Genotype[i] = this.m_Range[i][1];
        }
    }
    
    /** This method will allow the user to set the current ES 'genotype'.
     * @param b    The new genotype of the Individual
     */
    public void SetDGenotypeNocheck(double[] b) {
        this.m_Genotype = b;
    }
    
    /** This method performs a simple one element mutation on the double vector
     */
    public void defaultMutate() {
        int mutationIndex = RandomNumberGenerator.randomInt(0, this.m_Genotype.length-1);
        this.m_Genotype[mutationIndex] += ((this.m_Range[mutationIndex][1] - this.m_Range[mutationIndex][0])/2)*RandomNumberGenerator.gaussianDouble(0.05f);
        if (this.m_Genotype[mutationIndex] < this.m_Range[mutationIndex][0]) this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][0];
        if (this.m_Genotype[mutationIndex] > this.m_Range[mutationIndex][1]) this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][1];
    }

    /** This method initializes the double vector
     */
    public void defaultInit() {
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i] = RandomNumberGenerator.randomDouble(this.m_Range[i][0], this.m_Range[i][1]);
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
        return "ES individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a ES individual suited to optimize double values.";
    }
    
//    public String toString() {
//    	String str = "Ind " + m_Genotype[0];
//    	for (int i=1; i<this.m_Genotype.length; i++) str += "/" + m_Genotype[i];
//    	str += "~" + m_Fitness[0];
//    	for (int i=1; i<this.m_Fitness.length; i++) str += "/" + m_Fitness[i];
//    	return str;
//    }
}
