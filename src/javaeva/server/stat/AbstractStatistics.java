package javaeva.server.stat;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javaeva.gui.BeanInspector;
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.problems.InterfaceAdditionalPopulationInformer;
import wsi.ra.tool.StatisticUtils;

public abstract class AbstractStatistics implements InterfaceTextListener, InterfaceStatistics {
	private PrintWriter resultOut;
	public final static boolean TRACE = false;
	protected InterfaceStatisticsParameter m_StatsParams;
	protected String startDate;
	protected long startTime;
	
	/**
	 * Keep track of all intermediate fitness values, best, avg. and worst, averaging over all runs
	 * for final output, "refining" the multi run data.
	 * If the runs have different lengths, the shortest of all defines the length
	 * of averaged data to be displayed. This mechanism expects that createNextGenerationPerformed
	 * comes in regular intervals (in terms of function calls performed). This needs to be emulated
	 * by dynamic population optimizers, also due to the graph output.
	 */
	private boolean refineMultiRuns = true;
	private ArrayList<double[][]> meanCollection;
	
	// say whether the object should be written to a file every time
	private boolean saveParams = true;
	private boolean firstPlot = true;
	private int runIterCnt = 0;
	// show this many iterations of the averaged performance after a full multi-run
	private int showAvgIntervals = 9;
	
	// collect data
	protected int functionCalls;
	protected int functionCallSum;
	protected int convergenceCnt;
	protected int optRunsPerformed;
	protected double[] currentBestFit;
	protected double[] meanFitness;
	protected double[] currentWorstFit;
	protected IndividualInterface bestCurrentIndividual, bestIndivdualAllover;

	
	private ArrayList<InterfaceTextListener> textListeners;

	public AbstractStatistics() {
		firstPlot = true;
		functionCalls = 0;
		functionCallSum = 0;
		convergenceCnt = 0;
		optRunsPerformed = 0;
		runIterCnt = 0;
		textListeners = new ArrayList<InterfaceTextListener>();
	}
	
	public void addTextListener(InterfaceTextListener listener) {
		if (!textListeners.contains(listener)) textListeners.add(listener);
	}
	
	public boolean removeTextListener(InterfaceTextListener listener) {
		return textListeners.remove(listener);
	}
	
	protected void initOutput() {
		SimpleDateFormat formatter = new SimpleDateFormat(
		"E'_'yyyy.MM.dd'_at_'hh.mm.ss");
		startDate = formatter.format(new Date());
		startTime = System.currentTimeMillis();
		// open the result file:
		String resFName = m_StatsParams.getResultFileName();
		if ((m_StatsParams.getOutputTo().getSelectedTagID()!=1) // not "text only" 
				&& (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_NONE)) { // verbosity accordingly high
			//!resFName.equalsIgnoreCase("none") && !resFName.equals("")) {
			String name = resFName + "_" + startDate + ".txt";
			if (TRACE) System.out.println("FileName =" + name);
			try {
				resultOut = new PrintWriter(new FileOutputStream(name));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error: " + e);
			}
			resultOut.println("StartDate:" + startDate);
			resultOut.println("On Host:" + getHostName());
		} else resultOut = null;
	}
	
	/**
	 * If set to true, before every run the parameters will be stored to a file at the start
	 * of each run. Default is true.
	 * 
	 * @param doSave
	 */
	public void setSaveParams(boolean doSave) {
		saveParams = doSave;
	}

	public void startOptPerformed(String infoString, int runNumber, Object params) {
		if (TRACE) System.out.println("AbstractStatistics.startOptPerformed " + runNumber);
		if (runNumber == 0) {
			functionCallSum = 0;
			firstPlot = true;
			optRunsPerformed = 0;
			convergenceCnt = 0;
			if (saveParams) m_StatsParams.saveInstance();
			initOutput();
			bestCurrentIndividual = null;
			bestIndivdualAllover = null;
			if (refineMultiRuns) meanCollection = new ArrayList<double[][]>();
			else meanCollection = null;
		}
		runIterCnt = 0;
    	if (printRunIntroVerbosity()) printToTextListener("\n****** Multirun "+runNumber);
    	if (params != null) {
    		if (printRunIntroVerbosity()) printToTextListener("\nModule parameters: ");
    		if (printRunIntroVerbosity()) printToTextListener(BeanInspector.toString(params));
    	}
    	if (printRunIntroVerbosity()) printToTextListener("\nStatistics parameters: ");
    	if (printRunIntroVerbosity()) printToTextListener(BeanInspector.toString(getStatisticsParameter()) + '\n');
		functionCalls = 0;
	}
	
