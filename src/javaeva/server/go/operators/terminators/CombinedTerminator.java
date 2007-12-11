package javaeva.server.go.operators.terminators;

import java.io.Serializable;

import javaeva.server.go.PopulationInterface;
import javaeva.server.go.TerminatorInterface;
import javaeva.tools.SelectedTag;

public class CombinedTerminator implements TerminatorInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4748749151972645021L;
	private TerminatorInterface t1 = new ConvergenceTerminator();
	private TerminatorInterface t2 = new EvaluationTerminator();
	private SelectedTag andOrTag = new SelectedTag("OR", "AND");
	
	/**
	 *
	 */
	public CombinedTerminator() {}

	public String globalInfo() {
		return "Boolean combination of two terminators.";
	}
	
	public void init() {
		if (t1 != null) t1.init();
		if (t2 != null) t2.init();
	}

	public boolean isTerminated(PopulationInterface Pop) {
		boolean ret;
		if ((t1 == null) && (t2 == null)) {
			System.err.println("Error: No terminator set in CombinedTerminator");
			return true; 
		}
		if (t1 == null) return t2.isTerminated(Pop);
		if (t2 == null) return t1.isTerminated(Pop);
		
		if (andOrTag.isSelectedString("AND")) {
			ret = t1.isTerminated(Pop) && t2.isTerminated(Pop); 
		} else {
			ret = t1.isTerminated(Pop) || t2.isTerminated(Pop);
		}
		return ret;
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
	public TerminatorInterface getTerminatorOne() {
		return t1;
	}

	/**
	 * @param t1 the t1 to set
	 */
	public void setTerminatorOne(TerminatorInterface t1) {
		this.t1 = t1;
	}
	
	public String terminatorOneTipText() {
		return "The first terminator to be combined.";
	}

	/**
	 * @return the t2
	 */
	public TerminatorInterface getTerminatorTwo() {
		return t2;
	}

	/**
	 * @param t2 the t2 to set
	 */
	public void setTerminatorTwo(TerminatorInterface t2) {
		this.t2 = t2;
	}

	public String terminatorTwoTipText() {
		return "The second terminator to be combined.";
	}

}
