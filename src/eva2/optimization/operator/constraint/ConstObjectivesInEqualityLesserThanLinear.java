package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;

/**
 * This area constraint for parallelization is based on
 * a line constraint
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.09.2004
 * Time: 19:24:35
 * To change this template use File | Settings | File Templates.
 */
public class ConstObjectivesInEqualityLesserThanLinear implements InterfaceConstraint, java.io.Serializable {

    private double m, b;

    public ConstObjectivesInEqualityLesserThanLinear() {
    }

    public ConstObjectivesInEqualityLesserThanLinear(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public ConstObjectivesInEqualityLesserThanLinear(ConstObjectivesInEqualityLesserThanLinear a) {
        this.b = a.b;
        this.m = a.m;
    }

    @Override
    public Object clone() {
        return new ConstObjectivesInEqualityLesserThanLinear(this);
    }

    /**
     * This method allows you wether or not a given individual
     * violates the constraints.
     *
     * @param indy The individual to check.
     * @return true if valid false else.
     */
    @Override
    public boolean isValid(AbstractEAIndividual indy) {
        double[] d = indy.getFitness();
        if (d.length != 2) {
            return true;
        }
        if ((this.m * d[0] + this.b) > d[1]) {
            return true;
        } else {
            return false;
        }
    }
}