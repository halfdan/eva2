package eva2.server.stat;

import java.util.List;

/**
 * An interface to listen to statistical data of an optimization run. 
 * 
 * @see AbstractStatistics
 * @see InterfaceStatisticsParameter
 * @author mkron
 *
 */
public interface InterfaceStatisticsListener {
	/**
	 * Method called by the statistics class with the current data header, raw Objects and 
	 * Double values (as far as the raw objects could be converted to double - otherwise null).
	 * 
	 * @param header
	 * @param statObjects
	 * @param statDoubles
	 */
	public void notifyGenerationPerformed(String[] header, Object[] statObjects, Double[] statDoubles);
	
	/**
	 * Method called at the start of a single run.
	 * 
	 * @param runNumber the number of the new run, starting with 0
	 * @param plannedMultiRuns the number of planned multi-runs
	 */
	public void notifyRunStarted(int runNumber, int plannedMultiRuns);

	/**
	 * Method called at the end of a single run.
	 * 
	 * @param runsPerformed the number of runs performed
	 * @param completedLastRun true, if the last run was stopped normally, otherwise false, e.g. indicating a user break
	 */
	public void notifyRunStopped(int runsPerformed, boolean completedLastRun);
	
	/**
	 * Receive the list of last data lines for a set of multiruns. The data list may be null if no runs were
	 * performed or no data was collected. The method will only be called if a multi-run experiment was performed.
	 * 
	 * @see InterfaceStatisticsParameter
	 * @see AbstractStatistics
	 * @param header
	 * @param multiRunFinalObjectData
	 */
	public void finalMultiRunResults(String[] header, List<Object[]> multiRunFinalObjectData);
}
