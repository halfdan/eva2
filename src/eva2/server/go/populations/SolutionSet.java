package eva2.server.go.populations;

public class SolutionSet implements InterfaceSolutionSet {
	Population pop = null;
	Population sols = null;
	
	public SolutionSet(Population currentPop, Population allSols) {
		pop = currentPop;
		sols = allSols;
	}
	
	/**
	 * Create a solution set from a single population. This can be used by optimizers which 
	 * make no distinction between current solution set and archived solution set.
	 * 
	 * @param p
	 */
	public SolutionSet(Population p) {
		pop = p;
		sols = p;
	}
	
	public SolutionSet clone() {
		return new SolutionSet((Population)pop.clone(), (Population)sols.clone());
	}
	
	public Population getSolutions() {
		return sols;
	}
	
	public Population getCurrentPopulation() {
		return pop;
	}
}
