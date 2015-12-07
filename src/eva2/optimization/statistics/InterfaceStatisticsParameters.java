package eva2.optimization.statistics;

import eva2.tools.StringSelection;

/**
 * An interface to encapsulate statistics parameters.
 *
 * @see StatisticsParameters
 */
public interface InterfaceStatisticsParameters {
    String getName();

    void saveInstance();

    void setMultiRuns(int x);

    int getMultiRuns();

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

    void setOutputVerbosity(OutputVerbosity sTag);

    OutputVerbosity getOutputVerbosity();

    int getOutputVerbosityK();

    void setOutputVerbosityK(int k);

    void setOutputTo(OutputTo sTag);

    OutputTo getOutputTo();

    enum OutputVerbosity {
        NONE, FINAL, KTH_IT, ALL;

        @Override
        public String toString() {
            switch (this) {
                case NONE: return "No output";
                case FINAL: return "Final Results";
                case KTH_IT: return "K-th iterations";
                case ALL: return "All iterations";
                default: return this.name();
            }
        }
    }

    enum OutputTo {
        FILE, WINDOW, BOTH;

        public String toString() {
            switch (this) {
                case FILE: return "File (current dir.)";
                case WINDOW: return "Text-window";
                case BOTH: return "Both file and text-window";
                default: return name();
            }
        }
    }
}