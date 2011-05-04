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
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import eva2.tools.EVAHELP;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class PropertyPanel extends JPanel {
	private PropertyEditor m_PropertyEditor;
	private PropertyDialog m_PropertyDialog;
	/**
	 *
	 */
	public PropertyPanel(PropertyEditor Editor) {
		setBorder(BorderFactory.createEtchedBorder());
		setToolTipText("Click to edit properties for this object");
		setOpaque(true);
		m_PropertyEditor = Editor;
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (m_PropertyEditor.getValue() != null) {
					showDialog(getLocationOnScreen().x, getLocationOnScreen().y);
				}
			}

		});
		Dimension newPref = getPreferredSize();
		newPref.height = getFontMetrics(getFont()).getHeight() * 6 / 4;  //6 / 4;
		newPref.width = newPref.height * 6; //5
		setPreferredSize(newPref);
	}
	
	public void showDialog(int initX, int initY) {
		if (m_PropertyDialog == null) {
//			int x = getLocationOnScreen().x;
//			int y = getLocationOnScreen().y;
			m_PropertyDialog = new PropertyDialog(m_PropertyEditor, EVAHELP.cutClassName(m_PropertyEditor.getClass().getName()) , initX, initY);
			m_PropertyDialog.setPreferredSize(new Dimension(500,300));
		}
		else {
			m_PropertyDialog.updateFrameTitle(m_PropertyEditor);
//				System.out.println("" + BeanInspector.toString(m_PropertyDialog));
			m_PropertyDialog.setVisible(false);
			m_PropertyDialog.setExtendedState(JFrame.NORMAL);
			m_PropertyDialog.setVisible(true);
			m_PropertyDialog.requestFocus();
//				System.out.println("" + BeanInspector.toString(m_PropertyDialog));
//				System.out.println("Aft: " + m_PropertyDialog.isShowing() + " " + m_PropertyDialog.isVisible() + " " + m_PropertyDialog.isActive() + " " + m_PropertyDialog.isFocused());
		}
	}	
	
	/**
	 *
	 */
	public void removeNotify() {
		if (m_PropertyDialog != null) {
//			System.out.println("  m_PropertyDialog.dispose();");
//			System.out.println(m_PropertyDialog.isActive());
//			System.out.println(m_PropertyDialog.isVisible());
//			System.out.println(m_PropertyDialog.isValid());
//			System.out.println(m_PropertyDialog.isDisplayable());
//			if (m_PropertyDialog.isDisplayable()) m_PropertyDialog.dispose(); // this caused a deadlock!
//			m_PropertyDialog.dispose(); // this also caused a deadlock!
			m_PropertyDialog = null;
		}
	}
	
	/**
	 *
	 */
	public void paintComponent(Graphics g) {
		Insets i = getInsets();
		Rectangle box = new Rectangle(i.left, i.top,
				getSize().width - i.left - i.right ,
				getSize().height - i.top - i.bottom);
		g.clearRect(i.left, i.top,
				getSize().width - i.right - i.left,
				getSize().height - i.bottom - i.top);
		m_PropertyEditor.paintValue(g, box);

//		Rectangle box = new Rectangle(i.left,i.top,
//		this.getWidth() - i.right,
//		this.getHeight() - i.bottom );
	}
	
	public PropertyEditor getEditor() {
		return m_PropertyEditor;
	}
}
