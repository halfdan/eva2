package eva2.cli;

import eva2.OptimizerFactory;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.statistics.InterfaceStatisticsParameters;

/**
 * Created by becker on 01.11.2014.
 */
public class Main {

    public static void main(String[] args) {
        InterfaceOptimizationParameters parameters = OptimizationBuilder.parseOptimizerArguments(args);
        InterfaceStatisticsParameters statisticsParameters = OptimizationBuilder.parseStatisticsArguments(args);


        double[] result = OptimizerFactory.optimizeToDouble((OptimizationParameters)parameters);



    }
}
