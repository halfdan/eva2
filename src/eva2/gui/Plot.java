package eva2.gui;

/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of
 * Tuebingen, Computer Architecture @author Holger Ulmer, Felix Streichert,
 * Hannes Planatscher @version: $Revision: 322 $ $Date: 2007-12-11 17:24:07
 * +0100 (Tue, 11 Dec 2007) $ $Author: mkron $
 */
import eva2.EvAInfo;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.populations.Population;
import eva2.tools.BasicResourceLoader;
import eva2.tools.chart2d.DPointSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * ToDo: Rename to PlotWindow
 */
public class Plot implements PlotInterface, Serializable {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -9027101244918249825L;
    private JFileChooser m_FileChooser;
    private JPanel m_ButtonPanel;
    private String plotName;
    private String xAxisText;
    private String yAxisText;
    protected FunctionArea m_PlotArea;
    protected JInternalFrame m_Frame;

    /**
     * You might want to try to assign the x-range as x and y-range as y array
     * parameters.
     */
    public Plot(String plotName, String xname, String yname, double[] x, double[] y) {
        this(plotName, xname, yname, true);
        DPointSet points = new DPointSet();
        for (int i = 0; i < x.length; i++) {
            points.addDPoint(x[i], y[i]);
        }
        m_PlotArea.addDElement(points);
    }

    /**
     * A basic constructor.
     *
     * @param PlotName
     * @param xname
     * @param yname
     * @param init
     */
    public Plot(String PlotName, String xname, String yname) {
        this(PlotName, xname, yname, true);
    }

    /**
     * A basic constructor.
     *
     * @param plotName
     * @param xname
     * @param yname
     * @param init
     */
    public Plot(String plotName, String xname, String yname, boolean init) {
        xAxisText = xname;
        yAxisText = yname;
        this.plotName = plotName;
        if (init) {
            this.init();
        }
    }

