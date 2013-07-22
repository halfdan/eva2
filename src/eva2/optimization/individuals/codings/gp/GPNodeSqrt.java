package eva2.optimization.individuals.codings.gp;

import eva2.optimization.problems.InterfaceProgramProblem;

/**
 * A square root node taking one argument. Secure operation is guaranteed
 * by using the absolute value only.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.09.2003
 * Time: 14:33:13
 * To change this template use Options | File Templates.
 */
public class GPNodeSqrt extends AbstractGPNode implements java.io.Serializable {

    public GPNodeSqrt() {
    }

    public GPNodeSqrt(GPNodeSqrt node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Sqrt";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new GPNodeSqrt(this);
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

        tmpObj = this.m_Nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) {
            result = Math.sqrt(Math.abs(((Double) tmpObj).doubleValue()));
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
        return "sqrt";
    }

//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        String result = "sqrt( ";
//        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
//        result += ")";
//        return result;
//    }
}