package eva2.cli;

import eva2.EvAInfo;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.modules.Processor;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.statistics.*;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))) {
            /* Show help for empty argument list or --help */
            printHelp();
            System.exit(-1);
        } else if (args.length == 2 && args[0].equals("--help")) {
            /* Show help for specific class */
            String className = args[1];
            try {
                Class<?> clazz = Class.forName(className);
                printHelpFor(clazz);
            } catch (ClassNotFoundException e) {
                System.out.printf("Class %s does not exist.\n", className);
            }
            System.exit(-1);
        } else {
            executeArguments(args);
        }
    }

    private static void printHelp() {
        System.out.printf("EvA2 version \"%s\"\n", EvAInfo.getVersion());
        System.out.println("Usage: java -cp EvA2.jar eva2.cli.Main [args...]\n");

        printHelpFor(OptimizationParameters.class);
        printHelpFor(StatisticsParameters.class);
    }

    private static void printHelpFor(Class<?> clazz) {
        System.out.println(clazz.getName() + "\n");
        if (clazz.isAnnotationPresent(Description.class)) {
            Description description = clazz.getAnnotation(Description.class);
            System.out.println(description.value());
        }

        ParameterGenerator generator = new ParameterGenerator(clazz, false);
        generator.generate();
        Map<String, List<Parameter>> paramList = generator.getParameterList();

        List<Parameter> parameters = paramList.get(clazz.getName());
        if (parameters.size() > 0) {
            System.out.println("Options:");

            for (Parameter key : parameters) {
                System.out.printf("\t--%s\t%s\n", key.getName(), key.getType().toString());
                System.out.printf("\t\t%s\n", key.getDescription());
            }
        }
    }

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

        YamlStatistics yamlStatistics = new YamlStatistics(statisticsParameters);

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

final class YamlStatistics implements InterfaceStatistics {
    private InterfaceStatisticsParameters statisticsParameters;
    private List<LinkedHashMap<String, Object>> runs;
    private LinkedHashMap<String, Object> currentRun;
    private ArrayList<Map<String, Object>> currentGenerations;
    private InterfaceOptimizationParameters currentParameters;
    private int currentGeneration;


    public YamlStatistics(InterfaceStatisticsParameters statisticsParameters) {
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
    public void printToTextListener(String s) {
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
