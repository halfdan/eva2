package eva2.server.go.problems;

import java.io.Serializable;

import eva2.gui.Plot;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAPIndividualProgramData;
import eva2.server.go.individuals.GPIndividualProgramData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceDataTypeProgram;
import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.individuals.codings.gp.GPArea;
import eva2.server.go.individuals.codings.gp.GPNodeAbs;
import eva2.server.go.individuals.codings.gp.GPNodeAdd;
import eva2.server.go.individuals.codings.gp.GPNodeCos;
import eva2.server.go.individuals.codings.gp.GPNodeDiv;
import eva2.server.go.individuals.codings.gp.GPNodeExp;
import eva2.server.go.individuals.codings.gp.GPNodeInput;
import eva2.server.go.individuals.codings.gp.GPNodeMult;
import eva2.server.go.individuals.codings.gp.GPNodeOne;
import eva2.server.go.individuals.codings.gp.GPNodePi;
import eva2.server.go.individuals.codings.gp.GPNodePow2;
import eva2.server.go.individuals.codings.gp.GPNodePow3;
import eva2.server.go.individuals.codings.gp.GPNodeSin;
import eva2.server.go.individuals.codings.gp.GPNodeSqrt;
import eva2.server.go.individuals.codings.gp.GPNodeSub;
import eva2.server.go.individuals.codings.gp.InterfaceProgram;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.regression.InterfaceRegressionFunction;
import eva2.server.go.problems.regression.RFKoza_GPI_7_3;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.EVAERROR;
import eva2.tools.ToolBox;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:44:25
 * To change this template use Options | File Templates.
 */
public class PSymbolicRegression extends AbstractOptimizationProblem implements InterfaceProgramProblem, InterfaceAdditionalPopulationInformer, Serializable  {

    private double[]                                m_X                     = new double[1];
    private int                                     m_NumberOfConstants     = 3;
    private double                                  m_LowerBound            = -1;
	private double                                  m_UpperBound            = 1;
    private int                                     m_NumberOfCheckPoints   = 20;
    transient private InterfaceRegressionFunction   m_TargetFunction        = new RFKoza_GPI_7_3();
    private double[]                                m_C                     = new double[m_NumberOfConstants];
    private boolean                                 m_UseInnerConst         = false;
    private boolean                                 m_UseLocalHillClimbing  = false;
    private GPArea                        			m_GPArea                = new GPArea();
    protected AbstractEAIndividual                  m_OverallBest           = null;
    protected double                                m_Noise                 = 0.0;

    // This is graphics stuff
    transient private Plot                          m_Plot;
    private boolean                                 m_Show = false;

    public PSymbolicRegression() {
        this.m_Template     = new GPIndividualProgramData();
        this.initProblem();
        this.compileArea();
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
        if (m_TargetFunction == null) m_TargetFunction = new RFKoza_GPI_7_3();
        this.m_OverallBest  = null;
        this.m_C            = new double[this.m_NumberOfConstants];
        for (int i = 0; i < this.m_C.length; i++) this.m_C[i] = RNG.randomDouble(-10, 10);
	}

