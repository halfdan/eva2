package eva2.server.go.operators.constraint;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;

/** This area constraint for parallelization is based on
 * the class type an individual belongs to.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 03.10.2004
 * Time: 15:07:36
 * To change this template use File | Settings | File Templates.
 */
public class ConstBelongsToDifferentClass implements InterfaceConstraint, java.io.Serializable {

    private double[]        m_Class;
    private double[][]      m_OtherClasses;
    private boolean         m_UsePhenotype      = false;

    public ConstBelongsToDifferentClass() {
    }
    public ConstBelongsToDifferentClass(double[] m, double[][] b, boolean p) {
        this.m_Class            = m;
        this.m_OtherClasses     = b;
        this.m_UsePhenotype     = p;
    }

    public ConstBelongsToDifferentClass(ConstBelongsToDifferentClass a) {
        this.m_UsePhenotype     = a.m_UsePhenotype;
        if (a.m_Class != null) {
            this.m_Class = new double[a.m_Class.length];
            System.arraycopy(a.m_Class, 0, this.m_Class, 0, a.m_Class.length);
        }
        if (a.m_OtherClasses != null) {
            this.m_OtherClasses = new double[a.m_OtherClasses.length][];
            for (int i = 0; i < a.m_OtherClasses.length; i++) {
                this.m_OtherClasses[i] = new double[a.m_OtherClasses[i].length];
                System.arraycopy(a.m_OtherClasses[i], 0, this.m_OtherClasses[i], 0, a.m_OtherClasses[i].length);
            }
        }
    }

    @Override
    public Object clone() {
        return (Object) new ConstBelongsToDifferentClass(this);
    }

    /** This method allows you wether or not a given individual
     * violates the constraints.
     * @param indy  The individual to check.
     * @return true if valid false else.
     */
    @Override
    public boolean isValid(AbstractEAIndividual indy) {
        double[] data;
        if (this.m_UsePhenotype && (indy instanceof InterfaceDataTypeDouble)) {
            data = ((InterfaceDataTypeDouble)indy).getDoubleData();
        } else {
            data = ((AbstractEAIndividual)indy).getFitness();
        }
        double distanceToMyClass = this.distance(data, this.m_Class);
        for (int i = 0; i < this.m_OtherClasses.length; i++) {
            if (distanceToMyClass > this.distance(data, this.m_OtherClasses[i])) return false;
        }
        return true;
    }

    /** This method calculates the distance between two double values
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
