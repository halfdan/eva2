package eva2.server.stat;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.IndividualInterface;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.Mathematics;


public abstract class AbstractStatistics implements InterfaceTextListener, InterfaceStatistics {
	private PrintWriter resultOut;
	public final static boolean TRACE = false;
	protected InterfaceStatisticsParameter m_StatsParams;
	
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
	protected double[] meanBestOfRunFitness;
	protected double avgPopDist;
	protected double maxPopDist;
	protected IndividualInterface bestCurrentIndividual, bestRunIndividual, bestIndividualAllover;

	
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
	
	/**
	 * Collect start date and time of the run and if indicated, open a file output stream.
	 *  
	 * @param infoString
	 */
	protected void initOutput(String infoString) {
		SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_at_'hh.mm.ss");
		String startDate = formatter.format(new Date());
		// open the result file:
		if ((m_StatsParams.getOutputTo().getSelectedTagID()!=1) // not "text only" 
				&& (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_NONE)) { // verbosity accordingly high
			//!resFName.equalsIgnoreCase("none") && !resFName.equals("")) {
			String fname = makeOutputFileName(m_StatsParams.getResultFilePrefix(), infoString, startDate);
			if (TRACE) System.out.println("FileName =" + fname);
			try {
				resultOut = new PrintWriter(new FileOutputStream(fname));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error: " + e);
			}
			resultOut.println("StartDate:" + startDate);
			resultOut.println("On Host:" + getHostName());
		} else resultOut = null;
	}
	
	private String makeOutputFileName(String prefix, String infoString, String startDate) {
		return (prefix + "_" + infoString).replace(' ', '_') + "_" + startDate + ".txt";
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
			initOutput(infoString);
			bestIndividualAllover = null;
			meanBestOfRunFitness = null;
			if (refineMultiRuns) meanCollection = new ArrayList<double[][]>();
			else meanCollection = null;
		}
		bestCurrentIndividual = null;
		bestRunIndividual = null;
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
	
	public void stopOptPerformed(boolean normal, String stopMessage) {
		if (TRACE) System.out.println("AbstractStatistics.stopOptPerformed");
		if (runIterCnt < meanCollection.size()) {
			// no good: later run was shorter than the first one. What to do? Discard the longer one:
			if (TRACE) System.err.println("Error in AbstractStatistics: later run was shorter than earlier one... discarding rest...");
			for (int i=meanCollection.size()-1; i>=runIterCnt; i--) meanCollection.remove(i);
		}
		optRunsPerformed++;
		functionCallSum += functionCalls;
		if (printRunStoppedVerbosity() && (stopMessage != null)) printToTextListener(" Termination message: " + stopMessage + "\n");
		if (printRunStoppedVerbosity()) printToTextListener(" Function calls run: " + functionCalls + ", sum: " + functionCallSum + "\n");
		// check for convergence
		if (bestCurrentIndividual != null) {
			if (Mathematics.norm(bestCurrentIndividual.getFitness()) < this.m_StatsParams.getConvergenceRateThreshold()) {
				convergenceCnt++;
			}
			if (printRunStoppedVerbosity()) printToTextListener(" Last best individual	: " + BeanInspector.toString(bestCurrentIndividual) + "\n");
			if (printRunStoppedVerbosity()) printToTextListener("	Last solution data	: " + AbstractEAIndividual.getDefaultDataString(bestCurrentIndividual) + "\n");
			if (printRunStoppedVerbosity()) printToTextListener("	Last solution fit	: " + BeanInspector.toString(bestCurrentIndividual.getFitness()) + "\n");
		}		
		if (bestRunIndividual != null) {
			if (printRunStoppedVerbosity()) printToTextListener("  Run best individual	: " + BeanInspector.toString(bestRunIndividual) + "\n");
			if (printRunStoppedVerbosity()) printToTextListener("	Run solution data	: " + AbstractEAIndividual.getDefaultDataString(bestRunIndividual) + "\n");
			if (printRunStoppedVerbosity()) printToTextListener("	Run solution fit	: " + BeanInspector.toString(bestRunIndividual.getFitness()) + "\n");
			if (meanBestOfRunFitness==null) {
				meanBestOfRunFitness=bestRunIndividual.getFitness().clone();
			} else addMean(meanBestOfRunFitness, bestRunIndividual.getFitness());
		}
//		if (currentBestFit!= null) {
//			if (printRunStoppedVerbosity()) printToTextListener(" Best Fitness: " + BeanInspector.toString(currentBestFit) + "\n");
//		}
		if (optRunsPerformed == m_StatsParams.getMultiRuns()) finalizeOutput();
	}
	
