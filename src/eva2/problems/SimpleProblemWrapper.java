package eva2.problems;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.*;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;
import eva2.problems.simple.InterfaceSimpleProblem;
import eva2.problems.simple.SimpleF1;
import eva2.problems.simple.SimpleProblemBinary;
import eva2.problems.simple.SimpleProblemDouble;

import java.util.BitSet;

@Description("Wrapping simple problem implementations.")
public class SimpleProblemWrapper extends AbstractOptimizationProblem {
    InterfaceSimpleProblem<?> simProb = new SimpleF1();
    protected double defaultRange = 10;
    protected double noise = 0;
    private int repaintMinWait = 20;
    private int repaintCnt = 0;
    transient Plot plot = null;
    transient AbstractEAIndividual bestIndy = null;
    String plotFunc = "plotBest";
    transient Class[] plotFuncSig = new Class[]{Plot.class, AbstractEAIndividual.class};
    transient private boolean resetTemplate = true;

    public SimpleProblemWrapper() {
        plot = null;
        initTemplate();
    }

    public SimpleProblemWrapper(SimpleProblemWrapper other) {
        other.defaultRange = defaultRange;
        other.noise = noise;
        // warning! this does no deep copy!
        setSimpleProblem(other.simProb);
    }

    @Override
    public Object clone() {
        return new SimpleProblemWrapper(this);
    }

    @Override
    public void evaluate(AbstractEAIndividual individual) {
        if (simProb instanceof SimpleProblemDouble) {
            double[] x;
            double[] fitness;

            x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
            System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
            // evaluate the vector
            fitness = ((SimpleProblemDouble) simProb).evaluate(x);
            // if indicated, add Gaussian noise
            if (noise != 0) {
                RNG.addNoise(fitness, noise);
            }
            // set the fitness
            individual.setFitness(fitness);
        } else if (simProb instanceof SimpleProblemBinary) {
            BitSet tmpBitSet;
            double[] result;

            tmpBitSet = ((InterfaceDataTypeBinary) individual).getBinaryData();
            // evaluate the fitness
            result = ((SimpleProblemBinary) simProb).evaluate(tmpBitSet);
            // set the fitness
            individual.setFitness(result);
        } else {
            System.err.println("Error in SimpleProblemWrapper: " + simProb.getClass().getName() + " is unknown type!");
        }
    }

    @Override
    public void evaluatePopulationStart(Population population) {
        if (plot != null && (!plot.isValid())) {
            openPlot();
        }
    }

    @Override
    public void evaluatePopulationEnd(Population population) {
        super.evaluatePopulationEnd(population);
        repaintCnt += population.size();
        if (plot != null) {
            if (repaintCnt >= repaintMinWait) { // dont repaint always for small pops
                if ((bestIndy == null) || (population.getBestEAIndividual().isDominant(bestIndy.getFitness()))) {
                    // only paint improvement
                    bestIndy = population.getBestEAIndividual();
                    Object[] args = new Object[2];
                    args[0] = plot;
                    args[1] = bestIndy;
                    BeanInspector.callIfAvailable(simProb, plotFunc, args);
                }
                repaintCnt = 0;
            }
        }
    }

    @Override
    public void initializePopulation(Population population) {
        initTemplate();
        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    @Override
    public void initializeProblem() {
        bestIndy = null;
        initTemplate();
        setSimpleProblem(getSimpleProblem()); // possibly create plot
        BeanInspector.callIfAvailable(simProb, "initializeProblem", null);    // possibly call initializeProblem of subproblem
    }

    protected void initTemplate() {
        if (resetTemplate) {
            if (simProb instanceof SimpleProblemDouble) {
                this.template = new ESIndividualDoubleData();
            } else if (simProb instanceof SimpleProblemBinary) {
                this.template = new GAIndividualBinaryData();
            }
        }
        if (template instanceof InterfaceDataTypeDouble) {
            ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(simProb.getProblemDimension());
            ((InterfaceDataTypeDouble) this.template).setDoubleRange(makeRange());
        } else if (template instanceof InterfaceDataTypeBinary) {
            ((InterfaceDataTypeBinary) this.template).setBinaryDataLength(simProb.getProblemDimension());
        } else {
            System.err.println("Individual type not valid!");
        }
    }

    protected double[][] makeRange() {
        double[][] range = new double[simProb.getProblemDimension()][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = getRangeLowerBound(i);
            range[i][1] = getRangeUpperBound(i);
        }
        return range;
    }

    protected double getRangeLowerBound(int dim) {
        return -defaultRange;
    }

    protected double getRangeUpperBound(int dim) {
        return defaultRange;
    }

    /**
     * @return the simProb
     */
    public InterfaceSimpleProblem<?> getSimpleProblem() {
        return simProb;
    }

    private void openPlot() {
        plot = new Plot("SimpleProblemWrapper", "x", "y", true);
    }

    /**
     * @param simProb the simProb to set
     */
    public void setSimpleProblem(InterfaceSimpleProblem<?> simProb) {
        this.simProb = simProb;
        initTemplate();
        GenericObjectEditor.setShowProperty(getClass(), "noise", (simProb instanceof SimpleProblemDouble));
        GenericObjectEditor.setShowProperty(getClass(), "defaultRange", (simProb instanceof SimpleProblemDouble));
        if (BeanInspector.hasMethod(simProb, plotFunc, plotFuncSig) != null) {
            if (plot == null) {
                openPlot();
            } else {
                if (!plot.isValid()) {
                    plot.dispose();
                    openPlot();
                } else {
                    plot.clearAll();
                }
            }
        } else if (plot != null) {
            plot.dispose();
            plot = null;
        }
    }

    /**
     *
     */
    public String simpleProblemTipText() {
        return "Set the simple problem class which is to be optimized";
    }

    /**
     * This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     *
     * @param noise The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) {
            noise = 0;
        }
        this.noise = noise;
    }

    public double getNoise() {
        return this.noise;
    }

    public String noiseTipText() {
        return "Gaussian noise level on the fitness value.";
    }


    /**
     * A (symmetric) absolute range limit.
     *
     * @return value of the absolute range limit
     */
    public double getDefaultRange() {
        return defaultRange;
    }

    /**
     * Set a (symmetric) absolute range limit.
     *
     * @param defaultRange
     */
    public void setDefaultRange(double defaultRange) {
        this.defaultRange = defaultRange;
        initTemplate();
    }

    public String defaultRangeTipText() {
        return "Absolute limit for the symmetric range in any dimension";
    }

    /**
     * Take care that all properties which may be hidden (and currently are) send a "hide" message to the Java Bean properties.
     * This is called by PropertySheetPanel in use with the GenericObjectEditor.
     */
    public void hideHideable() {
        setSimpleProblem(getSimpleProblem());
    }

    public void setIndividualTemplate(AbstractEAIndividual indy) {
        resetTemplate = false;
        template = indy;
    }

    @Override
    public String individualTemplateTipText() {
        return "Set the individual properties for the optimization";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("A wrapped simple problem based on ");
        sb.append(simProb.getClass().getName());
        sb.append(", Dimension   : ");
        sb.append(simProb.getProblemDimension());
        return sb.toString();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "SimpleProblemWrapper";
    }

    public String[] getGOEPropertyUpdateLinks() {
        return new String[]{"simpleProblem"};
    }
}
