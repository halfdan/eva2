package eva2.server.go.problems;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.GIIndividualIntegerData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceDataTypeInteger;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.operators.terminators.FitnessConvergenceTerminator;
import eva2.server.go.operators.terminators.PhenotypeConvergenceTerminator;
import eva2.server.go.operators.terminators.PopulationMeasureTerminator.ChangeTypeEnum;
import eva2.server.go.operators.terminators.PopulationMeasureTerminator.DirectionTypeEnum;
import eva2.server.go.operators.terminators.PopulationMeasureTerminator.StagnationTypeEnum;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.stat.InterfaceTextListener;
import eva2.tools.ToolBox;

/**
 * Interface problem class for Matlab(TM). Towards EvA2 this behaves like any other double valued 
 * problem implementation. However internally, every evaluation "asks" a mediator instance for
 * the result which waits for Matlab to evaluate the x value. When Matlab is finished, the mediator
 * returns to the evaluate method and the optimization can continue. 
 * @author mkron
 *
 */
public class MatlabProblem extends AbstractOptimizationProblem implements InterfaceHasInitRange, InterfaceTextListener, Serializable {
	private static final long serialVersionUID = 4913310869887420815L;
	public static boolean 				TRACE = false; 
	transient OptimizerRunnable			runnable = null;
	protected boolean 					allowSingleRunnable = true;	
	protected int 						problemDimension = 10;
	transient PrintStream 				dos = null;
	private double 						range[][] =	null;
	private static String				defTestOut = "matlabproblem-debug.log";
//	private static String				resOutFile = "matlabproblem-output.txt";
//	transient PrintStream				resOutStream = null;
	int 								verbosityLevel	= 0;
	private MatlabEvalMediator 			handler = null;
//	private boolean isDouble = true;
	private MatlabProblemDataTypeEnum dataType = MatlabProblemDataTypeEnum.typeDouble;
	private double[][] initialRange = null; // the initial range for double-valued problems

	public static boolean hideFromGOE = true; 
	
//	transient private double[] currArray = null;
//	private String mtCmd = null;

	public MatlabProblem(MatlabProblem o) {
		this.m_Template=null;
		this.handler = o.handler;
		this.runnable = o.runnable;
		this.allowSingleRunnable = o.allowSingleRunnable;
//		this.jmInterface = new String(o.jmInterface);
		this.problemDimension = o.problemDimension;
//		this.res = new ResultArr();
//		if (o.res != null) if (o.res.get() != null) res.set(o.res.get());
		this.range = o.range;
		this.dataType = o.dataType;
		this.initialRange = o.initialRange;
//		this.mtCmd = o.mtCmd;
//		currArray = null;
	}

	public Object clone() {
		return new MatlabProblem(this);
	}

	/**
	 * Constructor of a real valued problem.
	 * @param dim
	 * @param range
	 */
//	public MatlabProblem(int dim, double[][] range) {
//		init(dim, ProblemDataTypeEnum.typeDouble, range, null, defTestOut);
//	}
	
	/**
	 * Constructor of a binary problem with given bit length.
	 * @param dim
	 */
	public MatlabProblem(int dim) {
		init(dim, MatlabProblemDataTypeEnum.typeBinary, null, null, defTestOut);
	}
	
	/**
	 * Constructor of a real valued problem with initialization range.
	 * @param dim
	 * @param range
	 * @param initRange
	 */
//	public MatlabProblem(int dim, double[][] range, double[][] initRange) {
//		init(dim, ProblemDataTypeEnum.typeDouble, range, initRange, defTestOut);
//	}

	/**
	 * Constructor of real or integer valued problem, the range will be converted
	 * to integer in the latter case.
	 * 
	 * @param dim
	 * @param datType
	 * @param range
	 */
	public MatlabProblem(int dim, MatlabProblemDataTypeEnum datType, double[][] range) {
		init(dim, datType, range, null, defTestOut);
	}
	
