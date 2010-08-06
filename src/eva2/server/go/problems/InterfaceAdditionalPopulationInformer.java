package eva2.server.go.problems;

import eva2.server.go.PopulationInterface;

/**
 * An interface for an instance providing specialized statistical data on an optimization run.
 * This may be statistics depending on specific optimization methods or a specific application
 * problem.
 * For every additional field, a field name (header) and a value at every iteration must be provided.
 * 
 * @author mkron
 *
 */
public interface InterfaceAdditionalPopulationInformer {
	/** 
	 * This method returns the header for additional statistical data.
	 * @param pop   The population of the optimization run.
	 * @return String
	 */
	public String[] getAdditionalFileStringHeader(PopulationInterface pop);

	/**
	 * Optionally return informative descriptions of the data fields.
	 * 
	 * @param pop
	 * @return
	 */
	public String[] getAdditionalFileStringInfo(PopulationInterface pop);
	
	/** 
	 * This method returns additional statistical data.
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public Object[] getAdditionalFileStringValue(PopulationInterface pop);
}
