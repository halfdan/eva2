package eva2.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.GradientDescentAlgorithm;
import eva2.util.annotation.Description;

@Description(value ="Generalized Rosenbrock's function.")
@SuppressWarnings("unused")
public class F2Problem extends AbstractProblemDoubleOffset implements InterfaceLocalSearchable, InterfaceMultimodalProblem, java.io.Serializable, InterfaceFirstOrderDerivableProblem {

    private transient GradientDescentAlgorithm localSearchOptimizer = null;


    public F2Problem() {
        this.template = new ESIndividualDoubleData();
    }

    public F2Problem(F2Problem b) {
        super(b);
    }

    public F2Problem(int dim) {
        super(dim);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F2Problem(this);
    }

    /**
     * Ths method allows you to evaluate a double[] to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset;
        double xi, xii;
        for (int i = 0; i < x.length - 1; i++) {
            xi = x[i] - xOffset;
            xii = x[i + 1] - xOffset;
            result[0] += (100 * (xii - xi * xi) * (xii - xi * xi) + (xi - 1) * (xi - 1));
        }
        if (yOffset == 0 && (result[0] <= 0)) {
            result[0] = Math.sqrt(Double.MIN_VALUE);
        } // guard for plots in log scale
        return result;
    }

    @Override
    public double[] getFirstOrderGradients(double[] x) {
        x = rotateMaybe(x);
        int dim = x.length;
        double[] result = new double[dim];
        double xi, xii;

        for (int i = 0; i < dim - 1; i++) {
            xi = x[i] - xOffset;
            xii = x[i + 1] - xOffset;

            result[i] += 400 * xi * (xi * xi - xii) + 2 * xi - 2;
            result[i + 1] += -200 * (xi * xi - xii);
        }
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F2 Generalized Rosenbrock function:\n";
        result += "This problem has a deceptive optimum at (0,0,..), the true optimum is at (1,1,1,..).\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Generalized Rosenbrock";
    }

    @Override
    public void doLocalSearch(Population pop) {
        if (localSearchOptimizer == null) {
            initLS();
        }
        localSearchOptimizer.setPopulation(pop);
        localSearchOptimizer.optimize();
    }

    private void initLS() {
        localSearchOptimizer = new GradientDescentAlgorithm();
        localSearchOptimizer.setProblem(this);
        localSearchOptimizer.initialize();
    }

    @Override
    public double getLocalSearchStepFunctionCallEquivalent() {
        double cost = 1;
        if (this.localSearchOptimizer instanceof GradientDescentAlgorithm) {
            cost = localSearchOptimizer.getIterations();
        }
        return cost;
    }
}
