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
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class JEFrame extends JInternalFrame {
    private boolean closeAllOnClose = false;

    public JEFrame() {
        super();
        init();
    }

    public JEFrame(String name) {
        super(name);
        init();
    }

    /**
     * Set to true if all registered JEFrames should be closed if this frame is closed.
     *
     * @param c
     */
    public void setCloseAllOnClosed(boolean c) {
        closeAllOnClose = c;
    }

    @Override
    public void addInternalFrameListener(InternalFrameListener l) {
        super.addInternalFrameListener(l);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                super.internalFrameClosed(e);
                JEFrameRegister.getInstance().unregister((JEFrame) e.getInternalFrame());
                if (closeAllOnClose) {
                    JEFrameRegister.getInstance().closeAll();
                }
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                super.internalFrameOpened(e);
                JEFrameRegister.getInstance().register((JEFrame) e.getInternalFrame());
            }

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                JEFrameRegister.getInstance().register((JEFrame) e.getInternalFrame());
                super.internalFrameActivated(e);
            }
        });
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK),
                "ctrlFpressed"
        );
        this.getRootPane().getActionMap().put(
                "ctrlFpressed",
                new AbstractAction("ctrlFpressed") {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        JEFrameRegister.getInstance().getFrameList().get(0).toFront();
                    }
                }
        );
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK),
                "ctrlOpressed"
        );
        this.getRootPane().getActionMap().put(
                "ctrlOpressed",
                new AbstractAction("ctrlOpressed") {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        java.util.List<JEFrame> frameList = JEFrameRegister.getInstance().getFrameList();
                        for (JEFrame frame : frameList) {
                            frame.toFront();
                        }
                    }
                }
        );
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LESS, Event.CTRL_MASK),
                "ctrlSmallerpressed"
        );
        final JEFrame self = this;
        this.getRootPane().getActionMap().put(
                "ctrlSmallerpressed",
                new AbstractAction("ctrlSmallerpressed") {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        JEFrameRegister.getInstance().setFocusToNext(self);
                    }
                }
        );

        this.setMaximizable(true);
        this.setResizable(true);
        this.setIconifiable(true);
        this.setClosable(true);


    }
}
