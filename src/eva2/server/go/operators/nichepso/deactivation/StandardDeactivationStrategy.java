package eva2.server.go.operators.nichepso.deactivation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.NichePSO;
import eva2.server.go.strategies.ParticleSubSwarmOptimization;
import eva2.tools.EVAERROR;
import java.util.Vector;

/**
 * A subswarm is deactivated if all its particles are converged. 
 * A particle is considered converged if the standard deviation of its fitness values 
 * over the last 3 iterations is less or equal a given threshold epsilon
 * Experiments showed good results using epsilon = 0.0001.
 *
 */
public class StandardDeactivationStrategy implements InterfaceDeactivationStrategy, java.io.Serializable {

	private double epsilon = 0.0001;
	private int stdDevHorizon = 3;

/**********************************************************************************************************************
 * ctors
 */	
	public StandardDeactivationStrategy(){
		
	}
	
	public StandardDeactivationStrategy(StandardDeactivationStrategy other){
		this.epsilon = other.epsilon;
		this.stdDevHorizon = other.stdDevHorizon;
	}
	
	public StandardDeactivationStrategy(double eps, int horizon) {
		this.epsilon = eps;
		this.stdDevHorizon = horizon;
	}
	
	public StandardDeactivationStrategy(double eps) {
		this.epsilon = eps;
	}
	
    @Override
	public Object clone(){
		return (Object) new StandardDeactivationStrategy(this);
	}
	
	public String globalInfo(){
		return "Strategy to deactivate subswarms";
	}
	
/**********************************************************************************************************************
 * shouldDeactivateSubswarm
 */	
	/** @tested
	 * @param pop
	 * @return
	 */
//	public boolean areAllConverged(Population pop){
//		for (int i = 0; i < pop.size(); ++i){
//			AbstractEAIndividual currentindy = pop.getEAIndividual(i);
//			Double sd = (Double)currentindy.getData(NichePSO.stdDevKey);
//			if (sd.doubleValue() > getEpsilon()){
//				return false; // particle not converged...
//			}
//		}
//		return true;
//	}
	public boolean areAllConverged(Population pop){
		for (int i = 0; i < pop.size(); ++i){
			AbstractEAIndividual currentindy = pop.getEAIndividual(i);
			double value;
			if (stdDevHorizon == NichePSO.defaultFitStdDevHorizon) {
				value = ((Double)currentindy.getData(NichePSO.stdDevKey)).doubleValue();
			} else {
				Vector<Double> fitArch  = (Vector<Double>)currentindy.getData(NichePSO.fitArchiveKey);
				value = ParticleSubSwarmOptimization.stdDev(fitArch,stdDevHorizon);
			}
			if (value > getEpsilon()){
				return false; // particle not converged...
			}

		}
		return true;
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
		if (subswarm.getFitnessArchiveSize()<stdDevHorizon) EVAERROR.errorMsgOnce("Warning: halting window length " + stdDevHorizon + " too long for sub swarm template, which stores only " + subswarm.getFitnessArchiveSize() + " fitness values!");
		return (areAllConverged(subswarm.getPopulation()));
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
		return "threshold used to identify converged particles";
	}

	public int getStdDevHorizon() {
		return stdDevHorizon;
	}

	public void setStdDevHorizon(int stdDevHorizon) {
		this.stdDevHorizon = stdDevHorizon;
	}
	
	public String stdDevHorizonTipText() {
		return "The number of past fitness values to use for deactivation indication, note theres a maximum defined by the NichePSO fitness archiving."; 
	}
}
