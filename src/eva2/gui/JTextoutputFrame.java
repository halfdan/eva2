package eva2.gui;

import eva2.optimization.tools.FileTools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

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
        frame.setClosable(false);
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
        textArea.setTabSize(16);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        // Limit text output to 2500 Lines
        ((AbstractDocument)textArea.getDocument()).setDocumentFilter(new LineBufferDocumentFilter(textArea, 2500));


        frame.getContentPane().setLayout(new BorderLayout());
        final JScrollPane scrollpane = new JScrollPane(textArea);
        frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
        scrollpane.getViewport().addChangeListener(new ChangeListener() {

            private int lastHeight;
            //

            @Override
            public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport) e.getSource();
                int height = viewport.getViewSize().height;
                if (height != lastHeight) {
                    lastHeight = height;
                    int x = height - viewport.getExtentSize().height;
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
            FileTools.saveObjectWithFileChooser(frame, textArea.getText(), null);
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

class LineBufferDocumentFilter extends DocumentFilter {
    private JTextArea area;
    private int max;

    public LineBufferDocumentFilter(JTextArea area, int max) {
        this.area = area;
        this.max = max;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, text, attr);
        int lines = area.getLineCount();
        if (lines > max) {
            int linesToRemove = lines - max -1;
            int lengthToRemove = area.getLineStartOffset(linesToRemove);
            remove(fb, 0, lengthToRemove);
        }
    }
}