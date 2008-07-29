package eva2;

/**
 * Main product and version information strings.
 * 
 * --- Changelog
 * 2.028: Tuned the Population to sort only when necessary on calls to getBestN... Added StatisticsDummy.
 * 			Slightly tuned SimpleProblemWrapper to call initProblem of simple problems if available.
 * 2.027: Renamed SetData and SetDataLamarckian from individual datatype interfaces to SetGenotype and SetPhenotype.
 * 			Repaired the GenericArrayEditor. Population measures can now be plotted in stats.
 * 2.026: Added DiversityTerminator and KnownOptimaTerminator, slightly changed InterfaceTerminator for these
 * 			and InterfaceStatistics to provide termination message to text window. 
 * 			Killed redundant method getGenerations() in Population. Population.getAllSolutions now returns a 
 * 			SolutionSet combining last population with a set of possibly archived solutions.
 * 			Post processing with HC may now use variable step size mutation.
 * 2.025: FunctionArea may now plot circles easily. The FLensProblemViewer seems to be cured. 
 * 2.024: Cleaned up AbstractGOParams, deactivated parent logging (saving memory)
 * 2.023: Cleaned up the PF strategy
 * 2.022: Some changes to the SimpleProblemWrapper, not of great interest. However,
 * 		simple problems may now access a plot quite easily.
 * 
 * --- End Changelog
 * 
 * @author mkron
 *
 */
public class EvAInfo {
	public static final String productName = "EvA 2";
	public static final String productLongName = "Evolutionary Algorithms Workbench 2";
	public static final String versionNum = new String ("2.028");
	public static final String url = "http://www.ra.cs.uni-tuebingen.de/software/EvA2";

	public static final String propertyFile = "resources/EvA2.props";
	public static final String licenseFile = "lgpl-3.0.txt";
	public static final String iconLocation = "resources/images/icon4.gif";

	public static final String splashLocation = "resources/images/splashScreen2.png";
	public static final String infoTitle = productName+" Information";

}
