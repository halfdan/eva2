package eva2.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditor;

import javax.swing.JCheckBox;

/**
 * A checkbox for boolean editors. 
 */
public class PropertyBoolSelector extends JCheckBox {
	private static final long serialVersionUID = 8181005734895597714L;
	private PropertyEditor m_Editor;

	public PropertyBoolSelector(PropertyEditor pe) {
		super();
		m_Editor = pe;
		if (m_Editor.getAsText().equals("True"))
			setSelected(true);
		else
			setSelected(false);

		addItemListener(new ItemListener () {
            @Override
			public void itemStateChanged (ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					m_Editor.setValue(Boolean.TRUE);
				}
				if (evt.getStateChange() == ItemEvent.DESELECTED) {
					m_Editor.setValue(Boolean.FALSE);
				}
			}
		});
	}
}
