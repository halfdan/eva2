package javaeva.server.go.problems;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javaeva.OptimizerFactory;
import javaeva.OptimizerRunnable;
import javaeva.gui.BeanInspector;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.operators.distancemetric.PhenotypeMetric;
import javaeva.server.go.operators.postprocess.InterfacePostProcessParams;
import javaeva.server.go.operators.postprocess.PostProcess;
import javaeva.server.go.operators.postprocess.PostProcessParams;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.stat.InterfaceTextListener;

public class MatlabProblem extends AbstractProblemDouble implements InterfaceTextListener, Serializable {
	private static final long serialVersionUID = 4913310869887420815L;
	public static final boolean 		TRACE = false; 
//	transient protected Matlab			matlab = null;
	transient OptimizerRunnable			runnable = null;
	protected boolean 					allowSingleRunnable = true;	
//	protected String 					jmInterface;
	protected int 						problemDimension = 10;
	transient PrintStream 				dos = null;
	protected double[][] 				range = null;
	private static final String			defTestOut = "matlabproblem-testout.dat";
	int 								verbosityLevel	= 0;
	private MatlabEvalMediator 			handler = null;
	
	public static boolean hideFromGOE = true; 
	
	private F1Problem f1 = new F1Problem(); // TODO
	
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
		this.range = o.makeRange();
//		this.mtCmd = o.mtCmd;
//		currArray = null;
	}
	
	public Object clone() {
		return new MatlabProblem(this);
	}
	
	public MatlabProblem(String nameJEInterface, int dim) {
		this(nameJEInterface, dim, null);
		range = super.makeRange();
	}
	
	public MatlabProblem(String nameJEInterface, int dim, double[][] range) {
		init(nameJEInterface, dim, range, defTestOut);
	}

	public MatlabProblem(String nameJEInterface, int dim, double lower, double upper) {
		this(nameJEInterface, dim, null);
		double[][] range = new double[dim][2];
		for (int i=0; i<dim; i++) {
			range[dim][0] = lower;
			range[dim][1] = upper;
		}
	}
	
	public void setMediator(MatlabEvalMediator h) {
		handler = h;
	}
	
	public void initProblem() {
		init(/*this.jmInterface*/ null, this.problemDimension, this.range, defTestOut);
	}
	
	private void init(String nameJEInterface, int dim, double[][] rng, String outFile) {
		problemDimension = dim;
		if ((rng != null) && (dim != rng.length)) throw new ArrayIndexOutOfBoundsException("Mismatching dimension and range!");
		range = rng;
		initTemplate();
//		res = new ResultArr();
		if ((dos == null) && TRACE) {
			try {
				dos = new PrintStream(new FileOutputStream(outFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		log("range is " + BeanInspector.toString(range)+ "\n");
		log("template len: " + ((ESIndividualDoubleData)m_Template).getDGenotype().length + "\n");
		try {
//			if (matlab == null)
//				matlab = new Matlab();//this command links to the current matlab session
//			try {
//				matlab.eval("JE='hello'");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		} catch (Error e) {
			log("Error: " + e.toString());
			System.err.println("Error: could not create MatlabProblem instance. MatlabProblem can only be used from Matlab.");
		}
		
//		this.jmInterface = nameJEInterface;
//		mtCmd = new String("evaluateJE("+jmInterface+")");
	}
	
//	/**
//	 * @return the jmInterface
//	 */
//	public String getJmInterfaceName() {
//		return jmInterface;
//	}

//	/**
//	 * @param jmInterface the jmInterface to set
//	 */
//	public void setJmInterfaceName(String jmInterface) {
//		this.jmInterface = jmInterface;
//	}
	
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

	public String problemDimensionTipTex() {
		return "The dimension of the problem.";
	}

	protected double[][] makeRange() {
		if (range==null) range=super.makeRange();
		return range;
	}	

	protected double getRangeLowerBound(int dim) {
		return (range==null) ? super.getRangeLowerBound(dim) : range[dim][0];
	}

	protected double getRangeUpperBound(int dim) {
		return (range==null) ? super.getRangeUpperBound(dim) : range[dim][1];
	}

//	public double[] getCurrentDoubleArray() {
//		return currArray;
//	}
//
//	public double[] getNewDoubleArray() {
//		currArray = new double[problemDimension];
//		for (int i=0; i<problemDimension; i++) currArray[i] = RandomNumberGenerator.gaussianDouble(1);
//		return currArray;
//	}

	public void log(String str) {
		if (dos != null) {
			dos.print((String)str);
			dos.flush();
		}
	}

	public double[] eval(double[] x) {
		log("evaluating " + BeanInspector.toString(x) + "\n");
		double[] res = handler.requestEval(this, x);
		double diff = PhenotypeMetric.euclidianDistance(res, f1.eval(x));
		log("result: " + BeanInspector.toString(res) + " compared to " + BeanInspector.toString(f1.eval(x)) + "\n");
		if (diff != 0) {
			log("!!! diff is " + diff + "\n");
		}
		return res;
		
//		synchronized (this) {
//			try {
//				res.reset();
//				currArray = x;
//				log("evaluating " + BeanInspector.toString(x) + "\n");
//				matlab.eval(mtCmd, (CompletionObserver)this);
//				this.wait();
//			} catch (InterruptedException e) {
//				log("wait interrupted: " + e.getMessage() + " \n");
//			}
//		}
//		log("wait done, returning " + res.get()[0] + " \n");
//		return res.get();
	}


	public void optimize(final int optType, String outputFilePrefix) {
		optimize(optType, outputFilePrefix, null, null);
	}
	
	public void optimize(final int optType, String outputFilePrefix, Object[] specParams, Object[] specValues) {
		if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
			System.err.println("Please wait for the current optimization to finish");
		} else {
			handler.setFinished(false);
			runnable = OptimizerFactory.getOptRunnable(optType, (AbstractOptimizationProblem)this, outputFilePrefix);
//			runnable.getGOParams().setPostProcessParams(new PostProcessParams(0, 0.01, 5));
			runnable.setTextListener(this);
			runnable.setVerbosityLevel(verbosityLevel);
			
			if ((specParams != null) && (specParams.length > 0)) {
				if ((specValues == null) || (specValues.length != specParams.length)) {
					System.err.println("mismatching value list for parameter arguments: " + specValues);
				} else {
					log("setting specific parameters...\n");
					InterfaceOptimizer opt = runnable.getGOParams().getOptimizer();
					for (int i=0; i<specParams.length; i++) { // loop over settings
						log("try setting " + specParams[i] + " to " + specValues[i]);
						if (!BeanInspector.setMem(opt, (String)specParams[i], specValues[i])) {
							log("... Fail!\n");
							System.err.println("Unable to set parameter " + specParams[i] + ", skipping...");
						} else log("... Ok.\n");
					}
					log(BeanInspector.toString(BeanInspector.getMemberDescriptions(opt, true)));
				}
			}
			new Thread(new WaitForEvARunnable(runnable, this)).start();
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
		PostProcess.stopHC();
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
//	
//	Matlab getMatlab() {
//		return matlab;
//	}
//	
	void exportResultPopulationToMatlab(Population pop) {
		double[][] solSet;
	
		if ((pop != null) && (pop.size()>0)) {
			solSet = new double[pop.size()][];
			for (int i=0; i<pop.size(); i++) {
				solSet[i]=((InterfaceDataTypeDouble)pop.getEAIndividual(i)).getDoubleData();
			}
			handler.setSolutionSet(solSet);
		} else {
			handler.setSolutionSet(null);
		}
//		String resStr;
//		if ((pop == null) || (pop.size() == 0)) resStr = "[]";
//		else {
//			StringBuffer sb = new StringBuffer();
//			sb.append("[");
//			for (int i=0; i<pop.size(); i++) {
//				sb.append(AbstractEAIndividual.getDefaultDataString(pop.getEAIndividual(i), " "));
//				if (i<pop.size()-1) sb.append(";");
//			}
//			sb.append("]");
//			resStr = sb.toString();
//		}
//		log("result array was " + resStr + "\n");
//		try {
//			String cmd = jmInterface + "= setResultArrayJE(" + jmInterface + ", " + resStr + ")";
//			log("trying cmd: "+ cmd + "\n");
//			matlab.eval(cmd);
//		} catch (Exception e) {
//			log("Exception when exporting result to Matlab! "+e.getMessage());
//		}
	}
	
	void exportResultToMatlab(double[] result) {
		handler.setSolution(result);
//		String resStr;
//		if (result == null) resStr = "[]";
//		else resStr = BeanInspector.toString(result);
//		log("result was " + resStr + "\n");
//		try {
//			String cmd = jmInterface + "= setResultJE(" + jmInterface + ", " + resStr + ")";
//			log("trying cmd: "+ cmd + "\n");
//			matlab.eval(cmd);
//		} catch (Exception e) {
//			log("Exception when exporting result to Matlab! "+e.getMessage());
//		}
	}
	
	/**
	 * To be called by the executing thread to inform that the thread is finished.
	 * We 
	 */
	void notifyFinished() {
		handler.setFinished(true);
	}
	
	public double[] getIntermediateResult() {
		if (runnable == null) return null;
		else return runnable.getDoubleSolution();
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
		if (verbosityLevel > 0) {
			// matlab displays sysout output in the command window, so we simply use this channel
			System.out.print(str);
		}
		log(str);		
	}

	public void println(String str) {
		print(str);
		print("\n");		
	}
}

////////////////////////////

class WaitForEvARunnable implements Runnable {
	OptimizerRunnable runnable;
	MatlabProblem mp;
	
	public WaitForEvARunnable(OptimizerRunnable runnable, MatlabProblem mp) {
		this.runnable = runnable;
		this.mp = mp;
	}
	
	public void run() {
		if (runnable != null) {
			mp.log("\nStarting optimize runnable!\n");

			synchronized (runnable) {
				try {
					// whole optimization thread goes in here
					new Thread(runnable).start();
					mp.log("Starting optimize thread done!\n");
					runnable.wait();
					// wait for the runnable to finish
					mp.log("After wait!\n");
				} catch (InterruptedException e) {
					e.printStackTrace();
					mp.log("WaitForEvARunnable was interrupted with " + e.getMessage());
				}
			}
			try {
				mp.log("runnable.getSolution: " + BeanInspector.toString(runnable.getDoubleSolution()));
				mp.log("\ngetAllSols best: " + AbstractEAIndividual.getDefaultDataString(runnable.getGOParams().getOptimizer().getAllSolutions().getBestEAIndividual()));
				mp.log("\n");
				// write results back to matlab
				mp.exportResultToMatlab(runnable.getDoubleSolution());
				mp.exportResultPopulationToMatlab(runnable.getSolutionSet());
				System.out.println("Optimization finished: " + mp.getInfoString());
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				mp.log("error in callback: " + e.getMessage() + " " + sw.toString() + "\n");
			}
		} else {
			System.err.println("Invalid optimization call.");
			mp.log("invalid call, no optimization started.\n");
			mp.exportResultToMatlab(null);
			mp.exportResultPopulationToMatlab(null);
		}
		mp.notifyFinished();
	}
	
}
