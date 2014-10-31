package eva2.optimization.tools;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;

/**
 * Representation of an OptimizationEditor Property
 */
public class GeneralOptimizationEditorProperty {
    public Method getMethod;
    public Method setMethod;
    public PropertyEditor editor;
    public Object value;
    public JComponent view;
    public JComponent viewWrapper;
    public JLabel label;
    public Class propertyType;
    public String name;
    public String tipText;

    public GeneralOptimizationEditorProperty() {
    }
}
