package javaeva.server.go.individuals.codings.gp;

import javaeva.server.go.problems.InterfaceProgramProblem;

import java.util.ArrayList;


/** This gives an abstract node, with default functionality for get and set methods.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 13:12:53
 * To change this template use Options | File Templates.
 */
public abstract class AbstractGPNode implements InterfaceProgram, java.io.Serializable {
    protected AbstractGPNode        m_Parent;
    protected AbstractGPNode[]      m_Nodes = new AbstractGPNode[0];
    protected int                   m_Depth = 0;

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public abstract Object clone();

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public abstract String getName();

    /** This method will evaluate a given node
     * @param environment
     */
    public abstract Object evaluate(InterfaceProgramProblem environment);

    /** This method will return the current arity
     * @return Arity.
     */
    public abstract int getArity();

    /** This method returns a string representation
     * @return string
     */
    public abstract String getStringRepresentation();

    /** This method returns the depth of the current node
     * @return The depth.
     */
    public int getDepth() {
        return this.m_Depth;
    }

    /** This method allows you to set the depth of the current node
     * @param depth     The depth of the node
     */
    public void setDepth(int depth) {
        this.m_Depth = depth;
    }

    /** This method allows you to fetch a certain node given by the index.
     * @param index     Index specifing the requested node.
     * @return The requested node.
     */
    public AbstractGPNode getNode(int index) {
        return this.m_Nodes[index];
    }

    /** This method allows you to set a node specified by the index
     * @param node      The new node
     * @param index     The position where it is to be inserted.
     */
    public void setNode(AbstractGPNode node, int index) {
        node.setParent(this);
        node.setDepth(this.m_Depth+1);
        this.m_Nodes[index] = node;
    }

    /** This method allows you to set a node specified by the index
     * @param newnode      The new node.
     * @param oldnode      The old node.
     */
    public void setNode(AbstractGPNode newnode, AbstractGPNode oldnode) {
        newnode.setParent(this);
        newnode.setDepth(this.m_Depth+1);
        for (int i = 0; i < this.m_Nodes.length; i++) if (this.m_Nodes[i].equals(oldnode)) this.m_Nodes[i] = newnode;
    }
    /** This method returns all nodes begining with the current node.
     * @param ListOfNodes   This ArrayList will contain all nodes
     */
    public void addNodesTo(ArrayList ListOfNodes) {
        ListOfNodes.add(this);
        for (int i = 0; i < this.m_Nodes.length; i++) this.m_Nodes[i].addNodesTo(ListOfNodes);
    }

    /** This method allows you to set the parent of the node
     * @param parent    The new parent
     */
    public void setParent(AbstractGPNode parent) {
        this.m_Parent = parent;
    }

    /** This method allows you to get the parent of the node
     */
    public AbstractGPNode getParent() {
        return this.m_Parent;
    }

    /** This method allows to fully connect a following nodes to thier parents
     * @param parent    The parent
     */
    public void connect(AbstractGPNode parent) {
        this.m_Parent = parent;
        if (parent != null) this.m_Depth = this.m_Parent.getDepth()+1;
        else this.m_Depth = 0;
        for (int i = 0; i < this.m_Nodes.length; i++) this.m_Nodes[i].connect(this);
    }

    /** This method will simply init the array of nodes
     */
    public void initNodeArray() {
        this.m_Nodes = new AbstractGPNode[this.getArity()];
    }

    /** This method performs a full init but with max depth
     * @param area  The allowed function area.
     * @param depth The absolute target depth.
     */
    public void initFull(GPArea area, int depth) {
        this.m_Nodes = new AbstractGPNode[this.getArity()];
        for (int i = 0; i < this.m_Nodes.length; i++) {
            if (this.m_Depth+1 >= depth) this.m_Nodes[i] = (AbstractGPNode)area.getRandomNodeWithArity(0).clone();
            else this.m_Nodes[i] = (AbstractGPNode)area.getRandomNonTerminal().clone();
            this.m_Nodes[i].setDepth(this.m_Depth+1);
            this.m_Nodes[i].setParent(this);
            this.m_Nodes[i].initFull(area, depth);
        }
    }

    /** This method performs a grow init but with max depth
     * @param area  The allowed function area.
     * @param depth The absolute target depth.
     */
    public void initGrow(GPArea area, int depth) {
        this.m_Nodes = new AbstractGPNode[this.getArity()];
        for (int i = 0; i < this.m_Nodes.length; i++) {
            if (this.m_Depth+1 >= depth) this.m_Nodes[i] = (AbstractGPNode)area.getRandomNodeWithArity(0).clone();
            else this.m_Nodes[i] = (AbstractGPNode)area.getRandomNode().clone();
            this.m_Nodes[i].setDepth(this.m_Depth+1);
            this.m_Nodes[i].setParent(this);
            this.m_Nodes[i].initGrow(area, depth);
        }
    }

    /** This method allows you to get the overall number of nodes
     * @return Number of nodes.
     */
    public int getNumberOfNodes() {
        int result = 1;
        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getNumberOfNodes();
        return result;
    }

    /** This method will return the max depth of the tree
     * @return The max depth.
     */
    public int getMaxDepth() {
        int result = this.m_Depth;
        for (int i = 0; i < this.m_Nodes.length; i++) {
            if (this.m_Nodes[i] != null) result = Math.max(result, this.m_Nodes[i].getMaxDepth());
        }
        return result;
    }

    /** This method will check if maxdepth is violated
     * @param maxDepth     The max depth.
     * @return True if MaxDepth is violated
     */
    public boolean isMaxDepthViolated(int maxDepth) {
        if (maxDepth < this.getMaxDepth()) return true;
        else return false;
//        if (depth > this.m_Depth) return false;
//        else {
//            boolean result = true;
//            for (int i = 0; i < this.m_Nodes.length; i++) {
//                result = result & this.m_Nodes[i].isMaxDepthViolated(depth);
//            }
//            return result;
//        }
    }

    /** This method will repair the maxDepth constraint
     * @param depth     The max depth.
     */
    public void repairMaxDepth(GPArea area, int depth) {
        if (this.m_Depth == depth-1) {
            // in this case i need to check wether or not my
            // followup nodes are terminals
            for (int i = 0; i < this.m_Nodes.length; i++) {
                if (this.m_Nodes[i].getArity() != 0) {
                    // replace this node with a new node
                    this.m_Nodes[i] = (AbstractGPNode)area.getRandomNodeWithArity(0).clone();
                    this.m_Nodes[i].setDepth(this.m_Depth+1);
                    this.m_Nodes[i].setParent(this);
                }
            }
        } else {
            // else i call the method on my followup nodes
            for (int i = 0; i < this.m_Nodes.length; i++) this.m_Nodes[i].repairMaxDepth(area, depth);
        }

    }

    /** This method allows you to determine wehter or not two subtrees
     * are actually the same.
     * @param obj   The other subtree.
     * @return boolean if equal true else false.
     */
    public abstract boolean equals(Object obj);
}
