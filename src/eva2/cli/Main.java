package eva2.cli;

import eva2.optimization.strategies.DifferentialEvolution;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.util.annotation.Parameter;
import org.apache.commons.cli.*;
import eva2.optimization.OptimizationStateListener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Main implements OptimizationStateListener {



    private Options createCommandLineOptions() {
        Options opt = new Options();
        OptionGroup optGroup = new OptionGroup();
        return null;
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

    public static void main(String[] args) {

        Map<String, Class<? extends InterfaceOptimizer>> optimizerList = new HashMap<String, Class<? extends InterfaceOptimizer>>();

        optimizerList.add("Differential Evolution", eva2.optimization.strategies.DifferentialEvolution.class);
        optimizerList.add("Particle Swarm Optimization", eva2.optimization.strategies.ParticleSwarmOptimization.class);
        optimizerList.add("Genetic Algorithm", eva2.optimization.strategies.GeneticAlgorithm.class);
        optimizerList.add("Evolution Strategies", eva2.optimization.strategies.EvolutionStrategies.class);

        eva2.optimization.strategies.DifferentialEvolution de = new DifferentialEvolution();

        for(Field field : de.getClass().getDeclaredFields()) {
            Parameter p;
            if((p = field.getAnnotation(Parameter.class)) != null) {
                System.out.println(p.name() + " -> " + p.description());

            }
        }

        /*
        List<String> classes = GenericObjectEditor.getClassesFromClassPath("eva2.optimization.strategies.InterfaceOptimizer", null);
        for(String classA : classes) {
            System.out.println(classA);
        }

        List<String> problems = GenericObjectEditor.getClassesFromClassPath("eva2.optimization.problems.InterfaceOptimizationProblem", null);
        for(String problem : problems) {
            System.out.println(problem);
        } */
        for(int i = 0; i<= 100; i++) {
            printProgressBar(i);
        }
    }
}
