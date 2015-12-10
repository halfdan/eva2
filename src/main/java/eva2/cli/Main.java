package eva2.cli;

import eva2.EvAInfo;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.Processor;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.*;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.ReflectPackage;
import eva2.tools.StringTools;
import eva2.util.annotation.Description;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))) {
            /* Show help for empty argument list or --help */
            printHelp();
            System.exit(-1);
        } else if (args.length == 1 && args[0].equals("--version")) {
            printVersion();
            System.exit(-1);
        } else if (args.length == 2 && args[0].equals("--help")) {
            /* Show help for specific class */
            String className = args[1];
            try {
                Class<?> clazz = Class.forName(className);
                printHelpFor(clazz);
            } catch (ClassNotFoundException e) {
                System.out.printf("No help available for %s.\n", className);
            }
            System.exit(-1);
        } else {
            executeArguments(args);
        }
    }

    /**
     * Print current version number.
     */
    private static void printVersion() {
        System.out.printf("EvA2 version \"%s\"\n", EvAInfo.getVersion());
    }

    private static void printHelp() {
        System.out.println("Usage: java -cp EvA2.jar eva2.cli.Main [args...]\n");

        printHelpFor(OptimizationParameters.class);
        printHelpFor(StatisticsParameters.class);
    }

    /**
     * Prints formatted help output for a specific class.
     *
     * If the class is an interface or abstract class additional information
     * on assignable subclasses will be shown.
     *
     * @param clazz The class to show help for.
     */
    private static void printHelpFor(Class<?> clazz) {
        System.out.println(clazz.getName() + "\n");
        if (clazz.isAnnotationPresent(Description.class)) {
            Description description = clazz.getAnnotation(Description.class);
            System.out.printf("%s\n\n", description.value());
        }

        /**
         * In case we have an Abstract class or Interface show available
         * sub types.
         */
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            Class<?>[] subTypes = ReflectPackage.getAssignableClassesInPackage(clazz.getPackage().getName(), clazz, true, true);
            if (subTypes.length > 0) {
                System.out.printf("Available types for %s\n\n", clazz.getName());
                for (Class<?> type : subTypes) {
                    if (Modifier.isAbstract(type.getModifiers())) {
                        continue;
                    }
                    Description description = clazz.getAnnotation(Description.class);
                    System.out.printf("\t\033[1m%s\033[0m (%s)\n", type.getName(), StringTools.cutClassName(type.getName()));
                    if (description != null) {
                        System.out.printf("\t\t%s", description.value());
                    } else {
                        System.out.println("\t\tNo description available.");
                    }
                }
                System.out.println();
            }
        }

        /**
         * Get available parameters for this class and list them with their
         * description.
         */
        ParameterGenerator generator = new ParameterGenerator(clazz, false);
        generator.generate();
        Map<String, List<Parameter>> paramList = generator.getParameterList();

        List<Parameter> parameters = paramList.get(clazz.getName());
        if (parameters.size() > 0) {
            System.out.println("Options:");

            for (Parameter key : parameters) {
                Class<?> type = key.getType();
                String typeDefinition;
                if (type.isEnum()) {
                    Enum[] enumConstants = (Enum[])type.getEnumConstants();
                    typeDefinition = "{";
                    for (int i = 0; i < enumConstants.length; i++) {
                        typeDefinition += enumConstants[i].name();
                        if (i != enumConstants.length - 1) {
                            typeDefinition += ",";
                        }
                    }
                    typeDefinition += "}";
                } else {
                    typeDefinition = key.getType().getName();
                }
                System.out.printf("\t\033[1m--%s\033[0m \033[4m%s\033[0m\n", key.getName(), typeDefinition);
                System.out.printf("\t\t%s\n", key.getDescription());
            }
        }
        System.out.print("\n\n");
    }

    /**
     * This method takes a set of command line arguments and tries to construct
     * OptimizationParameters and StatisticsParameters from it. It will use defaults if
     * not otherwise configured.
     *
     * @param args Command line arguments
     */
    private static void executeArguments(String[] args) {
        InterfaceOptimizationParameters parameters = OptimizationBuilder.parseOptimizerArguments(args);
        InterfaceStatisticsParameters statisticsParameters = OptimizationBuilder.parseStatisticsArguments(args);

        InterfaceTerminator terminator = parameters.getTerminator();
        InterfaceOptimizer optimizer = parameters.getOptimizer();
        InterfaceOptimizationProblem problem = parameters.getProblem();

        LinkedHashMap<String, Object> optimizationLog = new LinkedHashMap<>();
        // Meta parameters
        optimizationLog.put("populationSize", parameters.getOptimizer().getPopulation().getTargetSize());
        optimizationLog.put("numberOfRuns", statisticsParameters.getMultiRuns());
        optimizationLog.put("seed", parameters.getRandomSeed());
        optimizationLog.put("problem", parameters.getProblem());

        CommandLineStatistics yamlStatistics = new CommandLineStatistics(statisticsParameters);

        /**
         * Runs optimization
         */
        Processor optimizationProcessor = new Processor(yamlStatistics, parameters);
        optimizationProcessor.setSaveParams(false);
        optimizationProcessor.startOptimization();
        optimizationProcessor.runOptimizationOnce();

        /**
         * Get run statistics
         */
        optimizationLog.put("runs", yamlStatistics.getRuns());

        /**
         * Yaml configuration
         */
        DumperOptions options = new DumperOptions();
        options.setExplicitStart(true);
        options.setExplicitEnd(true);
        Yaml yaml = new Yaml();

        System.out.println(yaml.dump(optimizationLog));
    }
}

