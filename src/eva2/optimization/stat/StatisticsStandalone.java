package eva2.optimization.stat;

import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceAdditionalPopulationInformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This simple statistics implementation can collect all Object data available during runs.
 * Be aware that the memory requirements can be excessive depending on the data produced by
 * the additional informers, and depending on the selected fields to be collected.
 * Therefore, the default is not to log the data but just print it using the super class.
 *
 * @see InterfaceAdditionalPopulationInformer
 * @see AbstractStatistics
 */
public class StatisticsStandalone extends AbstractStatistics implements InterfaceStatistics, Serializable {
    private static final long serialVersionUID = -8451652609212653368L;

    private ArrayList<ArrayList<Object[]>> resultData = null;
    private ArrayList<String> resultHeaderStrings = null;
    private boolean collectData = false;


    public StatisticsStandalone(InterfaceStatisticsParameter statParams) {
        super();
        statisticsParameter = statParams;
    }

    public StatisticsStandalone(String resultFileName) {
        this(resultFileName, 1, resultFileName == null ? StatisticsParameter.VERBOSITY_NONE : StatisticsParameter.VERBOSITY_FINAL, false);
    }

    public StatisticsStandalone(String resultFileName, int multiRuns, int verbosity, boolean outputAllFieldsAsText) {
        this(StatisticsParameter.getInstance(false));
        statisticsParameter.setMultiRuns(multiRuns);
        statisticsParameter.setOutputVerbosity(statisticsParameter.getOutputVerbosity().setSelectedTag(verbosity));
        statisticsParameter.setResultFilePrefix(resultFileName);
        statisticsParameter.setOutputAllFieldsAsText(outputAllFieldsAsText);
        if (resultFileName == null) {
            statisticsParameter.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_WINDOW);
        } else {
            statisticsParameter.setOutputTo(statisticsParameter.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_FILE));
        }
    }

    public StatisticsStandalone() {
        this(new StatisticsParameter());
    }

    @Override
    protected void initPlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (collectData) {
            resultData = new ArrayList<ArrayList<Object[]>>(statisticsParameter.getMultiRuns());
            List<String> description = getOutputHeaderFieldNames(informerList);
            resultHeaderStrings = new ArrayList<String>();
            for (String str : description) {
                resultHeaderStrings.add(str);
            }
            for (int i = 0; i < statisticsParameter.getMultiRuns(); i++) {
                resultData.add(new ArrayList<Object[]>());
            }
        } else {
            resultData = null;
            resultHeaderStrings = null;
        }
    }

    @Override
    protected void plotCurrentResults() {
        if (collectData && (resultData != null)) {
            resultData.get(optRunsPerformed).add(currentStatObjectData);
        }
    }

    @Override
    public void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        double[] specificData = pop.getSpecificData();
        if (specificData != null) {
            for (int i = 0; i < specificData.length; i++) {
//				((ArrayList<Object[]>[]) resultFrame.get(i))[optRunsPerformed].add(new Double[] {new Double(functionCalls), specificData[i]});
                resultData.get(optRunsPerformed).add(new Object[]{new Double(functionCalls), specificData});
            }
        }
    }

    /**
     * Check whether data collection is activated, which stores an Object[] for every iteration and
     * every multi-run.
     *
     * @return
     */
    public boolean isCollectData() {
        return collectData;
    }

    /**
     * Set state of full data collection, which stores an Object[] for every iteration and
     * every multi-run.
     *
     * @param collectFullData
     */
    public void setCollectData(boolean collectFullData) {
        this.collectData = collectFullData;
    }

    public ArrayList<ArrayList<Object[]>> getCollectedData() {
        return resultData;
    }

    public ArrayList<Object[]> getCollectedRunData(int runIndex) {
        return resultData.get(runIndex);
    }

    public ArrayList<String> getCollectedDataHeaders() {
        return resultHeaderStrings;
    }
}