package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;

/**
 * A cos node with on argument.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 16:50:24
 * To change this template use Options | File Templates.
 */
public class GPNodeCos extends AbstractGPNode implements java.io.Serializable {

    public GPNodeCos() {
    }

    public GPNodeCos(GPNodeCos node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Cos";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new GPNodeCos(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 1;
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

        tmpObj = this.nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) {
            result = Math.sin(((Double) tmpObj).doubleValue());
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
        return "cos";
    }
}
