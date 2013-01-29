package eva2.server.go.operators.crossover;

import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.02.2004
 * Time: 17:49:16
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverESSBX implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private double                          m_Eta = 0.2;

    public CrossoverESSBX() {

    }
    
    public CrossoverESSBX(double eta) {
    	m_Eta=eta;
    }
    
    public CrossoverESSBX(CrossoverESSBX c) {
        this.m_OptimizationProblem      = c.m_OptimizationProblem;
        this.m_Eta                      = c.m_Eta;
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverESSBX(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[]  result = null;
        double[][]              parents, children;
        double                  beta, u;

        result      = new AbstractEAIndividual[partners.size()+1];
        result[0]   = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1]     = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());

        if ((indy1 instanceof InterfaceESIndividual) && (partners.get(0) instanceof InterfaceESIndividual)) {
            parents     = new double[partners.size()+1][];
            children    = new double[partners.size()+1][];
            for (int i = 0; i < result.length; i++) {
                parents[i] = new double[((InterfaceESIndividual)result[i]).getDGenotype().length];
                children[i] = new double[parents[i].length];
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, parents[i], 0, parents[i].length);
                System.arraycopy(((InterfaceESIndividual)result[i]).getDGenotype(), 0, children[i], 0, parents[i].length);
            }

            for (int i = 0; i < children[0].length; i++) {
                u = RNG.randomDouble(0,1);
                if (u <= 0.5) {
                    beta = Math.pow((2*u), 1/(this.m_Eta + 1));
                } else {
                    beta = Math.pow((0.5/(1-u)), 1/(this.m_Eta + 1));
                }
                children[0][i] = 0.5 * ( (1 + beta)*parents[0][i] + (1 - beta)*parents[1][i] );
                children[1][i] = 0.5 * ( (1 - beta)*parents[0][i] + (1 + beta)*parents[1][i] );
            }
            // write the result back
            for (int i = 0; i < result.length; i++) ((InterfaceESIndividual)result[i]).SetDGenotype(children[i]);
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverESSBX) {
            CrossoverESSBX cross = (CrossoverESSBX)crossover;
            if (this.m_Eta != cross.m_Eta) return false;
            return true;
        }
        else return false;
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    public static void main(String[] args) {
        Population              pop     = new Population();
        double[]                tmpD    = new double[2];
        ESIndividualDoubleData  indy1, indy2, indy3;
        F1Problem               prob = new F1Problem();

        // init individual
        indy1 = new ESIndividualDoubleData();
        double[][] range = new double[2][2];
        range[0][0] = -2.0;
        range[0][1] =  2.0;
        range[1][0] = -2.0;
        range[1][1] =  2.0;
        indy1.setDoubleDataLength(2);
        indy1.SetDoubleRange(range);
        // init values
        indy2 = (ESIndividualDoubleData)indy1.clone();
        indy3 = (ESIndividualDoubleData)indy1.clone();
        if (false) {
            // random init
            indy1.defaultInit(prob);
            indy2.defaultInit(prob);
            indy3.defaultInit(prob);
        } else {
            // value init
            tmpD[0] = 0.5;
            tmpD[1] = -0.5;
            indy1.initByValue(tmpD, prob);
            tmpD[0] =  -0.5;
            tmpD[1] =  0.5;
            indy2.initByValue(tmpD, prob);
            tmpD[0] = -0.5;
            tmpD[1] =  1;
            indy3.initByValue(tmpD, prob);
        }
        // set the parents
        pop.add(indy2);
        pop.add(indy3);
        tmpD[0] = 1;
        tmpD[1] = 1;
        Plot plot = new eva2.gui.Plot("SBX Test", "x", "y", true);
        tmpD = indy1.getDoubleData();
        plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
        tmpD = indy2.getDoubleData();
        plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
        tmpD = indy3.getDoubleData();
        plot.setUnconnectedPoint(tmpD[0], tmpD[1], 0);
        plot.setUnconnectedPoint(-2, -2, 0);
        plot.setUnconnectedPoint(2, 2, 0);
        plot.setUnconnectedPoint(2, 2, 0);

        CrossoverESSBX cross = new CrossoverESSBX();
        cross.m_Eta  = 0.2;
        AbstractEAIndividual[] offsprings;
        for (int i = 0; i < 5000; i++) {
            offsprings = cross.mate(indy1, pop);
            for (int j = 0; j < offsprings.length; j++) {
                tmpD = ((ESIndividualDoubleData)offsprings[j]).getDoubleData();
                plot.setUnconnectedPoint(tmpD[0], tmpD[1], 1);
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
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES SBX crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The SBX crossover simulates a binary crossover (works only for two partners!).";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param a   The number of crossovers.
     */
    public void setEta(double a) {
        if (a < 0) a = 0;
        this.m_Eta = a;
    }
    public double getEta() {
        return this.m_Eta;
    }
    public String etaTipText() {
        return "Set the Eta_c value (the larger the value, the more restricted the search).";
    }
}