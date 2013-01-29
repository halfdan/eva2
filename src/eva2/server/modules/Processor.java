package eva2.server.modules;

import eva2.gui.BeanInspector;
import eva2.server.go.*;
import eva2.server.go.operators.paramcontrol.ConstantParameters;
import eva2.server.go.operators.paramcontrol.InterfaceParameterControl;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.operators.postprocess.SolutionHistogram;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.operators.terminators.GenerationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.stat.InterfaceStatistics;
import eva2.server.stat.InterfaceTextListener;
import eva2.server.stat.StatisticsWithGUI;
import eva2.tools.EVAERROR;
import eva2.tools.EVAHELP;
import eva2.tools.StringTools;
import eva2.tools.jproxy.RemoteStateListener;
import eva2.tools.math.RNG;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * The Processor may run as a thread permanently (GenericModuleAdapter) and is then stopped and started
 * by a switch in startOpt/stopOpt.
 * 
 * Processor also handles adaptive parameter control by checking for the method getParamControl in (so far)
 * Optimizer and Problem instances. The return-value may be InterfaceParameterControl or an array of Objects.
 * If it is a control interface, it is applied to the instance that returned it directly. For arrays of objects
 * each array entry is again handled by checking for getParamControl, thus recursive controllable structures
 * are possible.
 *  
 * @author mkron
 *
 */
public class Processor extends Thread implements InterfaceProcessor, InterfacePopulationChangedEventListener {

    private static final Logger LOGGER = Logger.getLogger(Processor.class.getName());
    private volatile boolean isOptimizationRunning;
    private InterfaceStatistics m_Statistics;
    private InterfaceGOParameters goParams;
    private boolean m_createInitialPopulations = true;
    private boolean saveParams = true;
    private RemoteStateListener remoteStateListener;
    private boolean wasRestarted = false;
    private int runCounter = 0;
    private Population resPop = null;
    private boolean userAborted = false;

    @Override
    public void addListener(RemoteStateListener module) {
        LOGGER.log(
                Level.FINEST,
                "Processor: setting module as listener: " + ((module == null)
                ? "null" : module.toString()));

        remoteStateListener = module;
    }

    /**
     * Construct a Processor instance and make statistics instance informable of the parameters,
     * such they can by dynamically show additional information.
     * 
     * @see InterfaceNotifyOnInformers
     */
    public Processor(InterfaceStatistics Stat, ModuleAdapter moduleAdapter, InterfaceGOParameters params) {
        goParams = params;
        m_Statistics = Stat;
        remoteStateListener = moduleAdapter;

        // the statistics want to be informed if the strategy or the optimizer (which provide statistical data as InterfaceAdditionalInformer) change.
        if (Stat != null && (params != null)) {
            if (Stat.getStatisticsParameter() instanceof InterfaceNotifyOnInformers) {
                // 	addition for the statistics revision with selectable strings - make sure the go parameters are represented within the statistics
                params.addInformableInstance((InterfaceNotifyOnInformers) (Stat.getStatisticsParameter()));
            }
        }
    }

    public boolean isOptRunning() {
        return isOptimizationRunning;
    }

    protected void setOptRunning(boolean bRun) {
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
    public void startOpt() {
        m_createInitialPopulations = true;        
        if (isOptRunning()) {
            LOGGER.log(Level.SEVERE, "Processor is already running.");
            return;
        }
        resPop = null;
        userAborted = false;
        wasRestarted = false;
        setOptRunning(true);
    }

    /**
     * Return true if the optimization was stopped by the user instead of 
     * the termination criterion.
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
    public void restartOpt() {
        m_createInitialPopulations = false;        
        if (isOptRunning()) {
            LOGGER.log(Level.SEVERE, "Processor is already running.");
            return;
        }
        userAborted = false;
        wasRestarted = true;
        setOptRunning(true);
    }

    /**
     *
     */
    @Override
    public void stopOpt() { // this means user break
        setOptRunning(false);
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
            runOptOnce();
        }
    }

