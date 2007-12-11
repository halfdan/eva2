package javaeva.server.stat;

/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javaeva.gui.Graph;
import javaeva.gui.GraphWindow;
import javaeva.gui.JTextoutputFrame;
import javaeva.gui.JTextoutputFrameInterface;
import javaeva.server.EvAServer;
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.PopulationInterface;
import javaeva.tools.EVAERROR;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIProxyLocal;
import wsi.ra.jproxy.RMIProxyRemote;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * A statistics class to plot fitness curves in JavaEvA client-server mode.
 */
public class StatisticsWithGUI implements Serializable, Statistics {
	/**
	 * 
	 */
	private static final long serialVersionUID = -243368825290670991L;
	public static final boolean TRACE = false;
	// Plot frames:
	private GraphWindow[] m_FitnessFrame; // frame for the fitness plots
	private Graph[][] m_FitnessGraph;
	private Graph[][] m_StatGraph;

	private String m_GraphInfoString;
	private boolean m_firstPlot = true;
	protected int m_PlotCounter;

	private MainAdapterClient m_MainAdapterClient; // the connection to the client MainAdapter
	private JTextoutputFrameInterface m_ProxyPrinter;

	//////////////
	protected static String m_MyHostName = null;
	protected PrintWriter m_ResultOut;
	protected PrintWriter m_OverAllResultOut;
	protected long m_StartTime;
	protected String m_StartDate;
	protected int m_TextCounter;
	protected StatisticsParameter m_StatisticsParameter;
	protected double[] m_BestFitness;
	protected double[] m_MeanFitness;
	protected double[] m_WorstFitness;
	protected double[] m_BestSolution;
	protected IndividualInterface m_BestIndividual;
	protected int m_FunctionCalls;
	protected int m_OptRunsPerformed;
	protected String m_InfoString;
//	private String m_FileName = "";
//	private static boolean m_useMedian = false;
	private double[] m_SpecificData;
	/**
	 *
	 */
	public MainAdapterClient getMainAdapterClient() {
		return m_MainAdapterClient;
	}

	/**
	 *
	 */
	public StatisticsWithGUI(MainAdapterClient Client) {
		if (TRACE)
			System.out.println("Constructor RMIStatistics");
		m_MainAdapterClient = Client;
		if (Client != null) { // were probably in rmi mode
			try {
				m_MyHostName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				System.err.println(
						"InetAddress.getLocalHost().getHostAddress() --> ERROR" +
						e.getMessage());
			}
		} else m_MyHostName = "localhost";

		
		if ((Client == null) || Client.getHostName().equals(m_MyHostName)) {
			m_StatisticsParameter = StatisticsParameterImpl.getInstance();
			m_ProxyPrinter = new JTextoutputFrame("TextOutput of " + m_MyHostName);
		} else { // we use RMI
			m_StatisticsParameter = (StatisticsParameter) RMIProxyLocal.newInstance(
					StatisticsParameterImpl.getInstance());
			m_ProxyPrinter = (JTextoutputFrameInterface) RMIProxyRemote.newInstance(new
					JTextoutputFrame("TextOutput " + m_MyHostName),
					m_MainAdapterClient);
		}
		m_OptRunsPerformed = 0;
		if (TRACE)
			System.out.println("Constructor RMIStatistics --> end");
	}

	/**
	 *
	 */
	public synchronized void startOptPerformed(String InfoString, int runnumber) {
		if (runnumber == 0) {
			m_OptRunsPerformed = 0;
			m_firstPlot = true;
			m_StatisticsParameter.saveInstance();
		}
		if (TRACE)
			System.out.println("Statistics.startOptPerformed " + runnumber);
		m_GraphInfoString = InfoString;
		m_StartTime = System.currentTimeMillis();
		m_TextCounter = m_StatisticsParameter.GetTextoutput();
		m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
		if (m_OptRunsPerformed == 0) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"E'_'yyyy.MM.dd'_at_'hh.mm.ss");
			m_StartDate = formatter.format(new Date());
			// open the result file:
			String x = m_StatisticsParameter.getResultFileName();
			if (!x.equalsIgnoreCase("none") && !x.equals("")) {
				String name = x + "_" + m_StartDate + ".txt";
				if (TRACE)
					System.out.println("FileName =" + name);
				try {
					m_ResultOut = new PrintWriter(new FileOutputStream(name));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error: " + e);
				}
				m_ResultOut.println("StartDate:" + m_StartDate);
				m_ResultOut.println("On Host:" + m_MyHostName);
			} else
				m_ResultOut = null;
		}
	}

	/**
	 *
	 */
