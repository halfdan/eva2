package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 14:33:07
 * To change this template use File | Settings | File Templates.
 */
public class F10Problem extends F1Problem implements java.io.Serializable {

    private double m_D          = 1.5;
    private double m_b          = 2.3;
    private int     m_Iterations = 20;

    public F10Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F10Problem(F10Problem b) {
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
        this.m_D                = b.m_D;
        this.m_b                = b.m_b;
        this.m_Iterations       = b.m_Iterations;
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F10Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        double c1 = this.calculateC(1);
        result[0]     = 0.;
        for (int i = 0; i < x.length-1; i++) {
            result[0]  += ((this.calculateC(x[i]))/(c1 * Math.pow(Math.abs(x[i]),2-this.m_D))) + Math.pow(x[i], 2) -1;
        }
        return result;
    }

    private double calculateC(double x) {
        double result = 0;

        for (int i = -this.m_Iterations; i < this.m_Iterations+1; i++) {
            result += (1-Math.cos(Math.pow(this.m_b, i)*x))/(Math.pow(this.m_b, (2-this.m_D)*i));
        }

        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F10 Weierstrass-Mandelbrot Fractal Function:\n";
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
        return "F10 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Weierstrass-Mandelbrot Fractal Function";
    }

    /** This method allows you to set/get d.
     * @param d     The d.
     */
    public void setD(double d) {
        if (d < 1) d = 1;
        if (d > 2) d = 2;
        this.m_D = d;
    }
    public double getD() {
        return this.m_D;
    }
    public String dTipText() {
        return "Set 1 < D < 2.";
    }

    /** This method allows you to set/get b
     * @param b     The b.
     */
    public void setb(double b) {
        if (b < 1.000001) b = 1.000001;
        this.m_b = b;
    }
    public double getb() {
        return this.m_b;
    }
    public String bTipText() {
        return "Choose b > 1.";
    }

    /** This method allows you to set/get Iterations
     * @param iters     The Iterations.
     */
    public void setIterations(int iters) {
        if (iters < 2) iters = 2;
        this.m_Iterations = iters;
    }
    public int getIterations() {
        return this.m_Iterations;
    }
    public String iterationsTipText() {
        return "Choose the number of iterations per evaluation.";
    }
}