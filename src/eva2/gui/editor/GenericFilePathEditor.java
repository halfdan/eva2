package eva2.gui.editor;

import eva2.gui.PropertyFilePath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.io.File;

/**
 *
 */
public class GenericFilePathEditor extends JPanel implements PropertyEditor {

    /**
     * Handles property change notification
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    private JLabel label = new JLabel("Can't edit", SwingConstants.CENTER);
    /**
     * The filePath that is to be edited
     */
    private PropertyFilePath filePath;

    /**
     * The gaphix stuff
     */
    private JFileChooser fileChooser;
    private JPanel panel;

    public GenericFilePathEditor() {
        // compiled code
    }

    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyFilePath) {
            this.filePath = (PropertyFilePath) o;
        }
    }

    /**
     * Returns the current object.
     *
     * @return the current object
     */
    @Override
    public Object getValue() {
        return this.filePath;
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
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
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
        String rep = this.filePath.fileName;
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
        this.panel = new JPanel();
        this.fileChooser = new JFileChooser();
        File file = new File(this.filePath.getCompleteFilePath());
        this.fileChooser.setSelectedFile(file);
        this.fileChooser.setMultiSelectionEnabled(false);
        this.panel.add(this.fileChooser);
        this.fileChooser.addActionListener(this.fileChooserAction);
        return this.panel;
    }

    /**
     * This action listener, called by the "train" button, causes
     * the SOM to recalculate the mapping.
     */
    ActionListener fileChooserAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand() == "ApproveSelection") {
                filePath.setCompleteFilePath(fileChooser.getSelectedFile().getAbsolutePath());
                propertyChangeSupport.firePropertyChange("", filePath, null);
                Window w = (Window) fileChooser.getTopLevelAncestor();
                w.dispose();
                panel = null;
            }
            if (event.getActionCommand() == "CancelSelection") {
                filePath.setCompleteFilePath(fileChooser.getSelectedFile().getAbsolutePath());
                propertyChangeSupport.firePropertyChange("", filePath, null);
                Window w = (Window) fileChooser.getTopLevelAncestor();
                if (w != null) {
                    w.dispose();
                }
                panel = null;
            }
        }
    };
}
