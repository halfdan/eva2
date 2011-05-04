package eva2.gui;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import eva2.server.stat.EvAJobList;

public class JParaPanel implements Serializable, PanelMaker {

    public static boolean TRACE = false;
    protected String m_Name = "undefined";
    protected Object m_LocalParameter;
    protected Object m_ProxyParameter;
    protected PropertyEditor m_Editor;
    private JPanel m_Panel;

    public JParaPanel() {
    }

    /**
     */
    public JParaPanel(Object Parameter, String name) {
        m_Name = name;
        m_LocalParameter = Parameter;
    }

    /**
     */
    public JComponent makePanel() {
        m_Panel = new JPanel();
        //m_Panel.setPreferredSize(new Dimension(200, 200)); // MK: this was evil, killing all the auto-layout mechanisms
        PropertyEditorProvider.installEditors();
        
        if (m_LocalParameter instanceof EvAJobList) {
        	m_Editor = EvAJobList.makeEditor(m_Panel, (EvAJobList)m_LocalParameter);
        } else {
        	m_Editor = new GenericObjectEditor();
	        ((GenericObjectEditor) (m_Editor)).setClassType(m_LocalParameter.getClass());
	        ((GenericObjectEditor) (m_Editor)).setValue(m_LocalParameter);
	        ((GenericObjectEditor) (m_Editor)).disableOKCancel();
        }

        m_Panel.setLayout(new BorderLayout());
        m_Panel.add(m_Editor.getCustomEditor(), BorderLayout.CENTER);

        m_Panel.setLayout(new FlowLayout(FlowLayout.TRAILING, 10, 10));
        m_Panel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        m_Panel.setLayout(new GridBagLayout());
        m_Panel.add(Box.createRigidArea(new Dimension(0, 10)));
        return m_Panel;
    }

    /**
     */
    public String getName() {
        return m_Name;
    }

    public PropertyEditor getEditor() {
    	return m_Editor;
    }
    
    /** This method will allow you to add a new Editor to a given class
     * @param object
     * @param editor
     * @return False if failed true else.
     */
    public boolean registerEditor(Class object, Class editor) {
        try {
            PropertyEditorManager.registerEditor(object, editor);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
