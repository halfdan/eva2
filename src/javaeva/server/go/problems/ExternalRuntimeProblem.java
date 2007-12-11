package javaeva.server.go.problems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;

public class ExternalRuntimeProblem extends AbstractOptimizationProblem {

	protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;
    protected boolean                   m_UseTestConstraint = false;
    protected String					m_Command			= "";
    protected double					defaultRange		= 1;


    public ExternalRuntimeProblem() {
        this.m_Template         = new ESIndividualDoubleData();
        ((ESIndividualDoubleData)this.m_Template).setDoubleDataLength(m_ProblemDimension);
        ((ESIndividualDoubleData)this.m_Template).SetDoubleRange(makeRange());
    }
    public ExternalRuntimeProblem(ExternalRuntimeProblem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //ExternalRuntimeProblem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_UseTestConstraint = b.m_UseTestConstraint;
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new ExternalRuntimeProblem(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        this.m_OverallBest = null;
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        this.m_OverallBest = null;

        population.clear();

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }
    
    protected double[][] makeRange() {
	    double[][] range = new double[this.m_ProblemDimension][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    protected double getRangeLowerBound(int dim) {
    	return -defaultRange;
    }
    
    protected double getRangeUpperBound(int dim) {
    	return defaultRange;
    }
    
    protected double[][] getDoubleRange() {
    	return ((InterfaceDataTypeDouble)this.m_Template).getDoubleRange();                             
    }

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;
        //System.out.println("Population size: " + population.size());
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);

     //TODO call external runtime
        Process process;
        ProcessBuilder pb;
		try {
			List<String> parameters=new ArrayList<String>();
			parameters.add(this.m_Command);
			for(int i=0;i<this.m_ProblemDimension;i++){
				parameters.add(new String(""+x[i]));
			}
			pb = new ProcessBuilder(parameters);
			process=pb.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			int count=0;
			while ((line = br.readLine()) != null) {
				individual.SetFitness(count,new Double(line));
				count++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (this.m_UseTestConstraint) {
            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
        }
    }

    /** Ths method allows you to evaluate a simple bit string to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[] result = new double[1];
        result[0]     = 0;
        for (int i = 0; i < x.length; i++) {
            result[0]  += Math.pow(x[i], 2);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("External Runtime Problem:\n");
        sb.append("Here the individual codes a vector of real number x is to be minimized on a user given external problem.\nParameters:\n");
        sb.append("Dimension   : "); 
        sb.append(this.m_ProblemDimension);

        return sb.toString();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "External Runtime Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Sphere model.";
    }


    /** Length of the x vector at is to be optimized
     * @param t Length of the x vector at is to be optimized
     */
    public void setProblemDimension(int t) {
        this.m_ProblemDimension = t;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the x vector at is to be optimized.";
    }
    
    /** Length of the x vector at is to be optimized
     * @param t Length of the x vector at is to be optimized
     */
    public void setCommand(String t) {
        this.m_Command = t;
    }
    public String getCommand() {
        return this.m_Command;
    }
    public String commandTipText() {
        return "Command";
    }
    
    /** This method allows you to toggle the application of a simple test constraint.
     * @param b     The mode for the test constraint
     */
    public void setUseTestConstraint(boolean b) {
        this.m_UseTestConstraint = b;
    }
    public boolean getUseTestConstraint() {
        return this.m_UseTestConstraint;
    }
    public String useTestConstraintTipText() {
        return "Just a simple test constraint of x[0] >= 1.";
    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.m_Template = (AbstractEAIndividual)indy;
    }
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble)this.m_Template;
    }
	public double functionValue(double[] point) {
		double x[] = new double[m_ProblemDimension];
		for (int i=0; i<point.length; i++) x[i]=point[i];
		for (int i=point.length; i<m_ProblemDimension; i++) x[i] = 0;
		return Math.sqrt(doEvaluation(x)[0]);
	}
	public double[][] get2DBorder() {
		return getDoubleRange();
	}
	
	/**
	 * A (symmetric) absolute range limit.
	 * 
	 * @return value of the absolute range limit
	 */
	public double getDefaultRange() {
		return defaultRange;
	}
	
	/**
	 * Set a (symmetric) absolute range limit.
	 * 
	 * @param defaultRange
	 */
	public void setDefaultRange(double defaultRange) {
		this.defaultRange = defaultRange;
		if (((InterfaceDataTypeDouble)this.m_Template).getDoubleData().length != m_ProblemDimension) {
			((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(m_ProblemDimension);
		}
		((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
	}
	
	public String defaultRangeTipText() {
		return "Absolute limit for the symmetric range in any dimension (not used for all f-problems)";
	}

}
