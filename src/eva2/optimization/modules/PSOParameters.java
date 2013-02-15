package eva2.optimization.modules;


import eva2.gui.GenericObjectEditor;
import eva2.optimization.go.InterfaceGOParameters;
import eva2.optimization.enums.PSOTopologyEnum;
import eva2.optimization.operators.terminators.EvaluationTerminator;
import eva2.optimization.populations.Population;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.ParticleSwarmOptimization;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/** The class gives access to all PSO parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.11.2004
 * Time: 17:58:37
 * To change this template use File | Settings | File Templates.
 */
public class PSOParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static PSOParameters getInstance() {
        PSOParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("PSOParameters.ser");
            instance = (PSOParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new PSOParameters();
        }
        return instance;
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

    @Override
    public Object clone() {
        return new PSOParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return ParticleSwarmOptimization.globalInfo();
    }

	/**
	 * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.   
	 * This is called by PropertySheetPanel in use with the GenericObjectEditor.
	 */
	public void hideHideable() {
		setCheckSpeedLimit(isCheckSpeedLimit());
		setTopology(getTopology());
	}

    @Override
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
	public void setCheckRange(boolean s) {
		((ParticleSwarmOptimization)this.m_Optimizer).setCheckRange(s);
	}
	public boolean isCheckRange() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).isCheckRange();
	}
	public String checkConstraintsTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).checkRangeTipText();
	}
	
    /** This method allows you to choose the topology type.
     * @param t  The type.
     */
    public void setTopology(PSOTopologyEnum t) {
        ((ParticleSwarmOptimization)this.m_Optimizer).setTopology(t);
        ((ParticleSwarmOptimization)this.m_Optimizer).setGOEShowProperties(getClass());
    }
    public PSOTopologyEnum getTopology() {
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
	
	public int getMaxSubSwarmSize() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).getMaxSubSwarmSize();
	}
	public void setMaxSubSwarmSize(int subSize) {
		((ParticleSwarmOptimization)this.m_Optimizer).setMaxSubSwarmSize(subSize);
	}
	public String maxSubSwarmSizeTipText() {
		return ((ParticleSwarmOptimization)this.m_Optimizer).maxSubSwarmSizeTipText();
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
    
//	/**
//	 * @return the treeBranchDeg
//	 */
//	public int getTreeBranchDegree() {
//		return ((ParticleSwarmOptimization)this.m_Optimizer).getTreeBranchDegree();
//	}
//
//	/**
//	 * @param treeBranchDeg the treeBranchDeg to set
//	 */
//	public void setTreeBranchDegree(int treeBranchDeg) {
//		((ParticleSwarmOptimization)this.m_Optimizer).setTreeBranchDegree(treeBranchDeg);
//	}
//
//	public String treeBranchDegreeTipText() {
//		return ((ParticleSwarmOptimization)this.m_Optimizer).treeBranchDegreeTipText();
//	}
	
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