package eva2.server.go.tools;


import eva2.gui.GraphPointSet;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 06.10.2004
 * Time: 14:10:37
 * To change this template use File | Settings | File Templates.
 */
public class ParetoFrontLocalTester {

    private eva2.gui.Plot        m_Plot;
    private int                     index           = 0;
    private BufferedWriter          m_OutputFile    = null;

    private void show() {
        double[] loss = new double[5];
        loss[0] = 0.0;
        loss[1] = 1.0;
        loss[2] = 0.2;
        loss[3] = 0.5;
        loss[4] = 0.7;

        double[][] risk = new double[5][5];
        risk[0][0] = 1.0;
        risk[1][1] = 0.0;
        risk[2][2] = 0.7;
        risk[3][3] = 0.5;
        risk[4][4] = 0.2;

        risk[0][1] = 0.00;
        risk[0][2] = 0.10;
        risk[0][3] = 0.00;
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

        // now calc the stuff
        ArrayList complete = new ArrayList();
        ArrayList elements = new ArrayList();
        double[]    tmpD;
        double[][]  tmpElem;
        int         res = 100;
        for (int i = 0; i < loss.length; i++) {
            tmpElem = new double[res+1][2];
            for (int j = i+1; j < loss.length; j++) {
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

    private void saveThisStuff(ArrayList c, ArrayList e) {
        try {
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("CompleteList")));
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
            this.m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("ListElements")));
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
        this.m_Plot = new eva2.gui.Plot("Multiobjective Optimization", "Y1", "Y2", tmpD, tmpD);

    }

    private void showLine(double[][] elm) {
        GraphPointSet   mySet = new GraphPointSet(this.index, this.m_Plot.getFunctionArea());
        this.index++;
        for (int i = 0; i < elm.length; i++) {
            mySet.addDPoint(elm[i][1], elm[i][0]);
        }
    }

    public static void main(String[] args) {
        ParetoFrontLocalTester t = new ParetoFrontLocalTester();
        t.show();
    }
}
