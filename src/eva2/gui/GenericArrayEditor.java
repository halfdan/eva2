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
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eva2.tools.EVAHELP;
import eva2.tools.SerializedObject;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
public class GenericArrayEditor extends JPanel
implements PropertyEditor {
	/** Handles property change notification */
	private PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
	/** The label for when we can't edit that type */
	private JLabel m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
	/** The list component displaying current values */
	private JList m_ElementList = new JList();
	/** The class of objects allowed in the array */
	private Class m_ElementClass = String.class;
	/** The defaultlistmodel holding our data */
	private DefaultListModel m_ListModel;
	/** The property editor for the class we are editing */
	private PropertyEditor m_ElementEditor;
	/** Cheat to handle selectable lists as well */
	private PropertySelectableList selectableList = null;
	/** Click this to delete the selected array values */
	private JButton m_DeleteBut = new JButton("Delete");
	/** list of additional buttons above the list */
	private List<JButton> m_AdditionalUpperButtonList = new LinkedList<JButton>();
	/** list of additional buttons below the list */
	private List<JButton> m_AdditionalLowerButtonList = new LinkedList<JButton>();
	
	private JPanel additionalCenterPane = null;
	
	private List<JMenuItem> m_popupItemList = new LinkedList<JMenuItem>();
	private JButton m_AddBut = new JButton("Add");
	private JButton m_SetBut = new JButton("Set");
	private JButton m_SetAllBut = new JButton("Set all");

	private boolean withAddButton = true;
	private boolean withSetButton = true;
	private boolean withDeleteButton = true;
	
	private Component m_View = null;
	/** Listens to buttons being pressed and taking the appropriate action */
	private ActionListener m_InnerActionListener =
		new ActionListener() {
		//
		public void actionPerformed(ActionEvent e) {
			boolean consistentView = true; // be optimistic...
			if (m_View instanceof PropertyText) { // check consistency!
				consistentView = ((PropertyText)m_View).checkConsistency();
				if (!consistentView) {
//					System.err.println("Warning, inconsistent view!");
					((PropertyText)m_View).updateFromEditor();
				}
			}
			if (e.getSource() == m_DeleteBut) {
				int [] selected = m_ElementList.getSelectedIndices();
				if (selected != null) {
					for (int i = selected.length-1; i>=0; i--) {
						int current = selected[i];
						m_ListModel.removeElementAt(current);
						if (m_ListModel.size() > current) {
							m_ElementList.setSelectedIndex(current);
						}
						m_ElementList.setModel(m_ListModel);
					}

					if (selectableList!=null) selectableList.setObjects(modelToArray(selectableList.getObjects(), m_ListModel));
					m_Support.firePropertyChange("", null, null);
				}
				if (m_ElementList.getSelectedIndex() == -1) {
					m_DeleteBut.setEnabled(false);
				}
			} else if (e.getSource() == m_AddBut) {
				int selected = m_ElementList.getSelectedIndex();
				Object addObj = m_ElementEditor.getValue();

				// Make a full copy of the object using serialization
				try {
					SerializedObject so = new SerializedObject(addObj);
					addObj = so.getObject();
					so=null;
					if (selected != -1) {
						m_ListModel.insertElementAt(addObj, selected);
					} else {
						m_ListModel.addElement(addObj);
					}
					m_ElementList.setModel(m_ListModel);
					if (selectableList!=null) selectableList.setObjects(modelToArray(selectableList.getObjects(), m_ListModel));
					m_Support.firePropertyChange("", null, null);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(GenericArrayEditor.this,"Could not create an object copy",null,JOptionPane.ERROR_MESSAGE);
				}
			} else if (e.getSource() == m_SetAllBut) {
				Object addObj = m_ElementEditor.getValue();
				for (int i=0; i<m_ListModel.size(); i++) {
					try {
						m_ListModel.setElementAt(new SerializedObject(addObj).getObject(), i);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(GenericArrayEditor.this,"Could not create an object copy",null,JOptionPane.ERROR_MESSAGE);
					}
				}
				m_Support.firePropertyChange("", null, null);
			} else if (e.getSource() == m_SetBut) {
				int selected = m_ElementList.getSelectedIndex();
				Object addObj = m_ElementEditor.getValue();
				if (selected>=0 && (selected <m_ListModel.size())) {
					try {
						m_ListModel.setElementAt(new SerializedObject(addObj).getObject(), selected);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(GenericArrayEditor.this,"Could not create an object copy",null,JOptionPane.ERROR_MESSAGE);
					}
					m_Support.firePropertyChange("", null, null);
				}
			}
		}
	};
	
	public void setAdditionalCenterPane(JPanel panel) {
		this.additionalCenterPane = panel;
	}
	
	private Object[] modelToArray(Object[] origArray, DefaultListModel listModel) {
		Class objClass = origArray.getClass().getComponentType();
		Object[] os = (Object[]) java.lang.reflect.Array.newInstance(objClass, listModel.size());

//		Object[] os= new Object[listModel.size()];
		for (int i=0; i<listModel.size(); i++) {
			os[i]=listModel.get(i);
		}
		return os;
	}
	
	/** Listens to list items being selected and takes appropriate action */
	private ListSelectionListener m_InnerSelectionListener =
		new ListSelectionListener() {
		//
		public void valueChanged(ListSelectionEvent e) {

			if (e.getSource() == m_ElementList) {
				// Enable the delete button
				if (m_ElementList.getSelectedIndex() != -1) {
					m_DeleteBut.setEnabled(true);
					m_ElementEditor.setValue(m_ElementList.getSelectedValue());
					if (m_View instanceof PropertyText) ((PropertyText)m_View).updateFromEditor();
				}
			}
		}
	};

	/**
	 * Sets up the array editor.
	 */
	public GenericArrayEditor() {
		setLayout(new BorderLayout());
		add(m_Label, BorderLayout.CENTER);
		m_DeleteBut.addActionListener(m_InnerActionListener);
		m_AddBut.addActionListener(m_InnerActionListener);
		m_SetAllBut.addActionListener(m_InnerActionListener);
		m_SetBut.addActionListener(m_InnerActionListener);
		m_ElementList.addListSelectionListener(m_InnerSelectionListener);
		m_AddBut.setToolTipText("Add the current item to the list");
		m_DeleteBut.setToolTipText("Delete the selected list item");
		m_ElementList.addMouseListener(new ActionJList(m_ElementList, this));
	}

	public int[] getSelectedIndices() {
		return m_ElementList.getSelectedIndices();
	}

	private class ActionJList extends MouseAdapter {
		protected JList list;
		GenericArrayEditor gae=null;
//		PropertyPanel propPanel=null;
		
		
		public ActionJList(JList l, GenericArrayEditor genAE){
			list = l;
			gae=genAE;
//			if (pPan instanceof PropertyPanel ) propPanel = (PropertyPanel) pPan;
//			else {
//				System.err.println("Error, invalid property panel in " + this.getClass());
//			}
		}

		public void mouseClicked(MouseEvent e){
			if(e.getClickCount() == 2){
				int index = list.locationToIndex(e.getPoint());
				// Check if the index is valid and if the indexed cell really contains the clicked point
				if (index>=0 && (list.getCellBounds(index, index).contains(e.getPoint()))) {
					PropertyPanel propPanel=null;
					Component comp = gae.m_View;
					if (comp instanceof PropertyPanel ) propPanel = (PropertyPanel) comp;
					else System.err.println("Error, invalid property panel in " + this.getClass());
					ListModel dlm = list.getModel();
					Object item = dlm.getElementAt(index);
					list.ensureIndexIsVisible(index);
//					System.out.println(e);
//					System.out.println("Double clicked on " + item);
					propPanel.getEditor().setValue(item);
					propPanel.showDialog(e.getXOnScreen(), e.getYOnScreen());
					propPanel=null;
//					int x = getLocationOnScreen().x;
//					int y = getLocationOnScreen().y;
					
//					if (m_PropertyDialog == null)
//						m_PropertyDialog = new PropertyDialog(gae.m_ElementEditor, EVAHELP.cutClassName(gae.m_ElementEditor.getClass().getName()) , x, y);
//					else {
//						m_PropertyDialog.updateFrameTitle(gae.m_ElementEditor);
//						m_PropertyDialog.set
//						m_PropertyDialog.setVisible(false);
//						m_PropertyDialog.setExtendedState(JFrame.NORMAL);
//						m_PropertyDialog.setVisible(true);
//						m_PropertyDialog.requestFocus();
//					}

				}
			}
		}
	}
	/* This class handles the creation of list cell renderers from the
	 * property editors.
	 */
	private class EditorListCellRenderer implements ListCellRenderer {
		/** The class of the property editor for array objects */
		private Class m_EditorClass;
		/** The class of the array values */
		private Class m_ValueClass;
		/**
		 * Creates the list cell renderer.
		 *
		 * @param editorClass The class of the property editor for array objects
		 * @param valueClass The class of the array values
		 */
		public EditorListCellRenderer(Class editorClass, Class valueClass) {
			m_EditorClass = editorClass;
			m_ValueClass = valueClass;
		}
		/**
		 * Creates a cell rendering component.
		 *
		 * @param JList the list that will be rendered in
		 * @param Object the cell value
		 * @param int which element of the list to render
		 * @param boolean true if the cell is selected
		 * @param boolean true if the cell has the focus
		 * @return the rendering component
		 */
		public Component getListCellRendererComponent(final JList list,
				final Object value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus) {
			try {
				final PropertyEditor e = (PropertyEditor)m_EditorClass.newInstance();
				if (e instanceof GenericObjectEditor) {
					//	  ((GenericObjectEditor) e).setDisplayOnly(true);
					((GenericObjectEditor) e).setClassType(m_ValueClass);
				}
				e.setValue(value);
				JPanel cellPanel = new JPanel() {
//					return new JCheckBox("", isSelected) {
//						public void paintComponent(Graphics g) {
//							String name = (String)BeanInspector.callIfAvailable(value, "getName", new Object[]{});
//							if (name==null) setText(value.getClass().getSimpleName());
//							else setText(name);
//							super.paintComponent(g);
					public void paintComponent(Graphics g) {
						Insets i = this.getInsets();
						Rectangle box = new Rectangle(i.left,i.top,
								this.getWidth(), //- i.right,
								this.getHeight() );//- i.bottom +20);
						g.setColor(isSelected ? list.getSelectionBackground() : list.getBackground());
						g.fillRect(0, 0, this.getWidth(), this.getHeight());
						g.setColor(isSelected ? list.getSelectionForeground(): list.getForeground());
						e.paintValue(g, box);
					}
					public Dimension getPreferredSize() {
						Font f = this.getFont();
						FontMetrics fm = this.getFontMetrics(f);
						Dimension newPref = new Dimension(0, fm.getHeight());
						newPref.height = getFontMetrics(getFont()).getHeight() * 6 / 4;  //6 / 4;
						newPref.width = newPref.height * 6; //5
						return  newPref;
					}
				};
				return cellPanel;
			} catch (Exception ex) {
				return null;
			}
		}
	}

	/**
	 * Updates the type of object being edited, so attempts to find an
	 * appropriate propertyeditor.
	 *
	 * @param o a value of type 'Object'
	 */
	private void updateEditorType(Object obj) {

		// Determine if the current object is an array
		m_ElementEditor = null; 
		m_ListModel = null;
		m_View = null;
		removeAll();

		if ((obj != null) && (obj.getClass().isArray() || (obj instanceof PropertySelectableList))) {
			Object arrayInstance = obj;
			if (!(obj.getClass().isArray())) {
				arrayInstance=((PropertySelectableList)obj).getObjects();
				selectableList = (PropertySelectableList)obj;
			} else selectableList = null;
			Class elementClass = arrayInstance.getClass().getComponentType();
			PropertyEditor editor = PropertyEditorProvider.findEditor(elementClass);
			if (editor instanceof EnumEditor) editor.setValue(obj);
			m_View = null;
			ListCellRenderer lcr = new DefaultListCellRenderer();
			if (editor != null) {
				if (editor instanceof GenericObjectEditor) {
//					((GenericObjectEditor) editor).getCustomEditor();
					((GenericObjectEditor) editor).setClassType(elementClass);
				}
				if (editor.isPaintable() && editor.supportsCustomEditor()) {
					m_View = new PropertyPanel(editor);
					lcr = new EditorListCellRenderer(editor.getClass(), elementClass);
				} else if (editor.getTags() != null) {
					m_View = new PropertyValueSelector(editor);
				} else if (editor.getAsText() != null) {
					m_View = new PropertyText(editor);
				}
			}
			if (m_View == null) {
				System.err.println("No property editor for class: "
						+ elementClass.getName());
			} else {
				m_ElementEditor = editor;

				// Create the ListModel and populate it
				m_ListModel = new DefaultListModel();
				m_ElementClass = elementClass;
				for (int i = 0; i < Array.getLength(arrayInstance); i++) {
					m_ListModel.addElement(Array.get(arrayInstance,i));
				}
				m_ElementList.setCellRenderer(lcr);
				m_ElementList.setModel(m_ListModel);
				if (m_ListModel.getSize() > 0) {
					m_ElementList.setSelectedIndex(0);
					m_DeleteBut.setEnabled(true);
				} else {
					m_DeleteBut.setEnabled(false);
				}

				try {
					if (m_ListModel.getSize() > 0) {
						m_ElementEditor.setValue(m_ListModel.getElementAt(0));
					} else {
						if (m_ElementEditor instanceof GenericObjectEditor) {
							((GenericObjectEditor)m_ElementEditor).setDefaultValue();
						} else {
							if (m_ElementEditor.getValue()!=null) {
								m_ElementEditor.setValue(m_ElementClass.newInstance());
							}
						}
					}

					setPreferredSize(new Dimension(300,400));
//					JPanel panel = new JPanel();
//					panel.setLayout(new BorderLayout());
//					panel.add(view, BorderLayout.CENTER);
//					panel.add(m_AddBut, BorderLayout.EAST);
//					JPanel buttonPanel=new JPanel(new FlowLayout());
					
					if (withAddButton && !(m_AdditionalUpperButtonList.contains(m_AddBut))) m_AdditionalUpperButtonList.add(m_AddBut);
					if (withSetButton && !(m_AdditionalUpperButtonList.contains(m_SetBut))) m_AdditionalUpperButtonList.add(m_SetBut);
					if (withSetButton && !(m_AdditionalUpperButtonList.contains(m_SetAllBut))) m_AdditionalUpperButtonList.add(m_SetAllBut);
					
					JPanel combiUpperPanel = new JPanel(getButtonLayout(1, m_AdditionalUpperButtonList));
					combiUpperPanel.add(m_View );

					for (JButton but : m_AdditionalUpperButtonList) {
						combiUpperPanel.add(but);
					}
					add(combiUpperPanel, BorderLayout.NORTH);
					if (additionalCenterPane==null) add(new JScrollPane(m_ElementList), BorderLayout.CENTER);
					else {
						JPanel centerPane=new JPanel();
						centerPane.setLayout(new GridLayout(2, 1));
						centerPane.add(new JScrollPane(m_ElementList));
						centerPane.add(additionalCenterPane);
						add(centerPane, BorderLayout.CENTER);
					}
					
					if (withDeleteButton && !m_AdditionalLowerButtonList.contains(m_DeleteBut)) m_AdditionalLowerButtonList.add(m_DeleteBut);
					JPanel combiLowerPanel = new JPanel(getButtonLayout(0, m_AdditionalLowerButtonList));
					for (JButton but : m_AdditionalLowerButtonList) {
						combiLowerPanel.add(but);
					}
					add(combiLowerPanel, BorderLayout.SOUTH);
					m_ElementEditor.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent e) {
							repaint();
						}
					});
					
					addPopupMenu();
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					ex.printStackTrace();
					m_ElementEditor = null;
				}
			}
		}
		if (m_ElementEditor == null) {
			add(m_Label, BorderLayout.CENTER);
		}
		m_Support.firePropertyChange("", null, null);
		validate();
	}
	
	/**
	 * Make a fitting grid layout for a list of buttons. An additional offset may be given
	 * if further components should be added besides the buttons.
	 * 
	 * @param additionalOffset
	 * @param bList
	 * @return
	 */
	private LayoutManager getButtonLayout(int additionalOffset, List<JButton> bList) {
		int lines = 1+((bList.size()+additionalOffset-1)/3);
		int cols = 3;
		return new GridLayout(lines, cols);
	}

	public void removeUpperActionButton(String text) {
		removeActionButton(m_AdditionalUpperButtonList, text);
	}
	
	public void removeLowerActionButton(String text) {
		removeActionButton(m_AdditionalLowerButtonList, text);
	}
	
	protected void removeActionButton(List<JButton> bList, String text) {
		JButton but = null;
		for (JButton jb : bList) {
			if (text.equals(jb.getText())) {
				but = jb;
				break;
			}
		}
		if (but!=null) bList.remove(but);
	}
	
	public void addUpperActionButton(String text, ActionListener al) {
		addActionButton(m_AdditionalUpperButtonList, text, al);
	}
	
	/**
	 * Wrap an action listener such that the selection state will always be up to date
	 * in the selectableList (if it exists).
	 * @param al
	 * @return
	 */
	private ActionListener makeSelectionKnownAL(final ActionListener al) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectableList!=null) {
					selectableList.setSelectionByIndices(m_ElementList.getSelectedIndices());
				}
				al.actionPerformed(e);
			}
		};
	}

	public void addLowerActionButton(String text, ActionListener al) {
		addActionButton(m_AdditionalLowerButtonList, text, al);
	}
	
	public void addActionButton(List<JButton> bList, String text, ActionListener al) {
		JButton but = new JButton(text);
		but.addActionListener(makeSelectionKnownAL(al));
		bList.add(but);
	}
		
	/**
	 * Sets the current object array.
	 *
	 * @param o an object that must be an array.
	 */
	public void setValue(Object o) {
		// Create a new list model, put it in the list and resize?
		updateEditorType(o);
	}

	/**
	 * Select all items. If all are selected, then deselect all items.
	 */
	public void selectDeselectAll() {
		if (areAllSelected()) m_ElementList.getSelectionModel().clearSelection();
		else m_ElementList.setSelectionInterval(0, m_ElementList.getModel().getSize()-1);
	}
	
	public boolean areAllSelected() {
		for (int i=0; i<m_ElementList.getModel().getSize(); i++) {
			if (!m_ElementList.isSelectedIndex(i)) return false;
		}
		return true;
	}
	
	/**
	 * Gets the current object array.
	 *
	 * @return the current object array
	 */
	public Object getValue() {
		if (m_ListModel == null) {
			return null;
		}
		if (selectableList!=null) {
			return  selectableList;
		} else {
		// 	Convert the listmodel to an array of strings and return it.
			int length = m_ListModel.getSize();
			Object result = Array.newInstance(m_ElementClass, length);
			for (int i = 0; i < length; i++) {
				Array.set(result, i, m_ListModel.elementAt(i));
			}
			return result;
		}
	}

	public void addPopupItem(String text, ActionListener al) {
		JMenuItem item = createMenuItem(text, true, makeSelectionKnownAL(al));
		m_popupItemList.add(item);
	}
	
	public void addPopupMenu() {
		if (m_popupItemList.size()>0) {
			m_ElementList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (selectableList!=null) {
						selectableList.setSelectionByIndices(m_ElementList.getSelectedIndices());
					}
					if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
						// do nothing
					} else { // right click released, so show popup
						JPopupMenu popupMenu = new JPopupMenu();
						for (JMenuItem item : m_popupItemList) popupMenu.add(item);
						popupMenu.show(GenericArrayEditor.this, e.getX(), e.getY());
					}
				}
			});
		}
	}

	/**
	 * Create a menu item with given title and listener, add it to the menu and
	 * return it. It may be enabled or disabled.
	 * 
	 * @param menu
	 * @param title
	 * @param aListener
	 * @param enabled
	 * @return
	 */
	private JMenuItem createMenuItem(String title, boolean enabled,
			ActionListener aListener) {
		JMenuItem item = new JMenuItem(title);
		// if (bgColor!=null) item.setForeground(bgColor);
		item.addActionListener(aListener);
		item.setEnabled(enabled);
		return item;
	}
	
	/**
	 * Supposedly returns an initialization string to create a classifier
	 * identical to the current one, including it's state, but this doesn't
	 * appear possible given that the initialization string isn't supposed to
	 * contain multiple statements.
	 *
	 * @return the java source code initialisation string
	 */
	public String getJavaInitializationString() {
		return "null";
	}

	/**
	 * Returns true to indicate that we can paint a representation of the
	 * string array
	 *
	 * @return true
	 */
	public boolean isPaintable() {
		return true;
	}

	/**
	 * Paints a representation of the current classifier.
	 *
	 * @param gfx the graphics context to use
	 * @param box the area we are allowed to paint into
	 */
	public void paintValue(Graphics gfx, Rectangle box) {
		FontMetrics fm = gfx.getFontMetrics();
		int vpad = (box.height - fm.getAscent()) / 2;
//		System.out.println(m_ListModel + " --- " + m_ElementClass);
		String rep;
		if (m_ListModel.getSize() == 0) rep="Empty";
		else {
			rep = m_ListModel.getSize() + " of " + EVAHELP.cutClassName(m_ElementClass.getName());
			Object maybeName = BeanInspector.callIfAvailable(m_ListModel.get(0), "getName", new Object[]{});
			if (maybeName!=null) {
				rep = rep + " ("+(String)maybeName + "...)";
			}
		}
		gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
	}
	/**
	 *
	 */
	public String getAsText() {
		return null;
	}
	/**
	 *
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		throw new IllegalArgumentException(text);
	}
	/**
	 *
	 */
	public String[] getTags() {
		return null;
	}
	/**
	 *
	 */
	public boolean supportsCustomEditor() {
		return true;
	}
	/**
	 *
	 */
	public Component getCustomEditor() {
		return this;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (m_Support == null) m_Support = new PropertyChangeSupport(this);
		m_Support.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (m_Support == null) m_Support = new PropertyChangeSupport(this);
		m_Support.removePropertyChangeListener(l);
	}

	/**
	 *
	 */
