package javaeva.server.modules;
/*
 * Title:        JavaEvA
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
import javaeva.gui.JTabbedModuleFrame;
import javaeva.gui.LogPanel;
import javaeva.server.go.operators.postprocess.PostProcessParams;
import javaeva.server.stat.InterfaceTextListener;
import wsi.ra.jproxy.RemoteStateListener;
/*==========================================================================*
* INTERFACE DECLARATION
*==========================================================================*/
/**
 *
 */
public interface ModuleAdapter extends RemoteStateListener {
  public JTabbedModuleFrame getModuleFrame();
  public void startOpt(); // called from client
  public void restartOpt();
  public void stopOpt();
  public void runScript();
  public boolean hasPostProcessing();
  public void startPostProcessing();
  public void addRemoteStateListener(RemoteStateListener x);
  public String getAdapterName();
  public void setConnection(boolean flag);
  public boolean hasConnection();
  public void setRemoteThis(ModuleAdapter x);
  public String getHostName();
}