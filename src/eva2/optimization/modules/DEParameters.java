package eva2.optimization.modules;


import eva2.optimization.go.InterfaceGOParameters;
import eva2.optimization.enums.DETypeEnum;
import eva2.optimization.operators.terminators.EvaluationTerminator;
import eva2.optimization.populations.Population;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/** The class gives access to all DE parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class DEParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {


    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static DEParameters getInstance() {
        DEParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("DEParameters.ser");
            instance = (DEParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new DEParameters();
        }
        return instance;
    }

    /**
     *
     */
    public DEParameters() {
    	super(new DifferentialEvolution(), new F1Problem(), new EvaluationTerminator(1000));
    }

    private DEParameters(DEParameters Source) {
    	super(Source);
    }

    @Override
    public Object clone() {
        return new DEParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a Differential Evolution optimization method, limit DE to real-valued genotypes.";
    }

    /** This method allows you to set the current optimizing algorithm
     * @param optimizer The new optimizing algorithm
     */
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
        return ((DifferentialEvolution) this.m_Optimizer).getPopulation();
    }
    
    public void setPopulation(Population pop){
        ((DifferentialEvolution) this.m_Optimizer).setPopulation(pop);
    }
    
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** This method will set the amplication factor f.
     * @param f
     */
    public void setF (double f) {
        ((DifferentialEvolution) this.m_Optimizer).setF(f);
    }
    public double getF() {
        return ((DifferentialEvolution) this.m_Optimizer).getF();
    }
    public String fTipText() {
        return "F is a real and constant factor which controlls the ampllification of the differential variation.";
    }

    /** This method will set the crossover probability
     * @param k
     */
    public void setK(double k) {
        ((DifferentialEvolution) this.m_Optimizer).setK(k);
    }
    public double getK() {
        return ((DifferentialEvolution) this.m_Optimizer).getK();
    }
    public String kTipText() {
        return "Probability of alteration through DE1.";
    }

    /** This method will set greediness to move towards the best
     * @param l
     */
    public void setLambda (double l) {
        ((DifferentialEvolution) this.m_Optimizer).setLambda(l);
    }
    public double getLambda() {
        return ((DifferentialEvolution) this.m_Optimizer).getLambda();
    }
    public String lambdaTipText() {
        return "Enhance greediness through amplification of the differential vector to the best individual for DE2.";
    }

    /** This method allows you to choose the type of Differential Evolution.
     * @param s  The type.
     */
    public void setDEType(DETypeEnum s) {
        ((DifferentialEvolution) this.m_Optimizer).setDEType(s);
    }
    public DETypeEnum getDEType() {
        return ((DifferentialEvolution) this.m_Optimizer).getDEType();
    }
    public String dETypeTipText() {
        return "Choose the type of Differential Evolution.";
    }
}