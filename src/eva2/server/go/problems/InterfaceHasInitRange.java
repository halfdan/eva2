package eva2.server.go.problems;

/**
 * An interface for optimization problems having an extra initial range
 * opposed to the global search range. This makes sense mainly for double
 * and integer data types such as ESIndividuals and GIIndividuals. For binary types, 
 * the search range is defined by the bit length of the individual, respectively the 
 * dimension of the problem.
 * 
 * @author mkron
 *
 */
public interface InterfaceHasInitRange {
	/**
	 * The method should return the type expected by the individual type, e.g. double[][] or int[][].
	 * It may return null, in that case the global search range is used as initial range.
	 * 
	 * @return An initial search range or null in case it is equal to the global search range.
	 */
	public Object getInitRange();
}
