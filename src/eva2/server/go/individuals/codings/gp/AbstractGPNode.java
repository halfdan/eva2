package eva2.server.go.individuals.codings.gp;


import java.util.ArrayList;
import java.util.Vector;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.problems.InterfaceProgramProblem;
import eva2.tools.Pair;


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
    public String getStringRepresentation() {
    	StringBuffer sb = new StringBuffer();
    	AbstractGPNode.appendStringRepresentation(this, sb);
    	return sb.toString();
    }
    
    private static void appendStringRepresentation(AbstractGPNode node, StringBuffer sbuf) {
    	String op = node.getOpIdentifier(); 
    	sbuf.append(op);
    	if (node.getArity()>0) {
    		sbuf.append("(");
    		for (int i = 0; i < node.m_Nodes.length; i++) {
    			sbuf.append(node.m_Nodes[i].getStringRepresentation());
    			if (i<node.m_Nodes.length-1) sbuf.append(", ");
    		}
    		sbuf.append(")");
    	}
    }
    
    /** 
     * This method returns a string identifier of the operator name - it should be unique. 
     * 
     * @return string
     */
    public abstract String getOpIdentifier();
    
    /**
     * Small parser for GP nodes from a String. Format must be (nearly) equivalent to what makeStringRepresentation produces.
     * This mainly means prefix notation with braces and commata, such as in:
     *     AbstractGPNode node = AbstractGPNode.parseFromString("+(2.0,cos(*(pi,pi)))");
     *     System.out.println("Parsed GPNode: " + node.getStringRepresentation());
     *     node = AbstractGPNode.parseFromString(node.getStringRepresentation());
     * @param str
     * @param nodeTypes
     * @return
     */
    public static Pair<AbstractGPNode,String> parseFromString(String str, Vector<AbstractGPNode> nodeTypes) {
    	if (nodeTypes == null) {
    		ArrayList<String>cls = GenericObjectEditor.getClassesFromClassPath(AbstractGPNode.class.getCanonicalName());
    		nodeTypes = new Vector<AbstractGPNode>(cls.size());
    		for (int i=0; i<cls.size(); i++) {
    			try {
    				AbstractGPNode node = (AbstractGPNode)Class.forName((String)cls.get(i)).newInstance();
    				nodeTypes.add(node);
    			} catch(Exception e) {}
    		}
			nodeTypes.add(new GPNodeInput("X"));
    	}
    	if (nodeTypes.size()>0) {
    		Vector<AbstractGPNode> matchSet=AbstractGPNode.match(nodeTypes, str, true, true);
    		if (matchSet.size()==0) {
    			// try to read constant
    			Pair<Double, String> nextState=readDouble(str, true);
    			if (nextState != null) {
    				return new Pair<AbstractGPNode,String>(new GPNodeConst(nextState.head().doubleValue()), nextState.tail());
    			} else {
        			System.err.println("String has unknown prefix: " + str);
    			}
    		}
    		else if (matchSet.size()>1) System.err.println("String has ambiguous prefix: " + str + " -- " + BeanInspector.toString(matchSet));
    		else { // exactly one match:
    			AbstractGPNode currentNode = (AbstractGPNode)matchSet.get(0).clone();
//    			System.out.println("Found match: " + currentNode.getOpIdentifier() + "/" + currentNode.getArity());
    			int cutFront=currentNode.getOpIdentifier().length();
    			String restStr;
    			if (currentNode.getArity()==0) {
    				restStr = str.substring(cutFront).trim();
    				if (currentNode instanceof GPNodeInput) {
    					Pair<Double, String> nextState=readDouble(restStr, false);
    					if (nextState!=null) {
    						((GPNodeInput)currentNode).setIdentifier("X"+((int)nextState.head().doubleValue()));
        					restStr = nextState.tail();
    					} else {
    						((GPNodeInput)currentNode).setIdentifier("X");
    					}
    				}
    				return new Pair<AbstractGPNode,String>(currentNode,restStr);
    			} else {
					restStr = str.substring(cutFront+1).trim(); // cut this op and front brace
					currentNode.m_Nodes = new AbstractGPNode[currentNode.getArity()];
    				for (int i=0; i<currentNode.getArity(); i++) {
    					Pair<AbstractGPNode,String> nextState = parseFromString(restStr, nodeTypes);
    					currentNode.m_Nodes[i]=nextState.head();
    					restStr=nextState.tail().substring(1).trim(); // cut comma or brace
    				}
//    				System.out.println("lacking rest: " + restStr);
    				return new Pair<AbstractGPNode,String>(currentNode, restStr);
    			}
    		}
    	} return null;
    }
    
    private static Pair<Double, String> readDouble(String str, boolean expect) {
		String firstArg;
		int argLen = str.indexOf(',');
		if (argLen<0) argLen = str.indexOf(')');
		else {
			int firstBrace = str.indexOf(')');
			if ((firstBrace >= 0) && (firstBrace<argLen)) argLen = firstBrace;
		}
		firstArg=str.substring(0,argLen);
		try {
			Double d=Double.parseDouble(firstArg);
			return new Pair<Double,String>(d, str.substring(firstArg.length()));
		} catch(NumberFormatException e) {
			if (expect) System.err.println("String has unknown prefix: " + str);
			return null;
		}
	}

	/** 
     * This method returns a string representation
     * @return string
     */
    public static String makeStringRepresentation(AbstractGPNode[] nodes, String op) {
    	if (nodes.length==0) return op;
    	else if (nodes.length==1) return op+"(" + nodes[0].getStringRepresentation()+")";
    	else {
    		String result = "( "+nodes[0].getStringRepresentation();
    		for (int i = 1; i < nodes.length; i++) result += " " + op +  " " + nodes[i].getStringRepresentation();
    		result += ")";
    		return result;
    	}
    }
    
    /**
     * Match available nodes by their operator identifier string. Allows the option "first longest match" only
     * for ambiguous situations where several operators match.
     * 
     * @param nodeTypes
     * @param str
     * @param firstLongestOnly
     * @return
     */
    private static Vector<AbstractGPNode> match(
			Vector<AbstractGPNode> nodeTypes, String str, boolean firstLongestOnly, boolean ignoreCase) {
    	Vector<AbstractGPNode> matching = new Vector<AbstractGPNode>();
		for (int i=0; i<nodeTypes.size(); i++) {
			if (str.startsWith(nodeTypes.get(i).getOpIdentifier())) matching.add(nodeTypes.get(i));
			else if (ignoreCase && str.toLowerCase().startsWith(nodeTypes.get(i).getOpIdentifier().toLowerCase())) matching.add(nodeTypes.get(i));
		}
		if (matching.size()>1 && firstLongestOnly) { // allow only the longest match (or first longest)
			int maxLen = matching.get(0).getOpIdentifier().length();
			AbstractGPNode longest=matching.get(0);
			for (int i=1; i<matching.size(); i++) {
				if (matching.get(i).getOpIdentifier().length()>maxLen) {
					longest = matching.get(i);
					maxLen = longest.getOpIdentifier().length();
				}
			}
			matching.clear();
			matching.add(longest);
		}
		return matching;
	}

    public static AbstractGPNode parseFromString(String str) {
//    	System.out.println("Parsing " + str);
    	Pair<AbstractGPNode,String> result = AbstractGPNode.parseFromString(str, null);
    	return result.head();
    }
    
	public static void main(String[] args) {
//		Double d = Double.parseDouble("2.58923 + 3");
    	AbstractGPNode node = AbstractGPNode.parseFromString("+(x,cOs(*(pI,x1)))");
//    	System.out.println("Parsed GPNode: " + node.getStringRepresentation());
    	node = AbstractGPNode.parseFromString(node.getStringRepresentation());
    }
    
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
