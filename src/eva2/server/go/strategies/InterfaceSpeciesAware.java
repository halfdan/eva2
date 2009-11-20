package eva2.server.go.strategies;

import eva2.server.go.populations.Population;

/**
 * An interface for optimizers which are to be notified in case of species
 * based optimization; namely merging and split events.
 * 
 * @author mkron
 *
 */
public interface InterfaceSpeciesAware {
	// these can be used to tag a population as explorer or local search population.
	public final static String populationTagKey="specAwarePopulationTag";
	public final static Integer explorerPopTag=23; 
	public final static Integer localPopTag=42; 
	
	/**
	 * Two species have been merged to the first one.
	 * @param p1
	 * @param p2
	 */
	public void mergeToFirstPopulationEvent(Population p1, Population p2);
	
	/**
	 * Notify that a split has occurred separating p2 from p1.
	 * 
	 * @param p1
	 * @param p2
	 */
	public void splitFromFirst(Population p1, Population p2);
}
