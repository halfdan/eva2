package eva2.client;

/*
 * Title:        EvA2
 * Description: The main client class of the EvA framework.
 * Copyright:    Copyright (c) 2008
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.net.URL;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import eva2.EvAInfo;
import eva2.gui.BeanInspector;
import eva2.gui.EvATabbedFrameMaker;
import eva2.gui.ExtAction;
import eva2.gui.HtmlDemo;
import eva2.gui.JEFrame;
import eva2.gui.JEFrameRegister;
import eva2.gui.JExtMenu;
import eva2.gui.LogPanel;
import eva2.server.EvAServer;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.AbstractModuleAdapter;
import eva2.server.modules.GOParameters;
import eva2.server.modules.GenericModuleAdapter;
import eva2.server.modules.ModuleAdapter;
import eva2.server.stat.AbstractStatistics;
import eva2.server.stat.InterfaceStatisticsParameter;
import eva2.tools.BasicResourceLoader;
import eva2.tools.EVAERROR;
import eva2.tools.EVAHELP;
import eva2.tools.ReflectPackage;
import eva2.tools.Serializer;
import eva2.tools.StringTools;
import eva2.tools.jproxy.RemoteStateListener;

/**
 *
 */
public class EvAClient implements RemoteStateListener, Serializable {
	private final int splashScreenTime = 1500;
	private final int maxWindowMenuLength = 30;
	
	public static boolean TRACE = false;

	public JEFrame m_Frame;
	Runnable initRnbl = null;

	private EvAComAdapter m_ComAdapter;
	private transient JMenuBar 		m_barMenu;
	private transient JExtMenu 		m_mnuAbout;
	private transient JExtMenu 		m_mnuSelHosts;
	private transient JExtMenu 		m_mnuModule;
	private transient JExtMenu 		m_mnuWindow;
	private transient JExtMenu 		m_mnuOptions;
	private transient JProgressBar	m_ProgressBar;

//	public ArrayList m_ModulGUIContainer = new ArrayList();
	// LogPanel
	private LogPanel m_LogPanel;

	// Module:
	private ExtAction m_actModuleLoad;
	// GUI:

	// Hosts:
	private ExtAction m_actHost;
	private ExtAction m_actAvailableHost;
	private ExtAction m_actKillHost;
	private ExtAction m_actKillAllHosts;
	private ModuleAdapter currentModuleAdapter = null;
	// About:
	private ExtAction m_actAbout;
	private ExtAction m_actLicense;

//	private JPanel m_panelTool;
//	private FrameCloseListener m_frameCloseListener;
//	private JFileChooser m_FileChooser;

//	if not null, the module is loaded automatically and no other can be selected
	private String useDefaultModule = null;//"Genetic_Optimization";
	private boolean showLoadModules = false;
	private boolean localMode = false;
	// This variable says whether, if running locally, a local server should be addressed by RMI.
	// False should be preferred here to avoid overhead
	private boolean useLocalRMI = false;
	// measuring optimization runtime
	private long startTime = 0;
	// remember the module in use
	private transient String currentModule = null;

	Vector<RemoteStateListener> superListenerList = null;
	private boolean withGUI = true	;
	private EvATabbedFrameMaker frmMkr = null;
	
	public void addRemoteStateListener(RemoteStateListener l) {
		if (superListenerList == null) superListenerList = new Vector<RemoteStateListener>();
		superListenerList.add(l);
	}
	
	public boolean removeRemoteStateListener(RemoteStateListener l) {
		if (superListenerList != null) { 
			return superListenerList.remove(l);
		} else return false;
	}

	/**
	 * Constructor of GUI of EvA2.
	 * Works as client for the EvA2 server.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
	 * to await full initialization if necessary.
	 * 
	 */
	public EvAClient(final String hostName) {
		this(hostName, null, false, false);
	}
	
	/**
	 * A constructor. Splash screen is optional, Gui is activated, no parent window.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
	 * to await full initialization if necessary.
	 * 
	 * @see #EvAClient(String, Window, String, boolean, boolean, boolean)
	 * @param hostName
	 * @param paramsFile
	 * @param autorun
	 * @param nosplash
	 */
	public EvAClient(final String hostName, final String paramsFile, boolean autorun, boolean nosplash) {
		this(hostName, null, paramsFile, null, autorun, nosplash, false);
	}
	
