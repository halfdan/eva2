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

import java.util.ArrayList;

/**
 *
 */
public class CrossoverESPCX implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem optimizationProblem;
    private double eta = 0.2;
    private double zeta = 0.2;

    public CrossoverESPCX() {

    }

    public CrossoverESPCX(CrossoverESPCX c) {
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
        return new CrossoverESPCX(this);
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
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

            double[] g = Mathematics.meanVect(parents), tmpD;
            double w, v;
            ArrayList subSpace;
            // now determine the offsprings
            for (int i = 0; i < children.length; i++) {
                // direction vector
                tmpD = Mathematics.getVectorFromTo(g, parents[i]);

                subSpace = this.getCoordinates(g, i, parents);

//                Plot plot = new eva2.gui.plot.Plot("SBX Test", "x", "y", true);
//                plot.setUnconnectedPoint(-2, -2, 0);
//                plot.setUnconnectedPoint(2, 2, 0);
//                for (int z = 0; z < parents.length; z++) {
//                    plot.setUnconnectedPoint(parents[z][0], parents[z][1], 0);
//                    plot.setUnconnectedPoint(parents[z][0], parents[z][1], 0);
//                }
//                double[] tara = (double[])subSpace.get(0);
//                plot.setConnectedPoint(g[0], g[1], 1);
//                plot.setConnectedPoint(tara[0]+g[0], tara[1]+g[1], 1);
//                for (int z = 1; z < subSpace.size(); z++) {
//                    tara = (double[])subSpace.get(z);
//                    plot.setConnectedPoint(g[0], g[1], 2);
//                    plot.setConnectedPoint(tara[0]+g[0], tara[1]+g[1], 2);
//                }

                // now calucate the children
                // first the parent and the d
                for (int j = 0; j < parents[i].length; j++) {
                    children[i][j] = parents[i][j];
                    children[i][j] += RNG.gaussianDouble(this.zeta) * tmpD[j];
                }
                // then the other parents
                for (int j = 1; j < subSpace.size(); j++) {
                    tmpD = (double[]) subSpace.get(j);
                    w = RNG.gaussianDouble(this.zeta);
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

    private ArrayList getCoordinates(double[] mean, int index, double[][] parents) {
        ArrayList result = new ArrayList();
        double[] tmpVec, toro;
        double tmpD;

        tmpVec = Mathematics.vvSub(parents[index], mean);
        result.add(tmpVec);

        for (int i = 0; i < parents.length; i++) {
            if (i != index) {
                tmpVec = Mathematics.vvSub(parents[i], mean);
                if (this.isValidVec(tmpVec)) {
                    // apply the infamous Gram-Schmidt
                    for (int j = 0; j < result.size(); j++) {
                        toro = (double[]) result.get(j);
                        tmpD = Mathematics.vvMult(toro, tmpVec) / Mathematics.vvMult(toro, toro);
                        toro = Mathematics.svMult(tmpD, toro);
                        tmpVec = Mathematics.vvSub(tmpVec, toro);
                    }
                    if (this.isValidVec(tmpVec)) {
                        result.add(tmpVec);
                    }
                }
            }
        }

        // now determine the average D for all alternative Parents
        double tmpMean;
        for (int i = 1; i < result.size(); i++) {
            toro = (double[]) result.get(i);
            Mathematics.normVect(toro, toro);
            tmpMean = 0;
            for (int j = 0; j < parents.length; j++) {
                if (j != index) {
                    tmpMean += Math.abs(Mathematics.vvMult(toro, Mathematics.vvSub(parents[j], mean)));
                }
            }
            tmpMean /= ((double) (result.size() - 1));
            toro = Mathematics.svMult(tmpMean, toro);
            result.set(i, toro);
        }

        return result;
    }

    private boolean isValidVec(double[] d) {
        double sum = 0;
        for (int i = 0; i < d.length; i++) {
            if (Double.isNaN(d[i])) {
                return false;
            }
            sum += Math.pow(d[i], 2);
        }
        if (Double.isNaN(sum)) {
            return false;
        }
        if (Math.abs(sum) < 0.000000000000000001) {
            return false;
        }
        return true;
    }

    /**
     * This method allows you to evaluate wether two crossover operators
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

        //RNG.setseed(1);
        // initialize individual
        indy1 = new ESIndividualDoubleData();
        double[][] range = new double[n][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = -2;
            range[i][1] = 2;
        }
        indy1.setDoubleDataLength(n);
        indy1.setDoubleRange(range);
        // initialize values
        indy2 = (ESIndividualDoubleData) indy1.clone();
        indy3 = (ESIndividualDoubleData) indy1.clone();
        indy4 = (ESIndividualDoubleData) indy1.clone();
        if (false) {
            // random initialize
            indy1.defaultInit(prob);
            indy2.defaultInit(prob);
            indy3.defaultInit(prob);
            indy4.defaultInit(prob);
        } else {
            // value initialize
            tmpD[0] = 0;
            tmpD[1] = 1;
            indy1.initByValue(tmpD, prob);
            tmpD[0] = -1;
            tmpD[1] = 1;
            indy2.initByValue(tmpD, prob);
            tmpD[0] = 1;
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
        CrossoverESPCX cross = new CrossoverESPCX();
        cross.eta = 0.2;
        cross.zeta = 0.2;
        AbstractEAIndividual[] offsprings;
        for (int i = 0; i < 1; i++) {
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
        return "ES PCX crossover";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is the Parent Centric Crossover (PCX).";
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
        return "The Eta of PCX.";
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
        return "The Zeta of PCX.";
    }
}