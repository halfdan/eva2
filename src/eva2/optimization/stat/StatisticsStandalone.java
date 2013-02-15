package eva2.optimization.stat;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.optimization.go.PopulationInterface;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This simple statistics implementation can collect all Object data available during runs.
 * Be aware that the memory requirements can be excessive depending on the data produced by
 * the additional informers, and depending on the selected fields to be collected. 
 * Therefore, the default is not to log the data but just print it using the super class.
 * 
 * @see InterfaceAdditionalPopulationInformer
 * @see AbstractStatistics
 */
public class StatisticsStandalone extends AbstractStatistics implements InterfaceStatistics, Serializable {
	private static final long serialVersionUID = -8451652609212653368L;

	private static String m_MyHostName = "unknown";

//	private String m_InfoString;
	private ArrayList<ArrayList<Object[]>> m_ResultData = null;
	private ArrayList<String> m_ResultHeaderStrings = null;
	private boolean collectData = false;
	
//	private boolean m_useMedian = false;

//	private double m_MeanFinalFitness;
//	private double m_SumLastBestCurrentFit = 0;
//	private double[] m_LastBestCurrentFit;
//	private double m_FitnessMedianofALL;

	public StatisticsStandalone(InterfaceStatisticsParameter statParams) {
		super();
		m_StatsParams = statParams;
		try {
			m_MyHostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			System.err.println("ERROR getting HostName " + e.getMessage());
		}
	}

	public StatisticsStandalone(String resultFileName) {
		this(resultFileName, 1, resultFileName==null ? StatisticsParameter.VERBOSITY_NONE : StatisticsParameter.VERBOSITY_FINAL, false);
	}
	
	public StatisticsStandalone(String resultFileName, int multiRuns, int verbosity, boolean outputAllFieldsAsText) {
		this(StatisticsParameter.getInstance(false));
		m_StatsParams.setMultiRuns(multiRuns);
		m_StatsParams.setOutputVerbosity(m_StatsParams.getOutputVerbosity().setSelectedTag(verbosity));
		m_StatsParams.setResultFilePrefix(resultFileName);
		m_StatsParams.setOutputAllFieldsAsText(outputAllFieldsAsText);
		if (resultFileName==null) {
                m_StatsParams.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_WINDOW);
            } 
		else {
                m_StatsParams.setOutputTo(m_StatsParams.getOutputTo().setSelectedTag(StatisticsParameter.OUTPUT_FILE));
            }
	}
	
	public StatisticsStandalone() {
		this(new StatisticsParameter());
	}

    @Override
	protected void initPlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
		if (collectData) {
			m_ResultData = new ArrayList<ArrayList<Object[]>>(m_StatsParams.getMultiRuns());
			List<String> description = getOutputHeaderFieldNames(informerList);
			m_ResultHeaderStrings = new ArrayList<String>();
			for (String str : description) {
                m_ResultHeaderStrings.add(str);
            }
			for (int i = 0; i < m_StatsParams.getMultiRuns(); i++) {
                m_ResultData.add(new ArrayList<Object[]>());
            }
		} else {
			m_ResultData=null;
			m_ResultHeaderStrings = null;
		}
	}
	
    @Override
	protected void plotCurrentResults() {
		if (collectData && (m_ResultData!=null)) {
			m_ResultData.get(optRunsPerformed).add(currentStatObjectData);
		}
	}

    @Override
	public void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
		if (TRACE) {
                System.out.println(" m_SpecificData !!");
            }
		double[] specificData = pop.getSpecificData();
		if (specificData != null) {
			for (int i = 0; i < specificData.length; i++) {
//				((ArrayList<Object[]>[]) m_Result.get(i))[optRunsPerformed].add(new Double[] {new Double(functionCalls), specificData[i]});
				m_ResultData.get(optRunsPerformed).add(new Object[] {new Double(functionCalls), specificData});
			}
		}
	}

//	public void startOptPerformed(String infoString, int runNumber, Object params) {
//		super.startOptPerformed(infoString, runNumber, params);
//		if (runNumber == 0) {
//			m_Result = new ArrayList<ArrayList<Object[]>>();
//			m_ResultString = new ArrayList<String>();
////			m_LastBestCurrentFit = new double[this.m_StatsParams.getMultiRuns()];
////			m_SumLastBestCurrentFit=0;
//		} else {
//			for (int i = 0; i < m_Result.size(); i++)
//				((ArrayList[]) m_Result.get(i))[optRunsPerformed] = new ArrayList<ArrayList<double[]>[]>();
//		}
//		m_InfoString = infoString;
//	}
	
//	public void stopOptPerformed(boolean normal, String stopMessage) {
//		super.stopOptPerformed(normal, stopMessage);
//		if (bestCurrentIndy != null) {
//			m_SumLastBestCurrentFit = m_SumLastBestCurrentFit + bestCurrentIndy.getFitness()[0];
//			m_LastBestCurrentFit[optRunsPerformed-1] = bestCurrentIndy.getFitness()[0];
//		}
//
//		//System.out.println("stopOptPerformed :"+m_OptRunsPerformed);
//		if (optRunsPerformed == m_StatsParams.getMultiRuns()) {
//			m_MeanFinalFitness = m_SumLastBestCurrentFit / ((double) optRunsPerformed);
//			//System.out.println("m_FitnessMeanofALL  "+m_FitnessMeanofALL);
//			m_FitnessMedianofALL = Mathematics.median(m_LastBestCurrentFit, true);
//
////			finalizeOutput();
//		}
//	}

