package eva2.gui;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.beans.PropertyChangeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.08.2003
 * Time: 11:11:59
 * To change this template use Options | File Templates.
 */
public class GenericFilePathEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyFilePath                m_FilePath;

    /** The gaphix stuff */
    private JFileChooser            m_FileChooser;
    private JPanel                  m_Panel;

    public GenericFilePathEditor() {
        // compiled code
    }

    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyFilePath) {
            this.m_FilePath = (PropertyFilePath) o;
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_FilePath;
    }

    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    public String[] getTags() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.removePropertyChangeListener(l);
    }

    /** Returns true since the Object can be shown
     * @return true
     */
    public boolean isPaintable() {
        return true;
    }

    /** Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = this.m_FilePath.FileName;
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }

    /** Returns true because we do support a custom editor.
    * @return true
    */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    public Component getCustomEditor() {
        this.m_Panel            = new JPanel();
        this.m_FileChooser      = new JFileChooser();
        File file = new File(this.m_FilePath.getCompleteFilePath());
        this.m_FileChooser.setSelectedFile(file);
        this.m_FileChooser.setMultiSelectionEnabled(false);
        this.m_Panel.add(this.m_FileChooser);
        this.m_FileChooser.addActionListener(this.fileChooserAction);
        return this.m_Panel;
    }

    /** This action listener, called by the "train" button, causes
     * the SOM to recalculate the mapping.
     */
    ActionListener fileChooserAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand() == "ApproveSelection") {
                m_FilePath.setCompleteFilePath(m_FileChooser.getSelectedFile().getAbsolutePath());
                m_Support.firePropertyChange("", m_FilePath, null);
                Window w = (Window) m_FileChooser.getTopLevelAncestor();
	            w.dispose();
                m_Panel = null;
            }
            if (event.getActionCommand() == "CancelSelection") {
                m_FilePath.setCompleteFilePath(m_FileChooser.getSelectedFile().getAbsolutePath());
                m_Support.firePropertyChange("", m_FilePath, null);
                Window w = (Window) m_FileChooser.getTopLevelAncestor();
                if (w != null) w.dispose();
                m_Panel = null;
            }
        }
    };
}
