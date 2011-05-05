package eva2;

import java.io.InputStream;
import java.util.Properties;

import eva2.tools.BasicResourceLoader;

/**
 * Main product and version information strings.
 * 
 * ---- Changelog
 * 2.05: Added ScatterSearch (real-valued), BinaryScatterSearch, and the Bayesian Optimization Algorithm (thanks
 * 			to Alexander Seitz) to the base package. Added JobList and some statistical measures. Requires to put
 * 			the JSC package on the class path for Mann-Whitney test.
 * 2.046: Adaptions to the MatlabInterface: explicit data types are used now, added integer problem support.
 * 			Additional Integer operators: segment-wise mutation and crossover. Added an abstraction over individual
 * 			initialization methods. Added the ERPStarter class which is an example for running a csv-configured 
 * 			optimization. Some bug-fixes and clean-ups.
 * 2.045: Added MOOCMAES (de Paly); compatibility with Java 1.5 (Draeger); Revision of the statistics class
 * 			with improved pipelining of data (InterfaceAdditionalPopulationInformer); improved FunctionArea
 * 			concerning history, labels, legend and graph coloring; improved StringSelection; restructured
 * 			Terminators, additional ParetoMetricTerminator. Further bugfixes and clean-ups.
 * 2.043: Added proper Population equality predicate.
 * 2.042: Some bugfixes. Removing dependency on sun.beans.editors, replaced non-free jpeg-codec. There should
 * 			be no more problems on OpenJDK. Added a simple initialization range, especially for use from Matlab.
 * 			Some restructurings (RNG and Mathematics is now in eva2.tools.math). Some cleanup.
 * 2.040: Several updates: The clustering interface has been changed for easier implementation of adaptive clustering
 * 			methods. The GradientDescentAlgorithm has been updated and some benchmark problems been made derivable.
 * 			The ClusterBasedNiching algorithm has been slightly restructured updated according to the new clustering.
 * 			An additional clustering method is included: nearest-best clustering with dynamic adaption of niche radius.
 * 			Some changes to (text) statistics: they are now printed regarding the full solution set instead of the
 * 			last population. The Population field "size" is now termed "targetSize" to avoid misunderstandigs. Populations
 * 			can be initialized using a Random Latin Hypercube sampling. Some basic console options are recognized:
 * 			EvA2 can be started without splash screen and even without GUI. If configuration file is given which was 
 * 			earlier saved from the GUI, the thus defined optimization run is then processed automatically by EvA2. 
 * 2.036: New graph selection mode for statistic plots: every property may be selected independently.
 * 			A simple plot legend is produced in the graph window, which can be deactivated.
 * 2.035: Reactivated some of the model-based functionality within the GO framework. Minor bugfixes.
 * 2.034: Adding a generic parameter control method for optimizers, currently used by PSO to adapt inertness depending
 * 			on EvaluationTerminator or GenerationTerminator instances defining the number of function evaluations.
 * 			The new package is eva2.server.go.operators.paramcontrol.
 * 			A Population may now be ordered by a specific fitness criterion, employed, e.g., by Nelder-Mead-Simplex.
 * 2.033: There was an interesting problem with the Matlab-Interface, which just hung up after extensive optimization
 * 			loops, yet only if Matlab was started without X-forwarding (which is necessary for qsub, e.g.).
 * 			Debugging was tedious, since the debugging using System.out. itself caused threading deadlocks. The 
 * 			problem showed up to be with System.out itself. Whatever Matlab does with that stream, it does it differently
 * 			depending on the X-forwarding option. More specifically, the text written to System.out when X-forwarding
 * 			is not available seems to never show up (as opposed to being printed to the console when X-forwarding is on)
 * 			and silently fill up the JVM-memory. I havent the faintest idea why there havnt been OutOfMemory exceptions
 * 			earlier or whether and how the deadlocks have to do with it. 
 * 			The ingenious solution was: dont print anything to System.out, which is now done at verbosity 0.
 * 2.032: Some cosmetics, e.g. to AbstractEAIndividualComparator and older MOCCO classes. 
 * 2.031: Some updates to the OptimizerFactory. Review of the MatlabInterface with adding an own options structure.
 * 			Better access to the EvAClient, which now may have a RemoteStateListener added monitoring the optimization run.
 * 2.030: Added an EnumEditor to access enums easily through the GUI, which will replace SelectedTags sometimes.
 * 			IPOP-ES and RankMuCMA mutator have been added lately (wow!).
 * 			Cleaned up the IndividualInterface and reduced the usage of InterfaceESIndividual. This
 * 			means that, e.g., that DE and PSO now also work on GAIndividualDoubleData. Because this 
 * 			requires much time for transcoding, however, this is not useful by itself. Yet it could be
 * 			interesting for combined individuals composed of two data types. 
 * 			Cleaned up MutateXXDefault to a single MutateDefault, too. DE may now do range checking.
 * 			The "Description" button has made space for a "Show Solution" button. The Rank-Mu-CMA was improved
 * 			to use a CMAParameterSet which is associated with populations and not static any more.
 * 			Included Nelder-Mead-Simplex and CMA-ES as post processing methods.
 * 2.029: Tuned the 2d-graphs which now paints quicker and changes size depending on the
 * 			surrounding plot window. Added a preloader-thread to accelerate the GUI at starting time.
 * 2.028: Tuned the Population to sort only when necessary on calls to getBestN... Added StatisticsDummy.
 * 			Slightly tuned SimpleProblemWrapper to call initProblem of simple problems if available.
 * 2.027: Renamed SetData and SetDataLamarckian from individual data type interfaces to SetGenotype and SetPhenotype.
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
	public static final String productName = "EvA2";
	public static final String productLongName = "Evolutionary Algorithms Workbench 2";
	// public static final String fullVersion = "2.043"; // moved to EvA2.props!
	public static final String url = "http://www.ra.cs.uni-tuebingen.de/software/EvA2";

	public static final String propertyFile = "resources/EvA2.props";
	public static final String LGPLFile = "lgpl-3.0.txt";
	public static final String GPLFile= "gpl-3.0.txt";
	public static final String iconLocation = "resources/images/icon4.gif";

	public static final String splashLocation = "resources/images/splashScreen2.png";
	public static final String infoTitle = productName+" Information";
	public static final String copyrightYear = "2010";

	////////////// Property handling...
	
	private static Properties EVA_PROPERTIES;
	static {
		try {
			EVA_PROPERTIES = BasicResourceLoader.readProperties(EvAInfo.propertyFile);
		} catch (Exception ex) {
			System.err.println(resourceNotFoundErrorMessage(EvAInfo.propertyFile));
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		InputStream istr = BasicResourceLoader.instance().getStreamFromResourceLocation(EvAInfo.iconLocation);
		if (istr==null) {
			throw new RuntimeException(resourceNotFoundErrorMessage(EvAInfo.iconLocation) + " (EvAInfo.static)");
//			System.exit(2); // dont be as harsh right here - there will be plenty of exceptions later in the bad case...
		}
	}
	
    /**
     * An eloquent error message in case a resource was not found - which was
     * expected in the EvA2 resource directory.
     * @param resourceName
     * @return
     */
    public static String resourceNotFoundErrorMessage(String resourceName) {
    	String cp = System.getProperty("java.class.path");
    	return "Could not find " + resourceName +
		"\nPlease make resources folder available on the class path! " +
		"Current class path is: " + cp + 
		"\nYou may copy it there or add the parent folder of resources/ to the class path.";
    }
    
	public static String getProperty(String key) {
		String myVal = EVA_PROPERTIES.getProperty(key);
		return myVal;
	}

	public static Properties getProperties() {
		return EVA_PROPERTIES;
	}
	
	private static void setProperty(String key, String value) {
		EVA_PROPERTIES.setProperty(key, value);
	}

	public static String getVersion() {
		String version = getProperty("EvA2Version");
		if (version==null) System.err.println("ERROR, missing property EvA2Version!");
		return version;
	}

	public static String propDefaultModule() {
		return getProperty("DefaultModule");
	}

	public static String propShowModules() {
		return getProperty("ShowModules");
	}
}
