package eva2;

import eva2.optimization.OptimizationStateListener;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.individuals.InterfaceDataTypeInteger;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.modules.Processor;
import eva2.optimization.operator.postprocess.InterfacePostProcessParams;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.stat.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.BitSet;


/**
 * This Runnable class just encapsulates the Processor class with some simple ways to access a solution.
 *
 * @author mkron
 */
public class OptimizerRunnable implements Runnable {
    private Processor proc;
    private boolean isFinished = false;
    private boolean doRestart = false; // indicate whether start or restart should be done --> whether pop will be reinitialized.
    private boolean postProcessOnly = false;
    private InterfaceTextListener listener = null;
    private String ident = "OptimizerRunnable";
    private static int cntID = 0;
    private int rnblID = -1;

    /**
     * Construct an OptimizerRunnable with given parameters and a StatisticsStandalone instance without restart,
     * meaning that the population will be initialized randomly.
     *
     * @param params
     * @param outputFilePrefix
     */
    public OptimizerRunnable(InterfaceOptimizationParameters params, String outputFilePrefix) {
        this(params, outputFilePrefix, false);
    }

    /**
     * This constructor assumes that DummyStatistics are enough. This saves time e.g. for small populations.
     * If restart is true, the processor will not reinitialize the population allowing search on predefined populations.
     *
     * @param params
     * @param restart
     */
    public OptimizerRunnable(InterfaceOptimizationParameters params, boolean restart) {
        this(params, new StatisticsDummy(), restart);
    }

    /**
     * Construct an OptimizerRunnable with given parameters and a StatisticsStandalone instance with optional restart.
     * If restart is true, the processor will not reinitialize the population allowing search on predefined populations.
     * The outputFilePrefix may be null.
     *
     * @param params
     * @param outputFilePrefix
     * @param restart
     */
    public OptimizerRunnable(InterfaceOptimizationParameters params, String outputFilePrefix, boolean restart) {
        this(params, new StatisticsStandalone(outputFilePrefix), restart);
    }

    /**
     * Construct an OptimizerRunnable with given parameters and statistics instance with optional restart.
     * If restart is true, the processor will not reinitialize the population allowing search on predefined populations.
     *
     * @param params
     * @param stats
     * @param restart
     */
    public OptimizerRunnable(InterfaceOptimizationParameters params, InterfaceStatistics stats, boolean restart) {
        rnblID = cntID;
        cntID++;

        proc = new Processor(stats, params);
        if (proc.getStatistics() instanceof AbstractStatistics) {
            ((AbstractStatistics) proc.getStatistics()).setSaveParams(false);
        }
        doRestart = restart;
    }

    public void setIdentifier(String id) {
        ident = id;
    }

    public String getIdentifier() {
        return ident;
    }

    /**
     * A unique ID for the runnable.
     *
     * @return
     */
    public int getID() {
        return rnblID;
    }

    public InterfaceOptimizationParameters getOptimizationParameters() {
        return proc.getOptimizationParameters();
    }

    public InterfaceStatistics getStats() {
        return proc.getStatistics();
    }

    public void setStatistics(InterfaceStatistics stats) {
        if (proc.isOptimizationRunning()) {
            throw new RuntimeException("Error - cannot change statistics instance during optimization.");
        }
        InterfaceOptimizationParameters params = proc.getOptimizationParameters();
        proc = new Processor(stats, params);
        if (proc.getStatistics() instanceof AbstractStatistics) {
            ((AbstractStatistics) proc.getStatistics()).setSaveParams(false);
        }
    }

    public void setTextListener(InterfaceTextListener lsnr) {
        proc.getStatistics().removeTextListener(listener);
        this.listener = lsnr;
        if (listener != null) {
            proc.getStatistics().addTextListener(listener);
        }
    }

    public void addOptimizationStateListener(OptimizationStateListener rsl) {
        if (proc != null) {
            proc.addListener(rsl);
        }
    }

