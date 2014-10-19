package eva2.gui.editor;

import eva2.gui.PropertyValueSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditorSupport;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public class EnumEditor extends PropertyEditorSupport {
    /**
     * The Enum values that may be chosen
     */
    private Enum[] enumConstants;

    @Override
    public String getAsText() {
        return getValue().toString();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Enum) {
            enumConstants = ((Enum) value).getClass().getEnumConstants();
//			enumType = ((Enum)value);
            super.setValue(value);
        } else if (value.getClass().isArray() && value.getClass().getComponentType().isEnum()) {
//			values = value.getClass().getComponentType().getEnumConstants();
            Enum<?>[] e = (Enum[]) (value);
            enumConstants = (Enum[]) e.getClass().getComponentType().getEnumConstants();
            super.setValue(value);
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        for (int i = 0; i < enumConstants.length; i++) {
            if (text.equals(enumConstants[i].toString())) {
                setValue(enumConstants[i]);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid text for enum");
    }

    @Override
    public String[] getTags() {
        if (getValue() == null) {
            return null;
        }
        String[] tags = new String[enumConstants.length];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = enumConstants[i].toString();
        }
        return tags;
    }

    /**
     * Test the editor.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            Enum<?> initial = TestEnum.asdf;
            EnumEditor ed = new EnumEditor();
            ed.setValue(initial);
            PropertyValueSelector ps = new PropertyValueSelector(ed);
            JFrame f = new JFrame();
            f.addWindowListener(new WindowAdapter() {
                @Override
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

enum TestEnum {asdf, sdf, asdfa}