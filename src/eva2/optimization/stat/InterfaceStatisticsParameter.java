package eva2.optimization.stat;

import eva2.tools.SelectedTag;
import eva2.tools.StringSelection;

/**
 * An interface to encapsulate statistics parameters.
 *
 * @see StatsParameter
 */
public interface InterfaceStatisticsParameter {
    String getName();

    void saveInstance();

    void setMultiRuns(int x);

    int getMultiRuns();

    String multiRunsTipText();


    /**
     * Use averaged graph for multi-run plots or not.
     *
     * @return If an average graph is used or not
     */
    boolean getUseStatPlot();

    /**
     * Activate averaged graph for multi-run plots.
     *
     * @param x If averaged graph should be activated.
     */
    void setUseStatPlot(boolean x);

    StringSelection getFieldSelection();

    void setFieldSelection(StringSelection v);

    String getResultFilePrefix();

    void setResultFilePrefix(String x);

    void setConvergenceRateThreshold(double x);

    double getConvergenceRateThreshold();

    void setShowTextOutput(boolean show);

    boolean isShowTextOutput();

    boolean isOutputAllFieldsAsText();

    void setOutputAllFieldsAsText(boolean bShowFullText);

    void setOutputVerbosity(SelectedTag sTag);

    SelectedTag getOutputVerbosity();

    int getOutputVerbosityK();

    void setOutputVerbosityK(int k);

    void setOutputTo(SelectedTag sTag);

    SelectedTag getOutputTo();
}