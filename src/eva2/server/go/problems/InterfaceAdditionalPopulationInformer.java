package eva2.server.go.problems;

import eva2.server.go.PopulationInterface;

public interface InterfaceAdditionalPopulationInformer {
	/** This method returns the header for the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringHeader(PopulationInterface pop);

	/** This method returns the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringValue(PopulationInterface pop);
}
