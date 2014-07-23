package eva2.problems;

import eva2.gui.PropertyFilePath;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.constraint.InterfaceConstraint;
import eva2.optimization.operator.moso.InterfaceMOSOConverter;
import eva2.optimization.operator.paretofrontmetrics.InterfaceParetoFrontMetric;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 */
@Description("T1 is to be minimized.")
public class TF1Problem extends AbstractMultiObjectiveOptimizationProblem implements java.io.Serializable {
    protected int problemDimension = 30;
    protected int outputDimension = 2;
    protected double noise = 0.0;
    protected double xOffset = 0.0;
    protected double yOffset = 0.0;
    protected boolean applyConstraints = false;

//    transient private GraphPointSet     mySet;

    public TF1Problem() {
        super(1.);
    }

    public TF1Problem(double borderHigh) {
        super(borderHigh);
    }

    public TF1Problem(TF1Problem b) {
        //AbstractOptimizationProblem
        if (b.template != null) {
            this.template = (AbstractEAIndividual) ((AbstractEAIndividual) b.template).clone();
        }
        //AbstractMultiObjectiveOptimizationProblem
        if (b.mosoConverter != null) {
            this.mosoConverter = (InterfaceMOSOConverter) b.mosoConverter.clone();
        }
        if (b.metric != null) {
            this.metric = (InterfaceParetoFrontMetric) b.metric.clone();
        }
        if (b.paretoFront != null) {
            this.paretoFront = (Population) b.paretoFront.clone();
        }
        if (b.border != null) {
            this.border = new double[b.border.length][2];
            for (int i = 0; i < this.border.length; i++) {
                this.border[i][0] = b.border[i][0];
                this.border[i][1] = b.border[i][1];
            }
        }
        if (b.areaConst4Parallelization != null) {
            this.areaConst4Parallelization = new ArrayList();
            for (int i = 0; i < b.areaConst4Parallelization.size(); i++) {
                this.areaConst4Parallelization.add(((InterfaceConstraint) b.areaConst4Parallelization.get(i)).clone());
            }
        }
        // TF1Problem
        this.applyConstraints = b.applyConstraints;
        this.problemDimension = b.problemDimension;
        this.outputDimension = b.outputDimension;
        this.noise = b.noise;
        this.xOffset = b.xOffset;
        this.yOffset = b.yOffset;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new TF1Problem(this);
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    @Override
    public void initializePopulation(Population population) {
        this.paretoFront = new Population();

        double[][] newRange = makeRange();

        ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(this.problemDimension);
        ((InterfaceDataTypeDouble) this.template).setDoubleRange(newRange);

        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    protected double[][] makeRange() {
        return makeRange(0, 1);
    }

    protected double[][] makeRange(double lower, double upper) {
        double[][] newRange = new double[this.problemDimension][2];
        for (int i = 0; i < this.problemDimension; i++) {
            newRange[i][0] = lower;
            newRange[i][1] = upper;
        }
        return newRange;
    }

    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evalutated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[] x;
        double[] fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        for (int i = 0; i < x.length; i++) {
            x[i] -= this.xOffset;
        }
        fitness = this.doEvaluation(x);
        for (int i = 0; i < fitness.length; i++) {
            // add noise to the fitness
            fitness[i] += RNG.gaussianDouble(this.noise);
            fitness[i] += this.yOffset;
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }
        if (this.applyConstraints) {
            if (fitness[0] > 0.5) {
                individual.addConstraintViolation(fitness[0] - 0.5);
            }
            if (x[1] > 0.1) {
                individual.addConstraintViolation(x[1] - 0.1);
            }
            if (x[2] > 0.1) {
                individual.addConstraintViolation(x[2] - 0.1);
            }
            if (x[3] > 0.1) {
                individual.addConstraintViolation(x[3] - 0.1);
            }
        }
        individual.checkAreaConst4Parallelization(this.areaConst4Parallelization);
    }

    /**
     * Ths method allows you to evaluate a simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[] result = new double[2];
        double g = 0;

        result[0] = x[0];
        g = this.g(x);
        result[1] = g * this.h(result[0], g);

        return result;
    }

    /**
     * The g function
     *
     * @param x The decision variables.
     * @return Objective variable.
     */
    protected double g(double[] x) {
        double result = 0;

        for (int i = 1; i < x.length; i++) {
            result += x[i];
        }
        result = result * 9 / (x.length - 1);
        result += 1;
        return result;
    }

    /**
     * The h function
     *
     * @param x The decision variables.
     * @return Objective variable.
     */
    protected double h(double x, double y) {
        double result = 0;

        result = 1 - Math.sqrt(Math.abs(x / y));
        return result;
    }

    @Override
    public void drawAdditionalData(Plot plot, Population pop, int index) {
        AbstractMultiObjectiveOptimizationProblem.drawWithConstraints(plot, pop, border, index);
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "";

        result += "T1 Problem:\n";
        result += "Here the individual codes a vector of real number x and T1(x)= x is to be minimized.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.noise + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

    public static void main(String[] args) {
        int points = 500;
        String base = System.getProperty("user.dir");
        String FS = System.getProperty("file.separator");
        PropertyFilePath fileOutPath = PropertyFilePath.getFilePathFromResource("MOPReference" + FS + "T1_" + points + ".txt");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileOutPath.getCompleteFilePath()));
        } catch (java.io.IOException ed) {
            System.out.println("Could not open " + fileOutPath.getCompleteFilePath());
            return;
        }
        TF1Problem problem = new TF1Problem();
        System.out.println("This method generates a reference set for the T1 problem with " + points + " sample points.");
        double ub = 1, lb = 0;
        double x1, x2;
        String tmpStr;
        tmpStr = "x1 \t x2";
        for (int i = 0; i < points + 1; i++) {
            x1 = (ub - lb) / ((double) points) * i;
            x2 = problem.h(x1, 1);
            tmpStr += "\n" + x1 + "\t" + x2;
        }
        try {
            writer.write(tmpStr);
            writer.close();
        } catch (java.io.IOException e) {
            System.out.println("DAMM IOException " + e);
        }
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
    @Override
    public String getName() {
        return "T1 Problem";
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
        return "Noise level on the fitness value.";
    }

