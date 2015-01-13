package eva2.gui;

import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;
import eva2.tools.StringTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    public PropertyDialog(Window parent, PropertyEditor editor, String title) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setTitle(getFrameNameFromEditor(editor));
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        propertyEditor = editor;
        editorComponent = editor.getCustomEditor();
        add(editorComponent, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
    }

    protected static String getFrameNameFromEditor(PropertyEditor editor) {
        if (editor.getValue().getClass().isArray()) {
            return "Array of " + StringTools.cutClassName(editor.getValue().getClass().getComponentType().getName());
        } else {
            return StringTools.cutClassName(editor.getValue().getClass().getName());
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

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        Action actionListener = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        } ;
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", actionListener);

        return rootPane;
    }
}
