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
 * in the population as termination criterion. If the average (min./max.) distance
 * stagnates, the optimization stops.
 * May be computationally expensive.
 * 
 * @see Population.getPopulationMeasures()
 * @see PhenotypeMetric
 * @author mkron
 *
 */
public class DiversityTerminator extends PopulationMeasureTerminator implements InterfaceTerminator, Serializable {
	public enum DiversityCriterion {averageDistance, minimumDistance, maximumDistance};
//	private double distanceLimit = 0.001;
	private InterfaceDistanceMetric metric = new PhenotypeMetric();
	// leave the string order for this SelectedTag! (analogy to Population.getPopulationMeasures)
	private DiversityCriterion criterion = DiversityCriterion.averageDistance;
	
	public DiversityTerminator() {
		super();
	}
	
	public DiversityTerminator(DiversityCriterion divCrit, InterfaceDistanceMetric metric, double convergenceThreshold, int stagnationTime, StagnationTypeEnum stagType, ChangeTypeEnum changeType, DirectionTypeEnum dirType) {
		super(convergenceThreshold, stagnationTime, stagType, changeType, dirType);
		this.metric = metric;
		this.criterion = divCrit;
	}
	
	public static String globalInfo() {
		return "The diversity terminator uses the distance of individuals in the population as a termination criterion.";
	}

	/**
	 * @return the metric
	 */
	public InterfaceDistanceMetric getMetric() {
		return metric;
	}
	public void setMetric(InterfaceDistanceMetric metric) {
		this.metric = metric;
	}
	
	public String metricTipText() {
		return "Set the metric to be used to calculate individual distances.";
	}

	/**
	 * @return the criterion
	 */
	public DiversityCriterion getCriterion() {
		return criterion;
	}
	public void setCriterion(DiversityCriterion criterion) {
		this.criterion = criterion;
	}
	public String criterionTipText() {
		return "Define the distance criterion to check for in a population.";
	}

	@Override
	protected double calcInitialMeasure(PopulationInterface pop) {
		return calcPopulationMeasure(pop);
	}

	@Override
	protected double calcPopulationMeasure(PopulationInterface pop) {
		double[] measures = ((Population)pop).getPopulationMeasures(metric);
		int measureIndex = criterion.ordinal();
		return measures[measureIndex];
	}

	@Override
	protected String getMeasureName() {
		return "Population diversity ("+criterion+")";
	}
}
