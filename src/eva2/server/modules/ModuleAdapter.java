package eva2.server.modules;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 272 $
 *            $Date: 2007-11-21 18:06:36 +0100 (Wed, 21 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.gui.EvATabbedFrameMaker;
import eva2.gui.LogPanel;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.stat.InterfaceTextListener;
import eva2.tools.jproxy.RemoteStateListener;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface ModuleAdapter extends RemoteStateListener {
  public EvATabbedFrameMaker getModuleFrame();
  public void startOpt(); // called from client
  public void restartOpt();
  public void stopOpt();
  public void runScript();
  /**
   * Return true if post processing is available in principle, else false.
   * @return true if post processing is available in principle, else false
   */
  public boolean hasPostProcessing();
  /**
   * Return true if post processing was performed, else false.
   * @return true if post processing was performed, else false
   */
  public boolean startPostProcessing();
  public void addRemoteStateListener(RemoteStateListener x);
  public String getAdapterName();
  public void setConnection(boolean flag);
  public boolean hasConnection();
  public void setRemoteThis(ModuleAdapter x);
  public String getHostName();
}