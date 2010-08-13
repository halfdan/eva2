package eva2.server.go.individuals;

import java.io.Serializable;
import java.util.Comparator;

import eva2.tools.EVAERROR;

/**
 * Compare two individuals based on a linear combination of the fitness values.
 * 
 * @author mkron
 *
 */
public class IndividualWeightedFitnessComparator implements Comparator<Object>, Serializable {
	private double [] fitWeights = null;
	
	public IndividualWeightedFitnessComparator(double[] weights) {
		setFitWeights(weights);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IndividualWeightedFitnessComparator) {
			IndividualWeightedFitnessComparator o = (IndividualWeightedFitnessComparator)obj;
			if (fitWeights==null && (o.fitWeights==null)) return true;
			if (fitWeights==null || o.fitWeights==null) return false;
			// now both are non null:
			if (fitWeights.length==o.fitWeights.length) {
				for (int i=0; i<fitWeights.length; i++) {
					if (fitWeights[i]!=o.fitWeights[i]) return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (fitWeights==null) return super.hashCode();
		int code=0;
		for (int i=0; i<fitWeights.length; i++) {
			code+=(int)(fitWeights[i]*10000)%(10000*(i+1));
		}
		return code;
	}

	@Override
	public int compare(Object o1, Object o2) {
		double[] f1 = ((AbstractEAIndividual) o1).getFitness();
		double[] f2 = ((AbstractEAIndividual) o2).getFitness();
		
		double score1 = calcScore(f1);
		double score2 = calcScore(f2);
		
		if (score1 < score2) return  -1;
		else if (score1 > score2) return 1;
		else return 0;
	}

	private double calcScore(double[] f) {
		if (f==null || fitWeights==null) throw new RuntimeException("Error, missing information in " + this.getClass());
		if (f.length!=fitWeights.length) {
			if (f.length<fitWeights.length) EVAERROR.errorMsgOnce("Warning, fitness vector has less dimensions than the weights... some weights are ignored, in " + this.getClass());
			else EVAERROR.errorMsgOnce("Warning, fitness vector has more dimensions than the weights... some fitness values are ignored, in " + this.getClass());
		}
		double s = 0;
		for (int i=0; i<Math.min(f.length, fitWeights.length); i++) {
			s += f[i]*fitWeights[i];
		}
		return s;
	}
	
	public double calcScore(AbstractEAIndividual indy) {
		double[] f = indy.getFitness();
		return calcScore(f);
	}

	public void setAllWeights(int dim, double v) {
		fitWeights = new double[dim];
		for (int i = 0; i < fitWeights.length; i++) {
			fitWeights[i]=v;
		}
	}
	
	public void setFitWeights(double [] fitWeights) {
		this.fitWeights = fitWeights;
	}
	public double [] getFitWeights() {
		return fitWeights;
	}
	public String fitWeightsTipText() {
		return "Weights of the fitness values in the linear combination";
	}
	
//	public static void main(String[] args) {
//		TF1Problem prob = new TF1Problem();
//		Population pop = new Population(10);
//		prob.initPopulation(pop);
//		prob.evaluate(pop);
//		System.out.println(pop.getStringRepresentation());
//		System.out.println("***");
//		IndividualWeightedFitnessComparator wfComp = new IndividualWeightedFitnessComparator(new double[]{0.5,0.5});
//		System.out.println("***"); System.out.println(pop.getSortedPop(wfComp).getStringRepresentation());
//		wfComp.setFitWeights(new double[] {0.1, 0.9});
//		System.out.println("***"); System.out.println(pop.getSortedPop(wfComp).getStringRepresentation());
//		wfComp.setFitWeights(new double[] {0.9, 0.1});
//		System.out.println("***"); System.out.println(pop.getSortedPop(wfComp).getStringRepresentation());
//	}
}
