package eva2.gui;

/*
 * Title:        EvA2
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.mocco.paretofrontviewer.InterfaceRefPointListener;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconContent;
import eva2.tools.chart2d.Chart2DDPointIconCross;
import eva2.tools.chart2d.Chart2DDPointIconPoint;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DArea;
import eva2.tools.chart2d.DBorder;
import eva2.tools.chart2d.DFunction;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.DPointSet;
import eva2.tools.chart2d.ScaledBorder;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class FunctionArea extends DArea implements Serializable {
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1238444548498667204L;
	private GraphPointSetLegend legendBox = null;
	private ScaledBorder m_Border;
	private DPointIcon m_CurrentPointIcon;
	private JFileChooser m_FileChooser;
	private boolean m_legend = true;
	private boolean m_log = false;
	private ArrayList<GraphPointSet> m_PointSetContainer;
	private InterfaceRefPointListener m_RefPointListener;
	private int m_x;

	/**
	 * 
	 */
	private int m_y;

	/**
	 * 
	 */
	private boolean notifyNegLog = true;
	
	/**
	 * Indicate whether graphs should be annotated with tool tips if pointed to with the mouse.
	 */
	private boolean doShowGraphToolTips = true;

	/**
	 *
	 */
	public FunctionArea() {
		super();
		setToolTipText("Graph Info ");
	}

	/**
	  *
	  */
	public FunctionArea(String xname, String yname) {
		this();
		setPreferredSize(new Dimension(600, 500));
		setVisibleRectangle(1, 1, 100000, 1000);
		setAutoFocus(true);
		setMinRectangle(0, 0, 1, 1);
		setBackground(new Color(253, 253, 253)); // not quite white
		// setAutoFocus(true);
		m_Border = new ScaledBorder();
		m_Border.x_label = xname; // "App. " + Name + " func. calls";
		m_Border.y_label = yname; // "fitness";
		setBorder(m_Border);
		setAutoGrid(true);
		setGridVisible(true);
		m_PointSetContainer = new ArrayList<GraphPointSet>(20);
		// new DMouseZoom( this );
		addPopup();
		repaint();
		notifyNegLog = true;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (isShowGraphToolTips()) {
			int gIndex = getNearestGraphIndex(event.getX(), event.getY());
			if (gIndex >= 0) {
				StringBuffer sb = new StringBuffer(super.getToolTipText());
				sb.append(gIndex);
				sb.append(": ");
				sb.append(getGraphInfo(gIndex));
				return sb.toString();
			} else return null;
		} else return null;
	}
	
	@Override
	public Point getToolTipLocation(MouseEvent event) {
		if (isShowGraphToolTips()) {
			int gIndex = getNearestGraphIndex(event.getX(), event.getY());
			if (gIndex >= 0) {
				DPoint pt = ((GraphPointSet) (m_PointSetContainer.get(gIndex))).getMedPoint();
				Point pt2 = getDMeasures().getPoint(pt.x, pt.y);
				pt2.x+=(5*(gIndex%7)); // slight shift depending on index - easier distinction of very close graphs
				pt2.y-=(10+(gIndex%3)*5);
				return pt2;
			} else return null;
		} else return null;
	}

	/**
	  *
	  */
	public void addGraph(int GraphLabel_1, int GraphLabel_2, boolean forceAdd) {
		getGraphPointSet(GraphLabel_1).addGraph(getGraphPointSet(GraphLabel_2),
				this.getDMeasures(), forceAdd);
		notifyNegLog = true;
	}

	public void addGraphPointSet(GraphPointSet d) {
		this.m_PointSetContainer.add(d);
	}

	/**
	 * Add a popup menu for displaying certain information.
	 */
	private void addPopup() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
					// do nothing
				} else {
					JPopupMenu GraphMenu = new JPopupMenu();
					m_x = e.getX();
					m_y = e.getY();

					// The first info element
					JMenuItem Info = new JMenuItem("Graph Info: "
							+ getGraphInfo(e.getX(), e.getY()));
					Info.addActionListener(new ActionListener() {
						//
						public void actionPerformed(ActionEvent ee) {
							DPoint temp = FunctionArea.this.getDMeasures()
									.getDPoint(FunctionArea.this.m_x,
											FunctionArea.this.m_y);
							DPointIcon icon1 = new DPointIcon() {
								public DBorder getDBorder() {
									return new DBorder(4, 4, 4, 4);
								}

								public void paint(Graphics g) {
									g.drawLine(-2, 0, 2, 0);
									g.drawLine(0, 0, 0, 4);
								}
							};
							temp.setIcon(icon1);
							FunctionArea.this.addDElement(temp);
						}
					});
					GraphMenu.add(Info);
					if (m_RefPointListener != null) {
						DPoint temp = getDMeasures().getDPoint(m_x, m_y);
						JMenuItem refPoint = new JMenuItem("Reference Point:("
								+ temp.x + "/" + temp.y + ")");
						refPoint.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								DPoint temp = getDMeasures()
										.getDPoint(m_x, m_y);
								double[] point = new double[2];
								point[0] = temp.x;
								point[1] = temp.y;
								m_RefPointListener.refPointGiven(point);
							}
						});
						GraphMenu.add(refPoint);
					}

					// darn this point is an empty copy !!
					DPoint point = getNearestDPoint(e.getX(), e.getY());

					if (point != null) {
						JMenuItem InfoXY = new JMenuItem("(" + point.x + "/"
								+ point.y + ")");
						Info.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
							}
						});
						GraphMenu.add(InfoXY);

						if (point.getIcon() instanceof InterfaceSelectablePointIcon) {
							m_CurrentPointIcon = point.getIcon();
							if (((InterfaceSelectablePointIcon) m_CurrentPointIcon)
									.getSelectionListener() != null) {
								JMenuItem select;
								AbstractEAIndividual indy = ((InterfaceSelectablePointIcon) m_CurrentPointIcon)
										.getEAIndividual();
								if (indy.isMarked())
									select = new JMenuItem(
											"Deselect individual");
								else
									select = new JMenuItem("Select individual");
								select.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ee) {
										((InterfaceSelectablePointIcon) m_CurrentPointIcon)
												.getSelectionListener()
												.individualSelected(
														((InterfaceSelectablePointIcon) m_CurrentPointIcon)
																.getEAIndividual());
									}
								});
								GraphMenu.add(select);
							}
						}

						if (point.getIcon() instanceof InterfaceDPointWithContent) {
							m_CurrentPointIcon = point.getIcon();
							JMenuItem drawIndy = new JMenuItem(
									"Show individual");
							drawIndy.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ee) {
									((InterfaceDPointWithContent) m_CurrentPointIcon)
											.showIndividual();
								}
							});
							GraphMenu.add(drawIndy);
						}

					}
					if (FunctionArea.this.m_PointSetContainer.size() > 0) { // there is at least one graph
						String togGTTName = (isShowGraphToolTips() ? "Deactivate":"Activate") + " graph tool tips";
						JMenuItem togGraphToolTips = new JMenuItem(togGTTName);
						togGraphToolTips.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								setShowGraphToolTips(!isShowGraphToolTips());
							}
						});
						GraphMenu.add(togGraphToolTips);
						
						String togLName = (isShowLegend() ? "Hide" : "Show" ) + " legend";
						JMenuItem togLegend = new JMenuItem(togLName);
						togLegend.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								toggleLegend();
							}
						});
						GraphMenu.add(togLegend);

						JMenuItem removeGraph = new JMenuItem("Remove graph");
						removeGraph.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								clearGraph(FunctionArea.this.m_x,
										FunctionArea.this.m_y);
							}
						});
						GraphMenu.add(removeGraph);

						JMenuItem changecolorGraph = new JMenuItem("Change graph color");
						changecolorGraph.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								changeColorGraph(FunctionArea.this.m_x,
										FunctionArea.this.m_y);
							}
						});
						GraphMenu.add(changecolorGraph);

						JMenuItem changeAllColors = new JMenuItem("Recolor all graphs");
						changeAllColors.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								recolorAllGraphsByIndex();
							}
						});
						GraphMenu.add(changeAllColors);

						JMenuItem removePoint = new JMenuItem("Remove point");
						removePoint.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ee) {
								removePoint(FunctionArea.this.m_x,
										FunctionArea.this.m_y);
							}
						});
						GraphMenu.add(removePoint);
					}
					GraphMenu.show(FunctionArea.this, e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * This method allows to add a selection listener to the PointIcon it should
	 * need more than one listener to this abstruse event
	 * 
	 * @param a
	 *            The selection listener
	 */
	public void addRefPointSelectionListener(InterfaceRefPointListener a) {
		this.m_RefPointListener = a;
	}

	/**
	  *
	  */
	public void changeColorGraph(int GraphLabel) {
		getGraphPointSet(GraphLabel).incColor();
		repaint();
	}
	
	public void setColorByIndex(int graphLabel, int colorIndex) {
		getGraphPointSet(graphLabel).setColorByIndex(colorIndex);
	}

	/**
	  *
	  */
	public void changeColorGraph(int x, int y) {
		int index = getNearestGraphIndex(x, y);
		if (index == -1)
			return;
		int GraphLabel = ((GraphPointSet) (this.m_PointSetContainer.get(index)))
				.getGraphLabel();
		changeColorGraph(GraphLabel);
		updateLegend();
	}

	/**
	 * Re-color all graphs which are nonempty by their index.
	 * 
	 */
	public void recolorAllGraphsByIndex() {
		int index=0;
		for (int i=0; i<m_PointSetContainer.size(); i++) {
			GraphPointSet gps = ((GraphPointSet) (this.m_PointSetContainer.get(i)));
			if (gps.getPointCount()>0) {
				gps.setColorByIndex(index);
				index++;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean checkLoggable() {
		double minY = Double.MAX_VALUE;
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			DPointSet pSet = (m_PointSetContainer.get(i).getConnectedPointSet());
			if (pSet.getSize() > 0)
				minY = Math.min(minY, pSet.getMinYVal());
		}
		// if (TRACE) System.out.println("min is " + minY);
		return (minY > 0);
	}

	protected double getMinimalPositiveYValue() {
		double minY = Double.MAX_VALUE;
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			DPointSet pSet = (m_PointSetContainer.get(i).getConnectedPointSet());
			if (pSet.getSize() > 0) {
				double tmpMinY = Math.min(minY, pSet.getMinPositiveYValue());
				if (tmpMinY>0) minY=tmpMinY;
			}
		}
		return minY;
	}

	protected boolean checkLogValidYValue(double x, double y, int graphLabel) {
		if (y <= 0.0) {
			if (m_log && notifyNegLog) {
				System.err.println("Warning: trying to plot value (" + x + "/"
						+ y + ") with y <= 0 in logarithmic mode!");
				notifyNegLog = false;
			}
			return false;
		}
		return true;
	}

	private String cleanBlanks(String str, Character rpl) {
		return str.replace(' ', rpl);
	}

	/**
	  *
	  */
	public void clearAll() {
		this.removeAllDElements();
		for (int i = 0; i < m_PointSetContainer.size(); i++)
			((GraphPointSet) (m_PointSetContainer.get(i))).removeAllPoints();
		m_PointSetContainer.clear();
		if (getYScale() instanceof Exp) setYScale(new Exp()); // to remove smallest seen value
		notifyNegLog = true;
	}

	/**
	  *
	  */
	public void clearGraph(int graphLabel) {
		getGraphPointSet(graphLabel).removeAllPoints();
		m_PointSetContainer.remove(getGraphPointSet(graphLabel));
		if (getYScale() instanceof Exp) {
			((Exp)getYScale()).setMinValue(getMinimalPositiveYValue());
		}
		repaint();
		notifyNegLog = true;
	}

	/**
	  *
	  */
	public void clearGraph(int x, int y) {
		int index = getNearestGraphIndex(x, y);
		if (index == -1)
			return;
		int GraphLabel = ((GraphPointSet) (this.m_PointSetContainer.get(index)))
				.getGraphLabel();
		clearGraph(GraphLabel);
		updateLegend();
	}

	/**
	 * Reset the legend clearing all information.
	 */
	public void clearLegend() {
		setLegend(null);
	}

	/**
	  *
	  */
	protected void createFileChooser() {
		m_FileChooser = new JFileChooser(new File("/resources"));
		m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	/**
	 * Plot a circle icon to the function area which is annotated with a char
	 * and a double value.
	 * 
	 * @param c
	 * @param val
	 * @param position
	 * @param graphID
	 */
	public void drawCircle(char c, double val, double[] position, int graphID) {
		drawCircle(c + "" + val, position, graphID);
	}

	/**
	 * Plot a circle icon to the function area which is annotated with a char
	 * and a double value.
	 * 
	 * @param c
	 * @param val
	 * @param position
	 */
	public void drawCircle(double val, double[] position, int graphID) {
		drawCircle("" + val, position, graphID);
	}

	public void drawCircle(String label, double xPos, double yPos, int graphID) {
		double[] pos = new double[2];
		pos[0] = xPos;
		pos[1] = yPos;
		drawCircle(label, pos, graphID);
	}

	/**
	 * Plot a circle icon to the function area which is annotated with a char
	 * and a double value. The color corresponds to the color of the graph with
	 * given ID
	 * 
	 * @param label
	 * @param position
	 * @param graphID
	 */
	public void drawCircle(String label, double[] position, int graphID) {
		drawIcon(new Chart2DDPointIconCircle(), label, position, graphID);
	}

	/**
	 * Plot a circle icon to the function area which is annotated with a char
	 * and a double value. The color corresponds to the color of the graph with
	 * given ID
	 * 
	 * @param label
	 * @param position
	 * @param graphID
	 */
	public void drawIcon(DPointIcon theIcon, String label, double[] position,
			int graphID) {
		DPointSet popRep;
		popRep = new DPointSet();
		popRep.addDPoint(new DPoint(position[0], position[1]));
		DPointIcon icon = new Chart2DDPointIconText(label);
		((Chart2DDPointIconText) icon).setIcon(theIcon);
		((Chart2DDPointIconText) icon).setColor(getGraphPointSet(graphID)
				.getColor());
		popRep.setIcon(icon);
		addDElement(popRep);
	}

	// public void setConnectedPoint(double x, double y, int GraphLabel) {
	// if (m_log == true && y <= 0.0) {
	// // y = Double.MIN_VALUE;
	// if (notifyNegLog) {
	// System.err.println("Warning: trying to plot value (" + x + "/" + y +
	// ") with y <= 0 in logarithmic mode! Setting y to " + 1e-30);
	// notifyNegLog = false;
	// }
	// y = 1e-30;
	// }
	// getGraphPointSet(GraphLabel).addDPoint(x, y);
	//
	// }

	/**
	 * Plot an icon to the function area which is annotated with a char and a
	 * double value. The color corresponds to the color of the graph with given
	 * ID Icon types are 0: circle, 1: cross, otherwise: point.
	 * 
	 * @param label
	 * @param position
	 * @param graphID
	 */
	public void drawIcon(int iconType, String label, double[] position,
			int graphID) {
		DPointIcon theIcon;
		switch (iconType) {
		case 0:
			theIcon = new Chart2DDPointIconCircle();
			break;
		case 1:
			theIcon = new Chart2DDPointIconCross();
			break;
		default:
		case 2:
			theIcon = new Chart2DDPointIconPoint();
			break;
		}
		drawIcon(theIcon, label, position, graphID);
	}

	/**
	 * Draw a line with given start and end points.
	 * 
	 * @param p1
	 * @param p2
	 */
	public void drawLine(double[] p1, double[] p2) {
		DPointSet popRep = new DPointSet();
		popRep.setConnected(true);
		popRep.addDPoint(new DPoint(p1[0], p1[1]));
		popRep.addDPoint(new DPoint(p2[0], p2[1]));
		addDElement(popRep);
	}

	/**
	 * Export contained data to standard output.
	 * 
	 */
	public void exportToAscii() {
		exportToAscii((File) null);
	}

	/**
	 * Export contained data to a file or to standard out if null is given. The
	 * given File will be overwritten!
	 * 
	 * @param file
	 *            a File instance or null to export to standard out
	 * @return true if the export succeeded, else false
	 */
	public boolean exportToAscii(File file) {
		String[] s = null;
		int maxSize = 0;
		DPointSet maxSet = null;
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			// find maximum length of all point sets
			if (m_PointSetContainer.get(i).getConnectedPointSet().getSize() > maxSize) {
				maxSet = m_PointSetContainer.get(i).getConnectedPointSet();
				maxSize = maxSet.getSize();
			}
		}
		if (maxSize > 0) { // if there is any data, init string array and set x
			// value column
			s = new String[maxSize + 1];
			s[0] = "calls";
			for (int j = 1; j <= maxSize; j++)
				s[j] = "" + maxSet.getDPoint(j - 1).x;
		} else {
			System.err.println("Error: no data to export");
			return true;
		}
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			if (m_PointSetContainer.get(i) instanceof GraphPointSet) {
				GraphPointSet set = (GraphPointSet) m_PointSetContainer.get(i);
				DPointSet pset = set.getConnectedPointSet();
				s[0] = s[0] + " " + cleanBlanks(set.getInfoString(), '_'); // add
				// column
				// name
				for (int j = 1; j < s.length; j++) { // add column data of place
					// holder if no value in
					// this set
					if ((j - 1) < pset.getSize())
						s[j] = s[j] + " " + pset.getDPoint(j - 1).y;
					else
						s[j] += " #";
				}
			} else
				System.err.println("error in FunctionArea::exportToAscii");
		}
		if (file == null) {
			for (int j = 0; j < s.length; j++) {
				System.out.println(s[j]);
			}
			return true;
		} else
			try {
				PrintWriter out = new PrintWriter(new FileOutputStream(file));
				for (int j = 0; j < s.length; j++)
					out.println(s[j]);
				out.flush();
				out.close();
				return true;
			} catch (Exception e) {
				System.err.println("Error on data export:" + e.getMessage());
				return false;
			}
	}

	/**
	 * Export contained data to a file with a given String as prefix
	 * 
	 * @param prefix
	 *            file name prefix
	 * @return true if the export succeeded, else false
	 */
	public boolean exportToAscii(String prefix) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"E'_'yyyy.MM.dd'_'HH.mm.ss");
		String fname = prefix + "PlotExport_" + formatter.format(new Date())
				+ ".txt";
		try {
			File f = new File(fname);
			f.createNewFile();
			return exportToAscii(f);
		} catch (Exception e) {
			System.err.println("Error:" + e.getMessage());
			return false;
		}

	}

	/**
	 * This gives the number of graphs already plotted.
	 * 
	 * @return
	 */
	public int getContainerSize() {
		return m_PointSetContainer.size();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getGraphInfo(int x, int y) {
		String ret = "";
		if ((m_PointSetContainer == null) || (m_PointSetContainer.size() == 0))
			return ret;
		int minindex = getNearestGraphIndex(x, y);
		if (minindex >= 0)
			return ((GraphPointSet) (m_PointSetContainer.get(minindex)))
					.getInfoString();
		else
			return "none";
	}

	public String getGraphInfo(int graphIndex) {
		String ret = "";
		if ((m_PointSetContainer == null) || (m_PointSetContainer.size() == 0))
			return ret;

		if (graphIndex >= 0 && (graphIndex <m_PointSetContainer.size()))
			return ((GraphPointSet) (m_PointSetContainer.get(graphIndex)))
					.getInfoString();
		else
			return "none";
	}
	
	/**
	 * 
	 * @param GraphLabel
	 * @return
	 */
	private GraphPointSet getGraphPointSet(int GraphLabel) {
		// System.out.println("looping through " + m_PointSetContainer.size() +
		// " point sets...");
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			if (m_PointSetContainer.get(i) instanceof GraphPointSet) {
				GraphPointSet xx = (GraphPointSet) (m_PointSetContainer.get(i));
				// System.out.println("looking at "+xx.getGraphLabel());
				if (xx.getGraphLabel() == GraphLabel) {
					// System.out.println("match!");
					return xx;
				}
			}
		}
		// create new GraphPointSet
		GraphPointSet NewPointSet = new GraphPointSet(GraphLabel, this);
		// System.out.println("adding new point set " + GraphLabel);
		// NewPointSet.setStroke(new BasicStroke( (float)1.0 ));
		// addGraphPointSet(NewPointSet); already done within GraphPointSet!!!
		return NewPointSet;
	}

	/**
	  *
	  */
	private DPoint getNearestDPoint(int x, int y) {
		// get index of nearest Graph
		double distmin = 10000000;
		DPoint ret = null;
		DPoint point1 = getDMeasures().getDPoint(x, y);
		DPoint point2 = null;
		double dist = 0;
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			if (m_PointSetContainer.get(i) instanceof GraphPointSet) {
				GraphPointSet pointset = (GraphPointSet) (m_PointSetContainer
						.get(i));
				point2 = pointset.getNearestDPoint(point1);
				if (point2 == null)
					continue;
				dist = (point1.x - point2.x) * (point1.x - point2.x)
						+ (point1.y - point2.y) * (point1.y - point2.y);
				// System.out.println("dist="+dist+"i="+i);
				if (dist < distmin) {
					distmin = dist;
					ret = point2;
				}
				if ((dist == distmin)
						&& !(ret.getIcon() instanceof Chart2DDPointIconContent)
						&& !(ret.getIcon() instanceof InterfaceSelectablePointIcon)) {
					distmin = dist;
					ret = point2;
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getNearestGraphIndex(int x, int y) {
		// get index of nearest Graph
		double distmin = 10000000;
		int minindex = -1;
		DPoint point1 = getDMeasures().getDPoint(x, y);
		DPoint point2 = null;
		double dist = 0;
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			GraphPointSet pointset = m_PointSetContainer.get(i);
			point2 = pointset.getNearestDPoint(point1);
			if (point2 == null)
				continue;
			if (point1 == null)
				System.err.println("point1 == null");

			dist = (point1.x - point2.x) * (point1.x - point2.x)
					+ (point1.y - point2.y) * (point1.y - point2.y);
			// System.out.println("dist="+dist+"i="+i);
			if (dist < distmin) {
				distmin = dist;
				minindex = i;
			}
		}
		return minindex;
	}

	/**
	 * Returns the number of points within the graph of the given label.
	 * 
	 * @param index
	 * @return
	 */
	public int getPointCount(int label) {
		return getGraphPointSet(label).getPointCount();
	}

	/**
	 * This method returns the selection listener to the PointIcon
	 * 
	 * @return InterfaceSelectionListener
	 */
	public InterfaceRefPointListener getRefPointSelectionListener() {
		return this.m_RefPointListener;
	}

	/**
	  *
	  */
	public boolean isStatisticsGraph(int x, int y) {
		boolean ret = false;
		if ((m_PointSetContainer == null) || (m_PointSetContainer.size() == 0))
			return ret;
		int minindex = getNearestGraphIndex(x, y);
		ret = ((GraphPointSet) (m_PointSetContainer.get(minindex)))
				.isStatisticsGraph();
		return ret;
	}

	/**
	 * Causes all PointSets to interupt the connected painting at the current
	 * position.
	 */
	public void jump() {
		for (int i = 0; i < m_PointSetContainer.size(); i++)
			((GraphPointSet) (m_PointSetContainer.get(i))).jump();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (legendBox != null && m_legend)
			legendBox.paintIn(g, m_Border.getInnerRect(this));
	}

	/**
	 * 
	 * @return
	 */
	public DPointSet[] printPoints() {
		DPointSet[] ret = new DPointSet[m_PointSetContainer.size()];
		for (int i = 0; i < m_PointSetContainer.size(); i++) {
			System.out.println("");
			System.out.println("GraphPointSet No " + i);

			ret[i] = ((GraphPointSet) m_PointSetContainer.get(i)).printPoints();
		}
		return ret;

	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public DPointSet printPoints(int i) {
		// for (int i = 0; i < m_PointSetContainer.size();i++) {
		System.out.println("");
		System.out.println("GraphPointSet No " + i);

		return ((GraphPointSet) m_PointSetContainer.get(i)).printPoints();
		// }
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void removePoint(int x, int y) {
		DPoint point = getNearestDPoint(x, y);
		int index = getNearestGraphIndex(x, y);
		if (index == -1 || point == null)
			return;
		GraphPointSet pointset = (GraphPointSet) (this.m_PointSetContainer
				.get(index));
		pointset.removePoint(point);
	}

	/**
	 * This method allows to remove the selection listener to the PointIcon
	 */
	public void removeRefPointSelectionListeners() {
		this.m_RefPointListener = null;
	}

	public void setConnectedPoint(double x, double y, int graphLabel) {
		DFunction scF = getYScale();
		if (scF instanceof Exp) ((Exp)scF).updateMinValue(y);
		if (!checkLogValidYValue(x, y, graphLabel)) {
			//			 if (m_log) toggleLog();
		}
		getGraphPointSet(graphLabel).addDPoint(x, y);
	}

	/**
	 * 
	 * @param p
	 * @param graphLabel
	 */
	public void setConnectedPoint(double[] p, int graphLabel) {
		setConnectedPoint(p[0], p[1], graphLabel);
	}

	/**
	 * 
	 * @param GraphLabel
	 * @param color
	 */
	public void setGraphColor(int GraphLabel, Color color) {
		this.getGraphPointSet(GraphLabel).setColor(color);
	}

	// /**
	// */
	// public Object openObject() {
	// if (m_FileChooser == null)
	// createFileChooser();
	// int returnVal = m_FileChooser.showOpenDialog(this);
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File selected = m_FileChooser.getSelectedFile();
	// try {
	// ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new
	// FileInputStream(selected)));
	// Object obj = oi.readObject();
	// oi.close();
	//
	// Object[] objects = (Object[]) obj;
	// for (int i = 0; i < objects.length; i++) {
	// GraphPointSet xx = ((GraphPointSet.SerPointSet)
	// objects[i]).getGraphPointSet();
	// xx.initGraph(this);
	// addGraphPointSet(xx);
	// }
	// repaint();
	// return obj;
	// } catch (Exception ex) {
	// JOptionPane.showMessageDialog(this,
	// "Couldn't read object: "
	// + selected.getName()
	// + "\n" + ex.getMessage(),
	// "Open object file",
	// JOptionPane.ERROR_MESSAGE);
	// }
	// }
	// return null;
	// }

	// /**
	// *
	// */
	// public void saveObject() {
	// Object[] object = new Object[m_PointSetContainer.size()];
	// for (int i = 0; i < m_PointSetContainer.size(); i++) {
	// object[i] = ((GraphPointSet)
	// m_PointSetContainer.get(i)).getSerPointSet();
	// }
	// if (m_FileChooser == null)
	// createFileChooser();
	// int returnVal = m_FileChooser.showSaveDialog(this);
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File sFile = m_FileChooser.getSelectedFile();
	// try {
	// ObjectOutputStream oo = new ObjectOutputStream(new
	// BufferedOutputStream(new FileOutputStream(sFile)));
	// oo.writeObject(object);
	// oo.close();
	// } catch (IOException ex) {
	// JOptionPane.showMessageDialog(this,
	// "Couldn't write to file: "
	// + sFile.getName()
	// + "\n" + ex.getMessage(),
	// "Save object",
	// JOptionPane.ERROR_MESSAGE);
	// }
	// }
	// }

	/**
	 * 
	 * @param GraphLabel
	 * @param colorindex
	 */
	public void setGraphColor(int GraphLabel, int colorindex) {
		this.getGraphPointSet(GraphLabel).setColorByIndex(colorindex);
	}

	/**
	 * 
	 * @param GraphLabel
	 * @param Info
	 * @param stroke
	 */
	public void setInfoString(int GraphLabel, String Info, float stroke) {
		getGraphPointSet(GraphLabel).setInfoString(Info, stroke);
	}

	/**
	 * Set a legend for the function area. If null is given, the former legend
	 * is deactivated.
	 * 
	 * @param lBox
	 */
	protected void setLegend(GraphPointSetLegend lBox) {
		legendBox = lBox;
		if (lBox != null && m_legend)
			repaint();
	}


	/**
	 * 
	 * @param x
	 * @param y
	 * @param GraphLabel
	 */
	public void setUnconnectedPoint(double x, double y, int GraphLabel) {
		DFunction scF = getYScale();
		if (scF instanceof Exp) ((Exp)scF).updateMinValue(y);
		if (!checkLogValidYValue(x, y, GraphLabel)) {
			//			 if (m_log) toggleLog();
		}
		this.getGraphPointSet(GraphLabel).addDPoint(x, y);
		this.getGraphPointSet(GraphLabel).setConnectedMode(false);
		repaint();
	}

	/**
	 * 
	 * @param p
	 * @param GraphLabel
	 */
	public void setUnconnectedPoint(double[] p, int GraphLabel) {
		setUnconnectedPoint(p[0], p[1], GraphLabel);
	}

	/**
	 * 
	 */
	private void toggleLegend() {
		m_legend = !m_legend;
		repaint();
	}

	public boolean isShowLegend() {
		return m_legend;
	}
	
	/**
	 * Shows the legend or switches it off.
	 * 
	 * @param on
	 */
	public void setShowLegend(boolean on) {
		m_legend = on;
		if (!on)
			legendBox = null;
		else
			legendBox = new GraphPointSetLegend(m_PointSetContainer, true);
		repaint();
	}

	/**
	 * 
	 */
	public void toggleLog() {
		// System.out.println("ToggleLog log was: "+m_log);
		boolean setMinPos=false;
		if (!m_log && !checkLoggable()) {
			 System.err.println("Warning: toggling logarithmics scale with values <= 0! Some points will not be displayed.");
			 setMinPos=true;
		}
		if (m_log == false) {
			 setMinRectangle(0.001, 0.001, 1, 1);
			 //setVisibleRectangle(  0.001, 0.001, 100000, 1000 );
			 Exp exp = new Exp();
			 if (setMinPos) exp.setMinValue(getMinimalPositiveYValue());
			 setYScale(exp);
			 m_Border.setSrcdY(Math.log(10));
			 m_Border.applyPattern(false, "0.###E0");
			 m_log = true;
		} else {
			m_log = false;
			setYScale(null);
			ScaledBorder buffer = m_Border;
			m_Border = new ScaledBorder();
			m_Border.x_label = buffer.x_label; // "App. " + Name +
			// " func. calls";
			m_Border.y_label = buffer.y_label; // "fitness";
			setBorder(m_Border);
		}
		repaint();
	}

	/**
	 * Allows setting whether or not to paint the y-axis in logarithmic scale.
	 * 
	 * @param log
	 *            if true logarithmic scale is used, linear scale in case of
	 *            false.
	 */
	public void toggleLog(boolean log) {
		if (log != m_log)
			toggleLog();
	}

	/**
	 * Recreate the legend object with the current point sets.
	 * 
	 */
	public void updateLegend() {
		GraphPointSetLegend lb = new GraphPointSetLegend(m_PointSetContainer, true);
		setLegend(lb);
	}

	/**
	 * Gives the info string for a graph label.
	 * 
	 * @param j
	 *            The graph label identifier.
	 * @return The associated info string.
	 */
	public String getInfoString(int j) {
		return getGraphPointSet(j).getInfoString();
	}

	public void setShowGraphToolTips(boolean doShowGraphToolTips) {
		this.doShowGraphToolTips = doShowGraphToolTips;
	}

	public boolean isShowGraphToolTips() {
		return doShowGraphToolTips;
	}
}
