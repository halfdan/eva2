package eva2.problems;

import eva2.optimization.population.PopulationInterface;

/**
 * An interface for an instance providing specialized statistical data on an optimization run.
 * This may be statistics depending on specific optimization methods or a specific application
 * problem.
 * For every additional field, a field name (header) and a value at every iteration must be provided.
 */
public interface InterfaceAdditionalPopulationInformer {
    /**
     * This method returns the header for additional statistical data.
     *
     * @return String
     */
    String[] getAdditionalDataHeader();

    /**
     * Optionally return informative descriptions of the data fields.
     *
     * @return
     */
    String[] getAdditionalDataInfo();

    /**
     * This method returns additional statistical data.
     *
     * @param pop The population that is to be refined.
     * @return String
     */
    Object[] getAdditionalDataValue(PopulationInterface pop);
}
