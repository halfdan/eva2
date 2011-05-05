package eva2.server.go.strategies;

import java.util.BitSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.BKnapsackProblem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.tools.math.BayNet;
import eva2.tools.math.RNG;

/**
 * Basic implementation of the Bayesian Optimization Algorithm
 * 
 * Martin Pelikan, David E. Goldberg and Erick Cantu-Paz: 'BOA: The Bayesian Optimization Algorithm'
 * the works by Martin Pelikan and David E. Goldberg.
 * 
 * @author seitz
 *
 */
public class BOA implements InterfaceOptimizer, java.io.Serializable {

	private static boolean 										TRACE = false;
	transient private InterfacePopulationChangedEventListener	m_Listener	 = null;
	private String 												m_Identifier = "BOA";

	private int							probDim			= 3;
	private int							fitCrit			= -1;
	private int							PopSize			= 50;
	private int							numberOfParents	= 3;
	private boolean						replaceNetwork	= true;
	private transient BayNet			network			= null;
	private Population 					population 		= new Population();
	private AbstractOptimizationProblem	problem			= new BKnapsackProblem();
	private AbstractEAIndividual 		template 		= null;
	private double						learningSetRatio = 0.5;
	private double						resampleRatio 	= 0.5;
	private double 						upperProbLimit	= 0.9;
	private double 						lowerProbLimit	= 0.1;

	//	private	networkGenerationMethod		netGenMethod	= networkGenerationMethod.GREEDY;
	//	public enum networkGenerationMethod { GREEDY, K2 };

	public BOA(){

	}
	
	public BOA(int numberOfParents, int popSize, boolean replaceNetwork, double learningSetRatio, double resampleRatio){
		this.numberOfParents = numberOfParents;
		this.PopSize = popSize;
		this.replaceNetwork = replaceNetwork;
		this.learningSetRatio = learningSetRatio;
		this.resampleRatio = resampleRatio;
	}

	public BOA(BOA b){
		this.m_Listener 		= b.m_Listener;
		this.m_Identifier 		= b.m_Identifier;
		this.probDim			= b.probDim;
		this.fitCrit			= b.fitCrit;
		this.PopSize			= b.PopSize;
		this.numberOfParents	= b.numberOfParents;
		this.replaceNetwork		= b.replaceNetwork;
		this.network			= (BayNet) b.network.clone();
		this.population			= (Population) b.population.clone();
		this.problem			= (AbstractOptimizationProblem) b.problem.clone();
		this.template			= (AbstractEAIndividual) b.template.clone();
		this.learningSetRatio	= b.learningSetRatio;
		this.resampleRatio		= b.resampleRatio;
		this.upperProbLimit		= b.upperProbLimit;
	}

	public Object clone(){
		return new BOA(this);
	}

	public String getName() {
		return "Bayesian Optimization Algorithm";
	}

	public static String globalInfo() {
		return "Basic implementation of the Bayesian Optimization Algorithm based on the works by Martin Pelikan and David E. Goldberg.";
	}
	
	public void hideHideable() {
		GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
	}
	
