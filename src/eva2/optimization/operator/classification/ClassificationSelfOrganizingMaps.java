package eva2.optimization.operator.classification;

import eva2.tools.chart2d.*;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Self-organizing maps, a simple, but easy to visualize method
 * for classification. The Dikel flag is an undocumented extension,
 * which seems to work but is not published.
 */
public class ClassificationSelfOrganizingMaps implements java.io.Serializable, InterfaceClassification {

    /**
     * Generated serial version identifer.
     */
    private static final long serialVersionUID = 1447707947002269263L;
    private int dim1 = 5, dim2 = 15;
    private int alternativeClasses;
    private double[][][] SOM;
    private int[][][] SOMClass;
    private double[][] range; //[dimension][min, max, mean, sigma]
    private double alpha = 0.4;
    private int trainingCycles = 250;
    private int neighborhoodSize = 2;
    private boolean dikelThis = true;
    private boolean debug = false;

    public ClassificationSelfOrganizingMaps() {
    }

    public ClassificationSelfOrganizingMaps(ClassificationSelfOrganizingMaps a) {
        this.dim1 = a.dim1;
        this.dim2 = a.dim2;
        this.alternativeClasses = a.alternativeClasses;
        this.neighborhoodSize = a.neighborhoodSize;
        this.alpha = a.alpha;
        this.dikelThis = a.dikelThis;
        this.trainingCycles = a.trainingCycles;
        if (a.SOM != null) {
            this.SOM = new double[a.SOM.length][a.SOM[0].length][a.SOM[0][0].length];
            for (int i = 0; i < a.SOM.length; i++) {
                for (int j = 0; j < a.SOM[0].length; j++) {
                    for (int k = 0; k < a.SOM[0][0].length; k++) {
                        this.SOM[i][j][k] = a.SOM[i][j][k];
                    }
                }
            }
        }
        if (a.SOMClass != null) {
            this.SOMClass = new int[a.SOMClass.length][a.SOMClass[0].length][a.SOMClass[0][0].length];
            for (int i = 0; i < a.SOMClass.length; i++) {
                for (int j = 0; j < a.SOMClass[0].length; j++) {
                    for (int k = 0; k < a.SOMClass[0][0].length; k++) {
                        this.SOMClass[i][j][k] = a.SOMClass[i][j][k];
                    }
                }
            }
        }
        if (a.range != null) {
            this.range = new double[a.range.length][4];
            for (int i = 0; i < this.range.length; i++) {
                this.range[i][0] = a.range[i][0];
                this.range[i][1] = a.range[i][1];
                this.range[i][2] = a.range[i][2];
                this.range[i][3] = a.range[i][3];
            }
        }
    }

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    @Override
    public Object clone() {
        return (Object) new ClassificationSelfOrganizingMaps(this);
    }

    /**
     * This method will init the classificator
     *
     * @param space The double[n][d] space
     * @param type  The classes [0,1,..]
     */
    @Override
    public void init(double[][] space, int[] type) {
        this.alternativeClasses = 0;
        for (int i = 0; i < type.length; i++) {
            this.alternativeClasses = Math.max(type[i], this.alternativeClasses);
        }
        this.alternativeClasses++;
        this.SOM = new double[this.dim1][this.dim2][space[0].length];
        this.SOMClass = new int[this.dim1][this.dim2][this.alternativeClasses];
        this.range = new double[space[0].length][4];
        for (int i = 0; i < this.range.length; i++) {
            this.range[i][0] = Double.POSITIVE_INFINITY;
            this.range[i][1] = Double.NEGATIVE_INFINITY;
            this.range[i][2] = 0;
            this.range[i][3] = 0;
        }
        for (int i = 0; i < space.length; i++) {
            for (int k = 0; k < space[0].length; k++) {
                this.range[k][0] = Math.min(this.range[k][0], space[i][k]);
                this.range[k][1] = Math.max(this.range[k][1], space[i][k]);
                this.range[k][2] += space[i][k];
            }
        }
        for (int i = 0; i < this.range.length; i++) {
            this.range[i][2] /= ((double) space.length);
            for (int j = 0; j < space.length; j++) {
                this.range[i][3] += Math.pow((this.range[i][2] - space[j][i]), 2);
            }
            this.range[i][3] = Math.sqrt(this.range[i][3] / ((double) space.length));
//            System.out.println("Range: ["+this.range[i][0]+", "+this.range[i][1]+"] Mean: "+this.range[i][2]+" Var: "+this.range[i][3]);
        }
        this.initSOM();
//        if (this.debug) this.drawSOM(space, type);
    }

