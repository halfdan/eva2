package eva2.server.go.operators.initialization;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/**
 * A dummy initialization method which only calls the default init method of the individual.
 * 
 * @author mkron
 *
 */
public class DefaultInitialization implements InterfaceInitialization, java.io.Serializable { 
	private static final long serialVersionUID = 1L;

	public DefaultInitialization() {}
	
	public void initialize(AbstractEAIndividual indy, InterfaceOptimizationProblem problem) {
		indy.defaultInit(problem);
	}
	
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
