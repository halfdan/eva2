package javaeva.gui;

import javaeva.tools.EVAHELP;
import javaeva.server.go.individuals.codings.gp.AbstractGPNode;
import javaeva.server.go.individuals.codings.gp.GPArea;

import javax.swing.*;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public class GenericAreaEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The GPArea that is to be edited*/
    private GPArea                  m_AreaObject;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_NodePanel;
    private JCheckBox[]             m_BlackCheck;

    public GenericAreaEditor() {
        // compiled code
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor = new JPanel();
        this.m_CustomEditor.setLayout(new BorderLayout());
        this.m_CustomEditor.add(new JLabel("Choose the area:"), BorderLayout.NORTH);
        this.m_NodePanel = new JPanel();
        this.m_CustomEditor.add(new JScrollPane(this.m_NodePanel), BorderLayout.CENTER);
        this.updateEditor();
    }

    /** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_NodePanel != null) {

            ArrayList GPNodes = this.m_AreaObject.getCompleteList();
            ArrayList Allowed = this.m_AreaObject.getBlackList();
            this.m_NodePanel.removeAll();
            this.m_NodePanel.setLayout(new GridLayout(GPNodes.size(), 1));
            this.m_BlackCheck = new JCheckBox[GPNodes.size()];
            for (int i = 0; i < GPNodes.size(); i++) {
                 this.m_BlackCheck[i] = new JCheckBox((((AbstractGPNode)GPNodes.get(i))).getName(), ((Boolean)Allowed.get(i)).booleanValue());
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
            this.m_AreaObject.setBlackListElement(i, this.m_BlackCheck[i].isSelected());
        }
        this.m_AreaObject.compileReducedList();
    }

    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof GPArea) {
            this.m_AreaObject = (GPArea) o;
            this.updateEditor();
        }
    }

    /** Retruns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_AreaObject;
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
        String rep = "Select from available GPNodes";
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