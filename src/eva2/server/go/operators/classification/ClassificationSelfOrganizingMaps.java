package eva2.server.go.operators.classification;

import javax.swing.*;

import eva2.tools.chart2d.*;
import eva2.tools.math.RNG;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

/** Self-organizing maps, a simple, but easy to visualize method
 * for classification. The Dikel flag is an undocumented extension,
 * which seems to work but is not published.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.12.2004
 * Time: 15:10:45
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationSelfOrganizingMaps implements java.io.Serializable, InterfaceClassification {

    private int                         m_Dim1 = 5, m_Dim2 = 15;
    private int                         m_AlternativeClasses;
    private double[][][]                m_SOM;
    private int[][][]                   m_SOMClass;
    private double[][]                  m_Range; //[dimension][min, max, mean, sigma]
    private double                      m_Alpha             = 0.4;
    private int                         m_TrainingCycles    = 250;
    private int                         m_NeighborhoodSize  = 2;
    private boolean                     m_DikelThis         = true;
    private boolean                     debug               = false;

    public ClassificationSelfOrganizingMaps() {
    }
    public ClassificationSelfOrganizingMaps(ClassificationSelfOrganizingMaps a) {
        this.m_Dim1         = a.m_Dim1;
        this.m_Dim2         = a.m_Dim2;
        this.m_AlternativeClasses   = a.m_AlternativeClasses;
        this.m_NeighborhoodSize     = a.m_NeighborhoodSize;
        this.m_Alpha        = a.m_Alpha;
        this.m_DikelThis    = a.m_DikelThis;
        this.m_TrainingCycles = a.m_TrainingCycles;
        if (a.m_SOM != null) {
            this.m_SOM = new double[a.m_SOM.length][a.m_SOM[0].length][a.m_SOM[0][0].length];
            for (int i = 0; i < a.m_SOM.length; i++) {
                for (int j = 0; j < a.m_SOM[0].length; j++) {
                    for (int k = 0; k < a.m_SOM[0][0].length; k++) {
                        this.m_SOM[i][j][k] = a.m_SOM[i][j][k];
                    }
                }
            }
        }
        if (a.m_SOMClass != null) {
            this.m_SOMClass = new int[a.m_SOMClass.length][a.m_SOMClass[0].length][a.m_SOMClass[0][0].length];
            for (int i = 0; i < a.m_SOMClass.length; i++) {
                for (int j = 0; j < a.m_SOMClass[0].length; j++) {
                    for (int k = 0; k < a.m_SOMClass[0][0].length; k++) {
                        this.m_SOMClass[i][j][k] = a.m_SOMClass[i][j][k];
                    }
                }
            }
        }
        if (a.m_Range != null) {
            this.m_Range = new double[a.m_Range.length][4];
            for (int i = 0; i < this.m_Range.length; i++) {
                this.m_Range[i][0] = a.m_Range[i][0];
                this.m_Range[i][1] = a.m_Range[i][1];
                this.m_Range[i][2] = a.m_Range[i][2];
                this.m_Range[i][3] = a.m_Range[i][3];
            }
        }
    }

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone() {
        return (Object) new ClassificationSelfOrganizingMaps(this);
    }

    /** This method will init the classificator
     * @param space     The double[n][d] space
     * @param type      The classes [0,1,..]
     */
    public void init(double[][] space, int[] type) {
        this.m_AlternativeClasses = 0;
        for (int i = 0; i < type.length; i++) {
            this.m_AlternativeClasses = Math.max(type[i], this.m_AlternativeClasses);
        }
        this.m_AlternativeClasses++;
        this.m_SOM          = new double[this.m_Dim1][this.m_Dim2][space[0].length];
        this.m_SOMClass     = new int[this.m_Dim1][this.m_Dim2][this.m_AlternativeClasses];
        this.m_Range        = new double[space[0].length][4];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][0] = Double.POSITIVE_INFINITY;
            this.m_Range[i][1] = Double.NEGATIVE_INFINITY;
            this.m_Range[i][2] = 0;
            this.m_Range[i][3] = 0;
        }
        for (int i = 0; i < space.length; i++) {
            for (int k = 0; k < space[0].length; k++) {
                this.m_Range[k][0] = Math.min(this.m_Range[k][0], space[i][k]);
                this.m_Range[k][1] = Math.max(this.m_Range[k][1], space[i][k]);
                this.m_Range[k][2] += space[i][k];
            }
        }
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][2] = this.m_Range[i][2]/((double)space.length);
            for (int j = 0; j < space.length; j++) {
                this.m_Range[i][3] += Math.pow((this.m_Range[i][2] - space[j][i]), 2);
            }
            this.m_Range[i][3] = Math.sqrt(this.m_Range[i][3]/((double)space.length));
