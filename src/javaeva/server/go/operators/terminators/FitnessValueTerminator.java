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

import javaeva.gui.BeanInspector;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.InterfaceTerminator;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class FitnessValueTerminator implements InterfaceTerminator,
Serializable {
	protected double[] m_FitnessValue;
	/**
	 *
	 */
	public FitnessValueTerminator() {
		m_FitnessValue = new double []{0.1};
	}

	public void init(){}
	/**
	 *
	 */
	public String globalInfo() {
		return "Terminate if a certain fitness value has been reached.";
	}
	/**
	 *
	 */
	public FitnessValueTerminator( double[] x) {
		m_FitnessValue = (double[])x.clone();
	}
	/**
	 *
	 */
	public boolean isTerminated(PopulationInterface Pop) {
		double[] fit = Pop.getBestFitness();
		for (int i = 0; i < fit.length; i++) {
			if (m_FitnessValue[i]>fit[i]) return false;
		}
		return true;
	}

	public String terminatedBecause(PopulationInterface pop) {
		if (isTerminated(pop)) {
			return "Fitness value below " + BeanInspector.toString(m_FitnessValue);		
		} else return "Not yet terminated.";
	}

	/**
	 *
	 */
	public String toString() {
		String ret = "FitnessValueTerminator,m_FitnessValue="+m_FitnessValue;
		return ret;
	}
	/**
	 *
	 */
	public void  setFitnessValue(double[] x) {
		m_FitnessValue = x;
	}
	/**
	 *
	 */
	public double[] getFitnessValue() {
		return m_FitnessValue;
	}
	
	public String fitnessValueTipText() {
		return "Set the fitness objective value.";
	}
}