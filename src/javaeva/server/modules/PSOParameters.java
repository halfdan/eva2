package javaeva.server.modules;

import javaeva.gui.GenericObjectEditor;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.TerminatorInterface;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.ParticleSwarmOptimization;
import javaeva.server.go.strategies.PopulationBasedIncrementalLearning;
import javaeva.tools.Serializer;
import javaeva.tools.SelectedTag;

import java.io.Serializable;

/** The class gives access to all PSO parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.11.2004
 * Time: 17:58:37
 * To change this template use File | Settings | File Templates.
 */
public class PSOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new ParticleSwarmOptimization();
    private InterfaceOptimizationProblem    m_Problem           = new F1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private TerminatorInterface             m_Terminator        = new EvaluationTerminator();
    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;
    
	/**
     *
     */
    public static PSOParameters getInstance() {
        if (TRACE) System.out.println("PSOParameters getInstance 1");
        PSOParameters Instance = (PSOParameters) Serializer.loadObject("PSOParameters.ser");
        if (TRACE) System.out.println("PSOParameters getInstance 2");
        if (Instance == null) Instance = new PSOParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("PSOParameters.ser",this);
    }
    /**
     *
     */
    public PSOParameters() {
        if (TRACE) System.out.println("PSOParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new ParticleSwarmOptimization();
        this.m_Problem          = new F1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("PSOParameters Constructor end");
    }

    /**
     *
     */
    private PSOParameters(PSOParameters Source) {
        this.m_Name             = Source.m_Name;
        this.m_Optimizer        = Source.m_Optimizer;
        this.m_Problem          = Source.m_Problem;
        this.m_Terminator       = Source.m_Terminator;
        //this.m_FunctionCalls    = Source.m_FunctionCalls;
        this.m_Optimizer.SetProblem(this.m_Problem);
        this.m_Seed             = Source.m_Seed;
    }
    /**
     *
     */
    public String getName() {
        return m_Name;
    }
    /**
     *
     */
    public Object clone() {
        return new PSOParameters(this);
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
        if (this.m_Optimizer != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
    }

    /**
     *
     */
    public String toString() {
        String ret = "\r\nGO-Parameter:"+this.m_Problem.getStringRepresentationForProblem(this.m_Optimizer)+"\n"+this.m_Optimizer.getStringRepresentation();
        return ret;
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return ((ParticleSwarmOptimization)m_Optimizer).globalInfo();
    }

	/**
	 * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.   
	 * This is called by PropertySheetPanel in use with the GenericObjectEditor.
	 */
	public void hideHideable() {
		setCheckSpeedLimit(isCheckSpeedLimit());
		setTopology(getTopology());
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
        return "Random number seed, set to zero to use current time";
    }

    /** This method allows you to set the current optimizing algorithm
     * @param optimizer The new optimizing algorithm
     */
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // i'm a Monte Carlo Search Algorithm
        // *pff* i'll ignore that!
    }
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }

    /** This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     * @param term  The new terminator
     */
    public void setTerminator(TerminatorInterface term) {
        this.m_Terminator = term;
    }
    public TerminatorInterface getTerminator() {
        return this.m_Terminator;
    }
    public String terminatorTipText() {
        return "Choose a termination criterion.";
    }

    /** This method will set the problem that is to be optimized
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

    /** This method will set the output filename
     * @param name
     */
    public void setOutputFileName (String name) {
        this.m_OutputFileName = name;
    }
    public String getOutputFileName () {
        return this.m_OutputFileName;
    }
    public String outputFileNameTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((ParticleSwarmOptimization)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** This method will set the initial velocity
     * @param f
     */
    public void setInitialVelocity (double f) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setInitialVelocity(f);
    }
    public double getInitialVelocity() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getInitialVelocity();
    }
    public String initialVelocityTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).initialVelocityTipText();
    }

    /** This method will set the speed limit
     * @param k
     */
    public void setSpeedLimit (double k) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setSpeedLimit(k);
    }
    public double getSpeedLimit() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getSpeedLimit();
    }
    public String speedLimitTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).speedLimitTipText();
    }

    /** This method will set the inertness
     * @param k
     */
    public void setInertnessOrChi(double k) {
//        if (k < 0) k = 0;
//        if (k > 1) k = 1;
        ((ParticleSwarmOptimization)this.m_Optimizer).setInertnessOrChi(k);
    }
    
    public double getInertnessOrChi() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getInertnessOrChi();
    }
    
    public String inertnessOrChiTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).inertnessOrChiTipText();
    }

    /** This method will set greediness to move towards the best solution
     * based on the cognition model
     * @param l
     */
    public void setPhi1 (double l) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setPhi1(l);
    }
    public double getPhi1() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getPhi1();
    }
    public String phi1TipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).phi1TipText();
    }

    /** This method will set greediness to move towards the best solution
     * based on the social model
     * @param l
     */
    public void setPhi2 (double l) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setPhi2(l);
    }
    public double getPhi2() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getPhi2();
    }
    public String phi2TipText() {
    	return ((ParticleSwarmOptimization)this.m_Optimizer).phi2TipText();
    }

    
	/** Toggle Check Constraints.
	 * @param s    Check Constraints.
	 */
	public void setCheckConstraints(boolean s) {
		((ParticleSwarmOptimization)this.m_Optimizer).setCheckConstraints(s);
	}
	public boolean isCheckConstraints() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).isCheckConstraints();
	}
	public String checkConstraintsTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).checkConstraintsTipText();
	}
	
    /** This method allows you to choose the topology type.
     * @param s  The type.
     */
    public void setTopology(SelectedTag s) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setTopology(s);
        ((ParticleSwarmOptimization)this.m_Optimizer).setGOEShowProperties(getClass());
