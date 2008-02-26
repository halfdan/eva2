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
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.tools.RandomNumberGenerator;

import com.mathworks.jmi.CompletionObserver;
import com.mathworks.jmi.Matlab;

public class MatlabProblem extends AbstractProblemDouble implements CompletionObserver, Serializable {
	private static final long serialVersionUID = 2640948804853759358L;
	public static final boolean 		TRACE = true; 
	transient protected Matlab			matlab = null;
	transient OptimizerRunnable			runnable = null;
	protected boolean 					allowSingleRunnable = true;	
	protected String 					jmInterface;
	protected int 						problemDimension = 10;
	transient protected ResultArr		res = new ResultArr();
	transient PrintStream 				dos = null;
	protected double[][] 				range = null;
	private static final String			defTestOut = "matlabproblem-testout.dat";

	transient private double[] currArray = null;
	private String mtCmd = null;

//	public MatlabProblem() throws Exception {
//		this("JI", 10);
//		if (matlab == null) throw new Exception("Unable to create Matlab instance.");
//	}
	
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
	
	public void initProblem() {
		init(this.jmInterface, this.problemDimension, this.range, defTestOut);
	}
	
	private void init(String nameJEInterface, int dim, double[][] range, String outFile) {
		problemDimension = dim;
		initTemplate();
		res = new ResultArr();
		if ((range != null) && (dim != range.length)) throw new ArrayIndexOutOfBoundsException("Mismatching dimension and range!");
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
			if (matlab == null)
				matlab = new Matlab();//this command links to the current matlab session
		} catch (Error e) {
			log("Error: " + e.toString());
			System.err.println("Error: could not create MatlabProblem instance. MatlabProblem can only be used from Matlab.");
		}
		this.jmInterface = nameJEInterface;
		mtCmd = new String("evaluateJE("+jmInterface+")");
	}
	
	/**
	 * @return the jmInterface
	 */
	public String getJmInterfaceName() {
		return jmInterface;
	}

	/**
	 * @param jmInterface the jmInterface to set
	 */
	public void setJmInterfaceName(String jmInterface) {
		this.jmInterface = jmInterface;
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

	public double[] getCurrentDoubleArray() {
		return currArray;
	}

	public double[] getNewDoubleArray() {
		currArray = new double[problemDimension];
		for (int i=0; i<problemDimension; i++) currArray[i] = RandomNumberGenerator.gaussianDouble(1);
		return currArray;
	}

	public void log(String str) {
		if (dos != null) {
			dos.print((String)str);
			dos.flush();
		}
	}

	public synchronized void setResult(double[] v) {
		//System.err.println("A setting result " + v);
		log("Log setting result " + v + "\n");
		res.set(v);
	}

	public synchronized void completed(int i, Object obj) {
//		System.err.println("calling completed...");
		log("completed " + i + " " + obj.getClass().getName() + " " + obj + "\n");
		if (res.isSet()) {
			log("result0 = " + res.get()[0] + "\n");
			this.notifyAll();
		} else {
			// FATAL, stop opt!
			log("Received no result value from Matlab. Missing Interface object " + jmInterface + " ?\n");
			System.err.println("Received no result value from Matlab. Missing Interface object " + jmInterface + " ?");
			stopOptimize();
		}
	}

	public double[] eval(double[] x) {
		synchronized (this) {
			try {
				res.reset();
				currArray = x;
				matlab.eval(mtCmd, (CompletionObserver)this);
				this.wait();
			} catch (InterruptedException e) {
				log("wait interrupted: " + e.getMessage() + " \n");
			}
		}
		log("wait done, returning " + res.get()[0] + " \n");
		return res.get();


		//currArray = x.clone();
//		try {
		//Object ret = mc.blockingFeval(mlCmd, null);
		//mc.eval("evaluate("+jmInterface+")");
//		try {
//		res.reset();
//		mc.eval("start=1");
//		mc.testEval("evaluate("+jmInterface+")");
//		while (!res.isSet()) {
//		try {
//		Thread.sleep(5);
//		} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		}
//		mc.testEval("waitReturned=1");
//		} catch(Exception e) {
//		mc.testEval("waitReturnedExc=1");
//		}
//		if (res != null) mc.testEval("res="+ ((res.get() == null ) ? "null" : res.get()[0]));
//		Object[] args = new Object[x.length];
//		for (int i=0;i<x.length; i++) {
//		args[i] = new Double(x[i]);
//		}
//		System.out.println("calling blockingFeval");
//		Object ret = mc.blockingFeval("vectfun", args);
//		System.out.println("mc returned " + ret);
//		} catch(Exception e) {
//		System.err.println("Interrupted eval in MatlabProblem: "+e.getMessage());
//		}
//		currArray = null;
//		return null;
	}

//	public String mtGet(String str) {
//	String res = null;
//	try {
//	res = (Matlab.mtGet(0, "x")).toString();
//	mc.eval("mtGet="+res);
//	res = (Matlab.mtGet(1, "x")).toString();
//	mc.eval("mtGet="+res);
//	res = (Matlab.mtGet(-1, "x")).toString();
//	mc.eval("mtGet="+res);
//	} catch (Exception e) {}
//	return res;
//	}
//	public void test() {
//		EvalTestThread tt = new EvalTestThread(this, matlab, jmInterface);
//		new Thread(tt).start();
//	}

	public void optimize(final int optType, String outputFilePrefix) {
		if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
			System.err.println("Please wait for the current optimization to finish");
		} else {
			runnable = OptimizerFactory.getOptRunnable(optType, (AbstractOptimizationProblem)this, outputFilePrefix);
			new Thread(new WaitForEvARunnable(runnable, this)).start();
		}
	}
	
	public void stopOptimize() {
		if (runnable != null) {
			runnable.stopOpt();
		}
	}

	public String getInfoString() {
		if (runnable == null) return "";
		StringBuffer sb = new StringBuffer("Function calls: ");
		sb.append(getFunctionCalls());
		sb.append(". ");
		sb.append(runnable.terminatedBecause());
		return sb.toString();
	}
	
	public int getFunctionCalls() {
		if (runnable == null) return 0;
		return runnable.getGOParams().getOptimizer().getPopulation().getFunctionCalls();
	}