	public void stopOptPerformed(boolean normal) {
		if (TRACE) System.out.println("AbstractStatistics.stopOptPerformed");
		if (runIterCnt < meanCollection.size()) {
			// no good: later run was shorter than the first one. What to do? Discard the longer one:
			if (TRACE) System.err.println("Error in AbstractStatistics: later run was shorter than earlier one... discarding rest...");
			for (int i=meanCollection.size()-1; i>=runIterCnt; i--) meanCollection.remove(i);
		}
		optRunsPerformed++;
		functionCallSum += functionCalls;
		// check for convergence
		if (bestCurrentIndividual != null) {
			if (StatisticUtils.norm(bestCurrentIndividual.getFitness()) < this.m_StatsParams.getConvergenceRateThreshold()) {
				convergenceCnt++;
			}
			if (printRunStoppedVerbosity()) printToTextListener(" Best solution: " + BeanInspector.toString(bestCurrentIndividual) + "\n");
			if (printRunStoppedVerbosity()) printToTextListener(AbstractEAIndividual.getDefaultDataString(bestCurrentIndividual) + "\n");
		}
		if (currentBestFit!= null) {
			if (printRunStoppedVerbosity()) printToTextListener(" Best Fitness: " + BeanInspector.toString(currentBestFit) + "\n");
		}
		if (optRunsPerformed == m_StatsParams.getMultiRuns()) finalizeOutput();
	}
	
	protected void finalizeOutput() {
		if (printFinalVerbosity()) printToTextListener("*******\n Runs performed: " + optRunsPerformed + ", reached target " + convergenceCnt + " times with threshold " + m_StatsParams.getConvergenceRateThreshold() + ", rate " + convergenceCnt/(double)m_StatsParams.getMultiRuns() + '\n');
		if (printFinalVerbosity()) printToTextListener("Best overall individual: " + BeanInspector.toString(bestIndivdualAllover) + '\n');
		if (printFinalVerbosity()) printToTextListener("             solution	: " + AbstractEAIndividual.getDefaultDataString(bestIndivdualAllover) + '\n');
		if (printFinalVerbosity()) printToTextListener("             fitness	: " + BeanInspector.toString(bestIndivdualAllover.getFitness()) + '\n');
		if (refineMultiRuns && (optRunsPerformed>1) && (meanCollection != null)) {
			if (printFinalVerbosity()) printToTextListener("Averaged performance:\n");
			for (int i=0; i<meanCollection.size(); i++) divideMean(meanCollection.get(i), optRunsPerformed);
			if (printFinalVerbosity()) printToTextListener(refineToText(meanCollection, showAvgIntervals));
		}
		if (TRACE)
			System.out.println("End of run");
		if (resultOut != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(
			"E'_'yyyy.MM.dd'_at_'hh:mm:ss");
			String StopDate = formatter.format(new Date());
			resultOut.println("StopDate:" + StopDate);
			resultOut.close();
		}
	}

    public static String refineToText(ArrayList<double[][]> result, int iterationsToShow) {
    	double[][] mean;
    	StringBuffer sbuf = new StringBuffer("Iteration\tFun.Calls\tBest\tMean\tWorst\n");
    	double step = result.size()/(iterationsToShow-1.);
    	int printedIteration=0;
    	
        for(int i = 1; i < result.size()+1; i++) {
        	// print the first, last and intermediate iterations requested by the integer parameter
        	// first one is printed always, as printedIteration=0
        	if ((i==result.size()) || ((i-1)==Math.round(printedIteration*step))) {
        		printedIteration++;
        		mean = result.get(i-1);
        		sbuf.append(i);
        		sbuf.append("\t");
        		sbuf.append(BeanInspector.toString(mean[0]));
        		sbuf.append("\t");
        		sbuf.append(BeanInspector.toString(mean[1]));
        		sbuf.append("\t");
        		sbuf.append(BeanInspector.toString(mean[2]));
        		sbuf.append("\t");
        		sbuf.append(BeanInspector.toString(mean[3]));
        		sbuf.append("\n");
        	}
        }
        return sbuf.toString();
    }
	
