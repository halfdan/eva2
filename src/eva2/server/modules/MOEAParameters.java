package eva2.server.modules;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.operators.archiving.InterfaceArchiving;
import eva2.server.go.operators.archiving.InterfaceInformationRetrieval;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.TF1Problem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.MultiObjectiveEA;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/** The class gives access to all MOEA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class MOEAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static MOEAParameters getInstance() {
        MOEAParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("MOEAParameters.ser");
            instance = (MOEAParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new MOEAParameters();
        }
        return instance;
    }

    public MOEAParameters() {
    	super(new MultiObjectiveEA(), new TF1Problem(), new EvaluationTerminator(1000));
    }

    private MOEAParameters(MOEAParameters Source) {
    	super(Source);
    }

    @Override
    public Object clone() {
        return new MOEAParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a multi-objective evoluationary algorithm, limit MOEA to multi-objective problems (due to the given framework only the fitness of objective one will be plotted).";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((MultiObjectiveEA)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((MultiObjectiveEA)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the Population used.";
    }

    /** This method allows you to set/get the optimizing technique to use.
     * @return The current optimizing method
     */
    @Override
    public InterfaceOptimizer getOptimizer() {
        return ((MultiObjectiveEA)this.m_Optimizer).getOptimizer();
    }
    @Override
    public void setOptimizer(InterfaceOptimizer b){
        ((MultiObjectiveEA)this.m_Optimizer).setOptimizer(b);
    }
    @Override
    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** This method allows you to set/get the archiving strategy to use.
     * @return The current optimizing method
     */
    public InterfaceArchiving getArchivingStrategy() {
        return ((MultiObjectiveEA)this.m_Optimizer).getArchivingStrategy();
    }
    public void setArchivingStrategy(InterfaceArchiving b){
        ((MultiObjectiveEA)this.m_Optimizer).setArchivingStrategy(b);
    }
    public String archivingStrategyTipText() {
        return "Choose the archiving strategy.";
    }

    /** This method allows you to set/get the Information Retrieval strategy to use.
     * @return The current optimizing method
     */
    public InterfaceInformationRetrieval getInformationRetrieval() {
        return ((MultiObjectiveEA)this.m_Optimizer).getInformationRetrieval();
    }
    public void setInformationRetrieval(InterfaceInformationRetrieval b){
        ((MultiObjectiveEA)this.m_Optimizer).setInformationRetrieval(b);
    }
    public String informationRetrievalTipText() {
        return "Choose the Information Retrieval strategy.";
    }

    /** This method allows you to set/get the size of the archive.
     * @return The current optimizing method
     */
    public int getArchiveSize() {
        Population archive = ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA)this.m_Optimizer).getPopulation().SetArchive(archive);
        }
        return ((MultiObjectiveEA)this.m_Optimizer).getArchiveSize();
    }
    public void setArchiveSize(int b){
        Population archive = ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA)this.m_Optimizer).getPopulation().SetArchive(archive);
        }
        ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive().setTargetSize(b);
    }
    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}