package eva2.problems;

import eva2.optimization.population.Population;

/**
 * A standard interface for solution viewers which display a single (best) solution
 * or a population of solutions.
 *
 */
public interface InterfaceSolutionViewer {
    /**
     * Initialize the view for a certain problem.
     *
     * @param prob
     */
    void initView(AbstractOptimizationProblem prob);

    /**
     * Reset the view.
     */
    void resetView();

    /**
     * Update the view by displaying a population of solutions (often only the best one is shown).
     *
     * @param pop
     */
    void updateView(Population pop, boolean showAllIfPossible);
}
