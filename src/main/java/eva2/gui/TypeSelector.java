package eva2.gui;

import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.StringTools;
import eva2.util.annotation.Description;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by fabian on 16/12/15.
 */
public class TypeSelector extends JComboBox<Item> {
    private static final Logger LOGGER = Logger.getLogger(TypeSelector.class.getName());
    /**
     * The model containing the list of names to select from
     */
    private DefaultComboBoxModel<Item> comboBoxModel;

    public TypeSelector() {

        comboBoxModel = new DefaultComboBoxModel<>(new Vector<>());
    }

    public void updateClassType(String classTypeName) {
        java.util.List<String> classesLongNames;
        ArrayList<Class<?>> instances = new ArrayList<>(5);
        classesLongNames = GenericObjectEditor.getClassesFromProperties(classTypeName, instances);
        LOGGER.finest("Selected type for OptimizationEditorPanel: " + classTypeName);
        if (classesLongNames.size() > 1) {
            Vector<Item> classesList = new Vector<>();
            String[] toolTips = collectComboToolTips(instances, 100);
            int i = 0;
            for (String className : classesLongNames) {
                String displayName = StringTools.cutClassName(className);

                classesList.add(new Item(className, displayName, toolTips[i++]));
            }
            comboBoxModel = new DefaultComboBoxModel<>(classesList);
            this.setModel(comboBoxModel);
            this.setRenderer(new ToolTipComboBoxRenderer());
        }
    }

    private String[] collectComboToolTips(List<Class<?>> instances, int maxLen) {
        String[] tips = new String[instances.size()];

        for (int i = 0; i < tips.length; i++) {
            tips[i] = null;

            String tip = null;

            Description description = instances.get(i).getAnnotation(Description.class);
            if (description != null) {
                tip = description.value();
            }

            if (tip != null) {
                if (tip.length() <= maxLen) {
                    tips[i] = tip;
                } else {
                    tips[i] = tip.substring(0, maxLen - 2) + "..";
                }
            }
        }
        return tips;
    }
}


class ToolTipComboBoxRenderer extends BasicComboBoxRenderer {

    private static final long serialVersionUID = -5781643352198561208L;

    public ToolTipComboBoxRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);

        if (value != null) {
            Item item = (Item)value;
            setText(item.getDisplayName());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                list.setToolTipText(item.getDescription());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
        }

        if (index == -1) {
            Item item = (Item)value;
            setText(item.getDisplayName());
        }

        setFont(list.getFont());
        return this;
    }
}

class Item
{
    private String id;
    private String displayName;
    private String description;

    public Item(String id, String displayName, String description)
    {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString()
    {
        return id;
    }
}
