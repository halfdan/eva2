package eva2.gui;
//package javaeva.gui;
///*==========================================================================*
// * IMPORTS
// *==========================================================================*/
//import javaeva.client.EvAClient;
//import javax.swing.event.InternalFrameListener;
//import javax.swing.event.InternalFrameEvent;
//import javax.swing.JOptionPane;
//import java.awt.event.ActionEvent;
///*==========================================================================*
//* CLASS DECLARATION
//*==========================================================================*/
///**
// *
// */
//public class WindowCloseAction extends ExtAction implements InternalFrameListener{
//  private EvAClient m_App;
//  /**
//  *
//  */
//  public WindowCloseAction(String s,String toolTip,EvAClient App){
//    super(s, toolTip);
//    m_App = App;
//  }
//  /**
//  *
//  */
//  private void close(JDocFrame f){
//    if(f == null) return;
//    boolean doClose = false;
//    if(f.isChanged()){
//      switch(JOptionPane.showConfirmDialog(m_App.getDesktop(), "M�chten Sie die �nderungen an "
//              + f.getTitle() + " speichern?", "Frage", JOptionPane.YES_NO_CANCEL_OPTION)){
//        case JOptionPane.YES_OPTION:
//          System.out.println(f.getTitle() + " geschlossen, �nderungen gespeichert.");
//          doClose = true;
//          break;
//        case JOptionPane.NO_OPTION:
//          doClose = true;
//          break;
//        case JOptionPane.CANCEL_OPTION:;
//      }
//    }
//    else
//      doClose = true;
//    if(doClose) f.dispose();
//  }
//  /**
//  *
//  */
//  public void actionPerformed(ActionEvent e){
//    close((JDocFrame)((ExtDesktopManager)m_App.getDesktop().getDesktopManager()).getActiveFrame());
//  }
//  public void internalFrameOpened(InternalFrameEvent e){}
//  public void internalFrameClosed(InternalFrameEvent e){}
//  public void internalFrameIconified(InternalFrameEvent e){}
//  public void internalFrameDeiconified(InternalFrameEvent e){}
//  public void internalFrameActivated(InternalFrameEvent e){}
//  public void internalFrameDeactivated(InternalFrameEvent e){}
//  /**
//  *
//  */
//  public void internalFrameClosing(InternalFrameEvent e){
//    close((JDocFrame)e.getSource());
//  }
//}
