/**
 * 
 */
package eva2.tools.math.des;

import java.util.ArrayList;

import org.sbml.squeezer.math.DESEvent;

/**
 * This Class represents an event-driven DES
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2010-02-04
 *
 */
public interface EventDESystem extends DESystem {
	
	/**
	 * Returns an array with delays (entries >=0) for the
	 * events triggered either by the time t or by the concentrations
	 * of the species stored in Y. The new values for the species
	 * are stored in res. The positions in the array returned by this method
	 * correspond to the positions in Y/res. 
	 *
	 * @return Returns an array with delays for the change of concentration due to events
	 */
	public ArrayList<DESEvent> processEvents(double t, double Y[]);
	
	public double[] processAssignmentRules(double t, double Y[], double res[]);
	
	public double[] processAlgebraicRules(double t, double Y[], double res[]);

}
