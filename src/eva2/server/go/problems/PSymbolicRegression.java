package eva2.server.go.problems;

import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAPIndividualProgramData;
import eva2.server.go.individuals.GPIndividualProgramData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceDataTypeProgram;
import eva2.server.go.individuals.codings.gp.GPArea;
import eva2.server.go.individuals.codings.gp.GPNodeAdd;
import eva2.server.go.individuals.codings.gp.GPNodeCos;
import eva2.server.go.individuals.codings.gp.GPNodeDiv;
import eva2.server.go.individuals.codings.gp.GPNodeExp;
import eva2.server.go.individuals.codings.gp.GPNodeInput;
import eva2.server.go.individuals.codings.gp.GPNodeMult;
import eva2.server.go.individuals.codings.gp.GPNodePow2;
import eva2.server.go.individuals.codings.gp.GPNodePow3;
import eva2.server.go.individuals.codings.gp.GPNodeSin;
import eva2.server.go.individuals.codings.gp.GPNodeSqrt;
import eva2.server.go.individuals.codings.gp.GPNodeSub;
import eva2.server.go.individuals.codings.gp.InterfaceProgram;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.regression.InterfaceRegressionFunction;
import eva2.server.go.problems.regression.RFKoza_GPI_10_2;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.tools.RandomNumberGenerator;
import eva2.tools.EVAERROR;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:44:25
 * To change this template use Options | File Templates.
 */
public class PSymbolicRegression extends AbstractOptimizationProblem implements InterfaceProgramProblem, java.io.Serializable  {

    private double[]                                m_X                     = new double[1];
    private int                                     m_NumberOfConstants     = 10;
    private double                                  m_LowerBound            = 0;
    private double                                  m_UpperBound            = 10;
    private int                                     m_NumberOfCheckPoints   = 10;
    transient private InterfaceRegressionFunction   m_TargetFunction        = new RFKoza_GPI_10_2();
    private double[]                                m_C                     = new double[m_NumberOfConstants];
    private boolean                                 m_UseInnerConst         = false;
    private boolean                                 m_UseLocalHillClimbing  = false;
    transient private GPArea                        m_GPArea                = new GPArea();
    protected AbstractEAIndividual                  m_OverallBest           = null;
    protected double                                m_Noise                 = 0.0;

    // This is graphics stuff
    transient private Plot                          m_Plot;
    private boolean                                 m_Show = false;

    public PSymbolicRegression() {
        this.m_Template     = new GPIndividualProgramData();
        this.initProblem();
    }

    public PSymbolicRegression(PSymbolicRegression b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //F1Problem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        if (b.m_GPArea != null)
            this.m_GPArea           = (GPArea)b.m_GPArea.clone();
        if (b.m_TargetFunction != null)
            this.m_TargetFunction   = (InterfaceRegressionFunction)b.m_TargetFunction.clone();
        if (b.m_X != null) {
            this.m_X = new double[b.m_X.length];
            for (int i = 0; i < this.m_X.length; i++) this.m_X[i] = b.m_X[i];
        }
        if (b.m_C != null) {
            this.m_C = new double[b.m_C.length];
            for (int i = 0; i < this.m_C.length; i++) this.m_C[i] = b.m_C[i];
        }
        this.m_Noise                = b.m_Noise;
        this.m_UseInnerConst        = b.m_UseInnerConst;
        this.m_UseLocalHillClimbing = b.m_UseLocalHillClimbing;
        this.m_NumberOfConstants    = b.m_NumberOfConstants;
        this.m_LowerBound           = b.m_LowerBound;
        this.m_UpperBound           = b.m_UpperBound;
        this.m_NumberOfCheckPoints  = b.m_NumberOfCheckPoints;
        this.m_LowerBound           = b.m_LowerBound;
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new PSymbolicRegression(this);
    }

    /** This method inits the Problem to log multiruns
     */
    public void initProblem() {
        this.compileArea();
        if (m_TargetFunction == null) m_TargetFunction = new RFKoza_GPI_10_2();
        this.m_OverallBest  = null;
        this.m_C            = new double[this.m_NumberOfConstants];
        for (int i = 0; i < this.m_C.length; i++) this.m_C[i] = RandomNumberGenerator.randomDouble(-10, 10);
    }

