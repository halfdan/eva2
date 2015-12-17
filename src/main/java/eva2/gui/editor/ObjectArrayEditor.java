package eva2.gui.editor;

import eva2.gui.*;
import eva2.tools.StringTools;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.lang.reflect.Array;

/**
 * Created by halfdan on 17/12/15.
 */
public class ObjectArrayEditor<T> extends JPanel implements PropertyEditor {
    private T[] value;
    private JList<T> objectList;
    private DefaultListModel<T> listModel;
    private PropertyChangeSupport propChangeSupport;

    public ObjectArrayEditor(Class<T> type) {
        listModel = new DefaultListModel<>();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridx = 0;
        c.gridy = 0;

        TypeSelector typeSelector = new TypeSelector();
        typeSelector.updateClassType(type.getName());

        add(typeSelector, c);

        JButton addButton = new JButton("Add");
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        add(addButton, c);

        JButton removeButton = new JButton("Remove");
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        add(removeButton, c);

        JButton configButton;
        configButton = new JButton("Config");
        c.gridx = 2;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;

        add(configButton, c);

        objectList = new JList<>(listModel);
        objectList.setVisibleRowCount(10);

        c.gridwidth = 2;
        c.gridheight = 5;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JScrollPane scrollPane = new JScrollPane(objectList);
        add(scrollPane, c);

        addButton.addActionListener(event -> {
            String className = ((Item) typeSelector.getSelectedItem()).getId();
            try {
                T n = (T) Class.forName(className).newInstance();
                listModel.addElement(n);
                propChangeSupport.firePropertyChange("", null, null);
            } catch (Exception ex) {
                System.err.println("Exception in itemStateChanged " + ex.getMessage());
                System.err.println("Classpath is " + System.getProperty("java.class.path"));
                ex.printStackTrace();

                JOptionPane.showMessageDialog(this,
                        "Could not create an example of\n"
                                + className + "\n"
                                + "from the current classpath. Is the resource folder at the right place?\nIs the class abstract or the default constructor missing?",
                        "GenericObjectEditor",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        removeButton.addActionListener(event -> {
            if (!objectList.isSelectionEmpty()) {
                listModel.remove(objectList.getSelectedIndex());
                propChangeSupport.firePropertyChange("", null, null);
            }
        });

        configButton.addActionListener(event -> {
            T selected = objectList.getSelectedValue();
            PropertyEditor editor = PropertyEditorProvider.findEditor(selected.getClass());
            editor.setValue(selected);
            PropertyDialog propertyDialog = new PropertyDialog(null, editor, StringTools.cutClassName(editor.getClass().getName()));
            propertyDialog.setPreferredSize(new Dimension(500, 300));
            propertyDialog.setModal(true);
            propertyDialog.setVisible(true);
            propChangeSupport.firePropertyChange("", null, null);
        });
    }

    @Override
    public void setValue(Object value) {
        this.value = (T[])value;
        listModel.removeAllElements();
        for(T i : this.value) {
            listModel.addElement(i);
        }
    }

    @Override
    public Object getValue() {
        if (listModel == null) {
            return null;
        }
        if (true == false) {
            return true;
        } else {
            // 	Convert the listmodel to an array of strings and return it.
            int length = listModel.getSize();
            Object result = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(result, i, listModel.elementAt(i));
            }
            return result;
        }
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep;
        if (listModel.getSize() == 0) {
            rep = "Empty";
        } else {
            rep = listModel.getSize() + " of " + StringTools.cutClassName(value.getClass().getComponentType().getName());
            Object maybeName = BeanInspector.callIfAvailable(listModel.get(0), "getName", new Object[]{});
            if (maybeName != null) {
                rep = rep + " (" + maybeName + "...)";
            }
        }
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3);
    }

    @Override
    public String getJavaInitializationString() {
        return null;
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    @Override
    public String[] getTags() {
        return new String[0];
    }

    @Override
    public Component getCustomEditor() {
        return this;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
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
}
