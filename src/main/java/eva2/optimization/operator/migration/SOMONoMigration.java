package eva2.optimization.operator.migration;

import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.util.annotation.Description;

/**
 * Implements no migration as reference.
 */
@Description("This is actually no mirgation scheme, because no individuals are exchanged.")
public class SOMONoMigration implements InterfaceMigration, java.io.Serializable {

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new SOMONoMigration();
    }

    /**
     * Typically i'll need some initialization method for
     * every bit of code i write....
     */
    @Override
    public void initializeMigration(InterfaceOptimizer[] islands) {

    }

    /**
     * The migrate method can be called asychnronously or
     * sychronously. Basically it allows migration of individuals
     * between multiple EA islands and since there are so many
     * different possible strategies i've introduced this
     * interface which is mostlikely subject to numerous changes..
     * Note: Since i use the RMIRemoteThreadProxy everything done
     * to the islands will use the serialization method, so if
     * you call getPopulation() on an island it is not a reference
     * to the population but a serialized copy of the population!!
     */
    @Override
    public void migrate(InterfaceOptimizer[] islands) {

    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "SONoMigration";
    }
}
