package eva2.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.AWTException;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import eva2.client.EvAClient;
import wsi.ra.chart2d.DPointSet;
import wsi.ra.tool.BasicResourceLoader;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class Plot implements PlotInterface, Serializable {

	public static boolean TRACE = false;
	private JFileChooser m_FileChooser;
	private JPanel m_ButtonPanel;
	private String m_PlotName;
	private String m_xname;
	private String m_yname;
	protected FunctionArea m_PlotArea;
	protected JFrame m_Frame;

	/**
	 * You might want to try to assign the x-range as x and y-range as y array parameters.
	 */
	public Plot(String PlotName,String xname,String yname,double[] x,double[] y) {
		if (TRACE) System.out.println("Constructor Plot "+PlotName);
		m_xname = xname;
		m_yname = yname;
		m_PlotName = PlotName;
		init();
		DPointSet points = new DPointSet();
		for (int i=0;i<x.length;i++) {
			points.addDPoint(x[i],y[i]);
		}
		m_PlotArea.addDElement(points);
	}
	/**
	 *
	 */
	public Plot(String PlotName,String xname,String yname, boolean init) {
		if (TRACE) System.out.println("Constructor Plot "+PlotName);
		m_xname = xname;
		m_yname = yname;
		m_PlotName = PlotName;
		if (init)
			init();
	}
	/**
	 *
	 */
	public Plot(String PlotName,String xname,String yname) {
		if (TRACE) System.out.println("Constructor Plot "+PlotName);
		m_xname = xname;
		m_yname = yname;
		m_PlotName = PlotName;
		init();
	}
	/**
	 *
	 */
	public void init() {
		m_Frame = new JEFrame("Plot: "+m_PlotName);
		BasicResourceLoader  loader  = BasicResourceLoader.instance();
		byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
		try {
			m_Frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Could not find EvA2 icon, please move rescoure folder to working directory!");
		}

		m_ButtonPanel = new JPanel();
		m_PlotArea = new FunctionArea(m_xname,m_yname);
		m_ButtonPanel.setLayout( new FlowLayout(FlowLayout.LEFT, 10,10));
		JButton ClearButton = new JButton ("Clear");
		ClearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearAll();
			}
		});
		JButton LOGButton = new JButton ("Log/Lin");
		LOGButton.setToolTipText("Toggle between a linear and a log scale on the y-axis.");
		LOGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_PlotArea.toggleLog();
			}
		});
		JButton ExportButton = new JButton ("Export...");
		ExportButton.setToolTipText("Exports the graph data to a simple ascii file.");
		ExportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportPlot();
			}
		});
		JButton DumpButton = new JButton ("Dump");
		DumpButton.setToolTipText("Dump the graph data to standard output");
		DumpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_PlotArea.exportToAscii();
			}
		});
		
//		JButton PrintButton = new JButton ("Print");
//		PrintButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					Robot robot = new Robot();
//					// Capture a particular area on the screen
//					int x = 100;
//					int y = 100;
//					int width = 200;
//					int height = 200;
//					Rectangle area = new Rectangle(x, y, width, height);
//					BufferedImage bufferedImage = robot.createScreenCapture(area);
//
//					// Capture the whole screen
//					area = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//					bufferedImage = robot.createScreenCapture(area);
//					try {
//						FileOutputStream fos = new FileOutputStream("test.jpeg");
//						BufferedOutputStream bos = new BufferedOutputStream(fos);
//						JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
//						encoder.encode(bufferedImage);
//						bos.close();
//					} catch (Exception eee) {}
//
//
//				} catch (AWTException ee) {
//					ee.printStackTrace();
//				}
//
//
//
//				PrinterJob job = PrinterJob.getPrinterJob();
////				PageFormat format = job.defaultPage();
////				job.setPrintable(m_PlotArea, format);
////				if (job.printDialog()) {
////				// If not cancelled, start printing!  This will call the print()
////				// method defined by the Printable interface.
////				try { job.print(); }
////				catch (PrinterException ee) {
////				System.out.println(ee);
////				ee.printStackTrace();
////				}
////				}
//
//				///////////////////////////////////////////////
//				//PagePrinter pp = new PagePrinter(m_PlotArea,m_PlotArea.getGraphics(),job.defaultPage());
//				//pp.print();
//				//  public int print( Graphics g, PageFormat pf, int pi ){
////				m_PlotArea.print(m_PlotArea.getGraphics(), new PageFormat(),0);
//				// Obtain a java.awt.print.PrinterJob  (not java.awt.PrintJob)
//				//PrinterJob job = PrinterJob.getPrinterJob();
//				// Tell the PrinterJob to print us (since we implement Printable)
//				// using the default page layout
//				PageFormat page = job.defaultPage();
//
//				job.setPrintable(m_PlotArea, page);
//				// Display the print dialog that allows the user to set options.
//				// The method returns false if the user cancelled the print request
//				if (job.printDialog()) {
//					// If not cancelled, start printing!  This will call the print()
//					// method defined by the Printable interface.
//					try { job.print(); }
//					catch (PrinterException ee) {
//						System.out.println(ee);
//						ee.printStackTrace();
//					}
//				}
//			}
//		});
		
		// MK: Im not sure whether save/open ever worked...
