package eva2.server.stat;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 284 $
 *            $Date: 2007-11-27 14:37:05 +0100 (Tue, 27 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;
import java.util.ArrayList;

import eva2.gui.GenericObjectEditor;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;
import eva2.tools.Tag;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class StatsParameter implements InterfaceStatisticsParameter, Serializable {
	public final static int PLOT_BEST = 0;
	public final static int PLOT_WORST = 1;
	public final static int PLOT_BEST_AND_WORST = 2;
	public final static int PLOT_BEST_AND_MEASURES = 3;
	public final static Tag[] TAGS_PLOT_FITNESS = {
		new Tag(PLOT_BEST, "plot best fitness"),
		new Tag(PLOT_WORST, "plot worst fitness"),
		new Tag(PLOT_BEST_AND_WORST, "both best and worst"),
		new Tag(PLOT_BEST_AND_MEASURES, "both best and population measures")
	};

	public final static int VERBOSITY_NONE = 0;
	public final static int VERBOSITY_FINAL = 1;
	public final static int VERBOSITY_KTH_IT = 2;
	public final static int VERBOSITY_ALL = 3;
	SelectedTag outputVerbosity = new SelectedTag("No output", "Final results", "K-th iterations", "All iterations");

	public final static int OUTPUT_FILE = 0;
	public final static int OUTPUT_WINDOW = 1;
	public final static int OUTPUT_FILE_WINDOW = 2;
	SelectedTag outputTo = new SelectedTag("File (current dir.)", "Text-window", "Both file and text-window");
	private int verboK = 10;

	private int m_PlotFitness = PLOT_BEST;
	private int m_Textoutput = 0;
	private int m_Plotoutput = 1;
	private int m_MultiRuns = 1;
	private String m_ResultFilePrefix = "JE2";
	protected String m_Name = "not defined";
	protected String m_InfoString = "";
	private boolean m_useStatPlot = true;
	private boolean showAdditionalProblemInfo = false;
	private double m_ConvergenceRateThreshold=0.001;

	/**
	 *
	 */
	public static StatsParameter getInstance() {
		StatsParameter Instance = (StatsParameter) Serializer.loadObject("Statistics.ser");
		if (Instance == null)
			Instance = new StatsParameter();
		return Instance;
	}

	/**
	 *
	 */
	public StatsParameter() {
		m_Name = "Statistics";
		outputVerbosity.setSelectedTag(2);
		outputTo.setSelectedTag(1);
	}

	/**
	 *
	 */
	public String toString() {
		String ret = "\r\nStatParameter:\r\nm_MultiRuns=" + m_MultiRuns +
		"\r\nm_Textoutput=" + m_Textoutput +
		"\r\nm_Plotoutput=" + m_Plotoutput;
		return ret;
	}

	/**
	 * Return a list of String arrays describing the selected plot options, e.g. {"Best"} or {"Best", "Worst"}.
	 * For now, only one array is returned.
	 * 
	 * @return a list of String arrays describing the selected plot options
	 */
	public ArrayList<String[]> getPlotDescriptions() {
		ArrayList<String[]> desc = new ArrayList<String[]>();
		switch (getPlotData().getSelectedTagID()) {
		case StatsParameter.PLOT_BEST_AND_WORST:
			desc.add(new String[] {"Best", "Worst"});
			break;
		case StatsParameter.PLOT_BEST:
			desc.add(new String[] {"Best"});
			break;
		case StatsParameter.PLOT_WORST:
			desc.add(new String[] {"Worst"});
			break;
		case StatsParameter.PLOT_BEST_AND_MEASURES:
			desc.add(new String[] {"Best", "AvgDist", "MaxDist"});
			break;
		}
		return desc;
	}

	/**
	 *
	 */
	public void saveInstance() {
		Serializer.storeObject("Statistics.ser", this);
	}

	/**
	 *
	 */
	private StatsParameter(StatsParameter Source) {
		m_ConvergenceRateThreshold = Source.m_ConvergenceRateThreshold;
		m_useStatPlot = Source.m_useStatPlot;
		m_Textoutput = Source.m_Textoutput;
		m_Plotoutput = Source.m_Plotoutput;
		m_PlotFitness = Source.m_PlotFitness;
		m_MultiRuns = Source.m_MultiRuns;
		m_ResultFilePrefix = Source.m_ResultFilePrefix;
		verboK = Source.verboK;
	}

	/**
	 *
	 */
	public Object getClone() {
		return new StatsParameter(this);
	}

	/**
	 *
	 */
	public String getName() {
		return m_Name;
	}

	public String globalInfo() {
		return "Configure statistics and output of the optimization run.";
	}

	/**
	 *
	 */
	public void setPlotoutput(int i) {
		m_Plotoutput = i;
	}

	/**
	 *
	 */
	public int GetPlotoutput() {
		return m_Plotoutput;
	}

//	/**
//	*
//	*/
//	public String plotFrequencyTipText() {
//	return "Frequency how often the fitness plot gets an update. plotoutput=1 -> there is a output every generation. plotoutput<0 -> there is no plot output";
//	}

//	/**
//	*
//	*/
//	public String printMeanTipText() {
//	return "Prints the mean of the fitness plot. Makes only sense when multiRuns > 1;";
//	}

	/**
	 *
	 */
	public void setMultiRuns(int x) {
		m_MultiRuns = x;
	}

	/**
	 *
	 */
	public int getMultiRuns() {
		return m_MultiRuns;
	}

	/**
	 *
	 */
	public String multiRunsTipText() {
		return "Number of independent optimization runs to evaluate.";
	}

	/**
	 *
	 */
	public String GetInfoString() {
		return m_InfoString;
	}

	/**
	 *
	 */
	public void setInfoString(String s) {
		m_InfoString = s;
	}

	/**
	 *
	 */
	public String infoStringTipText() {
		return "Infostring displayed on fitness graph by prssing the right mouse button.";
	}

	/**
	 *
	 */
	public boolean GetuseStatPlot() {
		return m_useStatPlot;
	}

	/**
	 *
	 */
	public void setuseStatPlot(boolean x) {
		m_useStatPlot = x;
	}

	/**
	 *
	 */
	public String useStatPlotTipText() {
		return "Plotting each fitness graph separate if multiruns > 1.";
	}

	/**
	 *
	 */
	public SelectedTag getPlotData() {
		return new SelectedTag(m_PlotFitness, TAGS_PLOT_FITNESS);
	}

	/**
	 *
	 */
	public void setPlotData(SelectedTag newMethod) {
		m_PlotFitness = newMethod.getSelectedTag().getID();
	}

	/**
	 *
	 */
	public String plotDataTipText() {
		return "The data to be plotted: best fitness, worst fitness or average/max distance in population.";
	}

//	/**
//	*
//	*/
//	public String plotObjectivesTipText() {
//	return "The individual of which the objectives are plotted.";
//	}


	/**
	 *
	 */
	public void SetResultFilePrefix(String x) {
		if (x==null) m_ResultFilePrefix = "";
		else m_ResultFilePrefix = x;
	}

	/**
	 *
	 */
	public String getResultFilePrefix() {
		return m_ResultFilePrefix;
	}

	public void SetShowTextOutput(boolean show) {
		// activate if not activated
		if (show && outputTo.getSelectedTagID() == 0) outputTo.setSelectedTag(2);
		// deactivate if activated
		else if (!show && outputTo.getSelectedTagID()>0) outputTo.setSelectedTag(0);
	}

	public boolean isShowTextOutput() {
		return outputTo.getSelectedTagID()>0;
	}

//	/**
//	*
//	*/
//	public String resultFileNameTipText() {
//	return "File name for the result file. If empty or 'none', no output file will be created.";
//	}

	public String convergenceRateThresholdTipText() {
		return "Provided the optimal fitness is at zero, give the threshold below which it is considered as 'reached'";
	}

	/**
	 *
	 * @param x
	 */
	public void setConvergenceRateThreshold(double x) {
		m_ConvergenceRateThreshold = x;
	}

	/**
	 *
	 */
	public double getConvergenceRateThreshold() {
		return m_ConvergenceRateThreshold;
	}

//	/**
//	* @return the showOutputData
//	*/
//	public boolean isShowTextOutput() {
//	return showTextOutput;
//	}

//	/**
//	* 
//	* @param showOutputData the showOutputData to set
//	*/
//	public void setShowTextOutput(boolean bShow) {
//	this.showTextOutput = bShow;
//	}

//	public String showTextOutputTipText() {
//	return "Indicates whether further text output should be printed";
//	}

	public boolean isOutputAdditionalInfo() {
		return showAdditionalProblemInfo;
	}

	public void setOutputAdditionalInfo(boolean showAdd) {
		showAdditionalProblemInfo = showAdd;
	}

	public String outputAdditionalInfoTipText() {
		return "Activate to output additional problem information per iteration, such as the current solution representation.";
	}

	public void hideHideable() {
		setOutputVerbosity(getOutputVerbosity());
	}
	
	public void setOutputVerbosity(SelectedTag sTag) {
		outputVerbosity = sTag;
		GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", sTag.getSelectedTagID() != VERBOSITY_KTH_IT);
	}
	
	public void setOutputVerbosity(int i) {
		outputVerbosity.setSelectedTag(i);
		GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", outputVerbosity.getSelectedTagID() != VERBOSITY_KTH_IT);
	}
	
	public SelectedTag getOutputVerbosity() {
		return outputVerbosity;
	}

	public String outputVerbosityTipText() {
		return "Set the data output level.";
	}

	public int getOutputVerbosityK() {
		return verboK;
	}

	public void setOutputVerbosityK(int k) {
		verboK = k;
	}

	public String outputVerbosityKTipText() {
		return "Set the interval of data output for intermediate verbosity (in generations).";
	}

	public SelectedTag getOutputTo() {
		return outputTo;
	}

	public void setOutputTo(SelectedTag tag) {
		outputTo = tag;
	}

	public void setOutputTo(int i) {
		outputTo.setSelectedTag(i);
	}

	public String outputToTipText() {
		return "Set the output destination; to deactivate output, set verbosity to none.";
	}

}