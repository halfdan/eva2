package eva2.server.stat;

import eva2.tools.StringSelection;

public enum GraphSelectionEnum {
	currentBest, currentWorst, runBest, bestFeasible, avgPopDistance, maxPopDistance;
	
	public static boolean doPlotCurrentBest(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.currentBest.ordinal());
	}
	
	public static boolean doPlotRunBest(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.runBest.ordinal());
	}
	
	public static boolean doPlotWorst(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.currentWorst.ordinal());
	}
	
	public static boolean doPlotBestFeasible(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.bestFeasible.ordinal()); 
	}
	
	public static boolean doPlotAvgDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.avgPopDistance.ordinal());
	}
	
	public static boolean doPlotMaxPopDist(StringSelection sel) {
		return sel.isSelected(GraphSelectionEnum.maxPopDistance.ordinal());
	}
}
