package eva2.optimization.operator.paramcontrol;

import eva2.optimization.population.Population;

import java.io.Serializable;

/**
 * Dummy implementation. This class is ignored by the Processor. Parameters will not be changed.
 *
 * @author mkron
 */
public class ConstantParameters extends AbstractParameterControl implements Serializable {
    public ConstantParameters() {
    }

    public ConstantParameters(ConstantParameters o) {
        super(o);
    }

    @Override
    public Object clone() {
        return new ConstantParameters(this);
    }

    @Override
    public String[] getControlledParameters() {
        return null;
    }

    @Override
    public Object[] getValues(Object obj, Population pop, int iteration, int maxIteration) {
        return null;
    }

    @Override
    public void updateParameters(Object obj) {
    }

    public static String globalInfo() {
        return "Parameters will not be changed.";
    }
}
