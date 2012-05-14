package eva2.gui;
/*
 * Title: EvA2 Description: Copyright: Copyright (c) 2003 Company: University of Tuebingen, Computer
 * Architecture @author Holger Ulmer, Felix Streichert, Hannes Planatscher @version: $Revision: 57 $
 * $Date: 2007-05-04 14:22:16 +0200 (Fri, 04 May 2007) $ $Author: mkron $
 */

import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

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
            public Object getSelectedItem() {
                return propertyEditor.getAsText();
            }

            /**
             *
             */
            public void setSelectedItem(Object o) {
                propertyEditor.setAsText((String) o);
            }
        };
        setModel(model);
        setSelectedItem(value);
    }
}
