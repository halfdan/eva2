package eva2.problems;

import eva2.gui.plot.Plot;
import eva2.optimization.individuals.*;
import eva2.optimization.individuals.codings.gp.*;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.regression.InterfaceRegressionFunction;
import eva2.problems.regression.RFKoza_GPI_7_3;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.EVAERROR;
import eva2.tools.ToolBox;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 */
@Description("The task is to infer the equation of a system that can only be observed at a number of checkpoints.")
public class PSymbolicRegression extends AbstractOptimizationProblem implements InterfaceProgramProblem, InterfaceAdditionalPopulationInformer, Serializable {

    private double[] x = new double[1];
    private int numberOfConstants = 3;
    private double lowerBound = -1;
    private double upperBound = 1;
    private int numberOfCheckPoints = 20;
    transient private InterfaceRegressionFunction targetFunction = new RFKoza_GPI_7_3();
    private double[] constants = new double[numberOfConstants];
    private boolean useInnerConst = false;
    private boolean useLocalHillClimbing = false;
    private GPArea gpArea = new GPArea();
    protected AbstractEAIndividual overallBestIndividuum = null;
    protected double noise = 0.0;

    // This is graphics stuff
    transient private Plot plot;
    private boolean show = false;

    public PSymbolicRegression() {
        this.template = new GPIndividualProgramData();
        this.initializeProblem();
        this.compileArea();
    }

    public PSymbolicRegression(PSymbolicRegression b) {
        //AbstractOptimizationProblem
        if (b.template != null) {
            this.template = (AbstractEAIndividual) b.template.clone();
        }
        //F1Problem
        if (b.overallBestIndividuum != null) {
            this.overallBestIndividuum = (AbstractEAIndividual) b.overallBestIndividuum.clone();
        }
        if (b.gpArea != null) {
            this.gpArea = (GPArea) b.gpArea.clone();
        }
        if (b.targetFunction != null) {
            this.targetFunction = (InterfaceRegressionFunction) b.targetFunction.clone();
        }
        if (b.x != null) {
            this.x = new double[b.x.length];
            for (int i = 0; i < this.x.length; i++) {
                this.x[i] = b.x[i];
            }
        }
        if (b.constants != null) {
            this.constants = new double[b.constants.length];
            for (int i = 0; i < this.constants.length; i++) {
                this.constants[i] = b.constants[i];
            }
        }
        this.noise = b.noise;
        this.useInnerConst = b.useInnerConst;
        this.useLocalHillClimbing = b.useLocalHillClimbing;
        this.numberOfConstants = b.numberOfConstants;
        this.lowerBound = b.lowerBound;
        this.upperBound = b.upperBound;
        this.numberOfCheckPoints = b.numberOfCheckPoints;
        this.lowerBound = b.lowerBound;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new PSymbolicRegression(this);
    }

    /**
     * This method inits the Problem to log multiruns
     */
    @Override
    public void initializeProblem() {
        if (targetFunction == null) {
            targetFunction = new RFKoza_GPI_7_3();
        }
        this.overallBestIndividuum = null;
        this.constants = new double[this.numberOfConstants];
        for (int i = 0; i < this.constants.length; i++) {
            this.constants[i] = RNG.randomDouble(-10, 10);
        }
    }

