package eva2.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 57 $
 *            $Date: 2007-05-04 14:22:16 +0200 (Fri, 04 May 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
/**
 *
 */
public class PropertyValueSelector extends JComboBox {
	PropertyEditor m_Editor;
	/**
	 *
	 */
	public PropertyValueSelector(PropertyEditor pe) {
		m_Editor = pe;
		Object value = m_Editor.getAsText();
		String tags[] = m_Editor.getTags();
		/**
		 *
		 */
		ComboBoxModel model = new DefaultComboBoxModel(tags) {
			/**
			 *
			 */
			public Object getSelectedItem() {
				return m_Editor.getAsText();
			}
			/**
			 *
			 */
			public void setSelectedItem(Object o) {
				m_Editor.setAsText((String)o);
			}
		};
		setModel(model);
		setSelectedItem(value);
	}
}


