package eva2.optimization.operators.terminators;
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
import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.go.PopulationInterface;
import eva2.optimization.populations.InterfaceSolutionSet;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import java.io.Serializable;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class FitnessValueTerminator implements InterfaceTerminator,
Serializable {
	protected double[] m_FitnessValue;
	private String msg = "";
	/**
	 *
	 */
	public FitnessValueTerminator() {
		m_FitnessValue = new double []{0.1};
	}

    @Override
	public void init(InterfaceOptimizationProblem prob){
		msg = "Not terminated.";
	}
	/**
	 *
	 */
	public static String globalInfo() {
		return "Terminate if a certain fitness value has been reached.";
	}
	/**
	 *
	 */
	public FitnessValueTerminator( double[] v) {
		m_FitnessValue = (double[])v.clone();
	}
	
    @Override
	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerminated(solSet.getCurrentPopulation());
	}
	
    @Override
	public boolean isTerminated(PopulationInterface Pop) {
		double[] fit = Pop.getBestFitness();
		for (int i = 0; i < fit.length; i++) {
			if (m_FitnessValue[i]<fit[i]) {
                        return false;
                    }
		}
		msg = "Fitness value reached " + BeanInspector.toString(m_FitnessValue);	
		return true;
	}

    @Override
	public String lastTerminationMessage() {
		return msg;
	}

	/**
	 *
	 */
    @Override
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