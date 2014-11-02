package eva2.optimization.operator.paramcontrol;

import eva2.util.annotation.Description;

import java.io.Serializable;


/**
 * Linearly adapt a specific target String parameter.
 */
@Description("Simple linear parameter adaption.")
public class LinearParamAdaption extends AbstractLinearParamAdaption
        implements InterfaceHasUpperDoubleBound, GenericParamAdaption, Serializable {
    String target = "undefinedParameter";

    public LinearParamAdaption() {
        super(0.7, 0.2);
        target = "undefinedParameter";
    }

    public LinearParamAdaption(LinearParamAdaption o) {
        super(o);
        target = o.target;
    }

    public LinearParamAdaption(String target, double startValue, double endValue) {
        super(startValue, endValue);
        this.target = target;
    }

    @Override
    public Object clone() {
        return new LinearParamAdaption(this);
    }

    @Override
    public String getControlledParam() {
        return target;
    }

    @Override
    public void setControlledParam(String target) {
        this.target = target;
    }

    public String[] customPropertyOrder() {
        return new String[]{"startV", "endV"};
    }

    /**
     * Return the larger value of the start and end value.
     *
     * @return
     */
    @Override
    public double getUpperBnd() {
        return Math.max(getEndV(), getStartV());
    }

    /**
     * Set the larger one of start- or end-value to the given value. If they are
     * equal, both are set.
     *
     * @param u
     */
    @Override
    public void SetUpperBnd(double u) {
        if (getEndV() == getStartV()) {
            setEndV(u);
            setStartV(u);
        } else if (getEndV() > getStartV()) { // end value is larger
            if (u < getStartV()) {
                System.err.println("Warning, changing direction of linear adaption!");
            }
            setEndV(u);
        } else { // start value is larger
            if (u < getEndV()) {
                System.err.println("Warning, changing direction of linear adaption!");
            }
            setStartV(u);
        }
    }
}
