package eva2.server.stat;

/*
 * Title:        EvA2
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
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.gui.Graph;
import eva2.gui.GraphWindow;
import eva2.gui.JTextoutputFrame;
import eva2.gui.JTextoutputFrameInterface;
import eva2.server.EvAServer;
import eva2.server.go.PopulationInterface;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.EVAERROR;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIProxyLocal;
import wsi.ra.jproxy.RMIProxyRemote;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * A statistics class to plot fitness curves in client-server mode.
 */
public class StatisticsWithGUI extends AbstractStatistics implements Serializable, InterfaceStatistics {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3213603978877954103L;
	// Plot frames:
	private GraphWindow[] m_FitnessFrame; // frame for the fitness plots
	private Graph[][] m_FitnessGraph;
	private Graph[][] m_StatGraph;

	private String m_GraphInfoString;
	protected int m_PlotCounter;

	private MainAdapterClient m_MainAdapterClient; // the connection to the client MainAdapter
	private JTextoutputFrameInterface m_ProxyPrinter;

	//////////////
	protected static String m_MyHostName = null;

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
			m_StatsParams = StatsParameter.getInstance();
			m_ProxyPrinter = new JTextoutputFrame("TextOutput of " + m_MyHostName);
		} else { // we use RMI
			m_StatsParams = (InterfaceStatisticsParameter)RMIProxyLocal.newInstance(
					StatsParameter.getInstance());
			m_ProxyPrinter = (JTextoutputFrameInterface) RMIProxyRemote.newInstance(new
					JTextoutputFrame("TextOutput " + m_MyHostName),
					m_MainAdapterClient);
		}
		addTextListener(m_ProxyPrinter);
		if (TRACE) System.out.println("Constructor RMIStatistics --> end");
	}

	/**
	 *
	 */
	public synchronized void startOptPerformed(String infoString, int runNumber, Object goParams) {
		super.startOptPerformed(infoString, runNumber, goParams);
		m_GraphInfoString = infoString;

//		m_TextCounter = m_StatisticsParameter.GetTextoutput();
		m_PlotCounter = m_StatsParams.GetPlotoutput();
	}

	public void stopOptPerformed(boolean normal, String stopMessage) {
		super.stopOptPerformed(normal, stopMessage);

		if (optRunsPerformed > m_StatsParams.getMultiRuns()) {
			// this may happen if the user reduces the multirun parameter during late multiruns
			System.err.println("error: more runs performed than defined.");
		}
		// unite the graphs only if the break was "normal"
		if (normal && (m_StatsParams.getMultiRuns() > 1) && (m_StatGraph != null)) {
			// unite the point sets for a multirun
			for (int i = 0; i < m_FitnessGraph.length; i++) {
				for (int j = 0; j < m_FitnessGraph[i].length; j++) {
					if (m_FitnessFrame[i].isValid()) {
						m_StatGraph[i][j].addGraph(m_FitnessGraph[i][j]);
						m_StatGraph[i][j].setInfoString(
								(m_FitnessGraph[i][j].getInfo().length() > 0 ? (m_FitnessGraph[i][j].getInfo() + "_") : "" )
								+ (m_StatsParams.GetInfoString().length() > 0 ? (m_StatsParams.GetInfoString() + "_") : "" )
								 + m_StatsParams.GetInfoString()
								+ "Mean_of_" + optRunsPerformed + " ",
								(float) 2.0);
						m_FitnessGraph[i][j].clear();
					}
				}
			}
		}
		// this is inconsistent, shouldnt be necessary here but reset in startOpt...
//		if (optRunsPerformed == m_StatisticsParameter.getMultiRuns()) {
//			finalizeRuns(m_ConvergenceCnt);
//			m_OptRunsPerformed = 0;
//			m_ConvergenceCnt = 0;
//		}
	}
	
	public void maybeShowProxyPrinter() {
		if (m_ProxyPrinter != null) m_ProxyPrinter.setShow((m_StatsParams).isShowTextOutput());
	}
	
	protected void initPlots(List<String[]> description) {
		if (TRACE) System.out.println("initPlots");

		maybeShowProxyPrinter();
		
		m_FitnessFrame = new GraphWindow[description.size()];
		for (int i = 0; i < m_FitnessFrame.length; i++) {
//			m_FitnessFrame[i] = GraphWindow.getInstance(m_MainAdapterClient, m_GraphInfoString + " " + i + " " + " on " + m_MyHostName + ", VM " + EvAServer.m_NumberOfVM, "function calls", "fitness");
			m_FitnessFrame[i] = GraphWindow.getInstance(m_MainAdapterClient, "Optimization " + i + " " + " on " + m_MyHostName + ", VM " + EvAServer.m_NumberOfVM, "function calls", "fitness");
		}

		m_FitnessGraph = new Graph[description.size()][];
		// contains one graph for every value to be plotted (best / worst / best+worst)
		// TODO Im really not sure why this is a 2-dimensional array. shouldnt one be enough?
		for (int i = 0; i < m_FitnessGraph.length; i++) {
			m_FitnessGraph[i] = new Graph[((String[]) description.get(i)).length];
			for (int j = 0; j < m_FitnessGraph[i].length; j++) {
				String[] d = (String[]) description.get(i);
				// this is where the column string for ascii export is created!
				m_FitnessGraph[i][j] =
					m_FitnessFrame[i].getNewGraph(d[j] + "_" +
							m_StatsParams.GetInfoString() +
							m_GraphInfoString);
				m_FitnessGraph[i][j].jump();
			}
		}
		if (m_StatsParams.getMultiRuns() > 1 &&
				m_StatsParams.GetuseStatPlot() == true) {
			String Info = m_StatsParams.GetInfoString();
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
	
	protected void plotCurrentResults() {
		// Plots
		m_PlotCounter--;
		
		int fitnessplot_setting =  m_StatsParams.getPlotFitness().getSelectedTag().getID();
		
		if (m_PlotCounter == 0) {
			m_PlotCounter = m_StatsParams.GetPlotoutput();
			boolean doPlotBest = (fitnessplot_setting == StatsParameter.PLOT_BEST)
					|| (fitnessplot_setting == StatsParameter.PLOT_BEST_AND_WORST);
			boolean doPlotWorst = (fitnessplot_setting == StatsParameter.PLOT_WORST)
					|| (fitnessplot_setting == StatsParameter.PLOT_BEST_AND_WORST);
			if (doPlotBest) {
				plotFitnessPoint(0, 0, functionCalls, currentBestFit[0]);
			}
			if (doPlotWorst) {
				// schlechteste Fitness plotten
				m_PlotCounter = m_StatsParams.GetPlotoutput();
				if (currentWorstFit == null) {
					System.err.println("m_WorstFitness==null in plotStatisticsPerformed");
					return;
				}
				plotFitnessPoint(0, (doPlotBest ? 1 : 0) , functionCalls, currentWorstFit[0]);
			}
		}
	}

	/**
	 *
	 */
	public void plotSpecificData(PopulationInterface pop, InterfaceAdditionalPopulationInformer informer) {
		double[] specificData = pop.getSpecificData();
		int calls = pop.getFunctionCalls();
		ArrayList<String[]> description = new ArrayList<String[]>();
		ArrayList<String> temp = new ArrayList<String>();
		String[] ss = pop.getSpecificDataNames();
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
		if (doTextOutput()) {
			String s = "calls , " + calls + " bestfit , ";
			s = s + BeanInspector.toString(currentBestFit);
			if (currentWorstFit != null) {
				s = s + " , worstfit , " + BeanInspector.toString(currentWorstFit);
			}
			printToTextListener(s + "\n");
		}

		m_PlotCounter--;
		if (m_PlotCounter == 0) {
			m_PlotCounter = m_StatsParams.GetPlotoutput();
			int index = 0;
			for (int i = 0; i < m_FitnessGraph.length; i++) {
				for (int j = 0; j < m_FitnessGraph[i].length; j++) {
					plotFitnessPoint(i, j, calls, specificData[index]);
					index++;
				}
			}

		}
	}
	
	public String getHostName() {
		return m_MyHostName;
	}
}