package javaeva.server.stat;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javaeva.server.go.IndividualInterface;
import javaeva.server.go.PopulationInterface;

/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
/**
 *
 */
public class StatisticsStandalone implements Statistics, Serializable {
  public final static boolean TRACE = false;
  private static String m_MyHostName = "not def";

  private long m_StartTime;
  private String m_StartDate;

  private IndividualInterface m_BestIndividual;

  private int m_FunctionCalls;
  private int m_OptRunsPerformed;
  private String m_InfoString;
  private ArrayList m_Result;
  private ArrayList m_ResultString;
  private boolean m_useMedian = false;
  private boolean m_TimeTrace;

  private double[] m_SpecificData;
  private StatisticsParameter m_StatisticsParameter;
  private boolean m_NoTrace;
  private double m_FitnessMeanofALL;
  private double m_SumOfBestFit = 0;
  private int m_FunctionALLCalls;
  private int m_NumberOfConvergence;
  private double[] m_BestFitnessAtEnd;
  private double m_ConvergenceRate;
  private double m_FitnessMedianofALL;
  /**
   *
   */
  public StatisticsStandalone(boolean trace) {
    m_NoTrace = trace;
    m_StatisticsParameter = new StatisticsParameterImpl();
    try {
      m_MyHostName = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      System.out.println("ERROR getting HostName " + e.getMessage());
    }
    m_OptRunsPerformed = 0;
    m_NumberOfConvergence = 0;
    m_FunctionALLCalls = 0;

  }

  /**
   *
   */
  public StatisticsStandalone() {
    m_StatisticsParameter = new StatisticsParameterImpl();
    try {
      m_MyHostName = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      System.out.println("ERROR getting HostName " + e.getMessage());
    }
    m_OptRunsPerformed = 0;
    m_NumberOfConvergence = 0;
    m_FunctionALLCalls = 0;

  }


  /**
   *
   */
  public void setTimeTrace() {
    m_TimeTrace = true;
  }

  /**
   *
   */
  public synchronized void createNextGenerationPerformed(double[] bestfit, double[] worstfit, int calls) {
    m_FunctionCalls = calls;
    if (m_Result.size() == 0)
      initContainer(new String[] {"Fitness"});
    ((ArrayList[]) m_Result.get(0))[m_OptRunsPerformed].add(new double[] {m_FunctionCalls, bestfit[0]});

  }

  /**
   *
   */
  public void createNextGenerationPerformed(double bestfit, int calls) {
    if (m_TimeTrace == true)
      m_FunctionCalls = (int) (System.currentTimeMillis() - m_StartTime);
    else
      m_FunctionCalls = calls;
    if (m_Result.size() == 0)
      initContainer(new String[] {"Fitness"});
    ((ArrayList[]) m_Result.get(0))[m_OptRunsPerformed].add(new double[] {
            m_FunctionCalls, bestfit});
  }


  /**
   *
   */
  public void createNextGenerationPerformed(PopulationInterface Pop) {
    m_SpecificData = Pop.getSpecificData();
    if (m_TimeTrace == true)
      m_FunctionCalls = (int) (System.currentTimeMillis() - m_StartTime);
    else
      m_FunctionCalls = Pop.getFunctionCalls();
    if (m_NoTrace == true)
      return;
    if (m_SpecificData != null) {
      System.out.println(" m_SpecificData !!");
      for (int i = 0; i < m_SpecificData.length; i++) {
        if (m_Result.size() == 0)
          initContainer(Pop.getSpecificDataNames());
        ((ArrayList[]) m_Result.get(i))[m_OptRunsPerformed].add(new double[] {m_FunctionCalls, m_SpecificData[i]});
      }
      return;
    }
    // then the default procedure on the best individual
    this.m_BestIndividual = Pop.getBestIndividual();
    if (m_BestIndividual == null) {
      System.out.println("createNextGenerationPerformed BestInd==null in StatisticsStandalone");
      return;
    }
    double BestFitness = m_BestIndividual.getFitness()[0];
    if (m_Result.size() == 0)
      initContainer(new String[] {"Fitness"});
    ((ArrayList[]) m_Result.get(0))[m_OptRunsPerformed].add(new double[] {m_FunctionCalls, BestFitness});
  }

  /**
   *
   */
  private void initContainer(String[] description) {
    for (int i = 0; i < description.length; i++) {
      m_Result.add(new ArrayList[m_StatisticsParameter.getMultiRuns()]);
      m_ResultString.add(description[i]);
    }
    for (int i = 0; i < m_Result.size(); i++)
      ((ArrayList[]) m_Result.get(i))[m_OptRunsPerformed] = new ArrayList();
  }


  /**
   *
   */
  public void startOptPerformed(String InfoString, int runnumber) {
    if (m_OptRunsPerformed == 0) {
      m_Result = new ArrayList();
      m_ResultString = new ArrayList();
      m_BestFitnessAtEnd = new double[this.m_StatisticsParameter.getMultiRuns()];
    } else {
      for (int i = 0; i < m_Result.size(); i++)
        ((ArrayList[]) m_Result.get(i))[m_OptRunsPerformed] = new ArrayList();
    }
    m_InfoString = InfoString;
    m_StartTime = System.currentTimeMillis();
    if (m_OptRunsPerformed == 0) {
      SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
      m_StartDate = formatter.format(new Date());
    }
  }

