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
public class EvaluationTerminator implements InterfaceTerminator,
Serializable {
	private String msg = "Not terminated.";
	/**
	 * Number of fitness calls on the problem which is optimized.
	 */
	protected int m_FitnessCalls = 1000;

	public EvaluationTerminator() {
	}
	
	public void init(InterfaceOptimizationProblem prob){
		msg = "Not terminated.";
	}

	public static String globalInfo() {
		return "Terminates after the given number of fitness calls.";
	}

	/**
	 * Construct Terminator with a maximum number of fitness calls.
	 * @param maximum number of fitness calls
	 */
	public EvaluationTerminator(int x) {
		m_FitnessCalls = x;
	}

	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerminated(solSet.getCurrentPopulation());
	}

	public boolean isTerminated(PopulationInterface pop) {
		//System.out.println("m_FitnessCalls="+m_FitnessCalls);
		if (m_FitnessCalls>pop.getFunctionCalls()) return false;
		else {
			msg = m_FitnessCalls + " fitness calls were reached.";
			return true;
		}
	}

	public String lastTerminationMessage() {
		return msg;
	}

	public String toString() {
		String ret = "EvaluationTerminator,calls="+m_FitnessCalls;
		return ret;
	}

	public void setFitnessCalls(int x) {
		//System.out.println("setFitnessCalls"+x);
		m_FitnessCalls = x;
	}

	public int getFitnessCalls() {
		//System.out.println("getFitnessCalls"+m_FitnessCalls);
		return m_FitnessCalls;
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property
	 */
	public String fitnessCallsTipText() {
		return "number of calls to fitness function.";
	}

}