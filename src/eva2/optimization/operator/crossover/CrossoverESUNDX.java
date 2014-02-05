package eva2.optimization.operator.crossover;


import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

import java.util.ArrayList;

/**
 * This is the Unimodal Normal Distribution Crossover by Ono and Kobayashi, 1997. Cf.:
 *
 * @INPROCEEDINGS{Ono1997, author = {Ono, Isao and Kobayashi, Shigenobu},
 * title = {{A Real Coded Genetic Algorithm for Function Optimization Using Unimodal Normal Distributed Crossover}},
 * booktitle = {ICGA},
 * year = {1997},
 * pages = {246--253},
 * abstract = {This paper presents a new genetic algorithm for function optimization.
 * In function optimization, it is said to be difficult to optimize
 * functions that have strong epistasis among parameters. This is because
 * many of the conventional genetic algorithms work without adapting
 * to the landscape of functions. In this paper, we employ the real
 * number vector representation and propose a new crossover named the
 * unimodal normal distribution crossover (UNDX), considering epistasis
 * among parameters. The UNDX can optimize functions efficiently by
 * adapting the distribution of children to the landscape of functions.
 * By applying the proposed method to several benchmark problems, we
 * show its effectiveness.},
 * url = {http://garage.cse.msu.edu/icga97/Abstracts.html#092}
 * }
 */
public class CrossoverESUNDX implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem optimizationProblem;
    private double eta = 0.2;
    private double zeta = 0.2;

    public CrossoverESUNDX() {

    }

    public CrossoverESUNDX(CrossoverESUNDX c) {
        this.optimizationProblem = c.optimizationProblem;
        this.eta = c.eta;
        this.zeta = c.zeta;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESUNDX(this);
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

            double[][] nParents = new double[parents.length - 1][];
            for (int i = 1; i < parents.length; i++) {
                nParents[i - 1] = parents[i];
            }
            double[] g = Mathematics.meanVect(nParents), tmpD;
            double w, v;
            ArrayList givenCoordinates = this.getGivenCoordinates(g, nParents);
            ArrayList missingCorrdinates = this.getMissingCoordinates(g, parents[0], givenCoordinates);

            // now determine the offsprings
            for (int i = 0; i < children.length; i++) {
                // first the mean
                for (int j = 0; j < g.length; j++) {
                    children[i][j] = g[j];
                }
                // then the given coordinates
                for (int j = 0; j < givenCoordinates.size(); j++) {
                    tmpD = (double[]) givenCoordinates.get(j);
                    w = RNG.gaussianDouble(this.zeta);
                    children[i] = Mathematics.vvAdd(children[i], Mathematics.svMult(w, tmpD));
                }
                // now the missing stuff
                for (int j = 0; j < missingCorrdinates.size(); j++) {
                    tmpD = (double[]) missingCorrdinates.get(j);
                    w = RNG.gaussianDouble(this.eta);
                    children[i] = Mathematics.vvAdd(children[i], Mathematics.svMult(w, tmpD));
                }
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

    private ArrayList getGivenCoordinates(double[] mean, double[][] parents) {
        ArrayList result = new ArrayList();
        double[] tmpVec, toro;
        double tmpD;

        for (int i = 0; i < parents.length; i++) {
            tmpVec = Mathematics.vvSub(parents[i], mean);
            if (Mathematics.isValidVec(tmpVec)) {
                if (result.size() == 0) {
                    result.add(tmpVec);
                } else {
                    // apply the infamous Gram-Schmidt
                    for (int j = 0; j < result.size(); j++) {
                        toro = (double[]) result.get(j);
                        tmpD = Mathematics.vvMult(toro, tmpVec) / Mathematics.vvMult(toro, toro);
                        toro = Mathematics.svMult(tmpD, toro);
                        tmpVec = Mathematics.vvSub(tmpVec, toro);
                    }
                    if (Mathematics.isValidVec(tmpVec)) {
                        result.add(tmpVec);
                    }
                }
            }
        }

        return result;
    }

    private ArrayList getMissingCoordinates(double[] mean, double[] theOther, ArrayList given) {
        ArrayList result = new ArrayList(), completeList;
        double[] tmpVec, toro;
        double tmpD;

        completeList = new ArrayList();
        for (int i = 0; i < given.size(); i++) {
            completeList.add(given.get(i));
        }

        while (completeList.size() < mean.length) {
            tmpVec = RNG.gaussianVector(mean.length, 1., true);
            if (Mathematics.isValidVec(tmpVec)) {
                // apply the infamous Gram-Schmidt
                for (int j = 0; j < completeList.size(); j++) {
                    toro = (double[]) completeList.get(j);
                    tmpD = Mathematics.vvMult(toro, tmpVec) / Mathematics.vvMult(toro, toro);
                    toro = Mathematics.svMult(tmpD, toro);
                    tmpVec = Mathematics.vvSub(tmpVec, toro);
                }
                if (Mathematics.isValidVec(tmpVec)) {
                    Mathematics.normVect(tmpVec, tmpVec);
                    tmpVec = Mathematics.svMult(Mathematics.vvMult(theOther, tmpVec), tmpVec);
                    result.add(tmpVec);
                    completeList.add(tmpVec);
                }
            }
        }

        return result;
    }

    /**
     * This method allows you to evaluate whether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESUNDX) {
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
            tmpD[0] = -1.9;
            tmpD[1] = -1.8;
            indy1.initByValue(tmpD, prob);
            tmpD[0] = 1;
            tmpD[1] = 1;
            indy2.initByValue(tmpD, prob);
            tmpD[0] = -1;
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
        CrossoverESUNDX cross = new CrossoverESUNDX();
        cross.eta = 0.2;
        cross.zeta = 0.2;
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
        return "ES UNDX crossover";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is the Unimodal Normally Distributed crossover (UNDX) by Ono and Kobayashi, 1997, typically uses more than two parents.";
    }

    public void setEta(double a) {
        if (a < 0) {
            a = 0;
        }
        this.eta = a;
    }

    public double getEta() {
        return this.eta;
    }

    public String etaTipText() {
        return "The Eta of UNDX (=0,35/Math,sqrt(n-l-2)).";
    }

    public void setZeta(double a) {
        if (a < 0) {
            a = 0;
        }
        this.zeta = a;
    }

    public double getZeta() {
        return this.zeta;
    }

    public String zetaTipText() {
        return "The Zeta of UNDX (=1/Math,sqrt(l-2)).";
    }
}