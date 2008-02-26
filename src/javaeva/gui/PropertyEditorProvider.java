package javaeva.gui;

import java.awt.Color;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.individuals.codings.gp.GPArea;
import javaeva.tools.SelectedTag;

import sun.beans.editors.DoubleEditor;
import sun.beans.editors.IntEditor;
import sun.beans.editors.BoolEditor;
import sun.beans.editors.ByteEditor;
import sun.beans.editors.ColorEditor;	
import sun.beans.editors.ShortEditor;
import sun.beans.editors.FloatEditor;	
import sun.beans.editors.LongEditor;
import sun.beans.editors.StringEditor;

public class PropertyEditorProvider {
    final static boolean TRACE = false;
    // if true, we use the GenericObjectEditor whenever no specific one is registered, so keep it true
    // unless you want to register every single possibility.
    public static boolean useDefaultGOE = true; 
    
    public static PropertyEditor findEditor(Class cls) {
    	PropertyEditor  editor  = null;
        editor = PropertyEditorManager.findEditor(cls);

		if (TRACE)  System.out.println((editor == null ) ? "No editor from PEM" : ("Found " + editor.getClass()));
        if ((editor == null) && useDefaultGOE ) {
        	editor = new GenericObjectEditor();
        	if (TRACE) System.out.println("using GOE");
        }
        return editor;
    }
    
    
    /**

    private PropertyEditor makeEditor(PropertyDescriptor prop, Object value) {
    	Class type = prop.getPropertyType();
    	Class pec = prop.getPropertyEditorClass();
    	
    	PropertyEditor  editor  = null;
    	//Class           pec     = m_Properties[i].getPropertyEditorClass();
	    if (pec != null) {
	        try {
	            editor = (PropertyEditor)pec.newInstance();
	        } catch (Exception ex) {
	        }
	    }
	    if (editor == null) {
	        if (TRACE) System.out.println("PropertySheetPanel.setTarget(): No editor from pec.");
	        if (STREICHE) {
	            //@todo Streiche: Here i'm looking for a specialized editor
	            //if (TRACE) System.out.println("PropertySheetPanel.setTarget(): trying to find specialised editor for "+value.getClass()+".");
	            if (value != null) editor = PropertyEditorManager.findEditor(value.getClass());
	            if (TRACE) {
	                if (editor == null) System.out.println("PropertySheetPanel.setTarget(): Found no editor.");
	                else System.out.println("PropertySheetPanel.setTarget(): Found " + editor.getClass()+".");
	            }
	            if (editor == null) editor = PropertyEditorManager.findEditor(type);
	        } else {
	            editor = PropertyEditorManager.findEditor(type);
	        }
	    }
	    if ((TRACE) && (editor != null)) System.out.println("PropertySheetPanel.setTarget(): editor="+editor.getClass().getName());
	    if (editor == null) {
	        // If it's a user-defined property we give a warning.
	        String getterClass = prop.getReadMethod().getDeclaringClass().getName();
	        if (getterClass.indexOf("java.") != 0) {
	            System.err.println("Warning: Can't find public property editor"
	                + " for property \"" + prop.getDisplayName() + "\" (class \""
	                + type.getName() + "\").  Skipping.");
	        }
	    } else if (editor instanceof GenericObjectEditor) ((GenericObjectEditor) editor).setClassType(type);

	    return editor;
    }
     */
    /**
     * 
     * @param prop
     * @param value
     * @return
     */
    
    public static PropertyEditor findEditor(PropertyDescriptor prop, Object value) {
    	PropertyEditor  editor  = null;
    	Class pec = prop.getPropertyEditorClass();
    	Class type = prop.getPropertyType();

    	try {
    		if (pec != null) editor = (PropertyEditor)pec.newInstance();
    	} catch (Exception e) {
    		editor = null;
    	}

    	if (editor == null) {
	        if (TRACE) System.out.println("PropertySheetPanel.makeEditor(): No editor from PEC.");

	        //@todo Streiche: Here i'm looking for a specialized editor
	        //if (TRACE) System.out.println("PropertySheetPanel.setTarget(): trying to find specialised editor for "+value.getClass()+".");
	        if (value != null) editor = PropertyEditorManager.findEditor(value.getClass());
	        if (TRACE)  System.out.println((editor == null ) ? "No editor from PEM" : ("Found " + editor.getClass()));
	        
	        if (editor == null) editor = PropertyEditorManager.findEditor(type);
	        if (TRACE)  System.out.println((editor == null ) ? "No editor from PEM by type" : ("Found " + editor.getClass()));
	        if ((editor == null) && useDefaultGOE ) {
	        	editor = new GenericObjectEditor();
	        	if (TRACE) System.out.println("using GOE");
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
//	    	hier erst noch das object setzen?
//	    	((GenericObjectEditor) editor).getCustomEditor();
	    	((GenericObjectEditor) editor).setClassType(type);
	    }

	    return editor;
    }
    
    /**
     */
    public static void installEditors() {
        PropertyEditorManager.registerEditor(SelectedTag.class, TagEditor.class);
        PropertyEditorManager.registerEditor(double[].class, GenericArrayEditor.class);
        PropertyEditorManager.registerEditor(InterfaceTerminator[].class, GenericArrayEditor.class);
        
        PropertyEditorManager.registerEditor(Double.class, DoubleEditor.class);
        PropertyEditorManager.registerEditor(Integer.class, IntEditor.class);
        PropertyEditorManager.registerEditor(Boolean.class, BoolEditor.class);
        PropertyEditorManager.registerEditor(byte.class, ByteEditor.class);
        PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);
        PropertyEditorManager.registerEditor(short.class, ShortEditor.class);
        PropertyEditorManager.registerEditor(float.class, FloatEditor.class);
        PropertyEditorManager.registerEditor(long.class, LongEditor.class);
        PropertyEditorManager.registerEditor(String.class, StringEditor.class);
        
        // The Editor for the new GO
        
//            // Traveling Salesman problem
        PropertyEditorManager.registerEditor(GPArea.class                       , GenericAreaEditor.class);
        PropertyEditorManager.registerEditor(PropertyDoubleArray.class          , GenericDoubleArrayEditor.class);
        PropertyEditorManager.registerEditor(PropertyIntArray.class             , GenericIntArrayEditor.class);
        PropertyEditorManager.registerEditor(PropertyEpsilonThreshold.class     , GenericEpsilonThresholdEditor.class);
        PropertyEditorManager.registerEditor(PropertyEpsilonConstraint.class    , GenericEpsilonConstraintEditor.class);
        PropertyEditorManager.registerEditor(PropertyWeightedLPTchebycheff.class, GenericWeigthedLPTchebycheffEditor.class);
        PropertyEditorManager.registerEditor(PropertyStringList.class           , GenericStringListEditor.class);
        PropertyEditorManager.registerEditor(PropertyFilePath.class             , GenericFilePathEditor.class);
        PropertyEditorManager.registerEditor(PropertyRemoteServers.class        , GenericRemoteServersEditor.class);
        PropertyEditorManager.registerEditor(PropertyOptimizationObjectives.class  , GenericOptimizationObjectivesEditor.class);
        PropertyEditorManager.registerEditor(PropertyOptimizationObjectivesWithParam.class  , GenericOptimizationObjectivesWithParamEditor.class);
        PropertyEditorManager.registerEditor(javaeva.gui.MultiLineString.class, javaeva.gui.MultiLineStringEditor.class);
    }
}
