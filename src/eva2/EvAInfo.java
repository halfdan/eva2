package eva2;

import eva2.tools.BasicResourceLoader;
import java.io.InputStream;
import java.util.Properties;

/** 
 * @author mkron
 *
 */
public class EvAInfo {
	public static final String productName = "EvA2";
	public static final String productLongName = "Evolutionary Algorithms Workbench 2";
	// public static final String fullVersion = "2.043"; // moved to EvA2.props!
	public static final String url = "http://www.cogsys.cs.uni-tuebingen.de/software/EvA2";

	public static final String propertyFile = "META-INF/EvA2.props";
	public static final String LGPLFile = "lgpl-3.0.txt";
	public static final String GPLFile= "gpl-3.0.txt";
	public static final String iconLocation = "images/icon4.gif";

	public static final String splashLocation = "images/EvASplashScreen.png";
	public static final String infoTitle = productName+" Information";
	public static final String copyrightYear = "2010-2012";
        
        public static final String defaultLogger = "EvA2";

	////////////// Property handling...
	
	private static Properties evaProperties;
	static {
		try {
			evaProperties = BasicResourceLoader.readProperties(EvAInfo.propertyFile);
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
		String myVal = evaProperties.getProperty(key);
		return myVal;
	}

	public static Properties getProperties() {
		return evaProperties;
	}
	
	private static void setProperty(String key, String value) {
		evaProperties.setProperty(key, value);
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
