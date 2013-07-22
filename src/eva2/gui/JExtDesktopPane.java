package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 10 $
 * $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $ $Author: streiche $
 */

import eva2.gui.editor.ComponentFilter;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.Vector;
import javax.swing.*;

/**
 *
 */
public class JExtDesktopPane extends JDesktopPane {

    private ActionListener actMenuFrame;
    private ExtDesktopManager desktopManager;
    private JExtMenu windowMenu;
    private ExtAction actWindowTileVert;
    private ExtAction actWindowTileHorz;
    private ExtAction actWindowOverlap;
    public final static int WINDOW_TILEVERT = 0;
    public final static int WINDOW_TILEHORZ = 1;
    public final static int WINDOW_OVERLAP = 2;
    public final static int WINDOW_ARRANGEICONS = 3;
    public final static int WINDOW_LIST = 4;
    public final static int TITLEBAR_HEIGHT = 25;

    /**
     *
     */
    public JExtDesktopPane() {
        super();

        windowMenu = new JExtMenu("&Windows");
        desktopManager = new ExtDesktopManager(this);
        setDesktopManager(desktopManager);

        actMenuFrame = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                if (!(event.getSource() instanceof JMenuItem)) {
                    return;
                }
                JInternalFrame frame = (JInternalFrame) ((JMenuItem) event.getSource()).getClientProperty(ExtDesktopManager.FRAME);
                selectFrame(frame);
            }
        };

        windowMenu.add(actWindowTileVert = new ExtAction("Tile &Vertically", "Tiles all windows vertically",
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                tileWindows(SwingConstants.HORIZONTAL);
            }
        });

        windowMenu.add(actWindowTileHorz = new ExtAction("Tile &Horizontally", "Tiles all windows horizontically",
                KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                tileWindows(SwingConstants.VERTICAL);
            }
        });

        windowMenu.add(actWindowOverlap = new ExtAction("&Cascade Windows", "Cascades all visible windows",
                KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK)) {

            @Override
            public void actionPerformed(final ActionEvent event) {
                overlapWindows();
            }
        });

        windowMenu.addSeparator();
        desktopManager.WINDOW_LIST_START = 4;
    }

    /**
     * Method to access the window actions.
     *
     * @param action The desired action (use JExtDesktopPane constants). Default is null
     * @return The ExtAction
     * @deprecated
     */
    public ExtAction getWindowAction(int action) {
        switch (action) {
            case WINDOW_TILEVERT:
                return actWindowTileVert;
            case WINDOW_TILEHORZ:
                return actWindowTileHorz;
            case WINDOW_OVERLAP:
                return actWindowOverlap;
        }
        return null;
    }

    public void overlapWindows() {
        final int minWidth = 150, minHeight = 100;
        int fWidth, fHeight,
                oCount, i, indent;

        JInternalFrame[] frames = getOpenFrames();
        if (frames.length == 0) {
            return;
        }

        oCount = Math.min(frames.length, Math.min((getWidth() - minWidth) / TITLEBAR_HEIGHT + 1, (getHeight() - minHeight) / TITLEBAR_HEIGHT + 1));
        fWidth = getWidth() - (oCount - 1) * TITLEBAR_HEIGHT;
        fHeight = getHeight() - (oCount - 1) * TITLEBAR_HEIGHT;

        indent = 0;
        for (i = 0; i < frames.length; i++) {
            frames[frames.length - i - 1].setLocation(indent * TITLEBAR_HEIGHT, indent * TITLEBAR_HEIGHT);
            frames[frames.length - i - 1].setSize(fWidth, fHeight);
            indent = (i + 1) % oCount == 0 ? 0 : indent + 1;
        }
    }

    /**
     *
     */
    public void tileWindows(int orientation) {
        int rows, cols,
                rHeight, cWidth,
                row, col,
                i;

        JInternalFrame[] frames = getOpenFrames();
        if (frames.length == 0) {
            return;
        }

        if (orientation == SwingConstants.HORIZONTAL) {
            rows = (int) (Math.rint(Math.sqrt(frames.length) - 0.49));
            cols = frames.length / rows;
            rHeight = getHeight() / rows;
            cWidth = getWidth() / cols;
            row = col = 0;
            for (i = 0; i < frames.length; i++) {
                frames[i].setLocation(col * cWidth, row * rHeight);
                frames[i].setSize(cWidth, rHeight);
                if (col == cols - 1) {
                    row++;
                    col = 0;
                } else {
                    col++;
                }
                if (row > 0 && frames.length - i - (cols + 1) * (rows - row) > 0) {
                    cols++;
                    cWidth = getWidth() / cols;
                }
            }
        } else if (orientation == SwingConstants.VERTICAL) {
            cols = (int) (Math.rint(Math.sqrt(frames.length) - 0.49));
            rows = frames.length / cols;
            cWidth = getWidth() / cols;
            rHeight = getHeight() / rows;
            col = row = 0;
            for (i = 0; i < frames.length; i++) {
                frames[i].setLocation(col * cWidth, row * rHeight);
                frames[i].setSize(cWidth, rHeight);
                if (row == rows - 1) {
                    col++;
                    row = 0;
                } else {
                    row++;
                }
                if (col > 0 && frames.length - i - (rows + 1) * (cols - col) > 0) {
                    rows++;
                    rHeight = getHeight() / rows;
                }
            }
        }
    }

    public JInternalFrame[] getOpenFrames() {
        JInternalFrame[] result;
        Vector vResults = new Vector(10);
        Component tmp;

        for (int i = 0; i < getComponentCount(); i++) {
            tmp = getComponent(i);
            if (tmp instanceof JInternalFrame) {
                vResults.addElement(tmp);
            }
        }

        result = new JInternalFrame[vResults.size()];
        vResults.copyInto(result);

        return result;
    }

    public int getFrameCount() {
        return getComponentCount(new ComponentFilter() {

            @Override
            public boolean accept(Component c) {
                return c instanceof JInternalFrame
                        || (c instanceof JInternalFrame.JDesktopIcon
                        && ((JInternalFrame.JDesktopIcon) c).getInternalFrame() != null);
            }
        });
    }

    public int getComponentCount(ComponentFilter c) {
        int result = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            if (c.accept(getComponent(i))) {
                result++;
            }
        }
        return result;
    }

    public void selectFrame(JInternalFrame f) {
        if (f != null) {
            try {
                if (f.isIcon()) {
                    f.setIcon(false);
                } else {
                    f.setSelected(true);
                }
            } catch (PropertyVetoException exc) {
            }
        }
    }

    @Override
    public void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        //System.out.println("JExtDesktopPane.addImpl");
        if (comp instanceof JInternalFrame) {
            JInternalFrame docFrame = (JInternalFrame) comp;
            int frameIndex = windowMenu.getItemCount() - desktopManager.WINDOW_LIST_START + 1;
            if (docFrame.getClientProperty(ExtDesktopManager.INDEX) != null) {
                return;
            }
            docFrame.putClientProperty(ExtDesktopManager.INDEX, new Integer(frameIndex));
            JMenuItem m = new JMenuItem((frameIndex < 10 ? frameIndex + " " : "") + docFrame.getTitle());
            if (frameIndex < 10) {
                m.setMnemonic((char) (0x30 + frameIndex));
                m.setAccelerator(KeyStroke.getKeyStroke(0x30 + frameIndex, Event.ALT_MASK));
            }
            m.setToolTipText("Shows the window " + docFrame.getTitle());
            m.putClientProperty(ExtDesktopManager.FRAME, docFrame);
            m.addActionListener(actMenuFrame);
            windowMenu.add(m);
        }
    }

    public JMenu getWindowMenu() {
        return windowMenu;
    }
}