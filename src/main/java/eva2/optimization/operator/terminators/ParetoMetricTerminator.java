package eva2.optimization.operator.terminators;

import eva2.gui.BeanInspector;
import eva2.optimization.operator.paretofrontmetrics.InterfaceParetoFrontMetric;
import eva2.optimization.operator.paretofrontmetrics.MetricS;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * Employ a pareto metric to determine convergence of a population. Requires to be run
 * with a AbstractMultiObjectiveOptimizationProblem instance since the metric depend
 * on the fitness range.
 * The metric may be employed on the current population or the current pareto front
 * maintained by the problem instance.
 */
@Description("Terminate if the pareto front of a multi-objective optimization process converges " +
        "with respect to a certain measure. Note that this only works with " +
        "AbstractMultiObjectiveOptimizationProblem instances.")
public class ParetoMetricTerminator extends PopulationMeasureTerminator implements Serializable {
    private InterfaceParetoFrontMetric pMetric = new MetricS();
    AbstractMultiObjectiveOptimizationProblem moProb = null;
    private boolean useCurrentPop = false;

    public ParetoMetricTerminator() {
        moProb = null;
    }

    public ParetoMetricTerminator(InterfaceParetoFrontMetric metric, boolean useCurrentPop, double convergenceThreshold, int stagnationTime, StagnationTypeEnum stagType, ChangeTypeEnum changeType, DirectionTypeEnum dirType) {
        super(convergenceThreshold, stagnationTime, stagType, changeType, dirType);
        this.pMetric = metric;
        this.useCurrentPop = useCurrentPop;
    }

    public ParetoMetricTerminator(ParetoMetricTerminator o) {
        super(o);
        this.pMetric = (InterfaceParetoFrontMetric) o.pMetric.clone();
        this.moProb = o.moProb;
        this.useCurrentPop = o.useCurrentPop;
    }

    @Override
    public void initialize(InterfaceOptimizationProblem prob) {
        super.initialize(prob);
        if (prob instanceof AbstractMultiObjectiveOptimizationProblem) {
            moProb = (AbstractMultiObjectiveOptimizationProblem) prob;
        } else {
            throw new IllegalArgumentException("Error, " + this.getClass() + " works only with problems inheriting from " + AbstractMultiObjectiveOptimizationProblem.class + "!");
        }
    }

    @Override
    protected double calculateInitialMeasure(PopulationInterface pop) {
        if (moProb == null) {
            return Double.MAX_VALUE;
        } else {
            if (isUseCurrentPop()) {
                return getParetoMetric().calculateMetricOn((Population) pop, moProb);
            } else {
                return getParetoMetric().calculateMetricOn(moProb.getLocalParetoFront(), moProb);
            }
        }
    }

    @Override
    protected double calculatePopulationMeasure(PopulationInterface pop) {
        return calculateInitialMeasure(pop);
    }

    @Override
    protected String getMeasureName() {
        String metricName = null;
        try {
            metricName = (String) BeanInspector.callIfAvailable(getParetoMetric(), "getName", null);
        } catch (ClassCastException e) {
            metricName = null;
        }

        if (metricName == null) {
            return "ParetoMetric";
        } else {
            return metricName;
        }
    }

    @Parameter(description = "The pareto metric to use")
    public void setParetoMetric(InterfaceParetoFrontMetric pMetric) {
        this.pMetric = pMetric;
    }

    public InterfaceParetoFrontMetric getParetoMetric() {
        return pMetric;
    }

    @Parameter(description = "If true, the current population is used, otherwise the pareto front of the multi-objective problem instance is used")
    public void setUseCurrentPop(boolean useCurrentPop) {
        this.useCurrentPop = useCurrentPop;
    }

    public boolean isUseCurrentPop() {
        return useCurrentPop;
    }
}
