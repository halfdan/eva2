package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.archiving.InterfaceArchiving;
import javaeva.server.go.operators.archiving.InterfaceInformationRetrieval;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.problems.TF1Problem;
import javaeva.server.go.strategies.DifferentialEvolution;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.MultiObjectiveEA;
import javaeva.tools.Serializer;
import javaeva.tools.SelectedTag;

import java.io.Serializable;

/** The class gives access to all MOEA parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class MOEAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    public static MOEAParameters getInstance() {
        if (TRACE) System.out.println("MOEAParameters getInstance 1");
        MOEAParameters Instance = (MOEAParameters) Serializer.loadObject("MOEAParameters.ser");
        if (TRACE) System.out.println("MOEAParameters getInstance 2");
        if (Instance == null) Instance = new MOEAParameters();
        return Instance;
    }

    public void saveInstance() {
        Serializer.storeObject("MOEAParameters.ser",this);
    }

    public MOEAParameters() {
    	super(new MultiObjectiveEA(), new TF1Problem(), new EvaluationTerminator(1000));
    }

    private MOEAParameters(MOEAParameters Source) {
    	super(Source);
    }

    public Object clone() {
        return new MOEAParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a multi-objective evoluationary algorithm, please limit MOEA to multi-objective problems (due to the given framework only the fitness of objective one will be plotted).";
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
    public InterfaceOptimizer getOptimizer() {
        return ((MultiObjectiveEA)this.m_Optimizer).getOptimizer();
    }
    public void setOptimizer(InterfaceOptimizer b){
        ((MultiObjectiveEA)this.m_Optimizer).setOptimizer(b);
    }
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
        ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive().setPopulationSize(b);
    }
    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}