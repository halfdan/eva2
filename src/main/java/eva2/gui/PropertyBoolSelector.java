package eva2.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyEditor;

/**
 * A checkbox for boolean editors.
 */
public class PropertyBoolSelector extends JCheckBox {
    private static final long serialVersionUID = 8181005734895597714L;
    private PropertyEditor propertyEditor;

    public PropertyBoolSelector(PropertyEditor pe) {
        super();
        setBackground(Color.WHITE);
        propertyEditor = pe;
        if (propertyEditor.getAsText().equals("True")) {
            setSelected(true);
        } else {
            setSelected(false);
        }

        addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                propertyEditor.setValue(Boolean.TRUE);
            }
            if (evt.getStateChange() == ItemEvent.DESELECTED) {
                propertyEditor.setValue(Boolean.FALSE);
            }
        });
    }
}
