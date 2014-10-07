package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.problems.B1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.PopulationBasedIncrementalLearning;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;


/**
 * The class gives access to all PBIL parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:53:29
 * To change this template use File | Settings | File Templates.
 */
public class PBILParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static PBILParameters getInstance() {
        PBILParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("PBILParameters.ser");
            instance = (PBILParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new PBILParameters();
        }
        return instance;
    }

    public PBILParameters() {
        super(new PopulationBasedIncrementalLearning(), new B1Problem(), new EvaluationTerminator(1000));
    }

    private PBILParameters(PBILParameters Source) {
        super(Source);
    }

    @Override
    public Object clone() {
        return new PBILParameters(this);
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
//    public static String globalInfo() {
//    	return ((PopulationBasedIncrementalLearning)this.optimizer).globalInfo();
//    }
    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // i'm a Monte Carlo Search Algorithm
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
        return ((PopulationBasedIncrementalLearning) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((PopulationBasedIncrementalLearning) this.optimizer).setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /**
     * This method will set the selection method that is to be used
     *
     * @param selection
     */
    public void setSelectionMethod(InterfaceSelection selection) {
        ((PopulationBasedIncrementalLearning) this.optimizer).setSelectionMethod(selection);
    }

    public InterfaceSelection getSelectionMethod() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getSelectionMethod();
    }

    public String selectionMethodTipText() {
        return "Choose a selection method.";
    }

    /**
     * Enable/disable elitism.
     *
     * @param elitism
     */
    public void setElitism(boolean elitism) {
        ((PopulationBasedIncrementalLearning) this.optimizer).setElitism(elitism);
    }

    public boolean getElitism() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getElitism();
    }

    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /**
     * This method will set the learning rate for PBIL
     *
     * @param LearningRate
     */
    public void setLearningRate(double LearningRate) {
        if (LearningRate < 0) {
            LearningRate = 0;
        }
        ((PopulationBasedIncrementalLearning) this.optimizer).setLearningRate(LearningRate);
    }

    public double getLearningRate() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getLearningRate();
    }

    public String learningRateTipText() {
        return "The learing rate of PBIL.";
    }

    /**
     * This method will set the mutation rate for PBIL
     *
     * @param m
     */
    public void setMutationRate(double m) {
        if (m < 0) {
            m = 0;
        }
        if (m > 1) {
            m = 1;
        }
        ((PopulationBasedIncrementalLearning) this.optimizer).setMutationRate(m);

    }

    public double getMutationRate() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getMutationRate();
    }

    public String mutationRateTipText() {
        return "The mutation rate of PBIL.";
    }

    /**
     * This method will set the mutation sigma for PBIL
     *
     * @param m
     */
    public void setMutateSigma(double m) {
        if (m < 0) {
            m = 0;
        }
        ((PopulationBasedIncrementalLearning) this.optimizer).setMutateSigma(m);
    }

    public double getMutateSigma() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getMutateSigma();
    }

    public String mutateSigmaTipText() {
        return "Set the sigma for the mutation of the probability vector.";
    }

    /**
     * This method will set the number of positive samples for PBIL
     *
     * @param PositiveSamples
     */
    public void setPositiveSamples(int PositiveSamples) {
        if (PositiveSamples < 1) {
            PositiveSamples = 1;
        }
        ((PopulationBasedIncrementalLearning) this.optimizer).setPositiveSamples(PositiveSamples);
    }

    public int getPositiveSamples() {
        return ((PopulationBasedIncrementalLearning) this.optimizer).getPositiveSamples();
    }

    public String positiveSamplesTipText() {
        return "The number of positive samples that update the PBIL vector.";
    }
}