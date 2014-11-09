package eva2.gui.editor;

import eva2.gui.PropertyDoubleArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 * A eva2.problems.simple focus listener with an object ID and callback.
 *
 * @author mkron
 */
class MyFocusListener implements FocusListener {
    private int myID = -1;
    private DoubleArrayEditor arrEditor = null;

    public MyFocusListener(int id, DoubleArrayEditor gdae) {
        myID = id;
        this.arrEditor = gdae;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent e) {
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent e) {
        arrEditor.notifyFocusID(myID);
    }

}


/**
 * A generic editor for PropertyDoubleArray.
 */
public class DoubleArrayEditor extends JPanel implements PropertyEditor {
    /**
     * Handles property change notification
     */
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    private JLabel label = new JLabel("Can't edit", SwingConstants.CENTER);
    /**
     * The filePath that is to be edited
     */
    private PropertyDoubleArray doubleArray;

    /**
     * The gaphix stuff
     */
    private JPanel customEditor, dataPanel, buttonPanel;
    private JTextField[][] inputTextFields;
    private JButton okButton, addButton, deleteButton, normalizeButton;

    /**
     * Which columns has the focus? *
     */
    private int lastFocussedRow = -1;

    public DoubleArrayEditor() {
        // compiled code
    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.customEditor = new JPanel();
        this.customEditor.setLayout(new BorderLayout());

        this.customEditor.add(new JLabel("Current Double Array:"), BorderLayout.NORTH);

        // initialize data panel
        this.dataPanel = new JPanel();
        this.updateDataPanel();
        this.customEditor.add(this.dataPanel, BorderLayout.CENTER);

        // initialize button panel
        this.buttonPanel = new JPanel();
        this.addButton = new JButton("Add");
        this.addButton.addActionListener(this.addAction);
        this.deleteButton = new JButton("Delete");
        this.deleteButton.addActionListener(this.deleteAction);
        this.normalizeButton = new JButton("Normalize");
        this.normalizeButton.addActionListener(this.normalizeAction);
        this.okButton = new JButton("OK");
        this.okButton.setEnabled(true);
        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //backupObject = copyObject(object);
                if ((customEditor.getTopLevelAncestor() != null) && (customEditor.getTopLevelAncestor() instanceof Window)) {
                    Window w = (Window) customEditor.getTopLevelAncestor();
                    w.dispose();
                }
            }
        });
        this.buttonPanel.add(this.addButton);
        this.buttonPanel.add(this.deleteButton);
        this.buttonPanel.add(this.normalizeButton);
        this.buttonPanel.add(this.okButton);
        this.customEditor.add(this.buttonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /**
     * This action listener adds an element to DoubleArray
     */
    ActionListener addAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            doubleArray.addRowCopy(lastFocussedRow); // copy the last focussed row
            updateEditor();
        }
    };

    /**
     * This action listener removes an element from the DoubleArray.
     */
    ActionListener deleteAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (!doubleArray.isValidRow(lastFocussedRow)) {
                doubleArray.deleteRow(doubleArray.getNumRows() - 1);
            } else {
                doubleArray.deleteRow(lastFocussedRow);
            }
            updateEditor();
        }
    };

    /**
     * This action listener nomalizes each columng of the values of the DoubleArray.
     */
    ActionListener normalizeAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            doubleArray.normalizeColumns();
            updateEditor();
        }
    };

    /**
     * This action listener reads all values
     */
    KeyListener readDoubleArrayAction = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent event) {
        }

        @Override
        public void keyTyped(KeyEvent event) {
        }

        @Override
        public void keyReleased(KeyEvent event) {
            double[][] tmpDD = new double[inputTextFields.length][inputTextFields[0].length];

            for (int i = 0; i < tmpDD.length; i++) {
                for (int j = 0; j < tmpDD[0].length; j++) {
                    try {
                        double d = 0;
                        d = Double.parseDouble(inputTextFields[i][j].getText());
                        tmpDD[i][j] = d;
                    } catch (Exception e) {
                    }
                }
            }

            doubleArray.setDoubleArray(tmpDD);
            //updateEditor();
        }
    };


    /**
     * The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.customEditor != null) {
            this.updateDataPanel();
            this.customEditor.validate();
            this.customEditor.repaint();
        }
    }


    /**
     * This method updates the data panel
     */
    private void updateDataPanel() {
        int numRows = doubleArray.getNumRows();
        int numCols = doubleArray.getNumCols();
        this.dataPanel.removeAll();
        this.dataPanel.setLayout(new GridLayout(numRows, numCols + 1));
        this.inputTextFields = new JTextField[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            JLabel label = new JLabel("Value X" + i + ": ");
            this.dataPanel.add(label);
            for (int j = 0; j < numCols; j++) {
                this.inputTextFields[i][j] = new JTextField();
                this.inputTextFields[i][j].setText("" + doubleArray.getValue(i, j));
                this.inputTextFields[i][j].addKeyListener(this.readDoubleArrayAction);
                this.inputTextFields[i][j].addFocusListener(new MyFocusListener(i, this));
                this.dataPanel.add(this.inputTextFields[i][j]);
            }
        }
    }

    public void notifyFocusID(int id) {
        // notification of which column has the focus
        lastFocussedRow = id;
//    	System.out.println("Focus now on " + id);
    }

    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyDoubleArray) {
            this.doubleArray = (PropertyDoubleArray) o;
            this.updateEditor();
        }
    }

    /**
     * Returns the current object.
     *
     * @return the current object
     */
    @Override
    public Object getValue() {
        return this.doubleArray;
    }

    @Override
    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.removePropertyChangeListener(l);
    }

    /**
     * This is used to hook an action listener to the ok button
     *
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        okButton.addActionListener(a);
    }

    /**
     * This is used to remove an action listener from the ok button
     *
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        okButton.removeActionListener(a);
    }

    /**
     * Returns true since the Object can be shown
     *
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Edit double array...";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3);
    }

    /**
     * Returns true because we do support a custom editor.
     *
     * @return true
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns the array editing component.
     *
     * @return a value of type 'java.awt.Component'
     */
    @Override
    public Component getCustomEditor() {
        if (this.customEditor == null) {
            this.initCustomEditor();
        }
        return customEditor;
    }
}