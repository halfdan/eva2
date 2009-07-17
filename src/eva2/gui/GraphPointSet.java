package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 305 $
 *            $Date: 2007-12-04 12:10:04 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import wsi.ra.chart2d.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import eva2.tools.Mathematics;
 /*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
 /**
  *
  */
public class GraphPointSet {
  private String m_InfoString = "Incomplete_Run";
  private int m_GraphLabel;
  private int colorOffset = 0;
  private ArrayList<PointSet> m_PointSetContainer = new ArrayList<PointSet>();
  private float m_Stroke = (float) 1.0;
  private boolean m_isStatisticsGraph = false;
//  private DPointSet m_PointSet_1;
//  private DPointSet m_PointSet_2;
//  private DPointSet m_PointSet_3;
  private DPointSetMultiIcon m_ConnectedPointSet;
  private DArea m_Area;
  private Color m_Color;
  private DPointIcon m_Icon;
  private int m_CacheIndex = 0;
  private int m_CacheSize = 0;
  private double [] m_cachex;
  private double [] m_cachey;
  /**
   *
   */
  public GraphPointSet (/*int size*/int GraphLabel, DArea Area) {
    //System.out.println("Constructor GraphPointSet "+ GraphLabel);
    m_cachex = new double[m_CacheSize];
    m_cachey = new double[m_CacheSize];
    m_Area = Area;
    m_GraphLabel = GraphLabel;
    m_ConnectedPointSet = new DPointSetMultiIcon(100);
//    m_PointSet_1 = new DPointSet(100);
//    m_PointSet_2 = new DPointSet(100);
//    m_PointSet_3 = new DPointSet(100);
    //
//    DPointIcon icon1 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, 4);
//            }
//          public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//          };
//    DPointIcon icon2 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, -4);
//            }
//            public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//        };
    //
    m_ConnectedPointSet.setStroke(new BasicStroke( m_Stroke ));
    m_ConnectedPointSet.setConnected(true);
    
    setColor(indexToColor(GraphLabel));
    initGraph(Area);
  }
  
  private Color indexToColor(int index) {
	  Color c = Color.black;
	  int k = index%10;
	  switch(k) {
	  case 0: c = Color.black; break;
	  case 1: c = Color.red; break;
	  case 2: c = Color.blue; break;
	  case 3: c = Color.pink; break;
	  case 4: c = Color.green; break;
	  case 5: c = Color.gray; break;
	  case 6: c = Color.magenta; break;
	  case 7: c = Color.cyan; break;
	  case 8: c = Color.orange; break;
	  case 9: c = Color.darkGray; break;
	  }
	  return c;
  }
  
  /**
   * Increase the color sequentially.
   */
  public void incColor() {
	  colorOffset++;
	  setColor(indexToColor(m_GraphLabel+colorOffset));
  }
  
  public void setColorByIndex(int i) {
  	setColor(indexToColor(i));
  }
  
   /**
   *
   */
  private GraphPointSet (int size,int GraphLabel) {
    m_GraphLabel = GraphLabel;
    m_cachex = new double[m_CacheSize];
    m_cachey = new double[m_CacheSize];
    m_ConnectedPointSet = new DPointSetMultiIcon(100);
//    m_PointSet_1 = new DPointSet(100);
//    m_PointSet_2 = new DPointSet(100);
//    m_PointSet_3 = new DPointSet(100);
    //
//     DPointIcon icon1 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, 4);
//            }
//          public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//          };
//    DPointIcon icon2 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, -4);
//            }
//            public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//        };
//    m_PointSet_2.setIcon(icon1);
//    m_PointSet_3.setIcon(icon1);
    //
    m_ConnectedPointSet.setStroke(new BasicStroke( m_Stroke ));

    m_ConnectedPointSet.setConnected(true);
    m_Color = Color.black;

    m_Color = indexToColor(GraphLabel);

    m_ConnectedPointSet.setColor(m_Color);

  }
 /**
   *
   */
  public DPointSet printPoints() {
   for (int i = 0; i < m_ConnectedPointSet.getSize();i++) {
      DPoint p = m_ConnectedPointSet.getDPoint(i);
      double x = p.x;
      double y = p.y;
      //System.out.println("point "+i+ " x= "+x+"y= "+y);
    }
    return m_ConnectedPointSet.getDPointSet();
  }
  /**
   *
   */
  public Color getColor()  {
    return m_ConnectedPointSet.getColor();
  }
  /**
   *
   */
  public void setColor(Color c)  {
	  m_Color=c;
	  m_ConnectedPointSet.setColor(m_Color);
  }
  /**
   *
   */
  public DPointSet getConnectedPointSet()  {
    return m_ConnectedPointSet.getDPointSet();
  }
  public DPointSetMultiIcon getReference2ConnectedPointSet()  {
    return m_ConnectedPointSet;
  }

