package eva2.server.go.individuals.codings.gp;


import eva2.server.go.problems.InterfaceProgramProblem;
import eva2.tools.math.Mathematics;

/** 
 * A simple product node with a single, possibly vectorial (array), argument.
 * 
 */
public class GPNodeProd extends AbstractGPNode implements java.io.Serializable {

    public GPNodeProd() {
    }

    public GPNodeProd(GPNodeProd node) {
    	this.cloneMembers(node);
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