    protected void installButtons(JPanel buttonPan) {
        JButton ClearButton = new JButton("Clear");
        ClearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        JButton LOGButton = new JButton("Log/Lin");
        LOGButton.setToolTipText("Toggle between a linear and a log scale on the y-axis.");
        LOGButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_PlotArea.toggleLog();
            }
        });
        JButton ExportButton = new JButton("Export to TSV");
        ExportButton.setToolTipText("Exports the graph data to a simple TSV file.");
        ExportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                exportPlot();
            }
        });
        JButton DumpButton = new JButton("Dump");
        DumpButton.setToolTipText("Dump the graph data to standard output");
        DumpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_PlotArea.exportToAscii();
            }
        });

        JButton saveImageButton = new JButton("Save as PNG...");
        saveImageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Robot robot = new Robot();
                    Rectangle area;
                    area = m_Frame.getBounds();
                    BufferedImage bufferedImage = robot.createScreenCapture(area);
                    JFileChooser fc = new JFileChooser();
                    if (fc.showSaveDialog(m_Frame) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    // System.out.println("Name " + outfile);
                    try {
                        /*
                         * Old version FileOutputStream fos = new
                         * FileOutputStream
                         * (fc.getSelectedFile().getAbsolutePath()+".jpeg");
                         * BufferedOutputStream bos = new
                         * BufferedOutputStream(fos); JPEGImageEncoder encoder =
                         * JPEGCodec.createJPEGEncoder(bos);
                         * encoder.encode(bufferedImage); bos.close();
                         */
                        File file = new File(fc.getSelectedFile().getAbsolutePath()
                                + ".png");
                        ImageIO.write(bufferedImage, "png", file);
                        /*
                         * JPEG version with javax.imageio float compression =
                         * 0.8f; FileImageOutputStream out = new
                         * FileImageOutputStream(new
                         * File(fc.getSelectedFile().getAbsolutePath
                         * ()+".jpeg")); ImageWriter encoder =
                         * (ImageWriter)ImageIO
                         * .getImageWritersByFormatName("JPEG").next();
                         * JPEGImageWriteParam param = new
                         * JPEGImageWriteParam(null);
                         *
                         *
                         * param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT
                         * ); param.setCompressionQuality(compression);
                         *
                         * encoder.setOutput(out); encoder.write((IIOMetadata)
                         * null, new IIOImage(bufferedImage,null,null), param);
                         *
                         * out.close();
                         */

                    } catch (Exception eee) {
                        System.err.println("Error on exporting PNG: "
                                + eee.getMessage());
                    }
                } catch (AWTException ee) {
                    System.err.println("Error on creating PNG: "
                            + ee.getMessage());
                    ee.printStackTrace();
                }
            }
        });

        buttonPan.add(ClearButton);
        buttonPan.add(LOGButton);
        buttonPan.add(DumpButton);
        buttonPan.add(ExportButton);
        // m_ButtonPanel.add(PrintButton);
        // m_ButtonPanel.add(OpenButton);
        // m_ButtonPanel.add(SaveButton);
        buttonPan.add(saveImageButton);
    }

    /**
     *
     */
    @Override
    public void init() {
        m_Frame = new JEFrame("Plot: " + plotName);
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
//			m_Frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));

        m_ButtonPanel = new JPanel();
        m_PlotArea = new FunctionArea(xAxisText, yAxisText);
        m_ButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        installButtons(m_ButtonPanel);

        // getContentPane().smultetLayout( new GridLayout(1, 4) );
        m_Frame.add(m_ButtonPanel, BorderLayout.PAGE_END);
        m_Frame.add(m_PlotArea, BorderLayout.CENTER); // north was not so
        // nice
        m_Frame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                super.internalFrameClosing(e);
                m_PlotArea.clearAll(); // this was a memory leak
                m_PlotArea = null;
                m_Frame.dispose();
            }
        });
        m_Frame.pack();
        m_Frame.setVisible(true);
    }

    /**
     * Indicate whether graph legend entries should show their unique number.
     */
    public void setAppendIndexInLegend(boolean appendIndexInLegend) {
        this.m_PlotArea.setAppendIndexInLegend(appendIndexInLegend);
    }

    /**
     * Indicates whether graph legend entries show their unique number.
     */
    public boolean isAppendIndexInLegend() {
        return m_PlotArea.isAppendIndexInLegend();
    }

    /**
     * Indicate whether the graphs are annotated by tool tip info strings.
     *
     * @return true if the graphs are annotated by tool tip info strings
     */
    public boolean isShowGraphToolTips() {
        return m_PlotArea.isShowGraphToolTips();
    }

    /**
     * Toggle whether the graphs should be annotated by tool tip info strings.
     *
     * @param doShowGraphToolTips true if the graphs should be annotated by tool
     * tip info strings
     */
    public void setShowGraphToolTips(boolean doShowGraphToolTips) {
        m_PlotArea.setShowGraphToolTips(doShowGraphToolTips);
    }

    /**
     * Draw a population to the Plot instance. Each individual is annotated with
     * the given prefix and its fitness.
     *
     * @param prefix
     * @param pop
     */
    public void drawPopulation(String prefix, Population pop) {
        for (int i = 0; i < pop.size(); i++) {
            drawIndividual(0, 2, prefix, pop.getEAIndividual(i));
        }
    }

    /**
     * Draw an individual to the Plot instance. It is annotated with the given
     * prefix and its fitness with short scientific notation.
     *
     * @param prefix
     * @param pop
     * @see FunctionArea.drawIcon
     */
    public void drawIndividual(int iconType, int graphID, String prefix, AbstractEAIndividual indy) {
        StringBuffer sb = new StringBuffer();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%s %.3e", prefix, indy.getFitness(0));

        getFunctionArea().drawIcon(iconType, sb.toString(), indy.getDoublePosition(), graphID);
    }

    public void setPreferredSize(Dimension prefSize) {
        if (m_Frame != null) {
            m_Frame.setPreferredSize(prefSize);
            m_Frame.pack();
        }
    }

    /**
     * Return true if the Plot object is valid.
     *
     * @return true if the Plot object is valid
     */
    @Override
    public boolean isValid() {
        return (m_Frame != null) && (m_PlotArea != null);
    }

    /**
     *
     */
    @Override
    public void setConnectedPoint(double x, double y, int func) {
        m_PlotArea.setConnectedPoint(x, y, func);
    }

    @Override
    public int getPointCount(int graphLabel) {
        return m_PlotArea.getPointCount(graphLabel);
    }

    /**
     *
     */
    @Override
    public void addGraph(int g1, int g2, boolean forceAdd) {
        m_PlotArea.addGraph(g1, g2, forceAdd);
    }

    /**
     *
     */
    @Override
    public void setUnconnectedPoint(double x, double y, int GraphLabel) {
        m_PlotArea.setUnconnectedPoint(x, y, GraphLabel);
    }

    /**
     *
     */
    @Override
    public void clearAll() {
        m_PlotArea.clearAll();
        m_PlotArea.removeAllDElements();
        m_PlotArea.clearLegend();
        m_Frame.repaint();
    }

    /**
     *
     */
    @Override
    public void clearGraph(int GraphNumber) {
        m_PlotArea.clearGraph(GraphNumber);
    }

    /**
     *
     */
    @Override
    public void setInfoString(int GraphLabel, String Info, float stroke) {
        m_PlotArea.setInfoString(GraphLabel, Info, stroke);
    }

    /**
     *
     */
    @Override
    public void jump() {
        m_PlotArea.jump();
    }

    /**
     */
    protected Object openObject() {
        if (m_FileChooser == null) {
            createFileChooser();
        }
        int returnVal = m_FileChooser.showOpenDialog(m_Frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = m_FileChooser.getSelectedFile();
            try {
                ObjectInputStream oi = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(selected)));
                Object obj = oi.readObject();
                oi.close();
                Class<?> ClassType = Class.forName("FunctionArea");
                if (!ClassType.isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: "
                            + ClassType.getName());
                }
                return obj;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(m_Frame, "Couldn't read object: "
                        + selected.getName() + "\n" + ex.getMessage(),
                        "Open object file", JOptionPane.ERROR_MESSAGE);
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
        if (m_FileChooser == null) {
            createFileChooser();
        }
        int returnVal = m_FileChooser.showSaveDialog(m_Frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File sFile = m_FileChooser.getSelectedFile();
            if (sFile.exists()) {
                returnVal = JOptionPane.showConfirmDialog(m_Frame, "The file "
                        + sFile.getName() + " already exists. Overwrite?");
                if (returnVal != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            if (!(m_PlotArea.exportToAscii(sFile))) {
                JOptionPane.showMessageDialog(m_Frame,
                        "Couldn't write to file: " + sFile.getName(),
                        "Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     *
     */
    protected void saveObject(Object object) {
        if (m_FileChooser == null) {
            createFileChooser();
        }
        int returnVal = m_FileChooser.showSaveDialog(m_Frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File sFile = m_FileChooser.getSelectedFile();
            try {
                ObjectOutputStream oo = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(sFile)));
                oo.writeObject(object);
                oo.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(m_Frame,
                        "Couldn't write to file: " + sFile.getName() + "\n"
                        + ex.getMessage(), "Save object",
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
    @Override
    public String getName() {
        return this.plotName;
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
        m_Frame = null;
    }

    /**
     * Add the corners of the given range as unconnected points.
     *
     * @param range
     * @param graphLabel
     */
    public void setCornerPoints(double[][] range, int graphLabel) {
        setUnconnectedPoint(range[0][0], range[1][0], graphLabel);
        setUnconnectedPoint(range[0][1], range[1][1], graphLabel);
    }

    public void setColorByIndex(int graphLabel, int index) {
        getFunctionArea().setColorByIndex(graphLabel, index);
    }

    public void recolorAllGraphsByIndex() {
        getFunctionArea().recolorAllGraphsByIndex();
    }
    // /**
    // * Just for testing the Plot class.
    // */
    // public static void main( String[] args ){
    // Plot plot = new Plot("Plot-Test","x-value","y-value");
    // plot.init();
    // double x;
    // for (x= 0; x <6000; x++) {
    // //double y = SpecialFunction.getnormcdf(x);
    // // double yy = 0.5*SpecialFunction.getnormpdf(x);
    // double n = Math.sin(((double)x/1000*Math.PI));
    // //plot.setConnectedPoint(x,Math.sin(x),0);
    // //plot.setConnectedPoint(x,Math.cos(x),1);
    // //plot.setConnectedPoint(x,y,0);
    // plot.setConnectedPoint(x,n,1);
    // }
    // //plot.addGraph(1,2);
    // }
}
