package eva2.optimization.operator.mutation;

import eva2.gui.BeanInspector;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

import java.util.ArrayList;


/**
 * The correlated vector mutations stores a "velocity" vector for each individual,
 * updates the velocity by rotation and scaling, and then mutates the individual
 * by adding the velocity. This was used for a particle filter localization problem
 * and is less useful in general.
 * <p/>
 * Rotation vectors are normal distributed with mean zero, scaling factors are
 * log-normally distributed around mean 1. This means that the averaged expected change
 * of the mutation vector is zero. The smaller the deviations, the higher the correlations
 * between successive mutation steps.
 */
public class MutateESCorrVector implements InterfaceMutation, java.io.Serializable {
    protected double m_scalingDev = 0.05;
    protected double m_initialVelocity = 0.02;
    protected double m_LowerLimitStepSize = 0.0000001;
    protected double m_UpperLimitStepSize = 0.5;
    protected double m_rotationDev = 15.;
    protected boolean m_checkConstraints = true;
    public static final String vectorKey = "MutateESCorrVectorVector";
    public static final boolean TRACE = false;

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
        this.m_scalingDev = mutator.m_scalingDev;
        this.m_initialVelocity = mutator.m_initialVelocity;
        this.m_LowerLimitStepSize = mutator.m_LowerLimitStepSize;
        this.m_rotationDev = mutator.m_rotationDev;
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
            if (this.m_scalingDev != mut.m_scalingDev) {
                return false;
            }
            if (this.m_initialVelocity != m_initialVelocity) {
                return false;
            }
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to init the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        double[] initVelocity = calcInitialVel(m_initialVelocity, ((InterfaceESIndividual) individual).getDoubleRange());
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
//        if (TRACE) System.out.println("Before Mutate: " + AbstractEAIndividual.getDefaultDataString(individual));
        if (individual instanceof InterfaceESIndividual) {
            double[] genes = ((InterfaceESIndividual) individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual) individual).getDoubleRange();

            double[] vel = (double[]) individual.getData(vectorKey);

            // mutate the velocity vector and write it back
            if ((m_scalingDev > 0) || (m_rotationDev > 0)) {
//            	for (int i = 0; i < vel.length; i++) {
//            		vel[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.mutationStepSize);
//            	}
                double rotateRad = m_rotationDev * (Math.PI / 360.) * RNG.gaussianDouble(1.);
                // rotate with a gaussian distribution of deviation rotationDeg
                Mathematics.rotateAllAxes(vel, rotateRad, false); // rotate
                double rScale = Math.exp(RNG.gaussianDouble(m_scalingDev));

                if ((m_LowerLimitStepSize > 0) || (m_UpperLimitStepSize > 0)) {
                    double stepLen = Mathematics.norm(vel);
                    if (m_LowerLimitStepSize > 0) {
                        rScale = Math.max(rScale, m_LowerLimitStepSize / stepLen);
                    }
                    if (m_UpperLimitStepSize > 0) {
                        rScale = Math.min(rScale, m_UpperLimitStepSize / stepLen);
                    }
                }

                Mathematics.svMult(rScale, vel, vel); // mutate speed

                individual.putData(vectorKey, vel);
                if (TRACE) {
                    System.out.println("rotated by " + rotateRad + ", scaled by " + rScale);
                }
                if (TRACE) {
                    System.out.println("-- dir is  " + BeanInspector.toString(vel));
                }
            }

            // add velocity to the individual
            Mathematics.vvAdd(genes, vel, genes);

            // check the range
            if (m_checkConstraints) {
                Mathematics.projectToRange(genes, range);
            }

            // write genotype back 
            ((InterfaceESIndividual) individual).SetDGenotype(genes);

        }
//        if (TRACE) System.out.println("After Mutate:  " + AbstractEAIndividual.getDefaultDataString(individual));
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
        ArrayList<Double> tmpList = new ArrayList<Double>();
        if (indy1.getMutationOperator() instanceof MutateESCorrVector) {
            tmpList.add(new Double(((MutateESCorrVector) indy1.getMutationOperator()).m_scalingDev));
        }
        for (int i = 0; i < partners.size(); i++) {
            if (((AbstractEAIndividual) partners.get(i)).getMutationOperator() instanceof MutateESCorrVector) {
                tmpList.add(new Double(((MutateESCorrVector) ((AbstractEAIndividual) partners.get(i)).getMutationOperator()).m_scalingDev));
            }
        }
        double[] list = new double[tmpList.size()];
        for (int i = 0; i < tmpList.size(); i++) {
            list[i] = ((Double) tmpList.get(i)).doubleValue();
        }
        if (list.length <= 1) {
            return;
        }
        // discreete mutation for step size
        this.m_scalingDev = list[RNG.randomInt(0, list.length - 1)];
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

/**********************************************************************************************************************
 * These are for GUI
 */
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
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The correlated vector mutation stores a specific mutation vector per individual.";
    }

    /**
     * Set the initial mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setScalingDev(double d) {
        this.m_scalingDev = d;
    }

    public double getScalingDev() {
        return this.m_scalingDev;
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
        this.m_LowerLimitStepSize = d;
    }

    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }

    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step.";
    }

    public double getRotationDev() {
        return m_rotationDev;
    }

    public void setRotationDev(double rotationDeg) {
        this.m_rotationDev = rotationDeg;
    }

    public String rotationDevTipText() {
        return "Std deviation of the rotation angle distribution";
    }

    public double getInitialVelocity() {
        return m_initialVelocity;
    }

    public void setInitialVelocity(double velocity) {
        m_initialVelocity = velocity;
    }

    public double getUpperLimitStepSize() {
        return m_UpperLimitStepSize;
    }

    public void setUpperLimitStepSize(double upperLimitStepSize) {
        m_UpperLimitStepSize = upperLimitStepSize;
    }

    public String upperLimitStepSizeTipText() {
        return "Set the upper limit for the mutation step.";
    }
}
