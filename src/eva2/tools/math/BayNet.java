package eva2.tools.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.Pair;

public class BayNet {

	private boolean[][]		network 		= null;
	private int 			dimension		= 5;
	private BayNode[]		nodes			= null;
	private List<BayNode>	rootNodes		= new LinkedList<BayNode>();
	private double 			upperProbLimit	= 0.9;
	private double 			lowerProbLimit	= 0.1;

	public BayNet(int dimension, double upperLimit, double lowerLimit){
		this.dimension = dimension;
		this.upperProbLimit = upperLimit;
		this.lowerProbLimit = lowerLimit;
		init();
	}

	public BayNet(BayNet b){
		this.network = cloneNetwork(b.network);
		this.dimension = b.dimension;
		this.nodes = new BayNode[b.dimension];
		for(int i=0; i<this.nodes.length; i++){
			this.nodes[i] = (BayNode) b.nodes[i].clone();
		}
		this.rootNodes = new LinkedList<BayNode>();
		for(BayNode node: b.rootNodes){
			this.rootNodes.add(this.nodes[node.getId()]);
		}
		this.upperProbLimit = b.upperProbLimit;
		this.lowerProbLimit = b.lowerProbLimit;
	}

	public Object clone(){
		return new BayNet(this);
	}
	
	private boolean[][] cloneNetwork(boolean[][] b){
		boolean[][] result = new boolean[b.length][b.length];
		for(int i=0; i<b.length; i++){
			for(int j=0; j<b.length; j++){
				if(b[i][j]){
					result[i][j] = true;
				}
			}
		}
		return result;
	}

	/**
	 * initialize the Network
	 */
	public void init(){
		this.network = new boolean[this.dimension][this.dimension];
		this.nodes = new BayNode[this.dimension];
		for(int i=0; i<this.dimension; i++){
			this.nodes[i] = new BayNode(i);
			this.rootNodes.add(this.nodes[i]);
		}
	}

	private static BitSet getBinaryData(AbstractEAIndividual indy) {
		if (indy instanceof InterfaceGAIndividual) return ((InterfaceGAIndividual)indy).getBGenotype();
		else if (indy instanceof InterfaceDataTypeBinary) return ((InterfaceDataTypeBinary)indy).getBinaryData();
		else {
			throw new RuntimeException("Unable to get binary representation for " + indy.getClass());
		}
	}

	/**
	 * 
	 * @return	a list of the root nodes
	 */
	public List<BayNode> getRootNodes(){
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
		return this.rootNodes;
	}

	/**
	 * get the i-th node
	 * @param 	i the node to be returned 	
	 * @return	the requested node
	 */
	public BayNode getNode(int i){
		return this.nodes[i];
	}

	/**
	 * return the children for a given node
	 * @param 	n the node
	 * @return	the children of the node
	 */
	public List<BayNode> getChildren(BayNode n){
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
		for(int i: ids){
			result.add(this.nodes[i]);
		}
		return result;
	}

	/**
	 * return the parents for a given node
	 * @param n	the node
	 * @return	the parents of the node
	 */
	public List<BayNode> getParents(BayNode n){
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
		for(int i: ids){
			result.add(this.nodes[i]);
		}
		return result;
	}

	/**
	 * return the children of a list of nodes
	 * @param n	the list of nodes
	 * @return	the children of the nodes
	 */
	public List<BayNode> getChildren(List<BayNode> n){
		ArrayList<BayNode> result = new ArrayList<BayNode>();
		for(BayNode node: n){
			List<BayNode> children = getChildren(node);
			for(BayNode nod: children){
				if(!result.contains(nod)){
					result.add(nod);
				}
			}
		}
		return result;
	}
	
	/**
	 * remove the edge from node i to node j
	 * @param i	the node from which the edge comes
	 * @param j	the node to which the edge points
	 */
	public void removeEdge(int i, int j){
		if(this.network[i][j]){
			this.network[i][j] = false;
			this.nodes[j].decrNumberOfParents();
			this.nodes[i].removeChild(j);
			this.nodes[j].removeParent(i);
			this.nodes[j].generateNewPTable();
			if(this.nodes[j].getNumberOfParents() == 0){
				this.rootNodes.add(nodes[j]);
			}
		}
	}

	/**
	 * add an edge from the node i to the node j
	 * @param i	edge from this node
	 * @param j	edge to this node
	 */
	public void addEdge(int i, int j){
		if(!this.network[i][j]){
			this.network[i][j] = true;
			this.nodes[j].incrNumberOfParents();
			this.nodes[j].generateNewPTable();
			this.rootNodes.remove(this.nodes[j]);
			this.nodes[i].addChild(j);
			this.nodes[j].addParent(i);
		}
	}

