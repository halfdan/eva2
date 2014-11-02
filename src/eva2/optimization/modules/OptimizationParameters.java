package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;


/**
 * OptimizationParameters for configuration of an
 * optimization run.
 * </p><p>
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
        return getInstance("OptimizationParameters.yml", true);
    }

    /**
     * Create an instance from a given serialized parameter file.
     *
     * @param yamlFile Serialized Parameter File
     * @param casually     if true, standard parameters are used quietly if the params cannot be loaded
     * @return a OptimizationParameters instance
     */
    public static OptimizationParameters getInstance(String yamlFile, final boolean casually) {
        OptimizationParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(yamlFile);
            instance = (OptimizationParameters) new Yaml().load(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new OptimizationParameters();
        }
        return instance;
    }

    public OptimizationParameters() {
        super(new DifferentialEvolution(), new F1Problem(), new EvaluationTerminator(5000));
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
