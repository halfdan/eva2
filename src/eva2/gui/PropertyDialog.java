package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 14 $
 * $Date: 2006-12-18 16:32:23 +0100 (Mon, 18 Dec 2006) $ $Author: marcekro $
 */

import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;
import eva2.tools.EVAHELP;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyEditor;

/**
 *
 */
public class PropertyDialog extends JDialog {

    private PropertyEditor propertyEditor;
    private Component editorComponent;

    /**
     *
     */
    public PropertyDialog(PropertyEditor editor, String title, int x, int y) {
        super();
        setTitle(getFrameNameFromEditor(editor));
        //super(getFrameNameFromEditor(editor)); // that was the long class name !!
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        propertyEditor = editor;
        editorComponent = editor.getCustomEditor();
        add(editorComponent, BorderLayout.CENTER);
        pack();
        setLocation(x, y);
        setVisible(true);
    }

    protected static String getFrameNameFromEditor(PropertyEditor editor) {
        if (editor.getValue().getClass().isArray()) {
            return "Array of " + EVAHELP.cutClassName(editor.getValue().getClass().getComponentType().getName());
        } else {
            return EVAHELP.cutClassName(editor.getValue().getClass().getName());
        }
    }

    /**
     * Update the name of the dialogue from an editor instance.
     *
     * @param editor
     */
    public void updateFrameTitle(PropertyEditor editor) {
        setTitle(getFrameNameFromEditor(editor));
    }

    /**
     *
     */
    public PropertyEditor getEditor() {
        return propertyEditor;
    }
}