  /**
   *
   */
  public void initGraph (DArea Area) {
    m_Area = Area;
    m_Area.addDElement(m_ConnectedPointSet);
    ((FunctionArea)m_Area).addGraphPointSet(this);
//    m_Area.addDElement(m_PointSet_1);
//    m_Area.addDElement(m_PointSet_2);
//    m_Area.addDElement(m_PointSet_3);
//     DPointIcon icon1 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, 4);
//            }
//          public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//          };
//    DPointIcon icon2 = new DPointIcon(){
//            public void paint( Graphics g ){
//              g.drawLine(-2, 0, 2, 0);
//              g.drawLine(0, 0, 0, -4);
//            }
//            public DBorder getDBorder(){ return new DBorder(4, 4, 4, 4); }
//        };
  }
  /**
   *
   */
  public DPoint getNearestDPoint(DPoint p) {
    return m_ConnectedPointSet.getNearestDPoint(p);
  }
   
  /**
   * Causes the PointSet to interupt the connected painting at the
   * current position.
   */
  public void jump() {
    m_ConnectedPointSet.jump();
  }
  /**
   *
   */
  public void removeAllPoints() {
    m_ConnectedPointSet.removeAllPoints();
//    m_PointSet_1.removeAllPoints();
//    m_PointSet_2.removeAllPoints();
//    m_PointSet_3.removeAllPoints();
  }
  /**
   *
   */
  public void addDPoint(double x,double y) {
    //System.out.println(" "+x+" "+y);
    if (m_CacheIndex==m_CacheSize) {
      for (int i=0;i<m_CacheSize;i++) {
        m_ConnectedPointSet.addDPoint(m_cachex[i],m_cachey[i]);
      }
      m_ConnectedPointSet.addDPoint(x,y);
      m_CacheIndex=0;
    }
    else {
      m_cachex[m_CacheIndex]=x;
      m_cachey[m_CacheIndex]=y;
      m_CacheIndex++;
    }
  }
  /**
   *
   */
  public void addDPoint(DPoint p)  {
    m_ConnectedPointSet.addDPoint(p);
  }

  /**
   *
   */
  public void setIcon(DPointIcon p)  {
    this.m_Icon = p;
    this.m_ConnectedPointSet.setIcon(p);
  }
  /**
   *
   */
  public void setConnectedMode(boolean p)  {
    m_ConnectedPointSet.setConnected(p);
  }
  /**
   *
   */
  public boolean isStatisticsGraph() {
    return m_isStatisticsGraph;
  }

  /**
   * 
   */
  public int getPointCount() {
	  return m_ConnectedPointSet.getSize();
  }
  
  /**
   *
   */
  public PointSet getPointSet() {
    return new PointSet (this.m_ConnectedPointSet.getDPointSet());
  }
  /**
   *
   */
  public void removePoint(DPoint x) {
    System.out.println("removePoint "+x.x+ " "+ x.y);
    DPoint[] buf = new DPoint[m_ConnectedPointSet.getSize()];
    for (int i=0; i<m_ConnectedPointSet.getSize();i++) {
      buf[i] = m_ConnectedPointSet.getDPoint(i);
    }
    m_ConnectedPointSet.removeAllPoints();
    for (int i=0; i<buf.length;i++) {
      if(buf[i].x == x.x && buf[i].y == x.y)
        System.out.println("point found");
      else
        m_ConnectedPointSet.addDPoint(buf[i]);

    }
  }
  