//            System.out.println("Range: ["+this.m_Range[i][0]+", "+this.m_Range[i][1]+"] Mean: "+this.m_Range[i][2]+" Var: "+this.m_Range[i][3]);
        }
        this.initSOM();
//        if (this.debug) this.drawSOM(space, type);
    }

    /** This method inits the weights of the SOM in the current range
     */
    private void initSOM() {
        for (int i = 0; i < this.m_SOM.length; i++) {
            for (int j = 0; j < this.m_SOM[0].length; j++) {
                for (int k = 0; k < this.m_SOM[0][0].length; k++) {
                    this.m_SOM[i][j][k] = 0*RNG.randomDouble(
                        (this.m_Range[k][0] - this.m_Range[k][2])/(1+this.m_Range[k][3]),
                        (this.m_Range[k][1] - this.m_Range[k][2])/(1+this.m_Range[k][3]));
                }
                for (int k = 0; k < this.m_SOMClass[0][0].length; k++) {
                    this.m_SOMClass[i][j][k] = 0;
                }
            }
        }
    }

    /** This method allows you to train the classificator based on
     * double[d] values and the class. n gives the number of instances
     * and d gives the dimension of the search space.
     * @param space     The double[n][d] space
     * @param type      The int[n] classes [0,1,..]
     */
    public void train(double[][] space, int[] type) {
        // first init the assignment to zero
        for (int i = 0; i < this.m_SOM.length; i++) {
            for (int j = 0; j < this.m_SOM[0].length; j++) {
                for (int k = 0; k < this.m_SOMClass[0][0].length; k++) {
                    this.m_SOMClass[i][j][k] = 0;
                }
            }
        }
        // now train the stuff
        int[]   order;
        int[]   winner;
        for (int t = 0; t < this.m_TrainingCycles; t++) {
            // train the full set
            order = RNG.randomPermutation(space.length);
            for (int i = 0; i < order.length; i++) {
                winner = this.findWinningNeuron(space[order[i]]);
                // now i got the winning neuron *puh*
                // update this neuron and the neighbors
                this.update(winner, space[order[i]], t/((double)this.m_TrainingCycles));

            }
//            if (this.debug) this.drawSOM(space, type);
        }
        // finally assign the classes to the neurons
        // most likely it is a percentage value
        for (int i = 0; i < space.length; i++) {
            winner = this.findWinningNeuron(space[i]);
            this.m_SOMClass[winner[0]][winner[1]][type[i]]++;
        }
        if (this.debug) this.drawSOM(space, type);
    }

    /** This method updates a given winner neuron and it's neighbours
     * @param w         The winner coordinates [x,y]
     * @param data      The current data point
     * @param t         The current time
     */
    private void update(int[] w, double[] data, double t) {
        double  a = this.m_Alpha*(1-t);
        double  dist;
        int[]   tmpI = new int[2];

        // move the winner to the data point
        if (this.m_DikelThis) {
            this.drikelWinnerTo(w, data, a);
        } else {
            this.moveNeuronTo(w, data, a);
        }

        // move the neighbors to the data point
        for (int i = -this.m_NeighborhoodSize; i <= this.m_NeighborhoodSize; i++) {
            for (int j = -this.m_NeighborhoodSize; j <= this.m_NeighborhoodSize; j++) {
                // not the original point
                if ((j != 0) || (i != 0)) {
                    // not outside the array
                    if ((this.m_SOM.length > w[0]+i) && (w[0]+i>= 0)
                      && (this.m_SOM[0].length > w[1]+j) && (w[1]+j>= 0)) {
                        dist  = Math.sqrt(i*i + j*j);
                        tmpI[0] = w[0]+i;
                        tmpI[1] = w[1]+j;
                        this.moveNeuronTo(tmpI, data, a/dist);
                    }
                }
            }
        }
    }

    /** This method finds the winning neuron for a given data point
     * @param data  The data point
     */
    private int[] findWinningNeuron(double[] data) {
        double minDist = Double.POSITIVE_INFINITY, dist;
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        // find the winning neuron for order[j]
        for (int m = 0; m < this.m_SOM.length; m++) {
            for (int n = 0; n < this.m_SOM[0].length; n++) {
                dist = this.distance(this.m_SOM[m][n], data);
                if (minDist > dist) {
                    minDist = dist;
                    result[0] = m;
                    result[1] = n;
                }
            }
        }
        return result;
    }

    /** This method returns the euclidian distance between a neuron
     * and a data point
     * @param n    The neuron
     * @param d    The data point
     * @return The distance between them
     */
    public double distance(double[] n, double[] d) {
        double result = 0;

        for (int i = 0; i < n.length; i++) {
            result += Math.pow(n[i] - (d[i] - this.m_Range[i][2])/(1+this.m_Range[i][3]), 2);
        }
        result = Math.sqrt(result);

        return result;
    }

    /** This method moves a neuron to a data point
     * @param w    The neuron
     * @param d    The data point
     * @param a    The scaling factor
     */
    public void moveNeuronTo(int[] w, double[] d, double a) {
        double[] vec = new double[this.m_SOM[w[0]][w[1]].length];

        for (int i = 0; i < this.m_SOM[w[0]][w[1]].length; i++) {
            vec[i] = (d[i] - this.m_Range[i][2])/(1+this.m_Range[i][3]) - this.m_SOM[w[0]][w[1]][i];
            this.m_SOM[w[0]][w[1]][i] += a * vec[i];
        }
    }

    /** This method moves the winner neuro in a wierd way
     * back and forth again
     * @param w    The neuron
     * @param d    The data point
     * @param a    The scaling factor
     */
    public void drikelWinnerTo(int[] w, double[] d, double a) {
        double[] vec = new double[this.m_SOM[w[0]][w[1]].length];
        double[] nec = new double[this.m_SOM[w[0]][w[1]].length];
        double   dist;

        for (int i = 0; i < this.m_SOM[w[0]][w[1]].length; i++) {
            vec[i] = (d[i] - this.m_Range[i][2])/(1+this.m_Range[i][3]) - this.m_SOM[w[0]][w[1]][i];
            nec[i] = 0;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // not the original point
                if ((j != 0) || (i != 0)) {
                    // not outside the array
                    if ((this.m_SOM.length > w[0]+i) && (w[0]+i>= 0)
                      && (this.m_SOM[0].length > w[1]+j) && (w[1]+j>= 0)) {
                        dist  = Math.sqrt(i*i + j*j);
                        for (int k = 0; k < this.m_SOM[0][0].length; k++) {
                            nec[k] += (d[k] - this.m_Range[k][2])/(1+this.m_Range[k][3]) - this.m_SOM[w[0]+i][w[1]+j][k];
                        }
                    }
                }
            }
        }
        for (int i = 0; i < this.m_SOM[w[0]][w[1]].length; i++) {
            vec[i] = vec[i] - (a/2.0)*nec[i];
            this.m_SOM[w[0]][w[1]][i] += a * vec[i];
        }
    }

    /** This method will classify a given data point
     * @param point     The double[d] data point.
     * @return type     The resulting class.
     */
    public int getClassFor(double[] point) {
        int[] winner = this.findWinningNeuron(point);
        int mostClasses = 0;
        int result = 0;

        for (int i = 0; i < this.m_SOMClass[winner[0]][winner[1]].length; i++) {
            if (mostClasses < this.m_SOMClass[winner[0]][winner[1]][i]) {
                result      = i;
                mostClasses = this.m_SOMClass[winner[0]][winner[1]][i];
            }
        }
        return result;
    }

    /** For debugging only
     */
    private void drawSOM(double[][] data, int[] types) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        frame.setTitle("SOM tester");
        frame.setSize(500, 500);
        frame.setLocation(530, 50);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        DLine       tmpL;
        double[]    pos1, pos2;
        ScaledBorder myBorder = new ScaledBorder();
        DArea area = new DArea();
        area.setBorder(myBorder);
        area.setBackground(Color.white);
        for (int i = 0; i < this.m_SOM.length; i++) {
            for (int j = 0; j < this.m_SOM[0].length; j++) {
                pos1 = this.m_SOM[i][j];
                if ((i+1) < this.m_SOM.length) {
                    pos2 = this.m_SOM[i+1][j];
                    tmpL = new DLine(pos1[0], pos1[1], pos2[0], pos2[1], Color.BLACK);
                    area.addDElement(tmpL);
                }
                if ((j+1) < this.m_SOM[i].length) {
                    pos2 = this.m_SOM[i][j+1];
                    tmpL = new DLine(pos1[0], pos1[1], pos2[0], pos2[1], Color.BLACK);
                    area.addDElement(tmpL);
                }
            }
        }
        DPoint tmpP;
