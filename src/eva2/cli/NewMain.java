package eva2.cli;

import eva2.optimization.go.InterfaceOptimizationParameters;

/**
 * Created by becker on 01.11.2014.
 */
public class NewMain {

    public static void main(String[] args) {
        InterfaceOptimizationParameters parameters = OptimizationBuilder.parseArguments(args);


    }
}
