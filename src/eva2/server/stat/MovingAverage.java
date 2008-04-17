package eva2.server.stat;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 16 $
 *            $Date: 2006-12-20 10:42:57 +0100 (Wed, 20 Dec 2006) $
 *            $Author: marcekro $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public class MovingAverage {
  private int m_size = 0;
  private int m_index = 0;
  private double m_Average;
  private double[] m_array;
  private boolean m_overflow = false;
  /**
   *
   */
  public MovingAverage(int size) {
    m_size = size;
    m_array = new double[size];
  }
  /**
   *
   */
  private MovingAverage(MovingAverage Source) {
    m_size = Source.m_size;
    m_index = Source.m_index;
    m_Average = Source.m_Average;
    m_array = (double[])Source.m_array.clone();
    m_overflow = Source.m_overflow;
  }
  /**
   *
   */
  public MovingAverage getClone() {
    return new MovingAverage(this);
  }
  /**
   *
   */
  public void add (double value) {
    m_array[m_index] = value;
    m_index++;
    if (m_index==m_size) {
      m_index=0;
      m_overflow = true;
    }
    //
    m_Average = 0;
    int tail = m_index;
    //if (m_overflow=true)
    if (m_overflow) // MK 12/06
      tail = m_size;
    for (int i=0;i<tail;i++) {
      m_Average = m_Average + m_array[i];
    }
    m_Average = m_Average / tail;
  }
  /**
   *
   */
  public double getAverage() {
    return m_Average;
  }
}