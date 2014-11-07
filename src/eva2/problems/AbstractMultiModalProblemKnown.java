package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.tools.EVAERROR;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;

import java.util.List;

public abstract class AbstractMultiModalProblemKnown extends AbstractProblemDouble
        implements Interface2DBorderProblem, InterfaceMultimodalProblemKnown {
    protected static InterfaceDistanceMetric distanceMetric = new PhenotypeMetric();
    private double globalOptimum = 0;
    protected Population listOfOptima;
    protected double epsilon = 0.05;
    protected int problemDimension = 2;
    // if the global optimum is zero and we want to see logarithmic plots, the offset must be a little lower. see addOptimum()
    protected boolean makeGlobalOptUnreachable = false;

    public AbstractMultiModalProblemKnown() {
        this.problemDimension = 2;
        this.template = new ESIndividualDoubleData();
    }

    protected void cloneObjects(AbstractMultiModalProblemKnown b) {
        super.cloneObjects(b);
        if (b.listOfOptima != null) {
            this.listOfOptima = (Population) b.listOfOptima.clone();
        }
        this.globalOptimum = b.globalOptimum;
        this.epsilon = b.epsilon;
    }

    public AbstractMultiModalProblemKnown(AbstractMultiModalProblemKnown b) {
        cloneObjects(b);
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    @Override
    public void initializePopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        population.clear();

        ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(this.problemDimension);
        ((InterfaceDataTypeDouble) this.template).setDoubleRange(makeRange());
        for (int i = 0; i < population.getTargetSize(); i++) {
            tmpIndy = (AbstractEAIndividual) this.template.clone();
            tmpIndy.initialize(this);
            population.add(tmpIndy);
        }
        // population initialize must be last
        // it set's fitcalls and generation to zero
        population.initialize();
        if (listOfOptima == null) {
            this.globalOptimum = Double.NEGATIVE_INFINITY;
            listOfOptima = new Population();
            this.initListOfOptima();
        }
    }

    @Override
    public void initializeProblem() {
        super.initializeProblem();
        this.globalOptimum = Double.NEGATIVE_INFINITY;
        listOfOptima = new Population();
        this.initListOfOptima();
        if (!fullListAvailable() && (Double.isInfinite(globalOptimum))) {
            globalOptimum = 0;
        }
    }

    /**
     * Ths method allows you to evaluate a eva2.problems.simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        result[0] = this.globalOptimum - evalUnnormalized(x)[0];
        return result;
    }

    /**
     * This method returns the unnormalized (and unrotated!) function value for an maximization problem.
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    public abstract double[] evalUnnormalized(double[] x);

    @Override
    public String[] getAdditionalDataHeader() {
        return ToolBox.appendArrays(new String[]{"numOptsFound", "maxPeakRatio"}, super.getAdditionalDataHeader());
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return ToolBox.appendArrays(new String[]{"The number of optima identified with default accuracy", "The maximum peak ratio measure in [0,1], best at 1, if multiple local optima are known."}, super.getAdditionalDataInfo());
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] result = new Object[2];
        result[0] = this.getNumberOfFoundOptima((Population) pop);
        result[1] = this.getMaximumPeakRatio((Population) pop);
        return ToolBox.appendArrays(result, super.getAdditionalDataValue(pop));
    }


    /**********************************************************************************************************************
     * Implementation of InterfaceMultimodalProblemKnown
     */

    /**
     * This method allows you to add a 2d optima to the list of optima
     *
     * @param x
     * @param y
     */
    protected void add2DOptimum(double x, double y) {
        double[] point = new double[2];
        point[0] = x;
        point[1] = y;
        addOptimum(point);
    }

    /**
     * This method allows you to add a 2d optima to the list of optima
     *
     * @param point
     */
    protected void addOptimum(double[] point) {
        AbstractEAIndividual tmpIndy;
        tmpIndy = (AbstractEAIndividual) template.clone();
        ((InterfaceDataTypeDouble) tmpIndy).setDoubleGenotype(point);
        tmpIndy.setFitness(evalUnnormalized(point));
        if ((tmpIndy).getFitness(0) >= globalOptimum) {
            globalOptimum = (tmpIndy).getFitness(0);
            if (makeGlobalOptUnreachable) {
                double tmp = globalOptimum;
                double dx = 1e-30;
                while (tmp == globalOptimum) {
                    // this increases the optimum until there is a real difference.
                    // tries to avoid zero y-values which break the logarithmic plot
                    tmp += dx;
                    dx *= 10;
                }
                globalOptimum = tmp;
            }
        }
        if (isDoRotation()) {
            point = inverseRotateMaybe(point); // theres an inverse rotation required
            ((InterfaceDataTypeDouble) tmpIndy).setDoubleGenotype(point);
        }
        this.listOfOptima.add(tmpIndy);
    }

    /**
     * This method will prepare the problem to return a list of all optima
     * if possible and to return quality measures like NumberOfOptimaFound and
     * the MaximumPeakRatio. When implementing, use the addOptimum(double[])
     * method for every optimum, as it keeps track the global optimum.
     * This method will be called on initialization.
     */
    @Override
    public abstract void initListOfOptima();

    /**
     * This method returns a list of all optima as population
     *
     * @return population
     */
    @Override
    public Population getRealOptima() {
        return this.listOfOptima;
    }

    /**
     * Return true if the full list of optima is available, else false.
     *
     * @return
     */
    @Override
    public boolean fullListAvailable() {
        return ((getRealOptima() != null) && (getRealOptima().size() > 0));
    }

    /**
     * This method returns the Number of Identified optima
     *
     * @param pop A population of possible solutions.
     * @return int
     */
    @Override
    public int getNumberOfFoundOptima(Population pop) {
        return getNoFoundOptimaOf(this, pop);
    }

    public static int getNoFoundOptimaOf(InterfaceMultimodalProblemKnown mmProb, Population pop) {
        List<AbstractEAIndividual> sols = PostProcess.getFoundOptima(pop, mmProb.getRealOptima(), mmProb.getDefaultAccuracy(), true);
        return sols.size();
    }

    /**
     * This method returns the maximum peak ratio, which is the ratio of found fitness values corresponding to
     * known optima with the internal epsilon criterion and the sum of all fitness values seen as maximization.
     * Thus, if all optima are perfectly found, 1 is returned. If no optimum is found, zero is returned.
     * A return value of 0.5 may mean, e.g., that half of n similar optima have been found perfectly, or that 1 major
     * optimum of equal weight than all the others has been found perfectly, or that all optima have been found
     * with about 50% accuracy, etc.
     *
     * @param pop A population of possible solutions.
     * @return double
     */
    @Override
    public double getMaximumPeakRatio(Population pop) {
        if (!this.fullListAvailable()) {
            return -1;
        } else {
            return getMaximumPeakRatio(this.getRealOptima(), pop, epsilon);
        }
    }

    /**
     * Returns -1 if the full list is not available. Otherwise calculates the maximum peak ratio
     * based on the full list of known optima.
     * This assumes that the realOpts have fitness values assigned as for maximization and the
     * population has fitness values assigned for minimization (mirrored by maximum fitness within realOpts).
     * <p/>
     * This is in analogy to the original implementation by F.Streichert.
     *
     * @param realOpts
     * @param pop
     * @param epsilon
     * @return
     */
    public static double getMaximumPeakRatio(Population realOpts, Population pop, double epsilon) {
        double foundInvertedSum = 0, sumRealMaxima = 0;
        if (realOpts == null || (realOpts.size() == 0)) {
            return -1;
        }
        double tmp, maxOpt = realOpts.getEAIndividual(0).getFitness(0);
        sumRealMaxima = maxOpt;
        for (int i = 1; i < realOpts.size(); i++) {
            // search for the maximum fitness (for the maximization problem)
            // also sum up the fitness values
            maxOpt = Math.max(maxOpt, realOpts.getEAIndividual(i).getFitness(0));
            sumRealMaxima += realOpts.getEAIndividual(i).getFitness(0);
            if (realOpts.getEAIndividual(i).getFitness(0) < 0) {
                EVAERROR.errorMsgOnce("Warning: avoid negative maxima in AbstractMultiModalProblemKnown!");
            }
        }
        AbstractEAIndividual[] optsFound = PostProcess.getFoundOptimaArray(pop, realOpts, epsilon, true);
        for (int i = 0; i < realOpts.size(); i++) {
            // sum up the found optimal fitness values
            if (optsFound[i] != null) {
                tmp = (maxOpt - optsFound[i].getFitness(0));
                if (tmp < 0) {
                    EVAERROR.errorMsgOnce("warning: for the MPR calculation, negative fitness values may disturb the allover result (AbstractMultiModalProblemKnown)");
                }
                foundInvertedSum += Math.max(0., tmp);
            }
        }
        return foundInvertedSum / sumRealMaxima;
    }

    /**
     * Returns -1 if the full list is not available. Otherwise calculates the maximum peak ratio
     * based on the full list of known optima. Assumes that both realOpts and population have fitness
     * values assigned as in a maximization problem. This is the standard formulation of MPR.
     *
     * @param mmProb
     * @param pop
     * @param epsilon
     * @return
     */
    public static double getMaximumPeakRatioMaximization(Population realOpts, Population pop, double epsilon, int fitCrit) {
        AbstractEAIndividual[] optsFound = PostProcess.getFoundOptimaArray(pop, realOpts, epsilon, true);
        double mpr = 0;
        for (int i = 0; i < realOpts.size(); i++) {
            // sum up the specific optimal fitness values relative to optimal fitness
            if (optsFound[i] != null) {
                double tmp = optsFound[i].getFitness(fitCrit);
                if (tmp < 0) {
                    EVAERROR.errorMsgOnce("warning: for the MPR calculation, negative fitness values may disturb the allover result (AbstractMultiModalProblemKnown)");
                }
                mpr += (Math.max(0., tmp) / realOpts.getEAIndividual(i).getFitness(fitCrit));
            }
        }
        return mpr;
    }

    /**
     * Calculates the maximum peak ratio based on the given fitness values.
     * This is the standard formulation of MPR which assumes that all fitness
     * values are positive (and for a corresponding pair, foundFits[i]<realFits[i]).
     * If these assumptions hold, the MPR lies in [0,1].
     *
     * @param realFits
     * @param foundFits
     * @return
     */
    public static double getMaximumPeakRatioMaximization(double[] realFits, double[] foundFits) {
        double mpr = Mathematics.sum(foundFits) / Mathematics.sum(realFits);
        return mpr;
    }

    /**
     * Returns -1 if the full list is not available. Otherwise calculates the maximum peak ratio
     * based on the full list of known optima. Assumes that both realOpts and population have fitness
     * values assigned as in a minimization problem. Using a fitness value fitThreshold, they are inverted
     * and then treated as in maximization. An optimum which has not been found closely (by epsilon)
     * in the population is assumed to be covered with an individual of fitness fitThreshold.
     *
     * @param realOpts
     * @param pop
     * @param epsilon
     * @return
     */
    public static double getMaximumPeakRatioMinimization(Population realOpts, Population pop, double epsilon, int fitCrit, double fitThreshold) {
        AbstractEAIndividual[] optsFound = PostProcess.getFoundOptimaArray(pop, realOpts, epsilon, true);
        double[] realFits = new double[realOpts.size()];
        double[] foundFits = new double[realOpts.size()];
        double minOpt = Double.MAX_VALUE;
        for (int i = 0; i < realOpts.size(); i++) {
            // store the optimal fitness values and remember the smallest one
            realFits[i] = realOpts.getEAIndividual(i).getFitness(fitCrit);
            if (realFits[i] > fitThreshold) {
                EVAERROR.errorMsgOnce("Warning: The fitness threshold to turn minimization fitness values into " +
                        "maximization values should be larger than any optimal fitness! (AbstractMultiModalProblemKnown)");
            }
            if (i == 0 || (minOpt > realFits[i])) {
                minOpt = realFits[i];
            }
            // check if the opt. was found and store the corr. found fitness
            if (optsFound[i] != null) {
                foundFits[i] = optsFound[i].getFitness(fitCrit);
            } else {
                foundFits[i] = fitThreshold; // note that it wasnt found -- will result in zero
            }
        }
        // now we mirror all values with the threshold - provided they are below the threshold...
        for (int i = 0; i < realOpts.size(); i++) {
            realFits[i] = fitThreshold - realFits[i];
            foundFits[i] = fitThreshold - foundFits[i];
            if (foundFits[i] > realFits[i] && (foundFits[i] - realFits[i] > 1e-10)) {
                // this can happen if the real fitness is wrong or if the threshold allows individuals close to better optima to
                // be counted for actually inferior optima
                System.err.println("Warning: found fitness is better than real fitness - wrong predefined solution or suboptimal epsilon-criterion? Diff was: " + (foundFits[i] - realFits[i]));
            }
            if ((realFits[i] < 0) || (foundFits[i] < 0)) {
                EVAERROR.errorMsgOnce("warning: for the MPR calculation, negative fitness values may disturb the allover result (AbstractMultiModalProblemKnown)");
            }
        }
        // now we can call the standard calculation method
        return getMaximumPeakRatioMaximization(realFits, foundFits);
    }


    /**
     * @param epsilon the epsilon to set
     */
    @Override
    public void setDefaultAccuracy(double epsilon) {
        super.setDefaultAccuracy(epsilon);
    }
}