//	private void plotEndogenAttributes(PopulationInterface Pop) {
//	if (m_StatisticsParameter.getPrintObjects() == false)
//	return;
//	double[] x = ( (IndividualInterface) (Pop.getBestIndividual())).
//	getDoubleArray();
//	for (int ii = 0; ii < x.length; ii++)
//	m_ObjectsGraph[ii /*+x.length*i*/].setConnectedPoint(m_FunctionCalls,
//	x[ii]);
//	//}
//	}

	/**
	 *
	 */
	public void stopOptPerformed(boolean normal) {
		if (TRACE)
			System.out.println("stopOptPerformed");
		m_OptRunsPerformed++;
		// overall best objectives:
		String s = "";
		if (m_BestSolution != null) {
			for (int i = 0; i < m_BestSolution.length; i++)
				s = s + " x[" + i + "]=" + m_BestSolution[i];
		}
		printToTextListener(" Best solution objectives: " + s);
		s = "";
		if (m_BestSolution != null) {
			for (int i = 0; i < m_BestFitness.length; i++)
				s = s + " f[" + i + "]=" + m_BestFitness[i];
		}

		printToTextListener(" Best solution fitness: " + s);
		if (m_OptRunsPerformed <= m_StatisticsParameter.getMultiRuns()) {
			if ((m_StatisticsParameter.getMultiRuns() > 1) && (m_StatGraph != null)) {
				// unite the point sets for a multirun
				for (int i = 0; i < m_FitnessGraph.length; i++) {
					for (int j = 0; j < m_FitnessGraph[i].length; j++) {
						// unite the graphs only if the break was "normal"
						if (normal && m_FitnessFrame[i].isValid()) {
							m_StatGraph[i][j].addGraph(m_FitnessGraph[i][j]);
							m_StatGraph[i][j].setInfoString(m_FitnessGraph[i][j].getInfo() +
									"_" +
									m_StatisticsParameter.GetInfoString() +
									" Mean of " + m_OptRunsPerformed +
									" runs",
									(float) 2.0);
							m_FitnessGraph[i][j].clear();
						}
					}
				}
			} else {
				// create new graphs?? seems superfluous here. (MK)
//				for (int i = 0; i < m_FitnessFrame.length; i++) {
//					for (int j = 0; j < m_FitnessGraph[i].length; j++) {
//						m_FitnessGraph[i][j] = m_FitnessFrame[i].getNewGraph(
//								m_StatisticsParameter.GetInfoString() + m_GraphInfoString);
//
//					}
//				}
			}
		}
		if (m_OptRunsPerformed == m_StatisticsParameter.getMultiRuns()) {
			m_OptRunsPerformed = 0;
			if (TRACE)
				System.out.println("stopOptPerformed");
			if (TRACE)
				System.out.println("End of run");
			if (m_ResultOut != null) {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"E'_'yyyy.MM.dd'_at_'hh:mm:ss");
				String StopDate = formatter.format(new Date());
				m_ResultOut.println("StopDate:" + StopDate);
				m_ResultOut.close();
			}
		}
	}

	/**
	 * Called at the very first (multirun mode) plot of a fitness curve.
	 */
	private void initPlots(ArrayList description) {
		if (TRACE)
			System.out.println("initPlots");
		m_firstPlot = false;

		m_FitnessFrame = new GraphWindow[description.size()];
		for (int i = 0; i < m_FitnessFrame.length; i++) {
			m_FitnessFrame[i] = GraphWindow.getInstance(m_MainAdapterClient,
					m_GraphInfoString + " " + i + " " + " on " + m_MyHostName + ", VM " + EvAServer.m_NumberOfVM, "function calls", "fitness");
		}

		m_FitnessGraph = new Graph[description.size()][];
		// contains one graph for every value to be plotted (best / worst / best+worst)
		// TODO Im really not sure why this is a 2-dimensional array. shouldnt one be enough?
		for (int i = 0; i < m_FitnessGraph.length; i++) {
			m_FitnessGraph[i] = new Graph[((String[]) description.get(i)).length];
			for (int j = 0; j < m_FitnessGraph[i].length; j++) {
				String[] d = (String[]) description.get(i);
				m_FitnessGraph[i][j] =
					m_FitnessFrame[i].getNewGraph(d[j] + "_" +
							m_StatisticsParameter.GetInfoString() +
							m_GraphInfoString);
				m_FitnessGraph[i][j].jump();
			}
		}
		if (m_StatisticsParameter.getMultiRuns() > 1 &&
				m_StatisticsParameter.GetuseStatPlot() == true) {
			String Info = m_StatisticsParameter.GetInfoString();
			m_StatGraph = new Graph[description.size()][];
			for (int i = 0; i < m_StatGraph.length; i++) {
				m_StatGraph[i] = new Graph[((String[]) description.get(i)).length];
				for (int j = 0; j < m_StatGraph[i].length; j++) {
					String[] d = (String[]) description.get(i);
					m_StatGraph[i][j] = m_FitnessFrame[i].getNewGraph(d[j] + "_" + Info +
							m_GraphInfoString);
				}
			}
		}
	}

	private void plotFitnessPoint(int graph, int subGraph, int x, double y) {
		if (m_FitnessGraph == null) {
			EVAERROR.WARNING("fitness graph is null!");
			return;
		}
		if (graph >= m_FitnessGraph.length || subGraph >= m_FitnessGraph[graph].length) {
			EVAERROR.WARNING("tried to plot to invalid graph!");
			return;
		}
		boolean isValidGraph = m_FitnessFrame[graph].isValid();
		if (!isValidGraph) {
			// this happens if the user closed the plot window.
			// if the plots are reinitialized immediately, the user might get angry, so wait (till next opt start)
//			EVAERROR.WARNING("fitness graph is invalid, trying to reinitialize...");
//			initPlots(getDescription());
		}
		if (isValidGraph) m_FitnessGraph[graph][subGraph].setConnectedPoint(x, y);
	}
	
	private ArrayList<String[]> getDescription() {
		ArrayList<String[]> desc = new ArrayList<String[]>();
		int fitnessplot_setting = 0;
		fitnessplot_setting = m_StatisticsParameter.getPlotFitness().
		getSelectedTag().getID();
		switch (fitnessplot_setting) {
			case StatisticsParameterImpl.PLOT_BEST_AND_WORST:
				desc.add(new String[] {"Best", "Worst"});
				break;
			case StatisticsParameterImpl.PLOT_BEST:
				desc.add(new String[] {"Best"});
				break;
			case StatisticsParameterImpl.PLOT_WORST:
				desc.add(new String[] {"Worst"});
				break;
			}
		return desc;
	}

	/**
	 *
	 */
	public synchronized void createNextGenerationPerformed(double[] bestfit,
			double[] worstfit, int calls) {
		m_FunctionCalls = calls;
		m_BestFitness = bestfit;
		m_WorstFitness = worstfit;
		
		if (m_firstPlot == true) {
			initPlots(getDescription());
		}
		plotCurrentResults();
	}
	
	/**
	 *
	 */
	public synchronized void createNextGenerationPerformed(PopulationInterface
			Pop) {
		m_SpecificData = Pop.getSpecificData();
		if (m_SpecificData != null) {
			plotSpecificData(Pop);
			return;
		}
		// by default plotting only the best
		IndividualInterface BestInd = Pop.getBestIndividual();
		IndividualInterface WorstInd = Pop.getWorstIndividual();
		if (BestInd == null) {
			System.err.println("createNextGenerationPerformed BestInd==null");
			return;
		}


		if (m_firstPlot == true) {
			initPlots(getDescription());
		}

		double[] BestFitness = BestInd.getFitness();
		double[] WorstFitness = WorstInd.getFitness();
		if (BestFitness == null)
			System.err.println("BestFitness==null !");
		m_BestIndividual = BestInd.getClone();
		m_BestSolution = (double[]) BestInd.getDoubleArray().clone();
		m_BestFitness = (double[]) BestFitness.clone();
		m_WorstFitness = (double[]) WorstFitness.clone();
		m_FunctionCalls = Pop.getFunctionCalls();

		plotCurrentResults();
	}
	
	private void plotCurrentResults() {
		// Text-Ouput
		m_TextCounter--;
		if (m_TextCounter == 0 || m_ResultOut != null) {
			m_TextCounter = m_StatisticsParameter.GetTextoutput();
			String s = "calls , " + m_FunctionCalls + " bestfit , ";
			for (int i = 0; i < m_BestFitness.length; i++)
				s = s + m_BestFitness[i];

			if (m_WorstFitness != null) {
				s = s + " , worstfit , ";
				for (int i = 0; i < m_WorstFitness.length; i++)
					s = s + m_WorstFitness[i] + " , ";
			}
//			for (int i = 0; i < this.m_BestSolution.length; i++)
//			s = s + " x[" + i + "]=" + m_BestSolution[i];
			printToTextListener(s);
		}
		// Plots
		m_PlotCounter--;
		
		int fitnessplot_setting =  m_StatisticsParameter.getPlotFitness().getSelectedTag().getID();
		
		if (m_PlotCounter == 0) {
			m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
			boolean doPlotBest = (fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST)
					|| (fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST_AND_WORST);
			boolean doPlotWorst = (fitnessplot_setting == StatisticsParameterImpl.PLOT_WORST)
					|| (fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST_AND_WORST);
			if (doPlotBest) {
				plotFitnessPoint(0, 0, m_FunctionCalls, m_BestFitness[0]);
			}
			if (doPlotWorst) {
				// schlechteste Fitness plotten
				m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
				if (m_WorstFitness == null) {
					System.err.println("m_WorstFitness==null in plotStatisticsPerformed");
					return;
				}
				plotFitnessPoint(0, (doPlotBest ? 1 : 0) , m_FunctionCalls, m_WorstFitness[0]);
			}
		}
//		if (m_PlotCounter == 0) {
//			m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
//			//          int fitnessplot_setting = m_StatisticsParameter.getPlotFitness().getSelectedTag().getID();
//			if ((fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST)
//					||
//					(fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST_AND_WORST)) {
//				if (m_BestFitness == null) {
//					System.out.println("m_BestFitness==null in plotStatisticsPerformed");
//					return;
//				}
//				m_FitnessGraph[0][0].setConnectedPoint(m_FunctionCalls,
//						m_BestFitness[0]);
//			}
//			if ((fitnessplot_setting == StatisticsParameterImpl.PLOT_WORST)
//					||
//					(fitnessplot_setting == StatisticsParameterImpl.PLOT_BEST_AND_WORST)) {
//				// schlecht. Fitness plotten
//				m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
//				if (m_WorstFitness == null) {
//					System.out.println(
//							"m_WorstFitness==null in plotStatisticsPerformed");
//					return;
//				}
//				m_FitnessGraph[0][m_FitnessGraph[0].length -
//				                  1].setConnectedPoint(m_FunctionCalls, m_WorstFitness[0]);
//			}
//		}

	}

	/**
	 *
	 */
	public void plotSpecificData(PopulationInterface Pop) {
		// What in the name of ... is this method??
		m_FunctionCalls = Pop.getFunctionCalls();
		ArrayList<String[]> description = new ArrayList<String[]>();
		ArrayList<String> temp = new ArrayList<String>();
		String[] ss = Pop.getSpecificDataNames();
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].lastIndexOf("*") == -1) {
				temp.add(ss[i]);
			} else {
				String[] line = new String[temp.size()];
				temp.toArray(line);
				description.add(line);
				temp = new ArrayList<String>();
				temp.add(ss[i]);
			}
		}
		if (temp.size() > 0) {
			String[] line = new String[temp.size()];
			temp.toArray(line);
			description.add(line);

		}
		if (m_firstPlot == true)
			initPlots(description);
		m_TextCounter--;
		if (m_TextCounter == 0 || m_ResultOut != null) {
			m_TextCounter = m_StatisticsParameter.GetTextoutput();
			String s = "calls , " + m_FunctionCalls + " bestfit , ";
			for (int i = 0; i < m_BestFitness.length; i++)
				s = s + m_BestFitness[i];

			if (m_WorstFitness != null) {
				s = s + " , worstfit , ";
				for (int i = 0; i < m_WorstFitness.length; i++)
					s = s + m_WorstFitness[i] + " , ";
			}
//			for (int i = 0; i < this.m_BestSolution.length; i++)
//			s = s + " x[" + i + "]=" + m_BestSolution[i];
			printToTextListener(s);
		}

		m_PlotCounter--;
		if (m_PlotCounter == 0) {
			m_PlotCounter = m_StatisticsParameter.GetPlotoutput();
			int index = 0;
			for (int i = 0; i < m_FitnessGraph.length; i++) {
				for (int j = 0; j < m_FitnessGraph[i].length; j++) {
					plotFitnessPoint(i, j, m_FunctionCalls, m_SpecificData[index]);
					index++;
				}
			}

		}
	}

	/**
	 *
	 */
	public void printToTextListener(String s) {
//		if (m_StatisticsParameter.GetTextoutput() <= 0)
//		return;
		//   System.out.println(s);
		if (m_ResultOut != null)
			m_ResultOut.println(s);
		if (m_ProxyPrinter != null)
			m_ProxyPrinter.print(s);
	}
	/**
	 *
	 */
	public StatisticsParameter getStatisticsParameter() {
		return m_StatisticsParameter;
	}
	/**
	 *
	 */
	public Object getBestSolution() {
		return m_BestSolution;
	}

	/**
	 *
	 */
	public double[] getBestFitness() {
		return m_BestFitness;
	}
}