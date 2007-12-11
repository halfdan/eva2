package javaeva.server.stat;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 320 $
 *            $Date: 2007-12-06 16:05:11 +0100 (Thu, 06 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;
import java.lang.reflect.Field;
import javaeva.gui.DataViewer;
import javaeva.gui.DataViewerInterface;
import javaeva.gui.Graph;

import wsi.ra.jproxy.MainAdapterClient;
/**
 *
 */
public class GenericStatistics implements Serializable{
  public static boolean TRACE = false;
  //private Object m_target;
  private int m_Test;
  private String[] m_PropertyNames;
  private boolean[] m_State;
  private transient Field[] m_fields;
  private DataViewerInterface m_Viewer;
  private Graph m_Graph;
  private static MainAdapterClient m_MainAdapterClient;
  /**
   *
   */
  public static void setMainAdapterClient (MainAdapterClient x) {
    m_MainAdapterClient = x;
  }
  /**
   *
   */
  public GenericStatistics getClone() {
    return new GenericStatistics(this);
  }
  /**
   *
   */
  private GenericStatistics (GenericStatistics Source) {
    //m_target = Source.m_target;
    m_Test = Source.m_Test;
    m_PropertyNames = Source.m_PropertyNames;
    m_State = Source.m_State;
    m_fields = Source.m_fields;
    m_Viewer = Source.m_Viewer;
    m_Graph = Source.m_Graph;

  }
  /**
   *
   */
  public GenericStatistics(Object target) {
    //m_target = target;
    //System.out.println("GenericStatistics-->");
    try {
      m_fields = getDeclaredFields(target);
      //if (TRACE) System.out.println("fields-->"+m_fields.length);
      m_PropertyNames = new String [m_fields.length];
      m_State = new boolean [m_fields.length];
      for (int i=0;i<m_fields.length;i++) {
        String desc = m_fields[i].toString(); //System.out.println("desc "+desc);
        int istransient  = desc.indexOf("transient");
        //if (TRACE) System.out.println("Field :"+m_fields[i].getName() );
        Object FieldValue = null;
        if (istransient==-1 || m_fields[i].getName().equals("elementData")) {  // the elementdatahack
          m_fields[i].setAccessible(true);
          FieldValue = m_fields[i].get(target);
        }
        m_PropertyNames[i] = m_fields[i].getName();
      }
    } catch (Exception ex) {
        System.out.println("ERROR in GenericStatistics:"+ex.getMessage());
        ex.printStackTrace();
    }
  }
  /**
   *
   */
  public void setTest(int Test) {
    m_Test = Test;
  }
  /**
   *
   */
  public int getTest() {
    return m_Test;
  }
  /**
   *
   */
  public String[] getPropertyNames() {
    return m_PropertyNames;
  }
  /**
   *
   */
  public boolean[] getState() {
    return m_State;
  }
  /**
   *
   */
  public void setState(boolean[] x) {
    System.out.println("in statistics setState !!!!!!!!!!!!!!!!!!");
    m_State = x;
  }
  /**
   *
   */
  public void initViewer() {
    m_Viewer = DataViewer.getInstance(m_MainAdapterClient,"test");
    m_Graph = m_Viewer.getNewGraph("test");
  }
  /**
   *
   */
  public Field[] getDeclaredFields(Object target) {
    Field[] ret_1 = target.getClass().getSuperclass().getDeclaredFields();
    Field[] ret_2 = target.getClass().getDeclaredFields();
    Field[] ret = new Field[ret_1.length+ret_2.length];
    int index =0;
    for (int i=0;i<ret_1.length;i++) {
      ret[index] = ret_1[i];
      index++;
    }
    for (int i=0;i<ret_2.length;i++) {
      ret[index] = ret_2[i];
      index++;
    }
    return ret;
  }
  /**
   *
   */
  public void statechanged(Object target) {
    int len=0;
    for (int i=0;i<m_State.length;i++)
    if (m_State[i]==true) len++;
    if (len==0) return;
    if (m_Viewer == null) initViewer();
    double[] data = new double[len];
    try {
      m_fields = getDeclaredFields(target);
    } catch (Exception ex) {
      System.out.println("ERROR in GenericStatistics:"+ex.getMessage());
      ex.printStackTrace();
    }
    int index =0;
    for (int i=0;i<m_fields.length;i++) {
      for (int n=0;n<m_PropertyNames.length;n++) {
        if (this.m_State[n]==false)
          continue;
        if (m_fields[i].getName().equals(m_PropertyNames[n])) {
          String desc = m_fields[i].toString(); //System.out.println("desc "+desc);
          int istransient  = desc.indexOf("transient");
          //if (TRACE) System.out.println("Field :"+m_fields[i].getName() );
          Object FieldValue = null;
          if (istransient==-1 || m_fields[i].getName().equals("elementData")) {  // the elementdatahack
            m_fields[i].setAccessible(true);
            try {
              FieldValue = m_fields[i].get(target);
              //System.out.println("m_PropertyNames "+m_PropertyNames[n] +" value "+FieldValue.toString());
              if (FieldValue instanceof Double)
                data[index] = ((Double)FieldValue).doubleValue();
              if (FieldValue instanceof Integer)
                 data[index] = ((Integer)FieldValue).doubleValue();
              index++;
            } catch (Exception ex) {
              System.out.println("ERROR in GenericStatistics:"+ex.getMessage());
              ex.printStackTrace();
            }
          }
          break;
        }
      }
    }
    m_Graph.setConnectedPoint(data[1],data[0]);
  }
}