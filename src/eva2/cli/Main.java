package eva2.cli;

import eva2.optimization.OptimizationStateListener;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import org.apache.commons.cli.*;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Main implements OptimizationStateListener {


    private static Options createDefaultCommandLineOptions() {
        Options opt = new Options();

        opt.addOption(OptionBuilder
                .withLongOpt("optimizer")
                .withDescription("Optimizer")
                .create("op")
        );

        opt.addOption("ps", "popsize", true, "Population size");
        opt.addOption(OptionBuilder
                .withLongOpt("help")
                .withDescription("Shows this help message or specific help for [optimizer]")
                .hasOptionalArgs(1)
                .create('h')
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
        //To change body of implemented methods use File | Settings | File Templates.
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
            // We only want instantiable classes.ya
            if (optimizer.isInterface() || Modifier.isAbstract(optimizer.getModifiers())) {
                continue;
            }
            optimizerList.put(optimizer.getSimpleName(), optimizer);
        }
        return optimizerList;
    }

    public static Map<String, Class<? extends InterfaceOptimizationProblem>> createProblemList() {
        Map<String, Class<? extends InterfaceOptimizationProblem>> problemList = new TreeMap<String, Class<? extends InterfaceOptimizationProblem>>();
        Reflections reflections = new Reflections("eva2.optimization.problems");
        Set<Class<? extends InterfaceOptimizationProblem>> problems = reflections.getSubTypesOf(InterfaceOptimizationProblem.class);
        for (Class<? extends InterfaceOptimizationProblem> problem : problems) {
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
        /**
         * Default command line options only require help or optimizer.
         * Later we build extended command line options depending on
         * the selected optimizer.
         */
        Options defaultOptions = createDefaultCommandLineOptions();

        /**
         * Parse default options.
         */
        CommandLineParser cliParser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = cliParser.parse(defaultOptions, args);
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
                showProblemHelp();
            } else {
                showHelp(defaultOptions);
            }
        }


    }

    private static void showOptimizerHelp() {
        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = createOptimizerList();

        System.out.println("Available Optimizers:");
        for (String name : optimizerList.keySet()) {
            System.out.printf("\t%s\n", name);
        }
    }

    private static void showProblemHelp() {
        Map<String, Class<? extends InterfaceOptimizationProblem>> problemList = createProblemList();

        System.out.println("Available Problems:");
        for (String name : problemList.keySet()) {
            System.out.printf("\t%s\n", name);
        }
    }
}
