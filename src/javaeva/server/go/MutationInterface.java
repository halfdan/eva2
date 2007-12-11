package javaeva.server.go;
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
import javaeva.server.stat.Statistics;
/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/
/**
 *
 */
public interface MutationInterface {
  public void addStatisticsListner(Statistics e);
}