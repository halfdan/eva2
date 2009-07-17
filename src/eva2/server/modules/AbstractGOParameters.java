package eva2.server.modules;

import java.io.Serializable;

import eva2.gui.BeanInspector;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.InterfaceOptimizer;


public abstract class AbstractGOParameters implements InterfaceGOParameters, Serializable {
	public static boolean   TRACE   = false;
	protected long            m_Seed  = (long)0.0;

	// Opt. Algorithms and Parameters
	protected InterfaceOptimizer              m_Optimizer;
	protected InterfaceOptimizationProblem    m_Problem ;
	protected InterfaceTerminator             m_Terminator;
	protected InterfacePostProcessParams		m_PostProc = new PostProcessParams(false);
	transient protected InterfacePopulationChangedEventListener m_Listener;

	protected AbstractGOParameters() {
	}

	protected AbstractGOParameters(AbstractGOParameters Source) {
		this.m_Optimizer        = Source.m_Optimizer;
		this.m_Problem          = Source.m_Problem;
		this.m_Terminator       = Source.m_Terminator;
		this.m_Optimizer.SetProblem(this.m_Problem);
		this.m_Seed             = Source.m_Seed;
		this.m_PostProc			= Source.m_PostProc;
	}
	
	public AbstractGOParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
		m_Optimizer = opt;
		m_Problem = prob;
		m_Terminator = term;
		m_PostProc = new PostProcessParams(false);
		opt.SetProblem(prob);
	}

	/** 
	 * Add a listener to the current optimizer.
	 * 
	 * @param ea
	 */
	public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
		if (this.m_Optimizer != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
	}	
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			if (this.m_Optimizer!=null) this.m_Optimizer.removePopulationChangedEventListener(ea);
			return true;
		} else return false;
	}
	
	public String toString() {
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
		return "Choose an optimization strategy.";
	}

	public String getName() {
		return "Optimization parameters";
	}

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
