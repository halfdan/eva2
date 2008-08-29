package eva2.server.go.operators.mutation;

import eva2.server.go.populations.Population;

/**
 * An interface for a mutation operator which is updated on a generational basis, such as
 * the 1/5-success rule.
 * 
 * @author mkron
 *
 */
public interface InterfaceMutationGenerational extends InterfaceMutation {
	public void adaptAfterSelection(Population oldGen, Population selected);
    public void adaptGenerational(Population selectedPop, Population parentPop, Population newPop, boolean updateSelected);
}
