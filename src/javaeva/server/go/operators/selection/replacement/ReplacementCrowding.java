package javaeva.server.go.operators.selection.replacement;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.distancemetric.PhenotypeMetric;
import javaeva.server.go.operators.selection.SelectRandom;
import javaeva.server.go.populations.Population;

/** This crowding method replaces the most similar individual from a random group if better.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 15:25:15
 * To change this template use File | Settings | File Templates.
 */
public class ReplacementCrowding implements InterfaceReplacement, java.io.Serializable {

    PhenotypeMetric metric = new PhenotypeMetric();
    SelectRandom    random = new SelectRandom();
    int             m_C    = 5;

    public ReplacementCrowding() {

    }

    public ReplacementCrowding(ReplacementCrowding b) {
        this.metric = new PhenotypeMetric();
        this.random = new SelectRandom();
        this.m_C    = b.m_C;
    }

    /** The ever present clone method
     */
    public Object clone() {
        return new ReplaceRandom();
    }

    /** This method will insert the given individual into the population
     * by replacing a individual either from the population or the given
     * subset
     * @param indy      The individual to insert
     * @param pop       The population
     * @param sub       The subset
     */
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {
        int index = 0;

        double distance = Double.POSITIVE_INFINITY, tmpD;
        Population tmp = random.selectFrom(pop, this.m_C);
        for (int i = 0; i < tmp.size(); i++) {
            tmpD = this.metric.distance(indy, (AbstractEAIndividual)tmp.get(i));
            if (tmpD < distance) {
                index       = i;
                distance    = tmpD;
            }
        }
        if (indy.isDominatingDebConstraints((AbstractEAIndividual)tmp.get(index))) {
            if (pop.remove((AbstractEAIndividual)tmp.get(index))) {
                pop.addIndividual(indy);
            }
        }
    }
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method replaces the most similar individual from a random group if better.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Crowding";
    }
    /** This method will set c
     * @param c
     */
    public void setC(int c) {
        this.m_C = c;
    }
    public int getC() {
        return this.m_C;
    }
    public String cTipText() {
        return "Set the crwoding factor.";
    }
}