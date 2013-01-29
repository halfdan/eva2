package eva2.server.go.operators.distancemetric;

import java.io.Serializable;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.strategies.ParticleSwarmOptimization;
import eva2.tools.EVAERROR;

/**
 * Define a metric on data stored within individuals, such as the personal best position
 * in PSO. The metric tries to set the stored data as double position to an indy clone.
 * On these clones, the base metric is applied.
 * 
 * @author mkron
 *
 */
public class IndividualDataMetric implements InterfaceDistanceMetric, Serializable {
	private String dataKey = ParticleSwarmOptimization.partBestPosKey;
//	private boolean normedDistance = true; // flag whether to use normed distances (for InterfaceDataTypeDouble)
	private InterfaceDistanceMetric baseMetric = new EuclideanMetric(true);
	
    public IndividualDataMetric() {}    
    
    public IndividualDataMetric(String key) {
		dataKey = key;
	}
    
    public IndividualDataMetric(String key, InterfaceDistanceMetric bMetric) {
		dataKey = key;
		setBaseMetric(bMetric);
	}
    
    public IndividualDataMetric(IndividualDataMetric o) {
    	this.dataKey = o.dataKey;
//    	this.normedDistance = o.normedDistance;
    	this.setBaseMetric(o.getBaseMetric());
	}

	/** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    @Override
    public Object clone() {
    	return new IndividualDataMetric(this);
    }
    
    @Override
	public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
		if (dataKey==null) throw new RuntimeException("Error, no data key defined in " + this.getClass().getName() + "::distance()");
		else {
			Object data1 = indy1.getData(dataKey);
			Object data2 = indy2.getData(dataKey);
			if (data1 instanceof double[] && (data2 instanceof double[])) {
				AbstractEAIndividual dataIndy1 = (AbstractEAIndividual) indy1.clone();
				AbstractEAIndividual.setDoublePosition(dataIndy1, (double[]) data1);
				AbstractEAIndividual dataIndy2 = (AbstractEAIndividual) indy2.clone();
				AbstractEAIndividual.setDoublePosition(dataIndy2, (double[]) data2);
				return getBaseMetric().distance(dataIndy1, dataIndy2);
//				if (normedDistance) {
//					double[][] range1 = ((InterfaceDataTypeDouble)indy1).getDoubleRange();
//					double[][] range2 = ((InterfaceDataTypeDouble)indy2).getDoubleRange();
//					return EuclideanMetric.normedEuclideanDistance((double[])data1, range1, (double[])data2, range2);
//				} else return EuclideanMetric.euclideanDistance((double[])data1, (double[])data2);
			} else {
				EVAERROR.errorMsgOnce("Error, invalid key data, double array required by " + this.getClass().getName());
				EVAERROR.errorMsgOnce("Using PhenotypeMetric as Backup...");
				return (new PhenotypeMetric().distance(indy1, indy2));
//				throw new RuntimeException("Invalid data key, double array required by " + this.getClass().getName());
			}
		}
	}

	public String dataKeyTipText() {
		return "Name of the data key to use to retrieve individual data (double[] for now).";
	}
	public String getDataKey() {
		return dataKey;
	}
	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String normedDistanceTipText() {
		return "Flag whether to use euclidean distance directly or normed by the double range."; 
	}
//	public boolean isNormedDistance() {
//		return normedDistance;
//	}
//	public void setNormedDistance(boolean normedDistance) {
//		this.normedDistance = normedDistance;
//	}
	
	public static String globalInfo() {
		return "Uses individual object data (so far only double[]) to calculate the distance.";
	}

	public void setBaseMetric(InterfaceDistanceMetric baseMetric) {
		this.baseMetric = baseMetric;
	}
	public InterfaceDistanceMetric getBaseMetric() {
		return baseMetric;
	}
	public String baseMetricTipText() {
		return "The metric to be used on the stored data objects.";
	}
}
