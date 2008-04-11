package javaeva.server.modules;

import javaeva.gui.BeanInspector;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceProcessor;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.operators.postprocess.PostProcess;
import javaeva.server.go.operators.postprocess.PostProcessParams;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.operators.terminators.GenerationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.AbstractOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;
import javaeva.server.stat.InterfaceStatistics;
import javaeva.server.stat.InterfaceTextListener;
import javaeva.server.stat.StatisticsWithGUI;
import wsi.ra.jproxy.RemoteStateListener;

/**
 * The Processor may run as a thread permanently (GenericModuleAdapter) and is then stopped and started
 * by a switch in startOpt/stopOpt.
 *  
 * @author mkron
 *
 */
public class Processor extends Thread implements InterfaceProcessor, InterfacePopulationChangedEventListener {

    private static final boolean    TRACE=false;
    private volatile boolean      	m_optRunning;
//    private volatile boolean         m_doRunScript;
    private InterfaceStatistics     m_Statistics;
    private InterfaceGOParameters   goParams;
    private boolean                 m_createInitialPopulations = true;
    private boolean					saveParams = true;
    private RemoteStateListener		m_ListenerModule;
    private boolean 				wasRestarted = false;
//    private int 					postProcessSteps = 0;
    private int 					runCounter = 0;	
    private Population				resPop = null;

//    transient private String				m_OutputPath = "";
//    transient private BufferedWriter		m_OutputFile = null;

    public void addListener(RemoteStateListener module) {
		if (TRACE) System.out.println("Processor: setting module as listener: " + ((module==null) ? "null" : module.toString()));
   		m_ListenerModule = module;
    }
    
    /**
     */
    public Processor(InterfaceStatistics Stat, ModuleAdapter Adapter, InterfaceGOParameters params) {
        goParams    = params;
        m_Statistics        = Stat;
        m_ListenerModule      = Adapter;
    }

    /**
     *
     */
    public Processor(InterfaceStatistics Stat) {
        m_Statistics = Stat;
    }
    
    protected boolean isOptRunning() {
    	return m_optRunning;
    }
    
