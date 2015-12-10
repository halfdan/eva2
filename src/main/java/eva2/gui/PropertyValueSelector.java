package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 57 $
 * $Date: 2007-05-04 14:22:16 +0200 (Fri, 04 May 2007) $ $Author: mkron $
 */

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
