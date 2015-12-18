package eva2.gui;

import eva2.gui.editor.*;
import eva2.optimization.individuals.codings.gp.GPArea;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.tools.Primitives;
import eva2.tools.SelectedTag;
import eva2.tools.StringSelection;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropertyEditorProvider {
    // if true, we use the GenericObjectEditor whenever no specific one is registered, so keep it true
    // unless you want to register every single possibility.
    public static boolean useDefaultGOE = true;

    /**
     * Retrieve an editor object for a given class.
     * This method seems unable to retrieve a primitive editor for obscure reasons.
     * So better use the one based on PropertyDescriptor if possible.
     */
    public static PropertyEditor findEditor(Class<?> cls) {
        PropertyEditor editor = PropertyEditorManager.findEditor(cls);

        // Try to unwrap primitives
        if (editor == null && Primitives.isWrapperType(cls)) {
            editor = PropertyEditorManager.findEditor(Primitives.unwrap(cls));
        }

        if ((editor == null) && useDefaultGOE) {
            if (cls.isArray()) {
                Class<?> unwrapped = Primitives.isWrapperType(cls.getComponentType()) ? Primitives.unwrap(cls.getComponentType()) : cls;
                if (unwrapped.isPrimitive()) {
                    editor = new ArrayEditor();
                } else {
                    editor = new ObjectArrayEditor<>(unwrapped.getComponentType());
                }
            } else if (cls.isEnum()) {
                editor = new EnumEditor();
            } else {
                editor = new GenericObjectEditor();
                ((GenericObjectEditor)editor).setClassType(cls);
            }
        }
        return editor;
    }


    /**
     * @param prop
     * @param value
     * @return
     */
    public static PropertyEditor findEditor(PropertyDescriptor prop, Object value) {

        PropertyEditor editor = null;
        Class pec = prop.getPropertyEditorClass();
        Class type = prop.getPropertyType();

        try {
            if (pec != null) {
                editor = (PropertyEditor) pec.newInstance();
            }
        } catch (Exception e) {
            editor = null;
        }

        if (editor == null) {
            if (value != null) {
                // Try to unwrap primitives
                if (Primitives.isWrapperType(value.getClass())) {
                    editor = PropertyEditorManager.findEditor(Primitives.unwrap(value.getClass()));
                } else {
                    editor = PropertyEditorManager.findEditor(value.getClass());
                }
            }
            if (editor == null && (BeanInspector.isJavaPrimitive(value.getClass()))) {
                Class<?> prim = BeanInspector.getBoxedType(value.getClass());
                if (prim != null) {
                    editor = PropertyEditorManager.findEditor(prim);
                }
                if (editor == null) {

                    prim = BeanInspector.getUnboxedType(value.getClass());
                    if (prim != null) {
                        editor = PropertyEditorManager.findEditor(prim);
                    }
                }
            }

            if (editor == null) {
                editor = PropertyEditorManager.findEditor(type);
            }

            if ((editor == null) && useDefaultGOE) {
                if (type.isArray()) {
                    Class<?> unwrapped = Primitives.isWrapperType(type.getComponentType()) ? Primitives.unwrap(type.getComponentType()) : type;
                    if (unwrapped.isPrimitive()) {
                        editor = new ArrayEditor();
                    } else {
                        editor = new ObjectArrayEditor<>(unwrapped.getComponentType());
                    }
                } else if (type.isEnum()) {
                    editor = new EnumEditor();
                } else {
                    editor = new GenericObjectEditor();
                    ((GenericObjectEditor)editor).setClassType(type);
                }
            }
        }
        if (editor == null) {
            // If it's a user-defined property we give a warning.
            String getterClass = prop.getReadMethod().getDeclaringClass().getName();
            if (getterClass.indexOf("java.") != 0) {
                System.err.println("Warning: Can't find public property editor"
                        + " for property \"" + prop.getDisplayName() + "\" (class \""
                        + type.getName() + "\").  Skipping.");
            }
        } else if (editor instanceof GenericObjectEditor) {
            ((GenericObjectEditor) editor).setClassType(type);
        }
        return editor;
    }

    /**
     */
    public static void installEditors() {
        // First unregister the default EnumEditor provided by sun.*
        PropertyEditorManager.registerEditor(Enum.class, null);

        PropertyEditorManager.registerEditor(SelectedTag.class, TagEditor.class);
        PropertyEditorManager.registerEditor(Enum.class, EnumEditor.class);
        PropertyEditorManager.registerEditor(int[].class, ArrayEditor.class);
        PropertyEditorManager.registerEditor(double[].class, ArrayEditor.class);
        PropertyEditorManager.registerEditor(String[].class, ArrayEditor.class);

        PropertyEditorManager.registerEditor(InterfaceTerminator[].class, ArrayEditor.class);


        // The Editor for the new GO

        PropertyEditorManager.registerEditor(StringSelection.class, StringSelectionEditor.class);
        // Traveling Salesman problem
        PropertyEditorManager.registerEditor(GPArea.class, AreaEditor.class);
        PropertyEditorManager.registerEditor(PropertyDoubleArray.class, DoubleArrayEditor.class);
        PropertyEditorManager.registerEditor(PropertyIntArray.class, IntArrayEditor.class);
        PropertyEditorManager.registerEditor(PropertyEpsilonThreshold.class, EpsilonThresholdEditor.class);
        PropertyEditorManager.registerEditor(PropertyEpsilonConstraint.class, EpsilonConstraintEditor.class);
        PropertyEditorManager.registerEditor(PropertyWeightedLPTchebycheff.class, WeigthedLPTchebycheffEditor.class);
        PropertyEditorManager.registerEditor(PropertyFilePath.class, FilePathEditor.class);
        PropertyEditorManager.registerEditor(PropertyOptimizationObjectives.class, OptimizationObjectivesEditor.class);
        PropertyEditorManager.registerEditor(PropertyOptimizationObjectivesWithParam.class, OptimizationObjectivesWithParamEditor.class);
        PropertyEditorManager.registerEditor(eva2.gui.MultiLineString.class, MultiLineStringEditor.class);
        PropertyEditorManager.registerEditor(PropertySelectableList.class, ArrayEditor.class);
    }
}
