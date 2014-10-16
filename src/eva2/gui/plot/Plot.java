package eva2.gui.plot;

import eva2.EvAInfo;
import eva2.gui.JEFrame;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.BasicResourceLoader;
import eva2.tools.chart2d.DPointSet;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Formatter;
import java.util.Locale;

/**
 * ToDo: Rename to PlotWindow
 */
public class Plot implements PlotInterface, Serializable {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -9027101244918249825L;
    private JFileChooser fileChooser;
    private JPanel buttonPanel;
    private String plotName;
    private String xAxisText;
    private String yAxisText;
    protected FunctionArea plotArea;
    protected JInternalFrame internalFrame;

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
        plotArea.addDElement(points);
    }

    /**
     * A basic constructor.
     *
     * @param PlotName
     * @param xname
     * @param yname
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
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        JButton loglinButton = new JButton("Log/Lin");
        loglinButton.setToolTipText("Toggle between a linear and a log scale on the y-axis.");
        loglinButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                plotArea.toggleLog();
            }
        });
        JButton exportButton = new JButton("Export to TSV");
        exportButton.setToolTipText("Exports the graph data to a simple TSV file.");
        exportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                exportPlot();
            }
        });
        JButton dumpButton = new JButton("Dump");
        dumpButton.setToolTipText("Dump the graph data to standard output");
        dumpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                plotArea.exportToAscii();
            }
        });

        JButton saveImageButton = new JButton("Save as PNG...");
        saveImageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Robot robot = new Robot();
                    Rectangle area;
                    area = internalFrame.getBounds();
                    BufferedImage bufferedImage = robot.createScreenCapture(area);
                    JFileChooser fc = new JFileChooser();
                    if (fc.showSaveDialog(internalFrame) != JFileChooser.APPROVE_OPTION) {
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

        buttonPan.add(clearButton);
        buttonPan.add(loglinButton);
        buttonPan.add(dumpButton);
        buttonPan.add(exportButton);
        buttonPan.add(saveImageButton);
    }

    /**
     *
     */
    @Override
    public void init() {
        internalFrame = new JEFrame("Plot: " + plotName);
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);

        buttonPanel = new JPanel();
        plotArea = new FunctionArea(xAxisText, yAxisText);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        installButtons(buttonPanel);

        internalFrame.add(buttonPanel, BorderLayout.PAGE_END);
        internalFrame.add(plotArea, BorderLayout.CENTER); // north was not so
        // nice
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                super.internalFrameClosing(e);
                plotArea.clearAll(); // this was a memory leak
                plotArea = null;
                internalFrame.dispose();
            }
        });
        internalFrame.pack();
        internalFrame.setVisible(true);
    }

    /**
     * Indicate whether graph legend entries should show their unique number.
     */
    public void setAppendIndexInLegend(boolean appendIndexInLegend) {
        this.plotArea.setAppendIndexInLegend(appendIndexInLegend);
    }

    /**
     * Indicates whether graph legend entries show their unique number.
     */
    public boolean isAppendIndexInLegend() {
        return plotArea.isAppendIndexInLegend();
    }

    /**
     * Indicate whether the graphs are annotated by tool tip info strings.
     *
     * @return true if the graphs are annotated by tool tip info strings
     */
    public boolean isShowGraphToolTips() {
        return plotArea.isShowGraphToolTips();
    }

    /**
     * Toggle whether the graphs should be annotated by tool tip info strings.
     *
     * @param doShowGraphToolTips true if the graphs should be annotated by tool
     *                            tip info strings
     */
    public void setShowGraphToolTips(boolean doShowGraphToolTips) {
        plotArea.setShowGraphToolTips(doShowGraphToolTips);
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
        if (internalFrame != null) {
            internalFrame.setPreferredSize(prefSize);
            internalFrame.pack();
        }
    }

    /**
     * Return true if the Plot object is valid.
     *
     * @return true if the Plot object is valid
     */
    @Override
    public boolean isValid() {
        return (internalFrame != null) && (plotArea != null);
    }

    /**
     *
     */
    @Override
    public void setConnectedPoint(double x, double y, int func) {
        plotArea.setConnectedPoint(x, y, func);
    }

    @Override
    public int getPointCount(int graphLabel) {
        return plotArea.getPointCount(graphLabel);
    }

    /**
     *
     */
    @Override
    public void addGraph(int g1, int g2, boolean forceAdd) {
        plotArea.addGraph(g1, g2, forceAdd);
    }

    /**
     *
     */
    @Override
    public void setUnconnectedPoint(double x, double y, int GraphLabel) {
        plotArea.setUnconnectedPoint(x, y, GraphLabel);
    }

    /**
     *
     */
    @Override
    public void clearAll() {
        plotArea.clearAll();
        plotArea.removeAllDElements();
        plotArea.clearLegend();
        internalFrame.repaint();
    }

    /**
     *
     */
    @Override
    public void clearGraph(int GraphNumber) {
        plotArea.clearGraph(GraphNumber);
    }

    /**
     *
     */
    @Override
    public void setInfoString(int GraphLabel, String Info, float stroke) {
        plotArea.setInfoString(GraphLabel, Info, stroke);
    }

    /**
     *
     */
    @Override
    public void jump() {
        plotArea.jump();
    }

    /**
     */
    protected Object openObject() {
        if (fileChooser == null) {
            createFileChooser();
        }
        int returnVal = fileChooser.showOpenDialog(internalFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
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
                JOptionPane.showMessageDialog(internalFrame, "Couldn't read object: "
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
        plotArea.exportToAscii();
    }

    /**
     *
     */
    protected void exportPlot() {
        if (fileChooser == null) {
            createFileChooser();
        }
        int returnVal = fileChooser.showSaveDialog(internalFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File sFile = fileChooser.getSelectedFile();
            if (sFile.exists()) {
                returnVal = JOptionPane.showConfirmDialog(internalFrame, "The file "
                        + sFile.getName() + " already exists. Overwrite?");
                if (returnVal != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            if (!(plotArea.exportToAscii(sFile))) {
                JOptionPane.showMessageDialog(internalFrame,
                        "Couldn't write to file: " + sFile.getName(),
                        "Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     *
     */
    protected void saveObject(Object object) {
        if (fileChooser == null) {
            createFileChooser();
        }
        int returnVal = fileChooser.showSaveDialog(internalFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File sFile = fileChooser.getSelectedFile();
            try {
                ObjectOutputStream oo = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(sFile)));
                oo.writeObject(object);
                oo.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(internalFrame,
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
        fileChooser = new JFileChooser(new File("/resources"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
        return plotArea;
    }

    /**
     *
     */
    public void dispose() {
        internalFrame.dispose();
        internalFrame = null;
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
}
