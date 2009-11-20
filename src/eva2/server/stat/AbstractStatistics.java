package eva2.server.stat;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.IndividualInterface;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Mathematics;
import eva2.tools.Pair;

/**
 * An abstract class handling statistics. Most important stuff happens in startOptPerformed, stopOptPerformed
 * and createNextGenerationPerformed. Any measures (run based or multi-run based) are reset in startOptPerformed, 
 * updated per iteration in createNextGenerationPerformed and reported to listeners in stopOptPerformed.
 * Several different verbosity levels are regarded.  
 * The method plotCurrentResults should be implemented to plot further results per iteration. 
 *  
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
	private ArrayList<double[][]> meanCollection;
	private Double[] additionalInfoSums = null, lastAdditionalInfoSums=null;

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
	protected int feasibleFoundAfter;
	protected int numOfRunsFeasibleFound;
	protected double feasibleFoundAfterSum;
	protected int optRunsPerformed;
	protected double[] currentBestFit;
	protected double[] currentBestFeasibleFit;
//	protected double[] meanBestFeasibleFit;
	protected double[] meanFitness;
	protected double[] currentWorstFit;
//	protected double[] meanBestOfRunFitness;
	protected double avgPopDist;
	protected double maxPopDist;
	protected IndividualInterface bestCurrentIndy, bestOfRunIndy, bestOfRunFeasibleIndy, bestFeasibleAllRuns, bestIndyAllRuns;

		// collect feasible results of a run 
	private ArrayList<IndividualInterface> runBestFeasibleList;
	private ArrayList<IndividualInterface> runBestFitList;
	
	private ArrayList<InterfaceTextListener> textListeners;
	private List<InterfaceAdditionalPopulationInformer> lastInformerList = null;
	private PopulationInterface lastSols = null;

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
		SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_at_'HH.mm.ss");
		String startDate = formatter.format(new Date());
		// open the result file:
		if (doFileOutput()  // not "text-window only" 
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

	public void startOptPerformed(String infoString, int runNumber, Object params) {
		if (TRACE) {
			System.out.println("AbstractStatistics.startOptPerformed " + runNumber);
			System.out.println("Statsparams were " + BeanInspector.toString(m_StatsParams));
		}
		
		if (runNumber == 0) {
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
			if (refineMultiRuns) meanCollection = new ArrayList<double[][]>();
			else meanCollection = null;
			additionalInfoSums = null;
			lastAdditionalInfoSums = null;
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
		if (bestCurrentIndy != null) {
			if (Mathematics.norm(bestCurrentIndy.getFitness()) < this.m_StatsParams.getConvergenceRateThreshold()) {
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
		if (printFinalVerbosity()) printToTextListener(".");
		if (m_StatsParams.isOutputAdditionalInfo()) updateLastAdditionalInfo();
//		if (currentBestFit!= null) {
//			if (printRunStoppedVerbosity()) printToTextListener(" Best Fitness: " + BeanInspector.toString(currentBestFit) + "\n");
//		}
		if (optRunsPerformed >= m_StatsParams.getMultiRuns()) {
			if (printFinalVerbosity()) printToTextListener("\n");
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

	protected void finalizeOutput() {
		if (printFinalVerbosity()) printToTextListener("*******\n Runs performed: " + optRunsPerformed + ", reached target " + convergenceCnt + " times with threshold " + m_StatsParams.getConvergenceRateThreshold() + ", rate " + convergenceCnt/(double)m_StatsParams.getMultiRuns() + '\n');
		if (printFinalVerbosity()) printToTextListener(" Average function calls: " + (functionCallSum/optRunsPerformed) + "\n");
		
		if (printFinalVerbosity() && (feasibleFoundAfterSum>=0.)) {
			printToTextListener("     Feasible solution found in " + numOfRunsFeasibleFound + " of " + optRunsPerformed + " runs \n");
			printToTextListener("     Average evaluations until feasible ind. was found in " + numOfRunsFeasibleFound + " runs: " + feasibleFoundAfterSum/numOfRunsFeasibleFound + " evaluations\n");
		}

		if (printFinalVerbosity() && (additionalInfoSums != null)) {
			printToTextListener("     Averaged additional info sums: ");
			for (int i=0; i<additionalInfoSums.length; i++) if (additionalInfoSums[i]!=null) printToTextListener(" \t"+(additionalInfoSums[i]/optRunsPerformed));
			printToTextListener("\n     Averaged last additional info: ");
			for (int i=0; i<lastAdditionalInfoSums.length; i++) if (lastAdditionalInfoSums[i]!=null) printToTextListener(" \t"+(lastAdditionalInfoSums[i]/optRunsPerformed));
			printToTextListener("\n");
		}

		if (printFinalVerbosity() && (bestIndyAllRuns != null)) printIndy("Overall best", bestIndyAllRuns);
		if (printFinalVerbosity() && (m_StatsParams.isOutputAdditionalInfo())) printToTextListener(getFinalAdditionalInfo()+'\n');

		if (optRunsPerformed>1) {
			if (runBestFitList.size()>0) {
//				Mathematics.svDiv((double)optRunsPerformed, meanBestOfRunFitness, meanBestOfRunFitness);
				if (printFinalVerbosity()) {
					double[] meanBestFit=calcMeanFit(runBestFitList);
					printToTextListener(" MultiRun stats: Mean best fitness: " + BeanInspector.toString(meanBestFit)+"\n");
					if (meanBestFit.length==1) printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFitList, meanBestFit[0])) + "\n");
					printToTextListener(" MultiRun stats: Median best fitn.: " + BeanInspector.toString(calcMedianFit(runBestFitList))+"\n");
				}
			}
			if (printFinalVerbosity() && (bestFeasibleAllRuns != null)) printIndy("Overall best feasible", bestFeasibleAllRuns);
//			if ((runBestFeasibleList.size()>0) && (!equalLists(runBestFeasibleList, runBestFitList))) { // is there a difference between best feasibles and best fit?
			if (runBestFeasibleList.size()>0) { // always output feasible stats even if theyre equal
				if (printFinalVerbosity()) {
					double[] meanBestFeasibleFit=calcMeanFit(runBestFeasibleList);
					printToTextListener(" MultiRun stats: Mean best feasible fitness (" + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(meanBestFeasibleFit)+"\n");
					if (meanBestFeasibleFit.length==1) printToTextListener(" MultiRun stats: Variance/Std.Dev.: " + BeanInspector.toString(calcStdDevVar(runBestFeasibleList, meanBestFeasibleFit[0])) + "\n");
					printToTextListener(" MultiRun stats: Median best feasible fitn. (: " + numOfRunsFeasibleFound + " runs): " + BeanInspector.toString(calcMedianFit(runBestFeasibleList))+"\n");
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
	
	private String getFinalAdditionalInfo() {
		PopulationInterface bestPop = makeStatsPop();
		StringBuffer sbuf = new StringBuffer("Overall best additional data: "+ getAdditionalInfoHeader(lastInformerList, bestPop));
		sbuf.append('\n');
		appendAdditionalInfo(lastInformerList, bestPop, sbuf);
//		getOutputLine(lastInformerList, makeStatsPop());
		return sbuf.toString();
	}

	/**
	 * Perform a deep equals test on the fitness vectors of both individual lists.
	 * @param l1
	 * @param l2
	 * @return
	 */
	private boolean equalLists(
			ArrayList<IndividualInterface> l1,
			ArrayList<IndividualInterface> l2) {
		boolean equal = true;
		Iterator<IndividualInterface> iter1=l1.iterator();
		Iterator<IndividualInterface> iter2=l2.iterator();
		IndividualInterface indy1, indy2;
		if (l1.size()!=l2.size()) return false;
		else while (equal && (iter1.hasNext() && iter2.hasNext())) {
			equal = Arrays.equals(iter1.next().getFitness(), iter2.next().getFitness());
		}
		return equal;
	}

	private double[] calcStdDevVar(ArrayList<IndividualInterface> list, double meanFit) {
    	double tmp=0, sum=0;
    	for (Iterator iter = list.iterator(); iter.hasNext();) {
			IndividualInterface indy = (IndividualInterface) iter.next();
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
		for (int i=1; i<list.size(); i++) addSecond(sumFit, list.get(i).getFitness());
		Mathematics.svDiv(list.size(), sumFit, sumFit);
		
		return sumFit;
	}
	
    public static double[] calcMedianFit(List<IndividualInterface> list) {
    	ArrayList<double[]> dblAList = new ArrayList<double[]>(list.size());
    	for (int i=0; i<list.size(); i++) dblAList.add(list.get(i).getFitness());
		return Mathematics.median(dblAList, false);
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
		
		String headline = "Fun.calls\t Best\t Mean\t Worst ";
		if ((informerList == null) || !m_StatsParams.isOutputAdditionalInfo()) {
			return headline;
		} else {
			return headline + getAdditionalInfoHeader(informerList, pop);
		}
	}
	
	protected String getAdditionalInfoHeader(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
		String hdr="";
		for (InterfaceAdditionalPopulationInformer informer : informerList) {
			hdr = hdr + "\t " + informer.getAdditionalFileStringHeader(pop);
		}
		return hdr;
	}
	
	protected Pair<String,Double[]> getOutputLine(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop) {
		StringBuffer sbuf = new StringBuffer(Integer.toString(functionCalls));
		Double[] addNums = null;
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
		if (m_StatsParams.isOutputAdditionalInfo()) addNums = appendAdditionalInfo(informerList, pop, sbuf);		
		return new Pair<String,Double[]>(sbuf.toString(),addNums);
	}
	
	/**
	 * Append additional informer informations to the given StringBuffer.
	 * 
	 * @param informerList
	 * @param pop
	 * @param sbuf
	 */
	protected Double[] appendAdditionalInfo(List<InterfaceAdditionalPopulationInformer> informerList, PopulationInterface pop, StringBuffer sbuf) {
		if (informerList != null) {
			StringBuffer addBuffer = new StringBuffer();
			for (InterfaceAdditionalPopulationInformer informer : informerList) {
				addBuffer.append(" \t ");
				addBuffer.append(informer.getAdditionalFileStringValue(pop));
			}
			String addInfo = addBuffer.toString();
			Double[] retVals = parseDoubles(addInfo, "\t");
			if (sbuf!=null) sbuf.append(addInfo);
			return retVals;
		}
		return null;
	}	
	
	/**
	 * Parse Double from a String separated by the given regular expression.
	 * For Substrings which do not convert to Double by Double.parseDouble(String),
	 * a null value is added as representative.
	 * 
	 * @param str
	 * @param colSplit
	 * @return
	 */
	public static Double[] parseDoubles(String str, String splitRegExp) {
		ArrayList<Double> vals = new ArrayList<Double>();
		String[] entries = str.split(splitRegExp);
		for (int i=0; i<entries.length; i++) {
			Double d = null;
			try {
				d = Double.parseDouble(entries[i]);
			} catch(Exception e) { }
			vals.add(d); // null if unsuccessfull
		}
		return (Double[])vals.toArray(new Double[vals.size()]);
	}

	/**
	 * @deprecated The method {@link #createNextGenerationPerformed(PopulationInterface, List)} should be used instead.
	 */
	public synchronized void createNextGenerationPerformed(double[] bestfit,
			double[] worstfit, int calls) {
		functionCalls = calls;
		currentBestFit = bestfit;
		currentWorstFit = worstfit;
		currentBestFeasibleFit = null;
		meanFitness = null;
		
		if (firstPlot) {
			initPlots(m_StatsParams.getPlotDescriptions());
//			if (doTextOutput()) printToTextListener(getOutputHeader(null, null)+'\n');
			firstPlot = false;
		}
		if ((runIterCnt == 0) && printHeaderByVerbosity()) printToTextListener(getOutputHeader(null, null)+'\n');

		if (doTextOutput() && printLineByVerbosity(calls)) {
			Pair<String,Double[]> addInfo = getOutputLine(null, null);
			printToTextListener(addInfo.head()+'\n');
			if (addInfo.tail()!=null) {
				additionalInfoSums = updateAdditionalInfo(additionalInfoSums, addInfo.tail());
			}
		}
		plotCurrentResults();
		runIterCnt++;
	}
	
	/**
	 * Add the given array to the member array. Do some checks etc.
	 * If a resultSum array is provided, it is used to add the info and returned. Otherwise
	 * a new array is allocated.
	 * 
	 * @param curInfo
	 */
	private Double[] updateAdditionalInfo(Double[] resultSum, Double[] curInfo) {
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
	 * Re-request the last additional information from the lastInfomerList and update the
	 * Double value sums.
	 *  
	 * @param pop
	 */
	private void updateLastAdditionalInfo() {
		Double[] lastVals = appendAdditionalInfo(lastInformerList,  lastSols, null);
		lastAdditionalInfoSums = updateAdditionalInfo(lastAdditionalInfoSums, lastVals);
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
			pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList) {
		lastInformerList  = informerList;
		if (firstPlot) {
			initPlots(m_StatsParams.getPlotDescriptions());
//			if (doTextOutput()) printToTextListener(getOutputHeader(informer, pop)+'\n');
			firstPlot = false;
			currentBestFeasibleFit=null;
		}
		if ((runIterCnt==0) && printHeaderByVerbosity()) printToTextListener(getOutputHeader(informerList, pop)+'\n');

		if (pop.getSpecificData() != null) {
			plotSpecificData(pop, informerList);
			return;
		}
		// by default plotting only the best
		bestCurrentIndy = pop.getBestIndividual().getClone();
		if ((bestIndyAllRuns == null) || (secondIsBetter(bestIndyAllRuns, bestCurrentIndy))) {
			bestIndyAllRuns = bestCurrentIndy;
//			printToTextListener("new best found!, last was " + BeanInspector.toString(bestIndividualAllover) + "\n");
		}
		if ((bestOfRunIndy==null) || (secondIsBetter(bestOfRunIndy, bestCurrentIndy))) {
			bestOfRunIndy=bestCurrentIndy;
		}
//		IndividualInterface WorstInd = Pop.getWorstIndividual();
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
					if (feasibleFoundAfterSum<0) feasibleFoundAfterSum=0.; // initial signalling value was -1.
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
		
		meanFitness = pop.getMeanFitness().clone();
		currentWorstFit = pop.getWorstIndividual().getFitness().clone();
		functionCalls = pop.getFunctionCalls();
		if (GraphSelectionEnum.doPlotAvgDist(m_StatsParams.getGraphSelection()) 
				|| GraphSelectionEnum.doPlotMaxPopDist(m_StatsParams.getGraphSelection()))  {
			double[] measures = ((Population)pop).getPopulationMeasures((InterfaceDistanceMetric)null);
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
					// may happen for dynamic pop-sizes, e.g. in Tribes, when runs do not necessarily send the
					// "generation performed" event the same number of times. 
					// thus: dont do an update for events that are "too late"
					means = null;
				} else means = meanCollection.get(runIterCnt);
			}
			if (means != null) updateMeans(means, functionCalls, currentBestFit, meanFitness, currentWorstFit);
		}
//		meanCollection.set(pop.getGenerations()-1, means);
		
		lastSols  = (opt!=null) ? new Population(opt.getAllSolutions()) : pop;
		if (doTextOutput()) {
			Pair<String,Double[]> addInfo = getOutputLine(informerList, lastSols);

			if (printLineByVerbosity(runIterCnt)) printToTextListener(addInfo.head()+'\n');
//			updateAdditionalInfo(addInfo.tail());
			if (addInfo.tail()!=null) {
				additionalInfoSums = updateAdditionalInfo(additionalInfoSums, addInfo.tail());
			}
		}
		plotCurrentResults();

		runIterCnt++;
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
		return (i % k) == 0;
	}
	
	private boolean printHeaderByVerbosity() {
		return (m_StatsParams.getOutputVerbosity().getSelectedTagID() >= StatsParameter.VERBOSITY_KTH_IT);
	}

	private void updateMeans(double[][] means, double funCalls, double[] bestFit, double[] meanFit, double[] worstFit) {
		means[0][0]+=funCalls;
		addSecond(means[1], bestFit);
		addSecond(means[2], meanFit);
		addSecond(means[3], worstFit);
	}
	
	private static void divideMean(double[][] mean, double d) {
		for (int i=0; i<mean.length; i++) {
			for (int j=0; j<mean[i].length; j++) mean[i][j] /= d;
		}
	}
	
	public static void addSecond(double[] mean, double[] fit) {
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
		return bestIndyAllRuns;
	}
	
	public IndividualInterface getRunBestSolution() {
		return bestOfRunIndy;
	}
	
	public int getFitnessCalls() {
		return functionCalls;
	}
}
