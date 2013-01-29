package eva2.server.go.individuals;

import java.io.Serializable;
import java.util.Comparator;

import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;

/**
 * Compare two AbstractEAIndividuals by their distance to a reference individual.
 * Usable to sort by a distance.
 **/
public class IndividualDistanceComparator implements Comparator<Object>, Serializable {

	private AbstractEAIndividual refIndy=null;
	private InterfaceDistanceMetric distMetric = null;
	private boolean closerMeansLess = true;
	
	/**
	 * Constructor with a reference individual to which the distance is measured and the corresponding desired metric.
	 * Using closerIsLess it can be switched between increasing and decreasing order. 
	 * 
	 * @param referenceIndy
	 * @param metric
	 * @param closerIsLess
	 */
	public IndividualDistanceComparator(AbstractEAIndividual referenceIndy, InterfaceDistanceMetric metric, boolean closerIsLess) {
		refIndy = referenceIndy;
		distMetric = metric;
		closerMeansLess = closerIsLess;
	}

    @Override
	public int compare(Object o1, Object o2) {
		double d1 = distMetric.distance((AbstractEAIndividual)o1, refIndy);
		double d2 = distMetric.distance((AbstractEAIndividual)o2, refIndy);

		if (d1==d2) return 0;
		if (closerMeansLess) return ((d1<d2) ? -1 : 1);
		else return ((d1<d2) ? 1 : -1);
	}

}
