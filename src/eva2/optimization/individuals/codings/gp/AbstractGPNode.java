package eva2.optimization.individuals.codings.gp;


import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.problems.GPFunctionProblem;
import eva2.optimization.problems.InterfaceProgramProblem;
import eva2.tools.Pair;
import eva2.tools.ReflectPackage;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Vector;


/**
 * This gives an abstract node, with default functionality for get and set methods.
 */
public abstract class AbstractGPNode implements InterfaceProgram, java.io.Serializable {
    protected AbstractGPNode parentNode;
    protected AbstractGPNode[] nodes = new AbstractGPNode[0];
    protected int depth = 0;
    private static final boolean TRACE = false;

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public abstract Object clone();

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public abstract Object evaluate(InterfaceProgramProblem environment);

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    public abstract int getArity();

    /**
     * This method returns a string representation
     *
     * @return string
     */
    @Override
    public String getStringRepresentation() {
        StringBuffer sb = new StringBuffer();
        AbstractGPNode.appendStringRepresentation(this, sb);
        return sb.toString();
    }

    /**
     * Recursively perform deep cloning of the members of this instance and all sub-nodes.
     *
     * @param node the node to clone values from
     */
    protected void cloneMembers(AbstractGPNode node) {
        this.depth = node.depth;
        this.parentNode = node.parentNode;
        this.nodes = new AbstractGPNode[node.nodes.length];
        for (int i = 0; i < node.nodes.length; i++) {
            this.nodes[i] = (AbstractGPNode) node.nodes[i].clone();
        }
    }

