package eva2.optimization.modules;


import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.enums.PSOTopologyEnum;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.ParticleSwarmOptimization;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * The class gives access to all PSO parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.11.2004
 * Time: 17:58:37
 * To change this template use File | Settings | File Templates.
 */
public class PSOParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

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

    /**
     * This method returns a global info string
     *
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

    /**
     * Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((ParticleSwarmOptimization) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((ParticleSwarmOptimization) this.optimizer).setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /**
     * This method will set the initial velocity
     *
     * @param f
     */
    public void setInitialVelocity(double f) {
        ((ParticleSwarmOptimization) this.optimizer).setInitialVelocity(f);
    }

    public double getInitialVelocity() {
        return ((ParticleSwarmOptimization) this.optimizer).getInitialVelocity();
    }

    public String initialVelocityTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).initialVelocityTipText();
    }

    /**
     * This method will set the speed limit
     *
     * @param k
     */
    public void setSpeedLimit(double k) {
        ((ParticleSwarmOptimization) this.optimizer).setSpeedLimit(k);
    }

    public double getSpeedLimit() {
        return ((ParticleSwarmOptimization) this.optimizer).getSpeedLimit();
    }

    public String speedLimitTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).speedLimitTipText();
    }

    /**
     * This method will set the inertness
     *
     * @param k
     */
    public void setInertnessOrChi(double k) {
//        if (k < 0) k = 0;
//        if (k > 1) k = 1;
        ((ParticleSwarmOptimization) this.optimizer).setInertnessOrChi(k);
    }

    public double getInertnessOrChi() {
        return ((ParticleSwarmOptimization) this.optimizer).getInertnessOrChi();
    }

    public String inertnessOrChiTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).inertnessOrChiTipText();
    }

    /**
     * This method will set greediness to move towards the best solution
     * based on the cognition model
     *
     * @param l
     */
    public void setPhi1(double l) {
        ((ParticleSwarmOptimization) this.optimizer).setPhi1(l);
    }

    public double getPhi1() {
        return ((ParticleSwarmOptimization) this.optimizer).getPhi1();
    }

    public String phi1TipText() {
        return ((ParticleSwarmOptimization) this.optimizer).phi1TipText();
    }

    /**
     * This method will set greediness to move towards the best solution
     * based on the social model
     *
     * @param l
     */
    public void setPhi2(double l) {
        ((ParticleSwarmOptimization) this.optimizer).setPhi2(l);
    }

    public double getPhi2() {
        return ((ParticleSwarmOptimization) this.optimizer).getPhi2();
    }

    public String phi2TipText() {
        return ((ParticleSwarmOptimization) this.optimizer).phi2TipText();
    }


    /**
     * Toggle Check Constraints.
     *
     * @param s Check Constraints.
     */
    public void setCheckRange(boolean s) {
        ((ParticleSwarmOptimization) this.optimizer).setCheckRange(s);
    }

    public boolean isCheckRange() {
        return ((ParticleSwarmOptimization) this.optimizer).isCheckRange();
    }

    public String checkConstraintsTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).checkRangeTipText();
    }

    /**
     * This method allows you to choose the topology type.
     *
     * @param t The type.
     */
    public void setTopology(PSOTopologyEnum t) {
        ((ParticleSwarmOptimization) this.optimizer).setTopology(t);
        ((ParticleSwarmOptimization) this.optimizer).setGOEShowProperties(getClass());
    }

    public PSOTopologyEnum getTopology() {
        return ((ParticleSwarmOptimization) this.optimizer).getTopology();
    }

    public String topologyTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).topologyTipText();
    }

    /**
     * The range of the local neighbourhood.
     *
     * @param s The range.
     */
    public void setTopologyRange(int s) {
        ((ParticleSwarmOptimization) this.optimizer).setTopologyRange(s);
    }

    public int getTopologyRange() {
        return ((ParticleSwarmOptimization) this.optimizer).getTopologyRange();
    }

    public String topologyRangeTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).topologyRangeTipText();
    }

    public double getSubSwarmRadius() {
        return ((ParticleSwarmOptimization) this.optimizer).getSubSwarmRadius();
    }

    public void setSubSwarmRadius(double radius) {
        ((ParticleSwarmOptimization) this.optimizer).setSubSwarmRadius(radius);
    }

    public String subSwarmRadiusTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).subSwarmRadiusTipText();
    }

    public int getMaxSubSwarmSize() {
        return ((ParticleSwarmOptimization) this.optimizer).getMaxSubSwarmSize();
    }

    public void setMaxSubSwarmSize(int subSize) {
        ((ParticleSwarmOptimization) this.optimizer).setMaxSubSwarmSize(subSize);
    }

    public String maxSubSwarmSizeTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).maxSubSwarmSizeTipText();
    }

    /**
     * @return the checkSpeedLimit
     */
    public boolean isCheckSpeedLimit() {
        return ((ParticleSwarmOptimization) this.optimizer).isCheckSpeedLimit();
    }

    /**
     * @param checkSpeedLimit the checkSpeedLimit to set
     */
    public void setCheckSpeedLimit(boolean checkSpeedLimit) {
        ((ParticleSwarmOptimization) this.optimizer).setCheckSpeedLimit(checkSpeedLimit);
        GenericObjectEditor.setHideProperty(getClass(), "speedLimit", !checkSpeedLimit);
    }

    public String checkSpeedLimitTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).checkSpeedLimitTipText();
    }

    /**
     * This method allows you to choose the algorithm type.
     *
     * @param s The type.
     */
    public void setAlgoType(SelectedTag s) {
        ((ParticleSwarmOptimization) this.optimizer).setAlgoType(s);
    }

    public SelectedTag getAlgoType() {
        return ((ParticleSwarmOptimization) this.optimizer).getAlgoType();
    }

    public String algoTypeTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).algoTypeTipText();
    }

//	/**
//	 * @return the treeBranchDeg
//	 */
//	public int getTreeBranchDegree() {
//		return ((ParticleSwarmOptimization)this.optimizer).getTreeBranchDegree();
//	}
//
//	/**
//	 * @param treeBranchDeg the treeBranchDeg to set
//	 */
//	public void setTreeBranchDegree(int treeBranchDeg) {
//		((ParticleSwarmOptimization)this.optimizer).setTreeBranchDegree(treeBranchDeg);
//	}
//
//	public String treeBranchDegreeTipText() {
//		return ((ParticleSwarmOptimization)this.optimizer).treeBranchDegreeTipText();
//	}

    /**
     * @return the wrapTopology
     */
    public boolean isWrapTopology() {
        return ((ParticleSwarmOptimization) this.optimizer).isWrapTopology();
    }

    /**
     * @param wrapTopology the wrapTopology to set
     */
    public void setWrapTopology(boolean wrapTopology) {
        ((ParticleSwarmOptimization) this.optimizer).setWrapTopology(wrapTopology);
    }

    public String wrapTopologyTipText() {
        return ((ParticleSwarmOptimization) this.optimizer).wrapTopologyTipText();
    }

}