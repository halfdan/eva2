package eva2.gui.plot;

import eva2.optimization.problems.Interface2DBorderProblem;
import eva2.optimization.problems.InterfaceFirstOrderDerivableProblem;
import eva2.tools.chart2d.DRectangle;
import eva2.tools.diagram.ColorBarCalculator;
import eva2.tools.math.Mathematics;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class TopoPlot extends Plot {
    Interface2DBorderProblem prob = null;
    double[][] range = null;
    boolean withGrads = false;
    private int gridx = 50;
    private int gridy = 50;
    int colorScale = ColorBarCalculator.BLUE_TO_RED;

    /**
     *
     */
    public TopoPlot(String PlotName, String xname, String yname) {
        super(PlotName, xname, yname, true);
        //if (TRACE) System.out.println("Constructor TopoPlot "+PlotName);
    }

    public TopoPlot(String PlotName, String xname, String yname, double[] a, double[] b) {
        super(PlotName, xname, yname, a, b);
        //if (TRACE) System.out.println("Constructor TopoPlot "+PlotName);
    }

    /**
     * Defines parameters used for drawing the topology.
     *
     * @param gridX       the x-resolution of the topology, higher value means higher resolution
     * @param gridY       the y-resolution of the topology, higher value means higher resolution
     * @param color_scale the topologies color coding. Values (0-3) are valid. @See ColorBarCalculator.
     */
    public void setParams(int gridX, int gridY, int color_scale) {
        if (gridX > internalFrame.getWidth()) {
            gridX = internalFrame.getWidth();
        }
        if (gridY > internalFrame.getHeight()) {
            gridY = internalFrame.getHeight();
        }
        gridx = gridX;
        gridy = gridY;
        colorScale = color_scale;
    }


    @Override
    protected void installButtons(JPanel buttonPan) {
        super.installButtons(buttonPan);
        // TODO this actually works, but it is horribly slow
//	  JButton refineButton = new JButton ("Refine");
//	  refineButton.setToolTipText("Refine the graph resolution");
//	  refineButton.addActionListener(new ActionListener() {
//		  public void actionPerformed(ActionEvent e) {
//			  gridx=(int)(Math.sqrt(2.)*gridx);
//			  gridy=(int)(Math.sqrt(2.)*gridy);
//			  setTopology(prob, range, withGrads);
//		  }
//	  });
//	  buttonPan.add(refineButton);
    }

    /**
     * Defines parameters used for drawing the topology.
     *
     * @param gridX the x-resolution of the topology, higher value means higher resolution
     * @param gridY the y-resolution of the topology, higher value means higher resolution
     */
    public void setParams(int gridX, int gridY) {
        setParams(gridX, gridY, colorScale);
    }

    /**
     * Defines the topology (by setting a specific problem) and draws the topology
     */
    public void setTopology(Interface2DBorderProblem problem) {
        setTopology(problem, problem.get2DBorder(), false);
    }

    /**
     * Defines the topology (by setting a specific problem) and draws the topology
     */
    public void setTopology(Interface2DBorderProblem problem, double[][] border, boolean withGradientsIfAvailable) {
        prob = problem;
        range = border;
        withGrads = withGradientsIfAvailable;
        double[] sizeXY = Mathematics.getAbsRange(border);
        double deltaX = sizeXY[0] / gridx;
        double deltaY = sizeXY[1] / gridy;
        double maxDeriv = 0;
        double[] pos = new double[2];
        boolean TRACEMETH = false;
        //double fitRange = java.lang.Math.abs(problem.getMinFitness()-problem.getMaxFitness() );
        double fitRange = 0, max = -Double.MAX_VALUE, min = Double.MAX_VALUE, tmp;
        for (int x = 0; x < gridx; x++) {
            for (int y = 0; y < gridy; y++) {
                pos[0] = border[0][0] + x * deltaX;
                pos[1] = border[1][0] + y * deltaY;
                tmp = (float) (problem.functionValue(pos));
                if (TRACEMETH) {
                    System.out.println(pos[0] + " " + pos[1] + " " + tmp);
                }
                if (tmp < min) {
                    min = tmp;
                }
                if (tmp > max) {
                    max = tmp;
                }
                if (withGradientsIfAvailable && (problem instanceof InterfaceFirstOrderDerivableProblem)) {
                    double[] deriv = ((InterfaceFirstOrderDerivableProblem) problem).getFirstOrderGradients(problem.project2DPoint(pos));
                    for (int i = 0; i < 2; i++) {
                        maxDeriv = Math.max(maxDeriv, Math.abs(deriv[i]));
                    } // maximum deriv of first 2 dims
                }

            } // for y
        } // for x
        fitRange = java.lang.Math.abs(max - min);
        ColorBarCalculator colorBar = new ColorBarCalculator(colorScale);

        internalFrame.setVisible(false);
        for (int x = 0; x < gridx; x++) {
            for (int y = 0; y < gridy; y++) {
                pos[0] = border[0][0] + x * deltaX;
                pos[1] = border[1][0] + y * deltaY;
                DRectangle rect = new DRectangle(pos[0] - (deltaX / 2), pos[1] - (deltaY / 2), deltaX, deltaY);
                Color color = new Color(colorBar.getRGB((float) ((problem.functionValue(pos) - min) / fitRange)));
                // Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
                //  	  Color color = new Color(colorBar.getRGB((float)(problem.functionValue(pos)/fitRange))); // Color color = new Color(255,(int)(problem.doEvaluation(pos)[0]/fitRange*255),(int)(problem.doEvaluation(pos)[0]/fitRange*255));
                rect.setColor(color);
                rect.setFillColor(color);
                plotArea.addDElement(rect);
            } // for y
        } // for x
        if (withGradientsIfAvailable && (problem instanceof InterfaceFirstOrderDerivableProblem)) {
            for (int x = 0; x < gridx; x++) {
                for (int y = 0; y < gridy; y++) {
                    pos[0] = border[0][0] + x * deltaX;
                    pos[1] = border[1][0] + y * deltaY;
                    double[] derivPos = ((InterfaceFirstOrderDerivableProblem) problem).getFirstOrderGradients(problem.project2DPoint(pos));
                    Mathematics.svDiv(1.1 * (2 * maxDeriv / Math.max(deltaX, deltaY)), derivPos, derivPos);
                    Mathematics.vvAdd(pos, derivPos, derivPos);
                    getFunctionArea().drawLine(pos, derivPos);
                    getFunctionArea().drawIcon(1, "", derivPos, 0);
                } // for y
            } // for x
        }
        internalFrame.setVisible(true);
    } // setTopology
} // class
