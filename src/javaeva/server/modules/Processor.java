package javaeva.server.modules;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javaeva.gui.BeanInspector;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceProcessor;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceMultimodalProblem;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.tools.RandomNumberGenerator;
import javaeva.server.stat.Statistics;
import wsi.ra.jproxy.RemoteStateListener;

public class Processor extends Thread implements InterfaceProcessor, InterfacePopulationChangedEventListener {

    public static final boolean     TRACE=false;
    private volatile boolean      	m_optRunning;
    public volatile boolean         m_doRunScript;
    public Statistics               m_Statistics;
    public InterfaceGOParameters    m_ModulParameter;
    public boolean                  m_createInitialPopulations=true;
    private RemoteStateListener		m_ListenerModule;
    private boolean 				wasRestarted = false;
    private int 					runCounter = 0;	

    transient private String				m_OutputPath = "";
    transient private BufferedWriter		m_OutputFile = null;

    public void addListener(RemoteStateListener module) {
		if (TRACE) System.out.println("Processor: setting module as listener: " + ((module==null) ? "null" : module.toString()));
   		m_ListenerModule = module;
    }
    
    /**
     */
    public Processor(Statistics Stat, ModuleAdapter Adapter, InterfaceGOParameters params) {
        m_ModulParameter    = params;
        m_Statistics        = Stat;
        m_ListenerModule      = Adapter;
    }

    /**
     *
     */
    public Processor(Statistics Stat) {
        m_Statistics = Stat;
    }
    
    protected boolean isOptRunning() {
    	return m_optRunning;
    }
    
