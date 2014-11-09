package eva2.gui.editor;

import eva2.gui.*;
import eva2.tools.EVAHELP;
import eva2.tools.SerializedObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArrayEditor extends JPanel implements PropertyEditor {

    private final static Logger LOGGER = Logger.getLogger(ArrayEditor.class.getName());
    /**
     * Handles property change notification
     */
    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    /**
     * The label for when we can't edit that type
     */
    private JLabel cantEditLabel = new JLabel("Can't edit", SwingConstants.CENTER);
    /**
     * The list component displaying current values
     */
    private JList elementList = new JList();
    /**
     * The class of objects allowed in the array
     */
    private Class elementClass = String.class;
    /**
     * The defaultlistmodel holding our data
     */
    private DefaultListModel listModel;
    /**
     * The property editor for the class we are editing
     */
    private PropertyEditor elementEditor;
    /**
     * Cheat to handle selectable lists as well
     */
    private PropertySelectableList selectableList = null;
    /**
     * Click this to delete the selected array values
     */
    private JButton deleteButton = new JButton("Delete");
    /**
     * list of additional buttons above the list
     */
    private List<JButton> upperButtonList = new LinkedList<>();
    /**
     * list of additional buttons below the list
     */
    private List<JButton> lowerButtonList = new LinkedList<>();
    private JComponent additionalCenterComp = null;
    private List<JMenuItem> popupItemList = new LinkedList<>();
    private JButton addButton = new JButton("Add");
    private JButton setButton = new JButton("Set");
    private JButton setAllButton = new JButton("Set all");
    private boolean withAddButton = true;
    private boolean withSetButton = true;
    private boolean withDeleteButton = true;
    private Component view = null;
    /**
     * Listens to buttons being pressed and taking the appropriate action
     */
    private ActionListener innerActionListener = new ActionListener() {
        //

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean consistentView = true; // be optimistic...
            if (view instanceof PropertyText) { // check consistency!
                consistentView = ((PropertyText) view).checkConsistency();
                if (!consistentView) {
                    ((PropertyText) view).updateFromEditor();
                }
            }
            if (e.getSource() == deleteButton) {
                int[] selected = elementList.getSelectedIndices();
                if (selected != null) {
                    for (int i = selected.length - 1; i >= 0; i--) {
                        int current = selected[i];
                        listModel.removeElementAt(current);
                        if (listModel.size() > current) {
                            elementList.setSelectedIndex(current);
                        }
                        elementList.setModel(listModel);
                    }

                    if (selectableList != null) {
                        selectableList.setObjects(modelToArray(selectableList.getObjects(), listModel));
                    }
                    propChangeSupport.firePropertyChange("", null, null);
                }
                if (elementList.getSelectedIndex() == -1) {
                    deleteButton.setEnabled(false);
                }
            } else if (e.getSource() == addButton) {
                int selected = elementList.getSelectedIndex();
                Object addObj = elementEditor.getValue();

                // Make a full copy of the object using serialization
                try {
                    SerializedObject so = new SerializedObject(addObj);
                    addObj = so.getObject();
                    so = null;
                    if (selected != -1) {
                        listModel.insertElementAt(addObj, selected);
                    } else {
                        listModel.addElement(addObj);
                    }
                    elementList.setModel(listModel);
                    if (selectableList != null) {
                        selectableList.setObjects(modelToArray(selectableList.getObjects(), listModel));
                    }
                    propChangeSupport.firePropertyChange("", null, null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ArrayEditor.this, "Could not create an object copy", null, JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getSource() == setAllButton) {
                Object addObj = elementEditor.getValue();
                for (int i = 0; i < listModel.size(); i++) {
                    try {
                        listModel.setElementAt(new SerializedObject(addObj).getObject(), i);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(ArrayEditor.this, "Could not create an object copy", null, JOptionPane.ERROR_MESSAGE);
                    }
                }
                propChangeSupport.firePropertyChange("", null, null);
            } else if (e.getSource() == setButton) {
                int selected = elementList.getSelectedIndex();
                Object addObj = elementEditor.getValue();
                if (selected >= 0 && (selected < listModel.size())) {
                    try {
                        listModel.setElementAt(new SerializedObject(addObj).getObject(), selected);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(ArrayEditor.this, "Could not create an object copy", null, JOptionPane.ERROR_MESSAGE);
                    }
                    propChangeSupport.firePropertyChange("", null, null);
                }
            }
        }
    };

    public void setAdditionalCenterPane(JComponent component) {
        this.additionalCenterComp = component;
    }

    private Object[] modelToArray(Object[] origArray, DefaultListModel listModel) {
        Class objClass = origArray.getClass().getComponentType();
        Object[] os = (Object[]) java.lang.reflect.Array.newInstance(objClass, listModel.size());

        for (int i = 0; i < listModel.size(); i++) {
            os[i] = listModel.get(i);
        }
        return os;
    }

    /**
     * Listens to list items being selected and takes appropriate action
     */
    private ListSelectionListener innerSelectionListener =
            new ListSelectionListener() {
                //

                @Override
                public void valueChanged(ListSelectionEvent e) {

                    if (e.getSource() == elementList) {
                        // Enable the delete button
                        if (elementList.getSelectedIndex() != -1) {
                            deleteButton.setEnabled(true);
                            elementEditor.setValue(elementList.getSelectedValue());
                            if (view instanceof PropertyText) {
                                ((PropertyText) view).updateFromEditor();
                            }
                        }
                    }
                }
            };

    /**
     * Sets up the array editor.
     */
    public ArrayEditor() {
        setLayout(new BorderLayout());
        add(cantEditLabel, BorderLayout.CENTER);
        deleteButton.addActionListener(innerActionListener);
        addButton.addActionListener(innerActionListener);
        setAllButton.addActionListener(innerActionListener);
        setButton.addActionListener(innerActionListener);
        elementList.addListSelectionListener(innerSelectionListener);
        addButton.setToolTipText("Add the current item to the list");
        deleteButton.setToolTipText("Delete the selected list item");
        elementList.addMouseListener(new ActionJList(elementList, this));
    }

    public int[] getSelectedIndices() {
        return elementList.getSelectedIndices();
    }

    private class ActionJList extends MouseAdapter {

        protected JList list;
        ArrayEditor gae = null;

        public ActionJList(JList l, ArrayEditor genAE) {
            list = l;
            gae = genAE;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                // Check if the index is valid and if the indexed cell really contains the clicked point
                if (index >= 0 && (list.getCellBounds(index, index).contains(e.getPoint()))) {
                    PropertyPanel propPanel = null;
                    Component comp = gae.view;
                    if (comp instanceof PropertyPanel) {
                        propPanel = (PropertyPanel) comp;
                    } else {
                        System.err.println("Error, invalid property panel in " + this.getClass());
                    }
                    ListModel dlm = list.getModel();
                    Object item = dlm.getElementAt(index);
                    list.ensureIndexIsVisible(index);
                    propPanel.getEditor().setValue(item);
                    propPanel.showDialog();
                    propPanel = null;
                }
            }
        }
    }
    /*
     * This class handles the creation of list cell renderers from the property editors.
     */

    private class EditorListCellRenderer implements ListCellRenderer {

        /**
         * The class of the property editor for array objects
         */
        private Class editorClass;
        /**
         * The class of the array values
         */
        private Class valueClass;

        /**
         * Creates the list cell renderer.
         *
         * @param editorClass The class of the property editor for array objects
         * @param valueClass  The class of the array values
         */
        public EditorListCellRenderer(Class editorClass, Class valueClass) {
            this.editorClass = editorClass;
            this.valueClass = valueClass;
        }

        /**
         * Creates a cell rendering component.
         *
         * @param list          the list that will be rendered in
         * @param value         the cell value
         * @param index         which element of the list to render
         * @param isSelected    true if the cell is selected
         * @param cellHasFocus  true if the cell has the focus
         * @return the rendering component
         */
        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {
            try {
                final PropertyEditor e = (PropertyEditor) editorClass.newInstance();
                if (e instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) e).setClassType(valueClass);
                }
                e.setValue(value);
                JPanel cellPanel = new JPanel() {

                    @Override
                    public void paintComponent(Graphics g) {
                        Insets i = this.getInsets();
                        Rectangle box = new Rectangle(i.left, i.top,
                                this.getWidth(), //- i.right,
                                this.getHeight());//- i.bottom +20);
                        g.setColor(isSelected ? list.getSelectionBackground() : list.getBackground());
                        g.fillRect(0, 0, this.getWidth(), this.getHeight());
                        g.setColor(isSelected ? list.getSelectionForeground() : list.getForeground());
                        e.paintValue(g, box);
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        Font f = this.getFont();
                        FontMetrics fm = this.getFontMetrics(f);
                        Dimension newPref = new Dimension(0, fm.getHeight());
                        newPref.height = getFontMetrics(getFont()).getHeight() * 6 / 4;  //6 / 4;
                        newPref.width = newPref.height * 6; //5
                        return newPref;
                    }
                };
                return cellPanel;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * Updates the type of object being edited, so attempts to find an appropriate propertyeditor.
     *
     * @param obj a value of type 'Object'
     */
    private void updateEditorType(Object obj) {

        // Determine if the current object is an array
        elementEditor = null;
        listModel = null;
        view = null;
        removeAll();

        if ((obj != null) && (obj.getClass().isArray() || (obj instanceof PropertySelectableList))) {
            Object arrayInstance = obj;
            if (!(obj.getClass().isArray())) {
                arrayInstance = ((PropertySelectableList) obj).getObjects();
                selectableList = (PropertySelectableList) obj;
            } else {
                selectableList = null;
            }
            Class elementClass = arrayInstance.getClass().getComponentType();
            PropertyEditor editor = PropertyEditorProvider.findEditor(elementClass);
            if (editor instanceof EnumEditor) {
                editor.setValue(obj);
            }
            view = null;
            ListCellRenderer lcr = new DefaultListCellRenderer();
            if (editor != null) {
                if (editor instanceof GenericObjectEditor) {
                    ((GenericObjectEditor) editor).setClassType(elementClass);
                }
                if (editor.isPaintable() && editor.supportsCustomEditor()) {
                    view = new PropertyPanel(editor);
                    lcr = new EditorListCellRenderer(editor.getClass(), elementClass);
                } else if (editor.getTags() != null) {
                    view = new PropertyValueSelector(editor);
                } else if (editor.getAsText() != null) {
                    view = new PropertyText(editor);
                } else if (view == null) {
                    /* Dirty hack to view PropertyDoubleArray component */
                    view = new PropertyText(editor);
                }
            }
            if (view == null) {
                LOGGER.log(Level.WARNING, "No property editor for class: {0}", elementClass.getName());
            } else {
                elementEditor = editor;

                // Create the ListModel and populate it
                listModel = new DefaultListModel();
                this.elementClass = elementClass;
                for (int i = 0; i < Array.getLength(arrayInstance); i++) {
                    listModel.addElement(Array.get(arrayInstance, i));
                }

                elementList.setCellRenderer(lcr);
                elementList.setModel(listModel);

                if (listModel.getSize() > 0) {
                    elementList.setSelectedIndex(0);
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }

                try {
                    if (listModel.getSize() > 0) {
                        elementEditor.setValue(listModel.getElementAt(0));
                    } else {
                        if (elementEditor instanceof GenericObjectEditor) {
                            ((GenericObjectEditor) elementEditor).setDefaultValue();
                        } else {
                            if (elementEditor.getValue() != null) {
                                elementEditor.setValue(elementClass.newInstance());
                            }
                        }
                    }

                    //setPreferredSize(new Dimension(400,500));

                    if (withAddButton && !(upperButtonList.contains(addButton))) {
                        upperButtonList.add(addButton);
                    }
                    if (withSetButton && !(upperButtonList.contains(setButton))) {
                        upperButtonList.add(setButton);
                    }
                    if (withSetButton && !(upperButtonList.contains(setAllButton))) {
                        upperButtonList.add(setAllButton);
                    }

                    // Upper Button Panel
                    JPanel combiUpperPanel = new JPanel(getButtonLayout(1, upperButtonList));
                    // ToDo Figure out how to now show this on Job Pane
                    combiUpperPanel.add(view);
                    view.setVisible(withAddButton);

                    for (JButton but : upperButtonList) {
                        combiUpperPanel.add(but);
                    }

                    setLayout(new GridBagLayout());

                    GridBagConstraints gbConstraints = new GridBagConstraints();
                    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
                    gbConstraints.gridx = 0;
                    gbConstraints.gridy = 0;
                    add(combiUpperPanel, gbConstraints);

                    // Job List
                    gbConstraints.gridy++;
                    gbConstraints.fill = GridBagConstraints.BOTH;
                    gbConstraints.weightx = 1.0;
                    gbConstraints.weighty = 1.0;
                    add(new JScrollPane(elementList), gbConstraints);

                    // Lower Button Panel
                    if (withDeleteButton && !lowerButtonList.contains(deleteButton)) {
                        lowerButtonList.add(deleteButton);
                    }
                    JPanel combiLowerPanel = new JPanel(getButtonLayout(0, lowerButtonList));
                    for (JButton but : lowerButtonList) {
                        combiLowerPanel.add(but);
                    }
                    gbConstraints.gridy++;
                    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
                    gbConstraints.weightx = 1.0;
                    gbConstraints.weighty = 0.0;
                    add(combiLowerPanel, gbConstraints);

                    // Additional Center Panel (e.g. PropertySheetPanel)
                    if (additionalCenterComp != null) {
                        gbConstraints.weightx = 1.0;
                        gbConstraints.weighty = 1.0;
                        gbConstraints.fill = GridBagConstraints.BOTH;
                        gbConstraints.gridy++;
                        add(additionalCenterComp, gbConstraints);
                    }

                    elementEditor.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(final PropertyChangeEvent event) {
                            repaint();
                        }
                    });

                    addPopupMenu();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();
                    elementEditor = null;
                }
            }
        }
        if (elementEditor == null) {
            add(cantEditLabel, BorderLayout.CENTER);
        }
        propChangeSupport.firePropertyChange("", null, null);
        validate();
    }

    /**
     * Make a fitting grid layout for a list of buttons. An additional offset may be given if
     * further components should be added besides the buttons.
     *
     * @param additionalOffset
     * @param bList
     * @return
     */
    private LayoutManager getButtonLayout(int additionalOffset, List<JButton> bList) {
        int lines = 1 + ((bList.size() + additionalOffset - 1) / 3);
        int cols = 3;
        return new GridLayout(lines, cols);
    }

    public void removeUpperActionButton(String text) {
        removeActionButton(upperButtonList, text);
    }

    public void removeLowerActionButton(String text) {
        removeActionButton(lowerButtonList, text);
    }

    protected void removeActionButton(List<JButton> bList, String text) {
        JButton but = null;
        for (JButton jb : bList) {
            if (text.equals(jb.getText())) {
                but = jb;
                break;
            }
        }
        if (but != null) {
            bList.remove(but);
        }
    }

    public void addUpperActionButton(String text, ActionListener al) {
        addActionButton(upperButtonList, text, al);
    }

    /**
     * Wrap an action listener such that the selection state will always be up to date in the
     * selectableList (if it exists).
     *
     * @param al
     * @return
     */
    private ActionListener makeSelectionKnownAL(final ActionListener al) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectableList != null) {
                    selectableList.setSelectionByIndices(elementList.getSelectedIndices());
                }
                al.actionPerformed(e);
            }
        };
    }

    public void addLowerActionButton(String text, ActionListener al) {
        addActionButton(lowerButtonList, text, al);
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
    @Override
    public void setValue(Object o) {
        // Create a new list model, put it in the list and resize?
        updateEditorType(o);
    }

    /**
     * Select all items. If all are selected, then deselect all items.
     */
    public void selectDeselectAll() {
        if (areAllSelected()) {
            elementList.getSelectionModel().clearSelection();
        } else {
            elementList.setSelectionInterval(0, elementList.getModel().getSize() - 1);
        }
    }

    public boolean areAllSelected() {
        for (int i = 0; i < elementList.getModel().getSize(); i++) {
            if (!elementList.isSelectedIndex(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the current object array.
     *
     * @return the current object array
     */
    @Override
    public Object getValue() {
        if (listModel == null) {
            return null;
        }
        if (selectableList != null) {
            return selectableList;
        } else {
            // 	Convert the listmodel to an array of strings and return it.
            int length = listModel.getSize();
            Object result = Array.newInstance(elementClass, length);
            for (int i = 0; i < length; i++) {
                Array.set(result, i, listModel.elementAt(i));
            }
            return result;
        }
    }

    public void addPopupItem(String text, ActionListener al) {
        JMenuItem item = createMenuItem(text, true, makeSelectionKnownAL(al));
        popupItemList.add(item);
    }

    public void addPopupMenu() {
        if (popupItemList.size() > 0) {
            elementList.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectableList != null) {
                        selectableList.setSelectionByIndices(elementList.getSelectedIndices());
                    }
                    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                        // do nothing
                    } else { // right click released, so show popup
                        JPopupMenu popupMenu = new JPopupMenu();
                        for (JMenuItem item : popupItemList) {
                            popupMenu.add(item);
                        }
                        popupMenu.show(ArrayEditor.this, e.getX(), e.getY());
                    }
                }
            });
        }
    }

    /**
     * Create a menu item with given title and listener, add it to the menu and return it. It may be
     * enabled or disabled.
     *
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
     * Supposedly returns an initialization string to create a classifier identical to the current
     * one, including it's state, but this doesn't appear possible given that the initialization
     * string isn't supposed to contain multiple statements.
     *
     * @return the java source code initialisation string
     */
    @Override
    public String getJavaInitializationString() {
        return "null";
    }

    /**
     * Returns true to indicate that we can paint a representation of the string array
     *
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep;
        if (listModel.getSize() == 0) {
            rep = "Empty";
        } else {
            rep = listModel.getSize() + " of " + EVAHELP.cutClassName(elementClass.getName());
            Object maybeName = BeanInspector.callIfAvailable(listModel.get(0), "getName", new Object[]{});
            if (maybeName != null) {
                rep = rep + " (" + maybeName + "...)";
            }
        }
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3);
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
    }

    /**
     *
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     *
     */
    @Override
    public Component getCustomEditor() {
        return this;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propChangeSupport == null) {
            propChangeSupport = new PropertyChangeSupport(this);
        }
        propChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propChangeSupport == null) {
            propChangeSupport = new PropertyChangeSupport(this);
        }
        propChangeSupport.removePropertyChangeListener(l);
    }

    public boolean isWithAddButton() {
        return withAddButton;
    }

    public void setWithAddButton(boolean withAddButton) {
        this.withAddButton = withAddButton;
        // Hide/Show view based on whether we show the add button
        if (this.view != null) {
            this.view.setVisible(withAddButton);
        }
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

    @Override
    public void removeNotify() {
        super.removeNotify();
    }
}
