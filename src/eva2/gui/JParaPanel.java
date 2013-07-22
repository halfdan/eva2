package eva2.gui;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 */

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.stat.OptimizationJobList;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class JParaPanel implements Serializable, PanelMaker {

    protected String name = "undefined";
    protected Object localParameter;
    protected Object proxyParameter;
    protected PropertyEditor propertyEditor;

    /**
     * ToDo: Should be removed in future.
     */
    private JPanel tempPanel = new JPanel();

    public JParaPanel() {
    }

    /**
     */
    public JParaPanel(Object Parameter, String name) {
        this.name = name;
        localParameter = Parameter;
    }

    /**
     */
    @Override
    public JComponent makePanel() {
        PropertyEditorProvider.installEditors();

        if (localParameter instanceof OptimizationJobList) {
            /* ToDo: First parameter is useless and should be removed */
            propertyEditor = OptimizationJobList.makeEditor(tempPanel, (OptimizationJobList) localParameter);
        } else {
            propertyEditor = new GenericObjectEditor();
            ((GenericObjectEditor) (propertyEditor)).setClassType(localParameter.getClass());
            ((GenericObjectEditor) (propertyEditor)).setValue(localParameter);
            ((GenericObjectEditor) (propertyEditor)).disableOKCancel();
        }

        return (JComponent) propertyEditor.getCustomEditor();
    }

    /**
     */
    public String getName() {
        return name;
    }

    public PropertyEditor getEditor() {
        return propertyEditor;
    }

    /**
     * This method will allow you to add a new Editor to a given class
     *
     * @param object
     * @param editor
     * @return False if failed true else.
     */
    public boolean registerEditor(Class object, Class editor) {
        try {
            PropertyEditorManager.registerEditor(object, editor);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
