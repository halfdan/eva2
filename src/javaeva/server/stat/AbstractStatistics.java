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

public abstract class AbstractStatistics implements InterfaceStatistics {
	private PrintWriter resultOut;
	public final static boolean TRACE = false;
	protected StatisticsParameter m_StatisticsParameter;
	protected String startDate;
	protected long startTime;
	
	private boolean firstPlot = true;
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

		textListeners = new ArrayList<InterfaceTextListener>();
	}
	
	public void addTextListener(InterfaceTextListener listener) {
		textListeners.add(listener);
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
		String resFName = m_StatisticsParameter.getResultFileName();
		if (!resFName.equalsIgnoreCase("none") && !resFName.equals("")) {
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
	
	public void startOptPerformed(String infoString, int runNumber) {
		if (TRACE) System.out.println("AbstractStatistics.startOptPerformed " + runNumber);
		if (runNumber == 0) {
			functionCallSum = 0;
			firstPlot = true;
			optRunsPerformed = 0;
			convergenceCnt = 0;
			m_StatisticsParameter.saveInstance();
			initOutput();
			bestCurrentIndividual = null;
			bestIndivdualAllover = null;
		}
		functionCalls = 0;
	}
	
	public void stopOptPerformed(boolean normal) {
		if (TRACE) System.out.println("AbstractStatistics.stopOptPerformed");
		optRunsPerformed++;
		functionCallSum += functionCalls;
		// check for convergence
		if (bestCurrentIndividual != null) {
			if (StatisticUtils.norm(bestCurrentIndividual.getFitness()) < this.m_StatisticsParameter.getConvergenceRateThreshold()) {
				convergenceCnt++;
			}
			printToTextListener(" Best solution: " + BeanInspector.toString(bestCurrentIndividual) + "\n");
			printToTextListener(AbstractEAIndividual.getDefaultDataString(bestCurrentIndividual) + "\n");
		}
		if (currentBestFit!= null) {
			printToTextListener(" Best Fitness: " + BeanInspector.toString(currentBestFit) + "\n");
		}
		if (optRunsPerformed == m_StatisticsParameter.getMultiRuns()) finalizeOutput();
	}
	
	protected void finalizeOutput() {
		printToTextListener("*******\n Reached target " + convergenceCnt + " times with threshold " + m_StatisticsParameter.getConvergenceRateThreshold() + ", rate " + convergenceCnt/(double)m_StatisticsParameter.getMultiRuns() + '\n');
		printToTextListener("Best overall individual: " + BeanInspector.toString(bestIndivdualAllover) + '\n');
		printToTextListener("             solution	: " + AbstractEAIndividual.getDefaultDataString(bestIndivdualAllover) + '\n');
		printToTextListener("             fitness	: " + BeanInspector.toString(bestIndivdualAllover.getFitness()) + '\n');
		if (TRACE)
			System.out.println("stopOptPerformed");
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

	public abstract String getHostName();

	public void printToTextListener(String s) {
		if ((resultOut != null)) resultOut.print(s);
		for (InterfaceTextListener l : textListeners) {
			if (m_StatisticsParameter.isShowTextOutput()) l.print(s);
		}
	}
	
	public StatisticsParameter getStatisticsParameter() {
		return m_StatisticsParameter;
	}
	
	protected boolean doTextOutput() {
		return (resultOut != null) || (textListeners.size()>0);
	}
	
	protected String getOutputHeader(InterfaceAdditionalPopulationInformer informer, PopulationInterface pop) {
		String headline = "Fit.-calls \t Best \t Mean \t Worst ";
		if (informer == null)
			return headline;
		else return headline + "\t " + informer.getAdditionalFileStringHeader(pop);
	}
	
	protected String getOutputLine(InterfaceAdditionalPopulationInformer informer, PopulationInterface pop) {
		StringBuffer sbuf = new StringBuffer(Integer.toString(functionCalls));
		sbuf.append("\t");
		sbuf.append(BeanInspector.toString(currentBestFit));
		sbuf.append("\t");
		if (meanFitness != null) {
			sbuf.append(BeanInspector.toString(meanFitness));
			sbuf.append(" \t ");
		} else sbuf.append("- \t ");
		if (currentWorstFit != null) {
			sbuf.append(BeanInspector.toString(currentWorstFit));
			sbuf.append(" \t ");
		} else sbuf.append(" - \t ");
		if (informer != null) {
			sbuf.append(informer.getAdditionalFileStringValue(pop));
		}
//		if (m_BestIndividual instanceof AbstractEAIndividual) {
//			sbuf.append(((AbstractEAIndividual)m_BestIndividual).getStringRepresentation());
//		} else {
//			sbuf.append(m_BestIndividual.toString());
//		}
		
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
			initPlots(m_StatisticsParameter.getPlotDescriptions());
			if (doTextOutput()) printToTextListener(getOutputHeader(null, null)+'\n');
			firstPlot = false;
		}

		if (doTextOutput()) printToTextListener(getOutputLine(null, null)+'\n');
		plotCurrentResults();
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
			initPlots(m_StatisticsParameter.getPlotDescriptions());
			if (doTextOutput()) printToTextListener(getOutputHeader(informer, pop)+'\n');
			firstPlot = false;
		}
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

		if (doTextOutput()) printToTextListener(getOutputLine(informer, pop)+'\n');
		plotCurrentResults();
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
