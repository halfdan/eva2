package javaeva.server.go.problems;

import javaeva.server.go.PopulationInterface;

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
