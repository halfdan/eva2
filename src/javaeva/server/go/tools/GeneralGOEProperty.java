package javaeva.server.go.tools;

import javax.swing.*;
import java.lang.reflect.Method;
import java.beans.PropertyEditor;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.08.2004
 * Time: 15:21:56
 * To change this template use File | Settings | File Templates.
 */
public class GeneralGOEProperty {
    public Method               m_getMethod;
    public Method               m_setMethod;
    public PropertyEditor       m_Editor;
    public Object               m_Value;
    public JComponent           m_View;
    public JComponent           m_ViewWrapper;
    public JLabel               m_Label;
    public Class                m_PropertyType;
    public String               m_Name;
    public String               m_TipText;

    public GeneralGOEProperty() {
    }
}