	/**
	 * find the next value where all the parents are already set
	 * @param data
	 * @return
	 */
	private int findNext(double[] probabilities, List<BayNode> nodes){
		for(BayNode node: nodes){
			List<BayNode> parents = getParents(node);
			boolean possible = false;
			for(BayNode p: parents){
				if(probabilities[p.getId()] != -1){
					possible = true;
				}else{
					possible = false;
					break;
				}
			}
			if(possible){
				return node.getId();
			}
		}
		return -1;
	}

	/**
	 * calculate a new BitSet according to the network
	 * @param data	the BitSet that will be calculated
	 * @return		the new BitSet
	 */
	public BitSet sample(BitSet data){
		// generate a new probabilities-vector
		double[] probabilities = new double[this.network.length];
		for(int i=0; i<probabilities.length; i++){
			probabilities[i] = -1;
		}
		// get the root-nodes (the non dependent nodes)
		List<BayNode> nodes = getRootNodes();
		// calculate the BitSet-Value for these nodes
		for(BayNode node: nodes){
			int id = node.getId();
			probabilities[id] = node.getProbability(0);
			data.set(id, RNG.flipCoin(probabilities[id]));
		}
		// find the next node that can be evaluated
		List<BayNode> toCalculate = getChildren(nodes);
		int next = findNext(probabilities, toCalculate);
		while(next != -1){
			toCalculate.remove(this.nodes[next]);
			probabilities[next] = calculateNextProbability(data, toCalculate, next);
			data.set(next, RNG.flipCoin(probabilities[next]));
			next = findNext(probabilities, toCalculate);
		}
		return data;
	}

	/**
	 * calculate the next probability
	 * @param data			the already calculated data
	 * @param probabilities	the already calculated probabilities
	 * @param toCalculate	the Nodes that have yet to be calculated
	 * @param next			the node for which to calculate the probability
	 * @return				the new probabilities array
	 */
	private double calculateNextProbability(BitSet data, List<BayNode> toCalculate, int next) {
		toCalculate.addAll(getChildren(this.nodes[next]));
		int[] parId = calculateSortedParentIds(next);
		int prob = 0;
		int cnt = 0;
		for(int j=parId.length-1; j>=0; j--){
			if(data.get(parId[j])){
				prob += (int) Math.pow(2, j);
			}
			cnt++;
		}
		return this.nodes[next].getProbability(prob);
	}

	/**
	 * generate an array of the parents, sorted by there id
	 * @param id	the id of the node
	 * @return		the sorted parent-ids
	 */
	private int[] calculateSortedParentIds(int id) {
		List<BayNode> parents = getParents(this.nodes[id]);
		int[] parId = new int[parents.size()];
		int i=0;
		for(BayNode nod: parents){
			parId[i] = nod.getId();
			i++;
		}
		Arrays.sort(parId);
		return parId;
	}

	/**
	 * generate an array of the parents plus the given node, sorted by there id
	 * @param id	the id of the node
	 * @return		the sorted parent-ids
	 */
	private int[] calculateSortedParentPlusNodeIds(int id) {
		List<BayNode> nodes = getParents(this.nodes[id]);
		nodes.add(this.nodes[id]);
		int[] sortedIds = new int[nodes.size()];
		int i=0;
		for(BayNode nod: nodes){
			sortedIds[i] = nod.getId();
			i++;
		}
		Arrays.sort(sortedIds);
		return sortedIds;
	}
	
	private void resetCalculated(){
		for(int i=0; i<this.dimension; i++){
			this.nodes[i].setCalculated(false);
		}
	}
	
