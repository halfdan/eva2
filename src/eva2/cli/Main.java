package eva2.cli;

import eva2.OptimizerFactory;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.enums.DETypeEnum;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.FitnessValueTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import com.google.gson.*;
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
    private long seed = System.currentTimeMillis();
    private AbstractOptimizationProblem problem;
    private InterfaceOptimizer optimizer;
    private JsonObject jsonObject;
    private JsonArray optimizationRuns;
    private JsonArray generationsArray;

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
        return opt;
    }



    @Override
    public void performedStop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void performedStart(String infoString) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void performedRestart(String infoString) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = new TreeMap<String, Class<? extends InterfaceOptimizer>>();

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

    public static Map<String, Class<? extends AbstractOptimizationProblem>> createProblemList() {
        Map<String, Class<? extends AbstractOptimizationProblem>> problemList = new TreeMap<String, Class<? extends AbstractOptimizationProblem>>();
        Reflections reflections = new Reflections("eva2.optimization.problems");
        Set<Class<? extends AbstractOptimizationProblem>> problems = reflections.getSubTypesOf(AbstractOptimizationProblem.class);
        for (Class<? extends AbstractOptimizationProblem> problem : problems) {
            // We only want instantiable classes
            if (problem.isInterface() || Modifier.isAbstract(problem.getModifiers())) {
                continue;
            }
            problemList.put(problem.getSimpleName(), problem);
        }
        return problemList;
    }

    public static void showHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("eva2", "", options, "", true);
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
            if ("optimizer".equals(helpOption)) {
                showOptimizerHelp();
            } else if ("problem".equals(helpOption)) {
                listProblems();
            } else {
                showHelp(defaultOptions);
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

        if (commandLine.hasOption("problem")) {
            String problemName = commandLine.getOptionValue("problem");
            setProblemFromName(problemName);
        }

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
        this.jsonObject.addProperty("seed", this.seed);

        JsonObject problemObject = new JsonObject();
        problemObject.addProperty("name", this.problem.getName());
        problemObject.addProperty("dimension", 30);
        this.jsonObject.add("problem", problemObject);

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
            case "DifferentialEvolution":
                System.out.println("DE");
                opt.addOption("F", true, "Differential Weight");
                opt.addOption("CR", true, "Crossover Rate");
                opt.addOption("DEType", true, "DE Type (");
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
                            DETypeEnum.getFromId(
                                    Integer.parseInt(commandLine.getOptionValue("DEType"))
                            )
                    );
                }

                break;
            case "GeneticAlgorithm":
                System.out.println("Genetic Algorithm");
                break;
            case "ParticleSwarmOptimization":

                break;
            default:
                throw new Exception("Unsupported Optimizer");
        }
    }

    private void setProblemFromName(String problemName) {
        Map<String, Class<? extends AbstractOptimizationProblem>> problemList = createProblemList();

        Class<? extends AbstractOptimizationProblem> problem = problemList.get(problemName);
        try {
            this.problem = problem.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void runOptimization() {
        for(int i = 0; i < this.numberOfRuns; i++) {

            this.generationsArray = new JsonArray();
            // Terminate after 10000 function evaluations OR after reaching a fitness < 0.1
            OptimizerFactory.setEvaluationTerminator(50000);
            OptimizerFactory.addTerminator(new FitnessValueTerminator(new double[]{0.01}), CombinedTerminator.OR);

            LOGGER.log(Level.INFO, "Running {0}", "Differential Evolution");

            OptimizationParameters params = OptimizerFactory.makeParams(optimizer, this.populationSize, this.problem);
            double[] result = OptimizerFactory.optimizeToDouble(params);

            // This is stupid - why isn't there a way to wait for the optimization to finish?
            while(OptimizerFactory.terminatedBecause().equals("Not yet terminated")) {
                // wait
            }

            JsonObject optimizationDetails = new JsonObject();
            optimizationDetails.addProperty("total_time", 1.0);
            optimizationDetails.addProperty("total_function_calls", optimizer.getPopulation().getFunctionCalls());
            optimizationDetails.addProperty("termination_criteria", OptimizerFactory.terminatedBecause());
            optimizationDetails.add("generations", this.generationsArray);

            this.optimizationRuns.add(optimizationDetails);

            System.out.println(OptimizerFactory.terminatedBecause());
            System.out.println(optimizer.getPopulation().getFunctionCalls());
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
        Map<String, Class<? extends AbstractOptimizationProblem>> problemList = createProblemList();

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
            System.out.println(newGeneration.toString());
            //this.generationsArray.add(newGeneration);
        }

    }
}
