package eva2.server.stat;

/**
 * An interface to listen to statistical data of an optimization run. 
 * 
 * @see AbstractStatistics
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
}
