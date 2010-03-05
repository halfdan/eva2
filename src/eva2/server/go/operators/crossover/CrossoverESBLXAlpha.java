package eva2.server.go.operators.crossover;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.12.2003
 * Time: 14:50:03
 * To change this template use Options | File Templates.
 */
public class CrossoverESBLXAlpha implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private double                          m_Alpha = 0.5;

    public CrossoverESBLXAlpha() {

    }
    public CrossoverESBLXAlpha(CrossoverESBLXAlpha c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
        this.m_Alpha                    = c.m_Alpha;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverESBLXAlpha(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[]  result = null;
        double[][]              parents, children;
        double[][]              extremeValues;

        result      = new AbstractEAIndividual[partners.size()+1];
        result[0]   = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1]     = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            parents     = new double[partners.size()+1][];
            children    = new double[partners.size()+1][];
            extremeValues = new double[((InterfaceESIndividual)result[0]).getDGenotype().length][2];
            for (int i = 0; i < extremeValues.length; i++) {
                extremeValues[i][0] = Double.MAX_VALUE;
                extremeValues[i][1] = Double.MIN_VALUE;
            }
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual)result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
                for (int j = 0; j < parents[i].length; j++) {
                    extremeValues[j][0] = Math.min(extremeValues[j][0], parents[i][j]);
                    extremeValues[j][1] = Math.max(extremeValues[j][1], parents[i][j]);
                }
            }
            double I = 0;
            for (int i = 0; i < children.length; i++) {
                for(int j = 0; j < children[i].length; j++) {
                    I = (extremeValues[j][1] - extremeValues[j][0])*this.m_Alpha;
                    children[i][j] = RNG.randomDouble(extremeValues[j][0]-I, extremeValues[j][1]+I);
                }
            }
            // write the result back
            for (int i = 0; i < result.length; i++) ((InterfaceESIndividual)result[i]).SetDGenotype(children[i]);
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESBLXAlpha) {
            CrossoverESBLXAlpha cross = (CrossoverESBLXAlpha)crossover;
            if (this.m_Alpha != cross.m_Alpha) return false;
            return true;
        }
        else return false;
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    public String getStringRepresentation() {
        return this.getName();
    }

 /**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES BLX Alpha crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The BLX crossover inits the values within the extreme values plus an additional alpha range (BLX-0.0 equals flat crossover).";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param a   The number of crossovers.
     */
    public void setAlpha(double a) {
        if (a < 0) a = 0;
        this.m_Alpha = a;
    }
    public double getAlpha() {
        return this.m_Alpha;
    }
    public String alphaTipText() {
        return "The alpha of BLX.";
    }
}