package eva2.server.go.individuals.codings.gp;


/**
 * A simple constant node with the value 1.
 */
public class GPNodeOne extends GPNodeConst implements java.io.Serializable {
    public GPNodeOne() {
    	super(1.);
    }
    
    public GPNodeOne(GPNodeOne node) {
    	super(node);
	}
    
    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeOne(this);
    }
}
