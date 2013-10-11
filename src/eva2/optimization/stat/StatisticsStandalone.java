package eva2.optimization.stat;

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

import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;

import java.io.Serializable;
import java.net.InetAddress;
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

    private static String m_MyHostName = "unknown";

    //	private String m_InfoString;
    private ArrayList<ArrayList<Object[]>> m_ResultData = null;
    private ArrayList<String> m_ResultHeaderStrings = null;
    private boolean collectData = false;


    public StatisticsStandalone(InterfaceStatisticsParameter statParams) {
        super();
        m_StatsParams = statParams;
        try {
            m_MyHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            System.err.println("ERROR getting HostName " + e.getMessage());
        }
    }

    public StatisticsStandalone(String resultFileName) {
        this(resultFileName, 1, resultFileName == null ? StatisticsParameter.VERBOSITY_NONE : StatisticsParameter.VERBOSITY_FINAL, false);
    }

    public StatisticsStandalone(String resultFileName, int multiRuns, int verbosity, boolean outputAllFieldsAsText) {
        this(StatisticsParameter.getInstance(false));
        m_StatsParams.setMultiRuns(multiRuns);
        m_StatsParams.setOutputVerbosity(m_StatsParams.getOutputVerbosity().setSelectedTag(verbosity));
        m_StatsParams.setResultFilePrefix(resultFileName);
        m_StatsParams.setOutputAllFieldsAsText(outputAllFieldsAsText);
        if (resultFileName == null) {
            m_StatsParams.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_WINDOW);
        } else {
            m_StatsParams.setOutputTo(m_StatsParams.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_FILE));
        }
    }

    public StatisticsStandalone() {
        this(new StatisticsParameter());
    }

    @Override
    protected void initPlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (collectData) {
            m_ResultData = new ArrayList<ArrayList<Object[]>>(m_StatsParams.getMultiRuns());
            List<String> description = getOutputHeaderFieldNames(informerList);
            m_ResultHeaderStrings = new ArrayList<String>();
            for (String str : description) {
                m_ResultHeaderStrings.add(str);
            }
            for (int i = 0; i < m_StatsParams.getMultiRuns(); i++) {
                m_ResultData.add(new ArrayList<Object[]>());
            }
        } else {
            m_ResultData = null;
            m_ResultHeaderStrings = null;
        }
    }

    @Override
    protected void plotCurrentResults() {
        if (collectData && (m_ResultData != null)) {
            m_ResultData.get(optRunsPerformed).add(currentStatObjectData);
        }
    }

    @Override
    public void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        double[] specificData = pop.getSpecificData();
        if (specificData != null) {
            for (int i = 0; i < specificData.length; i++) {
//				((ArrayList<Object[]>[]) m_Result.get(i))[optRunsPerformed].add(new Double[] {new Double(functionCalls), specificData[i]});
                m_ResultData.get(optRunsPerformed).add(new Object[]{new Double(functionCalls), specificData});
            }
        }
    }

    @Override
    public String getHostName() {
        return m_MyHostName;
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
        return m_ResultData;
    }

    public ArrayList<Object[]> getCollectedRunData(int runIndex) {
        return m_ResultData.get(runIndex);
    }

    public ArrayList<String> getCollectedDataHeaders() {
        return m_ResultHeaderStrings;
    }
}