package eva2.optimization.stat;

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
import eva2.gui.BeanInspector;
import eva2.gui.plot.Graph;
import eva2.gui.plot.GraphWindow;
import eva2.gui.JTextoutputFrame;
import eva2.gui.JTextoutputFrameInterface;
import eva2.gui.plot.Plot;
import eva2.gui.plot.PlotInterface;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.EVAERROR;
import eva2.tools.Pair;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A statistics class to plot fitness curves in client-server mode. Mainly,
 * arrays of GraphWindows and Graphs are managed and the selected data fields
 * are plotted. TODO: this could finally be cleanly reduced to an
 * InterfaceStatisticsListener - without inheriting from AbstractStatistics.
 */
public class StatisticsWithGUI extends AbstractStatistics implements Serializable, InterfaceStatistics {

    private static final long serialVersionUID = 3213603978877954103L;
    private static final Logger LOGGER = Logger.getLogger(StatisticsWithGUI.class.getName());
    // Plot frames:
    private GraphWindow[] fitnessFrame; // frame for the fitness plots
    private Graph[][] fitnessGraph;
    private Graph[][] statGraph;
    private String graphInfoString;
    protected int plotCounter;
    private JTextoutputFrameInterface proxyPrinter;
    /*
     * List of descriptor strings and optional indices. strictly its redundant
     * since super.lastGraphSelection is always available. However it spares
     * some time.
     */
    private transient List<Pair<String, Integer>> graphDesc = null;
    protected static String hostName = null;

    /**
     *
     */
    public StatisticsWithGUI() {
        m_StatsParams = StatisticsParameter.getInstance(true);
        proxyPrinter = new JTextoutputFrame("TextOutput of " + hostName);
        addTextListener(proxyPrinter);
    }

    /**
     *
     */
    @Override
    public synchronized void startOptPerformed(String infoString, int runNumber, Object goParams, List<InterfaceAdditionalPopulationInformer> informerList) {
        super.startOptPerformed(infoString, runNumber, goParams, informerList);
        graphInfoString = infoString;

//		m_TextCounter = m_StatisticsParameter.GetTextoutput();
//		m_PlotCounter = m_StatsParams.GetPlotoutput();
        if ((fitnessFrame != null) && (fitnessFrame[0] != null)) {
            PlotInterface p = fitnessFrame[0].getPlotter();
            if ((p != null) && p.isValid()) {
                ((Plot) p).getFunctionArea().clearLegend();
            }
        }
    }

    @Override
    public void stopOptPerformed(boolean normal, String stopMessage) {
        super.stopOptPerformed(normal, stopMessage);

        if (optRunsPerformed > m_StatsParams.getMultiRuns()) {
            // this may happen if the user reduces the multirun parameter during late multiruns
            System.err.println("error: more runs performed than defined.");
        }

        int fullRuns = optRunsPerformed;
        if (!normal) {
            fullRuns--;
        }

        // unite the graphs only if the break was "normal"
        if ((m_StatsParams.getMultiRuns() > 1) && (statGraph != null)) {
            // unite the point sets for a multirun
            for (int i = 0; i < fitnessGraph.length; i++) {
                for (int j = 0; j < fitnessGraph[i].length; j++) {
                    statGraph[i][j].setInfoString(
                            (fitnessGraph[i][j].getInfo().length() > 0 ? (fitnessGraph[i][j].getInfo() + "_") : "")
                            + "Mean_of_" + fullRuns + " ",
                            (float) 2.0);
                    if (normal && fitnessFrame[i].isValid() && (fitnessGraph[i][j].getPointCount() > 0)) {
                        statGraph[i][j].addGraph(fitnessGraph[i][j]);
                        fitnessGraph[i][j].clear();
                    }
                }
            }
        }
        PlotInterface p = fitnessFrame[0].getPlotter();
        if ((optRunsPerformed >= m_StatsParams.getMultiRuns()) || !normal) {
            // update the legend after the last multirun or after a user break
            if ((p != null) && p.isValid()) {
                ((Plot) p).getFunctionArea().updateLegend();
            }
        }
    }

    public void maybeShowProxyPrinter() {
        if (proxyPrinter != null) {
            proxyPrinter.setShow(m_StatsParams.isShowTextOutput());
        }
    }

    @Override
    protected void initPlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (m_StatsParams instanceof StatisticsParameter) {
            graphDesc = lastFieldSelection.getSelectedWithIndex();
        } else {
            graphDesc = null;
            System.err.println("Error in StatisticsWithGUI.initPlots()!");
        }

