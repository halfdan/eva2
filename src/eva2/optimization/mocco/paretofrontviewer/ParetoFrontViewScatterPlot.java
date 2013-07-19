package eva2.optimization.mocco.paretofrontviewer;


import eva2.gui.*;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.optimization.problems.InterfaceOptimizationObjective;
import eva2.tools.chart2d.Chart2DDPointContentSelectable;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIcon;
import eva2.tools.chart2d.ScaledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 11:16:30
 * To change this template use File | Settings | File Templates.
 */

class SimpleView extends JComponent implements InterfaceRefPointListener {

    private InterfaceRefPointListener   m_RefPointListener;
    private FunctionArea                m_Area = null;
    private ScaledBorder                m_AreaBorder;
    ParetoFrontViewScatterPlot          m_Dad;
    int                                 m_Obj1, m_Obj2;
    JLabel                              m_JLabel;

    public SimpleView(ParetoFrontViewScatterPlot dad, int obj1, int obj2) {
        this.m_Dad = dad;
        this.m_Obj1 = obj1;
        this.m_Obj2 = obj2;
        this.init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        if (this.m_Obj1 == this.m_Obj2) {
            this.m_JLabel = new JLabel(""+((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives()[this.m_Obj1].getIdentName());
            this.add(this.m_JLabel, BorderLayout.CENTER);
        } else {
            this.m_Area     = new FunctionArea("?","?");
            this.m_Area.setPreferredSize(new Dimension(200, 200));
            this.m_Area.setMinimumSize(new Dimension(200, 200));
            this.m_AreaBorder = new ScaledBorder();
            this.m_AreaBorder.x_label = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives()[this.m_Obj1].getIdentName();
            this.m_AreaBorder.y_label = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives()[this.m_Obj2].getIdentName();
            this.m_Area.setBorder(this.m_AreaBorder);
            this.m_Area.setBackground(Color.WHITE);
            this.add(this.m_Area, BorderLayout.CENTER);
        }
    }

    private double[] fetchPlotValueFor (AbstractEAIndividual indy) {
        double[] result = new double[2];
        if (this.m_Dad.m_JCFitObj.getSelectedIndex() == 0) {
            // now this is tricky isn't
            // first findout whether a objective is constraint only
            // and second since some objectives are ommited find the one fitness value that is the correct one
            InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            if (tmpObj[this.m_Obj1].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                result[0] = 0;
            } else {
                int index = 0;
                for (int i = 0; i < this.m_Obj1; i++) {
                    if (!tmpObj[i].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                        index++;
                    }

                }
//                System.out.println("obj 1 is accessing fitness of " + tmpObj[this.m_Obj1].getIdentName() + " using " +index);
//                System.out.println(" fitness.length = " + indy.getFitness().length);
                result[0] = indy.getFitness(index);
            }
            if (tmpObj[this.m_Obj2].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                result[1] = 0;
            } else {
                int index = 0;
                for (int i = 0; i < this.m_Obj2; i++) {
                    if (!tmpObj[i].getOptimizationMode().equalsIgnoreCase("Constraint")) {
                        index++;
                    }
                }
//                System.out.println("obj 2 is accessing fitness of " + tmpObj[this.m_Obj2].getIdentName() + " using " +index);
//                System.out.println(" fitness.length = " + indy.getFitness().length);
                result[1] = indy.getFitness(index);
            }
        } else {
            InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
            result[0] = ((Double)indy.getData(tmpObj[this.m_Obj1].getIdentName())).doubleValue();
            result[1] = ((Double)indy.getData(tmpObj[this.m_Obj2].getIdentName())).doubleValue();
        }
        return result;
    }

    public void updateView() {
//        InterfaceOptimizationObjective[] tmpObj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
//        System.out.println("Plotting ("+this.m_Obj1+"/"+this.m_Obj2+") of " + tmpObj.length +" objectives.");
//        System.out.println(" ("+tmpObj[this.m_Obj1].getIdentName()+"/"+tmpObj[this.m_Obj2].getIdentName()+") which is of mode: ("+tmpObj[this.m_Obj1].getOptimizationMode()+"/"+tmpObj[this.m_Obj2].getOptimizationMode()+")");
        if (this.m_Obj1 == this.m_Obj2) {
            this.m_JLabel.setText(""+((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives()[this.m_Obj1].getIdentName());
        } else {
            // plot the objectives
            Population pf = this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_ParetoFront;
            double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
            this.m_Area.clearAll();
            this.m_Area.setBackground(Color.WHITE);
            GraphPointSet           mySet;
            DPoint                  myPoint;
            //double[]                fitness;
            double[]                plotValue;

            // now mark the ParetoFront
            if (pf.size() > 0) {
                DPoint      point;
                DPointIcon  icon;
                mySet = new GraphPointSet(1, this.m_Area);
                mySet.setConnectedMode(false);
                mySet.setColor(Color.BLACK);
                for (int i = 0; i < pf.size(); i++) {
                    plotValue = this.fetchPlotValueFor((AbstractEAIndividual)pf.get(i));
                    point = new DPoint(plotValue[0], plotValue[1]);
                    icon = new Chart2DDPointContentSelectable();
                    if (this.m_Dad.m_MOCCOViewer.m_RefSolutionSelectable) {
                        ((Chart2DDPointContentSelectable)icon).addSelectionListener(this.m_Dad.m_MOCCOViewer);
                    }
                    ((InterfaceDPointWithContent)icon).setProblem(this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem);
                    ((InterfaceDPointWithContent)icon).setEAIndividual((AbstractEAIndividual)pf.get(i));
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
                mySet = new GraphPointSet(0, this.m_Area);
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
                    mySet.addDPoint(xmin - 0.1*xrange, ymin - 0.1*yrange);
                    mySet.addDPoint(xmin - 0.1*xrange, ymin - 0.1*yrange);
                    mySet.addDPoint(xmax + 0.1*xrange, ymax + 0.1*yrange);
                    mySet.addDPoint(xmax + 0.1*xrange, ymax + 0.1*yrange);
                }

                // now lets prepare the contraints or goals if any...
                if ((this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) &&
                    (this.m_Dad.m_JCFitObj.getSelectedIndex() == 1)) {
                    InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem)this.m_Dad.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
                    if (tmp[this.m_Obj1].getOptimizationMode().equalsIgnoreCase("Objective")) {
                        double tmpD = tmp[this.m_Obj1].getConstraintGoal();
                        if (!(new Double(tmpD)).isNaN()) {
                            mySet = new GraphPointSet(500+1, this.m_Area);
                            mySet.setConnectedMode(true);
                            if ((tmpD > xmin - 0.1*xrange) && (tmpD < xmax + 0.1*xrange)) {
                                mySet.addDPoint(tmpD, ymin - 0.1*yrange);
                                mySet.addDPoint(tmpD, ymin - 0.1*yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1*yrange);
                                mySet.addDPoint(tmpD, ymax + 0.1*yrange);
                            }
                        }
                    }
                    if (tmp[this.m_Obj2].getOptimizationMode().equalsIgnoreCase("Objective")) {
                        double tmpD = tmp[this.m_Obj2].getConstraintGoal();
                        if (!(new Double(tmpD)).isNaN()) {
                            mySet = new GraphPointSet(500+2, this.m_Area);
                            mySet.setConnectedMode(true);
                            if ((tmpD > ymin - 0.1*yrange) && (tmpD < ymax + 0.1*yrange)) {
                                mySet.addDPoint(xmin - 0.1*xrange, tmpD);
                                mySet.addDPoint(xmin - 0.1*xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1*xrange, tmpD);
                                mySet.addDPoint(xmax + 0.1*xrange, tmpD);
                            }
                        }
                    }
                }
                // lets show a reference point if given...
                if ((this.m_Dad.m_MOCCOViewer.m_ReferencePoint != null) && (this.m_Dad.m_JCFitObj.getSelectedIndex() == 0)) {
                    mySet = new GraphPointSet(1001, this.m_Area);
                    mySet.setConnectedMode(true);
                    mySet.setColor(Color.RED);
                    double tmpD = this.m_Dad.m_MOCCOViewer.m_ReferencePoint[this.m_Obj1];
                    if ((tmpD > xmin - 0.1*xrange) && (tmpD < xmax + 0.1*xrange)) {
                        mySet.addDPoint(tmpD, ymin - 0.1*yrange);
                        mySet.addDPoint(tmpD, ymin - 0.1*yrange);
                        mySet.addDPoint(tmpD, ymax + 0.1*yrange);
                        mySet.addDPoint(tmpD, ymax + 0.1*yrange);
                    }
                    mySet = new GraphPointSet(1002, this.m_Area);
                    mySet.setConnectedMode(true);
                    mySet.setColor(Color.RED);
                    tmpD = this.m_Dad.m_MOCCOViewer.m_ReferencePoint[this.m_Obj2];
                    if ((tmpD > ymin - 0.1*yrange) && (tmpD < ymax + 0.1*yrange)) {
                        mySet.addDPoint(xmin - 0.1*xrange, tmpD);
                        mySet.addDPoint(xmin - 0.1*xrange, tmpD);
                        mySet.addDPoint(xmax + 0.1*xrange, tmpD);
                        mySet.addDPoint(xmax + 0.1*xrange, tmpD);
                    }
                }
            }

            // lets prepare to reference point selection
            if (this.m_Dad.m_MOCCOViewer.m_RefPointSelectable) {
                this.m_Area.addRefPointSelectionListener(this);
            } else {
                this.removeRefPointSelectionListeners();
                this.m_Area.removeRefPointSelectionListeners();
                this.m_Dad.removeRefPointSelectionListeners();
            }


        }
    }

    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /** This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     * @param point  The selected point, most likely 2d
     */
    @Override
    public void refPointGiven(double[] point) {
        if (this.m_Dad.m_JCFitObj.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.m_Dad.m_MOCCOViewer.m_MOCCO.m_JFrame,
                "Reference point needs to be selected in fitness space!",
                "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            double[] tmpD = this.m_Dad.m_MOCCOViewer.m_ReferencePoint;
            tmpD[this.m_Obj1] = point[0];
            tmpD[this.m_Obj1] = point[1];
            this.m_Dad.m_MOCCOViewer.refPointGiven(tmpD);
        }
    }

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.m_RefPointListener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.m_RefPointListener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.m_RefPointListener = null;
    }
}

public class ParetoFrontViewScatterPlot extends JPanel implements InterfaceParetoFrontView {

    public MOCCOViewer      m_MOCCOViewer;
    private JPanel          m_JPMain;
    private JPanel          m_JPCenter;
    private JPanel          m_JPTop;
    public JComboBox        m_JCFitObj;


    private InterfaceRefPointListener           m_RefPointListener;
    private SimpleView[][]  m_Scatter;

    public ParetoFrontViewScatterPlot(MOCCOViewer t) {
        this.m_MOCCOViewer = t;
        this.init();
    }

    private void init() {
        this.m_JPMain = new JPanel();
        this.m_JPMain.setLayout(new BorderLayout());
        this.setLayout(new BorderLayout());
        this.add(this.m_JPMain, BorderLayout.CENTER);
        this.m_JPCenter = new JPanel();
        this.m_JPMain.add(new JScrollPane(this.m_JPCenter), BorderLayout.CENTER);
        this.m_JPTop = new JPanel();
        this.m_JPTop.setLayout(new GridLayout(1,2));
        String[] tmp = new String[2];
        tmp[0] = "Fitness";
        tmp[1] = "Objective";
        this.m_JCFitObj     = new JComboBox(tmp);
        this.m_JCFitObj.setSelectedIndex(0);
        this.m_JCFitObj.addActionListener(this.jcomboboxListener);
        this.m_JPTop.add(new JLabel("Showing:"));
        this.m_JPTop.add(this.m_JCFitObj);
        this.m_JPMain.add(this.m_JPTop, BorderLayout.NORTH);
        this.updateView();
    }
    ActionListener jcomboboxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateView();
        }
    };

