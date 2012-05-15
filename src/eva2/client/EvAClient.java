package eva2.client;

/*
 * Title: EvA2
 * Description: The main client class of the EvA framework.
 * Copyright: Copyright (c) 2008
 * Company: University of Tuebingen, Computer
 * Architecture 
 * 
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version: $Revision: 322 $ $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007)$
 * $Author: mkron $
 */
import eva2.EvAInfo;
import eva2.gui.*;
import eva2.server.EvAServer;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.AbstractModuleAdapter;
import eva2.server.modules.GOParameters;
import eva2.server.modules.GenericModuleAdapter;
import eva2.server.modules.ModuleAdapter;
import eva2.server.stat.AbstractStatistics;
import eva2.server.stat.InterfaceStatisticsListener;
import eva2.server.stat.InterfaceStatisticsParameter;
import eva2.tools.BasicResourceLoader;
import eva2.tools.EVAERROR;
import eva2.tools.ReflectPackage;
import eva2.tools.StringTools;
import eva2.tools.jproxy.RemoteStateListener;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;


/**
 *
 */
public class EvAClient implements RemoteStateListener, Serializable {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = 8232856334379977970L;
    private final int splashScreenTime = 2500;
    private final int maxWindowMenuLength = 30;
    private boolean clientInited = false;
    private JExtDesktopPaneToolBar desktopToolBar;
    private JDesktopPane desktopPane;
    private JFrame mainFrame;
    private JPanel configurationPane;
    private JSplitPane horizontalSplit;
    private Runnable initRnbl = null;    
    
    private EvAComAdapter comAdapter;
    private transient JMenuBar menuBar;
    private transient JExtMenu menuHelp;
    private transient JExtMenu menuSelHosts;
    private transient JExtMenu menuModule;
    private transient JExtMenu menuOptions;
    private transient JProgressBar progressBar;
    
    // Option
    private ExtAction actQuit;
    
    // LogPanel
    private LoggingPanel logPanel;
    private static final Logger LOGGER = Logger.getLogger(EvAInfo.defaultLogger);
    
    // Module:
    private ExtAction actModuleLoad;

    // Hosts:    
    private ExtAction actHost;
    private ExtAction actAvailableHost;
    private ExtAction actKillHost;
    private ExtAction actKillAllHosts;
    private ModuleAdapter currentModuleAdapter = null;
    
    // About:
    private ExtAction actAbout;
    private ExtAction actLicense;
    
    //	if not null, the module is loaded automatically and no other can be selected
    private String useDefaultModule = null;	//"Genetic_Optimization";
    private boolean showLoadModules = false;
    private boolean localMode = false;
    
    // This variable says whether, if running locally, a local server should be addressed by RMI.
    // False should be preferred here to avoid overhead
    private boolean useLocalRMI = false;
    // measuring optimization runtime
    private long startTime = 0;
    // remember the module in use
    private transient String currentModule = null;
    private List<RemoteStateListener> superListenerList = null;
    private boolean withGUI = true;
    private boolean withTreeView = false;
    private EvATabbedFrameMaker frameMaker = null;
    private Window parentWindow;

    public void addRemoteStateListener(RemoteStateListener l) {
        if (superListenerList == null) {
            superListenerList = new ArrayList<RemoteStateListener>();
        }
        superListenerList.add(l);
    }

    public boolean removeRemoteStateListener(RemoteStateListener l) {
        if (superListenerList != null) {
            return superListenerList.remove(l);
        } else {
            return false;
        }
    }    

    /**
     * Constructor of GUI of EvA2. Works as client for the EvA2 server. Note
     * that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
     * to await full initialization if necessary.
     *
     */
    public EvAClient(final String hostName) {
        this(hostName, null, false, false);
    }

    /**
     * A constructor. Splash screen is optional, Gui is activated, no parent
     * window. Note that the EvAClient initialized multi-threaded for
     * efficiency. Use {@link #awaitGuiInitialized()} to await full
     * initialization if necessary.
     *
     * @see #EvAClient(String, Window, String, boolean, boolean, boolean)
     * @param hostName
     * @param paramsFile
     * @param autorun
     * @param nosplash
     */
    public EvAClient(final String hostName, final String paramsFile, boolean autorun, boolean nosplash) {
        this(hostName, null, paramsFile, null, autorun, nosplash, false, false);
    }

    /**
     * A constructor with optional spash screen. Note that the EvAClient is
     * initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
     * to await full initialization if necessary.
     *
     * @see #EvAClient(String, String, boolean, boolean)
     *
     * @param hostName
     * @param autorun
     * @param nosplash
     */
    public EvAClient(final String hostName, boolean autorun, boolean nosplash) {
        this(hostName, null, autorun, nosplash);
    }

