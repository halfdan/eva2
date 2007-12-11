package javaeva.server.go.tools;

import javaeva.gui.GraphPointSet;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.02.2005
 * Time: 19:17:38
 * To change this template use File | Settings | File Templates.
 */
public class PortfolioFrontTester {

    private javaeva.gui.Plot        m_Plot;
    private int                     index           = 0;
    private BufferedWriter          m_OutputFile    = null;
    private double[]                loss;
    private double[][]              risk;

    private void show() {
        loss = new double[5];
        loss[0] = 1.0;
        loss[1] = 0.4;
        loss[2] = 0.7;
        loss[3] = 0.5;
        loss[4] = 0.2;

        risk = new double[5][5];
        risk[0][0] = 1.0;
        risk[1][1] = 0.3;
        risk[2][2] = 0.7;
        risk[3][3] = 0.5;
        risk[4][4] = 0.2;

        risk[0][1] = 0.00;
        risk[0][2] = 0.20;
        risk[0][3] = -0.1;
        risk[0][4] = 0.30;

        risk[1][2] = 0.00;
        risk[1][3] = 0.00;
        risk[1][4] = 0.00;

        risk[2][3] = 0.30;
        risk[2][4] = -0.10;

        risk[3][4] = 0.00;

        for (int i = 0; i < risk.length; i++) {
            for (int j = i+1; j < risk.length; j++) {
                risk[j][i] = risk[i][j];
            }
        }

        this.initShow();

        this.limitK3(0.283);
    }

    private void fullK2() {
        // now calc the stuff
        ArrayList complete = new ArrayList();
        ArrayList elements = new ArrayList();
        double[]    tmpD;
        double[][]  tmpElem;
        int         res = 25;
        for (int i = 0; i < loss.length; i++) {
            for (int j = i+1; j < loss.length; j++) {
                tmpElem = new double[res+1][2];
                double w = 0;
                for (int k = 0; k < res+1; k++) {
                    tmpElem[k][0] = w*loss[i] + (1-w)*loss[j];
                    tmpElem[k][1] = (w*w)*risk[i][i] + ((1-w)*(1-w))*risk[j][j] + 2*(w*(1-w)*risk[i][j]);
                    tmpD = new double[2];
                    tmpD[0] = tmpElem[k][0];
                    tmpD[1] = tmpElem[k][1];
                    w += 1/(double)res;
                    complete.add(tmpD);
                }
                // now plot this line
                this.showLine(tmpElem);
                elements.add(tmpElem);
            }
        }
        this.saveThisStuff(complete, elements);
    }

    private void limitK2(double limit) {
        // now calc the stuff
        ArrayList complete = new ArrayList();
        ArrayList elements = new ArrayList();
        double[]    tmpD;
        double[][]  tmpElem;
        int         res = 100;
        for (int i = 0; i < loss.length; i++) {
            for (int j = i+1; j < loss.length; j++) {
                tmpElem = new double[res+1][2];
                double w = 0;
                for (int k = 0; k < res+1; k++) {
                    if (! ((w > limit) &&((1-w)> limit))) {
                        tmpElem[k][0] = w*loss[i] + (1-w)*loss[j];
                        tmpElem[k][1] = (w*w)*risk[i][i] + ((1-w)*(1-w))*risk[j][j] + 2*(w*(1-w)*risk[i][j]);
                        tmpD = new double[2];
                        tmpD[0] = tmpElem[k][0];
                        tmpD[1] = tmpElem[k][1];
                        complete.add(tmpD);
                    } else {
                        tmpElem[k][0] = loss[i];
                        tmpElem[k][1] = risk[i][i];
                    }
                    w += 1/(double)res;
                }
                // now plot this line
                this.showLine(tmpElem);
                elements.add(tmpElem);
            }
        }
        this.saveThisStuff(complete, elements);
    }

    private void fullK3() {
        // now calc the stuff
        ArrayList complete = new ArrayList();
        ArrayList elements = new ArrayList();
        double[]  tmpD;
        ArrayList tmpElem;
        int         res = 20;
        for (int i = 0; i < loss.length; i++) {
            for (int j = i+1; j < loss.length; j++) {
                for (int k = j+1; k < loss.length; k++) {
                    tmpElem = new ArrayList();
                    for (double t1 = 0; t1 < 1.0; t1 += 1/(double)res) {
                        for (double t2 = 0; t2 < (1.0 - t1); t2 += 1/(double)res) {
                            tmpD = new double[2];
                            tmpD[0] = t1*loss[i] + t2*loss[j] + (1-t1-t2)*loss[k];
                            tmpD[1] = (t1*t1)*risk[i][i] + t2*t2*risk[j][j] + (1-t1-t2)*(1-t1-t2)*risk[k][k]
                                    + 2*(t1*t2*risk[i][j]) + 2*((1-t1-t2)*t2*risk[k][j])+ 2*(t1*(1-t1-t2)*risk[i][k]);
                            tmpElem.add(tmpD);
                            complete.add(tmpD);
                        }
                    }
                    // now plot this line
                    this.showLine(tmpElem);
                    elements.add(tmpElem);
                }
            }
        }
        this.saveThisStuffK3(complete, elements);
    }

