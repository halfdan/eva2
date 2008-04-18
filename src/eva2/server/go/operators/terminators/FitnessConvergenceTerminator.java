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
import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.tools.SelectedTag;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class FitnessConvergenceTerminator implements InterfaceTerminator,
Serializable {
	private static final long serialVersionUID = 5749620193474954959L;
	protected static boolean TRACE = false;
	protected double convThresh = 0.01;
	protected int m_stagTime = 100;
	protected int popFitCalls = 1000;
	protected int popGens = 1000;
	protected boolean firstTime = true;
	protected double[] oldFit;
	protected double oldNorm;
	private SelectedTag stagnationMeasure = new SelectedTag("Fitness calls", "Generations");
	private SelectedTag convergenceCondition = new SelectedTag("Relative", "Absolute");
	PhenotypeMetric pMetric = null;
	
	public FitnessConvergenceTerminator() {
		pMetric = new PhenotypeMetric();
	}

	public FitnessConvergenceTerminator(double thresh, int stagnPeriod, boolean bFitCallBased, boolean bAbsolute) {
		pMetric = new PhenotypeMetric();
		convThresh = thresh;
		this.m_stagTime = stagnPeriod;
		if (bFitCallBased) stagnationMeasure.setSelectedTag("Fitness calls");
		else stagnationMeasure.setSelectedTag("Generations");
		if (bAbsolute) convergenceCondition.setSelectedTag("Absolute");
		else convergenceCondition.setSelectedTag("Relative");
	}
	
	/**
	 *
	 */
	public String globalInfo() {
		return "Stop if a fitness convergence criterion has been met.";
	}
	
	public void init() {
		if (pMetric == null) pMetric = new PhenotypeMetric();
		firstTime = true;
	}

	public boolean isTerminated(PopulationInterface Pop) {
		if (!firstTime && isStillConverged(Pop.getBestIndividual())) {
			if (stagnationTimeHasPassed(Pop)) {
				// population hasnt improved much for max time, criterion is met
				return true;
			} else {
				// population hasnt improved much for i<max time, keep running
				return false;
			}
		} else {
			// first call at all - or population improved more than "allowed" to terminate
			saveState(Pop);

			return false;
		}
	}
	
	public String terminatedBecause(PopulationInterface pop) {
		if (isTerminated(pop)) {
			return getTerminationMessage("Fitness converged");		
		} else return "Not yet terminated.";
	}

	protected String getTerminationMessage(String prefix) {
		StringBuffer sb = new StringBuffer(prefix);
		if (convergenceCondition.isSelectedString("Relative")) sb.append(" relatively below ");
		else sb.append(" absolutely below ");
		sb.append(convThresh);
		sb.append(" for ");
		sb.append(m_stagTime);
		if (stagnationMeasure.isSelectedString("Generations")) sb.append(" generations.");
		else sb.append(" function calls.");
		return sb.toString();
	}
	
	protected void saveState(PopulationInterface Pop) {
		oldFit = Pop.getBestFitness();
		oldNorm = PhenotypeMetric.norm(oldFit);
		popFitCalls = Pop.getFunctionCalls();
		popGens = Pop.getGenerations();
		firstTime = false;		
	}
	
	/**
	 * Return true if |oldFit - curFit| < |oldFit| * thresh% (relative case)
	 * and if |oldFit - curFit| < thresh (absolute case).
	 * 
	 * @param curFit
	 * @return
	 */
	protected boolean isStillConverged(IndividualInterface indy) {
		double[] curFit = indy.getFitness();
		double dist = PhenotypeMetric.euclidianDistance(oldFit, curFit);
		boolean ret;
		if (convergenceCondition.isSelectedString("Relative")) {
			ret = (dist < (oldNorm * convThresh));
		} else {
			ret = (dist < convThresh);
		}
		if (TRACE) System.out.println("isStillConverged returns " + ret + ", dist " + dist + ", old fit " + BeanInspector.toString(oldFit) + ", curFit " + BeanInspector.toString(curFit));
		return ret;
	}
	
	private boolean stagnationTimeHasPassed(PopulationInterface pop) {
		if (stagnationMeasure.isSelectedString("Fitness calls")) { // by fitness calls
//			System.out.println("stagnationTimeHasPassed returns " + ((pop.getFunctionCalls() - popFitCalls) >= m_stagTime) + " after " + (pop.getFunctionCalls() - popFitCalls));
			return (pop.getFunctionCalls() - popFitCalls) >= m_stagTime;
		} else {// by generation
//			System.out.println("stagnationTimeHasPassed returns " + ((pop.getFunctionCalls() - popGens) >= m_stagTime) + " after " + (pop.getFunctionCalls() - popGens));
			return (pop.getGenerations() - popGens) >= m_stagTime;
		}
	}
	
	/**
	 *
	 */
//	public String toString() {
//		return BeanTest.toString(this);
//	}

	/**
	 *
	 */
	public void setConvergenceThreshold(double x) {
		convThresh = x;
	}

	/**
	 *
	 */
	public double getConvergenceThreshold() {
		return convThresh;
	}

	public String convergenceThresholdTipText() {
		return "Terminate if the fitness has not improved by this percentage / absolute value for a whole stagnation time period";
	}
	
	/**
	 *
	 */
	public void setStagnationTime(int k) {
		m_stagTime = k;
	}

	/**
	 *
	 */
	public int getStagnationTime() {
		return m_stagTime;
	}
	
	public String stagnationTimeTipText() {
		return "Terminate if the population has not improved for this time";
	}


	/**
	 * @return the stagnationTimeIn
	 */
	public SelectedTag getStagnationMeasure() {
		return stagnationMeasure;
	}

	/**
	 * @param stagnationTimeIn the stagnationTimeIn to set
	 */
	public void setStagnationMeasure(SelectedTag stagnationTimeIn) {
		this.stagnationMeasure = stagnationTimeIn;
	}
	
	public String stagnationMeasureTipText() {
		return "Stagnation time is measured in fitness calls or generations, to be selected here.";
	}

	/**
	 * @return the convergenceCondition
	 */
	public SelectedTag getConvergenceCondition() {
		return convergenceCondition;
	}

	/**
	 * @param convergenceCondition the convergenceCondition to set
	 */
	public void setConvergenceCondition(SelectedTag convergenceCondition) {
		this.convergenceCondition = convergenceCondition;
	}
	
	public String convergenceConditionTipText() {
		return "Select between absolute and relative convergence condition";
	}
}