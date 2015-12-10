package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * Sphere with changing environment
 */
@Description("Sphere Model, changing Environment.")
public class F7Problem extends AbstractProblemDoubleOffset implements Serializable {

    public enum TimeIntervalType {
        FunctionCalls, Generation
    }
    private double t = 250;
    private double change = 4;
    protected TimeIntervalType timeIntervalType;
    private int currentTimeStamp;

    public F7Problem() {
        this.timeIntervalType = TimeIntervalType.FunctionCalls;
        this.template = new ESIndividualDoubleData();
    }

    public F7Problem(F7Problem b) {
        super(b);
        this.change = b.change;
        this.t = b.t;
        this.timeIntervalType = b.timeIntervalType;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F7Problem(this);
    }

    /**
     * This method evaluates a given population and set the fitness values
     * accordingly
     *
     * @param population The population that is to be evaluated.
     */
    @Override
    public void evaluate(Population population) {
        AbstractEAIndividual tmpIndy;

        evaluatePopulationStart(population);
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = population.get(i);
            tmpIndy.resetConstraintViolation();
            if (this.timeIntervalType == TimeIntervalType.FunctionCalls) {
                this.currentTimeStamp = population.getFunctionCalls();
            } else {
                this.currentTimeStamp = population.getGeneration();
            }
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
        evaluatePopulationEnd(population);
    }

    /**
     * This method allows you to evaluate a double[] to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = yOffset;
        if ((Math.floor(this.currentTimeStamp / this.t) % 2) == 0) {
            for (int i = 0; i < x.length - 1; i++) {
                result[0] += Math.pow(x[i] - xOffset, 2);
            }
        } else {
            for (int i = 0; i < x.length - 1; i++) {
                result[0] += Math.pow(x[i] - xOffset - this.change, 2);
            }
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

        result += "F7 Sphere Model, changing Environemt:\n";
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
        return "Changing Sphere";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Set the time interval for environmental change.")
    public void sett(double d) {
        if (d < 1) {
            d = 1;
        }
        this.t = d;
    }

    public double gett() {
        return this.t;
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Set the amount of environmental change (x[i]-b).")
    public void setChange(double d) {
        this.change = d;
    }

    public double getChange() {
        return this.change;
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    @Parameter(description = "Choose the timeinterval type.")
    public void setTimeIntervalType(TimeIntervalType d) {
        this.timeIntervalType = d;
    }

    public TimeIntervalType getTimeIntervalType() {
        return this.timeIntervalType;
    }
}