package eva2.server.go.problems;

import eva2.gui.Plot;
import eva2.gui.PropertyFilePath;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.constraint.InterfaceConstraint;
import eva2.server.go.operators.moso.InterfaceMOSOConverter;
import eva2.server.go.operators.paretofrontmetrics.InterfaceParetoFrontMetric;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 10.05.2004
 * Time: 17:22:37
 * To change this template use File | Settings | File Templates.
 */
public class TF1Problem extends AbstractMultiObjectiveOptimizationProblem implements java.io.Serializable {
    protected int                       m_ProblemDimension  = 30;
    protected int                       m_OutputDimension   = 2;
    protected double                    m_Noise             = 0.0;
    protected double                    m_XOffSet           = 0.0;
    protected double                    m_YOffSet           = 0.0;
    protected boolean                   m_ApplyConstraints  = false;

//    transient private GraphPointSet     mySet;
    
    public TF1Problem() {
    	super(1.);
    }
    
    public TF1Problem(double borderHigh) {
    	super(borderHigh);
    }
    
    public TF1Problem(TF1Problem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //AbstractMultiObjectiveOptimizationProblem
        if (b.m_MOSOConverter != null)
            this.m_MOSOConverter    = (InterfaceMOSOConverter)b.m_MOSOConverter.clone();
        if (b.m_Metric != null)
            this.m_Metric           = (InterfaceParetoFrontMetric)b.m_Metric.clone();
        if (b.m_ParetoFront != null)
            this.m_ParetoFront      = (Population)b.m_ParetoFront.clone();
        if (b.m_Border != null) {
            this.m_Border = new double[b.m_Border.length][2];
            for (int i = 0; i < this.m_Border.length; i++) {
                this.m_Border[i][0] = b.m_Border[i][0];
                this.m_Border[i][1] = b.m_Border[i][1];
            }
        }
        if (b.m_AreaConst4Parallelization != null) {
            this.m_AreaConst4Parallelization = new ArrayList();
            for (int i = 0; i < b.m_AreaConst4Parallelization.size(); i++) {
                this.m_AreaConst4Parallelization.add(((InterfaceConstraint)b.m_AreaConst4Parallelization.get(i)).clone());
            }
        }
        // TF1Problem
        this.m_ApplyConstraints = b.m_ApplyConstraints;
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_OutputDimension  = b.m_OutputDimension;
        this.m_Noise            = b.m_Noise;
        this.m_XOffSet          = b.m_XOffSet;
        this.m_YOffSet          = b.m_YOffSet;
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new TF1Problem(this);
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    @Override
    public void initPopulation(Population population) {
        this.m_ParetoFront = new Population();

        double[][] newRange = makeRange();

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(newRange);
        
        AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
    }
    
	protected double[][] makeRange() {
		return makeRange(0, 1);
	}
	
	protected double[][] makeRange(double lower, double upper) {
		double[][] newRange = new double[this.m_ProblemDimension][2];
        for (int i = 0; i < this.m_ProblemDimension; i++) {
            newRange[i][0] = lower;
            newRange[i][1] = upper;
        }
		return newRange;
	}

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        for (int i = 0; i < x.length; i++) {
            x[i] -= this.m_XOffSet;
        }
        fitness = this.doEvaluation(x);
        for (int i = 0; i < fitness.length; i++) {
            // add noise to the fitness
            fitness[i] += RNG.gaussianDouble(this.m_Noise);
            fitness[i] += this.m_YOffSet;
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }     
        if (this.m_ApplyConstraints) {
            if (fitness[0] > 0.5) individual.addConstraintViolation(fitness[0]-0.5);
            if (x[1] > 0.1) individual.addConstraintViolation(x[1]-0.1);
            if (x[2] > 0.1) individual.addConstraintViolation(x[2]-0.1);
            if (x[3] > 0.1) individual.addConstraintViolation(x[3]-0.1);
        }
        individual.checkAreaConst4Parallelization(this.m_AreaConst4Parallelization);
    }

    /** Ths method allows you to evaluate a simple bit string to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[] result = new double[2];
        double   g = 0;

        result[0]   = x[0];
        g           = this.g(x);
        result[1]   = g * this.h(result[0], g);

        return result;
    }

    /** The g function
     * @param x    The decision variables.
     * @return Objective variable.
     */
    protected double g(double[] x) {
        double result = 0;

        for (int i = 1; i < x.length; i++) {
            result += x[i];
        }
        result = result * 9/(x.length-1);
        result += 1;
        return result;
    }

    /** The h function
     * @param x    The decision variables.
     * @return Objective variable.
     */
    protected double h(double x, double y) {
        double result = 0;

        result = 1 - Math.sqrt(Math.abs(x/y));
        return result;
    }