	/**
	 * see if the network is still acyclic after inserting the edge
	 * @param from	the node from which the edge comes from
	 * @param to	the node to which the edgte points
	 * @return		is the network still acyclic
	 */
	public boolean isACyclic(int from, int to){
		int cnt1 = 0;
		int cnt2 = 0;
		for(int i=0; i<this.dimension; i++){
			if(this.network[i][from]){
				cnt1++;
			}
			if(this.network[to][i]){
				cnt2++;
			}
		}
		// if the from node has no incoming edges or the to node has no outgoing edges the network is still acyclic
		if(cnt1==0 || cnt2==0){
			return true;
		}
		// look at all the children and see if we can get to the from-node from the to-node
		List<BayNode> toCalculate = getChildren(this.nodes[to]);
		while(!toCalculate.isEmpty()){
			BayNode node = toCalculate.get(0);
			toCalculate.remove(node);
			if(!node.getCalculated()){
				node.setCalculated(true);
				if(from == node.getId()){
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
	 * @param net	the Network
	 * @return		is the net acyclic
	 */
	public boolean isACyclic(){
		List<Pair<Integer,Integer>> deletedEdges = new LinkedList<Pair<Integer,Integer>>();
		List<BayNode> nodes = getRootNodes();
		boolean res=false;
		for(int i=0; i<=this.dimension; i++){
			for(BayNode node: nodes){
				int id = node.getId();
				for(int j=0; j<this.dimension; j++){
					if (this.network[id][j]) {
						this.network[id][j] = false;
						deletedEdges.add(new Pair<Integer,Integer>(id,j));
					}
				}
			}
			nodes = getRootNodes();
			// if we only have root nodes, we have an acyclic graph
			if(nodes.size() == this.nodes.length){
				res = true;
				break;
			}
		}
//		System.out.println("Deleted edges: " + BeanInspector.toString(deletedEdges));
		for (Pair<Integer,Integer> edge : deletedEdges) {
			this.network[edge.head][edge.tail] = true;
		}
		return res;
	}

	private double getPrior(List<BayNode> parents, Population pop){
		return (double) pop.size() / Math.pow(2.0, (double) parents.size());
	}

	private double getPrior(List<BayNode> parents, BayNode current, Population pop){
		return getPrior(parents, pop) / 2.0;
	}
	
	private void setRootPTables(Population pop){
		List<BayNode> rootNodes = getRootNodes();
		for(BayNode node: rootNodes){
			int id = node.getId();
			double count = 0;
			for(int i=0; i<pop.size(); i++){
				BitSet data = getBinaryData(pop.getEAIndividual(i));
				if(data.get(id)){
					count++;
				}
			}
			double prob = count / (double) pop.size();
			setProbability(node, 0, prob);
//			node.setPTable(0, count / (double) pop.size());
		}
	}

	public void setUpperProbLimit(double upperProbLimit) {
		this.upperProbLimit = upperProbLimit;
	}

	public void setLowerProbLimit(double lowerProbLimit) {
		this.lowerProbLimit = lowerProbLimit;
	}

	/**
	 * calculate the bayesian Dirichlet Metric
	 * @param pop	the population on which the metric is based on
	 * @return		the metric
	 */
	public double bayesianDirichletMetric(Population pop){
		double result = 1.0;
		//for every node
		setRootPTables(pop);
		for(int i=0; i<this.dimension; i++){
			BayNode currentNode = this.nodes[i];
			//get the parents
			List<BayNode> parents = getParents(currentNode);
//			System.out.println("parents: "+parents.size());
			// get the parentIds sorted (for the lookup)
			if(!parents.isEmpty()){
				int[] parId = calculateSortedParentIds(i);
				// the parentIds plus the id of the current node sorted (for the lookup
				int[] nodeIds = calculateSortedParentPlusNodeIds(i);
				double[] pTable = currentNode.getPTable();
				for(int j=0; j<pTable.length; j++){
					Population pop2 = numberSetCorrectly(pop, j, parId);
					double count = (double) pop2.size();
					double numeratorFirstFraction = SpecialFunction.gamma(getPrior(parents, pop));
					double denominatorFirstFraction = SpecialFunction.gamma(getPrior(parents, pop)+count);
					double firstFraction = numeratorFirstFraction / denominatorFirstFraction;
					result = result * firstFraction;
//					currentNode.setPTable(j, count / (double) pop.size());
					count = 0;
					for(int k=0; k<2; k++){
						double cnt = numberSetCorrectly(pop2, j, k, nodeIds, parId);
						double numeratorSecondFraction = SpecialFunction.gamma(getPrior(parents, currentNode, pop) + cnt);
						double denumeratorSecondFraction = SpecialFunction.gamma(getPrior(parents, currentNode, pop));
						double secondFraction = numeratorSecondFraction / denumeratorSecondFraction;
						result = result * secondFraction;
						double prob = cnt / (double) pop2.size();
						setProbability(currentNode, j, prob);
						cnt = 0;
					}
				}
			}
		}
		return result;
	}
	
	private void setProbability(BayNode n, int j, double prob){
		n.setPTable(j, Math.min(upperProbLimit, Math.max(lowerProbLimit, prob)));
	}

	private double numberSetCorrectly(Population pop, int j, int k, int[] Ids, int[] parIds){
		double result = 0.0;
		String binaryString = Integer.toBinaryString(j);
		while(binaryString.length() < parIds.length){
			binaryString = "0"+binaryString;
		}
		boolean found = false;
		boolean end = false;
		int different = 0;
		for(int i=0; i<parIds.length; i++){
			if(parIds[i] != Ids[i]){
				different = i;
				found = true;
				break;
			}
		}
		if(!found){
			different = Ids.length;
			end = true;
		}
		if(end){
			binaryString = binaryString+k;
		}else{
			binaryString = binaryString.substring(0, different)+k+binaryString.substring(different);
		}
		int l = Integer.parseInt(binaryString);
//		binary = getBinaryArray(Ids, binaryString);
		for(int i=0; i<pop.size(); i++){
			AbstractEAIndividual indy = pop.getEAIndividual(i);
			BitSet data = ((InterfaceDataTypeBinary) indy).getBinaryData();
			boolean setCorrectly = isSetCorrectly(Ids, data, l);
			if(setCorrectly){
				result ++;
			}
		}
		return result;
	}
	
	private Population numberSetCorrectly(Population pop, int j, int[] Ids){
		Population result = new Population();
//		String binaryString = Integer.toBinaryString(j);
		// append zeroes to the front
//		boolean[] binary = getBinaryArray(Ids, binaryString);
		for(int i=0; i<pop.size(); i++){
			AbstractEAIndividual indy = pop.getEAIndividual(i);
			BitSet data = ((InterfaceDataTypeBinary) indy).getBinaryData();
			boolean setCorrectly = isSetCorrectly(Ids, data, j);
			if(setCorrectly){
//				result ++;
				result.add(indy);
			}
		}
		return result;
	}

	/**
	 * is the BitSet of the individual set correctly corresponding to the binary String and the parId
	 * @param ids		the Ids of the parents sorted
	 * @param data		the data of the individual to be checked
	 * @param j			how the Bits have to be set (as Integer)
	 * @return			is the data set correctly
	 */
	private boolean isSetCorrectly(int[] ids, BitSet data, int j) {
		boolean setCorrectly = false;
		for(int m=0; m<ids.length; m++){
			if(((j & (1<<m))>0) && (data.get(ids[m]))){
				setCorrectly = true;
			}else if(!((j & (1<<m))>0) && (!data.get(ids[m]))){
				setCorrectly = true;
			}else{
				setCorrectly = false;
				m = j+10;
			}
		}
		return setCorrectly;
	}
	
	public void print(){
		for(int i=0; i<this.dimension; i++){
			for(int j=0; j<this.dimension; j++){
				if(this.network[i][j]){
					System.out.print("1");
				}else{
					System.out.print("0");
				}
			}
			System.out.println();
		}
		for(int i=0; i<this.dimension; i++){
			System.out.println(BeanInspector.toString(nodes[i].getPTable()));
		}
	}

	/**
	 * has the network already an edge from node i to node j
	 * @param i	the node from which the edge originates
	 * @param j	the node to which the edge points
	 * @return	is the edge already there
	 */
	public boolean hasEdge(int i, int j){
		return this.network[i][j];
	}

	public static void main(String[] args){
		BayNet b = new BayNet(3, 0.9, 0.1);
		b.addEdge(0, 2);
		b.addEdge(1, 2);
		Population pop = new Population();
		GAIndividualBinaryData indy1 = new GAIndividualBinaryData();
		indy1.setBinaryDataLength(3);
		GAIndividualBinaryData indy2 = (GAIndividualBinaryData) indy1.clone();
		GAIndividualBinaryData indy3 = (GAIndividualBinaryData) indy1.clone();
		GAIndividualBinaryData indy4 = (GAIndividualBinaryData) indy1.clone();
		GAIndividualBinaryData indy5 = (GAIndividualBinaryData) indy1.clone();
		BitSet data1 = indy1.getBinaryData();
		BitSet data2 = indy2.getBinaryData();
		BitSet data3 = indy3.getBinaryData();
		BitSet data4 = indy4.getBinaryData();
		BitSet data5 = indy5.getBinaryData();
		data1.set(0, true);
		data1.set(1, true);
		data1.set(2, false);
		data2.set(0, true);
		data2.set(1, true);
		data2.set(2, true);
		data3.set(0, false);
		data3.set(1, true);
		data3.set(2, false);
		data4.set(0, false);
		data4.set(1, true);
		data4.set(2, true);
		data5.set(0, true);
		data5.set(1, false);
		data5.set(2, false);
		indy1.SetBinaryGenotype(data1);
		indy2.SetBinaryGenotype(data2);
		indy3.SetBinaryGenotype(data3);
		indy4.SetBinaryGenotype(data4);
		indy5.SetBinaryGenotype(data5);
		pop.add(indy1);
		pop.add(indy2);
		pop.add(indy3);
		pop.add(indy4);
		pop.add(indy5);
		
//		System.out.println("-----");
//		System.out.println(pop.getStringRepresentation());
//		System.out.println("-----");
		System.out.println(b.bayesianDirichletMetric(pop));
	}

}