	/**
	 * A constructor with optional spash screen.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
	 * to await full initialization if necessary.
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
	 * A constructor with optional splash screen.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
	 * to await full initialization if necessary.
	 * 
	 * @see #EvAClient(String, String, boolean, boolean)
	 * @param hostName
	 * @param paramsFile
	 * @param autorun
	 * @param noSplash
	 * @param noGui
	 */
	public EvAClient(final String hostName, String paramsFile, boolean autorun, boolean noSplash, boolean noGui) {
		this(hostName, null, paramsFile, null, autorun, noSplash, noGui);
	}
	
	/**
	 * A constructor with optional splash screen.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
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
		this(hostName, null, null, goParams, autorun, noSplash, noGui);
	}
	
	/**
	 * Constructor of GUI of EvA2. Works as client for the EvA2 server. GO parameters may be
	 * loaded from a file (paramsFile) or given directly as a java instance. Both may be null
	 * to start with standard parameters. If both are non null, the java instance has the
	 * higher priority.
	 * Note that the EvAClient initialized multi-threaded for efficiency. Use {@link #awaitGuiInitialized()}
	 * to await full initialization if necessary.
	 *
	 * @param hostName
	 * @param parent
	 * @param paramsFile
	 * @param autorun
	 * @param noSplash
	 * @param noGui
	 */
	public EvAClient(final String hostName, final Window parent, final String paramsFile, final InterfaceGOParameters goParams, final boolean autorun, final boolean noSplash, final boolean noGui) {
		final SplashScreenShell fSplashScreen = new SplashScreenShell(EvAInfo.splashLocation);

		// preload some classes (into system cache) in a parallel thread
		preloadClasses();

	    withGUI = !noGui;
		// activate the splash screen (show later using SwingUtilities)
		if (!noSplash && withGUI) {
			try {
				fSplashScreen.splash();
			} catch(HeadlessException e) {
				System.err.println("Error: no xserver present - deactivating GUI.");
				withGUI=false;
			}
		}
	    
		currentModule = null;
		
		m_ComAdapter = EvAComAdapter.getInstance();

		SwingUtilities.invokeLater( initRnbl = new Runnable() {
			public void run(){
				synchronized (this) {
					long startTime = System.currentTimeMillis();
					init(hostName, paramsFile, goParams, parent); // this takes a bit

					long wait = System.currentTimeMillis() - startTime;
					if (!autorun) {
						if (!noSplash) try {
							// if splashScreenTime has not passed, sleep some more 
							if (wait < splashScreenTime) Thread.sleep(splashScreenTime - wait);
						} catch (Exception e) {}
					} else {
						if (!withGUI && (currentModuleAdapter instanceof GenericModuleAdapter)) {
							// do not save new parameters for an autorun without GUI - they werent changed manually anyways.
							((GenericModuleAdapter)currentModuleAdapter).getStatistics().setSaveParams(false);
							System.out.println("Autorun without GUI - not saving statistics parameters...");
						}
						if (withGUI) frmMkr.onUserStart();
						else currentModuleAdapter.startOpt();
					}
					// close splash screen
					if (!noSplash && withGUI) fSplashScreen.dispose();
					notify();
				}
			}
		});
	}
	
