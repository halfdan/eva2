package eva2.gui.editor;

import java.beans.PropertyEditorSupport;

/**
 *
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
            super.setValue(value);
        } else if (value.getClass().isArray() && value.getClass().getComponentType().isEnum()) {
            Enum<?>[] e = (Enum[]) (value);
            enumConstants = (Enum[]) e.getClass().getComponentType().getEnumConstants();
            super.setValue(value);
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        for (Enum enumConstant : enumConstants) {
            if (text.equals(enumConstant.toString())) {
                setValue(enumConstant);
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
}