    private void limitK3(double limit) {
        // now calc the stuff
        ArrayList complete = new ArrayList();
        ArrayList elements = new ArrayList();
        double[]  tmpD;
        ArrayList tmpElem;
        int         res = 35;
        for (int i = 0; i < loss.length; i++) {
            for (int j = i+1; j < loss.length; j++) {
                for (int k = j+1; k < loss.length; k++) {
                    tmpElem = new ArrayList();
                    for (double t1 = 0; t1 < 1.0; t1 += 1/(double)res) {
                        for (double t2 = 0; t2 < (1.0 - t1); t2 += 1/(double)res) {
                            if (!(this.exceedingLimit(limit, t1, t2, (1-t1-t2)) > 1)) {
                                tmpD = new double[2];
                                tmpD[0] = t1*loss[i] + t2*loss[j] + (1-t1-t2)*loss[k];
                                tmpD[1] = (t1*t1)*risk[i][i] + t2*t2*risk[j][j] + (1-t1-t2)*(1-t1-t2)*risk[k][k]
                                        + 2*(t1*t2*risk[i][j]) + 2*((1-t1-t2)*t2*risk[k][j])+ 2*(t1*(1-t1-t2)*risk[i][k]);
                                tmpElem.add(tmpD);
                                complete.add(tmpD);
                            }
                        }
                    }
                    // now plot this line
                    this.showLine(tmpElem);
                    elements.add(tmpElem);
                }
            }
        }
        this.saveThisStuffK3(complete, elements);
    }

    private int exceedingLimit(double limit, double x1, double x2, double x3) {
        int result = 0;
        if (x1 > limit) result++;
        if (x2 > limit) result++;
        if (x3 > limit) result++;
        return result;
    }

    private void saveThisStuff(ArrayList c, ArrayList e) {
        try {
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("PortX_K2_Limits045_CompleteList.txt")));
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open output file!");
        }
        String line;
        double[] tmpD;
        line = "Risk \t Loss";
        this.writeToFile(line);
        for (int i = 0; i < c.size(); i++) {
            tmpD = (double[]) c.get(i);
            line = tmpD[1] +"\t" + tmpD[0];
            this.writeToFile(line);
        }
        try {
            this.m_OutputFile.close();
        } catch (java.io.IOException ex) {
        }
        // next file
        try {
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("PortX_K2_Limits045_ListElements.txt")));
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open output file!");
        }
        int length = e.size();
        double[][][] cara = new double[length][][];
        for (int i = 0; i < e.size(); i++) {
            cara[i] = (double[][])e.get(i);
        }
        line = "";
        for (int i = 0; i < cara.length; i++) {
            line = "Risk"+i+"\t"+"Loss"+i;
        }
        this.writeToFile(line);
        for (int i = 0; i < cara[0].length; i++) {
            line = "";
            for (int j = 0; j < cara.length; j++) {
                line += cara[j][i][1] + "\t" + cara[j][i][0] + "\t";
            }
            this.writeToFile(line);
        }
        try {
            this.m_OutputFile.close();
        } catch (java.io.IOException ex) {
        }
    }

    private void saveThisStuffK3(ArrayList c, ArrayList e) {
        try {
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("PortX_K3_Limits0283_CompleteList.txt")));
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open output file!");
        }
        String line;
        double[] tmpD;
        line = "Risk \t Loss";
        this.writeToFile(line);
        for (int i = 0; i < c.size(); i++) {
            tmpD = (double[]) c.get(i);
            line = tmpD[1] +"\t" + tmpD[0];
            this.writeToFile(line);
        }
        try {
            this.m_OutputFile.close();
        } catch (java.io.IOException ex) {
        }
        // next file
        try {
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("PortX_K3_Limits0283_ListElements.txt")));
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open output file!");
        }
        int length = e.size();
        double[][][] cara = new double[length][][];
        ArrayList tmpA;
        for (int i = 0; i < e.size(); i++) {
            tmpA = (ArrayList)e.get(i);
            cara[i] = new double[tmpA.size()][];
            for (int j = 0; j < tmpA.size(); j++) {
                cara[i][j] = (double[]) tmpA.get(j);
            }
        }

        line = "";
        for (int i = 0; i < cara.length; i++) {
            line = "Risk"+i+"\t"+"Loss"+i;
        }
        this.writeToFile(line);
        for (int i = 0; i < cara[0].length; i++) {
            line = "";
            for (int j = 0; j < cara.length; j++) {
                try {
                    line += cara[j][i][1] + "\t" + cara[j][i][0] + "\t";
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    System.out.print(".");
                    line += "-1 \t - 1\t";
                }
            }
            this.writeToFile(line);
        }
        try {
            this.m_OutputFile.close();
        } catch (java.io.IOException ex) {
        }
    }

    /** This method writes Data to file.
     * @param line      The line that is to be added to the file
     */
    private void writeToFile(String line) {
        String write = line + "\n";
        if (this.m_OutputFile == null) return;
        try {
            this.m_OutputFile.write(write, 0, write.length());
            this.m_OutputFile.flush();
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
    }

    private void initShow() {
        double[] tmpD = new double[2];
        tmpD[0] = 0;
        tmpD[1] = 0;
        this.m_Plot = new javaeva.gui.Plot("Multiobjective Optimization", "Y1", "Y2", tmpD, tmpD);

    }

    private void showLine(double[][] elm) {
        GraphPointSet   mySet = new GraphPointSet(this.index, this.m_Plot.getFunctionArea());
        mySet.setConnectedMode(false);
        this.index++;
        for (int i = 0; i < elm.length; i++) {
            mySet.addDPoint(elm[i][1], elm[i][0]);
        }
    }
    private void showLine(ArrayList elm) {
        double[][] res = new double[elm.size()][];
        for (int i = 0; i < elm.size(); i++) {
            res[i] = (double[])elm.get(i);
        }
        this.showLine(res);
    }


    public static void main(String[] args) {
        PortfolioFrontTester t = new PortfolioFrontTester();
        t.show();
    }
}