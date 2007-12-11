package javaeva.server.go.individuals.codings.gp;

import javaeva.server.go.problems.InterfaceProgramProblem;

/** This node puts the argument to the power of two.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.09.2003
 * Time: 14:32:48
 * To change this template use Options | File Templates.
 */
public class GPNodePow2 extends AbstractGPNode implements java.io.Serializable {

    public GPNodePow2() {
    }
    public GPNodePow2(GPNodePow2 node) {
        this.m_Depth    = node.m_Depth;
        this.m_Parent   = node.m_Parent;
        this.m_Nodes    = new AbstractGPNode[node.m_Nodes.length];
        for (int i = 0; i < node.m_Nodes.length; i++) this.m_Nodes[i] = (AbstractGPNode) node.m_Nodes[i].clone();
    }

    /** This method allows you to determine wehter or not two subtrees
     * are actually the same.
     * @param obj   The other subtree.
     * @return boolean if equal true else false.
     */
    public boolean equals(Object obj) {
        if (obj instanceof GPNodePow2) {
            GPNodePow2 node = (GPNodePow2)obj;
            if (this.m_Nodes.length != node.m_Nodes.length) return false;
            for (int i = 0; i < this.m_Nodes.length; i++) {
                if (!this.m_Nodes[i].equals(node.m_Nodes[i])) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Pow2";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodePow2(this);
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
        if (tmpObj instanceof Double) result = Math.pow(((Double)tmpObj).doubleValue(), 2);
        return new Double(result);
    }

    /** This method returns a string representation
     * @return string
     */
    public String getStringRepresentation() {
        String result = "pow( ";
        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
        result += ", 2)";
        return result;
    }
}
