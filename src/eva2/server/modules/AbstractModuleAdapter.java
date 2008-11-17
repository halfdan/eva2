package eva2.server.modules;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;

import wsi.ra.jproxy.MainAdapterClient;
import wsi.ra.jproxy.RemoteStateListener;
import eva2.gui.JTabbedModuleFrame;
import eva2.gui.LogPanel;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfaceProcessor;
/*==========================================================================*
* ABSTRACT CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
abstract public class AbstractModuleAdapter implements ModuleAdapter, Serializable {
  public static boolean TRACE = false;
  private static int m_InstanceCounter;
  protected int m_Instance;
  protected String m_AdapterName;
  protected InterfaceProcessor m_Processor;
  protected String m_myHostName = "not defined";
  protected boolean m_Connection = true;
  protected ModuleAdapter m_RemoteThis = null;
  protected boolean m_RMI = true;
  protected MainAdapterClient m_MainAdapterClient; // connection to client
  private Vector<RemoteStateListener> m_RemoteStateListeners;
  protected LogPanel	logPanel = null;
  
  /**
   *
   */
  abstract public JTabbedModuleFrame getModuleFrame();
  /**
   *
   */
  protected AbstractModuleAdapter(MainAdapterClient Client) {
    if (TRACE) System.out.println("AbstractModuleAdapter.AbstractModuleAdapter()");
    m_InstanceCounter++;
    m_Instance = m_InstanceCounter;
    if (TRACE) System.out.println ("AbstractModuleAdapter Nr. "+m_InstanceCounter +" on EvAServer");

    if (Client != null) {
		try {
			m_myHostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			System.out.println("InetAddress.getLocalHost().getHostAddress() --> ERROR" + e.getMessage());
		}
    } else m_myHostName = "localhost";
    
    if ((Client==null) || Client.getHostName().equals(m_myHostName)) {
        m_RMI = false;
    } else {// we use RMI
    	m_RMI = true;
    }
    m_RemoteStateListeners = new Vector<RemoteStateListener>();
  }
  
  /**
   * From the interface RemoteStateListener. Added this method to make progress bar possible.
   */
  public void updateProgress(final int percent, String msg) {
	  if (TRACE) System.out.println("AbstractModuleAdapter::updateProgress");
	  for (RemoteStateListener listener : m_RemoteStateListeners) {
		  listener.updateProgress(percent, msg);
	  }
  }
  
  /**
   *
   */
  public String getAdapterName() {
    return m_AdapterName;
  }
  /**
   *
   */
  public void startOpt() {
    if (TRACE) System.out.println("Module AbstractModuleAdapter on EvA-Server StartOpt called:" );
    m_Processor.startOpt();
  }
  /**
   *
   */
  public void restartOpt() {
    if (TRACE) System.out.println("Module AbstractModuleAdapter on EvA-Server ReStartOpt called:" );
    m_Processor.restartOpt();
  }
  /**
   *
   */
  public void stopOpt() {
    if (TRACE) System.out.println("Module AbstractModuleAdapter on EvA-Server StopOpt called:" );
    m_Processor.stopOpt();	// This means user break
  }
  
  public boolean hasPostProcessing() {
	  return ((m_Processor instanceof Processor) && ((Processor)m_Processor).getGOParams().getPostProcessParams().isDoPostProcessing());
  }
  
  public boolean startPostProcessing() {
	  if (hasPostProcessing() && ((Processor)m_Processor).getGOParams().getPostProcessParams().isDoPostProcessing()) {
		  ((Processor)m_Processor).performPostProcessing();
		  return true;
	  } else return false;
  }
  
  public InterfaceGOParameters getGOParameters() {
	  if ((m_Processor != null) && (m_Processor instanceof Processor)) {
		  return ((Processor)m_Processor).getGOParams();
	  } else return null;
  }
  
  public boolean isOptRunning() {
	  if ((m_Processor != null) && (m_Processor instanceof Processor)) {
		  return ((Processor)m_Processor).isOptRunning();
	  } else return false;
  }
  
  /**
   *
   */
  public void runScript() {

  }
  /**
   *
   */
  public void addRemoteStateListener(RemoteStateListener x) {
    if (TRACE) System.out.println("module adapter on Server addRemoteStateListener called:" );
    m_RemoteStateListeners.add(x);
  }
  /**
   *
   */
  public void setConnection (boolean flag)  {
    if (TRACE) System.out.println("module adapter on Server setConnection "+flag+" called:" );
    m_Connection = flag;
  }
  
  public boolean hasConnection() {
	  return m_Connection;
  }
  /**
   *
   */
  public void setRemoteThis (ModuleAdapter x) {
  if (TRACE) System.out.println("module adapter on Server setRemoteThis called:" );
    m_RemoteThis = x;
  }
  /**
   *
   */
  public String getHostName () {
    if (TRACE) System.out.println("module adapter on Server getHostName called:"+m_myHostName );
    return m_myHostName;
  }
  /**
   *
   */
  public void performedStop () {
	  if (TRACE) System.out.println("AbstractModuleAdapter::performedStop");
	  for (RemoteStateListener listener : m_RemoteStateListeners) {
		  listener.performedStop();
	  }
//	  if (logPanel != null) logPanel.logMessage("Stopped optimization run");
  }

  public void performedStart(String infoString) {
	  if (TRACE) System.out.println("AbstractModuleAdapter::performedStart");
	  for (RemoteStateListener listener : m_RemoteStateListeners) {
		  listener.performedStart(infoString);
	  }
//	  if (logPanel != null) logPanel.logMessage("Started optimization " + m_Processor.getInfoString());
  }

  public void performedRestart(String infoString) {
	  if (TRACE) System.out.println("AbstractModuleAdapter::performedRestart");
	  for (RemoteStateListener listener : m_RemoteStateListeners) {
		  listener.performedRestart(infoString);
	  }
//	  if (logPanel != null) logPanel.logMessage("Restarted optimization run");
  }
  
}