package eva2.server.go.tools;

import java.util.Comparator;

import eva2.server.go.individuals.AbstractEAIndividual;

/**
 * Comparator implementation which compares two double arrays.
 * It assigns -1 if first is pareto dominant (smaller), 1 if second is pareto dominant (larger), 0 if the two ind.s
 * are not comparable.
 * If a criterion>=0 is specified, only the thereby indexed entry of any array
 * will be used for comparison (single criterion case).
 *
 */
public class DoubleArrayComparator implements Comparator<Object> {
	int fitCriterion = -1;
	
	public DoubleArrayComparator() {
		this(-1);
	}

	/**
	 * Generic constructor.
	 * 
	 * @param fitnessCriterion
	 */
	public DoubleArrayComparator(int criterion) {
		fitCriterion = criterion;	
	}


	public DoubleArrayComparator(DoubleArrayComparator o) {
		fitCriterion = o.fitCriterion;
	}

    @Override
	public Object clone() {
		return new DoubleArrayComparator(this);
	}
	
	/**
	 * Compare two double arrays based on dominance, return -1 if the first is dominant, 1 if the second is dominant, 0 if they
	 * are not comparable.
	 * 
	 * @see #AbstractEAIndividual.isDominatingFitness(double[], double[])
	 * @param o1 the first double[] to compare
	 * @param o2 the second double[] to compare
	 * @return -1 if the first is dominant, 1 if the second is dominant, otherwise 0
	 */
    @Override
	public int compare(Object o1, Object o2) {
		boolean o1domO2, o2domO1;
		
		double[] fit1 = (double[])o1;
		double[] fit2 = (double[])o2;
		if (fitCriterion < 0) {
			o1domO2 = AbstractEAIndividual.isDominatingFitness(fit1, fit2);
			o2domO1 = AbstractEAIndividual.isDominatingFitness(fit2, fit1);
		} else {
			if (fit1[fitCriterion] == fit2[fitCriterion]) return 0;
			else return (fit1[fitCriterion] < fit2[fitCriterion]) ? -1 : 1;
		}
		if (o1domO2 ^ o2domO1) return (o1domO2 ? -1 : 1);
		else return 0; // these are not comparable
	}
}
