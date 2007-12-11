package javaeva.tools;
/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.util.Comparator;
import wsi.ra.math.RNG;
/**
 *
 */
public class KMEANSJAVA {
  static public boolean TRACE = false;
  protected double[][] m_C;
  protected int[] m_IDX;
  /**
   *
   */
  public double[][] getC() {
    return m_C;
  }
  /**
   *
   */
  public int[] getIDX() {
    return m_IDX;
  }
  /**
   *
   */
  private double dist (double[] x1,double[] x2) {
    double ret = 0;
    for (int i=0;i<x1.length;i++)
      ret = ret + (x1[i]-x2[i]) *  (x1[i]-x2[i]);
    return Math.sqrt(ret);
  }
  /**
   *
   */
  public KMEANSJAVA(double[][] samples, int K, int iterations) {
    //System.out.print("in");
    if (TRACE) System.out.println("K"+K);
    if (K>samples.length) K = samples.length;
    int counter=0;
    m_C = new double[K][];
    for (int i=0;i<K;i++)  // random Init !!
      m_C[i] = (double[])samples[i].clone();
    m_IDX = new int[samples.length];
    while (counter++<iterations) {
      // determine m_IDX start
      for (int i=0;i<m_IDX.length;i++) {
        int index_nc =0; // index of nearest cluster
        double mindist = 999999999;
        for (int j=0;j<m_C.length;j++) {
          if (mindist>dist(samples[i],m_C[j])) {
            mindist=dist(samples[i],m_C[j]);
            index_nc = j;
          }
        }
        m_IDX[i] = index_nc;
      }
      // determine m_IDX end !
      // determine the new centers
      for (int indexofc=0;indexofc<m_C.length;indexofc++) {
        double[] newcenter = new double[samples[0].length];
        int treffer = 0;
        for (int j=0;j<m_IDX.length;j++) { //System.out.println("j="+j);
          if (m_IDX[j]==indexofc) {
            treffer++;
            for (int d =0;d<newcenter.length;d++) {
              newcenter[d] = newcenter[d] + m_C[m_IDX[j]][d];
              //newcenter[d] = newcenter[d] + samples[j][d];
            }
          }
        }
        for (int d =0;d<newcenter.length;d++)
          newcenter[d] = newcenter[d] / treffer;
         m_C[indexofc] = newcenter;
      }
      // determine the new centers
    }
    //System.out.println("out");
  }
  /**
   * Just a test function.
   *
   */
  public static void main( String[] args ){
    int k = 3;
    int samples = 10;
    int d = 2;
    double[][] test = new double[samples][d];
    for (int i=0;i<samples;i++) {
       for (int j=0;j<d;j++) {
        test[i][j] = RNG.randomDouble(0,10);
      }
    }
    KMEANSJAVA app = new KMEANSJAVA(test,k,5);
    double[][] c = app.getC();
    int[] idx = app.getIDX();
    System.out.println("c");
    for (int i= 0; i < c.length; i++) {
      for( int j = 0;j < c[i].length; j++) {
        System.out.print(c[i][j] +" ");
      }
      System.out.println("");
    }
    System.out.println("test");
    for (int i= 0; i < test.length; i++) {
      for( int j = 0;j < test[i].length; j++) {
        System.out.print(test[i][j] +" ");
      }
      System.out.println("");
    }
  }
}
/**
 *
 */
class ClusterComp implements Comparator {
  /**
   *
   */
  public ClusterComp () {}
  /**
   *
   */
  public int compare (Object p1,Object p2) {
    int x1 = ((Cluster) p1).m_SamplesInCluster;
    int x2 = ((Cluster) p2).m_SamplesInCluster;
    if (x1 > x2 )
      return -1;
    if (x1 <= x2 )
      return 1;
    return 0;
  }
  /**
   *
   */
  public boolean equals (Object x) {
    return false;
  }
}