	/**
	 * Since the constructor runs multi-threaded for efficiency, this method
	 * may be called to await the full initialization of a client instance.
	 * As soon as it returns, the EvAClient GUI is fully initialized. 
	 */
	public void awaitGuiInitialized() {
		if (initRnbl!=null) {
			synchronized (initRnbl) {
				try {
					initRnbl.wait();
					initRnbl=null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void preloadClasses() {
		ClassPreloader cp = new ClassPreloader( "eva2.server.go.strategies.InterfaceOptimizer", "eva2.server.go.problems.InterfaceOptimizationProblem", "eva2.server.go.InterfaceTerminator");
		new Thread(cp).start();
	}
	
	/**
	 * Try to start the optimization with current parameters on the loaded module.
	 * Return true on success, otherwise false.
	 * 
	 * @return
	 */
	public boolean startOptimization() {
		if (currentModuleAdapter!=null) {
			currentModuleAdapter.startOpt();
			return true;
		} else return false;
	}
	
	/**
	 * Add a window listener to the EvA2 JFrame instance.
	 * 
	 * @param l
	 */
	public void addWindowListener(WindowListener l) {
		if (m_Frame != null) {
			m_Frame.setName(getClass().getSimpleName());
			m_Frame.addWindowListener(l);
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
		if (m_Frame != null) {
			m_Frame.removeWindowListener(l);
		} else {
			System.err.println("Error, no JFrame existent in "
					+ this.getClass().getSimpleName());
		}
	}
	
	/**
	 * Sets given hostname and tries to load GOParamsters from given file if non null.
	 */
	private void init(String hostName, String paramsFile, InterfaceGOParameters goParams, final Window parent) {
		//EVA_EDITOR_PROPERTIES
		useDefaultModule = EvAInfo.propDefaultModule();
		
		if (useDefaultModule != null) {
			useDefaultModule = useDefaultModule.trim();
			if (useDefaultModule.length() < 1) useDefaultModule = null;
		}
		
		if (withGUI ) {
			m_Frame = new JEFrame(EvAInfo.productName + " workbench");
			BasicResourceLoader loader = BasicResourceLoader.instance();
			byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
			m_Frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
//			m_Frame.setTitle(EvAInfo.productName + " workbench");

			try {
				Thread.sleep(200);
			} catch (Exception e) {
				System.out.println("Error" + e.getMessage());
			}

			m_Frame.getContentPane().setLayout(new BorderLayout());
			m_LogPanel = new LogPanel();
			m_Frame.getContentPane().add(m_LogPanel, BorderLayout.CENTER);
			m_ProgressBar = new JProgressBar();
			m_Frame.getContentPane().add(m_ProgressBar, BorderLayout.SOUTH);

			if (EvAInfo.propShowModules() != null) showLoadModules = true;
			else showLoadModules = false; // may be set to true again if default module couldnt be loaded

			createActions();
		}
		if (useDefaultModule != null) {
			// if goParams are not defined and a params file is defined
			// try to load parameters from file 
			if (goParams==null && (paramsFile!=null && (paramsFile.length()>0))) goParams = GOParameters.getInstance(paramsFile, false);
			loadModuleFromServer(useDefaultModule, goParams);//loadSpecificModule
		}
		
		if (withGUI) {
			buildMenu();
			m_Frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.out.println("Closing EvA2 Client. Bye!");
					m_Frame.dispose();
					Set<String> keys = System.getenv().keySet();
					if (keys.contains("MATLAB")) {
						System.out.println("Seems like Ive been started from Matlab: not killing JVM");
					} else {
						if (parent == null) System.exit(1); 
					}
				}
			});
		}
		
		if (m_ComAdapter != null) {
			if (hostName != null) selectHost(hostName);
			m_ComAdapter.setLogPanel(m_LogPanel);
			logMessage("Selected Host: " + m_ComAdapter.getHostName());
		}
//		m_mnuModule.setText("Select module");
//		m_mnuModule.repaint();
		
		if (withGUI) {
			m_LogPanel.logMessage("Working directory is: " + System.getProperty("user.dir"));
			m_LogPanel.logMessage("Class path is: " + System.getProperty("java.class.path","."));

			if (!(m_Frame.isVisible())) {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				m_Frame.setLocation((int)((screenSize.width-m_Frame.getWidth())/2), (int)((screenSize.height-m_Frame.getHeight())/2.5));
				m_Frame.pack();
				m_Frame.setVisible(true);
			}
			m_LogPanel.logMessage("EvA2 ready"); // if this message is omitted, the stupid scroll pane runs to the end of the last line which is ugly for a long class path
		}
	}
	
	/**
	 * Refresh the parameter panels (if settings have been changed outside
	 * of the GUI which should be updated in the GUI.
	 * 
	 */
	public void refreshMainPanels() {
		frmMkr.refreshPanels();
	}
	
	/**
	 * The one and only main of the client program. Possible arguments:
	 * --autorun immediately starts the optimization (with parameters loaded from current
	 * directory if available.
	 * --hostname HOST: sets the hostname for the EvAClient to HOST 
	 * --nosplash: skip the splash screen.
	 * --params PFILE: load the optimization parameter from the serialized file PFILE 
	 * 
	 * @param args command line parameters
	 */
	public static void main(String[] args) {
		if (TRACE) {
			System.out.println(EVAHELP.getSystemPropertyString());
		}

		String[] keys= new String[]{"--help", "--autorun", "--nosplash", "--nogui", "--remotehost", "--params"};
		int[] arities = new int[]{0, 0, 0, 0, 1, 1};
		Object[] values = new Object[6];
		
		Integer[] unknownArgs = StringTools.parseArguments(args, keys, arities, values, true);
		
		if (unknownArgs.length>0) {
			System.err.println("Unrecognized command line options: ");
			for (int i=0; i<unknownArgs.length; i++) System.err.println("   " + args[unknownArgs[i]]);
			if (values[0]==null) System.err.println("Try --help as argument.");
		}
		
		if (values[0]!=null) {
			System.out.println(usage());
		} else {
			boolean autorun=(values[1]!=null);
			boolean nosplash=(values[2]!=null);
			boolean nogui=(values[3]!=null);
			String hostName=StringTools.checkSingleStringArg(keys[4], values[4], arities[4]-1);
			String paramsFile=StringTools.checkSingleStringArg(keys[5], values[5], arities[5]-1);
			
			if (TRACE) System.out.println("Command line arguments were: ");
			if (TRACE) System.out.println("	" + BeanInspector.toString(keys));
			if (TRACE) System.out.println("	" + BeanInspector.toString(values));
			EvAClient Client = new EvAClient(hostName, paramsFile, autorun, nosplash, nogui);
		}
	}

	public static String usage() {
		StringBuffer sbuf = new StringBuffer();
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
		//////////////////////////////////////////////////////////////
		// Module:
		/////////////////////////////////////////////////////////////
		m_actModuleLoad = new ExtAction("&Load", "Load Module",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				loadModuleFromServer(null, null);
			}
		};

		m_actAbout = new ExtAction("&About...", "Product Information",
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showAboutDialog();
			}
		};
		m_actLicense = new ExtAction("&License...", "View License",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showLicense();
			}
		};
		m_actHost = new ExtAction("&List of all servers", "All servers in list",
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				selectAvailableHost(m_ComAdapter.getHostNameList());
			}
		};
		m_actAvailableHost = new ExtAction("Available &Server", "Available server",
				KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAvailableHost(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		m_actKillHost = new ExtAction("&Kill server", "Kill server process on selected host",
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAvailableHostToKill(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		m_actKillAllHosts = new ExtAction("Kill &all servers", "Kill all servers",
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAllAvailableHostToKill(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		/* m_actStartServerManager = new ExtAction("Start &Server Manager", "Start &Server Manager",
             KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK)){
             public void actionPerformed(ActionEvent e){
               m_LogPanel.logMessage(e.getActionCommand());
               ServerStartFrame sm = new ServerStartFrame(m_ComAdapter.getHostNameList());
             }
           };
		 */
	}

	private void buildMenu() {
		m_barMenu = new JMenuBar();
		m_Frame.setJMenuBar(m_barMenu);
		////////////////////////////////////////////////////////////////////////////
		JExtMenu mnuLookAndFeel = new JExtMenu("&Look and Feel");
		ButtonGroup grpLookAndFeel = new ButtonGroup();
		UIManager.LookAndFeelInfo laf[] = UIManager.getInstalledLookAndFeels();

		String LAF = Serializer.loadString("LookAndFeel.ser");

		boolean lafSelected = false;
		for (int i = 0; i < laf.length; i++) {
			JRadioButtonMenuItem mnuItem = new JRadioButtonMenuItem(laf[i].getName());
			mnuItem.setActionCommand(laf[i].getClassName());
			if (!lafSelected && laf[i].getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
//			if (!lafSelected && laf[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
//				if (LAF==null) {// do this only if no older selection one could be loaded
//					LAF = laf[i].getClassName(); // set for later selection
//				} // this causes problems with my gnome!
				if (LAF == null) {
					lafSelected = true;
					mnuItem.setSelected(true);
				}
			}
			mnuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						UIManager.setLookAndFeel(e.getActionCommand());
						SwingUtilities.updateComponentTreeUI(m_Frame);
						// TODO hier noch reinhacken dass alle frame geupdated werden.
						m_Frame.pack();
//						m_Frame.setSize(new Dimension(900, 700));
//						m_Frame.setVisible(true);
						Serializer.storeString("LookAndFeel.ser", e.getActionCommand());
					} catch (ClassNotFoundException exc) {} catch (InstantiationException exc) {} catch (UnsupportedLookAndFeelException exc) {} catch (
							IllegalAccessException exc) {}
				}
			});
			mnuLookAndFeel.add(mnuItem);
			grpLookAndFeel.add(mnuItem);
		}
		if (LAF != null) {
			try {
				UIManager.setLookAndFeel(LAF);
				SwingUtilities.updateComponentTreeUI(m_Frame);
//				m_Frame.pack();
//				m_Frame.setSize(new Dimension(900, 700));
//				m_Frame.setVisible(true);
			} catch (ClassNotFoundException exc) {} catch (InstantiationException exc) {} catch (UnsupportedLookAndFeelException exc) {} catch (
					IllegalAccessException exc) {}
		}
		m_mnuModule = new JExtMenu("&Module");
		m_mnuModule.add(m_actModuleLoad);

		////////////////////////////////////////////////////////////////

		m_mnuWindow = new JExtMenu("&Window");
		m_mnuWindow.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				//				System.out.println("Selected");
				m_mnuWindow.removeAll();
				JExtMenu curMenu = m_mnuWindow;
//				JScrollPane jsp = new JScrollPane();
				Object[] framelist = JEFrameRegister.getFrameList();
				for (int i = 0; i < framelist.length; i++) {
					JMenuItem act = new JMenuItem((i + 1) + ". " + ((JEFrame) framelist[i]).getTitle());
					final JFrame x = ((JEFrame) framelist[i]);
					
					act.addActionListener((new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (!x.isActive()) {
								x.setExtendedState(JFrame.NORMAL);
								x.setVisible(false);
								x.setVisible(true); // it seems to be quite a fuss to bring something to the front and actually mean it...
								x.toFront();		// this seems useless
								x.requestFocus();	// this seems useless too
							}
						}
					}));
					if (curMenu.getItemCount()>=maxWindowMenuLength) {
						JExtMenu subMenu = new JExtMenu("&More...");
						curMenu.add(new JSeparator());
						curMenu.add(subMenu);
						curMenu=subMenu;
					}
					curMenu.add(act);
				}
				String[] commonPrefixes = JEFrameRegister.getCommonPrefixes(10);
				if (commonPrefixes.length > 0) m_mnuWindow.add(new JSeparator());
				for (int i=0; i<commonPrefixes.length; i++) {
					final String prefix = commonPrefixes[i];
					JMenuItem act = new JMenuItem("Close all of " + prefix + "...");
					act.addActionListener((new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JEFrameRegister.closeAllByPrefix(prefix);
						}
					}));
					m_mnuWindow.add(act);
				}

			}
			public void menuCanceled(MenuEvent e) {
			}
			public void menuDeselected(MenuEvent e) {
			}
		}
		);

		////////////////////////////////////////////////////////////////
		m_mnuSelHosts = new JExtMenu("&Select Hosts");
		m_mnuSelHosts.setToolTipText("Select a host for the server application");
		//if (EvAClient.LITE_VERSION == false)
		m_mnuSelHosts.add(m_actHost);
		m_mnuSelHosts.add(m_actAvailableHost);
		m_mnuSelHosts.addSeparator();
		m_mnuSelHosts.add(m_actKillHost);
		m_mnuSelHosts.add(m_actKillAllHosts);
