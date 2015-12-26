package eva2.optimization.operator.terminators;

import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * The diversity terminator uses the distance of individuals
 * in the population as termination criterion. If the average (min./max.) distance
 * stagnates, the optimization stops.
 * May be computationally expensive.
 *
 * @author mkron
 * @see Population#getPopulationMeasures()
 * @see PhenotypeMetric
 */
@Description("The diversity terminator uses the distance of individuals in the population as a termination criterion.")
public class DiversityTerminator extends PopulationMeasureTerminator implements InterfaceTerminator, Serializable {
    public enum DiversityCriterion {averageDistance, minimumDistance, maximumDistance}

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

    /**
     * @return the metric
     */
    @Parameter(description = "Set the metric to be used to calculate individual distances.")
    public InterfaceDistanceMetric getMetric() {
        return metric;
    }

    public void setMetric(InterfaceDistanceMetric metric) {
        this.metric = metric;
    }

    /**
     * @return the criterion
     */
    @Parameter(description = "Define the distance criterion to check for in a population.")
    public DiversityCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(DiversityCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    protected double calculateInitialMeasure(PopulationInterface pop) {
        return calculatePopulationMeasure(pop);
    }

    @Override
    protected double calculatePopulationMeasure(PopulationInterface pop) {
        double[] measures = ((Population) pop).getPopulationMeasures(metric);
        int measureIndex = criterion.ordinal();
        return measures[measureIndex];
    }

    @Override
    protected String getMeasureName() {
        return "Population diversity (" + criterion + ")";
    }
}
