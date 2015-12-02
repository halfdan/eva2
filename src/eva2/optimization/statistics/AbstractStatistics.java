package eva2.optimization.statistics;

import eva2.gui.BeanInspector;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.Pair;
import eva2.tools.StringSelection;
import eva2.tools.StringTools;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;
import eva2.yaml.BeanSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract class handling statistics. Most important stuff happens in startOptimizationPerformed, stopOptimizationPerformed
 * and createNextGenerationPerformed. Any measures (run based or multi-run based) are reset in startOptimizationPerformed,
 * updated per iteration in createNextGenerationPerformed and reported to listeners in stopOptimizationPerformed.
 * Several different verbosity levels are regarded.
 * The method plotCurrentResults should be implemented to plot further results per iteration.
 * <p>
 * All displayable data is now routed through a single pipeline, which consists in a
 * list of Objects assembled in the getOutputValues method. This allows all simple data types which are
 * provided by the external informer instances to be handled uniformly to the internally collected data, and
 * thus they can be plotted and text-dumped in the same manner.
 * Basic fields are identified by the enum GraphSelectionEnum and are available independently of additional
 * informer instances.
 * <p>
 * Depending on the field selection state and the informers, the list of data fields is dynamically altered,
 * however changes during a multi-run are ignored, since the potential of inconsistencies is too high.
 * <p>
 * Listeners implementing InterfaceTextListener receive String output (human readable).
 * Listeners implementing InterfaceStatisticsListener receive the raw data per iteration.
 */
public abstract class AbstractStatistics implements InterfaceTextListener, InterfaceStatistics {
    private static final Logger LOGGER = Logger.getLogger(AbstractStatistics.class.getName());
    private transient PrintWriter resultOut;
    protected InterfaceStatisticsParameters statisticsParameter;

    /**
     * Keep track of all intermediate fitness values, best, avg. and worst, averaging over all runs
     * for final output, "refining" the multi run data.
     * If the runs have different lengths, the shortest of all defines the length
     * of averaged data to be displayed. This mechanism expects that createNextGenerationPerformed
     * comes in regular intervals (in terms of function calls performed). This needs to be emulated
     * by dynamic population optimizers, also due to the graph output.
     */
    private boolean refineMultiRuns = true;
    //	private ArrayList<double[][]> meanCollection;
    private ArrayList<Object[]> finalObjectData;
    private ArrayList<Double[]> sumDataCollection; // collect summed-up data of multiple runs indexed per iteration
    protected Object[] currentStatObjectData = null; // the raw Object data collected in an iteration
    protected Double[] currentStatDoubleData = null; // the parsed doubles collected in an iteration (or null for complex data fields)
    protected String[] currentStatHeader = null; // the header Strings of the currently provided data
    protected String[] currentStatMetaInfo = null; // meta information on the statistical data
    private Double[] statDataSumOverAll = null;

    // say whether the object should be written to a file every time
    private boolean saveParams = true;
    private boolean firstPlot = true;
    private int iterationCounter = 0;
    // show this many iterations of the averaged performance after a full multi-run
    private int showAvgIntervals = 9;

    // collect data
    protected int functionCalls;
    protected int functionCallSum;
    protected int convergenceCnt;
    protected int feasibleFoundAfter;
    protected int numOfRunsFeasibleFound;
    protected double feasibleFoundAfterSum;
    protected int optRunsPerformed;
    protected double[] currentBestFit;
    protected double[] currentBestFeasibleFit;
    protected double[] currentMeanFit;
    protected double[] currentWorstFit;
    protected double currentAvgEucDistInPop, currentMaxEucDistInPop;
    protected double currentAvgPopDistMetric, currentMaxPopDistMetric;
    protected IndividualInterface bestCurrentIndy, bestOfRunIndy, bestOfRunFeasibleIndy, bestFeasibleAllRuns, bestIndyAllRuns;

    // collect feasible results of a run
    private ArrayList<IndividualInterface> runBestFeasibleList;
    private ArrayList<IndividualInterface> runBestFitList;

    private transient Set<InterfaceTextListener> textListeners;
    private transient Set<InterfaceStatisticsListener> dataListeners = null;

    private List<InterfaceAdditionalPopulationInformer> lastInformerList = null;
    private PopulationInterface lastSols = null;
    private String textFieldDelimiter = " | ";
    private int defaultFitCriterion = 0; // ToDo this might be a user chosen int - or even more elegantly, a MOSOConverter

    protected StringSelection lastFieldSelection = null; // store the graph selection at the beginning of a multi-run
    protected boolean lastIsShowFull = false; // store the "show full text" stats property at the beginning of a multi-run

    public AbstractStatistics() {
        firstPlot = true;
        functionCalls = 0;
        functionCallSum = 0;
        convergenceCnt = 0;
        optRunsPerformed = 0;
        iterationCounter = 0;
        textListeners = new CopyOnWriteArraySet<>();
        dataListeners = new CopyOnWriteArraySet<>();
    }

    @Override
    public void addDataListener(InterfaceStatisticsListener l) {
        dataListeners.add(l);
    }

    @Override
    public boolean removeDataListener(InterfaceStatisticsListener l) {
        return dataListeners.remove(l);
    }

    private void fireDataListeners() {
        if (dataListeners != null) {
            for (InterfaceStatisticsListener l : dataListeners) {
                l.notifyGenerationPerformed(currentStatHeader, currentStatObjectData, currentStatDoubleData);
            }
        }
    }

    private void fireDataListenersFinalize() {
        if (dataListeners != null) {
            LinkedList<InterfaceStatisticsListener> toRemove = new LinkedList<>();
            for (InterfaceStatisticsListener l : dataListeners) {
                boolean rm = l.notifyMultiRunFinished(currentStatHeader, finalObjectData);
                if (rm) {
                    toRemove.add(l);
                }
            }
            for (InterfaceStatisticsListener l : toRemove) {
                dataListeners.remove(l);
            }
        }
    }