//		GenericObjectEditor.setHideProperty(getClass(), "topologyRange", (s.getSelectedTag().getID() >= 2));
//		GenericObjectEditor.setHideProperty(getClass(), "subSwarmRadius", (s.getSelectedTag().getID() != 3));
//		GenericObjectEditor.setHideProperty(getClass(), "subSwarmSize", (s.getSelectedTag().getID() != 3));
    }
    public SelectedTag getTopology() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getTopology();
    }
    public String topologyTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).topologyTipText();
    }

    /** The range of the local neighbourhood.
     * @param s  The range.
     */
    public void setTopologyRange(int s) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setTopologyRange(s);
    }
    public int getTopologyRange() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getTopologyRange();
    }
    public String topologyRangeTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).topologyRangeTipText();
    }
    
	public double getSubSwarmRadius() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).getSubSwarmRadius();
	}
	public void setSubSwarmRadius(double radius) {
		((ParticleSwarmOptimization)this.m_Optimizer).setSubSwarmRadius(radius);
	}
	public String subSwarmRadiusTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).subSwarmRadiusTipText();
	}
	
	public int getSubSwarmSize() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).getSubSwarmSize();
	}
	public void setSubSwarmSize(int subSize) {
		((ParticleSwarmOptimization)this.m_Optimizer).setSubSwarmSize(subSize);
	}
	public String subSwarmSizeTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).subSwarmSizeTipText();
	}
    
	/**
	 * @return the checkSpeedLimit
	 **/
	public boolean isCheckSpeedLimit() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).isCheckSpeedLimit();
	}
	/**
	 * @param checkSpeedLimit the checkSpeedLimit to set
	 **/
	public void setCheckSpeedLimit(boolean checkSpeedLimit) {
		((ParticleSwarmOptimization)this.m_Optimizer).setCheckSpeedLimit(checkSpeedLimit);
		GenericObjectEditor.setHideProperty(getClass(), "speedLimit", !checkSpeedLimit);
	}
	
	public String checkSpeedLimitTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).checkSpeedLimitTipText();
	}
	
    /** This method allows you to choose the algorithm type.
     * @param s  The type.
     */
    public void setAlgoType(SelectedTag s) {
    	((ParticleSwarmOptimization)this.m_Optimizer).setAlgoType(s);
    }
    
    public SelectedTag getAlgoType() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).getAlgoType();
    }
    
    public String algoTypeTipText() {
        return ((ParticleSwarmOptimization)this.m_Optimizer).algoTypeTipText();
    }
    
	/**
	 * @return the treeBranchDeg
	 */
	public int getTreeBranchDegree() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).getTreeBranchDegree();
	}

	/**
	 * @param treeBranchDeg the treeBranchDeg to set
	 */
	public void setTreeBranchDegree(int treeBranchDeg) {
		((ParticleSwarmOptimization)this.m_Optimizer).setTreeBranchDegree(treeBranchDeg);
	}

	public String treeBranchDegreeTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).treeBranchDegreeTipText();
	}
	
	/**
	 * @return the wrapTopology
	 */
	public boolean isWrapTopology() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).isWrapTopology();
	}

	/**
	 * @param wrapTopology the wrapTopology to set
	 */
	public void setWrapTopology(boolean wrapTopology) {
		((ParticleSwarmOptimization)this.m_Optimizer).setWrapTopology(wrapTopology);
	}
	
	public String wrapTopologyTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).wrapTopologyTipText();
	}

}