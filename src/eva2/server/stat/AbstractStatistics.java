package eva2.server.stat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.IndividualInterface;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Pair;
import eva2.tools.StringSelection;
import eva2.tools.StringTools;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;

/**
 * An abstract class handling statistics. Most important stuff happens in startOptPerformed, stopOptPerformed
 * and createNextGenerationPerformed. Any measures (run based or multi-run based) are reset in startOptPerformed, 
 * updated per iteration in createNextGenerationPerformed and reported to listeners in stopOptPerformed.
 * Several different verbosity levels are regarded.  
 * The method plotCurrentResults should be implemented to plot further results per iteration. 
 * 
 * All displayable data is now routed through a single pipeline, which consists in a
 * list of Objects assembled in the getOutputValues method. This allows all simple data types which are
 * provided by the external informer instances to be handled uniformly to the internally collected data, and
 * thus they can be plotted and text-dumped in the same manner.
 * Basic fields are identified by the enum GraphSelectionEnum and are available independently of additional
 * informer instances.
 *
 * Depending on the field selection state and the informers, the list of data fields is dynamically altered,
 * however changes during a multi-run are ignored, since the potential of inconsistencies is too high.  
 * 
 * Listeners implementing InterfaceTextListener receive String output (human readable). 
 * Listeners implementing InterfaceStatisticsListener receive the raw data per iteration.
 * 
 * @see StatsParameter
 * @author mkron
 *
 */
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
//	private ArrayList<double[][]> meanCollection;
	private ArrayList<Object[]> finalObjectData;
	private ArrayList<Double[]> sumDataCollection; // collect summed-up data of multiple runs indexed per iteration
	protected Object[] currentStatObjectData = null; // the raw Object data collected in an iteration
	protected Double[] currentStatDoubleData = null; // the parsed doubles collected in an iteration (or null for complex data fields)
	protected String[] currentStatHeader = null; // the header Strings of the currently provided data
	protected String[] currentStatMetaInfo = null; // meta information on the statistical data
	private Double[] statDataSumOverAll = null;
//	, lastAdditionalInfoSums=null;

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
//	protected double[] meanBestFeasibleFit;
	protected double[] currentMeanFit;
	protected double[] currentWorstFit;
