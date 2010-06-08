package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;

/** This argument puts the argument to the power of three.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.09.2003
 * Time: 14:33:03
 * To change this template use Options | File Templates.
 */
public class GPNodePow3 extends AbstractGPNode implements java.io.Serializable {

    public GPNodePow3() {
    }
    public GPNodePow3(GPNodePow3 node) {
    	this.cloneMembers(node);
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Pow3";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodePow3(this);
    }

    /** This method will return the current arity
     * @return Arity.
     */
    public int getArity() {
        return 1;
    }

    /** This method will evaluate a given node
     * @param environment
     */
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 1;

        tmpObj = this.m_Nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) result = Math.pow(((Double)tmpObj).doubleValue(), 3);
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
    	return "pow3";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        String result = "pow( ";
//        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
//        result += ", 3)";
//        return result;
//    }
}