package eva2.gui.editor;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public abstract class AbstractListSelectionEditor extends JPanel implements PropertyEditor, PropertyChangeListener {

    /**
     * Handles property change notification
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    protected JLabel label = new JLabel("Can't edit", SwingConstants.CENTER);

    /**
     * The graphics stuff
     */
    private JPanel customEditor, nodePanel;
    protected JCheckBox[] blackCheck;


    public AbstractListSelectionEditor() {
    }

    /**
     * This method will initialize the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.customEditor = new JPanel();
        this.customEditor.setLayout(new BorderLayout());
        this.customEditor.add(new JLabel("Choose:"), BorderLayout.NORTH);
        this.nodePanel = new JPanel();
        this.customEditor.add(new JScrollPane(this.nodePanel), BorderLayout.CENTER);
        this.updateEditor();
    }

    /**
     * Return the number of elements in the list.
     *
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
        if (this.nodePanel != null) {
            this.nodePanel.removeAll();
            this.nodePanel.setLayout(new GridLayout(getElementCount(), 1));
            this.blackCheck = new JCheckBox[getElementCount()];
            for (int i = 0; i < getElementCount(); i++) {
                this.blackCheck[i] = new JCheckBox(getElementName(i), isElementSelected(i));
                this.blackCheck[i].setToolTipText(getElementToolTip(i));
                this.blackCheck[i].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        if (actionOnSelect()) {
                            propertyChangeSupport.firePropertyChange("AbstractListSelectionEditor", null, this);
                        }
                    }
                });
                this.nodePanel.add(this.blackCheck[i]);
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

    /**
     * This method will set the value of object that is to be edited.
     *
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (setObject(o)) {
            updateEditor();
        }
    }

    /**
     * Returns the current object.
     *
     * @return the current object
     */
    @Override
    public abstract Object getValue();

    @Override
    public String getJavaInitializationString() {
        return "";
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

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
        String rep = "Select from list";
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange("AbstractListSelectionEditor", null, this);
    }
}