//	public void optimize() {
//		OptimizeThread oT = new OptimizeThread(this);
//		new Thread(oT).start();
//
//		/*
//		 * neuer Thread mit Processor
//		 * 	der ruft this.eval auf
//		 * 	eval ruft Matlab auf, wartet auf resultat
//		 *  schreibt dieses ins ind
//		 *  
//		 * 
//		 */
//	}
	
	Matlab getMatlab() {
		return matlab;
	}
	
	void exportResultToMatlab(double[] result) {
		String resStr;
		if (result == null) resStr = "[]";
		else resStr = BeanInspector.toString(result);
		log("result was " + resStr + "\n");
		try {
			String cmd = jmInterface + "= setResultJE(" + jmInterface + ", " + resStr + ")";
			log("trying cmd: "+ cmd + "\n");
			matlab.eval(cmd);
		} catch (Exception e) {
			log("Exception when exporting result to Matlab! "+e.getMessage());
		}
	}
	
	public double[] getIntermediateResult() {
		if (runnable == null) return null;
		else return runnable.getDoubleSolution();
	}
	
	public String globalInfo() {
		return "Interface problem class for optimization in Matlab, only usable from within Matlab";
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
			mp.log("Starting optimize runnable!\n");

			synchronized (runnable) {
				try {
					new Thread(runnable).start();
					mp.log("Starting optimize thread done!\n");
					runnable.wait();
					mp.log("After wait!\n");
				} catch (InterruptedException e) {
					e.printStackTrace();
					mp.log("WaitForEvARunnable was interrupted with " + e.getMessage());
				}
			}
			try {
				mp.exportResultToMatlab(runnable.getDoubleSolution());
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
		}
	}
}

//class OptimizeThread implements Runnable, InterfaceTextListener {
//	MatlabProblem mp=null;
////	String jmInt = "";
//	Processor proc;
////	InterfaceGOParameters params;
//
//	public OptimizeThread(MatlabProblem mp) {
//		mp.log("Init OptimizeThread\n");
//		this.mp = mp;
////		this.jmInt = jmInt;
//		GOParameters params = new GOParameters();
//		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
//		params.setOptimizer(pso);
//		Population pop = new Population();
//		pop.setPopulationSize(20);
//		params.getOptimizer().setPopulation(pop);
//		params.setProblem(mp);
//		params.setTerminator(new EvaluationTerminator(1000));
//		proc = new Processor(new StatisticsStandalone("/home/mkron/mp-result"), null, params);
//		proc.getStatistics().addTextListener(this);
//	}
//
//	public void run() {
//		mp.log("starting run...\n");
//		mp.log("proc is " + proc + "\n");
//		try {
//			proc.startOpt();
//			proc.run();
//		} catch(Exception e) {
//			mp.log("Exception! " + e.getMessage() + "\n");
//			e.printStackTrace(mp.dos);
//		}
//		mp.log("finished run...\n");
//	}
//
//	public void print(String s) {
//		mp.log(s);
//	}
//}

//class EvalTestThread implements Runnable {
//	MatlabProblem mp=null;
//	Matlab matlab;
//	String jmInt = "";
//
//	public EvalTestThread(MatlabProblem mp, Matlab matlab, String jmInt) {
//		this.mp = mp;
//		this.matlab = matlab;
//		this.jmInt = jmInt;
//	}
//
//	public void run() {
//		for (int i=0; i<3; i++) {
//			mp.eval(mp.getNewDoubleArray());
////			synchronized (mp) {
////			try {
////			mp.wait();
////			} catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			mp.log("wait interrupted: " + e.getMessage() + " \n");
////			}
////			}
////			mp.log("wait " + i + " done!\n");
//		}
//	}
//
//}

//class SynchTestThread implements Runnable {
//MatlabProblem mp=null;

//public SynchTestThread(MatlabProblem mp) {
//this.mp = mp;
//}

//public void run() {
//try {
//Thread.sleep(1000);
//double[] v = new double[3];
//for (int i = 0; i < v.length; i++) {
//v[i] = 7+i;
//}
//mp.setResult(v);
//} catch (InterruptedException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//}

//}

//}

class ResultArr extends Object {
	private double[] val;
	private boolean unset = true;

	public ResultArr() {
		val = null;
	}

	public void set(double[] v) {
		val = v;
		unset = false;
	}

	public double[] get() {
		return val;
	}

	public void reset() {
		if (val != null) {
			synchronized (val) {
				val = null;
				unset=true;
			}
		}

	}

	public boolean isSet() {
		return !unset;
	}
}
