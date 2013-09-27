package eva2.tools.math;

import eva2.gui.BeanInspector;
import eva2.optimization.enums.BOAScoringMethods;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class BayNet {

    private boolean[][] network = null;
    private int dimension = 3;
    private BayNode[] nodes = null;
    private List<Integer> rootNodes = new LinkedList<Integer>();
    private double upperProbLimit = 0.9;
    private double lowerProbLimit = 0.1;
    private BOAScoringMethods scoringMethod = BOAScoringMethods.BDM;
    private double[] scoreArray = null;
//	private String			tables			= "";

    public BayNet(int dimension, double upperLimit, double lowerLimit) {
        this.dimension = dimension;
        this.upperProbLimit = upperLimit;
        this.lowerProbLimit = lowerLimit;
        init();
    }

    public BayNet(BayNet b) {
        this.network = cloneNetwork(b.network);
        this.dimension = b.dimension;
        this.nodes = new BayNode[b.dimension];
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i] = (BayNode) b.nodes[i].clone();
        }
        this.rootNodes = new LinkedList<Integer>();
        for (Integer node : b.rootNodes) {
            this.rootNodes.add(node);
        }
        this.upperProbLimit = b.upperProbLimit;
        this.lowerProbLimit = b.lowerProbLimit;
    }

    @Override
    public Object clone() {
        return new BayNet(this);
    }

    private boolean[][] cloneNetwork(boolean[][] b) {
        boolean[][] result = new boolean[b.length][b.length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (b[i][j]) {
                    result[i][j] = true;
                }
            }
        }
        return result;
    }

    /**
     * initialize the Network
     */
    public void init() {
        this.network = new boolean[this.dimension][this.dimension];
        this.nodes = new BayNode[this.dimension];
        for (int i = 0; i < this.dimension; i++) {
            this.nodes[i] = new BayNode(i);
            this.rootNodes.add(i);
        }
        this.scoreArray = new double[this.dimension];
        Arrays.fill(scoreArray, -1.0);
    }

    private static BitSet getBinaryData(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceGAIndividual) {
            return ((InterfaceGAIndividual) indy).getBGenotype();
        } else if (indy instanceof InterfaceDataTypeBinary) {
            return ((InterfaceDataTypeBinary) indy).getBinaryData();
        } else {
            throw new RuntimeException("Unable to get binary representation for " + indy.getClass());
        }
    }

    /**
     * @return a list of the root nodes
     */
    public List<BayNode> getRootNodes() {
//		ArrayList<BayNode> result = new ArrayList<BayNode>();
//		for (int i=0; i<this.dimension; i++){
//			// when the column is empty, we have no incoming edges and the node is a root-node
//			boolean root = true;
//			for(int j=0; j<this.dimension; j++){
//				if(this.network[j][i]){
//					root = false;
//					j=this.dimension;
//				}
//			}
//			if(root){
//				result.add(getNode(i));
//			}
//		}
//		return result;
        LinkedList<BayNode> result = new LinkedList<BayNode>();
        for (Integer i : this.rootNodes) {
            result.add(this.nodes[i]);
        }
        return result;
    }

    /**
     * get the i-th node
     *
     * @param i the node to be returned
     * @return the requested node
     */
    public BayNode getNode(int i) {
        return this.nodes[i];
    }

    /**
     * return the children for a given node
     *
     * @param n the node
     * @return the children of the node
     */
    public List<BayNode> getChildren(BayNode n) {
//		ArrayList<BayNode> result = new ArrayList<BayNode>();
//		int i = n.getId();
//		for(int j=0; j<this.dimension; j++){
//			if(this.network[i][j]){
//				result.add(this.nodes[j]);
//			}
//		}
//		return result;
        List<Integer> ids = n.getChildren();
        List<BayNode> result = new ArrayList<BayNode>();
        for (int i : ids) {
            result.add(this.nodes[i]);
        }
        return result;
    }

    /**
     * return the parents for a given node
     *
     * @param n the node
     * @return the parents of the node
     */
    public List<BayNode> getParents(BayNode n) {
//		ArrayList<BayNode> result = new ArrayList<BayNode>();
//		int i = n.getId();
//		for(int j=0; j<this.dimension; j++){
//			if(this.network[j][i]){
//				result.add(this.nodes[j]);
//			}
//		}
//		if (result.size()!=n.getNumberOfParents()) {
//			System.err.println("Error in getParents!");
//		}
//		return result;
        List<Integer> ids = n.getParents();
        List<BayNode> result = new LinkedList<BayNode>();
        for (int i : ids) {
            result.add(this.nodes[i]);
        }
        return result;
    }

    /**
     * return the children of a list of nodes
     *
     * @param n the list of nodes
     * @return the children of the nodes
     */
    public List<BayNode> getChildren(List<BayNode> n) {
        ArrayList<BayNode> result = new ArrayList<BayNode>();
        for (BayNode node : n) {
            List<BayNode> children = getChildren(node);
            for (BayNode nod : children) {
                if (!result.contains(nod)) {
                    result.add(nod);
                }
            }
        }
        return result;
    }

    /**
     * remove the edge from node i to node j
     *
     * @param i the node from which the edge comes
     * @param j the node to which the edge points
     */
    public void removeEdge(Integer i, Integer j) {
        if (this.network[i][j]) {
            this.network[i][j] = false;
            this.nodes[j].decrNumberOfParents();
            this.nodes[i].removeChild(j);
            this.nodes[j].removeParent(i);
            this.nodes[j].generateNewPTable();
            if (this.nodes[j].getNumberOfParents() == 0) {
                this.rootNodes.add(j);
            }
        }
    }

    /**
     * add an edge from the node i to the node j
     *
     * @param i edge from this node
     * @param j edge to this node
     */
    public void addEdge(Integer i, Integer j) {
        if (i != j) {
            if (!this.network[i][j]) {
                this.network[i][j] = true;
                this.rootNodes.remove(j);
                this.nodes[j].incrNumberOfParents();
                this.nodes[j].generateNewPTable();
                this.nodes[i].addChild(j);
                this.nodes[j].addParent(i);
            }
        }
    }

    private int findNext(double[] probabilities) {
        int result = -1;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] != -1) {
                continue;
            }
            BayNode currentNode = nodes[i];
            List<BayNode> parents = getParents(currentNode);
            boolean possible = true;
            for (BayNode node : parents) {
                if (probabilities[node.getId()] == -1) {
                    possible = false;
                }
            }
            if (!possible) {
                continue;
            }
            result = i;
            break;
        }
        return result;
    }

    /**
     * find the next value where all the parents are already set
     *
     * @param data
     * @return
     */
    private int findNext(double[] probabilities, List<BayNode> nodes) {
        nodes = removeDuplicate(nodes);
        for (BayNode node : nodes) {
            if (node.getCalculated()) {
                continue;
            }
            node.setCalculated(true);
            List<BayNode> parents = getParents(node);
            boolean possible = false;
            for (BayNode p : parents) {
                if (probabilities[p.getId()] != -1) {
                    possible = true;
                } else {
                    possible = false;
                    break;
                }
            }
            if (possible) {
                resetCalculated();
                return node.getId();
            }
        }
        resetCalculated();
        return -1;
    }

    private List<BayNode> removeDuplicate(List<BayNode> nodes) {
        //Create a HashSet which allows no duplicates
        HashSet<BayNode> hashSet = new HashSet<BayNode>(nodes);

        //Assign the HashSet to a new ArrayList
        ArrayList<BayNode> arrayList2 = new ArrayList<BayNode>(hashSet);
        return arrayList2;
    }

    /**
     * calculate a new BitSet according to the network
     *
     * @param data the BitSet that will be calculated
     * @return the new BitSet
     */
    public BitSet sample(BitSet data) {
        // generate a new probabilities-vector
        double[] probabilities = new double[this.network.length];
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = -1;
        }
        // get the root-nodes (the non dependent nodes)
        List<BayNode> nodes = getRootNodes();
        // calculate the BitSet-Value for these nodes
        for (BayNode node : nodes) {
            int id = node.getId();
            probabilities[id] = node.getProbability(0);
            data.set(id, RNG.flipCoin(probabilities[id]));
            node.setCalculated(true);
        }
        // find the next node that can be evaluated