    public Population runOptOnce() {
        try {
            EVAERROR.clearMsgCache();
            while (isOptRunning()) {
                setPriority(3);
                if (saveParams) {
                    try {
                        goParams.saveInstance();
                    } catch (Exception e) {
                        System.err.println("Error on saveInstance!");
                    }
                }
                resPop = optimize("Run");
                setPriority(1);
            }
        } catch (Exception e) {
            String errMsg = e.toString();
            if ((errMsg == null) || (errMsg.length() == 0)) {
                errMsg = "check console output for error messages.";
            }
            errMsg = "Exception in Processor: " + errMsg;
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            try {
                JOptionPane.showMessageDialog(null, StringTools.wrapLine(errMsg, 60, 0.2), "Error in Optimization", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
            } catch (Error error) {
            }
            //m_Statistics.stopOptPerformed(false);
            setOptRunning(false); // normal finish
            if (remoteStateListener != null) {
                remoteStateListener.performedStop(); // is only needed in client server mode
                remoteStateListener.updateProgress(0, errMsg);
            }
        }
        return resPop;
    }

    /**
     * Main optimization loop.
     * Return a population containing the solutions of the last run if there were multiple.
     */
    protected Population optimize(String infoString) {
        Population resultPop = null;

        if (!isOptRunning()) {
            System.err.println("warning, this shouldnt happen in processor! Was startOpt called?");
            setOptRunning(true);
        }

        RNG.setRandomSeed(goParams.getSeed());

        if (remoteStateListener != null) {
            if (wasRestarted) {
                remoteStateListener.performedRestart(getInfoString());
            } else {
                remoteStateListener.performedStart(getInfoString());
            }
        }

        goParams.getOptimizer().addPopulationChangedEventListener(this);

        runCounter = 0;
        String popLog = null; //"populationLog.txt";

        while (isOptRunning() && (runCounter < m_Statistics.getStatisticsParameter().getMultiRuns())) {
            m_Statistics.startOptPerformed(getInfoString(), runCounter, goParams, getInformerList());

            this.goParams.getProblem().initProblem();
            this.goParams.getOptimizer().setProblem(this.goParams.getProblem());
            this.goParams.getTerminator().init(this.goParams.getProblem());
            maybeInitParamCtrl(goParams);
            if (this.m_createInitialPopulations) {
                this.goParams.getOptimizer().init();
            }

            //m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
            if (remoteStateListener != null) {
                remoteStateListener.updateProgress(getStatusPercent(goParams.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()), null);
            }
            if (popLog != null) {
                EVAHELP.clearLog(popLog);
            }

            do {	// main loop
                maybeUpdateParamCtrl(goParams);

                this.goParams.getOptimizer().optimize();
                // registerPopulationStateChanged *SHOULD* be fired by the optimizer or resp. the population
                // as we are event listener
                if (popLog != null) {
                    EVAHELP.logString(this.goParams.getOptimizer().getPopulation().getIndyList(), popLog);
                }
            } while (isOptRunning() && !this.goParams.getTerminator().isTerminated(this.goParams.getOptimizer().getAllSolutions()));
            runCounter++;
            maybeFinishParamCtrl(goParams);
            userAborted = !isOptRunning(); // stop is "normal" if opt wasnt set false by the user (and thus still true)
            //////////////// Default stats
            m_Statistics.stopOptPerformed(!userAborted, goParams.getTerminator().lastTerminationMessage()); // stop is "normal" if opt wasnt set false by the user (and thus still true)

            //////////////// PP or set results without further PP
            if (!userAborted) {
                resultPop = performPostProcessing();
                if (resultPop == null) { // post processing disabled, so use opt. solutions
                    resultPop = goParams.getOptimizer().getAllSolutions().getSolutions();
                }
            } else {
                resultPop = goParams.getOptimizer().getAllSolutions().getSolutions();
            }
            m_Statistics.postProcessingPerformed(resultPop);

        }
        setOptRunning(false); // normal finish
        if (remoteStateListener != null) {
            remoteStateListener.performedStop(); // is only needed in client server mode
        }
        if (remoteStateListener != null) {
            remoteStateListener.updateProgress(0, null);
        }
        goParams.getOptimizer().removePopulationChangedEventListener(this);
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
                        if (!((InterfaceParameterControl) controllerOrSubControllable instanceof ConstantParameters)) {
                            BeanInspector.callIfAvailable((InterfaceParameterControl) controllerOrSubControllable, methodName, args);
                        }
                    } else {
                        args[0] = controllerOrSubControllable;
                        iterateParamCtrl(controllerOrSubControllable, methodName, args);
                    }
                }
            } else if (paramCtrlReturn instanceof InterfaceParameterControl) {
                if (!((InterfaceParameterControl) paramCtrlReturn instanceof ConstantParameters)) {
                    BeanInspector.callIfAvailable((InterfaceParameterControl) paramCtrlReturn, methodName, args);
                }
            }
        }
    }

    private void maybeInitParamCtrl(InterfaceGOParameters goParams) {
        iterateParamCtrl(goParams.getOptimizer(), "init", new Object[]{goParams.getOptimizer(), goParams.getOptimizer().getPopulation()});
        iterateParamCtrl(goParams.getProblem(), "init", new Object[]{goParams.getProblem(), goParams.getOptimizer().getPopulation()});
    }

    private void maybeFinishParamCtrl(InterfaceGOParameters goParams) {
        iterateParamCtrl(goParams.getOptimizer(), "finish", new Object[]{goParams.getOptimizer(), goParams.getOptimizer().getPopulation()});
        iterateParamCtrl(goParams.getProblem(), "finish", new Object[]{goParams.getProblem(), goParams.getOptimizer().getPopulation()});
    }

    private void maybeUpdateParamCtrl(InterfaceGOParameters goParams) {
        Object[] args;
        InterfaceTerminator terminator = goParams.getTerminator();
        InterfaceOptimizer optimizer = goParams.getOptimizer();
        if (terminator instanceof GenerationTerminator) {
            args = new Object[]{optimizer, optimizer.getPopulation(), optimizer.getPopulation().getGeneration(), ((GenerationTerminator) terminator).getGenerations()};
        } //			((InterfaceParameterControl)paramCtrl).updateParameters(optimizer, optimizer.getPopulation().getGeneration(),  ((GenerationTerminator)terminator).getGenerations());
        else if (terminator instanceof EvaluationTerminator) {
            args = new Object[]{optimizer, optimizer.getPopulation(), optimizer.getPopulation().getFunctionCalls(), ((EvaluationTerminator) terminator).getFitnessCalls()};
        } //			((InterfaceParameterControl)paramCtrl).updateParameters(optimizer, optimizer.getPopulation().getFunctionCalls(), ((EvaluationTerminator)terminator).getFitnessCalls());
        else {
            args = new Object[]{optimizer};
        }
//			((InterfaceParameterControl)paramCtrl).updateParameters(optimizer);

        if (args != null) { // only if iteration counting is available
            iterateParamCtrl(optimizer, "updateParameters", args);
            args[0] = goParams.getProblem();
            iterateParamCtrl(goParams.getProblem(), "updateParameters", args);
        }
    }

    /**
     * Calculate the percentage of current (multi-)run already performed, based on evaluations/generations 
     * for the EvaluationTerminator/GenerationTerminator or multi-runs only.
     * 
     * @param pop
     * @param currentRun
     * @param multiRuns
     * @return the percentage of current (multi-)run already performed
     */
    private int getStatusPercent(Population pop, int currentRun, int multiRuns) {
        double percentPerRun = 100. / multiRuns;
        int curProgress;
        if (this.goParams.getTerminator() instanceof EvaluationTerminator) {
            double curRunPerf = pop.getFunctionCalls() * percentPerRun / (double) ((EvaluationTerminator) this.goParams.getTerminator()).getFitnessCalls();
            curProgress = (int) (currentRun * percentPerRun + curRunPerf);
        } else if (this.goParams.getTerminator() instanceof GenerationTerminator) {
            double curRunPerf = pop.getGeneration() * percentPerRun / (double) ((GenerationTerminator) this.goParams.getTerminator()).getGenerations();
            curProgress = (int) (currentRun * percentPerRun + curRunPerf);
        } else {
            curProgress = (int) (currentRun * percentPerRun);
        }
        return curProgress;
    }

    /** 
     * This method allows an optimizer to register a change in the optimizer.
     * Send some information to the statistics module and update the progress.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals(Population.nextGenerationPerformed)) {
//    		System.out.println(getGOParams().getOptimizer().getPopulation().getFunctionCalls() + " " + getGOParams().getOptimizer().getPopulation().getBestFitness()[0]);
            m_Statistics.createNextGenerationPerformed(
                    (PopulationInterface) this.goParams.getOptimizer().getPopulation(),
                    this.goParams.getOptimizer(),
                    getInformerList());
            if (remoteStateListener != null) {
                remoteStateListener.updateProgress(
                        getStatusPercent(
                        goParams.getOptimizer().getPopulation(),
                        runCounter,
                        m_Statistics.getStatisticsParameter().getMultiRuns()),
                        null);
            }
        }
    }

    protected List<InterfaceAdditionalPopulationInformer> getInformerList() {
        List<InterfaceAdditionalPopulationInformer> informerList = new ArrayList<InterfaceAdditionalPopulationInformer>(2);
        informerList.add(this.goParams.getProblem());
        if (this.goParams.getOptimizer() instanceof InterfaceAdditionalPopulationInformer) {
            informerList.add((InterfaceAdditionalPopulationInformer) this.goParams.getOptimizer());
        }
        return informerList;
    }

    /** This method writes Data to file.
     * @param line      The line that is to be added to the file
     */
