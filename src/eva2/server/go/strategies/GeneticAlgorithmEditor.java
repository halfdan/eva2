package eva2.server.go.strategies;
//package javaeva.server.oa.go.Strategies;
//
//import javaeva.server.oa.go.Tools.AbstractObjectEditor;
//import javaeva.server.oa.go.Tools.GeneralGenericObjectEditorPanel;
//import javaeva.server.oa.go.Tools.GeneralGOEProperty;
//import javaeva.client.EvAClient;
//import javaeva.gui.*;
//import javaeva.tools.CompileAndLoad;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionListener;
//import java.awt.event.ActionEvent;
//import java.util.ArrayList;
//import java.util.Vector;
//import java.util.StringTokenizer;
//import java.util.Enumeration;
//import java.beans.*;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
///** Again a generic editor for the GA this time, actually this
// * can be a lot of work and in case of only few and simple
// * parameters the GenericObjectEditor is much more convenient.
// * Created by IntelliJ IDEA.
// * User: streiche
// * Date: 24.08.2004
// * Time: 15:06:00
// * To change this template use File | Settings | File Templates.
// */
//public class GeneticAlgorithmEditor extends AbstractObjectEditor {
//
//    /********************************* java.beans.PropertyChangeListener *************************/
//    public void addPropertyChangeListener(PropertyChangeListener l) {
//        m_Support.addPropertyChangeListener(l);
//        if (this.m_EditorComponent != null) this.m_EditorComponent.addPropertyChangeListener(l);
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener l) {
//        m_Support.removePropertyChangeListener(l);
//    }
//    /** This will wait for the GenericObjectEditor to finish
//     * editing an object.
//     * @param evt
//     */
//     public void propertyChange(PropertyChangeEvent evt) {
//        this.updateCenterComponent(evt); // Let our panel update before guys downstream
//        m_Support.firePropertyChange("", m_Backup, m_Object);
//    }
//    /********************************* PropertyEditor *************************/
//   /** Returns true since the Object can be shown
//     * @return true
//     */
//    public boolean isPaintable() {
//       return true;
//   }
//
//    /** Paints a representation of the current classifier.
//     * @param gfx the graphics context to use
//     * @param box the area we are allowed to paint into
//     */
//     public void paintValue(Graphics gfx, Rectangle box) {
//        if (m_Object != null) {
//            String  rep     = m_Object.getClass().getName();
//            rep = "Genetic Algorithms";
//            int     dotPos  = rep.lastIndexOf('.');
//            if (dotPos != -1) rep = rep.substring(dotPos + 1);
//            FontMetrics fm = gfx.getFontMetrics();
//            int vpad = (box.height - fm.getHeight()) / 2;
//            gfx.drawString(rep, 2, fm.getHeight() + vpad -2 );
//        } else {
//        }
//    }
//
//    /** Returns true because we do support a custom editor.
//    * @return true
//    */
//    public boolean supportsCustomEditor() {
//        return true;
//    }
//
//    /** Returns the array editing component.
//    * @return a value of type 'java.awt.Component'
//    */
//    public Component getCustomEditor() {
//        if (m_EditorComponent == null) {
//            m_EditorComponent = new GeneralGenericObjectEditorPanel(this);
//        }
//        return m_EditorComponent;
//    }
//
//    public String getAsText() {
//        return null;
//    }
//    public void setAsText(String text) throws IllegalArgumentException {
//        throw new IllegalArgumentException(text);
//    }
//    public String getJavaInitializationString() {
//        return "new " + m_Object.getClass().getName() + "()";
//    }
//    public String[] getTags() {
//        return null;
//    }
//
//    /********************************* AbstractObjectEditor *************************/
//    /** This method will make a back up of the current
//     * object value
//     */
//    public void makeBackup() {
//        //@todo
//    }
//
//    /** This method will use the backup to undo the
//     * last action.
//     */
//    public void undoBackup() {
//        //@todo
//    }
//
//    /** This method allows you to set the current value
//     * @param obj   The new value
//     */
//    public void setValue(Object obj) {
//        if (obj instanceof GeneticAlgorithm) {
//            this.m_Object = (GeneticAlgorithm)obj;
//        } else {
////            System.out.println("No Genetic Algorithm!");
//            this.firePropertyChange("Object Type Changed", this.m_Object, obj);
//        }
//    }
//
//    /** This method returns the class type
//     * @return Class
//     */
//    public Class getClassType() {
//        return this.m_Object.getClass();
//    }
//
//    /** This method gets the classes from properties
//     * @return Vector
//     */
//    public ArrayList<String> getClassesFromProperties() {
//    	return GenericObjectEditor.getClassesFromProperties("javaeva.server.oa.go.Strategies.InterfaceOptimizer");
//    }
//
//    /** This method return the global info on the current
//     * object.
//     * @return The global info.
//     */
//    public String getGlobalInfo() {
//        return ((GeneticAlgorithm)this.m_Object).globalInfo();
//    }
//
//    /** This method returns the central editing panel for the current
//     * object.
//     * @return The center component.
//     */
//    public JComponent getCenterComponent() {
//        JPanel              result      = new JPanel();
//        JLabel              tmpLabel;
//        PropertyDescriptor  m_Properties[];
//        MethodDescriptor    m_Methods[];
//        GeneralGOEProperty  editor;
//        GridBagLayout       gbLayout    = new GridBagLayout();
//        GridBagConstraints  gbConst     = new GridBagConstraints();
//        try {
//            BeanInfo    bi  = Introspector.getBeanInfo(this.m_Object.getClass());
//            m_Properties    = bi.getPropertyDescriptors();
//            m_Methods       = bi.getMethodDescriptors();
//        } catch (IntrospectionException ex) {
//            System.out.println("PropertySheetPanel: Couldn't introspect");
//            return result;
//        }
//        // first sort the stuff
//        JPanel centerPanel, tmpPanel, selectionPanel;
//        centerPanel = new JPanel();
//        centerPanel.setLayout(gbLayout);
//
//        // population
//        tmpPanel = new JPanel();
//        tmpPanel.setLayout(new GridLayout(0,2));
//        editor = this.getEditorFor("population", m_Properties, m_Methods, this.m_Object);
//            editor.m_Editor.removePropertyChangeListener(this);
//            editor.m_Editor.addPropertyChangeListener(this);
//        editor.m_View.repaint();
//        tmpLabel    = new JLabel("Population:");
//        tmpLabel.setToolTipText(editor.m_TipText);
//        tmpPanel.add(tmpLabel);
//        editor.m_View.setToolTipText(editor.m_TipText);
//        editor.m_ViewWrapper = new JPanel();
//        editor.m_ViewWrapper.setLayout(new BorderLayout());
//        editor.m_ViewWrapper.add(editor.m_View, BorderLayout.CENTER);
//        editor.m_ViewWrapper.setToolTipText(editor.m_TipText);
//        tmpPanel.add(editor.m_ViewWrapper);
//        tmpPanel.setToolTipText(editor.m_TipText);
//        gbConst           = new GridBagConstraints();
//            gbConst.anchor    = GridBagConstraints.WEST;
//            gbConst.fill      = GridBagConstraints.BOTH;
//            gbConst.gridy     = 0;
//            gbConst.gridx     = 0;
//            gbConst.weightx   = 100;
//            gbLayout.setConstraints(tmpPanel, gbConst);
//        centerPanel.add(tmpPanel);
//        this.m_Editors.put("Population", editor);
//
//        // elitism
//        tmpPanel = new JPanel();
//        tmpPanel.setLayout(new GridLayout(0,2));
//        tmpLabel = new JLabel("Elitism:");
//        tmpLabel.setToolTipText(this.getToolTipText("elitism", m_Methods, m_Object));
//        tmpPanel.add(tmpLabel);
//        JCheckBox elitism = new JCheckBox();
//        elitism.setSelected(((GeneticAlgorithm)this.m_Object).getElitism());
//        elitism.addActionListener(new ActionListener() {
//	        public void actionPerformed(ActionEvent e) {
//                ((GeneticAlgorithm)m_Object).setElitism(((JCheckBox)e.getSource()).isSelected());
//                firePropertyChange("", m_Backup, m_Object);
////	            if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
////	                Window w = (Window) getTopLevelAncestor();
////	                w.dispose();
////	            }
//	        }
//        });
//        tmpPanel.add(elitism);
//        gbConst           = new GridBagConstraints();
//            gbConst.anchor    = GridBagConstraints.WEST;
//            gbConst.fill      = GridBagConstraints.BOTH;
//            gbConst.gridy     = 1;
//            gbConst.gridx     = 0;
//            gbConst.weightx   = 100;
//            gbLayout.setConstraints(tmpPanel, gbConst);
//        centerPanel.add(tmpPanel);
//
//        // selection
//        selectionPanel = new JPanel();
//        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
//		    BorderFactory.createTitledBorder("Mating Selection"),
//			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
//	    selectionPanel.setLayout(new GridLayout(3, 2));
//
//        editor = this.getEditorFor("ParentSelection", m_Properties, m_Methods, this.m_Object);
//            editor.m_Editor.removePropertyChangeListener(this);
//            editor.m_Editor.addPropertyChangeListener(this);
//        editor.m_View.repaint();
//        this.m_Editors.put("ParentSelection", editor);
//        tmpLabel = new JLabel("Selection for Parents:");
//        tmpLabel.setToolTipText(editor.m_TipText);
//        selectionPanel.add(tmpLabel);
//        editor.m_View.setToolTipText(editor.m_TipText);
//        editor.m_ViewWrapper = new JPanel();
//        editor.m_ViewWrapper.setLayout(new BorderLayout());
//        editor.m_ViewWrapper.add(editor.m_View, BorderLayout.CENTER);
//        editor.m_ViewWrapper.setToolTipText(editor.m_TipText);
//        selectionPanel.add(editor.m_ViewWrapper);
//
//        editor = this.getEditorFor("PartnerSelection", m_Properties, m_Methods, this.m_Object);
//            editor.m_Editor.removePropertyChangeListener(this);
//            editor.m_Editor.addPropertyChangeListener(this);
//        editor.m_View.repaint();
//        this.m_Editors.put("PartnerSelection", editor);
//        tmpLabel = new JLabel("Selection for Partners:");
//        tmpLabel.setToolTipText(editor.m_TipText);
//        selectionPanel.add(tmpLabel);
//        editor.m_View.setToolTipText(editor.m_TipText);
//        editor.m_ViewWrapper = new JPanel();
//        editor.m_ViewWrapper.setLayout(new BorderLayout());
//        editor.m_ViewWrapper.add(editor.m_View, BorderLayout.CENTER);
//        editor.m_ViewWrapper.setToolTipText(editor.m_TipText);
//        selectionPanel.add(editor.m_ViewWrapper);
//
//        editor = this.getEditorFor("NumberOfPartners", m_Properties, m_Methods, this.m_Object);
//            editor.m_Editor.removePropertyChangeListener(this);
//            editor.m_Editor.addPropertyChangeListener(this);
//        editor.m_View.repaint();
//        this.m_Editors.put("NumberOfPartners", editor);
//        tmpLabel = new JLabel("Number of Partners:");
//        tmpLabel.setToolTipText(editor.m_TipText);
//        selectionPanel.add(tmpLabel);
//        editor.m_View.setToolTipText(editor.m_TipText);
//        editor.m_ViewWrapper = new JPanel();
//        editor.m_ViewWrapper.setLayout(new BorderLayout());
//        editor.m_ViewWrapper.add(editor.m_View, BorderLayout.CENTER);
//        editor.m_ViewWrapper.setToolTipText(editor.m_TipText);
//        selectionPanel.add(editor.m_ViewWrapper);
//        gbConst           = new GridBagConstraints();
//            gbConst.anchor    = GridBagConstraints.WEST;
//            gbConst.fill      = GridBagConstraints.BOTH;
//            gbConst.gridy     = 2;
//            gbConst.gridx     = 0;
//            gbConst.weightx   = 100;
//            gbLayout.setConstraints(selectionPanel, gbConst);
//        centerPanel.add(selectionPanel);
//        result.setLayout(new BorderLayout());
//        result.add(centerPanel, BorderLayout.CENTER);
//        return result;
//    }
//
//    /** This method will udate the status of the object taking the values from all
//     * supsequent editors and setting them to my object.
//     */
//    public void updateCenterComponent(PropertyChangeEvent evt) {
//        if (evt.getSource() instanceof PropertyEditor) {
//            PropertyEditor editor = (PropertyEditor) evt.getSource();
//            GeneralGOEProperty  prop;
//            Object          newValue;
//            Enumeration         myEnum = this.m_Editors.elements();
//            while (myEnum.hasMoreElements()) {
//                prop = (GeneralGOEProperty)myEnum.nextElement();
//	            if (prop.m_Editor == editor) {
//                    Object value    = editor.getValue();
//                    Method setter   = prop.m_setMethod;
//                    Method getter   = prop.m_getMethod;
//                    prop.m_Value    = value;
//                    
//                    /////// MK
//                    newValue    = evt.getNewValue();
//                    if (newValue == null) newValue = editor.getValue();
//                    ///////
//                    if (false) { /////// MK I really dont know what this was for TODO
//                        PropertyEditor  tmpEdit     = null;
//                        newValue    = evt.getNewValue();
//                        if (newValue == null) newValue = editor.getValue();
//                        tmpEdit = PropertyEditorProvider.findEditor(newValue.getClass());
//                        if (tmpEdit == null)    tmpEdit = PropertyEditorProvider.findEditor(prop.m_PropertyType);
//                        if (tmpEdit.getClass() != prop.m_Editor.getClass()) { /// MK why should it be different??
//                        	System.err.println("warning: differing editor types (" + tmpEdit.getClass() + " / " + editor.getClass());
//                            value           = newValue;
//                            prop.m_Value    = newValue;
//                            prop.m_Editor   = tmpEdit;
//                            if (tmpEdit instanceof GenericObjectEditor) ((GenericObjectEditor) tmpEdit).setClassType(prop.m_PropertyType);
//                            prop.m_Editor.setValue(newValue);
//                            JComponent NewView = null;
//                            if (tmpEdit instanceof sun.beans.editors.BoolEditor) {
//                                NewView = new PropertyBoolSelector(tmpEdit);
//                            } else {
//                                if (tmpEdit instanceof sun.beans.editors.DoubleEditor) {
//                                    NewView = new PropertyText(tmpEdit);
//                                } else {
//                                    if (tmpEdit.isPaintable() && tmpEdit.supportsCustomEditor()) {
//                                        NewView = new PropertyPanel(tmpEdit);
//                                    } else {
//                                        if (tmpEdit.getTags() != null ) {
//                                            NewView = new PropertyValueSelector(tmpEdit);
//                                        } else {
//                                            if (tmpEdit.getAsText() != null) {
//                                                NewView = new PropertyText(tmpEdit);
//                                            } else {
//                                                System.out.println("Warning: Property \"" + prop.m_Name
//                                                    + "\" has non-displayabale editor.  Skipping.");
//                                                continue;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            prop.m_Editor.addPropertyChangeListener(this);
//                            prop.m_View = NewView;
//                            if (prop.m_TipText != null) prop.m_View.setToolTipText(prop.m_TipText);
//                            prop.m_ViewWrapper.removeAll();
//                            prop.m_ViewWrapper.setLayout(new BorderLayout());
//                            prop.m_ViewWrapper.add(prop.m_View, BorderLayout.CENTER);
//                            prop.m_ViewWrapper.repaint();
//                        }
//                    }
////                    System.out.println("Value: "+value +" / m_Values[i]: " + m_Values[i]);
//                    // Now try to update the target with the new value of the property
//                    // and allow the target to do some changes to the value, therefore
//                    // reread the new value from the target
//	                try {
//	                    Object  args[]  = { value };
//	                    args[0]         = value;
//                        Object  args2[] = { };
//                        // setting the current value to the target object
//	                    setter.invoke(m_Object, args);
//                        // i could also get the new value
//                        //value = getter.invoke(m_Target, args2);
//                        // Now i'm reading the set value from the target to my local values
//                        prop.m_Value = getter.invoke(m_Object, args2);
//
//                        if (value instanceof Integer) {
//                            // This could check whether i have to set the value back to
//                            // the editor, this would allow to check myu and lambda
//                            // why shouldn't i do this for every property!?
////                            System.out.println("value: "+((Integer)value).intValue());
////                            System.out.println(" m_Values[i]: "+ ((Integer) m_Values[i]).intValue());
//                            if (((Integer)value).intValue() != ((Integer) prop.m_Value).intValue()) {
//                                editor.setValue(prop.m_Value);
//                            }
//                        }
//	                } catch (InvocationTargetException ex) {
//	                    if (ex.getTargetException() instanceof PropertyVetoException) {
//	                        System.out.println("PropertySheetPanel.wasModified(): WARNING: Vetoed; reason is: " + ex.getTargetException().getMessage());
//	                    } else {
//	                        System.out.println("PropertySheetPanel.wasModified(): InvocationTargetException while updating " + prop.m_Name);
//                            System.out.println("PropertySheetPanel.wasModified(): "+ex.getMessage());
//                            ex.printStackTrace();
//	                    }
//	                } catch (Exception ex) {
//	                    System.out.println("PropertySheetPanel.wasModified(): Unexpected exception while updating " + prop.m_Name);
//	                }
//                    //revalidate();
//	                if (prop.m_View != null && prop.m_View instanceof PropertyPanel) {
//	                    //System.err.println("Trying to repaint the property canvas");
//	                    prop.m_View.repaint();
//	                    m_EditorComponent.revalidate();
//	                }
//	                break;
//	            } // end if (m_Editors[i] == editor) {
//            } // end for (int i = 0 ; i < m_Editors.length; i++) {
//        } // end if (evt.getSource() instanceof PropertyEditor) {
//
//        // Now re-read all the properties and update the editors
//        // for any other properties that have changed.
//        GeneralGOEProperty  prop;
//        Enumeration         myEnum = this.m_Editors.elements();
//        while (myEnum.hasMoreElements()) {
//            prop = (GeneralGOEProperty)myEnum.nextElement();
//            Object  o;
//            Method  getter = null;
//            try {
//                getter = prop.m_getMethod;
//	            Object args[] = { };
//	            o = getter.invoke(m_Object, args);
//            } catch (Exception ex) {
//	            o = null;
//            }
//            if (o == prop.m_Value) {
//	            // The property is equal to its old value.
//	            continue;
//            }
//            if (o != null && o.equals(prop.m_Value)) {
//	            // The property is equal to its old value.
//	            continue;
//            }
//            prop.m_Value = o;
//            // Make sure we have an editor for this property...
//            if (prop.m_Editor == null) {
//	            continue;
//            }
//            // The property has changed!  Update the editor.
//            prop.m_Editor.removePropertyChangeListener(this);
//            prop.m_Editor.setValue(o);
//            prop.m_Editor.addPropertyChangeListener(this);
//            if (prop.m_View != null) {
//	            //System.out.println("Trying to repaint " + (i + 1));
//	            prop.m_View.repaint();
//            }
//        }
//
//        // Make sure the target bean gets repainted.
//        if (Beans.isInstanceOf(m_Object, Component.class)) {
//            //System.out.println("Beans.getInstanceOf repaint ");
//            ((Component)(Beans.getInstanceOf(m_Object, Component.class))).repaint();
//        }
////        if (evt.getSource() instanceof PropertyEditor) {
////            PropertyEditor      editor = (PropertyEditor) evt.getSource();
////            GeneralGOEProperty  prop;
////            Enumeration myEnum = this.m_Editors.elements();
////            while (myEnum.hasMoreElements()) {
////                prop = (GeneralGOEProperty)myEnum.nextElement();
////	            if (prop.m_Editor == editor) {
////                    prop.m_Value = prop.m_Editor.getValue();;
////	                try {
////	                    Object args[]   = {prop.m_Value};
////                        Object args2[]  = { };
////	                    args[0]         = prop.m_Value;
////	                    prop.m_setMethod.invoke(m_Object, args);
////                        prop.m_Value    = prop.m_getMethod.invoke(m_Object, args2);
////	                } catch (InvocationTargetException ex) {
////	                    if (ex.getTargetException() instanceof PropertyVetoException) {
////	                        System.out.println("WARNING: Vetoed; reason is: " + ex.getTargetException().getMessage());
////	                    } else {
////	                        System.out.println("InvocationTargetException while updating " + prop.m_Name);
////                            System.out.println(ex.getMessage());
////                            ex.printStackTrace();
////	                    }
////	                } catch (Exception ex) {
////	                    System.out.println("Unexpected exception while updating " + prop.m_Name);
////	                }
////	                if (prop.m_View != null && prop.m_View instanceof PropertyPanel) {
////	                    prop.m_View.repaint();
////	                    m_EditorComponent.revalidate();
////	                }
////	                break;
////	            }
////            } // end for editors
////        } // end if
////
////        // Now re-read all the properties and update the editors
////        // for any other properties that have changed.
////        // here i could check for inconsitencys like mu > lambda
////        GeneralGOEProperty  prop;
////        Enumeration         myEnum = this.m_Editors.elements();
////        while (myEnum.hasMoreElements()) {
////            prop = (GeneralGOEProperty)myEnum.nextElement();
////            Object o;
////            try {
////	            Object args[] = { };
////	            o = prop.m_getMethod.invoke(m_Object, args);
////            } catch (Exception ex) {
////	            o = null;
////            }
////            if (o == prop.m_Value) {
////	            // The property is equal to its old value.
////	            continue;
////            }
////            if (o != null && o.equals(prop.m_Value)) {
////	            // The property is equal to its old value.
////	            continue;
////            }
////            prop.m_Value = o;
////            // Make sure we have an editor for this property...
////            if (prop.m_Editor == null) {
////	            continue;
////            }
////            // The property has changed!  Update the editor.
////            prop.m_Editor.removePropertyChangeListener(this);
////            prop.m_Editor.setValue(o);
////            prop.m_Editor.addPropertyChangeListener(this);
////            if (prop.m_View != null) {
////	            prop.m_View.repaint();
////            }
////        }
////
////        if (Beans.isInstanceOf(m_Object, Component.class)) {
////            ((Component)(Beans.getInstanceOf(m_Object, Component.class))).repaint();
////        }
////        this.m_EditorComponent.repaint();
//    }
//}
