package eva2.server.go.problems;

import java.lang.reflect.Method;
import java.util.BitSet;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.tools.RandomNumberGenerator;

import simpleprobs.InterfaceSimpleProblem;
import simpleprobs.SimpleF1;
import simpleprobs.SimpleProblemBinary;
import simpleprobs.SimpleProblemDouble;

public class SimpleProblemWrapper extends AbstractOptimizationProblem {
	InterfaceSimpleProblem simProb = new SimpleF1();
	protected double m_DefaultRange = 10;
	protected double m_Noise = 0;
	
	public SimpleProblemWrapper() {
		initTemplate();
	}
	
	public SimpleProblemWrapper(SimpleProblemWrapper other) {
		other.m_DefaultRange = m_DefaultRange;
		other.m_Noise = m_Noise;
		// warning! this does no deep copy!
		other.simProb = simProb;
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
	        if (m_Noise != 0) RandomNumberGenerator.addNoise(fitness, m_Noise); 
	        // set the fitness 
	        individual.SetFitness(fitness);
		} else if (simProb instanceof SimpleProblemBinary) {
	        BitSet          tmpBitSet;
	        double[]        result;
	        
	        tmpBitSet   = ((InterfaceDataTypeBinary) individual).getBinaryData();
	        // evaluate the fitness
	        result = ((SimpleProblemBinary)simProb).eval(tmpBitSet);
	        // set the fitness
	        individual.SetFitness(result);
		} else {
			System.err.println("Error in SimpleProblemWrapper: " + simProb.getClass().getName() + " is unknown type!");
		}
	}

	@Override
	public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;
        population.clear();

        initTemplate();

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
		
	}

	@Override
	public void initProblem() {
		initTemplate();
	}

	protected void initTemplate() {
		if (simProb instanceof SimpleProblemDouble) {
			this.m_Template         = new ESIndividualDoubleData();
			((ESIndividualDoubleData)this.m_Template).setDoubleDataLength(simProb.getProblemDimension());
			((ESIndividualDoubleData)this.m_Template).SetDoubleRange(makeRange());
		} else if (simProb instanceof SimpleProblemBinary) {
			this.m_Template         = new GAIndividualBinaryData();
			((InterfaceDataTypeBinary)this.m_Template).setBinaryDataLength(simProb.getProblemDimension());
		}
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
	public InterfaceSimpleProblem getSimpleProblem() {
		return simProb;
	}

	/**
	 * @param simProb the simProb to set
	 */
	public void setSimpleProblem(InterfaceSimpleProblem simProb) {
		this.simProb = simProb;
		initTemplate();
		GenericObjectEditor.setShowProperty(getClass(), "noise", (simProb instanceof SimpleProblemDouble));
		GenericObjectEditor.setShowProperty(getClass(), "defaultRange", (simProb instanceof SimpleProblemDouble));
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
    
/////////// for GUI
	
    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
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
    public String getName() {
    	return "SimpleProblemWrapper";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
    	Object maybeAdditionalString = BeanInspector.callIfAvailable(simProb, "globalInfo", null);
    	if (maybeAdditionalString != null) {
    		return "Wrapping a simple problem: " + (String)maybeAdditionalString;
    	} else return "Wrapping a simple problem.";
    }
}