    protected void setOptRunning(boolean bRun) {
    	m_optRunning = bRun;
    }

//    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
//    	listener = ea;
////    	hier weiter! TODO
//    }
    
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
        wasRestarted = false;
        setOptRunning(true);
    }

    /**
     *
     */
    public void stopOpt() { // this means user break
        if (TRACE) System.out.println("called StopOpt");
        setOptRunning(false);
        m_doRunScript = false;
        if (TRACE) System.out.println("m_doRunScript = false ");
    }

    /**
     *
     */
    public void runScript() {
        m_doRunScript = true;
    }

    /**
     *
     */
    public void run() {
        setPriority(1);
        while (true) {
        try {Thread.sleep(200);} catch (Exception e) {
            System.err.println ("There was an error in sleep Processor.run()" + e); }
//            while (m_doRunScript == true) {
//                setPriority(3);
//                //doRunScript();
//                setPriority(1);
//            }
            while (isOptRunning()) {
                setPriority(3);
                m_ModulParameter.saveInstance();
                optimize("Run ");
                setPriority(1);
            }
        }
    }

    /**
     *
     */
    public void optimize(String Info) {
    	if (!isOptRunning()) {
    		System.err.println("warning, this shouldnt happen in processor! Was startOpt called?");
    		setOptRunning(true);
    	}
        
        RandomNumberGenerator.setseed(m_ModulParameter.getSeed());
        
        if (m_ListenerModule!=null) {
        	if (wasRestarted) m_ListenerModule.performedRestart(getInfoString());
        	else m_ListenerModule.performedStart(getInfoString());
        }
        
//        if (this.show) this.m_StatusField.setText("Optimizing...");

        // opening output file...
        String name = this.m_ModulParameter.getOutputFileName();
        if (!name.equalsIgnoreCase("none") && !name.equals("")) {
            SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
            String m_StartDate = formatter.format(new Date());
            name = this.m_OutputPath + name +"_"+this.m_ModulParameter.getOptimizer().getName()+"_"+m_StartDate+".dat";
            try {
                this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (name)));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file! Filename: " + name);
            }
        	this.writeToFile(" FitnessCalls\t Best\t Mean\t Worst \t" + this.m_ModulParameter.getProblem().getAdditionalFileStringHeader(this.m_ModulParameter.getOptimizer().getPopulation()));
        } else {
            this.m_OutputFile = null;
        }
        m_ModulParameter.getOptimizer().addPopulationChangedEventListener(this);

        runCounter = 0;

        while (isOptRunning() && (runCounter<m_Statistics.getStatisticsParameter().getMultiRuns())) {
//        for (int runCounter = 0; runCounter<m_Statistics.getStatisticsParameter().getMultiRuns(); runCounter++) {
        	m_Statistics.printToTextListener("****** Multirun "+runCounter);
        	m_Statistics.startOptPerformed(Info,runCounter);
        	m_Statistics.printToTextListener("Module parameters:");
        	m_Statistics.printToTextListener(BeanInspector.toString(m_ModulParameter));
        	m_Statistics.printToTextListener("Statistics parameters:");
        	m_Statistics.printToTextListener(BeanInspector.toString(m_Statistics.getStatisticsParameter()));
        	
        	this.m_ModulParameter.getOptimizer().SetProblem(this.m_ModulParameter.getProblem());
        	if (this.m_createInitialPopulations) this.m_ModulParameter.getOptimizer().init();
        	this.m_ModulParameter.getTerminator().init();
        	
        	//m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
        	m_ListenerModule.updateProgress(getStatusPercent(m_ModulParameter.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()));
        	
        	do {	// main loop
        		this.m_ModulParameter.getOptimizer().optimize();
//        		m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
//            	m_ListenerModule.updateProgress(getStatusPercent(m_ModulParameter.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()));     		
        	} while (isOptRunning() && !this.m_ModulParameter.getTerminator().isTerminated(this.m_ModulParameter.getOptimizer().getPopulation()));
        	runCounter++;

//        	m_doOpt = true;
//        	while (m_doOpt) { // old main loop
//        		// deleted by HU m_Statistics.plotStatisticsPerformed();
//        		this.m_ModulParameter.getOptimizer().optimize();
//        		if (this.m_ModulParameter.getTerminator().isTerminated(this.m_ModulParameter.getOptimizer().getPopulation())) {
//        			m_doOpt = false;
//        		} else if (!m_doOpt) { // this means user break
//        			runCounter = m_Statistics.getStatisticsParameter().getMultiRuns();
//        		}
//        		m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
//            	m_ListenerModule.updateProgress(getStatusPercent(m_ModulParameter.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()));
//        	} // end of while (m_doOpt==true)
        	if (m_ModulParameter.getProblem() instanceof InterfaceMultimodalProblem) {
        		//	TODO improve this?
        		InterfaceMultimodalProblem mmProb = (InterfaceMultimodalProblem)m_ModulParameter.getProblem();
        		System.out.println("no optima found: " + mmProb.getNumberOfFoundOptima(m_ModulParameter.getOptimizer().getPopulation()));
        	}
        	m_Statistics.stopOptPerformed(isOptRunning()); // stop is "normal" if opt wasnt set false by the user
        }
        setOptRunning(false); // normal finish
        if (m_ListenerModule!=null) m_ListenerModule.performedStop(); // is only needed in client server mode
        m_ListenerModule.updateProgress(0);
    }
    
    private int getStatusPercent(Population pop, int currentRun, int multiRuns) {
	    double x = 100/multiRuns;
	    int curProgress;
	    if (this.m_ModulParameter.getTerminator() instanceof EvaluationTerminator) {
	        double y = x/(double)((EvaluationTerminator)this.m_ModulParameter.getTerminator()).getFitnessCalls();
	        curProgress = (int)(currentRun * x + pop.getFunctionCalls()*y);
	    } else {
	    	curProgress = (int)(currentRun * x);
	    }
	    return curProgress;
    }
    
    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
        Population population = ((InterfaceOptimizer)source).getPopulation();

		m_Statistics.createNextGenerationPerformed((PopulationInterface)this.m_ModulParameter.getOptimizer().getPopulation());
    	m_ListenerModule.updateProgress(getStatusPercent(m_ModulParameter.getOptimizer().getPopulation(), runCounter, m_Statistics.getStatisticsParameter().getMultiRuns()));     		
                
    	if (this.m_OutputFile != null) {
	        // data to be stored in file
//	        double tmpd = 0;
	        StringBuffer  tmpLine = new StringBuffer("");
	        tmpLine.append(population.getFunctionCalls());
	        tmpLine.append("\t");
	        tmpLine.append(population.getBestEAIndividual().getFitness(0));
	        tmpLine.append("\t");
	        double[] fit = population.getMeanFitness();
	        //for (int i = 0; i < population.size(); i++) tmpd += ((AbstractEAIndividual)population.get(i)).getFitness(0)/(double)population.size();
	        tmpLine.append(BeanInspector.toString(fit));
	        tmpLine.append("\t");
	        tmpLine.append(population.getWorstEAIndividual().getFitness(0));
	        tmpLine.append("\t");
	        //tmpLine.append(population.getBestEAIndividual().getStringRepresentation());
	        tmpLine.append(this.m_ModulParameter.getProblem().getAdditionalFileStringValue(population));
	        this.writeToFile(tmpLine.toString());
    	}
    }
    
    /** This method writes Data to file.
     * @param line      The line that is to be added to the file
     */
    private void writeToFile(String line) {
        //String write = line + "\n";
        if (this.m_OutputFile == null) return;
        try {
            this.m_OutputFile.write(line, 0, line.length());
            this.m_OutputFile.write('\n');
            this.m_OutputFile.flush();
        } catch (IOException e) {
            System.err.println("Problems writing to output file!");
        }
    }
    
    public String getInfoString() {
    	StringBuffer sb = new StringBuffer("processing ");
    	sb.append(this.m_ModulParameter.getProblem().getName());
    	sb.append(" using ");
    	sb.append(this.m_ModulParameter.getOptimizer().getName());
    	// commented out because the number of multi-runs can be changed after start
    	// so it might create misinformation (would still be the user's fault, though) 
//    	sb.append(" for ");
//    	sb.append(m_Statistics.getStatistisParameter().getMultiRuns());
//    	sb.append(" runs");
    	return sb.toString();
    }
    
    /** This method return the Statistics object.
     */
    public Statistics getStatistics() {
        return m_Statistics;
    }

    /** These methods allow you to get and set the Modul Parameters.
     */
    public InterfaceGOParameters getModuleParameter() {
        return m_ModulParameter;
    }
    public void setModuleParameter(InterfaceGOParameters x) {
        m_ModulParameter= x;
    }
}