	public abstract String getHostName();

	public void printToTextListener(String s) {
		if ((resultOut != null)) resultOut.print(s);
		for (InterfaceTextListener l : textListeners) {
			if (m_StatsParams.getOutputTo().getSelectedTagID() >= 1) l.print(s);
		}
	}
	
	////////////// InterfaceTextListener
	public void print(String str) {
		printToTextListener(str);
	}
	////////////// InterfaceTextListener
	public void println(String str) {
		printToTextListener(str);
		printToTextListener("\n");
	}
	
	public InterfaceStatisticsParameter getStatisticsParameter() {
		return m_StatsParams;
	}
	
	protected boolean doTextOutput() {
		return (resultOut != null) || (textListeners.size()>0);
	}
	
	protected String getOutputHeader(InterfaceAdditionalPopulationInformer informer, PopulationInterface pop) {
		String headline = "Fit.-calls \t Best \t Mean \t Worst ";
		if ((informer == null) || !m_StatsParams.isOutputAdditionalInfo()) {
			return headline;
		} else return headline + "\t " + informer.getAdditionalFileStringHeader(pop);
	}
	
	protected String getOutputLine(InterfaceAdditionalPopulationInformer informer, PopulationInterface pop) {
		StringBuffer sbuf = new StringBuffer(Integer.toString(functionCalls));
		sbuf.append(" \t ");
		sbuf.append(BeanInspector.toString(currentBestFit));
		if (meanFitness != null) {
			sbuf.append(" \t ");
			sbuf.append(BeanInspector.toString(meanFitness));
		} else sbuf.append(" \t #");
		if (currentWorstFit != null) {
			sbuf.append(" \t ");
			sbuf.append(BeanInspector.toString(currentWorstFit));
		} else sbuf.append(" # \t");
		if (informer != null && m_StatsParams.isOutputAdditionalInfo()) {
			sbuf.append(" \t ");
			sbuf.append(informer.getAdditionalFileStringValue(pop));
		}		
		return sbuf.toString();
	}
	
	/**
	 *
	 */
	public synchronized void createNextGenerationPerformed(double[] bestfit,
			double[] worstfit, int calls) {
		functionCalls = calls;
		currentBestFit = bestfit;
		currentWorstFit = worstfit;
		meanFitness = null;
		
		if (firstPlot) {
			initPlots(m_StatsParams.getPlotDescriptions());
//			if (doTextOutput()) printToTextListener(getOutputHeader(null, null)+'\n');
			firstPlot = false;
		}
		if ((runIterCnt == 0) && printHeaderByVerbosity()) printToTextListener(getOutputHeader(null, null)+'\n');

		if (doTextOutput() && printLineByVerbosity(calls)) printToTextListener(getOutputLine(null, null)+'\n');
		plotCurrentResults();
		runIterCnt++;
	}
	
	/**
	 * If the population returns a specific data array, this method is called instead of doing standard output
	 * @param pop
	 * @param informer
	 */
	public abstract void plotSpecificData(PopulationInterface pop, InterfaceAdditionalPopulationInformer informer);
	
	protected abstract void plotCurrentResults();
	
	/**
	 * Called at the very first (multirun mode) plot of a fitness curve.
	 */
	protected abstract void initPlots(List<String[]> description);
	
