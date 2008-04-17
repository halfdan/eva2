package eva2.server.go.operators.crossover;


import java.util.ArrayList;

import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.02.2005
 * Time: 11:17:17
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverESPCX implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private double                          m_Eta = 0.2;
    private double                          m_Zeta  = 0.2;

    public CrossoverESPCX() {

    }
    public CrossoverESPCX(CrossoverESPCX c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
        this.m_Eta                      = c.m_Eta;
        this.m_Zeta                     = c.m_Zeta;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverESPCX(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[]  result = null;
        double[][]              parents, children;

        result      = new AbstractEAIndividual[partners.size()+1];
        result[0]   = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1]     = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            double      intermediate;
            parents     = new double[partners.size()+1][];
            children    = new double[partners.size()+1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual)result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }

            double[]    g = this.getMeanVector(parents), tmpD;
            double      w, v;
            ArrayList   subSpace;
            // now determine the offsprings
            for (int i = 0; i < children.length; i++) {
                // direction vector
                tmpD = this.getVectorFromTo(g, parents[i]);

                subSpace = this.getCoordinates(g, i, parents);

//                Plot plot = new javaeva.gui.Plot("SBX Test", "x", "y", true);
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
                    children[i][j] += RNG.gaussianDouble(this.m_Zeta)*tmpD[j];
                }
                // then the other parents
                for (int j = 1; j < subSpace.size(); j++) {
                    tmpD = (double[])subSpace.get(j);
                    w = RNG.gaussianDouble(this.m_Zeta);
                    children[i] = this.addVector(children[i], this.scalarMultVector(w, tmpD));
                }
            }

            // write the result back
            for (int i = 0; i < result.length; i++) ((InterfaceESIndividual)result[i]).SetDGenotype(children[i]);
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    private ArrayList getCoordinates(double[] mean, int index, double[][] parents) {
        ArrayList   result = new ArrayList();
        double[]    tmpVec, toro;
        double      tmpD;

        tmpVec = this.subVector(parents[index], mean);
        result.add(tmpVec);

        for (int i = 0; i < parents.length; i++) {
            if (i != index) {
                tmpVec = this.subVector(parents[i], mean);
                if (this.isValidVec(tmpVec)) {
                    // apply the infamous Gram-Schmidt
                    for (int j = 0; j < result.size(); j++) {
                        toro = (double[]) result.get(j);
                        tmpD = this.multVector(toro, tmpVec)/this.multVector(toro, toro);
                        toro = this.scalarMultVector(tmpD, toro);
                        tmpVec = this.subVector(tmpVec, toro);
                    }
                    if (this.isValidVec(tmpVec)) result.add(tmpVec);
                }
            }
        }

        // now determine the average D for all alternative Parents
        double tmpMean;
        for (int i = 1; i < result.size(); i++) {
            toro = (double[]) result.get(i);
            toro = this.getNormalizedVector(toro);
            tmpMean = 0;
            for (int j = 0; j < parents.length; j++) {
                if (j != index) {
                    tmpMean += Math.abs(this.multVector(toro, this.subVector(parents[j], mean)));
                }
            }
            tmpMean = tmpMean/((double)(result.size()-1));
            toro = this.scalarMultVector(tmpMean, toro);
            result.set(i, toro);
        }

        return result;
    }

    private double[] randomVector(int n) {
        double[] result = new double[n];
        for (int i = 0; i < result.length; i++) {
            result[i] = RNG.gaussianDouble(1);
        }
        return this.getNormalizedVector(result);
    }

    private double[] subVector(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private double[] addVector(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    private double multVector(double[] a, double[] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i]*b[i];
        }
        return result;
    }

    private double[] scalarMultVector(double a, double[] b) {
        double[] result = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i]= a*b[i];
        }
        return result;
    }

    private double[] getNormalizedVector(double[] d) {
        double[] result = new double[d.length];
        double sum = 0;

        for (int i = 0; i < d.length; i++) {
            result[i] = d[i];
            sum += Math.pow(d[i], 2);
        }
        sum = Math.sqrt(sum);

        for (int i = 0; i < d.length; i++) {
            result[i] = result[i]/sum;
        }

        return result;
    }

    private boolean isValidVec(double[] d) {
        double sum = 0;
        for (int i = 0; i < d.length; i++) {
            if (Double.isNaN(d[i])) return false;
            sum += Math.pow(d[i],2);
        }
        if (Double.isNaN(sum)) return false;
        if (Math.abs(sum) < 0.000000000000000001) return false;
        return true;
    }

    /** This method return a vector from a to b
     * @param a     first vector
     * @param b     second vectors
     * @return the vector from a to b
     */
    private double[] getVectorFromTo(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = b[i] - a[i];
        }
        return result;
    }

    /** This method returns a mean vector from a whole number of vectors
     * @param d     d[i] the vectors, d[i][j] the jth coordinate of the ith vector
     * @return The mean vector.
     */
    private double[] getMeanVector(double[][] d) {
        double[] result = new double[d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                result[j] += d[i][j];
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i]/((double)d.length);
        }
        return result;
    }


    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESUNDX) return true;
        else return false;
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    public static void main(String[] args) {
        boolean                 plotFlag    = true;
        Plot                    plot        = null;
        Population              pop         = new Population();
        double[]                tmpD        = new double[2];
        ESIndividualDoubleData  indy1, indy2, indy3, indy4;
        F1Problem               prob        = new F1Problem();
        int                     n           = 2;

        //RNG.setseed(1);
        // init individual
        indy1 = new ESIndividualDoubleData();
        double[][] range = new double[n][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = -2;
            range[i][1] =  2;
        }
        indy1.setDoubleDataLength(n);
        indy1.SetDoubleRange(range);
        // init values
        indy2 = (ESIndividualDoubleData)indy1.clone();
        indy3 = (ESIndividualDoubleData)indy1.clone();
        indy4 = (ESIndividualDoubleData)indy1.clone();
        if (false) {
            // random init
            indy1.defaultInit();
            indy2.defaultInit();
            indy3.defaultInit();
            indy4.defaultInit();
        } else {
            // value init
            tmpD[0] = 0;
            tmpD[1] = 1;
            indy1.initByValue(tmpD, prob);
            tmpD[0] =  -1;
            tmpD[1] =  1;
            indy2.initByValue(tmpD, prob);
            tmpD[0] = 1;
            tmpD[1] = -1;
            indy3.initByValue(tmpD, prob);
            tmpD[0] = 0.5;
            tmpD[1] =  1;
            indy4.initByValue(tmpD, prob);
        }
        // set the parents
        pop= new Population();
        pop.add(indy2);
        pop.add(indy3);
        //pop.add(indy4);
        tmpD[0] = 1;
        tmpD[1] = 1;
        if (plotFlag) {
            plot = new eva2.gui.Plot("SBX Test", "x", "y", true);
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
        cross.m_Eta  = 0.2;
        cross.m_Zeta   = 0.2;
        AbstractEAIndividual[] offsprings;
        for (int i = 0; i < 1; i++) {
            offsprings = cross.mate(indy1, pop);
            for (int j = 0; j < offsprings.length; j++) {
                tmpD = ((ESIndividualDoubleData)offsprings[j]).getDoubleData();
                if (plotFlag) plot.setUnconnectedPoint(tmpD[0], tmpD[1], 1);
                //range = ((ESIndividualDoubleData)offsprings[j]).getDoubleRange();
                //System.out.println("["+range[0][0]+"/"+range[0][1]+";"+range[1][0]+"/"+range[1][1]+"]");
            }
        }
    }

    public String getStringRepresentation() {
        return this.getName();
    }

 /**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES PCX crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is the Parent Centric Crossover (PCX).";
    }
    public void setEta(double a) {
        if (a < 0) a = 0;
        this.m_Eta = a;
    }
    public double getEta() {
        return this.m_Eta;
    }
    public String etaTipText() {
        return "The Eta of PCX.";
    }
    public void setZeta(double a) {
        if (a < 0) a = 0;
        this.m_Zeta = a;
    }
    public double getZeta() {
        return this.m_Zeta;
    }
    public String zetaTipText() {
        return "The Zeta of PCX.";
    }
}