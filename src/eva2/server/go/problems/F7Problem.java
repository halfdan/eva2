package eva2.server.go.problems;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;
import eva2.tools.SelectedTag;
import eva2.tools.Tag;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 13:23:43
 * To change this template use File | Settings | File Templates.
 */
public class F7Problem extends F1Problem implements java.io.Serializable {

    private double              m_t         = 250;
    private double              m_Change    = 4;
    protected SelectedTag       m_TimeIntervalType;
    private int                 m_CurrentTimeStamp;

    public F7Problem() {
        Tag[] tag = new Tag[2];
        tag[0] = new Tag(0, "Function Calls");
        tag[1] = new Tag(1, "Generation");
        this.m_TimeIntervalType    = new SelectedTag(0, tag);
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F7Problem(F7Problem b) {
        super(b);
        this.m_Change			= b.m_Change;
        this.m_t                = b.m_t;
        this.m_TimeIntervalType = (SelectedTag)b.m_TimeIntervalType.clone();
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F7Problem(this);
    }

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;
        
        evaluatePopulationStart(population);
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            if (this.m_TimeIntervalType.getSelectedTag().getID() == 0) {
                this.m_CurrentTimeStamp = population.getFunctionCalls();
            } else {
                this.m_CurrentTimeStamp = population.getGeneration();
            }
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
        evaluatePopulationEnd(population);
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        double[]        x;
        double[]        fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);

        for (int i = 0; i < x.length; i++) x[i] = x[i] - this.m_XOffSet;
        fitness = this.eval(x);
        for (int i = 0; i < fitness.length; i++) {
            // add noise to the fitness
            fitness[i] += RNG.gaussianDouble(this.getNoise());
            fitness[i] += this.m_YOffSet;
            // set the fitness of the individual
            individual.SetFitness(i, fitness[i]);
        }
        if (this.m_UseTestConstraint) {
            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
        }
//        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
//            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
//        }
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
        double[] result = new double[1];
        result[0]     = 0;
        if ((Math.floor(this.m_CurrentTimeStamp / this.m_t)%2) == 0) {
            for (int i = 0; i < x.length-1; i++) {
                result[0]  += Math.pow(x[i], 2);
            }
        } else {
            for (int i = 0; i < x.length-1; i++) {
                result[0]  += Math.pow(x[i]-this.m_Change, 2);
            }
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F7 Sphere Model, changing Environemt:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "F7 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Sphere Model, changing Environment.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void sett(double d) {
        if (d < 1) d = 1;
        this.m_t = d;
    }
    public double gett() {
        return this.m_t;
    }
    public String tTipText() {
        return "Set the time interval for environmental change.";
    }

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setChange(double d) {
        this.m_Change = d;
    }
    public double getChange() {
        return this.m_Change;
    }
    public String changeTipText() {
        return "Set the amount of environmental change (x[i]-b).";
    }

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setTimeIntervalType(SelectedTag d) {
        this.m_TimeIntervalType = d;
    }
    public SelectedTag getTimeIntervalType() {
        return this.m_TimeIntervalType;
    }
    public String timeIntervalTypeTipText() {
        return "Choose the timeinterval type.";
    }
}