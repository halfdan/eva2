package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;

/**
 * This area constraint for parallelization is based on
 * the class type an individual belongs to.
 */
public class ConstBelongsToDifferentClass implements InterfaceConstraint, java.io.Serializable {

    private double[] classes;
    private double[][] otherClasses;
    private boolean usePhenotype = false;

    public ConstBelongsToDifferentClass() {
    }

    public ConstBelongsToDifferentClass(double[] m, double[][] b, boolean p) {
        this.classes = m;
        this.otherClasses = b;
        this.usePhenotype = p;
    }

    public ConstBelongsToDifferentClass(ConstBelongsToDifferentClass a) {
        this.usePhenotype = a.usePhenotype;
        if (a.classes != null) {
            this.classes = new double[a.classes.length];
            System.arraycopy(a.classes, 0, this.classes, 0, a.classes.length);
        }
        if (a.otherClasses != null) {
            this.otherClasses = new double[a.otherClasses.length][];
            for (int i = 0; i < a.otherClasses.length; i++) {
                this.otherClasses[i] = new double[a.otherClasses[i].length];
                System.arraycopy(a.otherClasses[i], 0, this.otherClasses[i], 0, a.otherClasses[i].length);
            }
        }
    }

    @Override
    public Object clone() {
        return (Object) new ConstBelongsToDifferentClass(this);
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
        double[] data;
        if (this.usePhenotype && (indy instanceof InterfaceDataTypeDouble)) {
            data = ((InterfaceDataTypeDouble) indy).getDoubleData();
        } else {
            data = ((AbstractEAIndividual) indy).getFitness();
        }
        double distanceToMyClass = this.distance(data, this.classes);
        for (int i = 0; i < this.otherClasses.length; i++) {
            if (distanceToMyClass > this.distance(data, this.otherClasses[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method calculates the distance between two double values
     *
     * @param d1
     * @param d2
     * @return The scalar distances between d1 and d2
     */
    private double distance(double[] d1, double[] d2) {
        // @todo basically i should use InterfaceClustering belongsToSpecies() here

        double result = 0;

        for (int i = 0; i < d1.length; i++) {
            result += Math.pow(d1[i] - d2[i], 2);
        }
        result = Math.sqrt(result);
        return result;
    }
}
