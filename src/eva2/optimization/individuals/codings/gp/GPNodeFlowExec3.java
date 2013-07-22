package eva2.optimization.individuals.codings.gp;

import eva2.optimization.problems.InterfaceProgramProblem;

/**
 * This node executes all three arguments.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.06.2003
 * Time: 13:36:08
 * To change this template use Options | File Templates.
 */
public class GPNodeFlowExec3 extends AbstractGPNode implements java.io.Serializable {

    public GPNodeFlowExec3() {

    }

    public GPNodeFlowExec3(GPNodeFlowExec3 node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Exec3";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new GPNodeFlowExec3(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 3;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        Object[] result = new Object[this.m_Nodes.length];

        for (int i = 0; i < this.m_Nodes.length; i++) {
            result[i] = this.m_Nodes[i].evaluate(environment);
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "Exec3";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        String result = "Exec3( ";
//        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
//        result += ")";
//        return result;
//    }
}