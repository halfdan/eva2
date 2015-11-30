package eva2.gui;

import eva2.EvAInfo;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.OptimizationStateListener;
import eva2.optimization.modules.AbstractModuleAdapter;
import eva2.optimization.modules.GenericModuleAdapter;
import eva2.optimization.modules.ModuleAdapter;
import eva2.optimization.modules.OptimizationModuleAdapter;
import eva2.optimization.statistics.AbstractStatistics;
import eva2.optimization.statistics.InterfaceStatisticsListener;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.tools.BasicResourceLoader;
import eva2.tools.EVAERROR;
import eva2.tools.ReflectPackage;
import eva2.util.ClassPreloader;

import javax.help.HelpSet;
import javax.help.JHelpContentViewer;
import javax.help.JHelpNavigator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MainFrame extends JFrame implements OptimizationStateListener {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = 8232856334379977970L;
    private final int splashScreenTime;
    private boolean clientInitialized = false;
    private JDesktopPane desktopPane;
    private JPanel configurationPane;
    private Runnable initRunnable = null;

    private transient JProgressBar progressBar;

    // Option
    private ExtAction actPreferences;
    private ExtAction actQuit;

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    // Configurable module:
    private ModuleAdapter currentModuleAdapter = null;

    // Help:
    private ExtAction actHelp;
    private ExtAction actAbout;
    private ExtAction actLicense;

    //	if not null, the module is loaded automatically and no other can be selected
    private String useDefaultModule = null;    //"Genetic_Optimization";


    // measuring optimization runtime
    private long startTime = 0;
    private boolean withGUI = true;
    private boolean withTreeView = false;
    private EvATabbedFrameMaker frameMaker = null;
    private Window parentWindow;

    private java.util.List<OptimizationStateListener> superListenerList = null;


    public void addOptimizationStateListener(OptimizationStateListener l) {
        if (superListenerList == null) {
            superListenerList = new ArrayList<>();
        }
        superListenerList.add(l);
    }

    public boolean removeOptimizationStateListener(OptimizationStateListener l) {
        return superListenerList != null && superListenerList.remove(l);
    }

    /**
     * Constructor of GUI of EvA2. Works as client for the EvA2 server. Note
     * that the Main initialized multi-threaded for efficiency. Use {@link #awaitClientInitialized()} }
     * to await full initialization if necessary.
     */
    public MainFrame() {
        this(null, false, false);
    }

    /**
     * A constructor. Splash screen is optional, Gui is activated, no parent
     * window. Note that the Main initialized multi-threaded for
     * efficiency. Use {@link #awaitClientInitialized()} to await full
     * initialization if necessary.
     *
     * @param paramsFile
     * @param autorun
     * @param nosplash
     */
    public MainFrame(final String paramsFile, boolean autorun, boolean nosplash) {
        this(null, paramsFile, null, autorun, nosplash, false, false);
    }

    /**
     * A constructor with optional spash screen. Note that the Main is
     * initialized multi-threaded for efficiency. Use {@link #awaitClientInitialized()}
     * to await full initialization if necessary.
     *
     * @param autorun
     * @param nosplash
     * @see #MainFrame(String, boolean, boolean)
     */
    public MainFrame(boolean autorun, boolean nosplash) {
        this(null, autorun, nosplash);
    }

    /**
     * A constructor with optional splash screen. Note that the Main
     * initialized multi-threaded for efficiency. Use {@link #awaitClientInitialized()}
     * to await full initialization if necessary.
     *
     * @param paramsFile
     * @param autorun
     * @param noSplash
     * @param noGui
     * @see #MainFrame(boolean, boolean)
     */
    public MainFrame(String paramsFile, boolean autorun, boolean noSplash, boolean noGui, boolean withTreeView) {
        this(null, paramsFile, null, autorun, noSplash, noGui, withTreeView);
    }

    /**
     * A constructor with optional splash screen. Note that the Main
     * initialized multi-threaded for efficiency. Use {@link #awaitClientInitialized()}
     * to await full initialization if necessary.
     *
     * @param optimizationParameters
     * @param autorun
     * @param noSplash
     * @param noGui
     * @see #MainFrame(String, boolean, boolean)
     */
    public MainFrame(InterfaceOptimizationParameters optimizationParameters, boolean autorun, boolean noSplash, boolean noGui) {
        this(null, null, optimizationParameters, autorun, noSplash, noGui, false);
    }

    /**
     * Do not use the tree view by default.
     *
     * @param parent
     * @param paramsFile
     * @param goParams
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public MainFrame(final Window parent, final String paramsFile, final InterfaceOptimizationParameters goParams, final boolean autorun, final boolean noSplash, final boolean noGui) {
        this(parent, paramsFile, goParams, autorun, noSplash, noGui, false);
    }

    /**
     * Main constructor of the EvA2 client GUI. Works as standalone version
     * locally or as client for the EvA2 server. GO parameters may be loaded
     * from a file (paramsFile) or given directly as a java instance. Both may
     * be null to start with standard parameters. If both are non null, the java
     * instance has the higher priority. Note that the Main initialized
     * multi-threaded for efficiency. Use {@link #awaitClientInitialized()} to
     * await full initialization if necessary.
     *
     * @param parent
     * @param paramsFile
     * @param autorun
     * @param noSplash
     * @param noGui
     */
    public MainFrame(final Window parent, final String paramsFile, final InterfaceOptimizationParameters optimizationParameters, final boolean autorun, final boolean noSplash, final boolean noGui, final boolean showTreeView) {
        clientInitialized = false;
        final eva2.gui.SplashScreen splashScreen = new eva2.gui.SplashScreen(EvAInfo.splashLocation);

        // preload some classes (into system cache) in a parallel thread
        preloadClasses();

        withGUI = !noGui;
        withTreeView = showTreeView;
        // activate the splash screen (show later using SwingUtilities)
        if (!noSplash && withGUI) {
            try {
                splashScreen.splash();
            } catch (HeadlessException e) {
                LOGGER.severe("Error: no xserver present - deactivating GUI.");
                withGUI = false;
            }
        }

        splashScreenTime = 2500;
        initRunnable = new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    long startTime = System.currentTimeMillis();
                    init(paramsFile, optimizationParameters, parent); // this takes a bit

                    long wait = System.currentTimeMillis() - startTime;
                    LOGGER.info("Loaded EvA2 in " + wait + "ms.");
                    System.out.print(wait);
                    if (!autorun) {
                        if (!noSplash) {
                            try {
                                // if splashScreenTime has not passed, sleep some more
                                if (wait < splashScreenTime) {
                                    Thread.sleep(splashScreenTime - wait);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    } else {
                        if (!withGUI && (currentModuleAdapter instanceof GenericModuleAdapter)) {
                            // do not save new parameters for an autorun without GUI - they weren't changed manually anyways.
                            ((GenericModuleAdapter) currentModuleAdapter).getStatistics().setSaveParams(false);
                            LOGGER.info("Autorun without GUI - not saving statistics parameters...");
                        }
                        if (withGUI) {
                            frameMaker.onUserStart();
                        } else {
                            currentModuleAdapter.startOptimization();
                        }
                    }
                    // close splash screen
                    if (!noSplash && withGUI) {
                        splashScreen.dispose();
                    }
                    clientInitialized = true;
                    notifyAll();
                }
            }
        };
        SwingUtilities.invokeLater(initRunnable);
    }

    private void initLookAndFeel() {
        // The native L&F on Linux looks horrible - don't set it.
        if (System.getProperty("os.name").equals("Linux")) {
            return;
        }
        
        // Properties for Mac OS X support.
        if ((System.getProperty("mrj.version") != null)
                || (System.getProperty("os.name").toLowerCase().contains("mac"))) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                LOGGER.log(Level.INFO, "Could not set Look&Feel", ex);
            }
        } else {
            /* Set Look and Feel */
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "Could not set Look&Feel", ex);
            }
        }
    }

    /**
     * Since the constructor runs multi-threaded for efficiency, this method may
     * be called to await the full initialization of a client instance. As soon
     * as it returns, the Main GUI is fully initialized.
     */
    public void awaitClientInitialized() {
        if (initRunnable != null) {
            synchronized (initRunnable) {
                if (!clientInitialized) {
                    try {
                        initRunnable.wait();
                        initRunnable = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void preloadClasses() {
        ClassPreloader cp = new ClassPreloader("eva2.optimization.strategies.InterfaceOptimizer", "eva2.problems.InterfaceOptimizationProblem", "eva2.optimization.operator.terminators.InterfaceTerminator");
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
            currentModuleAdapter.startOptimization();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Set UI Font for all controls.
     *
     * @param fontResource The FontUIResource for the controls
     */
    private static void setUIFont(javax.swing.plaf.FontUIResource fontResource) {
        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value != null && value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, fontResource);
            }
        }
    }

    /**
     * Tries to load OptimizationParameters from given file if not null.
     */
    private void init(String paramsFile, InterfaceOptimizationParameters optimizationParameters, final Window parent) {
        useDefaultModule = EvAInfo.propDefaultModule();
        this.parentWindow = parent;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        setUIFont(new javax.swing.plaf.FontUIResource(Font.SANS_SERIF, 0, 11));

        if (useDefaultModule != null) {
            useDefaultModule = useDefaultModule.trim();
            if (useDefaultModule.length() < 1) {
                useDefaultModule = null;
            }
        }

        if (withGUI) {
            // Initialize look and feel for EvA2
            initLookAndFeel();

            GridBagConstraints gbConstraints = new GridBagConstraints();

            /* Create main frame with GridBagLayout */
            setTitle(EvAInfo.productName);
            setLayout(new GridBagLayout());
            setMinimumSize(new Dimension(1024, 800));

            /* Creates the desktopPane for Plot/Text Output */
            desktopPane = new JExtDesktopPane();
            JEFrameRegister.getInstance().setDesktopPane(desktopPane);
            /* Creates desktopPane ToolBar to show tiling buttons */
            JExtDesktopPaneToolBar desktopToolBar = new JExtDesktopPaneToolBar((JExtDesktopPane) desktopPane);

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

            BasicResourceLoader loader = BasicResourceLoader.getInstance();
            byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
            // TODO: use setIconImages (for better support of multiple icons when changing programs etc.)
            setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));

            LoggingPanel logPanel = new LoggingPanel();
            logPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            createActions();

            setSize(1024, 800);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screenSize.width - this.getWidth()) / 2, (int) ((screenSize.height - this.getHeight()) / 2.5));

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
            add(configurationPane, gbConstraints);

            /* SplitPane for desktopPanel and loggingPanel */
            JSplitPane horizontalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
            horizontalSplit.setTopComponent(desktopPanel);
            horizontalSplit.setBottomComponent(logPanel);
            horizontalSplit.setDividerSize(8);
            horizontalSplit.setOneTouchExpandable(true);
            horizontalSplit.setResizeWeight(1.0);
            horizontalSplit.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            horizontalSplit.setContinuousLayout(true);

            horizontalSplit.setDividerLocation(0.25);
            /* Add horizontal split pane at 1,1 */
            gbConstraints.gridx = 1;
            gbConstraints.gridy = 1;
            gbConstraints.fill = GridBagConstraints.BOTH;
            gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gbConstraints.gridheight = GridBagConstraints.RELATIVE;
            add(horizontalSplit, gbConstraints);

            /* StatusBar of the main frame */
            JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JPanel statusBarControls = new JPanel();
            statusBarControls.setLayout(new BoxLayout(statusBarControls, BoxLayout.LINE_AXIS));

            statusBarControls.add(Box.createHorizontalGlue());

            /* Set default logging level to INFO */
            Logger.getLogger("eva2").setLevel(Level.INFO);
            /* Logging settings drop down */
            LoggingLevelLabel loggingOption = new LoggingLevelLabel();

            statusBarControls.add(loggingOption);

            statusBarControls.add(Box.createHorizontalStrut(5));
            statusBarControls.add(new JSeparator(JSeparator.VERTICAL));
            statusBarControls.add(Box.createHorizontalStrut(5));

            /* Create ProgressBar and add it to the status bar */
            statusBarControls.add(new JLabel("Progress"));
            statusBarControls.add(Box.createHorizontalStrut(5));

            progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            statusBarControls.add(progressBar);

            statusBarControls.add(Box.createHorizontalStrut(5));
            statusBarControls.add(new JSeparator(JSeparator.VERTICAL));
            statusBarControls.add(Box.createHorizontalStrut(5));
            /* Create ProgressBar and add it to the status bar */
            statusBarControls.add(new JLabel("CPU"));
            statusBarControls.add(Box.createHorizontalStrut(5));
            statusBarControls.add(new CPUPanel(100));

            statusBar.add(statusBarControls);

            gbConstraints.gridx = 0;
            gbConstraints.gridy = 2;
            gbConstraints.gridwidth = 2;
            gbConstraints.weighty = 0.0;
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.anchor = GridBagConstraints.PAGE_END;
            add(statusBar, gbConstraints);
        }

        if (useDefaultModule != null) {
            /*
             * if optimizationParameters are not defined and a params file is defined
             * try to load parameters from file
             */
            if (optimizationParameters == null && (paramsFile != null && (paramsFile.length() > 0))) {
                optimizationParameters = OptimizationParameters.getInstance(paramsFile, false);
            }
            loadSpecificModule(useDefaultModule, optimizationParameters);//loadSpecificModule
        }

        if (withGUI) {
            buildMenu();
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent event) {
                    int result = JOptionPane.showConfirmDialog(
                            MainFrame.this,
                            "Do you really want to exit EvA2?",
                            "Exit Application",
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        MainFrame.this.close();
                    }
                }
            });

            LOGGER.log(Level.INFO, "Working directory is: {0}", System.getProperty("user.dir"));
            LOGGER.log(Level.FINE, "Class path is: {0}", System.getProperty("java.class.path", "."));

            if (!(configurationPane.isVisible())) {
                configurationPane.setVisible(true);
            }

            setVisible(true);

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
            } else {
                this.setVisible(false);
                this.dispose();
            }
        }
    }

    /**
     * Refresh the parameter panels (if settings have been changed outside of
     * the GUI which should be updated in the GUI.
     */
    public void refreshMainPanels() {
        frameMaker.refreshPanels();
    }

    /**
     * Initialize the client GUI with given parameters and set listeners. This
     * will return as soon as the GUI is visible and ready.
     *
     * @param goParams           optimization parameters
     * @param statisticsListener statistics listener receiving data during
     *                           optimization
     * @param windowListener     additional window listener for client frame
     */
    public static MainFrame initClientGUI(OptimizationParameters goParams,
                                          InterfaceStatisticsListener statisticsListener,
                                          WindowListener windowListener, final Window parent) {
        MainFrame evaClient;

        evaClient = new MainFrame(parent, null, goParams,
                false, true, false, false); // initializes GUI in the background
        // important: wait for GUI initialization before accessing any internal
        // settings:
        evaClient.awaitClientInitialized(); // this returns as soon as the
        // GUI is ready
        evaClient.addWindowListener(windowListener);
        // modify initial settings and activate output of all data:
        evaClient.getStatistics().getStatisticsParameters().setOutputAllFieldsAsText(true);
        // add a data listener instance:
        evaClient.getStatistics().addDataListener(statisticsListener);

        // GUI update due to the changes made through the API
        evaClient.refreshMainPanels();

        return evaClient;
    }

    /**
     *
     */
    private void createActions() {

        actAbout = new ExtAction("&About", "Product Information") {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                showAboutDialog();
            }
        };
        actLicense = new ExtAction("&License", "View License") {

            @Override
            public void actionPerformed(final ActionEvent event) {
                LOGGER.info(event.getActionCommand());
                showLicense();
            }
        };

        actQuit = new ExtAction("&Quit", "Quit EvA2 workbench",
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                int result = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Do you really want to exit EvA2?",
                        "Exit Application",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    MainFrame.this.close();
                }
            }
        };

        actPreferences = new ExtAction("&Preferences", "Show preferences dialog",
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                // ToDo
            }
        };

        actHelp = new ExtAction("&Help", "Show help contents",
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                // ToDo
                String helpHS = "EvA2Help/EvA2Help.hs";
                ClassLoader cl = MainFrame.class.getClassLoader();
                JHelpContentViewer helpPane;
                try {
                    URL hsURL = HelpSet.findHelpSet(cl, helpHS);
                    HelpSet helpSet = new HelpSet(null, hsURL);
                    // Trigger the help viewer:
                    helpPane = new JHelpContentViewer(helpSet);
                    JHelpNavigator helpNavigator = (JHelpNavigator) helpSet.getNavigatorView("TOC").createNavigator(helpPane.getModel());
                    JEFrame helpFrame = new JEFrame("Help contents");
                    JSplitPane helpSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, helpNavigator, helpPane);
                    helpFrame.add(helpSplit);
                    helpFrame.setVisible(true);
                    helpFrame.setMaximum(true);
                } catch (Exception ee) {
                    // Say what the exception really is
                    LOGGER.log(Level.WARNING, "Could not open application help", ee);
                }
            }
        };
    }

    /**
     * Create the main menu and add actions.
     */
    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JExtMenu menuHelp = new JExtMenu("&Help");
        menuHelp.add(actHelp);
        menuHelp.addSeparator();
        menuHelp.add(actAbout);
        menuHelp.add(actLicense);

        JExtMenu menuOptions = new JExtMenu("&Options");
        menuOptions.add(actPreferences);
        menuOptions.addSeparator();
        menuOptions.add(actQuit);

        menuBar.add(menuOptions);
        menuBar.add(((JExtDesktopPane) desktopPane).getWindowMenu());
        menuBar.add(menuHelp);
    }

    /**
     * Retrieve the optimization parameters of a loaded module. Return null if no module is
     * loaded.
     *
     * @return
     */
    public InterfaceOptimizationParameters getOptimizationParameters() {
        if (currentModuleAdapter != null) {
            if (currentModuleAdapter instanceof AbstractModuleAdapter) {
                return ((AbstractModuleAdapter) currentModuleAdapter).getOptimizationParameters();
            }
        }
        return null;
    }

    public AbstractStatistics getStatistics() {
        return ((GenericModuleAdapter) currentModuleAdapter).getStatistics();
    }

    public InterfaceStatisticsParameters getStatisticsParameter() {
        return ((GenericModuleAdapter) currentModuleAdapter).getStatistics().getStatisticsParameters();
    }

    private void loadSpecificModule(String selectedModule, InterfaceOptimizationParameters optimizationParameters) {
        ModuleAdapter newModuleAdapter = null;
        //
        try {
            InterfaceOptimizationParameters params = optimizationParameters;
            if (optimizationParameters == null) {
                params = OptimizationParameters.getInstance();
            }
            newModuleAdapter = new OptimizationModuleAdapter(selectedModule, params, withGUI ? null : "EvA2");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading module.", e);
            EVAERROR.EXIT("Error while comAdapter.GetModuleAdapter Host: " + e.getMessage());
        }
        if (newModuleAdapter == null) {
            // When launching a Java Web Start application, baseDir will always be null!
            URL baseDir = getClass().getClassLoader().getResource("");
            String cp = System.getProperty("java.class.path", ".");
            String dir = (baseDir == null) ? System.getProperty("user.dir") : baseDir.getPath();

            if (!cp.contains(dir)) {
                // this was added due to matlab not adding base dir to base path...
                System.err.println("classpath does not contain base directory!");
                System.err.println("adding base dir and trying again...");
                System.setProperty("java.class.path", cp + System.getProperty("path.separator") + dir);
                ReflectPackage.resetDynCP();
                loadSpecificModule(selectedModule, optimizationParameters); // end recursive call! handle with care!
            }
        } else {
            newModuleAdapter.addOptimizationStateListener(this);
            try {
                if (withGUI) {
                    // this (or rather: EvAModuleButtonPanelMaker) is where the start button etc come from!
                    frameMaker = newModuleAdapter.getModuleFrame();

                    /* This is the left TabPane on the main frame */
                    JPanel moduleContainer = frameMaker.makePanel();

                    configurationPane.setVisible(false);
                    configurationPane.removeAll();

                    GridBagConstraints gbConstraints = new GridBagConstraints();

                    /* ToDo: Find a way to properly add the TreeView to the GOPanel */
                    if (false && (newModuleAdapter instanceof AbstractModuleAdapter)) {
                        JComponent tree = null;
                        tree = getEvATreeView(frameMaker.getOptimizationParametersPanel(), "OptimizationParameters", ((AbstractModuleAdapter) newModuleAdapter).getOptimizationParameters());
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
                    add(frameMaker.getToolBar(), gbConstraints);

                    GridBagConstraints gbConstraints2 = new GridBagConstraints();
                    gbConstraints2.gridx = 0;
                    gbConstraints2.gridy = 0;
                    gbConstraints2.fill = GridBagConstraints.VERTICAL;
                    gbConstraints2.weighty = 1.0;
                    configurationPane.add(moduleContainer, gbConstraints2);
                    configurationPane.validate();
                }


            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while newModuleAdapter.getModuleFrame(): " + e.getMessage(), e);
                EVAERROR.EXIT("Error while newModuleAdapter.getModuleFrame(): " + e.getMessage());
            }

            currentModuleAdapter = newModuleAdapter;
        }
    }

    /**
     * Create a tree view of an object based on EvATreeNode. It is encapsulated
     * in a JScrollPane.
     *
     * @param goPanel
     * @param title
     * @param object
     * @return
     * @see eva2.gui.EvATreeNode
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

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
        aboutDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    private void showLicense() {
        HtmlDemo lgpl = new HtmlDemo(EvAInfo.LGPLFile);
        HtmlDemo gpl = new HtmlDemo(EvAInfo.GPLFile);
        gpl.show();
        lgpl.show();
    }

    @Override
    public void performedRestart(String infoString) {
        if (superListenerList != null) {
            for (OptimizationStateListener l : superListenerList) {
                l.performedRestart(infoString);
            }
        }
        LOGGER.log(Level.INFO, "Restarted processing {0}", infoString);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void performedStart(String infoString) {
        if (superListenerList != null) {
            for (OptimizationStateListener l : superListenerList) {
                l.performedStart(infoString);
            }
        }
        LOGGER.log(Level.INFO, "Started processing {0}", infoString);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void performedStop() {
        if (superListenerList != null) {
            for (OptimizationStateListener l : superListenerList) {
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
    @Override
    public void updateProgress(final int percent, String msg) {
        if (superListenerList != null) {
            for (OptimizationStateListener l : superListenerList) {
                l.updateProgress(percent, msg);
            }
        }
        if (msg != null) {
            LOGGER.info(msg);
        }
        if (this.progressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {

                @Override
                public void run() {
                    progressBar.setValue(percent);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }
}
