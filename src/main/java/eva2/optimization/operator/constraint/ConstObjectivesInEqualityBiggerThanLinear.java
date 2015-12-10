package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;

/**
 * This area constraint for parallelization is based on
 * a line constraint.
 */
public class ConstObjectivesInEqualityBiggerThanLinear implements InterfaceConstraint, java.io.Serializable {

    private double m = 1.0, b = 0.0;

    public ConstObjectivesInEqualityBiggerThanLinear() {
    }

    public ConstObjectivesInEqualityBiggerThanLinear(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public ConstObjectivesInEqualityBiggerThanLinear(ConstObjectivesInEqualityBiggerThanLinear a) {
        this.b = a.b;
        this.m = a.m;
    }

    @Override
    public Object clone() {
        return new ConstObjectivesInEqualityBiggerThanLinear(this);
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
        return (this.m * d[0] + this.b) < d[1];
    }
}

