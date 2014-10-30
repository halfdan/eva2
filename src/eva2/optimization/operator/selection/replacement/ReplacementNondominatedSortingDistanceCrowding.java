package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

import java.util.Collections;
import java.util.Comparator;


/**
 * This crowding method replaces the most similar individual from a random group if better.
 */
@Description("This method replaces the individual with the worsr paretorank and crowding factor")
public class ReplacementNondominatedSortingDistanceCrowding implements InterfaceReplacement, java.io.Serializable {

    public class HypervolumeComperator implements Comparator<AbstractEAIndividual> {
        @Override
        public int compare(AbstractEAIndividual arg0, AbstractEAIndividual arg1) {
            // TODO Auto-generated method stub
            Double a0 = (Double) arg0.getData("HyperCube");
            Double a1 = (Double) arg1.getData("HyperCube");
            if (a0 != null & a1 != null) {
                return a1.compareTo(a0);
            } else {
                return 0;
            }
        }
    }

    ArchivingNSGAII dummyArchive = new ArchivingNSGAII();

    SelectRandom random = new SelectRandom();


    public ReplacementNondominatedSortingDistanceCrowding() {

    }

    public ReplacementNondominatedSortingDistanceCrowding(ReplacementNondominatedSortingDistanceCrowding b) {
        this.dummyArchive = new ArchivingNSGAII();

    }


    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /**
     * From a random subset of size C, the closest is replaced by the given individual.
     * The sub parameter is not regarded.
     *
     * @param indy The individual to insert
     * @param pop  The population
     * @param sub  The subset
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {


        pop.add(indy);
        Population[] store = dummyArchive.getNonDominatedSortedFronts(pop);
        dummyArchive.calculateCrowdingDistance(store);//TODO die f�r das gesamte Archiv am St�ck berechnen und nicht f�r die Einzelfronten!
        for (int i = 0; i < store.length; i++) {
            synchronized (store[i]) {


                try {
                    Collections.sort(store[i], new HypervolumeComperator());
                } catch (java.util.ConcurrentModificationException e) {


                }
            }
        }

        pop.clear();
        for (int i = 0; i < store.length && pop.size() < pop.getTargetSize(); i++) {
            for (int j = 0; j < store[i].size() && pop.size() < pop.getTargetSize(); j++) {
                pop.add(store[i].getEAIndividual(j));
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Crowding";
    }

}