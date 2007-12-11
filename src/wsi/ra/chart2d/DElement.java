/**
 *  Filename: $RCSfile: DElement.java,v $
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
 * IMPORTS
 *==========================================================================*/

import java.awt.Color ;

/*==========================================================================*
 * INTERFACE DECLARATION
 *==========================================================================*/

/**
 * some useful methods for objects which should be paintable in a scaled area
 */
public interface DElement
{
  Color DEFAULT_COLOR = Color.black;
  DRectangle getRectangle();

  void setDParent( DParent parent );
  DParent getDParent();

  void paint( DMeasures m );
  void repaint();

  void setVisible( boolean aFlag );
  boolean isVisible();

  void setColor( Color color );
  Color getColor();

  void setDBorder( DBorder b );
  DBorder getDBorder();
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