	public void addPopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
	}

	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			return true;
		} else return false;
	}

	private static BitSet getBinaryData(AbstractEAIndividual indy) {
		if (indy instanceof InterfaceGAIndividual) return ((InterfaceGAIndividual)indy).getBGenotype();
		else if (indy instanceof InterfaceDataTypeBinary) return ((InterfaceDataTypeBinary)indy).getBinaryData();
		else {
			throw new RuntimeException("Unable to get binary representation for " + indy.getClass());
		}
	}

	/**
	 * evaluate the given Individual and increments the counter. if the individual is null, only the counter is incremented
	 * @param indy the individual you want to evaluate
	 */
	private void evaluate(AbstractEAIndividual indy){
		// evaluate the given individual if it is not null
		if(indy == null){
			System.err.println("tried to evaluate null");
			return;
		}
		this.problem.evaluate(indy);
		// increment the number of evaluations 
		this.population.incrFunctionCalls();
	}

	/**
	 * the default initialization
	 */
	private void defaultInit(){
		if (population==null) {
			this.population = new Population(this.PopSize);
		} else {
			this.population.setTargetPopSize(this.PopSize);
		}
		this.template = this.problem.getIndividualTemplate();
		if (!(template instanceof InterfaceDataTypeBinary)){
			System.err.println("Requiring binary data!");
		}else{
			Object dim = BeanInspector.callIfAvailable(problem, "getProblemDimension", null);
			if (dim==null) System.err.println("Couldnt get problem dimension!");
			probDim = (Integer)dim;
			((InterfaceDataTypeBinary)this.template).SetBinaryGenotype(new BitSet(probDim));
		}
		this.network = new BayNet(this.probDim, upperProbLimit, lowerProbLimit);
	}

	public void init() {
		defaultInit();
		this.problem.initPopulation(this.population);
		this.evaluatePopulation(this.population);
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);
	}

	private void evaluatePopulation(Population pop) {
		for (int i=0; i<pop.size(); i++) {
			evaluate(pop.getEAIndividual(i));
		}
	}

	public void initByPopulation(Population pop, boolean reset) {
		if(reset){
			init();
		}else{
			defaultInit();
			this.population = (pop);
		}
	}

	private void generateGreedy(Population pop){
		this.network = new BayNet(this.probDim, upperProbLimit, lowerProbLimit);
//		BayNet net = (BayNet) this.network.clone();
//		BayNet best = (BayNet) this.network.clone();

		boolean improvement = true;
		double score = this.network.bayesianDirichletMetric(pop);
		score = 0;
//		Date time = new Date();
//		System.out.println("Start: "+time.getHours()+":"+time.getMinutes()+":"+time.getSeconds());
		List<Pair<Integer, Integer>> bestNetworks = new LinkedList<Pair<Integer, Integer>>();
		while(improvement){
			improvement = false;
//			System.out.println("score:"+score);
			for(int i=0; i<this.probDim; i++){
				for(int j=0; j<this.probDim; j++){
					if((!this.network.hasEdge(i, j)) && (i != j)  && (this.network.getNode(j).getNumberOfParents() < this.numberOfParents)){
						BayNet tmp = this.network;
						tmp.addEdge(i, j);
						if(tmp.isACyclic(i, j)){
							double tmpScore = tmp.bayesianDirichletMetric(pop);
							if(tmpScore >= score){
								if(tmpScore == score){
									bestNetworks.add(new Pair<Integer, Integer>(i, j));
								}else{
									bestNetworks.clear();
									bestNetworks.add(new Pair<Integer, Integer>(i, j));
									score = tmpScore;
									improvement = true;
								}
							}
						}
						this.network.removeEdge(i, j);
					}
				}
			}
			if(bestNetworks.size() > 0){
				int val = RNG.randomInt(bestNetworks.size());
				Pair<Integer, Integer> pair = bestNetworks.get(val);
				this.network.addEdge(pair.getHead(), pair.getTail());
			}
			bestNetworks.clear();
		}
//		time = new Date();
//		System.out.println("Stop: "+time.getHours()+":"+time.getMinutes()+":"+time.getSeconds());
	}

	private boolean expandGreedy(Population pop){
		BayNet net = (BayNet) this.network.clone();
		BayNet best = (BayNet) this.network.clone();
		boolean improv = false;
		boolean improvement = true;
		double score = net.bayesianDirichletMetric(pop);
		Date time = new Date();
//		System.out.println("Start: "+time.getHours()+":"+time.getMinutes()+":"+time.getSeconds());
		while(improvement){
			improvement = false;
//			System.out.println("score:"+score);
			for(int i=0; i<this.probDim; i++){
				for(int j=0; j<this.probDim; j++){
					if((!net.hasEdge(i, j)) && (i != j)  && (net.getNode(j).getNumberOfParents() < this.numberOfParents)){
						BayNet tmp = (BayNet) net.clone();
						tmp.addEdge(i, j);
						if(tmp.isACyclic()){
							double tmpScore = tmp.bayesianDirichletMetric(pop);
							if(tmpScore > score){
								best = (BayNet) tmp.clone();
								score = tmpScore;
								improvement = true;
								improv = true;
							}
						}
					}
				}
			}
			net = (BayNet) best.clone();;
		}
		time = new Date();
//		System.out.println("Stop: "+time.getHours()+":"+time.getMinutes()+":"+time.getSeconds());
		this.network = (BayNet) best.clone();
		return improv;
	}

	/**
	 * Generate a Bayesian network with the individuals of the population as a reference Point 
	 * @param pop	the individuals the network is based on
	 */
	private void constructNetwork(Population pop){
		if(this.replaceNetwork){
			generateGreedy(pop);
		}else{
			boolean improve = expandGreedy(pop);
			if(!improve){
				generateGreedy(pop);
			}
		}
		//TODO
	}

	/**
	 * generate new individuals based on the bayesian network
	 * @return 	the new individuals
	 */
	private Population generateNewIndys(int sampleSetSize){
		Population pop = new Population(sampleSetSize);
		if (TRACE) System.out.println("Resampling " + sampleSetSize + " indies...");
		while(pop.size() < sampleSetSize){
			AbstractEAIndividual indy = (AbstractEAIndividual) this.template.clone();
			BitSet data = this.network.sample(getBinaryData(indy));
			((InterfaceDataTypeBinary) indy).SetBinaryGenotype(data);
			evaluate(indy);
			pop.add(indy);
		}
		return pop;
	}

	/**
	 * Calculate a plausible number of individuals to be resampled per iteration.
	 * @return
	 */
	private int calcResampleSetSize() {
		int result = (int)Math.min(PopSize, Math.max(1.0, ((double)PopSize)*resampleRatio));
//		System.out.println(result);
		return result;
	}

	/**
	 * Calculate a plausible number of individuals from which the BayNet is learned. 
	 * In principle this can be independent of the resampling set size. 
	 * @return
	 */
	private int calcLearningSetSize() {
		return (int)Math.min(PopSize, Math.max(1.0, ((double)PopSize)*learningSetRatio));
	}
	
	public void remove(Population pop){
		for(Object indy: pop){
			this.population.remove(indy);
		}
	}

	public void optimize() {
		Population best = this.population.getBestNIndividuals(calcLearningSetSize(), this.fitCrit);
		constructNetwork(best);
		Population newlyGenerated = generateNewIndys(calcResampleSetSize());
		Population toRemove = this.population.getWorstNIndividuals(calcResampleSetSize(), this.fitCrit);
		remove(toRemove);
		this.population.addAll(newlyGenerated);
//		print();
		this.firePropertyChangedEvent(Population.nextGenerationPerformed);
	}


	/** Something has changed
	 */
	protected void firePropertyChangedEvent (String name) {
		if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
	}

	public Population getPopulation() {
		return this.population;
	}

	public void setPopulation(Population pop) {
		this.population = pop;
	}

	public InterfaceSolutionSet getAllSolutions() {
		return new SolutionSet(this.population);
	}

	public void SetIdentifier(String name) {
		this.m_Identifier = name;
	}

	public String getIdentifier() {
		return this.m_Identifier;
	}

	public void SetProblem(InterfaceOptimizationProblem problem) {
		this.problem = (AbstractOptimizationProblem) problem;
	}

	public InterfaceOptimizationProblem getProblem() {
		return this.problem;
	}

	public String getStringRepresentation() {
		return "Bayesian Network";
	}

	public void freeWilly() {

	}

	//-------------------------------
	//-------------GUI---------------
	//-------------------------------

	public int getNumberOfParents(){
		return this.numberOfParents;
	}

	public void setNumberOfParents(int i){
		this.numberOfParents = i;
	}

	public String numberOfParentsTipText(){
		return "The maximum number of parents a node in the Bayesian Network can have";
	}

	public boolean getReplaceNetwork(){
		return this.replaceNetwork;
	}

	public void setReplaceNetwork(boolean b){
		this.replaceNetwork = b;
	}

	public String replaceNetworkTipText(){
		return "if set, the network will be completely replaced. If not, it will be tried to improve the last network, if that is not possible, it will be replaced";
	}

	//	public networkGenerationMethod getNetworkGenerationMethod(){
	//		return this.netGenMethod;
	//	}
	//	
	//	public void setNetworkGenerationMethod(networkGenerationMethod n){
	//		this.netGenMethod = n;
	//	}
	//	
	//	public String networkGenerationMethodTipText(){
	//		return "The Method with which the Bayesian Network will be gererated";
	//	}
	
	public void print(){
		this.network.print();
	}
	
	public static void main(String[] args){
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
		AbstractEAIndividual ind = (AbstractEAIndividual) indy2.clone();
		pop.add(ind);
//		pop.add(indy3);
//		pop.add(indy4);
//		pop.add(indy5);
		BOA b = new BOA();
		b.generateGreedy(pop);
		System.out.println(pop.getStringRepresentation());
		b.print();
	}

	public int getPopulationSize() {
		return PopSize;
	}
	public void setPopulationSize(int popSize) {
		PopSize = popSize;
	}
	public String populationSizeTipText() {
		return "Define the pool size used by BOA";
	}

	public double getResamplingRatio() {
		return resampleRatio;
	}
	public void setResamplingRatio(double resampleRat) {
		this.resampleRatio = resampleRat;
	}
	public String resamplingRatioTipText() {
		return "Ratio of individuals to be resampled from the Bayesian network per iteration";
	}
	
	public double getLearningRatio() {
		return learningSetRatio;
	}
	public void setLearningRatio(double rat) {
		this.learningSetRatio = rat;
	}
	public String learningRatioTipText() {
		return "Ratio of individuals to be used to learn the Bayesian network";
	}
	
	public double getProbLimitHigh() {
		return upperProbLimit;
	}

	public void setProbLimitHigh(double upperProbLimit) {
		this.upperProbLimit = upperProbLimit;
	}
	
	public String probLimitHighTipText(){
		return "the upper limit of the probability to set one Bit to 1";
	}

	public double getProbLimitLow() {
		return lowerProbLimit;
	}

	public void setProbLimitLow(double lowerProbLimit) {
		this.lowerProbLimit = lowerProbLimit;
	}
	
	public String probLimitLowTipText(){
		return "the lower limit of the probability to set one Bit to 1";
	}
	
	public String[] customPropertyOrder() {
		return new String[] {"learningRatio", "resamplingRatio"};
	}
	
}
