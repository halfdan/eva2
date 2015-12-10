package eva2.optimization.population;

/**
 * This is just a wrapper type to distinguish between a Population (current solution set
 * of an optimizer) and a final solution set, in which archived and deactivated
 * individuals may be contained as well. Both may be equal if the optimizer doesn't
 * make this distinction.
 *
 * @author mkron
 */
public interface InterfaceSolutionSet {
    Population getSolutions();

    Population getCurrentPopulation();

    SolutionSet clone();
}
