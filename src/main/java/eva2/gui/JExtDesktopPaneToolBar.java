package eva2.gui;

import eva2.tools.ToolBoxGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
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
        JButton verticalButton = ToolBoxGui.createIconifiedButton("images/TileVertical16.png", "Tile vertically", false);
        verticalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.tileWindows(SwingConstants.VERTICAL);
            }

        });
        add(verticalButton);

        JButton horizontalButton = ToolBoxGui.createIconifiedButton("images/TileHorizontal16.png", "Tile horizontally", false);
        horizontalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.tileWindows(SwingConstants.HORIZONTAL);
            }

        });
        add(horizontalButton);

        JButton cascadeButton = ToolBoxGui.createIconifiedButton("images/Cascade16.png", "Cascade windows", false);
        cascadeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                desktopPane.overlapWindows();
            }

        });
        add(cascadeButton);
    }
}