package eva2.optimization.individuals.codings.gp;


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

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodePi(this);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "pi";
    }
}
