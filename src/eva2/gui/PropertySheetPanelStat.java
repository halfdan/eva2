package eva2.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 *
 */
public class PropertySheetPanelStat extends JPanel implements Serializable {
    private Object[] values;
    private JCheckBoxFlag[] views;
    private JLabel[] labels;
    private boolean[] flags;

    /**
     * Creates the property sheet panel.
     */
    public PropertySheetPanelStat() {
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    /**
     * A support object for handling property change listeners
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public synchronized void setTarget(String[] names, boolean[] flag) {
        int componentOffset = 0;
        // Close any child windows at this point
        removeAll();
        GridBagLayout gbLayout = new GridBagLayout();
        setLayout(gbLayout);
        setVisible(false);

        values = new Object[flag.length];
        views = new JCheckBoxFlag[flag.length];
        labels = new JLabel[names.length];

        for (int i = 0; i < names.length; i++) {
            labels[i] = new JLabel(names[i], SwingConstants.RIGHT);
            labels[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
            views[i] = new JCheckBoxFlag(flag[i]);
            GridBagConstraints gbConstraints = new GridBagConstraints();
            gbConstraints.anchor = GridBagConstraints.EAST;
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.gridy = i + componentOffset;
            gbConstraints.gridx = 0;
            gbLayout.setConstraints(labels[i], gbConstraints);
            add(labels[i]);
            JPanel newPanel = new JPanel();
            newPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));

            newPanel.setLayout(new BorderLayout());
            newPanel.add(views[i], BorderLayout.CENTER);
            gbConstraints = new GridBagConstraints();
            gbConstraints.anchor = GridBagConstraints.WEST;
            gbConstraints.fill = GridBagConstraints.BOTH;
            gbConstraints.gridy = i + componentOffset;
            gbConstraints.gridx = 1;
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
        boolean[] ret = new boolean[this.views.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = views[i].isSelected();
        }
        return ret;
    }
}

/**
 *
 */
class JCheckBoxFlag extends JCheckBox {

    private boolean flag = true;

    public JCheckBoxFlag(boolean flag) {
        super();
        this.flag = flag;
        addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    JCheckBoxFlag.this.flag = true;
                }
                if (evt.getStateChange() == ItemEvent.DESELECTED) {
                    JCheckBoxFlag.this.flag = false;
                }
            }
        });

    }
}