//        for (int i = 0; i < data.length; i++) {
//            tmpP = new DPoint(data[i][0], data[i][1]);
//            tmpP.setIcon(new Chart2DDPointIconCross());
//            tmpP.setColor(Color.RED);
//            area.addDElement(tmpP);
//        }
        for (int i = 0; i < data.length; i++) {
            tmpP = new DPoint((data[i][0] - this.m_Range[0][2])/(1+this.m_Range[0][3]), (data[i][1] - this.m_Range[1][2])/(1+this.m_Range[1][3]));
            tmpP.setIcon(new Chart2DDPointIconCross());
            tmpP.setColor(this.getColorFor(types[i]));
            area.addDElement(tmpP);
        }
        panel.add(area, BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.validate();
        frame.show();
    }

    /** This method will return a view on the SOM and it classification result
     */
    public JComponent getViewOnSOM() {
        DArea result = new DArea();
        result.setBackground(Color.WHITE);
        result.setVisibleRectangle(0,0,this.m_SOM.length, this.m_SOM[0].length);
        result.setPreferredSize(new Dimension(this.m_SOM.length*10, this.m_SOM[0].length*10));
        result.setMinimumSize(new Dimension(this.m_SOM.length*2, this.m_SOM[0].length*2));
        DRectangle  tmpRect;
        int         best;
        double      total;
        double      currentP;
        double      lastP;
        for (int i = 0; i < this.m_SOM.length; i++) {
            for (int j = 0; j < this.m_SOM[i].length; j++) {
                total       = 0;
                currentP    = 0;
                lastP       = 0;
                // first determine how many instances have been assigned to
                for (int k = 0; k < this.m_SOMClass[i][j].length; k++) {
                    total += this.m_SOMClass[i][j][k];
                }
                // now determine the percentage for each class and draw the box
                if (false) {
                    // draw only dominat class
                    tmpRect = new DRectangle(i, j, 1, 1);
                    tmpRect.setColor(Color.BLACK);
                    best    = 0;
                    for (int k = 0; k < this.m_SOMClass[i][j].length; k++) {
                        if (best < this.m_SOMClass[i][j][k]) {
                            best = this.m_SOMClass[i][j][k];
                            tmpRect.setFillColor(this.getColorFor(k));
                        }
                    }
                    result.addDElement(tmpRect);
                } else {
                    // try to draw the percentage for each element
                    for (int k = 0; k < this.m_SOMClass[i][j].length; k++) {
                        currentP = this.m_SOMClass[i][j][k]/total;
                        if (currentP > 0) {
                            tmpRect = new DRectangle(i, j+lastP, 1, currentP);
                            tmpRect.setColor(this.getColorFor(k));
                            tmpRect.setFillColor(this.getColorFor(k));
                            result.addDElement(tmpRect);
                            lastP += currentP;
                        }
                    }
                    // just a final bounding box around the neuron
                    tmpRect = new DRectangle(i, j, 1, 1);
                    tmpRect.setColor(Color.BLACK);
                    tmpRect.setFillColor(new Color(0, 0, 0, 0.0f));
                    result.addDElement(tmpRect);
                }
            }
        }

        return result;
    }

    private Color getColorFor(int i)  {
        switch (i) {
            case 0 : return Color.BLUE;
            case 1 : return Color.RED;
            case 2 : return Color.GREEN;
            case 3 : return Color.CYAN;
            case 4 : return Color.ORANGE;
            case 5 : return Color.MAGENTA;
            case 6 : return Color.YELLOW;
            case 7 : return Color.PINK;
            case 8 : return Color.GRAY;
        }
        return Color.LIGHT_GRAY;
    }

    public static void main(String[] args) {
        ClassificationSelfOrganizingMaps som = new ClassificationSelfOrganizingMaps();
        som.setSizeX(5);
        som.setSizeY(25);
        int num = 25;
        double[][]  data = new double[num*2][2];
        int[]       type = new int[num*2];
        for (int i = 0; i < num; i++) {
            data[i][0]  = -0.8 + RNG.gaussianDouble(0.3);
            data[i][1]  = -0.5 + RNG.gaussianDouble(0.1);
            type[i]     = 0;
            data[i+num][0] =  0.1 + RNG.gaussianDouble(0.1);
            data[i+num][1] =  0.2 + RNG.gaussianDouble(0.1);
            type[i+num]  = 1;
        }
        som.init(data, type);
        som.train(data, type);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Self-Organizing Maps";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The Self-Organizing Maps, have been proposed by Kohonen (read this book on SOMs for further details).";
    }

    /** This method allows you to set the number of neurons
     * in x dimension
     * @param t    The number of neurons in x dimension
     */
    public void setSizeX(int t) {
        if (t < 1) t = 1;
        this.m_Dim1 = t;
    }
    public int getSizeX() {
        return this.m_Dim1;
    }
    public String sizeXTipText() {
        return "Set the number of neurons in x dimension.";
    }
    /** This method allows you to set the number of neurons
     * in y dimension
     * @param t    The number of neurons in y dimension
     */
    public void setSizeY(int t) {
        if (t < 1) t = 1;
        this.m_Dim2 = t;
    }
    public int getSizeY() {
        return this.m_Dim2;
    }
    public String sizeYTipText() {
        return "Set the number of neurons in y dimension.";
    }
    /** This method allows you to set the number of training cycles
     * @param t    The number of training cycles
     */
    public void setTrainingCycles(int t) {
        if (t < 1) t = 1;
        this.m_TrainingCycles = t;
    }
    public int getTrainingCycles() {
        return this.m_TrainingCycles;
    }
    public String trainingCyclesTipText() {
        return "Set the number of training cycles to perform.";
    }
    /** This method allows you to set the size of the neighorhood
     * @param t    The size of the neighborhood
     */
    public void setNeighborhoodSize(int t) {
        if (t < 0) t = 0;
        this.m_NeighborhoodSize = t;
    }
    public int getNeighborhoodSize() {
        return this.m_NeighborhoodSize;
    }
    public String neighborhoodSizeTipText() {
        return "Set the size of the neighborhood.";
    }
    /** This method allows you to set the initial alpha value
     * @param t    The initial alpha value
     */
    public void setAlpha(double t) {
        if (t < 0) t = 0;
        if (t > 0.5) t = 0.5;
        this.m_Alpha = t;
    }
    public double getAlpha() {
        return this.m_Alpha;
    }
    public String AlphaTipText() {
        return "Choose the initial alpha (0-0.5).";
    }
    /** Activate attractive neignbors for the winner neuron
     * @param t    The dikel factor
     */
    public void setDikelThis(boolean t) {
        this.m_DikelThis = t;
    }
    public boolean getDikelThis() {
        return this.m_DikelThis;
    }
    public String dikelThisTipText() {
        return "Activate attractive neignbors for the winner neuron.";
    }

}
