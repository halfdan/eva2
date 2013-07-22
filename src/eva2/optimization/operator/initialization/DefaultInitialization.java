package eva2.optimization.operator.initialization;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.problems.InterfaceOptimizationProblem;

/**
 * A dummy initialization method which only calls the default init method of the individual.
 *
 * @author mkron
 */
public class DefaultInitialization implements InterfaceInitialization, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public DefaultInitialization() {
    }

    @Override
    public void initialize(AbstractEAIndividual indy, InterfaceOptimizationProblem problem) {
        indy.defaultInit(problem);
    }

    @Override
    public InterfaceInitialization clone() {
        return new DefaultInitialization();
    }

    public String getName() {
        return "DefaultInitialization";
    }

    public String globalInfo() {
        return "Uses the standard initialization of the individual implementation";
    }
}