//		List<BayNode> toCalculate = getChildren(nodes);
//		int next = findNext(probabilities, toCalculate);
        int next = findNext(probabilities);
        while (next != -1) {
//			toCalculate.remove(this.nodes[next]);
            this.nodes[next].setCalculated(true);
//			toCalculate = addToToCalculate(toCalculate, this.nodes[next]);
//			toCalculate.addAll(getChildren(this.nodes[next]));
//			probabilities[next] = calculateNextProbability(data, toCalculate, next);
            probabilities[next] = calculateNextProbability(data, next);
            data.set(next, RNG.flipCoin(probabilities[next]));
//			next = findNext(probabilities, toCalculate);
            next = findNext(probabilities);
        }
        resetCalculated();
        return data;
    }

    private List<BayNode> addToToCalculate(List<BayNode> toCalculate,
                                           BayNode next) {
        List<BayNode> toAdd = getChildren(next);
        for (int i = 0; i < toAdd.size(); i++) {
            BayNode node = toAdd.get(i);
            if (!toCalculate.contains(node) && !node.getCalculated()) {
                toCalculate.add(node);
            }
        }
        return toCalculate;
    }

    private double calculateNextProbability(BitSet data, int next) {
        int[] parId = calculateSortedParentIds(next);
        int par = 0;
        for (int i = parId.length - 1; i >= 0; i--) {
            if (data.get(parId[i])) {
                par += Math.pow(2, i);
            }
        }
        return this.nodes[next].getProbability(par);
    }

    /**
     * calculate the next probability
     *
     * @param data          the already calculated data
     * @param probabilities the already calculated probabilities
     * @param toCalculate   the Nodes that have yet to be calculated
     * @param next          the node for which to calculate the probability
     * @return the new probabilities array
     */
    private double calculateNextProbability(BitSet data, List<BayNode> toCalculate, int next) {
        toCalculate.addAll(getChildren(this.nodes[next]));
        int[] parId = calculateSortedParentIds(next);
        int prob = 0;
        int cnt = 0;
        for (int j = parId.length - 1; j >= 0; j--) {
            if (data.get(parId[j])) {
                prob += (int) Math.pow(2, j);
            }
            cnt++;
        }
        return this.nodes[next].getProbability(prob);
    }

    /**
     * generate an array of the parents, sorted by there id
     *
     * @param id the id of the node
     * @return the sorted parent-ids
     */
    private int[] calculateSortedParentIds(int id) {
        List<BayNode> parents = getParents(this.nodes[id]);
        int[] parId = new int[parents.size()];
        int i = 0;
        for (BayNode nod : parents) {
            parId[i] = nod.getId();
            i++;
        }
        Arrays.sort(parId);
        return parId;
    }

