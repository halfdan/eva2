/**
 *  Filename: $RCSfile: DParent.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:41 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package wsi.ra.chart2d;


/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/

public interface DParent
{
  void addDElement( DElement e );
  boolean removeDElement( DElement e );
  void repaint( DRectangle r );
  DElement[] getDElements();
  boolean contains( DElement e );
  void addDBorder( DBorder b );
  void restoreBorder();
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
