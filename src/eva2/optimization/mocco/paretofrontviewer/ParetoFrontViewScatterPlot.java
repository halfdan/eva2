package eva2.optimization.mocco.paretofrontviewer;


import eva2.gui.plot.FunctionArea;
import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.InterfaceDPointWithContent;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.Chart2DDPointContentSelectable;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.ScaledBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */

class SimpleView extends JComponent implements InterfaceRefPointListener {

    private InterfaceRefPointListener refPointListener;
    private FunctionArea functionArea = null;
    private ScaledBorder areaBorder;
    ParetoFrontViewScatterPlot paretoFrontViewScatterPlot;
    int object1, object2;
    JLabel label;

    public SimpleView(ParetoFrontViewScatterPlot dad, int obj1, int obj2) {
        this.paretoFrontViewScatterPlot = dad;
        this.object1 = obj1;
        this.object2 = obj2;
        this.init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        if (this.object1 == this.object2) {
            this.label = new JLabel("" + ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives()[this.object1].getIdentName());
            this.add(this.label, BorderLayout.CENTER);
        } else {
            this.functionArea = new FunctionArea("?", "?");
            this.functionArea.setPreferredSize(new Dimension(200, 200));
            this.functionArea.setMinimumSize(new Dimension(200, 200));
            this.areaBorder = new ScaledBorder();
            this.areaBorder.xLabel = ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives()[this.object1].getIdentName();
            this.areaBorder.yLabel = ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives()[this.object2].getIdentName();
            this.functionArea.setBorder(this.areaBorder);
            this.functionArea.setBackground(Color.WHITE);
            this.add(this.functionArea, BorderLayout.CENTER);
        }
    }

    private double[] fetchPlotValueFor(AbstractEAIndividual indy) {
        double[] result = new double[2];
        if (this.paretoFrontViewScatterPlot.fitnessObjectiveComboBox.getSelectedIndex() == 0) {
            // now this is tricky isn't
            // first findout whether a objective is constraint only
            // and second since some objectives are ommited find the one fitness value that is the correct one
            InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
            if (tmpObj[this.object1].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                result[0] = 0;
            } else {
                int index = 0;
                for (int i = 0; i < this.object1; i++) {
                    if (!tmpObj[i].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                        index++;
                    }

                }
//                System.out.println("obj 1 is accessing fitness of " + tmpObj[this.object1].getIdentName() + " using " +index);
//                System.out.println(" fitness.length = " + indy.getFitness().length);
                result[0] = indy.getFitness(index);
            }
            if (tmpObj[this.object2].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                result[1] = 0;
            } else {
                int index = 0;
                for (int i = 0; i < this.object2; i++) {
                    if (!tmpObj[i].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                        index++;
                    }
                }
//                System.out.println("obj 2 is accessing fitness of " + tmpObj[this.object2].getIdentName() + " using " +index);
//                System.out.println(" fitness.length = " + indy.getFitness().length);
                result[1] = indy.getFitness(index);
            }
        } else {
            InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
            result[0] = ((Double) indy.getData(tmpObj[this.object1].getIdentName())).doubleValue();
            result[1] = ((Double) indy.getData(tmpObj[this.object2].getIdentName())).doubleValue();
        }
        return result;
    }

