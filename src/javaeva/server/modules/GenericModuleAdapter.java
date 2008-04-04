package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.stat.StatisticsWithGUI;
import javaeva.server.stat.InterfaceStatisticsParameter;
import javaeva.server.EvAServer;
import javaeva.gui.GenericObjectEditor;
import javaeva.gui.JTabbedModuleFrame;
import javaeva.gui.JModuleGeneralPanel;
import javaeva.gui.JParaPanel;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RMIProxyLocal;

public class GenericModuleAdapter extends AbstractModuleAdapter implements Serializable {

    private StatisticsWithGUI     m_StatisticsModul;
    public String 				helperFilename;

    /** 
     * Constructor of the ModuleAdapter
     * @param adapterName   The AdapterName
     * @param helperFName	name of a html help file name
     * @param Client        The client to serve
     * @param params		a parameter set describing the optimizer module
     * @param optimizerExpert	 set to true if setting the optimizer is an expert option being hidden from the gui
     */
    public GenericModuleAdapter(String adapterName, String helperFName, MainAdapterClient Client, InterfaceGOParameters params, boolean optimizerExpert) {
        super (Client);
        if (TRACE) System.out.println("Constructor GenericModuleAdapter  --> start");
        m_RemoteThis        = this;
        m_AdapterName       = adapterName;
        m_MainAdapterClient = Client;
        helperFilename		= helperFName;

        m_StatisticsModul   = new StatisticsWithGUI(Client);
        m_Processor         = new Processor((StatisticsWithGUI)m_StatisticsModul,this, params);

        // this prevents the optimizer property to be shown by the GOE if optimizerExpert is true
    	GenericObjectEditor.setExpertProperty(params.getClass(), "optimizer", optimizerExpert);
       
    	((Processor)m_Processor).start();
        if (TRACE) System.out.println("Constructor GenericModuleAdapter <-- end");
    }
    
    /** This method returns a GUI element
     * @return the JTabbedModulFrame
     */
    public JTabbedModuleFrame getModuleFrame() {
        if (TRACE) System.out.println("GenericModulAdapter.getModuleFrame");
        ArrayList<Object> GUIContainer    = new ArrayList<Object>();
        InterfaceStatisticsParameter Stat             = ((StatisticsWithGUI)m_StatisticsModul).getStatisticsParameter();
        JModuleGeneralPanel ButtonPanel      = new JModuleGeneralPanel(m_RemoteThis,((Processor)m_Processor).isOptRunning());
        ButtonPanel.setHelperFilename(helperFilename);
        GUIContainer.add(ButtonPanel);
        InterfaceGOParameters Para = ((Processor)m_Processor).getGOParams();
        if (TRACE) System.out.println("parameters are of type "+Para.getClass());
        // TODO do we really need proxies here?
        if (m_RMI && !Proxy.isProxyClass(Para.getClass())) GUIContainer.add(new JParaPanel( RMIProxyLocal.newInstance(Para), Para.getName()));
        else GUIContainer.add(new JParaPanel(Para, Para.getName()));
        if (m_RMI && !Proxy.isProxyClass(Stat.getClass())) GUIContainer.add(new JParaPanel( RMIProxyLocal.newInstance(Stat), Stat.getName()));
        else GUIContainer.add(new JParaPanel(Stat, Stat.getName()));

        return new JTabbedModuleFrame(m_RemoteThis,getName(),m_myHostName+EvAServer.m_NumberOfVM,GUIContainer);
    }
    
    public static String getName() {
    	return null;
    }

}
