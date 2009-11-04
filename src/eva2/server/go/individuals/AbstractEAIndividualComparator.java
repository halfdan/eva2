package eva2.server.go.individuals;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator implementation which compares two individuals based on their fitness.
 * The default version calls isDominatingDebConstraints() of the AbstractEAIndividual
 * class and assigns -1 if first is dominant, 1 if second is dominant, 0 if the two ind.s
 * are not comparable.
 * By default, the dominance criterion is used on fitness vectors.
 * If a specific criterion i is set, the comparison is based only on the i-th 
 * entry of the fitness vector.
 * As an alternative, a data key String may be set which is then used to request a data
 * object from the individuals. The objects are interpreted as fitness vectors (double[]) and
 * the comparison is based on those. This may be used to access alternative (e.g. older or
 * best-so-far fitness values) for individual comparison.
 *
 * @see #AbstractEAIndividual().isDominatingFitness(double[], double[])
 * @author mkron
 *
 */
public class AbstractEAIndividualComparator implements Comparator<Object>, Serializable {

	// flag whether a data field should be used.
	String indyDataKey = "";
	int fitCriterion = -1;
	
	/**
	 * Comparator implementation which compares two individuals based on their fitness.
	 * The default version calls isDominatingDebConstraints() of the AbstractEAIndividual
	 * class and assigns -1 if first is dominant, 1 if second is dominant, 0 if the two ind.s
	 * are not comparable.
	 *
	 */
	public AbstractEAIndividualComparator() {
		this("", -1);
	}
	
	/**
	 * Constructor with data key. A data field of the individuals may be used to retrieve
	 * the double array used for comparison. Both individuals must have a data field with
	 * the given key and return a double array of the same dimension. No constraints are 
	 * regarded for this comparison.
	 * If indyDataKey is null, the default comparison is used.
	 * 
	 * @param indyDataKey
	 */
	public AbstractEAIndividualComparator(String indyDataKey) {
		this(indyDataKey, -1);		
	}
	
	/**
	 * Constructor for a specific fitness criterion in the multiobjective case. 
	 * For comparison, only the given fitness criterion is used if it is >= 0.
	 * 
	 * @param fitnessCriterion
	 */
	public AbstractEAIndividualComparator(int fitnessCriterion) {
		this("", fitnessCriterion);
	}
	
	/**
	 * Generic constructor.
	 * 
	 * @see #AbstractEAIndividualComparator(int)
	 * @see #AbstractEAIndividualComparator(String)
	 * @param indyDataKey
	 * @param fitnessCriterion
	 */
	public AbstractEAIndividualComparator(String indDataKey, int fitnessCriterion) {
		indyDataKey = indDataKey;
		fitCriterion = fitnessCriterion;	
	}

	public AbstractEAIndividualComparator(AbstractEAIndividualComparator other) {
		indyDataKey = other.indyDataKey;
	}
	
	public Object clone() {
		return new AbstractEAIndividualComparator(this);
	}
	
	/**
	 * Compare two individuals, return -1 if the first is dominant, 1 if the second is dominant, 0 if they
	 * are not comparable.
	 * 
	 * @param o1 the first AbstractEAIndividual to compare
	 * @param o2 the second AbstractEAIndividual to compare
	 * @return -1 if the first is dominant, 1 if the second is dominant, otherwise 0
	 */
	public int compare(Object o1, Object o2) {
		boolean o1domO2, o2domO1;
		
		if (indyDataKey != null && (indyDataKey.length()>0)) {
			double[] fit1 = (double[])((AbstractEAIndividual)o1).getData(indyDataKey);
			double[] fit2 = (double[])((AbstractEAIndividual)o2).getData(indyDataKey);
			if (fitCriterion < 0) {
				o1domO2 = AbstractEAIndividual.isDominatingFitness(fit1, fit2);
				o2domO1 = AbstractEAIndividual.isDominatingFitness(fit2, fit1);
			} else {
				if (fit1[fitCriterion] == fit2[fitCriterion]) return 0;
				else return (fit1[fitCriterion] < fit2[fitCriterion]) ? -1 : 1;
			}
		} else {
			if (fitCriterion < 0) {
				o1domO2 = ((AbstractEAIndividual) o1).isDominatingDebConstraints((AbstractEAIndividual) o2);
				o2domO1 = ((AbstractEAIndividual) o2).isDominatingDebConstraints((AbstractEAIndividual) o1);
			} else {
				if (((AbstractEAIndividual) o1).getFitness()[fitCriterion] == ((AbstractEAIndividual) o2).getFitness()[fitCriterion]) return 0;
				return (((AbstractEAIndividual) o1).getFitness()[fitCriterion] < ((AbstractEAIndividual) o2).getFitness()[fitCriterion]) ? -1 : 1;
			}
		}
		if (o1domO2 ^ o2domO1) return (o1domO2 ? -1 : 1);
		else return 0; // these are not comparable
	}

	public String getIndyDataKey() {
		return indyDataKey;
	}
	public void setIndyDataKey(String indyDataKey) {
		this.indyDataKey = indyDataKey;
	}
	public String indyDataKeyTipText() {
		return "A String can be given which retrievies individual properties based on which the comparison is performed.";
	}

	public int getFitCriterion() {
		return fitCriterion;
	}
	public void setFitCriterion(int fitCriterion) {
		this.fitCriterion = fitCriterion;
	}
	public String fitCriterionTipText() {
		return "If -1, dominance is used, otherwise the indexed fitness criterion (for multiobjective problems)"; 
	}
	
	public String globalInfo() {
		return "A comparator class for general EA individuals. Compares individuals based on their fitness in context of minimization.";
	}
	public String getName() {
		return "IndividualComparator";
	}
}
