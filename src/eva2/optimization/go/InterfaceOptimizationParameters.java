package eva2.optimization.go;

import eva2.optimization.operator.postprocess.InterfacePostProcessParams;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;

/**
 *
 */
public interface InterfaceOptimizationParameters {
    /**
     * This method allows you to serialize the current parameters into a *.ser file
     */
    void saveInstance();

    /**
     * This method returns the name
     *
     * @return string
     */
    String getName();

    /**
     * This methods allow you to set and get the Seed for the Random Number Generator.
     *
     * @param x Long seed.
     */
    void setSeed(long x);

    long getSeed();

    /**
     * This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     *
     * @param term The new terminator
     */
    void setTerminator(InterfaceTerminator term);

    InterfaceTerminator getTerminator();

    /**
     * This method allows you to set the current optimizing algorithm
     *
     * @param optimizer The new optimizing algorithm
     */
    void setOptimizer(InterfaceOptimizer optimizer);

    InterfaceOptimizer getOptimizer();

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    void setProblem(InterfaceOptimizationProblem problem);

    InterfaceOptimizationProblem getProblem();

    InterfacePostProcessParams getPostProcessParams();

    void setPostProcessParams(InterfacePostProcessParams ppp);

    void setDoPostProcessing(boolean doPP);

    /**
     * Give an instance which should be informed about elements which are additional informers.
     *
     * @param o
     * @see InterfaceAdditionalPopulationInformer
     */
    void addInformableInstance(InterfaceNotifyOnInformers o);

    boolean removeInformableInstance(InterfaceNotifyOnInformers o);
}
