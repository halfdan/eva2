package eva2.tools;
/**
 * Title:        EvA2
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
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 * This class represents a cluster object in the R^N.
 */
public class Cluster {
  /**
   * Number of samples in the cluster.
   */
  public int m_SamplesInCluster;
  /**
   * Center of the cluster.
   */
  public double[] m_Center;
  /**
   * nearest sample (double[]) to the center of the cluster.
   */
  public double[] m_NearestSample;
  /**
   * This class represents a cluster of
   * sample points.
   *
   * @param center center
   * @param SamplesInCluster Number of samples in cluster
   * @param nearestSample Nearest sample to cluster center.
   */
  public Cluster(double[] center,int SamplesInCluster,double[] nearestSample) {
    m_SamplesInCluster = SamplesInCluster;
    m_Center = center;
    m_NearestSample = nearestSample;
  }
}