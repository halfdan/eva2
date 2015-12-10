package eva2.optimization.operator.crossover;


public interface InterfaceEvaluatingCrossoverOperator extends InterfaceCrossover {
    /**
     * Retrieve the number of evaluations performed during crossover.
     *
     * @return
     */
    int getEvaluations();

    void resetEvaluations();
}
