package eva2.cli;

import com.google.gson.*;
import eva2.OptimizerFactory;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.enums.DEType;
import eva2.optimization.enums.PSOTopology;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.crossover.InterfaceCrossover;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateDefault;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectXProbRouletteWheel;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.FitnessValueTerminator;
import eva2.optimization.population.Population;
import eva2.problems.AbstractProblemDouble;
import eva2.problems.AbstractProblemDoubleOffset;
import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import org.apache.commons.cli.*;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Class for the EvA2 Command Line Interface
 *
 * The command line interface features a limited subset of the EvA2
 * optimization suite since it's difficult to parameterize all available
 * classes in EvA2 from the command line.
 * Supported Features:
 * - Select all optimization problems that implement InterfaceOptimizationProblem
 * - Select all optimizers that implement InterfaceOptimizer
 *   * Not all optimizers are configurable on the command line and will run
 *     with default parameters.
 *   * Optimizers can use the @Description / @Parameter annotations to specify
 *     parameters for CLI
 * - Configure default parameters
 *   * Population size
 *   * Number of optimization runs (multi-runs)
 * - Termination:
 *   * Not configurable!
 *   * Default: EvaluationTerminator(20000)
 */
public class Main implements OptimizationStateListener, InterfacePopulationChangedEventListener {
    private static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private int populationSize = 20;
    private int numberOfRuns = 1;
    private int dimension = 30;
    private long seed = System.currentTimeMillis();

    private AbstractProblemDoubleOffset problem;
    private InterfaceOptimizer optimizer;
    private InterfaceMutation mutator;
    private InterfaceCrossover crossover;
    private InterfaceSelection selection;

    private JsonObject jsonObject;
    private JsonArray optimizationRuns;
    private JsonArray generationsArray;

    private double[] fBias = { -4.5000000e+002, -4.5000000e+002, -4.5000000e+002, -4.5000000e+002, -3.1000000e+002,
            3.9000000e+002, -1.8000000e+002, -1.4000000e+002, -3.3000000e+002, -3.3000000e+002,  9.0000000e+001,
            -4.6000000e+002, -1.3000000e+002, -3.0000000e+002,  1.2000000e+002,  1.2000000e+002,  1.2000000e+002,
            1.0000000e+001,  1.0000000e+001,  1.0000000e+001,  3.6000000e+002,  3.6000000e+002,  3.6000000e+002,
            2.6000000e+002,  2.6000000e+002};

    /**
     * Creates a set of default options used in all optimizations.
     *
     * @return Options Default options used for optimizations
     */
    private Options createDefaultCommandLineOptions() {
        Options opt = new Options();

        opt.addOption(OptionBuilder
                .withLongOpt("optimizer")
                .withDescription("Optimizer")
                .hasArg()
                .create("op")
        );

        opt.addOption("ps", "popsize", true, "Population size");
        opt.addOption("n", "runs", true, "Number of runs to perform");
        opt.addOption("s", "seed", true, "Random seed");

        // Those two only make sense when used in an algorithm with mutation/crossover
        opt.addOption("pc", true, "Crossover Probability");
        opt.addOption("pm", true, "Mutation Probability");

        opt.addOption("mutator", true, "Mutator Operator");
        opt.addOption("crossover", true, "Crossover Operator");
        opt.addOption("selection", true, "Selection Operator");

        opt.addOption(OptionBuilder
                .withLongOpt("help")
                .withDescription("Shows this help message or specific help for [optimizer]")
                .hasOptionalArgs(1)
                .create('h')
        );

        opt.addOption(OptionBuilder
                .withLongOpt("problem")
                .withDescription("Select Optimization Problem to optimize.")
                .hasArg()
                .create('p')
        );
        opt.addOption("dim", true, "Problem Dimension");
        return opt;
    }



    @Override
    public void performedStop() {
        LOGGER.info("Optimization stopped.");
    }

    @Override
    public void performedStart(String infoString) {
        LOGGER.info("Optimization started.");
    }

    @Override
    public void performedRestart(String infoString) {
        LOGGER.info("Optimization restarted.");
    }

    @Override
    public void updateProgress(int percent, String msg) {
        printProgressBar(percent);
    }

