package eva2.server.stat;

import eva2.server.go.IndividualInterface;
import eva2.server.go.PopulationInterface;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;

/**
 * This may be given to a Processor if no further stats are required. It speeds up
 * optimization especially with small populations (e.g. HC as local search operator).
 * 
 * @author mkron
 *
 */
public class StatisticsDummy implements InterfaceStatistics, InterfaceTextListener {
	boolean consoleOut = false;
	StatsParameter sParams = null;
	
	public StatisticsDummy() {
		sParams = new StatsParameter();
		sParams.setOutputVerbosityK(StatsParameter.VERBOSITY_NONE);
	}
	
	public StatisticsDummy(boolean doConsoleOut) {
		sParams = new StatsParameter();
		sParams.setOutputVerbosityK(StatsParameter.VERBOSITY_NONE);
		consoleOut = doConsoleOut;
	}
	
	public void addTextListener(InterfaceTextListener listener) {
		System.err.println("addTextListener not provided!");
	}

	public void createNextGenerationPerformed(PopulationInterface Pop,
			InterfaceAdditionalPopulationInformer informer) {
	}

	public void createNextGenerationPerformed(double[] bestfit,
			double[] worstfit, int calls) {
	}

	public double[] getBestFitness() {
		System.err.println("getBestFitness not provided!");
		return null;
	}

	public IndividualInterface getBestSolution() {
		System.err.println("getBestSolution not provided!");
		return null;
	}

	public InterfaceStatisticsParameter getStatisticsParameter() {
		return sParams;
	}

	public void printToTextListener(String s) {
		if (consoleOut) System.out.println(s);
	}

	public boolean removeTextListener(InterfaceTextListener listener) {
		System.err.println("removeTextListener not provided!");
		return false;
	}

	public void startOptPerformed(String InfoString, int runnumber,
			Object params) {}

	public void stopOptPerformed(boolean normal, String stopMessage) {}
	
	public void print(String str) {
		if (consoleOut) System.out.print(str);
	}
	public void println(String str) {
		if (consoleOut) System.out.println(str);
	}

}