    public void updateView() {
//        InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem)this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
//        System.out.println("Plotting ("+this.object1+"/"+this.object2+") of " + tmpObj.length +" objectives.");
//        System.out.println(" ("+tmpObj[this.object1].getIdentName()+"/"+tmpObj[this.object2].getIdentName()+") which is of mode: ("+tmpObj[this.object1].getOptimizationMode()+"/"+tmpObj[this.object2].getOptimizationMode()+")");
        if (this.object1 == this.object2) {
            this.label.setText("" + ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives()[this.object1].getIdentName());
        } else {
            // plot the objectives
            Population pf = this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.paretoFront;
            double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
            this.functionArea.clearAll();
            this.functionArea.setBackground(Color.WHITE);
            GraphPointSet mySet;
            DPoint myPoint;
            //double[]                fitness;
            double[] plotValue;

            // now mark the ParetoFront
            if (pf.size() > 0) {
                DPoint point;
                DPointIcon icon;
                mySet = new GraphPointSet(1, this.functionArea);
                mySet.setConnectedMode(false);
                mySet.setColor(Color.BLACK);
                for (int i = 0; i < pf.size(); i++) {
                    plotValue = this.fetchPlotValueFor((AbstractEAIndividual) pf.get(i));
                    point = new DPoint(plotValue[0], plotValue[1]);
                    icon = new Chart2DDPointContentSelectable();
                    if (this.paretoFrontViewScatterPlot.moccoViewer.refSolutionSelectable) {
                        ((Chart2DDPointContentSelectable) icon).addSelectionListener(this.paretoFrontViewScatterPlot.moccoViewer);
                    }
                    ((InterfaceDPointWithContent) icon).setProblem(this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem);
                    ((InterfaceDPointWithContent) icon).setEAIndividual((AbstractEAIndividual) pf.get(i));
                    point.setIcon(icon);
                    mySet.addDPoint(point);
                    if (plotValue[0] < xmin) {
                        xmin = plotValue[0];
                    }
                    if (plotValue[0] > xmax) {
                        xmax = plotValue[0];
                    }
                    if (plotValue[1] < ymin) {
                        ymin = plotValue[1];
                    }
                    if (plotValue[1] > ymax) {
                        ymax = plotValue[1];
                    }
                }
                mySet = new GraphPointSet(0, this.functionArea);
                mySet.setConnectedMode(false);
                double xrange = (xmax - xmin), yrange = (ymax - ymin);
                if (xrange < 0.0000001) {
                    xrange = 0.0000001;
                }
                if (yrange < 0.0000001) {
                    yrange = 0.0000001;
                }
                if ((new Double(xrange)).isNaN()) {
                    mySet.addDPoint(0, 0);
                    mySet.addDPoint(1, 1);
                } else {
                    mySet.addDPoint(xmin - 0.1 * xrange, ymin - 0.1 * yrange);
                    mySet.addDPoint(xmin - 0.1 * xrange, ymin - 0.1 * yrange);
                    mySet.addDPoint(xmax + 0.1 * xrange, ymax + 0.1 * yrange);
                    mySet.addDPoint(xmax + 0.1 * xrange, ymax + 0.1 * yrange);
                }

                // now lets prepare the contraints or goals if any...
                if ((this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) &&
                        (this.paretoFrontViewScatterPlot.fitnessObjectiveComboBox.getSelectedIndex() == 1)) {
                    InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
                    if (tmp[this.object1].getOptimizationMode().equalsIgnoreCase("Objective")) {
                        double tmpD = tmp[this.object1].getConstraintGoal();
                        if (!(new Double(tmpD)).isNaN()) {
                            mySet = new GraphPointSet(500 + 1, this.functionArea);
                            mySet.setConnectedMode(true);
                            if ((tmpD > xmin - 0.1 * xrange) && (tmpD < xmax + 0.1 * xrange)) {
                                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                            }
                        }
                    }
                    if (tmp[this.object2].getOptimizationMode().equalsIgnoreCase("Objective")) {
                        double tmpD = tmp[this.object2].getConstraintGoal();
                        if (!(new Double(tmpD)).isNaN()) {
                            mySet = new GraphPointSet(500 + 2, this.functionArea);
                            mySet.setConnectedMode(true);
                            if ((tmpD > ymin - 0.1 * yrange) && (tmpD < ymax + 0.1 * yrange)) {
                                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                            }
                        }
                    }
                }
                // lets show a reference point if given...
                if ((this.paretoFrontViewScatterPlot.moccoViewer.referencePoint != null) && (this.paretoFrontViewScatterPlot.fitnessObjectiveComboBox.getSelectedIndex() == 0)) {
                    mySet = new GraphPointSet(1001, this.functionArea);
                    mySet.setConnectedMode(true);
                    mySet.setColor(Color.RED);
                    double tmpD = this.paretoFrontViewScatterPlot.moccoViewer.referencePoint[this.object1];
                    if ((tmpD > xmin - 0.1 * xrange) && (tmpD < xmax + 0.1 * xrange)) {
                        mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                        mySet.addDPoint(tmpD, ymin - 0.1 * yrange);
                        mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                        mySet.addDPoint(tmpD, ymax + 0.1 * yrange);
                    }
                    mySet = new GraphPointSet(1002, this.functionArea);
                    mySet.setConnectedMode(true);
                    mySet.setColor(Color.RED);
                    tmpD = this.paretoFrontViewScatterPlot.moccoViewer.referencePoint[this.object2];
                    if ((tmpD > ymin - 0.1 * yrange) && (tmpD < ymax + 0.1 * yrange)) {
                        mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                        mySet.addDPoint(xmin - 0.1 * xrange, tmpD);
                        mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                        mySet.addDPoint(xmax + 0.1 * xrange, tmpD);
                    }
                }
            }

            // lets prepare to reference point selection
            if (this.paretoFrontViewScatterPlot.moccoViewer.refPointSelectable) {
                this.functionArea.addRefPointSelectionListener(this);
            } else {
                this.removeRefPointSelectionListeners();
                this.functionArea.removeRefPointSelectionListeners();
                this.paretoFrontViewScatterPlot.removeRefPointSelectionListeners();
            }


        }
    }

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /**
     * This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     *
     * @param point The selected point, most likely 2d
     */
    @Override
    public void refPointGiven(double[] point) {
        if (this.paretoFrontViewScatterPlot.fitnessObjectiveComboBox.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.paretoFrontViewScatterPlot.moccoViewer.moccoStandalone.getMainFrame(),
                    "Reference point needs to be selected in fitness space!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            double[] tmpD = this.paretoFrontViewScatterPlot.moccoViewer.referencePoint;
            tmpD[this.object1] = point[0];
            tmpD[this.object1] = point[1];
            this.paretoFrontViewScatterPlot.moccoViewer.refPointGiven(tmpD);
        }
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.refPointListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.refPointListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.refPointListener = null;
    }
}

