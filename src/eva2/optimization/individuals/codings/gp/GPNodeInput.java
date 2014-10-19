package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;

/**
 *
 */
public class GPNodeInput extends AbstractGPNode implements java.io.Serializable {
    private String identifier;
    private Object lastValue;

    /**
     * This method creates a new GPNodeInput
     */
    public GPNodeInput() {
        this.identifier = "X";
    }

    /**
     * This method creates a new GPNodeInput
     *
     * @param identifier The name of the sensor requested.
     */
    public GPNodeInput(String identifier) {
        this.identifier = identifier;
    }

    public GPNodeInput(GPNodeInput node) {
        this.identifier = node.identifier;
        this.cloneMembers(node);
    }

    public void setIdentifier(String str) {
        identifier = str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GPNodeInput) {
            GPNodeInput node = (GPNodeInput) obj;
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
        return "Sensor:" + this.identifier;
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeInput(this);
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
        lastValue = environment.getSensorValue(this.identifier);
        return lastValue;
    }

    /**
     * This method returns a string representation
     *
     * @return string
     */
    @Override
    public String getOpIdentifier() {
        if (this.lastValue == null) {
            return this.identifier;
        } else {
            if (this.lastValue instanceof Double) {
                double tmpD = ((Double) this.lastValue).doubleValue();
                tmpD = ((long) (tmpD * 10000.0 + ((tmpD >= 0.0) ? 0.5 : -0.5))) / 10000.0;
                return ("S:" + this.identifier + " = " + tmpD);
            } else {
                return ("S:" + this.identifier + " = " + this.lastValue.toString());
            }
        }
    }
}