    private static void appendStringRepresentation(AbstractGPNode node, StringBuffer sbuf) {
        String op = node.getOpIdentifier();
        sbuf.append(op);
        if (node.getArity() > 0) {
            sbuf.append("(");
            for (int i = 0; i < node.nodes.length; i++) {
                sbuf.append(node.nodes[i].getStringRepresentation());
                if (i < node.nodes.length - 1) {
                    sbuf.append(", ");
                }
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
     * Small parser for GP nodes from a String. Format must be (nearly) equivalent to what
     * makeStringRepresentation produces.
     * This mainly means prefix notation with braces and commata, such as in:
     * AbstractGPNode node = AbstractGPNode.parseFromString("+(2.0,cos(*(pi,pi)))");
     * System.out.println("Parsed GPNode: " + node.getStringRepresentation());
     * node = AbstractGPNode.parseFromString(node.getStringRepresentation());
     *
     * @param str
     * @param nodeTypes
     * @return
     */
    public static Pair<AbstractGPNode, String> parseFromString(String str, Vector<AbstractGPNode> nodeTypes) {
        if (nodeTypes == null) {
            nodeTypes = getNodeTypes();
        }
        if (nodeTypes.size() > 0) {
            Vector<AbstractGPNode> matchSet = AbstractGPNode.match(nodeTypes, str, true, true);
            if (matchSet.size() == 0) {
                // try to read constant
                Pair<Double, String> nextState = readDouble(str, true);
                if (nextState != null) {
                    return new Pair<AbstractGPNode, String>(new GPNodeConst(nextState.head().doubleValue()), nextState.tail());
                } else {
                    System.err.println("String has unknown prefix: " + str);
                }
            } else if (matchSet.size() > 1) {
                System.err.println("String has ambiguous prefix: " + str + " -- " + BeanInspector.toString(matchSet));
            } else { // exactly one match:
                AbstractGPNode currentNode = (AbstractGPNode) matchSet.get(0).clone();
                if (TRACE) {
                    System.out.println("Found match: " + currentNode.getOpIdentifier() + "/" + currentNode.getArity());
                }
                int cutFront = currentNode.getOpIdentifier().length();
                String restStr;
                if (currentNode.getArity() == 0) {
                    restStr = str.substring(cutFront).trim();
                    if (currentNode instanceof GPNodeInput) {
                        Pair<Double, String> nextState = readDouble(restStr, false);
                        if (nextState != null) {
                            ((GPNodeInput) currentNode).setIdentifier(currentNode.getOpIdentifier() + ((int) nextState.head().doubleValue()));
                            restStr = nextState.tail();
                        } else {
                            ((GPNodeInput) currentNode).setIdentifier(currentNode.getOpIdentifier());
                        }
                    }
                    return new Pair<AbstractGPNode, String>(currentNode, restStr);
                } else {
                    restStr = str.substring(cutFront + 1).trim(); // cut this op and front brace
                    currentNode.nodes = new AbstractGPNode[currentNode.getArity()];
                    for (int i = 0; i < currentNode.getArity(); i++) {
                        Pair<AbstractGPNode, String> nextState = parseFromString(restStr, nodeTypes);
                        currentNode.nodes[i] = nextState.head();
                        try {
                            restStr = nextState.tail().substring(1).trim(); // cut comma or brace
                        } catch (StringIndexOutOfBoundsException e) {
                            System.err.println("Error: parsing failed for node " + currentNode.getOpIdentifier() + "/" + currentNode.getArity() + ", depth " + currentNode.getDepth());
                            System.err.println("String was " + str);
                            e.printStackTrace();
                        }
                    }
                    if (TRACE) {
                        System.out.println("read " + currentNode.getName() + ", rest: " + restStr);
                    }
                    return new Pair<AbstractGPNode, String>(currentNode, restStr);
                }
            }
        }
        return null;
    }

    /**
     * Return all available node types as AbstractGPNode list.
     * Using getOpIdentifier on all elements gives an overview of the operators
     * that can be used.
     *
     * @return a list of available AbstractGPNode implementations
     */
    public static Vector<AbstractGPNode> getNodeTypes() {
        ArrayList<String> cls = GenericObjectEditor.getClassesFromClassPath(AbstractGPNode.class.getCanonicalName(), null);
        Vector<AbstractGPNode> nodeTypes = new Vector<AbstractGPNode>(cls.size());
        for (int i = 0; i < cls.size(); i++) {
            try {
                AbstractGPNode node = (AbstractGPNode) Class.forName((String) cls.get(i)).newInstance();
                nodeTypes.add(node);
            } catch (Exception e) {
            }
        }
//			nodeTypes.add(new GPNodeInput("X"));
        nodeTypes.add(new GPNodeInput("N"));
        return nodeTypes;
    }

    private static Pair<Double, String> readDouble(String str, boolean expect) {
        String firstArg;
        int argLen = str.indexOf(',');
        if (argLen < 0) {
            argLen = str.indexOf(')');
        } else {
            int firstBrace = str.indexOf(')');
            if ((firstBrace >= 0) && (firstBrace < argLen)) {
                argLen = firstBrace;
            }
        }
        if (argLen > 0) {
            firstArg = str.substring(0, argLen);
        } else {
            firstArg = str.trim();
        }
        try {
            Double d = Double.parseDouble(firstArg);
            return new Pair<Double, String>(d, str.substring(firstArg.length()));
        } catch (NumberFormatException e) {
            if (expect) {
                System.err.println("String has unknown prefix: " + str);
            }
            return null;
        }
    }

    /**
     * This method returns a string representation
     *
     * @return string
     */
    public static String makeStringRepresentation(AbstractGPNode[] nodes, String op) {
        if (nodes.length == 0) {
            return op;
        } else if (nodes.length == 1) {
            return op + "(" + nodes[0].getStringRepresentation() + ")";
        } else {
            String result = "( " + nodes[0].getStringRepresentation();
            for (int i = 1; i < nodes.length; i++) {
                result += " " + op + " " + nodes[i].getStringRepresentation();
            }
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
        for (int i = 0; i < nodeTypes.size(); i++) {
            String reqPrefix = nodeTypes.get(i).getOpIdentifier();
            if (nodeTypes.get(i).getArity() > 0) {
                reqPrefix += "(";
            }
            if (str.startsWith(reqPrefix)) {
                matching.add(nodeTypes.get(i));
            } else if (ignoreCase && str.toLowerCase().startsWith(reqPrefix.toLowerCase())) {
                matching.add(nodeTypes.get(i));
            }
        }
        if (matching.size() > 1 && firstLongestOnly) { // allow only the longest match (or first longest)
            int maxLen = matching.get(0).getOpIdentifier().length();
            AbstractGPNode longest = matching.get(0);
            Vector<AbstractGPNode> longestList = new Vector<AbstractGPNode>();
            longestList.add(longest);
            for (int i = 1; i < matching.size(); i++) {
                if (matching.get(i).getOpIdentifier().length() > maxLen) {
                    longest = matching.get(i);
                    maxLen = longest.getOpIdentifier().length();
                    longestList.clear();
                    longestList.add(longest);
                } else if (matching.get(i).getOpIdentifier().length() == maxLen) {
                    longestList.add(matching.get(i));
                }
            }
            matching.clear();
            matching.addAll(longestList);
            // TODO test if arities are different!
        }
        return matching;
    }

    public static AbstractGPNode parseFromString(String str) {
//    	System.out.println("Parsing " + str);
        Pair<AbstractGPNode, String> result = AbstractGPNode.parseFromString(str, null);
        return result.head();
    }

    public static void main(String[] args) {
//		Double d = Double.parseDouble("2.58923 + 3");
//    	AbstractGPNode node = AbstractGPNode.parseFromString("-23421");
        AbstractGPNode node = AbstractGPNode.parseFromString("+(-23421,cOs(*(pI,x)))");
        AbstractGPNode.parseFromString("+(+(85.334407,*(0.0056858,*(x1,x4))), +(*(0.00026,*(x0,x3)),*(-0.0022053,*(x2,x4))))");
        AbstractGPNode.parseFromString("+(+(80.51249,*(0.0071317,*(x1,x4))), +(*(0.0029955,*(x0,x1)),*(0.0021813,*(x2,x2))))");
        AbstractGPNode.parseFromString("+(+(9.300961,*(0.0047026,*(x2,x4))), +(*(0.0012547,*(x0,x2)),*(0.0019085,*(x2,x3))))");

        System.out.println("Parsed GPNode: " + node.getStringRepresentation());
        node = AbstractGPNode.parseFromString(node.getStringRepresentation());

        double[] sol = new double[]{4.755837346122817, 0.0, 1.618818602745894, 7.941611605461133, 7.949805645271173, 7.9567145687445695, 4.8033535294211225, 7.96718976492528, 1.641971622483205, 7.973813526015599, 7.980394418430633, 7.98301197251176, 7.98590997257042, 1.6493767411801206, 7.994756424330215, 7.994983501150322, 7.9971658558418035, 8.00273733683876, 8.00492865462689, 8.006601147955184};
        double[] sol2 = {7.897269942114308, 0.0, 7.939346674715275, 1.6272963933436047, 7.952303730484389, 7.960893192129872, 4.804987144876599, 7.9682843963405805, 7.977546251710085, 7.981109017707746, 1.642081396353059, 7.985246784301232, 4.827113167927753, 1.6448751122424057, 7.997468593784776, 8.00165633007073, 8.000613763831703, 8.003920903217887, 8.005789437120203, 8.012425280944097};
        double[] sol3 = {4.705970234231343, 4.71343334004773, 7.845971927185614, 4.708648989456629, 4.723918978896874, 7.864710619970946, 1.5776948341096448, 7.854961967305262, 7.858760422458277, 1.5743212019457036, 7.8488102514506, 1.5637070804731334, 1.5778078319616269, 1.5757833862993071, 4.711995406637344, 4.715448624806491, 7.8434193487088155, 4.7036514083601535, 7.848371610694223, 7.856489370257257};
        test("-(0.75,prod(x))", sol3);
        test("-(sum(x),*(7.5,n))", sol3);
//    	test("+(*(1000,+(sin(-(-0.25,x2)),sin(-(-0.25,x3)))), -(894.8,x0))", new double[]{1.,2,0,0,});
        double[] solG5lit = new double[]{679.9453, 1026.067, 0.1188764, -0.3962336};
        double[] solG5 = new double[]{891.702675571982, 808.9201991846442, -0.028381806025171354, -0.4684444512076402};
        test("-(x2,+(x3,0.55))", solG5);
        test("-(x3,+(x2,0.55))", solG5);
        test("+(*(1000,+(sin(-(-0.25,x2)),sin(-(-0.25,x3)))), -(894.8,x0))", solG5);
        test("+(*(1000,+(sin(+(-0.25,x2)),sin(-(x2,+(x3,0.25))))), -(894.8,x1))", solG5);
        test("+(*(1000,+(sin(+(-0.25,x3)),sin(-(x3,+(x2,0.25))))), 1294.8)", solG5);

        double[] solG13lit = new double[]{-1.717143, 1.595709, 1.827247, -0.7636413, -0.763645};
//    	double[] solG13 = new double[] {-0.999977165120676, -0.03949641197962931, 2.9901909235593664, 0.11170038214968671, -0.21164083835675082};
//NMS:   	double[] solG13 = new double[] {-1.20317028354022, 0.9052295512320271, 2.580255691052748, 0.5210663754783309, 0.8965551458319728};
        double[] solG13 = {-1.717136209326236, 1.5957142570821299, -1.8272614459011625, -0.7636708932891901, 0.7636501970281446};
        test("-(+(+(pow2(x0),pow2(x1)),+(pow2(x2),+(pow2(x3),pow2(x4)))),10)", solG13);
        test("-(*(x1,x2),*(5,*(x3,x4)))", solG13);
        test("+(pow3(x0),+(pow3(x1),1))", solG13);
        System.out.println("" + Math.exp(Mathematics.product(solG13)));
        test("+(sum(x),abs(sin(*(x0,x3))))", solG5);
        test("-(abs(sum(x)),*(abs(-7.5),n))", solG5);

        GPNodeConst n1 = new GPNodeConst(3.);
        GPNodeConst n2 = new GPNodeConst(7.);
        GPNodeAdd n3 = new GPNodeAdd();
        System.out.println(n1.equals(n2));
        System.out.println(n2.equals(n1));
        System.out.println(n1.equals(n3));

        System.out.println(createNodeList());
    }

    /**
     * Print all operator identifiers with arities.
     *
     * @return
     */
    public static String createNodeList() {
        String ret = new String();

        Class<?> cls = AbstractGPNode.class;
        Class<?>[] nodes = ReflectPackage.getAssignableClassesInPackage(cls.getPackage().getName(), AbstractGPNode.class, true, false);
        for (Class<?> c : nodes) {
            if (Modifier.isAbstract(c.getModifiers()) || c.isInterface()) {
                continue;
            }
            AbstractGPNode node;
            try {
                node = (AbstractGPNode) c.newInstance();
                ret = ret + " (" + node.getOpIdentifier() + "," + node.getArity() + ")";
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static void test(String constr, double[] pos) {
        AbstractGPNode node = AbstractGPNode.parseFromString(constr);
        GPFunctionProblem func = new GPFunctionProblem(node, null, pos.length, 0., 0.);
        double[] ret = func.evaluate(pos);
        System.out.println("testing " + constr + " evaluated to " + BeanInspector.toString(ret));
    }

    /**
     * This method returns the depth of the current node
     *
     * @return The depth.
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * This method allows you to set the depth of the current node
     *
     * @param depth The depth of the node
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * This method allows you to fetch a certain node given by the index.
     *
     * @param index Index specifing the requested node.
     * @return The requested node.
     */
    public AbstractGPNode getNode(int index) {
        return this.nodes[index];
    }

    /**
     * This method allows you to set a node specified by the index
     *
     * @param node  The new node
     * @param index The position where it is to be inserted.
     */
    public void setNode(AbstractGPNode node, int index) {
        node.setParent(this);
        node.setDepth(this.depth + 1);
        this.nodes[index] = node;
    }

    /**
     * This method allows you to set a node specified by the reference.
     *
     * @param newnode The new node.
     * @param oldnode The old node.
     */
    public void setNode(AbstractGPNode newnode, AbstractGPNode oldnode) {
        newnode.setParent(this);
        newnode.updateDepth(this.depth + 1);
        for (int i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i] == oldnode) {
                this.nodes[i] = newnode;
//        		System.out.println("SWITCHED " + i);
            }
        }
    }

    /**
     * This method returns all nodes begining with the current node.
     *
     * @param ListOfNodes This ArrayList will contain all nodes
     */
    public void addNodesTo(ArrayList ListOfNodes) {
        ListOfNodes.add(this);
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].addNodesTo(ListOfNodes);
        }
    }

    /**
     * Return a shallow reference to a random node within this tree.
     *
     * @return
     */
    public AbstractGPNode getRandomNode() {
        ArrayList allNodes = new ArrayList(10);
        addNodesTo(allNodes);
        return (AbstractGPNode) allNodes.get(RNG.randomInt(allNodes.size()));
    }

    /**
     * Return a shallow reference to a random leaf of this tree.
     *
     * @return
     */
    public AbstractGPNode getRandomLeaf() {
        if (nodes.length > 0) {
            int k = RNG.randomInt(nodes.length);
            return nodes[k].getRandomLeaf();
        } else {
            return this;
        }
    }

    /**
     * This method allows you to set the parent of the node
     *
     * @param parent The new parent
     */
    public void setParent(AbstractGPNode parent) {
        this.parentNode = parent;
    }

    /**
     * This method allows you to get the parent of the node
     */
    public AbstractGPNode getParent() {
        return this.parentNode;
    }

    /**
     * This method allows to fully connect a following nodes to thier parents
     *
     * @param parent The parent
     */
    public void connect(AbstractGPNode parent) {
        this.parentNode = parent;
        if (parent != null) {
            this.depth = this.parentNode.getDepth() + 1;
        } else {
            this.depth = 0;
        }
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].connect(this);
        }
    }

    /**
     * This method will simply init the array of nodes
     */
    public void initNodeArray() {
        this.nodes = new AbstractGPNode[this.getArity()];
    }

    /**
     * This method performs a full init but with max depth
     *
     * @param area  The allowed function area.
     * @param depth The absolute target depth.
     */
    public void initFull(GPArea area, int depth) {
        this.nodes = new AbstractGPNode[this.getArity()];
        for (int i = 0; i < this.nodes.length; i++) {
            if (this.depth + 1 >= depth) {
                this.nodes[i] = (AbstractGPNode) area.getRandomNodeWithArity(0).clone();
            } else {
                this.nodes[i] = (AbstractGPNode) area.getRandomNonTerminal().clone();
            }
            this.nodes[i].setDepth(this.depth + 1);
            this.nodes[i].setParent(this);
            this.nodes[i].initFull(area, depth);
        }
    }

    /**
     * This method performs a grow init but with max depth
     *
     * @param area  The allowed function area.
     * @param depth The absolute target depth.
     */
    public void initGrow(GPArea area, int depth) {
        this.nodes = new AbstractGPNode[this.getArity()];
        for (int i = 0; i < this.nodes.length; i++) {
            if (this.depth + 1 >= depth) {
                this.nodes[i] = (AbstractGPNode) area.getRandomNodeWithArity(0).clone();
            } else {
                this.nodes[i] = (AbstractGPNode) area.getRandomNode().clone();
            }
            this.nodes[i].setDepth(this.depth + 1);
            this.nodes[i].setParent(this);
            this.nodes[i].initGrow(area, depth);
        }
    }

    /**
     * This method allows you to get the overall number of nodes
     *
     * @return Number of nodes.
     */
    public int getNumberOfNodes() {
        int result = 1;
        for (int i = 0; i < this.nodes.length; i++) {
            result += this.nodes[i].getNumberOfNodes();
        }
        return result;
    }

    /**
     * Return the maximal depth of the tree starting here, but relating to the whole tree.
     *
     * @return The max depth.
     */
    public int getMaxDepth() {
        int result = this.depth;
        for (int i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i] != null) {
                result = Math.max(result, this.nodes[i].getMaxDepth());
            }
        }
        return result;
    }

    /**
     * Return the depth of the subtree only.
     *
     * @return
     */
    public int getSubtreeDepth() {
        int maxDepth = getMaxDepth();
        return maxDepth - depth;
    }

    /**
     * This method will check if maxdepth is violated
     *
     * @param maxDepth The max depth.
     * @return True if MaxDepth is violated
     */
    public boolean isMaxDepthViolated(int maxDepth) {
        if (maxDepth < this.getMaxDepth()) {
            return true;
        } else {
            return false;
        }
//        if (depth > this.depth) return false;
//        else {
//            boolean result = true;
//            for (int i = 0; i < this.nodes.length; i++) {
//                result = result & this.nodes[i].isMaxDepthViolated(depth);
//            }
//            return result;
//        }
    }

    /**
     * This method will repair the maxDepth constraint
     *
     * @param depth The max depth.
     */
    public void repairMaxDepth(GPArea area, int depth) {
        if (this.depth == depth - 1) {
            // in this case i need to check whether or not my
            // follow-up nodes are terminals
            for (int i = 0; i < this.nodes.length; i++) {
                if (this.nodes[i].getArity() != 0) {
                    // replace this node with a new node
                    this.nodes[i] = (AbstractGPNode) area.getRandomNodeWithArity(0).clone();
                    this.nodes[i].setDepth(this.depth + 1);
                    this.nodes[i].setParent(this);
                }
            }
        } else {
            // else i call the method on my followup nodes
            for (int i = 0; i < this.nodes.length; i++) {
                this.nodes[i].repairMaxDepth(area, depth);
            }
        }

    }

    /**
     * This method allows you to determine whether or not two subtrees
     * are actually the same.
     *
     * @param obj The other subtree.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            AbstractGPNode node = (AbstractGPNode) obj;
            if (this.getArity() != node.getArity()) {
                return false;
            }
            if (this.nodes.length != node.nodes.length) {
                return false;
            }
            for (int i = 0; i < this.nodes.length; i++) {
                if (!this.nodes[i].equals(node.nodes[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update the depth of this node tree starting with the given initial depth of the root.
     *
     * @param myDepth
     */
    public void updateDepth(int myDepth) {
        depth = myDepth;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].updateDepth(myDepth + 1);
        }
    }

    /**
     * Check the depth of this node tree starting with the given initial depth of the root.
     *
     * @param myDepth
     */
    public boolean checkDepth(int myDepth) {
        if (depth != myDepth) {
            System.err.println("Depth was wrong at level " + myDepth);
            return false;
        }
        for (int i = 0; i < nodes.length; i++) {
            if (!nodes[i].checkDepth(myDepth + 1)) {
                return false;
            }
        }
        return true;
    }
}
