package eva2.server.stat;

import eva2.tools.SelectedTag;
import eva2.tools.StringSelection;

/**
 * An interface to encapsulate statistics parameters.
 *
 * @see StatsParameter
 */
public interface InterfaceStatisticsParameter {
  public String getName();
  public void saveInstance();
//  public String globalInfo();
//  public void setTextoutput(int i);
//  public void setPlotoutput(int i); // noone knows what these were useful for...
//  public int GetPlotoutput();
//  public int GetTextoutput();
//  public String textoutputTipText();
//  public String plotFrequencyTipText();
  public void setMultiRuns(int x);
  public int getMultiRuns();
  public String multiRunsTipText();

//  public String GetInfoString();
//  public void setInfoString(String s);
  
  public boolean GetUseStatPlot(); // use averaged graph for multi-run plots or not
  public void setUseStatPlot(boolean x); // activate averaged graph for multi-run plots
  
//  public List<String[]> getPlotDescriptions();

//  public SelectedTag getPlotData();
//  public void setPlotData(SelectedTag newMethod);
  
  public StringSelection getFieldSelection();
  public void setFieldSelection(StringSelection v);
  
  public String getResultFilePrefix();
  public void SetResultFilePrefix(String x);
  
  public void setConvergenceRateThreshold(double x);
  public double getConvergenceRateThreshold();

  public void SetShowTextOutput(boolean show);
  public boolean isShowTextOutput();
  
  public boolean isOutputAllFieldsAsText();
  public void setOutputAllFieldsAsText(boolean bShowFullText);
  
  public void setOutputVerbosity(SelectedTag sTag);
  public SelectedTag getOutputVerbosity();
  
  public int getOutputVerbosityK();
  public void setOutputVerbosityK(int k);
  
  public void setOutputTo(SelectedTag sTag);
  public SelectedTag getOutputTo();
}