    /**
     * Notify listeners on the start and stop of a run.
     *
     * @param runNumber current run (started or stopped)
     * @param normal    in case of stop: the stop was terminated normally (as opposed to manually)
     * @param start     if true, give the start signal, otherwise the stop signal
     */
    private void fireDataListenersStartStop(int runNumber, boolean normal, boolean start) {
        if (dataListeners != null) {
            for (InterfaceStatisticsListener l : dataListeners) {
                if (start) {
                    l.notifyRunStarted(runNumber, statisticsParameter.getMultiRuns(), currentStatHeader, currentStatMetaInfo);
                } else {
                    l.notifyRunStopped(optRunsPerformed, normal);
                    l.finalMultiRunResults(currentStatHeader, finalObjectData);
                }
            }
        }
    }

    @Override
    public void addTextListener(InterfaceTextListener listener) {
        if (!textListeners.contains(listener)) {
            textListeners.add(listener);
        }
    }

    @Override
    public boolean removeTextListener(InterfaceTextListener listener) {
        return textListeners.remove(listener);
    }

    /**
     * Collect start date and time of the run and if indicated, open a file output stream.
     *
     * @param infoString Info string for the optimization run
     */
    protected void initializeOutput(String infoString) {
        String startDate = getDateString();
        // open the result file:
        if (doFileOutput()  // not "text-window only"
                && (statisticsParameter.getOutputVerbosity() != InterfaceStatisticsParameters.OutputVerbosity.NONE)) { // verbosity accordingly high
            String fnameBase = makeOutputFileName(statisticsParameter.getResultFilePrefix(), infoString, startDate);
            int cnt = 0;
            String fname = fnameBase;
            while (new File(fname).exists()) {
                cnt++;
                fname = fnameBase + "." + cnt;
            }
            try {
                resultOut = new PrintWriter(new FileOutputStream(fname));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while opening log file", e);
            }
        } else {
            resultOut = null;
        }
    }

