package eva2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.BitSet;

import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceDataTypeInteger;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.populations.Population;
import eva2.server.modules.GOParameters;
import eva2.server.modules.Processor;
import eva2.server.stat.AbstractStatistics;
import eva2.server.stat.InterfaceTextListener;
import eva2.server.stat.StatisticsStandalone;


/**
 * This Runnable class just encapsulates the Processor class with some simple ways to access a solution.
 * 
 * @author mkron
 *
 */
public class OptimizerRunnable implements Runnable {
	Processor proc;
	boolean isFinished = false;
	boolean doRestart = false; // indicate whether start or restart should be done --> whether pop will be reinitialized.
	boolean postProcessOnly = false;
	InterfaceTextListener listener = null;
	
	public OptimizerRunnable(GOParameters params, String outputFilePrefix) {
		this(params, outputFilePrefix, false);
	}
		
	public OptimizerRunnable(GOParameters params, String outputFilePrefix, boolean restart) {
		proc = new Processor(new StatisticsStandalone(outputFilePrefix), null, params);
		((AbstractStatistics)proc.getStatistics()).setSaveParams(false);
		doRestart = restart;
	}
	
	public InterfaceGOParameters getGOParams() {
		return proc.getGOParams();
	}
	
	public void setTextListener(InterfaceTextListener lsnr) {
		proc.getStatistics().removeTextListener(listener);
		this.listener = lsnr;
		if (listener != null) proc.getStatistics().addTextListener(listener);
	}
	
	public void setDoRestart(boolean restart) {
		doRestart = restart;
	}
	
	public void run() {
		isFinished = false;
		try {
			proc.setSaveParams(false);
			if (postProcessOnly) {
				proc.performPostProcessing((PostProcessParams)proc.getGOParams().getPostProcessParams(), listener);
			} else {
				if (doRestart) proc.restartOpt();
				else proc.startOpt();
				proc.runOptOnce();
			}
		} catch(Exception e) {
			proc.getStatistics().printToTextListener("Exception in OptimizeThread::run: " + e.getMessage() + "\n");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			proc.getStatistics().printToTextListener(sw.toString());
		}
		isFinished = true;
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public void setDoPostProcessOnly(boolean poPO) {
		postProcessOnly = poPO;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public void restartOpt() {
		proc.restartOpt();
	}
	
	public void stopOpt() {
		proc.stopOpt();
	}
	
	public IndividualInterface getResult() {
		return proc.getStatistics().getBestSolution();
	}
	
	public Population getSolutionSet() {
		return proc.getResultPopulation();
	}
	
	public void setPostProcessingParams(InterfacePostProcessParams ppp) {
		proc.getGOParams().setPostProcessParams(ppp);
	}
	
	public int getProgress() {
		return proc.getGOParams().getOptimizer().getPopulation().getFunctionCalls();
	}

	public String terminatedBecause() {
		if (isFinished) {
			if (postProcessOnly) {
				return "Post processing finished";
			} else {
				InterfaceTerminator term = proc.getGOParams().getTerminator();
				return term.terminatedBecause(proc.getGOParams().getOptimizer().getPopulation());
			}
		} else return "Not yet terminated";
	}
	
	public double[] getDoubleSolution() {
		IndividualInterface indy = getResult();
		if (indy instanceof InterfaceDataTypeDouble) {
			return ((InterfaceDataTypeDouble)indy).getDoubleData();
		} else return null;
	}
	
	public BitSet getBinarySolution() {
		IndividualInterface indy = getResult();
		if (indy instanceof InterfaceDataTypeBinary) {
			return ((InterfaceDataTypeBinary)indy).getBinaryData();
		} else return null;
	}
	
	public int[] getIntegerSolution() {
		IndividualInterface indy = getResult();
		if (indy instanceof InterfaceDataTypeInteger) {
			return ((InterfaceDataTypeInteger)indy).getIntegerData();
		} else return null;
	}
	
	/**
	 * Set the verbosity level in the statistics module to the given value. See StatsParameter.
	 * @param vLev
	 */
	public void setVerbosityLevel(int vLev) {
		if (vLev >= 0 && vLev < proc.getStatistics().getStatisticsParameter().getOutputVerbosity().getTags().length) {
			proc.getStatistics().getStatisticsParameter().getOutputVerbosity().setSelectedTag(vLev);
		} else System.err.println("Invalid verbosity leveln in OptimizerRunnable.setVerbosityLevel!");
	}
}