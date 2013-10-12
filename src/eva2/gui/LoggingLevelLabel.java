/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eva2.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a JLabel that shows the current logging level
 * depending on the Logger provided. It creates a popup
 * menu on left-click to change the logging level. Logging
 * levels are as specified by "java.util.logging.Level.*"
 *
 * @author becker
 */
public final class LoggingLevelLabel extends JLabel {
    private JPopupMenu menu;
    private String[] options;
    private static final Logger LOGGER = Logger.getLogger(LoggingLevelLabel.class.getName());

    public LoggingLevelLabel() {
        options = new String[]{"Info", "Warning", "Severe", "Fine", "Finer", "Finest", "All"};


        setToolTipText("Click to change current logging level");
        createPopupMenu();
        updateText();
    }

    private void createPopupMenu() {
        this.menu = new JPopupMenu();
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(final MouseEvent ev) {
                menu.show(ev.getComponent(), ev.getX(), ev.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JMenuItem menuItem;
        ActionListener menuAction = new MenuActionListener();
        for (String option : options) {
            menuItem = new JMenuItem(option);
            menuItem.addActionListener(menuAction);
            menu.add(menuItem);
        }
    }

    /**
     * Updates the visible text on the label.
     */
    private void updateText() {
        /* Get the current logging Level */
        Level lvl = LOGGER.getLevel();
        /* Level could be null, fetch parent level */
        if (lvl == null) {
            lvl = LOGGER.getParent().getLevel();
        }
        /* Show the updated text */
        setText("<html><b>Level</b>: " + lvl.getName());
    }

    /**
     * Sets the level of the logger to a new level.
     *
     * @param level The new level for the logger
     */
    private void setLoggerLevel(Level level) {
        // Recursively set logging level for all classes under eva2
        Logger.getLogger("eva2").setLevel(level);
        LOGGER.log(Level.INFO, "Logging Level changed to {0}", level.getName());
    }

    /**
     *
     */
    class MenuActionListener implements ActionListener {

        @Override
        public void actionPerformed(final ActionEvent ev) {
            JMenuItem menuItem = (JMenuItem) ev.getSource();
            String levelName = menuItem.getText();

            try {
                Level level = Level.parse(levelName.toUpperCase());
                LoggingLevelLabel.this.setLoggerLevel(level);
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, "Could not determine new logging level!", ex);
            }

            LoggingLevelLabel.this.updateText();
        }
    }
}