    public void setDoRestart(boolean restart) {
        doRestart = restart;
    }

    @Override
    public void run() {
        isFinished = false;
        try {
            proc.setSaveParams(false);
            if (postProcessOnly) {
                proc.performPostProcessing((PostProcessParams) proc.getOptimizationParameters().getPostProcessParams(), listener);
            } else {
                if (doRestart) {
                    proc.restartOptimization();
                } else {
                    proc.startOptimization();
                }
                proc.runOptOnce();
            }
        } catch (Exception e) {
            proc.getStatistics().printToTextListener("Exception in OptimizeThread::run: " + e.getMessage() + "\n");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            proc.getStatistics().printToTextListener(sw.toString());
        }
        isFinished = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void setDoPostProcessOnly(boolean poPO) {
        postProcessOnly = poPO;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean wasAborted() {
        return (proc != null && (proc.wasAborted()));
    }

    public void restartOpt() {
        proc.restartOptimization();
    }

    public void stopOpt() {
        proc.stopOptimization();
    }

    public IndividualInterface getResult() {
        return proc.getStatistics().getBestSolution();
    }

    public Population getResultPopulation() {
        return proc.getResultPopulation();
    }

    public SolutionSet getSolutionSet() {
        return (SolutionSet) proc.getOptimizationParameters().getOptimizer().getAllSolutions();
    }

    public void setPostProcessingParams(InterfacePostProcessParams ppp) {
        proc.getOptimizationParameters().setPostProcessParams(ppp);
    }

    public int getProgress() {
        return proc.getOptimizationParameters().getOptimizer().getPopulation().getFunctionCalls();
    }

    public String terminatedBecause() {
        if (isFinished) {
            if (postProcessOnly) {
                return "Post processing finished";
            } else {
                InterfaceTerminator term = proc.getOptimizationParameters().getTerminator();
                return term.lastTerminationMessage();
            }
        } else {
            return "Not yet terminated";
        }
    }

    public double[]  getDoubleSolution() {
        IndividualInterface indy = getResult();
        if (indy instanceof InterfaceDataTypeDouble) {
            return ((InterfaceDataTypeDouble) indy).getDoubleData();
        } else {
            return null;
        }
    }

    public BitSet getBinarySolution() {
        IndividualInterface indy = getResult();
        if (indy instanceof InterfaceDataTypeBinary) {
            return ((InterfaceDataTypeBinary) indy).getBinaryData();
        } else {
            return null;
        }
    }

    public int[] getIntegerSolution() {
        IndividualInterface indy = getResult();
        if (indy instanceof InterfaceDataTypeInteger) {
            return ((InterfaceDataTypeInteger) indy).getIntegerData();
        } else {
            return null;
        }
    }

    /**
     * Set the verbosity level in the statistics module to the given value.
     *
     * @param vLev
     */
    public void setVerbosityLevel(int vLev) {
        if (vLev >= 0 && vLev < proc.getStatistics().getStatisticsParameter().getOutputVerbosity().getTags().length) {
            proc.getStatistics().getStatisticsParameter().getOutputVerbosity().setSelectedTag(vLev);
        } else {
            System.err.println("Invalid verbosity leveln in OptimizerRunnable.setVerbosityLevel!");
        }
    }

    /**
     * Set the output direction in the statistics module.
     *
     * @param outp
     */
    public void setOutputTo(int outp) {
        ((StatisticsParameter) proc.getStatistics().getStatisticsParameter()).setOutputTo(outp);
    }

    /**
     * Set the number of multiruns in the statistics module.
     *
     * @param multis
     */
    public void setMultiRuns(int multis) {
        proc.getStatistics().getStatisticsParameter().setMultiRuns(multis);
    }

    /**
     * Indicate whether full stats should be printed as text (or only selected entries).
     *
     * @param addInfo
     */
    public void setOutputFullStatsToText(boolean addInfo) {
        proc.getStatistics().getStatisticsParameter().setOutputAllFieldsAsText(addInfo);
    }
}