package eva2.examples;

import eva2.OptimizerFactory;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.problems.FM0Problem;

import java.util.List;

public class TestingCbnPostProc {
    public static void main(String[] args) {
        // a simple bimodal target function, two optima near (1.7,0) and (-1.44/0)
        FM0Problem fm0 = new FM0Problem();
        AbstractEAIndividual best;
        List<AbstractEAIndividual> ppSols;

        OptimizationParameters esParams = OptimizerFactory.standardCbnES(fm0);
        esParams.setTerminator(new EvaluationTerminator(2000));
        esParams.setSeed(0);
        best = (AbstractEAIndividual) OptimizerFactory.optimizeToInd(esParams, null);

        System.out.println(esParams.getTerminator().lastTerminationMessage() + "\nFound solution: "
                + AbstractEAIndividual.getDefaultDataString(best));

        // post-process with clustering only
        ppSols = OptimizerFactory.postProcessIndVec(new PostProcessParams(0, 0.1, 5));
        System.out.println("After clustering: ");
        for (AbstractEAIndividual indy : ppSols) {
            System.out.println(AbstractEAIndividual.getDefaultDataString(indy));
        }

        // post-process with clustering and hill climbing
        ppSols = OptimizerFactory.postProcessIndVec(new PostProcessParams(1000, 0.1, 5));
        System.out.println("After clustering and local refinement: ");
        for (AbstractEAIndividual indy : ppSols) {
            System.out.println(AbstractEAIndividual.getDefaultDataString(indy));
        }
    }

}