package eva2.optimization.operator.paramcontrol;

import eva2.optimization.population.Population;

/**
 * Adapt exactly one parameter with a generic method.
 *
 * @author mkron
 */
public interface ParamAdaption {

    Object clone();

    String getControlledParam();

    /**
     * Perform the adaption.
     *
     * @param iteration    iteration count at the time of the call, evaluations or generations, depending on the terminator
     * @param maxIteration
     * @return
     */
    Object calcValue(Object obj, Population pop, int iteration, int maxIteration);

    void init(Object obj, Population pop, Object[] initialValues);

    void finish(Object obj, Population pop);

}
