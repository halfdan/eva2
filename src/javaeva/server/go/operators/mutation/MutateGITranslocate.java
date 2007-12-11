package javaeva.server.go.operators.mutation;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceGIIndividual;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.05.2005
 * Time: 16:15:37
 * To change this template use File | Settings | File Templates.
 */
public class MutateGITranslocate implements InterfaceMutation, java.io.Serializable {

    int         m_MaxLengthOfTranslocate = 4;

    public MutateGITranslocate() {

    }
    public MutateGITranslocate(MutateGITranslocate mutator) {
        this.m_MaxLengthOfTranslocate     = mutator.m_MaxLengthOfTranslocate;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGITranslocate();
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGITranslocate) {
            MutateGITranslocate mut = (MutateGITranslocate)mutator;
            if (this.m_MaxLengthOfTranslocate != mut.m_MaxLengthOfTranslocate) return false;
            return true;
        }
        else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt){

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[]       x = ((InterfaceGIIndividual)individual).getIGenotype();
            int         from, to, length;
            length = RandomNumberGenerator.randomInt(1, this.m_MaxLengthOfTranslocate);
            if (x.length < length+2) return;
            from = RandomNumberGenerator.randomInt(0, x.length - 1 - length);
            to = RandomNumberGenerator.randomInt(0, x.length - 1 - length);
            //this.pintInt("Before ", x);
            int[] tmp = new int[x.length];
            int[] without = new int[x.length - length];
            int[] insert = new int[length];
            for (int i = 0; i < length; i++) insert[i] = x[i+from];
            for (int i = 0; i < without.length; i++) {
                if (i < from) without[i] = x[i];
                else without[i] = x[i+length];
            }
            for (int i = 0; i < to; i++) {
                tmp[i] = without[i];
            }
            for (int i = to; i < to+length; i++) {
                tmp[i] = insert[i-to];
            }
            for (int i = to+length; i < x.length; i++) {
                tmp[i] = without[i-length];
            }
            //System.out.println(""+from+"/"+to+"/"+length);
            //this.pintInt("After ", tmp);
            ((InterfaceGIIndividual)individual).SetIGenotype(tmp);
        }
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    private void pintInt(String s, int[] x) {
        String tmp = "{"+x[0];
        for (int i = 1; i < x.length; i++) tmp += ", "+x[i];
        System.out.println(s+tmp+"}");
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "GI translocation mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GI translocation mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This mutation translocates a segment of the int[].";
    }

    /** This method allows you to set the max length of invert.
     * @param n     The max length of invert
     */
    public void setMaxLengthOfTranslocate(int n) {
        this.m_MaxLengthOfTranslocate = n;
    }
    public int getMaxLengthOfTranslocate() {
        return this.m_MaxLengthOfTranslocate;
    }
    public String maxLengthOfTranslocateTipText() {
        return "Gives the maximum length of the translocated segment.";
    }
}