package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;


/**
 * A division node with two arguments and secure division. If
 * the second argument is absolute smaller than 0.00000001 the
 * result is 1.
 */
public class GPNodeDiv extends AbstractGPNode implements java.io.Serializable {
    private double lowerBorderForSec = 0.00000001;


    public GPNodeDiv() {

    }

    public GPNodeDiv(GPNodeDiv node) {
        this.lowerBorderForSec = node.lowerBorderForSec;
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Div";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeDiv(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 2;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 1;
        double tmpValue = 0;

        tmpObj = this.nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) {
            result = ((Double) tmpObj).doubleValue();
        }
        for (int i = 1; i < this.nodes.length; i++) {
            tmpObj = this.nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) {
                tmpValue = ((Double) tmpObj).doubleValue();
            }
            if (Math.abs(tmpValue) < this.lowerBorderForSec) {
                if (tmpValue < 0) {
                    tmpValue = -this.lowerBorderForSec;
                } else {
                    tmpValue = this.lowerBorderForSec;
                }
            }
            result /= tmpValue;
        }
        return new Double(result);
    }

    @Override
    public String getOpIdentifier() {
        return "/";
    }

//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//    	return AbstractGPNode.makeStringRepresentation(nodes, "/");
//    }
}
