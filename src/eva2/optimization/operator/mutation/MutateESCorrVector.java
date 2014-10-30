package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;


/**
 * The correlated vector mutations stores a "velocity" vector for each individual,
 * updates the velocity by rotation and scaling, and then mutates the individual
 * by adding the velocity. This was used for a particle filter localization problem
 * and is less useful in general.
 * </p><p>
 * Rotation vectors are normal distributed with mean zero, scaling factors are
 * log-normally distributed around mean 1. This means that the averaged expected change
 * of the mutation vector is zero. The smaller the deviations, the higher the correlations
 * between successive mutation steps.
 */
@Description("The correlated vector mutation stores a specific mutation vector per individual.")
public class MutateESCorrVector implements InterfaceMutation, java.io.Serializable {
    protected double scalingDev = 0.05;
    protected double initialVelocity = 0.02;
    protected double lowerLimitStepSize = 0.0000001;
    protected double upperLimitStepSize = 0.5;
    protected double rotationDev = 15.;
    protected boolean checkConstraints = true;
    public static final String vectorKey = "MutateESCorrVectorVector";

    public MutateESCorrVector() {
    }

    public MutateESCorrVector(double scalingDev) {
        setScalingDev(scalingDev);
    }

    public MutateESCorrVector(double scalingDev, double initialVelocity) {
        setScalingDev(scalingDev);
        setInitialVelocity(initialVelocity);
    }

    public MutateESCorrVector(double scalingDev, double initialVelocity, double rotDev) {
        setScalingDev(scalingDev);
        setInitialVelocity(initialVelocity);
        setRotationDev(rotDev);
    }

    public MutateESCorrVector(MutateESCorrVector mutator) {
        this.scalingDev = mutator.scalingDev;
        this.initialVelocity = mutator.initialVelocity;
        this.lowerLimitStepSize = mutator.lowerLimitStepSize;
        this.rotationDev = mutator.rotationDev;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESCorrVector(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESCorrVector) {
            MutateESCorrVector mut = (MutateESCorrVector) mutator;
            if (this.scalingDev != mut.scalingDev) {
                return false;
            }
            if (this.initialVelocity != initialVelocity) {
                return false;
            }
            return this.lowerLimitStepSize == mut.lowerLimitStepSize;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        double[] initVelocity = calcInitialVel(initialVelocity, ((InterfaceESIndividual) individual).getDoubleRange());
        individual.putData(vectorKey, initVelocity);
    }

    /**
     * Create a random vector of length relative to the given range given by the velocity parameter.
     *
     * @param velocity
     * @param doubleRange
     * @return
     */
    private double[] calcInitialVel(double velocity, double[][] doubleRange) {
        double[] initVelocity = Mathematics.randomVector(doubleRange.length, 1.0);
        double nrm = Mathematics.norm(initVelocity);
        double[] shiftedRange = Mathematics.getAbsRange(doubleRange);
        // normalize to speed
        Mathematics.svMult(velocity / nrm, initVelocity, initVelocity);
        // and scale by ranges
        Mathematics.vvMultCw(shiftedRange, initVelocity, initVelocity);
        // System.out.println(Mathematics.getRelativeLength(initVelocity, doubleRange));
        return initVelocity;
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceESIndividual) {
            double[] genes = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();

            double[] vel = (double[]) individual.getData(vectorKey);

            // mutate the velocity vector and write it back
            if ((scalingDev > 0) || (rotationDev > 0)) {
                double rotateRad = rotationDev * (Math.PI / 360.) * RNG.gaussianDouble(1.);
                // rotate with a gaussian distribution of deviation rotationDeg
                Mathematics.rotateAllAxes(vel, rotateRad, false); // rotate
                double rScale = Math.exp(RNG.gaussianDouble(scalingDev));

                if ((lowerLimitStepSize > 0) || (upperLimitStepSize > 0)) {
                    double stepLen = Mathematics.norm(vel);
                    if (lowerLimitStepSize > 0) {
                        rScale = Math.max(rScale, lowerLimitStepSize / stepLen);
                    }
                    if (upperLimitStepSize > 0) {
                        rScale = Math.min(rScale, upperLimitStepSize / stepLen);
                    }
                }

                Mathematics.svMult(rScale, vel, vel); // mutate speed

                individual.putData(vectorKey, vel);
            }

            // add velocity to the individual
            Mathematics.vvAdd(genes, vel, genes);

            // check the range
            if (checkConstraints) {
                Mathematics.projectToRange(genes, range);
            }

            // write genotype back 
            ((InterfaceESIndividual) individual).setDGenotype(genes);

        }
    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        ArrayList<Double> tmpList = new ArrayList<>();
        if (indy1.getMutationOperator() instanceof MutateESCorrVector) {
            tmpList.add(((MutateESCorrVector) indy1.getMutationOperator()).scalingDev);
        }
        for (Object partner : partners) {
            if (((AbstractEAIndividual) partner).getMutationOperator() instanceof MutateESCorrVector) {
                tmpList.add(((MutateESCorrVector) ((AbstractEAIndividual) partner).getMutationOperator()).scalingDev);
            }
        }
        double[] list = new double[tmpList.size()];
        for (int i = 0; i < tmpList.size(); i++) {
            list[i] = tmpList.get(i);
        }
        if (list.length <= 1) {
            return;
        }
        // discrete mutation for step size
        this.scalingDev = list[RNG.randomInt(0, list.length - 1)];
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "ES global mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "ES correlated vector mutation";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setScalingDev(double d) {
        this.scalingDev = d;
    }

    public double getScalingDev() {
        return this.scalingDev;
    }

    public String scalingDevTipText() {
        return "Choose the devation of lognormal vector scaling.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.lowerLimitStepSize;
    }

    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step.";
    }

    public double getRotationDev() {
        return rotationDev;
    }

    public void setRotationDev(double rotationDeg) {
        this.rotationDev = rotationDeg;
    }

    public String rotationDevTipText() {
        return "Std deviation of the rotation angle distribution";
    }

    public double getInitialVelocity() {
        return initialVelocity;
    }

    public void setInitialVelocity(double velocity) {
        initialVelocity = velocity;
    }

    public double getUpperLimitStepSize() {
        return upperLimitStepSize;
    }

    public void setUpperLimitStepSize(double upperLimitStepSize) {
        this.upperLimitStepSize = upperLimitStepSize;
    }

    public String upperLimitStepSizeTipText() {
        return "Set the upper limit for the mutation step.";
    }
}
