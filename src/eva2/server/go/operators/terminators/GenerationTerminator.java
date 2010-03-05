package eva2.server.go.operators.terminators;
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
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;

import eva2.gui.BeanInspector;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class GenerationTerminator implements  InterfaceTerminator,
Serializable {
	/**
	 * Number of fitness calls on the problem which is optimized
	 */
	protected int m_Generations = 100;
	private String msg = "";
	
	public void init(InterfaceOptimizationProblem prob){
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
	
	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerminated(solSet.getCurrentPopulation());
	}

	public boolean isTerminated(PopulationInterface Pop) {
		if (m_Generations<Pop.getGeneration()) {
			msg = m_Generations + " generations reached.";
			return true;
		}
		return false;
	}

	public String lastTerminationMessage() {
		return msg;
	}

	public String toString() {
		String ret = "Generations calls="+m_Generations;
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
	 * @return tip text for this property
	 */
	public String generationsTipText() {
		return "number of generations to evaluate.";
	}
}