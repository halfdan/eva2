package eva2.gui;

import eva2.EvAInfo;
import eva2.tools.StringTools;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main method, creates and sets up MainFrame
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * The one and only main of the client program. Possible arguments:
     * --autorun:   immediately starts the optimization (with parameters loaded
     *              from current directory if available.
     * --nosplash:  skip the splash screen.
     * --params:    PFILE: load the optimization parameter from the serialized file PFILE
     *
     * @param args command line parameters
     */
    public static void main(String[] args) {
        // Properties for Mac OS X support.
        if ((System.getProperty("mrj.version") != null)
                || (System.getProperty("os.name").toLowerCase().contains("mac"))) {
            /*
             * Note: the xDock name property must be set before parsing
             * command-line arguments! See above!
             */
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", EvAInfo.productName);
            System.setProperty("apple.awt.application.name", EvAInfo.productName);
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");

            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        /* Available command-line parameters */
        String[] keys = new String[]{
                "--help", "--autorun", "--nosplash", "--nogui", "--params", "--treeView"
        };
        /* Number of arguments per parameter */
        int[] arities = new int[]{0, 0, 0, 0, 1, 0};
        Object[] values = new Object[keys.length];

        Integer[] unknownArgs = StringTools.parseArguments(args, keys, arities, values, true);

        if (unknownArgs.length > 0) {
            LOGGER.warning("Unrecognized command line options: ");
            for (Integer unknownArg : unknownArgs) {
                System.err.println("   " + args[unknownArg]);
            }
            if (values[0] == null) {
                System.err.println("Try --help as argument.");
            }
        }

        // Set up logging
        Logger rootLogger = Logger.getLogger("eva2");
        rootLogger.setLevel(Level.INFO);
        rootLogger.setUseParentHandlers(false);

        if (values[0] != null) {
            System.out.println(usage());
        } else {
            boolean autorun = (values[1] != null);
            boolean nosplash = (values[2] != null);
            boolean nogui = (values[3] != null);
            boolean treeView = (values[5] != null);
            String paramsFile = StringTools.checkSingleStringArg(keys[4], values[4], arities[4] - 1);

            new MainFrame(paramsFile, autorun, nosplash, nogui, treeView);
        }
    }

    /**
     * This method returns a readable usage string.
     *
     * @return Returns usage message
     */
    public static String usage() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(EvAInfo.productName);
        sbuf.append(" - ");
        sbuf.append(EvAInfo.productLongName);
        sbuf.append(" - Version ");
        sbuf.append(EvAInfo.getVersion());
        sbuf.append("\n");
        sbuf.append("License: ");
        sbuf.append(EvAInfo.LGPLFile);
        sbuf.append("\n");
        sbuf.append("Homepage: ");
        sbuf.append(EvAInfo.url);
        sbuf.append("\n");
        sbuf.append("Command-line arguments:\n");
        sbuf.append("	--help: Show this text and exit\n");
        sbuf.append("	--nosplash: Deactivate splash screen\n");
        sbuf.append("	--nogui: Deactivate GUI (makes most sense with autorun and params set)\n");
        sbuf.append("	--autorun: Start an optimization immediately and exit after execution\n");
        sbuf.append("	--params PARAMFILE: Load the (serialized) parameters file on start\n");

        return sbuf.toString();
    }
}