public class ParetoFrontViewScatterPlot extends JPanel implements InterfaceParetoFrontView {

    public MOCCOViewer moccoViewer;
    private JPanel mainPanel;
    private JPanel centerPanel;
    private JPanel topPanel;
    public JComboBox fitnessObjectiveComboBox;


    private InterfaceRefPointListener refPointListener;
    private SimpleView[][] simpleViews;

    public ParetoFrontViewScatterPlot(MOCCOViewer t) {
        this.moccoViewer = t;
        this.init();
    }

    private void init() {
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(this.mainPanel, BorderLayout.CENTER);
        this.centerPanel = new JPanel();
        this.mainPanel.add(new JScrollPane(this.centerPanel), BorderLayout.CENTER);
        this.topPanel = new JPanel();
        this.topPanel.setLayout(new GridLayout(1, 2));
        String[] tmp = new String[2];
        tmp[0] = "Fitness";
        tmp[1] = "Objective";
        this.fitnessObjectiveComboBox = new JComboBox(tmp);
        this.fitnessObjectiveComboBox.setSelectedIndex(0);
        this.fitnessObjectiveComboBox.addActionListener(this.jcomboboxListener);
        this.topPanel.add(new JLabel("Showing:"));
        this.topPanel.add(this.fitnessObjectiveComboBox);
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);
        this.updateView();
    }

    ActionListener jcomboboxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateView();
        }
    };

    private void makeScatter() {
        InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
        this.simpleViews = new SimpleView[tmp.length][tmp.length];
        this.centerPanel.removeAll();
        this.centerPanel.setLayout(new GridLayout(tmp.length, tmp.length));
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp.length; j++) {
                this.simpleViews[i][j] = new SimpleView(this, i, j);
                this.centerPanel.add(this.simpleViews[i][j]);
            }
        }
    }

    /**
     * This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    @Override
    public void updateView() {
        if (this.moccoViewer.moccoStandalone.state.currentProblem == null) {
            this.simpleViews = null;
            this.centerPanel.removeAll();
            this.centerPanel.add(new JLabel("no problem given"));
            this.validate();
            return;
        }

        // first set the names of the objectives
        InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem) this.moccoViewer.moccoStandalone.state.currentProblem).getProblemObjectives();
        if (this.simpleViews == null) {
            this.makeScatter();
        }
        if (this.simpleViews.length != tmp.length) {
            this.makeScatter();
        }
        if (this.simpleViews != null) {
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp.length; j++) {
                    this.simpleViews[i][j].updateView();
                }
            }
        }
        this.validate();
    }


    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /**
     * This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     *
     * @param point The selected point, most likely 2d
     */
    public void refPointGiven(double[] point) {
        if (this.fitnessObjectiveComboBox.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.moccoViewer.moccoStandalone.getMainFrame(),
                    "Reference point needs to be selected in fitness space!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            this.moccoViewer.refPointGiven(point);
        }
    }

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.refPointListener = a;
    }

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.refPointListener;
    }

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.refPointListener = null;
    }
}