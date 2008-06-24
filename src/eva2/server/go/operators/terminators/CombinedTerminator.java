package eva2.server.go.operators.terminators;

import java.io.Serializable;

import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.SelectedTag;


public class CombinedTerminator implements InterfaceTerminator, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4748749151972645021L;
	private InterfaceTerminator t1 = new FitnessConvergenceTerminator();
	private InterfaceTerminator t2 = new EvaluationTerminator();
	private SelectedTag andOrTag = new SelectedTag("OR", "AND");
	private String msg = null;
	
	public static final boolean AND = true;
	public static final boolean OR = false;
	
	/**
	 *
	 */
	public CombinedTerminator() {}

	/**
	 * Convenience constructor combining the given terminators in the expected way.
	 * 
	 */
	public CombinedTerminator(InterfaceTerminator t1, InterfaceTerminator t2, boolean bAnd) {
		this.t1 = t1;
		this.t2 = t2;
		andOrTag.setSelectedTag(bAnd ? "AND" : "OR");
	}
	
	public String globalInfo() {
		return "Boolean combination of two terminators.";
	}
	
	public void init(InterfaceOptimizationProblem prob) {
		if (t1 != null) t1.init(prob);
		if (t2 != null) t2.init(prob);
		msg = "Not terminated.";
	}

	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerminated(solSet.getCurrentPopulation());
	}
	
	public boolean isTerminated(PopulationInterface pop) {
		boolean ret;
		if ((t1 == null) && (t2 == null)) {
			System.err.println("Error: No terminator set in CombinedTerminator");
			return true; 
		}
		if (t1 == null) {
			ret = t2.isTerminated(pop);
			msg = t2.lastTerminationMessage();
			return ret;
		}
		if (t2 == null) {
			ret = t1.isTerminated(pop);
			msg = t1.lastTerminationMessage();
			return ret;
		}
		
		if (andOrTag.isSelectedString("AND")) {
			// make sure that both terminators are triggered by every call, because some judge
			// time-dependently and store information on the population.
			ret = t1.isTerminated(pop);
			ret = ret && t2.isTerminated(pop); 
			if (ret) msg = "Terminated because both: " + t1.lastTerminationMessage() + " And " + t2.lastTerminationMessage();
		} else { // OR
			// make sure that both terminators are triggered by every call, because some judge
			// time-dependently and store information on the population.
			ret = t1.isTerminated(pop);
			if (ret) msg = t1.lastTerminationMessage();
			ret = ret || t2.isTerminated(pop);
			if (ret) msg = t2.lastTerminationMessage();
		}
		return ret;
	}
	
	public String lastTerminationMessage() {
		return msg;
	}

	/**
	 * @return the andOrTag
	 */
	public SelectedTag getAndOrTag() {
		return andOrTag;
	}

	/**
	 * @param andOrTag the andOrTag to set
	 */
	public void setAndOrTag(SelectedTag andOrTag) {
		this.andOrTag = andOrTag;
	}
	
	public String andOrTagTipText() {
		return "Set the boolean operator to be used to combine the two terminators.";
	}

	/**
	 * @return the t1
	 */
	public InterfaceTerminator getTerminatorOne() {
		return t1;
	}

	/**
	 * @param t1 the t1 to set
	 */
	public void setTerminatorOne(InterfaceTerminator t1) {
		this.t1 = t1;
	}
	
	public String terminatorOneTipText() {
		return "The first terminator to be combined.";
	}

	/**
	 * @return the t2
	 */
	public InterfaceTerminator getTerminatorTwo() {
		return t2;
	}

	/**
	 * @param t2 the t2 to set
	 */
	public void setTerminatorTwo(InterfaceTerminator t2) {
		this.t2 = t2;
	}

	public String terminatorTwoTipText() {
		return "The second terminator to be combined.";
	}

}
