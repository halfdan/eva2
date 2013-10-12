package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ExtDesktopManager extends DefaultDesktopManager {

    private static final Logger LOGGER = Logger.getLogger(ExtDesktopManager.class.getName());

    int WINDOW_LIST_START;
    public final static String INDEX = "Index";
    public final static String FRAME = "Frame";
    private JInternalFrame activeFrame = null;
    private JExtDesktopPane desktop;

    public ExtDesktopManager(JExtDesktopPane desktop) {
        this.desktop = desktop;
    }

    @Override
    public void activateFrame(JInternalFrame f) {
        super.activateFrame(f);
        activeFrame = f;
    }

    @Override
    public void deactivateFrame(JInternalFrame f) {
        super.deactivateFrame(f);
        if (activeFrame == f) {
            activeFrame = null;
        }
    }

    public JInternalFrame getActiveFrame() {
        return activeFrame;
    }

    @Override
    public void closeFrame(JInternalFrame internalFrame) {
        LOGGER.log(Level.FINE, "Closing Internal Frame: {0}", internalFrame.getTitle());
        super.closeFrame(internalFrame);
        int index = ((Integer) internalFrame.getClientProperty(INDEX)).intValue() + WINDOW_LIST_START - 1;
        int i;
        desktop.getWindowMenu().remove(index);
        for (i = index; i < Math.min(WINDOW_LIST_START + 9, desktop.getWindowMenu().getItemCount()); i++) {
            JMenuItem m = desktop.getWindowMenu().getItem(i);
            JInternalFrame frame = (JInternalFrame) m.getClientProperty(FRAME);
            frame.putClientProperty(INDEX, new Integer(((Integer) frame.getClientProperty(INDEX)).intValue() - 1));
            int winIndex = i - WINDOW_LIST_START + 1;
            m.setText((winIndex) + " " + frame.getTitle());
            m.setMnemonic((char) (0x30 + winIndex));
            m.setAccelerator(KeyStroke.getKeyStroke(0x30 + winIndex, Event.ALT_MASK));
        }

        if (internalFrame.isSelected()) {
            Component tmp = null;
            boolean found = false;
            for (i = 0; i < desktop.getComponentCount() && !found; i++) {
                tmp = desktop.getComponent(i);
                if (tmp instanceof JInternalFrame) {
                    found = true;
                }
            }

            if (found) {
                desktop.selectFrame((JInternalFrame) tmp);
            } else {
                activeFrame = null;
            }
        }
    }
}
