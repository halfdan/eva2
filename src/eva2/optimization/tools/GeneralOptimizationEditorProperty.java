package eva2.optimization.tools;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import javax.swing.*;

/**
 * Representation of an OptimizationEditor Property
 */
public class GeneralOptimizationEditorProperty {
    public Method m_getMethod;
    public Method m_setMethod;
    public PropertyEditor m_Editor;
    public Object m_Value;
    public JComponent m_View;
    public JComponent m_ViewWrapper;
    public JLabel m_Label;
    public Class m_PropertyType;
    public String m_Name;
    public String m_TipText;

    public GeneralOptimizationEditorProperty() {
    }
}
