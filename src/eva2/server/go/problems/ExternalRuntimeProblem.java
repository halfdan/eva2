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

public class ExternalRuntimeProblem extends AbstractOptimizationProblem implements Interface2DBorderProblem {

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
			} catch (IOException ioe) {
				ioe.printStackTrace(System.err);
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
    	return m_lowerBound;
    }
    
    protected double getRangeUpperBound(int dim) {
    	return m_upperBound;
    }
    
    protected double[][] getDoubleRange() {
    	return ((InterfaceDataTypeDouble)this.m_Template).getDoubleRange();                             
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
//        double[]        fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);

     //TODO call external runtime
        double[] fit = eval(x);
        individual.SetFitness(fit);
		        
//        if (this.m_UseTestConstraint) {
//            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
//        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
        }
    }
    
	protected double[] eval(double[] x) {
		Process process;
        ProcessBuilder pb;
        
        ArrayList<Double> fitList = new ArrayList<Double>();
		try {
			List<String> parameters=new ArrayList<String>();
			parameters.add(this.m_Command);
			if (additionalArg!=null && (additionalArg.length()>0)) {
				parameters.add(additionalArg);
			}
			for(int i=0;i<this.m_ProblemDimension;i++){
				parameters.add(new String(""+x[i]));
			}
			pb = new ProcessBuilder(parameters);
			pb.directory(new File(this.m_WorkingDir));
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
						fitList.add(new Double(str));
					}
				} else {
					fitList.add(new Double(line)); 
				}
			}
		} catch (IOException e) {
			System.err.println("IO Error in ExternalRuntimeProblem!");
			e.printStackTrace();
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
		double x[] = new double[m_ProblemDimension];
		for (int i=0; i<point.length; i++) x[i]=point[i];
		for (int i=point.length; i<m_ProblemDimension; i++) x[i] = 0;
		return eval(x)[0];
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
