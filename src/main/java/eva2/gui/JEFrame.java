package eva2.gui;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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

        setDoubleBuffered(true);
        setIgnoreRepaint(true);

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
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
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
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK),
                "ctrlOpressed"
        );
        this.getRootPane().getActionMap().put(
                "ctrlOpressed",
                new AbstractAction("ctrlOpressed") {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        java.util.List<JEFrame> frameList = JEFrameRegister.getInstance().getFrameList();
                        frameList.forEach(JEFrame::toFront);
                    }
                }
        );
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LESS, InputEvent.CTRL_MASK),
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
        this.setIconifiable(false);
        this.setClosable(true);


    }
}