//	/**
//	 * generate an array of the parents plus the given node, sorted by there id
//	 * @param id	the id of the node
//	 * @return		the sorted parent-ids
//	 */
//	private int[] calculateSortedParentPlusNodeIds(int id) {
//		List<BayNode> nodes = getParents(this.nodes[id]);
//		nodes.add(this.nodes[id]);
//		int[] sortedIds = new int[nodes.size()];
//		int i=0;
//		for(BayNode nod: nodes){
//			sortedIds[i] = nod.getId();
//			i++;
//		}
//		Arrays.sort(sortedIds);
//		return sortedIds;
//	}

    private void resetCalculated() {
        for (int i = 0; i < this.dimension; i++) {
            this.nodes[i].setCalculated(false);
        }
    }

    /**
     * see if the network is still acyclic after inserting the edge
     *
     * @param from the node from which the edge comes from
     * @param to   the node to which the edgte points
     * @return is the network still acyclic
     */
    public boolean isACyclic(int from, int to) {
        int cnt1 = 0;
        int cnt2 = 0;
        for (int i = 0; i < this.dimension; i++) {
            if (this.network[i][from]) {
                cnt1++;
            }
            if (this.network[to][i]) {
                cnt2++;
            }
        }
        // if the from node has no incoming edges or the to node has no outgoing edges the network is still acyclic
        if (cnt1 == 0 || cnt2 == 0) {
            return true;
        }
        // look at all the children and see if we can get to the from-node from the to-node
        List<BayNode> toCalculate = getChildren(this.nodes[to]);
        while (!toCalculate.isEmpty()) {
            BayNode node = toCalculate.get(0);
            toCalculate.remove(node);
            if (!node.getCalculated()) {
                node.setCalculated(true);
                if (from == node.getId()) {
                    resetCalculated();
                    return false;
                }
                List<BayNode> children = getChildren(node);
                toCalculate.addAll(children);
            }
        }
        resetCalculated();
        return true;
    }

    /**
     * check if the given Network is acyclic
     *
     * @param net the Network
     * @return is the net acyclic
     */
    public boolean isACyclic() {
        List<Pair<Integer, Integer>> deletedEdges = new LinkedList<Pair<Integer, Integer>>();
        List<BayNode> nodes = getRootNodes();
        boolean res = false;
        for (int i = 0; i <= this.dimension; i++) {
            for (BayNode node : nodes) {
                int id = node.getId();
                for (int j = 0; j < this.dimension; j++) {
                    if (this.network[id][j]) {
                        this.network[id][j] = false;
                        deletedEdges.add(new Pair<Integer, Integer>(id, j));
                    }
                }
            }
            nodes = getRootNodes();
            // if we only have root nodes, we have an acyclic graph
            if (nodes.size() == this.nodes.length) {
                res = true;
                break;
            }
        }
        for (Pair<Integer, Integer> edge : deletedEdges) {
            this.network[edge.head][edge.tail] = true;
        }
        return res;
    }

    private double getPrior(List<BayNode> parents, Population pop) {
        double result = 1.0;
        switch (this.scoringMethod) {
            case BDM:
                result = ((double) pop.size()) / Math.pow(2.0, (double) parents.size());
                break;
            case K2:
                result = 2.0;
        }
        return result;
    }

    private double getPrior(List<BayNode> parents, BayNode current, Population pop) {
        double result = 1.0;
        switch (this.scoringMethod) {
            case BDM:
                result = getPrior(parents, pop) / 2.0;
                break;
            case K2:
                result = 1.0;
        }
        return result;
    }

    public void setUpperProbLimit(double upperProbLimit) {
        this.upperProbLimit = upperProbLimit;
    }

    public void setLowerProbLimit(double lowerProbLimit) {
        this.lowerProbLimit = lowerProbLimit;
    }


    private double gamma(double x) {
        double result = 1.0;
        result = SpecialFunction.gamma(x);
        return result;
    }

    /**
     * calculate the score, either BDM, K2 or BIC
     *
     * @param pop the population on which the metric is based on
     * @return the metric
     */
    public double getScore(Population pop) {
        double result = 0.0;
        //for every node
        for (int i = 0; i < this.dimension; i++) {
            BayNode currentNode = this.nodes[i];
            //get the parents
            List<BayNode> parents = getParents(currentNode);
            // get the parentIds sorted (for the lookup)
            int[] parId = new int[0];
            if (!parents.isEmpty()) {
                parId = calculateSortedParentIds(i);
            }
            double[] pTable = currentNode.getPTable();
            switch (this.scoringMethod) {
                case BIC:
                    result -= (Math.log(pop.size()) * pTable.length * 2) / 2;
                    break;
                default:
                    break;
            }
            for (int j = 0; j < pTable.length; j++) {
                Population pop2 = numberSetCorrectly(pop, j, parId);
//				double firstFraction = 0.0;
                switch (this.scoringMethod) {
                    case BDM:
                        result += Math.log(firstFractionBDM(pop, parents, pop2));
                        break;
                    case K2:
                        result += Math.log(firstFractionBDM(pop, parents, pop2));
                        break;
                    case BIC:
                        result -= firstFractionBIC(pop2);
                        break;
                }
//				result = result + Math.log(firstFraction);
                if (pop2.size() > 0) {
                    for (int k = 0; k < 2; k++) {
//						double secondFraction = 0.0;
                        switch (this.scoringMethod) {
                            case BDM:
                                result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                                break;
                            case K2:
                                result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                                break;
                            case BIC:
                                result += secondFractionBIC(pop2, currentNode, k, j);
                                break;
                        }
//						result = result + Math.log(secondFraction);
                    }
                }
            }
        }
        return result;
    }

    public void initScoreArray(Population pop) {
        //for every node
        for (int i = 0; i < this.dimension; i++) {
            double result = 0.0;
            BayNode currentNode = this.nodes[i];
            //get the parents
            List<BayNode> parents = getParents(currentNode);
            // get the parentIds sorted (for the lookup)
            int[] parId = new int[0];
            if (!parents.isEmpty()) {
                parId = calculateSortedParentIds(i);
            }
            double[] pTable = currentNode.getPTable();
            switch (this.scoringMethod) {
                case BIC:
                    result -= (Math.log(pop.size()) * pTable.length * 2) / 2;
                    break;
                default:
                    break;
            }
            for (int j = 0; j < pTable.length; j++) {
                Population pop2 = numberSetCorrectly(pop, j, parId);
//				double firstFraction = 0.0;
                switch (this.scoringMethod) {
                    case BDM:
                        result += Math.log(firstFractionBDM(pop, parents, pop2));
                        break;
                    case K2:
                        result += Math.log(firstFractionBDM(pop, parents, pop2));
                        break;
                    case BIC:
                        result -= firstFractionBIC(pop2);
                        break;
                }
//				result = result + Math.log(firstFraction);
                if (pop2.size() > 0) {
                    for (int k = 0; k < 2; k++) {
//						double secondFraction = 0.0;
                        switch (this.scoringMethod) {
                            case BDM:
                                result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                                break;
                            case K2:
                                result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                                break;
                            case BIC:
                                result += secondFractionBIC(pop2, currentNode, k, j);
                                break;
                        }
//						result = result + Math.log(secondFraction);
                    }
                }
            }
            scoreArray[i] = result;
        }
    }

    public void updateScoreArray(Population pop, int i) {
        //for every node
        double result = 0.0;
        BayNode currentNode = this.nodes[i];
        //get the parents
        List<BayNode> parents = getParents(currentNode);
        // get the parentIds sorted (for the lookup)
        int[] parId = new int[0];
        if (!parents.isEmpty()) {
            parId = calculateSortedParentIds(i);
        }
        double[] pTable = currentNode.getPTable();
        switch (this.scoringMethod) {
            case BIC:
                result -= (Math.log(pop.size()) * pTable.length * 2) / 2;
                break;
            default:
                break;
        }
        for (int j = 0; j < pTable.length; j++) {
            Population pop2 = numberSetCorrectly(pop, j, parId);
            switch (this.scoringMethod) {
                case BDM:
                    result += Math.log(firstFractionBDM(pop, parents, pop2));
                    break;
                case K2:
                    result += Math.log(firstFractionBDM(pop, parents, pop2));
                    break;
                case BIC:
                    result -= firstFractionBIC(pop2);
                    break;
            }
            if (pop2.size() > 0) {
                for (int k = 0; k < 2; k++) {
                    switch (this.scoringMethod) {
                        case BDM:
                            result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                            break;
                        case K2:
                            result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                            break;
                        case BIC:
                            result += secondFractionBIC(pop2, currentNode, k, j);
                            break;
                    }
                }
            }
        }
        scoreArray[i] = result;
    }

    public double getNewScore(Population pop, int i) {
        double result = 0;
        for (int j = 0; j < this.dimension; j++) {
            if (j == i) {
                result += getSingleScore(pop, i);
            } else {
                result += scoreArray[j];
            }
        }
        return result;
    }

    private double getSingleScore(Population pop, int i) {
        //for every node
        double result = 0.0;
        BayNode currentNode = this.nodes[i];
        //get the parents
        List<BayNode> parents = getParents(currentNode);
        // get the parentIds sorted (for the lookup)
        int[] parId = new int[0];
        if (!parents.isEmpty()) {
            parId = calculateSortedParentIds(i);
        }
        double[] pTable = currentNode.getPTable();
        switch (this.scoringMethod) {
            case BIC:
                result -= (Math.log(pop.size()) * pTable.length * 2) / 2;
                break;
            default:
                break;
        }
        for (int j = 0; j < pTable.length; j++) {
            Population pop2 = numberSetCorrectly(pop, j, parId);
            switch (this.scoringMethod) {
                case BDM:
                    result += Math.log(firstFractionBDM(pop, parents, pop2));
                    break;
                case K2:
                    result += Math.log(firstFractionBDM(pop, parents, pop2));
                    break;
                case BIC:
                    result -= firstFractionBIC(pop2);
                    break;
            }
            if (pop2.size() > 0) {
                for (int k = 0; k < 2; k++) {
                    switch (this.scoringMethod) {
                        case BDM:
                            result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                            break;
                        case K2:
                            result += Math.log(secondFractionBDM(pop, currentNode, parents, j, pop2, k));
                            break;
                        case BIC:
                            result += secondFractionBIC(pop2, currentNode, k, j);
                            break;
                    }
                }
            }
        }
        return result;
    }

    private double secondFractionBIC(Population pop2, BayNode currentNode, int k, int j) {
        double result = 0.0;
        double Nijk = numberSetCorrectly(pop2, k, currentNode.getId());
        if (Nijk > 0) {
            result = (double) Nijk * Math.log((double) Nijk);
        }
        if (k == 1) {
            double prob = Nijk / (double) pop2.size();
            setProbability(currentNode, j, prob);
        }
        return result;
    }

    private double firstFractionBIC(Population pop2) {
        double result = 0.0;
        if (pop2.size() > 0) {
            result = ((double) pop2.size()) * Math.log((double) pop2.size());
        }
        return result;
    }

    private double secondFractionBDM(Population pop, BayNode currentNode,
                                     List<BayNode> parents, int j, Population pop2, int k) {
        double mXiPiXi = numberSetCorrectly(pop2, k, currentNode.getId());
        double mDashXiPiXi = getPrior(parents, currentNode, pop);
        double numeratorSecondFraction = gamma(mDashXiPiXi + mXiPiXi);
        double denumeratorSecondFraction = gamma(mDashXiPiXi);
        double secondFraction = numeratorSecondFraction / denumeratorSecondFraction;
        if (k == 1) {
            double prob = mXiPiXi / (double) pop2.size();
            setProbability(currentNode, j, prob);
        }
        return secondFraction;
    }

    private double firstFractionBDM(Population pop, List<BayNode> parents,
                                    Population pop2) {
        double mPiXi = (double) pop2.size();
        double mDashPiXi = getPrior(parents, pop);
        double numeratorFirstFraction = gamma(mDashPiXi);
        double denominatorFirstFraction = gamma(mDashPiXi + mPiXi);
        double firstFraction = numeratorFirstFraction / denominatorFirstFraction;
        return firstFraction;
    }

