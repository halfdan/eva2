package eva2.optimization.operator.crossover;


public interface InterfaceEvaluatingCrossoverOperator extends InterfaceCrossover {
	/**
	 * Retrieve the number of evaluations performed during crossover.
	 *  
	 * @return
	 */
	public int getEvaluations();
	
	public void resetEvaluations();
}
