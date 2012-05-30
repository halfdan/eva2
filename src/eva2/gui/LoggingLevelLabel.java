/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eva2.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author becker
 */
public final class LoggingLevelLabel extends JLabel {
    private JPopupMenu menu;
    private String[] options;
    private String selected;
    private Logger logger;
    
    public LoggingLevelLabel(Logger logger) {
        options = new String[]{"Info", "Warning", "Severe", "Fine", "Finer", "Finest", "All"};
        
        this.logger = logger;
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

        for (String option : options) {
            JMenuItem menuItem = new JMenuItem(option);
            menuItem.addActionListener(new MenuActionListener());
            menu.add(menuItem);
        }
    }
    
    private void updateText() {
        /* Get the current logging Level */
        Level lvl = logger.getLevel();
        /* Level could be null, fetch parent level */
        if (lvl == null) {
            lvl = logger.getParent().getLevel();
        }
        /* Show the updated text */
        setText("<html><b>Level</b>: " + lvl.getName());
    }
    
    private void setLoggerLevel(Level level) {        
        logger.setLevel(level);
        logger.log(Level.INFO, "Logging Level changed to {0}", level.getName());
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
                logger.log(Level.INFO, "Could not determine new logging level!", ex);
            }
            
            LoggingLevelLabel.this.updateText();
        }
        
    }
    
}
