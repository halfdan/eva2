/*
 * MultirunRefiner.java
 *
 * Created on 8. Oktober 2002, 09:47
 */

package eva2.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * MultirunRefiner
 * Description:     This is a small programm .
 * Copyright:       Copyright (c) 2001
 * Company:         University of Tuebingen, Computer Architecture
 *
 * @author Felix Streichert
 * @version: $Revision: 10 $
 * $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 * $Author: streiche $
 * @since JDK 1.3.0_02
 */

public class MultirunRefiner {
    private JFrame mainFrame;
    private JPanel myPanel, myJButtonJPanel;
    private JButton refineJButton, exitJButton;
    //    private JButton				confidenceJButton;
    private JTextArea inputText, outputText;
    private JScrollPane m_SP1, m_SP2;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem loadMenuItem, saveMenuItem;
    private JMenuItem exitMenuItem;

    /**
     * Creates a new instance of MultirunRefiner
     */
    public MultirunRefiner() {

    }

    public MultirunRefiner(File f) {
        starter();
        if (!readFile(f)) {
            System.err.println("Error, couldnt open file " + f);
        }
    }

    public MultirunRefiner(String fileName) {
        starter();
        File f = new File(fileName);
        if (!readFile(f)) {
            System.err.println("Error, couldnt open file " + f);
        }
    }

    public MultirunRefiner(String text, int numRuns) {
        starter();
        inputText.setText(text);
    }

    public void starter() {
        this.mainFrame = new JFrame("MultirunRefiner\u2122");

        // The menuebar
        this.menuBar = new JMenuBar();
        this.fileMenu = new JMenu("File");
        this.loadMenuItem = new JMenuItem("Load");
        this.loadMenuItem.setEnabled(true);
        this.loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                loadFile();
            }
        });
        this.saveMenuItem = new JMenuItem("Save");
        this.saveMenuItem.setEnabled(true);
        this.saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeFile();
            }
        });
        this.exitMenuItem = new JMenuItem("Exit");
        this.exitMenuItem.setEnabled(true);
        this.exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });

        this.fileMenu.add(this.loadMenuItem);
        this.fileMenu.add(this.saveMenuItem);
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.exitMenuItem);
        this.mainFrame.setJMenuBar(this.menuBar);

        this.mainFrame.setSize(300, 300);
        this.mainFrame.setLocation(0, 150);
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });

        this.myPanel = new JPanel();
        this.myPanel.setLayout(new GridLayout(1, 2));
        this.inputText = new JTextArea();
        this.outputText = new JTextArea();
        this.m_SP1 = new JScrollPane(this.inputText);
        this.m_SP2 = new JScrollPane(this.outputText);
        this.myPanel.add(this.m_SP1);
        this.myPanel.add(this.m_SP2);
        this.mainFrame.getContentPane().add(this.myPanel, BorderLayout.CENTER);

        this.myJButtonJPanel = new JPanel();
        this.myJButtonJPanel.setLayout(new GridLayout(2, 2));
        this.mainFrame.getContentPane().add(this.myJButtonJPanel, BorderLayout.SOUTH);

        refineJButton = new JButton("Refine Multiruns");
        refineJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                outputText.setText(refineToText(refine(inputText.getText())));
            }
        });
//        confidenceJButton = new JButton("Create Matlab/Confidence");
//        confidenceJButton.addMouseListener (new java.awt.event.MouseAdapter () {
//            public void mouseClicked (java.awt.event.MouseEvent evt) {
//                compute();
//            }
//        });
        exitJButton = new JButton("EXIT");
        exitJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.exit(0);
            }
        });
        this.myJButtonJPanel.add(refineJButton);
