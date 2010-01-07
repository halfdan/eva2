package eva2.tools;

/**
 * Dummy class replacing the log4j Category because log4j couldnt be included in a clean
 * way and seemingly wasnt used in the main classes anyways.
 * 
 * @author mkron, mpaly
 *
 */
public class DummyCategory {
	static DummyCategory dummy = new DummyCategory();
	static boolean bDebugEnabled = false;
	
	public void error() {
		System.err.println("Error");
	}
	
	public void error(String msg) {
		System.err.println(msg);
	}

	public static DummyCategory getInstance(String str) {
		return dummy;
	}
	
    public boolean isDebugEnabled() {
    	return bDebugEnabled;
    }
    
    public void debug(String str) {
    	System.err.println(str);
    }
    
    public void info(String str) {
    	System.err.println(str);
    }
    
    public void warn(String str) {
    	System.err.println(str);
    }
}