//		JButton OpenButton = new JButton ("Open..");
//		OpenButton.setToolTipText("Load an old plot");
//		OpenButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_PlotArea.openObject();
//			}
//		});
//		JButton SaveButton = new JButton ("Save..");
//		SaveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_PlotArea.saveObject();
//			}
//		});
		JButton SaveJPGButton = new JButton ("Save as JPG...");
		SaveJPGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String outfile ="";
				try {
					Robot       robot = new Robot();
					Rectangle   area;
					area        = m_Frame.getBounds();
					BufferedImage   bufferedImage   = robot.createScreenCapture(area);
					JFileChooser    fc              = new JFileChooser();
					if (fc.showSaveDialog(m_Frame) != JFileChooser.APPROVE_OPTION) return;
//					System.out.println("Name " + outfile);
					try {
						FileOutputStream fos = new FileOutputStream(fc.getSelectedFile().getAbsolutePath()+".jpeg");
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
						encoder.encode(bufferedImage);
						bos.close();
					} catch (Exception eee) {
						System.err.println("Error on exporting JPEG: " + eee.getMessage());
					}
				} catch (AWTException ee) {
					System.err.println("Error on creating JPEG: " + ee.getMessage());
					ee.printStackTrace();
				}
			}
		});

		m_ButtonPanel.add(ClearButton);
		m_ButtonPanel.add(LOGButton);
		m_ButtonPanel.add(DumpButton);
		m_ButtonPanel.add(ExportButton);
