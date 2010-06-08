package eva2.server.go.individuals.codings.gp;


/**
 * A simple constant node with the value 1.
 */
public class GPNodePi extends GPNodeConst implements java.io.Serializable {
    public GPNodePi() {
    	super(Math.PI);
    }
    
    public GPNodePi(GPNodePi node) {
    	super(node);
	}

	/** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodePi(this);
    }
    
    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "pi";
    }
}