//	public String getTables(){
//		return this.tables;
//	}

//	public void setTables(String s){
//		this.tables = "";
//		this.tables = s;
//	}

    private void setProbability(BayNode n, int j, double prob) {
        n.setPTable(j, Math.min(upperProbLimit, Math.max(lowerProbLimit, prob)));
    }

    private double numberSetCorrectly(Population pop, int k, int Id) {
        double result = 0.0;
        for (int i = 0; i < pop.size(); i++) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            BitSet data = getBinaryData(indy);
            int val = 0;
            if (data.get(Id)) {
                val = 1;
            }
            if (val == k) {
                result++;
            }
        }
        return result;
//		double result = 0.0;
//		String binaryString = Integer.toBinaryString(j);
//		while(binaryString.length() < parIds.length){
//			binaryString = "0"+binaryString;
//		}
//		boolean found = false;
//		boolean end = false;
//		int different = 0;
//		for(int i=0; i<parIds.length; i++){
//			if(parIds[i] != Ids[i]){
//				different = i;
//				found = true;
//				break;
//			}
//		}
//		if(!found){
//			different = Ids.length;
//			end = true;
//		}
//		if(end){
//			binaryString = binaryString+k;
//		}else{
//			binaryString = binaryString.substring(0, different)+k+binaryString.substring(different);
//		}
//		int l = Integer.parseInt(binaryString,2);
////		binary = getBinaryArray(Ids, binaryString);
//		for(int i=0; i<pop.size(); i++){
//			AbstractEAIndividual indy = pop.getEAIndividual(i);
//			BitSet data = ((InterfaceDataTypeBinary) indy).getBinaryData();
//			boolean setCorrectly = isSetCorrectly(Ids, data, l);
//			if(setCorrectly){
//				result ++;
//			}
//		}
//		return result;
    }

    private Population numberSetCorrectly(Population pop, int j, int[] Ids) {
        Population result = new Population();
//		String binaryString = Integer.toBinaryString(j);
        // append zeroes to the front
//		boolean[] binary = getBinaryArray(Ids, binaryString);
        for (int i = 0; i < pop.size(); i++) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            BitSet data = ((InterfaceDataTypeBinary) indy).getBinaryData();
            boolean setCorrectly = isSetCorrectly(Ids, data, j);
            if (setCorrectly) {
//				result ++;
                result.add(indy);
            }
        }
        return result;
    }

    /**
     * Encodes the given integer value in the <tt>BitSet</tt> from the position offset by the
     * given amount of bits.
     *
     * @param bitSet the <tt>BitSet</tt> to operate on
     * @param offset the offset in the bit set
     * @param length the length of the bit string that should represent the given value
     * @param value  the value to encode in the bit set
     * @return the modified bit set
     * @throws RuntimeException if <tt>length</tt> is greater than the amount of bits in an
     *                          integer value
     * @throws RuntimeException if <tt>value</tt> is greather than the value encodeable by the
     *                          given amount of bits or if value == Integer.MIN_VALUE (no absolute value awailable as
     *                          int)
     */
    public static BitSet intToBitSet(BitSet bitSet, int offset, int length, int value) {
        // checking the bit length
        if (length > Integer.SIZE) {
            throw new RuntimeException("You can not set a higher length than " + Integer.SIZE
                    + " bits.");
        }
        length += 1;
        // checking whether the value fits into the bit string of length - 1
        int absValue = Math.abs(value);
        if (absValue > Math.pow(2.0, length - 1 - 1) * 2 - 1 || value == Integer.MIN_VALUE) {
            throw new RuntimeException("The value of " + value
                    + " does not fit into a bit string of " + (length - 1) + " bits.");
        }

        // setting all bits to zero
        bitSet.clear(offset, offset + length - 1);

        // setting up the number in reverse order
        int mask = 1;
        for (int i = 0; i < length; ++i, mask <<= 1) {
            if ((mask & absValue) > 0) {
                bitSet.set(offset + i);
            }
        }

        // setting up the sign
        if (value < 0) {
            bitSet.set(offset + length - 1);
        }

        return bitSet;
    }

    /**
     * is the BitSet of the individual set correctly corresponding to the binary String and the parId
     *
     * @param ids  the Ids of the parents sorted
     * @param data the data of the individual to be checked
     * @param j    how the Bits have to be set (as Integer)
     * @return is the data set correctly
     */
    private boolean isSetCorrectly(int[] ids, BitSet data, int j) {
        int value = 0;
//		BitSet toTest = new BitSet(length);
        for (int i = 0; i < ids.length; i++) {
            if (data.get(ids[i])) {
                value += Math.pow(2, ids.length - i - 1);
            }
        }
        return value == j;
    }

    public void print() {
        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                if (this.network[i][j]) {
                    System.out.print("1");
                } else {
                    System.out.print("0");
                }
            }
            System.out.println();
        }
        for (int i = 0; i < this.dimension; i++) {
            System.out.println(BeanInspector.toString(nodes[i].getPTable()));
        }
    }

    public String generateYFilesCode() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
        result += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">\n";
        result += "  <!--Created by yFiles for Java HEAD-Current-->\n";
        result += "  <key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>\n";
        result += "  <key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>\n";
        result += "  <key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>\n";
        result += "  <key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>\n";
        result += "  <key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>\n";
        result += "  <key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>\n";
        result += "  <key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>\n";
        result += "  <key attr.name=\"Beschreibung\" attr.type=\"string\" for=\"graph\" id=\"d7\"/>\n";
        result += "  <key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>\n";
        result += "  <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>\n";
        result += "  <key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>\n";
        result += "  <graph edgedefault=\"directed\" id=\"G\">\n";
        result += "    <data key=\"d7\"/>\n";
        for (int i = 0; i < this.nodes.length; i++) {
            Pair<Integer, String> pair = generateTable(i);
            Integer length = pair.getHead();
            String table = pair.getTail();
            int x = 40 + 100 * (i % 20);
            int y = (int) (40 + 100 * Math.floor(((double) i) / 20));
            Double height = 40 + 11 * Math.pow(2, length - 1);
            Double width = (double) (40 + 10 * length);
            result = result + "    <node id=\"n" + i + "\">\n";
            result += "      <data key=\"d5\"/>\n";
            result += "      <data key=\"d6\">\n";
            result += "        <y:ShapeNode>\n";
            result = result + "          <y:Geometry height=\"" + height + "\" width=\"" + width + "\" x=\"" + x + "\" y=\"" + y + "\"/>\n";
            result += "          <y:Fill color=\"#FFCC00\" transparent=\"false\"/>\n";
            result += "          <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n";
            result = result + "          <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\" width=\"10.673828125\" x=\"9.6630859375\" y=\"5.6494140625\">" + i + table + "</y:NodeLabel>\n";
            result += "          <y:Shape type=\"roundrectangle\"/>\n";
            result += "        </y:ShapeNode>\n";
            result += "      </data>\n";
            result += "    </node>\n";
        }
        int cnt = 0;
        for (int i = 0; i < this.network.length; i++) {
            for (int j = 0; j < this.network[i].length; j++) {
                if (this.network[i][j]) {
                    result = result + "    <edge id=\"e" + cnt + "\" source=\"n" + i + "\" target=\"n" + j + "\">\n";
                    result += "      <data key=\"d9\"/>\n";
                    result += "      <data key=\"d10\">\n";
                    result += "        <y:PolyLineEdge>\n";
                    result += "          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n";
                    result += "          <y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n";
                    result += "          <y:Arrows source=\"none\" target=\"standard\"/>\n";
                    result += "          <y:BendStyle smoothed=\"false\"/>\n";
                    result += "        </y:PolyLineEdge>\n";
                    result += "      </data>\n";
                    result += "    </edge>\n";
                    cnt++;
                }
            }
        }
        result += "  </graph>\n";
        result += "  <data key=\"d0\">\n";
        result += "    <y:Resources/>\n";
        result += "  </data>\n";
        result += "</graphml>\n";
        return result;
    }

    private Pair<Integer, String> generateTable(int i) {
        String result = "";
        double[] pTable = nodes[i].getPTable();
        int length = Integer.toBinaryString(pTable.length).length();
        for (int j = 0; j < pTable.length; j++) {
            result += "\n";
            String line = Integer.toBinaryString(j);
            while (line.length() < length - 1) {
                line = "0" + line;
            }
            line = line + ": " + pTable[j];
            result += line;
        }
        Pair<Integer, String> p = new Pair<Integer, String>();
        p.setHead(length);
        p.setTail(result);
        return p;
    }

    /**
     * has the network already an edge from node i to node j
     *
     * @param i the node from which the edge originates
     * @param j the node to which the edge points
     * @return is the edge already there
     */
    public boolean hasEdge(int i, int j) {
        return this.network[i][j];
    }

    public static void main(String[] args) {
//		BayNet b = new BayNet(3, 0.9, 0.1);
////		b.addEdge(0, 2);
//		b.addEdge(1, 2);
//		Population pop = new Population();
//		GAIndividualBinaryData indy1 = new GAIndividualBinaryData();
//		indy1.setBinaryDataLength(3);
//		GAIndividualBinaryData indy2 = (GAIndividualBinaryData) indy1.clone();
//		GAIndividualBinaryData indy3 = (GAIndividualBinaryData) indy1.clone();
//		GAIndividualBinaryData indy4 = (GAIndividualBinaryData) indy1.clone();
//		GAIndividualBinaryData indy5 = (GAIndividualBinaryData) indy1.clone();
//		BitSet data1 = indy1.getBinaryData();
//		BitSet data2 = indy2.getBinaryData();
//		BitSet data3 = indy3.getBinaryData();
//		BitSet data4 = indy4.getBinaryData();
//		BitSet data5 = indy5.getBinaryData();
//		data1.set(0, false);
//		data1.set(1, true);
//		data1.set(2, true);
//		
//		data2.set(0, true);
//		data2.set(1, false);
//		data2.set(2, true);
//		
//		data3.set(0, true);
//		data3.set(1, true);
//		data3.set(2, true);
//
//		data4.set(0, true);
//		data4.set(1, true);
//		data4.set(2, true);
//		
//		data5.set(0, true);
//		data5.set(1, true);
//		data5.set(2, true);
//		indy1.setBinaryGenotype(data1);
//		indy2.setBinaryGenotype(data2);
//		indy3.setBinaryGenotype(data3);
//		indy4.setBinaryGenotype(data4);
//		indy5.setBinaryGenotype(data5);
//		pop.add(indy1);
//		pop.add(indy2);
//		pop.add(indy3);
//		pop.add(indy4);
//		pop.add(indy5);
//		
////		System.out.println("-----");
//		System.out.println(pop.getStringRepresentation());
//		System.out.println("-----");
//		System.out.println(b.bayesianDirichletMetric(pop));
//		b.print();
        double val = SpecialFunction.gamma(26);
        double val2 = SpecialFunction.gamma(51);
        double val3 = (SpecialFunction.gamma(12 + 12.5)) / SpecialFunction.gamma(12.5);
        double val4 = (SpecialFunction.gamma(13 + 12.5)) / SpecialFunction.gamma(12.5);
        double erg = val / val2;
        erg = erg * val3 * val4;
        System.out.println(erg);
    }

    public void setScoringMethod(BOAScoringMethods method) {
        this.scoringMethod = method;
    }

    public BOAScoringMethods getScoringMethod() {
        return this.scoringMethod;
    }

    public int[][] adaptEdgeRate(int[][] edgeRate) {
        for (int i = 0; i < edgeRate.length; i++) {
            for (int j = 0; j < edgeRate.length; j++) {
                if (this.network[i][j]) {
                    edgeRate[i][j]++;
                }
            }
        }
        return edgeRate;
    }

}