    /**
     * A constructor with optional splash screen. Note that the EvAClient
     * initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
     * to await full initialization if necessary.
     *
     * @see #EvAClient(String, String, boolean, boolean)
     * @param hostName
     * @param paramsFile
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public EvAClient(final String hostName, String paramsFile, boolean autorun, boolean noSplash, boolean noGui, boolean withTreeView) {
        this(hostName, null, paramsFile, null, autorun, noSplash, noGui, withTreeView);
    }

    /**
     * A constructor with optional splash screen. Note that the EvAClient
     * initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
     * to await full initialization if necessary.
     *
     * @see #EvAClient(String, String, boolean, boolean)
     *
     * @param hostName
     * @param paramsFile
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public EvAClient(final String hostName, InterfaceGOParameters goParams, boolean autorun, boolean noSplash, boolean noGui) {
        this(hostName, null, null, goParams, autorun, noSplash, noGui, false);
    }

    /**
     * Do not use the tree view by default.
     *
     * @param hostName
     * @param parent
     * @param paramsFile
     * @param goParams
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public EvAClient(final String hostName, final Window parent, final String paramsFile, final InterfaceGOParameters goParams, final boolean autorun, final boolean noSplash, final boolean noGui) {
        this(hostName, parent, paramsFile, goParams, autorun, noSplash, noGui, false);
    }

    /**
     * Main constructor of the EvA2 client GUI. Works as standalone verson
     * locally or as client for the EvA2 server. GO parameters may be loaded
     * from a file (paramsFile) or given directly as a java instance. Both may
     * be null to start with standard parameters. If both are non null, the java
     * instance has the higher priority. Note that the EvAClient initialized
     * multi-threaded for efficiency. Use {@link #awaitGuiInitialized()} to
     * await full initialization if necessary.
     *
     * @param hostName
     * @param parent
     * @param paramsFile
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public EvAClient(final String hostName, final Window parent, final String paramsFile, final InterfaceGOParameters goParams, final boolean autorun, final boolean noSplash, final boolean noGui, final boolean showTreeView) {
        clientInited = false;
        final SplashScreen splashScreen = new SplashScreen(EvAInfo.splashLocation);

        // preload some classes (into system cache) in a parallel thread
        preloadClasses();
        

        withGUI = !noGui;
        withTreeView = showTreeView;
        // activate the splash screen (show later using SwingUtilities)
        if (!noSplash && withGUI) {
            try {
                splashScreen.splash();
            } catch (HeadlessException e) {
                System.err.println("Error: no xserver present - deactivating GUI.");
                withGUI = false;
            }
        }

        currentModule = null;

        comAdapter = EvAComAdapter.getInstance();

        SwingUtilities.invokeLater(initRnbl = new Runnable() {

            public void run() {
                synchronized (this) {
                    long startTime = System.currentTimeMillis();
                    init(hostName, paramsFile, goParams, parent); // this takes a bit

                    long wait = System.currentTimeMillis() - startTime;
                    if (!autorun) {
                        if (!noSplash) {
                            try {
                                // if splashScreenTime has not passed, sleep some more 
                                if (wait < splashScreenTime) {
                                    Thread.sleep(splashScreenTime - wait);
                                }
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        if (!withGUI && (currentModuleAdapter instanceof GenericModuleAdapter)) {
                            // do not save new parameters for an autorun without GUI - they werent changed manually anyways.
                            ((GenericModuleAdapter) currentModuleAdapter).getStatistics().setSaveParams(false);
                            System.out.println("Autorun without GUI - not saving statistics parameters...");
                        }
                        if (withGUI) {
                            frameMaker.onUserStart();
                        } else {
                            currentModuleAdapter.startOpt();
                        }
                    }
                    // close splash screen
                    if (!noSplash && withGUI) {
                        splashScreen.dispose();
                    }
                    clientInited = true;
                    notifyAll();
                }
            }
        });
    }

    /**
     * Since the constructor runs multi-threaded for efficiency, this method may
     * be called to await the full initialization of a client instance. As soon
     * as it returns, the EvAClient GUI is fully initialized.
     */
    public void awaitClientInitialized() {
        if (initRnbl != null) {
            synchronized (initRnbl) {
                if (!clientInited) {
                    try {
                        initRnbl.wait();
                        initRnbl = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void preloadClasses() {
        ClassPreloader cp = new ClassPreloader("eva2.server.go.strategies.InterfaceOptimizer", "eva2.server.go.problems.InterfaceOptimizationProblem", "eva2.server.go.InterfaceTerminator");
        new Thread(cp).start();
    }

    /**
     * Try to start the optimization with current parameters on the loaded
     * module. Return true on success, otherwise false.
     *
     * @return
     */
    public boolean startOptimization() {
        if (currentModuleAdapter != null) {
            currentModuleAdapter.startOpt();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a window listener to the EvA2 JFrame instance.
     *
     * @param l
     */
    public void addWindowListener(WindowListener l) {
        if (mainFrame != null) {
            mainFrame.addWindowListener(l);
        } else {
            System.err.println("Error, no JFrame existent in "
                    + this.getClass().getSimpleName());
        }
    }

    /**
     * Remove a window listener to the EvA2 JFrame instance.
     *
     * @param l
     */
    public void removeWindowListener(WindowListener l) {
        if (mainFrame != null) {
            mainFrame.removeWindowListener(l);
        } else {
            System.err.println("Error, no JFrame existent in "
                    + this.getClass().getSimpleName());
        }
    }

    /**
     * Sets given hostname and tries to load GOParamsters from given file if non
     * null.
     */
    private void init(String hostName, String paramsFile, InterfaceGOParameters goParams, final Window parent) {        
        useDefaultModule = EvAInfo.propDefaultModule();
        this.parentWindow = parent;
        
        if (useDefaultModule != null) {
            useDefaultModule = useDefaultModule.trim();
            if (useDefaultModule.length() < 1) {
                useDefaultModule = null;
            }
        }

        if (withGUI) {
            GridBagConstraints gbConstraints = new GridBagConstraints();
            
            /* Set Look and Feel */
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "Could not set Look&Feel", ex);
            }
            
            /* Create main frame with GridBagLayout */
            mainFrame = new JFrame(EvAInfo.productName);
            mainFrame.setLayout(new GridBagLayout());
            mainFrame.setMinimumSize(new Dimension(800, 600));

            /* Creates the desktopPane for Plot/Text Output */
            desktopPane = new JExtDesktopPane();            
            JEFrameRegister.getInstance().setDesktopPane(desktopPane);
            /* Creates desktopPane ToolBar to show tiling buttons */
            desktopToolBar = new JExtDesktopPaneToolBar((JExtDesktopPane) desktopPane);
            
            /* Pane to hold ToolBar + DesktopPane */
            JPanel desktopPanel = new JPanel(new GridBagLayout());
            GridBagConstraints desktopConst = new GridBagConstraints();
            desktopConst.gridx = 0;
            desktopConst.gridy = 0;
            desktopConst.fill = GridBagConstraints.HORIZONTAL;
            desktopConst.weightx = 1.0;
            desktopPanel.add(desktopToolBar, desktopConst);
            desktopConst.gridy = 1;
            desktopConst.fill = GridBagConstraints.BOTH;
            desktopConst.weighty = 1.0;
            desktopPanel.add(desktopPane, desktopConst);
                        
            BasicResourceLoader loader = BasicResourceLoader.instance();
            byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
            // TODO: use setIconImages (for better support of multiple icons when changing programs etc.)
            mainFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));

            try {
                Thread.sleep(200);
            } catch (Exception e) {
                System.out.println("Error" + e.getMessage());
            }
			
            logPanel = new LoggingPanel(LOGGER);
            logPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


            if (EvAInfo.propShowModules() != null) {
                showLoadModules = true;
            } else {
                showLoadModules = false; // may be set to true again if default module couldnt be loaded
            }
            createActions();
            
            mainFrame.setSize(800, 600);
  
            /* Create a new ConfigurationPanel (left side) */
            configurationPane = new JPanel(new GridBagLayout());
            gbConstraints.ipadx = 5;
            gbConstraints.weightx = 0.0;
            gbConstraints.weighty = 1.0;
            /* Set configurationPane at 0,1 */
            gbConstraints.gridx = 0;
            gbConstraints.gridy = 1;
            gbConstraints.fill = GridBagConstraints.VERTICAL;
            gbConstraints.gridwidth = GridBagConstraints.RELATIVE;
            gbConstraints.gridheight = GridBagConstraints.RELATIVE;
            mainFrame.add(configurationPane, gbConstraints);
            
            /* SplitPane for desktopPanel and loggingPanel */
            horizontalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
            horizontalSplit.setTopComponent(desktopPanel);
            horizontalSplit.setBottomComponent(logPanel);
            horizontalSplit.setDividerLocation(0.25);  
            horizontalSplit.setDividerSize(8);            
            horizontalSplit.setOneTouchExpandable(true);
            horizontalSplit.setResizeWeight(1.0);
            horizontalSplit.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            horizontalSplit.setContinuousLayout(true);
            /* Add horizontal split pane at 1,1 */
            gbConstraints.gridx = 1;
            gbConstraints.gridy = 1;
            gbConstraints.fill = GridBagConstraints.BOTH;
            gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gbConstraints.gridheight = GridBagConstraints.RELATIVE;
            mainFrame.add(horizontalSplit, gbConstraints);
            
            
            /* Create ProgressBar and add it to the bottom */
            progressBar = new JProgressBar();
			progressBar.setBorder(new TitledBorder("Progress"));
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
            
            gbConstraints.gridx = 0;
            gbConstraints.gridy = 2;
            gbConstraints.gridwidth = 2;
            gbConstraints.weighty = 0.0;
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.anchor = GridBagConstraints.PAGE_END;
            mainFrame.add(progressBar, gbConstraints);
            
            mainFrame.pack();
            mainFrame.setVisible(true);
        }
        if (useDefaultModule != null) {
            /* 
             * if goParams are not defined and a params file is defined
             * try to load parameters from file
             */
            if (goParams == null && (paramsFile != null && (paramsFile.length() > 0))) {
                goParams = GOParameters.getInstance(paramsFile, false);
            }
            loadModuleFromServer(useDefaultModule, goParams);//loadSpecificModule
        }

        if (withGUI) {
            buildMenu();
            mainFrame.addWindowListener(new WindowAdapter() {

                public void windowClosing(final WindowEvent event) {
                    EvAClient.this.close();
                }
            });
        }

        if (comAdapter != null) {
            if (hostName != null) {
                selectHost(hostName);
            }
            comAdapter.setLogPanel(logPanel);
            LOGGER.log(Level.INFO, "Selected Host: {0}", comAdapter.getHostName());
        }

        if (withGUI) {
            LOGGER.log(Level.INFO, "Working directory is: {0}", System.getProperty("user.dir"));
            LOGGER.log(Level.INFO, "Class path is: {0}", System.getProperty("java.class.path", "."));

            if (!(configurationPane.isVisible())) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                //evaFrame.setLocation((int) ((screenSize.width - evaFrame.getWidth()) / 2), (int) ((screenSize.height - evaFrame.getHeight()) / 2.5));
                configurationPane.setVisible(true);
                
            }
            
            if (!(mainFrame.isVisible())) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                mainFrame.setLocation((int) ((screenSize.width - mainFrame.getWidth()) / 2), (int) ((screenSize.height - mainFrame.getHeight()) / 2.5));
                mainFrame.pack();
                mainFrame.setSize(screenSize);
                mainFrame.setVisible(true);
                mainFrame.setVisible(true);
            }
            
            // if this message is omitted, the stupid scroll pane runs to
            // the end of the last line which is ugly for a long class path
            LOGGER.info("EvA2 ready");
        }
    }
    
    /**
     * Closes EvA2 workbench. Will not kill the JVM iff
     * the MATLAB environment variable has been set.
     */
    public void close() {
        LOGGER.info("Closing EvA2 Client. Bye!");
        Set<String> keys = System.getenv().keySet();
        if (keys.contains("MATLAB")) {
            LOGGER.info("EvA2 workbench has been started from Matlab: not killing JVM");
        } else {
            if (parentWindow == null) {
                System.exit(1);
            }
        }
    }

    /**
     * Refresh the parameter panels (if settings have been changed outside of
     * the GUI which should be updated in the GUI.
     *
     */
    public void refreshMainPanels() {
        frameMaker.refreshPanels();
    }

    /**
     * The one and only main of the client program. Possible arguments:
     * --autorun immediately starts the optimization (with parameters loaded
     * from current directory if available. --hostname HOST: sets the hostname
     * for the EvAClient to HOST --nosplash: skip the splash screen. --params
     * PFILE: load the optimization parameter from the serialized file PFILE
     *
     * @param args command line parameters
     */
    public static void main(String[] args) {
    	/*============================COPIED FROM SYSBIO==============================*/
    	// Properties for Mac OS X support.
    	if ((System.getProperty("mrj.version") != null)
    		|| (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1)) {
    			/* 
    			 * Note: the xDock name property must be set before parsing 
    			 * command-line arguments! See above!
    			 */
    			System.setProperty("com.apple.mrj.application.apple.menu.about.name", EvAInfo.productName);

    			System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
    			System.setProperty("apple.laf.useScreenMenuBar", "true");
    			System.setProperty("com.apple.macos.smallTabs", "true");
    			System.setProperty("com.apple.macos.useScreenMenuBar", "true");

    			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
    			System.setProperty("com.apple.mrj.application.live-resize", "true");
    		}
    	/*==========================================================================*/
    	
    	
        /* Available command-line parameters */
        String[] keys = new String[]{
            "--help", "--autorun", "--nosplash", "--nogui", 
            "--remotehost", "--params", "--treeView"
        };
        /* Number of arguments per parameter */
        int[] arities = new int[]{0, 0, 0, 0, 1, 1, 0};
        Object[] values = new Object[keys.length];

        Integer[] unknownArgs = StringTools.parseArguments(args, keys, arities, values, true);

        if (unknownArgs.length > 0) {
            System.err.println("Unrecognized command line options: ");
            for (int i = 0; i < unknownArgs.length; i++) {
                System.err.println("   " + args[unknownArgs[i]]);
            }
            if (values[0] == null) {
                System.err.println("Try --help as argument.");
            }
        }

        if (values[0] != null) {
            System.out.println(usage());
        } else {
            boolean autorun = (values[1] != null);
            boolean nosplash = (values[2] != null);
            boolean nogui = (values[3] != null);
            boolean treeView = (values[6] != null);
            String hostName = StringTools.checkSingleStringArg(keys[4], values[4], arities[4] - 1);
            String paramsFile = StringTools.checkSingleStringArg(keys[5], values[5], arities[5] - 1);

            new EvAClient(hostName, paramsFile, autorun, nosplash, nogui, treeView);
        }
    }

    /**
     * Initialize the client GUI with given parameters and set listeners. This
     * will return as soon as the GUI is visible and ready.
     *
     * @param goParams	optimization parameters
     * @param statisticsListener	statistics listener receiving data during
     * optimization
     * @param windowListener	additional window listener for client frame
     */
    public static EvAClient initClientGUI(GOParameters goParams,
            InterfaceStatisticsListener statisticsListener,
            WindowListener windowListener, final Window parent) {
        EvAClient evaClient;

        evaClient = new EvAClient(null, parent, null, goParams,
                false, true, false, false); // initializes GUI in the background
        // important: wait for GUI initialization before accessing any internal
        // settings:
        evaClient.awaitClientInitialized(); // this returns as soon as the
        // GUI is ready
        evaClient.addWindowListener(windowListener);
        // modify initial settings and activate output of all data:
        evaClient.getStatistics().getStatisticsParameter().setOutputAllFieldsAsText(true); 
        // add a data listener instance:
        evaClient.getStatistics().addDataListener(statisticsListener);

        // GUI update due to the changes made through the API
        evaClient.refreshMainPanels();


        return evaClient;
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
        sbuf.append("	--remotehost HOSTNAME: Try to load a module from a (remote) server\n");

        return sbuf.toString();
    }

    /**
     *
     */
    private void createActions() {
        actModuleLoad = new ExtAction("&Load", "Load Module",
                KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                loadModuleFromServer(null, null);
            }
        };

        actAbout = new ExtAction("&About", "Product Information",
                KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                showAboutDialog();
            }
        };
        actLicense = new ExtAction("&License", "View License",
                KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                showLicense();
            }
        };
        actHost = new ExtAction("&List of all servers", "All servers in list",
                KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                selectAvailableHost(comAdapter.getHostNameList());
            }
        };
        actAvailableHost = new ExtAction("Available &Server", "Available server",
                KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                showPleaseWaitDialog();
                new Thread() {
                    @Override
                    public void run() {
                        selectAvailableHost(comAdapter.getAvailableHostNameList());
                    }
                }.start();
            }
        };
        actKillHost = new ExtAction("&Kill server", "Kill server process on selected host",
                KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());                
                new Thread() {
                    @Override
                    public void run() {
                        selectAvailableHostToKill(comAdapter.getAvailableHostNameList());
                    }
                }.start();
                showPleaseWaitDialog();
            }
        };
        actKillAllHosts = new ExtAction("Kill &all servers", "Kill all servers",
                KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                new Thread() {
                    @Override
                    public void run() {
                        selectAllAvailableHostToKill(comAdapter.getAvailableHostNameList());
                    }
                }.start();
                showPleaseWaitDialog();
            }
        };
        
        actQuit = new ExtAction("&Quit", "Quit EvA2 workbench",
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                EvAClient.this.close();
            }
        };
    }

    /**
     * Create the main menu and add actions.
     */
    private void buildMenu() {
        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);
        menuModule = new JExtMenu("&Module");
        menuModule.add(actModuleLoad);

        menuSelHosts = new JExtMenu("&Select Hosts");
        menuSelHosts.setToolTipText("Select a host for the server application");
        menuSelHosts.add(actHost);
        menuSelHosts.add(actAvailableHost);
        menuSelHosts.addSeparator();
        menuSelHosts.add(actKillHost);
        menuSelHosts.add(actKillAllHosts);

        menuHelp = new JExtMenu("&Help");
        menuHelp.add(actAbout);
        menuHelp.add(actLicense);

        menuOptions = new JExtMenu("&Options");
        menuOptions.add(menuSelHosts);
        menuOptions.add(actQuit);
        // this is accessible if no default module is given
        if (showLoadModules) {
            menuBar.add(menuModule);
        }

        menuBar.add(menuOptions);
        menuBar.add(((JExtDesktopPane) desktopPane).getWindowMenu());
        menuBar.add(menuHelp);
    }


    /**
     *
     */
    private void loadModuleFromServer(String selectedModule, InterfaceGOParameters goParams) {
        if (comAdapter.getHostName() == null) {
            System.err.println("error in loadModuleFromServer!");
            return;
        }
        if (comAdapter.getHostName().equals("localhost")) {
            localMode = true;
            if (useLocalRMI) {
                EvAServer Server = new EvAServer(true, false);
                comAdapter.setLocalRMIServer(Server.getRMIServer());
                LOGGER.info("Local EvAServer started");
                comAdapter.setRunLocally(false); // this is not quite true but should have the desired effect
            } else {
                LOGGER.info("Working locally");
                comAdapter.setLocalRMIServer(null);
                comAdapter.setRunLocally(true);
            }
        } else {
            localMode = false;
            comAdapter.setRunLocally(false);
        }
        if (selectedModule == null) { // show a dialog and ask for a module
            String[] moduleNameList = comAdapter.getModuleNameList();
            if (moduleNameList == null) {
                JOptionPane.showMessageDialog(mainFrame, "No modules available on " + comAdapter.getHostName(), EvAInfo.infoTitle, 1);
            } else {
                String lastModule = null;
                
                try {
                    java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot();
                    lastModule = prefs.get("lastModule", null);
                } catch (SecurityException ex) {
                    LOGGER.log(Level.WARNING, "Can't write user preference.", ex);
                }
                
                if (lastModule == null) {
                    lastModule = moduleNameList[0];
                    LOGGER.log(Level.INFO, "Defaulting to module: {0}", lastModule);
                }
                
                selectedModule = (String) JOptionPane.showInputDialog(mainFrame,
                        "Which module do you want \n to load on host: " + comAdapter.getHostName() + " ?",
                        "Load optimization module on host",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        moduleNameList,
                        lastModule);
            }
        }

        if (selectedModule == null) {
            System.err.println("not loading any module");
        } else {
            try {
                java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot();
                prefs.put("lastModule", selectedModule);
            } catch (SecurityException ex) {
                LOGGER.log(Level.WARNING, "Can't write user preference.", ex);
            }

            loadSpecificModule(selectedModule, goParams);

            if (withGUI) {
                actHost.setEnabled(true);
                actAvailableHost.setEnabled(true);
            }
            LOGGER.log(Level.INFO, "Selected Module: {0}", selectedModule);
        }
    }

    /**
     * Retrieve the GOParamters of a loaded module. Return null if no module is
     * loaded.
     *
     * @return
     */
    public InterfaceGOParameters getGOParameters() {
        if (currentModuleAdapter != null) {
            if (currentModuleAdapter instanceof AbstractModuleAdapter) {
                return ((AbstractModuleAdapter) currentModuleAdapter).getGOParameters();
            }
        }
        return null;
    }

    public AbstractStatistics getStatistics() {
        return ((GenericModuleAdapter) currentModuleAdapter).getStatistics();
    }

    public InterfaceStatisticsParameter getStatsParams() {
        return ((GenericModuleAdapter) currentModuleAdapter).getStatistics().getStatisticsParameter();
    }

    /**
     * Check if there is an optimization currently running.
     *
     * @return
     */
    public boolean isOptRunning() {
        if ((currentModuleAdapter != null) && (currentModuleAdapter instanceof AbstractModuleAdapter)) {
            return ((AbstractModuleAdapter) currentModuleAdapter).isOptRunning();
        } else {
            return false;
        }
    }

    private void loadSpecificModule(String selectedModule, InterfaceGOParameters goParams) {
        ModuleAdapter newModuleAdapter = null;
        //
        try {
            newModuleAdapter = comAdapter.getModuleAdapter(selectedModule, goParams, withGUI ? null : "EvA2");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading module.", e);
            EVAERROR.EXIT("Error while m_ComAdapter.GetModuleAdapter Host: " + e.getMessage());
        }
        if (newModuleAdapter == null) {
            // When launching a Java Web Start application, baseDir will always be null!
            URL baseDir = getClass().getClassLoader().getResource("");
            String cp = System.getProperty("java.class.path", ".");
            String dir = (baseDir == null) ? System.getProperty("user.dir") : baseDir.getPath();
            // System.err.println("Working dir: " + dir);
            /*if (baseDir == null) {
                throw new RuntimeException("Cannot launch EvA2 due to an access restriction. If you are using Java Web Start, please download the application and try again.");
            }*/
            if (!cp.contains(dir)) {
                // this was added due to matlab not adding base dir to base path...
                System.err.println("classpath does not contain base directory!");
                System.err.println("adding base dir and trying again...");
                System.setProperty("java.class.path", cp + System.getProperty("path.separator") + dir);
                ReflectPackage.resetDynCP();
                comAdapter.updateLocalMainAdapter();
                loadSpecificModule(selectedModule, goParams); // end recursive call! handle with care!
                return;
            }
            showLoadModules = true;
        } else {
            newModuleAdapter.setConnection(!localMode);
            if (comAdapter.isRunLocally()) {
                // TODO in rmi-mode this doesnt work yet! meaning e.g. that theres no content in the info log
                newModuleAdapter.addRemoteStateListener((RemoteStateListener) this);
            }
            try {
                if (withGUI) {
                    // this (or rather: EvAModuleButtonPanelMaker) is where the start button etc come from!
                    frameMaker = newModuleAdapter.getModuleFrame();

                    /* This is the left TabPane on the main frame */
                    JPanel moduleContainer = frameMaker.makePanel(); 

                    boolean wasVisible = configurationPane.isVisible();
                    configurationPane.setVisible(false);
                    configurationPane.removeAll();

                    GridBagConstraints gbConstraints = new GridBagConstraints();
                    
                    /* ToDo: Find a way to properly add the TreeView to the GOPanel */
                    if (withTreeView && (newModuleAdapter instanceof AbstractModuleAdapter)) {
                        JComponent tree = null;
                        tree = getEvATreeView(frameMaker.getGOPanel(), "GOParameters", ((AbstractModuleAdapter) newModuleAdapter).getGOParameters());
                        gbConstraints.gridx = 0;
                        gbConstraints.gridy = 0;
                        gbConstraints.fill = GridBagConstraints.BOTH;
                        gbConstraints.weightx = 1.0;
                        gbConstraints.weighty = 1.0;
                        configurationPane.add(tree, gbConstraints);
                    }
                    
                    
                    gbConstraints.weightx = 1.0;
                    gbConstraints.weighty = 0.0;
                    gbConstraints.gridx = 0;
                    gbConstraints.gridy = 0;
                    gbConstraints.gridwidth = 2;
                    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
                    gbConstraints.anchor = GridBagConstraints.PAGE_START;
                    mainFrame.add(frameMaker.getToolBar(), gbConstraints);
                    
                    GridBagConstraints gbConstraints2 = new GridBagConstraints();
                    gbConstraints2.gridx = 0;
                    gbConstraints2.gridy = 0;                    
                    gbConstraints2.fill = GridBagConstraints.VERTICAL;                    
                    //gbConstraints2.gridheight = GridBagConstraints.REMAINDER;
                    gbConstraints2.weighty = 1.0;                    
                    configurationPane.add(moduleContainer, gbConstraints2);
                    configurationPane.validate();
                }

                currentModule = selectedModule;
            } catch (Exception e) {
                currentModule = null;
                LOGGER.log(Level.SEVERE, "Error while newModulAdapter.getModulFrame(): " + e.getMessage(), e);
                EVAERROR.EXIT("Error while newModulAdapter.getModulFrame(): " + e.getMessage());
            }
//			try { TODO whats this?
//				newModuleAdapter.setConnection(true);
//			} catch (Exception e) {
//				e.printStackTrace();
//				m_LogPanel.logMessage("Error while m_ComAdapter.AddRMIPlotListener Host: " + e.getMessage());
//				EVAERROR.EXIT("Error while m_ComAdapter.AddRMIPlotListener: " + e.getMessage());
//			}
            // set mode (rmi or not)

            // ModuladapterListe adden
//			m_ModuleAdapterList.add(newModuleAdapter);
            currentModuleAdapter = newModuleAdapter;
        }
    }

    /**
     * Create a tree view of an object based on EvATreeNode. It is encapsulated
     * in a JScrollPane.
     *
     * @see EvATreeNode
     * @param title
     * @param object
     * @return
     */
    public JComponent getEvATreeView(JParaPanel goPanel, String title, Object object) {
        EvATreeNode root = new EvATreeNode(title, object); // the root of the tree
        JTree jtree = new JTree(root);
        JScrollPane treeView = new JScrollPane(jtree);

        EvATreeSelectionListener treeListener = new EvATreeSelectionListener(root, goPanel.getEditor(), jtree);
        // hooks itself up as the tree listener. It reacts both to changes in the selection
        // state of the tree (to update the parameter panel) and to changes in the 
        // parameters to update the tree
        return treeView;
    }

    /**
     *
     */
    private void selectAvailableHost(String[] hostNames) {
        if (hostNames == null || hostNames.length == 0) {
            showNoHostFoundDialog();
        } else {
            String hostName = (String) JOptionPane.showInputDialog(configurationPane,
                    "Which active host do you want to connect to?", "Host", JOptionPane.QUESTION_MESSAGE, null,
                    hostNames, comAdapter.getHostName());
            if (hostName != null) {
                selectHost(hostName);
            }
        }
    }

    private void selectHost(String hostName) {
        comAdapter.setHostName(hostName);
        LOGGER.info("Selected Host: " + hostName);
        if (currentModule != null) {
            LOGGER.info("Reloading module from server...");
            loadModuleFromServer(currentModule, null);
        }
    }

    private void showPleaseWaitDialog() {
        JOptionPane.showMessageDialog(configurationPane, "Please wait one moment.", EvAInfo.infoTitle, 1);
    }

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(mainFrame);
        aboutDialog.setLocationRelativeTo(mainFrame);
        aboutDialog.setVisible(true);
        aboutDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    private void showLicense() {
        HtmlDemo lgpl = new HtmlDemo(EvAInfo.LGPLFile);
        HtmlDemo gpl = new HtmlDemo(EvAInfo.GPLFile);
        gpl.show();
        lgpl.show();
    }

    private void showNoHostFoundDialog() {
        JOptionPane.showMessageDialog(configurationPane, "No host with running EVASERVER found. Please start one or \nadd the correct address to the properties list.", EvAInfo.infoTitle, 1);
    }

    private void selectAvailableHostToKill(String[] HostNames) {
        if (HostNames == null || HostNames.length == 0) {
            showNoHostFoundDialog();
            return;
        }
        String HostName = (String) JOptionPane.showInputDialog(configurationPane,
                "Which server do you want to be killed ?", "Host", JOptionPane.QUESTION_MESSAGE, null,
                HostNames, comAdapter.getHostName());
        if (HostName == null) {
            return;
        }
        LOGGER.info("Kill host process on = " + HostName);
        comAdapter.killServer(HostName);
//		m_LogPanel.statusMessage("");
    }

    private void selectAllAvailableHostToKill(String[] hostNames) {
        System.out.println("SelectAllAvailableHostToKill");
        if (hostNames == null || hostNames.length == 0) {
            System.out.println("no host is running");
            return;
        }
        comAdapter.killAllServers();
    }

    public void performedRestart(String infoString) {
        if (superListenerList != null) {
            for (RemoteStateListener l : superListenerList) {
                l.performedRestart(infoString);
            }
        }
        LOGGER.log(Level.INFO, "Restarted processing {0}", infoString);
        startTime = System.currentTimeMillis();
    }

    public void performedStart(String infoString) {
        if (superListenerList != null) {
            for (RemoteStateListener l : superListenerList) {
                l.performedStart(infoString);
            }
        }
        LOGGER.log(Level.INFO, "Started processing {0}", infoString);
        startTime = System.currentTimeMillis();
    }

    public void performedStop() {
        if (superListenerList != null) {
            for (RemoteStateListener l : superListenerList) {
                l.performedStop();
            }
        }
        long t = (System.currentTimeMillis() - startTime);
        LOGGER.info(String.format("Stopped after %1$d.%2$tL s", (t / 1000), (t % 1000)));
        if (!withGUI) {
            System.exit(0);
        }
    }

    /**
     * When the worker needs to update the GUI we do so by queuing a Runnable
     * for the event dispatching thread with SwingUtilities.invokeLater(). In
     * this case we're just changing the progress bars value.
     */
    public void updateProgress(final int percent, String msg) {
        if (superListenerList != null) {
            for (RemoteStateListener l : superListenerList) {
                l.updateProgress(percent, msg);
            }
        }
        if (msg != null) {
            LOGGER.info(msg);
        }
        if (this.progressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {

                public void run() {
                    progressBar.setValue(percent);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }
}
final class SplashScreen extends Frame {

    private static final long serialVersionUID = 1281793825850423095L;
    private String imgLocation;

    public SplashScreen(String imgLoc) {
        imgLocation = imgLoc;
    }

    /**
     * Show the splash screen to the end user.
     *
     * <P>Once this method returns, the splash screen is realized, which means
     * that almost all work on the splash screen should proceed through the
     * event dispatch thread. In particular, any call to
     * <code>dispose</code> for the splash screen must be performed in the event
     * dispatch thread.
     */
    public void splash() {
        JWindow splashWindow = new JWindow(this);
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(imgLocation, true);
        ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
        JLabel splashLabel = new JLabel(ii);
        
        splashWindow.add(splashLabel);
        splashWindow.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        splashWindow.setLocation(screenSize.width / 2 - splashWindow.getSize().width / 2, screenSize.height / 2 - splashWindow.getSize().height / 2);
        splashWindow.setVisible(true);        
    }
}