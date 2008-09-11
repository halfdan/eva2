package eva2.server.modules;


import java.io.Serializable;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.ParticleSwarmOptimization;
import eva2.server.go.strategies.PopulationBasedIncrementalLearning;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;

/** The class gives access to all PSO parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.11.2004
 * Time: 17:58:37
 * To change this template use File | Settings | File Templates.
 */
public class PSOParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    
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
    	super(new ParticleSwarmOptimization(), new F1Problem(), new EvaluationTerminator());
    }

    private PSOParameters(PSOParameters Source) {
    	super(Source);
    }

    public Object clone() {
        return new PSOParameters(this);
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

    public void setOptimizer(InterfaceOptimizer optimizer) {
        // *pff* i'll ignore that!
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