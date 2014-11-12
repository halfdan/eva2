package eva2.tools;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class EVAERROR {
    private static final Logger LOGGER = Logger.getLogger(EVAERROR.class.getName());
    private static transient HashMap<String, Boolean> errorMap = null;

    /**
     *
     */
    public static void EXIT(String message) {
        LOGGER.log(Level.SEVERE, message);
        System.exit(-1);
    }

    /**
     *
     */
    public static void WARNING(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    /**
     * Send a message only once to System.err. Once means that the msg is stored for the lifetime of
     * the VM or until clearMsgCache() is called.
     *
     * @param message
     */
    public static void errorMsgOnce(String message) {
        if (errorMap == null) {
            errorMap = new HashMap<>();
        }

        if (!errorMap.containsKey(message)) {
            LOGGER.log(Level.SEVERE, message);
            errorMap.put(message, true);
        }
    }

    /**
     * Clear the error message cache, so that any error messages are displayed at least once.
     */
    public static void clearMsgCache() {
        if (errorMap != null) {
            errorMap.clear();
        }
    }
}
