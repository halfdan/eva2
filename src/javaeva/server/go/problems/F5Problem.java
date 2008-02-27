package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.populations.Population;
import javaeva.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:30:52
 * To change this template use File | Settings | File Templates.
 */
public class F5Problem extends F1Problem implements java.io.Serializable {

    public F5Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F5Problem(F5Problem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //F1Problem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_Noise            = b.m_Noise;
        this.m_XOffSet          = b.m_XOffSet;
        this.m_YOffSet          = b.m_YOffSet;
        this.m_UseTestConstraint = b.m_UseTestConstraint;        
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        this.m_OverallBest = null;

        population.clear();

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        double[][] range = new double[this.m_ProblemDimension][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = -65.536;
            range[i][1] = 65.536;
        }
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(range);

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F5Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[]    result = new double[1];
        double      tmp;
        result[0]     = 0;
        for (int i = 0; i < x.length-1; i++) {
            tmp = 0;
            for (int j = 0; j <= i; j++) {
                tmp += x[j];
            }
            result[0] += Math.pow(tmp, 2);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F5 Schwefel's Function:\n";
        result += "This problem is unimodal.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.m_Noise + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "F5 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Schwefel's Function.";
    }
}