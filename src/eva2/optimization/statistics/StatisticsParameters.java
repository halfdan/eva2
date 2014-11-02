package eva2.optimization.statistics;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfaceNotifyOnInformers;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.EVAERROR;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;
import eva2.tools.StringSelection;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of parameters for statistics in EvA2. Several data entries are provided by the AbstractStatistics class,
 * others by the additional informers. This class allows customization of entries and frequency of data output.
 * Data entries can be selected using a StringSelection instance.
 * There is a switch called "output full data as text" which will be interpreted by AbstractStatistics showing
 * all or only the selected entities.
 *
 * @see AbstractStatistics
 */
@Description(value = "Configure statistics and output of the optimization run. Changes to the data selection state will not take effect during a run.")
public class StatisticsParameters implements InterfaceStatisticsParameters, InterfaceNotifyOnInformers, Serializable {
    private static final Logger LOGGER = Logger.getLogger(StatisticsParameters.class.getName());
    public final static int VERBOSITY_NONE = 0;
    public final static int VERBOSITY_FINAL = 1;
    public final static int VERBOSITY_KTH_IT = 2;
    public final static int VERBOSITY_ALL = 3;
    SelectedTag outputVerbosity = new SelectedTag("No output", "Final results", "K-th iterations", "All iterations");
    public final static int OUTPUT_FILE = 0;
    public final static int OUTPUT_WINDOW = 1;
    public final static int OUTPUT_FILE_WINDOW = 2;
    SelectedTag outputTo = new SelectedTag("File (current dir.)", "Text-window", "Both file and text-window");
    private int verbosityK = 10;
    private int textOutput = 0;
    private int multiRuns = 1;
    private String resultFilePrefix = "EvA2";
    protected String name = "not defined";
    private boolean useStatPlot = true;
    private boolean showAdditionalProblemInfo = false;
    private double convergenceRateThreshold = 0.001;
    private StringSelection graphSel = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings());

    /**
     *
     */
    public static StatisticsParameters getInstance(boolean loadDefaultSerFile) {
        if (loadDefaultSerFile) {
            return getInstance("Statistics.ser");
        } else {
            return new StatisticsParameters();
        }
    }

    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static StatisticsParameters getInstance(String serFileName) {
        StatisticsParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(serFileName);
            instance = (StatisticsParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }

        if (instance == null) {
            instance = new StatisticsParameters();
        }
        return instance;
    }

    /**
     *
     */
    public StatisticsParameters() {
        name = "Statistics";
        outputVerbosity.setSelectedTag(VERBOSITY_KTH_IT);
        outputTo.setSelectedTag(1);
    }

    /**
     *
     */
    @Override
    public String toString() {
        String ret = "\r\nStatisticsParameter (" + super.toString() + "):\r\nmultiRuns=" + multiRuns
                + ", textOutput=" + textOutput
                + ", verbosity= " + outputVerbosity.getSelectedString()
                + "\nTo " + outputTo.getSelectedString()
                + ", " + BeanInspector.toString(graphSel.getStrings());
        return ret;
    }

    /**
     *
     */
    @Override
    public void saveInstance() {
        try {
            FileOutputStream fileStream = new FileOutputStream("Statistics.ser");
            Serializer.storeObject(fileStream, this);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }
    }

    /**
     *
     */
    private StatisticsParameters(StatisticsParameters statisticsParameters) {
        convergenceRateThreshold = statisticsParameters.convergenceRateThreshold;
        useStatPlot = statisticsParameters.useStatPlot;
        textOutput = statisticsParameters.textOutput;
        multiRuns = statisticsParameters.multiRuns;
        resultFilePrefix = statisticsParameters.resultFilePrefix;
        verbosityK = statisticsParameters.verbosityK;
    }

    /**
     *
     */
    public Object getClone() {
        return new StatisticsParameters(this);
    }

    /**
     *
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     */
    @Override
    public void setMultiRuns(int x) {
        multiRuns = x;
    }

    /**
     *
     */
    @Override
    public int getMultiRuns() {
        return multiRuns;
    }

    /**
     *
     */
    @Override
    public String multiRunsTipText() {
        return "Number of independent optimization runs to evaluate.";
    }

    /**
     * Use averaged graph for multi-run plots or not
     */
    @Override
    public boolean getUseStatPlot() {
        return useStatPlot;
    }

    /**
     * Activate or deactivate averaged graph for multi-run plots
     */
    @Override
    public void setUseStatPlot(boolean x) {
        useStatPlot = x;
    }

    public String useStatPlotTipText() {
        return "Plotting each fitness graph separately if multiruns > 1.";
    }

    /**
     *
     */
    @Override
    public void setResultFilePrefix(String x) {
        if (x == null) {
            resultFilePrefix = "";
        } else {
            resultFilePrefix = x;
        }
    }

    /**
     *
     */
    @Override
    public String getResultFilePrefix() {
        return resultFilePrefix;
    }

    @Override
    public void setShowTextOutput(boolean show) {
        // activate if not activated
        if (show && outputTo.getSelectedTagID() == 0) {
            outputTo.setSelectedTag(2);
        } // deactivate if activated
        else if (!show && outputTo.getSelectedTagID() > 0) {
            outputTo.setSelectedTag(0);
        }
    }

    @Override
    public boolean isShowTextOutput() {
        return outputTo.getSelectedTagID() > 0;
    }

    /**
     * @param x
     */
    @Override
    @Parameter(description = "Provided the optimal fitness is at zero, give the threshold below which it is considered as 'reached'")
    public void setConvergenceRateThreshold(double x) {
        convergenceRateThreshold = x;
    }

    /**
     *
     */
    @Override
    public double getConvergenceRateThreshold() {
        return convergenceRateThreshold;
    }

    @Override
    public boolean isOutputAllFieldsAsText() {
        return showAdditionalProblemInfo;
    }

    @Override
    @Parameter(description = "Output all available data fields or only the selected entries as value.")
    public void setOutputAllFieldsAsText(boolean bShowFullText) {
        showAdditionalProblemInfo = bShowFullText;
    }

    public void hideHideable() {
        setOutputVerbosity(getOutputVerbosity());
    }

    @Override
    public void setOutputVerbosity(SelectedTag sTag) {
        outputVerbosity = sTag;
        GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", sTag.getSelectedTagID() != VERBOSITY_KTH_IT);
    }

    public void setOutputVerbosity(int i) {
        outputVerbosity.setSelectedTag(i);
        GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", outputVerbosity.getSelectedTagID() != VERBOSITY_KTH_IT);
    }

    @Override
    public SelectedTag getOutputVerbosity() {
        return outputVerbosity;
    }

    public String outputVerbosityTipText() {
        return "Set the data output level.";
    }

    @Override
    public int getOutputVerbosityK() {
        return verbosityK;
    }

    @Override
    @Parameter(description = "Set the interval of data output for intermediate verbosity (in generations).")
    public void setOutputVerbosityK(int k) {
        verbosityK = k;
    }

    @Override
    public SelectedTag getOutputTo() {
        return outputTo;
    }

    @Override
    public void setOutputTo(SelectedTag tag) {
        outputTo = tag;
    }

    @Parameter(description = "Set the output destination; to deactivate output, set verbosity to none.")
    public void setOutputTo(int i) {
        outputTo.setSelectedTag(i);
    }


    @Override
    public StringSelection getFieldSelection() {
        return graphSel;
    }

    @Override
    @Parameter(description = "Select the data fields to be collected and plotted. Note that only simple types can be plotted to the GUI.")
    public void setFieldSelection(StringSelection v) {
        graphSel = v;
    }


    /**
     * May be called to dynamically alter the set of graphs that can be
     * selected, using a list of informer instances, which usually are the
     * problem and the optimizer instance.
     *
     * @see InterfaceAdditionalPopulationInformer
     */
    @Override
    public void setInformers(List<InterfaceAdditionalPopulationInformer> informers) {
        ArrayList<String> headerFields = new ArrayList<>();
        ArrayList<String> infoFields = new ArrayList<>();
        // parse list of header elements, show additional Strings according to names.
        for (InterfaceAdditionalPopulationInformer inf : informers) {
            String[] dataHeader = inf.getAdditionalDataHeader();
            headerFields.addAll(Arrays.asList(dataHeader));
            if (infoFields.size() < headerFields.size()) { // add info strings for tool tips - fill up with null if none have been returned.
                String[] infos = inf.getAdditionalDataInfo();
                if (infos != null) {
                    if (infos.length != dataHeader.length) {
                        System.out.println(BeanInspector.toString(infos));
                        System.out.println(BeanInspector.toString(dataHeader));
                        EVAERROR.errorMsgOnce("Warning, mismatching number of headers and additional data fields for " + inf.getClass() + " (" + dataHeader.length + " vs. " + infos.length + ").");
                    }
                    infoFields.addAll(Arrays.asList(infos));
                }
                while (infoFields.size() < headerFields.size()) {
                    infoFields.add(null);
                }
            }
        }
        // create additional fields to be selectable by the user, defined by the informer headers
        StringSelection ss = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings(),
                headerFields, infoFields.toArray(new String[infoFields.size()]));
        ss.takeOverSelection(graphSel);
        // This works!
        setFieldSelection(ss);
    }
}