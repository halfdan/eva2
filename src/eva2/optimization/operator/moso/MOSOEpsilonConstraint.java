package eva2.optimization.operator.moso;

import eva2.gui.PropertyEpsilonConstraint;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This method uses n-1 objected as hard constraints.")
public class MOSOEpsilonConstraint implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyEpsilonConstraint epsilonConstraint = null;

    public MOSOEpsilonConstraint() {
        this.epsilonConstraint = new PropertyEpsilonConstraint();
        this.epsilonConstraint.optimizeObjective = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 0.0;
        }
        this.epsilonConstraint.targetValue = tmpD;
    }

    public MOSOEpsilonConstraint(MOSOEpsilonConstraint b) {
        if (b.epsilonConstraint != null) {
            this.epsilonConstraint = (PropertyEpsilonConstraint) b.epsilonConstraint.clone();
        }
    }

    @Override
    public Object clone() {
        return new MOSOEpsilonConstraint(this);
    }

    /**
     * This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     *
     * @param pop The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
            this.convertSingleIndividual(pop.get(i));
        }
    }

    /**
     * This method processes a single individual
     *
     * @param indy The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[] resultFit = new double[1];
        double[] tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        resultFit[0] = tmpFit[this.epsilonConstraint.optimizeObjective];
        for (int i = 0; i < this.epsilonConstraint.targetValue.length; i++) {
            if (i != this.epsilonConstraint.optimizeObjective) {
                indy.addConstraintViolation(Math.max(0, tmpFit[i] - this.epsilonConstraint.targetValue[i]));
            }
        }
        indy.setFitness(resultFit);
    }

    /**
     * This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     *
     * @param dim Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        double[] newTarget = new double[dim];

        for (int i = 0; i < newTarget.length; i++) {
            newTarget[i] = 0;
        }
        for (int i = 0; (i < this.epsilonConstraint.targetValue.length) && (i < newTarget.length); i++) {
            newTarget[i] = this.epsilonConstraint.targetValue[i];
        }
        if (this.epsilonConstraint.optimizeObjective >= dim) {
            this.epsilonConstraint.optimizeObjective = dim - 1;
        }

        this.epsilonConstraint.targetValue = newTarget;
    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        return this.getName() + "\n";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Epsilon Constraint";
    }

    /**
     * This method allows you to choose the EpsilonThreshold
     *
     * @param weights The Epsilon Threshhold for the fitness sum.
     */
    public void setEpsilonThreshhold(PropertyEpsilonConstraint weights) {
        this.epsilonConstraint = weights;
    }

    public PropertyEpsilonConstraint getEpsilonThreshhold() {
        return this.epsilonConstraint;
    }

    public String epsilonThreshholdTipText() {
        return "Choose the epsilon constraints for the fitness sum.";
    }

}