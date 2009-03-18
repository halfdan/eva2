package eva2.server.go.operators.distancemetric;

import eva2.server.go.individuals.AbstractEAIndividual;

/**
 * The Euclidean metric just measures the Euclidean distance based on the default double representation
 * as given by AbstractEAIndividual.getDoublePosition(AbstractEAIndividual).
 * 
 * @see AbstractEAIndividual.getDoublePosition(AbstractEAIndividual)
 * @author mkron
 *
 */
public class EuclideanMetric implements InterfaceDistanceMetric {

	public Object clone() {
		return (Object) new EuclideanMetric(this);
	}

	public EuclideanMetric(EuclideanMetric a) {
	}

	public EuclideanMetric() {
	}

	public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
		double[]    dIndy1, dIndy2;
		double      result = 0;

		dIndy1 = AbstractEAIndividual.getDoublePosition(indy1);
		dIndy2 = AbstractEAIndividual.getDoublePosition(indy2);

		for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
			result += Math.pow((dIndy1[i] - dIndy2[i]), 2);
		}
		return Math.sqrt(result);
	}

	/** This method returns a global info string
	 * @return description
	 */
	public String globalInfo() {
		return "The euclidean metric calculates euclidian distances for individuals which have a real valued interpretation.";
	}
	/** This method will return a naming String
	 * @return The name of the algorithm
	 */
	public String getName() {
		return "Euclidean Metric";
	}
}