    /**
     * This method inits the weights of the SOM in the current range
     */
    private void initSOM() {
        for (int i = 0; i < this.SOM.length; i++) {
            for (int j = 0; j < this.SOM[0].length; j++) {
                for (int k = 0; k < this.SOM[0][0].length; k++) {
                    this.SOM[i][j][k] = 0 * RNG.randomDouble(
                            (this.range[k][0] - this.range[k][2]) / (1 + this.range[k][3]),
                            (this.range[k][1] - this.range[k][2]) / (1 + this.range[k][3]));
                }
                for (int k = 0; k < this.SOMClass[0][0].length; k++) {
                    this.SOMClass[i][j][k] = 0;
                }
            }
        }
    }

    /**
     * This method allows you to train the classificator based on
     * double[d] values and the class. n gives the number of instances
     * and d gives the dimension of the search space.
     *
     * @param space The double[n][d] space
     * @param type  The int[n] classes [0,1,..]
     */
    @Override
    public void train(double[][] space, int[] type) {
        // first init the assignment to zero
        for (int i = 0; i < this.SOM.length; i++) {
            for (int j = 0; j < this.SOM[0].length; j++) {
                for (int k = 0; k < this.SOMClass[0][0].length; k++) {
                    this.SOMClass[i][j][k] = 0;
                }
            }
        }
        // now train the stuff
        int[] order;
        int[] winner;
        for (int t = 0; t < this.trainingCycles; t++) {
            // train the full set
            order = RNG.randomPerm(space.length);
            for (int i = 0; i < order.length; i++) {
                winner = this.findWinningNeuron(space[order[i]]);
                // now i got the winning neuron *puh*
                // update this neuron and the neighbors
                this.update(winner, space[order[i]], t / ((double) this.trainingCycles));

            }
//            if (this.debug) this.drawSOM(space, type);
        }
        // finally assign the classes to the neurons
        // most likely it is a percentage value
        for (int i = 0; i < space.length; i++) {
            winner = this.findWinningNeuron(space[i]);
            this.SOMClass[winner[0]][winner[1]][type[i]]++;
        }
        if (this.debug) {
            this.drawSOM(space, type);
        }
    }

    /**
     * This method updates a given winner neuron and it's neighbours
     *
     * @param w    The winner coordinates [x,y]
     * @param data The current data point
     * @param t    The current time
     */
    private void update(int[] w, double[] data, double t) {
        double a = this.alpha * (1 - t);
        double dist;
        int[] tmpI = new int[2];

        // move the winner to the data point
        if (this.dikelThis) {
            this.drikelWinnerTo(w, data, a);
        } else {
            this.moveNeuronTo(w, data, a);
        }

        // move the neighbors to the data point
        for (int i = -this.neighborhoodSize; i <= this.neighborhoodSize; i++) {
            for (int j = -this.neighborhoodSize; j <= this.neighborhoodSize; j++) {
                // not the original point
                if ((j != 0) || (i != 0)) {
                    // not outside the array
                    if ((this.SOM.length > w[0] + i) && (w[0] + i >= 0)
                            && (this.SOM[0].length > w[1] + j) && (w[1] + j >= 0)) {
                        dist = Math.sqrt(i * i + j * j);
                        tmpI[0] = w[0] + i;
                        tmpI[1] = w[1] + j;
                        this.moveNeuronTo(tmpI, data, a / dist);
                    }
                }
            }
        }
    }

    /**
     * This method finds the winning neuron for a given data point
     *
     * @param data The data point
     */
    private int[] findWinningNeuron(double[] data) {
        double minDist = Double.POSITIVE_INFINITY, dist;
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        // find the winning neuron for order[j]
        for (int m = 0; m < this.SOM.length; m++) {
            for (int n = 0; n < this.SOM[0].length; n++) {
                dist = this.distance(this.SOM[m][n], data);
                if (minDist > dist) {
                    minDist = dist;
                    result[0] = m;
                    result[1] = n;
                }
            }
        }
        return result;
    }

    /**
     * This method returns the euclidian distance between a neuron
     * and a data point
     *
     * @param n The neuron
     * @param d The data point
     * @return The distance between them
     */
    public double distance(double[] n, double[] d) {
        double result = 0;

        for (int i = 0; i < n.length; i++) {
            result += Math.pow(n[i] - (d[i] - this.range[i][2]) / (1 + this.range[i][3]), 2);
        }
        result = Math.sqrt(result);

        return result;
    }

