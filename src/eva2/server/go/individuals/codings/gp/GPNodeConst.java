package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;

/**
 * A simple constant node with the value 1.
 */
public class GPNodeConst extends AbstractGPNode implements java.io.Serializable {
	double value = 1.;
	
    public GPNodeConst() { }
    
    public GPNodeConst(double val) {
    	value = val;
    }

    public GPNodeConst(GPNodeConst node) {
    	value = node.value;
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
        if (obj instanceof GPNodeConst) {
            return true;
        } else {
            return false;
        }
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return ""+value;
    }

    /** This method will return the current arity
     * @return Arity.
     */
    public int getArity() {
        return 0;
    }

    /** This method will evaluate a given node
     * @param environment
     */
    public Object evaluate(InterfaceProgramProblem environment) {
        return new Double(value);
    }

    @Override
    public String getOpIdentifier() {
    	return getName();
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        return getName();
//    }

	@Override
	public Object clone() {
		return new GPNodeConst(this);
	}
}
