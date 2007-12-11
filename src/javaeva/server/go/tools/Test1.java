package javaeva.server.go.tools;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.01.2004
 * Time: 13:08:13
 * To change this template use Options | File Templates.
 */
public class Test1 implements InterfaceTest, Serializable {
    private double m_Var    = 1.0;

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Test1";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Test1.";
    }
    public void setVar(double v) {
        this.m_Var = v;
    }
    public double getVar() {
        return this.m_Var;
    }
    public String varTipText() {
        return "Test1";
    }


}
