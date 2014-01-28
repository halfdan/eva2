package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.util.annotation.Description;

/**
 * Weierstrass-Mandelbrot Fractal Function
 */
@Description("Weierstrass-Mandelbrot Fractal Function")
public class F10Problem extends AbstractProblemDoubleOffset implements InterfaceMultimodalProblem, java.io.Serializable {

    private double d = 1.5;
    private double b = 2.3;
    private int iterations = 20;

    public F10Problem() {
        this.template = new ESIndividualDoubleData();
    }

    public F10Problem(F10Problem b) {
        super(b);
        this.d = b.d;
        this.b = b.b;
        this.iterations = b.iterations;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F10Problem(this);
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
        double c1 = this.calculateC(1);
        result[0] = yOffset;
        for (int i = 0; i < x.length - 1; i++) {
            double xi = x[i] - xOffset;
            result[0] += ((this.calculateC(xi)) / (c1 * Math.pow(Math.abs(xi), 2 - this.d))) + Math.pow(xi, 2) - 1;
        }
        return result;
    }

    private double calculateC(double x) {
        double result = 0;

        for (int i = -this.iterations; i < this.iterations + 1; i++) {
            result += (1 - Math.cos(Math.pow(this.b, i) * x)) / (Math.pow(this.b, (2 - this.d) * i));
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

        result += "F10 Weierstrass-Mandelbrot Fractal Function:\n";
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
        return "F10 Problem";
    }

    /**
     * This method allows you to set/get d.
     *
     * @param d The d.
     */
    public void setD(double d) {
        if (d < 1) {
            d = 1;
        }
        if (d > 2) {
            d = 2;
        }
        this.d = d;
    }

    public double getD() {
        return this.d;
    }

    public String dTipText() {
        return "Set 1 < D < 2.";
    }

    /**
     * This method allows you to set/get b
     *
     * @param b The b.
     */
    public void setb(double b) {
        if (b < 1.000001) {
            b = 1.000001;
        }
        this.b = b;
    }

    public double getb() {
        return this.b;
    }

    public String bTipText() {
        return "Choose b > 1.";
    }

    /**
     * This method allows you to set/get Iterations
     *
     * @param iters The Iterations.
     */
    public void setIterations(int iters) {
        if (iters < 2) {
            iters = 2;
        }
        this.iterations = iters;
    }

    public int getIterations() {
        return this.iterations;
    }

    public String iterationsTipText() {
        return "Choose the number of iterations per evaluation.";
    }
}