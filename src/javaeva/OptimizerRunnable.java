package javaeva;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.BitSet;

import javaeva.gui.BeanInspector;
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.individuals.InterfaceDataTypeInteger;
import javaeva.server.go.operators.terminators.CombinedTerminator;
import javaeva.server.modules.GOParameters;
import javaeva.server.modules.Processor;
import javaeva.server.stat.StatisticsStandalone;

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
	
	public OptimizerRunnable(GOParameters params, String outputFilePrefix) {
		this(params, outputFilePrefix, false);
	}
		
	public OptimizerRunnable(GOParameters params, String outputFilePrefix, boolean restart) {
		proc = new Processor(new StatisticsStandalone(outputFilePrefix), null, params);
		doRestart = restart;
	}
	
	public InterfaceGOParameters getGOParams() {
		return proc.getGOParams();
	}
	
	public void run() {
		isFinished = false;
		try {
			proc.setSaveParams(false);
			if (doRestart) proc.restartOpt();
			else proc.startOpt();
			proc.runOptOnce();
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
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public void stopOpt() {
		proc.stopOpt();
	}
	
	public IndividualInterface getSolution() {
		return proc.getStatistics().getBestSolution();
	}

	public int getProgress() {
		return proc.getGOParams().getOptimizer().getPopulation().getFunctionCalls();
	}

	public String terminatedBecause() {
		if (isFinished) {
			InterfaceTerminator term = proc.getGOParams().getTerminator();
			return term.terminatedBecause(proc.getGOParams().getOptimizer().getPopulation());
		} else return "Not yet terminated";
	}
	
	public double[] getDoubleSolution() {
		IndividualInterface indy = getSolution();
		if (indy instanceof InterfaceDataTypeDouble) {
			return ((InterfaceDataTypeDouble)indy).getDoubleData();
		} else return null;
	}
	
	public BitSet getBinarySolution() {
		IndividualInterface indy = getSolution();
		if (indy instanceof InterfaceDataTypeBinary) {
			return ((InterfaceDataTypeBinary)indy).getBinaryData();
		} else return null;
	}
	
	public int[] getIntegerSolution() {
		IndividualInterface indy = getSolution();
		if (indy instanceof InterfaceDataTypeInteger) {
			return ((InterfaceDataTypeInteger)indy).getIntegerData();
		} else return null;
	}
}