package eva2.optimization.operator.paramcontrol;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Dummy implementation. This class is ignored by the Processor. Parameters will not be changed.
 */
@Description("Parameters will not be changed.")
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
}
