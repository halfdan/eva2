package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import eva2.util.annotation.Description;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;


/**
 * OptimizationParameters for configuration of an
 * optimization run.
 * <p/>
 * This class is used to generate the default GUI
 * configuration panel for optimizations.
 */
@Description("Select the optimization parameters.")
public class OptimizationParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    /**
     * Should be removed and replaced by a more solid
     * serialization. (EvAScript?)
     *
     * @deprecated
     * @return
     */
    public static OptimizationParameters getInstance() {
        return getInstance("OptimizationParameters.set", true);
    }

    /**
     * Create an instance from a given serialized parameter file.
     *
     * @param serParamFile Serialized Parameter File
     * @param casually     if true, standard parameters are used quietly if the params cannot be loaded
     * @return a OptimizationParameters instance
     */
    public static OptimizationParameters getInstance(String serParamFile, final boolean casually) {
        OptimizationParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(serParamFile);
            instance = (OptimizationParameters) Serializer.loadObject(fileStream, casually);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new OptimizationParameters();
        }
        return instance;
    }

    public OptimizationParameters() {
        super(new GeneticAlgorithm(), new F1Problem(), new EvaluationTerminator(1000));
    }

    public OptimizationParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
        super(opt, prob, term);
    }

    /**
     *
     */
    private OptimizationParameters(OptimizationParameters parameters) {
        super(parameters);
    }

    /**
     *
     */
    @Override
    public String getName() {
        return "Optimization parameters";
    }

    /**
     *
     */
    @Override
    public Object clone() {
        return new OptimizationParameters(this);
    }
}
