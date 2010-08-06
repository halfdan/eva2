package eva2.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public abstract class AbstractListSelectionEditor extends JPanel implements PropertyEditor, PropertyChangeListener {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    protected JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    
    /** The graphics stuff */
    private JPanel                  m_CustomEditor, m_NodePanel;
    protected JCheckBox[]             m_BlackCheck;

	
	public AbstractListSelectionEditor() {	
	}

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor = new JPanel();
        this.m_CustomEditor.setLayout(new BorderLayout());
        this.m_CustomEditor.add(new JLabel("Choose:"), BorderLayout.NORTH);
        this.m_NodePanel = new JPanel();
        this.m_CustomEditor.add(new JScrollPane(this.m_NodePanel), BorderLayout.CENTER);
        this.updateEditor();
    }

    /**
     * Return the number of elements in the list.
     * @return
     */
    protected abstract int getElementCount();
        
    /**
     * Get the display name of an element.
     * 
     * @param i
     * @return
     */
    protected abstract String getElementName(int i);
    
    /**
     * Get the tool tip of an element or null if none is available.
     * 
     * @param i
     * @return
     */
    protected String getElementToolTip(int i) {
    	return null;
    }
    
    /**
     * Get the selection state of an element.
     * 
     * @param i
     * @return
     */
    protected abstract boolean isElementSelected(int i);
    
    /** 
     * The object may have changed update the editor. This notifies change listeners automatically.
     */
    private void updateEditor() {
        if (this.m_NodePanel != null) {
            this.m_NodePanel.removeAll();
            this.m_NodePanel.setLayout(new GridLayout(getElementCount(), 1));
            this.m_BlackCheck = new JCheckBox[getElementCount()];
            for (int i = 0; i < getElementCount(); i++) {
                 this.m_BlackCheck[i] = new JCheckBox(getElementName(i), isElementSelected(i));
                 this.m_BlackCheck[i].setToolTipText(getElementToolTip(i));
                 this.m_BlackCheck[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        if (actionOnSelect()) m_Support.firePropertyChange("AbstractListSelectionEditor", null, this);
                    }
                });
                this.m_NodePanel.add(this.m_BlackCheck[i]);
            }
        }
    }

    /** 
     * Perform actions when the selection state changes. Return true if there was an actual change.
     */
    protected abstract boolean actionOnSelect();

    /**
     * Set the base object, return true on success. Make sure that the editor instance is 
     * added as a listener to the object (if supported). 
     * 
     * @param o
     * @return
     */
    protected abstract boolean setObject(Object o);
    
    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
    	if (setObject(o)) updateEditor();
    }

    /** 
     * Returns the current object.
     * @return the current object
     */
    public abstract Object getValue();

    public String getJavaInitializationString() {
        return "";
    }

    public String getAsText() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    public String[] getTags() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        m_Support.removePropertyChangeListener(l);
    }
    
    /** Returns true since the Object can be shown
     * @return true
     */
    public boolean isPaintable() {
        return true;
    }
    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Select from list";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }
    
    /** 
     * Returns true because we do support a custom editor.
     * @return true
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    public Component getCustomEditor() {
        if (this.m_CustomEditor == null) this.initCustomEditor();
        return m_CustomEditor;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
    	m_Support.firePropertyChange("AbstractListSelectionEditor", null, this);
    }
}