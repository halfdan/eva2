package eva2.problems;

import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.statistics.InterfaceStatisticsParameters;

import java.io.PrintWriter;
import java.io.StringWriter;

final class WaitForEvARunnable implements Runnable {
    OptimizerRunnable runnable;
    MatlabProblem mp;

    public WaitForEvARunnable(OptimizerRunnable runnable, MatlabProblem mp) {
        this.runnable = runnable;
        this.mp = mp;
        mp.log("Created WaitForEvARunnable " + this + "\n");
    }

    @Override
    public void run() {
        if (runnable != null) {
            mp.log("\nStarting optimize runnable!\n");
            synchronized (runnable) {
                try {
                    // whole optimization thread goes in here
                    new Thread(runnable).start();
                    mp.log("Started optimize thread\n");
                    runnable.wait();
                    // wait for the runnable to finish
                    mp.log("runnable continues...\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mp.log("WaitForEvARunnable was interrupted with " + e.getMessage());
                }
            }
            try {
                mp.log("runnable.getDoubleSolution: " + BeanInspector.toString(runnable.getDoubleSolution()) + "\n");
                mp.log("runnable.getIntegerSolution: " + BeanInspector.toString(runnable.getIntegerSolution()) + "\n");
                mp.log("getAllSols best: " + AbstractEAIndividual.getDefaultDataString(runnable.getOptimizationParameters().getOptimizer().getAllSolutions().getSolutions().getBestEAIndividual()) + "\n");
                mp.log("\n");
                // write results back to matlab
                mp.exportResultToMatlab(runnable);
                mp.exportResultPopulationToMatlab(runnable.getResultPopulation());
                mp.log("reported results.\n");
                mp.notifyFinished();
                mp.log("notified finish.\n");
                if (mp.verbosityLevel != InterfaceStatisticsParameters.OutputVerbosity.NONE) {
                    System.out.println("Optimization finished: " + mp.getInfoString());
                }
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
            mp.log("notifying finish...\n");
            mp.notifyFinished();
            mp.log("notified finish.\n");
        }

    }

}