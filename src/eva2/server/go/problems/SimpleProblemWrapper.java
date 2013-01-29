package eva2.server.go.problems;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;
import java.util.BitSet;
import simpleprobs.InterfaceSimpleProblem;
import simpleprobs.SimpleF1;
import simpleprobs.SimpleProblemBinary;
import simpleprobs.SimpleProblemDouble;

public class SimpleProblemWrapper extends AbstractOptimizationProblem {
	InterfaceSimpleProblem<?> simProb = new SimpleF1();
	protected double m_DefaultRange = 10;
	protected double m_Noise = 0;
	private int repaintMinWait = 20;
	private int repaintCnt = 0;
	transient Plot m_plot = null;
	transient AbstractEAIndividual bestIndy = null;
	String plotFunc = "plotBest";
	transient Class[] plotFuncSig = new Class[]{Plot.class, AbstractEAIndividual.class};
	transient private boolean resetTemplate = true;
	
	public SimpleProblemWrapper() {
		m_plot = null;
		initTemplate();
	}
	
	public SimpleProblemWrapper(SimpleProblemWrapper other) {
		other.m_DefaultRange = m_DefaultRange;
		other.m_Noise = m_Noise;
		// warning! this does no deep copy!
		setSimpleProblem(other.simProb);
	}
	
	@Override
	public Object clone() {
		return new SimpleProblemWrapper(this);
	}

	@Override
	public void evaluate(AbstractEAIndividual individual) {
		if (simProb instanceof SimpleProblemDouble) {
	        double[]        x;
	        double[]        fitness;

	        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
	        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
	        // evaluate the vector
	        fitness = ((SimpleProblemDouble)simProb).eval(x);
	        // if indicated, add Gaussian noise
	        if (m_Noise != 0) RNG.addNoise(fitness, m_Noise); 
	        // set the fitness 
	        individual.setFitness(fitness);
		} else if (simProb instanceof SimpleProblemBinary) {
	        BitSet          tmpBitSet;
	        double[]        result;
	        
	        tmpBitSet   = ((InterfaceDataTypeBinary) individual).getBinaryData();
	        // evaluate the fitness
	        result = ((SimpleProblemBinary)simProb).eval(tmpBitSet);
	        // set the fitness
	        individual.setFitness(result);
		} else {
			System.err.println("Error in SimpleProblemWrapper: " + simProb.getClass().getName() + " is unknown type!");
		}
	}

    @Override
	public void evaluatePopulationStart(Population population) {
		if (m_plot != null && (!m_plot.isValid())) {
			openPlot();
		}
	}
	
    @Override
	public void evaluatePopulationEnd(Population population) {
		super.evaluatePopulationEnd(population);
		repaintCnt += population.size();
		if (m_plot != null) {
			if (repaintCnt >= repaintMinWait) { // dont repaint always for small pops
				if ((bestIndy == null) || (population.getBestEAIndividual().isDominant(bestIndy.getFitness()))) {
					// only paint improvement
					bestIndy = population.getBestEAIndividual();
					Object[] args = new Object[2];
					args[0] = m_plot;
					args[1] = bestIndy;
//					System.out.println(population.getBestEAIndividual().getStringRepresentation());
					BeanInspector.callIfAvailable(simProb, plotFunc, args);
				}
				repaintCnt = 0;
			}
		}
	}
	
	@Override
	public void initPopulation(Population population) {
        initTemplate();
        AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
	}

	@Override
	public void initProblem() {
		bestIndy = null;
		initTemplate();
		setSimpleProblem(getSimpleProblem()); // possibly create plot
		BeanInspector.callIfAvailable(simProb, "initProblem", null);	// possibly call initProblem of subproblem  
	}

	protected void initTemplate() {
		if (resetTemplate) {
			if (simProb instanceof SimpleProblemDouble) {
				this.m_Template         = new ESIndividualDoubleData();
			} else if (simProb instanceof SimpleProblemBinary) {
				this.m_Template         = new GAIndividualBinaryData();
			}
		}
		if (m_Template instanceof InterfaceDataTypeDouble) {
			((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(simProb.getProblemDimension());
			((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
		} else if (m_Template instanceof InterfaceDataTypeBinary) {
			((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(simProb.getProblemDimension());
		} else System.err.println("Individual type not valid!");
	}
	
    protected double[][] makeRange() {
	    double[][] range = new double[simProb.getProblemDimension()][2];
	    for (int i = 0; i < range.length; i++) {
	        range[i][0] = getRangeLowerBound(i);
	        range[i][1] = getRangeUpperBound(i);
	    }
	    return range;
    }
    
    protected double getRangeLowerBound(int dim) {
    	return -m_DefaultRange;
    }
    
    protected double getRangeUpperBound(int dim) {
    	return m_DefaultRange;
    }
	
	/**
	 * @return the simProb
	 */
	public InterfaceSimpleProblem<?> getSimpleProblem() {
		return simProb;
	}
	
	private void openPlot() {
		m_plot = new Plot("SimpleProblemWrapper", "x", "y", true);
	}

	/**
	 * @param simProb the simProb to set
	 */
	public void setSimpleProblem(InterfaceSimpleProblem<?> simProb) {
		this.simProb = simProb;
		initTemplate();
		GenericObjectEditor.setShowProperty(getClass(), "noise", (simProb instanceof SimpleProblemDouble));
		GenericObjectEditor.setShowProperty(getClass(), "defaultRange", (simProb instanceof SimpleProblemDouble));
		if (BeanInspector.hasMethod(simProb, plotFunc, plotFuncSig) != null) {
			if (m_plot == null) openPlot();
			else {
				if (!m_plot.isValid()) {
					m_plot.dispose();
					openPlot();
				} else m_plot.clearAll();
			}
		} else if (m_plot != null) {
			m_plot.dispose();
			m_plot = null;
		}
	}

	/**
	 * 
	 */
	public String simpleProblemTipText() {
		return "Set the simple problem class which is to be optimized";
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
        return "Gaussian noise level on the fitness value.";
    }
    
    
	/**
	 * A (symmetric) absolute range limit.
	 * 
	 * @return value of the absolute range limit
	 */
	public double getDefaultRange() {
		return m_DefaultRange;
	}
	/**
	 * Set a (symmetric) absolute range limit.
	 * 
	 * @param defaultRange
	 */
	public void setDefaultRange(double defaultRange) {
		this.m_DefaultRange = defaultRange;
		initTemplate();
	}
	public String defaultRangeTipText() {
		return "Absolute limit for the symmetric range in any dimension";
	}
	
	/**
	 * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.   
	 * This is called by PropertySheetPanel in use with the GenericObjectEditor.
	 */
	public void hideHideable() {
		setSimpleProblem(getSimpleProblem());
	}
	
	public void setIndividualTemplate(AbstractEAIndividual indy) {
		resetTemplate = false;
		m_Template = indy;
	}
    
    @Override
	public String individualTemplateTipText() {
		return "Set the individual properties for the optimization";
	}
	
/////////// for GUI
	
    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("A wrapped simple problem based on ");
        sb.append(simProb.getClass().getName());
        sb.append(", Dimension   : "); 
        sb.append(simProb.getProblemDimension());
        return sb.toString();
    }
    
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
    	return "SimpleProblemWrapper";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
    	return "Wrapping simple problem implementations.";
    }
    
    public String[] getGOEPropertyUpdateLinks() {
    	return new String[] {"globalInfo", "simpleProblem"};
    }
}
