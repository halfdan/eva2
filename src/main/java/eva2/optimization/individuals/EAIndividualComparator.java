package eva2.optimization.individuals;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator implementation which compares two individuals based on their fitness.
 * The default version calls isDominatingDebConstraints() of the AbstractEAIndividual
 * class and assigns -1 if first is dominant, 1 if second is dominant, 0 if the two ind.s
 * are not comparable.
 * By default, the dominance criterion is used on fitness vectors.
 * If a specific criterion i is set, the comparison is based only on the i-th
 * entry of the fitness vector.
 * As an alternative, a data key String may be set which is then used to request a data
 * object from the individuals. The objects are interpreted as fitness vectors (double[]) and
 * the comparison is based on those. This may be used to access alternative (e.g. older or
 * best-so-far fitness values) for individual comparison.
 *
 * @see AbstractEAIndividual#isDominatingFitness(double[], double[])
 */
@eva2.util.annotation.Description(value = "A comparator class for general EA individuals. Compares individuals based on their fitness in context of minimization.")
public class EAIndividualComparator implements Comparator<AbstractEAIndividual>, Serializable {
    // flag whether a data field should be used.
    private String indyDataKey = "";
    private int fitCriterion = -1;
    private boolean preferFeasible = true;

    /**
     * Comparator implementation which compares two individuals based on their fitness.
     * The default version calls compares based on dominance with priority of feasibility if there are constraints.
     * It assigns -1 if first is better, 1 if second is better, 0 if the two ind.s are not comparable.
     */
    public EAIndividualComparator() {
        this("", -1, true);
    }

    /**
     * Constructor with data key. A data field of the individuals may be used to retrieve
     * the double array used for comparison. Both individuals must have a data field with
     * the given key and return a double array of the same dimension. Constraints are
     * also regarded by default.
     * If indyDataKey is null, the default comparison is used.
     *
     * @param indyDataKey Field of the individual to use for comparison
     */
    public EAIndividualComparator(String indyDataKey) {
        this(indyDataKey, -1, true);
    }

    /**
     * Constructor for a specific fitness criterion in the multi-objective case.
     * For comparison, only the given fitness criterion is used if it is &gt;= 0.
     *
     * @param fitnessCriterion
     */
    public EAIndividualComparator(int fitnessCriterion) {
        this("", fitnessCriterion, true);
    }

    /**
     * Constructor for a specific fitness criterion in the multi-objective case.
     * For comparison, only the given fitness criterion is used if it is &gt;= 0.
     * If preferFeasible is true, feasible individuals will always be prefered.
     *
     * @param fitIndex
     * @param preferFeasible
     */
    public EAIndividualComparator(int fitIndex, boolean preferFeasible) {
        this("", fitIndex, preferFeasible);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EAIndividualComparator) {
            EAIndividualComparator o = (EAIndividualComparator) other;
            if ((indyDataKey.equals(o.indyDataKey)) || (indyDataKey != null && (indyDataKey.equals(o.indyDataKey)))) {
                if ((fitCriterion == o.fitCriterion) && (preferFeasible == o.preferFeasible)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return indyDataKey.hashCode() + 100 + fitCriterion + (preferFeasible ? 7 : 13);
    }

    /**
     * Generic constructor.
     *
     * @param indyDataKey Field of the individual to use for comparison
     * @param fitnessCriterion
     * @param preferFeasible
     * @see #EAIndividualComparator(int)
     * @see #EAIndividualComparator(String)
     */
    public EAIndividualComparator(String indyDataKey, int fitnessCriterion, boolean preferFeasible) {
        this.indyDataKey = indyDataKey;
        this.fitCriterion = fitnessCriterion;
        this.preferFeasible = preferFeasible;
    }

    public EAIndividualComparator(EAIndividualComparator other) {
        indyDataKey = other.indyDataKey;
        fitCriterion = other.fitCriterion;
        preferFeasible = other.preferFeasible;
    }

    @Override
    public Object clone() {
        return new EAIndividualComparator(this);
    }

    /**
     * Compare two individuals, return -1 if the first is dominant, 1 if the second is dominant, 0 if they
     * are not comparable.
     *
     * @param o1 the first AbstractEAIndividual to compare
     * @param o2 the second AbstractEAIndividual to compare
     * @return -1 if the first is dominant, 1 if the second is dominant, otherwise 0
     */
    @Override
    public int compare(AbstractEAIndividual o1, AbstractEAIndividual o2) {
        boolean o1domO2, o2domO1;

        if (preferFeasible) { // check constraint violation first?
            int constrViolComp = ((AbstractEAIndividual) o1).compareConstraintViolation((AbstractEAIndividual) o2);
            if (constrViolComp > 0) {
                return -1;
            } else if (constrViolComp < 0) {
                return 1;
            }
            // otherwise both do not violate, so regard fitness
        }
        if (indyDataKey != null && (indyDataKey.length() > 0)) { // check specific key
            double[] fit1 = (double[]) ((AbstractEAIndividual) o1).getData(indyDataKey);
            double[] fit2 = (double[]) ((AbstractEAIndividual) o2).getData(indyDataKey);
            if ((fit1 == null) || (fit2 == null)) {
                throw new RuntimeException("Unknown individual data key " + indyDataKey + ", unable to compare individuals (" + this.getClass().getSimpleName() + ")");
            }
            if (fitCriterion < 0) {
                o1domO2 = AbstractEAIndividual.isDominatingFitness(fit1, fit2);
                o2domO1 = AbstractEAIndividual.isDominatingFitness(fit2, fit1);
            } else {
                if (fit1[fitCriterion] == fit2[fitCriterion]) {
                    return 0;
                } else {
                    return (fit1[fitCriterion] < fit2[fitCriterion]) ? -1 : 1;
                }
            }
        } else {
            if (fitCriterion < 0) {
                o1domO2 = ((AbstractEAIndividual) o1).isDominating((AbstractEAIndividual) o2);
                o2domO1 = ((AbstractEAIndividual) o2).isDominating((AbstractEAIndividual) o1);
            } else {
                if (((AbstractEAIndividual) o1).getFitness()[fitCriterion] == ((AbstractEAIndividual) o2).getFitness()[fitCriterion]) {
                    return 0;
                }
                return (((AbstractEAIndividual) o1).getFitness()[fitCriterion] < ((AbstractEAIndividual) o2).getFitness()[fitCriterion]) ? -1 : 1;
            }
        }
        if (o1domO2 ^ o2domO1) {
            return (o1domO2 ? -1 : 1);
        } // they are comparable
        else {
            return 0;
        } // they are not comparable
    }

    public String getIndyDataKey() {
        return indyDataKey;
    }

    public void setIndyDataKey(String indyDataKey) {
        this.indyDataKey = indyDataKey;
    }

    public String indyDataKeyTipText() {
        return "A String can be given which retrievies individual properties based on which the comparison is performed.";
    }

    public int getFitCriterion() {
        return fitCriterion;
    }

    public void setFitCriterion(int fitCriterion) {
        this.fitCriterion = fitCriterion;
    }

    public String fitCriterionTipText() {
        return "If -1, dominance is used, otherwise the indexed fitness criterion (for multiobjective problems)";
    }

    public boolean isPreferFeasible() {
        return preferFeasible;
    }

    public void setPreferFeasible(boolean priorConst) {
        preferFeasible = priorConst;
    }

    public String preferFeasibleTipText() {
        return "Activate preference of feasible individuals in any comparison acc. to Deb's rules.";
    }

    public String getName() {
        return "IndividualComparator";
    }
}
