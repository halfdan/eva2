package eva2.server.go.problems;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.GAIndividualIntegerData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceDataTypeInteger;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.stat.InterfaceTextListener;

/**
 * Interface problem class for Matlab(TM). Towards EvA2 this behaves like any other double valued 
 * problem implementation. However internally, every evaluation "asks" a mediator instance for
 * the result which waits for Matlab to evaluate the x value. When Matlab is finished, the mediator
 * returns to the evaluate method and the optimization can continue. 
 * @author mkron
 *
 */
public class MatlabProblem extends AbstractOptimizationProblem implements InterfaceTextListener, Serializable {
	private static final long serialVersionUID = 4913310869887420815L;
	public static boolean 				TRACE = false; 
	transient OptimizerRunnable			runnable = null;
	protected boolean 					allowSingleRunnable = true;	
	protected int 						problemDimension = 10;
	transient PrintStream 				dos = null;
	private double 						range[][] =	null;
	private static String				defTestOut = "matlabproblem-debug.log";
	private static String				resOutFile = "matlabproblem-output.txt";
	transient PrintStream				resOutStream = null;
	int 								verbosityLevel	= 0;
	private MatlabEvalMediator 			handler = null;
	private boolean isDouble = true;

	public static boolean hideFromGOE = true; 

//	transient private double[] currArray = null;
//	private String mtCmd = null;

	public MatlabProblem(MatlabProblem o) {
//		this.matlab = o.matlab;
		this.handler = o.handler;
		this.runnable = o.runnable;
		this.allowSingleRunnable = o.allowSingleRunnable;
//		this.jmInterface = new String(o.jmInterface);
		this.problemDimension = o.problemDimension;
//		this.res = new ResultArr();
//		if (o.res != null) if (o.res.get() != null) res.set(o.res.get());
		this.range = o.range;
		this.isDouble = o.isDouble;
//		this.mtCmd = o.mtCmd;
//		currArray = null;
	}

	public Object clone() {
		return new MatlabProblem(this);
	}

	public MatlabProblem(int dim) {
		this(dim, (double[][])null);
	}

	public MatlabProblem(int dim, double[][] range) {
		init(dim, range, defTestOut);
	}

	public MatlabProblem(int dim, double lower, double upper) {
		this(dim, null);
		double[][] range = new double[dim][2];
		for (int i=0; i<dim; i++) {
			range[dim][0] = lower;
			range[dim][1] = upper;
		}
	}

