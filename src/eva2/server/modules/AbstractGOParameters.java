package eva2.server.modules;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfaceNotifyOnInformers;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractGOParameters implements InterfaceGOParameters, Serializable {	
    protected static final Logger LOGGER = Logger.getLogger(AbstractGOParameters.class.getName());
	protected long randomSeed  = (long)0.0;

	// Opt. Algorithms and Parameters
	protected InterfaceOptimizer              m_Optimizer;
	protected InterfaceOptimizationProblem    m_Problem ;
	protected InterfaceTerminator             m_Terminator;
	protected InterfacePostProcessParams		m_PostProc = new PostProcessParams(false);
	transient protected InterfacePopulationChangedEventListener m_Listener;
	transient private List<InterfaceNotifyOnInformers> toInformAboutInformers = null;
	
	protected AbstractGOParameters() {
	}

	protected AbstractGOParameters(AbstractGOParameters goParameters) {
		this();
		this.m_Optimizer        = goParameters.m_Optimizer;
		this.m_Problem          = goParameters.m_Problem;
		this.m_Terminator       = goParameters.m_Terminator;
		this.m_Optimizer.setProblem(this.m_Problem);
		this.randomSeed             = goParameters.randomSeed;
		this.m_PostProc			= goParameters.m_PostProc;
	}
	
	public AbstractGOParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
		this();
		m_Optimizer = opt;
		m_Problem = prob;
		m_Terminator = term;
		m_PostProc = new PostProcessParams(false);
		opt.setProblem(prob);
	}

	/**
	 * Apply the given GO parameter settings to this instance. This maintains the listeners etc.
	 * 
	 * @param src
	 */
	public void setSameParams(AbstractGOParameters src) {
		setOptimizer(src.m_Optimizer);
		setProblem(src.m_Problem);
		setTerminator(src.m_Terminator);
		this.m_Optimizer.setProblem(this.m_Problem);
		setSeed(src.randomSeed);
		setPostProcessParams(src.m_PostProc);
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
    
    /**
     *
     */
    public void saveInstance(String fileName) {
        try {
            FileOutputStream fileStream = new FileOutputStream(fileName);
            Serializer.storeObject(fileStream, this);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }
    }
    
    public void saveInstance() {
        String fileName = this.getClass().getSimpleName() + ".ser";
        saveInstance(fileName);
    }
	
	public String toString() {
		StringBuilder sBuilder = new StringBuilder(getName());
		sBuilder.append("\n");
		sBuilder.append("seed=");
		sBuilder.append(randomSeed);
		sBuilder.append("\nProblem: ");
		sBuilder.append(BeanInspector.toString(m_Problem));
		sBuilder.append("\nOptimizer: ");
		sBuilder.append(BeanInspector.toString(m_Optimizer));
		sBuilder.append("\nTerminator: ");
		sBuilder.append(BeanInspector.toString(m_Terminator));
		sBuilder.append("\n");
		return sBuilder.toString();
	}

	public void addInformableInstance(InterfaceNotifyOnInformers o) {
		if (toInformAboutInformers==null) toInformAboutInformers=new LinkedList<InterfaceNotifyOnInformers>();
		if (!toInformAboutInformers.contains(o)) toInformAboutInformers.add(o);
		o.setInformers(getInformerList());
	}
	
	public boolean removeInformableInstance(InterfaceNotifyOnInformers o) {
		if (toInformAboutInformers==null) return false;
		else return toInformAboutInformers.remove(o);
	}
	
	private void fireNotifyOnInformers() {
		if (toInformAboutInformers!=null) for (InterfaceNotifyOnInformers listener : toInformAboutInformers) {
			listener.setInformers(getInformerList());
		}
	}

	public void setOptimizer(InterfaceOptimizer optimizer) {
		this.m_Optimizer = optimizer;
		this.m_Optimizer.setProblem(this.m_Problem);
		if (this.m_Listener != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
		fireNotifyOnInformers();
	}
	
	private List<InterfaceAdditionalPopulationInformer> getInformerList() {
		LinkedList<InterfaceAdditionalPopulationInformer> ret = new LinkedList<InterfaceAdditionalPopulationInformer>();
		if (m_Problem instanceof InterfaceAdditionalPopulationInformer) ret.add(m_Problem);
		if (m_Optimizer instanceof InterfaceAdditionalPopulationInformer) ret.add((InterfaceAdditionalPopulationInformer)m_Optimizer);
		return ret;
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
		this.m_Optimizer.setProblem(this.m_Problem);
		fireNotifyOnInformers();
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
		randomSeed = x;
	}
    
    /** 
     * Returns the current seed for the random number generator.
     * 
	 * @return The current seed for the random number generator.
	 */
	public long getSeed() {
		return randomSeed;
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
