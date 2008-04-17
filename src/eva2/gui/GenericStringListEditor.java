package eva2.gui;


import javax.swing.*;

import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.individuals.codings.gp.GPArea;

import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.03.2004
 * Time: 15:03:29
 * To change this template use File | Settings | File Templates.
 */
public class GenericStringListEditor implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The GPArea that is to be edited*/
    private PropertyStringList              m_List;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_NodePanel;
    private JCheckBox[]             m_BlackCheck;

    public GenericStringListEditor() {
        // compiled code
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor = new JPanel();
        this.m_CustomEditor.setPreferredSize(new Dimension(200, 200));
        this.m_CustomEditor.setLayout(new BorderLayout());
        this.m_CustomEditor.add(new JLabel("Select:"), BorderLayout.NORTH);
        this.m_NodePanel = new JPanel();
        this.m_CustomEditor.add(new JScrollPane(this.m_NodePanel), BorderLayout.CENTER);
        this.updateEditor();
    }

    /** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_NodePanel != null) {
            String[]    strings = this.m_List.getStrings();
            boolean[]   booleans = this.m_List.getSelection();
            this.m_NodePanel.removeAll();
            this.m_NodePanel.setLayout(new GridLayout(strings.length, 1));
            this.m_BlackCheck = new JCheckBox[strings.length];
            for (int i = 0; i < strings.length; i++) {
                 this.m_BlackCheck[i] = new JCheckBox(strings[i], booleans[i]);
                 this.m_BlackCheck[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        makeNodeList();
                    }
                });
                this.m_NodePanel.add(this.m_BlackCheck[i]);
            }
        }
    }

    /** This method checks the current BlackList and compiles it
     * to a new ReducedList.
     */
    private void makeNodeList() {
        for (int i = 0; i < this.m_BlackCheck.length; i++) {
            this.m_List.setSelectionForElement(i, this.m_BlackCheck[i].isSelected());
        }
    }

    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyStringList) {
            this.m_List = (PropertyStringList) o;
            this.updateEditor();
        }
    }

    /** Retruns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_List;
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
    /**
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_Support.addPropertyChangeListener(l);
    }
    /**
     *
     */
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
        String rep = "Select from available Elements";
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
        if (this.m_CustomEditor == null) this.initCustomEditor();
        return m_CustomEditor;
    }
}