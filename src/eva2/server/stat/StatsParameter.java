package eva2.server.stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfaceNotifyOnInformers;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;
import eva2.tools.StringSelection;

/**
 * A set of parameters for statistics in EvA2. Several data entries are provided by the AbstractStatistics class,
 * others by the additional informers. This class allows customization of entries and frequency of data output.
 * Data entries can be selected using a StringSelection instance.
 * There is a switch called "output full data as text" which will be interpreted by AbstractStatistics showing
 * all or only the selected entities.
 * 
 * @see AbstractStatistics
 * @author mkron
 */

public class StatsParameter implements InterfaceStatisticsParameter, InterfaceNotifyOnInformers, Serializable {
	private static final long serialVersionUID = -8681061379203108390L;

	private static boolean TRACE = false;
	
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

//	private int m_PlotFitness = PLOT_BEST;
	private int m_Textoutput = 0;
//	private int m_Plotoutput = 1;
	private int m_MultiRuns = 1;
	private String m_ResultFilePrefix = "EvA2";
	protected String m_Name = "not defined";
//	protected String m_InfoString = "";
	private boolean m_useStatPlot = true;
	private boolean showAdditionalProblemInfo = false;
	private double m_ConvergenceRateThreshold=0.001;

	private StringSelection graphSel = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings());

	/**
	 *
	 */
	public static StatsParameter getInstance(boolean loadDefaultSerFile) {
		if (loadDefaultSerFile) return getInstance("Statistics.ser");
		else return new StatsParameter();
	}
	
	/**
	 * Try to load instance from serialized file. If impossible, instantiate a new one.
	 */
	public static StatsParameter getInstance(String serFileName) {
		if (TRACE ) System.out.println("Loading serialized stats..");
		StatsParameter Instance = (StatsParameter) Serializer.loadObject(serFileName);
		if (Instance == null) {
			Instance = new StatsParameter();
			if (TRACE) System.out.println("Loading failed!");
		}
		return Instance;
	}

	/**
	 *
	 */
	public StatsParameter() {
		m_Name = "Statistics";
		outputVerbosity.setSelectedTag(VERBOSITY_KTH_IT);
		outputTo.setSelectedTag(1);
	}

	/**
	 *
	 */
	public String toString() {
		String ret = "\r\nStatParameter (" + super.toString() + "):\r\nm_MultiRuns=" + m_MultiRuns +
		", m_Textoutput=" + m_Textoutput +
//		", m_Plotoutput=" + m_Plotoutput +
		", verbosity= " + outputVerbosity.getSelectedString() +
		"\nTo " + outputTo.getSelectedString() + 
		", " + BeanInspector.toString(graphSel.getStrings());
		return ret;
	}

//	/**
//	 * Return a list of String arrays describing the selected plot options, e.g. {"Best"} or {"Best", "Worst"}.
//	 * For now, only one array is returned.
//	 * 
//	 * @return a list of String arrays describing the selected plot options
//	 */
//	public ArrayList<String[]> getPlotDescriptions() {
//		ArrayList<String> desc = new ArrayList<String>();
//		for (int i=0; i<graphSel.getLength(); i++) {
//			if (graphSel.isSelected(i)) desc.add(graphSel.getElement(i));
//		}
//		ArrayList<String[]> alist = new ArrayList<String[]>();
//		alist.add(desc.toArray(new String[desc.size()]));
//		return alist;
//	}

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
//		m_Plotoutput = Source.m_Plotoutput;
//		m_PlotFitness = Source.m_PlotFitness;
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

	public static String globalInfo() {
		return "Configure statistics and output of the optimization run. Changes to the data selection state will not take effect during a run.";
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

//	/**
//	 *
//	 */
//	public String GetInfoString() {
//		return m_InfoString;
//	}
//
//	/**
//	 *
//	 */
//	public void setInfoString(String s) {
//		m_InfoString = s;
//	}

	/**
	 *
	 */
	public String infoStringTipText() {
		return "Infostring displayed on fitness graph by prssing the right mouse button.";
	}

	/**
	 *  Use averaged graph for multi-run plots or not
	 */
	public boolean GetUseStatPlot() {
		return m_useStatPlot;
	}

	/**
	 * Activate or deactivate averaged graph for multi-run plots
	 */
	public void setUseStatPlot(boolean x) {
		m_useStatPlot = x;
	}

	public String useStatPlotTipText() {
		return "Plotting each fitness graph separately if multiruns > 1.";
	}

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

	public boolean isOutputAllFieldsAsText() {
		return showAdditionalProblemInfo;
	}

	public void setOutputAllFieldsAsText(boolean bShowFullText) {
		showAdditionalProblemInfo = bShowFullText;
	}

	public String outputAllFieldsAsTextTipText() {
		return "Output all available data fields or only the selected entries as text.";
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

	public StringSelection getFieldSelection() {
		return graphSel;
	}
	
	public void setFieldSelection(StringSelection v) {
		graphSel = v;
	}
	
	public String fieldSelectionTipText() {
		return "Select the data fields to be collected and plotted. Note that only simple types can be plotted to the GUI.";
	}

	/**
	 * May be called to dynamically alter the set of graphs that can be selected, 
	 * using a list of informer instances, which usually are the problem and the
	 * optimizer instance.
	 * 
	 * @see InterfaceAdditionalPopulationInformer
	 */
	public void setInformers(
			List<InterfaceAdditionalPopulationInformer> informers) {
		ArrayList<String> headerFields = new ArrayList<String>();
		ArrayList<String> infoFields = new ArrayList<String>();
		// parse list of header elements, show additional Strings according to names.
		for (InterfaceAdditionalPopulationInformer inf : informers) {
			headerFields.addAll(Arrays.asList(inf.getAdditionalFileStringHeader()));
			if (infoFields.size()<headerFields.size()) { // add info strings for tool tips - fill up with null if none have been returned.
				String[] infos = inf.getAdditionalFileStringInfo();
				if (infos!=null) infoFields.addAll(Arrays.asList(infos));
				while (infoFields.size()<headerFields.size()) infoFields.add(null);
			}
//			header += inf.getAdditionalFileStringHeader(null); // lets hope this works with a null 
		}
		// create additional fields to be selectable by the user, defined by the informer headers
//		StringSelection ss = new StringSelection(GraphSelectionEnum.getAndAppendArray(headerFields.toArray(new String[headerFields.size()])));
		StringSelection ss = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings(), 
				headerFields, infoFields.toArray(new String[infoFields.size()]));
		ss.takeOverSelection(graphSel);
//		System.out.println("In " + this + ": setting new informers: " + BeanInspector.toString(ss.getStrings()));
		// This works!
		setFieldSelection(ss);
//		System.out.println("After: " + this);
	}
}