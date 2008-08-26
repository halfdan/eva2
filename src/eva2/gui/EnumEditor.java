package eva2.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditorSupport;

import javax.swing.JFrame;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public class EnumEditor extends PropertyEditorSupport {
	/** The Enum values that may be chosen */
	private Object[] 				values = null;

	public String getAsText() {
		return getValue().toString();
	}
	
	public void setValue(Object value) {
		if (value instanceof Enum) {
			values = ((Enum)value).getClass().getEnumConstants();
			super.setValue(value);
		}
	}
	
	/**
	 *
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		for (int i=0; i<values.length; i++) {
			if (text.equals(values[i].toString())) {
				setValue((Enum)values[i]);
				return;
			}
		}
		throw new IllegalArgumentException("Invalid text for enum");
	}
	/**
	 *
	 */
	public String[] getTags() {
		String[] tags = new String[values.length];
		for (int i=0; i<tags.length; i++) tags[i]=values[i].toString();
		return tags;
	}

	/**
	 * Test the editor.
	 *
	 * @param args ignored
	 */
	public static void main(String [] args) {
		try {
			Enum<?> initial = TestEnum.asdf;
			EnumEditor ed = new EnumEditor();
			ed.setValue(initial);
			PropertyValueSelector ps = new PropertyValueSelector(ed);
			JFrame f = new JFrame();
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			f.getContentPane().setLayout(new BorderLayout());
			f.getContentPane().add(ps, BorderLayout.CENTER);
			f.pack();
			f.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
}

enum TestEnum { asdf, sdf, asdfa};