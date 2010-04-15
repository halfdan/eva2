package eva2.server.go.operators.distancemetric;

import java.io.Serializable;

import eva2.server.go.individuals.AbstractEAIndividual;

/**
 * The Euclidean metric just measures the Euclidean distance based on the default double representation
 * as given by AbstractEAIndividual.getDoublePosition(AbstractEAIndividual).
 * 
 * @see AbstractEAIndividual.getDoublePosition(AbstractEAIndividual)
 * @author mkron
 *
 */
public class EuclideanMetric implements InterfaceDistanceMetric, Serializable {

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

		dIndy1 = AbstractEAIndividual.getDoublePositionShallow(indy1);
		dIndy2 = AbstractEAIndividual.getDoublePositionShallow(indy2);

		for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
			result += Math.pow((dIndy1[i] - dIndy2[i]), 2);
		}
		return Math.sqrt(result);
	}
	
    /**
     * The euclidean distance normed by the given ranges lying between 0 and sqrt(n)
     * for n dimensions.
     * 
     * @param pos1
     * @param range1
     * @param pos2
     * @param range2
     * @return
     */
    public static double normedEuclideanDistance(double[] pos1, double[][] range1, double[] pos2, double[][] range2) {
    	double      tmpResult = 0, tmp=0;

    	for (int i = 0; (i < pos1.length) && (i < pos2.length); i++) {
    		tmp=((pos1[i] - range1[i][0])/(range1[i][1] - range1[i][0])) - ((pos2[i] - range2[i][0])/(range2[i][1] - range2[i][0]));
    		tmpResult += (tmp*tmp);
    	}
    	return Math.sqrt(tmpResult);
    }
    
    public static double squaredEuclideanDistance(double[] v1, double[] v2) {
        double      tmp, result = 0;
        for (int i = 0; (i < v1.length) && (i < v2.length); i++) {
        	tmp=v1[i] - v2[i];
        	result += (tmp*tmp);
        }
        return result;
    }
    
    public static double euclideanDistance(double[] v1, double[] v2) {
        double      result = 0, tmp=0;
        for (int i = 0; (i < v1.length) && (i < v2.length); i++) {
        	tmp = v1[i] - v2[i];
        	result += (tmp*tmp);
        }
        return Math.sqrt(result);
    }
	
	/** This method returns a global info string
	 * @return description
	 */
	public static String globalInfo() {
		return "The euclidean metric calculates euclidian distances for individuals which have a real valued interpretation.";
	}
	/** This method will return a naming String
	 * @return The name of the algorithm
	 */
	public String getName() {
		return "Euclidean Metric";
	}
}

