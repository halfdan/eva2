package eva2.optimization.modules;

import eva2.gui.BeanInspector;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.go.InterfaceNotifyOnInformers;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.paramcontrol.ConstantParameters;
import eva2.optimization.operator.paramcontrol.InterfaceParameterControl;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.GenerationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatistics;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.statistics.StatisticsWithGUI;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.StringTools;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Processor may run as a thread permanently (GenericModuleAdapter) and is
 * then stopped and started by a switch in startOptimization/stopOptimization.
 * <p>
 * Processor also handles adaptive parameter control by checking for the method
 * getParamControl in (so far) Optimizer and Problem instances. The return-value
 * may be InterfaceParameterControl or an array of Objects. If it is a control
 * interface, it is applied to the instance that returned it directly. For
 * arrays of objects each array entry is again handled by checking for
 * getParamControl, thus recursive controllable structures are possible.
 */
public class Processor extends Thread implements InterfaceProcessor, InterfacePopulationChangedEventListener {

    private static final Logger LOGGER = Logger.getLogger(Processor.class.getName());
    private volatile boolean isOptimizationRunning;
    private InterfaceStatistics statistics;
    private InterfaceOptimizationParameters optimizationParameters;
    private boolean createInitialPopulations = true;
    private boolean saveParams = true;
    private OptimizationStateListener optimizationStateListener;
    private boolean wasRestarted = false;
    private int runCounter = 0;
    private Population resultPopulation = null;
    private boolean userAborted = false;

    @Override
    public void addListener(OptimizationStateListener module) {
        LOGGER.log(Level.FINEST, "Processor: setting module as listener: " + ((module == null) ? "null" : module.toString()));
        optimizationStateListener = module;
    }


    /**
     * Construct a Processor instance and make statistics instance informable of
     * the parameters, such they can by dynamically show additional information.
     *
     * @see InterfaceNotifyOnInformers
     */
    public Processor(InterfaceStatistics statistics, InterfaceOptimizationParameters optimizationParameters) {
        this.optimizationParameters = optimizationParameters;
        this.statistics = statistics;

        // the statistics want to be informed if the strategy or the optimizer (which provide statistical data as InterfaceAdditionalInformer) change.
        if (statistics != null && (optimizationParameters != null)) {
            if (statistics.getStatisticsParameters() instanceof InterfaceNotifyOnInformers) {
                // 	addition for the statistics revision with selectable strings - make sure the go parameters are represented within the statistics
                optimizationParameters.addInformableInstance((InterfaceNotifyOnInformers) (statistics.getStatisticsParameters()));
            }
        }
    }

    public boolean isOptimizationRunning() {
        return isOptimizationRunning;
    }

    protected void setOptimizationRunning(boolean bRun) {
        isOptimizationRunning = bRun;
    }

    /**
     * If set to true, before every run the parameters will be stored to a file.
     *
     * @param doSave
     */
    public void setSaveParams(boolean doSave) {
        saveParams = doSave;
    }

    /**
     *
     */
    @Override
    public void startOptimization() {
        createInitialPopulations = true;
        if (isOptimizationRunning()) {
            LOGGER.log(Level.WARNING, "Processor is already running.");
            return;
        }
        resultPopulation = null;
        userAborted = false;
        wasRestarted = false;
        setOptimizationRunning(true);
    }

    /**
     * Return true if the optimization was stopped by the user instead of the
     * termination criterion.
     *
     * @return
     */
    public boolean wasAborted() {
        return userAborted;
    }

    /**
     *
     */
    @Override
    public void restartOptimization() {
        createInitialPopulations = false;
        if (isOptimizationRunning()) {
            LOGGER.log(Level.WARNING, "Processor is already running.");
            return;
        }
        userAborted = false;
        wasRestarted = true;
        setOptimizationRunning(true);
    }

    /**
     *
     */
    @Override
    public void stopOptimization() { // this means user break
        setOptimizationRunning(false);
    }