//		m_ButtonPanel.add(PrintButton);
//		m_ButtonPanel.add(OpenButton);
//		m_ButtonPanel.add(SaveButton);
		m_ButtonPanel.add(SaveJPGButton);
		//  getContentPane().smultetLayout( new GridLayout(1, 4) );
		m_Frame.getContentPane().add(m_ButtonPanel,"South");
		m_Frame.getContentPane().add(m_PlotArea,"North");
		m_Frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				m_PlotArea.clearAll(); // this was a memory leak
				m_PlotArea = null;
				m_Frame.dispose();
			}
		});
		m_Frame.pack();
		m_Frame.setVisible(true);
	}

	/**
	 * Return true if the Plot object is valid.
	 * 
	 * @return true if the Plot object is valid
	 */
	public boolean isValid() {
		return (m_Frame != null) && (m_PlotArea != null);
	}

	/**
	 *
	 */
	public void setConnectedPoint (double x,double y,int func) {
		if (TRACE) System.out.println("size before is " + m_PlotArea.getPointCount(func));
		m_PlotArea.setConnectedPoint(x,y,func);
		if (TRACE) {
			System.out.println("added "+x+"/" + y + " to graph "+ func);
			System.out.println("size is now " + m_PlotArea.getPointCount(func));
		}
	}

	public int getPointCount(int graphLabel) {
		return m_PlotArea.getPointCount(graphLabel);
	}
	/**
	 *
	 */
	public void addGraph (int g1,int g2, boolean forceAdd) {
		m_PlotArea.addGraph(g1, g2, forceAdd);
	}
	/**
	 *
	 */
	public void setUnconnectedPoint (double x, double y,int GraphLabel) {
		m_PlotArea.setUnconnectedPoint(x,y,GraphLabel);
	}

	/**
	 *
	 */
	public void clearAll () {
		m_PlotArea.clearAll();
		m_PlotArea.removeAllDElements();
		m_Frame.repaint();
	}
	/**
	 *
	 */
	public void clearGraph (int GraphNumber) {
		m_PlotArea.clearGraph(GraphNumber);
	}
	/**
	 *
	 */
	public void setInfoString (int GraphLabel, String Info, float stroke) {
		m_PlotArea.setInfoString(GraphLabel,Info,stroke);
	}
	/**
	 *
	 */
	public void jump () {
		m_PlotArea.jump();
	}
	/**
	 */
	protected Object openObject() {
		if (m_FileChooser == null)
			createFileChooser();
		int returnVal = m_FileChooser.showOpenDialog(m_Frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selected = m_FileChooser.getSelectedFile();
			try {
				ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
				Object obj = oi.readObject();
				oi.close();
				Class ClassType = Class.forName("FunctionArea");
				if (!ClassType.isAssignableFrom(obj.getClass()))
					throw new Exception("Object not of type: " + ClassType.getName());
				return obj;
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(m_Frame,
						"Couldn't read object: "
						+ selected.getName()
						+ "\n" + ex.getMessage(),
						"Open object file",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	/**
	 * Just dump the plot to stdout.
	 */
	protected void dumpPlot() {
		m_PlotArea.exportToAscii();
	}
	
	/**
	 *
	 */
	protected void exportPlot() {
		if (m_FileChooser == null)
			createFileChooser();
		int returnVal = m_FileChooser.showSaveDialog(m_Frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File sFile = m_FileChooser.getSelectedFile();
			if (sFile.exists()) {
				returnVal = JOptionPane.showConfirmDialog(m_Frame, "The file "+sFile.getName()+" already exists. Overwrite?");
				if (returnVal != JOptionPane.YES_OPTION) return;
			}
			if (!(m_PlotArea.exportToAscii(sFile))) { 
				JOptionPane.showMessageDialog(m_Frame,
						"Couldn't write to file: "
						+ sFile.getName(),
						"Export error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 *
	 */
	protected void saveObject(Object object) {
		if (m_FileChooser == null)
			createFileChooser();
		int returnVal = m_FileChooser.showSaveDialog(m_Frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File sFile = m_FileChooser.getSelectedFile();
			try {
				ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
				oo.writeObject(object);
				oo.close();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(m_Frame,
						"Couldn't write to file: "
						+ sFile.getName()
						+ "\n" + ex.getMessage(),
						"Save object",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 *
	 */
	protected void createFileChooser() {
		m_FileChooser = new JFileChooser(new File("/resources"));
		m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	/**
	 *
	 */
	public String getName() {
		return this.m_PlotName;
	}
	/**
	 *
	 */
	public FunctionArea getFunctionArea() {
		return m_PlotArea;
	}
	/**
	 *
	 */
	public void dispose() {
		m_Frame.dispose();
	}

//	/**
//	 * Just for testing the Plot class.
//	 */
//	public static void main( String[] args ){
//		Plot plot = new Plot("Plot-Test","x-value","y-value");
//		plot.init();
//		double x;
//		for  (x= 0; x <6000; x++) {
//			//double y = SpecialFunction.getnormcdf(x);
//			// double yy = 0.5*SpecialFunction.getnormpdf(x);
//			double n = Math.sin(((double)x/1000*Math.PI));
//			//plot.setConnectedPoint(x,Math.sin(x),0);
//			//plot.setConnectedPoint(x,Math.cos(x),1);
//			//plot.setConnectedPoint(x,y,0);
//			plot.setConnectedPoint(x,n,1);
//		}
//		//plot.addGraph(1,2);
//	}
}