//		m_mnuOptions.add(m_actStartServerManager);
		////////////////////////////////////////////////////////////////
		m_mnuAbout = new JExtMenu("&About");
		m_mnuAbout.add(m_actAbout);
		m_mnuAbout.add(m_actLicense);
		//////////////////////////////////////////////////////////////
		// m_barMenu.add(m_Desktop.getWindowMenu());
		
		m_mnuOptions = new JExtMenu("&Options");
		m_mnuOptions.add(mnuLookAndFeel);
		m_mnuOptions.add(m_mnuSelHosts);
		//m_barMenu.add(m_mnuSelHosts);
		// this is accessible if no default module is given
		if (showLoadModules) {
			m_barMenu.add(m_mnuModule);
		}

		m_barMenu.add(m_mnuOptions);
		m_barMenu.add(m_mnuWindow);
		m_barMenu.add(m_mnuAbout);

	}
	
	public static String getProductName() {
		return EvAInfo.productName;
	}
	
	protected void logMessage(String msg) {
		if (TRACE || m_LogPanel == null) System.out.println(msg);
		if (m_LogPanel != null) m_LogPanel.logMessage(msg);
	}

	/**
	 *
	 */
	private void loadModuleFromServer(String selectedModule, InterfaceGOParameters goParams) {
		if (m_ComAdapter.getHostName() == null) {
			System.err.println("error in loadModuleFromServer!");
			return;
		}
		if (m_ComAdapter.getHostName().equals("localhost")) {
			localMode = true;
			if (useLocalRMI) {
				EvAServer Server = new EvAServer(true, false);
				m_ComAdapter.setLocalRMIServer(Server.getRMIServer());
				logMessage("Local EvAServer started");
				m_ComAdapter.setRunLocally(false); // this is not quite true but should have the desired effect
			} else {
				logMessage("Working locally");
				m_ComAdapter.setLocalRMIServer(null);
				m_ComAdapter.setRunLocally(true);
			}
		} else {
			localMode = false;
			if (TRACE) logMessage("Using RMI on m_ComAdapter.getHostName()");
			m_ComAdapter.setRunLocally(false);
		}
		if (selectedModule == null) { // show a dialog and ask for a module
			String[] ModuleNameList = m_ComAdapter.getModuleNameList();
			if (ModuleNameList == null) {
				JOptionPane.showMessageDialog(m_Frame.getContentPane(), "No modules available on " + m_ComAdapter.getHostName(), EvAInfo.infoTitle, 1);
			} else {
				String LastModuleName = Serializer.loadString("lastmodule.ser");
				if (LastModuleName == null) LastModuleName = ModuleNameList[0];
				selectedModule = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
						"Which module do you want \n to load on host: " + m_ComAdapter.getHostName() + " ?", "Load optimization module on host",
						JOptionPane.QUESTION_MESSAGE,
						null,
						ModuleNameList,
						LastModuleName);
			}
		}
		if (selectedModule == null) {
			System.err.println("not loading any module");
		} else {
			Serializer.storeString("lastmodule.ser", selectedModule);

			loadSpecificModule(selectedModule, goParams);

			if (withGUI) {
				m_actHost.setEnabled(true);
				m_actAvailableHost.setEnabled(true);
			}
			logMessage("Selected Module: " + selectedModule);
//			m_LogPanel.statusMessage("Selected Module: " + selectedModule);
		}
	}
	
	/**
	 * Retrieve the GOParamters of a loaded module. Return null if no module is loaded.
	 * 
	 * @return
	 */
	public InterfaceGOParameters getGOParameters() {
		if (currentModuleAdapter != null) {
			if (currentModuleAdapter instanceof AbstractModuleAdapter) {
				return ((AbstractModuleAdapter)currentModuleAdapter).getGOParameters();
			}
		}
		return null;
	}
	
	public AbstractStatistics getStatistics() {
		return ((GenericModuleAdapter)currentModuleAdapter).getStatistics();
	}
	
	public InterfaceStatisticsParameter getStatsParams() {
		return ((GenericModuleAdapter)currentModuleAdapter).getStatistics().getStatisticsParameter();
	}
	
	/**
	 * Check if there is an optimization currently running. 
	 * 
	 * @return
	 */
	public boolean isOptRunning() {
		if (currentModuleAdapter != null && (currentModuleAdapter instanceof AbstractModuleAdapter)) {
			return ((AbstractModuleAdapter)currentModuleAdapter).isOptRunning();
		} else return false;
	}

	private void loadSpecificModule(String selectedModule, InterfaceGOParameters goParams) {
		ModuleAdapter newModuleAdapter = null;
		//
		try {
			newModuleAdapter = m_ComAdapter.getModuleAdapter(selectedModule, goParams, withGUI ? null : "EvA2");
		} catch (Exception e) {
			logMessage("Error while m_ComAdapter.GetModuleAdapter Host: " + e.getMessage());
			e.printStackTrace();
			EVAERROR.EXIT("Error while m_ComAdapter.GetModuleAdapter Host: " + e.getMessage());
		}
		if (newModuleAdapter == null) {
			URL baseDir = this.getClass().getClassLoader().getResource("");
			String cp = System.getProperty("java.class.path",".");
			if (!cp.contains(baseDir.getPath())) {
				// this was added due to matlab not adding base dir to base path...
				System.err.println("classpath does not contain base directory!");
				System.err.println("adding base dir and trying again...");
				System.setProperty("java.class.path", cp + System.getProperty("path.separator") + baseDir.getPath());
				ReflectPackage.resetDynCP();
				m_ComAdapter.updateLocalMainAdapter();
				loadSpecificModule(selectedModule, goParams); // end recursive call! handle with care!
				return;
			}
			showLoadModules = true;
		}
		else {
			newModuleAdapter.setConnection(!localMode);
			if (m_ComAdapter.isRunLocally()) {
				// TODO in rmi-mode this doesnt work yet! meaning e.g. that theres no content in the info log
				newModuleAdapter.addRemoteStateListener((RemoteStateListener)this);
			}
			try {
				if (withGUI) {
					// this (or rather: EvAModuleButtonPanelMaker) is where the start button etc come from!
					frmMkr  = newModuleAdapter.getModuleFrame();
//					newModuleAdapter.setLogPanel(m_LogPanel);
					JPanel moduleContainer = frmMkr.makePanel(); // MK the main frame is actually painted in here

					boolean wasVisible = m_Frame.isVisible();
					m_Frame.setVisible(false);
					m_Frame.getContentPane().removeAll();
					
					// nested info-panel so that we can stay with simple borderlayouts
					JPanel infoPanel = new JPanel();
					infoPanel.setLayout(new BorderLayout());
					infoPanel.add(m_ProgressBar, BorderLayout.SOUTH);
					infoPanel.add(m_LogPanel, BorderLayout.NORTH);
					
					m_Frame.add(frmMkr.getToolBar(), BorderLayout.NORTH);
					m_Frame.add(moduleContainer, BorderLayout.CENTER);
					//m_Frame.add(m_ProgressBar, BorderLayout.CENTER);
					//m_Frame.add(m_LogPanel, BorderLayout.SOUTH);
					m_Frame.add(infoPanel, BorderLayout.SOUTH);
	
					m_Frame.pack();
					m_Frame.setVisible(wasVisible);
				}
				
				currentModule = selectedModule;
				//			m_ModulGUIContainer.add(Temp);
			} catch (Exception e) {
				currentModule = null;
				e.printStackTrace();
				logMessage("Error while newModulAdapter.getModulFrame(): " + e.getMessage());
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
	 *
	 */
	private void selectAvailableHost(String[] HostNames) {
		if (TRACE) System.out.println("SelectAvailableHost");
		if (HostNames == null || HostNames.length == 0) {
			showNoHostFoundDialog();
		} else {
			String HostName = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
					"Which active host do you want to connect to?", "Host", JOptionPane.QUESTION_MESSAGE, null,
					HostNames, m_ComAdapter.getHostName());
			if (HostName != null) selectHost(HostName);
		}
	}
	
	private void selectHost(String hostName) {
		m_ComAdapter.setHostName(hostName);
		logMessage("Selected Host: " + hostName);
		if (currentModule != null) {
			logMessage("Reloading module from server...");
			loadModuleFromServer(currentModule, null);
		}
		
//		m_mnuModule.setText("Select module");
//		m_mnuModule.repaint();
	//			System.out.println(HostName + " selected");
	}
	
	private void showPleaseWaitDialog() {
		JOptionPane.showMessageDialog(m_Frame.getContentPane(), "Please wait one moment.", EvAInfo.infoTitle, 1);
	}
	
	private void showAboutDialog() {
		JOptionPane.showMessageDialog
		(m_Frame,
				EvAInfo.productName + " - " + EvAInfo.productLongName + 
				"\n University of Tübingen\n Chair for Computer Architecture\n " +
				"M. Kronfeld, H. Planatscher, M. de Paly, A. Dräger, F. Streichert, H. Ulmer\n " +
//				"H. Ulmer & F. Streichert & H. Planatscher & M. de Paly & M. Kronfeld\n" +
				"Prof. Dr. Andreas Zell \n (c) " + EvAInfo.copyrightYear + "\n Version " + EvAInfo.getVersion()+ 
				"\n URL: " + EvAInfo.url, EvAInfo.infoTitle, 1);
	}
	
	private void showLicense() {
    	HtmlDemo lgpl = new HtmlDemo(EvAInfo.LGPLFile);
    	HtmlDemo gpl = new HtmlDemo(EvAInfo.GPLFile);
        gpl.show();
        lgpl.show();
        }	
	
	private void showNoHostFoundDialog() {
		JOptionPane.showMessageDialog(m_Frame.getContentPane(), "No host with running EVASERVER found. Please start one or \nadd the correct address to the properties list.", EvAInfo.infoTitle, 1);
	}

	private void selectAvailableHostToKill(String[] HostNames) {
		if (TRACE) System.out.println("SelectAvailableHostToKill");
		if (HostNames == null || HostNames.length == 0) {
			showNoHostFoundDialog();
			return;
		}
		String HostName = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
				"Which server do you want to be killed ?", "Host", JOptionPane.QUESTION_MESSAGE, null,
				HostNames, m_ComAdapter.getHostName());
		if (HostName == null)
			return;
		logMessage("Kill host process on = " + HostName);
		m_ComAdapter.killServer(HostName);
//		m_LogPanel.statusMessage("");
	}

	private void selectAllAvailableHostToKill(String[] HostNames) {
		System.out.println("SelectAllAvailableHostToKill");
		if (HostNames == null || HostNames.length == 0) {
			System.out.println("no host is running");
			return;
		}
		m_ComAdapter.killAllServers();
	}

	public void performedRestart(String infoString) {
		if (superListenerList != null) for (RemoteStateListener l : superListenerList) {
			l.performedRestart(infoString);
		}
		logMessage("Restarted processing " + infoString);
		startTime = System.currentTimeMillis();
	}

	public void performedStart(String infoString) {
		if (superListenerList != null) for (RemoteStateListener l : superListenerList) {
			l.performedStart(infoString);
		}
		logMessage("Started processing " + infoString);
		startTime = System.currentTimeMillis();
	}

	public void performedStop() {
		if (superListenerList != null) for (RemoteStateListener l : superListenerList) {
			l.performedStop();
		}
		long t = (System.currentTimeMillis() - startTime);
		logMessage(String.format("Stopped after %1$d.%2$tL s", (t / 1000), (t % 1000)));
		if (!withGUI) System.exit(0);
	}
	
    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
	public void updateProgress(final int percent, String msg) {
		if (superListenerList != null) for (RemoteStateListener l : superListenerList) {
			l.updateProgress(percent, msg);
		}
		if (msg != null) logMessage(msg);
        if (this.m_ProgressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                public void run() {
                    m_ProgressBar.setValue(percent);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }
}

final class SplashScreenShell {
	SplashScreen splScr = null;
	String imgLoc = null;
	
	public SplashScreenShell(String imageLoc) {
		imgLoc = imageLoc;
	}
	
	public void splash() {
		splScr = new SplashScreen(imgLoc);
		splScr.splash();
	}
	
	public void dispose() {
		if (splScr!=null) {
			splScr.dispose();
			splScr=null;
		}
	}
}

class SplashScreen extends Frame {
	private static final long serialVersionUID = 1281793825850423095L;
	String imgLocation;

	public SplashScreen(String imgLoc) {
		imgLocation = imgLoc;
	}

	/**
	 * Show the splash screen to the end user.
	 *
	 * <P>Once this method returns, the splash screen is realized, which means 
	 * that almost all work on the splash screen should proceed through the event 
	 * dispatch thread. In particular, any call to <code>dispose</code> for the 
	 * splash screen must be performed in the event dispatch thread.
	 */
	public void splash(){
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