    /**
     * This method allows you to set/get an offset for decision variables.
     *
     * @param XOffSet The offset for the decision variables.
     */
    public void setXOffSet(double XOffSet) {
        this.xOffset = XOffSet;
    }

    public double getXOffSet() {
        return this.xOffset;
    }

    public String xOffSetTipText() {
        return "Choose an offset for the decision variable.";
    }

    /**
     * This method allows you to set/get the offset for the
     * objective value.
     *
     * @param YOffSet The offset for the objective value.
     */
    public void setYOffSet(double YOffSet) {
        this.yOffset = YOffSet;
    }

    public double getYOffSet() {
        return this.yOffset;
    }

    public String yOffSetTipText() {
        return "Choose an offset for the objective value.";
    }

    /**
     * This method allows you to set the input dimension of the problem.
     *
     * @param d The number of input dimensions
     */
    public void setProblemDimension(int d) {
        this.problemDimension = d;
    }

    public int getProblemDimension() {
        return this.problemDimension;
    }

    public String problemDimensionTipText() {
        return "Length of the x vector at is to be optimized.";
    }

//    /** This method allows you to set the number of objective variables
//     * @param a The number of objective variables
//     */
//    public void setOutputDimension(int a) {
//        this.outputDimension = a;
//        this.border = new double[this.outputDimension][2];
//        for (int i = 0; i < this.border.length; i++) {
//            this.border[i][0] = 0;
//            this.border[i][1] = 5;
//        }
//    }
//    public int getOutputDimension() {
//        return this.outputDimension;
//    }
//    public String outputDimensionTipText() {
//        return "Number of objective variables.";
//    }

    /**
     * This method allows you to choose the EA individual
     *
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.template = (AbstractEAIndividual) indy;
    }

    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble) this.template;
    }

    /**
     * This method allows you to set a Multiobjective to
     * Singleobjective converter if you choose to.
     *
     * @param b The new MO2SO converter.
     */
    @Override
    public void setMOSOConverter(InterfaceMOSOConverter b) {
        this.mosoConverter = b;
        this.mosoConverter.setOutputDimension(this.outputDimension);
    }

    @Override
    public InterfaceMOSOConverter getMOSOConverter() {
        return this.mosoConverter;
    }

    @Override
    public String mOSOConverterTipText() {
        return "Choose a Multiobjective to Singleobjective converter.";
    }

//    /** This method allows you to toggel the application of constraints on the problem
//     * @param b     state.
//     */
//    public void setApplyConstraints(boolean b) {
//        this.applyConstraints = b;
//    }
//    public boolean getApplyConstraints() {
//        return this.applyConstraints;
//    }
//    public String applyConstraintsTipText() {
//        return "Toggle application of constraint (works only for T1).";
//    }

    /**
     * Since you can apply single objective optimization algorithms on
     * multi-objective problems, the problem needs a way to log the pareto-
     * front for such algorithms. This is especially the case for the
     * dynamically weighted fitness MOSO.
     *
     * @param pop The pareto-front archive.
     */
    public void setParetoFront(Population pop) {
        this.paretoFront = pop;
    }

    public Population getParetoFront() {
        return this.paretoFront;
    }

    public String paretoFrontTipText() {
        return "Choose the properties of the local log of the pareto-front.";
    }
}