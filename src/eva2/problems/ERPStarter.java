package eva2.problems;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.gui.Main;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.moso.MOSONoConvert;
import eva2.optimization.operator.moso.MOSOWeightedFitness;
import eva2.optimization.operator.mutation.MutateESRankMuCMA;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.StatisticsStandalone;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.BasicResourceLoader;
import eva2.tools.StringTools;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;

public class ERPStarter {

    /**
     * Start an external runtime problem with some basic configs. The
     * default variant uses equally weighs all objectives through an equally weighted sum.
     * Arguments: --csv <path-to-csv-config-file> --cmd <command-with-full-path> --maxEvals <maxEvals> [--multiObjective] [--gui]
     *
     * @param args
     */
    public static void main(String[] args) {
        ExternalRuntimeProblem erp;
        boolean useMultiObjective = false, startGUI = false;
        // define the number of evaluations
        int maxEvals = 5000;
        // prefix for data output file - set null to deactivate
        String outputFilePrefix = "erpTest";

        /** Argument handling ****************/
        String[] keys = new String[]{"--gui", "--multiObjective", "--csv", "--cmd", "--maxEvals"};
        int[] arities = new int[]{0, 0, 1, 1, 1};
        Object[] values = new Object[6];

        Integer[] unknownArgs = StringTools.parseArguments(args, keys, arities, values, true);

        if (unknownArgs.length > 0 || (values[2] == null) || (values[3] == null) || (values[4] == null)) {
            System.err.println("Missing or unrecognized command line options: ");
            for (int i = 0; i < unknownArgs.length; i++) {
                System.err.println("   " + args[unknownArgs[i]]);
            }
            System.err.println("Use with: --csv <path-to-csv-config-file> --cmd <command-with-full-path> --maxEvals <maxEvals> [--multiObjective] [--gui]");
            return;
        }
        System.out.println("Parsing file: " + values[2]);
        erp = parseCSV((String) values[2]);
        String cmd = (String) values[3];

        maxEvals = Integer.parseInt((String) values[4]);
        System.out.println("Setting maxEvals: " + maxEvals);
        useMultiObjective = (values[1] != null);
        startGUI = (values[0] != null);
        // set target function and working directory
        System.out.println("Setting target function command: " + cmd);
        erp.setCommand(cmd);
        int indexOfLastSlash = cmd.lastIndexOf(System.getProperty("file.separator"));
        erp.setWorkingDirectory(cmd.substring(0, indexOfLastSlash));

        /******************/
        // create optimization instance
        InterfaceOptimizer opt = null;
        if (useMultiObjective) {
            // this uses a multi-objective strategy
            erp.setMosoConverter(new MOSONoConvert());
            opt = OptimizerFactory.createMultiObjectiveEA(new GeneticAlgorithm(), 50, erp, null);
        } else {
            // this uses a single objective (5,20)-CMA-ES with equally weighted criteria
//			erp.setMosoConverter(new MOSOWeightedFitness());
            opt = OptimizerFactory.createEvolutionStrategy(5, 20, false, new MutateESRankMuCMA(), 1., new CrossoverESDefault(), 0., new SelectBestIndividuals(), erp, null);
        }

        System.out.println("Optimizer instance: " + BeanInspector.toString(opt));
        System.out.println("Problem instance: " + BeanInspector.toString(erp));
        //	Instantiate optimization
        OptimizationParameters params = new OptimizationParameters(opt, erp, new EvaluationTerminator(maxEvals));
        if (startGUI) {
            Main.initClientGUI(params, null, null, null);
        } else {
            OptimizerRunnable rnbl = new OptimizerRunnable(params, new StatisticsStandalone(outputFilePrefix, 1, InterfaceStatisticsParameters.OutputVerbosity.ALL, true), false);
            // actually start the optimization
            OptimizerFactory.optimize(rnbl);

            // some data output
            InterfaceSolutionSet solSet = opt.getAllSolutions();
            Population sols = solSet.getSolutions();
            System.out.println("*** Solutions found: " + sols.size());
            System.out.println("Best allover solution: " + AbstractEAIndividual.getDefaultStringRepresentation((AbstractEAIndividual) rnbl.getResult()));
            System.out.println("Best last solution: " + AbstractEAIndividual.getDefaultStringRepresentation(solSet.getCurrentPopulation().getBestEAIndividual()));
            System.out.println("Last solution set:");
            for (int i = 0; i < sols.size(); i++) {
                System.out.println(AbstractEAIndividual.getDefaultStringRepresentation(sols.getEAIndividual(i)));
            }
        }
    }