//    private void writeToFile(String line) {
//        //String write = line + "\n";
//        if (this.m_OutputFile == null) return;
//        try {
//            this.m_OutputFile.write(line, 0, line.length());
//            this.m_OutputFile.write('\n');
//            this.m_OutputFile.flush();
//        } catch (IOException e) {
//            System.err.println("Problems writing to output file!");
//        }
//    }
    @Override
    public String getInfoString() {
        //StringBuffer sb = new StringBuffer("processing ");
        StringBuilder sb = new StringBuilder(this.goParams.getProblem().getName());
        sb.append("+");
        sb.append(this.goParams.getOptimizer().getName());
        // commented out because the number of multi-runs can be changed after start
        // so it might create misinformation (would still be the user's fault, though) 
//    	sb.append(" for ");
//    	sb.append(m_Statistics.getStatistisParameter().getMultiRuns());
//    	sb.append(" runs");
        return sb.toString();
    }

    /** 
     * This method return the Statistics object.
     */
    public InterfaceStatistics getStatistics() {
        return m_Statistics;
    }

    /** 
     * These methods allow you to get and set the Module Parameters.
     */
    public InterfaceGOParameters getGOParams() {
        return goParams;
    }

    public void setGOParams(InterfaceGOParameters params) {
        if (params != null) {
            goParams = params;
        } else {
            System.err.println("Setting parameters failed (parameters were null) (Processor.setGOParams)");
        }
    }

    /** 
     * Return the last solution population or null if there is none available.
     * 
     * @return the last solution population or null
     */
    public Population getResultPopulation() {
        return resPop;
    }

    public Population performPostProcessing() {
        return performPostProcessing((PostProcessParams) goParams.getPostProcessParams(), (InterfaceTextListener) m_Statistics);
    }

    /**
     * Perform a post processing step with given parameters, based on all solutions found by the optimizer.
     * Use getResultPopulation() to retrieve results.
     * 
     * @param ppp
     * @param listener
     */
    public Population performPostProcessing(PostProcessParams ppp, InterfaceTextListener listener) {
        if (ppp.isDoPostProcessing()) {
            if (listener != null) {
                listener.println("Post processing params: " + BeanInspector.toString(ppp));
                // if textwindow was closed, check if it should be reopened for pp
                if (m_Statistics instanceof StatisticsWithGUI) {
                    ((StatisticsWithGUI) m_Statistics).maybeShowProxyPrinter();
                }
            }
            Population resultPop = (Population) (goParams.getOptimizer().getAllSolutions().getSolutions().clone());
            if (resultPop.getFunctionCalls() != goParams.getOptimizer().getPopulation().getFunctionCalls()) {
                //    		System.err.println("bad case in Processor::performNewPostProcessing ");
                resultPop.SetFunctionCalls(goParams.getOptimizer().getPopulation().getFunctionCalls());
            }
//	    	if (!resultPop.contains(m_Statistics.getBestSolution())) {
//	    		resultPop.add(m_Statistics.getBestSolution()); 
            // this is a minor cheat but guarantees that the best solution ever found is contained in the final results
            // This was evil in case multiple runs were performed with PP, because the best of an earlier run is added which is confusing.
            // the minor cheat should not be necessary anymore anyways, since the getAllSolutions() variant replaced the earlier getPopulation() call
//	    		resultPop.synchSize();
//	    	}

            PostProcess.checkAccuracy((AbstractOptimizationProblem) goParams.getProblem(), resultPop, ppp.getAccuracies(), ppp.getAccAssumeConv(),
                    -1, ppp.getAccMaxEval(), (SolutionHistogram[]) null, true, listener);

            resultPop = PostProcess.postProcess(ppp, resultPop, (AbstractOptimizationProblem) goParams.getProblem(), listener);
            resPop = resultPop;
            return resultPop;
        } else {
            return null;
        }
    }
}
