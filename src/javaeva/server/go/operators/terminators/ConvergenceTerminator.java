package javaeva.server.go.operators.terminators;

/*
 * Title:        JavaEvA
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

import javaeva.server.go.PopulationInterface;
import javaeva.server.go.TerminatorInterface;
import javaeva.server.go.operators.distancemetric.PhenotypeMetric;
import javaeva.tools.SelectedTag;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class ConvergenceTerminator implements TerminatorInterface,
Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5749620193474954959L;
	private double m_percent = 0.01;
	private int m_stagTime = 100;
	private int popFitCalls = 1000;
	private int popGens = 1000;
	private boolean firstTime = true;
	private double[] oldFit;
	private double oldFitNorm;
	private SelectedTag stagnationMeasure = new SelectedTag("Fitness calls", "Generations");

	/**
	 *
	 */
	public ConvergenceTerminator() {

	}


	/**
	 *
	 */
	public String globalInfo() {
		return "Stop if a convergence criterion has been met.";
	}
	
	/**
	 *
	 * @return
	 */
	public void init() {
		firstTime = true;
	}

	/**
	 *
	 */
	public boolean isTerminated(PopulationInterface Pop) {
		if (!firstTime && isStillConverged(Pop.getBestFitness())) {
			if (stagnationTimeHasPassed(Pop)) {
				// population hasnt improved much for max time, criterion is met
				return true;
			} else {
				// population hasnt improved much for i<max time, keep running
				return false;
			}
		} else {
			// first call at all - or population improved more than "allowed" to terminate
			oldFit = Pop.getBestFitness();
			oldFitNorm = PhenotypeMetric.norm(oldFit);
			popFitCalls = Pop.getFunctionCalls();
			popGens = Pop.getGenerations();
			firstTime = false;
			return false;
		}
	}
	
	/**
	 * Return true if |oldFit - curFit| < |oldFit| * p%
	 * @param curFit
	 * @return
	 */
	private boolean isStillConverged(double[] curFit) {
		double dist = PhenotypeMetric.euclidianDistance(oldFit, curFit);
//		System.out.println("isStillConverged returns " + (dist < (oldFitNorm * m_percent)) + ", dist " + dist + ", old norm " + oldFitNorm + ", ratio " + (dist/oldFitNorm));
		return (dist < (oldFitNorm * m_percent));
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
	public String toString() {
		String ret = "\r\nConvergenceTerminator";
		return ret;
	}

	/**
	 *
	 */
	public void setFitnessPerCent(double x) {
		m_percent = x;
	}

	/**
	 *
	 */
	public double getFitnessPerCent() {
		return m_percent;
	}

	public String fitnessPerCentTipText() {
		return "Terminate if the population has not improved by the given percentage for n generations";
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
}