    /**
     * This method moves a neuron to a data point
     *
     * @param w The neuron
     * @param d The data point
     * @param a The scaling factor
     */
    public void moveNeuronTo(int[] w, double[] d, double a) {
        double[] vec = new double[this.SOM[w[0]][w[1]].length];

        for (int i = 0; i < this.SOM[w[0]][w[1]].length; i++) {
            vec[i] = (d[i] - this.range[i][2]) / (1 + this.range[i][3]) - this.SOM[w[0]][w[1]][i];
            this.SOM[w[0]][w[1]][i] += a * vec[i];
        }
    }

    /**
     * This method moves the winner neuro in a wierd way
     * back and forth again
     *
     * @param w The neuron
     * @param d The data point
     * @param a The scaling factor
     */
    public void drikelWinnerTo(int[] w, double[] d, double a) {
        double[] vec = new double[this.SOM[w[0]][w[1]].length];
        double[] nec = new double[this.SOM[w[0]][w[1]].length];

        for (int i = 0; i < this.SOM[w[0]][w[1]].length; i++) {
            vec[i] = (d[i] - this.range[i][2]) / (1 + this.range[i][3]) - this.SOM[w[0]][w[1]][i];
            nec[i] = 0;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // not the original point
                if ((j != 0) || (i != 0)) {
                    // not outside the array
                    if ((this.SOM.length > w[0] + i) && (w[0] + i >= 0)
                            && (this.SOM[0].length > w[1] + j) && (w[1] + j >= 0)) {
                        for (int k = 0; k < this.SOM[0][0].length; k++) {
                            nec[k] += (d[k] - this.range[k][2]) / (1 + this.range[k][3]) - this.SOM[w[0] + i][w[1] + j][k];
                        }
                    }
                }
            }
        }
        for (int i = 0; i < this.SOM[w[0]][w[1]].length; i++) {
            vec[i] -= (a / 2.0) * nec[i];
            this.SOM[w[0]][w[1]][i] += a * vec[i];
        }
    }

    /**
     * This method will classify a given data point
     *
     * @param point The double[d] data point.
     * @return type     The resulting class.
     */
    @Override
    public int getClassFor(double[] point) {
        int[] winner = this.findWinningNeuron(point);
        int mostClasses = 0;
        int result = 0;

        for (int i = 0; i < this.SOMClass[winner[0]][winner[1]].length; i++) {
            if (mostClasses < this.SOMClass[winner[0]][winner[1]][i]) {
                result = i;
                mostClasses = this.SOMClass[winner[0]][winner[1]][i];
            }
        }
        return result;
    }

    /**
     * For debugging only
     */
    private void drawSOM(double[][] data, int[] types) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        frame.setTitle("SOM tester");
        frame.setSize(500, 500);
        frame.setLocation(530, 50);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        DLine tmpL;
        double[] pos1, pos2;
        ScaledBorder myBorder = new ScaledBorder();
        DArea area = new DArea();
        area.setBorder(myBorder);
        area.setBackground(Color.white);
        for (int i = 0; i < this.SOM.length; i++) {
            for (int j = 0; j < this.SOM[0].length; j++) {
                pos1 = this.SOM[i][j];
                if ((i + 1) < this.SOM.length) {
                    pos2 = this.SOM[i + 1][j];
                    tmpL = new DLine(pos1[0], pos1[1], pos2[0], pos2[1], Color.BLACK);
                    area.addDElement(tmpL);
                }
                if ((j + 1) < this.SOM[i].length) {
                    pos2 = this.SOM[i][j + 1];
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
            tmpP = new DPoint((data[i][0] - this.range[0][2]) / (1 + this.range[0][3]), (data[i][1] - this.range[1][2]) / (1 + this.range[1][3]));
            tmpP.setIcon(new Chart2DDPointIconCross());
            tmpP.setColor(this.getColorFor(types[i]));
            area.addDElement(tmpP);
        }
        panel.add(area, BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.validate();
        frame.show();
    }

    /**
     * This method will return a view on the SOM and it classification result
     */
    public JComponent getViewOnSOM() {
        DArea result = new DArea();
        result.setBackground(Color.WHITE);
        result.setVisibleRectangle(0, 0, this.SOM.length, this.SOM[0].length);
        result.setPreferredSize(new Dimension(this.SOM.length * 10, this.SOM[0].length * 10));
        result.setMinimumSize(new Dimension(this.SOM.length * 2, this.SOM[0].length * 2));
        DRectangle tmpRect;
        int best;
        double total;
        double currentP;
        double lastP;
        for (int i = 0; i < this.SOM.length; i++) {
            for (int j = 0; j < this.SOM[i].length; j++) {
                total = 0;
                currentP = 0;
                lastP = 0;
                // first determine how many instances have been assigned to
                for (int k = 0; k < this.SOMClass[i][j].length; k++) {
                    total += this.SOMClass[i][j][k];
                }
                // now determine the percentage for each class and draw the box
                if (false) {
                    // draw only dominat class
                    tmpRect = new DRectangle(i, j, 1, 1);
                    tmpRect.setColor(Color.BLACK);
                    best = 0;
                    for (int k = 0; k < this.SOMClass[i][j].length; k++) {
                        if (best < this.SOMClass[i][j][k]) {
                            best = this.SOMClass[i][j][k];
                            tmpRect.setFillColor(this.getColorFor(k));
                        }
                    }
                    result.addDElement(tmpRect);
                } else {
                    // try to draw the percentage for each element
                    for (int k = 0; k < this.SOMClass[i][j].length; k++) {
                        currentP = this.SOMClass[i][j][k] / total;
                        if (currentP > 0) {
                            tmpRect = new DRectangle(i, j + lastP, 1, currentP);
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

    private Color getColorFor(int i) {
        switch (i) {
            case 0:
                return Color.BLUE;
            case 1:
                return Color.RED;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.CYAN;
            case 4:
                return Color.ORANGE;
            case 5:
                return Color.MAGENTA;
            case 6:
                return Color.YELLOW;
            case 7:
                return Color.PINK;
            case 8:
                return Color.GRAY;
        }
        return Color.LIGHT_GRAY;
    }

    public static void main(String[] args) {
        ClassificationSelfOrganizingMaps som = new ClassificationSelfOrganizingMaps();
        som.setSizeX(5);
        som.setSizeY(25);
        int num = 25;
        double[][] data = new double[num * 2][2];
        int[] type = new int[num * 2];
        for (int i = 0; i < num; i++) {
            data[i][0] = -0.8 + RNG.gaussianDouble(0.3);
            data[i][1] = -0.5 + RNG.gaussianDouble(0.1);
            type[i] = 0;
            data[i + num][0] = 0.1 + RNG.gaussianDouble(0.1);
            data[i + num][1] = 0.2 + RNG.gaussianDouble(0.1);
            type[i + num] = 1;
        }
        som.init(data, type);
        som.train(data, type);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Self-Organizing Maps";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The Self-Organizing Maps, have been proposed by Kohonen (read this book on SOMs for further details).";
    }

    /**
     * This method allows you to set the number of neurons
     * in x dimension
     *
     * @param t The number of neurons in x dimension
     */
    public void setSizeX(int t) {
        if (t < 1) {
            t = 1;
        }
        this.dim1 = t;
    }

    public int getSizeX() {
        return this.dim1;
    }

    public String sizeXTipText() {
        return "Set the number of neurons in x dimension.";
    }

    /**
     * This method allows you to set the number of neurons
     * in y dimension
     *
     * @param t The number of neurons in y dimension
     */
    public void setSizeY(int t) {
        if (t < 1) {
            t = 1;
        }
        this.dim2 = t;
    }

    public int getSizeY() {
        return this.dim2;
    }

    public String sizeYTipText() {
        return "Set the number of neurons in y dimension.";
    }

    /**
     * This method allows you to set the number of training cycles
     *
     * @param t The number of training cycles
     */
    public void setTrainingCycles(int t) {
        if (t < 1) {
            t = 1;
        }
        this.trainingCycles = t;
    }

    public int getTrainingCycles() {
        return this.trainingCycles;
    }

    public String trainingCyclesTipText() {
        return "Set the number of training cycles to perform.";
    }

    /**
     * This method allows you to set the size of the neighorhood
     *
     * @param t The size of the neighborhood
     */
    public void setNeighborhoodSize(int t) {
        if (t < 0) {
            t = 0;
        }
        this.neighborhoodSize = t;
    }

    public int getNeighborhoodSize() {
        return this.neighborhoodSize;
    }

    public String neighborhoodSizeTipText() {
        return "Set the size of the neighborhood.";
    }

    /**
     * This method allows you to set the initial alpha value
     *
     * @param t The initial alpha value
     */
    public void setAlpha(double t) {
        if (t < 0) {
            t = 0;
        }
        if (t > 0.5) {
            t = 0.5;
        }
        this.alpha = t;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public String AlphaTipText() {
        return "Choose the initial alpha (0-0.5).";
    }

    /**
     * Activate attractive neignbors for the winner neuron
     *
     * @param t The dikel factor
     */
    public void setDikelThis(boolean t) {
        this.dikelThis = t;
    }

    public boolean getDikelThis() {
        return this.dikelThis;
    }

    public String dikelThisTipText() {
        return "Activate attractive neignbors for the winner neuron.";
    }

}
