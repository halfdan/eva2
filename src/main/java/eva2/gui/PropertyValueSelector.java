package eva2.gui;

import javax.swing.*;
import java.beans.PropertyEditor;

public class PropertyValueSelector extends JComboBox {

    private PropertyEditor propertyEditor;

    /**
     *
     */
    public PropertyValueSelector(PropertyEditor pe) {
        propertyEditor = pe;
        this.setBorder(BorderFactory.createEmptyBorder());
        Object value = propertyEditor.getAsText();
        String[] tags = propertyEditor.getTags();
        /**
         *
         */
        ComboBoxModel model = new DefaultComboBoxModel(tags) {

            /**
             *
             */
            @Override
            public Object getSelectedItem() {
                return propertyEditor.getAsText();
            }

            /**
             *
             */
            @Override
            public void setSelectedItem(Object o) {
                propertyEditor.setAsText((String) o);
            }
        };
        setModel(model);
        setSelectedItem(value);
    }
}