  /**
   * Add a graph to another one forming a statistics graph if it isnt one already.
   * 
   * @param set
   * @param measures
   * @param useForce 	forces the add even if point counts mismatch, maybe losing some data points
   */
  public void addGraph (GraphPointSet set,DMeasures measures, boolean useForce) {
	  if (set.m_ConnectedPointSet.getSize()!=m_ConnectedPointSet.getSize() &&
			  m_ConnectedPointSet.getSize()!=0 && !useForce) {
		  System.err.println("WARNING addGraph not possible, lost last graph");
		  System.err.println(" m_ConnectedPointSet.getSize() "+ m_ConnectedPointSet.getSize());
		  return;
	  }
	  if (set.getPointSet().getSize()==0) {
		  System.err.println("Refusing to add empty graph...");
		  return;
	  }
	  m_isStatisticsGraph = true;
	  removeAllPoints();
	  m_ConnectedPointSet.setColor(set.getColor());

	  m_PointSetContainer.add(set.getPointSet());
	  int[] index = new int[m_PointSetContainer.size()];
	  int[] GraphSize = new int[m_PointSetContainer.size()];
	  for (int i=0;i<m_PointSetContainer.size();i++) {
		  GraphSize[i] = ((PointSet) m_PointSetContainer.get(i)).getSize();
		  if (GraphSize[i]<=0) System.err.println("Warning: invalid graph size of " + GraphSize[i] + " at " + i + "!  (GraphPointSet.addGraph)");
	  }
	  if (Mathematics.sum(GraphSize)==0) {
		  System.err.println("Error: not adding empty graphs... (GraphPointSet.addGraph)");
		  return;
	  }
	  boolean allSetsHaveMorePoints = true;
	  double nextXValue;
	  double[] y = new double[m_PointSetContainer.size()];
	  while ( allSetsHaveMorePoints ) { // Loop over all point sets, add them up and calc. mean  
		  // this is a bit more complicated because it is allowed that the point sets are asynchronouos 
		  // in the sense that the x values do not have to match - y values for any x value found are averaged 
		  // over all points. However curves may look strange if this happens, since they consist of
		  // heterogenous points. 
		  nextXValue = m_PointSetContainer.get(0).m_X[index[0]];
		  //System.out.println("m_PointSetContainer.size()"+m_PointSetContainer.size());
		  for (int i = 1;i<m_PointSetContainer.size();i++) { // search for smalles x value at next index
			  //System.out.println("i="+i);
			  if (nextXValue > m_PointSetContainer.get(i).m_X[index[i]]) {
				  nextXValue =  m_PointSetContainer.get(i).m_X[index[i]];
			  }
		  }
		  // Stelle nextXValue wird gezeichnet. jetzt alle y werte dazu finden
		  int numberofpoints =0;
		  for (int i = 0;i<m_PointSetContainer.size();i++) { // collect all points at next x-value
			  if (nextXValue == m_PointSetContainer.get(i).m_X[index[i]]) {
				  y[i]=m_PointSetContainer.get(i).m_Y[index[i]];
				  index[i]++;
				  numberofpoints++;
			  } else y[i]=0;
		  }
		  double ymean = Mathematics.sum(y)/numberofpoints;
		  // compute median double median = getMedian(y);
		  addDPoint(nextXValue,ymean);// System.out.println("ymean "+ymean+"  y.length "+ y.length);
		  //addDPoint(minx,median);// System.out.println("ymean "+ymean+"  y.length "+ y.length);
		  for (int i=0;i<m_PointSetContainer.size();i++) { // Stop if one of the point sets has no more points
			  if (GraphSize[i] <= index[i] ) {
				  allSetsHaveMorePoints = false;
				  break;
			  }
		  }
	  }
  }
  /**
   *
   */
  private double getMedian(double[] in) {
    double[] x = (double[]) in.clone();
    double ret = 0;
    Arrays.sort(x);
    int m = (int)( x.length/2.0);
    return x[m];
  }
  /**
   *
   */
  public int getGraphLabel () {
    return m_GraphLabel;
  }
//  /**
//   *
//   */
//  public void setUnconnectedPoint (double x, double y) {
//    m_PointSet_1.addDPoint(x,y);
//  }
  /**
   *
   */
  public String getInfoString () {
    return m_InfoString;
  }
  /**
   *
   */
  public void setInfoString (String x, float stroke ) {
    m_InfoString=x;
    m_Stroke = stroke;
    //setStroke(new BasicStroke( m_Stroke ));
  }
  
  /**
   *
   */
  class PointSet implements Serializable {
    private double[] m_X;
    private double[] m_Y;
    private Color m_Color;
    /**
     *
     */
    public PointSet (DPointSet pointset) {
      m_Color = pointset.getColor();
      m_X = new double[pointset.getSize()];
      m_Y = new double[pointset.getSize()];
      for (int i=0;i<pointset.getSize();i++) {
        DPoint point = pointset.getDPoint(i);
        m_X[i] = point.x;
        m_Y[i] = point.y;
      }
    }
    /**
     *
     */
    public DPointSet getDPointSet () {
      DPointSet ret = new DPointSet(100);
      ret.setColor(m_Color);
      for (int i=0;i<m_X.length;i++)
        ret.addDPoint(m_X[i],m_Y[i]);
      return ret;
    }
    /**
     *
     */
    public int getSize() {
      return m_X.length;
    }
    
//    /**
//     *
//     */
//    public DPointSet printPoints() {
//     for (int i = 0; i < m_ConnectedPointSet.getSize();i++) {
//        DPoint p = m_ConnectedPointSet.getDPoint(i);
//        double x = p.x;
//        double y = p.y;
//        //System.out.println("point "+i+ " x= "+x+"y= "+y);
//      }
//      return m_ConnectedPointSet.getDPointSet();
//    }

  }
}