    /**
     * Return a simple String describing the current date and time.
     *
     * @return A string containing current date and time
     */
    public static String getDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        return formatter.format(new Date());
    }

    protected boolean doFileOutput() {
        return (statisticsParameter.getOutputTo() != InterfaceStatisticsParameters.OutputTo.WINDOW);  // not "text-window only"
    }

    private String makeOutputFileName(String prefix, String infoString, String startDate) {
        return (prefix + "_" + infoString).replace(' ', '_') + "_" + startDate + ".log";
    }

    /**
     * If set to true, before every run the parameters will be stored to a file at the start
     * of each run. Default is true.
     *
     * @param doSave Whether to save a serialized version of the optimization parameters or not.
     */
    public void setSaveParams(boolean doSave) {
        saveParams = doSave;
    }

    @Override
    public void startOptimizationPerformed(String infoString, int runNumber, InterfaceOptimizationParameters params, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (printRunIntroVerbosity()) {
            printToTextListener("# Optimization");
        }

        if (runNumber == 0) {
            // store the initial graph selection state, so that modifications during runtime cannot cause inconsistencies
            lastFieldSelection = (StringSelection) statisticsParameter.getFieldSelection().clone();
            lastIsShowFull = statisticsParameter.isOutputAllFieldsAsText();

            currentStatDoubleData = null;
            currentStatObjectData = null;

            List<String> headerFields = getOutputHeaderFieldNames(informerList);
            currentStatHeader = headerFields.toArray(new String[headerFields.size()]);
            currentStatMetaInfo = getOutputMetaInfoAsArray(informerList);

            functionCallSum = 0;
            firstPlot = true;
            optRunsPerformed = 0;
            convergenceCnt = 0;
            if (saveParams) {
                statisticsParameter.saveInstance();
            }
            initializeOutput(infoString);
            bestIndyAllRuns = null;
            bestFeasibleAllRuns = null;
            runBestFeasibleList = new ArrayList<>();
            runBestFitList = new ArrayList<>();
            if (refineMultiRuns) {
                sumDataCollection = new ArrayList<>();
            } else {
                sumDataCollection = null;
            }

            finalObjectData = null;

            statDataSumOverAll = null;
            feasibleFoundAfterSum = -1;
            numOfRunsFeasibleFound = 0;

            if (params != null) {
                if (printRunIntroVerbosity()) {
                    printToTextListener("\n### Optimization parameters \n```\n");
                    printToTextListener(BeanSerializer.serializeObject(params));
                    printToTextListener("\n```\n\n");
                }
            }
            /*
            ToDo: Figure out if we need this. Right now it is just spamming the text output
            if (printRunIntroVerbosity()) {
                printToTextListener("\nStatistics parameters: ");
                printToTextListener(BeanInspector.niceToString(getStatisticsParameters()) + '\n');
            }
            */

        }

        if (printRunIntroVerbosity()) {
            printToTextListener("## Multirun " + (runNumber + 1) + "\n");
        }
        
        feasibleFoundAfter = -1;
        bestCurrentIndy = null;
        bestOfRunIndy = null;
        currentBestFeasibleFit = null;
        bestOfRunFeasibleIndy = null;
        lastInformerList = null;
        lastSols = null;
        iterationCounter = 0;
        functionCalls = 0;
        fireDataListenersStartStop(runNumber, true, true);
    }

    @Override
    public void stopOptimizationPerformed(boolean normal, String stopMessage) {
        if (lastSols == null) {
            LOGGER.warning("WARNING, possibly there was no call to createNextGenerationPerformed before calling stopOptimizationPerformed (AbstractStatistics).");
        }

        if (iterationCounter < sumDataCollection.size()) {
            // no good: later run was shorter than the first one. What to do? Discard the longer one:
            for (int i = sumDataCollection.size() - 1; i >= iterationCounter; i--) {
                sumDataCollection.remove(i);
            }
        }
        optRunsPerformed++;
        functionCallSum += functionCalls;
        if (printRunStoppedVerbosity() && (stopMessage != null)) {
            printToTextListener(" Termination message: " + stopMessage + "\n");
        }
        if (printRunStoppedVerbosity()) {
            printToTextListener(" Function calls run: " + functionCalls + ", sum: " + functionCallSum + "\n");
        }
        // check if target zero was reached
        if (bestCurrentIndy != null) {
            if (Mathematics.norm(bestOfRunIndy.getFitness()) < this.statisticsParameter.getConvergenceRateThreshold()) {
                convergenceCnt++;
            }
            if (printRunStoppedVerbosity()) {
                printIndy("Last best", bestCurrentIndy);
            }
        }
        if (bestOfRunIndy != null) {
            runBestFitList.add(bestOfRunIndy);
            if (printRunStoppedVerbosity()) {
                printIndy("Run best", bestOfRunIndy);
            }
        }
        if (feasibleFoundAfter > 0) {
            if (printRunStoppedVerbosity()) {
                printToTextListener(" Feasible ind. found after " + feasibleFoundAfter + " evaluations.\n");
            }
        } else {
            if (printRunStoppedVerbosity()) {
                printToTextListener(" NO feasible individual found.\n");
            }
        }
        if (printRunStoppedVerbosity()) {
            printToTextListener(" Solution correlations (min,max,avg,med,var): " + BeanInspector.toString(Population.getCorrelations((Population) lastSols)) + "\n");
        }
        if (bestOfRunFeasibleIndy != null) {
            runBestFeasibleList.add(bestOfRunFeasibleIndy);
            if (printRunStoppedVerbosity()) {
                if ((bestOfRunFeasibleIndy instanceof AbstractEAIndividual) && ((AbstractEAIndividual) bestOfRunFeasibleIndy).equalGenotypes((AbstractEAIndividual) bestOfRunIndy)) {
                    printToTextListener("* Run best feasible individual equals best individual.\n");
                } else {
                    if (bestOfRunIndy instanceof AbstractEAIndividual) {
                        if (((AbstractEAIndividual) bestOfRunIndy).violatesConstraint()) {
                            printToTextListener(" Run best individual violates constraints by " + ((AbstractEAIndividual) bestOfRunIndy).getConstraintViolation() + "\n");
                        }
                        if (((AbstractEAIndividual) bestOfRunIndy).isMarkedPenalized()) {
                            printToTextListener(" Run best individual is penalized.\n");
                        }
                    }
                    printIndy("Run best feasible", bestOfRunFeasibleIndy);
                }
            }
        }
        if (finalObjectData == null) {
            finalObjectData = new ArrayList<>();
        }
        finalObjectData.add(currentStatObjectData);

        if (!printRunStoppedVerbosity() && printFinalVerbosity()) {
            printToTextListener(".");
        }
        fireDataListenersStartStop(optRunsPerformed, normal, false);
    }

    @Override
    public void postProcessingPerformed(Population resultPop) { // called from processor
        if (!printRunStoppedVerbosity() && printFinalVerbosity() && optRunsPerformed >= statisticsParameter.getMultiRuns()) {
            printToTextListener("\n");
        }
        if (printRunStoppedVerbosity()) {
            if (resultPop != null && (resultPop.size() > 0)) {
                printToTextListener("### Resulting population \n");
                for (int i = 0; i < resultPop.size(); i++) {
                    printToTextListener(AbstractEAIndividual.getDefaultStringRepresentation(resultPop.getEAIndividual(i)) + "  ");
                    printToTextListener("\n");
                }
            }
        }
        if (optRunsPerformed >= statisticsParameter.getMultiRuns()) {
            finalizeOutput();
            fireDataListenersFinalize();
        }
    }

    private PopulationInterface makeStatsPop() {
        Population pop = new Population(1);

        if (bestIndyAllRuns != null) {
            pop.add(bestIndyAllRuns);
        }
        return pop;
    }

    private void printIndy(String prefix, IndividualInterface indy) {
        printToTextListener("* " + prefix + " ind.: " + BeanInspector.toString(indy) + '\n');
        printToTextListener("         solution data	: " + AbstractEAIndividual.getDefaultDataString(indy) + '\n');
        printToTextListener("         solution fit	: " + BeanInspector.toString(indy.getFitness()));
        if (!(indy instanceof AbstractEAIndividual)) {
            printToTextListener(" - feasibility unknown\n");
        } else {
            if (((AbstractEAIndividual) indy).isMarkedPenalized() || ((AbstractEAIndividual) indy).violatesConstraint()) {
                printToTextListener(" - infeasible\n");
            } else {
                printToTextListener("\n");
            }
        }

    }

    /**
     * Calculate the mean fitness of final best individuals over the last series of multi-runs.
     *
     * @return Mean fitness
     */
    public double[] getMeanBestFitness(boolean requireFeasible) {
        return calculateMeanFitness(requireFeasible ? runBestFeasibleList : runBestFitList);
    }

    /**
     * Calculate the median fitness of final best individuals over the last series of multi-runs.
     *
     * @return Median fitness
     */
    public double[] getMedianBestFitness(boolean requireFeasible) {
        return calculateMedianFitness(requireFeasible ? runBestFeasibleList : runBestFitList);
    }

    protected void finalizeOutput() {
        if (printFinalVerbosity()) {
            printToTextListener("*******\n Runs performed: " + optRunsPerformed + ", reached target " + convergenceCnt + " times with threshold " + statisticsParameter.getConvergenceRateThreshold() + ", rate " + convergenceCnt / (double) statisticsParameter.getMultiRuns() + '\n');
        }
        if (printFinalVerbosity()) {
            printToTextListener(" Average function calls: " + (functionCallSum / optRunsPerformed) + "\n");
        }

        if (printFinalVerbosity() && (feasibleFoundAfterSum >= 0.)) {
            printToTextListener("     Feasible solution found in " + numOfRunsFeasibleFound + " of " + optRunsPerformed + " runs \n");
            printToTextListener("     Average evaluations until feasible ind. was found in " + numOfRunsFeasibleFound + " runs: " + feasibleFoundAfterSum / numOfRunsFeasibleFound + " evaluations\n");
        }

        if (printFinalVerbosity() && (statDataSumOverAll != null)) {
            printToTextListener("     Averaged sum of run statistical data: (" + optRunsPerformed + " runs):");
            for (int i = 0; i < statDataSumOverAll.length; i++) {
                if (statDataSumOverAll[i] != null) {
                    printToTextListener(textFieldDelimiter + (statDataSumOverAll[i] / optRunsPerformed));
                }
            }
            printToTextListener("\n     Averaged last statistical data (" + optRunsPerformed + " runs):");
            Double[] lastSum = sumDataCollection.get(sumDataCollection.size() - 1);
            for (int i = 0; i < lastSum.length; i++) {
                if (lastSum[i] != null) {
                    printToTextListener(textFieldDelimiter + (lastSum[i] / optRunsPerformed));
                }
            }
            printToTextListener("\n");
        }

        if (printFinalVerbosity() && (bestIndyAllRuns != null)) {
            printIndy("Overall best", bestIndyAllRuns);
        }
        if (printFinalVerbosity()) {
            printToTextListener(getFinalAdditionalInfo() + '\n');
        }

        if (optRunsPerformed > 1) {
            if (runBestFitList.size() > 0) {
                if (printFinalVerbosity()) {
                    double[] meanBestFit = getMeanBestFitness(false);
                    printToTextListener(" MultiRun stats: Mean best fitness: " + BeanInspector.toString(meanBestFit) + "\n");
                    if (meanBestFit.length == 1) {
                        printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFitList, meanBestFit[0])) + "\n");
                    }
                    printToTextListener(" MultiRun stats: Median best fitn.: " + BeanInspector.toString(getMedianBestFitness(false)) + "\n");
                }
            }
            if (printFinalVerbosity() && (bestFeasibleAllRuns != null)) {
                printIndy("Overall best feasible", bestFeasibleAllRuns);
            }
            if (runBestFeasibleList.size() > 0) { // always output feasible stats even if they're equal
                if (printFinalVerbosity()) {
                    double[] meanBestFeasibleFit = getMeanBestFitness(true);
                    printToTextListener(" MultiRun stats: Mean best feasible fitness (" + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(meanBestFeasibleFit) + "\n");
                    if (meanBestFeasibleFit.length == 1) {
                        printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFeasibleList, meanBestFeasibleFit[0])) + "\n");
                    }
                    printToTextListener(" MultiRun stats: Median best feasible fitn. (: " + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(getMedianBestFitness(true)) + "\n");
                }
            }
            if (refineMultiRuns && (sumDataCollection != null)) {
                if (printFinalVerbosity()) {
                    printToTextListener(" Averaged performance:\n");
                }
                // the summed-up values of the mean collection is divided by the number of runs
                for (int i = 0; i < sumDataCollection.size(); i++) {
                    divideMean(sumDataCollection.get(i), optRunsPerformed);
                }
                if (printFinalVerbosity()) {
                    printToTextListener(refineToText(sumDataCollection, showAvgIntervals));
                }
            }
            if (printFinalVerbosity() && (finalObjectData != null)) {
                printToTextListener(" Last data line of " + finalObjectData.size() + " multi-runs:\n");
                for (int i = 0; i < finalObjectData.size(); i++) {
                    printToTextListener(BeanInspector.toString(finalObjectData.get(i)));
                    printToTextListener("\n");
                }
            }
        }

        if (resultOut != null) {
            String StopDate = getDateString();
            resultOut.println("StopDate:" + StopDate);
            resultOut.close();
        }
    }

    private String getFinalAdditionalInfo() {
        PopulationInterface bestPop = makeStatsPop();
        String additionalFields = getOutputHeaderFieldNamesAsString(lastInformerList);
        List<Object> vals = getOutputValues(lastInformerList, bestPop);

        StringBuilder sbuf = new StringBuilder("Overall best statistical data: \n");
        sbuf.append(additionalFields);
        sbuf.append('\n');
        sbuf.append(StringTools.concatValues(vals, textFieldDelimiter));
        return sbuf.toString();
    }

    private double[] calcStdDevVar(ArrayList<IndividualInterface> list, double meanFit) {
        double tmp, sum = 0;
        for (IndividualInterface indy : list) {
            tmp = indy.getFitness()[0] - meanFit;
            sum += (tmp * tmp);
        }
        double[] res = new double[2];
        res[0] = sum / list.size();
        res[1] = Math.sqrt(res[0]);
        return res;
    }

    /**
     * Calculate the mean fitness of a list of individuals.
     *
     * @param list List of individuals
     * @return Mean fitness of individuals in list
     */
    public static double[] calculateMeanFitness(List<IndividualInterface> list) {
        double[] sumFit = list.get(0).getFitness().clone();
        for (int i = 1; i < list.size(); i++) {
            Mathematics.vvAdd(sumFit, list.get(i).getFitness(), sumFit);
        }
        Mathematics.svDiv(list.size(), sumFit, sumFit);

        return sumFit;
    }

    public static double[] calculateMedianFitness(List<IndividualInterface> list) {
        ArrayList<double[]> dblAList = new ArrayList<>(list.size());
        for (IndividualInterface indy : list) {
            dblAList.add(indy.getFitness());
        }
        return Mathematics.median(dblAList, false);
    }

    public String refineToText(ArrayList<Double[]> data, int iterationsToShow) {
        String hd = getOutputHeaderFieldNamesAsString(lastInformerList);
        StringBuffer sbuf = new StringBuffer("Iteration");
        sbuf.append(textFieldDelimiter);
        sbuf.append(hd);
        sbuf.append("\n");
        refineToText(data, iterationsToShow, sbuf, textFieldDelimiter);
        return sbuf.toString();
    }

    public static void refineToText(ArrayList<Double[]> data, int iterationsToShow, StringBuffer sbuf, String delim) {
        double step = data.size() / (iterationsToShow - 1.);
        int printedIteration = 0;
        Double[] meanData;
        for (int i = 1; i < data.size() + 1; i++) {
            // print the first, last and intermediate iterations requested by the integer parameter
            // first one is printed always, as printedIteration=0
            if ((i == data.size()) || ((i - 1) == Math.round(printedIteration * step))) {
                printedIteration++;
                meanData = data.get(i - 1);
                sbuf.append(i);
                for (Double value : meanData) {
                    sbuf.append(delim);
                    sbuf.append(BeanInspector.toString(value));
                }
                sbuf.append("\n");
            }
        }
    }


    @Override
    public void printToTextListener(String... s) {
        String text = StringTools.concatFields(s, "");
        if ((resultOut != null)) {
            resultOut.print(text);
        }
        for (InterfaceTextListener l : textListeners) {
            if (statisticsParameter.getOutputTo() != InterfaceStatisticsParameters.OutputTo.FILE) {
                l.print(text);
            }
        }
    }

    @Override
    public void print(String str) {
        printToTextListener(str);
    }

    @Override
    public void println(String str) {
        printToTextListener(str);
        printToTextListener("\n");
    }

    @Override
    public InterfaceStatisticsParameters getStatisticsParameters() {
        return statisticsParameter;
    }

    protected boolean doTextOutput() {
        return (resultOut != null) || (textListeners.size() > 0);
    }

    /**
     * Collect all field names of both internal fields and fields of external informers. Then
     * concatenate them to a string using the textFieldDelimiter of the instance.
     *
     * @param informerList
     * @return
     */
    protected String getOutputHeaderFieldNamesAsString(List<InterfaceAdditionalPopulationInformer> informerList) {
        List<String> headlineFields = getOutputHeaderFieldNames(informerList);
        return StringTools.concatFields(headlineFields, textFieldDelimiter);
    }

    /**
     * Collect meta information on both internal fields and fields of external informers.
     * The length of this list depends on the field selection state.
     *
     * @param informerList
     * @return
     */
    protected List<String> getOutputHeaderFieldNames(List<InterfaceAdditionalPopulationInformer> informerList) {
        ArrayList<String> headlineFields = new ArrayList<>(5);
        headlineFields.addAll(Arrays.asList(getSimpleOutputHeader()));
        if (informerList != null) {
            headlineFields.addAll(getAdditionalHeaderMetaInfo(informerList, null));
        }
        return headlineFields;
    }

    /**
     * Collect all field names of both internal fields and fields of external informers.
     * The length of this list depends on the field selection state.
     *
     * @param informerList
     * @return
     */
    protected List<String> getOutputMetaInfo(List<InterfaceAdditionalPopulationInformer> informerList) {
        ArrayList<String> infoStrings = new ArrayList<>(5);
        ArrayList<String> addStrings = new ArrayList<>(5);
        infoStrings.addAll(Arrays.asList(getSimpleOutputMetaInfo()));
        if (informerList != null) {
            getAdditionalHeaderMetaInfo(informerList, addStrings);
        }
        infoStrings.addAll(addStrings);
        return infoStrings;
    }

    protected String[] getOutputMetaInfoAsArray(List<InterfaceAdditionalPopulationInformer> informerList) {
        List<String> metaStrings = getOutputMetaInfo(informerList);
        return metaStrings.toArray(new String[metaStrings.size()]);
    }

    /**
     * Collect the names of data fields which are collected internally.This must correspond to the
     * method {@link #getSimpleOutputValues()}.
     * Basic fields are identified by the enum GraphSelectionEnum and are available independently of additional
     * informer instances.
     *
     * @return
     * @see #getSimpleOutputValues()
     */
    protected String[] getSimpleOutputHeader() {
        // collect the full header by using the entries of the GraphSelectionEnum
        GraphSelectionEnum[] vals = GraphSelectionEnum.values();
        ArrayList<String> headerEntries = new ArrayList<>();
        headerEntries.add("FunctionCalls");
        for (GraphSelectionEnum val : vals) {
            if (isRequestedField(val)) {
                headerEntries.add(val.toString());
            }
        }
        return headerEntries.toArray(new String[headerEntries.size()]);
    }

    /**
     * Collect the info strings of data fields collected internally. This must correspond to
     * the method {@link #getSimpleOutputValues()}.
     * Basic meta info is defined by the enum GraphSelectionEnum.
     *
     * @return
     * @see #getSimpleOutputValues()
     */
    protected String[] getSimpleOutputMetaInfo() {
        GraphSelectionEnum[] vals = GraphSelectionEnum.values();
        ArrayList<String> headerInfo = new ArrayList<>();
        headerInfo.add("The number of function evaluations");
        for (int i = 0; i < vals.length; i++) {
            if (isRequestedField(vals[i])) {
                headerInfo.add(GraphSelectionEnum.getInfoStrings()[i]);
            }
        }
        return headerInfo.toArray(new String[headerInfo.size()]);
    }

    /**
     * Indicate whether the given statistics data type is requested to
     * be displayed (and thus needs to be calculated).
     *
     * @param graphSelectionEnum
     * @return
     */
    protected boolean isRequestedField(GraphSelectionEnum graphSelectionEnum) {
        return (lastIsShowFull || (lastFieldSelection.isSelected(graphSelectionEnum)));
    }

    /**
     * Indicate whether the given statistics data type is requested to
     * be displayed (and thus needs to be calculated).
     *
     * @param index the index of the field within the string selection object
     * @return
     */
    protected boolean isRequestedField(int index) {
        return (lastIsShowFull || (lastFieldSelection.isSelected(index)));
    }

    /**
     * Indicate whether the given statistics data type is requested to
     * be displayed (and thus needs to be calculated).
     *
     * @param header the header string of the field in question
     * @return
     */
    protected boolean isRequestedAdditionalField(String header) {
        return (lastIsShowFull || (lastFieldSelection.isSelected(header)));
    }

    /**
     * Return all simple data fields collected internally. This must correspond to the
     * method {@link #getSimpleOutputHeader()}.
     *
     * @return
     * @see #getSimpleOutputHeader()
     */
    protected Object[] getSimpleOutputValues() {
        GraphSelectionEnum[] selEnumVals;
        selEnumVals = GraphSelectionEnum.values();
        Object[] ret = new Object[1 + selEnumVals.length];
        ret[0] = functionCalls;
        for (int i = 1; i <= selEnumVals.length; i++) {
            switch (selEnumVals[i - 1]) { // the field i+1 contains enum value i, because field 0 is reserved for the number of function calls
                case currentBest:
                    ret[i] = currentBestFit[defaultFitCriterion];
                    break;
                case meanFit:
                    ret[i] = (currentMeanFit == null) ? Double.NaN : currentMeanFit[defaultFitCriterion];
                    break;
                case currentWorst:
                    ret[i] = (currentWorstFit == null) ? Double.NaN : currentWorstFit[defaultFitCriterion];
                    break;
                case runBest:
                    ret[i] = bestOfRunIndy.getFitness()[defaultFitCriterion];
                    break;
                case currentBestFeasible:
                    ret[i] = (currentBestFeasibleFit == null) ? Double.NaN : currentBestFeasibleFit[defaultFitCriterion];
                    break;
                case runBestFeasible:
                    ret[i] = (bestOfRunFeasibleIndy == null) ? Double.NaN : bestOfRunFeasibleIndy.getFitness()[defaultFitCriterion];
                    break;
                case avgEucPopDistance:
                    ret[i] = currentAvgEucDistInPop;
                    break;
                case maxEucPopDistance:
                    ret[i] = currentMaxEucDistInPop;
                    break;
                case avgPopMetricDist:
                    ret[i] = currentAvgPopDistMetric;
                    break;
                case maxPopMetricDist:
                    ret[i] = currentMaxPopDistMetric;
                    break;
            }
        }
        // all standard fields should be filled now
        return ret;
    }

    /**
     * Assemble a list of data fields which should be traced by the statistics class.
     * Both internal fields as well as external informer data are collected in this list.
     * It should be consistent with the getOutputHeader method, which provides the
     * names of the corresponding fields in the same order.
     * The length of this list depends on the field selection state.
     *
     * @param informerList
     * @param pop
     * @return
     * @see #getOutputHeaderFieldNames(java.util.List) (List)
     */
    protected List<Object> getOutputValues(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
        LinkedList<Object> values = new LinkedList<>();
        values.addAll(Arrays.asList(getSimpleOutputValues()));
        if (informerList != null) {
            for (InterfaceAdditionalPopulationInformer informer : informerList) {
                List<Object> reqList = Arrays.asList(informer.getAdditionalDataValue(pop));
                values.addAll(reqList);
            }
        }
        // remove those which are not requested
        Iterator<Object> iter = values.iterator();
        int cnt = 0;
        iter.next(); // skip the first field (function calls) which is not regarded here
        if (!lastIsShowFull) {
            while (iter.hasNext()) {
                iter.next();
                if (!isRequestedField(cnt++)) {
                    iter.remove();
                }
            }
        }
        return values;
    }

    /**
     * Collect additional info header and (optionally) meta information for the fields selected.
     * The length of this list depends on the field selection state.
     *
     * @param informerList
     * @param metaInfo     if non null, the meta info strings are returned in this list
     * @return
     */
    protected List<String> getAdditionalHeaderMetaInfo(List<InterfaceAdditionalPopulationInformer> informerList, List<String> metaInfo) {
        LinkedList<String> headers = new LinkedList<>();
        if (metaInfo != null && (metaInfo.size() > 0)) {
            System.err.println("Warning, metaInfo list should be empty in AbstractStatistics.getAdditionalHeaderMetaInfo");
        }
        for (InterfaceAdditionalPopulationInformer informer : informerList) {
            headers.addAll(Arrays.asList(informer.getAdditionalDataHeader()));
            if (metaInfo != null) {
                metaInfo.addAll(Arrays.asList(informer.getAdditionalDataInfo()));
            }
        }
        Iterator<String> hIter = headers.iterator();
        Iterator<String> mIter = (metaInfo != null) ? metaInfo.iterator() : null;
        if (!lastIsShowFull) {
            while (hIter.hasNext()) {
                if (mIter != null && mIter.hasNext()) {
                    mIter.next();
                }
                if (!isRequestedAdditionalField(hIter.next())) {
                    hIter.remove();
                    if (mIter != null && mIter.hasNext()) {
                        mIter.remove();
                    }
                }
            }
        }
        return headers;
    }

    /**
     * Take the output values and convert them to a concatenated String and a Double array.
     * The array will have null entries whenever a field contained non-primitive numeric types (such
     * as arrays or other non-numeric data).
     * The string concatenation uses the textFieldDelimiter of the instance.
     *
     * @param informerList
     * @param pop
     * @return
     */
    protected Pair<String, Object[]> getOutputData(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
        List<Object> statValues = getOutputValues(informerList, pop);
        String statValuesString = StringTools.concatValues(statValues, textFieldDelimiter);

        return new Pair<>(statValuesString, statValues.toArray(new Object[statValues.size()]));
    }

    /**
     * Add the given array to the member array. Do some checks etc.
     * If a resultSum array is provided, it is used to add the info and returned. Otherwise
     * a new array is allocated.
     *
     * @param curInfo
     */
    private static Double[] updateSum(Double[] resultSum, Double[] curInfo) {
        if (resultSum == null) {
            resultSum = curInfo.clone();
        } else {
            if (curInfo.length != resultSum.length) {
                System.err.println("Error in AbstractStatistics.updateAdditionalInfo: mismatching info arrays!");
            } else {
                for (int i = 0; i < curInfo.length; i++) {
                    if (resultSum[i] == null || (curInfo[i] == null)) {
                        resultSum[i] = null;
                    } else {
                        resultSum[i] += curInfo[i];
                    }
                }
            }
        }
        return resultSum;
    }

    /**
     * If the population returns a specific data array, this method is called instead of doing standard output
     *
     * @param pop
     * @param informerList
     */
    public abstract void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList);

    protected abstract void plotCurrentResults();

    /**
     * Called at the very first (multirun mode) plot of a fitness curve.
     */
    protected abstract void initializePlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList);

    /**
     * To set a list of informers (even before the actual run is started).
     *
     * @param informerList
     */
    public void setInitialInformerList(List<InterfaceAdditionalPopulationInformer> informerList) {
        lastInformerList = informerList;
    }

    /**
     * Collect statistical data for the given population, such as best individual, best fitness,
     * population measures.
     * This should be called exactly once per generation.
     *
     * @param pop
     */
    private void collectPopData(PopulationInterface pop) {
        bestCurrentIndy = pop.getBestIndividual().getClone();
        if ((bestIndyAllRuns == null) || (secondIsBetter(bestIndyAllRuns, bestCurrentIndy))) {
            bestIndyAllRuns = bestCurrentIndy;
        }
        if ((bestOfRunIndy == null) || (secondIsBetter(bestOfRunIndy, bestCurrentIndy))) {
            bestOfRunIndy = bestCurrentIndy;
        }
        if (bestCurrentIndy == null) {
            System.err.println("createNextGenerationPerformed BestInd==null");
        }
        currentBestFit = bestCurrentIndy.getFitness().clone();
        if (currentBestFit == null) {
            System.err.println("BestFitness==null !");
        }
        if (pop instanceof Population) {
            AbstractEAIndividual curBestFeasible = ((Population) pop).getBestFeasibleIndividual(-1);
            if (curBestFeasible != null) { // a feasible ind. was found!
                if (currentBestFeasibleFit == null) { // feasible indy found for the first time
                    numOfRunsFeasibleFound++;
                    feasibleFoundAfter = pop.getFunctionCalls();
                    if (feasibleFoundAfterSum < 0) {
                        feasibleFoundAfterSum = 0.;
                    } // initial signaling value was -1.
                    feasibleFoundAfterSum += feasibleFoundAfter;
                }
                currentBestFeasibleFit = curBestFeasible.getFitness().clone();
                if ((bestOfRunFeasibleIndy == null) || (secondIsBetter(bestOfRunFeasibleIndy, curBestFeasible))) {
                    bestOfRunFeasibleIndy = (AbstractEAIndividual) curBestFeasible.clone();
                }
                if ((bestFeasibleAllRuns == null) || (secondIsBetter(bestFeasibleAllRuns, bestOfRunFeasibleIndy))) {
                    bestFeasibleAllRuns = bestOfRunFeasibleIndy;
                }
            }
        } else {
            System.err.println("INVALID POPULATION (AbstractStatistics)");
        }

        // collect these data fields only if requested by the user
        if (lastIsShowFull || GraphSelectionEnum.doPlotMean(lastFieldSelection)) {
            currentMeanFit = pop.getMeanFitness().clone();
        } else {
            currentMeanFit = null;
        }
        if (lastIsShowFull || GraphSelectionEnum.doPlotWorst(lastFieldSelection)) {
            currentWorstFit = pop.getWorstIndividual().getFitness().clone();
        } else {
            currentWorstFit = null;
        }

        functionCalls = pop.getFunctionCalls();

        if (lastIsShowFull || GraphSelectionEnum.doPlotAvgEucDist(lastFieldSelection)
                || GraphSelectionEnum.doPlotMaxEucDist(lastFieldSelection)) {
            double[] measures = ((Population) pop).getPopulationMeasures(null);
            if (measures != null) {
                currentAvgEucDistInPop = measures[0];
                currentMaxEucDistInPop = measures[2];
            }
        }

        if (lastIsShowFull || GraphSelectionEnum.doPlotAvgPopMetricDist(lastFieldSelection)
                || GraphSelectionEnum.doPlotMaxPopMetricDist(lastFieldSelection)) {
            double[] measures = pop.getPopulationMeasures();
            if (measures != null) {
                currentAvgPopDistMetric = measures[0];
                currentMaxPopDistMetric = measures[2];
            }
        }
    }

    public String[] getCurrentFieldHeaders() {
        StringSelection fSel = statisticsParameter.getFieldSelection();
        return fSel.getSelected();
    }

    /**
     * Do some data collection on the population.
     */
    @Override
    public synchronized void createNextGenerationPerformed(PopulationInterface pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList) {
        lastInformerList = informerList;

        if (resultOut != null) {
            resultOut.flush();
        }
        if (firstPlot) {
            initializePlots(pop, informerList);
            firstPlot = false;
            currentBestFeasibleFit = null;
        }

        if (pop.getSpecificData() != null) { // this is more or less deprecated. the standard population implementation will always return null. However the ES module wont
            plotSpecificData(pop, informerList);
            return;
        }

        collectPopData(pop);

        if (iterationCounter == 0) {
            String headerLine = StringTools.concatFields(currentStatHeader, textFieldDelimiter);
            if (printHeaderByVerbosity()) {
                printToTextListener("| " + headerLine + " | \n");
                String[] tableSeparator = new String[currentStatHeader.length];
                Arrays.fill(tableSeparator, "---");
                printToTextListener("| " + StringTools.concatFields(tableSeparator, textFieldDelimiter) + " | \n");
            }
        }

        lastSols = (opt != null) ? new Population(opt.getAllSolutions().getSolutions()) : pop;
        Pair<String, Object[]> addData = getOutputData(informerList, lastSols);
        if (doTextOutput()) { // this is where the text output is actually written
            if (printLineByVerbosity(iterationCounter)) {
                printToTextListener("| " + addData.head() + " | \n");
            }
        }
        currentStatObjectData = addData.tail();
        currentStatDoubleData = ToolBox.parseDoubles(currentStatObjectData);
        if (currentStatObjectData != null) {
            statDataSumOverAll = updateSum(statDataSumOverAll, currentStatDoubleData); // this adds up all data of a single run
        } else {
            System.err.println("Warning in AbstractStatistics!");
        }

        if (sumDataCollection != null) {
            // Collect average data
            Double[] sumDataEntry = null;
            if ((optRunsPerformed == 0) && (sumDataCollection.size() <= iterationCounter)) {
                // in the first run, newly allocate the arrays
                // assume that all later data sets will have the same format
                sumDataEntry = currentStatDoubleData.clone();
                sumDataCollection.add(sumDataEntry);
            } else {
                if (sumDataCollection.size() <= iterationCounter) {// bad case!
                    // may happen for dynamic pop-sizes, e.g. in Tribes, when runs do not necessarily send the
                    // "generation performed" event the same number of times.
                    // thus: dont do an update for events that are "too late"
                    sumDataEntry = null;
                } else {
                    sumDataEntry = sumDataCollection.get(iterationCounter);
                }
                if (sumDataEntry != null) {
                    updateSum(sumDataEntry, currentStatDoubleData);
                } // this adds up data of a single iteration across multiple runs
            }
        }
        plotCurrentResults();
        fireDataListeners();
        if (resultOut != null) {
            resultOut.flush();
        }
        iterationCounter++;
    }

    /**
     * Returns true if the given iteration is a verbose one according to StatsParameter - meaning
     * that full iteration data should be plotted.
     *
     * @param iteration Iteration number
     * @return true if current iteration is verbose
     */
    private boolean printLineByVerbosity(int iteration) {
        return (statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.ALL)
                || ((statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.KTH_IT)
                && (isKthRun(iteration, statisticsParameter.getOutputVerbosityK())));
    }

    private boolean printRunIntroVerbosity() {
        return (statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.ALL) ||
                (statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.KTH_IT)
                || (optRunsPerformed == 0 && (statisticsParameter.getOutputVerbosity() != InterfaceStatisticsParameters.OutputVerbosity.NONE));
    }

    private boolean printRunStoppedVerbosity() {
        return statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.KTH_IT ||
               statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.ALL;
    }

    private boolean printFinalVerbosity() {
        return (statisticsParameter.getOutputVerbosity() != InterfaceStatisticsParameters.OutputVerbosity.NONE);
    }

    private boolean isKthRun(int i, int k) {
        // ingeniously shifting i by two since the stats counter starts at 0
        // after two evaluations have already happened: initialization and first optimization
        // this allows the last iteration to be within the displayed set if k is a divisor of whole iterations as expected
        if ((i == 0) || (k == 0)) {
            return true;
        } else {
            if (i <= 2) {
                return (i % k) == 0;
            } // show more at the beginning (always first time)
            else {
                return ((i + 2) % k) == 0;
            }
        }
    }

    private boolean printHeaderByVerbosity() {
        return (statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.ALL) ||
                (statisticsParameter.getOutputVerbosity() == InterfaceStatisticsParameters.OutputVerbosity.KTH_IT);
    }

    private static void divideMean(Double[] mean, double d) {
        for (int j = 0; j < mean.length; j++) {
            if (mean[j] != null) {
                mean[j] /= d;
            }
        }
    }

    /**
     * Compare two individual interfaces and return true if the second one is dominant.
     *
     * @param indy1 First individual
     * @param indy2 Second individual
     * @return true if the second individual is dominant, else false
     */
    public static boolean secondIsBetter(IndividualInterface indy1, IndividualInterface indy2) {
        if (indy1 == null) {
            return true;
        }
        if (indy2 == null) {
            return false;
        }
        if (indy1 instanceof AbstractEAIndividual) {
            return ((AbstractEAIndividual) indy2).isDominatingDebConstraints((AbstractEAIndividual) indy1);
        }
        return indy2.isDominant(indy1);
    }

    @Override
    public double[] getBestFitness() {
        return currentBestFit;
    }

    @Override
    public IndividualInterface getBestSolution() {
        return bestIndyAllRuns;
    }

    @Override
    public IndividualInterface getRunBestSolution() {
        return bestOfRunIndy;
    }

    public int getFitnessCalls() {
        return functionCalls;
    }
}