    /**
     *
     */
    @Override
    public void run() {
        this.setPriority(1);
        while (true) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                System.err.println("There was an error in sleep Processor.run()" + e);
            }
            runOptimizationOnce();
        }
    }

    public Population runOptimizationOnce() {
        try {
            EVAERROR.clearMsgCache();
            this.setName(getInfoString());
            if (isOptimizationRunning() && saveParams) {
                try {
                    optimizationParameters.saveInstance();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not save optimization instance!", e);
                }
            }

            while (isOptimizationRunning()) {
                setPriority(3);
                resultPopulation = this.optimize();
                setPriority(1);
            }
        } catch (Exception e) {
            String errMsg = e.toString();
            if (errMsg.length() == 0) {
                errMsg = "check console output for error messages.";
            }
            errMsg = "Exception in Processor: " + errMsg;
            LOGGER.log(Level.SEVERE, errMsg, e);
            e.printStackTrace();
            try {
                JOptionPane.showMessageDialog(null, StringTools.wrapLine(errMsg, 60, 0.2), "Error in Optimization", JOptionPane.ERROR_MESSAGE);
            } catch (Exception | Error ignored) {
            }
            setOptimizationRunning(false); // normal finish
            if (optimizationStateListener != null) {
                optimizationStateListener.performedStop(); // is only needed in client server mode
                optimizationStateListener.updateProgress(0, errMsg);
            }
        } finally {
            this.setName("Optimization Processor");
        }
        return resultPopulation;
    }

    /**
     * Main optimization loop. Return a population containing the solutions of
     * the last run if there were multiple.
     */
    protected Population optimize() {
        Population resultPop = null;

        if (!isOptimizationRunning()) {
            LOGGER.warning("Was startOptimization already called?");
            setOptimizationRunning(true);
        }

        RNG.setRandomSeed(optimizationParameters.getRandomSeed());

        if (optimizationStateListener != null) {
            if (wasRestarted) {
                optimizationStateListener.performedRestart(getInfoString());
            } else {
                optimizationStateListener.performedStart(getInfoString());
            }
        }

        runCounter = 0;

        InterfaceTerminator terminator = this.optimizationParameters.getTerminator();
        InterfaceOptimizer optimizer = this.optimizationParameters.getOptimizer();
        InterfaceOptimizationProblem problem = this.optimizationParameters.getProblem();
        optimizer.addPopulationChangedEventListener(this);
        /**
         * We keep the optimization running until it is aborted by the user or
         * the number of multiple runs has been reached.
         */
        while (isOptimizationRunning() && (runCounter < statistics.getStatisticsParameters().getMultiRuns())) {
            LOGGER.info(String.format("Starting Optimization %d/%d", runCounter + 1, statistics.getStatisticsParameters().getMultiRuns()));
            statistics.startOptimizationPerformed(getInfoString(), runCounter, optimizationParameters, getInformerList());

            problem.initializeProblem();
            optimizer.setProblem(problem);
            terminator.initialize(problem);

            maybeInitParamCtrl(optimizationParameters);
            if (this.createInitialPopulations) {
                optimizer.initialize();
            }

            if (optimizationStateListener != null) {
                optimizationStateListener.updateProgress(getStatusPercent(optimizer.getPopulation(), runCounter, statistics.getStatisticsParameters().getMultiRuns()), null);
            }

            /**
             * This is the main optimization loop. We keep calling
             * optimize() until a termination criterion is met or
             * the user aborts the optimization manually.
             */
            do {
                maybeUpdateParamCtrl(optimizationParameters);
                optimizer.optimize();
            } while (isOptimizationRunning() && !terminator.isTerminated(optimizer.getAllSolutions()));

            runCounter++;
            maybeFinishParamCtrl(optimizationParameters);
            userAborted = !isOptimizationRunning(); // stop is "normal" if opt wasnt set false by the user (and thus still true)
            //////////////// Default stats
            statistics.stopOptimizationPerformed(!userAborted, terminator.lastTerminationMessage()); // stop is "normal" if opt wasnt set false by the user (and thus still true)

            //////////////// PP or set results without further PP
            if (!userAborted) {
                resultPop = performPostProcessing();
                if (resultPop == null) { // post processing disabled, so use opt. solutions
                    resultPop = optimizer.getAllSolutions().getSolutions();
                }
            } else {
                resultPop = optimizer.getAllSolutions().getSolutions();
            }
            statistics.postProcessingPerformed(resultPop);

        }
        setOptimizationRunning(false); // normal finish
        if (optimizationStateListener != null) {
            optimizationStateListener.performedStop(); // is only needed in client server mode
        }
        if (optimizationStateListener != null) {
            optimizationStateListener.updateProgress(0, null);
        }
        optimizer.removePopulationChangedEventListener(this);
        return resultPop;
    }

    private void iterateParamCtrl(Object instance, String methodName, Object[] args) {
        Object paramCtrlReturn = null;
        if (null != (paramCtrlReturn = BeanInspector.callIfAvailable(instance, "getParamControl", null))) {
            if (paramCtrlReturn instanceof Object[]) {
                Object[] controllersOrSubControllables = (Object[]) paramCtrlReturn;
                for (Object controllerOrSubControllable : controllersOrSubControllables) {
                    // The returned array may contain (i) InterfaceParameterControl associated with the instance
                    // itself or (ii) sub-instances which have their own parameter controls. On these, the method
                    // is called recursively.
                    if (controllerOrSubControllable instanceof InterfaceParameterControl) {
                        args[0] = instance;
                        if (!(controllerOrSubControllable instanceof ConstantParameters)) {
                            BeanInspector.callIfAvailable(controllerOrSubControllable, methodName, args);
                        }
                    } else {
                        args[0] = controllerOrSubControllable;
                        iterateParamCtrl(controllerOrSubControllable, methodName, args);
                    }
                }
            } else if (paramCtrlReturn instanceof InterfaceParameterControl) {
                if (!(paramCtrlReturn instanceof ConstantParameters)) {
                    BeanInspector.callIfAvailable(paramCtrlReturn, methodName, args);
                }
            }
        }
    }

    private void maybeInitParamCtrl(InterfaceOptimizationParameters goParams) {
        iterateParamCtrl(goParams.getOptimizer(), "initialize", new Object[]{goParams.getOptimizer(), goParams.getOptimizer().getPopulation()});
        iterateParamCtrl(goParams.getProblem(), "initialize", new Object[]{goParams.getProblem(), goParams.getOptimizer().getPopulation()});
    }

    private void maybeFinishParamCtrl(InterfaceOptimizationParameters goParams) {
        iterateParamCtrl(goParams.getOptimizer(), "finish", new Object[]{goParams.getOptimizer(), goParams.getOptimizer().getPopulation()});
        iterateParamCtrl(goParams.getProblem(), "finish", new Object[]{goParams.getProblem(), goParams.getOptimizer().getPopulation()});
    }

    private void maybeUpdateParamCtrl(InterfaceOptimizationParameters goParams) {
        Object[] args;
        InterfaceTerminator terminator = goParams.getTerminator();
        InterfaceOptimizer optimizer = goParams.getOptimizer();
        if (terminator instanceof GenerationTerminator) {
            args = new Object[]{optimizer, optimizer.getPopulation(), optimizer.getPopulation().getGeneration(), ((GenerationTerminator) terminator).getGenerations()};
        } else if (terminator instanceof EvaluationTerminator) {
            args = new Object[]{optimizer, optimizer.getPopulation(), optimizer.getPopulation().getFunctionCalls(), ((EvaluationTerminator) terminator).getFitnessCalls()};
        } else {
            args = new Object[]{optimizer};
        }

        // only if iteration counting is available
        iterateParamCtrl(optimizer, "updateParameters", args);
        args[0] = goParams.getProblem();
        iterateParamCtrl(goParams.getProblem(), "updateParameters", args);
    }

    /**
     * Calculate the percentage of current (multi-)run already performed, based
     * on evaluations/generations for the
     * EvaluationTerminator/GenerationTerminator or multi-runs only.
     *
     * @param pop
     * @param currentRun
     * @param multiRuns
     * @return the percentage of current (multi-)run already performed
     */
    private int getStatusPercent(Population pop, int currentRun, int multiRuns) {
        double percentPerRun = 100. / multiRuns;
        int curProgress;
        if (this.optimizationParameters.getTerminator() instanceof EvaluationTerminator) {
            double curRunPerf = pop.getFunctionCalls() * percentPerRun / (double) ((EvaluationTerminator) this.optimizationParameters.getTerminator()).getFitnessCalls();
            curProgress = (int) (currentRun * percentPerRun + curRunPerf);
        } else if (this.optimizationParameters.getTerminator() instanceof GenerationTerminator) {
            double curRunPerf = pop.getGeneration() * percentPerRun / (double) ((GenerationTerminator) this.optimizationParameters.getTerminator()).getGenerations();
            curProgress = (int) (currentRun * percentPerRun + curRunPerf);
        } else {
            curProgress = (int) (currentRun * percentPerRun);
        }
        return curProgress;
    }

    /**
     * This method allows an optimizer to register a change in the optimizer.
     * Send some information to the statistics module and update the progress.
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals(Population.NEXT_GENERATION_PERFORMED)) {
            statistics.createNextGenerationPerformed(
                    this.optimizationParameters.getOptimizer().getPopulation(),
                    this.optimizationParameters.getOptimizer(),
                    getInformerList());
            if (optimizationStateListener != null) {
                optimizationStateListener.updateProgress(
                        getStatusPercent(
                                optimizationParameters.getOptimizer().getPopulation(),
                                runCounter,
                                statistics.getStatisticsParameters().getMultiRuns()),
                        null);
            }
        }
    }

    protected List<InterfaceAdditionalPopulationInformer> getInformerList() {
        List<InterfaceAdditionalPopulationInformer> informerList = new ArrayList<>(2);
        informerList.add(this.optimizationParameters.getProblem());
        if (this.optimizationParameters.getOptimizer() instanceof InterfaceAdditionalPopulationInformer) {
            informerList.add((InterfaceAdditionalPopulationInformer) this.optimizationParameters.getOptimizer());
        }
        return informerList;
    }

    @Override
    public String getInfoString() {
        InterfaceOptimizationProblem problem = this.optimizationParameters.getProblem();

        return problem.getName() + "{" + problem.getProblemDimension() + "}+" + this.optimizationParameters.getOptimizer().getName();
    }

    /**
     * This method return the Statistics object.
     */
    public InterfaceStatistics getStatistics() {
        return statistics;
    }

    /**
     * These methods allow you to get and set the Module Parameters.
     */
    public InterfaceOptimizationParameters getOptimizationParameters() {
        return optimizationParameters;
    }

    public void setOptimizationParameters(InterfaceOptimizationParameters params) {
        if (params != null) {
            optimizationParameters = params;
        } else {
            System.err.println("Setting parameters failed (parameters were null) (Processor.setOptimizationParameters)");
        }
    }

    /**
     * Return the last solution population or null if there is none available.
     *
     * @return the last solution population or null
     */
    public Population getResultPopulation() {
        return resultPopulation;
    }

    public Population performPostProcessing() {
        PostProcessParams ppp = (PostProcessParams)optimizationParameters.getPostProcessParams();
        if (ppp.isDoPostProcessing()) {
            return performPostProcessing(ppp, (InterfaceTextListener) statistics);
        } else {
            return null;
        }
    }

    /**
     * Perform a post processing step with given parameters, based on all
     * solutions found by the optimizer. Use getResultPopulation() to retrieve
     * results.
     *
     * @param ppp
     * @param listener
     */
    public Population performPostProcessing(PostProcessParams ppp, InterfaceTextListener listener) {
        if (ppp.isDoPostProcessing()) {
            if (listener != null) {
                listener.println("Post processing params: " + BeanInspector.toString(ppp));
                // if textwindow was closed, check if it should be reopened for pp
                if (statistics instanceof StatisticsWithGUI) {
                    ((StatisticsWithGUI) statistics).maybeShowProxyPrinter();
                }
            }
            Population resultPop = (Population) (optimizationParameters.getOptimizer().getAllSolutions().getSolutions().clone());
            if (resultPop.getFunctionCalls() != optimizationParameters.getOptimizer().getPopulation().getFunctionCalls()) {
                resultPop.setFunctionCalls(optimizationParameters.getOptimizer().getPopulation().getFunctionCalls());
            }

            PostProcess.checkAccuracy((AbstractOptimizationProblem) optimizationParameters.getProblem(), resultPop, ppp.getAccuracies(), ppp.getAccAssumeConv(),
                    -1, ppp.getAccMaxEval(), null, true, listener);

            resultPop = PostProcess.postProcess(ppp, resultPop, (AbstractOptimizationProblem) optimizationParameters.getProblem(), listener);
            resultPopulation = resultPop;
            return resultPop;
        } else {
            return null;
        }
    }
}