  /**
   *
   */
  public void stopOptPerformed(boolean normal) {
	  if (!normal) {
		  System.err.println("stopped unsuccessfully... not updating stats...");
	  } else {
		  m_FunctionALLCalls = m_FunctionALLCalls + m_FunctionCalls;
		  if (m_BestIndividual != null) {
			  m_SumOfBestFit = m_SumOfBestFit + this.m_BestIndividual.getFitness()[0];
			  m_BestFitnessAtEnd[m_OptRunsPerformed] = this.m_BestIndividual.getFitness()[0];
		  }
//		  else {
//		  System.out.println(" else");
//		  }

		  m_OptRunsPerformed++;
		  if (m_BestIndividual != null) {
			  if (m_BestIndividual.getFitness()[0] < this.m_StatisticsParameter.getConvergenceRateThreshold())
				  m_NumberOfConvergence++;
		  }

		  //System.out.println("stopOptPerformed :"+m_OptRunsPerformed);
		  if (m_OptRunsPerformed == m_StatisticsParameter.getMultiRuns()) {
			  m_ConvergenceRate = ((double) m_NumberOfConvergence) / ((double) m_OptRunsPerformed);
			  m_FitnessMeanofALL = m_SumOfBestFit / ((double) m_OptRunsPerformed);
			  //System.out.println("m_FitnessMeanofALL  "+m_FitnessMeanofALL);
			  m_FitnessMedianofALL = getMedian(m_BestFitnessAtEnd);
			  m_OptRunsPerformed = 0;
			  m_NumberOfConvergence = 0;
			  if (TRACE)
				  System.out.println("stopOptPerformed");
			  if (TRACE)
				  System.out.println("End of ES run");
		  }
	  }
  }

  /**
   *
   */
  public void printToTextListener(String s) {
//    if (m_StatisticsParameter.getTextoutput() <= 0)
//      return;
//    if (m_ResultOut != null)
//      m_ResultOut.println(s);
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
    return m_BestIndividual.getDoubleArray();
  }

  /**
   *
   */
  public int getFitnessCalls() {
    return m_FunctionCalls;
  }

  /**
   *
   */
  public double[] getBestFitness() {
    return this.m_BestIndividual.getFitness();
  }

  /**
   *
   */
  private static double getMedian(double[] in) {
    double[] x = (double[]) in.clone();
    double ret = 0;
    Arrays.sort(x);
    int m = (int) (x.length / 2.0);
    return x[m];
  }


  /**
   * write result of all runs.
   */
  public static File[] writeResult_All_Container(ArrayList StatList, String FileName) {
    File ret[] = new File[((StatisticsStandalone) StatList.get(0)).m_Result.size()];
    //System.out.println("StatList.size " + StatList.size());
    for (int counter = 0; counter < ret.length; counter++) {
      ArrayList staticResult = new ArrayList();
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
//        if (m_useMedian == true) // use the median
//          mean = getMedian(value);
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

  /**
   * write result of all runs.
   */
  public static File writeResultErrorBar(ArrayList StatList, String FileName) {
    File ret = null;
    ArrayList staticResult = new ArrayList();
    String staticDescription = "";
    for (int index = 0; index < StatList.size(); index++) {
      StatisticsStandalone Stat = (StatisticsStandalone) StatList.get(index);
      staticDescription = staticDescription + Stat.m_InfoString + " ";
      System.out.println(" laenge m_result "+Stat.m_Result.size());
      for (int i=0;i<Stat.m_Result.size();i++) {
        ArrayList[] list = (ArrayList[])Stat.m_Result.get(i);
        System.out.println(" i "+i+"  ArrayList[]" +list.length);
        for (int j=0;j<list.length;j++) {
          if (staticResult.size()==j)
            staticResult.add(new String(""));
          String temp = (String)staticResult.get(j);
           double[] x = ((double[])list[j].get(list[j].size()-1));
          temp = temp +"  "+ x[1];staticResult.set(j,temp);
          //System.out.println(" list j "+j +" list[j].size "+ list[j].size());
          //System.out.println(" x "+x[0]+ " " + x.length);

        }
      }
    } // end of for index
    try {
      File d = new File(m_MyHostName);
      d.mkdir();
      ret = new File(m_MyHostName + "//" + "Error" + FileName + ".txt");
      ret.createNewFile();
      PrintWriter Out = new PrintWriter(new FileOutputStream(ret));
      Out.println(staticDescription);
      for (int i = 0; i < staticResult.size(); i++) {
        Out.println( (String) staticResult.get(i));
        System.out.println("print " + (String) staticResult.get(i));
      }
      Out.flush();
      Out.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    staticResult = null;
    staticDescription = "";
    return ret;
  }


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
   * @return
   */
  public double getConvergenceRate() {
    return m_ConvergenceRate;
  }

  /**
   *
   */
  public double getBestFitnessMedianofALL() {
    return m_FitnessMedianofALL;
  }

  /**
   *
   */
  public int getFitnessALLCalls() {
    return m_FunctionALLCalls;
  }

}