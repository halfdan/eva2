package eva2.server.go.individuals.codings.gp;


import eva2.server.go.problems.InterfaceProgramProblem;
import eva2.tools.Mathematics;

/** 
 * A simple product node with a single, possibly vectorial (array), argument.
 * 
 */
public class GPNodeProd extends AbstractGPNode implements java.io.Serializable {

    public GPNodeProd() {
    }

    public GPNodeProd(GPNodeProd node) {
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
        if (obj instanceof GPNodeProd) {
            GPNodeProd node = (GPNodeProd)obj;
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
        return "Prod";
    }

    public Object clone() {
        return (Object) new GPNodeProd(this);
    }

    public int getArity() {
        return 1;
    }

    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 1;

        for (int i = 0; i < this.m_Nodes.length; i++) {
            tmpObj = this.m_Nodes[i].evaluate(environment);
            if (tmpObj instanceof double[]) result*=Mathematics.product((double[])tmpObj);
            else if (tmpObj instanceof Double[]) {
            	Double[] vals = (Double[])tmpObj;
            	for (int j=0; j<vals.length; j++) result*=vals[j];
            } else if (tmpObj instanceof Double) result=(Double)tmpObj;
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
    	return "prod";
    }
}
