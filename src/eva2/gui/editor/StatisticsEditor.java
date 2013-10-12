package eva2.gui.editor;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 194 $
 *            $Date: 2007-10-23 13:43:24 +0200 (Tue, 23 Oct 2007) $
 *            $Author: mkron $
 */

import eva2.gui.PropertySheetPanelStat;
import eva2.optimization.stat.GenericStatistics;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

public class StatisticsEditor implements PropertyEditor {

    private PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
    private PropertySheetPanelStat m_StatPanel;
    private JScrollPane m_ScrollPane;
    private JPanel m_Panel;
    private GenericStatistics m_Value;

    /**
     *
     */
    public StatisticsEditor() {
        super();
        m_StatPanel = new PropertySheetPanelStat();
        m_StatPanel.addPropertyChangeListener(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        m_Support.firePropertyChange("", null, null);
                    }
                });
        m_ScrollPane = new JScrollPane(m_StatPanel);
        m_Panel = new JPanel();
        m_Panel.setLayout(new BorderLayout());

        m_Panel.add(m_ScrollPane, BorderLayout.CENTER);

    }

    /**
     *
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof GenericStatistics) {
            m_Value = (GenericStatistics) value;
            m_StatPanel.setTarget(m_Value.getPropertyNames(), m_Value.getState());
        }
        m_Support.firePropertyChange("", null, null);
    }

    /**
     *
     */
    @Override
    public Object getValue() {
        System.out.println("getValue !!!!!!!!!!!!");
        m_Value.setState(m_StatPanel.getState());
        return m_Value;
    }

    /**
     *
     */
    @Override
    public String getJavaInitializationString() {
        return "null";
    }

    /**
     *
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     *
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        //String rep = EVAHELP.cutClassName(m_ElementClass.getName());
        gfx.drawString("StatisticeEditor", 2, fm.getHeight() + vpad - 3);
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
    }

    /**
     *
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     *
     */
    @Override
    public Component getCustomEditor() {
        return m_Panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.removePropertyChangeListener(l);
    }
}
