package eva2.gui;

import javax.swing.JToolBar;
import eva2.tools.ToolBoxGui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.SwingConstants;

/**
 *
 * @author becker
 */
public class JExtDesktopPaneToolBar extends JToolBar {
    
    private JExtDesktopPane desktopPane;
    
    public JExtDesktopPaneToolBar(JExtDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
        
        initComponents();
    }
    
    private void initComponents() {
        /* We don't want the ToolBar to be draggable */
        setFloatable(false);
        
        /* Add Buttons to tile the desktopPane */
        JButton verticalButton = ToolBoxGui.createIconifiedButton("resources/images/TileVertical16.png", "Tile vertically", false);
        verticalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.tileWindows(SwingConstants.VERTICAL);
            }
            
        });
        add(verticalButton);        
        
        JButton horizontalButton = ToolBoxGui.createIconifiedButton("resources/images/TileHorizontal16.png", "Tile horizontally", false);
        horizontalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.tileWindows(SwingConstants.HORIZONTAL);
            }
            
        });
        add(horizontalButton);
        
        JButton cascadeButton = ToolBoxGui.createIconifiedButton("resources/images/Cascade16.png", "Cascade windows", false);
        cascadeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.overlapWindows();
            }
            
        });
        add(cascadeButton);
    }
}
