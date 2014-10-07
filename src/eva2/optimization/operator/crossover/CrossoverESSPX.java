package eva2.optimization.operator.crossover;

import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/**
 *
 */
public class CrossoverESSPX implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem optimizationProblem;
    private double epsilon = 1.2;

    public CrossoverESSPX() {

    }

    public CrossoverESSPX(CrossoverESSPX c) {
        this.optimizationProblem = c.optimizationProblem;
        this.epsilon = c.epsilon;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESSPX(this);
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceESIndividual, then nothing will happen.
     *
     * @param indy1    The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        double[][] parents, children;

        result = new AbstractEAIndividual[partners.size() + 1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i + 1] = (AbstractEAIndividual) ((AbstractEAIndividual) partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            double intermediate;
            parents = new double[partners.size() + 1][];
            children = new double[partners.size() + 1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual) result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual) result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }

            double r;
            double[] g = Mathematics.meanVect(parents);
            double[][] Y = new double[parents.length][], C;

            // calculate the Y vectors
            for (int i = 0; i < parents.length; i++) {
                Y[i] = Mathematics.vvAdd(g, Mathematics.svMult(this.epsilon, Mathematics.vvSub(parents[i], g)));
            }

            // now for each child the C vectors and the result
            for (int i = 0; i < children.length; i++) {
                C = new double[Y.length][];
                C[0] = Mathematics.zeroes(parents[0].length);
                for (int j = 1; j < Y.length; j++) {
                    r = Math.pow(RNG.randomDouble(0, 1), 1 / ((double) j));
                    C[j] = Mathematics.vvAdd(Y[j - 1], C[j - 1]);
                    C[j] = Mathematics.vvSub(C[j], Y[j]);
                    C[j] = Mathematics.svMult(r, C[j]);
                    //C[j]    = this.scalarMultVector(r, this.subVector(Y[j-1], this.addVector(Y[j], C[j-1])));
                }
                // now the children results from
                children[i] = Mathematics.vvAdd(Y[Y.length - 1], C[C.length - 1]);
            }

            // write the result back
            for (int i = 0; i < result.length; i++) {
                ((InterfaceESIndividual) result[i]).setDGenotype(children[i]);
            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }


    /**
     * This method allows you to evaluate wether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESSPX) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.optimizationProblem = opt;
    }

    public static void main(String[] args) {
        boolean plotFlag = true;
        Plot plot = null;
        Population pop = new Population();
        double[] tmpD = new double[2];
        ESIndividualDoubleData indy1, indy2, indy3, indy4;
        F1Problem prob = new F1Problem();
        int n = 2;

        RNG.setRandomSeed(1);
        // init individual
        indy1 = new ESIndividualDoubleData();
        double[][] range = new double[n][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = -2.0;
            range[i][1] = 2.0;
        }
        indy1.setDoubleDataLength(n);
        indy1.setDoubleRange(range);
        // init values
        indy2 = (ESIndividualDoubleData) indy1.clone();
        indy3 = (ESIndividualDoubleData) indy1.clone();
        indy4 = (ESIndividualDoubleData) indy1.clone();
        if (false) {
            // random init
            indy1.defaultInit(prob);
            indy2.defaultInit(prob);
            indy3.defaultInit(prob);
            indy4.defaultInit(prob);
        } else {
            // value init
            tmpD[0] = -1;
            tmpD[1] = -0.5;
            indy1.initByValue(tmpD, prob);
            tmpD[0] = -0.5;
            tmpD[1] = 0.5;
            indy2.initByValue(tmpD, prob);
            tmpD[0] = 0.5;
            tmpD[1] = -1;
            indy3.initByValue(tmpD, prob);
            tmpD[0] = 0.5;
            tmpD[1] = 1;
            indy4.initByValue(tmpD, prob);
        }
        // set the parents
        pop = new Population();
        pop.add(indy2);
        pop.add(indy3);
        //pop.add(indy4);
        tmpD[0] = 1;
        tmpD[1] = 1;
        if (plotFlag) {
            plot = new Plot("SBX Test", "x", "y", true);
            tmpD = indy1.getDoubleData();
            plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
            tmpD = indy2.getDoubleData();
            plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
            tmpD = indy3.getDoubleData();
            plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
            tmpD = indy4.getDoubleData();
            //plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
            plot.setUnconnectedPoint(-2, -2, 0);
            plot.setUnconnectedPoint(2, 2, 0);
            plot.setUnconnectedPoint(2, 2, 0);
        }
        CrossoverESSPX cross = new CrossoverESSPX();
        cross.epsilon = 1.2;
        AbstractEAIndividual[] offsprings;
        for (int i = 0; i < 500; i++) {
            offsprings = cross.mate(indy1, pop);
            for (int j = 0; j < offsprings.length; j++) {
                tmpD = ((ESIndividualDoubleData) offsprings[j]).getDoubleData();
                if (plotFlag) {
                    plot.setUnconnectedPoint(tmpD[0], tmpD[1], 1);
                }
                //range = ((ESIndividualDoubleData)offsprings[j]).getDoubleRange();
                //System.out.println("["+range[0][0]+"/"+range[0][1]+";"+range[1][0]+"/"+range[1][1]+"]");
            }
        }
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
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
        return "ES SPX crossover";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is the Simplex Crossover (SPX).";
    }

    public void setEpsilon(double a) {
        if (a < 0) {
            a = 0;
        }
        this.epsilon = a;
    }

    public double getEpsilon() {
        return this.epsilon;
    }

    public String epsilonTipText() {
        return "The epsilon of SPX.";
    }
}