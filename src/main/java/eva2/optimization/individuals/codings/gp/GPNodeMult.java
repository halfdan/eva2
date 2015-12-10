package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;


/**
 * A multiplicator node taking two arguments.
 */
public class GPNodeMult extends AbstractGPNode implements java.io.Serializable {

    public GPNodeMult() {
    }

    public GPNodeMult(GPNodeMult node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Mult";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeMult(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 2;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 1;

        for (int i = 0; i < this.nodes.length; i++) {
            tmpObj = this.nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) {
                result *= (Double) tmpObj;
            } else {
                System.err.println("Unexpected type returned in evaluate for " + this.getClass().getSimpleName());
            }
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "*";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//    	return AbstractGPNode.makeStringRepresentation(nodes, "*");
//    }
}
