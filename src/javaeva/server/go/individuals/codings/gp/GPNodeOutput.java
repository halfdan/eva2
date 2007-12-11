package javaeva.server.go.individuals.codings.gp;

import javaeva.server.go.problems.InterfaceProgramProblem;

/** The node allows the program to give an output or to perform an action
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

    private String      m_Identifier;

    /** This method creates a new GPNodeInput
     * @param identifier    The name of the sensor requested.
     */
    public GPNodeOutput(String identifier) {
        this.m_Identifier   = identifier;
    }

    public GPNodeOutput(GPNodeOutput node) {
        this.m_Identifier   = node.m_Identifier;
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
        if (obj instanceof GPNodeOutput) {
            GPNodeOutput node = (GPNodeOutput)obj;
            if (!this.m_Identifier.equalsIgnoreCase(node.m_Identifier)) return false;
            return true;
        } else {
            return false;
        }
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Actuator:" +this.m_Identifier;
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeOutput(this);
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
        environment.setActuatorValue(this.m_Identifier, null);
        return null;
    }

    /** This method returns a string representation
     * @return string
     */
    public String getStringRepresentation() {
        return ("( A:" +this.m_Identifier + " )");
    }
}