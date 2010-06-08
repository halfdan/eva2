package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;


/** 
 * A node for retrieving the absolute value
 * 
 */
public class GPNodeAbs extends AbstractGPNode implements java.io.Serializable {

    public GPNodeAbs() {
    }
    public GPNodeAbs(GPNodeAbs node) {
        this.cloneMembers(node);
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Abs";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeAbs(this);
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
        double result = 0;

        tmpObj = this.m_Nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) result += ((Double)tmpObj).doubleValue();
        Double ret = new Double(result);
        
        if (ret<0) return -ret; 
        else return ret;
    }

    @Override
    public String getOpIdentifier() {
    	return "abs";
    }
}
