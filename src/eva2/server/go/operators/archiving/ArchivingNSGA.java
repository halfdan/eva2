package eva2.server.go.operators.archiving;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** The non dominated sorting GA archiving method, based on dominace sorting.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.02.2004
 * Time: 16:50:45
 * To change this template use File | Settings | File Templates.
 */
public class ArchivingNSGA extends AbstractArchiving implements java.io.Serializable {

    public InterfaceRemoveSurplusIndividuals m_Cleaner = new RemoveSurplusIndividualsDynamicHyperCube();

    public ArchivingNSGA() {     
    }

    public ArchivingNSGA(ArchivingNSGA a) {
        this.m_Cleaner  = (InterfaceRemoveSurplusIndividuals)a.m_Cleaner.clone();
    }

    public Object clone() {
        return (Object) new ArchivingNSGA(this);
    }

    /** This method allows you to merge to populations into an archive.
     *  This method will add elements from pop to the archive but will also
     *  remove elements from the archive if the archive target size is exceeded.
     * @param pop       The population that may add Individuals to the archive.
     */
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) pop.SetArchive(new Population());     

        // test for each element in population if it
        // is dominating a element in the archive
        for (int i = 0; i < pop.size(); i++) {
            if (this.isDominant((AbstractEAIndividual)pop.get(i), pop.getArchive())) {
                this.addIndividualToArchive((AbstractEAIndividual)((AbstractEAIndividual)pop.get(i)).clone(), pop.getArchive());
            }
        }

        // Now clear the archive of surplus individuals
        Population  archive = pop.getArchive();

        this.m_Cleaner.removeSurplusIndividuals(archive);
    }


/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Non-dominating sorting GA revision 1.0.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "NSGA";
    }
    
    /** This method allows you to toggle between hypercube resambling and
     * static mode for hybercube sampling
     * @param s  The design mode.
     */
    public void setCleaner(InterfaceRemoveSurplusIndividuals s) {
        this.m_Cleaner = s;
    }
    public InterfaceRemoveSurplusIndividuals getCleaner() {
        return this.m_Cleaner;
    }
    public String cleanerTipText() {
        return "Choose the method to remove surplus individuals from the archive.";
    }
}