    public static void printProgressBar(int percent) {
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 50; i++) {
            if (i < (percent / 2)) {
                bar.append("=");
            } else if (i == (percent / 2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append("]   " + percent + "%     ");
        System.out.print("\r" + bar.toString());
    }

    public static Map<String, Class<? extends InterfaceOptimizer>> createOptimizerList() {
        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = new TreeMap<>();

        Reflections reflections = new Reflections("eva2.optimization.strategies");
        Set<Class<? extends InterfaceOptimizer>> optimizers = reflections.getSubTypesOf(InterfaceOptimizer.class);
        for (Class<? extends InterfaceOptimizer> optimizer : optimizers) {
            // We only want instantiable classes
            if (optimizer.isInterface() || Modifier.isAbstract(optimizer.getModifiers())) {
                continue;
            }
            optimizerList.put(optimizer.getSimpleName(), optimizer);
        }
        return optimizerList;
    }

    public static Map<String, Class<? extends AbstractProblemDoubleOffset>> createProblemList() {
        Map<String, Class<? extends AbstractProblemDoubleOffset>> problemList = new TreeMap<>();
        Reflections reflections = new Reflections("eva2.problems");
        Set<Class<? extends AbstractProblemDoubleOffset>> problems = reflections.getSubTypesOf(AbstractProblemDoubleOffset.class);
        for (Class<? extends AbstractProblemDoubleOffset> problem : problems) {
            // We only want instantiable classes
            if (problem.isInterface() || Modifier.isAbstract(problem.getModifiers())) {
                continue;
            }
            problemList.put(problem.getSimpleName(), problem);
        }
        return problemList;
    }

    public static Map<String, Class<? extends InterfaceMutation>> createMutatorList() {
        Map<String, Class<? extends InterfaceMutation>> mutationList = new TreeMap<>();
        Reflections reflections = new Reflections("eva2.optimization.operator.mutation");
        Set<Class<? extends InterfaceMutation>> mutators = reflections.getSubTypesOf(InterfaceMutation.class);
        for (Class<? extends InterfaceMutation> mutator : mutators) {
            // We only want instantiable classes
            if (mutator.isInterface() || Modifier.isAbstract(mutator.getModifiers())) {
                continue;
            }
            mutationList.put(mutator.getSimpleName(), mutator);
        }
        return mutationList;
    }

    public static Map<String, Class<? extends InterfaceCrossover>> createCrossoverList() {
        Map<String, Class<? extends InterfaceCrossover>> crossoverList = new TreeMap<>();
        Reflections reflections = new Reflections("eva2.optimization.operator.crossover");
        Set<Class<? extends InterfaceCrossover>> crossovers = reflections.getSubTypesOf(InterfaceCrossover.class);
        for (Class<? extends InterfaceCrossover> crossover : crossovers) {
            // We only want instantiable classes
            if (crossover.isInterface() || Modifier.isAbstract(crossover.getModifiers())) {
                continue;
            }
            crossoverList.put(crossover.getSimpleName(), crossover);
        }
        return crossoverList;
    }

    public static Map<String, Class<? extends InterfaceSelection>> createSelectionList() {
        Map<String, Class<? extends InterfaceSelection>> selectionList = new TreeMap<>();
        Reflections reflections = new Reflections("eva2.optimization.operator.selection");
        Set<Class<? extends InterfaceSelection>> selections = reflections.getSubTypesOf(InterfaceSelection.class);
        for (Class<? extends InterfaceSelection> selection : selections) {
            // We only want instantiable classes
            if (selection.isInterface() || Modifier.isAbstract(selection.getModifiers())) {
                continue;
            }
            selectionList.put(selection.getSimpleName(), selection);
        }
        return selectionList;
    }



    public static void showHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar EvA2.jar", "Global Parameters", options, "", true);
    }

    public static void main(String[] args) {
        Main optimizationMain = new Main(args);
        optimizationMain.runOptimization();
    }

    public Main(String[] args)  {
        int index = Arrays.asList(args).indexOf("--");
        String[] programParams = args;
        String[] optimizerParams;

        if (index >= 0) {
            programParams = Arrays.copyOfRange(args, 0, index);
            optimizerParams = Arrays.copyOfRange(args, index + 1, args.length);
        } else {
            optimizerParams = new String[]{};
        }

        this.jsonObject = new JsonObject();
        this.optimizationRuns = new JsonArray();
        this.generationsArray = new JsonArray();

        /**
         * Default command line options only require help or optimizer.
         * Later we build extended command line options depending on
         * the selected optimizer.
         */
        Options defaultOptions = this.createDefaultCommandLineOptions();

        /**
         * Parse default options.
         */
        CommandLineParser cliParser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = cliParser.parse(defaultOptions, programParams);
        } catch (ParseException e) {
            showHelp(defaultOptions);
            System.exit(-1);
        }

        /**
         * Process help and help sub pages.
         */
        if (commandLine.hasOption("help")) {
            String helpOption = commandLine.getOptionValue("help");
            if (helpOption == null) {
                showHelp(defaultOptions);
            } else {
                switch (helpOption) {
                    case "optimizer":
                        showOptimizerHelp();
                        break;
                    case "problem":
                        listProblems();
                        break;
                    default:
                        showHelp(defaultOptions);
                        break;
                }
            }

            System.exit(0);
        }

        // OK, so we've got valid parameters - let's setup the optimizer and problem
        if (commandLine.hasOption("popsize")) {
            this.populationSize = Integer.parseInt(commandLine.getOptionValue("popsize"));
        }

        if (commandLine.hasOption("runs")) {
            this.numberOfRuns = Integer.parseInt(commandLine.getOptionValue("runs"));
        }

        if (commandLine.hasOption("seed")) {
            this.seed = Long.parseLong(commandLine.getOptionValue("seed"));
        }

        if (commandLine.hasOption("dim")) {
            this.dimension = Integer.parseInt(commandLine.getOptionValue("dim"));
        }

        if (commandLine.hasOption("problem")) {
            String problemName = commandLine.getOptionValue("problem");
            setProblemFromName(problemName);
            this.problem.setProblemDimension(this.dimension);
        } else {
            LOGGER.severe("No problem specified. Please specify a problem with '--problem'.");
            System.exit(-1);
        }


        if (commandLine.hasOption("mutator")) {
            String mutatorName = commandLine.getOptionValue("mutator");
            try {
                setMutatorFromName(mutatorName);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
        } else {
            this.mutator = new MutateDefault();
        }

        if (commandLine.hasOption("crossover")) {
            String crossoverName = commandLine.getOptionValue("crossover");
            try {
                setCrossoverFromName(crossoverName);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
        } else {
            this.crossover = new CrossoverESDefault();
        }

        if (commandLine.hasOption("selection")) {
            String selectionName = commandLine.getOptionValue("selection");
            try {
                setSelectionFromName(selectionName);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
        } else {
            this.selection = new SelectXProbRouletteWheel();
        }

        // Depends on mutator/crossover/selection being set
        if (commandLine.hasOption("optimizer")) {
            String optimizerName = commandLine.getOptionValue("optimizer");
            try {
                createOptimizerFromName(optimizerName, optimizerParams);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
        }

        this.jsonObject.addProperty("population_size", this.populationSize);
        this.jsonObject.addProperty("number_of_runs", this.numberOfRuns);
        this.jsonObject.addProperty("dimension", this.dimension);
        this.jsonObject.addProperty("seed", this.seed);

        JsonObject problemObject = new JsonObject();
        problemObject.addProperty("name", this.problem.getName());
        problemObject.addProperty("dimension", 30);
        this.jsonObject.add("problem", problemObject);
    }

    private void setMutatorFromName(String mutatorName) {
        Map<String, Class<? extends InterfaceMutation>> mutatorList = createMutatorList();

        Class<? extends InterfaceMutation> mutator = mutatorList.get(mutatorName);
        try {
            this.mutator = mutator.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setCrossoverFromName(String crossoverName) {
        Map<String, Class<? extends InterfaceCrossover>> crossoverList = createCrossoverList();

        Class<? extends InterfaceCrossover> crossover = crossoverList.get(crossoverName);
        try {
            this.crossover = crossover.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setSelectionFromName(String selectionName) {
        Map<String, Class<? extends InterfaceSelection>> selectionList = createSelectionList();

        Class<? extends InterfaceSelection> selection = selectionList.get(selectionName);
        try {
            this.selection = selection.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will create the various optimizers that are supported on the CLI.
     * It's a really messy process since neither Java nor args4j/apache-cli can handle
     * complex object parameters. The trick here is that all parameters after the
     * double-dash (--) are treated as parameters for the optimization algorithm.
     *
     * @param optimizerName The name of the optimizer.
     * @param optimizerParams The remaining command line parameters.
     * @throws Exception
     */
    private void createOptimizerFromName(String optimizerName, String[] optimizerParams) throws Exception {
        Options opt = new Options();
        CommandLineParser cliParser = new BasicParser();
        CommandLine commandLine = null;

        switch(optimizerName) {
            case "DifferentialEvolution": {
                opt.addOption("F", true, "Differential Weight");
                opt.addOption("CR", true, "Crossover Rate");
                opt.addOption("DEType", true, "DE Type ()");
                /**
                 * Parse default options.
                 */
                try {
                    commandLine = cliParser.parse(opt, optimizerParams);
                } catch (ParseException e) {
                    showHelp(opt);
                    System.exit(-1);
                }


                double f = 0.8, lambda = 0.6, cr = 0.6;
                if (commandLine.hasOption("F")) {
                    f = Double.parseDouble(commandLine.getOptionValue("F"));
                }

                if (commandLine.hasOption("CR")) {
                    cr = Double.parseDouble(commandLine.getOptionValue("CR"));
                }

                this.optimizer = OptimizerFactory.createDifferentialEvolution(this.problem, this.populationSize, f, lambda, cr, this);

                if (commandLine.hasOption("DEType")) {
                    ((DifferentialEvolution)this.optimizer).setDEType(
                            DEType.getFromId(
                                    Integer.parseInt(commandLine.getOptionValue("DEType"))
                            )
                    );
                }

                break;
            }
            case "GeneticAlgorithm": {
                double pm = 0.01, pc = 0.5;
                opt.addOption("pm", true, "Mutation Probability");
                opt.addOption("pc", true, "Crossover Probability");

                /**
                 * Parse default options.
                 */
                try {
                    commandLine = cliParser.parse(opt, optimizerParams);
                } catch (ParseException e) {
                    showHelp(opt);
                    System.exit(-1);
                }

                if (commandLine.hasOption("pm")) {
                    pm = Double.parseDouble(commandLine.getOptionValue("pm"));
                }

                if (commandLine.hasOption("pc")) {
                    pc = Double.parseDouble(commandLine.getOptionValue("pc"));
                }

                this.optimizer = OptimizerFactory.createGeneticAlgorithm(mutator, pm, crossover, pc, selection, this.populationSize, this.problem, this);
                break;
            }
            case "ParticleSwarmOptimization": {
                double phi1 = 2.05, phi2 = 2.05, speedLimit = 0.1;
                int topoRange = 2;
                PSOTopology selectedTopology = PSOTopology.star;

                opt.addOption("speedLimit", true, "Speed Limit");
                opt.addOption("topology", true, "Particle Swarm Topology (0-7)");
                opt.addOption("phi1", true, "Phi 1");
                opt.addOption("phi2", true, "Phi 2");

                /**
                 * Parse default options.
                 */
                try {
                    commandLine = cliParser.parse(opt, optimizerParams);
                } catch (ParseException e) {
                    showHelp(opt);
                    System.exit(-1);
                }

                if (commandLine.hasOption("phi1")) {
                    phi1 = Double.parseDouble(commandLine.getOptionValue("phi1"));
                }

                if (commandLine.hasOption("phi2")) {
                    phi2 = Double.parseDouble(commandLine.getOptionValue("phi2"));
                }

                if (commandLine.hasOption("topology")) {
                    selectedTopology = PSOTopology.getFromId(Integer.parseInt(commandLine.getOptionValue("topology")));
                }

                if (commandLine.hasOption("speedLimit")) {
                    speedLimit = Double.parseDouble(commandLine.getOptionValue("speedLimit"));
                }

                this.optimizer = OptimizerFactory.createParticleSwarmOptimization(problem, this.populationSize, phi1, phi2, speedLimit, selectedTopology, topoRange, this);
                break;
            }
            case "EvolutionStrategies": {
                double pm, pc;
                int mu = 5, lambda = 20;
                boolean plusStrategy = false;

                opt.addOption("pm", true, "Mutation Probability");
                opt.addOption("pc", true, "Crossover Probability");
                opt.addOption("mu", true, "Mu");
                opt.addOption("lambda", true, "Lambda");
                opt.addOption("plusStrategy", true, "Whether to use the plus or comma strategy.");

                /**
                 * Parse default options.
                 */
                try {
                    commandLine = cliParser.parse(opt, optimizerParams);
                } catch (ParseException e) {
                    showHelp(opt);
                    System.exit(-1);
                }

                if (commandLine.hasOption("pm")) {
                    pm = Double.parseDouble(commandLine.getOptionValue("pm"));
                } else {
                    pm = 0.01;
                }

                if (commandLine.hasOption("pc")) {
                    pc = Double.parseDouble(commandLine.getOptionValue("pc"));
                } else {
                    pc = 0.9;
                }

                if (commandLine.hasOption("mu")) {
                    mu = Integer.parseInt(commandLine.getOptionValue("mu"));
                }

                if (commandLine.hasOption("lambda")) {
                    lambda = Integer.parseInt(commandLine.getOptionValue("lambda"));
                }

                if (commandLine.hasOption("plusStrategy")) {
                    plusStrategy = Boolean.parseBoolean(commandLine.getOptionValue("plusStrategy"));
                }

                this.optimizer = OptimizerFactory.createEvolutionStrategy(mu, lambda, plusStrategy, this.mutator, pm, this.crossover, pc, this.selection, problem, this);
                break;
            }
            default:
                throw new Exception("Unsupported Optimizer");
        }
    }

    private void setProblemFromName(String problemName) {
        Map<String, Class<? extends AbstractProblemDoubleOffset>> problemList = createProblemList();

        Class<? extends AbstractProblemDoubleOffset> problem = problemList.get(problemName);
        try {
            this.problem = problem.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //setCECDefaults(this.problem);
    }

    private void setCECDefaults(AbstractProblemDouble problem) {
        switch(problem.getName()) {
            case "F1-Problem": // F1: Shifted Sphere
                this.problem.setDefaultRange(100);
                this.problem.setYOffset(fBias[0]);
                break;
            case "F2-Problem": // F6: Shifted Rosenbrock's Function
                this.problem.setDefaultRange(100);
                this.problem.setYOffset(fBias[5]);
                break;
            case "F5-Problem": // F2: Schwefel's 1.2
                this.problem.setDefaultRange(100);
                this.problem.setYOffset(fBias[1]);
                break;
            case "F6-Problem": // F9: Shifted Rastrigin's Function
                this.problem.setDefaultRange(5);
                this.problem.setYOffset(fBias[8]);
                break;
            default:
                LOGGER.info("No CEC'05 default parameters for this problem found.");
                break;
        }
    }

    /**
     * Executes the optimization and outputs a JSON document to the command line
     * with the statistics of the optimization run(s).
     */
    private void runOptimization() {
        for(int i = 0; i < this.numberOfRuns; i++) {
            // Terminate after 10000 function evaluations OR after reaching a fitness < 0.1
            OptimizerFactory.setEvaluationTerminator(500000);
            //OptimizerFactory.setTerminator(new FitnessValueTerminator(new double[]{0.0001}));
            OptimizerFactory.addTerminator(new FitnessValueTerminator(new double[]{0.0001}), CombinedTerminator.OR);

            LOGGER.log(Level.INFO, "Running {0}", optimizer.getName());

            OptimizationParameters params = OptimizerFactory.makeParams(optimizer, this.populationSize, this.problem, this.seed, OptimizerFactory.getTerminator());
            double[] result = OptimizerFactory.optimizeToDouble(params);

            JsonObject optimizationDetails = new JsonObject();
            optimizationDetails.addProperty("total_time", 1.0);
            optimizationDetails.addProperty("total_function_calls", optimizer.getPopulation().getFunctionCalls());
            optimizationDetails.addProperty("termination_criteria", OptimizerFactory.terminatedBecause());
            optimizationDetails.add("generations", this.generationsArray);

            JsonArray solutionArray = new JsonArray();
            for(double val : result) {
                solutionArray.add(new JsonPrimitive(val));
            }
            optimizationDetails.add("solution", solutionArray);

            this.optimizationRuns.add(optimizationDetails);

            // Needs to be re-created here.
            this.generationsArray = new JsonArray();
        }

        this.jsonObject.add("runs", this.optimizationRuns);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this.jsonObject));
    }

    private static void showOptimizerHelp() {
        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = createOptimizerList();

        System.out.println("Available Optimizers:");
        for (String name : optimizerList.keySet()) {
            System.out.printf("%s\n", name);
        }
    }

    private static void listProblems() {
        Map<String, Class<? extends AbstractProblemDoubleOffset>> problemList = createProblemList();

        System.out.println("Available Problems:");
        for (String name : problemList.keySet()) {
            System.out.printf("%s\n", name);
        }
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals("NextGenerationPerformed")) {
            InterfaceOptimizer optimizer = (InterfaceOptimizer)source;
            Population population = optimizer.getPopulation();

            JsonObject newGeneration = new JsonObject();
            newGeneration.addProperty("generation", population.getGeneration());
            newGeneration.addProperty("function_calls", population.getFunctionCalls());

            JsonArray bestFitness = new JsonArray();
            for(double val : population.getBestFitness()) {
                bestFitness.add(new JsonPrimitive(val));
            }
            newGeneration.add("best_fitness", bestFitness);

            JsonArray meanFitness = new JsonArray();
            for(double val : population.getMeanFitness()) {
                meanFitness.add(new JsonPrimitive(val));
            }
            newGeneration.add("mean_fitness", meanFitness);
            //System.out.println(newGeneration.toString());
            this.generationsArray.add(newGeneration);
        }

    }
}