    public static ExternalRuntimeProblem parseCSV(String fname) {
//		return parseCSV(fname, 5, 7, 2, 4, 3, 9); //
        //String fname, int isVariableColIndex, int isObjectiveColIndex,
        //int lowerBoundIndex, int upperBoundIndex, int initialPosColIndex, int fitWeightsColIndex) {
        return parseCSV(fname, 5, 2, 4, 3, -1, 7, 9);
        //public static ExternalRuntimeProblem parseCSV(String fname, int isVariableColIndex,
        //int lowerBoundIndex, int upperBoundIndex, int initialPosColIndex, int initialPosBoxLenColIndex,
        //int isObjectiveColIndex, int fitWeightsColIndex) {
    }

    /**
     * Parse a csv file as in the example. Reads columns 2,3,4,5 where counting starts at zero.
     * Checks for the column entries at index 5 to contain a value of 1. These columns are interpreted as
     * target function variables with boundaries and seed. Note that all boundary values must contain
     * valid numbers. If a single seed value is empty, no initialization range is used.
     *
     * @param fname name of the csv data file
     * @return an instance of ExternalRuntimeProblem
     */
    public static ExternalRuntimeProblem parseCSV(String fname, int isVariableColIndex, int lowerBoundIndex,
                                                  int upperBoundIndex, int initialPosColIndex, int initialPosBoxLenColIndex,
                                                  int isObjectiveColIndex, int fitWeightsColIndex) {
        double defaultInitialBoxLenRatio = 0.05; // by default, the initialize range is 5% of the domain range in a component
        // this assumes that data colums are as in the example csv
        // parse only columns with index 2-9. Note that these will be reindexed to 0,1,2,3
        int[] filterCols = new int[7];
        filterCols[0] = isVariableColIndex;
        filterCols[1] = lowerBoundIndex;
        filterCols[2] = upperBoundIndex;
        filterCols[3] = initialPosColIndex;
        filterCols[4] = initialPosBoxLenColIndex;
        filterCols[5] = isObjectiveColIndex;
        filterCols[6] = fitWeightsColIndex;
//		double[][] dat = BasicResourceLoader.loadDoubleData(fname, null, ";", 1, -1, new int[]{2,3,4,5,6,7,8,9});
        double[][] dat = BasicResourceLoader.loadDoubleData(fname, null, ";", 1, -1, filterCols);
//		System.out.println(BeanInspector.toString(dat));
        double[][] filteredVars = ToolBox.filterBy(dat, 0, 1., 1.); // filter only those with a value of one in column isVariable
        double[][] range = ToolBox.getCols(filteredVars, 1, 2); // get columns with lower and upper bound
        double[][] initPos = ToolBox.getCols(filteredVars, 3); // get column with initial position center
        double[][] initBoxLen = ToolBox.getCols(filteredVars, 4); // get column with initialization range box length
        double[][] filteredObjectives = ToolBox.filterBy(dat, 5, 1., 1.); // filter those with a value of one in isObjective

        ExternalRuntimeProblem erp = new ExternalRuntimeProblem();
        if (filteredObjectives != null && (filteredObjectives.length > 1)) {
            double[][] weights = ToolBox.getCols(filteredObjectives, 6);
            if (Mathematics.areFinite(weights) < 0) {
                erp.setMosoConverter(new MOSOWeightedFitness(weights));
            }
        }
        erp.setProblemDimension(filteredVars.length);
        erp.setRange(range);
//		erp.setInitialRange(range);
        // produce an initial range around the seed position if available, otherwise use domain range
        double[][] initialRange = new double[filteredVars.length][2];
        for (int i = 0; i < filteredVars.length; i++) {
            if (Mathematics.isFinite(initPos[i][0])) { // if the initialize pos is valid (non NaN)...
                double dv = defaultInitialBoxLenRatio * (range[i][1] - range[i][0]); // default box length for initial interval
                if (Mathematics.isFinite(initBoxLen[i][0])) {
                    dv = initBoxLen[i][0];
                } // or box length defined in the file
                if (!Mathematics.isInRange(initPos[i][0], range[i][0], range[i][1])) {
                    System.err.println("Warning: initial seed is not in range in dim. " + i + " when parsing from " + fname);
                }
                initialRange[i][0] = initPos[i][0] - dv;
                initialRange[i][1] = initPos[i][0] + dv;
            } else {
                initialRange[i][0] = range[i][0];
                initialRange[i][1] = range[i][1];
            }
        }
        erp.setInitialRange(initialRange);
        return erp;
    }
}
