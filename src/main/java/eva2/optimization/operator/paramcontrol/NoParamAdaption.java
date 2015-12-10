package eva2.optimization.operator.paramcontrol;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * A dummy implementation which does not do any adaption.
 */
@Description("A dummy implementation which will not change parameters.")
public class NoParamAdaption implements ParamAdaption, Serializable {

    @Override
    public Object clone() {
        return new NoParamAdaption();
    }

    @Override
    public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
        return null;
    }

    @Override
    public String getControlledParam() {
        return null;
    }

    @Override
    public void finish(Object obj, Population pop) {
    }

    @Override
    public void init(Object obj, Population pop, Object[] initialValues) {
    }
}
