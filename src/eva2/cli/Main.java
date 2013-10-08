package eva2.cli;

import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.util.annotation.Parameter;
import org.apache.commons.cli.*;
import eva2.optimization.OptimizationStateListener;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static void printProgressBar(int percent){
        StringBuilder bar = new StringBuilder("[");

        for(int i = 0; i < 50; i++){
            if( i < (percent/2)){
                bar.append("=");
            }else if( i == (percent/2)){
                bar.append(">");
            }else{
                bar.append(" ");
            }
        }

        bar.append("]   " + percent + "%     ");
        System.out.print("\r" + bar.toString());
    }

    public static Map<String, Class<? extends InterfaceOptimizer>> createOptimizerList() {
        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = new HashMap<String, Class<? extends InterfaceOptimizer>>();

        Reflections reflections = new Reflections("eva2.optimization.strategies");
        Set<Class<? extends InterfaceOptimizer>> optimizers = reflections.getSubTypesOf(InterfaceOptimizer.class);
        for(Class<? extends InterfaceOptimizer> optimizer : optimizers) {
            // We only want instantiable classes.
            if(optimizer.isInterface() || Modifier.isAbstract(optimizers.getClass().getModifiers())) {
                continue;
            }
            optimizerList.put(optimizer.getName(), optimizer);
        }
        return optimizerList;
    }

    public static void showHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("eva2", "", options, "", true);
    }

    public static void main(String[] args) {
        Options defaultOptions = createDefaultCommandLineOptions();

        CommandLineParser cliParser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = cliParser.parse(defaultOptions, args);
        } catch (ParseException e) {
            showHelp(defaultOptions);
            System.exit(-1);
        }

        if(commandLine.hasOption("help")) {
            String helpOption = commandLine.getOptionValue("help");
            if("optimizer".equals(helpOption)) {
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
        for(String name : optimizerList.keySet()) {
            System.out.printf("\t%s\n", name);
        }
    }

    private static void showProblemHelp() {
        System.out.println("Available Problems:");
        Reflections reflections = new Reflections("eva2.optimization.problems");
        Set<Class<? extends InterfaceOptimizationProblem>> problemsList = reflections.getSubTypesOf(InterfaceOptimizationProblem.class);
        for(Class<? extends InterfaceOptimizationProblem> problem : problemsList) {

            System.out.printf("\t%s\n", problem.getSimpleName());
        }
    }
}
