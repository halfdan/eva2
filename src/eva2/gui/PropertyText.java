package eva2.gui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * A text property editor view. Updates the editor on key release and lost focus events.
 * 
 */
public class PropertyText extends JTextField {
	private PropertyEditor propertyEditor;
	/**
	 *
	 */
	public PropertyText(PropertyEditor pe) {
		super(pe.getAsText());
        this.setBorder(BorderFactory.createEmptyBorder());        
		propertyEditor = pe;
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
			if (!propertyEditor.getAsText().equals(x)) {
				propertyEditor.setAsText(x);
//				setText(m_Editor.getAsText());
			}
		} catch (IllegalArgumentException ex) {
//			System.err.println("Warning: Couldnt set value (PropertyText)");
		}
	}
	
	public boolean checkConsistency() {
		String x = getText();
		return x.equals(propertyEditor.getAsText());
	}
	
	public void updateFromEditor() {
		setText(propertyEditor.getAsText());
	}
}
