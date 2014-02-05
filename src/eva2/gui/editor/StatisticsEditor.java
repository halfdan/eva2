package eva2.gui.editor;
/*
 *
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

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private PropertySheetPanelStat statPanel;
    private JScrollPane scrollPane;
    private JPanel panel;
    private GenericStatistics statistics;

    /**
     *
     */
    public StatisticsEditor() {
        super();
        statPanel = new PropertySheetPanelStat();
        statPanel.addPropertyChangeListener(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        propertyChangeSupport.firePropertyChange("", null, null);
                    }
                });
        scrollPane = new JScrollPane(statPanel);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(scrollPane, BorderLayout.CENTER);

    }

    /**
     *
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof GenericStatistics) {
            statistics = (GenericStatistics) value;
            statPanel.setTarget(statistics.getPropertyNames(), statistics.getState());
        }
        propertyChangeSupport.firePropertyChange("", null, null);
    }

    /**
     *
     */
    @Override
    public Object getValue() {
        System.out.println("getValue !!!!!!!!!!!!");
        statistics.setState(statPanel.getState());
        return statistics;
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
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
