package eva2.optimization.individuals.codings.gp;

import eva2.optimization.problems.InterfaceProgramProblem;

/**
 * A simple constant node with the value 1.
 */
public class GPNodeConst extends AbstractGPNode implements java.io.Serializable {
    double value = 1.;

    public GPNodeConst() {
    }

    public GPNodeConst(double val) {
        value = val;
    }

    public GPNodeConst(GPNodeConst node) {
        value = node.value;
        this.cloneMembers(node);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GPNodeConst) {
            GPNodeConst node = (GPNodeConst) obj;
            return (node.value == this.value);
        } else {
            return false;
        }
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "" + value;
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 0;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        return new Double(value);
    }

    @Override
    public String getOpIdentifier() {
        return getName();
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        return getName();
//    }

    @Override
    public Object clone() {
        return new GPNodeConst(this);
    }
}
