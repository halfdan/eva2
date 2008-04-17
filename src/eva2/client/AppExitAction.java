package eva2.client;


import javax.swing.*;

import eva2.gui.ExtAction;

import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 12.05.2003
 * Time: 18:28:54
 * To change this template use Options | File Templates.
 */
/**
 *
 */
class AppExitAction extends ExtAction implements WindowListener{
  public AppExitAction(String s, String toolTip, KeyStroke key){
    super(s, toolTip, key);
  }
  private void exit(){
    System.exit(1);
  }
  public void actionPerformed(ActionEvent e){exit();}
  public void windowOpened(WindowEvent e){}
  public void windowClosed(WindowEvent e){}
  public void windowIconified(WindowEvent e){ }
  public void windowDeiconified(WindowEvent e){ }
  public void windowActivated(WindowEvent e){ }
  public void windowDeactivated(WindowEvent e){ }
  public void windowClosing(WindowEvent e){exit();}
}
