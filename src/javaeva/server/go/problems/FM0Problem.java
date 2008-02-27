package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.operators.distancemetric.InterfaceDistanceMetric;
import javaeva.server.go.operators.distancemetric.PhenotypeMetricDoubleData;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.04.2003
 * Time: 11:10:43
 * To change this template use Options | File Templates.
 */
public class FM0Problem extends F1Problem implements Interface2DBorderProblem, InterfaceMultimodalProblem, java.io.Serializable {

    protected InterfaceDistanceMetric   m_Metric = new PhenotypeMetricDoubleData();
    protected double                    m_GlobalOpt = 0;
    protected Population                m_Optima;
    protected double                    m_Epsilon = 0.05;
    protected boolean                   m_UseXCrit = true;
    protected boolean                   m_UseYCrit = true;
    protected double[][]                m_Range;
    protected double[]                  m_Extrema;

    public FM0Problem() {
        this.m_Template         = new ESIndividualDoubleData();
        this.m_ProblemDimension = 2;
        this.m_Range            = new double [this.m_ProblemDimension][2];
        this.m_Range[0][0]      = -2.0;
        this.m_Range[0][1]      =  2.0;
        this.m_Range[1][0]      = -2.8;
        this.m_Range[1][1]      =  2.8;
        this.m_Extrema          = new double[2];
        this.m_Extrema[0]       = -2;
        this.m_Extrema[1]       = 6;
    }

    protected double getRangeUpperBound(int dim) {
    	if (dim == 0) return 2.0;
    	else return 2.8;
    }
    
    protected double getRangeLowerBound(int dim) {
    	return -1*getRangeUpperBound(dim);
    }
    
    public FM0Problem(FM0Problem b) {
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
        //FM0Problem
        this.m_GlobalOpt        = b.m_GlobalOpt;
        this.m_Epsilon         = b.m_Epsilon;
        this.m_UseXCrit         = b.m_UseXCrit;
        this.m_UseYCrit         = b.m_UseYCrit;
        this.m_UseXCrit          = b.m_UseXCrit;
        if (b.m_Metric != null)
            this.m_Metric           = (InterfaceDistanceMetric)((InterfaceDistanceMetric)b.m_Metric).clone();
        if (b.m_Optima != null)
            this.m_Optima           = (Population)((Population)b.m_Optima).clone();
        if (b.m_Extrema != null) {
            this.m_Extrema          = new double[b.m_Extrema.length];
            for (int i = 0; i < this.m_Extrema.length; i++) {
                this.m_Extrema[i] = b.m_Extrema[i];
            }
        }
        if (b.m_Range != null) {
            this.m_Range          = new double[b.m_Range.length][b.m_Range[0].length];
            for (int i = 0; i < this.m_Range.length; i++) {
                for (int j = 0; j < this.m_Range[i].length; j++) {
                    this.m_Range[i][j] = b.m_Range[i][j];
                }
            }
        }
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new FM0Problem(this);
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        this.m_OverallBest = null;

        population.clear();

        this.m_ProblemDimension = 2;
        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(this.m_Range);
        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
        this.initListOfOptima();
    }

    /** Ths method allows you to evaluate a simple bit string to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        result[0]   = this.m_GlobalOpt - evalUnnormalized(x)[0];
        return result;
    }

    /** This method returns the unnormalized function value for an maximisation problem
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] evalUnnormalized(double[] x) {
        double[] result = new double[1];
        result[0]   = Math.sin(2*x[0] - 0.5*Math.PI) + 1 + 2*Math.cos(x[1]) + 0.5*x[0];
        return result;
    }

    /** This method returns the header for the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringHeader(Population pop) {
        return "Solution \t Number of Optima found \t Maximum Peak Ratio";
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(Population pop) {
        String result = "";
        result += AbstractEAIndividual.getDefaultDataString(pop.getBestEAIndividual()) +"\t";
        result += this.getNumberOfFoundOptima(pop)+"\t";
        result += this.getMaximumPeakRatio(pop);
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "M0 function:\n";
        result += "This problem has one global and one local optimum.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.m_Noise + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * Implementation of InterfaceMultimodalProblem
 */

    /** This method allows you to add a 2d optima to the list of optima
     * @param x
     * @param y
     */
    protected void add2DOptimum(double x, double y) {
        double[] point = new double[2];
        point[0] = x;
        point[1] = y;
        addOptimum(point);
    }
    
    /** This method allows you to add a 2d optima to the list of optima
     * @param x
     * @param y
     */
    protected void addOptimum(double[] point) {
        InterfaceDataTypeDouble tmpIndy;
        tmpIndy = (InterfaceDataTypeDouble)((AbstractEAIndividual)this.m_Template).clone();
        tmpIndy.SetDoubleDataLamarkian(point);
        ((AbstractEAIndividual)tmpIndy).SetFitness(this.evalUnnormalized(point));
        this.m_GlobalOpt = Math.max(this.m_GlobalOpt, ((AbstractEAIndividual)tmpIndy).getFitness(0));
        this.m_Optima.add(tmpIndy);
    }