	protected void initTemplate() {
		if (isDouble) {
			if (m_Template == null) m_Template         = new ESIndividualDoubleData();
			if (getProblemDimension() > 0) { // avoid evil case setting dim to 0 during object init
				((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(getProblemDimension());
				((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(range);
			}
		} else {
			m_Template         = new GAIndividualIntegerData();
			int intLen = 1+((getProblemDimension()-1)/32);
			int lastIntCodingBits = getProblemDimension()-((intLen-1)*32);
			if (lastIntCodingBits > 32) System.err.println("ERROR in MatlabProblem:initTemplate");
			((GAIndividualIntegerData)m_Template).setIntegerDataLength(intLen);
			((GAIndividualIntegerData)m_Template).SetIntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
			if (lastIntCodingBits < 32) ((GAIndividualIntegerData)m_Template).SetIntRange(intLen-1, 0, (int)Math.pow(2, lastIntCodingBits)-1);
//			System.err.println("integer length is "+((GAIndividualIntegerData)m_Template).getIntegerData().length);
//			System.err.println("Range is " + BeanInspector.toString(((GAIndividualIntegerData)m_Template).getIntRange()));
//			m_Template         = new GAIndividualBinaryData();
//			((GAIndividualBinaryData)m_Template).setBinaryDataLength(getProblemDimension());
		}
	}

	public void setMediator(MatlabEvalMediator h) {
		handler = h;
		handler.setMatlabProblem(this);
	}

	public void initProblem() {
		init(this.problemDimension, range, defTestOut);
	}

	private void init(int dim, double[][] rng, String outFile) {
		problemDimension = dim;
//		if ((rng != null) && (dim != rng.length)) throw new ArrayIndexOutOfBoundsException("Mismatching dimension and range!");
		range = rng;
		if (range==null) isDouble = false;
		else isDouble = true;

//		System.err.println("isDouble: " + isDouble);
//		System.err.println("range: " + BeanInspector.toString(range));
		initTemplate();
//		res = new ResultArr();
		
		setDebugOut(TRACE, defTestOut);

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

	public void optimize(final int optType, String outputFilePrefix, Object[] specParams, Object[] specValues) {
		if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
			System.err.println("Please wait for the current optimization to finish");
		} else {
//			log("in MP optimize A\n");
			handler.setMatlabProblem(this);
			handler.setFinished(false);
			runnable = OptimizerFactory.getOptRunnable(optType, (AbstractOptimizationProblem)this, outputFilePrefix);
//			runnable.getGOParams().setPostProcessParams(new PostProcessParams(0, 0.01, 5));
//			log("in MP optimize B\n");
			runnable.setTextListener(this);
			runnable.setVerbosityLevel(verbosityLevel);
			runnable.setOutputAdditionalInfo(true);

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

	public void stopOptimize() {
		log(">>>>>>>>>> Stop event!\n");
		if (runnable != null) {
			runnable.stopOpt();
		}
		PostProcess.stopPP();
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
			if (isDouble) {
				double[][] solSet = new double[pop.size()][];
				for (int i=0; i<pop.size(); i++) {
					solSet[i]=((InterfaceDataTypeDouble)pop.getEAIndividual(i)).getDoubleData();
				}
				handler.setSolutionSet(solSet);
			} else {
				int[][] solSet = new int[pop.size()][];
				for (int i=0; i<pop.size(); i++) {
					solSet[i]=((InterfaceDataTypeInteger)pop.getEAIndividual(i)).getIntegerData();
				}
				handler.setSolutionSet(solSet);
			}
		} else {
			if (isDouble) handler.setSolutionSet((double[][])null);
			else handler.setSolutionSet((int[][])null);
		}
	}

	void exportResultToMatlab(OptimizerRunnable runnable) {
		if (isDouble) handler.setSolution(runnable.getDoubleSolution());
		else handler.setSolution(runnable.getIntegerSolution());
	}

//	void exportResultToMatlab(double[] result) {
//		handler.setSolution(result);
//	}

	/**
	 * To be called by the executing thread to inform that the thread is finished.
	 * We 
	 */
	void notifyFinished() {
		handler.setFinished(true);
	}

	public Object getIntermediateResult() {
		if (runnable == null) return null;
		else {
			if (isDouble) return runnable.getDoubleSolution();
			else return runnable.getIntegerSolution();
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

	public String globalInfo() {
		return "Interface problem class for optimization in Matlab, only usable from within Matlab";
	}

	public void print(String str) {
		if (resOutStream==null) {
			try {
				resOutStream = new PrintStream(new FileOutputStream(resOutFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (verbosityLevel > 0) {
			// matlab displays sysout output in the command window, so we simply use this channel
			System.out.print(str);
			if (resOutStream != null) resOutStream.print(str);
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
		indy.SetFitness(res);
	}

	@Override
	public void initPopulation(Population population) {
		AbstractEAIndividual tmpIndy;
		population.clear();
		initTemplate();

		for (int i = 0; i < population.getPopulationSize(); i++) {
			tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
			tmpIndy.init(this);
//			System.err.println("initPopulation: " + AbstractEAIndividual.getDefaultDataString(tmpIndy) + " , " + tmpIndy.getStringRepresentation());
			population.add(tmpIndy);
		}
		// population init must be last
		// it set's fitcalls and generation to zero
		population.init();
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("A general Matlab problem");
        sb.append(this.getName());
        //sb.append("\n");
        return sb.toString();
	}
}
