package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 * The non dominated sorting GA archiving method, based on dominace sorting.
 */
@eva2.util.annotation.Description(value ="Non-dominating sorting GA revision 1.0.")
public class ArchivingNSGA extends AbstractArchiving implements java.io.Serializable {

    public InterfaceRemoveSurplusIndividuals cleaner = new RemoveSurplusIndividualsDynamicHyperCube();

    public ArchivingNSGA() {
    }

    public ArchivingNSGA(ArchivingNSGA a) {
        this.cleaner = (InterfaceRemoveSurplusIndividuals) a.cleaner.clone();
    }

    @Override
    public Object clone() {
        return new ArchivingNSGA(this);
    }

    /**
     * This method allows you to merge to populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    @Override
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }

        // test for each element in population if it
        // is dominating a element in the archive
        for (int i = 0; i < pop.size(); i++) {
            if (this.isDominant((AbstractEAIndividual) pop.get(i), pop.getArchive())) {
                this.addIndividualToArchive((AbstractEAIndividual) ((AbstractEAIndividual) pop.get(i)).clone(), pop.getArchive());
            }
        }

        // Now clear the archive of surplus individuals
        Population archive = pop.getArchive();

        this.cleaner.removeSurplusIndividuals(archive);
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "NSGA";
    }

    /**
     * This method allows you to toggle between hypercube resambling and
     * static mode for hybercube sampling
     *
     * @param s The design mode.
     */
    public void setCleaner(InterfaceRemoveSurplusIndividuals s) {
        this.cleaner = s;
    }

    public InterfaceRemoveSurplusIndividuals getCleaner() {
        return this.cleaner;
    }

    public String cleanerTipText() {
        return "Choose the method to remove surplus individuals from the archive.";
    }
}