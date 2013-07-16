package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
import eva2.tools.EVAHELP;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
/**
 *
 */
public class PropertyPanel extends JPanel {
	private PropertyEditor propertyEditor;
	private PropertyDialog propertyDialog;
    
    private JLabel textLabel;
	/**
	 *
	 */
	public PropertyPanel(PropertyEditor editor) {
		setToolTipText("Click to edit properties for this object");
		setOpaque(true);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.weightx = 1.0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		propertyEditor = editor;
        
        textLabel = new JLabel();

        add(textLabel, gbConstraints);


	}
	
	public void showDialog(int initX, int initY) {
		if (propertyDialog == null) {
			propertyDialog = new PropertyDialog(propertyEditor, EVAHELP.cutClassName(propertyEditor.getClass().getName()) , initX, initY);
			propertyDialog.setPreferredSize(new Dimension(500,300));
            propertyDialog.setModal(true);
            propertyDialog.setVisible(true);
		}
		else {
			propertyDialog.updateFrameTitle(propertyEditor);
			propertyDialog.setVisible(false);
			propertyDialog.requestFocus();
		}
	}	
	
	/**
	 *
	 */
    @Override
	public void removeNotify() {
		if (propertyDialog != null) {
			propertyDialog = null;
		}
	}
	
	/**
	 *
	 */
    @Override
	public void paintComponent(Graphics g) {
		Insets i = textLabel.getInsets();
		Rectangle box = new Rectangle(i.left, i.top,
				getSize().width - i.left - i.right ,
				getSize().height - i.top - i.bottom);
		g.clearRect(i.left, i.top,
				getSize().width - i.right - i.left,
				getSize().height - i.bottom - i.top);
		propertyEditor.paintValue(g, box);
	}
	
	public PropertyEditor getEditor() {
		return propertyEditor;
	}
}
