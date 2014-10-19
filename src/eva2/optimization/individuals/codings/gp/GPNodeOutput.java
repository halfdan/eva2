package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;

/**
 * The node allows the program to give an output or to perform an action
 * in the enviroment simulated in the problem. The type of action is given
 * by the identifier and has to be implemented by the problem definition.
 * See the artificial ant problem for an example.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 16.06.2003
 * Time: 13:32:40
 * To change this template use Options | File Templates.
 */
public class GPNodeOutput extends AbstractGPNode implements java.io.Serializable {

    private String identifier;

    public GPNodeOutput() {
        this.identifier = "Y";
    }

    /**
     * This method creates a new GPNodeInput
     *
     * @param identifier The name of the sensor requested.
     */
    public GPNodeOutput(String identifier) {
        this.identifier = identifier;
    }

    public GPNodeOutput(GPNodeOutput node) {
        this.identifier = node.identifier;
        this.cloneMembers(node);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GPNodeOutput) {
            GPNodeOutput node = (GPNodeOutput) obj;
            return this.identifier.equalsIgnoreCase(node.identifier);
        } else {
            return false;
        }
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Actuator:" + this.identifier;
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeOutput(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 0;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        environment.setActuatorValue(this.identifier, null);
        return null;
    }

    @Override
    public String getOpIdentifier() {
        return "OUT:" + identifier;
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//        return ("( A:" +this.identifier + " )");
//    }
}