	/**
	 * Constructor of real or integer valued problem with initialization range. 
	 * The ranges will be converted to integer in the latter case.
	 * 
	 * @param dim
	 * @param datType
	 * @param range
	 */
	public MatlabProblem(int dim, MatlabProblemDataTypeEnum datType, double[][] range, double[][] initRange) {
		init(dim, datType, range, initRange, defTestOut);
	}

	
//	public MatlabProblem(int dim, double lower, double upper) {
//		this(dim, null);
//		double[][] range = new double[dim][2];
//		for (int i=0; i<dim; i++) {
//			range[dim][0] = lower;
//			range[dim][1] = upper;
//		}
//	}

	protected void initTemplate() {
		switch (dataType) {
		case typeDouble:
			if (m_Template == null || !(m_Template instanceof ESIndividualDoubleData)) m_Template         = new ESIndividualDoubleData();
			if (getProblemDimension() > 0) { // avoid evil case setting dim to 0 during object init
				((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(getProblemDimension());
				((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(range);
			}
			break;
		case typeBinary:
			///// binary alternative
			if (m_Template == null || !(m_Template instanceof GAIndividualBinaryData)) m_Template         = new GAIndividualBinaryData(getProblemDimension());
			break;
		case typeInteger:
			int[][] intRange=makeIntRange(range);
			if (m_Template == null || !(m_Template instanceof GIIndividualIntegerData)) m_Template         = new GIIndividualIntegerData(intRange);
			break;
		}
	}

	private int[][] makeIntRange(double[][] r) {
		int[][] intRange=new int[r.length][r[0].length];
		for (int i=0; i<r.length; i++){
			for (int j=0; j<r[0].length; j++) {
				intRange[i][j]=(int)r[i][j];
			}
		}
		return intRange;
	}

	public void setMediator(MatlabEvalMediator h) {
		handler = h;
		handler.setMatlabProblem(this);
	}

	public void initProblem() {
		init(this.problemDimension, dataType, range, initialRange, defTestOut);
	}

	public static FitnessConvergenceTerminator makeFitConvTerm(double thresh, int stagnPeriod) {
		FitnessConvergenceTerminator fct = new FitnessConvergenceTerminator(thresh, stagnPeriod, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease);
		return fct;
	}

	public static PhenotypeConvergenceTerminator makePhenConvTerm(double thresh, int stagnPeriod) {
		PhenotypeConvergenceTerminator pct = new PhenotypeConvergenceTerminator(thresh, stagnPeriod, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease);
		return pct;
	}
	
	/**
	 * Make deep clones for the ranges, or there may be deadlocks in communicating with Matlab!
	 *  
	 * @param dim
	 * @param globalRange
	 * @param initRange
	 * @param outFile
	 */
	private void init(int dim, MatlabProblemDataTypeEnum datType, double[][] globalRange, double[][] initRange, String outFile) {
		this.problemDimension = dim;
//		if ((rng != null) && (dim != rng.length)) throw new ArrayIndexOutOfBoundsException("Mismatching dimension and range!");
		if (globalRange!=null) { // these may be Matlab objects, so I do it by foot, just to be sure not to clone them within Matlab instead of here 
			this.range = new double[globalRange.length][globalRange[0].length];
			for (int i=0; i<this.range.length; i++) {
				for (int j=0; j<this.range[0].length; j++) this.range[i][j]=globalRange[i][j]; 
			}
		} else this.range=null;
		
		if (initRange!=null) { // these may be Matlab objects, so I do it by foot, just to be sure not to clone them within Matlab instead of here
			this.initialRange = new double[initRange.length][initRange[0].length];
			for (int i=0; i<this.initialRange.length; i++) {
				for (int j=0; j<this.initialRange[0].length; j++) this.initialRange[i][j]=initRange[i][j]; 
			}
		} else this.initialRange=null;
		
		if (Arrays.deepEquals(initialRange, range)) initialRange=null;

		dataType=datType; // store the data type
		log("### Data type is " + dataType);

//		System.err.println("isDouble: " + isDouble);
//		System.err.println("range: " + BeanInspector.toString(range));
		initTemplate();
//		res = new ResultArr();
		
		setDebugOut(TRACE, defTestOut);
		log("Initial range is " + BeanInspector.toString(initialRange) + "\n");

//		log("range is " + BeanInspector.toString(range)+ "\n");
//		log("template len: " + ((ESIndividualDoubleData)m_Template).getDGenotype().length + "\n");
	}

	/**
	 * If swtch is true and no output file is open yet, open a new one which will be used for debug output.
	 * if fname is null, the default filename will be used.
	 * if swtch is false, close the output file and deactivate debug output.
	 * 
	 * @param swtch
	 * @param fname
	 */
	public void setDebugOut(boolean swtch, String fname) {
		TRACE=swtch;
//		System.err.println("setDebugOut: " +  swtch +" " + fname);
		if (!swtch && (dos != null)) {
			dos.close();
			dos = null;
		} else if (swtch && (dos == null)) {
			try {
				if (fname==null || (fname.length()==0)) fname = defTestOut;
//				System.err.println("Opening "+fname);
				dos = new PrintStream(new FileOutputStream(fname));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setStatsOutput(int verboLevel) {
		if ((verboLevel >= 0) && (verboLevel <= 3)) {
			verbosityLevel = verboLevel;
		}
		else System.err.println("Error, invalid verbosity level for statistics output!");
	}

	public String jmiInterfaceNameTipText() {
		return "Name of the JEInterface instance in Matlab";
	}

	/**
	 * @param problemDimension the problemDimension to set
	 */
	public void setProblemDimension(int problemDimension) {
		this.problemDimension = problemDimension;
	}


	public int getProblemDimension() {
		return problemDimension;
	}

	public String problemDimensionTipText() {
		return "The dimension of the problem.";
	}

//	public double[][] makeRange() {
//	if (range==null) range=super.makeRange();
//	return range;
//	}	

//	protected double getRangeLowerBound(int dim) {
//	return (range==null) ? super.getRangeLowerBound(dim) : range[dim][0];
//	}

//	protected double getRangeUpperBound(int dim) {
//	return (range==null) ? super.getRangeUpperBound(dim) : range[dim][1];
//	}

//	public double[] getCurrentDoubleArray() {
//	return currArray;
//	}

//	public double[] getNewDoubleArray() {
//	currArray = new double[problemDimension];
//	for (int i=0; i<problemDimension; i++) currArray[i] = RNG.gaussianDouble(1);
//	return currArray;
//	}

	public void log(String str) {
		if (dos != null) {
			dos.print((String)str);
			dos.flush();
		}
	}

	public void optimize(final int optType, String outputFilePrefix) {
		optimize(optType, outputFilePrefix, null, null);
	}

	/**
	 * Start an optimization using the MatlabProblem. The optType references the standard
	 * optimizer as in OptimizerFactory. An output file prefix is optional. In two arrays,
	 * name-value mappings can be given as additional parameters to the optimizer.
	 * 
	 * @see OptimizerFactory.getOptRunnable
	 * @param optType
	 * @param outputFilePrefix
	 * @param specParams
	 * @param specValues
	 */
	public void optimize(final int optType, String outputFilePrefix, Object[] specParams, Object[] specValues) {
		if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
			System.err.println("Please wait for the current optimization to finish");
		} else {
//			log("in MP optimize A\n");
			handler.setMatlabProblem(this);
			handler.setFinished(false);
			runnable = OptimizerFactory.getOptRunnable(optType, (AbstractOptimizationProblem)this, outputFilePrefix);
//			runnable.getGOParams().setPostProcessParams(new PostProcessParams(0, 0.01, 5));
			log("in MP optimize B\n");
			log("Setting text listener, verbo " + verbosityLevel + "\n");
			runnable.setTextListener(this);
			runnable.setVerbosityLevel(verbosityLevel);
			if (verbosityLevel>0) runnable.setOutputTo(2); // both file + window
			else runnable.setOutputTo(1); // only window
			runnable.setOutputFullStatsToText(true);

//			log("in MP optimize C\n");
			if ((specParams != null) && (specParams.length > 0)) {
				if ((specValues == null) || (specValues.length != specParams.length)) {
					System.err.println("mismatching value list for parameter arguments: " + specValues);
				} else {
					log("setting specific parameters...\n");
					InterfaceOptimizer opt = runnable.getGOParams().getOptimizer();
//					log(BeanInspector.toString(BeanInspector.getMemberDescriptions(opt, true)));
					for (int i=0; i<specParams.length; i++) { // loop over settings
						log("try setting " + specParams[i] + " to " + specValues[i]);
						String paramName = null;
						try {
							paramName = (String)specParams[i];
						} catch (ClassCastException e) {
							paramName = "" + specParams[i];
							if (!(specParams[i] instanceof Character)) {
								System.err.println("Error, parameter "+ specParams[i] + " could not be cast to String, trying " + paramName);
							}
						}
						Object specVal = null; // avoid giving chars to the converter method here - the ascii value would be assigned instead of the string 
						if (specValues[i] instanceof Character) specVal = ""+specValues[i];
						else specVal = specValues[i];
						if ((paramName == null) || (!BeanInspector.setMem(opt, paramName, specVal))) {
							log("... Fail!\n");
							System.err.println("Unable to set parameter " + paramName + ", skipping...");
						} else log("... Ok.\n");
					}
					log(BeanInspector.toString(BeanInspector.getMemberDescriptions(opt, true)));
				}
			}
//			log("in MP optimize D\n");
			new Thread(new WaitForEvARunnable(runnable, this)).start();
//			log("in MP optimize E\n");
		}
	}

	public void startPostProcess(InterfacePostProcessParams ppp) {
		if (ppp.isDoPostProcessing()) {
			if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
				System.err.println("Please wait for the current optimization to finish");
			} else {
				handler.setFinished(false);
				log("\nstarting post process thread... " + BeanInspector.toString(ppp));
				//			runnable.setTextListener(this);
				runnable.setDoRestart(true);
				runnable.setDoPostProcessOnly(true);
				runnable.setPostProcessingParams(ppp);
				//			runnable.restartOpt();
				//			log("\nppp are "  + BeanInspector.toString(runnable.getGOParams().getPostProcessParams()));
				new Thread(new WaitForEvARunnable(runnable, this)).start();
			}
		} else System.err.println("Nothing to be done.");
	}

	/**
	 * Request post processing of the last optimization results with given parameters 
	 * and export the result solution set to matlab.
	 * 
	 * @param steps post processing steps with hill climber
	 * @param sigma	sigma parameter for clustering
	 * @param nBest	maximum number of solutions to retrieve
	 */
	public void requestPostProcessing(int steps, double sigma, int nBest) {
		PostProcessParams ppp = new PostProcessParams(steps, sigma, nBest);
		startPostProcess(ppp);
	}

	/**
	 * Request post processing of the last optimization results with given parameters 
	 * and export the result solution set to matlab.
	 * This variant retrieves all solutions found in this way. 
	 * 
	 * @param steps post processing steps with hill climber
	 * @param sigma	sigma parameter for clustering
	 */
	public void requestPostProcessing(int steps, double sigma) {
		requestPostProcessing(steps, sigma, -1);
	}

	/**
	 * Try and stop the current optimization as well as any post processing 
	 * currently running.
	 */
	public void stopOptimize() {
		log(">>>>>>>>>> Stop event!\n");
		if (runnable != null) {
			runnable.stopOpt();
		}
		PostProcess.stopAllPP();
	}

	public String getInfoString() {
		if (runnable == null) return "";
		StringBuffer sb = new StringBuffer("");
		sb.append(runnable.terminatedBecause());
		return sb.toString();
	}

	public int getFunctionCalls() {
		if (runnable == null) return 0;
		return runnable.getGOParams().getOptimizer().getPopulation().getFunctionCalls();
	}

//	Matlab getMatlab() {
//	return matlab;
//	}

	void exportResultPopulationToMatlab(Population pop) {
		if ((pop != null) && (pop.size()>0)) {
			switch(dataType) {
			case typeDouble:
				double[][] rsolSet = new double[pop.size()][];
				for (int i=0; i<pop.size(); i++) {
					rsolSet[i]=((InterfaceDataTypeDouble)pop.getEAIndividual(i)).getDoubleData();
				}
				handler.setSolutionSet(rsolSet);
				break;
			case typeBinary:
				BitSet[] bsolSet = new BitSet[pop.size()];
				for (int i=0; i<pop.size(); i++) {
					bsolSet[i]=((InterfaceDataTypeBinary)pop.getEAIndividual(i)).getBinaryData();
				}
				handler.setSolutionSet(bsolSet);
				break;
			case typeInteger: 
				int[][] isolSet = new int[pop.size()][];
				for (int i=0; i<pop.size(); i++) {
					isolSet[i]=((InterfaceDataTypeInteger)pop.getEAIndividual(i)).getIntegerData();
				}
				handler.setSolutionSet(isolSet);
				break;
			}
		} else {
			switch(dataType) {
			case typeDouble: handler.setSolutionSet((double[][])null); break;
			case typeBinary: handler.setSolutionSet((BitSet[])null); break;
			case typeInteger: handler.setSolutionSet((int[][])null); break;
			}
		}
	}

	public void exportResultToMatlab(OptimizerRunnable runnable) {
		handler.setSolution(getIntermediateResult());
	}

	/**
	 * To be called by the executing thread to inform that the thread is finished.
	 * We 
	 */
	void notifyFinished() {
		handler.setFinished(true);
	}

	public Object getIntermediateResult() {
		if (runnable == null) { 
			System.err.println("Warning, runnable is null in MatlabProblem!");
			return null;
		} else {
			switch(dataType) {
			case typeDouble: return runnable.getDoubleSolution();
			case typeBinary: return runnable.getBinarySolution();
			case typeInteger: return runnable.getIntegerSolution();
			default: System.err.println("Warning, incompatible data type in MatlabProblem!");return null;
			}
		}
	}

	/**
	 * Return the number of function calls performed so far.
	 * @return
	 */
	public int getProgress() {
		if (runnable == null) return 0;
		else return runnable.getProgress();
	}

	public static String globalInfo() {
		return "Interface problem class for optimization in Matlab, only usable from within Matlab";
	}

	public void print(String str) {
//		System.err.println("MP print: " + str);
//		if (resOutStream==null) {
//			try {
//				resOutStream = new PrintStream(new FileOutputStream(resOutFile));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
		if (verbosityLevel > 0) {
			// matlab displays sysout output in the command window, so we simply use this channel
			System.out.print(str);
//			if (resOutStream != null) resOutStream.print(str);
		}
		log(str);		
	}

	public void println(String str) {
		print(str);
		print("\n");		
	}
	//
//	public double[] eval(double[] x) {
//	log("evaluating " + BeanInspector.toString(x) + "\n");
//	double[] res = handler.requestEval(this, x);
//	return res;
//	}

	@Override
	public void evaluate(AbstractEAIndividual indy) {
		log("evaluating " + AbstractEAIndividual.getDefaultStringRepresentation(indy) + "\n");
		double[] res = handler.requestEval(this, AbstractEAIndividual.getIndyData(indy));
		log("evaluated to " + BeanInspector.toString(res) + "\n");
		log("Free mem is " + Runtime.getRuntime().freeMemory() + ", time is " + System.currentTimeMillis() + "\n");
		indy.SetFitness(res);
	}

	@Override
	public void initPopulation(Population population) {
		initTemplate();
		AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("A general Matlab problem");
        sb.append(this.getName());
        //sb.append("\n");
        return sb.toString();
	}

	public Object getInitRange() {
		log("retrieving initial range..., first entry: " + ((initialRange==null) ? "null" : BeanInspector.toString(initialRange[0])));
		return initialRange;
	}
	
	public String getName() {
		return "MatlabProblem";
	}

//	@Override
//	public String[] getAdditionalDataHeader() {
//		return ToolBox.appendArrays(super.getAdditionalDataHeader(), "matlabSol");
//	}
//	@Override
//	public String[] getAdditionalDataInfo() {
//		return ToolBox.appendArrays(super.getAdditionalDataInfo(), "Additional solution representation");
//	}
//	@Override
//	public Object[] getAdditionalDataValue(PopulationInterface pop) {
////		String addStr=((AbstractEAIndividual)pop.getBestIndividual()).getStringRepresentation();
//		String addStr=BeanInspector.toString(pop.getBestIndividual());
//		return ToolBox.appendArrays(super.getAdditionalDataValue(pop), addStr);
//	}
}