    /** This method compiles the area
     */
    private void compileArea() {
        this.m_GPArea = new GPArea();

        this.m_GPArea.add2CompleteList(new GPNodeAdd());
        this.m_GPArea.add2CompleteList(new GPNodeSub());
        this.m_GPArea.add2CompleteList(new GPNodeDiv());
        this.m_GPArea.add2CompleteList(new GPNodeMult());
        this.m_GPArea.add2CompleteList(new GPNodeSin(), false);
        this.m_GPArea.add2CompleteList(new GPNodeCos(), false);
        this.m_GPArea.add2CompleteList(new GPNodeExp(), false);
        this.m_GPArea.add2CompleteList(new GPNodePow2(), false);
        this.m_GPArea.add2CompleteList(new GPNodePow3(), false);
        this.m_GPArea.add2CompleteList(new GPNodeSqrt(), false);
        for (int i = 0; i < this.m_X.length; i++) this.m_GPArea.add2CompleteList(new GPNodeInput("X"+i));
        for (int i = 0; i < this.m_C.length; i++) this.m_GPArea.add2CompleteList(new GPNodeInput("C"+i));
        this.m_GPArea.compileReducedList();
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        population.clear();

        GPArea tmpArea[]    = new GPArea[1];
        tmpArea[0]          = this.m_GPArea;
        ((InterfaceDataTypeProgram)this.m_Template).setProgramDataLength(1);
        ((InterfaceDataTypeProgram)this.m_Template).SetFunctionArea(tmpArea);
        if ((this.m_Template instanceof GAPIndividualProgramData) && (this.m_UseInnerConst)) {
            ((GAPIndividualProgramData)this.m_Template).setDoubleDataLength(this.m_NumberOfConstants);
        }
        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }
    /** This method init the enviroment panel if necessary.
     */
    private void initEnvironmentPanel() {
        if (this.m_Plot == null) this.m_Plot = new Plot("Symbolic Regression", "x", "y", true);
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
            if ((this.m_UseLocalHillClimbing) && (tmpIndy instanceof GAPIndividualProgramData)) {
            	AbstractEAIndividual tmpBestConst = (AbstractEAIndividual)((GAPIndividualProgramData)tmpIndy).getNumbers();
            	AbstractEAIndividual tmpConst;
            	this.evaluate(tmpIndy);
            	tmpBestConst.SetFitness(0, tmpIndy.getFitness(0));
            	population.incrFunctionCalls();
            	for (int j = 0; j < 10; j++) {
            		tmpConst = (AbstractEAIndividual)tmpBestConst.clone();
            		tmpConst.mutate();
            		((GAPIndividualProgramData)tmpIndy).setNumbers((InterfaceDataTypeDouble)tmpConst);
            		this.evaluate(tmpIndy);
            		tmpConst.SetFitness(0, tmpIndy.getFitness(0));
            		population.incrFunctionCalls();
            		if (tmpBestConst.getFitness(0) > tmpConst.getFitness(0)) tmpBestConst = (AbstractEAIndividual)tmpConst.clone();
            	}
            	((GAPIndividualProgramData)tmpIndy).setNumbers((InterfaceDataTypeDouble)tmpBestConst);
            	tmpIndy.SetFitness(0, tmpBestConst.getFitness(0));
            } else {
            	if (m_UseLocalHillClimbing) EVAERROR.errorMsgOnce("Error: local hill climbing only works on GAPIndividualProgramData individuals!");
                this.evaluate(tmpIndy);
                population.incrFunctionCalls();
            }
        }
        evaluatePopulationEnd(population);
    }

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual) {
        InterfaceProgram            program;
        double                      fitness = 0, tmpValue;
        InterfaceDataTypeProgram    tmpIndy;

        tmpIndy     = (InterfaceDataTypeProgram) individual;
        program     = tmpIndy.getProgramData()[0];
        if ((tmpIndy instanceof GAPIndividualProgramData) && (this.m_UseInnerConst))
            this.m_C = ((GAPIndividualProgramData)tmpIndy).getDoubleData();
        fitness     = 0;
        for (double i = 0; i < this.m_NumberOfCheckPoints; i++) {
            for (int j = 0; j < this.m_X.length; j++)
                this.m_X[j] = this.m_LowerBound +(i*(this.m_UpperBound-this.m_LowerBound)/this.m_NumberOfCheckPoints);
            tmpValue = ((Double)program.evaluate(this)).doubleValue();
            fitness += Math.pow((this.m_TargetFunction.evaulateFunction(this.m_X) - ((Double)program.evaluate(this)).doubleValue()), 2);
        }

        fitness = fitness / (double)this.m_NumberOfCheckPoints;
        // add noise to the fitness
        fitness += RandomNumberGenerator.gaussianDouble(this.m_Noise);
        // set the fitness of the individual
        individual.SetFitness(0, fitness);
        if ((this.m_Plot != null) && (this.m_Plot.getFunctionArea().getContainerSize() ==0)) this.m_OverallBest = null;
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
            if (this.m_Show) {
                this.m_Plot.clearAll();
                program     = ((InterfaceDataTypeProgram)this.m_OverallBest).getProgramData()[0];
                for (double i = 0; i < this.m_NumberOfCheckPoints; i++) {
                    for (int j = 0; j < this.m_X.length; j++) this.m_X[j] = i ;
                    tmpValue = ((Double)program.evaluate(this)).doubleValue();
                    this.m_Plot.setConnectedPoint(this.m_X[0], tmpValue, 0);
                    tmpValue = this.m_TargetFunction.evaulateFunction(this.m_X);
                    this.m_Plot.setConnectedPoint(this.m_X[0], tmpValue, 1);
                    this.m_Plot.setInfoString(1, program.getStringRepresentation(), 1.0f);
                }
            }
        }
    }

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "Symbolic Regression Problem";
        return result;
    }

    /** These methods are for the InterfaceProgramProblem and allow the program to acess to an
     * artificial environment, e.g. the Artifical Ant World or the x values for Symoblic Regression.
     */
    /** This method allows a GP program to sense the environment, e.g.
     * input values, current time etc
     * @param sensor    The identifier for the sensor.
     * @return Sensor value
     */
    public Object getSensorValue(String sensor) {
        for (int i = 0; i < this.m_X.length; i++) if (sensor.equalsIgnoreCase("X"+i)) return new Double(this.m_X[i]);
        for (int i = 0; i < this.m_C.length; i++) if (sensor.equalsIgnoreCase("C"+i)) return new Double(this.m_C[i]);
        return new Double(0);
    }

    /** This method allows a GP program to act in the environment
     * @param actuator      The identifier for the actuator.
     * @param parameter     The actuator parameter.
     */
    public void setActuatorValue(String actuator, Object parameter) {
        // no actuators in this problem
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Symbolic Regression problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The task is to infer the equation from a system that can only be observed";
    }

    /** This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     * @param noise     The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) noise = 0;
        this.m_Noise = noise;
    }
    public double getNoise() {
        return this.m_Noise;
    }
    public String noiseTipText() {
        return "Noise level on the fitness value.";
    }

    /** This method toggels the use of inner constants.
     * @param b     The flag to use inner constants.
     */
    public void setUseInnerConst(boolean b) {
        this.m_UseInnerConst = b;
    }
    public boolean getUseInnerConst() {
        return this.m_UseInnerConst;
    }
    public String useInnerConstTipText() {
        return "Toggel the use of inner constants.";
    }

    /** This method toggels the use of local hill climbing for inner constants.
     * @param b     The flag to use local hill climbing for inner constants.
     */
    public void setUseLocalHillClimbing(boolean b) {
        this.m_UseLocalHillClimbing = b;
    }
    public boolean getUseLocalHillClimbing() {
        return this.m_UseLocalHillClimbing;
    }
    public String useLocalHillClimbingTipText() {
        return "Toggel the use of local hill climbing for inner constants.";
    }

    /** This method allows you to set the number of ephremial constants.
     * @param b     The new number of ephremial constants.
     */
    public void setNumberOfConstants(int b) {
        this.m_NumberOfConstants = b;
        this.initProblem();
        this.compileArea();
    }
    public int getNumberOfConstants() {
        return this.m_NumberOfConstants;
    }
    public String numberOfConstantsTipText() {
        return "Gives the number of ephremal constants.";
    }

    /** This method allows you to set the number of check points.
     * @param b     The new number of check points.
     */
    public void setNumberOfCheckPoints(int b) {
        if (b < 0) b  = 1;
        this.m_NumberOfCheckPoints = b;
    }
    public int getNumberOfCheckPoints() {
        return this.m_NumberOfCheckPoints;
    }
    public String numberOfCheckPointsTipText() {
        return "Choose the number of points where the GP have to compare to the target function.";
    }

    /** This method allows you to toggle the use of the elements in the GPArea.
     * @param i     Number of maximal steps.
     */
    public void setArea(GPArea i) {
        this.m_GPArea       = i;
        GPArea tmpArea[]    = new GPArea[1];
        tmpArea[0]          = this.m_GPArea;
        ((InterfaceDataTypeProgram)this.m_Template).setProgramDataLength(1);
        ((InterfaceDataTypeProgram)this.m_Template).SetFunctionArea(tmpArea);
    }
    public GPArea getArea() {
        return this.m_GPArea;
    }
    public String areaTipText() {
        return "Select elements from the available area.";
    }

    /** This method allows you to toggle path visualisation on and off.
     * @param b     True if the path is to be shown.
     */
    public void setShowResult(boolean b) {
        this.m_Show = b;
        if (this.m_Show) this.initEnvironmentPanel();
        else if (this.m_Plot != null) {
            this.m_Plot.dispose();
            this.m_Plot = null;
        }
    }
    public boolean getShowResult() {
        return this.m_Show;
    }
    public String showResultTipText() {
        return "Toggles the result visualisation on/or off.";
    }

    /** This method allows you to choose the target function.
     * @param b     The target function.
     */
    public void setTargetFunction(InterfaceRegressionFunction b) {
        this.m_TargetFunction = b;
    }
    public InterfaceRegressionFunction getTargetFunction() {
        return this.m_TargetFunction;
    }
    public String targetFunctionTipText() {
        return "Choose from the available target functions.";
    }

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setGPIndividual(InterfaceDataTypeProgram indy) {
        this.m_Template = (AbstractEAIndividual) indy;
    }
    public InterfaceDataTypeProgram getGPIndividual() {
        return (InterfaceDataTypeProgram)this.m_Template;
    }
}
