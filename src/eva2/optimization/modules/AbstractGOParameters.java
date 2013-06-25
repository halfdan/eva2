package eva2.optimization.modules;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceGOParameters;
import eva2.optimization.go.InterfaceNotifyOnInformers;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.operators.postprocess.InterfacePostProcessParams;
import eva2.optimization.operators.postprocess.PostProcessParams;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractGOParameters implements InterfaceGOParameters, Serializable {	
    protected static final Logger LOGGER = Logger.getLogger(AbstractGOParameters.class.getName());
	protected long randomSeed  = (long)0.0;

	// Opt. Algorithms and Parameters
	protected InterfaceOptimizer optimizer;
	protected InterfaceOptimizationProblem problem;
	protected InterfaceTerminator terminator;
	protected InterfacePostProcessParams postProcessing = new PostProcessParams(false);
	transient protected InterfacePopulationChangedEventListener populationChangedEventListener;
	transient private List<InterfaceNotifyOnInformers> toInformAboutInformers = null;
	
	protected AbstractGOParameters() {
	}

	protected AbstractGOParameters(AbstractGOParameters goParameters) {
		this();
		this.optimizer = goParameters.optimizer;
		this.problem = goParameters.problem;
		this.terminator = goParameters.terminator;
		this.optimizer.setProblem(this.problem);
		this.randomSeed = goParameters.randomSeed;
		this.postProcessing = goParameters.postProcessing;
	}
	
	public AbstractGOParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
		this();
		optimizer = opt;
		problem = prob;
		terminator = term;
		postProcessing = new PostProcessParams(false);
		opt.setProblem(prob);
	}

	/**
	 * Apply the given GO parameter settings to this instance. This maintains the listeners etc.
	 * 
	 * @param src
	 */
	public void setSameParams(AbstractGOParameters src) {
		setOptimizer(src.optimizer);
		setProblem(src.problem);
		setTerminator(src.terminator);
		this.optimizer.setProblem(this.problem);
		setSeed(src.randomSeed);
		setPostProcessParams(src.postProcessing);
	}
	
	/** 
	 * Add a listener to the current optimizer.
	 * 
	 * @param ea
	 */
	public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
		this.populationChangedEventListener = ea;
		if (this.optimizer != null) {
            this.optimizer.addPopulationChangedEventListener(this.populationChangedEventListener);
        }
	}

	public boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener ==ea) {
            populationChangedEventListener =null;
            if (this.optimizer !=null) {
                this.optimizer.removePopulationChangedEventListener(ea);
            }
            return true;
        } else {
            return false;
        }
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
    
    @Override
    public void saveInstance() {
        String fileName = this.getClass().getSimpleName() + ".ser";
        saveInstance(fileName);
    }
	
    @Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder(getName());
		sBuilder.append("\n");
		sBuilder.append("seed=");
		sBuilder.append(randomSeed);
		sBuilder.append("\nProblem: ");
		sBuilder.append(BeanInspector.toString(problem));
		sBuilder.append("\nOptimizer: ");
		sBuilder.append(BeanInspector.toString(optimizer));
		sBuilder.append("\nTerminator: ");
		sBuilder.append(BeanInspector.toString(terminator));
		sBuilder.append("\n");
		return sBuilder.toString();
	}

    @Override
	public void addInformableInstance(InterfaceNotifyOnInformers o) {
		if (toInformAboutInformers==null) {
                toInformAboutInformers=new LinkedList<InterfaceNotifyOnInformers>();
            }
		if (!toInformAboutInformers.contains(o)) {
                toInformAboutInformers.add(o);
            }
		o.setInformers(getInformerList());
	}
	
    @Override
	public boolean removeInformableInstance(InterfaceNotifyOnInformers o) {
		if (toInformAboutInformers==null) {
                return false;
            }
		else {
                return toInformAboutInformers.remove(o);
            }
	}
	
	private void fireNotifyOnInformers() {
		if (toInformAboutInformers!=null) {
                for (InterfaceNotifyOnInformers listener : toInformAboutInformers) {
listener.setInformers(getInformerList());
}           }
	}

    @Override
	public void setOptimizer(InterfaceOptimizer optimizer) {
		this.optimizer = optimizer;
		this.optimizer.setProblem(this.problem);
		if (this.populationChangedEventListener != null) {
                this.optimizer.addPopulationChangedEventListener(this.populationChangedEventListener);
            }
		fireNotifyOnInformers();
	}
	
	private List<InterfaceAdditionalPopulationInformer> getInformerList() {
		LinkedList<InterfaceAdditionalPopulationInformer> ret = new LinkedList<InterfaceAdditionalPopulationInformer>();
		if (problem instanceof InterfaceAdditionalPopulationInformer) {
                ret.add(problem);
            }
		if (optimizer instanceof InterfaceAdditionalPopulationInformer) {
                ret.add((InterfaceAdditionalPopulationInformer) optimizer);
            }
		return ret;
	}

    @Override
	public InterfaceOptimizer getOptimizer() {
		return this.optimizer;
	}
	public String optimizerTipText() {
		return "Choose an optimization strategy.";
	}

    @Override
	public String getName() {
		return "Optimization parameters";
	}

	/** 
	 * This method will set the problem that is to be optimized.
	 * @param problem
	 */
    @Override
	public void setProblem (InterfaceOptimizationProblem problem) {
		this.problem = problem;
		this.optimizer.setProblem(this.problem);
		fireNotifyOnInformers();
	}
	
    @Override
	public InterfaceOptimizationProblem getProblem() {
		return this.problem;
	}
    @Override
	public String problemTipText() {
		return "Choose the problem that is to optimize and the EA individual parameters.";
	}

	/** This methods allow you to set and get the Seed for the Random Number Generator.
	 * @param x     Long seed.
	 */
    @Override
	public void setSeed(long x) {
		randomSeed = x;
	}
    
    /** 
     * Returns the current seed for the random number generator.
     * 
	 * @return The current seed for the random number generator.
	 */
    @Override
	public long getSeed() {
		return randomSeed;
	}
    
    @Override
	public String seedTipText() {
		return "Random number seed, set to zero to use current system time.";
	}

	/** This method allows you to choose a termination criteria for the
	 * evolutionary algorithm.
	 * @param term  The new terminator
	 */
    @Override
	public void setTerminator(InterfaceTerminator term) {
		this.terminator = term;
	}
    @Override
	public InterfaceTerminator getTerminator() {
		return this.terminator;
	}
    @Override
	public String terminatorTipText() {
		return "Choose a termination criterion.";
	}

    @Override
    public InterfacePostProcessParams getPostProcessParams() {
    	return postProcessing;
    }
    @Override
    public void setPostProcessParams(InterfacePostProcessParams ppp) {
    	postProcessing = ppp;
    }
    @Override
    public String postProcessParamsTipText() {
    	return "Parameters for the post processing step";
    }
    @Override
    public void setDoPostProcessing(boolean doPP){
    	postProcessing.setDoPostProcessing(doPP);
    }
}
