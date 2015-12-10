package eva2.optimization.operator.paramcontrol;

import eva2.gui.BeanInspector;
import eva2.optimization.population.Population;
import eva2.tools.math.Mathematics;

import java.io.Serializable;

/**
 * Simple linear adaption of a String property.
 *
 * @author mkron
 */
public abstract class AbstractLinearParamAdaption implements ParamAdaption, Serializable {
    private double startV = 0.7, endV = 0.2;

    public AbstractLinearParamAdaption(AbstractLinearParamAdaption o) {
        startV = o.startV;
        endV = o.endV;
    }

    public AbstractLinearParamAdaption(double startValue, double endValue) {
        this.startV = startValue;
        this.endV = endValue;
    }

    @Override
    public abstract Object clone();

    @Override
    public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
        return Mathematics.linearInterpolation(iteration, 0, maxIteration, startV, endV);
    }

    @Override
    public abstract String getControlledParam();

    public String controlledParamTipText() {
        return "The name of the parameter to be controlled by this adaption scheme.";
    }

    @Override
    public void init(Object obj, Population pop, Object[] initialValues) {
        BeanInspector.setMem(obj, getControlledParam(), startV);
    }

    @Override
    public void finish(Object obj, Population pop) {
    }

    public double getStartV() {
        return startV;
    }

    public void setStartV(double startV) {
        this.startV = startV;
    }

    public String startVTipText() {
        return "The initial value.";
    }

    public double getEndV() {
        return endV;
    }

    public void setEndV(double endV) {
        this.endV = endV;
    }

    public String endVTipText() {
        return "The final value.";
    }

    public String getName() {
        return "Lin.adpt." + getControlledParam() + "(" + startV + "-" + endV + ")";
    }

}