    /**
     * This method compiles the area. Only to be called once in the constructor
     * to build the basic function set or if the number of constants etc. change.
     */
    private void compileArea() {
    	// unfortunately this must be cloned or the GUI wont update.
    	GPArea oldArea=m_GPArea;
    	m_GPArea = new GPArea();
    	
    	if (m_GPArea.isEmpty()) {
	        this.m_GPArea.add2CompleteList(new GPNodeOne());
	        this.m_GPArea.add2CompleteList(new GPNodePi(), false);
	        this.m_GPArea.add2CompleteList(new GPNodeAdd());
	        this.m_GPArea.add2CompleteList(new GPNodeSub());
	        this.m_GPArea.add2CompleteList(new GPNodeDiv());
	        this.m_GPArea.add2CompleteList(new GPNodeMult());
	        this.m_GPArea.add2CompleteList(new GPNodeAbs(), false);
	        this.m_GPArea.add2CompleteList(new GPNodeSin(), false);
	        this.m_GPArea.add2CompleteList(new GPNodeCos(), false);
	        this.m_GPArea.add2CompleteList(new GPNodeExp(), false);
	        this.m_GPArea.add2CompleteList(new GPNodeSqrt(), false);
	        this.m_GPArea.add2CompleteList(new GPNodePow2(), false);
	        this.m_GPArea.add2CompleteList(new GPNodePow3(), false);
	        for (int i = 0; i < this.m_X.length; i++) this.m_GPArea.add2CompleteList(new GPNodeInput("X"+i));
	        for (int i = 0; i < this.m_C.length; i++) this.m_GPArea.add2CompleteList(new GPNodeInput("C"+i), false);
    	}
    	if ((oldArea!=null) && (oldArea.getBlackList()!=null) && (oldArea.getBlackList().size()==m_GPArea.getBlackList().size())) {
    		m_GPArea.SetBlackList(oldArea.getBlackList());
    	}
        this.m_GPArea.compileReducedList();
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
    	initPopulation(population, this, m_UseInnerConst, m_NumberOfConstants);
    }
    
    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public static void initPopulation(Population pop, InterfaceProgramProblem prob, boolean useInnerConsts, int numConsts) {
        AbstractEAIndividual template;

        template = ((AbstractOptimizationProblem)prob).getIndividualTemplate();
        GPArea tmpArea[]    = new GPArea[1];
        tmpArea[0]          = prob.getArea();
        ((InterfaceDataTypeProgram)template).setProgramDataLength(1);
        ((InterfaceDataTypeProgram)template).SetFunctionArea(tmpArea);
        if ((template instanceof GAPIndividualProgramData) && useInnerConsts) {
            ((GAPIndividualProgramData)template).setDoubleDataLength(numConsts);
        }
        AbstractOptimizationProblem.defaultInitPopulation(pop, template, prob);
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

    /** 
     * This method evaluates a single individual and sets the fitness values. In this case,
     * the averaged square error between represented function and target function per check point is calculated.
     * 
     * @param individual    The individual that is to be evaluated
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

        for (int j = 0; j < this.m_NumberOfCheckPoints; j++) {
    		setCheckPoint(m_X, j);
            tmpValue = ((Double)program.evaluate(this)).doubleValue();
            fitness += Math.pow((this.m_TargetFunction.evaluateFunction(this.m_X) - ((Double)program.evaluate(this)).doubleValue()), 2);
        }
        
        fitness = fitness / (double)this.m_NumberOfCheckPoints;
        // add noise to the fitness
        fitness += RNG.gaussianDouble(this.m_Noise);
        // set the fitness of the individual
        individual.SetFitness(0, fitness);
        if ((this.m_Plot != null) && (this.m_Plot.getFunctionArea().getContainerSize() ==0)) this.m_OverallBest = null;
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual)individual.clone();
            if (this.m_Show) {
            	if (m_Plot==null) this.initEnvironmentPanel();
                this.m_Plot.clearAll();
                program     = ((InterfaceDataTypeProgram)this.m_OverallBest).getProgramData()[0];
                for (int i = 0; i < this.m_NumberOfCheckPoints; i++) {
                    setCheckPoint(m_X, i);
                    tmpValue = ((Double)program.evaluate(this)).doubleValue();
                    this.m_Plot.setConnectedPoint(this.m_X[0], tmpValue, 0);
                    tmpValue = this.m_TargetFunction.evaluateFunction(this.m_X);
                    this.m_Plot.setConnectedPoint(this.m_X[0], tmpValue, 1);
                    this.m_Plot.setInfoString(1, program.getStringRepresentation(), 1.0f);
                }
            }
        }
    }

    /**
     * Select a test point - TODO this btw only makes much sense in 1D.
     * @param x
     * @param j
     */
	private void setCheckPoint(double[] x, int j) {
    	for (int i = 0; i < x.length; i++) {
            x[i] = this.m_LowerBound +(j*(this.m_UpperBound-this.m_LowerBound)/(this.m_NumberOfCheckPoints-1));
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
    	return PSymbolicRegression.getSensorValue(sensor, m_X, m_C);
//        for (int i = 0; i < this.m_X.length; i++) if (sensor.equalsIgnoreCase("X"+i)) return new Double(this.m_X[i]);
//        for (int i = 0; i < this.m_C.length; i++) if (sensor.equalsIgnoreCase("C"+i)) return new Double(this.m_C[i]);
//        return new Double(0);
    }
    
    /** 
     * This method allows a GP program to sense the environment, e.g.
     * input values, current time etc. Specialized to constants and variables
     * in arrays whose identifiers have the form "Xi" and "Ci".
     * For an identifier X only, the full vars array is returned. For an
     * identifier N only, the array dimension is returned.
     * 
     * @param sensor    The identifier for the sensor.
     * @param vars	array of x_i
     * @param consts	array of c_i
     * @return Sensor value
     */
    public static Object getSensorValue(String sensor, double[] vars, double[] consts) {
    	if (sensor.charAt(0)=='X') {
    		try {
    			if (sensor.length()==1) return vars;
    			int index=Integer.parseInt(sensor.substring(1));
        		return new Double(vars[index]);
    		} catch(Exception e) {
    			System.err.println("Warning, unable to access " + sensor);
    			return vars;
    		}
    	} else if (sensor.charAt(0)=='C') {
    		int index=Integer.parseInt(sensor.substring(1));
    		return new Double(consts[index]);
    	} else if (sensor.charAt(0)=='N') {
    		return (double)vars.length;
    	} else return new Double(0);
//        for (int i = 0; i < this.m_X.length; i++) if (sensor.equalsIgnoreCase("X"+i)) return new Double(this.m_X[i]);
//        for (int i = 0; i < this.m_C.length; i++) if (sensor.equalsIgnoreCase("C"+i)) return new Double(this.m_C[i]);
//        return new Double(0);
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
    public static String globalInfo() {
        return "The task is to infer the equation of a system that can only be observed at a number of checkpoints.";
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

    /** This method toggles the use of inner constants.
     * @param b     The flag to use inner constants.
     */
    public void setUseInnerConst(boolean b) {
        this.m_UseInnerConst = b;
    }
    public boolean getUseInnerConst() {
        return this.m_UseInnerConst;
    }
    public String useInnerConstTipText() {
        return "Toggle the use of inner constants.";
    }

    /** This method toggles the use of local hill climbing for inner constants.
     * @param b     The flag to use local hill climbing for inner constants.
     */
    public void setUseLocalHillClimbing(boolean b) {
        this.m_UseLocalHillClimbing = b;
    }
    public boolean getUseLocalHillClimbing() {
        return this.m_UseLocalHillClimbing;
    }
    public String useLocalHillClimbingTipText() {
        return "Toggle the use of local hill climbing for inner constants.";
    }

    /** This method allows you to set the number of ephremial constants.
     * @param b     The new number of ephremial constants.
     */
    public void setNumberOfConstants(int b) {
        this.m_NumberOfConstants = b;
        this.initProblem();
        m_GPArea.clear();
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
    	if (m_GPArea==null) initProblem();
        return this.m_GPArea;
    }
    public String areaTipText() {
        return "Select function set from the available area.";
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
    	if (m_TargetFunction==null) initProblem();
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
    public String GPIndividualTipText() {
    	return "Modify the properties of the template GP individual such as maximum tree depth etc.";
    }
    
    public double getLowerBound() {
		return m_LowerBound;
	}
	public void setLowerBound(double mLowerBound) {
		m_LowerBound = mLowerBound;
	}
	public String lowerBoundTipText() {
		return "The lower bound of the 1D double interval where the target function is sampled.";
	}

	public double getUpperBound() {
		return m_UpperBound;
	}
	public void setUpperBound(double mUpperBound) {
		m_UpperBound = mUpperBound;
	}
	public String upperBoundTipText() {
		return "The upper bound of the 1D double interval where the target function is sampled.";
	}

	public String[] customPropertyOrder() {
		return new String[] {"lowerBound", "upperBound"};
	}

	@Override
	public String[] getAdditionalDataHeader() {
		String[] superHd = super.getAdditionalDataHeader();
		return ToolBox.appendArrays(new String[]{"bestIndySize","avgIndySize","avgMaxIndyDepth"}, superHd);
	}

	@Override
	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		Object[] superDat = super.getAdditionalDataValue(pop);
		return ToolBox.appendArrays(new Object[]{getBestIndySize(pop), getAvgIndySize(pop), getAvgIndyDepth(pop)}, superDat);
	}

	/**
	 * Get the average tree depth of the given population (for program individuals).
	 * 
	 * @see #getIndyDepth(AbstractEAIndividual)
	 * @param pop
	 * @return
	 */
	public static double getAvgIndyDepth(PopulationInterface pop) {
		Population p = (Population)pop;
		double sum=0;
		for (int i=0; i<p.size(); i++) {
			sum+=getIndyDepth(p.getEAIndividual(i));
		}
		return sum/p.size();
	}

	/**
	 * Return the average number of nodes of program individuals.
	 * 
	 * @see #getIndySize(AbstractEAIndividual)
	 * @param pop
	 * @return
	 */
	public static double getAvgIndySize(PopulationInterface pop) {
		Population p = (Population)pop;
		double sum=0;
		for (int i=0; i<p.size(); i++) {
			sum+=getIndySize(p.getEAIndividual(i));
		}
		return sum/p.size();
	}

	/**
	 * Return the number of nodes of the best current individual if it represents a program,
	 * otherwise zero.
	 * 
	 * @param pop
	 * @return
	 */
	public static int getBestIndySize(PopulationInterface pop) {
		Population p = (Population)pop;
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
			InterfaceProgram prog = ((InterfaceDataTypeProgram)indy).getProgramDataWithoutUpdate()[0];
			if (prog instanceof AbstractGPNode) {
				AbstractGPNode gpNode = (AbstractGPNode)prog;
				return gpNode.getNumberOfNodes();
			} else return 0;
		} else return 0;
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
			InterfaceProgram prog = ((InterfaceDataTypeProgram)indy).getProgramDataWithoutUpdate()[0];
			if (prog instanceof AbstractGPNode) {
				AbstractGPNode gpNode = (AbstractGPNode)prog;
				int d = gpNode.getMaxDepth();
				//if (!gpNode.checkDepth(0)) {
				//	System.out.println(d + "\n" + gpNode.getStringRepresentation());
				//}
				return d;
			} else return 0;
		} else return 0;
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
    	indy.SetPGenotype(node, 0);
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
