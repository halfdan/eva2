package eva2.server.stat;

/*
 * Title:        JavaEvA
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.PopulationInterface;
import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;


/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
/**
 *
 */
public class StatisticsStandalone extends AbstractStatistics implements InterfaceStatistics, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2621394534751748968L;

	private static String m_MyHostName = "not def";

	private String m_InfoString;
	private ArrayList<ArrayList<double[]>[]> m_Result;
	private ArrayList<String> m_ResultString;
//	private boolean m_useMedian = false;

	private double m_FitnessMeanofALL;
	private double m_SumOfBestFit = 0;
	private double[] m_BestFitnessAtEnd;
	private double m_FitnessMedianofALL;
	/**
	 *
	 */
	public StatisticsStandalone(InterfaceStatisticsParameter statParams) {
		super();
		m_StatsParams = statParams;
		try {
			m_MyHostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			System.out.println("ERROR getting HostName " + e.getMessage());
		}
	}

	/**
	 *
	 */
	public StatisticsStandalone(String resultFileName) {
		this(new StatsParameter());
		m_StatsParams.SetResultFilePrefix(resultFileName);
	}

	/**
	 *
	 */
	public StatisticsStandalone() {
		this(new StatsParameter());
	}
	
	protected void initPlots(List<String[]> description) {
		if (m_Result.size() == 0)
			initContainer(new String[] {"Fitness"});
	}
	
	protected void plotCurrentResults() {
		((ArrayList<double[]>[]) m_Result.get(0))[optRunsPerformed].add(new double[] {functionCalls, currentBestFit[0]});
	}

	public void plotSpecificData(PopulationInterface pop, InterfaceAdditionalPopulationInformer informer) {
		if (TRACE) System.out.println(" m_SpecificData !!");
		double[] specificData = pop.getSpecificData();
		if (specificData != null) {
			for (int i = 0; i < specificData.length; i++) {
				((ArrayList<double[]>[]) m_Result.get(i))[optRunsPerformed].add(new double[] {functionCalls, specificData[i]});
			}
		}
	}

	/**
	 *
	 */
	private void initContainer(String[] description) {
		for (int i = 0; i < description.length; i++) {
			m_Result.add(new ArrayList[m_StatsParams.getMultiRuns()]);
			m_ResultString.add(description[i]);
		}
		for (int i = 0; i < m_Result.size(); i++)
			((ArrayList[]) m_Result.get(i))[optRunsPerformed] = new ArrayList<double[]>();
	}

	public void startOptPerformed(String infoString, int runNumber, Object params) {
		super.startOptPerformed(infoString, runNumber, params);
		if (runNumber == 0) {
			m_Result = new ArrayList<ArrayList<double[]>[]>();
			m_ResultString = new ArrayList<String>();
			m_BestFitnessAtEnd = new double[this.m_StatsParams.getMultiRuns()];
		} else {
			for (int i = 0; i < m_Result.size(); i++)
				((ArrayList[]) m_Result.get(i))[optRunsPerformed] = new ArrayList<ArrayList<double[]>[]>();
		}
		m_InfoString = infoString;
	}
	
	public void stopOptPerformed(boolean normal) {
		super.stopOptPerformed(normal);

		if (bestCurrentIndividual != null) {
			m_SumOfBestFit = m_SumOfBestFit + bestCurrentIndividual.getFitness()[0];
			m_BestFitnessAtEnd[optRunsPerformed-1] = bestCurrentIndividual.getFitness()[0];
		}

		//System.out.println("stopOptPerformed :"+m_OptRunsPerformed);
		if (optRunsPerformed == m_StatsParams.getMultiRuns()) {
			m_FitnessMeanofALL = m_SumOfBestFit / ((double) optRunsPerformed);
			//System.out.println("m_FitnessMeanofALL  "+m_FitnessMeanofALL);
			m_FitnessMedianofALL = getMedian(m_BestFitnessAtEnd);

//			finalizeOutput();
		}
	}

	/**
	 *
	 */
	private static double getMedian(double[] in) {
		double[] x = (double[]) in.clone();
//		double ret = 0;
		Arrays.sort(x);
		int m = (int) (x.length / 2.0);
		return x[m];
	}


	/**
	 * write result of all runs.
	 */
	public static File[] writeResult_All_Container(ArrayList<StatisticsStandalone> StatList, String FileName) {
		File ret[] = new File[((StatisticsStandalone) StatList.get(0)).m_Result.size()];
		//System.out.println("StatList.size " + StatList.size());
		for (int counter = 0; counter < ret.length; counter++) {
			ArrayList<String> staticResult = new ArrayList<String>();
			String staticDescription = "calls ";
			for (int index = 0; index < StatList.size(); index++) {
				StatisticsStandalone Stat = (StatisticsStandalone) StatList.get(index);
				staticDescription = staticDescription + Stat.m_InfoString + " ";
				for (int row = 0; row < ((ArrayList[]) Stat.m_Result.get(counter))[0].size();
				row++) {
					double mean = 0;
					double calls = 0;
					double[] value = new double[((ArrayList[]) Stat.m_Result.get(counter)).
					                            length];
					for (int i = 0; i < ((ArrayList[]) Stat.m_Result.get(counter)).length; i++) {
						//double[] result = (double[]) Stat.m_QRestultContainer[i].get(row);
						double[] result = (double[]) ((ArrayList[]) Stat.m_Result.get(counter))[i].get(row);
						mean = mean + result[1];
						calls = result[0];
						value[i] = result[1];
					}
					//mean = mean / Stat.m_QRestultContainer.length;
					mean = mean / ((ArrayList[]) Stat.m_Result.get(counter)).length;
//					if (m_useMedian == true) // use the median
//					mean = getMedian(value);
					if (row == staticResult.size())
						staticResult.add(new String("" + calls));
					String temp = (String) staticResult.get(row);
					String newrow = new String(temp + " " + mean);
					//System.out.println("newrow"+newrow);
					staticResult.set(row, newrow);
				} // end of for row
			} // end of for index
			try {
				File d = new File(m_MyHostName);
				d.mkdir();
				String info = (String) ((StatisticsStandalone) StatList.get(0)).m_ResultString.get(counter);
				ret[counter] = new File(m_MyHostName + "//" + FileName + "_" + info + "_" + counter + ".txt");
				ret[counter].createNewFile();
				PrintWriter Out = new PrintWriter(new FileOutputStream(ret[counter]));
				Out.println(staticDescription);
				for (int i = 0; i < staticResult.size(); i++) {
					Out.println((String) staticResult.get(i));
					//System.out.println("print " + (String) staticResult.get(i));
				}
				Out.flush();
				Out.close();
			} catch (Exception e) {
				System.out.println("Error in wreiteresult" + e.getMessage());
				e.printStackTrace();
			}
			staticResult = null;
			staticDescription = "";
		}
		return ret;
	}

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


	/**
	 *
	 */
	public static File writeString(String FileName, String Result) {
		File f = null;
		try {
			f = new File(FileName + ".txt");
			f.createNewFile();
			PrintWriter Out = new PrintWriter(new FileOutputStream(f));
			Out.println(Result);
			Out.flush();
			Out.close();
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
		return f;
	}

	/**
	 *
	 */
	public double getBestFitnessMeanofALL() {
		return m_FitnessMeanofALL;
	}

	/**
	 *
	 */
	public double getBestFitnessMedianofALL() {
		return m_FitnessMedianofALL;
	}

	public String getHostName() {
		return m_MyHostName;
	}
}