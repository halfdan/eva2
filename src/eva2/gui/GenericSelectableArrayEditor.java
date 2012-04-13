package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 235 $
 *            $Date: 2007-11-08 13:53:51 +0100 (Thu, 08 Nov 2007) $
 *            $Author: mkron $
 * 
 */
import eva2.tools.EVAHELP;
import eva2.tools.SelectedTag;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;

public class GenericSelectableArrayEditor extends GenericArrayEditor {
    protected JCheckBox[]             m_BlackCheck;

	public static void main(String [] args) {
		try {
			java.beans.PropertyEditorManager.registerEditor(SelectedTag.class,TagEditor.class);
			java.beans.PropertyEditorManager.registerEditor(int [].class,GenericArrayEditor.class);
			java.beans.PropertyEditorManager.registerEditor(double [].class,GenericArrayEditor.class);
			GenericArrayEditor editor = new GenericArrayEditor();
			

			int[] initial = {3, 45, 7};
			editor.setValue(initial);
			PropertyDialog pd = new PropertyDialog(editor,EVAHELP.cutClassName(editor.getClass().getName())
					, 100, 100);
			pd.setSize(200,200);
			pd.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			editor.setValue(initial);
			//ce.validate();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
}