//	protected double[] meanBestOfRunFitness;
	protected double currentAvgPopDist;
	protected double currentMaxPopDist;
	protected IndividualInterface bestCurrentIndy, bestOfRunIndy, bestOfRunFeasibleIndy, bestFeasibleAllRuns, bestIndyAllRuns;

		// collect feasible results of a run 
	private ArrayList<IndividualInterface> runBestFeasibleList;
	private ArrayList<IndividualInterface> runBestFitList;
	
	private transient ArrayList<InterfaceTextListener> textListeners;
	private transient List<InterfaceStatisticsListener> dataListeners = null;

	private List<InterfaceAdditionalPopulationInformer> lastInformerList = null;
	private PopulationInterface lastSols = null;
	private String textFieldDelimiter = "\t";
	private int defaultFitCriterion = 0; // TODO this might be a user chosen int - or even more elegantly, a MOSOConverter
	
	protected StringSelection lastFieldSelection = null; // store the graph selection at the beginning of a multi-run
	protected boolean lastIsShowFull = false; // store the "show full text" stats property at the beginning of a multi-run
	
	public AbstractStatistics() {
		firstPlot = true;
		functionCalls = 0;
		functionCallSum = 0;
		convergenceCnt = 0;
		optRunsPerformed = 0;
		iterationCounter = 0;
		textListeners = new ArrayList<InterfaceTextListener>();
	}
	
	public void addDataListener(InterfaceStatisticsListener l) {
		if (dataListeners==null) {
			dataListeners=new LinkedList<InterfaceStatisticsListener>();
		}
		if (l!=null && !dataListeners.contains(l)) dataListeners.add(l);
	}
	
	public boolean removeDataListener(InterfaceStatisticsListener l) {
		if (dataListeners==null) return false;
		else return dataListeners.remove(l);
	}
	
	private void fireDataListeners() {
		if (dataListeners!=null) for (InterfaceStatisticsListener l : dataListeners) {
			l.notifyGenerationPerformed(currentStatHeader, currentStatObjectData, currentStatDoubleData);
		}
	}
	
	/**
	 * Notify listeners on the start and stop of a run.
	 * 
	 * @param runNumber current run (started or stopped)
	 * @param normal in case of stop: the stop was terminated normally (as opposed to manually)
	 * @param start if true, give the start signal, otherwise the stop signal
	 */
	private void fireDataListenersStartStop(int runNumber, boolean normal, boolean start) {
		if (dataListeners!=null) for (InterfaceStatisticsListener l : dataListeners) {
				if (start) {
					l.notifyRunStarted(runNumber, m_StatsParams.getMultiRuns(),
							currentStatHeader, currentStatMetaInfo);
				} else {
					l.notifyRunStopped(optRunsPerformed, normal);
					if (optRunsPerformed > 1) {
						l.finalMultiRunResults(currentStatHeader,
								finalObjectData);
					}
			}
		}
	}
	
	public void addTextListener(InterfaceTextListener listener) {
		if (!textListeners.contains(listener)) {
			textListeners.add(listener);
		}
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
		String startDate = getDateString();
		// open the result file:
		if (doFileOutput()  // not "text-window only" 
				&& (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_NONE)) { // verbosity accordingly high
			//!resFName.equalsIgnoreCase("none") && !resFName.equals("")) {
			String fnameBase = makeOutputFileName(m_StatsParams.getResultFilePrefix(), infoString, startDate);
			int cnt=0;
			String fname = fnameBase;
			while (new File(fname).exists()) {
				cnt++;
				fname=fnameBase+"."+cnt;
			}
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
	
	/**
	 * Return a simple String describing the current date and time.
	 * @return
	 */
	public static String getDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_at_'HH.mm.ss");
		String dt = formatter.format(new Date());
		return dt;
	}
	
	protected boolean doFileOutput() {
		return (m_StatsParams.getOutputTo().getSelectedTagID()!=1);  // not "text-window only" 
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

	public void startOptPerformed(String infoString, int runNumber, Object params, List<InterfaceAdditionalPopulationInformer> informerList) {
		if (TRACE) {
			System.out.println("AbstractStatistics.startOptPerformed " + runNumber);
			System.out.println("Statsparams were " + BeanInspector.toString(m_StatsParams));
		}
		
		if (runNumber == 0) {
			// store the intial graph selection state, so that modifications during runtime cannot cause inconsistencies
			lastFieldSelection = (StringSelection)m_StatsParams.getFieldSelection().clone();
			lastIsShowFull = m_StatsParams.isOutputAllFieldsAsText();

			currentStatDoubleData=null;
			currentStatObjectData=null;
			
			List<String> headerFields=getOutputHeaderFieldNames(informerList);
			currentStatHeader = headerFields.toArray(new String[headerFields.size()]);
			currentStatMetaInfo = getOutputMetaInfoAsArray(informerList);
			
			functionCallSum = 0;
			firstPlot = true;
			optRunsPerformed = 0;
			convergenceCnt = 0;
			if (saveParams) m_StatsParams.saveInstance();
			initOutput(infoString);
			bestIndyAllRuns = null;
			bestFeasibleAllRuns = null;
//			meanBestOfRunFitness = null;
//			meanBestFeasibleFit = null;
			runBestFeasibleList = new ArrayList<IndividualInterface>();
			runBestFitList = new ArrayList<IndividualInterface>();
//			if (refineMultiRuns) meanCollection = new ArrayList<double[][]>();
//			else meanCollection = null;
			if (refineMultiRuns) sumDataCollection = new ArrayList<Double[]>();
			else sumDataCollection = null;

			finalObjectData = null;

			statDataSumOverAll = null;
//			lastAdditionalInfoSums = null;
			feasibleFoundAfterSum=-1;
			numOfRunsFeasibleFound=0;
			
		}
		feasibleFoundAfter=-1;
		bestCurrentIndy = null;
		bestOfRunIndy = null;
		currentBestFeasibleFit=null;
		bestOfRunFeasibleIndy = null;
		lastInformerList = null;
		lastSols = null;
		iterationCounter = 0;
    	if (printRunIntroVerbosity()) printToTextListener("\n****** Multirun "+runNumber);
    	if (params != null) {
    		if (printRunIntroVerbosity()) printToTextListener("\nModule parameters: ");
    		if (printRunIntroVerbosity()) printToTextListener(BeanInspector.niceToString(params));
    	}
    	if (printRunIntroVerbosity()) printToTextListener("\nStatistics parameters: ");
    	if (printRunIntroVerbosity()) printToTextListener(BeanInspector.niceToString(getStatisticsParameter()) + '\n');
		functionCalls = 0;
		fireDataListenersStartStop(runNumber, true, true);
	}
	
	public void stopOptPerformed(boolean normal, String stopMessage) {
		if (TRACE) System.out.println("AbstractStatistics.stopOptPerformed");
		if (lastSols==null) System.err.println("WARNING, possibly there was no call to createNextGenerationPerformed before calling stopOptPerformed (AnstractStatistics).");

		if (iterationCounter < sumDataCollection.size()) {
			// no good: later run was shorter than the first one. What to do? Discard the longer one:
			if (TRACE) System.err.println("Error in AbstractStatistics: later run was shorter than earlier one... discarding rest...");
			for (int i=sumDataCollection.size()-1; i>=iterationCounter; i--) sumDataCollection.remove(i);
		}
		optRunsPerformed++;
		functionCallSum += functionCalls;
		if (printRunStoppedVerbosity() && (stopMessage != null)) printToTextListener(" Termination message: " + stopMessage + "\n");
		if (printRunStoppedVerbosity()) printToTextListener(" Function calls run: " + functionCalls + ", sum: " + functionCallSum + "\n");
		// check if target zero was reached
		if (bestCurrentIndy != null) {
			if (Mathematics.norm(bestOfRunIndy.getFitness()) < this.m_StatsParams.getConvergenceRateThreshold()) {
				convergenceCnt++;
			}
			if (printRunStoppedVerbosity()) printIndy("Last best", bestCurrentIndy);
		}		
		if (bestOfRunIndy != null) {
			runBestFitList.add(bestOfRunIndy);
			if (printRunStoppedVerbosity()) printIndy("Run best", bestOfRunIndy);
//			if (meanBestOfRunFitness==null) {
//				meanBestOfRunFitness=bestRunIndividual.getFitness().clone();
//			} else addSecond(meanBestOfRunFitness, bestRunIndividual.getFitness());
		}
		if (feasibleFoundAfter>0) {
			if (printRunStoppedVerbosity()) printToTextListener(" Feasible ind. found after " + feasibleFoundAfter + " evaluations.\n");
		} else {
			if (printRunStoppedVerbosity()) printToTextListener(" NO feasible individual found.\n");
		}
		if (printRunStoppedVerbosity()) {
			printToTextListener(" Solution correlations (min,max,avg,med,var): " + BeanInspector.toString(((Population)lastSols).getCorrelations((Population)lastSols)) + "\n");
		}
		if (bestOfRunFeasibleIndy != null) { 
			runBestFeasibleList.add(bestOfRunFeasibleIndy);
//			if (meanBestFeasibleFit==null) {
//				meanBestFeasibleFit=bestRunFeasibleIndy.getFitness().clone();
//			} else addSecond(meanBestFeasibleFit, bestRunFeasibleIndy.getFitness());
			if (printRunStoppedVerbosity()) {
				if ((bestOfRunFeasibleIndy instanceof AbstractEAIndividual) && ((AbstractEAIndividual)bestOfRunFeasibleIndy).equalGenotypes((AbstractEAIndividual)bestOfRunIndy)) {
					printToTextListener("* Run best feasible individual equals best individual.\n");
				} else {
					if (bestOfRunIndy instanceof AbstractEAIndividual) {
						if (((AbstractEAIndividual)bestOfRunIndy).violatesConstraint())
							printToTextListener(" Run best individual violates constraints by " + ((AbstractEAIndividual)bestOfRunIndy).getConstraintViolation() + "\n");
						if (((AbstractEAIndividual)bestOfRunIndy).isMarkedPenalized())
							printToTextListener(" Run best individual is penalized.\n");
					}
					printIndy("Run best feasible", bestOfRunFeasibleIndy);
				}
			}
		}
		if (finalObjectData==null) finalObjectData = new ArrayList<Object[]>();
		finalObjectData.add(currentStatObjectData);
		
		if (!printRunStoppedVerbosity() && printFinalVerbosity()) printToTextListener(".");
//		if (currentBestFit!= null) {
//			if (printRunStoppedVerbosity()) printToTextListener(" Best Fitness: " + BeanInspector.toString(currentBestFit) + "\n");
//		}

		fireDataListenersStartStop(optRunsPerformed, normal, false);
	}

	public void postProcessingPerformed(Population resultPop) { // called from processor
		if (!printRunStoppedVerbosity() && printFinalVerbosity() && optRunsPerformed >= m_StatsParams.getMultiRuns()) printToTextListener("\n");
		if (printRunStoppedVerbosity()) {
			if (resultPop!=null && (resultPop.size()>0)) {
				printToTextListener("Resulting population: \n");
				for (int i=0; i<resultPop.size(); i++) {
					printToTextListener(AbstractEAIndividual.getDefaultStringRepresentation(resultPop.getEAIndividual(i)));
					printToTextListener("\n");
				}
			}
		}
		if (optRunsPerformed >= m_StatsParams.getMultiRuns()) {
			finalizeOutput();
		}
	}
	
	private PopulationInterface makeStatsPop() {
		Population pop = new Population(4);
		
		if (bestCurrentIndy!=null) pop.add(bestCurrentIndy);
		if (bestOfRunIndy!=null) pop.add(bestOfRunIndy);
		if (bestOfRunFeasibleIndy!=null) pop.add(bestOfRunFeasibleIndy);
		if (bestIndyAllRuns!=null) pop.add(bestIndyAllRuns);
		return pop;
	}

	private void printIndy(String prefix, IndividualInterface indy) {
		printToTextListener("* " + prefix + " ind.: " + BeanInspector.toString(indy) + '\n');
		printToTextListener("         solution data	: " + AbstractEAIndividual.getDefaultDataString(indy) + '\n');
		printToTextListener("         solution fit	: " + BeanInspector.toString(indy.getFitness()));
		if (!(indy instanceof AbstractEAIndividual)) printToTextListener(" - feasibility unknown\n");
		else {
			if (((AbstractEAIndividual)indy).isMarkedPenalized() || ((AbstractEAIndividual)indy).violatesConstraint()) printToTextListener(" - infeasible\n");
			else printToTextListener("\n");
		}

	}

	/**
	 * Calculate the mean fitness of final best individuals over the last series of multi-runs.
	 *  
	 * @return
	 */
	public double[] getMeanBestFit(boolean requireFeasible) {
		return calcMeanFit(requireFeasible ? runBestFeasibleList : runBestFitList);
	}
	
	/**
	 * Calculate the median fitness of final best individuals over the last series of multi-runs.
	 *  
	 * @return
	 */
	public double[] getMedianBestFit(boolean requireFeasible) {
		return calcMedianFit(requireFeasible ? runBestFeasibleList : runBestFitList);
	}
	
	protected void finalizeOutput() {
		if (printFinalVerbosity()) printToTextListener("*******\n Runs performed: " + optRunsPerformed + ", reached target " + convergenceCnt + " times with threshold " + m_StatsParams.getConvergenceRateThreshold() + ", rate " + convergenceCnt/(double)m_StatsParams.getMultiRuns() + '\n');
		if (printFinalVerbosity()) printToTextListener(" Average function calls: " + (functionCallSum/optRunsPerformed) + "\n");
		
		if (printFinalVerbosity() && (feasibleFoundAfterSum>=0.)) {
			printToTextListener("     Feasible solution found in " + numOfRunsFeasibleFound + " of " + optRunsPerformed + " runs \n");
			printToTextListener("     Average evaluations until feasible ind. was found in " + numOfRunsFeasibleFound + " runs: " + feasibleFoundAfterSum/numOfRunsFeasibleFound + " evaluations\n");
		}

		if (printFinalVerbosity() && (statDataSumOverAll != null)) {
			printToTextListener("     Averaged sum of run statistical data: (" +  optRunsPerformed + " runs):");
			for (int i=0; i<statDataSumOverAll.length; i++) if (statDataSumOverAll[i]!=null) printToTextListener(textFieldDelimiter+(statDataSumOverAll[i]/optRunsPerformed));
			printToTextListener("\n     Averaged last statistical data (" +  optRunsPerformed + " runs):");
			Double[] lastSum = sumDataCollection.get(sumDataCollection.size()-1);
			for (int i=0; i<lastSum.length; i++) if (lastSum[i]!=null) printToTextListener(textFieldDelimiter+(lastSum[i]/optRunsPerformed));
//			for (int i=0; i<lastAdditionalInfoSums.length; i++) if (lastAdditionalInfoSums[i]!=null) printToTextListener(" \t"+(lastAdditionalInfoSums[i]/optRunsPerformed));
			printToTextListener("\n");
		}

		if (printFinalVerbosity() && (bestIndyAllRuns != null)) printIndy("Overall best", bestIndyAllRuns);
		if (printFinalVerbosity()) printToTextListener(getFinalAdditionalInfo()+'\n');

		if (optRunsPerformed>1) {
			if (runBestFitList.size()>0) {
//				Mathematics.svDiv((double)optRunsPerformed, meanBestOfRunFitness, meanBestOfRunFitness);
				if (printFinalVerbosity()) {
					double[] meanBestFit=getMeanBestFit(false);
					printToTextListener(" MultiRun stats: Mean best fitness: " + BeanInspector.toString(meanBestFit)+"\n");
					if (meanBestFit.length==1) printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFitList, meanBestFit[0])) + "\n");
					printToTextListener(" MultiRun stats: Median best fitn.: " + BeanInspector.toString(getMedianBestFit(false))+"\n");
				}
			}
			if (printFinalVerbosity() && (bestFeasibleAllRuns != null)) printIndy("Overall best feasible", bestFeasibleAllRuns);
//			if ((runBestFeasibleList.size()>0) && (!equalLists(runBestFeasibleList, runBestFitList))) { // is there a difference between best feasibles and best fit?
			if (runBestFeasibleList.size()>0) { // always output feasible stats even if theyre equal
				if (printFinalVerbosity()) {
					double[] meanBestFeasibleFit=getMeanBestFit(true);
					printToTextListener(" MultiRun stats: Mean best feasible fitness (" + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(meanBestFeasibleFit)+"\n");
					if (meanBestFeasibleFit.length==1) printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFeasibleList, meanBestFeasibleFit[0])) + "\n");
					printToTextListener(" MultiRun stats: Median best feasible fitn. (: " + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(getMedianBestFit(true))+"\n");
				}
			}
			if (refineMultiRuns && (sumDataCollection != null)) {
				if (printFinalVerbosity()) printToTextListener(" Averaged performance:\n");
				// the summed-up values of the mean collection is divided by the number of runs
				for (int i=0; i<sumDataCollection.size(); i++) divideMean(sumDataCollection.get(i), optRunsPerformed);
				if (printFinalVerbosity()) printToTextListener(refineToText(sumDataCollection, showAvgIntervals));
			}
			if (printFinalVerbosity() && (finalObjectData!=null)) {
				printToTextListener(" Last data line of " + finalObjectData.size() + " multi-runs:\n" );
				for (int i=0; i<finalObjectData.size(); i++) {
					printToTextListener(BeanInspector.toString(finalObjectData.get(i)));
					printToTextListener("\n");
				}
			}
		}

		if (TRACE)
			System.out.println("End of run");
		if (resultOut != null) {
			String StopDate = getDateString();
			resultOut.println("StopDate:" + StopDate);
			resultOut.close();
		}
	}
	
	private String getFinalAdditionalInfo() {
		PopulationInterface bestPop = makeStatsPop();
//		List<String> additionalFields = getAdditionalInfoHeader(lastInformerList, bestPop);
		String additionalFields = getOutputHeaderFieldNamesAsString(lastInformerList);
//		String header = getOutputHeader(lastInformerList, bestPop);
		List<Object> vals = getOutputValues(lastInformerList, bestPop);
		
		StringBuffer sbuf = new StringBuffer("Overall best statistical data: ");
		sbuf.append(additionalFields);
		sbuf.append('\n');
		sbuf.append(StringTools.concatValues(vals, textFieldDelimiter));
//		appendAdditionalInfo(lastInformerList, bestPop, sbuf);
//		getOutputLine(lastInformerList, makeStatsPop());
		return sbuf.toString();
	}

	private double[] calcStdDevVar(ArrayList<IndividualInterface> list, double meanFit) {
    	double tmp=0, sum=0;
    	for (Iterator<IndividualInterface> iter = list.iterator(); iter.hasNext();) {
			IndividualInterface indy = iter.next();
			tmp=indy.getFitness()[0]-meanFit;
			sum+=(tmp*tmp);
		}
    	double[] res = new double[2];
    	res[0]=sum/list.size();
    	res[1]=Math.sqrt(res[0]);
    	return res;
	}

	/**
	 * Calculate the mean fitness of a list of individuals.
	 * @param list
	 * @return
	 */
    public static double[] calcMeanFit(List<IndividualInterface> list) {
		double[] sumFit = list.get(0).getFitness().clone();
		for (int i=1; i<list.size(); i++) Mathematics.vvAdd(sumFit, list.get(i).getFitness(), sumFit);
		Mathematics.svDiv(list.size(), sumFit, sumFit);
		
		return sumFit;
	}
	
    public static double[] calcMedianFit(List<IndividualInterface> list) {
    	ArrayList<double[]> dblAList = new ArrayList<double[]>(list.size());
    	for (int i=0; i<list.size(); i++) dblAList.add(list.get(i).getFitness());
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
    	double step = data.size()/(iterationsToShow-1.);
    	int printedIteration=0;
    	Double[] meanData;
        for(int i = 1; i < data.size()+1; i++) {
        	// print the first, last and intermediate iterations requested by the integer parameter
        	// first one is printed always, as printedIteration=0
        	if ((i==data.size()) || ((i-1)==Math.round(printedIteration*step))) {
        		printedIteration++;
        		meanData = data.get(i-1);
        		sbuf.append(i);
        		for (int k=0; k<meanData.length; k++) {
            		sbuf.append(delim);
            		sbuf.append(BeanInspector.toString(meanData[k]));
        		}
        		sbuf.append("\n");
        	}
        }
		
//    	double[][] mean;
//    	StringBuffer sbuf = new StringBuffer("Iteration\tFun.Calls\tBest\tMean\tWorst\n");
//    	double step = result.size()/(iterationsToShow-1.);
//    	int printedIteration=0;
//    	
//        for(int i = 1; i < result.size()+1; i++) {
//        	// print the first, last and intermediate iterations requested by the integer parameter
//        	// first one is printed always, as printedIteration=0
//        	if ((i==result.size()) || ((i-1)==Math.round(printedIteration*step))) {
//        		printedIteration++;
//        		mean = result.get(i-1);
//        		sbuf.append(i);
//        		sbuf.append("\t");
//        		sbuf.append(BeanInspector.toString(mean[0]));
//        		sbuf.append("\t");
//        		sbuf.append(BeanInspector.toString(mean[1]));
//        		sbuf.append("\t");
//        		sbuf.append(BeanInspector.toString(mean[2]));
//        		sbuf.append("\t");
//        		sbuf.append(BeanInspector.toString(mean[3]));
//        		sbuf.append("\n");
//        	}
//        }
//        return sbuf.toString();
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
	
	/**
	 * Collect all field names of both internal fields and fields of external informers. Then 
	 * concatenate them to a string using the textFieldDelimiter of the instance.
	 * 
	 * @param informerList
	 * @param pop
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
	 * @param pop
	 * @return
	 */
	protected List<String> getOutputHeaderFieldNames(List<InterfaceAdditionalPopulationInformer> informerList) {
		ArrayList<String> headlineFields = new ArrayList<String>(5);
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
	 * @param pop
	 * @return
	 */
	protected List<String> getOutputMetaInfo(List<InterfaceAdditionalPopulationInformer> informerList) {
		ArrayList<String> infoStrings = new ArrayList<String>(5);
		ArrayList<String> addStrings = new ArrayList<String>(5);
		infoStrings.addAll(Arrays.asList(getSimpleOutputMetaInfo())); 
		if (informerList != null) {
			getAdditionalHeaderMetaInfo(informerList, addStrings);
		}
		infoStrings.addAll(addStrings);
		return infoStrings;
	}

	protected String[] getOutputMetaInfoAsArray(List<InterfaceAdditionalPopulationInformer> informerList) {
		List<String> metaStrings = getOutputMetaInfo(informerList);
		return metaStrings.toArray( new String[metaStrings.size()]);
	}
	
	/**
	 * Collect the names of data fields which are collected internally.This must correspond to the
	 * method {@link #getSimpleOutputValues()}.
	 * Basic fields are identified by the enum GraphSelectionEnum and are available independently of additional
	 * informer instances.
	 * @see #getSimpleOutputValues() 
	 * @return
	 */
	protected String[] getSimpleOutputHeader() {
		// collect the full header by using the entries of the GraphSelectionEnum
		GraphSelectionEnum[] vals = GraphSelectionEnum.values();
		ArrayList<String>headerEntries = new ArrayList<String>();
		headerEntries.add("FunctionCalls");
		for (int i=0; i<vals.length; i++) {
			if (isRequestedField(vals[i])) headerEntries.add(vals[i].toString());
		}
//		return new String[]{"Fun.calls","Best","Mean", "Worst"};
		return headerEntries.toArray(new String[headerEntries.size()]);
	}
	
	/**
	 * Collect the info strings of data fields collected internally. This must correspond to
	 * the method {@link #getSimpleOutputValues()}.
	 * Basic meta info is defined by the enum GraphSelectionEnum.
	 * 
	 * @see #getSimpleOutputValues() 
	 * @return 
	 */
	protected String[] getSimpleOutputMetaInfo() {
		GraphSelectionEnum[] vals = GraphSelectionEnum.values();
		ArrayList<String> headerInfo = new ArrayList<String>();
		headerInfo.add("The number of function evaluations");
		for (int i=0; i<vals.length; i++) {
			if (isRequestedField(vals[i])) headerInfo.add(GraphSelectionEnum.getInfoStrings()[i]);
		}
//		return new String[]{"Fun.calls","Best","Mean", "Worst"};
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
	 * @see #getSimpleOutputHeader()
	 * @return
	 */
	protected Object[] getSimpleOutputValues() {
		GraphSelectionEnum[] selEnumVals=null;
		selEnumVals = GraphSelectionEnum.values();
//		else selEnumVals = (GraphSelectionEnum[]) (m_StatsParams.getGraphSelection().getSelectedEnum(GraphSelectionEnum.values()));
		Object[] ret = new Object[1+selEnumVals.length];
		ret[0]=functionCalls;
		for (int i=1; i<=selEnumVals.length; i++) {
			switch (selEnumVals[i-1]) { // the field i+1 contains enum value i, because field 0 is reserved for the number of function calls 
//			currentBest, currentWorst, runBest, currentBestFeasible, runBestFeasible, avgPopDistance, maxPopDistance;
			case currentBest: ret[i]=currentBestFit[defaultFitCriterion ]; break;
			case meanFit: ret[i] = (currentMeanFit==null) ? Double.NaN : currentMeanFit[defaultFitCriterion]; break;
			case currentWorst: ret[i] = (currentWorstFit==null) ? Double.NaN : currentWorstFit[defaultFitCriterion]; break;
			case runBest: ret[i] = bestOfRunIndy.getFitness()[defaultFitCriterion]; break;
			case currentBestFeasible: ret[i] = (currentBestFeasibleFit==null) ? Double.NaN : currentBestFeasibleFit[defaultFitCriterion]; break;
			case runBestFeasible: ret[i] = (bestOfRunFeasibleIndy==null) ? Double.NaN : bestOfRunFeasibleIndy.getFitness()[defaultFitCriterion]; break;
			case avgPopDistance: ret[i] = currentAvgPopDist; break;
			case maxPopDistance: ret[i] = currentMaxPopDist; break;
			}
		}
		// all standard fields should be filled now
		return ret;
		
//		Object[] ret = new Object[4];
//		ret[0]=functionCalls;
//		ret[1]=currentBestFit;
//		if (meanFitness!=null) ret[2]=meanFitness;
//		else ret[2]="#";
//		if (currentWorstFit!=null) ret[3] = currentWorstFit;
//		else ret[3]="#";
//		return ret; 
	}
	
	/**
	 * Assemble a list of data fields which should be traced by the statistics class.
	 * Both internal fields as well as external informer data are collected in this list.
	 * It should be consistent with the getOutputHeader method, which provides the
	 * names of the corresponding fields in the same order.
	 * The length of this list depends on the field selection state.
	 * 
	 * @see #getOutputHeader(List, PopulationInterface)
	 * @param informerList
	 * @param pop
	 * @return
	 */
	protected List<Object> getOutputValues(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
		LinkedList<Object> values = new LinkedList<Object>();
		values.addAll(Arrays.asList(getSimpleOutputValues())); 
		if (informerList != null) {
			for (InterfaceAdditionalPopulationInformer informer : informerList) {
				List<Object> reqList = Arrays.asList(informer.getAdditionalDataValue(pop));
				values.addAll(reqList);
			}
		}
		// remove those which are not requested
		Iterator<Object> iter = values.iterator();
		int cnt=0;
		iter.next(); // skip the first field (function calls) which is not regarded here
		if (!lastIsShowFull) while (iter.hasNext()) {
			iter.next();
			if (!isRequestedField(cnt++)) iter.remove(); 
			// the cnt variable is one behind the index in the values list, because of the function calls field. 
		}
		return values;
//		return StringTools.concatValues(values, textFieldDelimiter);
	}

	/**
	 * Collect additional info header and (optionally) meta information for the fields selected.
	 * The length of this list depends on the field selection state.
	 * 
	 * @param informerList
	 * @param pop
	 * @param metaInfo if non null, the meta info strings are returned in this list
	 * @return
	 */
	protected List<String> getAdditionalHeaderMetaInfo(List<InterfaceAdditionalPopulationInformer> informerList, List<String> metaInfo) {
		LinkedList<String> headers = new LinkedList<String>();
		if (metaInfo!=null && (metaInfo.size()>0)) System.err.println("Warning, metaInfo list should be empty in AbstractStatistics.getAdditionalInfoInfo"); 
		for (InterfaceAdditionalPopulationInformer informer : informerList) {
			headers.addAll(Arrays.asList(informer.getAdditionalDataHeader()));
			if (metaInfo!=null) metaInfo.addAll(Arrays.asList(informer.getAdditionalDataInfo()));
//			hdr = hdr + "\t " + informer.getAdditionalDataHeader(pop);
		}
		Iterator<String> hIter = headers.iterator();
		Iterator<String> mIter = (metaInfo!=null) ? metaInfo.iterator() : null;  
		if (!lastIsShowFull) while (hIter.hasNext()) {
			if (mIter!=null && mIter.hasNext()) mIter.next();
			if (!isRequestedAdditionalField(hIter.next())) {
				hIter.remove();
				if (mIter!=null && mIter.hasNext()) mIter.remove();
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
	protected Pair<String,Object[]> getOutputData(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
		List<Object> statValues = getOutputValues(informerList, pop);
		String statValuesString = StringTools.concatValues(statValues, textFieldDelimiter);

		return new Pair<String,Object[]>(statValuesString, statValues.toArray(new Object[statValues.size()]));
	}
	
//	/**
//	 * Append additional informer informations to the given StringBuffer.
//	 * 
//	 * @param informerList
//	 * @param pop
//	 * @param sbuf
//	 */
//	protected Double[] appendAdditionalInfo(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop, StringBuffer sbuf) {
//		if (informerList != null) {
//			ArrayList<Object> additionalObjects = new ArrayList<Object>(5);
//			
//			for (InterfaceAdditionalPopulationInformer informer : informerList) {
//				additionalObjects.addAll(Arrays.asList(informer.getAdditionalDataValue(pop)));
//			}
//			String addInfo = StringTools.concatValues(additionalObjects, textFieldDelimiter);
//			Double[] retVals = parseDoubles(additionalObjects);
//			if (sbuf!=null) sbuf.append(addInfo);
//			return retVals;
//			
////			StringBuffer addBuffer = new StringBuffer();
////			for (InterfaceAdditionalPopulationInformer informer : informerList) {
////				addBuffer.append(" \t ");
////				addBuffer.append(informer.getAdditionalDataValue(pop));
////			}
////			String addInfo = addBuffer.toString().trim();
////			if (addInfo.startsWith("\t")) addInfo.substring(2); // remove first separator to avoid returning empty field as double
////			Double[] retVals = parseDoubles(addInfo, "\t");
////			if (sbuf!=null) sbuf.append(addInfo);
////			return retVals;
//		}
//		return null;
//	}	

	/**
	 * @deprecated The method {@link #createNextGenerationPerformed(PopulationInterface, List)} should be used instead.
	 */
	public synchronized void createNextGenerationPerformed(double[] bestfit,
			double[] worstfit, int calls) {
		functionCalls = calls;
		currentBestFit = bestfit;
		currentWorstFit = worstfit;
		currentBestFeasibleFit = null;
		currentMeanFit = null;
		
		if (firstPlot) {
			initPlots(null, null);
//			if (doTextOutput()) printToTextListener(getOutputHeader(null, null)+'\n');
			firstPlot = false;
		}
		if ((iterationCounter == 0) && printHeaderByVerbosity()) printToTextListener(getOutputHeaderFieldNamesAsString(null)+'\n');

		if (doTextOutput() && printLineByVerbosity(calls)) {
			Pair<String,Object[]> addInfo = getOutputData(null, null);
			printToTextListener(addInfo.head()+'\n');
			if (addInfo.tail()!=null) {
				statDataSumOverAll = updateSum(statDataSumOverAll, ToolBox.parseDoubles(addInfo.tail()));
			}
		}
		plotCurrentResults();
		iterationCounter++;
	}
	
	/**
	 * Add the given array to the member array. Do some checks etc.
	 * If a resultSum array is provided, it is used to add the info and returned. Otherwise
	 * a new array is allocated.
	 * 
	 * @param curInfo
	 */
	private static Double[] updateSum(Double[] resultSum, Double[] curInfo) {
		if (resultSum==null) {
			resultSum = curInfo.clone();
		} else {
			if (curInfo.length != resultSum.length) {
				System.err.println("Error in AbstractStatistics.updateAdditionalInfo: mismatching info arrays!");
			} else {
				for (int i=0; i<curInfo.length; i++) {
					if (resultSum[i]==null || (curInfo[i]==null)) resultSum[i]=null; 
					else resultSum[i]+=curInfo[i];
				}
			}
		}
		return resultSum;
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
	protected abstract void initPlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList);
	
	/**
	 * To set a list of informers (even before the actual run is started).
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
		if ((bestOfRunIndy==null) || (secondIsBetter(bestOfRunIndy, bestCurrentIndy))) {
			bestOfRunIndy=bestCurrentIndy;
		}
		if (bestCurrentIndy == null) {
			System.err.println("createNextGenerationPerformed BestInd==null");
		}
		currentBestFit = bestCurrentIndy.getFitness().clone();
		if (currentBestFit == null) {
			System.err.println("BestFitness==null !");
		}
		if (pop instanceof Population) {
			AbstractEAIndividual curBestFeasible = ((Population)pop).getBestFeasibleIndividual(-1);
			if (curBestFeasible!=null) { // a feasible ind. was found!
				if (currentBestFeasibleFit==null) { // feasible indy found for the first time 
					numOfRunsFeasibleFound++;
					feasibleFoundAfter=((Population)pop).getFunctionCalls();
					if (feasibleFoundAfterSum<0) feasibleFoundAfterSum=0.; // initial signaling value was -1.
					feasibleFoundAfterSum+=feasibleFoundAfter;
				}
				currentBestFeasibleFit = curBestFeasible.getFitness().clone();
				if ((bestOfRunFeasibleIndy==null) || (secondIsBetter(bestOfRunFeasibleIndy, curBestFeasible))) {
					bestOfRunFeasibleIndy=(AbstractEAIndividual)curBestFeasible.clone();
//					System.out.println("New best feasible: " + AbstractEAIndividual.getDefaultStringRepresentation((AbstractEAIndividual)bestRunFeasibleIndy));
				}
				if ((bestFeasibleAllRuns == null) || (secondIsBetter(bestFeasibleAllRuns, bestOfRunFeasibleIndy))) {
					bestFeasibleAllRuns = bestOfRunFeasibleIndy;
				}
			}
		} else System.err.println("INVALID POPULATION (AbstractStatistics)");
		
		// collect these data fields only if requested by the user
		if (lastIsShowFull || GraphSelectionEnum.doPlotMean(lastFieldSelection)) currentMeanFit = pop.getMeanFitness().clone();
		else currentMeanFit = null;
		if (lastIsShowFull || GraphSelectionEnum.doPlotWorst(lastFieldSelection)) currentWorstFit = pop.getWorstIndividual().getFitness().clone();
		else currentWorstFit = null;
		
		functionCalls = pop.getFunctionCalls();
		
		if (lastIsShowFull || GraphSelectionEnum.doPlotAvgDist(lastFieldSelection) 
				|| GraphSelectionEnum.doPlotMaxPopDist(lastFieldSelection))  {
			double[] measures = ((Population)pop).getPopulationMeasures((InterfaceDistanceMetric)null);
			if (measures != null) {
				currentAvgPopDist = measures[0];
				currentMaxPopDist = measures[2];
			}
		}
	}
	
	/**
	 * Do some data collection on the population.
	 *
	 */
	public synchronized void createNextGenerationPerformed(PopulationInterface
			pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList) {
		lastInformerList  = informerList;
		if (TRACE) printToTextListener(".. in createNextGenPerformed after " + pop.getFunctionCalls() + " evals.\n");
		if (resultOut != null) resultOut.flush(); 
		if (firstPlot) {
			initPlots(pop, informerList);
//			if (doTextOutput()) printToTextListener(getOutputHeader(informer, pop)+'\n');
			firstPlot = false;
			currentBestFeasibleFit=null;
		}
		if (TRACE) printToTextListener("A1\n");
		if (pop.getSpecificData() != null) { // this is more or less deprecated. the standard population implementation will always return null. However the ES module wont 
			plotSpecificData(pop, informerList);
			return;
		}

		if (TRACE) printToTextListener("A2\n");
		collectPopData(pop);
		if (TRACE) printToTextListener("A3\n");

		if (iterationCounter==0) {
			if (TRACE) printToTextListener("A3.1 " + currentStatHeader.length + "\n");
			
			String headerLine = StringTools.concatFields(currentStatHeader, textFieldDelimiter);
			if (TRACE) printToTextListener("A3.2\n");
			if (printHeaderByVerbosity()) printToTextListener(headerLine+'\n');
			if (TRACE) printToTextListener("A3.3\n");
		}
		if (TRACE) printToTextListener("A4\n");

		lastSols  = (opt!=null) ? new Population(opt.getAllSolutions().getSolutions()) : pop;
//		Pair<String,Double[]> addData = getOutputData(informerList, lastSols);
//		System.out.println("lastSols size: " + 500*PSymbolicRegression.getAvgIndySize(lastSols));
//		System.out.println("Mem use:  " + getMemoryUse());
		if (TRACE) printToTextListener("A5\n");

		Pair<String,Object[]> addData = getOutputData(informerList, lastSols);
		if (doTextOutput()) { // this is where the text output is actually written
			if (printLineByVerbosity(iterationCounter)) {
//				printToTextListener(functionCalls + textFieldDelimiter);
				printToTextListener(addData.head()+'\n');
			}
		}
		if (TRACE) printToTextListener("A6\n");

		currentStatObjectData = addData.tail();
		currentStatDoubleData = ToolBox.parseDoubles(currentStatObjectData);
		if (currentStatObjectData!=null) {
			statDataSumOverAll = updateSum(statDataSumOverAll, currentStatDoubleData); // this adds up all data of a single run
		} else {
			System.err.println("Warning in AbstractStatistics!");
		}
		if (TRACE) printToTextListener("A7\n");

		if (sumDataCollection != null) {
			// Collect average data
			Double[] sumDataEntry = null;
			if ((optRunsPerformed==0) && (sumDataCollection.size()<=iterationCounter)) { 
				// in the first run, newly allocate the arrays
				// assume that all later data sets will have the same format
//				means = new double[4][currentBestFit.length]; // this only fits fitness vectors! of course this is sensible for multi-crit fitnesses...
				sumDataEntry = currentStatDoubleData.clone();
				sumDataCollection.add(sumDataEntry);
			} else {
				if (sumDataCollection.size()<=iterationCounter) {// bad case!
					// may happen for dynamic pop-sizes, e.g. in Tribes, when runs do not necessarily send the
					// "generation performed" event the same number of times. 
					// thus: dont do an update for events that are "too late"
					sumDataEntry = null;
				} else sumDataEntry = sumDataCollection.get(iterationCounter);
				if (sumDataEntry != null) updateSum(sumDataEntry, currentStatDoubleData); // this adds up data of a single iteration across multiple runs
			}
		}
		if (TRACE) printToTextListener("A8\n");

//		if (doTextOutput()) {
//			Pair<String,Double[]> addInfo = getOutputLine(informerList, lastSols);
//
//			if (printLineByVerbosity(runIterCnt)) {
//				printToTextListener(addInfo.head()+'\n');
//			}
//			currentAdditionalInfo = addInfo.tail();
//			if (addInfo.tail()!=null) {
//				additionalInfoSums = updateAdditionalInfo(additionalInfoSums, addInfo.tail());
//			}
//		}
		plotCurrentResults();
		fireDataListeners();
		if (TRACE) printToTextListener(".. done createNextGenPerformed after " + pop.getFunctionCalls() + " evals.\n");
		if (resultOut != null) resultOut.flush(); 
		iterationCounter++;
	}

	/**
	 * Returns true if the given iteration is a verbose one according to StatsParameter - meaning
	 * that full iteration data should be plotted.
	 * 
	 * @param iteration
	 * @return
	 */
	private boolean printLineByVerbosity(int iteration) {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_KTH_IT) 
				|| ((m_StatsParams.getOutputVerbosity().getSelectedTagID() == StatsParameter.VERBOSITY_KTH_IT) 
						&& (isKthRun(iteration, m_StatsParams.getOutputVerbosityK())));
	}
	
	private boolean printRunIntroVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT)
		|| (optRunsPerformed==0 && (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_FINAL));
	}
	
	private boolean printRunStoppedVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}
	
	private boolean printFinalVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() > StatsParameter.VERBOSITY_NONE);
	}
		
	private boolean isKthRun(int i, int k) {
		// ingeniously shifting i by two since the stats counter starts at 0
		// after two evaluations have already happened: initialization and first optimization
		// this allows the last iteration to be within the displayed set if k is a divisor of whole iterations as expected 
		if ((i==0) || (k==0)) return true;
		else {
			if (i<=2) return (i % k) == 0; // show more at the beginning (always first time)
			else return ((i+2) % k) == 0;
		}
	}
	
	private boolean printHeaderByVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}
	
	private static void divideMean(Double[] mean, double d) {
		for (int j=0; j<mean.length; j++) if (mean[j]!=null) mean[j] /= d;
//		for (int i=0; i<mean.length; i++) {
//			for (int j=0; j<mean[i].length; j++) mean[i][j] /= d;
//		}
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
		return bestIndyAllRuns;
	}
	
	public IndividualInterface getRunBestSolution() {
		return bestOfRunIndy;
	}
	
	public int getFitnessCalls() {
		return functionCalls;
	}
}