//	/**
//	 * write result of all runs.
//	 */
//	public static File[] writeResult_All_Container(ArrayList<StatisticsStandalone> StatList, String FileName) {
//		File ret[] = new File[((StatisticsStandalone) StatList.get(0)).m_Result.size()];
//		//System.out.println("StatList.size " + StatList.size());
//		for (int counter = 0; counter < ret.length; counter++) {
//			ArrayList<String> staticResult = new ArrayList<String>();
//			String staticDescription = "calls ";
//			for (int index = 0; index < StatList.size(); index++) {
//				StatisticsStandalone Stat = (StatisticsStandalone) StatList.get(index);
//				staticDescription = staticDescription + Stat.m_InfoString + " ";
//				for (int row = 0; row < ((ArrayList[]) Stat.m_Result.get(counter))[0].size();
//				row++) {
//					double mean = 0;
//					double calls = 0;
//					double[] value = new double[((ArrayList[]) Stat.m_Result.get(counter)).
//					                            length];
//					for (int i = 0; i < ((ArrayList[]) Stat.m_Result.get(counter)).length; i++) {
//						//double[] result = (double[]) Stat.m_QRestultContainer[i].get(row);
//						double[] result = (double[]) ((ArrayList[]) Stat.m_Result.get(counter))[i].get(row);
//						mean = mean + result[1];
//						calls = result[0];
//						value[i] = result[1];
//					}
//					//mean = mean / Stat.m_QRestultContainer.length;
//					mean = mean / ((ArrayList[]) Stat.m_Result.get(counter)).length;
////					if (m_useMedian == true) // use the median
////					mean = getMedian(value);
//					if (row == staticResult.size())
//						staticResult.add(new String("" + calls));
//					String temp = (String) staticResult.get(row);
//					String newrow = new String(temp + " " + mean);
//					//System.out.println("newrow"+newrow);
//					staticResult.set(row, newrow);
//				} // end of for row
//			} // end of for index
//			try {
//				File d = new File(m_MyHostName);
//				d.mkdir();
//				String info = (String) ((StatisticsStandalone) StatList.get(0)).m_ResultString.get(counter);
//				ret[counter] = new File(m_MyHostName + "//" + FileName + "_" + info + "_" + counter + ".txt");
//				ret[counter].createNewFile();
//				PrintWriter Out = new PrintWriter(new FileOutputStream(ret[counter]));
//				Out.println(staticDescription);
//				for (int i = 0; i < staticResult.size(); i++) {
//					Out.println((String) staticResult.get(i));
//					//System.out.println("print " + (String) staticResult.get(i));
//				}
//				Out.flush();
//				Out.close();
//			} catch (Exception e) {
//				System.out.println("Error in wreiteresult" + e.getMessage());
//				e.printStackTrace();
//			}
//			staticResult = null;
//			staticDescription = "";
//		}
//		return ret;
//	}

//	/**
//	 * write result of all runs.
//	 */
//	public static File writeResultErrorBar(ArrayList StatList, String FileName) {
//		File ret = null;
//		ArrayList staticResult = new ArrayList();
//		String staticDescription = "";
//		for (int index = 0; index < StatList.size(); index++) {
//			StatisticsStandalone Stat = (StatisticsStandalone) StatList.get(index);
//			staticDescription = staticDescription + Stat.m_InfoString + " ";
//			System.out.println(" laenge m_result "+Stat.m_Result.size());
//			for (int i=0;i<Stat.m_Result.size();i++) {
//				ArrayList[] list = (ArrayList[])Stat.m_Result.get(i);
//				System.out.println(" i "+i+"  ArrayList[]" +list.length);
//				for (int j=0;j<list.length;j++) {
//					if (staticResult.size()==j)
//						staticResult.add(new String(""));
//					String temp = (String)staticResult.get(j);
//					double[] x = ((double[])list[j].get(list[j].size()-1));
//					temp = temp +"  "+ x[1];staticResult.set(j,temp);
//					//System.out.println(" list j "+j +" list[j].size "+ list[j].size());
//					//System.out.println(" x "+x[0]+ " " + x.length);
//
//				}
//			}
//		} // end of for index
//		try {
//			File d = new File(m_MyHostName);
//			d.mkdir();
//			ret = new File(m_MyHostName + "//" + "Error" + FileName + ".txt");
//			ret.createNewFile();
//			PrintWriter Out = new PrintWriter(new FileOutputStream(ret));
//			Out.println(staticDescription);
//			for (int i = 0; i < staticResult.size(); i++) {
//				Out.println( (String) staticResult.get(i));
//				System.out.println("print " + (String) staticResult.get(i));
//			}
//			Out.flush();
//			Out.close();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		staticResult = null;
//		staticDescription = "";
//		return ret;
//	}

//	/**
//	 *
//	 */
//	public double getBestFitnessMeanofALL() {
//		return m_MeanFinalFitness;
//	}
//
//	/**
//	 *
//	 */
//	public double getBestFitnessMedianofALL() {
//		return m_FitnessMedianofALL;
//	}
	
    @Override
	public String getHostName() {
		return m_MyHostName;
	}

	/**
	 * Check whether data collection is activated, which stores an Object[] for every iteration and
	 * every multi-run.
	 * @return
	 */
	public boolean isCollectData() {
		return collectData;
	}

	/**
	 * Set state of full data collection, which stores an Object[] for every iteration and
	 * every multi-run.
	 * 
	 * @param collectFullData
	 */
	public void setCollectData(boolean collectFullData) {
		this.collectData = collectFullData;
	}
	
	public ArrayList<ArrayList<Object[]>> getCollectedData() {
		return m_ResultData;
	}

	public ArrayList<Object[]> getCollectedRunData(int runIndex) {
		return m_ResultData.get(runIndex);
	}
	
	public ArrayList<String> getCollectedDataHeaders() {
		return m_ResultHeaderStrings;
	}
}