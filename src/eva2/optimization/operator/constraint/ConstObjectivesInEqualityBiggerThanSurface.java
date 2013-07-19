package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.AbstractEAIndividual;

/** This area constraint for parallelization is based on
 * a surface constraint
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.02.2005
 * Time: 16:57:24
 * To change this template use File | Settings | File Templates.
 */
public class ConstObjectivesInEqualityBiggerThanSurface implements InterfaceConstraint, java.io.Serializable {

    private double[]      base, norm;

    public ConstObjectivesInEqualityBiggerThanSurface() {
    }
    public ConstObjectivesInEqualityBiggerThanSurface(double[] base, double[] norm) {
        this.base  = base;
        this.norm  = norm;
    }

    public ConstObjectivesInEqualityBiggerThanSurface(ConstObjectivesInEqualityBiggerThanSurface a) {
        this.base      = a.base;
        this.norm      = a.norm;
    }

    @Override
    public Object clone() {
        return (Object) new ConstObjectivesInEqualityBiggerThanSurface(this);
    }

    /** This method allows you wether or not a given individual
     * violates the constraints.
     * @param indy  The individual to check.
     * @return true if valid false else.
     */
    @Override
    public boolean isValid(AbstractEAIndividual indy) {
        double[] d = indy.getFitness();
        if (this.getScalarProduct(norm, this.getSubstraction(d, base)) >= 0) {
            return true;
        }
        else {
            return false;
        }
    }

    private double[] getSubstraction(double[] a, double[] b) {
        double[] result = new double[3];
        result[0] = a[0] - b[0];
        result[1] = a[1] - b[1];
        result[2] = a[2] - b[2];
        return result;
    }

    /** This method returns the scalar product of two vectors
     * @param a The first vector
     * @param b The second vector
     * @return The scalar product of a and b
     */
    private double getScalarProduct(double[] a, double[] b) {
        return (a[0]*b[0] + a[1]*b[1] + a[2]*b[2]);
    }

    /** This method will return a normalized vector
     * @param a     The vector to normalize
     * @return A normalized version of the input vector
     */
    private double[] getNormalized(double[] a) {
        double[] result = new double[a.length];
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum = Math.pow(a[i], 2);
        }
        sum = Math.sqrt(sum);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i]/sum;
        }
        return result;
    }
}