    protected void setOptRunning(boolean bRun) {
    	m_optRunning = bRun;
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
    public void startOpt() {
        m_createInitialPopulations = true;
        if (TRACE) System.out.println("startOpt called:");
        if (isOptRunning()) {
            System.err.println("ERROR: Processor is already running !!");
            return;
        }
        resPop = null;
        wasRestarted = false;
        setOptRunning(true);
    }

    /**
     *
     */
    public void restartOpt() {
        m_createInitialPopulations = false;
        if (TRACE) System.out.println("restartOpt called:");
        if (isOptRunning()) {
            System.err.println("ERROR: Processor is already running !!");
            return;
        }
        wasRestarted = true;
        setOptRunning(true);
    }

    /**
     *
     */
    public void stopOpt() { // this means user break
        if (TRACE) System.out.println("called StopOpt");
        setOptRunning(false);
        if (TRACE) System.out.println("m_doRunScript = false ");
    }

    /**
     *
     */
    public void run() {
    	setPriority(1);
    	while (true) {
    		try {
    			Thread.sleep(200);
    		} catch (Exception e) {
    			System.err.println ("There was an error in sleep Processor.run()" + e); 
    		}
    		runOptOnce();
    	}
    }
    
    public Population runOptOnce() {
    	try {
    		while (isOptRunning()) {
    			setPriority(3);
    			if (saveParams) goParams.saveInstance();
    			resPop = optimize("Run");
    			setPriority(1);
    		}
    	} catch (Exception e) {
    		System.err.println("Caught exception in Processor: "+e.getMessage());
    		e.printStackTrace();
        	//m_Statistics.stopOptPerformed(false);
        	setOptRunning(false); // normal finish
        	if (m_ListenerModule!=null) {
        		m_ListenerModule.performedStop(); // is only needed in client server mode
        		String errMsg = e.getMessage();
        		if ((errMsg == null) || (errMsg.length() == 0)) errMsg="check console output for error messages.";
            	m_ListenerModule.updateProgress(0, "Error in optimization: " + errMsg);    		
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

    	RandomNumberGenerator.setRandomSeed(goParams.getSeed());
        
        if (m_ListenerModule!=null) {
        	if (wasRestarted) m_ListenerModule.performedRestart(getInfoString());
        	else m_ListenerModule.performedStart(getInfoString());
        }
       
    	goParams.getOptimizer().addPopulationChangedEventListener(this);

        runCounter = 0;

        while (isOptRunning() && (runCounter<m_Statistics.getStatisticsParameter().getMultiRuns())) {
        	m_Statistics.startOptPerformed(getInfoString(),runCounter, goParams);

        	this.goParams.getProblem().initProblem();
        	this.goParams.getOptimizer().SetProblem(this.goParams.getProblem());
        	if (this.m_createInitialPopulations) this.goParams.getOptimizer().init();
        	this.goParams.getTerminator().init();
        	
        	//m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
        	if (m_ListenerModule!=null) m_ListenerModule.updateProgress(getStatusPercent(goParams.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()), null);
        	
        	do {	// main loop
        		this.goParams.getOptimizer().optimize();
        		// registerPopulationStateChanged *SHOULD* be fired by the optimizer or resp. the population
        		// as we are event listener
        	} while (isOptRunning() && !this.goParams.getTerminator().isTerminated(this.goParams.getOptimizer().getPopulation()));
        	runCounter++;

        	//////////////// Default stats
        	m_Statistics.stopOptPerformed(isOptRunning()); // stop is "normal" if opt wasnt set false by the user (and thus still true)
        	
        	//////////////// PP or set results without further PP
        	if (isOptRunning()) {
        		resultPop = performPostProcessing();
        	} else resultPop = goParams.getOptimizer().getAllSolutions();

        }
        setOptRunning(false); // normal finish
        if (m_ListenerModule!=null) m_ListenerModule.performedStop(); // is only needed in client server mode
        if (m_ListenerModule!=null) m_ListenerModule.updateProgress(0, null);
        return resultPop;
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
	    double percentPerRun = 100/multiRuns;
	    int curProgress;
	    if (this.goParams.getTerminator() instanceof EvaluationTerminator) {
	        double curRunPerf = pop.getFunctionCalls()*percentPerRun/(double)((EvaluationTerminator)this.goParams.getTerminator()).getFitnessCalls();
	        curProgress = (int)(currentRun * percentPerRun + curRunPerf);
	    } else if (this.goParams.getTerminator() instanceof GenerationTerminator) {
	        double curRunPerf = pop.getGeneration()*percentPerRun/(double)((GenerationTerminator)this.goParams.getTerminator()).getGenerations();
	        curProgress = (int)(currentRun * percentPerRun + curRunPerf);
	    } else curProgress = (int)(currentRun * percentPerRun);
	    return curProgress;
    }
    
    /** 
     * This method allows an optimizer to register a change in the optimizer.
     * Send some information to the statistics module and update the progress.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
		m_Statistics.createNextGenerationPerformed(
				(PopulationInterface)this.goParams.getOptimizer().getPopulation(), 
				this.goParams.getProblem());
		if (m_ListenerModule != null) {
			m_ListenerModule.updateProgress(
					getStatusPercent(
							goParams.getOptimizer().getPopulation(), 
							runCounter,
							m_Statistics.getStatisticsParameter().getMultiRuns()), 
							null);
		}
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
    
    public String getInfoString() {
    	//StringBuffer sb = new StringBuffer("processing ");
    	StringBuffer sb = new StringBuffer(this.goParams.getProblem().getName());
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
    public void setGOParams(InterfaceGOParameters x) {
        goParams= x;
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
    	return performPostProcessing((PostProcessParams)goParams.getPostProcessParams(), (InterfaceTextListener)m_Statistics);
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
	    		if (m_Statistics instanceof StatisticsWithGUI) ((StatisticsWithGUI)m_Statistics).maybeShowProxyPrinter();
	    	}
	    	Population resultPop = goParams.getOptimizer().getAllSolutions();
	    	if (resultPop.getFunctionCalls() != goParams.getOptimizer().getPopulation().getFunctionCalls()) {
	//    		System.err.println("bad case in Processor::performNewPostProcessing ");
	    		resultPop.SetFunctionCalls(goParams.getOptimizer().getPopulation().getFunctionCalls());
	    	}
	    	if (!resultPop.contains(m_Statistics.getBestSolution())) resultPop.add(m_Statistics.getBestSolution()); // this is a minor cheat but guarantees that the best solution ever found is contained in the final results
	    	resultPop = PostProcess.postProcess(ppp, resultPop, (AbstractOptimizationProblem)goParams.getProblem(), listener);
	    	return resultPop;
    	} else return null;
    }
}
