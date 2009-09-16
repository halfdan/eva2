/**
 *  Filename: $RCSfile: XYDoubleArray.java,v $
 *  Purpose:  A description of the contents of this file.
 *  Language: Java
 *  Compiler: JDK 1.4
 *  Authors:  Joerg K. Wegner
 *  Version:  $Revision: 1.6 $
 *            $Date: 2001/10/17 11:37:42 $
 *            $Author: wegnerj $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

/*==========================================================================*
 * PACKAGE
 *==========================================================================*/

package eva2.tools.sort;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * Defines two <code>double[]</code> arrays.
 *
 * @version         $Revision: 1.6 $, $Date: 2001/10/17 11:37:42 $
 *
 */
public class XYDoubleArray implements Cloneable{
  /*-------------------------------------------------------------------------*
   * public member variables
   *-------------------------------------------------------------------------*/
  //public static final int TABLE_OUTPUT=JChart2DUtils.TABLE_OUTPUT;
  //public static final int VECTOR_OUTPUT=JChart2DUtils.VECTOR_OUTPUT;
  public double[] x=null;
  public double[] y=null;

  /*-------------------------------------------------------------------------*
   * constructor
   *-------------------------------------------------------------------------*/
  public XYDoubleArray(int length)
  {
    this.x=new double[length];
    this.y=new double[length];
  }

  public XYDoubleArray(double[] x, double[] y)
  {
    this.x=x;
    this.y=y;
  }

  /*-------------------------------------------------------------------------*
   * public methods
   *-------------------------------------------------------------------------*/

  public final void swap(int a, int b)
  {
    double xx = x[a]; x[a] = x[b]; x[b] = xx;
    double yy = y[a]; y[a] = y[b]; y[b] = yy;
  }

  /*public final void sortX(){
    QuickInsertSort quickInsertSort = new QuickInsertSort();
    quickInsertSort.sortX(this);
  }

  public final void sortY(){
    QuickInsertSort quickInsertSort = new QuickInsertSort();
    quickInsertSort.sortY(this);
  }*/

  //
  // uses jchart2d methods !;-(
  //
  /*public String toString(){
    return toString(TABLE_OUTPUT);
  }

  private String toString(int output){
    if(x==null || y==null)return null;
    BasicDataSet data=new BasicDataSet(x, y,
                                       AbstractDataSet.CONTINUOUS_DATA,
                                       "x",
                                       "y");
    String s=JChart2DUtils.writeDataSetToString(data, output);
    return s;
  }*/

  public Object clone()
  {
    double[] newX=new double[x.length];
    double[] newY=new double[y.length];
    XYDoubleArray newArray=new XYDoubleArray(newX, newY);

    for(int i=0;i<x.length;i++){
      newArray.x[i]=x[i];
      newArray.y[i]=y[i];
    }

    return newArray;
  }
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/