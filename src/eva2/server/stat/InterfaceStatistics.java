package eva2.server.stat;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.util.List;

import eva2.server.go.IndividualInterface;
import eva2.server.go.PopulationInterface;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/
/**
 *
 */
public interface InterfaceStatistics {
	/**
	 * Initialize statistics computations.
	 */
	public void startOptPerformed(String InfoString,int runnumber, Object params); // called from processor
	/**
	 * Finalize statistics computations.
	 */
	public void stopOptPerformed(boolean normal, String stopMessage); // called from processor
	public void addTextListener(InterfaceTextListener listener);
	public boolean removeTextListener(InterfaceTextListener listener);
	public void printToTextListener(String s);
	public void createNextGenerationPerformed(PopulationInterface Pop, List<InterfaceAdditionalPopulationInformer> informerList);
	public void createNextGenerationPerformed(double[] bestfit,double[] worstfit,int calls);
	public InterfaceStatisticsParameter getStatisticsParameter(); // called from moduleadapter
	public IndividualInterface getBestSolution(); // returns the best overall solution
	public double[] getBestFitness(); // returns the best overall fitness
}