package eva2.optimization.individuals.codings.gp;


import eva2.problems.InterfaceProgramProblem;

/**
 * A simple add node with two arguments.
 */
public class GPNodeAdd extends AbstractGPNode implements java.io.Serializable {

    public GPNodeAdd() {
    }

    public GPNodeAdd(GPNodeAdd node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Add";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeAdd(this);
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
        double result = 0;

        for (int i = 0; i < this.nodes.length; i++) {
            tmpObj = this.nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) {
                result += (Double) tmpObj;
            }
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "+";
    }
}
