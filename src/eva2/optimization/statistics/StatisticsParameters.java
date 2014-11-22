package eva2.optimization.statistics;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.InterfaceNotifyOnInformers;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.EVAERROR;
import eva2.tools.StringSelection;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;
import eva2.util.annotation.Parameter;
import eva2.yaml.BeanSerializer;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
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

    private OutputVerbosity outputVerbosity;
    private OutputTo outputTo;
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
            return getInstance("Statistics.yml");
        } else {
            return new StatisticsParameters();
        }
    }

    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static StatisticsParameters getInstance(String yamlFile) {
        StatisticsParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(yamlFile);
            instance = (StatisticsParameters) new Yaml().load(fileStream);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not load Statistics.yml.", ex);
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

        outputVerbosity = OutputVerbosity.KTH_IT;
        outputTo = OutputTo.WINDOW;
    }

    /**
     *
     */
    @Override
    public String toString() {
        String ret = "\r\nStatisticsParameter (" + super.toString() + "):\r\nmultiRuns=" + multiRuns
                + ", textOutput=" + textOutput
                + ", verbosity= " + outputVerbosity
                + "\nTo " + outputTo
                + ", " + BeanInspector.toString(graphSel.getStrings());
        return ret;
    }

    /**
     *
     */
    @Override
    public void saveInstance() {
        try {
            FileOutputStream fileStream = new FileOutputStream("Statistics.yml");
            String yaml = BeanSerializer.serializeObject(this);
            fileStream.write(yaml.getBytes());
            fileStream.close();
        } catch (IOException ex) {
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
    @Parameter(name = "runs", description = "Number of independent optimization runs to evaluate.")
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
    @Parameter(description = "Plotting each fitness graph separately if multiruns > 1.")
    public void setUseStatPlot(boolean x) {
        useStatPlot = x;
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
    @Hidden
    public void setShowTextOutput(boolean show) {
        // activate if not activated
        if (show && outputTo == OutputTo.FILE) {
            outputTo = OutputTo.BOTH;
        } // deactivate if activated
        else if (!show) {
            outputTo = OutputTo.FILE;
        }
    }

    @Override
    public boolean isShowTextOutput() {
        return outputTo == OutputTo.WINDOW || outputTo == OutputTo.BOTH;
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
    @Parameter(description = "Set the data output level.")
    public void setOutputVerbosity(OutputVerbosity sTag) {
        outputVerbosity = sTag;
        GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", sTag != OutputVerbosity.KTH_IT);
    }

    @Override
    public OutputVerbosity getOutputVerbosity() {
        return outputVerbosity;
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
    public OutputTo getOutputTo() {
        return outputTo;
    }

    @Override
    @Parameter(description = "Set the output destination; to deactivate output, set verbosity to none.")
    public void setOutputTo(OutputTo tag) {
        outputTo = tag;
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