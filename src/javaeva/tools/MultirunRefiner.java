/*
 * MultirunRefiner.java
 *
 * Created on 8. Oktober 2002, 09:47
 */

package javaeva.tools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/** MultirunRefiner
 * Description:     This is a small programm .
 * Copyright:       Copyright (c) 2001
 * Company:         University of Tuebingen, Computer Architecture
 * @author  Felix Streichert
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 * @since       JDK 1.3.0_02
 */

public class MultirunRefiner {
    private JFrame              m_Frame;
    private JPanel              myPanel, myJButtonJPanel;
    private JButton             refineJButton, confidenceJButton, exitJButton;
    private JTextArea           m_InputText, m_OutputText;
    private JScrollPane         m_SP1, m_SP2;
    private JMenuBar            m_MenuBar;
    private JMenu               m_FileJMenu;
    private JMenuItem           m_LoadExpItem, m_SaveExpItem;
    private JMenuItem           m_ExitItem;    /** Creates a new instance of MultirunRefiner */
    public MultirunRefiner() {

    }

    public void starter() {
        this.m_Frame = new JFrame("MultirunRefiner\u2122");

        // The menuebar
        this.m_MenuBar      = new JMenuBar();
        this.m_FileJMenu    = new JMenu("File");
        this.m_LoadExpItem  = new JMenuItem("Load");
            this.m_LoadExpItem.setEnabled(true);
            this.m_LoadExpItem.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent ev) {
                     loadFile();
                 }
            });
        this.m_SaveExpItem  = new JMenuItem("Save");
            this.m_SaveExpItem.setEnabled(true);
            this.m_SaveExpItem.addActionListener (new java.awt.event.ActionListener () {
                public void actionPerformed (java.awt.event.ActionEvent evt) {
                    writeFile();
                }
            });
        this.m_ExitItem     = new JMenuItem("Exit");
            this.m_ExitItem.setEnabled(true);
            this.m_ExitItem.addActionListener (new java.awt.event.ActionListener () {
                public void actionPerformed (java.awt.event.ActionEvent evt) {
                    System.exit(0);
                }
            });

        this.m_FileJMenu.add(this.m_LoadExpItem);
        this.m_FileJMenu.add(this.m_SaveExpItem);
        this.m_MenuBar.add(this.m_FileJMenu);
        this.m_MenuBar.add(this.m_ExitItem);
        this.m_Frame.setJMenuBar(this.m_MenuBar);

        this.m_Frame.setSize(300,300);
        this.m_Frame.setLocation(0, 150);
        this.m_Frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent ev) {
                    System.exit(0);
                }
        });

        this.myPanel = new JPanel();
        this.myPanel.setLayout(new GridLayout(1,2));
        this.m_InputText = new JTextArea();
        this.m_OutputText = new JTextArea();
        this.m_SP1 = new JScrollPane(this.m_InputText);
        this.m_SP2 = new JScrollPane(this.m_OutputText);
        this.myPanel.add(this.m_SP1);
        this.myPanel.add(this.m_SP2);
        this.m_Frame.getContentPane().add(this.myPanel, BorderLayout.CENTER);

        this.myJButtonJPanel    = new JPanel();
        this.myJButtonJPanel.setLayout(new GridLayout(2, 2));
        this.m_Frame.getContentPane().add(this.myJButtonJPanel, BorderLayout.SOUTH);

        refineJButton = new JButton("Refine Multiruns");
        refineJButton.addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                refine();
            }
        });
        confidenceJButton = new JButton("Create Matlab/Confidence");
        confidenceJButton.addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                compute();
            }
        });
        exitJButton = new JButton("EXIT");
        exitJButton.addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                System.exit (0);
            }
        });
        this.myJButtonJPanel.add(refineJButton);
        this.myJButtonJPanel.add(confidenceJButton);
        this.myJButtonJPanel.add(exitJButton);

        m_Frame.validate();
        m_Frame.setVisible(true);
    }

    /** This method lets you select a file that is then opened and
     * displayed.
     */
    public void loadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select an Multirun.TXT File");
        fc.setFileFilter(new TXTFileFilter());
        int returnVal = fc.showDialog(this.m_Frame, "Load Multirun.TXT");
        if (returnVal == 0) {
            BufferedReader      dis;
            String              tmp, result="";
            try {
                this.clearInputText();
                FileReader fileStream = new FileReader(fc.getSelectedFile());
                this.m_InputText.read( fileStream, fc.getSelectedFile());
            } catch (FileNotFoundException e) {
                System.out.println("FILE " + fc.getSelectedFile() + " NOT FOUND!");
            } catch (java.lang.NullPointerException npe) {
            } catch (IOException e) {
            }
            //this.m_InputText.setText(fc.getSelectedFile().getName());
            this.m_Frame.validate();
        }
    }

    public void clearInputText() {
        this.m_InputText.setText("");
    }

    public void clearOutputText() {
        this.m_OutputText.setText("");
    }

    public void addOutputText(String t) {
        this.m_OutputText.setText(this.m_OutputText.getText()+t);
    }

    /** This method lets you select a destination file to save the
     * current XML document
     */
    private void writeFile() {
        // Das hier sollte ich nicht mehr brauchen....
        //this.m_Document = this.m_XMLPanel.getXML();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select destination file");
        fc.setFileFilter(new TXTFileFilter());
        int returnVal = fc.showSaveDialog(this.m_Frame);
        if (returnVal == 0) {
            try {
                FileWriter fileStream = new FileWriter(fc.getSelectedFile());
                this.m_OutputText.write(fileStream);
            } catch (java.io.IOException ioe) {}
        }
    }

    /** This method will refine multiple run into on mean element
     */
    public void refine() {
        double[]    tmp;
        double[]    mean = new double[3];
        int         begin, end, numExp = 0;
        ArrayList   result = new ArrayList(), tmpA;

        this.clearOutputText();
        for (int  i = 0; i < this.m_InputText.getLineCount(); i++) {
            try {
                begin = this.m_InputText.getLineStartOffset(i);
                end = this.m_InputText.getLineEndOffset(i);
                tmp = this.parseStringForDouble(this.m_InputText.getText(begin, end-begin));
                if (tmp.length > 3) {
                    if (((int)(tmp[0])) == 1) numExp++;
                    if (result.size()-1 < ((int)(tmp[0]))) result.add(((int)(tmp[0])), new double[3]);
                    mean = ((double[])(result.get(((int)(tmp[0])))));
                    mean[0] += tmp[1];
                    mean[1] += tmp[2];
                    mean[2] += tmp[3];
                }
            } catch (javax.swing.text.BadLocationException ble){}
        }
        System.out.println(this.m_InputText.getLineCount() + " lines parsed. " + numExp + " experiments with " + result.size() + " events each.");
        this.addOutputText("Event\tBest\tMean\tWorst\n");
        for(int i = 0; i < result.size(); i++) {
            mean = ((double[])(result.get(i)));
            this.addOutputText(i+"\t"+mean[0]/numExp+"\t"+mean[1]/numExp+"\t"+mean[2]/numExp+"\n");
        }
    }

    public void compute() {
        double[]    tmp;
        double[]    mean = new double[3];
        int         begin, end, numExp = 0;
        ArrayList   result = new ArrayList(), tmpA;

        this.clearOutputText();
        for (int  i = 0; i < this.m_InputText.getLineCount(); i++) {
            try {
                begin = this.m_InputText.getLineStartOffset(i);
                end = this.m_InputText.getLineEndOffset(i);
                tmp = this.parseStringForDouble(this.m_InputText.getText(begin, end-begin));
                if (tmp.length > 3) {
                    if (((int)(tmp[0])) == 1) numExp++;
                    if (result.size()-1 < ((int)(tmp[0]))) result.add(((int)(tmp[0])), new double[3]);
                    mean = ((double[])(result.get(((int)(tmp[0])))));
                    mean[0] += tmp[1];
                    mean[1] += tmp[2];
                    mean[2] += tmp[3];
                }
            } catch (javax.swing.text.BadLocationException ble){}
        }
        System.out.println(this.m_InputText.getLineCount() + " lines parsed. " + numExp + " experiments with " + result.size() + " events each.");
        this.addOutputText("Event\tBest\tMean\tWorst\n");
        for(int i = 0; i < result.size(); i++) {
            mean = ((double[])(result.get(i)));
            this.addOutputText(i+"\t"+mean[0]/numExp+"\t"+mean[1]/numExp+"\t"+mean[2]/numExp+"\n");
        }
    }

    /** A simple method to read doubles from a string.
     * @param String    The string to be searched.
     * @return          The array of doubles found.
     */
    private double[] parseStringForDouble (String searchme) {
        double []       output;
        Vector          tmpOutput;
        int             positionInString = 0, from, to, i, tmp;
        boolean         EndOfStringReached = false;
        String          tmpString;
        char            tmpchar;

        // because new Double(tmpString) does not regonize 2,3 as float(double) i need to replace ','=>'.'
        if ((searchme.startsWith("calls")) || (searchme == null)) {
            output = new double[0];
            return output;
        }
        searchme    = searchme.replace(',', '.');
        tmpOutput   = new Vector(10);
        from        = positionInString;
        to          = positionInString;
        i = 0;
        while (to < searchme.length()) {
            while ((to < searchme.length()-1) && (searchme.charAt(to) != 9) &&(! Character.isSpaceChar(searchme.charAt(to))) && (searchme.charAt(to) != '\n')) {
                tmp = searchme.charAt(to);
                to++;
            }
            if (to < searchme.length()) {
                if (to == searchme.length()-1) to = searchme.length();
                tmpString = searchme.substring(from, to);
                try {
                    tmpOutput.add(i, new Double(tmpString));
                    i ++;
                } catch (java.lang.NumberFormatException e) {
                }
            }
            to++;
            from = to;
        }
        output  = new double[i];
        for (int j = 0; j < i; j++) output[j] = ((Double)tmpOutput.elementAt(j)).doubleValue();
        return output;
    }
    /**
     *   A example for a standalone GO optimization.
     */
    public static void  main (String[] x) {
        MultirunRefiner universalprogram = new MultirunRefiner();
        universalprogram.starter();
    }
}
