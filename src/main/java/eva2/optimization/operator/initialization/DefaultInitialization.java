package eva2.optimization.operator.initialization;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * A dummy initialization method which only calls the default initialize method of the individual.
 */
@Description("Uses the standard initialization of the individual implementation")
public class DefaultInitialization implements InterfaceInitialization, java.io.Serializable {

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
}
