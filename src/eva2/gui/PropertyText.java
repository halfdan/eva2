package eva2.gui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;

import javax.swing.JTextField;

/**
 * A text property editor view. Updates the editor on key release and lost focus events.
 * 
 */
public class PropertyText extends JTextField {
	private PropertyEditor m_Editor;
	/**
	 *
	 */
	public PropertyText(PropertyEditor pe) {
		super(pe.getAsText());
		m_Editor = pe;
		//    m_Editor.addPropertyChangeListener(new PropertyChangeListener() {
		//      public void propertyChange(PropertyChangeEvent evt) {
		//	updateUs();
		//      }
		//    });
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				//if (e.getKeyCode() == KeyEvent.VK_ENTER)
				updateEditor();
			}
		});
		addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateEditor();
			}
		});
	}

	/**
	 *
	 */
	protected void updateEditor() {
		try {
			String x = getText();
			if (!m_Editor.getAsText().equals(x)) {
				m_Editor.setAsText(x);
//				setText(m_Editor.getAsText());
			}
		} catch (IllegalArgumentException ex) {
//			System.err.println("Warning: Couldnt set value (PropertyText)");
		}
	}
	
	public boolean checkConsistency() {
		String x = getText();
		return x.equals(m_Editor.getAsText());
	}
	
	public void updateFromEditor() {
		setText(m_Editor.getAsText());
	}
}
