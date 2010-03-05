package eva2.server.go.operators.terminators;

import java.io.Serializable;

import eva2.server.go.InterfaceTerminator;
import eva2.server.go.PopulationInterface;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.SelectedTag;

/**
 * The diversity terminator uses the distance of individuals
 * in the population as termination criterion. If the average distance
 * sinks below the given limit, the optimization stops.
 * May be computationally expensive.
 * 
 * @see Population.getPopulationMeasures()
 * @see PhenotypeMetric
 * @author mkron
 *
 */
public class DiversityTerminator implements InterfaceTerminator, Serializable {
	private double distanceLimit = 0.001;
	private InterfaceDistanceMetric metric = new PhenotypeMetric();
	private String msg = "";
	// leave the string order for this SelectedTag! (analogy to Population.getPopulationMeasures)
	private SelectedTag criterion = new SelectedTag("Average distance", "Minimum distance", "Maximum distance");
	
	public DiversityTerminator() {}
	
	/**
	 * The default uses Phenotype metric and average distance as criterion.
	 * 
	 * @param limit
	 */
	public DiversityTerminator(double limit) {
		distanceLimit = limit;
		metric = new PhenotypeMetric();
		criterion.setSelectedTag(0);
	}
	
	/**
	 * Create a special DiversityTerminator defining distance limit, metric and distance
	 * criterion to use (average, minimum or maximum distance). 
	 * 
	 * @param limit	the distance limit of individuals below which termination is triggered
	 * @param metric the metric to be used to calculate individual distances
	 * @param criterionID 0 for average distance, 1 for minimum distance and 2 for maximum distance
	 */
	public DiversityTerminator(double limit, InterfaceDistanceMetric metric, int criterionID) {
		distanceLimit = limit;
		this.metric = metric;
		criterion.setSelectedTag(criterionID);
	}
	
	public void init(InterfaceOptimizationProblem prob) {
		msg = "Not terminated.";
	}

	public static String globalInfo() {
		return "The diversity terminator uses the distance of individuals in the population as a termination criterion.";
	}
	
	/**
	 * Returns true if the average phenotypic distance within the given
	 * population is below the limit set in the terminator instance.
	 * 
	 * @return true if the population is seen as converged due to low average phenotypic distance, else false
	 */
	public boolean isTerminated(PopulationInterface pop) {
		double[] measures = ((Population)pop).getPopulationMeasures(metric);
		int measureIndex = criterion.getSelectedTagID();
		if (measures[measureIndex] < distanceLimit) {
			msg = "Average individual distance below " + distanceLimit;
			return true;
		} else return false;
	}
	
	public boolean isTerminated(InterfaceSolutionSet sols) {
		return isTerminated(sols.getCurrentPopulation());
	}

	public String lastTerminationMessage() {
		return msg;
	}

	/**
	 * @return the avgDistanceLimit
	 */
	public double getDistanceLimit() {
		return distanceLimit;
	}

	/**
	 * @param avgDistanceLimit the avgDistanceLimit to set
	 */
	public void setDistanceLimit(double avgDistanceLimit) {
		this.distanceLimit = avgDistanceLimit;
	}

	public String distanceLimitTipText() {
		return "Set the distance limit of individuals below which termination is triggered.";
	}

	/**
	 * @return the metric
	 */
	public InterfaceDistanceMetric getMetric() {
		return metric;
	}

	/**
	 * @param metric the metric to set
	 */
	public void setMetric(InterfaceDistanceMetric metric) {
		this.metric = metric;
	}
	
	public String metricTipText() {
		return "Set the metric to be used to calculate individual distances.";
	}

	/**
	 * @return the criterion
	 */
	public SelectedTag getCriterion() {
		return criterion;
	}

	/**
	 * @param criterion the criterion to set
	 */
	public void setCriterion(SelectedTag criterion) {
		this.criterion = criterion;
	}
	
	public String criterionTipText() {
		return "Define the distance criterion to check for in a population.";
	}
}
