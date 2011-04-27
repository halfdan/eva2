package eva2.server.go.operators.crossover;


public interface InterfaceEvaluatingCrossoverOperator extends InterfaceCrossover {
	/**
	 * Retrieve the number of evaluations performed during crossover.
	 *  
	 * @return
	 */
	public int getEvaluations();
	
	public void resetEvaluations();
}
