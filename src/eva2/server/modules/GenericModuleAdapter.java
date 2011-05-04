package eva2.server.modules;


import java.io.Serializable;
import java.lang.reflect.Proxy;

import eva2.gui.EvAModuleButtonPanelMaker;
import eva2.gui.EvATabbedFrameMaker;
import eva2.gui.GenericObjectEditor;
import eva2.gui.JParaPanel;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.stat.AbstractStatistics;
import eva2.server.stat.EvAJob;
import eva2.server.stat.EvAJobList;
import eva2.server.stat.InterfaceStatisticsParameter;
import eva2.server.stat.StatisticsStandalone;
import eva2.server.stat.StatisticsWithGUI;
import eva2.tools.jproxy.MainAdapterClient;
import eva2.tools.jproxy.RMIProxyLocal;

public class GenericModuleAdapter extends AbstractModuleAdapter implements Serializable {

    private AbstractStatistics     m_StatisticsModul;
    private EvAJobList	jobList = null;
    public String 				helperFilename;
    JParaPanel jobPanel=null, paramPanel=null;

    /** 
     * Constructor of the ModuleAdapter
     * @param adapterName   The AdapterName
     * @param helperFName	name of a html help file name
     * @param Client        The client to serve
     * @param params		a parameter set describing the optimizer module
     * @param optimizerExpert	 set to true if setting the optimizer is an expert option being hidden from the gui
     * @param noGUIStatOut	if null, statistics with GUI are used, else the standalone statistics with given output filename.
     */
    public GenericModuleAdapter(String adapterName, String helperFName, MainAdapterClient Client, InterfaceGOParameters params, boolean optimizerExpert, String noGUIStatOut) {
        super (Client);
        if (TRACE) System.out.println("Constructor GenericModuleAdapter  --> start");
        m_RemoteThis        = this;
        m_AdapterName       = adapterName;
        m_MainAdapterClient = Client;
        helperFilename		= helperFName;

        if (noGUIStatOut==null) {
        	m_StatisticsModul   = new StatisticsWithGUI(Client);
        } else {
        	m_StatisticsModul	= new StatisticsStandalone(noGUIStatOut);
        }
        m_Processor         = new Processor(m_StatisticsModul,this, params);
        
        // the statistics want to be informed if the strategy or the optimizer (which provide statistical data as InterfaceAdditionalInformer) change.
        // THIS is now done directly in the constructor of a Processor
//        if (m_StatisticsModul.getStatisticsParameter() instanceof InterfaceNotifyOnInformers) 
//        	params.addInformableInstance((InterfaceNotifyOnInformers)m_StatisticsModul.getStatisticsParameter());
        // this prevents the optimizer property to be shown by the GOE if optimizerExpert is true
    	GenericObjectEditor.setExpertProperty(params.getClass(), "optimizer", optimizerExpert);
       
    	((Processor)m_Processor).start();
        if (TRACE) System.out.println("Constructor GenericModuleAdapter <-- end");
    }
    
    /** 
     * Constructor of the ModuleAdapter. Convenience constructor with GUI.
     * 
     * @param adapterName   The AdapterName
     * @param helperFName	name of a html help file name
     * @param Client        The client to serve
     * @param params		a parameter set describing the optimizer module
     * @param optimizerExpert	 set to true if setting the optimizer is an expert option being hidden from the gui
     */
    public GenericModuleAdapter(String adapterName, String helperFName, MainAdapterClient Client, InterfaceGOParameters params, boolean optimizerExpert) {
    	this(adapterName, helperFName, Client, params, optimizerExpert, null);
    }
    
    /** 
     * This method returns a newly created GUI element containing the EvA button panel
     * and two JParaPanel instances representing the module parameters (e.g. GO) 
     * and the statistics parameters.
     * If the statistics are not of type StatisticsWithGUI, it is assumed that no GUI is
     * desired (and this method should not be called).
     * 
     * @see JParaPanel
     * @see EvAModuleButtonPanelMaker
     * @see StatisticsWithGUI
     * @return the EvATabbedFrameMaker
     */
    public EvATabbedFrameMaker getModuleFrame() {
    	if (TRACE) System.out.println("GenericModulAdapter.getModuleFrame");
    	if (!(m_StatisticsModul instanceof StatisticsWithGUI)) {
    		System.err.println("Error: Unable to create Frame when startet with noGUI option (GenericModuleAdapter)!");
    		return null;
    	}
        EvATabbedFrameMaker frmMkr = new EvATabbedFrameMaker();

        InterfaceStatisticsParameter Stat             = ((StatisticsWithGUI)m_StatisticsModul).getStatisticsParameter();
        EvAModuleButtonPanelMaker ButtonPanel      = new EvAModuleButtonPanelMaker(m_RemoteThis,((Processor)m_Processor).isOptRunning());
        ButtonPanel.setHelperFilename(helperFilename);
        frmMkr.addPanelMaker(ButtonPanel);
        InterfaceGOParameters goParams = ((Processor)m_Processor).getGOParams();
        if (TRACE) System.out.println("parameters are of type "+goParams.getClass());
        // TODO do we really need proxies here?
        if (m_RMI && !Proxy.isProxyClass(goParams.getClass())) frmMkr.addPanelMaker(paramPanel = new JParaPanel( RMIProxyLocal.newInstance(goParams), goParams.getName()));
        else frmMkr.addPanelMaker(paramPanel = new JParaPanel(goParams, goParams.getName()));
        if (m_RMI && !Proxy.isProxyClass(Stat.getClass())) frmMkr.addPanelMaker(new JParaPanel( RMIProxyLocal.newInstance(Stat), Stat.getName()));
        else frmMkr.addPanelMaker(new JParaPanel(Stat, Stat.getName()));

        jobList = new EvAJobList(new EvAJob[]{});
        jobList.setModule(this);
        jobList.addTextListener((AbstractStatistics) ((Processor)m_Processor).getStatistics());
//        if (m_RMI && !Proxy.isProxyClass(Stat.getClass())) frmMkr.addPanelMaker(new JParaPanel( RMIProxyLocal.newInstance(jobList), jobList.getName()));
//        else frmMkr.addPanelMaker(new JParaPanel(jobList, jobList.getName()));
        if (m_RMI && !Proxy.isProxyClass(Stat.getClass())) jobPanel = new JParaPanel( RMIProxyLocal.newInstance(jobList), jobList.getName());
        else jobPanel = new JParaPanel(jobList, jobList.getName());
        
        frmMkr.addPanelMaker(jobPanel);
        
        ((Processor)m_Processor).getGOParams().addInformableInstance(frmMkr);
        return frmMkr;
    }
    
    @Override
	public void performedStart(String infoString) {
		super.performedStart(infoString);
		EvAJob job = scheduleJob();
		((AbstractStatistics)(((Processor)m_Processor).getStatistics())).addDataListener(job);
	}
    
    @Override
	public void performedStop() {
		super.performedStop();
	}
    
	public static String getName() {
    	return null;
    }

    /**
     * Return the statistics module instance of this module.
     * @return
     */
    public AbstractStatistics getStatistics() {
    	return m_StatisticsModul;
    }
    
	public EvAJob scheduleJob() {
		EvAJob job = jobList.addJob(((Processor)m_Processor).getGOParams(), (AbstractStatistics)(((Processor)m_Processor).getStatistics()));
		jobPanel.getEditor().setValue(jobList);
		return job;
	}

	@Override
	public void setGOParameters(InterfaceGOParameters goParams) {
		super.setGOParameters(goParams);
		paramPanel.getEditor().setValue(goParams);
	}
}
