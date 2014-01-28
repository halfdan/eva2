package eva2.optimization.individuals.codings.gp;

import eva2.optimization.problems.InterfaceProgramProblem;

/**
 * A sinus node taking one argument.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 16:50:14
 * To change this template use Options | File Templates.
 */
public class GPNodeSin extends AbstractGPNode implements java.io.Serializable {

    public GPNodeSin() {
    }

    public GPNodeSin(GPNodeSin node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Sin";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new GPNodeSin(this);
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
        return "sin";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        String result = "sin( ";
//        for (int i = 0; i < this.nodes.length; i++) result += this.nodes[i].getStringRepresentation() +" ";
//        result += ")";
//        return result;
//    }
}
