package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 255
 * $ $Date: 2007-11-15 14:58:12 +0100 (Thu, 15 Nov 2007) $ $Author: mkron $
 */

import eva2.EvAInfo;
import eva2.optimization.tools.FileTools;
import eva2.tools.BasicResourceLoader;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 */
public class JTextoutputFrame implements JTextoutputFrameInterface, ActionListener, Serializable {

    private JMenuItem clearItem, saveItem;
    protected String frameTitle = "undefined";
    private transient JTextArea textArea = null;
    private final JInternalFrame frame;
    private JPopupMenu popup;

    /**
     *
     */
    public JTextoutputFrame(String title) {
        frameTitle = title;
        frame = new JEFrame(frameTitle);
        textArea = null;
    }

    /**
     *
     */
    @Override
    public void print(String text) {
        if (textArea == null) {
            createFrame();
        }
        textArea.append(text);
        textArea.repaint();
    }

    @Override
    public void println(String txt) {
        print(txt + '\n');
    }

    @Override
    public void setShow(boolean bShow) {
        if (frame.isVisible() != bShow) {
            if (frame.isVisible()) {
                frame.dispose();
                textArea.setText(null);
            } else {
                if (textArea == null) {
                    createFrame();
                } else {
                    frame.setVisible(true);
                }
                frame.setEnabled(true);
            }
        }
    }

    /**
     *
     */
    private void createFrame() {
        textArea = new JTextArea(10, 80);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);

        frame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(final InternalFrameEvent event) {
                super.internalFrameClosing(event);
                frame.dispose();
                frame.setEnabled(false);
            }
        });
        frame.getContentPane().setLayout(new BorderLayout());
        final JScrollPane scrollpane = new JScrollPane(textArea);
        frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
        scrollpane.getViewport().addChangeListener(new ChangeListener() {

            private int lastHeight;
            //

            @Override
            public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport) e.getSource();
                int Height = viewport.getViewSize().height;
                if (Height != lastHeight) {
                    lastHeight = Height;
                    int x = Height - viewport.getExtentSize().height;
                    viewport.setViewPosition(new Point(0, x));
                }
            }
        });
        makePopupMenu();
        frame.pack();
        frame.setSize(800, 400);
        frame.setVisible(true);
    }

    void makePopupMenu() {
        //Create the popup menu.
        popup = new JPopupMenu();
        clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(this);
        popup.add(clearItem);
        saveItem = new JMenuItem("Save as...");
        saveItem.addActionListener(this);
        popup.add(saveItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener(popup);
        textArea.addMouseListener(popupListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem src = (JMenuItem) e.getSource();
        if (src == clearItem) {
            textArea.setText(null);
        } else if (src == saveItem) {
            FileTools.saveObjectWithFileChooser(frame, textArea.getText());
//			File outfile = FileTools.writeString("TextOutput.txt", m_TextArea.getText());
        } else {
            System.err.println("Unknown popup component (JTextoutputFrame)!");
        }
    }
}

/**
 * A popup listener opening a popup menu on right clicks.
 *
 * @author mkron
 */
class PopupListener extends MouseAdapter {

    JPopupMenu popup;

    public PopupListener(JPopupMenu pm) {
        popup = pm;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
}