	/**
	 * Do some data collection on the population. The informer parameter will not be handled by this method.
	 *
	 */
	public synchronized void createNextGenerationPerformed(PopulationInterface
			pop, InterfaceAdditionalPopulationInformer informer) {
		if (firstPlot) {
			initPlots(m_StatsParams.getPlotDescriptions());
//			if (doTextOutput()) printToTextListener(getOutputHeader(informer, pop)+'\n');
			firstPlot = false;
		}
		if ((runIterCnt==0) && printHeaderByVerbosity()) printToTextListener(getOutputHeader(informer, pop)+'\n');

		if (pop.getSpecificData() != null) {
			plotSpecificData(pop, informer);
			return;
		}
		// by default plotting only the best
		bestCurrentIndividual = pop.getBestIndividual().getClone();
		if ((bestIndivdualAllover == null) || (secondIsBetter(bestIndivdualAllover, bestCurrentIndividual))) {
			bestIndivdualAllover = bestCurrentIndividual;
//			printToTextListener("new best found!, last was " + BeanInspector.toString(bestIndivdualAllover) + "\n");
		}
		
//		IndividualInterface WorstInd = Pop.getWorstIndividual();
		if (bestCurrentIndividual == null) {
			System.err.println("createNextGenerationPerformed BestInd==null");
		}

		currentBestFit = bestCurrentIndividual.getFitness().clone();
		if (currentBestFit == null) {
			System.err.println("BestFitness==null !");
		}
		meanFitness = pop.getMeanFitness().clone();
		currentWorstFit = pop.getWorstIndividual().getFitness().clone();
		functionCalls = pop.getFunctionCalls();
		
		if (meanCollection != null) {
			// Collect average data
			double[][] means = null;
			if ((optRunsPerformed==0) && (meanCollection.size()<=runIterCnt)) { 
				// in the first run, newly allocate the arrays
				means = new double[4][currentBestFit.length];
				meanCollection.add(means);
			} else {
				if (meanCollection.size()<=runIterCnt) {// bad case!
					// may happen for dynamic pop-sizes, e.g. in Tribe, when runs do not necessarily send the
					// "generation performed" event the same number of times. 
					// thus: dont do an update for events that are "too late"
					means = null;
				} else means = meanCollection.get(runIterCnt);
			}
			if (means != null) updateMeans(means, functionCalls, currentBestFit, meanFitness, currentWorstFit);
		}
//		meanCollection.set(pop.getGenerations()-1, means);
		
		if (doTextOutput() && printLineByVerbosity(runIterCnt)) printToTextListener(getOutputLine(informer, pop)+'\n');
		plotCurrentResults();

		runIterCnt++;
	}

	private boolean printLineByVerbosity(int iteration) {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_KTH_IT) 
				|| ((m_StatsParams.getOutputVerbosity().getSelectedTagID() == StatsParameter.VERBOSITY_KTH_IT) 
						&& (isKthRun(iteration, m_StatsParams.getOutputVerbosityK())));
	}
	
	private boolean printRunIntroVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}
	
	private boolean printRunStoppedVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}
	
	private boolean printFinalVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_NONE);
	}
		
	private boolean isKthRun(int i, int k) {
		return (i % k) == 0;
	}
	
	private boolean printHeaderByVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}

	private void updateMeans(double[][] means, double funCalls, double[] bestFit, double[] meanFit, double[] worstFit) {
		means[0][0]+=funCalls;
		addMean(means[1], bestFit);
		addMean(means[2], meanFit);
		addMean(means[3], worstFit);
	}
	
	private static void divideMean(double[][] mean, double d) {
		for (int i=0; i<mean.length; i++) {
			for (int j=0; j<mean[i].length; j++) mean[i][j] /= d;
		}
	}
	
	private void addMean(double[] mean, double[] fit) {
		for (int i=0; i<mean.length; i++) mean[i] += fit[i];
	}
	
	private boolean secondIsBetter(IndividualInterface indy1, IndividualInterface indy2) {
		if (indy1 == null) return true;
		if (indy2 == null) return false;
		if (indy1 instanceof AbstractEAIndividual) return ((AbstractEAIndividual)indy2).isDominatingDebConstraints((AbstractEAIndividual)indy1);
		return (indy1.isDominant(indy2));
	}
	
	public double[] getBestFitness() {
		return currentBestFit;
	}
	
	public IndividualInterface getBestSolution() {
		return bestIndivdualAllover;
	}
	
	public int getFitnessCalls() {
		return functionCalls;
	}
}
