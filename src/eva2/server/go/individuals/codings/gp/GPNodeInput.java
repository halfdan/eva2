package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;

/** This node is able to read a sensor value from the environment (e.g. the
 * problem) the sensor to read is given by the identifier and has to be
 * implemented in the problem definition.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 15:38:05
 * To change this template use Options | File Templates.
 */
public class GPNodeInput extends AbstractGPNode implements java.io.Serializable {
    private String      m_Identifier;
    private Object      lastValue;
    
    /** This method creates a new GPNodeInput
     */
    public GPNodeInput() {
        this.m_Identifier   = "X";
    }

    /** This method creates a new GPNodeInput
     * @param identifier    The name of the sensor requested.
     */
    public GPNodeInput(String identifier) {
        this.m_Identifier   = identifier;
    }

    public GPNodeInput(GPNodeInput node) {
        this.m_Identifier   = node.m_Identifier;
        this.cloneMembers(node);
    }
    
    public void setIdentifier(String str) {
    	m_Identifier=str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GPNodeInput) {
            GPNodeInput node = (GPNodeInput)obj;
            if (!this.m_Identifier.equalsIgnoreCase(node.m_Identifier)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    @Override
    public String getName() {
        return "Sensor:"+this.m_Identifier;
    }
    /** This method allows you to clone the Nodes
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new GPNodeInput(this);
    }

    /** This method will return the current arity
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 0;
    }

    /** This method will evaluate a given node
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        lastValue = environment.getSensorValue(this.m_Identifier);
        return lastValue;
    }

    /** This method returns a string representation
     * @return string
     */
    @Override
    public String getOpIdentifier() {
        if (this.lastValue == null) {
            return this.m_Identifier;
        }
        else {
            if (this.lastValue instanceof Double) {
                double tmpD = ((Double)this.lastValue).doubleValue();
                tmpD = ((long)(tmpD*10000.0 + ((tmpD>=0.0)?0.5:-0.5)))/10000.0;
                return ("S:" +this.m_Identifier + " = " + tmpD);
            } else {
                return ("S:" +this.m_Identifier + " = " + this.lastValue.toString());
            }
        }
    }
}
