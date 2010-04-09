package eva2.server.stat;

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
	currentBest, meanFit, currentWorst, runBest, currentBestFeasible, runBestFeasible, avgPopDistance, maxPopDistance;
	
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
	public static boolean doPlotAvgDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.avgPopDistance.ordinal());
	}
	
	public static boolean doPlotMaxPopDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.maxPopDistance.ordinal());
	}
}
