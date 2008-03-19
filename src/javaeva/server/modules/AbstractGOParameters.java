package javaeva.server.modules;

import java.io.Serializable;

import javaeva.gui.BeanInspector;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.postprocess.InterfacePostProcessParams;
import javaeva.server.go.operators.postprocess.PostProcessParams;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.InterfaceOptimizer;

public abstract class AbstractGOParameters implements InterfaceGOParameters, Serializable {
	public static boolean   TRACE   = false;
	protected long            m_Seed  = (long)0.0;

	// Opt. Algorithms and Parameters
	protected InterfaceOptimizer              m_Optimizer;
	protected InterfaceOptimizationProblem    m_Problem ;
	protected InterfaceTerminator             m_Terminator;
	protected InterfacePostProcessParams		m_PostProc = new PostProcessParams(false);
//	protected int								hcPostProcessSteps = 5000;
	transient protected InterfacePopulationChangedEventListener m_Listener;
//	private boolean postProcess = false;
//	protected double postProcessClusterSigma = 0.05;

	protected AbstractGOParameters() {
	}

	protected AbstractGOParameters(AbstractGOParameters Source) {
		this.m_Optimizer        = Source.m_Optimizer;
		this.m_Problem          = Source.m_Problem;
		this.m_Terminator       = Source.m_Terminator;
		this.m_Optimizer.SetProblem(this.m_Problem);
		this.m_Seed             = Source.m_Seed;
		this.m_PostProc			= Source.m_PostProc;
//		this.hcPostProcessSteps = Source.hcPostProcessSteps;
	}
	
	protected AbstractGOParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
		m_Optimizer = opt;
		m_Problem = prob;
		m_Terminator = term;
		m_PostProc = new PostProcessParams(false);
		opt.SetProblem(prob);
	}

	/** This method allows you to add the LectureGUI as listener to the Optimizer
	 * @param ea
	 */
	public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
		if (this.m_Optimizer != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
	}
	public String toString() {
//		String ret = "\r\nParameters:"+this.m_Problem.getStringRepresentationForProblem(this.m_Optimizer)+"\n"+this.m_Optimizer.getStringRepresentation();
//		return ret;
		StringBuffer sb = new StringBuffer(getName());
		sb.append("\n");
		sb.append("seed=");
		sb.append(m_Seed);
		sb.append("\nProblem: ");
		sb.append(BeanInspector.toString(m_Problem));
		sb.append("\nOptimizer: ");
		sb.append(BeanInspector.toString(m_Optimizer));
		sb.append("\nTerminator: ");
		sb.append(BeanInspector.toString(m_Terminator));
		sb.append("\n");
//		sb.append(m_N)
		return sb.toString();
	}

	public void setOptimizer(InterfaceOptimizer optimizer) {
		this.m_Optimizer = optimizer;
		this.m_Optimizer.SetProblem(this.m_Problem);
		if (this.m_Listener != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
	}
	public InterfaceOptimizer getOptimizer() {
		return this.m_Optimizer;
	}
	public String optimizerTipText() {
		return "Choose an optimizing strategy.";
	}

	public String getName() {
		return "Optimization parameters";
	}

//	public String postProcessStepsTipText() {
//		return "The number of HC post processing steps in fitness evaluations.";
//	}
//	public int getPostProcessSteps() {
//		return hcPostProcessSteps;
//	}
//	public void setPostProcessSteps(int ppSteps) {
//		hcPostProcessSteps = ppSteps;
//	}
	
	/** 
	 * This method will set the problem that is to be optimized.
	 * @param problem
	 */
	public void setProblem (InterfaceOptimizationProblem problem) {
		this.m_Problem = problem;
		this.m_Optimizer.SetProblem(this.m_Problem);
	}
	public InterfaceOptimizationProblem getProblem() {
		return this.m_Problem;
	}
	public String problemTipText() {
		return "Choose the problem that is to optimize and the EA individual parameters.";
	}

	/** This methods allow you to set and get the Seed for the Random Number Generator.
	 * @param x     Long seed.
	 */
	public void setSeed(long x) {
		m_Seed = x;
	}
	public long getSeed() {
		return m_Seed;
	}
	public String seedTipText() {
		return "Random number seed, set to zero to use current system time.";
	}

	/** This method allows you to choose a termination criteria for the
	 * evolutionary algorithm.
	 * @param term  The new terminator
	 */
	public void setTerminator(InterfaceTerminator term) {
		this.m_Terminator = term;
	}
	public InterfaceTerminator getTerminator() {
		return this.m_Terminator;
	}
	public String terminatorTipText() {
		return "Choose a termination criterion.";
	}

//	/**
//	 * @return the postProcess
//	 */
//	public boolean isPostProcess() {
//		return postProcess;
//	}
//	/**
//	 * @param postProcess the postProcess to set
//	 */
//	public void setPostProcess(boolean postProcess) {
//		this.postProcess = postProcess;
//		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessSteps", postProcess);
//		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessClusterSigma", postProcess);
//	}
//	public String postProcessTipText() {
//		return "Toggle post processing of the solutions.";
//	}
//	/**
//	 * @return the postProcessClusterSigma
//	 */
//	public double getPostProcessClusterSigma() {
//		return postProcessClusterSigma;
//	}
//	/**
//	 * @param postProcessClusterSigma the postProcessClusterSigma to set
//	 */
//	public void setPostProcessClusterSigma(double postProcessClusterSigma) {
//		this.postProcessClusterSigma = postProcessClusterSigma;
//	}
//	public String postProcessClusterSigmaTipText() {
//		return "Set the sigma parameter for clustering during post processing. Set to zero for no clustering.";
//	}
	
    public InterfacePostProcessParams getPostProcessParams() {
    	return m_PostProc;
    }
    public void setPostProcessParams(InterfacePostProcessParams ppp) {
    	m_PostProc = ppp;
    }
    public String postProcessParamsTipText() {
    	return "Parameters for the post processing step";
    }
    public void setDoPostProcessing(boolean doPP){
    	m_PostProc.setDoPostProcessing(doPP);
    }
}
