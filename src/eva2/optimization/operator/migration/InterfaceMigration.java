package eva2.optimization.operator.migration;

import eva2.optimization.strategies.InterfaceOptimizer;

/** The migration methods give the migration topology
 * (currently all migration methods are typically fully
 * connected) and give the selection criteria. While
 * SOXMigration stands for uni-criterial migration
 * MOXMigration typically stands for multi-criterial migration.
 * For multi-criterial optimization the migration scheme
 * also may give the subdividing scheme.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.09.2004
 * Time: 14:45:15
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceMigration {

    /** The ever present clone method
     */
    public Object clone();

    /** Typically i'll need some initialization method for
     * every bit of code i write....
     */
    public void initMigration(InterfaceOptimizer[] islands);

    /** The migrate method can be called asychnronously or
     * sychronously. Basically it allows migration of individuals
     * between multiple EA islands and since there are so many
     * different possible strategies i've introduced this
     * interface which is mostlikely subject to numerous changes..
     * Note: Since i use the RMIRemoteThreadProxy everything done
     * to the islands will use the serialization method, so if
     * you call getPopulation() on an island it is not a reference
     * to the population but a serialized copy of the population!!
     */
    public void migrate(InterfaceOptimizer[] islands);
}