/**
 *
 */
final class CommandLineStatistics implements InterfaceStatistics {
    private InterfaceStatisticsParameters statisticsParameters;
    private List<LinkedHashMap<String, Object>> runs;
    private LinkedHashMap<String, Object> currentRun;
    private ArrayList<Map<String, Object>> currentGenerations;
    private InterfaceOptimizationParameters currentParameters;
    private int currentGeneration;


    public CommandLineStatistics(InterfaceStatisticsParameters statisticsParameters) {
        super();
        this.statisticsParameters = statisticsParameters;
        this.runs  = new ArrayList<>(statisticsParameters.getMultiRuns());
    }

    @Override
    public void startOptimizationPerformed(String infoString, int runNumber, InterfaceOptimizationParameters params, List<InterfaceAdditionalPopulationInformer> informerList) {
        this.currentRun = new LinkedHashMap<>();
        this.currentRun.put("name", infoString);
        this.currentRun.put("runNumber", runNumber + 1);
        this.currentGenerations = new ArrayList<>();
        this.currentGeneration = 0;
        this.currentParameters = params;
    }

    @Override
    public void stopOptimizationPerformed(boolean normal, String stopMessage) {
        this.currentRun.put("stopMessage", stopMessage);
        this.currentRun.put("totalFunctionCalls", this.currentParameters.getOptimizer().getPopulation().getFunctionCalls());
        // ToDo: Figure out a sane way to do this. Having multirun > 1 increases SnakeYAML memory consumption to beyond infinity
        //this.currentRun.put("generations", currentGenerations);
        Population pop = this.currentParameters.getOptimizer().getAllSolutions().getSolutions();
        this.currentRun.put("solution", pop.getBestEAIndividual().getDoublePosition().clone());
        this.currentRun.put("bestFitness", pop.getBestFitness().clone());
        this.currentRun.put("meanFitness", pop.getMeanFitness().clone());
        this.runs.add(currentRun);
    }

    @Override
    public void addDataListener(InterfaceStatisticsListener listener) {
        // We don't support that.
    }

    @Override
    public boolean removeDataListener(InterfaceStatisticsListener listener) {
        return false;
    }

    @Override
    public void addTextListener(InterfaceTextListener listener) {
        // We don't support that.
    }

    @Override
    public boolean removeTextListener(InterfaceTextListener listener) {
        return false;
    }

    @Override
    public void printToTextListener(String... s) {
        System.out.println(s);
    }

    @Override
    public void createNextGenerationPerformed(PopulationInterface pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList) {
        LinkedHashMap<String, Object> generation = new LinkedHashMap<>();
        generation.put("generation", currentGeneration);
        generation.put("bestFitness", pop.getBestFitness().clone());
        generation.put("meanFitness", pop.getMeanFitness().clone());
        generation.put("functionCalls", pop.getFunctionCalls());
        this.currentGenerations.add(generation);
        this.currentGeneration++;
    }

    @Override
    public InterfaceStatisticsParameters getStatisticsParameters() {
        return statisticsParameters;
    }

    @Override
    public IndividualInterface getRunBestSolution() {
        return null;
    }

    @Override
    public IndividualInterface getBestSolution() {
        return null;
    }

    @Override
    public double[] getBestFitness() {
        return new double[0];
    }

    @Override
    public void postProcessingPerformed(Population resultPop) {

    }

    public List<LinkedHashMap<String, Object>> getRuns() {
        return this.runs;
    }
}
