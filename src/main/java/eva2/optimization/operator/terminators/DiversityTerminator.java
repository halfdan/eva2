package eva2.optimization.operator.terminators;

import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.util.annotation.Description;

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
        double[] measures = ((Population) pop).getPopulationMeasures(metric);
        int measureIndex = criterion.ordinal();
        return measures[measureIndex];
    }

    @Override
    protected String getMeasureName() {
        return "Population diversity (" + criterion + ")";
    }
}
