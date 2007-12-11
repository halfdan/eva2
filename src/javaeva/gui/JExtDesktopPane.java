package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javax.swing.*;
import java.beans.PropertyVetoException;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
/**
 *
 */
public class JExtDesktopPane extends JDesktopPane{
  private ActionListener m_actMenuFrame;
  private ExtDesktopManager m_manager;
  public JExtMenu m_mnuWindow;
  private ExtAction actWindowTileVert;
  private ExtAction actWindowTileHorz;
  private ExtAction actWindowOverlap;
  private ExtAction actWindowArrangeIcons;
  private ExtAction actWindowList;
  public final static int WINDOW_TILEVERT = 0;
  public final static int WINDOW_TILEHORZ = 1;
  public final static int WINDOW_OVERLAP = 2;
  public final static int WINDOW_ARRANGEICONS = 3;
  public final static int WINDOW_LIST = 4;
  public final static int TITLEBAR_HEIGHT = 25;
  /**
   *
   */
  public JExtDesktopPane(){
    super();
    m_mnuWindow = new JExtMenu("&Windows");
    m_manager = new ExtDesktopManager(this);
    setDesktopManager(m_manager);

    m_actMenuFrame = new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if(!(e.getSource() instanceof JMenuItem)) return;
	JInternalFrame frame = (JInternalFrame)((JMenuItem)e.getSource()).getClientProperty(ExtDesktopManager.FRAME);
        selectFrame(frame);
      }

    };

    m_mnuWindow.add(actWindowTileVert = new ExtAction("&Nebeneinander", "Ordnet die Fenster nebeneinander an"){
      public void actionPerformed(ActionEvent e){
        tileWindows(SwingConstants.HORIZONTAL);
      }
    });

    m_mnuWindow.add(actWindowTileHorz = new ExtAction("&Untereinander", "Ordnet die Fenster untereinander an"){
      public void actionPerformed(ActionEvent e){
        tileWindows(SwingConstants.VERTICAL);
      }
    });

    m_mnuWindow.add(actWindowOverlap = new ExtAction("Ü&berlappend", "Ordnet die Fenster überlappend an"){
      public void actionPerformed(ActionEvent e){
        overlapWindows();
      }
    });

    m_mnuWindow.add(actWindowArrangeIcons = new ExtAction("&Symbole anordnen", "Ordnet die Symbole auf dem Desktop an"){
      public void actionPerformed(ActionEvent e){
      }
    });

    m_mnuWindow.add(actWindowList = new ExtAction("Fenster&liste...", "Zeigt eine Liste aller Fenster an", KeyStroke.getKeyStroke(KeyEvent.VK_0, Event.ALT_MASK)){
      public void actionPerformed(ActionEvent e){
        Vector l = new Vector();
        JInternalFrame frames[] = getAllFrames();

        for(int i = 0; i < frames.length; i++)
         l.add(frames[i].getTitle());

        JList list = new JList(l);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(pane.getPreferredSize().width, 150));

        if(JOptionPane.showOptionDialog(JExtDesktopPane.this, pane, "Fenster auswählen", JOptionPane.OK_CANCEL_OPTION,
           JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
         if(list.getSelectedIndex() != -1) selectFrame(frames[list.getSelectedIndex()]);
      }
    });

    m_mnuWindow.addSeparator();
    m_manager.WINDOW_LIST_START = 6;
  }

  public ExtAction getWindowAction(int action){
    switch(action){
      case WINDOW_TILEVERT: return actWindowTileVert;
      case WINDOW_TILEHORZ: return actWindowTileHorz;
      case WINDOW_OVERLAP: return actWindowOverlap;
      case WINDOW_ARRANGEICONS: return actWindowArrangeIcons;
      case WINDOW_LIST: return actWindowList;
    }
    return null;
  }

  public void overlapWindows(){
    final int minWidth = 150, minHeight = 100;
    int fWidth, fHeight,
     oCount, i, indent;

    JInternalFrame[] frames = getOpenFrames();
    if(frames.length == 0) return;

    oCount = Math.min(frames.length, Math.min((getWidth() - minWidth) / TITLEBAR_HEIGHT + 1, (getHeight() - minHeight) / TITLEBAR_HEIGHT + 1));
    fWidth = getWidth() - (oCount - 1) * TITLEBAR_HEIGHT;
    fHeight = getHeight() - (oCount - 1) * TITLEBAR_HEIGHT;

    indent = 0;
    for(i = 0; i < frames.length; i++){
      frames[frames.length - i - 1].setLocation(indent * TITLEBAR_HEIGHT, indent * TITLEBAR_HEIGHT);
      frames[frames.length - i - 1].setSize(fWidth, fHeight);
      indent = (i + 1) % oCount == 0 ? 0 : indent + 1;
    }
  }
  /**
   *
   */
  public void tileWindows(int orientation){
    int rows, cols,
     rHeight, cWidth,
     row, col,
     i;

    JInternalFrame[] frames = getOpenFrames();
    if(frames.length == 0) return;

    if(orientation == SwingConstants.HORIZONTAL){
      rows = (int)(Math.rint(Math.sqrt(frames.length) - 0.49));
      cols = frames.length / rows;
      rHeight = getHeight() / rows;
      cWidth = getWidth() / cols;
      row = col = 0;
      for(i = 0; i < frames.length; i++){
        frames[i].setLocation(col * cWidth, row * rHeight);
        frames[i].setSize(cWidth, rHeight);
        if(col == cols - 1){
          row++;
          col = 0;
        }
        else col++;
        if(row > 0 && frames.length - i - (cols + 1) * (rows - row) > 0){
          cols++;
          cWidth = getWidth() / cols;
        }
      }
    }
    else if(orientation == SwingConstants.VERTICAL){
      cols = (int)(Math.rint(Math.sqrt(frames.length) - 0.49));
      rows = frames.length / cols;
      cWidth = getWidth() / cols;
      rHeight = getHeight() / rows;
      col = row = 0;
      for(i = 0; i < frames.length; i++){
        frames[i].setLocation(col * cWidth, row * rHeight);
        frames[i].setSize(cWidth, rHeight);
        if(row == rows - 1){
          col++;
          row = 0;
        }
        else row++;
        if(col > 0 && frames.length - i - (rows + 1) * (cols - col) > 0){
          rows++;
          rHeight = getHeight() / rows;
        }
      }
    }
  }

  public JInternalFrame[] getOpenFrames(){
    JInternalFrame[] result;
    Vector vResults = new Vector(10);
    Component tmp;

    for(int i = 0; i < getComponentCount(); i++){
      tmp = getComponent(i);
      if(tmp instanceof JInternalFrame) vResults.addElement(tmp);
    }

    result = new JInternalFrame[vResults.size()];
    vResults.copyInto(result);

    return result;
  }
  public int getFrameCount(){
    return getComponentCount(new ComponentFilter(){
      public boolean accept(Component c){
	return c instanceof JInternalFrame ||
	 (c instanceof JInternalFrame.JDesktopIcon &&
	 ((JInternalFrame.JDesktopIcon)c).getInternalFrame() != null);
      }
    });
  }
  public int getComponentCount(ComponentFilter c){
    int result = 0;
    for(int i = 0; i < getComponentCount(); i++) if(c.accept(getComponent(i))) result++;
    return result;
  }
  public void selectFrame(JInternalFrame f){
    if(f != null){
      try{
        if(f.isIcon()) f.setIcon(false);
        else f.setSelected(true);
      }
      catch(PropertyVetoException exc){}
    }
  }
  public void addImpl(Component comp, Object constraints, int index){
    super.addImpl(comp, constraints, index);
    //System.out.println("JExtDesktopPane.addImpl");
    if(comp instanceof JDocFrame){
      JDocFrame f = (JDocFrame)comp;
      int frameIndex = m_mnuWindow.getItemCount() - m_manager.WINDOW_LIST_START + 1;
      if(f.getClientProperty(ExtDesktopManager.INDEX) != null) return;
      f.putClientProperty(ExtDesktopManager.INDEX, new Integer(frameIndex));
      JMenuItem m = new JMenuItem((frameIndex < 10 ? frameIndex + " " : "") + f.getTitle());
      if(frameIndex < 10){
        m.setMnemonic((char)(0x30 + frameIndex));
        m.setAccelerator(KeyStroke.getKeyStroke(0x30 + frameIndex, Event.ALT_MASK));
      }
      m.setToolTipText("Shows the window " + f.getTitle() + " an");
      m.putClientProperty(ExtDesktopManager.FRAME, f);
      m.addActionListener(m_actMenuFrame);
      m_mnuWindow.add(m);
    }
//    if(comp instanceof Plot){
//      Plot f = (Plot)comp;
//      int frameIndex = m_mnuWindow.getItemCount() - m_manager.WINDOW_LIST_START + 1;
//      if(f.getClientProperty(ExtDesktopManager.INDEX) != null) return;
//      f.putClientProperty(ExtDesktopManager.INDEX, new Integer(frameIndex));
//      JMenuItem m = new JMenuItem((frameIndex < 10 ? frameIndex + " " : "") + f.getTitle());
//      if(frameIndex < 10){
//        m.setMnemonic((char)(0x30 + frameIndex));
//        m.setAccelerator(KeyStroke.getKeyStroke(0x30 + frameIndex, Event.ALT_MASK));
//      }
//      m.setToolTipText("Shows the window " + f.getTitle() + " an");
//      m.putClientProperty(ExtDesktopManager.FRAME, f);
//      m.addActionListener(m_actMenuFrame);
//      m_mnuWindow.add(m);
//    }
  }
  public JMenu getWindowMenu(){
    return m_mnuWindow;
  }
}