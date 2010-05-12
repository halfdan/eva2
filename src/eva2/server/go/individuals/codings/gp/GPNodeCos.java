package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;

/** A cos node with on argument.
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
        this.m_Depth    = node.m_Depth;
        this.m_Parent   = node.m_Parent;
        this.m_Nodes    = new AbstractGPNode[node.m_Nodes.length];
        for (int i = 0; i < node.m_Nodes.length; i++) this.m_Nodes[i] = (AbstractGPNode) node.m_Nodes[i].clone();
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Cos";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeCos(this);
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
        if (tmpObj instanceof Double) result = Math.sin(((Double)tmpObj).doubleValue());
        return new Double(result);
    }
    
    @Override
    public String getOpIdentifier() {
    	return "cos";
    }
}
