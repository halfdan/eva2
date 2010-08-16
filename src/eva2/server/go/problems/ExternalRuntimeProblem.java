package eva2.server.go.problems;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;



import eva2.server.go.problems.Interface2DBorderProblem;
import eva2.tools.math.Mathematics;

public class ExternalRuntimeProblem extends AbstractOptimizationProblem implements Interface2DBorderProblem, InterfaceProblemDouble {

	protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;
//    protected boolean                   m_UseTestConstraint = false;
    protected String					m_Command			= "";
    protected String					m_WorkingDir		= "";
    protected double					m_upperBound		= 10;
    protected double					m_lowerBound		= 0;
	private String additionalArg="";

	// Private Subclass to redirect Streams within an extra Thread to avoid dead
	// locks
	private static class MonitorInputStreamThread extends Thread {
		private Reader reader;
		private Writer writer;

		public MonitorInputStreamThread(InputStream in) {
			reader = new InputStreamReader(new BufferedInputStream(in));
			writer = new OutputStreamWriter(System.err);
			setDaemon(true);
		}

		public void run() {
			try {
				int c;
				while ((c = reader.read()) != -1) {
					writer.write(c);
					writer.flush();
				}
//				System.out.println("monitor-thread finished!");
			} catch (IOException ioe) {
				ioe.printStackTrace(System.err);
			} finally {
				try {
					reader.close();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

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
//        this.m_UseTestConstraint = b.m_UseTestConstraint;
        m_Command = b.m_Command;
        m_lowerBound = b.m_lowerBound;
        m_upperBound = b.m_upperBound;
        
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
        this.m_OverallBest = null;

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());

        AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
    }
    
    public double[][] makeRange() {
	    double[][] range = new double[this.m_ProblemDimension][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    public double getRangeLowerBound(int dim) {
    	return m_lowerBound;
    }
    
    public double getRangeUpperBound(int dim) {
    	return m_upperBound;
    }
    
    protected double[][] getDoubleRange() {
    	return ((InterfaceDataTypeDouble)this.m_Template).getDoubleRange();                             
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evaluatated
     */
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
//        double[]        fitness;

        x = getXVector(individual);

        double[] fit = eval(x);
        individual.SetFitness(fit);
		        
//        if (this.m_UseTestConstraint) {
//            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
//        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
        }
    }
    
	protected double[] getXVector(AbstractEAIndividual individual) {
		double[] x;
		x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
		return x;
	}
	
	public static List<String> runProcess(String[] parameters, String workingDir) {
		List<String> params = new ArrayList<String>(parameters.length);
		for (String str : parameters) {
			params.add(str);
		}
		return runProcess(params, workingDir);
	}
	
    public static List<String> runProcess(List<String> parameters, String workingDir) {
		Process process;
        ProcessBuilder pb;
		List<String> results  = new ArrayList<String>(); 
		try {
			pb = new ProcessBuilder(parameters);
			pb.directory(new File(workingDir));
			process=pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread thread = new MonitorInputStreamThread(process.getErrorStream());//grab the Error Stream
            thread.start();
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.contains(" ")) {
					String[] parts = line.split(" ");
					for (String str : parts) {
						results.add(str);
					}
				} else {
					results.add(line); 
				}
			}
			br.close();
		} catch (IOException e) {
			System.err.println("IO Error when calling external command!");
			e.printStackTrace();
		}
		return results;
	}

    public double[] eval(double[] x) {
    	if (x==null) throw new RuntimeException("Error, x=null value received in ExternalRuntimeProblem.eval");
    	ArrayList<Double> fitList = new ArrayList<Double>();

    	List<String> parameters=new ArrayList<String>();
    	parameters.add(this.m_Command);
    	if (additionalArg!=null && (additionalArg.length()>0)) parameters.add(additionalArg);
    	for(int i=0;i<this.m_ProblemDimension;i++){
    		String p = prepareParameter(x, i);
    		parameters.add(p);
    	}

    	List<String> res = runProcess(parameters, m_WorkingDir);
    	try {
    		for (String str : res) {
    			fitList.add(new Double(str));
    		}
    	} catch (NumberFormatException e) {
    		System.err.println("Error: " + m_Command + " delivered malformatted output for " + BeanInspector.toString(x));
    		e.printStackTrace();
    	}
    	double[] fit = new double[fitList.size()];
    	for (int i=0; i<fit.length; i++) {
    		fit[i] = fitList.get(i);
    	}
    	return fit;
    }

    /**
     * How to prepare a given parameter within a double array to present it 
     * to the external program.
     * 
     * @param x
     * @param i
     * @return
     */
    protected String prepareParameter(double[] x, int i) {
		return new String(""+x[i]);
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
    public static String globalInfo() {
        return "Use an external command as target function.";
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
    
    /** Working dir of the external runtime
     * @param t working directory
     */
    public void setWorkingDirectory(String t) {
        this.m_WorkingDir = t;
    }
    public String getWorkingDirectory() {
        return this.m_WorkingDir;
    }
    public String workingDirectoryTipText() {
        return "Working directory";
    }
    
//    /** This method allows you to toggle the application of a simple test constraint.
//     * @param b     The mode for the test constraint
//     */
//    public void setUseTestConstraint(boolean b) {
//        this.m_UseTestConstraint = b;
//    }
//    public boolean getUseTestConstraint() {
//        return this.m_UseTestConstraint;
//    }
//    public String useTestConstraintTipText() {
//        return "Just a simple test constraint of x[0] >= 1.";
//    }

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
		return eval(project2DPoint(point))[0];
	}
    public double[] project2DPoint(double[] point) {
    	return Mathematics.expandVector(point, getProblemDimension(), 0.);
    }
	public double[][] get2DBorder() {
		return getDoubleRange();
	}
	
	/**
	 * @return the m_upperBound
	 */
	public double getRangeUpperBound() {
		return m_upperBound;
	}
	/**
	 * @param bound the m_upperBound to set
	 */
	public void setRangeUpperBound(double bound) {
		m_upperBound = bound;
	}
	
	public String rangeUpperBoundTipText() {
		return "Upper bound of the search space in any dimension.";
	}
	/**
	 * @return the m_lowerBound
	 */
	public double getRangeLowerBound() {
		return m_lowerBound;
	}
	/**
	 * @param bound the m_lowerBound to set
	 */
	public void setRangeLowerBound(double bound) {
		m_lowerBound = bound;
	}	
	
	public String rangeLowerBoundTipText() {
		return "Lower bound of the search space in any dimension.";
	}

	public String additionalArgumentTipText() {
		return "Optionally define an additional (first) argument for the command line command.";
	}
	
    public String getAdditionalArgument() {
		return additionalArg;
	}
	public void setAdditionalArgument(String additionalArg) {
		this.additionalArg = additionalArg;
	}
}
