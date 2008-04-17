package eva2.gui;
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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;

import eva2.tools.EVAHELP;
import sun.beans.editors.*;
import java.io.Serializable;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class PropertySheetPanelStat extends JPanel implements Serializable {
  public final static boolean TRACE = false;
  private Object[] m_Values;
  private JCheckBoxFlag[] m_Views;
  private JLabel[] m_Labels;
  private boolean[] m_flag;
  /**
   * Creates the property sheet panel.
   */
  public PropertySheetPanelStat() {
    //    setBorder(BorderFactory.createLineBorder(Color.red));
    setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
  }
  /** A support object for handling property change listeners */
  private PropertyChangeSupport m_support = new PropertyChangeSupport(this);



  public synchronized void setTarget(String[] names, boolean[] flag) {
    int componentOffset = 0;
    // Close any child windows at this point
    removeAll();
    GridBagLayout gbLayout = new GridBagLayout();
    setLayout(gbLayout);
    setVisible(false);

    int rowHeight = 12;
    JTextArea jt = new JTextArea();
    JScrollPane js = null;

    m_Values = new Object[flag.length];
    m_Views = new JCheckBoxFlag[flag.length];
    m_Labels = new JLabel[names.length];

    for (int i = 0; i < names.length; i++) {
      m_Labels[i] = new JLabel(names[i], SwingConstants.RIGHT);
      m_Labels[i].setBorder(BorderFactory.createEmptyBorder(10,10,0,5));
      m_Views[i] = new JCheckBoxFlag(flag[i]);
      GridBagConstraints gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.EAST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = i+componentOffset;     gbConstraints.gridx = 0;
      gbLayout.setConstraints(m_Labels[i], gbConstraints);
      add(m_Labels[i]);
      JPanel newPanel = new JPanel();
      newPanel.setBorder(BorderFactory.createEmptyBorder(10,5,0,10));

      newPanel.setLayout(new BorderLayout());
      newPanel.add(m_Views[i], BorderLayout.CENTER);
      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.WEST;
      gbConstraints.fill = GridBagConstraints.BOTH;
      gbConstraints.gridy = i+componentOffset;     gbConstraints.gridx = 1;
      gbConstraints.weightx = 100;
      gbLayout.setConstraints(newPanel, gbConstraints);
      add(newPanel);
    }
    validate();
    setVisible(true);
  }
  /**
   *
   */
  public boolean[] getState() {
    boolean[] ret = new boolean[this.m_Views.length];
    for (int i=0;i<ret.length;i++) {
      ret[i] = m_Views[i].isSelected();
    }
    return ret;
  }

}
/**
 *
 */
class JCheckBoxFlag extends JCheckBox {
  private boolean m_Flag = true;
  public JCheckBoxFlag (boolean flag) {
    super();
    m_Flag = flag;
    addItemListener(new ItemListener () {
        public void itemStateChanged (ItemEvent evt) {
          if (evt.getStateChange() == evt.SELECTED) {
            m_Flag = true;
          }
          if (evt.getStateChange() == evt.DESELECTED) {
          m_Flag = false;
          }
        }
    });

  }
}


