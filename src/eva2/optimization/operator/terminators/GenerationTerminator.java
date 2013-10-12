package eva2.optimization.operator.terminators;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 319 $
 *            $Date: 2007-12-05 11:29:32 +0100 (Wed, 05 Dec 2007) $
 *            $Author: mkron $
 */

import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceOptimizationProblem;

import java.io.Serializable;

/**
 *
 */
public class GenerationTerminator implements InterfaceTerminator, Serializable {

    /**
     * Number of fitness calls on the problem which is optimized
     */
    protected int m_Generations = 100;
    private String msg = "";

    @Override
    public void init(InterfaceOptimizationProblem prob) {
        msg = "Not terminated.";
    }

    public static String globalInfo() {
        return "Terminate after the given number of generations";
    }

    public GenerationTerminator() {
    }

    public GenerationTerminator(int gens) {
        m_Generations = gens;
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerminated(solSet.getCurrentPopulation());
    }

    @Override
    public boolean isTerminated(PopulationInterface Pop) {
        if (m_Generations < Pop.getGeneration()) {
            msg = m_Generations + " generations reached.";
            return true;
        }
        return false;
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    @Override
    public String toString() {
        String ret = "Generations calls=" + m_Generations;
        return ret;
    }

    public void setGenerations(int x) {
        m_Generations = x;
    }

    public int getGenerations() {
        return m_Generations;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property
     */
    public String generationsTipText() {
        return "number of generations to evaluate.";
    }
}