package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.archiving.InterfaceArchiving;
import eva2.optimization.operator.archiving.InterfaceInformationRetrieval;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.TF1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.MultiObjectiveEA;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * The class gives access to all MOEA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class MOEAParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

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

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a multi-objective evoluationary algorithm, limit MOEA to multi-objective problems (due to the given framework only the fitness of objective one will be plotted).";
    }

    /**
     * Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((MultiObjectiveEA) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((MultiObjectiveEA) this.optimizer).setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the Population used.";
    }

    /**
     * This method allows you to set/get the optimizing technique to use.
     *
     * @return The current optimizing method
     */
    @Override
    public InterfaceOptimizer getOptimizer() {
        return ((MultiObjectiveEA) this.optimizer).getOptimizer();
    }

    @Override
    public void setOptimizer(InterfaceOptimizer b) {
        ((MultiObjectiveEA) this.optimizer).setOptimizer(b);
    }

    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /**
     * This method allows you to set/get the archiving strategy to use.
     *
     * @return The current optimizing method
     */
    public InterfaceArchiving getArchivingStrategy() {
        return ((MultiObjectiveEA) this.optimizer).getArchivingStrategy();
    }

    public void setArchivingStrategy(InterfaceArchiving b) {
        ((MultiObjectiveEA) this.optimizer).setArchivingStrategy(b);
    }

    public String archivingStrategyTipText() {
        return "Choose the archiving strategy.";
    }

    /**
     * This method allows you to set/get the Information Retrieval strategy to use.
     *
     * @return The current optimizing method
     */
    public InterfaceInformationRetrieval getInformationRetrieval() {
        return ((MultiObjectiveEA) this.optimizer).getInformationRetrieval();
    }

    public void setInformationRetrieval(InterfaceInformationRetrieval b) {
        ((MultiObjectiveEA) this.optimizer).setInformationRetrieval(b);
    }

    public String informationRetrievalTipText() {
        return "Choose the Information Retrieval strategy.";
    }

    /**
     * This method allows you to set/get the size of the archive.
     *
     * @return The current optimizing method
     */
    public int getArchiveSize() {
        Population archive = ((MultiObjectiveEA) this.optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA) this.optimizer).getPopulation().SetArchive(archive);
        }
        return ((MultiObjectiveEA) this.optimizer).getArchiveSize();
    }

    public void setArchiveSize(int b) {
        Population archive = ((MultiObjectiveEA) this.optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA) this.optimizer).getPopulation().SetArchive(archive);
        }
        ((MultiObjectiveEA) this.optimizer).getPopulation().getArchive().setTargetSize(b);
    }

    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}