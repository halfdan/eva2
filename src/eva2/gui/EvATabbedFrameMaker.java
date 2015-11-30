package eva2.gui;

import eva2.optimization.InterfaceNotifyOnInformers;
import eva2.problems.InterfaceAdditionalPopulationInformer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Produces the main EvA2 frame and a tool bar instance.
 */
public class EvATabbedFrameMaker implements Serializable, PanelMaker, InterfaceNotifyOnInformers {

    private static final Logger LOGGER = Logger.getLogger(EvATabbedFrameMaker.class.getName());
    private static final long serialVersionUID = 2637376545826821423L;
    private ArrayList<PanelMaker> pmContainer = null;
    private JExtToolBar extToolBar;
    EvAModuleButtonPanelMaker butPanelMkr = null;
    private JTabbedPane tabbedPane;

    public EvATabbedFrameMaker() {
        pmContainer = null;
    }

    public void addPanelMaker(PanelMaker pm) {
        if (pmContainer == null) {
            pmContainer = new ArrayList<>(2);
        }
        pmContainer.add(pm);
    }

    @Override
    public JPanel makePanel() {
        JPanel tabControlPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.fill = GridBagConstraints.VERTICAL;
        gbConstraints.gridy = 0;

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        //tabbedPane.setUI(new eva2.gui.utils.CustomTabbedPaneUI());
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        
        /* This toolbar will hold the closed tabs */
        JToolBar tabToolBar = new JToolBar(JToolBar.VERTICAL);
        tabToolBar.setFloatable(false);

        /* ToDo: The control buttons shouldn't be added here.. */
        extToolBar = new JExtToolBar();
        extToolBar.setFloatable(false);

        for (PanelMaker element : pmContainer) {
            JComponent panel = element.makePanel();
            if (element instanceof EvAModuleButtonPanelMaker) {
                extToolBar.add(panel);
                butPanelMkr = (EvAModuleButtonPanelMaker) element;
            } else if (element instanceof JParaPanel) {
                tabbedPane.addTab(((JParaPanel) element).getName(), panel);
            }
        }

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTabComponentAt(i, new ClosableTabComponent(tabbedPane, tabToolBar));
        }

        gbConstraints.weighty = 1.0;
        gbConstraints.gridx = 0;
        tabControlPanel.add(tabToolBar, gbConstraints);
        gbConstraints.gridx = 1;
        tabControlPanel.add(tabbedPane, gbConstraints);
        tabbedPane.validate();
        return tabControlPanel;
    }

    /**
     * @return The toolbar with control buttons
     * @deprecated
     */
    public JExtToolBar getToolBar() {
        return extToolBar;
    }

    /**
     * Emulate pressing the start button.
     */
    public void onUserStart() {
        if (butPanelMkr != null) {
            butPanelMkr.onUserStart();
        } else {
            System.err.println("Error: button panel was null (EvATabbedFrameMaker)");
        }
    }

    public void refreshPanels() {
        for (PanelMaker jpp : pmContainer) {
            if (jpp instanceof JParaPanel) {
                ((JParaPanel) jpp).propertyEditor.setValue(((JParaPanel) jpp).propertyEditor.getValue());
            }
        }
    }

    @Override
    public void setInformers(List<InterfaceAdditionalPopulationInformer> informers) {
        // if the informers have changed, update the GUI element which displays them
        try {
            JParaPanel statsPan = getStatsPanel();
            if (statsPan.propertyEditor != null) {
                // really update the contents of the stats panel
                statsPan.propertyEditor.setValue(statsPan.propertyEditor.getValue());
            }
        } catch (Exception e) {
            System.err.println("Failed to update statistics panel from " + this.getClass());
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public JParaPanel getOptimizationParametersPanel() {
        try {
            JParaPanel sP = (JParaPanel) pmContainer.get(1);
            return sP;
        } catch (Exception e) {
            System.err.println("Failed to get OptimizationParameters panel from " + this.getClass());
        }
        return null;
    }

    public JParaPanel getStatsPanel() {
        try {
            JParaPanel sP = (JParaPanel) pmContainer.get(2);
            return sP;
        } catch (Exception e) {
            System.err.println("Failed to get statistics panel from " + this.getClass());
        }
        return null;
    }
}

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
class ClosableTabComponent extends JPanel {
    private final JTabbedPane pane;
    private final JToolBar toolBar;

    public ClosableTabComponent(final JTabbedPane pane, final JToolBar toolBar) {
        super(new FlowLayout(FlowLayout.LEADING, 0, 0));

        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        this.toolBar = toolBar;
        this.toolBar.setVisible(false);
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            @Override
            public String getText() {
                int index = pane.indexOfTabComponent(ClosableTabComponent.this);
                if (index != -1) {
                    return pane.getTitleAt(index);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();

        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Hide this Tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ClosableTabComponent.this);
            if (i != -1) {
                final String tabTitle = pane.getTitleAt(i);
                final Component tabPane = pane.getComponentAt(i);
                final int tabPosition = i;

                pane.remove(i);
                if (pane.getTabCount() == 0) {
                    pane.setVisible(false);
                }
                /* Create a button to be shown in the ToolBar */
                JButton tabButton = new JButton(tabTitle);
                /* Rotate it by -90° */
                tabButton.setUI(new eva2.gui.utils.VerticalButtonUI(-90));
                tabButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        /* Add the Tab Panel again */
                        // ToDo: Fix indexing problem                        
                        pane.insertTab(tabTitle, null, tabPane, "", tabPosition);                        
                        /* Set the tab component (closable) */
                        pane.setTabComponentAt(tabPosition, ClosableTabComponent.this);
                        pane.setVisible(true);
                        /* Remove the Button */
                        toolBar.remove((Component) e.getSource());
                        /* If the Button was the last one, hide ToolBar again */
                        if (toolBar.getComponentCount() == 0) {
                            toolBar.setVisible(false);
                        }
                    }
                });
                /* Add it to the ToolBar */
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }
                toolBar.add(tabButton);
            }
        }

        //we don't want to update UI for this button
        @Override
        public void updateUI() {
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}