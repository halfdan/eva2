package eva2.optimization.operators.nichepso.deactivation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.populations.Population;
import eva2.optimization.strategies.NichePSO;
import eva2.optimization.strategies.ParticleSubSwarmOptimization;
import eva2.tools.EVAERROR;
import java.util.List;
import java.util.Vector;

/**
 * A subswarm is deactivated if all its particles are converged. 
 * A particle is considered converged if the standard deviation of its fitness values 
 * over the last 3 iterations is less or equal a given threshold epsilon
 * Experiments showed good results using epsilon = 0.0001.
 *
 */
public class ImprovementDeactivationStrategy implements InterfaceDeactivationStrategy, java.io.Serializable {

	private double epsilon = 0.0001;
	private int haltingWindow = 15;

/**********************************************************************************************************************
 * ctors
 */	
	public ImprovementDeactivationStrategy(){
	}
	
	public ImprovementDeactivationStrategy(double thresh, int hwLen) {
		epsilon=thresh;
		haltingWindow=hwLen;
	}
	
	public ImprovementDeactivationStrategy(ImprovementDeactivationStrategy other){
		this.epsilon = other.epsilon;
		this.haltingWindow=other.haltingWindow;
	}
	
	public ImprovementDeactivationStrategy(double eps) {
		this.epsilon = eps;
	}
	
    @Override
	public Object clone(){
		return (Object) new ImprovementDeactivationStrategy(this);
	}
	
	public String globalInfo(){
		return "Strategy to deactivate subswarms";
	}
	
/**********************************************************************************************************************
 * shouldDeactivateSubswarm
 */	
	
	public boolean isConverged(Population pop){
//		Vector<AbstractEAIndividual> bests = new Vector<AbstractEAIndividual>(pop.size());
		
		Vector<Double> bests  = (Vector<Double>)pop.getEAIndividual(0).getData(NichePSO.fitArchiveKey);
		if (bests.size()<haltingWindow) {
			return false;
		} else {
			List<Double> lst = bests.subList(bests.size()-haltingWindow, bests.size());
			bests = new Vector<Double>(haltingWindow);
			bests.addAll(lst);
			for (int i=1; i<pop.size(); i++) {
				for (int k = 0; k < haltingWindow; k++) {
					Vector<Double> fitArch  = (Vector<Double>)pop.getEAIndividual(i).getData(NichePSO.fitArchiveKey);
					int archIndex=fitArch.size()-haltingWindow+k; // index within the fitness archive of the current particle, which may be larger than the bests list - the tail is important
					if (archIndex>=0 && (fitArch.get(archIndex)<bests.get(k))) {
                                        bests.set(k, fitArch.get(archIndex));
                                    }
				}
			}
		}
		// bests contains now the sequence of best fitness values across the last generations
		Double historicHWAgo = bests.get(0);
		boolean res=true;
		for (int i = 1; i < haltingWindow; i++) {
			// if historic[-hW] is worse than historic[-hW+i] return false
			Double historicIter = bests.get(i);
			// if the iterated value (the later one in history) has improved, there is no convergence.
			boolean improvementHappened = (historicIter < historicHWAgo);// testSecondForImprovement(historicHWAgo, historicIter));
			if (improvementHappened) {
				res = false;
				break;
			}
		}
		return res;
	}
	
	/**  @tested 
	 * true if the subswarm is active and all particles are completely converged
	 * (i.e. the stddev over the past 3 iterations is < epsilson)
	 * (non-Javadoc) @see javaeva.server.oa.go.Operators.NichePSO.InterfaceDeactivationStrategy#shouldDeactivateSubswarm(javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization)
	 */
    @Override
	public boolean shouldDeactivateSubswarm(ParticleSubSwarmOptimization subswarm) {
		if (!subswarm.isActive()){
			return false;
		}
		if (subswarm.getFitnessArchiveSize()<haltingWindow) {
                EVAERROR.errorMsgOnce("Warning: halting window length " + haltingWindow + " too long for sub swarm template, which stores only " + subswarm.getFitnessArchiveSize() + " fitness values!");
            }
		return (isConverged(subswarm.getPopulation()));
	}
	
/**********************************************************************************************************************
 * deactivateSubswarm
 */		

	/**  @tested 
	 * the subswarm is deactivated and the particles indices are returned. They are 
	 * to be reinitialized into the mainswarm.
	 * (non-Javadoc) @see javaeva.server.oa.go.Operators.NichePSO.InterfaceDeactivationStrategy#deactivateSubswarm(javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization, javaeva.server.oa.go.Strategies.ParticleSubSwarmOptimization)
	 */
    @Override
	public int[] deactivateSubswarm(ParticleSubSwarmOptimization subswarm, ParticleSubSwarmOptimization mainswarm) {
		if (!subswarm.isActive()){
			System.out.println("deactivateSubSwarm: try to deactivate inactive subswarm");
			return null;
		}

		// use the indizes of the deactivated particles for the reinitialized particles (important for ANPSO)
		Population pop = subswarm.getPopulation();
		int[] particleIndices = new int[pop.size()];
		for (int i = 0; i < pop.size(); ++i){
			AbstractEAIndividual indy = pop.getEAIndividual(i);
			//Integer index = (Integer)indy.getData("particleIndex");
			particleIndices[i] = indy.getIndividualIndex();//index.intValue();
		}
		subswarm.SetActive(false);
		return particleIndices;
	}
	
/**********************************************************************************************************************
 * getter, setter
 */

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public String epsilonTipText(){
		return "If the fitness improves by less than this threshold within the halting window, convergence is assumed.";
	}

	public int getHaltingWindowLen() {
		return haltingWindow;
	}
	public void setHaltingWindowLen(int hwLen) {
		this.haltingWindow = hwLen;
	}	
	public String haltingWindowLenTipText() {
		return "The number of generations to be tested for improvement"; 
	}
}