    /** This method will prepare the problem to return a list of all optima
     * if possible and to return quality measures like NumberOfOptimaFound and
     * the MaximumPeakRatio. This method should be called by the user.
     */
    public void initListOfOptima() {
        this.m_GlobalOpt = Double.NEGATIVE_INFINITY;
        this.m_Optima = new Population();
        //this.add2DOptimum((Math.PI - (Math.PI - Math.acos(-1/4.0)) + Math.PI/2.0)/2.0, 0);
        //this.add2DOptimum((-Math.PI - (Math.PI - Math.acos(-1/4.0)) + Math.PI/2.0)/2.0, 0);

        // These are Matlab fminserach results with tol = 0.00000000001
        this.add2DOptimum(1.69713645852390, -0.00000000896995);
        this.add2DOptimum(-1.44445618316078, 0.00000000700284);
    }

    /** This method returns a list of all optima as population
     * @return population
     */
    public Population getAllOptima() {
        return this.m_Optima;
    }

    /** This method returns the overall number of optima
     * @return int
     */
    public int getNumberOfOptima() {
        return this.m_Optima.size();
    }

    /** This method returns the Number of Identified optima
     * @param pop       A population of possible solutions.
     * @return int
     */
    public int getNumberOfFoundOptima(Population pop) {
        int                     result = 0;
        AbstractEAIndividual   posOpt, opt;
        boolean[]               found = new boolean[this.m_Optima.size()];
        for (int i = 0; i < found.length; i++) found[i] = false;

        for (int i = 0; i < pop.size(); i++) {
            posOpt = (AbstractEAIndividual) pop.get(i);
            for (int j = 0; j < this.m_Optima.size(); j++) {
                opt = (AbstractEAIndividual) this.m_Optima.get(j);
                if (!found[j]) {
                    if (this.m_Metric.distance(posOpt, opt) < this.m_Epsilon) found[j] = true;
                }
            }
        }
        for (int i = 0; i < found.length; i++) if (found[i]) result++;
        return result;
    }

    /** This method returns the Maximum Peak Ratio.
     * @param pop       A population of possible solutions.
     * @return double
     */
    public double getMaximumPeakRatio(Population pop) {
        double                  result = 0, sum = 0;
        AbstractEAIndividual   posOpt, opt;
        boolean[]               found = new boolean[this.m_Optima.size()];
        for (int i = 0; i < found.length; i++) {
            found[i] = false;
            sum += ((AbstractEAIndividual)this.m_Optima.get(i)).getFitness(0) ;
            //System.out.println("Optimum " + i + ".: " + (((AbstractEAIndividual)this.m_Optima.get(i)).getFitness(0)));
        }

        for (int i = 0; i < pop.size(); i++) {
            posOpt = (AbstractEAIndividual) pop.get(i);
            for (int j = 0; j < this.m_Optima.size(); j++) {
                if (!found[j]) {
                    opt = (AbstractEAIndividual) this.m_Optima.get(j);
                    if (this.m_Metric.distance(posOpt, opt) < this.m_Epsilon) {
                        found[j] = true;
                        result += this.m_GlobalOpt - posOpt.getFitness(0);
                        //System.out.println("Found Optimum " + j + ".: " + (this.m_GlobalOpt - posOpt.getFitness(0)));
                    }
                }
            }
        }
        return result/sum;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "M0 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "M0(x) = sin(2*x - 0.5*PI) + 1 + 2*cos(y) + 0.5*x is to be maximized.";
    }

    /*** For Debugging only ***/
    /** This method returns the 2d borders of the problem
     * @return double[][]
     */
    public double[][] get2DBorder() {
        return this.m_Range;
    }

    /** This method returns the double value
     * @param point     The double[2] that is queried.
     * @return double
     */
    public double functionValue(double[] point) {
        return evalUnnormalized(point)[0];
    }
    /** This method returns this min and may fitness occuring
     * @return double[]
     */
    public double[] getExtrema() {
        double[] range = new double[2];
        range[0] = -5;
        range[1] = 5;
        return range;
    }

	/**
	 * @return the m_Epsilon
	 */
	public double getEpsilon() {
		return m_Epsilon;
	}

	/**
	 * @param epsilon the m_Epsilon to set
	 */
	public void setEpsilon(double epsilon) {
		m_Epsilon = epsilon;
	}
	
	public String epsilonTipText() {
		return "Epsilon criterion indicating whether an optimum was found";
	}
}
