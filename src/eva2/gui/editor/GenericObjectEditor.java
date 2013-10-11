package eva2.gui.editor;

import eva2.EvAInfo;
import eva2.gui.OptimizationEditorPanel;
import eva2.tools.ReflectPackage;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericObjectEditor implements PropertyEditor {

    private static final Logger logger = Logger.getLogger(GenericObjectEditor.class.getName());
    private Object m_Object;
    private Object m_Backup;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Class<?> classType;
    private OptimizationEditorPanel editorComponent;
    private boolean isEnabled = true;

    /**
     * Read the classes available for user selection from the properties or the classpath
     * respectively
     */
    public static ArrayList<String> getClassesFromProperties(String className, ArrayList<Class<?>> instances) {
        logger.log(Level.FINEST, "Requesting className: {0}", className);

        // Try to read the predefined classes from the props file.
        String typeOptions = EvAInfo.getProperty(className);
        if (typeOptions == null) {
            // If none are defined, all assignable classes are searched the hard way, using the ReflectPackage 
            return getClassesFromClassPath(className, instances);
        } else {
            StringTokenizer st = new StringTokenizer(typeOptions, ", ");
            ArrayList<String> classes = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String current = st.nextToken().trim();
                try {
                    Class<?> clz = Class.forName(current); // test for instantiability
                    if (instances != null) {
                        instances.add(clz);
                    }
                    classes.add(current);
                } catch (ClassNotFoundException ex) {
                    logger.log(Level.WARNING,
                            String.format("Requesting className: %1$s, Couldn't load: %2%s", className, current), ex);
                }
            }
            return classes;
        }
    }

    /**
     * Return the names of all classes in the same package that are assignable from the named class,
     * and that can be loaded through the classpath. If a class has a declared field called
     * "hideFromGOE" this method will skip it. Abstract classes and interfaces will be skipped as
     * well.
     *
     * @param className
     * @return
     * @see ReflectPackage.getAssignableClassesInPackage
     */
    public static ArrayList<String> getClassesFromClassPath(String className, ArrayList<Class<?>> instances) {
        ArrayList<String> classes = new ArrayList<String>();
        Class<?>[] classArray;
        classArray = ReflectPackage.getAssignableClasses(className, true, true);
        if (classArray == null) {
            logger.log(Level.WARNING, String.format("No assignable classes found in property file or on classpath: %1$s for %2$s", EvAInfo.propertyFile, className));
            classes.add(className);
        } else {
            for (Class<?> clazz : classArray) {
                int m = clazz.getModifiers();
                try {
                    // a field allowing a class to indicate it doesnt want to be displayed
                    Field f = clazz.getDeclaredField("hideFromGOE");
                    if (f.getBoolean(clazz) == true) {
                        logger.log(Level.FINEST, "Class {0} wants to be hidden from GOE.", clazz);
                        continue;
                    }
                } catch (NoSuchFieldException e) {
                    /*
                     * We are just logging this exception here. It is expected that most classes do
                     * not have this field.
                     */
                    logger.log(Level.FINER, String.format("%1$s does not have a hideFromGOE field", clazz.toString()), e);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.FINER, e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.log(Level.FINER, e.getMessage(), e);
                }


                if (!Modifier.isAbstract(m) && !clazz.isInterface()) {    // dont take abstract classes or interfaces
                    try {
                        Class<?>[] params = new Class[0];
                        clazz.getConstructor(params);
                        if (instances != null) {
                            instances.add(clazz);
                        }
                        classes.add(clazz.getName());
                    } catch (NoSuchMethodException e) {
                        logger.log(Level.WARNING, String.format("GOE warning: Class %1$s has no default constructor", clazz.getName()), e);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Hide or show the editable property of a class, this makes sense for classes which are
     * represented visually using the GenericObjectEditor. Returns false, if an error occurs, else
     * true. An instance may call this statically on itself by means of this.getClass(). Actually
     * this only sets the hidden property of the java bean which is checked in the wasModified
     * method of PropertySheetPanel.
     *
     * @param cls      class the property belongs to
     * @param property string name of the property
     * @param hide     desired value to set, true for hidden, false for visible
     * @return false, if an error occurs, else true
     */
    public static boolean setExpertProperty(Class<?> cls, String property, boolean expertValue) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(cls);
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if ((prop.getName().equals(property))) {
                    if (expertValue != prop.isExpert()) {
                        prop.setExpert(expertValue);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("Couldn't set expert property for %1$s/%2$s", cls.getName(), property), e);
            return false;
        }
    }

    /**
     * Hide or show the editable property of a class, this makes sense for classes which are
     * represented visually using the GenericObjectEditor. Returns false, if an error occurs, else
     * true. An instance may call this statically on itself by means of this.getClass(). Actually
     * this only sets the hidden property of the java bean which is checked in the wasModified
     * method of PropertySheetPanel.
     *
     * @param cls      class the property belongs to
     * @param property string name of the property
     * @param hide     desired value to set, true for hidden, false for visible
     * @return false, if an error occurs, else true
     */
    public static boolean setHideProperty(Class<?> cls, String property, boolean hide) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(cls);
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if ((prop.getName().equals(property))) {
                    if (hide != prop.isHidden()) {
                        prop.setHidden(hide);
                    }
                    return true;
                }
            }

            logger.log(Level.WARNING, "Property {0} not found", property);
            return false;
        } catch (IntrospectionException e) {
            logger.log(Level.WARNING, String.format("Couldn't set hide property for %1$s/%2$s", cls.getName(), property), e);
            return false;
        }
    }

    /**
     * Hide or unhide all properties of a given class. Added to avoid the problem with hidden
     * properties of inherited classes hide the property for all classes within the same inheritance
     * tree.
     *
     * @param cls
     * @param hide
     * @return the original hidden states or null if an error occurred.
     */
    public static boolean[] setHideAllProperties(Class<?> cls, boolean hide) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(cls);
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            boolean[] orig = new boolean[props.length];
            for (int i = 0; i < props.length; i++) {
                orig[i] = props[i].isHidden();
                props[i].setHidden(hide);
            }
            return orig;
        } catch (IntrospectionException e) {
            logger.log(Level.WARNING, String.format("Couldn't hide all properties for %1$s/all", cls.getName()), e);
            return null;
        }
    }

    public static void setHideProperties(Class<?> cls, boolean[] hideStates) {
        if (hideStates != null) {
            BeanInfo bi;
            try {
                bi = Introspector.getBeanInfo(cls);
            } catch (IntrospectionException e) {
                logger.log(Level.WARNING, String.format("Error on introspection of %1$s", cls.getName()), e);
                return;
            }
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            if (hideStates.length == props.length) {
                for (int i = 0; i < props.length; i++) {
                    props[i].setHidden(hideStates[i]);
                }
            } else {
                System.err.println("Error, mismatching length of hide state array in GenericObjectEditor.setHideProperites");
            }
        }
    }

    /**
     * Convenience-method. See setHideProperty.
     *
     * @param cls
     * @param property
     * @param show
     * @return
     */
    public static boolean setShowProperty(Class<?> cls, String property, boolean show) {
        return GenericObjectEditor.setHideProperty(cls, property, !show);
    }

    /**
     * Sets whether the editor is "enabled", meaning that the current values will be painted.
     *
     * @param newVal a value of type 'boolean'
     */
    public void setEnabled(boolean newVal) {
        if (newVal != isEnabled) {
            isEnabled = newVal;
        }
    }

    /**
     * Sets the class of values that can be edited.
     *
     * @param type a value of type 'Class'
     */
    public void setClassType(Class<?> type) {
        classType = type;
        if (editorComponent != null) {
            editorComponent.updateClassType();
        }

    }

    public Class<?> getClassType() {
        return classType;
    }

    /**
     * Sets the current object to be the default, taken as the first item in the chooser
     */
    public void setDefaultValue() {
        if (classType == null) {
            logger.log(Level.WARNING, "No ClassType set up for GenericObjectEditor!");
            return;
        }

        Vector<String> v = null;
        v = new Vector<String>(getClassesFromProperties(classType.getName(), null));

        try {
            if (v.size() > 0) {
                setObject((Object) Class.forName((String) v.get(0)).newInstance());
            }
        } catch (Exception ex) {
            System.err.println("Exception in setDefaultValue !!!" + ex.getMessage());
        }
    }

    /**
     * Sets the current Object. If the Object is in the Object chooser, this becomes the selected
     * item (and added to the chooser if necessary).
     *
     * @param o an object that must be a Object.
     */
    @Override
    public void setValue(Object o) {

        if (o == null || classType == null) {
            logger.log(Level.WARNING, "No ClassType set up for GenericObjectEditor!");
            return;
        }
        if (!classType.isAssignableFrom(o.getClass())) {
            if (classType.isPrimitive()) {
                System.err.println("setValue object not of correct type! Expected " + classType.getName() + ", got " + o.getClass().getName());
                System.err.println("setting primitive type");
                setObject(o);
            } else {
                System.err.println("setValue object not of correct type! Expected " + classType.getName() + ", got " + o.getClass().getName());
            }
            return;
        }

        setObject(o);
        if (editorComponent != null) {
            editorComponent.updateChooser();
        }

    }

    /**
     * Sets the current Object, but doesn't worry about updating the state of the Object chooser.
     *
     * @param c a value of type 'Object'
     */
    private void setObject(Object c) {
        // This should really call equals() for comparison.
        boolean trueChange = (c != getValue());

        m_Backup = m_Object;
        m_Object = c;

        if (editorComponent != null) {
            editorComponent.updateChildPropertySheet();
            if (trueChange) {
                propertyChangeSupport.firePropertyChange("", m_Backup, m_Object);
            }
        }
    }

    /**
     * Gets the current Object.
     *
     * @return the current Object
     */
    @Override
    public Object getValue() {
        return m_Object;
    }

    /**
     * Supposedly returns an initialization string to create a Object identical to the current one,
     * including it's state, but this doesn't appear possible given that the initialization string
     * isn't supposed to contain multiple statements.
     *
     * @return the java source code initialization string
     */
    @Override
    public String getJavaInitializationString() {
        return "new " + m_Object.getClass().getName() + "()";
    }

    /**
     * Returns true to indicate that we can paint a representation of the Object.
     *
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints a representation of the current Object.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        if (isEnabled && m_Object != null) {
            int getNameMethod = -1;
            MethodDescriptor[] methods;
            String rep = "";
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(m_Object.getClass());
                methods = beanInfo.getMethodDescriptors();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equalsIgnoreCase("getName")) {
                        getNameMethod = i;
                    }
                }
            } catch (IntrospectionException ex) {
                System.err.println("PropertySheetPanel.setTarget(): Couldn't introspect");
                return;
            }
            if (getNameMethod >= 0) {
                try {
                    rep = (String) methods[getNameMethod].getMethod().invoke(m_Object, (Object[]) null);
                } catch (java.lang.IllegalAccessException e1) {
                } catch (java.lang.reflect.InvocationTargetException e2) {
                }
            }
            if (rep.length() <= 0) {
                rep = m_Object.getClass().getName();
                int dotPos = rep.lastIndexOf('.');
                if (dotPos != -1) {
                    rep = rep.substring(dotPos + 1);
                }
            }
            FontMetrics fm = gfx.getFontMetrics();
            int vpad = (box.height - fm.getHeight()) / 2;
            gfx.drawString(rep, 2, fm.getHeight() + vpad - 2);
        }
    }

    /**
     * Returns null as we don't support getting/setting values as text.
     *
     * @return null
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     * Returns null as we don't support getting/setting values as text.
     *
     * @param text the text value
     * @throws IllegalArgumentException as we don't support getting/setting values as text.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     * Returns null as we don't support getting values as tags.
     *
     * @return null
     */
    @Override
    public String[] getTags() {
        return null;
    }

    /**
     * Returns true because we do support a custom editor.
     *
     * @return true
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns the array editing component.
     *
     * @return a value of type 'java.awt.Component'
     */
    @Override
    public Component getCustomEditor() {
        if (editorComponent == null) {
            editorComponent = new OptimizationEditorPanel(m_Object, m_Backup, propertyChangeSupport, this);
        }
        return editorComponent;
    }

    /**
     *
     */
    public void disableOKCancel() {
        if (editorComponent == null) {
            editorComponent = new OptimizationEditorPanel(m_Object, m_Backup,
                    propertyChangeSupport, this);
        }
        editorComponent.setEnabledOkCancelButtons(false);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
