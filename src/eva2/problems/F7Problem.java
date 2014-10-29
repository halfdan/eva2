package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.population.Population;
import eva2.tools.SelectedTag;
import eva2.tools.Tag;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 *
 */
@Description("Sphere Model, changing Environment.")
public class F7Problem extends AbstractProblemDoubleOffset implements Serializable {

    private double t = 250;
    private double change = 4;
    protected SelectedTag timeIntervalType;
    private int currentTimeStamp;

    public F7Problem() {
        Tag[] tag = new Tag[2];
        tag[0] = new Tag(0, "Function Calls");
        tag[1] = new Tag(1, "Generation");
        this.timeIntervalType = new SelectedTag(0, tag);
        this.template = new ESIndividualDoubleData();
    }

    public F7Problem(F7Problem b) {
        super(b);
        this.change = b.change;
        this.t = b.t;
        this.timeIntervalType = (SelectedTag) b.timeIntervalType.clone();
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
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            if (this.timeIntervalType.getSelectedTag().getID() == 0) {
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
    public void sett(double d) {
        if (d < 1) {
            d = 1;
        }
        this.t = d;
    }

    public double gett() {
        return this.t;
    }

    public String tTipText() {
        return "Set the time interval for environmental change.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setChange(double d) {
        this.change = d;
    }

    public double getChange() {
        return this.change;
    }

    public String changeTipText() {
        return "Set the amount of environmental change (x[i]-b).";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTimeIntervalType(SelectedTag d) {
        this.timeIntervalType = d;
    }

    public SelectedTag getTimeIntervalType() {
        return this.timeIntervalType;
    }

    public String timeIntervalTypeTipText() {
        return "Choose the timeinterval type.";
    }
}