	protected void finalizeOutput() {
		if (printFinalVerbosity()) printToTextListener("*******\n Runs performed: " + optRunsPerformed + ", reached target " + convergenceCnt + " times with threshold " + m_StatsParams.getConvergenceRateThreshold() + ", rate " + convergenceCnt/(double)m_StatsParams.getMultiRuns() + '\n');
		if (printFinalVerbosity()) printToTextListener(" Average function calls: " + (functionCallSum/optRunsPerformed) + "\n");
		if (printFinalVerbosity() && (bestIndividualAllover != null)) printToTextListener(" Overall best individual: " + BeanInspector.toString(bestIndividualAllover) + '\n');
		if (printFinalVerbosity() && (bestIndividualAllover != null)) printToTextListener("     Overall solution data	: " + AbstractEAIndividual.getDefaultDataString(bestIndividualAllover) + '\n');
		if (printFinalVerbosity() && (bestIndividualAllover != null)) printToTextListener("     Overall solution fit	: " + BeanInspector.toString(bestIndividualAllover.getFitness()) + '\n');
		if (optRunsPerformed>1) {
			if (meanBestOfRunFitness!=null) {
				Mathematics.svDiv((double)optRunsPerformed, meanBestOfRunFitness, meanBestOfRunFitness);
				if (printFinalVerbosity()) {
					printToTextListener(" Averaged best fitness per run: " + BeanInspector.toString(meanBestOfRunFitness)+"\n");
				}
			}
			if (refineMultiRuns && (meanCollection != null)) {
				if (printFinalVerbosity()) printToTextListener(" Averaged performance:\n");
				for (int i=0; i<meanCollection.size(); i++) divideMean(meanCollection.get(i), optRunsPerformed);
				if (printFinalVerbosity()) printToTextListener(refineToText(meanCollection, showAvgIntervals));
			}
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
	
	protected String getOutputHeader(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
		
		String headline = "Fit.-calls \t Best \t Mean \t Worst ";
		if ((informerList == null) || !m_StatsParams.isOutputAdditionalInfo()) {
			return headline;
		} else {
			for (InterfaceAdditionalPopulationInformer informer : informerList) {
				headline = headline + "\t " + informer.getAdditionalFileStringHeader(pop);
			}
			return headline;
		}
	}
	
	protected String getOutputLine(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
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
		if (informerList != null && m_StatsParams.isOutputAdditionalInfo()) {
			for (InterfaceAdditionalPopulationInformer informer : informerList) {
				sbuf.append(" \t ");
				sbuf.append(informer.getAdditionalFileStringValue(pop));
			}
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
	public abstract void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList);
	
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
			pop, List<InterfaceAdditionalPopulationInformer> informerList) {
		if (firstPlot) {
			initPlots(m_StatsParams.getPlotDescriptions());
//			if (doTextOutput()) printToTextListener(getOutputHeader(informer, pop)+'\n');
			firstPlot = false;
		}
		if ((runIterCnt==0) && printHeaderByVerbosity()) printToTextListener(getOutputHeader(informerList, pop)+'\n');

		if (pop.getSpecificData() != null) {
			plotSpecificData(pop, informerList);
			return;
		}
		// by default plotting only the best
		bestCurrentIndividual = pop.getBestIndividual().getClone();
		if ((bestIndividualAllover == null) || (secondIsBetter(bestIndividualAllover, bestCurrentIndividual))) {
			bestIndividualAllover = bestCurrentIndividual;
//			printToTextListener("new best found!, last was " + BeanInspector.toString(bestIndividualAllover) + "\n");
		}
		if ((bestRunIndividual==null) || (secondIsBetter(bestRunIndividual, bestCurrentIndividual))) {
			bestRunIndividual=bestCurrentIndividual;
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
		if (m_StatsParams.getPlotData().getSelectedTag().getID() == StatsParameter.PLOT_BEST_AND_MEASURES) {
			double[] measures = pop.getPopulationMeasures();
			if (measures != null) {
				avgPopDist = measures[0];
				maxPopDist = measures[2];
			}
		}
		
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
		
		if (doTextOutput() && printLineByVerbosity(runIterCnt)) printToTextListener(getOutputLine(informerList, pop)+'\n');
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
	
	/**
	 * Compare two individual interfaces and return true if the second one is dominant.
	 * 
	 * @param indy1
	 * @param indy2
	 * @return true if the second individual is dominant, else false
	 */
	public static boolean secondIsBetter(IndividualInterface indy1, IndividualInterface indy2) {
		if (indy1 == null) return true;
		if (indy2 == null) return false;
		if (indy1 instanceof AbstractEAIndividual) return ((AbstractEAIndividual)indy2).isDominatingDebConstraints((AbstractEAIndividual)indy1);
		return (indy2.isDominant(indy1));
	}
	
	public double[] getBestFitness() {
		return currentBestFit;
	}
	
	public IndividualInterface getBestSolution() {
		return bestIndividualAllover;
	}
	
	public IndividualInterface getRunBestSolution() {
		return bestRunIndividual;
	}
	
	public int getFitnessCalls() {
		return functionCalls;
	}
}
