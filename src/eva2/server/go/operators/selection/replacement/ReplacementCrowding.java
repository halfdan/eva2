package eva2.server.go.operators.selection.replacement;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.selection.SelectRandom;
import eva2.server.go.populations.Population;

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

    public ReplacementCrowding(int C) {
    	setC(C);
    }
    
    /** The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /** 
     * From a random subset of size C, the closest is replaced by the given individual. 
     * The sub parameter is not regarded.
     * 
     * @param indy      The individual to insert
     * @param pop       The population
     * @param sub       The subset
     */
    @Override
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
    public static String globalInfo() {
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