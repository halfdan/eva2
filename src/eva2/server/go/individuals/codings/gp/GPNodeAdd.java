package eva2.server.go.individuals.codings.gp;


import java.util.ArrayList;

import eva2.server.go.problems.InterfaceProgramProblem;

/** A simple add node with two arguments.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 15:44:01
 * To change this template use Options | File Templates.
 */
public class GPNodeAdd extends AbstractGPNode implements java.io.Serializable {

    public GPNodeAdd() {
    }

    public GPNodeAdd(GPNodeAdd node) {
    	this.cloneMembers(node);
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Add";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeAdd(this);
    }

    /** This method will return the current arity
     * @return Arity.
     */
    public int getArity() {
        return 2;
    }

    /** This method will evaluate a given node
     * @param environment
     */
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 0;

        for (int i = 0; i < this.m_Nodes.length; i++) {
            tmpObj = this.m_Nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) result += ((Double)tmpObj).doubleValue();
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
    	return "+";
    }
}