//        this.myJButtonJPanel.add(confidenceJButton);
        this.myJButtonJPanel.add(exitJButton);

        mainFrame.validate();
        mainFrame.setVisible(true);
    }

    /**
     * This method lets you select a file that is then opened and
     * displayed.
     */
    public void loadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select an Multirun.TXT File");
        fc.setFileFilter(new TXTFileFilter());
        int returnVal = fc.showDialog(this.mainFrame, "Load Multirun.TXT");
        if (returnVal == 0) {
            readFile(fc.getSelectedFile());
            this.mainFrame.validate();
        }
    }

    protected boolean readFile(File f) {
        FileReader fileStream;
        clearInputText();
        try {
            fileStream = new FileReader(f);
            this.inputText.read(fileStream, f);
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            clearInputText();
            return false;
        }
    }

    public void clearInputText() {
        this.inputText.setText("");
    }

    public void clearOutputText() {
        this.outputText.setText("");
    }

    public void addOutputText(String t) {
        this.outputText.setText(this.outputText.getText() + t);
    }

    /**
     * This method lets you select a destination file to save the
     * current XML document
     */
    private void writeFile() {
        // Das hier sollte ich nicht mehr brauchen....
        //this.m_Document = this.m_XMLPanel.getXML();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select destination file");
        fc.setFileFilter(new TXTFileFilter());
        int returnVal = fc.showSaveDialog(this.mainFrame);
        if (returnVal == 0) {
            try {
                FileWriter fileStream = new FileWriter(fc.getSelectedFile());
                this.outputText.write(fileStream);
            } catch (java.io.IOException ioe) {
            }
        }
    }

    protected static boolean hasNextLine(String txt) {
        return (txt != null && (txt.length() > 0));
    }

    protected static String readLine(BufferedReader br) {
        String line;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return line;
    }

    /**
     * This method will refine multiple run into on mean element
     */
    public static ArrayList<double[]> refine(String text) {
        double[] tmp;
        double[] mean = new double[3];
        int numExp = 0, iteration = 0, lineCnt = 0;
        ArrayList<double[]> result = new ArrayList<double[]>();
        String line;
        String runHeader = "Fun.calls 	 Best 	 Mean 	 Worst 	 Solution";
        String runFinalizer = " Best solution: ";
        boolean readRun = false;

        BufferedReader br = new BufferedReader(new StringReader(text));

        while ((line = readLine(br)) != null) {
//        	Pair<String, String> p = popNextLine(text);
            lineCnt++;
//        	line = p.car();
//        	text = p.cdr();

            if (line.startsWith(runHeader)) {
                numExp++;
                readRun = true;
                iteration = 0;
                System.out.println("Experiment starts at line " + lineCnt);
                continue;
            } else if (line.startsWith(runFinalizer)) {
                System.out.println("Experiment ends at line " + lineCnt);
                readRun = false;
                continue;
            }
            if (readRun) {
                tmp = parseStringForDouble(line);
                if (tmp.length > 3) {
                    if (numExp == 1) {
                        mean = new double[3];
                        result.add(iteration, mean);
                    } else {
                        mean = result.get(iteration);
                    }
                    mean[0] += tmp[1];
                    mean[1] += tmp[2];
                    mean[2] += tmp[3];
                } else {
                    System.err.println("Error in MultiRunRefiner!");
                }
                iteration++;
            }
        }
        System.out.println(lineCnt + " lines parsed. " + numExp + " experiments with " + result.size() + " events each.");

        for (int i = 0; i < result.size(); i++) {
            mean = ((double[]) (result.get(i)));
            for (int k = 0; k < mean.length; k++) {
                mean[k] /= numExp;
            }
        }
        return result;
    }

    /**
     * This method will refine multiple run into on mean element
     */
    public static String refineToText(ArrayList<double[]> result) {
        double[] mean;
        StringBuffer sbuf = new StringBuffer("Event\tBest\tMean\tWorst\n");

        for (int i = 0; i < result.size(); i++) {
            mean = ((double[]) (result.get(i)));
            sbuf.append(i + "\t" + mean[0] + "\t" + mean[1] + "\t" + mean[2] + "\n");
        }
        return sbuf.toString();
    }

    public static String refineToText(String input) {
        return refineToText(refine(input));
    }

//    public void compute() {
//        double[]    tmp;
//        double[]    mean = new double[3];
//        int         begin, end, numExp = 0;
//        ArrayList   result = new ArrayList(), tmpA;
//
//        this.clearOutputText();
//        for (int  i = 0; i < this.inputText.getLineCount(); i++) {
//            try {
//                begin = this.inputText.getLineStartOffset(i);
//                end = this.inputText.getLineEndOffset(i);
//                tmp = this.parseStringForDouble(this.inputText.getText(begin, end-begin));
//                if (tmp.length > 3) {
//                    if (((int)(tmp[0])) == 1) numExp++;
//                    if (result.size()-1 < ((int)(tmp[0]))) result.add(((int)(tmp[0])), new double[3]);
//                    mean = ((double[])(result.get(((int)(tmp[0])))));
//                    mean[0] += tmp[1];
//                    mean[1] += tmp[2];
//                    mean[2] += tmp[3];
//                }
//            } catch (javax.swing.text.BadLocationException ble){}
//        }
//        System.out.println(this.inputText.getLineCount() + " lines parsed. " + numExp + " experiments with " + result.size() + " events each.");
//        this.addOutputText("Event\tBest\tMean\tWorst\n");
//        for(int i = 0; i < result.size(); i++) {
//            mean = ((double[])(result.get(i)));
//            this.addOutputText(i+"\t"+mean[0]/numExp+"\t"+mean[1]/numExp+"\t"+mean[2]/numExp+"\n");
//        }
//    }

    /**
     * A simple method to read doubles from a string.
     *
     * @param searchme The string to be searched.
     * @return The array of doubles found.
     */
    public static double[] parseStringForDouble(String searchme) {
        double[] output;
        Vector tmpOutput;
        int positionInString = 0, from, to, i, tmp;
        boolean EndOfStringReached = false;
        String tmpString;
        char tmpchar;

        // because new Double(tmpString) does not regonize 2,3 as float(double) i need to replace ','=>'.'
        if ((searchme.startsWith("calls")) || (searchme == null)) {
            output = new double[0];
            return output;
        }
        searchme = searchme.replace(',', '.');
        tmpOutput = new Vector(10);
        from = positionInString;
        to = positionInString;
        i = 0;
        while (to < searchme.length()) {
            while ((to < searchme.length() - 1) && (searchme.charAt(to) != 9) && (!Character.isSpaceChar(searchme.charAt(to))) && (searchme.charAt(to) != '\n')) {
                tmp = searchme.charAt(to);
                to++;
            }
            if (to < searchme.length()) {
                if (to == searchme.length() - 1) {
                    to = searchme.length();
                }
                tmpString = searchme.substring(from, to);
                try {
                    tmpOutput.add(i, new Double(tmpString));
                    i++;
                } catch (java.lang.NumberFormatException e) {
                }
            }
            to++;
            from = to;
        }
        output = new double[i];
        for (int j = 0; j < i; j++) {
            output[j] = ((Double) tmpOutput.elementAt(j)).doubleValue();
        }
        return output;
    }

    /**
     * A example for a standalone GO optimization.
     */
    public static void main(String[] x) {
        MultirunRefiner universalprogram = new MultirunRefiner();
        universalprogram.starter();
    }
}
