package eva2.optimization.statistics;

import java.util.List;

/**
 * An interface to listen to statistical data of an optimization run.
 *
 * @author mkron
 * @see AbstractStatistics
 * @see InterfaceStatisticsParameters
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
    void notifyGenerationPerformed(String[] header, Object[] statObjects, Double[] statDoubles);

    /**
     * Method called at the start of a single run.
     *
     * @param runNumber        the number of the new run, starting with 0
     * @param plannedMultiRuns the number of planned multi-runs
     * @param header           field names of the data
     * @param metaInfo         additional meta information on the data fields
     */
    void notifyRunStarted(int runNumber, int plannedMultiRuns, String[] header, String[] metaInfo);

    /**
     * Method called at the end of a single run.
     *
     * @param runsPerformed    the number of runs performed
     * @param completedLastRun true, if the last run was stopped normally, otherwise false, e.g. indicating a user break
     */
    void notifyRunStopped(int runsPerformed, boolean completedLastRun);

    /**
     * Receive the list of last data lines for a set of multiruns. The data list may be null if no runs were
     * performed or no data was collected. The method will only be called if a multi-run experiment was performed.
     *
     * @param header
     * @param multiRunFinalObjectData
     * @see InterfaceStatisticsParameters
     * @see AbstractStatistics
     */
    void finalMultiRunResults(String[] header, List<Object[]> multiRunFinalObjectData);

    /**
     * Called after the job is finished. Return true if the listener should be removed after this multi-run.
     *
     * @param header
     * @param multiRunFinalObjectData
     */
    boolean notifyMultiRunFinished(String[] header, List<Object[]> multiRunFinalObjectData);
}
