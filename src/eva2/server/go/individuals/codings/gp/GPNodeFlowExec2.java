package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;

/** This nodes executes both arguments.
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
        if (obj instanceof GPNodeFlowExec2) {
            GPNodeFlowExec2 node = (GPNodeFlowExec2)obj;
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
        return "Exec2";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeFlowExec2(this);
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
        Object[]    result = new Object[this.m_Nodes.length];

        for (int i = 0; i < this.m_Nodes.length; i++) {
            result[i] = this.m_Nodes[i].evaluate(environment);
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
//        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
//        result += ")";
//        return result;
//    }
}
