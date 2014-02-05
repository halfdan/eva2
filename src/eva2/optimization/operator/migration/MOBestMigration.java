package eva2.optimization.operator.migration;

import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectMOMaxiMin;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;

/**
 * Migration based on a Multi-criterial selection mechanism
 * migrating the n best individuals between all populations.
 */
public class MOBestMigration implements InterfaceMigration, java.io.Serializable {

    private InterfaceSelection selection = new SelectMOMaxiMin();
    int n = 5;

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new MOBestMigration();
    }

    /**
     * Typically i'll need some initialization method for
     * every bit of code i write....
     */
    @Override
    public void initMigration(InterfaceOptimizer[] islands) {
        // pff at a later stage i could initialize a topology here
    }

    /**
     * The migrate method can be called asychnronously or
     * sychronously. Basically it allows migration of individuals
     * between multiple EA islands and since there are so many
     * different possible strategies i've introduced this
     * interface which is most likely subject to numerous changes..
     * Note: Since i use the RMIRemoteThreadProxy everything done
     * to the islands will use the serialization method, so if
     * you call getPopulation() on an island it is not a reference
     * to the population but a serialized copy of the population!!
     */
    @Override
    public void migrate(InterfaceOptimizer[] islands) {
        Population[] oldIPOP = new Population[islands.length];
        Population[] newIPOP = new Population[islands.length];
        Population[] comSet;
        Population selected;

        // collect the populations
        for (int i = 0; i < islands.length; i++) {
            oldIPOP[i] = islands[i].getPopulation();
            newIPOP[i] = (Population) oldIPOP[i].clone();
        }

        // perform migration for each ipop
        for (int i = 0; i < newIPOP.length; i++) {

            // @todo: Here i could implement multiple alternative topologies
            comSet = oldIPOP;

            // todo: Here i could implement multiple selection and replacement schemes
            newIPOP[i].removeNIndividuals(comSet.length * this.n);
            for (int j = 0; j < comSet.length; j++) {
                selected = this.selection.selectFrom(comSet[j], this.n);
                newIPOP[i].addPopulation((Population) selected.clone());
            }
        }

        // set the population back to the islands
        for (int i = 0; i < islands.length; i++) {
            islands[i].setPopulation(newIPOP[i]);
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is multi-objective migration scheme.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "MOBestMigration";
    }

    /**
     * This method allows you to set/get the selection method for migration.
     *
     * @return The selection method
     */
    public InterfaceSelection getSelection() {
        return this.selection;
    }

    public void setSelection(InterfaceSelection b) {
        this.selection = b;
    }

    public String selectionTipText() {
        return "Choose the selection method for migration.";
    }

    /**
     * This method allows you to set/get the number of individuals
     * to migrate per migration event.
     *
     * @return The current number of individuals to migrate
     */
    public int getN() {
        return this.n;
    }

    public void setN(int b) {
        if (b < 1) {
            b = 1;
        }
        this.n = b;
    }

    public String nTipText() {
        return "The number of individuals to migrate per migration event.";
    }
}
