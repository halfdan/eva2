package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;


/** A multiplicator node taking two arguments.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:00:16
 * To change this template use Options | File Templates.
 */
public class GPNodeMult extends AbstractGPNode implements java.io.Serializable {

    public GPNodeMult() {
    }
    public GPNodeMult(GPNodeMult node) {
    	this.cloneMembers(node);
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Mult";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeMult(this);
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
        double result = 1;

        for (int i = 0; i < this.m_Nodes.length; i++) {
            tmpObj = this.m_Nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) result = result * ((Double)tmpObj).doubleValue();
            else System.err.println("Unexpected type returned in evaluate for "+this.getClass().getSimpleName());
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
    	return "*";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//    	return AbstractGPNode.makeStringRepresentation(m_Nodes, "*");
//    }
}