    /**
     * This method compiles the area. Only to be called once in the constructor
     * to build the basic function set or if the number of constants etc. change.
     */
    private void compileArea() {
        // unfortunately this must be cloned or the GUI wont update.
        GPArea oldArea = gpArea;
        gpArea = new GPArea();

        if (gpArea.isEmpty()) {
            this.gpArea.add2CompleteList(new GPNodeOne());
            this.gpArea.add2CompleteList(new GPNodePi(), false);
            this.gpArea.add2CompleteList(new GPNodeAdd());
            this.gpArea.add2CompleteList(new GPNodeSub());
            this.gpArea.add2CompleteList(new GPNodeDiv());
            this.gpArea.add2CompleteList(new GPNodeMult());
            this.gpArea.add2CompleteList(new GPNodeAbs(), false);
            this.gpArea.add2CompleteList(new GPNodeSin(), false);
            this.gpArea.add2CompleteList(new GPNodeCos(), false);
            this.gpArea.add2CompleteList(new GPNodeExp(), false);
            this.gpArea.add2CompleteList(new GPNodeSqrt(), false);
            this.gpArea.add2CompleteList(new GPNodePow2(), false);
            this.gpArea.add2CompleteList(new GPNodePow3(), false);
            for (int i = 0; i < this.x.length; i++) {
                this.gpArea.add2CompleteList(new GPNodeInput("X" + i));
            }
            for (int i = 0; i < this.constants.length; i++) {
                this.gpArea.add2CompleteList(new GPNodeInput("C" + i), false);
            }
        }
        if ((oldArea != null) && (oldArea.getBlackList() != null) && (oldArea.getBlackList().size() == gpArea.getBlackList().size())) {
            gpArea.SetBlackList(oldArea.getBlackList());
        }
        this.gpArea.compileReducedList();
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    @Override
    public void initializePopulation(Population population) {
        initPopulation(population, this, useInnerConst, numberOfConstants);
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    public static void initPopulation(Population pop, InterfaceProgramProblem prob, boolean useInnerConsts, int numConsts) {
        AbstractEAIndividual template;

        template = ((AbstractOptimizationProblem) prob).getIndividualTemplate();
        GPArea tmpArea[] = new GPArea[1];
        tmpArea[0] = prob.getArea();
        ((InterfaceDataTypeProgram) template).setProgramDataLength(1);
        ((InterfaceDataTypeProgram) template).SetFunctionArea(tmpArea);
        if ((template instanceof GAPIndividualProgramData) && useInnerConsts) {
            ((GAPIndividualProgramData) template).setDoubleDataLength(numConsts);
        }
        AbstractOptimizationProblem.defaultInitPopulation(pop, template, prob);
    }

    /**
     * This method initialize the enviroment panel if necessary.
     */
    private void initEnvironmentPanel() {
        if (this.plot == null) {
            this.plot = new Plot("Symbolic Regression", "x", "y", true);
        }
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
            if ((this.useLocalHillClimbing) && (tmpIndy instanceof GAPIndividualProgramData)) {
                AbstractEAIndividual tmpBestConst = (AbstractEAIndividual) ((GAPIndividualProgramData) tmpIndy).getNumbers();
                AbstractEAIndividual tmpConst;
                this.evaluate(tmpIndy);
                tmpBestConst.SetFitness(0, tmpIndy.getFitness(0));
                population.incrFunctionCalls();
                for (int j = 0; j < 10; j++) {
                    tmpConst = (AbstractEAIndividual) tmpBestConst.clone();
                    tmpConst.mutate();
                    ((GAPIndividualProgramData) tmpIndy).setNumbers((InterfaceDataTypeDouble) tmpConst);
                    this.evaluate(tmpIndy);
                    tmpConst.SetFitness(0, tmpIndy.getFitness(0));
                    population.incrFunctionCalls();
                    if (tmpBestConst.getFitness(0) > tmpConst.getFitness(0)) {
                        tmpBestConst = (AbstractEAIndividual) tmpConst.clone();
                    }
                }
                ((GAPIndividualProgramData) tmpIndy).setNumbers((InterfaceDataTypeDouble) tmpBestConst);
                tmpIndy.SetFitness(0, tmpBestConst.getFitness(0));
            } else {
                if (useLocalHillClimbing) {
                    EVAERROR.errorMsgOnce("Error: local hill climbing only works on GAPIndividualProgramData individuals!");
                }
                this.evaluate(tmpIndy);
                population.incrFunctionCalls();
            }
        }
        evaluatePopulationEnd(population);
    }

    /**
     * This method evaluates a single individual and sets the fitness values. In this case,
     * the averaged square error between represented function and target function per check point is calculated.
     *
     * @param individual The individual that is to be evaluated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        InterfaceProgram program;
        double fitness = 0, tmpValue;
        InterfaceDataTypeProgram tmpIndy;

        tmpIndy = (InterfaceDataTypeProgram) individual;
        program = tmpIndy.getProgramData()[0];
        if ((tmpIndy instanceof GAPIndividualProgramData) && (this.useInnerConst)) {
            this.constants = ((GAPIndividualProgramData) tmpIndy).getDoubleData();
        }
        fitness = 0;

        for (int j = 0; j < this.numberOfCheckPoints; j++) {
            setCheckPoint(x, j);
            tmpValue = ((Double) program.evaluate(this)).doubleValue();
            fitness += Math.pow((this.targetFunction.evaluateFunction(this.x) - ((Double) program.evaluate(this)).doubleValue()), 2);
        }

        fitness /= (double) this.numberOfCheckPoints;
        // add noise to the fitness
        fitness += RNG.gaussianDouble(this.noise);
        // set the fitness of the individual
        individual.SetFitness(0, fitness);
        if ((this.plot != null) && (this.plot.getFunctionArea().getContainerSize() == 0)) {
            this.overallBestIndividuum = null;
        }
        if ((this.overallBestIndividuum == null) || (this.overallBestIndividuum.getFitness(0) > individual.getFitness(0))) {
            this.overallBestIndividuum = (AbstractEAIndividual) individual.clone();
            if (this.show) {
                if (plot == null) {
                    this.initEnvironmentPanel();
                }
                this.plot.clearAll();
                program = ((InterfaceDataTypeProgram) this.overallBestIndividuum).getProgramData()[0];
                for (int i = 0; i < this.numberOfCheckPoints; i++) {
                    setCheckPoint(x, i);
                    tmpValue = ((Double) program.evaluate(this)).doubleValue();
                    this.plot.setConnectedPoint(this.x[0], tmpValue, 0);
                    tmpValue = this.targetFunction.evaluateFunction(this.x);
                    this.plot.setConnectedPoint(this.x[0], tmpValue, 1);
                    this.plot.setInfoString(1, program.getStringRepresentation(), 1.0f);
                }
            }
        }
    }

    /**
     * Select a test point - TODO this btw only makes much sense in 1D.
     *
     * @param x
     * @param j
     */
    private void setCheckPoint(double[] x, int j) {
        for (int i = 0; i < x.length; i++) {
            x[i] = this.lowerBound + (j * (this.upperBound - this.lowerBound) / (this.numberOfCheckPoints - 1));
        }
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "Symbolic Regression Problem";
        return result;
    }

    /** These methods are for the InterfaceProgramProblem and allow the program to acess to an
     * artificial environment, e.g. the Artifical Ant World or the x values for Symoblic Regression.
     */
    /**
     * This method allows a GP program to sense the environment, e.g.
     * input values, current time etc
     *
     * @param sensor The identifier for the sensor.
     * @return Sensor value
     */
    @Override
    public Object getSensorValue(String sensor) {
        return PSymbolicRegression.getSensorValue(sensor, x, constants);
//        for (int i = 0; i < this.x.length; i++) if (sensor.equalsIgnoreCase("X"+i)) return new Double(this.x[i]);
//        for (int i = 0; i < this.c.length; i++) if (sensor.equalsIgnoreCase("C"+i)) return new Double(this.c[i]);
//        return new Double(0);
    }

    /**
     * This method allows a GP program to sense the environment, e.g.
     * input values, current time etc. Specialized to constants and variables
     * in arrays whose identifiers have the form "Xi" and "Ci".
     * For an identifier X only, the full vars array is returned. For an
     * identifier N only, the array dimension is returned.
     *
     * @param sensor The identifier for the sensor.
     * @param vars   array of x_i
     * @param consts array of c_i
     * @return Sensor value
     */
    public static Object getSensorValue(String sensor, double[] vars, double[] consts) {
        if (sensor.charAt(0) == 'X') {
            try {
                if (sensor.length() == 1) {
                    return vars;
                }
                int index = Integer.parseInt(sensor.substring(1));
                return new Double(vars[index]);
            } catch (Exception e) {
                System.err.println("Warning, unable to access " + sensor);
                return vars;
            }
        } else if (sensor.charAt(0) == 'C') {
            int index = Integer.parseInt(sensor.substring(1));
            return new Double(consts[index]);
        } else if (sensor.charAt(0) == 'N') {
            return (double) vars.length;
        } else {
            return new Double(0);
        }
//        for (int i = 0; i < this.x.length; i++) if (sensor.equalsIgnoreCase("X"+i)) return new Double(this.x[i]);
//        for (int i = 0; i < this.c.length; i++) if (sensor.equalsIgnoreCase("C"+i)) return new Double(this.c[i]);
//        return new Double(0);
    }

    /**
     * This method allows a GP program to act in the environment
     *
     * @param actuator  The identifier for the actuator.
     * @param parameter The actuator parameter.
     */
    @Override
    public void setActuatorValue(String actuator, Object parameter) {
        // no actuators in this problem
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
        return "Symbolic Regression problem";
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
     * This method toggles the use of inner constants.
     *
     * @param b The flag to use inner constants.
     */
    public void setUseInnerConst(boolean b) {
        this.useInnerConst = b;
    }

    public boolean getUseInnerConst() {
        return this.useInnerConst;
    }

    public String useInnerConstTipText() {
        return "Toggle the use of inner constants.";
    }

    /**
     * This method toggles the use of local hill climbing for inner constants.
     *
     * @param b The flag to use local hill climbing for inner constants.
     */
    public void setUseLocalHillClimbing(boolean b) {
        this.useLocalHillClimbing = b;
    }

    public boolean getUseLocalHillClimbing() {
        return this.useLocalHillClimbing;
    }

    public String useLocalHillClimbingTipText() {
        return "Toggle the use of local hill climbing for inner constants.";
    }

    /**
     * This method allows you to set the number of ephremial constants.
     *
     * @param b The new number of ephremial constants.
     */
    public void setNumberOfConstants(int b) {
        this.numberOfConstants = b;
        this.initializeProblem();
        gpArea.clear();
        this.compileArea();
    }

    public int getNumberOfConstants() {
        return this.numberOfConstants;
    }

    public String numberOfConstantsTipText() {
        return "Gives the number of ephremal constants.";
    }

    /**
     * This method allows you to set the number of check points.
     *
     * @param b The new number of check points.
     */
    public void setNumberOfCheckPoints(int b) {
        if (b < 0) {
            b = 1;
        }
        this.numberOfCheckPoints = b;
    }

    public int getNumberOfCheckPoints() {
        return this.numberOfCheckPoints;
    }

    public String numberOfCheckPointsTipText() {
        return "Choose the number of points where the GP have to compare to the target function.";
    }

    /**
     * This method allows you to toggle the use of the elements in the GPArea.
     *
     * @param i Number of maximal steps.
     */
    public void setArea(GPArea i) {
        this.gpArea = i;
        GPArea tmpArea[] = new GPArea[1];
        tmpArea[0] = this.gpArea;
        ((InterfaceDataTypeProgram) this.template).setProgramDataLength(1);
        ((InterfaceDataTypeProgram) this.template).SetFunctionArea(tmpArea);
    }

    @Override
    public GPArea getArea() {
        if (gpArea == null) {
            initializeProblem();
        }
        return this.gpArea;
    }

    public String areaTipText() {
        return "Select function set from the available area.";
    }

    /**
     * This method allows you to toggle path visualisation on and off.
     *
     * @param b True if the path is to be shown.
     */
    public void setShowResult(boolean b) {
        this.show = b;
        if (this.show) {
            this.initEnvironmentPanel();
        } else if (this.plot != null) {
            this.plot.dispose();
            this.plot = null;
        }
    }

    public boolean getShowResult() {
        return this.show;
    }

    public String showResultTipText() {
        return "Toggles the result visualisation on/or off.";
    }

    /**
     * This method allows you to choose the target function.
     *
     * @param b The target function.
     */
    public void setTargetFunction(InterfaceRegressionFunction b) {
        this.targetFunction = b;
    }

    public InterfaceRegressionFunction getTargetFunction() {
        if (targetFunction == null) {
            initializeProblem();
        }
        return this.targetFunction;
    }

    public String targetFunctionTipText() {
        return "Choose from the available target functions.";
    }

    /**
     * This method allows you to choose the EA individual
     *
     * @param indy The EAIndividual type
     */
    public void setGPIndividual(InterfaceDataTypeProgram indy) {
        this.template = (AbstractEAIndividual) indy;
    }

    public InterfaceDataTypeProgram getGPIndividual() {
        return (InterfaceDataTypeProgram) this.template;
    }

    public String GPIndividualTipText() {
        return "Modify the properties of the template GP individual such as maximum tree depth etc.";
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double mLowerBound) {
        lowerBound = mLowerBound;
    }

    public String lowerBoundTipText() {
        return "The lower bound of the 1D double interval where the target function is sampled.";
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double mUpperBound) {
        upperBound = mUpperBound;
    }

    public String upperBoundTipText() {
        return "The upper bound of the 1D double interval where the target function is sampled.";
    }

    public String[] customPropertyOrder() {
        return new String[]{"lowerBound", "upperBound"};
    }

    @Override
    public String[] getAdditionalDataHeader() {
        String[] superHd = super.getAdditionalDataHeader();
        return ToolBox.appendArrays(new String[]{"bestIndySize", "avgIndySize", "avgMaxIndyDepth"}, superHd);
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] superDat = super.getAdditionalDataValue(pop);
        return ToolBox.appendArrays(new Object[]{getBestIndySize(pop), getAvgIndySize(pop), getAvgIndyDepth(pop)}, superDat);
    }

    /**
     * Get the average tree depth of the given population (for program individuals).
     *
     * @param pop
     * @return
     * @see #getIndyDepth(AbstractEAIndividual)
     */
    public static double getAvgIndyDepth(PopulationInterface pop) {
        Population p = (Population) pop;
        double sum = 0;
        for (int i = 0; i < p.size(); i++) {
            sum += getIndyDepth(p.getEAIndividual(i));
        }
        return sum / p.size();
    }

    /**
     * Return the average number of nodes of program individuals.
     *
     * @param pop
     * @return
     * @see #getIndySize(AbstractEAIndividual)
     */
    public static double getAvgIndySize(PopulationInterface pop) {
        Population p = (Population) pop;
        double sum = 0;
        for (int i = 0; i < p.size(); i++) {
            sum += getIndySize(p.getEAIndividual(i));
        }
        return sum / p.size();
    }

    /**
     * Return the number of nodes of the best current individual if it represents a program,
     * otherwise zero.
     *
     * @param pop
     * @return
     */
    public static int getBestIndySize(PopulationInterface pop) {
        Population p = (Population) pop;
        AbstractEAIndividual indy = p.getBestEAIndividual();
        return getIndySize(indy);
    }

    /**
     * Return the number of nodes in an individual representing a program or null
     * if it is of an incompatible type.
     *
     * @param indy
     * @return
     */
    public static int getIndySize(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceDataTypeProgram) {
            InterfaceProgram prog = ((InterfaceDataTypeProgram) indy).getProgramDataWithoutUpdate()[0];
            if (prog instanceof AbstractGPNode) {
                AbstractGPNode gpNode = (AbstractGPNode) prog;
                return gpNode.getNumberOfNodes();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Return the maximal depth of an individual representing a program or null
     * if it is of an incompatible type.
     *
     * @param indy
     * @return
     */
    public static int getIndyDepth(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceDataTypeProgram) {
            InterfaceProgram prog = ((InterfaceDataTypeProgram) indy).getProgramDataWithoutUpdate()[0];
            if (prog instanceof AbstractGPNode) {
                AbstractGPNode gpNode = (AbstractGPNode) prog;
                int d = gpNode.getMaxDepth();
                //if (!gpNode.checkDepth(0)) {
                //	System.out.println(d + "\n" + gpNode.getStringRepresentation());
                //}
                return d;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Test method for an arbitrary string coded program.
     *
     * @param nodeStr
     * @return
     */
    public static double[] evalNodeString(String nodeStr) {
        AbstractGPNode node = AbstractGPNode.parseFromString(nodeStr);
        PSymbolicRegression regrProb = new PSymbolicRegression();
        GPIndividualProgramData indy = new GPIndividualProgramData();
        indy.setPGenotype(node, 0);
        regrProb.evaluate(indy);
        System.out.println("Evaluated individual: " + indy);
        return indy.getFitness();
    }

//	public static void main(String[] args) {
//		String bestStudentSol = "-(abs(abs(-(-(X0, -(-(X0, X0), X0)), sin(abs(-(-(X0, X0), X0)))))), -(-(sin(-(abs(X0), -(-(-(X0, X0), X0), abs(abs(X0))))), -(-(abs(abs(abs(X0))), -(-(-(X0, X0), X0), abs(-(X0,X0)))), sin(-(abs(X0), sin(sin(X0)))))), X0))";
////    	String bestStudentSol = "-(abs(-(sqrt(-(-(abs(1.0), X0), X0)), -(sqrt(-(sqrt(1.0), sqrt(pi))), X0))),-(-(sin(sqrt(-(-(sqrt(1.0), X0), pi))), abs(X0)), -(sqrt(pi), -(sqrt(sqrt(-(abs(1.0), X0))),X0))))";
//		evalNodeString(bestStudentSol);
//	}
}