        maybeShowProxyPrinter();
        int windowCount = 1; // TODO this was earlier description.length for the 2-D String-Array returned by m_StatsParams.getPlotDescriptions, which however always returned an array of length 1 (in the first dim).
        int graphCount = graphDesc.size();
        fitnessFrame = new GraphWindow[windowCount];
        for (int i = 0; i < fitnessFrame.length; i++) {
            fitnessFrame[i] = GraphWindow.getInstance("Optimization " + i, "function calls", "fitness");
        }

        fitnessGraph = new Graph[windowCount][];
        // contains one graph for every value to be plotted (best / worst / best+worst)
        // TODO Im really not sure why this is a 2-dimensional array. shouldnt one be enough?
        for (int i = 0; i < fitnessGraph.length; i++) {
            fitnessGraph[i] = new Graph[graphCount];
            for (int j = 0; j < fitnessGraph[i].length; j++) {
//				String[] d = (String[]) description.get(i);
                // this is where the column string for ascii export is created! Uah!
                fitnessGraph[i][j] =
                        fitnessFrame[i].getNewGraph(graphDesc.get(j).head + "_"
                        + graphInfoString);
                fitnessGraph[i][j].jump();
            }
        }
        if (m_StatsParams.getMultiRuns() > 1
                && m_StatsParams.getUseStatPlot() == true) {
//			String Info = m_StatsParams.GetInfoString();
            statGraph = new Graph[windowCount][];
            for (int i = 0; i < statGraph.length; i++) {
                statGraph[i] = new Graph[graphCount];
                for (int j = 0; j < statGraph[i].length; j++) {
//					String[] d = (String[]) description.get(i);
                    statGraph[i][j] = fitnessFrame[i].getNewGraph(graphDesc.get(j).head + "_" + //Info +
                            graphInfoString);
                }
            }
        }
    }

    private void plotFitnessPoint(int graph, int subGraph, int x, double y) {
        if (fitnessGraph == null) {
            EVAERROR.WARNING("fitness graph is null! (StatisticsWithGUI)");
            return;
        }
        if (graph >= fitnessGraph.length || subGraph >= fitnessGraph[graph].length) {
            EVAERROR.WARNING("tried to plot to invalid graph! (StatisticsWithGUI)");
            return;
        }
        boolean isValidGraph = fitnessFrame[graph].isValid();
        if (!isValidGraph) {
            // this happens if the user closed the plot window.
            // if the plots are reinitialized immediately, the user might get angry, so wait (till next opt start)
//			EVAERROR.WARNING("fitness graph is invalid, trying to reinitialize...");
//			initPlots(getDescription());
        }
        if (isValidGraph) {
            fitnessGraph[graph][subGraph].setConnectedPoint(x, y);
        }
    }

    /**
     * Plots the selected data to the fitness graphs.
     */
    @Override
    protected void plotCurrentResults() {
//		m_PlotCounter--;

//		if (m_PlotCounter == 0) {
//			m_PlotCounter = m_StatsParams.GetPlotoutput();
        int subGraph = 0;
//			boolean doPlotAdditionalInfo = m_StatsParams.isOutputAdditionalInfo();
        for (int i = 0; i < graphDesc.size(); i++) {
            Integer colIndex = i + 1; // always add one because the function calls are located in column zero
            if (lastIsShowFull) {
                colIndex = 1 + graphDesc.get(i).tail;
            }
            // plot the column as indicated by the graph description
            if (currentStatDoubleData[colIndex] != null) {
                plotFitnessPoint(0, subGraph++, functionCalls, currentStatDoubleData[colIndex]);
            } else {
//					EVAERROR.errorMsgOnce("Error, data field " + graphDesc.get(i).head + " does not contain primitive data and cannot be plotted.");
                subGraph++; // increase index anyways or the name assignment gets inconsistent
            }
        }
//		}
    }

    /**
     * This method is more or less deprecated. The current standard population
     * does not define specific data. However its used by the ES module
     * implementation.
     */
    @Override
    public void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informer) {
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
            s += BeanInspector.toString(currentBestFit);
            if (currentWorstFit != null) {
                s = s + " , worstfit , " + BeanInspector.toString(currentWorstFit);
            }
            printToTextListener(s + "\n");
        }

//		m_PlotCounter--;
//		if (m_PlotCounter == 0) {
//			m_PlotCounter = m_StatsParams.GetPlotoutput();
        int index = 0;
        for (int i = 0; i < fitnessGraph.length; i++) {
            for (int j = 0; j < fitnessGraph[i].length; j++) {
                plotFitnessPoint(i, j, calls, specificData[index]);
                index++;
            }
        }
//		}
    }

    @Override
    public String getHostName() {
        return hostName;
    }
}