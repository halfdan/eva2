package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;

/**
 * This nodes executes both arguments.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.06.2003
 * Time: 13:35:55
 * To change this template use Options | File Templates.
 */
public class GPNodeFlowExec2 extends AbstractGPNode implements java.io.Serializable {

    public GPNodeFlowExec2() {

    }

    public GPNodeFlowExec2(GPNodeFlowExec2 node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Exec2";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeFlowExec2(this);
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
        Object[] result = new Object[this.nodes.length];

        for (int i = 0; i < this.nodes.length; i++) {
            result[i] = this.nodes[i].evaluate(environment);
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "Exec2";
    }

//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        String result = "Exec2( ";
//        for (int i = 0; i < this.nodes.length; i++) result += this.nodes[i].getStringRepresentation() +" ";
//        result += ")";
//        return result;
//    }
}