//	public static void main(String [] args) {
//		try {
//			java.beans.PropertyEditorManager.registerEditor(SelectedTag.class,TagEditor.class);
//			java.beans.PropertyEditorManager.registerEditor(int [].class,GenericArrayEditor.class);
//			java.beans.PropertyEditorManager.registerEditor(double [].class,GenericArrayEditor.class);
//			GenericArrayEditor editor = new GenericArrayEditor();
//			
//
//			int[] initial = { 3,45, 7};
//			editor.setValue(initial);
//			PropertyDialog pd = new PropertyDialog(editor,EVAHELP.cutClassName(editor.getClass().getName())
//					, 100, 100);
////			pd.setSize(200,200);
//			pd.addWindowListener(new WindowAdapter() {
//				public void windowClosing(WindowEvent e) {
//					System.exit(0);
//				}
//			});
//			editor.setValue(initial);
//			//ce.validate();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.err.println(ex.getMessage());
//		}
//	}
	public boolean isWithAddButton() {
		return withAddButton;
	}
	public void setWithAddButton(boolean withAddButton) {
		this.withAddButton = withAddButton;
	}
	public boolean isWithSetButton() {
		return withSetButton;
	}
	public void setWithSetButton(boolean withSetButton) {
		this.withSetButton = withSetButton;
	}
	
	public boolean isWithDeleteButton() {
		return withDeleteButton;
	}
	public void setWithDeleteButton(boolean wB) {
		this.withDeleteButton = wB;
	}
	
	public void removeNotify() {
		super.removeNotify();
	}
}