    @Override
    public void drawAdditionalData(Plot plot, Population pop, int index) {
		AbstractMultiObjectiveOptimizationProblem.drawWithConstraints(plot, pop, m_Border, index);
	}

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "";

        result += "T1 Problem:\n";
        result += "Here the individual codes a vector of real number x and T1(x)= x is to be minimized.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.m_Noise + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

    public static void main(String[] args) {
        int points = 500;
        String              base        = System.getProperty("user.dir");
        String              FS          = System.getProperty("file.separator");
        PropertyFilePath    fileOutPath = PropertyFilePath.getFilePathFromResource("MOPReference"+FS+"T1_"+points+".txt");
        BufferedWriter      writer      = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileOutPath.getCompleteFilePath()));
        } catch (java.io.IOException ed) {
            System.out.println("Could not open " + fileOutPath.getCompleteFilePath());
            return;
        }
        TF1Problem problem = new TF1Problem();
        System.out.println("This method generates a reference set for the T1 problem with "+points+" sample points.");
        double  ub = 1, lb = 0;
        double  x1, x2;
        String  tmpStr;
        tmpStr = "x1 \t x2";
        for (int i = 0; i < points+1; i++) {
            x1 = (ub-lb)/((double)points) * i;
            x2 = problem.h(x1, 1);
            tmpStr += "\n"+x1+"\t"+x2;
        }
        try {
            writer.write(tmpStr);
            writer.close();
        } catch (java.io.IOException e) {
            System.out.println("DAMM IOException "+e);
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "T1 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "T1 is to be minimized.";
    }

    /** This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     * @param noise     The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) noise = 0;
        this.m_Noise = noise;
    }
    public double getNoise() {
        return this.m_Noise;
    }
    public String noiseTipText() {
        return "Noise level on the fitness value.";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param XOffSet     The offset for the decision variables.
     */
    public void setXOffSet(double XOffSet) {
        this.m_XOffSet = XOffSet;
    }
    public double getXOffSet() {
        return this.m_XOffSet;
    }
    public String xOffSetTipText() {
        return "Choose an offset for the decision variable.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param YOffSet     The offset for the objective value.
     */
    public void setYOffSet(double YOffSet) {
        this.m_YOffSet = YOffSet;
    }
    public double getYOffSet() {
        return this.m_YOffSet;
    }
    public String yOffSetTipText() {
        return "Choose an offset for the objective value.";
    }
    /** This method allows you to set the input dimension of the problem.
     * @param d     The number of input dimensions
     */
    public void setProblemDimension(int d) {
        this.m_ProblemDimension = d;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the x vector at is to be optimized.";
    }

//    /** This method allows you to set the number of objective variables
//     * @param a The number of objective variables
//     */
//    public void setOutputDimension(int a) {
//        this.m_OutputDimension = a;
//        this.m_Border = new double[this.m_OutputDimension][2];
//        for (int i = 0; i < this.m_Border.length; i++) {
//            this.m_Border[i][0] = 0;
//            this.m_Border[i][1] = 5;
//        }
//    }
//    public int getOutputDimension() {
//        return this.m_OutputDimension;
//    }
//    public String outputDimensionTipText() {
//        return "Number of objective variables.";
//    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.m_Template = (AbstractEAIndividual) indy;
    }
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble)this.m_Template;
    }

    /** This method allows you to set a Multiobjective to
     * Singleobjective converter if you choose to.
     * @param b     The new MO2SO converter.
     */
    @Override
    public void setMOSOConverter(InterfaceMOSOConverter b) {
        this.m_MOSOConverter = b;
        this.m_MOSOConverter.setOutputDimension(this.m_OutputDimension);
    }
    @Override
    public InterfaceMOSOConverter getMOSOConverter() {
        return this.m_MOSOConverter;
    }
    @Override
    public String mOSOConverterTipText() {
        return "Choose a Multiobjective to Singleobjective converter.";
    }

//    /** This method allows you to toggel the application of constraints on the problem
//     * @param b     state.
//     */
//    public void setApplyConstraints(boolean b) {
//        this.m_ApplyConstraints = b;
//    }
//    public boolean getApplyConstraints() {
//        return this.m_ApplyConstraints;
//    }
//    public String applyConstraintsTipText() {
//        return "Toggle application of constraint (works only for T1).";
//    }

    /** 
     * Since you can apply single objective optimization algorithms on
     * multi-objective problems, the problem needs a way to log the pareto-
     * front for such algorithms. This is especially the case for the
     * dynamically weighted fitness MOSO.
     * @param pop     The pareto-front archive.
     */
    public void setParetoFront(Population pop) {
        this.m_ParetoFront = pop;
    }
    public Population getParetoFront() {
        return this.m_ParetoFront;
    }
    public String paretoFrontTipText() {
        return "Choose the properties of the local log of the pareto-front.";
    }
}