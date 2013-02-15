package eva2.optimization.stat;

import eva2.tools.StringSelection;

/**
 * An Enum to be used in the statistics classes for identifying data fields.
 * 
 * @see AbstractStatistics
 * @author mkron
 *
 */
public enum GraphSelectionEnum {
	// DONT change this order, or the relation to AbstractStatistics will be lost
	currentBest, meanFit, currentWorst, runBest, currentBestFeasible, runBestFeasible, 
	avgEucPopDistance, maxEucPopDistance, avgPopMetricDist, maxPopMetricDist;
	
	private static String[] toolTips = { 
		"The current best fitness within the population",
		"The mean fitness within the population",
		"The current worst fitness within the population",
		"The best fitness up to the current generation",
		"The best feasible fitness within the population",
		"The best feasible fitness up to the current generation",
		"The average euclidean distance of individuals in the current population",
		"The maximum euclidean distance of individuals in the current population",
		"The average distance of individuals in the current population metric",
		"The maximum distance of individuals in the current population metric",
	};
	
	public static String[] getInfoStrings() {
		if (GraphSelectionEnum.values().length != toolTips.length) {
			System.err.println("Error, mismatching length of info strings in GraphSelectionEnum");
			return null;
		} else {
                return toolTips;
            }
	}
	
//	public static boolean doPlotCurrentBest(StringSelection sel) {
//		return sel.isSelected(GraphSelectionEnum.currentBest.ordinal());
//	}
//	
//	public static boolean doPlotRunBest(StringSelection sel) {
//		return sel.isSelected(GraphSelectionEnum.runBest.ordinal());
//	}
//	
	public static boolean doPlotWorst(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.currentWorst.ordinal());
	}
	
	public static boolean doPlotMean(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.meanFit.ordinal());
	}
//	public static boolean doPlotCurrentBestFeasible(StringSelection sel) {
//		return sel.isSelected(GraphSelectionEnum.currentBestFeasible.ordinal()); 
//	}
//	
//	public static boolean doPlotRunBestFeasible(StringSelection sel) {
//		return sel.isSelected(GraphSelectionEnum.runBestFeasible.ordinal()); 
//	}
//	
	public static boolean doPlotAvgEucDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.avgEucPopDistance.ordinal());
	}
	
	public static boolean doPlotMaxEucDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.maxEucPopDistance.ordinal());
	}
	
	public static boolean doPlotAvgPopMetricDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.avgPopMetricDist.ordinal());
	}
	
	public static boolean doPlotMaxPopMetricDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.maxPopMetricDist.ordinal());
	}
}