    private void makeScatter() {
        InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem)this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
        this.m_Scatter = new SimpleView[tmp.length][tmp.length];
        this.m_JPCenter.removeAll();
        this.m_JPCenter.setLayout(new GridLayout(tmp.length, tmp.length));
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp.length; j++) {
                this.m_Scatter[i][j] = new SimpleView(this, i, j);
                this.m_JPCenter.add(this.m_Scatter[i][j]);
            }
        }
    }

    /** This method notifies the Pareto front view that
     * the data has changed most likely due to changes in
     * the problem definition
     */
    @Override
    public void updateView() {
        if (this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem == null) {
            this.m_Scatter = null;
            this.m_JPCenter.removeAll();
            this.m_JPCenter.add(new JLabel("no problem given"));
            this.validate();
            return;
        }

        // first set the names of the objectives
        InterfaceOptimizationObjective[] tmp = ((InterfaceMultiObjectiveDeNovoProblem)this.m_MOCCOViewer.m_MOCCO.m_State.m_CurrentProblem).getProblemObjectives();
        if (this.m_Scatter == null) {
            this.makeScatter();
        }
        if (this.m_Scatter.length != tmp.length) {
            this.makeScatter();
        }
        if (this.m_Scatter != null) {
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp.length; j++) {
                    this.m_Scatter[i][j].updateView();
                }
            }
        }
        this.validate();
    }



    /***************************************************************************
     *  InterfaceReferenceListener
     */

    /** This method will notify the listener that a point has been selected
     * It has to be noted though that at this point the reference point is not
     * a full vector since it is only 2d
     * @param point  The selected point, most likely 2d
     */
    public void refPointGiven(double[] point) {
        if (this.m_JCFitObj.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this.m_MOCCOViewer.m_MOCCO.m_JFrame,
                "Reference point needs to be selected in fitness space!",
                "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            this.m_MOCCOViewer.refPointGiven(point);
        }
    }

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.m_RefPointListener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.m_RefPointListener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.m_RefPointListener = null;
    }
}