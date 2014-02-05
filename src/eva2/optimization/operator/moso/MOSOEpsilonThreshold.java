package eva2.optimization.operator.moso;

import eva2.gui.PropertyEpsilonThreshold;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 *
 */
public class MOSOEpsilonThreshold implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyEpsilonThreshold epsilonThreshold = null;

    public MOSOEpsilonThreshold() {
        this.epsilonThreshold = new PropertyEpsilonThreshold();
        this.epsilonThreshold.optimizeObjective = 0;
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 0.0;
        }
        this.epsilonThreshold.targetValue = tmpD;
        tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 1.0;
        }
        this.epsilonThreshold.punishment = tmpD;
    }

    public MOSOEpsilonThreshold(MOSOEpsilonThreshold b) {
        if (b.epsilonThreshold != null) {
            this.epsilonThreshold = (PropertyEpsilonThreshold) b.epsilonThreshold.clone();
        }
    }

    @Override
    public Object clone() {
        return (Object) new MOSOEpsilonThreshold(this);
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
            this.convertSingleIndividual((AbstractEAIndividual) pop.get(i));
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
        for (int i = 0; i < tmpFit.length; i++) {
            if (new Double(tmpFit[i]).isNaN()) {
                System.out.println("Fitness is NaN");
            }
            if (new Double(tmpFit[i]).isInfinite()) {
                System.out.println("Fitness is Infinite");
            }
        }
        resultFit[0] = tmpFit[this.epsilonThreshold.optimizeObjective];

//        System.out.println("Optimize: " + this.epsilonThreshold.optimizeObjective);
//        for (int i = 0; i < tmpFit.length; i++) {
//            System.out.println("Target: " + this.epsilonThreshold.targetValue[i] + " Punish: " + this.epsilonThreshold.punishment[i]);
//        }

        for (int i = 0; i < this.epsilonThreshold.punishment.length; i++) {
            if (i != this.epsilonThreshold.optimizeObjective) {
                resultFit[0] += this.epsilonThreshold.punishment[i] * Math.max(0, tmpFit[i] - this.epsilonThreshold.targetValue[i]);
            }
        }
        tmpFit = (double[]) indy.getData("MOFitness");
        for (int i = 0; i < tmpFit.length; i++) {
            if (new Double(tmpFit[i]).isNaN()) {
                System.out.println("-Fitness is NaN");
            }
            if (new Double(tmpFit[i]).isInfinite()) {
                System.out.println("-Fitness is Infinite");
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
        double[] newPunish = new double[dim];
        double[] newTarget = new double[dim];

        for (int i = 0; i < newPunish.length; i++) {
            newPunish[i] = 1;
            newTarget[i] = 0;
        }
        for (int i = 0; (i < this.epsilonThreshold.punishment.length) && (i < newTarget.length); i++) {
            newPunish[i] = this.epsilonThreshold.punishment[i];
            newTarget[i] = this.epsilonThreshold.targetValue[i];
        }
        if (this.epsilonThreshold.optimizeObjective >= dim) {
            this.epsilonThreshold.optimizeObjective = dim - 1;
        }

        this.epsilonThreshold.punishment = newPunish;
        this.epsilonThreshold.targetValue = newTarget;
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


/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Epsilon Threshold";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This method uses n-1 objected as soft constraints.";
    }

    /**
     * This method allows you to choose the EpsilonThreshold
     *
     * @param weights The Epsilon Threshhold for the fitness sum.
     */
    public void setEpsilonThreshhold(PropertyEpsilonThreshold weights) {
        this.epsilonThreshold = weights;
    }

    public PropertyEpsilonThreshold getEpsilonThreshhold() {
        return this.epsilonThreshold;
    }

    public String epsilonThreshholdTipText() {
        return "Choose the epsilon thresholds for the fitness sum.";
    }

}
