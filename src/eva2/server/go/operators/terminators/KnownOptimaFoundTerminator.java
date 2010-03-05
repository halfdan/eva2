package eva2.server.go.operators.terminators;

import java.io.Serializable;

import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceMultimodalProblemKnown;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;

/**
 * This terminator counts the number of found optima for a problem that
 * implements InterfaceMultimodalProblemKnown. A population is regarded as terminated
 * if the preset number of optima is identified.
 * For any other problem types, this terminator will not make sense, so take care.
 * 
 * @author mkron
 *
 */
public class KnownOptimaFoundTerminator implements InterfaceTerminator, Serializable {
	InterfaceMultimodalProblemKnown mProblem = null;
	int reqOptima = 1;
	private String msg = "";
	
	public KnownOptimaFoundTerminator() {		
	}
	
	public void init(InterfaceOptimizationProblem prob) {
		if (prob != null) {
			if (prob instanceof InterfaceMultimodalProblemKnown) {
				mProblem = (InterfaceMultimodalProblemKnown)prob;
			} else System.err.println("KnownOptimaFoundTerminator only works with InterfaceMultimodalProblemKnown instances!");
		} else System.err.println("KnownOptimaFoundTerminator wont work with null problem!");
		msg = "Not terminated.";
	}

	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerm(solSet.getSolutions());
	}
	
	public boolean isTerminated(PopulationInterface pop) {
		EVAERROR.errorMsgOnce("Warning, the KnownOptimaFoundTerminator is supposed to work on a final population.");
		return isTerm((Population)pop);
	}

	private boolean isTerm(Population pop) {
		int found = mProblem.getNumberOfFoundOptima(pop);
		if (found >= reqOptima) {
			msg = "There were " + reqOptima + " optima found.";
			return true;
		} else return false;
	}
	
	public String lastTerminationMessage() {
		return msg;
	}

	/**
	 * @return the reqOptima
	 */
	public int getReqOptima() {
		return reqOptima;
	}

	/**
	 * @param reqOptima the reqOptima to set
	 */
	public void setReqOptima(int reqOptima) {
		this.reqOptima = reqOptima;
	}

	public String reqOptimaTipText() {
		return "The number of optima that need to be found to terminate the optimization."; 
	}
	
	public String toString() {
		return "KnownOptimaFoundTerminator requiring " + reqOptima + " optima.";
	}
	
	public static String globalInfo() {
		return "Terminate if a given number of optima has been found. Works for problems implementing InterfaceMultimodalProblemKnown, e.g. FM0."; 
	}
}
